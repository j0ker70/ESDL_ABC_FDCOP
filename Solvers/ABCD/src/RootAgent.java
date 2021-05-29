import java.util.*;

public class RootAgent extends Agent {
    boolean[][]  V;
    double gBestFitness;

    List<Double> fitValues;
    List<Double> probabilities;

    public RootAgent(int id, List<UtilityEdge> neighbors, List<UtilityEdge> children,
                 int parentId, int s, int m, int l, int n, int lowerBound, int upperBound, List<Messenger> messengers, int itr) {
        super(id, neighbors, children, parentId, s, m, l, n, lowerBound, upperBound, messengers, itr);
        V = new boolean[S][N];
    }

    public void run() {
        initialization();
        long startTime = System.nanoTime();
        for (int itr = 0; itr < ITR; itr++) {
            abcE();
//            System.out.println("After iteration " + (itr + 1) + " " + gBestFitness);
        }
        long endTime = System.nanoTime();
        System.out.println("Time = " + (endTime - startTime) / 1000000000.0);
        System.out.println("Global best = " + gBestFitness);
        System.out.println("Agent-" + id + " X = " + X);
    }

    public void abcE() {
        try {
            build();
            employed();
            calculateProbabilities();
            for (int itr = 0; itr < S; itr++) {
                onlooker();
            }
            scout();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isAllTrue(boolean[] visited) {
        boolean[] now = new boolean[visited.length];
        Arrays.fill(now, true);
        return Arrays.equals(visited, now);
    }

    public void scout() throws InterruptedException {
        List<Integer> indexes = new LinkedList<>();
        for (int i = 0; i < S; i++) {
            if (isAllTrue(V[i])) {
                indexes.add(i);
            }
        }
        for (int i = 0; i < N; i++) {
            messengers.get(i).putScoutUpdateMessage(id, i, "ok", indexes);
        }
        scoutReplaceOperation();
    }

    public void onlooker() throws InterruptedException {
        int uIndex = chooseViaProbability();
        for (int i = 0; i < N; i++) {
            messengers.get(i).putCopyOnlookerSolutionMessage(id, i, "ok", uIndex);
        }

        List<Double> popR = new LinkedList<>();
        List<Double> rLocalFitness = new LinkedList<>(Collections.nCopies(M, 0.0));
        List<Double> rFitness = new LinkedList<>();
        copyOnlookerSolution(popR);

        for (int m = 0; m < M; m++) {
            List<Integer> twoAgents = twoRandomAgent();
            int firstAgent = twoAgents.get(0), secondAgent = twoAgents.get(1);

            V[uIndex][firstAgent] = true;

            double eliteValue, popValue;
            if (secondAgent != id) {
                messengers.get(secondAgent).putReqOnlookerValuesMessage(id, secondAgent, "ok", m, uIndex);
                Message message = messengers.get(id).getReceivedOnlookerValuesMessage();
                String[] values = message.content.split(" ");
                eliteValue = Double.parseDouble(values[0]);
                popValue = Double.parseDouble(values[1]);
            }
            else {
                eliteValue = popP.get(elite.get(m));
                popValue = popP.get(uIndex);
            }

            messengers.get(firstAgent).putOnlookerUpdateMessage(id, firstAgent, "ok", uIndex, m, eliteValue,
                    popValue);
        }

        for (int i = 1; i < N; i++) {
            messengers.get(i).putReqOnlookerValuesMessage(id, i, "X", 0, 0);
        }
        for (int i = 0; i < N; i++) {
            messengers.get(i).putOnlookerUpdateMessage(id, i, "X", 0, 0, 0, 0);
        }

        while (true) {
            Message message = messengers.get(id).getOnlookerUpdateMessage();
            if (message.type.equals("X")) {
                break;
            }
            String[] values = message.content.split(" ");
            int m = Integer.parseInt(values[1]);
            double emh = Double.parseDouble(values[2]), puh = Double.parseDouble(values[3]);

            int l = randomInt(0, M - 1);

            double phi1 = randomDouble(-0.5, 0.5), phi2 = randomDouble(0, 1);
            double z = fitBetween(0.5 * (emh + X) + phi1 * (puh - popP.get(elite.get(l))) + phi2 * (puh - X));
            popR.set(m, z);
        }
        evaluate(popR, rLocalFitness, rFitness);
        int maximumIndex = maxIndex(rFitness);
        String type = "X";
        if (rFitness.get(maximumIndex) > fitness.get(uIndex)) {
            type = "ok";
            Arrays.fill(V[uIndex], false);
        }
        if (rFitness.get(maximumIndex) > gBestFitness) {
            type = "both";
            gBestFitness = rFitness.get(maximumIndex);
        }
        for (int i = 0; i < N; i++) {
            messengers.get(i).putReplaceOnlookerValuesMessage(id, i, type, uIndex, maximumIndex);
        }
        onlookerReplaceOperation(popR, rLocalFitness, rFitness);
    }

    private void calculateProbabilities() {
        fitValues = new LinkedList<>();
        probabilities = new LinkedList<>();
        double totalSum = 0;
        for (int i = 0; i < S; i++) {
            fitValues.add(convertToPositive(fitness.get(i)));
            totalSum += fitValues.get(i);
        }
        for (int i = 0; i < S; i++) {
            probabilities.add(fitValues.get(i) / totalSum);
        }
    }

    private int chooseViaProbability() {
        double mark = randomDouble(0, 1);
        double totalSum = 0;
        for (int i = 0; i < S; i++) {
            totalSum += probabilities.get(i);
            if (mark <= totalSum) {
                return i;
            }
        }
        return -1;
    }

    private Double convertToPositive(Double aDouble) {
        if (aDouble >= 0) {
            return 1 + aDouble;
        }
        return 1 / (1 - aDouble);
    }

    public void employed() throws InterruptedException {
        List<Double> popQ = new LinkedList<>(popP);
        List<Double> qLocalFitness = new LinkedList<>(Collections.nCopies(S, 0.0));
        List<Double> qFitness = new LinkedList<>();

        for (int i = 0; i < S; i++) {
            List<Integer> twoAgents = twoRandomAgent();
            int firstAgent = twoAgents.get(0), secondAgent = twoAgents.get(1);
            int eliteIndex = randomInt(0, M - 1);

            V[i][firstAgent] = true;

            double eliteValue, popValue;
            if (secondAgent != id) {
                messengers.get(secondAgent).putReqEmployedValuesMessage(id, secondAgent, "ok", eliteIndex, i);
                Message message = messengers.get(id).getReceivedEmployedValuesMessage();
                String[] values = message.content.split(" ");
                eliteValue = Double.parseDouble(values[0]);
                popValue = Double.parseDouble(values[1]);
            }
            else {
                eliteValue = popP.get(elite.get(eliteIndex));
                popValue = popP.get(i);
            }

            messengers.get(firstAgent).putEmployedUpdateMessage(id, firstAgent, "ok", i, eliteIndex, eliteValue,
                    popValue);
        }
        for (int i = 1; i < N; i++) {
            messengers.get(i).putReqEmployedValuesMessage(id, i, "X", 0, 0);
        }
        for (int i = 0; i < N; i++) {
            messengers.get(i).putEmployedUpdateMessage(id, i, "X", 0, 0, 0, 0);
        }
        while (true) {
            Message message = messengers.get(id).getEmployedUpdateMessage();
            if (message.type.equals("X")) {
                break;
            }
            String[] values = message.content.split(" ");
            int u = Integer.parseInt(values[0]), l = Integer.parseInt(values[1]);
            double elh = Double.parseDouble(values[2]), puh = Double.parseDouble(values[3]);

            double phi1 = randomDouble(-0.5, 0.5), phi2 = randomDouble(0, 1);
            double z = fitBetween(0.5 * (elh + X) + phi1 * (puh - popP.get(elite.get(l))) + phi2 * (puh - X));
            popQ.set(u, z);
        }
        evaluate(popQ, qLocalFitness, qFitness);

        List<Integer> goodIndexes = new LinkedList<>();
        int bestIndex = -1;
        for (int i = 0; i < S; i++) {
            if (qFitness.get(i) > fitness.get(i)) {
                goodIndexes.add(i);
                Arrays.fill(V[i], false);
            }
            if (qFitness.get(i) > gBestFitness) {
                gBestFitness = qFitness.get(i);
                bestIndex = i;
            }
        }
        goodIndexes.add(bestIndex);

        for (int i = 0; i < N; i++) {
            messengers.get(i).putReplaceEmployedValues(id, i, "ok", goodIndexes);
        }

        employedReplaceOperation(popQ, qLocalFitness, qFitness);
    }

    public Integer maxIndex(List<Double> list) {
        double maxVal = list.get(0);
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) > maxVal) {
                index = i;
                maxVal = list.get(i);
            }
        }

        return index;
    }

    public void build() throws InterruptedException {
        evaluate(popP, localFitness, fitness);
        int bestIndex = maxIndex(fitness);
        String type = "X";
        if (fitness.get(bestIndex) > gBestFitness) {
            type = "ok";
            gBestFitness = fitness.get(bestIndex);
            X = popP.get(bestIndex);
        }
        for (int i = 1; i < N; i++) {
            messengers.get(i).putGBestUpdateMessage(id, i, type, bestIndex);
        }

        buildElite();
    }

    public void buildElite() throws InterruptedException {
        elite = new LinkedList<>();
        for (int i = 0; i < M; i++) {
            elite.add(i);
        }
        for (int i = M; i < S; i++) {
            double minValue = fitness.get(elite.get(0));
            int index = 0;
            for (int j = 0; j < M; j++) {
                if (fitness.get(elite.get(j)) < minValue) {
                    minValue = fitness.get(elite.get(j));
                    index = j;
                }
            }
            if (minValue < fitness.get(i)) {
                elite.set(index, i);
            }
        }
        for (int i = 1; i < N; i++) {
            messengers.get(i).putEliteSetUpdateMessage(id, i, "ok", elite);
        }
    }

    public void initialization() {
        popP = new LinkedList<>();
        for (int i = 0; i < S; i++) {
            popP.add(randomDouble(lowerBound, upperBound));
        }
        localFitness = new ArrayList<>(Collections.nCopies(S, 0.0));
        fitness = new LinkedList<>();
        for (int i = 0; i < S; i++) {
            for (int j = 0; j < N; j++) {
                V[i][j] = false;
            }
        }
        gBestFitness = -Double.MAX_VALUE;
    }

    public void evaluate(List<Double> popW, List<Double> wLocalFitness, List<Double> wFitness)
            throws InterruptedException {
        for (UtilityEdge edge : neighbors) {
            messengers.get(edge.vId).putNeighborValues(id, edge.vId, "ok", popW);
        }
        Collections.fill(wLocalFitness, 0.0);
        for (UtilityEdge edge : neighbors) {
            Message message = messengers.get(id).getNeighborValues(edge.vId);
            String[] stringValues = message.content.split(" ");
            List <Double> neighborValues = new ArrayList<>();
            for (String string : stringValues) {
                neighborValues.add(Double.parseDouble(string));
            }
            for (int i = 0; i < popW.size(); i++) {
                wLocalFitness.set(i, wLocalFitness.get(i) + edge.utility(popW.get(i), neighborValues.get(i)));
            }
        }
        wFitness.clear();
        wFitness.addAll(wLocalFitness);
        for (UtilityEdge edge : children) {
            Message message = messengers.get(id).getUtilityMessage(edge.vId);
            String[] stringValues = message.content.split(" ");
            List <Double> childValues = new ArrayList<>();
            for (String string : stringValues) {
                childValues.add(Double.parseDouble(string));
            }
            for (int i = 0; i < popW.size(); i++) {
                wFitness.set(i, wFitness.get(i) + childValues.get(i));
            }
        }
        for (int i = 0; i < popW.size(); i++) {
            wFitness.set(i, wFitness.get(i) / 2);
        }
    }
}