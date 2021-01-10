package PartitionLayer;

import java.util.concurrent.ConcurrentHashMap;

public class HashRing {
    ConcurrentHashMap hashMap = new ConcurrentHashMap();
    public int hash(){return 0;}
}
