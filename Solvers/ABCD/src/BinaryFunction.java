public class BinaryFunction {
    int a, b, c, d, e, f; // ax^2 + bx + cy^2 + dy + exy + f

    public BinaryFunction(int a, int b, int c, int d, int e, int f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    double functionValue (double x, double y) {
        return a * x * x + b * x + c * y * y + d * y + e * x * y + f;
    }

    @Override
    public String toString() {
        return a + "x^2 + " + b + "x + " + c + "y^2 + " +
                d + "y + " + e + "xy + " + f;
    }
}
