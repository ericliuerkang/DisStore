package StreamLayer;

import Socket.*;
import sun.plugin.net.proxy.PluginProxyInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class StreamManager extends Storage implements IStreamManager{
    private final SocketNode socket;
    private final String id;

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

//    private Runnable setStreamHealthCheck(StreamInfo s) {
//        final Runnable healthcheck = () -> {
//            do {
//                send(s.getPort(), "HEALTHCHECK");
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    return;
//                }
//            } while (receive() != null);
//            s.setStatus(Status.FAILED);
//        };
//        return healthcheck;
//    }
//
//    private final Runnable healthCheck = () -> {
//        ExecutorService executor = Executors.newFixedThreadPool(5);
//        for (StreamInfo s:
//             streams) {
//            executor.execute(setStreamHealthCheck(s));
//        }
//    };

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
            });
        };
        return reportHandle;
    }

    private void listen () {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        while (true) {
            Message msg = receive();
            if (msg == null) {continue;}

            switch (msg.getMessage()) {

                //when a stream report its status
                case "REPORT":
                    executor.execute(getReportHandle(msg));
            }
        }
    }



    public static void main(String[] args){
        StreamManager streamManager = new StreamManager("StreamManager", 59898);
//        ExecutorService executor = Executors.newFixedThreadPool(5);
//        executor.execute(streamManager.healthCheck);
        streamManager.listen();
    }
}
