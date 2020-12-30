package Socket;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
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
    private PriorityQueue<Message> msgQ;
    private int timeStamp;
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public SocketNode(InetAddress address, int port){
        this.address = address;
        this.port = port;
        this.msgQ = new PriorityQueue<Message>();
        timeStamp = 0;
        try {
            this.ss = new ServerSocket(port);
//            run(address, listenPort);
//            ExecutorService pool = Executors.newFixedThreadPool(3);
            listener = new Listen(ss, msgQ);
            executor.execute(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable getListener (HashMap<String, Runnable> map) {
        final Runnable listener = ()->{
            try {
                Socket client = ss.accept();
                Message msg = (Message) new ObjectInputStream(client.getInputStream()).readObject();
                Runnable runnable = map.get(msg.getMessage());
                if (runnable!=null) {
                    executor.execute(runnable);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };
        return listener;
    }

    private class Listen implements Runnable {
        private final ServerSocket ss;
        private final PriorityQueue<Message> msgQ;

        Listen(ServerSocket ss, PriorityQueue<Message> msgQ){
            this.ss = ss;
            this.msgQ = msgQ;
        }

        @Override
        public void run() {
            ExecutorService pool = Executors.newFixedThreadPool(3);
            while (true) {
                try {
                    pool.execute(new ThreadListen(ss.accept(), msgQ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ThreadListen implements Runnable {
        private final Socket socket;
        private final PriorityQueue<Message> msgQ;

        ThreadListen(Socket socket, PriorityQueue<Message> msgQ) throws IOException {
            this.socket = socket;
            this.msgQ = msgQ;
        }

        @Override
        public void run() {
            readSocket(socket, msgQ);
        }
    }

    public static void readSocket(Socket socket, PriorityQueue<Message> msgQ) {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message msg = null;
        try {
            assert in != null;
            do {
                msg = (Message) in.readObject();
                System.out.println("\nReceived " + msg.getMessage() + " from " + msg.getId());
                msgQ.add(msg);
            }
            while (msg != null);
        } catch (EOFException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    System.out.println("\nSuccessfully closed input stream");
                }
                socket.close();
                System.out.println("\nSuccessfully closed socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Message receive() {
        return msgQ.poll();
    }

    public PriorityQueue<Message> getMsgQ() {return msgQ;}

    public void send(String id, int destPort, String msg){
        while (true) {
            try (Socket socket = new Socket("localhost", destPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(new Message(msg, id));
                return;
            } catch (Exception e) { }
        }
    }

    public void send(String id, int destPort, Message msg){
        while (true) {
            try (Socket socket = new Socket("localhost", destPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(msg);
                System.out.println("sent message");
                return;
            } catch (Exception e) {e.printStackTrace(); }
        }
    }
}