package PartitionLayer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import Socket.Message;
import Socket.DataMessage;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

public class Partition {
    int streamPort;
    String id;
    ServerSocket ss;
    ConcurrentHashMap<String, String> readBuffer = new ConcurrentHashMap<>();

    public Partition(String id, int port, int streamPort){
        try{
            ss = new ServerSocket(port);
        } catch (IOException e){
            e.printStackTrace();
        }
        this.id = id;
        this.streamPort = streamPort;
    }

    private void send(int destPort, String msg){
        while (true) {
            try (Socket socket = new Socket("localhost", destPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(new Message(msg, id));
                return;
            } catch (Exception ignored) { }
        }
    }

    private void send(int destPort, String msg, String key, String value){
        while (true) {
            try (Socket socket = new Socket("localhost", destPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(new DataMessage(msg, id, key, value));
                return;
            } catch (Exception ignored) { }
        }
    }


    protected void store(String key, String value){
        send(streamPort, "STORE", key, value);
    }

    protected void update(String key, String value){
        send(streamPort, "UPDATE", key, value);
    }

    protected String read(String key) {
        send(streamPort, "READ", key, "");
        String tmp = readBuffer.get(key);
        if (tmp != null){
            readBuffer.remove(key);
            return tmp;
        }
        while (true) {
            try{
                Socket client = ss.accept();
                DataMessage msg = (DataMessage) new ObjectInputStream(client.getInputStream()).readObject();
                if (msg == null) {continue;}
                if (msg.getData().key.equals(key)) {
                    return msg.getData().value;
                } else {
                    readBuffer.put(key, msg.getData().value);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    protected void delete(String key){
        send(streamPort, "DELETE", key, "");
    }
}
