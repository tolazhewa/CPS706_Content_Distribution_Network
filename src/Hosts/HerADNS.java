package Hosts;

import DNS.Record;

import java.util.ArrayList;

/**
 * HerCDN.com's Authoritative DNS Server
 */
public class HerADNS {

    private static ArrayList<Record> records;

    public static void main(String args[]){
        instantiate();

    }

    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiate(){
        records = new ArrayList<>();
        records.add(new Record("herCDN.com","www.herCDN.com","CName"));
        records.add(new Record("herCDN.com","127.0.0.1","A"));
    }
}
