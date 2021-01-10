package StreamLayer;

import Socket.*;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.function.Function;

public class StreamManager extends Storage implements IStreamManager{
    private final SocketNode socket;
    private final String id;
    private HashMap<String, Function<Message, Runnable>> handlerMapping = new HashMap<>();

    private static class StreamInfo{
        public String id;
        private int port;
        private Status status = Status.IDLE;
        private int numTables;
        public long timeMillis;

        public StreamInfo(String id, int port){
            this.id = id;
            this.port = port;
            status = Status.IDLE;
            numTables = 0;
            timeMillis = System.currentTimeMillis();
        }

        public StreamInfo(String id, StreamReportMessage.Report report){
            this.id = id;
            this.port = report.getPort();
            status = Status.IDLE;
            numTables = report.getNumTables();
            timeMillis = System.currentTimeMillis();
        }

        public void updateInfo(StreamReportMessage.Report report) {
            numTables = report.getNumTables();
            timeMillis = System.currentTimeMillis();
        }

        public int getPort() {return port;}

        public void setStatus(Status s){this.status = s;}
        public Status getStatus(){return status;}
    }

    private final ConcurrentHashMap<String, StreamInfo> streams = new ConcurrentHashMap<>();

    public StreamManager(String id, int port) {
        super(id, port);
        this.socket = super.socket;
        this.id = super.id;
    }

    public void handlerMappingSetup(){
        handlerMapping.put("REPORT", this::getReportHandle);
    }

    @Override
    public void send(int destPort, String msg) {
        super.sendMessage(id, destPort, msg);
    }

    @Override
    public Message receive() {
        return null;
    }

    @Override
    public void addTable(int streamID) {

    }

    @Override
    public void garbageCollect() {

    }

    @Override
    public void notifyStreamChange(int nodeID) {

    }


    private Runnable getReportHandle(Message msg) {
        final Runnable reportHandle = () -> {
//            System.out.println("Got report msg: "+msg.getId());
            StreamReportMessage reportMsg;
            String id = null;
            StreamReportMessage.Report report;
            try {
                reportMsg = (StreamReportMessage) msg;
                id = reportMsg.getId();
                report = reportMsg.getReport();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            StreamInfo stream = streams.get(id);
            if (stream == null) {
                System.out.println("New Stream Online: " + id);
                StreamInfo newStream = new StreamInfo(id, report);
                streams.put(id, newStream);
            } else {
                streams.computeIfPresent(id, (key, value)->{
                    value.timeMillis = System.currentTimeMillis();
                    value.updateInfo(report);
                    return value;
                });
            }
        };
        return reportHandle;
    }


    private final Runnable listener = super.getListener(handlerMapping);

    private final Runnable healthCheck = () -> {
        System.out.println("Health Check Running");
        while (true) {
            long currTime = System.currentTimeMillis();
            streams.forEachEntry(2, (entry)->{
                StreamInfo stream = entry.getValue();
                System.out.println("Checking " + stream.id + ", time is " + stream.timeMillis + ", currTime is " + currTime);
                if (currTime - stream.timeMillis >= 2000) {
                    System.out.println("Stream " + stream.id + " is offline");
                    streams.remove(entry.getKey());
                }
            });
        }
    };

    public static void main(String[] args){
        StreamManager streamManager = new StreamManager("StreamManager", 59898);
        streamManager.handlerMappingSetup();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.execute(streamManager.getListener(streamManager.handlerMapping));
        executor.execute(streamManager.healthCheck);
    }
}
