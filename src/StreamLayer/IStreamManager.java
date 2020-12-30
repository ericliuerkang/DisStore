package StreamLayer;

import Socket.Message;

public interface IStreamManager {
    void send(int destPort, String msg);
    Message receive();
    void addTable(int streamID);
    void garbageCollect();
    void notifyStreamChange(int nodeID);
}
