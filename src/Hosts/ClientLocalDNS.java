package Hosts;

import DNS.Record;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Local Domain Name Server
 */
public class ClientLocalDNS {

    private static ArrayList<Record> records; //list of records

    /**
     * main method to run the dummy Local DNS server
     * @param args arguments that can be passed
     * @throws IOException accounts for ioexceptions
     */
    public static void main(String args[]) throws IOException {
        instantiate();


    }


    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiate(){
        records = new ArrayList<>();
        records.add(new Record("herCDN.com","NSherCDN.com","NS"));
        records.add(new Record("NSherCDN.com","10.5.0.6","A"));
        records.add(new Record("hiscinema.com","NShiscinema.com","NS"));
        records.add(new Record("NShiscinema.com","10.5.0.3","A"));
    }

}
