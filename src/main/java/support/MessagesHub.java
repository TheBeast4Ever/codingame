package support;

import java.util.*;

public class MessagesHub {
    private Map<Integer, List<Message>> oldMessages;

    private Queue<Message> messages;

    public MessagesHub() {
        messages = new ArrayDeque<Message>();
        oldMessages = new HashMap<Integer, List<Message>>();
    }

    public void pub(Message message) {
        messages.add(message);
    }

    public Message consumeWithoutRemove() {
        return messages.peek();
    }
    public Message consumeAndRemove() {
        return messages.poll();
    }

    public void oldPub(Message message) {
        List<Message> messageForSamePriority = oldMessages.containsKey(message.priority)? oldMessages.get(message.priority):new ArrayList<Message>();
        messageForSamePriority.add(message);
        oldMessages.put(message.priority, messageForSamePriority);
        System.err.println("New message : " + message);
    }

}
