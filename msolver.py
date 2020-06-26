import numpy as np
import math


def evaluate_boundaries(new_bee):
    new_bee.pos[new_bee.pos > new_bee.ub] = new_bee.ub
    new_bee.pos[new_bee.pos < new_bee.lb] = new_bee.lb
    return new_bee


class Bee:
    def __init__(self, graph, max_trials, lb, ub):
        self.graph = graph
        self.dim = graph.nodes
        self.max_trials = max_trials
        self.trials = max_trials
        self.lb = lb
        self.ub = ub
        self.pos = []
        self.fitness = None

    def generate(self):
        self.pos = np.random.uniform(low=self.lb, high=self.ub, size=self.dim).astype('float32')
        self.trials = self.max_trials

    def __str__(self):
        return ' '.join([str(v) for v in self.pos])

    def get_fitness(self):
        if self.fitness is None:
            self.fitness = self.graph.cost(self.pos)
        return self.fitness

    def create_new(self, partner):
        phi = np.random.uniform(low=-1, high=1)
        ind = np.random.choice(range(self.dim))
        new_bee = Bee(self.graph, self.max_trials, self.lb, self.ub)

        new_bee.pos = np.empty_like(self.pos)
        new_bee.pos[:] = self.pos
        new_bee.pos[ind] = self.pos[ind] + phi * (self.pos[ind] - partner.pos[ind])
        
        # phi = np.random.uniform(low=-1, high=1, size=self.dim)
        # new_bee.pos = self.pos + phi * (self.pos - partner.pos)

        new_bee = evaluate_boundaries(new_bee)
        if new_bee.get_fitness() <= self.get_fitness():
            self.pos = np.empty_like(new_bee.pos)
            self.pos[:] = new_bee.pos
            self.fitness = new_bee.fitness
            self.trials = self.max_trials



class ModifiedABCsolver:
    def __init__(self, iterations, population_size, max_trials, graph, lb, ub, alpha, k, p):
        self.ub = ub
        self.lb = lb
        self.alpha = alpha
        self.graph = graph
        self.dim = graph.nodes
        self.max_trials = max_trials
        self.population_size = population_size
        self.iterations = iterations
        self.K = k
        self.P = p
        self.population = []
        self.fitness = []
        self.weights = []
        self.min_value = None
        self.min_sol = None


    def generate_population(self):
        self.population = [Bee(self.graph, self.max_trials, self.lb, self.ub) for _ in range(self.population_size)]
        for i in range(self.population_size):
            now_pos = []
            opo_pos = []
            for j in range(self.dim):
                ch = np.random.uniform(0, 1)
                for _ in range(self.K):
                    ch = math.sin(math.pi * ch)
                now_pos.append(self.lb + (self.ub - self.lb) * ch)
                opo_pos.append(self.lb + self.ub - now_pos[-1])
            if self.graph.cost(now_pos) < self.graph.cost(opo_pos):
                self.population[i].pos = np.array(now_pos)
            else:
                self.population[i].pos = np.array(opo_pos)
        self.get_minimum()
        # for i in range(self.population_size):
            # print(i, self.population[i].pos.tolist())


    def choose(self, i):
        partner, partner1 = np.random.choice(range(self.population_size), size=2, replace=False)
        if partner == i:
            partner = partner1
        return partner


    def get_minimum(self):
        for i in range(self.population_size):
            if self.min_value is None or self.population[i].get_fitness() < self.min_value:
                self.min_value = self.population[i].get_fitness()
                self.min_sol = self.population[i].pos


    def normal_solve(self, i):
        partner = self.choose(i)
        self.population[i].create_new(self.population[partner])


    def solve(self):
        self.generate_population()
        for ith in range(self.iterations * 100):
            self.get_minimum()
            for i in range(self.population_size):
                r1, r2, x = np.random.choice(range(self.population_size), size=3, replace=False)
                if r1 == i:
                    r1 = x
                elif r2 == i:
                    r2 = x
                phi = np.random.uniform(low=-1, high=1)
                ind = np.random.choice(range(self.dim))

                now_pos = np.empty_like(self.min_sol)
                now_pos[:] = self.min_sol
                now_pos[ind] = self.min_sol[ind] + (self.population[r1].pos[ind] - self.population[r2].pos[ind]) * phi
                if now_pos[ind] > self.ub:
                    now_pos[ind] = self.ub
                elif now_pos[ind] < self.lb:
                    now_pos[ind] = self.lb
                
                # phi = np.random.uniform(low=-1, high=1, size=self.dim)
                # now_pos = self.min_sol + (self.population[r1] - self.population[r2]) * phi

                if self.graph.cost(now_pos) < self.population[i].get_fitness():
                    self.population[i].pos = np.empty_like(now_pos)
                    self.population[i].pos[:] = now_pos
                    self.population[i].fitness = self.graph.cost(now_pos)
                elif np.random.uniform(0, 1) < self.P:
                    self.normal_solve(i)

            print('Iteration No = {} Minimum Result = {}'.format(ith, self.min_value))
        self.get_minimum()
        return self.min_value, self.min_sol

