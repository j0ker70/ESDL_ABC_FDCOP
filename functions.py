import random


class BinaryFunction:  # ax^2 + bx + cy^2 + dy + exy + f
    # def __init__(self, lower_bound, upper_bound):
    #     self.a, self.b, self.c, self.d, self.e, self.f = [random.randint(lower_bound, upper_bound)
    #                                                       for _ in range(6)]

    def __init__(self, a, b, c, d, e, f):
        self.a, self.b, self.c, self.d, self.e, self.f = a, b, c, d, e, f

    def functional_value(self, x, y):
        return self.a * x * x + self.b * x + self.c * y * y + self.d * y + self.e * x * y + self.f

    def __str__(self):
        return str(self.a) + ' * x^2 + ' + str(self.b) + ' * x + ' + str(self.c) + ' * y^2 + ' + \
                str(self.d) + ' * y + ' + str(self.e) + ' * xy + ' + str(self.f)

    def __repr__(self):
        return str(self)
