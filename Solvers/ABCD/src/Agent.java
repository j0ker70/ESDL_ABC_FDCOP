import java.util.*;

public class Agent extends Thread {
    int id;

    List<UtilityEdge> neighbors;
    List<UtilityEdge> children;
    int parentId;

    int S, M, L, N, ITR;

    int upperBound, lowerBound;

    List<Messenger> messengers;

    List<Double> popP;
    List<Integer> elite;

    List<Double> localFitness;
    List<Double> fitness;

    double X;

    Random random;

    public Agent(int id, List<UtilityEdge> neighbors, List<UtilityEdge> children,
                 int parentId, int s, int m, int l, int n, int lowerBound, int upperBound, List<Messenger> messengers, int itr) {
        this.id = id;
        this.neighbors = neighbors;
        this.children = children;
        this.parentId = parentId;
        S = s;
        M = m;
        L = l;
        N = n;
        ITR = itr;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.messengers = messengers;
        random = new Random();
        setName("Agent-" + id);
    }

    public void run() {
        initialization();
        for (int itr = 0; itr < ITR; itr++) {
            abcE();
        }
        System.out.println("Agent-" + id + " X = " + X);
    }

    public void abcE() {
        try {
            build();
            employed();
            for (int itr = 0; itr < S; itr++) {
                onlooker();
            }
            scout();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void scout() throws InterruptedException {
        scoutReplaceOperation();
    }

    public void scoutReplaceOperation() throws InterruptedException {
        Message message = messengers.get(id).getScoutUpdateMessage();
        if (message.content.isEmpty()) {
            return ;
        }
        String[] stringValues = message.content.split(" ");
        for (String string : stringValues) {
            int index = Integer.parseInt(string);
            popP.set(index, randomDouble(lowerBound, upperBound));
        }
    }

    public void onlooker() throws InterruptedException {
        List<Double> popR = new LinkedList<>();
        List<Double> rLocalFitness = new LinkedList<>(Collections.nCopies(M, 0.0));
        List<Double> rFitness = new LinkedList<>();
        copyOnlookerSolution(popR);

        while (true) {
            Message message = messengers.get(id).getReqOnlookerValuesMessage();
            if (message.type.equals("X")) {
                break;
            }
            String[] values = message.content.split(" ");
            int eliteIndex = Integer.parseInt(values[0]), popIndex = Integer.parseInt(values[1]);
            messengers.get(message.senderId).putReceivedOnlookerValuesMessage(id, message.senderId, "ok",
                    popP.get(elite.get(eliteIndex)), popP.get(popIndex));
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
        onlookerReplaceOperation(popR, rLocalFitness, rFitness);
    }

    public void onlookerReplaceOperation(List<Double> popW, List<Double> wLocalFitness, List<Double> wFitness)
            throws InterruptedException {
        Message message = messengers.get(id).getReplaceOnlookerValuesMessage();
        String[] values = message.content.split(" ");
        int uIndex = Integer.parseInt(values[0]), rIndex = Integer.parseInt(values[1]);
        if (message.type.equals("X")) {
            return ;
        }
        popP.set(uIndex, popW.get(rIndex));
        localFitness.set(uIndex, wLocalFitness.get(rIndex));
        fitness.set(uIndex, wFitness.get(rIndex));
        if (message.type.equals("both")) {
            X = popW.get(rIndex);
        }
    }

    public void copyOnlookerSolution(List<Double> popR) throws InterruptedException {
        Message message = messengers.get(id).getCopyOnlookerMessage();
        int uIndex = Integer.parseInt(message.content);
        popR.addAll(Collections.nCopies(M, popP.get(uIndex)));
    }

    public void employed() throws InterruptedException {
        List<Double> popQ = new LinkedList<>(popP);
        List<Double> qLocalFitness = new LinkedList<>(Collections.nCopies(S, 0.0));
        List<Double> qFitness = new LinkedList<>();

        while (true) {
            Message message = messengers.get(id).getReqEmployedValueMessage();
            if (message.type.equals("X")) {
                break;
            }
            String[] values = message.content.split(" ");
            int eliteIndex = Integer.parseInt(values[0]), popIndex = Integer.parseInt(values[1]);
            messengers.get(message.senderId).putReceivedEmployedValuesMessage(id, message.senderId, "ok",
                    popP.get(elite.get(eliteIndex)), popP.get(popIndex));
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
        employedReplaceOperation(popQ, qLocalFitness, qFitness);
    }

    public void employedReplaceOperation(List<Double> popW, List<Double> wLocalFitness, List<Double> wFitness)
            throws InterruptedException {
        Message message = messengers.get(id).getReplaceEmployedValues();
        String[] stringIndexes = message.content.split(" ");
        List<Integer> indexes = new LinkedList<>();
        for (String st : stringIndexes) {
            indexes.add(Integer.parseInt(st));
        }
        int gBestIndex = indexes.get(indexes.size() - 1);
        indexes.remove(indexes.size() - 1);
        if (gBestIndex != -1) {
            X = popW.get(gBestIndex);
        }

        for (int index : indexes) {
            popP.set(index, popW.get(index));
            localFitness.set(index, wLocalFitness.get(index));
            fitness.set(index, wFitness.get(index));
        }
    }

    public Double fitBetween(double z) {
        z = Math.max(z, lowerBound);
        z = Math.min(z, upperBound);
        return z;
    }

    public void build() throws InterruptedException {
        evaluate(popP, localFitness, fitness);
        Message message = messengers.get(id).getGBestUpdateMessage();
        if (!message.type.equals("X")) {
            int index = Integer.parseInt(message.content);
            X = popP.get(index);
        }
        buildElite();
    }

    public void buildElite() throws InterruptedException {
        Message message = messengers.get(id).getEliteSetUpdateMessage();
        String[] values = message.content.split(" ");
        elite = new LinkedList<>();
        for (String value : values) {
            elite.add(Integer.parseInt(value));
        }
    }

    public void initialization() {
        popP = new LinkedList<>();
        localFitness = new LinkedList<>(Collections.nCopies(S, 0.0));
        fitness = new LinkedList<>();
        for (int i = 0; i < S; i++) {
            popP.add(randomDouble(lowerBound, upperBound));
        }
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
        messengers.get(parentId).putUtilityMessage(id, parentId, "ok", wFitness);
    }

    public int randomInt(int l, int r) {
        return l + random.nextInt(r - l + 1);
    }

    public double randomDouble(double l, double r) {
        return l + (r - l) * random.nextDouble();
    }

    public List<Integer> twoRandomAgent() {
        int first = randomInt(0, N - 1);
        int second = randomInt(0, N - 2);
        if (second >= first) {
            ++second;
        }
        return Arrays.asList(first, second);
    }
}