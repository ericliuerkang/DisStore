package Socket;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketNode {
    private ServerSocket ss;
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public SocketNode(InetAddress address, int port){
        try {
            this.ss = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerSocket getServerSocket(){return ss;}


    public void send(String id, int destPort, String msg){
        while (true) {
            try (Socket socket = new Socket("localhost", destPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(new Message(msg, id));
                return;
            } catch (Exception ignored) { }
        }
    }

    public void send(int destPort, Message msg){
        while (true) {
            try (Socket socket = new Socket("localhost", destPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(msg);
                System.out.println("sent message");
                return;
            } catch (Exception ignored) { }
        }
    }
}