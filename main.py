from graph_maker import Graph
from solver import ABCSolver
from msolver import ModifiedABCsolver
from hsolver import HABCSolver
from esdlsolver import Solver
import time
import numpy as np


def main(agent, simulations, tot_test):
    iterations = 100
    population_size = 100
    max_trials = agent
    lower_bound = -50
    upper_bound = 50
    alpha = 1
    chaotic_iteration = 300
    prob = 1
    elite_size = 5
    print('Iteration = {} population size = {} max trials = {} alpha = {}'.
          format(iterations, population_size, max_trials, alpha))
    # file = open('output.txt', 'w')
    tot_sum = 0
    tot_iter_sol = np.zeros(iterations)
    tot_time = 0
    for test in range(tot_test):
        graph = Graph(nodes=agent, probability=None, lower_bound=None, upper_bound=None, test_no=test)
        sum_res = 0
        sum_iter_sol = np.zeros(iterations)
        sum_time = 0
        for simulation in range(simulations):
            print('Agent = {} Test No = {} Simulation = {}'.format(agent, test, simulation))
            start_time = time.time()
            solver = Solver(iterations, population_size, max_trials, graph, lower_bound, upper_bound, alpha, chaotic_iteration, prob, elite_size)
            now_result, now_pos, now_iter_sol = solver.solve()
            sum_res += now_result
            sum_iter_sol += now_iter_sol
            end_time = time.time()
            sum_time += end_time - start_time
            print('Simulation took {} seconds'.format(end_time - start_time))
        sum_res /= simulations
        sum_iter_sol /= simulations
        sum_time /= simulations
        print('Prb = {} Util = {}'.format(test, sum_res))
        # file.write(str(sum_res) + '\n')
        tot_iter_sol += sum_iter_sol
        tot_sum += sum_res
        tot_time += sum_time
    # file.close()
    print('Agent = {} Util = {}'.format(agent, tot_sum / tot_test));
    return tot_sum / tot_test, tot_iter_sol / tot_test, tot_time / tot_test


if __name__ == "__main__":
    main(25, 1, 10)
