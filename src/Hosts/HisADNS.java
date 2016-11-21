package Hosts;

import DNS.Record;

import java.util.ArrayList;

/**
 * hiscinema.com authoritative name server
 */
public class HisADNS {
    private static ArrayList<Record> records;

    public static void main(){
        instantiate();

    }
    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiate(){
        records = new ArrayList<>();
        records.add(new Record("video.netcinema.com","herCDN.com","R"));
        records.add(new Record("herCDN.com","www.herCDN.com","CName"));
        records.add(new Record("www.herCDN.com", "127.0.0.1","A"));
    }
}
