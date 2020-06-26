import networkx as nx
import functions
import matplotlib.pyplot as plt


class Edge:
    def __init__(self, u, v, edge_function):
        self.u = u
        self.v = v
        self.edge_function = edge_function

    def cost(self, x, y):
        return self.edge_function.functional_value(x, y)

    def __str__(self):
        return 'From ' + str(self.u) + ' To ' + str(self.v) + ' with function -->> ' \
               + str(self.edge_function)

    def __repr__(self):
        return str(self)


def get_edges(nodes, test_no):
#    filename = 'tests/d' + str(nodes) + '/test_' + str(test_no) + '.txt'
    filename = 'FDCOP_50/P' + str(test_no) + '.txt'
    input_file = open(filename, 'r')
#    lines = [v.strip() for v in input_file.readlines()]
    edge_list = []
    n, m = map(int, input_file.readline().split())
    for i in range(n):
        input_file.readline()
    for _ in range(m):
        u, v = map(int, input_file.readline().split())
        a, b, c = map(int, input_file.readline().split())
        # u, v, a, b, c = map(int, line.split())
        edge_list.append(Edge(u, v, functions.BinaryFunction(a, 0, c, 0, b, 0)))
    input_file.close()
    return edge_list


class Graph:
    def __init__(self, nodes, probability, lower_bound, upper_bound, test_no):
        self.nodes = nodes
        # self.nx_graph = nx.erdos_renyi_graph(nodes, probability)
        self.edge_list = get_edges(nodes, test_no)

    def cost(self, node_values):
        cost_sum = 0
        for edge in self.edge_list:
            cost_sum += edge.cost(node_values[edge.u], node_values[edge.v])
        return cost_sum

    # def show(self):
    #     nx.draw(self.nx_graph, with_labels=True, font_weight='bold')
    #     plt.show()

    def __str__(self):
        return '\n'.join([str(v) for v in self.edge_list])

    def __repr__(self):
        return str(self)
