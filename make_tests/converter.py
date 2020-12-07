import os


for i in range(4, 101):
    for j in range(10):
        filename = 'small-world/sparse/d{}/test_{}.txt'.format(i, j)
        ofilename = 'new_tests/small-world/sparse/d{}/rep_{}_d{}.dzn'.format(i, j, i)

        odirname = 'new_tests/small-world/sparse/d{}'.format(i)
        if not os.path.exists(odirname):
            os.makedirs(odirname)

        output_file = open(ofilename, 'w')
        input_file = open(filename, 'r')
        lines = [v.strip() for v in input_file.readlines()]
        input_file.close()
        g = []
        for _ in range(i + 1):
            g.append(list())
        output_file.write('domain {};\n'.format(50))
        for line in lines:
            u, v, a, b, c = map(int, line.split())
            u += 1
            v += 1
            g[u].append(v)
            g[v].append(u)
            output_file.write('function {}x_{}^2 {}x_{} {}x_{}^2 {}x_{} {}x_{}x_{} {};\n'.format(a, u, 0, u, c, v, 0, v, b, u, v, 0))
        for u in range(1, i + 1):
            output_file.write('neighbor set: x_{}: '.format(u))
            for v in g[u]:
                output_file.write('x_{} '.format(v))
            output_file.write(';\n')
        output_file.close()

