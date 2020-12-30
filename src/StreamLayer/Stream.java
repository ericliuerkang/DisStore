package StreamLayer;

import Socket.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.*;

public class Stream extends Storage implements IStorage {
    private SocketNode socket;
    private final String PROMPT = "\nStream> ";
    private String id;
    private int port;
    protected Status status;

    //Constructors

    public Stream(String id, int port) {
        super(id, port);
        this.id = super.id;
        this.port = super.port;
        this.socket = super.socket;
    }

    public Stream(String id, String address, int port) {
        super(id, address, port);
        this.id = super.id;
        this.port = super.port;
        this.socket = super.socket;
    }

    public int getPort(){return port;}
    public String getId(){return id;}


    //Commandline interface stuff

    private void handleCommand(String cmdLine) {
        System.out.println("command: "+cmdLine);
        String[] args = cmdLine.split("\\s+");
        System.out.println(Arrays.toString(args));
        if (args.length < 1) {
            System.out.println("Enter commands");
        } else if(args.length == 1 && args[0].equals("connect")){
            connect();
        } else if(args.length == 2 && args[0].equals("setup")){
            if (findTable(args[1])) {
                System.out.println("Table named: "+args[1]+" already exists");
            }
            else {
                createTable(args[1]);
            }
        } else if(args.length == 2 && args[0].equals("clear")){
            clear(args[1]);
        } else if (args.length == 4 && args[0].equals("store")) {
            System.out.println("Storing "+args[2]+", "+args[3]+", to table: "+args[1]);
            store(args[1], args[2], args[3]);
            System.out.println("\nStored key: "+ args[2]+ ", value: "+ args[3]);
        } else if (args.length == 3 && args[0].equals("read")) {
            String res = read(args[1], args[2]);
            if (res == null) {
                System.out.println("\nKey not found");
            } else {
                System.out.println("\nValue for key: "+args[1]+ " is: "+ res+"\n");
            }
        } else if (args.length == 3 && args[0].equals("delete")) {
            delete(args[1], args[2]);
        } else if (args.length == 3 && args[0].equals("send")) {
            try {
                int port = Integer.parseInt(args[1]);
                super.sendMessage(id, port, args[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (args.length == 1 && args[0].equals("receive")) {
            Message msg = socket.receive();
            if (msg == null) {
                System.out.println("Didn't receive message");
            }
            else {
                System.out.println("Received " + msg.getMessage() + ", from " + msg.getId());
            }
        } else if (args.length == 1 && args[0].equals("info")) {
            System.out.println("\nPort number: "+String.valueOf(port));
            System.out.print("\n"+Arrays.toString(socket.getMsgQ().toArray()));
            System.out.println("\nThere are "+String.valueOf(numTables)+" tables");
        }
    }

    private Runnable run = () -> {

        while (true) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(PROMPT);

            try {
                String cmdLine = stdin.readLine();
                if (cmdLine.equals("disconnect")) {
                    disconnect();
                    break;
                }
                this.handleCommand(cmdLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable getAddTableHandle(Message msg) {
        final Runnable reportHandle = () -> {
            createTable(String.valueOf(numTables));
        };
        return reportHandle;
    }

    private final Runnable listen = () -> {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        while (true) {
            Message msg = socket.receive();
            if (msg == null) {continue;}

            switch (msg.getMessage()) {

                //when a stream report its status
                case "ADDTABLE":
                    executor.execute(getAddTableHandle(msg));
            }
        }
    };

    private final Runnable periodicReport = () -> {
//        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        final Runnable sendReport = () -> {

        while (true){
            System.out.println("Sending report message");
            ReportMessage msg = new ReportMessage("REPORT", id, port, numTables);
            send(id, 59898, msg);
//            try {
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                System.out.println("got here");
//            }
        }
//        final ScheduledFuture<?> reportHandle = scheduler.scheduleAtFixedRate(sendReport, 0, 1, TimeUnit.SECONDS);
//        scheduler.schedule(sendReport, 1, TimeUnit.SECONDS);

    };

    public static void main(String[] args){
        /* conenct
           store foo bar
           read foo
           delete foo
           clear
           disconnect
         */

        try {
            System.out.println("Storage Initializing");
            int port = Integer.parseInt(args[0]);
            StreamLayer.Stream stream = new StreamLayer.Stream("C:\\StreamLayer.Storage\\test.txt", port);
            System.out.println("Storage Initialized");
            ExecutorService executor = Executors.newFixedThreadPool(3);
//            executor.execute(stream.run);
//            executor.execute(stream.listen);
            executor.execute(stream.periodicReport);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
