package Socket;

import java.io.Serializable;

public class DataMessage extends Message implements Serializable {

    public class Data implements Serializable{
        public String key;
        public String value;

        public Data(String key, String value){
            this.key = key;
            this.value = value;
        }
    }
    private Data data;

    public DataMessage(String message, String id, String key, String value) {
        super(message, id);
        data = new Data(key, value);
    }

    public DataMessage(String message, String id, int priority, String key, String value) {
        super(message, id, priority);
        data = new Data(key, value);
    }

    public Data getData(){return data;}
}
