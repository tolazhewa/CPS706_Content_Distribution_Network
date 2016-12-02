package Hosts;

import DNS.Answer;
import DNS.DNSQuery;
import DNS.Record;
import HTTP.HTTPGet;
import HTTP.HTTPResponse;
import HTTP.Link;

import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Client's Personal Computer
 */
public class ClientPC {

    private final static int MAX_FILE_SIZE = 1024 * 128;
    private final static int HWS_PORT = 40081; //TCP
    private final static int HCDN_PORT = 40080; //TCP
    private final static int LDNS_PORT = 40080; //UDP
    private final static String LDNS_IP = "127.0.0.1";
    private final static String HWS_IP = "127.0.0.1";
    private static ArrayList<String> files;
    private static ArrayList<Record> records;
    //HerCDN.com Web Server info

    /**
     * Main function for the Hosts.ClientPC
     *
     * @param args usable arguments
     */
    public static void main(String args[]) {
        instantiateRecords();

        File indexHTML, curr;
        ArrayList<Link> fileLinks;
        String input;
        Scanner inputScanner;

        files = new ArrayList<>();
        inputScanner = new Scanner(System.in);
        indexHTML = getFileFromServer(
                new Link("www.hiscinema.com/index.html\""), HWS_PORT);
        fileLinks = getLinksFromFile(indexHTML);


        while(true) {
            showVideosMenu();
            input = inputScanner.nextLine();
            switch(input){
                case "1":
                    curr = getFileFromServer(fileLinks.get(0), HCDN_PORT);
                    break;
                case "2":
                    curr = getFileFromServer(fileLinks.get(1), HCDN_PORT);
                    break;
                case "3":
                    curr = getFileFromServer(fileLinks.get(2), HCDN_PORT);
                    break;
                case "4":
                    curr = getFileFromServer(fileLinks.get(3), HCDN_PORT);
                    break;
                case "5":
                    curr = getFileFromServer(fileLinks.get(4), HCDN_PORT);
                    break;
                case "q":
                    return;
                default:
                    curr = null;
                    System.out.println("Invalid Input!!");
                    continue;
            }
            try {
                Desktop.getDesktop().open(curr);
            } catch (IOException e) {
                System.out.println("File not found");
                e.printStackTrace();
            }
        }
    }

    /**
     * Performs a DNS lookup to the Local DNS Server
     *
     * @param name name of the domain requested
     * @return IP address of domain
     */
    public static String dnsLookup(String name) {
        DNSQuery query;
        DatagramSocket dnsSocket;
        DatagramPacket receivePacket;
        byte[] bytes;

        bytes = new byte[MAX_FILE_SIZE];
        query = new DNSQuery(name, "V");

        try {
            System.out.println("\n\n\n\nDNS Query Sent: \n\n\n\n" + query);
            dnsSocket = new DatagramSocket();
            dnsSocket.send(query.getPacket(LDNS_IP, LDNS_PORT));

            receivePacket = new DatagramPacket(bytes, bytes.length);
            dnsSocket.receive(receivePacket);

            query = new DNSQuery(bytes);
            System.out.println("\n\n\n\nDNS Response Received: \n\n\n\n" + query);

            for (Answer a : query.getAns()) {
                if (a.getType().equals("A")) {
                    return a.getValue();
                }
            }

            return null;

        } catch (IOException e) {
            System.out.println("Connection to the local DNS server failed... ");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves links from a given file
     *
     * @param file the given file
     * @return ArrayList of links containing files
     */
    public static ArrayList<Link> getLinksFromFile(File file){
        Scanner fileScanner;
        ArrayList<Link> links;
        String line;

        links = new ArrayList<>();
        try {
            fileScanner = new Scanner(file);
            while(fileScanner.hasNext()){
                line = fileScanner.nextLine();
                if(line.contains("http://")){
                    line = line.substring(line.indexOf("\"http://") + 8);
                    links.add(new Link(line));
                }
            }
            return links;

        } catch(IOException e){
            System.out.println("File not found...");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * shows all the videos available to get
     */
    public static void showVideosMenu(){
        System.out.println("1. Video #1");
        System.out.println("2. Video #2");
        System.out.println("3. Video #3");
        System.out.println("4. Video #4");
        System.out.println("5. Video #5");
        System.out.println("q. exit");
    }

    /**
     * Retrieves file from a server given parameters
     *
     * @param port        port used to access the server retrieved from
     * @return File requested
     */
    public static File getFileFromServer(Link link, int port) {
        for(String f: files){
            if(f.equals(getFileName(link.getExt())))
                return new File("rsc/ClientPCContents/" + f);
        }
        Socket socket;
        InputStream inStream;
        OutputStream outStream;
        HTTPGet httpGet;
        HTTPResponse httpResponse;
        byte[] receivedBytes, bytesToSend;
        File file;
        String ip;

        ip = null;
        file = null;
        socket = null;
        receivedBytes = new byte[MAX_FILE_SIZE];

        try {
            for(Record r: records){
                if(r.getName().equals(link.getUrl()) && r.getType().equals("A"))
                    ip = r.getValue();
            }

            if(ip == null)
                ip = dnsLookup(link.getUrl());

            if(ip == null)
                return null;
            System.out.println("IP: " + ip + " | Port: " +
                    port + " | Looking for: " + getFileName(link.getExt()));
            socket = new Socket(ip, port);
            httpGet = new HTTPGet("/" + getFileName(link.getExt()));
            bytesToSend = httpGet.getBytes();

            //initializing input and output for the Socket
            inStream = socket.getInputStream();
            outStream = socket.getOutputStream();

            //sending the GET packet
            outStream.write(bytesToSend);

            //Getting response
            inStream.read(receivedBytes);

            //repackaging the HTTP response from bytes
            httpResponse = new HTTPResponse(receivedBytes);
            //checking response
            if (!checkResponse(httpResponse.getStatusCode())) {
                return null;
            }
            System.out.println("File Received: " + httpGet.getUrl());

            file = createFile(getFileName(link.getExt()), httpResponse.getData());
            files.add(getFileName(link.getExt()));
            socket.close();
            return file;

        } catch (IOException e) {
            System.out.println("Connection to the web server [" +
                    socket.getInetAddress() + " | " + port + "] failed... \n");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * checks response to a HTTP request
     *
     * @param response the response being checked
     * @return if the response indicated success return true,
     * otherwise printout problem and return false
     */
    public static boolean checkResponse(String response){
        switch (response){
            case "404":
                System.out.println("404: Not Found." +
                        "\n\tRequested document not found on the server");
                return false;
            case "400":
                System.out.println("400: Bad Request." +
                        "\n\tRequest message not understood by server");
                return false;
            case "505":
                System.out.println("505: HTTP Version Not Supported");
                return false;
            case "200":
                System.out.println("200: OK." +
                        "\n\tRequest Succeeded, requested object in data");
                return true;
            default:
                System.out.println("Unknown response...");
                return false;
        }
    }

    /**
     * Creates file based on given content (in bytes)
     *
     * @param fileName name of the file being created
     * @param fileContent content in bytes
     * @return file made up of the given bytes
     */
    public static File createFile(String fileName, byte[] fileContent){
        File file;
        FileOutputStream fileOutStream;

        try {
            file = new File("rsc/ClientPCContents/" + fileName);
            file.createNewFile();

            fileOutStream = new FileOutputStream(file);
            fileOutStream.write(fileContent);
            fileOutStream.close();

            return file;
        } catch(IOException e){
            System.out.println("Unable to create file");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gives the name of the file based on ext
     *
     * @param ext extention
     * @return file name
     */
    public static String getFileName(String ext){
        switch (ext) {
            case "F1":
                return "File1.txt";
            case "F2":
                return "File2.txt";
            case "F3":
                return "File3.txt";
            case "F4":
                return "File4.txt";
            case "F5":
                return "File5.txt";
            case "index.html":
                return "index.html";
            default:
                return null;
        }
    }

    /**
     * adds all the preloaded records into the ArrayList
     */
    public static void instantiateRecords() {
        records = new ArrayList<>();
        records.add(new Record("www.hiscinema.com", HWS_IP, "A"));
    }

}
