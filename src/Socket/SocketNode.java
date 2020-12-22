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
    private PriorityQueue<Message> msgQ;

    public SocketNode(InetAddress address, int port){
        this.address = address;
        this.port = port;
        this.msgQ = new PriorityQueue<Message>();
        try {
            this.ss = new ServerSocket(port);
//            run(address, listenPort);
            ExecutorService pool = Executors.newFixedThreadPool(3);
            listener = new Listen(ss, msgQ);
            pool.execute(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Listen implements Runnable {
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

    private static class ThreadListen implements Runnable {
        private final Socket socket;
        private final PriorityQueue<Message> msgQ;

        ThreadListen(Socket socket, PriorityQueue<Message> msgQ) throws IOException {
            this.socket = socket;
            this.msgQ = msgQ;
        }

        @Override
        public void run() {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    readMsg(in, msgQ);
                }
            } catch (IOException e) {
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

        private synchronized void readMsg(ObjectInputStream in, PriorityQueue<Message> msgQ){
            try {
                Message msg = (Message) in.readObject();
                System.out.println("\nReceived " + msg.getMessage() + " from " + msg.getId());
                msgQ.add(msg);
            } catch (EOFException eof) {
//                System.out.println("EOF");
            }

            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Message receive() {
        return msgQ.poll();
    }

    public void send(String id, int destPort, String msg){
        while (true) {
            try (Socket socket = new Socket("localhost", destPort);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());) {
                out.writeObject(new Message(msg, id));
                return;
            } catch (Exception e) { }
        }
    }
}