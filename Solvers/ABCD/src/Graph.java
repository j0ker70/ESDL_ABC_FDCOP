import java.util.*;

public class Graph {

    final int ROOT = 0;

    HashMap <Integer, List <UtilityEdge>> adjacencyList;
    HashMap <Integer, List <UtilityEdge>> childrenList;
    HashMap <Integer, Integer> parent;

    public Graph() {
        adjacencyList = new HashMap<>();
        childrenList = new HashMap<>();
        parent = new HashMap<>();
    }

    void addVertex (int u) {
        if (!adjacencyList.containsKey(u)) {
            adjacencyList.put(u, new ArrayList<>());
        }
    }

    public void addEdge (int u, int v, int a, int b, int c, int d, int e, int f) {
        addVertex(u);
        addVertex(v);
        adjacencyList.get(u).add(new UtilityEdge(u, v, new BinaryFunction(a, b, c, d, e, f)));
        adjacencyList.get(v).add(new UtilityEdge(v, u, new BinaryFunction(c, d, a, b, e, f)));
    }

    public List <UtilityEdge> neighborEdges (int u) {
        return adjacencyList.get(u);
    }

    public List <UtilityEdge> childrenEdges (int u) {
        return childrenList.get(u);
    }

    public Integer getParent (int u) {
        return parent.get(u);
    }

    public void pseudoBfsTree () {
        Queue <Integer> queue = new LinkedList<>();
        HashMap <Integer, Boolean> isVisited = new HashMap<>();

        queue.add(ROOT);
        isVisited.put(ROOT, true);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (!childrenList.containsKey(u)) {
                childrenList.put(u, new ArrayList<>());
            }
            for (UtilityEdge edge : adjacencyList.get(u)) {
                int v = edge.vId;
                if (!isVisited.getOrDefault(v, false)) {
                    childrenList.get(u).add(edge);
                    parent.put(v, u);
                    queue.add(v);
                    isVisited.put(v, true);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Graph Representation:\n");
        for (Map.Entry<Integer, List<UtilityEdge>> entry : adjacencyList.entrySet()) {
            ret.append("Edges connected with Agent ").append(entry.getKey()).append("\n");
            for (UtilityEdge edge : entry.getValue()) {
                ret.append(edge.toString()).append("\n");
            }
        }
        return ret.toString();
    }
}
