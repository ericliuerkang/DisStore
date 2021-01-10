package Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Storage {
    public String id;
    private Connection c;
    private Statement s;
    public SocketNode socket;
    public int port;
    protected int numTables;

    //Constructors

    public Storage(String id, int port){
        this.id = id;
        this.port = port;
        connect();
        numTables = listTable();
        System.out.println("There are "+String.valueOf(numTables)+" tables");
        try {
            System.out.println("Socket node initializing");
            socket = new SocketNode(InetAddress.getLocalHost(), port);
            System.out.println("Socket node initialized");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public Storage(String id, String address, int port){
        this.id = id;
        connect();
        numTables = listTable();
        System.out.println("There are "+String.valueOf(numTables)+" tables");
        try {
            socket = new SocketNode(InetAddress.getLocalHost(), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //Database connection

    public void connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            // database ip: 127.0.0.1
            // database port: 3306 (mysql port)
            // database name: filestore
            // encoding: UTF-8
            // username: root
            // password: admin

            c = DriverManager
                    .getConnection(
                            "jdbc:mysql://127.0.0.1:3306/filestore?characterEncoding=UTF-8",
                            "root", "admin");
            s = c.createStatement();
            System.out.println("Connection established： " + c);
            System.out.println("Statement created:  "+s);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


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

    //Table related

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

    public int listTable() {
        try {
            String sql = "select * from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = 'filestore'";
            System.out.println("executing: " + sql);
            ResultSet rs = s.executeQuery(sql);
            String res;
            int cnt = 0;
            while (rs.next()) {
                res = rs.getString("TABLE_NAME");
                System.out.println("Found table: "+res);
                cnt += 1;
            }
            return cnt;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
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
        numTables += 1;
    }

    public void deleteTable(String tableName) {
        String sql = "DROP TABLE "+tableName;
        execute(sql);
        numTables -= 1;
    }

    //CRUD and clear

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

    public void store(String tableName, String key, String value) {
        String sql = "insert into "+tableName+" values(null,'"+key+"', '"+value+"')";
        System.out.println(sql);
        execute(sql);
    }

    public void update(String tableName, String key, String value) {
        String sql = "update "+tableName+" set v = "+value+" where k = '"+key+"'";
    }

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

    public void delete(String tableName, String key) {
        String sql = "delete from "+tableName+" where k = '"+key+"'";
        execute(sql);
    }

    public void clear(String tableName) {
        String sql = "delete from "+tableName;
        execute(sql);
    }

    //Socket related

    public void sendMessage(String id, int destPort, String msg) {
        socket.send(id, destPort, msg);
    }
    public void send(int destPort, Message msg) {
        socket.send(destPort, msg);
    }

//    public Message receiveMessage(String id, int destPort, String msg) {
//        return socket.receive();
//    }


    public Runnable getListener (HashMap<String, Function<Message, Runnable>> map) {
        final Runnable listener = ()-> {
            ExecutorService executor = Executors.newFixedThreadPool(6);
            while (true) {
                try {
                    Socket client = socket.getServerSocket().accept();
                    Message msg = (Message) new ObjectInputStream(client.getInputStream()).readObject();

                    Runnable runnable = map.get(msg.getMessage()).apply(msg);
                    if (runnable != null) {
                        executor.execute(runnable);
                    } else {
                        System.out.println("Got message "+msg.getMessage()+", no handle found for it");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        return listener;
    }
}
