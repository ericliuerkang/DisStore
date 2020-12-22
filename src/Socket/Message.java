package Socket;

import java.io.Serializable;

public class Message extends Object implements Serializable, Comparable<Message>{
    private final String message;
    private final String id;
    private final int priority;

    public Message(String message, String id){
        this.message = message;
        this.id = id;
        this.priority = 0;
    }

    public Message(String message, String id, int priority){
        this.message = message;
        this.id = id;
        this.priority = priority;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    @Override
    public int compareTo(Message o) {
        return Integer.compare(priority, o.priority);
    }
}
