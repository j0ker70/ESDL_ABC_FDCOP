file = open('example.txt')

lines = file.readlines()

v = {}

for line in lines:
    a, b = line.strip().split()
    a = int(a)
    b = float(b)
    v[a] = b

agent = 100

filename = "tests/d{}/test_0.txt".format(agent)

new_file = open(filename)

val = 0

for line in new_file.readlines():
    x, y, a, b, c = map(int, line.strip().split())
    val += a * v[x] * v[x] + b * v[x] * v[y] + c * v[y] * v[y]

print(val)

