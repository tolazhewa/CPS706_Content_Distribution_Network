package Hosts;

import DNS.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Local Domain Name Server
 */
public class ClientLocalDNS {

    private final static int MAX_FILE_SIZE = 1024;
    //client
    private final static int CLIENT_PORT = 62221;
    //hiscinema.com dns port&IP
    private final static int HisDNS_PORT = 62224;
    private final static String HisDNS_IP = "127.0.0.1";
    //hercdn.com dns port&IP
    private final static int HerDNS_PORT = 62225;
    private final static String HerDNS_IP = "127.0.0.1";

    private static ArrayList<Record> records; //list of records
    private static DNSQuery query; //current query handled
    /**
     * Main method to instantiate the Local DNS server
     *
     * @param args arguments that can be passed
     */
    public static void main(String args[]) {
        instantiateRecords();

        DatagramSocket datagramSocket;
        DatagramPacket datagramPacket;
        byte[] data;

        data = new byte[MAX_FILE_SIZE];
        try {
            datagramSocket = new DatagramSocket(CLIENT_PORT);

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
                    query.addAnswer(r);
                    handleRQuery(r.getValue());
                    break;
                case "CNAME":
                    handleCNAMEQuery(r);
                    break;
                case "NS":
                    handleNSQuery(r);
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
     * Method to handle CNAME query types
     *
     * @param r "NS" record
     */
    public static void handleNSQuery(Record r) {
        query.addAuthority(r);

        for (Record x : findInRecords(r.getValue())) {
            if (x.getType().equals("A")) {
                dnsLookup(x.getName(), x.getValue(),
                        query.getQues()[0].getName());
            }
        }
        if (findInRecords(r.getValue()).isEmpty()) {
            System.out.println("A type for NS lookup: "
                    + r.getName() + "was not found!");
        }
    }

    /**
     * Method to handle R query types
     *
     * @param name "R" Record's redirection value
     */
    public static void handleRQuery(String name) {
        for (Record rec : findInRecords(name)) {
            if (!existsInQuery(rec)) {
                switch (rec.getType()) {
                    case "NS":
                        query.addAuthority(rec);
                        for (Record a : findInRecords(rec.getValue())) {
                            if (a.getType().equals("A"))
                                dnsLookup(a.getName(), a.getValue(), name);
                        }
                        break;
                    case "CNAME":
                        handleCNAMEQuery(rec);
                        break;
                    case "A":
                        query.addAnswer(rec.getName(), rec.getType(), rec.getValue());
                        recordsLookup(rec.getValue());
                        break;
                    case "R":
                        handleRQuery(rec.getValue());
                        break;
                }
            }
        }
    }

    /**
     * method to do a lookup in authoritative dns servers
     *
     * @param dnsName name of dns server
     * @param ip      ip of the dns server
     * @param target  the domain queried for
     */
    public static void dnsLookup(String dnsName, String ip, String target) {

        DatagramSocket dnsSocket;
        DatagramPacket receivePacket;
        byte[] bytes;
        int port;
        DNSQuery query2;
        Record r;

        //REMOVE THIS PART ONCE SET UP ON LAB MACHINES
        if (dnsName.equals("NSherCDN.com"))
            port = HerDNS_PORT;
        else
            port = HisDNS_PORT;

        query2 = new DNSQuery(target, "A");

        bytes = new byte[MAX_FILE_SIZE];

        try {
            dnsSocket = new DatagramSocket();
            dnsSocket.send(query2.getPacket(ip, port));

            receivePacket = new DatagramPacket(bytes, bytes.length);
            dnsSocket.receive(receivePacket);
            query2 = new DNSQuery(bytes);

            if (query2.getAns().length != 0 &&
                    query2.getAns()[0].getType().equals("R")) {
                r = new Record(query2.getAns()[0].getName(),
                        query2.getAns()[0].getValue(),
                        query2.getAns()[0].getType());
                if (!existsInRecords(r)) {
                    records.add(r);
                    orderRecords();
                }
                query.addAnswer(query2.getAns()[0].getName(),
                        query2.getAns()[0].getType(),
                        query2.getAns()[0].getValue());
                handleRQuery(query2.getAns()[0].getValue());
            } else {
                for (Answer a : query2.getAns()) {
                    r = new Record(a.getName(), a.getValue(), a.getType());
                    if (!existsInRecords(r)) {
                        records.add(r);
                        orderRecords();
                    }
                    query.addAnswer(a.getName(), a.getType(), a.getValue());
                }
            }

        } catch (IOException e) {
            System.out.println("Connection to the authoritative DNS server " +
                    dnsName + " failed... ");
            e.printStackTrace();
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
            if (r.getType().equals("R"))
                rec.add(r);
        }
        for (Record r : records) {
            if (r.getType().equals("CNAME"))
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
     * prints all the records currently held by the Local DNS server
     */
    public static void printRecords() {
        System.out.println("------------------");
        for (Record r : records) {
            System.out.println(r);
        }
        System.out.println("------------------");
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
            if (r.getName().trim().equals(name))
                rs.add(r);
        }
        if (rs.isEmpty())
            System.out.println("NO RECORDS WERE FOUND FOR: " + name);

        return rs;
    }

    /**
     * checks if the record already exists in records
     *
     * @param a record to check
     * @return true if exists in records, false if it doesn't
     */
    public static boolean existsInRecords(Record a) {
        for (Record r : records) {
            if (r.equals(a))
                return true;
        }
        return false;
    }

    /**
     * checks if record exists in the query
     *
     * @param rec record to check
     * @return true if it already exists, false if it doesn't
     */
    public static boolean existsInQuery(Record rec) {
        for (Answer a : query.getAns()) {
            if (rec.getName().equals(a.getName())
                    && rec.getType().equals(a.getType())
                    && rec.getValue().equals(rec.getValue()))
                return true;
        }
        for (Authority a : query.getAuth()) {
            if (rec.getName().equals(a.getName())
                    && rec.getType().equals(a.getType())
                    && rec.getValue().equals(rec.getValue()))
                return true;
        }
        return false;
    }

    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiateRecords() {
        records = new ArrayList<>();
        records.add(new Record("herCDN.com", "NSherCDN.com", "NS"));
        records.add(new Record("NSherCDN.com", HisDNS_IP, "A"));
        records.add(new Record("video.hiscinema.com", "hiscinema.com", "CNAME"));
        records.add(new Record("hiscinema.com", "NShiscinema.com", "NS"));
        records.add(new Record("NShiscinema.com", HerDNS_IP, "A"));
        orderRecords();
    }
}
