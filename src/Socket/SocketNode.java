package Socket;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketNode {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final InetAddress address;
    private final int port;
    private ServerSocket ss;
    private Listen listener;

    public SocketNode(InetAddress address, int sendPort, int listenPort){
        this.address = address;
        this.port = sendPort;
        try {
            this.ss = new ServerSocket(listenPort);
//            run(address, listenPort);
            ExecutorService pool = Executors.newFixedThreadPool(3);
            listener = new Listen(ss.accept());
            pool.execute(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Listen implements Runnable {
        private final Socket socket;
        private final PriorityQueue<Message> msgQ;

        Listen(Socket socket) throws IOException {
            this.socket = socket;
            this.msgQ = new PriorityQueue<Message>();
        }

        Listen(Socket socket, int queueSize) throws IOException {
            this.socket = socket;
            this.msgQ = new PriorityQueue<Message>(queueSize);
        }

        @Override
        public void run() {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    try {
                        Message msg = (Message) in.readObject();
                        msgQ.add(msg);
                        System.out.println("Received " + msg.getMessage() + " from " + msg.getId());
                    } catch (EOFException e) {
//                        continue;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in!=null) {
                        in.close();
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Message receive() {
        return listener.msgQ.poll();
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