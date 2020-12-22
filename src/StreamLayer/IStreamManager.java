package StreamLayer;

public interface IStreamManager {
    void isFull(int streamID);
    void addTable(int streamID);
    void garbageCollect();
}
