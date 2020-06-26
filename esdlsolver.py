import numpy as np
import time
import math


class Bee:
    def __init__(self, lb, ub, limit, graph, N):
        self.lb = lb
        self.ub = ub
        self.limit = limit
        self.graph = graph
        self.N = N
        self.tried = 0
        self.pos = np.random.uniform(low=lb, high=ub, size=N)
        self.fitness = graph.cost(self.pos)


    def set_pos(self, pos):
        self.pos = np.copy(pos)
        self.fitness = self.graph.cost(self.pos)
        self.tried = 0


    def fit_boundaries(self, pos):
        pos[pos > self.ub] = self.ub
        pos[pos < self.lb] = self.lb


    def check(self):
        if self.tried >= self.limit:
            self.set_pos(np.random.uniform(low=self.lb, high=self.ub, size=self.N))


    def search(self, gbest, m, e, flag):
        new_bee = np.copy(self.pos)
        j, h = np.random.choice(range(self.N), size=2, replace=False)
        phi1 = np.random.uniform(low=-0.5, high=0.5)
        phi2 = np.random.uniform(low=0, high=1)
        if flag:
            new_bee[j] = 0.5 * (m[h] + gbest[j]) + phi1 * (self.pos[h] - e[j]) + phi2 * (self.pos[h] - gbest[j])
        else:
            new_bee[j] = 0.5 * (m[j] + gbest[h]) + phi1 * (self.pos[j] - e[h]) + phi2 * (self.pos[j] - gbest[h])

        self.fit_boundaries(new_bee)

        if self.graph.cost(new_bee) < self.fitness:
            self.set_pos(new_bee)
        else:
            self.tried += 1


class Solver:
    def __init__(self, iters, SN, limit, graph, lb, ub, alpha, k, p, M):
        self.iters = iters
        self.SN = SN
        self.limit = limit
        self.graph = graph
        self.lb = lb
        self.ub = ub
        self.alpha = alpha
        self.CHI = k
        self.p = p
        self.N = graph.nodes
        self.M = M
        self.population = None
        self.elite = None
        self.fitness = None
        self.weights = None
        self.min_res = None
        self.min_sol = None


    def generate_population(self):
        self.population = [Bee(self.lb, self.ub, self.limit, self.graph, self.N) for _ in range(self.SN)]
        self.get_minimum()


    def get_minimum(self):
        for i in range(self.SN):
            if self.min_res is None or self.min_res > self.population[i].fitness:
                self.min_res = self.population[i].fitness
                self.min_sol = np.copy(self.population[i].pos)
        sorted_pos = np.argsort([self.population[i].fitness for i in range(self.SN)])
        self.elite = sorted_pos[:self.M]


    def build(self):
        self.fitness = [(1 - self.population[i].fitness) if self.population[i].fitness < 0 else 1 / (1 + self.population[i].fitness)
                for i in range(self.SN)]
        total_sum = sum([self.fitness[i] ** self.alpha for i in range(self.SN)])
        self.weights = [(self.fitness[i] ** self.alpha) / total_sum for i in range(self.SN)]


    def update(self, i):
        if self.min_res is None or self.min_res > self.population[i].fitness:
            self.min_res = self.population[i].fitness
            self.min_sol = np.copy(self.population[i].pos)
            
        if self.elite is None:
            self.elite = [i]
        elif len(self.elite) < self.M:
            self.elite.append(i)
        elif i not in self.elite:
            assert len(self.elite) == self.M
            index = None
            for m in range(self.M):
                if index is None or self.population[self.elite[m]].fitness > self.population[self.elite[index]].fitness:
                    index = m
            assert index is not None
            if self.population[self.elite[index]].fitness > self.population[i].fitness:
                self.elite[index] = i


    def search_for(self, i, m = -1):
        e = np.random.choice(self.elite)
        if m == -1:
            m = e
        self.population[i].search(self.min_sol, self.population[m].pos, self.population[e].pos, e == m)


    def employed_bee_phase(self):
        for i in range(self.SN):
            self.search_for(i)


    def onlooker_bee_phase(self):
        for _ in range(self.SN):
            i = np.random.choice(range(self.SN), p=self.weights)
            for j in range(self.M):
                self.search_for(i, self.elite[j])


    def scout_bee_phase(self):
        for i in range(self.SN):
            self.population[i].check()


    def solve(self):
        self.generate_population()
        for ith in range(self.iters):
            # st = time.time()
            self.employed_bee_phase()
            # en = time.time()
            # print('Employed bee phase took {} seconds'.format(en - st))

            # st = time.time()
            self.get_minimum()
            # en = time.time()
            # print('get_minimum took {} seconds'.format(en - st))

            # st = time.time()
            self.build()
            # en = time.time()
            # print('build took {} seconds'.format(en - st))

            # st = time.time()
            self.onlooker_bee_phase()
            # en = time.time()
            # print('onlooker bee phase took {} seconds'.format(en - st))
            
            # st = time.time()
            self.scout_bee_phase()
            # en = time.time()
            # print('scout bee phase took {} seconds'.format(en - st))

            # st = time.time()
            self.get_minimum()
            # en = time.time()
            # print('get_minimum took {} seconds'.format(en - st))

            print('iteration no = {} Result = {}'.format(ith, self.min_res))
        return self.min_res, self.min_sol

