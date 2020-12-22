package Socket;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.PriorityQueue;


public class SocketNode {
    private Socket socket;
    private PriorityQueue<Message> msgQ;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private InetAddress address;
    private int port;

    public SocketNode(InetAddress address, int port){
        this.address = address;
        this.port = port;
    }

    public SocketNode(InetAddress address, int port, int queueSize){
        try {
            socket = new Socket(address, port);
            msgQ = new PriorityQueue<Message>(queueSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void run(Socket socket) throws IOException, ClassNotFoundException {
        List<Message> listOfMessages = (List<Message>) in.readObject();
        listOfMessages.forEach((msg) ->msgQ.add(msg));
        listOfMessages.forEach((msg) ->System.out.println("Received "+msg.getMessage()+" from "+msg.getId()));
    }

    public Message receive() {
        return msgQ.poll();
    }

    public void send(Message msg) throws IOException {
        try (Socket socket = new Socket(address, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());){
            out.writeObject(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String id, String msg){
        try (Socket socket = new Socket(address, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());){
            out.writeObject(new Message(msg, id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}