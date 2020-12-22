package Socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A server program which accepts requests from clients to capitalize strings.
 * When a client connects, a new thread is started to handle it. Receiving
 * client data, capitalizing it, and sending the response back is all done on
 * the thread, allowing much greater throughput because more clients can be
 * handled concurrently.
 */
public class MultiClientServer {

    /**
     * Runs the server. When a client connects, the server spawns a new thread to do
     * the servicing and immediately returns to listening. The application limits
     * the number of threads via a thread pool (otherwise millions of clients could
     * cause the server to run out of resources by allocating too many threads).
     */
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(59898)) {
            System.out.println("The capitalization server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new Capitalizer(listener.accept()));
            }
        }
    }

    private static class Capitalizer implements Runnable {
        private Socket socket;

        Capitalizer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            try {
//                Scanner in = new Scanner(socket.getInputStream());
//                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                while (in.hasNextLine()) {
//                    String inline = in.nextLine();
//                    String outline = inline.toUpperCase();
//                    System.out.println("\nreceived: "+inline+"\nsent: "+outline);
//                    out.println(outline);
//                }
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                OutputStream out = socket.getOutputStream();

//                List<Message> listOfMessages = (List<Message>) in.readObject();
                Message msg = (Message) in.readObject();
                System.out.println("Received "+msg.getMessage()+" from "+msg.getId());
//                listOfMessages.forEach((msg) ->System.out.println("Received "+msg.getMessage()+" from "+msg.getId()));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error:" + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Closed: " + socket);
            }
        }
    }
}