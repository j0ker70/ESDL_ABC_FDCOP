import java.io.IOException;
import java.util.*;

public class Main {
    public static void main (String[] args) throws IOException {
        int agents = Integer.parseInt(args[0]), testNo = Integer.parseInt(args[1]);
        String filename = "tests/d" + agents + "/test_" + testNo + ".txt";
        GraphParser graphParser = new GraphParser(filename);
        Graph graph = graphParser.getGraph();
        graph.pseudoBfsTree();

        int S = 100, M = 5, L = 3, iteration = 100;
        int lb = -50, ub = 50;

        List<Messenger> messengers = new ArrayList<>();
        for (int i = 0; i < agents; i++) {
            messengers.add(new Messenger());
        }

        RootAgent rootAgent = new RootAgent(0, graph.neighborEdges(0), graph.childrenEdges(0), -1,
                S, M, L, agents, lb, ub, messengers, iteration);
        rootAgent.start();
        for (int i = 1; i < agents; i++) {
            Agent agent = new Agent(i, graph.neighborEdges(i), graph.childrenEdges(i), graph.getParent(i), S, M, L,
                    agents, lb, ub, messengers, iteration);
            agent.start();
        }
    }
}
