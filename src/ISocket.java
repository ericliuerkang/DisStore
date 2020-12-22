public interface ISocket {
    void send(String message, int port);
    String receive(int port);
}
