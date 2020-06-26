import numpy as np
import math
import time


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
        self.fitness = self.graph.cost(self.pos)


    def __str__(self):
        return ' '.join([str(v) for v in self.pos])


    def evaluate_boundaries(self, pos):
        pos[pos > self.ub] = self.ub
        pos[pos < self.lb] = self.lb


    def create_new(self, gbest, r1, r2, partner, p):
        phi = np.random.uniform(low=-1, high=1)
        ind = np.random.choice(range(self.dim))

        new_bee = np.copy(self.pos)
        new_bee[ind] = gbest[ind] + phi * (r1[ind] - r2[ind])
        
        self.evaluate_boundaries(new_bee)

        if self.graph.cost(new_bee) < self.fitness:
            self.pos = np.copy(new_bee)
            self.fitness = self.graph.cost(new_bee)
            self.trials = self.max_trials
        elif np.random.uniform(0, 1) < p:   
            phi = np.random.uniform(low=-1, high=1)
            ind = np.random.choice(range(self.dim))

            new_bee = np.copy(self.pos)
            new_bee[ind] = new_bee[ind] + phi * (new_bee[ind] - partner[ind])

            self.evaluate_boundaries(new_bee)
            if self.graph.cost(new_bee) < self.fitness:
                self.pos = np.copy(new_bee)
                self.fitness = self.graph.cost(new_bee)
                self.trials = self.max_trials
            else:
                self.trials -= 1
        else:
            self.trials -= 1


class HABCSolver:
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
            self.population[i].fitness = self.graph.cost(self.population[i].pos)
        self.get_minimum()
        # for i in range(self.population_size):
            # print(i, self.population[i].pos.tolist())


    def build_weight(self):
        total_sum = sum([v ** self.alpha for v in self.fitness])
        self.weights = [(v ** self.alpha) / total_sum for v in self.fitness]


    def build_fitness(self):
        # max_val = max([v.fitness for v in self.population])
        # self.fitness = [max_val - v.fitness for v in self.population]
        self.fitness = [(1 - v.fitness) if v.fitness <= 0 else 1 / (1 + v.fitness)
                        for v in self.population]


    def build(self):
        self.build_fitness()
        self.build_weight()
    

    def choose(self, i):
        partner, partner1 = np.random.choice(range(self.population_size), size=2, replace=False)
        if partner == i:
            partner = partner1
        return partner


    def choose2(self, i):
        r1, r2, x = np.random.choice(range(self.population_size), size=3, replace=False)
        if r1 == i:
            r1 = x
        elif r2 == i:
            r2 = x
        return r1, r2

    
    def search_for(self, i):
        r1, r2 = self.choose2(i)
        partner = self.choose(i)
        self.population[i].create_new(self.min_sol, self.population[r1].pos, self.population[r2].pos, 
                                        self.population[partner].pos, self.P)


    def employed_bee_phase(self):
        for i in range(self.population_size):
            self.search_for(i)


    def onlooker_bee_phase(self):
        for _ in range(self.population_size):
            x = np.random.choice(range(self.population_size), p=self.weights)
            self.search_for(x)


    def scout_bee_phase(self):
        for i in range(self.population_size):
            if self.population[i].trials <= 0:
                self.population[i].generate()


    def get_minimum(self):
        for i in range(self.population_size):
            if self.min_value is None or self.population[i].fitness < self.min_value:
                self.min_value = self.population[i].fitness
                self.min_sol = self.population[i].pos       
        if (self.min_sol < self.lb).any() or (self.min_sol > self.ub).any():
            self.end_it('out of boundaries')

    
    def end_it(self, message):
        print('Hasta La Vista Baby!', message)
        exit()


    def check_all_equal(self, ind):
        f = False
        for i in range(self.population_size):
            if (self.population[i].pos != self.population[0].pos).any():
                f = True
    

    def solve(self):
        self.generate_population()
        for ith in range(self.iterations):
            self.employed_bee_phase()
            self.build()
            self.get_minimum()
            self.onlooker_bee_phase()
            self.scout_bee_phase()
            self.get_minimum()
            print('Iteration No = {} Minimum Result = {}'.format(ith, self.min_value))
        # for i in range(self.population_size):
            # print(i, self.population[i].pos.tolist())
        return self.min_value, self.min_sol



