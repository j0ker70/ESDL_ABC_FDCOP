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
        else:
            self.trials -= 1


class ABCSolver:
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


    def build_weight(self):
        total_sum = sum([v ** self.alpha for v in self.fitness])
        self.weights = [(v ** self.alpha) / total_sum for v in self.fitness]


    def build_fitness(self):
        # max_val = max([v.get_fitness() for v in self.population])
        # self.fitness = [max_val - v.get_fitness() for v in self.population]
        self.fitness = [1 - v.get_fitness() if v.get_fitness() <= 0 else 1 / (1 + v.get_fitness())
                        for v in self.population]


    def build(self):
        self.build_fitness()
        self.build_weight()
    

    def choose(self, i):
        partner, partner1 = np.random.choice(range(self.population_size), size=2, replace=False)
        if partner == i:
            partner = partner1
        return partner


    def employed_bee_phase(self):
        for i in range(self.population_size):
            partner = self.choose(i)
            self.population[i].create_new(self.population[partner])


    def onlooker_bee_phase(self):
        for _ in range(self.population_size):
            x = np.random.choice(range(self.population_size), p=self.weights)
            partner = self.choose(x)
            self.population[x].create_new(self.population[partner])


    def scout_bee_phase(self):
        for i in range(self.population_size):
            if self.population[i].trials <= 0:
                self.population[i].generate()


    def get_minimum(self):
        for i in range(self.population_size):
            if self.min_value is None or self.population[i].get_fitness() < self.min_value:
                self.min_value = self.population[i].get_fitness()
                self.min_sol = self.population[i].pos


    def solve(self):
        self.generate_population()
        for ith in range(self.iterations * 100):
            self.employed_bee_phase()
            self.build()
            self.onlooker_bee_phase()
            self.scout_bee_phase()
            self.get_minimum()
            print('Iteration No = {} Minimum Result = {}'.format(ith, self.min_value))
        # for i in range(self.population_size):
            # print(i, self.population[i].pos.tolist())
        return self.min_value, self.min_sol


