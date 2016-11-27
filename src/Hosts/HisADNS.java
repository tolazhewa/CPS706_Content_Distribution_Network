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
    private static final int PORT = 62224;
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
                    handleAQuery(r);
                    break;
                case "R":
                    handleRQuery(r);
                    break;
                case "CNAME":
                    handleCNAMEQuery(r);
                    break;
                default:
                    System.out.println("Invalid type");
                    break;
            }
        }
    }

    /**
     * Method to handle A query types
     *
     * @param r "A" record
     */
    public static void handleAQuery(Record r) {
        query.addAnswer(r.getName(), r.getType(), r.getValue());
    }

    /**
     * Method to handle CNAME query types
     *
     * @param r "CNAME" record
     */
    public static void handleCNAMEQuery(Record r) {
        query.addAnswer(r.getName(), r.getType(), r.getValue());
        recordsLookup(r.getValue());
    }

    /**
     * Method to handle R query types
     *
     * @param r "R" Record
     */
    public static void handleRQuery(Record r) {
        query.addAnswer(r);
        recordsLookup(r.getValue());
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
            if (r.getType().equals("R"))
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
            if (r.getName().equals(name))
                rs.add(r);
        }
        return rs;
    }

    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiateRecords() {
        records = new ArrayList<>();
        records.add(new Record("video.hiscinema.com", "herCDN.com", "R"));
        orderRecords();
    }
}
