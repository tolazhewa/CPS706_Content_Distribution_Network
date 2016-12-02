package Hosts;

import DNS.DNSQuery;
import DNS.Question;
import DNS.Record;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * hiscinema.com authoritative name server
 */
public class HisADNS {

    private final static int MAX_FILE_SIZE = 1024;
    private final static String HerDNS_IP = "127.0.0.1";
    private static final int PORT = 40081;
    private static ArrayList<Record> records;
    private static DNSQuery query;



    /**
     * Main method that instantiates hiscinema.com's Authoritative DNS
     *
     * @param args potential arguments from users to use (unused)
     */
    public static void main(String[] args) {
        instantiateRecords();

        DatagramSocket datagramSocket;
        byte[] data;
        DatagramPacket datagramPacket;

        data = new byte[MAX_FILE_SIZE];

        try {
            datagramSocket = new DatagramSocket(PORT);

            while (true) {
                datagramPacket = new DatagramPacket(data, data.length);
                datagramSocket.receive(datagramPacket);
                query = new DNSQuery(data);
                System.out.println("DNS Query RECEIVED:\n" + query);
                datagramPacket = getDNSResponse(
                        datagramPacket.getAddress(),
                        datagramPacket.getPort());
                datagramSocket.send(datagramPacket);
            }

        } catch (IOException e) {
            System.out.println("Could not access port...");
            e.printStackTrace();
        }
    }

    /**
     * package the query and return it in a DatagramPacket
     *
     * @param ip   IP that the packet will be sent to
     * @param port port that it will be sent through
     * @return DatagramPacket containing all the information
     */
    public static DatagramPacket getDNSResponse(InetAddress ip, int port) {
        for (Question q : query.getQues()) {
            recordsLookup(q.getName());
        }
        System.out.println("DNS Query RETURNED: \n" + query);
        return query.getPacket(ip, port);
    }

    /**
     * Look up the records for a specific DNS query name
     *
     * @param name name property of the query
     */
    public static void recordsLookup(String name) {
        for (Record r : findInRecords(name)) {
            switch (r.getType()) {
                case "A":
                    query.addAnswer(r);
                    break;
                case "V":
                    recordsLookup(r.getValue());
                    break;
                case "CNAME":
                    query.addAnswer(r);
                    recordsLookup(r.getValue());
                    break;
                case "NS":
                    query.addAuthority(r);

                    recordsLookup(r.getValue());
                    break;
                default:
                    System.out.println("Invalid type");
                    break;
            }
        }
    }

    /**
     * orders records based on hierarchy
     */
    public static void orderRecords() {
        ArrayList<Record> rec = new ArrayList<>();

        for (Record r : records) {
            if (r.getType().equals("A"))
                rec.add(r);
        }
        for (Record r : records) {
            if (r.getType().equals("AAAA"))
                rec.add(r);
        }
        for (Record r : records) {
            if (r.getType().equals("CNAME"))
                rec.add(r);
        }
        for (Record r : records) {
            if (r.getType().equals("V"))
                rec.add(r);
        }
        for (Record r : records) {
            if (r.getType().equals("NS"))
                rec.add(r);
        }
        for (Record r : records) {
            if (r.getType().equals("MX"))
                rec.add(r);
        }
        records = rec;
    }

    /**
     * find list of records that have given name
     *
     * @param name name searched for
     * @return ArrayList of Records
     */
    public static ArrayList<Record> findInRecords(String name) {
        ArrayList<Record> rs = new ArrayList<>();

        for (Record r : records) {
            if (r.getName().trim().equals(name.trim()))
                rs.add(r);
        }
        return rs;
    }

    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiateRecords() {
        records = new ArrayList<>();
        records.add(new Record("video.hiscinema.com", "herCDN.com", "V"));
        records.add(new Record("herCDN.com", "NSherCDN.com", "NS"));
        records.add(new Record("NSherCDN.com", HerDNS_IP, "A"));
        orderRecords();
    }
}
