import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GraphParser {
    String fileName;

    public GraphParser(String fileName) {
        this.fileName = fileName;
    }

    List <String> getLines() throws IOException {
        List <String> lines = new ArrayList<>();
        File file = new File(fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    public Graph getGraph () throws IOException {
        List <String> lines = getLines();
        Graph graph = new Graph();
        for (String st : lines) {
            String[] splits = st.split(" ");
            int[] num = new int[splits.length];
            for (int i = 0; i < splits.length; i++) {
                num[i] = Integer.parseInt(splits[i]);
            }
            graph.addEdge(num[0], num[1], num[2], 0, num[4], 0, num[3], 0);
        }
        return graph;
    }
}
