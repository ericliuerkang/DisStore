package Socket;

import java.io.Serializable;

public class ReportMessage extends Message implements Serializable, Comparable<Message> {

    public static class Report implements Serializable{
        private int port;
        private int numTables;
        public Report(int port, int numTables) {
            this.port = port;
            this.numTables = numTables;
        }
        public int getPort(){return port;}
        public int getNumTables(){return numTables;}
    }

    public Report report;

    public ReportMessage(String message, String id) {
        super(message, id);
    }

    public ReportMessage(String message, String id, int priority) {
        super(message, id, priority);
    }

    public ReportMessage(String message, String id, int port, int numTables){
        super(message, id);
        this.report = new Report(port, numTables);
    }

    public ReportMessage(String message, String id, int priority, int port, int numTables) {
        super(message, id, priority);
        this.report = new Report(port, numTables);
    }

    public Report getReport(){return  report;}
}
