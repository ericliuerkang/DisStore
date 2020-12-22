package Socket;

import java.io.Serializable;

public class Message extends Object implements Serializable {
    private final String message;
    private final String id;

    public Message(String message, String id){
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
