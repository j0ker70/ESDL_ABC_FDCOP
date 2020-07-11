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
        # file.write(str(sum_res) + '\n')
        tot_iter_sol += sum_iter_sol
        tot_sum += sum_res
        tot_time += sum_time
    # file.close()
    return tot_sum / tot_test, tot_iter_sol / tot_test, tot_time / tot_test


if __name__ == "__main__":
    file1 = open('result_output.txt', 'w')
    file2 = open('time_output.txt', 'w')
    file3 = open('iteration_output.txt', 'w')
    for agents in range(3, 51):
        result, iter_sol, time_taken = main(agent=agents, simulations=5, tot_test=10)
        print(result)
        file1.write('{} {}\n'.format(agents, result))
        file2.write('{} {}\n'.format(agents, time_taken))
        file3.write('{} {}\n'.format(agents, ' '.join([str(v) for v in iter_sol])))
    file1.close()
    file2.close()
    file3.close()

