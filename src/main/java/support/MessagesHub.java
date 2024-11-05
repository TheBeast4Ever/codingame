package support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesHub {
    private Map<Integer, List<Message>> messages;

    public MessagesHub() {
        messages = new HashMap<Integer, List<Message>>();
    }

    public void pub(Message message) {
        List<Message> messageForSamePriority = messages.containsKey(message.priority)?messages.get(message.priority):new ArrayList<Message>();
        messageForSamePriority.add(message);
        messages.put(message.priority, messageForSamePriority);
        System.err.println("New message : " + message);
    }


}
