package StreamLayer;

public interface IStorage {
    void connect();
    void store(String tableName, String key, String value);
    String read(String tableName, String key);
    void update(String tableName, String key, String value);
    void delete(String tableName, String key);
    void clear(String tableName);
    void disconnect();
}
