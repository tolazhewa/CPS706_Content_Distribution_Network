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
    private final static int CLIENT_PORT = 40080;
    private final static int ADNS_PORT = 40081;
    private final static int HerDNS_PORT = 40082;
    private final static String HisDNS_IP = "127.0.0.1";
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
                    return;
                case "CNAME":
                    query.addAnswer(r);
                    recordsLookup(r.getValue());
                    return;
                case "NS":
                    handleNSQuery(r);
                    return;
                default:
                    System.out.println("Invalid type");
                    return;
            }
        }
    }

    /**
     * Method to handle NS query types
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
        if (dnsName.trim().equals("NSherCDN.com"))
            port = HerDNS_PORT;
        else
            port = ADNS_PORT;

        query2 = new DNSQuery(target, "A");

        bytes = new byte[MAX_FILE_SIZE];

        try {
            dnsSocket = new DatagramSocket();
            dnsSocket.send(query2.getPacket(ip, port));
            System.out.println("Sending to ADNS: " + ip +
                    " | " + port + "\n" + query2);

            receivePacket = new DatagramPacket(bytes, bytes.length);
            dnsSocket.receive(receivePacket);
            query2 = new DNSQuery(bytes);

            System.out.println("Receiving from ADNS: " + ip +
                    " | " + port + "\n" + query2);

            if(query2.getAns().length == 0 &&
                    query2.getAuth().length == 0)
                return;

            if(query2.getAuth().length != 0) {
                for(Authority a: query2.getAuth()){
                    if(a.getType().equals("NS")){
                        query.addAuthority(a);
                        for(Answer ans: query2.getAns()){
                            if(ans.getName().trim().equals
                                    (a.getValue().trim())){
                                dnsLookup(ans.getName(),
                                        ans.getValue(),a.getName());
                            }
                        }
                    }
                }
            }
            else {
                for (Answer a : query2.getAns()) {
                    r = new Record(a.getName(),
                            a.getValue(), a.getType());
                    if (!existsInRecords(r)) {
                        records.add(r);
                        orderRecords();
                    }
                    if (!existsInQuery(r))
                        query.addAnswer(a.getName(),
                                a.getType(), a.getValue());
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
            if (r.getName().trim().equals(name.trim()))
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
        records.add(new Record("NSherCDN.com", HerDNS_IP, "A"));
        records.add(new Record("video.hiscinema.com", "hiscinema.com", "CNAME"));
        records.add(new Record("hiscinema.com", "NShiscinema.com", "NS"));
        records.add(new Record("NShiscinema.com", HisDNS_IP, "A"));
        orderRecords();
    }
}
