public class UtilityEdge {
    int uId, vId;
    BinaryFunction function;

    public UtilityEdge(int uId, int vId, BinaryFunction function) {
        this.uId = uId;
        this.vId = vId;
        this.function = function;
    }

    double utility (double x, double y) {
        return function.functionValue(x, y);
    }

    @Override
    public String toString() {
        return "Agent " + uId + " <<-- " + function + " -->> Agent " + vId;
    }
}
