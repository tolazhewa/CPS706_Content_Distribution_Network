package Hosts;

import DNS.DNSQuery;
import HTTP.HTTPGet;
import HTTP.HTTPResponse;
import Misc.Link;
import sun.java2d.pipe.BufferedOpCodes;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Client's Personal Computer
 */
public class ClientPC {

    public final static int MAX_FILE_SIZE = 1024;
    //HisCinema Web Server info
    public final static int HWS_PORT = 62220;
    public final static String HWS_IP = "127.0.0.1";
    //Local DNS Server info
    public final static int LDNS_PORT = 62221;
    public final static String LDNS_IP = "127.0.0.1";
    //HerCDN.com Web Server info
    public final static int HCDN_PORT = 62222;

    /**
     * main function for the Hosts.ClientPC
     * @param args usable arguments
     * @throws IOException accounts IO Errors
     */
    public static void main(String args[]) throws IOException {
        File indexHTML;
        ArrayList<Link> links;
        String IP;

        indexHTML = getFileFromServer("index.html", HWS_IP, HWS_PORT);
        links = getLinksFromFile(indexHTML);

        IP = dnsLookup(links.get(0).getUrl());

        for(Link a: links) {
            getFileFromServer(a.getExt(), IP, HCDN_PORT);
        }

        System.out.println("All operations complete.\nGoodBye!");
    }

    public static String dnsLookup(String name){
        DNSQuery query;
        DatagramSocket dnsSocket;
        DatagramPacket receivePacket;
        byte[] receivedBytes;

        receivedBytes = new byte[MAX_FILE_SIZE];
        query = new DNSQuery(name, "A");

        try {
            dnsSocket = new DatagramSocket();
            dnsSocket.send(query.getPacket(LDNS_IP,LDNS_PORT));

            receivePacket = new DatagramPacket(receivedBytes,receivedBytes.length);
            dnsSocket.receive(receivePacket);

            query = new DNSQuery(receivedBytes);
            return query.getAns()[0].getValue();


        } catch(IOException e){
            System.out.println("Connection to the local DNS server failed... ");
            e.printStackTrace();
            return null;
        }


    }

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
                    links.add(new Link(line.substring(0,line.indexOf('/')),
                            line.substring(line.indexOf('/'), line.indexOf('\"'))));
                }
            }
            return links;

        } catch(IOException e){
            System.out.println("File not found...");
            e.printStackTrace();
            return null;
        }
    }

    public static File getFileFromServer(String requestFile, String IPAddress, int port){
        Socket hisWSSocket;
        InputStream inStream;
        OutputStream outStream;
        HTTPGet httpGet;
        HTTPResponse httpResponse;
        byte[] receivedBytes, bytesToSend;
        File indexFile;

        receivedBytes = new byte[MAX_FILE_SIZE];
        httpGet = new HTTPGet("/" + requestFile);
        bytesToSend = httpGet.getBytes();

        try {
            hisWSSocket = new Socket(IPAddress, port);

            //initializing input and output for the Socket
            inStream = hisWSSocket.getInputStream();
            outStream = hisWSSocket.getOutputStream();

            //sending the GET packet
            outStream.write(bytesToSend);

            //Getting response
            inStream.read(receivedBytes);

            //repackaging the HTTP response from bytes
            httpResponse = new HTTPResponse(receivedBytes);

            //checking response
            if(!checkResponse(httpResponse.getStatusCode())){
                return null;
            }

            indexFile = createFile(requestFile, httpResponse.getData());
            return indexFile;

        } catch (IOException e) {
            System.out.println("Connection to the web server [" + IPAddress
                                + " | " + port + "] failed... \n" + e);
            return null;
        }
    }

    public static boolean checkResponse(String response){
        switch (response){
            case "404":
                System.out.println("404: Not Found.\n\tRequested document not found on the server");
                return false;
            case "400":
                System.out.println("400: Bad Request.\n\tRequest message not understood by server");
                return false;
            case "505":
                System.out.println("505: HTTP Version Not Supported");
                return false;
            case "200":
                System.out.println("200: OK.\n\tRequest Succeeded, requested object in data");
                return true;
            default:
                System.out.println("Unknown response...");
                return false;
        }
    }

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

}
