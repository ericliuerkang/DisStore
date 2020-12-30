package StreamLayer;

import Socket.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        public StreamInfo(String id, ReportMessage.Report report){
            this.id = id;
            this.port = report.getPort();
            status = Status.IDLE;
            numTables = report.getNumTables();
            timeMillis = System.currentTimeMillis();
        }

        public void updateInfo(ReportMessage.Report report) {
            numTables = report.getNumTables();
            timeMillis = System.currentTimeMillis();
        }

        public int getPort() {return port;}

        public void setStatus(Status s){this.status = s;}
        public Status getStatus(){return status;}
    }

    private final List<StreamInfo> streams = new ArrayList<StreamInfo>();

    public StreamManager(String id, int port) {
        super(id, port);
        this.socket = super.socket;
        this.id = super.id;
    }

    public void handlerMappingSetup(){
        Function<Message, Runnable> func = this::getReportHandle;
        assert (func!=null);
        handlerMapping.put("REPORT", func);
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
        System.out.println("Got report msg: "+msg.getId());
        final Runnable reportHandle = () -> {
            ReportMessage reportMsg;
            try {
                reportMsg = (ReportMessage) msg;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            streams.forEach(stream -> {
                if (stream.id == reportMsg.getId()) {
                    stream.timeMillis = System.currentTimeMillis();
                    stream.updateInfo(reportMsg.getReport());
                    return;
                }
                System.out.println("New Stream Online"+reportMsg.getId());

                StreamInfo newStream = new StreamInfo(reportMsg.getId(), reportMsg.getReport());
                streams.add(newStream);
            });
        };
        return reportHandle;
    }


    private Runnable listener = super.getListener(handlerMapping);



    public static void main(String[] args){
        StreamManager streamManager = new StreamManager("StreamManager", 59898);
        streamManager.handlerMappingSetup();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.execute(streamManager.getListener(streamManager.handlerMapping));
    }
}
