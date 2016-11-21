package Hosts;

import DNS.Record;

import java.util.ArrayList;

/**
 * Authoritative DNS server for hercdn.com
 */
public class HerADNS {
    private static ArrayList<Record> records;
    public static void main(){
        instantiate();

    }
    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiate(){
        records = new ArrayList<>();

    }
}
