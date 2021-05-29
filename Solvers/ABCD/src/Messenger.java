import java.util.*;
import java.util.stream.Collectors;

public class Messenger {
    final int MAX_QUEUE_SIZE = 1000;
    final int MAX_AGENT = 200;

    List<Queue<Message>> utilityMessage;
    Queue<Message> employedUpdateMessage;
    Queue<Message> onlookerUpdateMessage;
    Queue<Message> scoutUpdateMessage;
    Queue<Message> eliteSetUpdateMessage;
    List<Queue<Message>> neighborValuesMessage;
    Queue<Message> gBestUpdateMessage;
    Queue<Message> reqEmployedValuesMessage;
    Queue<Message> receivedEmployedValuesMessage;
    Queue<Message> replaceEmployedValuesMessage;
    Queue<Message> copyOnlookerSolutionMessage;
    Queue<Message> reqOnlookerValuesMessage;
    Queue<Message> receivedOnlookerValuesMessage;
    Queue<Message> replaceOnlookerValuesMessage;
    Queue<Message> replaceScoutMessage;

    public Messenger() {
        utilityMessage = new LinkedList<>();
        for (int i = 0; i < MAX_AGENT; i++) {
            utilityMessage.add(new LinkedList<>());
        }
        neighborValuesMessage = new LinkedList<> ();
        for (int i = 0; i < MAX_AGENT; i++) {
            neighborValuesMessage.add(new LinkedList<>());
        }

        employedUpdateMessage = new LinkedList<>();
        onlookerUpdateMessage = new LinkedList<>();
        scoutUpdateMessage = new LinkedList<>();
        eliteSetUpdateMessage = new LinkedList<>();
        gBestUpdateMessage = new LinkedList<>();
        reqEmployedValuesMessage = new LinkedList<>();
        receivedEmployedValuesMessage = new LinkedList<>();
        replaceEmployedValuesMessage = new LinkedList<>();
        copyOnlookerSolutionMessage = new LinkedList<>();
        reqOnlookerValuesMessage = new LinkedList<>();
        receivedOnlookerValuesMessage = new LinkedList<>();
        replaceOnlookerValuesMessage = new LinkedList<>();
        replaceScoutMessage = new LinkedList<>();
    }

    public synchronized void putScoutUpdateMessage(int senderId, int receiverId, String type, List<Integer> indexes)
            throws InterruptedException {
        List<String> stringValues = indexes.stream().map(Object::toString).collect(Collectors.toList());
        while (replaceScoutMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        replaceScoutMessage.add(new Message(senderId, receiverId, type, String.join(" ", stringValues)));
        notifyAll();
    }

    public synchronized Message getScoutUpdateMessage() throws InterruptedException {
        while (replaceScoutMessage.isEmpty()) {
            wait();
        }
        Message message = replaceScoutMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putReplaceOnlookerValuesMessage(int senderId, int receiverId, String type, int uIndex,
                                                             int rIndex) throws InterruptedException {
        String content = uIndex + " " + rIndex;
        while (replaceOnlookerValuesMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        replaceOnlookerValuesMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getReplaceOnlookerValuesMessage() throws InterruptedException {
        while (replaceOnlookerValuesMessage.isEmpty()) {
            wait();
        }
        Message message = replaceOnlookerValuesMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putOnlookerUpdateMessage(int senderId, int receiverId, String type, int u, int m,
                                                      double emh, double puh) throws InterruptedException {
        String content = u + " " + m + " " + emh + " " + puh;
        while (onlookerUpdateMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        onlookerUpdateMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getOnlookerUpdateMessage() throws InterruptedException {
        while (onlookerUpdateMessage.isEmpty()) {
            wait();
        }
        Message message = onlookerUpdateMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putReceivedOnlookerValuesMessage(int senderId, int receiverId, String type, double emh,
                                                              double puh) throws InterruptedException {
        String content = emh + " " + puh;
        while (receivedOnlookerValuesMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        receivedOnlookerValuesMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getReceivedOnlookerValuesMessage() throws InterruptedException {
        while (receivedOnlookerValuesMessage.isEmpty()) {
            wait();
        }
        Message message = receivedOnlookerValuesMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putReqOnlookerValuesMessage(int senderId, int receiverId, String type, int eliteIndex,
                                                         int popIndex) throws InterruptedException {
        String content = eliteIndex + " " + popIndex;
        while (reqOnlookerValuesMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        reqOnlookerValuesMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getReqOnlookerValuesMessage() throws InterruptedException {
        while (reqOnlookerValuesMessage.isEmpty()) {
            wait();
        }
        Message message = reqOnlookerValuesMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putCopyOnlookerSolutionMessage(int senderId, int receiverId, String type, int solutionIndex)
            throws InterruptedException {
        while (copyOnlookerSolutionMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        copyOnlookerSolutionMessage.add(new Message(senderId, receiverId, type, String.valueOf(solutionIndex)));
        notifyAll();
    }

    public synchronized Message getCopyOnlookerMessage() throws InterruptedException {
        while (copyOnlookerSolutionMessage.isEmpty()) {
            wait();
        }
        Message message = copyOnlookerSolutionMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putReplaceEmployedValues(int senderId, int receiverId, String type, List<Integer> index)
            throws InterruptedException {
        List<String> stringValues = index.stream().map(Object::toString).collect(Collectors.toList());
        while (replaceEmployedValuesMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        replaceEmployedValuesMessage.add(new Message(senderId, receiverId, type, String.join(" ", stringValues)));
        notifyAll();
    }

    public synchronized Message getReplaceEmployedValues() throws InterruptedException {
        while (replaceEmployedValuesMessage.isEmpty()) {
            wait();
        }
        Message message = replaceEmployedValuesMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putReceivedEmployedValuesMessage(int senderId, int receiverId, String type, double elh,
                                                              double puh) throws InterruptedException {
        String content = elh + " " + puh;
        while (receivedEmployedValuesMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        receivedEmployedValuesMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getReceivedEmployedValuesMessage() throws InterruptedException {
        while (receivedEmployedValuesMessage.isEmpty()) {
            wait();
        }
        Message message = receivedEmployedValuesMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putEmployedUpdateMessage(int senderId, int receiverId, String type, int u, int l,
                                                      double elh, double puh) throws InterruptedException {
        String content = u + " " + l + " " + elh + " " + puh;
        while (employedUpdateMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        employedUpdateMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getEmployedUpdateMessage() throws InterruptedException {
        while (employedUpdateMessage.isEmpty()) {
            wait();
        }
        Message message = employedUpdateMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putReqEmployedValuesMessage(int senderId, int receiverId, String type, int eliteIndex,
                                                         int popIndex) throws InterruptedException {
        String content = eliteIndex + " " + popIndex;
        while (reqEmployedValuesMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        reqEmployedValuesMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getReqEmployedValueMessage() throws InterruptedException {
        while (reqEmployedValuesMessage.isEmpty()) {
            wait();
        }
        Message message = reqEmployedValuesMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putUtilityMessage(int senderId, int receiverId, String type, List<Double> utility)
            throws InterruptedException {
        List<String> stringValues = utility.stream().map(Object::toString).collect(Collectors.toList());
        while (utilityMessage.get(senderId).size() == MAX_QUEUE_SIZE) {
            wait();
        }
        utilityMessage.get(senderId).add(new Message(senderId, receiverId, type,
                String.join(" ", stringValues)));
        notifyAll();
    }

    public synchronized Message getUtilityMessage(int from) throws InterruptedException {
        while (utilityMessage.get(from).isEmpty()) {
            wait();
        }
        Message message = utilityMessage.get(from).poll();
        notifyAll();
        return message;
    }

    public synchronized void putNeighborValues(int senderId, int receiverId, String type, List<Double> values)
            throws InterruptedException {
        List<String> stringValues = values.stream().map(Object::toString).collect(Collectors.toList());
        while (neighborValuesMessage.get(senderId).size() == MAX_QUEUE_SIZE) {
            wait();
        }
        neighborValuesMessage.get(senderId).add(new Message(senderId, receiverId, type,
                String.join(" ", stringValues)));
        notifyAll();
    }

    public synchronized Message getNeighborValues(int from) throws InterruptedException {
        while (neighborValuesMessage.get(from).isEmpty()) {
            wait();
        }
        Message message = neighborValuesMessage.get(from).poll();
        notifyAll();
        return message;
    }

    public synchronized void putGBestUpdateMessage(int senderId, int receiverId, String type, int index)
            throws InterruptedException {
        while (gBestUpdateMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        String content = null;
        if (!type.equals("X")) {
            content = String.valueOf(index);
        }
        gBestUpdateMessage.add(new Message(senderId, receiverId, type, content));
        notifyAll();
    }

    public synchronized Message getGBestUpdateMessage() throws InterruptedException {
        while (gBestUpdateMessage.isEmpty()) {
            wait();
        }
        Message message = gBestUpdateMessage.poll();
        notifyAll();
        return message;
    }

    public synchronized void putEliteSetUpdateMessage(int senderId, int receiverId, String type, List<Integer> set)
            throws InterruptedException {
        List<String> stringValues = set.stream().map(Object::toString).collect(Collectors.toList());
        while (eliteSetUpdateMessage.size() == MAX_QUEUE_SIZE) {
            wait();
        }
        eliteSetUpdateMessage.add(new Message(senderId, receiverId, type, String.join(" ", stringValues)));
        notifyAll();
    }

    public synchronized Message getEliteSetUpdateMessage() throws InterruptedException {
        while (eliteSetUpdateMessage.isEmpty()) {
            wait();
        }
        Message message = eliteSetUpdateMessage.poll();
        notifyAll();
        return message;
    }
}
