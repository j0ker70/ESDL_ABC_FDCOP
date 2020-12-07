import networkx as nx
import os
import numpy as np

dirname = 'scale-free/dense'

if not os.path.exists(dirname):
     os.mkdir(dirname)

for nodes in range(3, 101):
     dirname2 = dirname + '/d{}'.format(nodes)
     if not os.path.exists(dirname2):
         os.mkdir(dirname2)
     for t in range(10):
         print('node = {} test = {}'.format(nodes, t))
         m = 10
         if nodes < 20:
             m = nodes // 2
         graph = nx.barabasi_albert_graph(nodes, m)
         while not nx.is_connected(graph):
             graph = nx.barabasi_albert_graph(nodes, m)
         filename = dirname2 + '/test_{}.txt'.format(t)
         write_file = open(filename, 'w')
         for u, v in graph.edges():
             a, b, c = np.random.randint(low=-9, high=10, size=3)
             write_file.write('{} {} {} {} {}\n'.format(u, v, a, b, c))
         write_file.close()


dirname = 'scale-free/sparse'

if not os.path.exists(dirname):
     os.mkdir(dirname)

for nodes in range(4, 101):
     dirname2 = dirname + '/d{}'.format(nodes)
     if not os.path.exists(dirname2):
         os.mkdir(dirname2)
     for t in range(10):
         print('node = {} test = {}'.format(nodes, t))
         graph = nx.barabasi_albert_graph(nodes, 3)
         while not nx.is_connected(graph):
             graph = nx.barabasi_albert_graph(nodes, 3)
         filename = dirname2 + '/test_{}.txt'.format(t)
         write_file = open(filename, 'w')
         for u, v in graph.edges():
             a, b, c = np.random.randint(low=-9, high=10, size=3)
             write_file.write('{} {} {} {} {}\n'.format(u, v, a, b, c))
         write_file.close()


