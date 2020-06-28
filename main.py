from graph_maker import Graph
from solver import ABCSolver
from msolver import ModifiedABCsolver
from hsolver import HABCSolver
from esdlsolver import Solver
import time


def main(agent, simulations, tot_test):
    iterations = 200
    population_size = 100
    max_trials = 50
    lower_bound = -50
    upper_bound = 50
    alpha = 1
    chaotic_iteration = 300
    prob = 1
    elite_size = 5
    print('Iteration = {} population size = {} max trials = {} alpha = {}'.
          format(iterations, population_size, max_trials, alpha))
    file = open('output.txt', 'w')
    tot_sum = 0
    for test in range(tot_test):
        graph = Graph(nodes=agent, probability=None, lower_bound=None, upper_bound=None, test_no=test)
        sum_res = 0
        for simulation in range(simulations):
            print('Agent = {} Test No = {} Simulation = {}'.format(agent, test, simulation))
            start_time = time.time()
            solver = Solver(iterations, population_size, max_trials, graph, lower_bound, upper_bound, alpha, chaotic_iteration, prob, elite_size)
            now_result, now_pos = solver.solve()
            sum_res += now_result
            end_time = time.time()
            print('Simulation took {} seconds'.format(end_time- start_time))
        sum_res /= simulations
        file.write(str(sum_res) + '\n')
        tot_sum += sum_res
    file.close()
    return tot_sum / tot_test


if __name__ == "__main__":
    # file = open('output.txt', 'w')
#    for agents in range(20, 21):
    result = main(agent=50, simulations=5, tot_test=30)
    print(result)
        # file.write(str(agents) + ' ' + str(result) + '\n')
    # file.close()



# def main(agent, simulations):
    # iterations = 300
    # population_size = 100
    # max_trials = 5
    # lower_bound = -50
    # upper_bound = 50
    # alpha = 3
    # sum_res = 0
    # print('Iteration = {} population size = {} max trails = {} alpha = {}'.
          # format(iterations, population_size, max_trials, alpha))
    # for test in range(30):
        # graph = Graph(nodes=agent, probability=None, lower_bound=None, upper_bound=None, test_no=test)
        # max_result = None
        # max_pos = None
        # for simulation in range(simulations):
            # print('Agent = {} Test No = {} Simulation No = {}'.format(agent, test, simulation))
            # solver = ABCSolver(iterations, population_size, max_trials, graph, lower_bound, upper_bound, alpha)
            # solver.generate_population()
            # now_result, now_pos = solver.solve()
            # if max_result is None or max_result > now_result:
                # max_result = now_result
                # max_pos = now_pos
        # sum_res += max_result
    # return sum_res / 30


# if __name__ == "__main__":
    # # file = open('output.txt', 'w')
    # # for test in range(3, 31):
    # result = main(agent=50, simulations=3)
    # print(result)
        # # file.write(str(agents) + ' ' + str(result) + '\n')
    # # file.close()

