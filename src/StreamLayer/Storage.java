package StreamLayer;

import Socket.Message;
import Socket.SocketNode;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.sql.*;

public class Storage implements IStorage{
    Properties prop = new Properties();
    String id;
    private final String PROMPT = "Storage> ";
    Connection c;
    Statement s;
    SocketNode socket;

    public Storage(String id){
        this.id = id;
        try {
            System.out.println("Socket node initializing");
            socket = new SocketNode(InetAddress.getLocalHost(), 59898);
            System.out.println("Socket node initialized");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public Storage(String id, String address, int port){
        this.id = id;
        try {
            socket = new SocketNode(InetAddress.getByName(address), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            // 建立与数据库的Connection连接
            // 这里需要提供：
            // 数据库所处于的ip:127.0.0.1 (本机)
            // 数据库的端口号： 3306 （mysql专用端口号）
            // 数据库名称 how2java
            // 编码方式 UTF-8
            // 账号 root
            // 密码 admin

            c = DriverManager
                    .getConnection(
                            "jdbc:mysql://127.0.0.1:3306/filestore?characterEncoding=UTF-8",
                            "root", "admin");
            s = c.createStatement();
            System.out.println("Connection established： " + c);
            System.out.println("Statement created:  "+s);

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean findTable(String tableName) {
        try {
            String sql = "select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = '"+tableName+"'";
            System.out.println("executing: " + sql);
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                String res = rs.getString("TABLE_SCHEMA");
                System.out.println("Found table in schema: "+res);
                return true;
            } else {
                System.out.println("No table");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void listTable() {
        try {
            String sql = "select * from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = 'filestore'";
            System.out.println("executing: " + sql);
            ResultSet rs = s.executeQuery(sql);
            String res;
            while (rs.next()) {
                res = rs.getString("TABLE_NAME");
                System.out.println("Found table: "+res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable(String tableName) {
        String sql = " CREATE TABLE "+tableName+" ("+
                "id int(11) AUTO_INCREMENT,"+
                "k varchar(30) ,"+
                "v varchar(30) ,"+
                "PRIMARY KEY (id)"+
                ")  DEFAULT CHARSET=utf8";
        execute(sql);
    }

    public void deleteTable(String tableName) {
        String sql = "DROP TABLE "+tableName;
        execute(sql);
    }

    private void execute(String sql) {
        if (c==null) {
            System.out.println("Connection not established");
            return;
        }
        System.out.println("executing: "+sql);
        try {
            s.execute(sql);
            System.out.println("executed: "+sql);
        } catch (SQLException e){
            System.out.println("faaaak");
            e.printStackTrace();
        }
    }
    @Override
    public void store(String tableName, String key, String value) {
        String sql = "insert into "+tableName+" values(null,'"+key+"', '"+value+"')";
        System.out.println(sql);
        execute(sql);
    }

    @Override
    public void update(String tableName, String key, String value) {
        String sql = "update "+tableName+" set v = "+value+" where k = '"+key+"'";
    }

    @Override
    public String read(String tableName, String key) {
        if (c==null) {
            System.out.println("Connection not established");
            return null;
        }
        try {
            String sql = "select * from "+tableName+" where k = '"+key+"'";
            System.out.println("executing: "+sql);
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                String res = rs.getString("v");
                System.out.println("Found value: "+res+", for key: "+key);
                return res;
            } else {
                System.out.println("Key: "+key+" cannot be found");
                return null;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete(String tableName, String key) {
        String sql = "delete from "+tableName+" where k = '"+key+"'";
        execute(sql);
    }

    @Override
    public void disconnect() {
        try {
            if (s != null) {
                s.close();
                s = null;
            }
            if (c != null) {
                c.close();
                c = null;
            }
            System.out.println("Successfully disconnected");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void clear(String tableName) {
        String sql = "delete from "+tableName;
        execute(sql);
    }

    private void run(){
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
    }

    private void handleCommand(String cmdLine) {
        System.out.println("command: "+cmdLine);
        String[] args = cmdLine.split("\\s+");

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
        } else if (args.length == 2 && args[0].equals("send")) {
            socket.send(id, args[1]);
        } else if (args.length == 1 && args[0].equals("receive")) {
            Message msg = socket.receive();
            if (msg == null) {
                System.out.println("Didn't receive message");
            }
            else {
                System.out.println("Received " + msg.getMessage() + ", from " + msg.getId());
            }
        }
    }

    public static void main(String[] args){
        /* conenct
           store foo bar
           read foo
           delete foo
           clear
           disconnect
         */


        System.out.println("Storage Initializing");
        Storage storage = new Storage("C:\\StreamLayer.Storage\\test.txt");
        System.out.println("Storage Initialized");
        storage.run();
    }
}
