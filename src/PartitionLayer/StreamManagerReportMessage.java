package PartitionLayer;

import Socket.Message;
import StreamLayer.StreamManager;

import java.io.Serializable;

public class StreamManagerReportMessage extends Message implements Serializable, Comparable<Message> {
    private int numStreams;
    private UpdateType type;
    private String streamID;
    private int streamPort;

    public StreamManagerReportMessage(String message, String id, UpdateType type, String streamID, int streamPort, int numStreams) {
        super(message, id);
        this.type = type;
        this.streamPort = streamPort;
        this.streamID = streamID;
        this.numStreams = numStreams;
    }

    public StreamManagerReportMessage(String message, String id, int priority, UpdateType type, String streamID, int streamPort, int numStreams) {
        super(message, id, priority);
        this.type = type;
        this.streamPort = streamPort;
        this.streamID = streamID;
        this.numStreams = numStreams;
    }

    public UpdateType getType(){return type;}
    public String getStreamID(){return streamID;}
    public int getStreamPort(){return streamPort;}
    public int getNumStreams(){return numStreams;}
}