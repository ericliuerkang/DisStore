package PartitionLayer;

import Socket.Message;
import StreamLayer.StreamReportMessage;
import Socket.Storage;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PartitionManager extends Storage {
    int numStreams;
    ConcurrentHashMap<String, Integer> partitionMapTable = new ConcurrentHashMap<>();
    ServerSocket ss;
    ArrayList<Partition> partitionList = new ArrayList<>();
    ArrayList<Integer> streamList = new ArrayList<>();
    String id;
    HashRing hashRing = new HashRing();
    private HashMap<String, Function<Message, Runnable>> handlerMapping = new HashMap<>();

    public PartitionManager(int port){
        super("Partition Manager", port);
    }

    public void handlerMappingSetup(){
        handlerMapping.put("REPORT", this::getReportHandle);
    }

    public Runnable getReportHandle(Message msg){
        final Runnable reportHandle = () -> {
            StreamManagerReportMessage reportMsg;

            try{
                reportMsg = (StreamManagerReportMessage) msg;
                UpdateType type = reportMsg.getType();
                if (type == UpdateType.INIT){
                    numStreams = reportMsg.getNumStreams();
                } else if(type == UpdateType.ADDED_STREAM){
                    //TODO
                } else if(type == UpdateType.REMOVED_STREAM){
                    //TODO
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        };
        return reportHandle;
    }

    public void partitionSetup(int numSpareStream){
        int numStreams = streamList.size();
        int numPartition = numStreams - numSpareStream;
        String pid;
        int pPort;

        for (int i = 0; i < numPartition; i++) {
            pid = "Partition"+String.valueOf(i);
            pPort = streamList.get(i);
            Partition partition = new Partition(pid, pPort, partitionMapTable.get(pid));
            partitionMapTable.put(pid, pPort);
        }
    }
}
