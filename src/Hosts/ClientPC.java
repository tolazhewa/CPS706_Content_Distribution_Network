package Hosts;

import DNS.Answer;
import DNS.DNSQuery;
import HTTP.HTTPGet;
import HTTP.HTTPResponse;
import HTTP.Link;

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

    private final static int MAX_FILE_SIZE = 1024;
    //HisCinema Web Server info
    private final static int HWS_PORT = 62220;
    private final static String HWS_IP = "127.0.0.1";
    //Local DNS Server info
    private final static int LDNS_PORT = 62221;
    private final static String LDNS_IP = "127.0.0.1";
    //HerCDN.com Web Server info
    private final static int HCDN_PORT = 62222;

    /**
     * Main function for the Hosts.ClientPC
     *
     * @param args usable arguments
     */
    public static void main(String args[]) {
        File indexHTML;
        ArrayList<Link> links;

        indexHTML = getFileFromServer("index.html", HWS_IP, HWS_PORT);
        links = getLinksFromFile(indexHTML);

        for(Link a: links) {
            getFileFromServer(a.getExt(), dnsLookup(a.getUrl()), HCDN_PORT);
        }

        System.out.println("All operations complete.\nGoodBye!");
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
        query = new DNSQuery(name, "A");

        try {
            System.out.println("Sending DNS Query");
            dnsSocket = new DatagramSocket();
            dnsSocket.send(query.getPacket(LDNS_IP, LDNS_PORT));
            receivePacket = new DatagramPacket(bytes, bytes.length);
            dnsSocket.receive(receivePacket);

            query = new DNSQuery(bytes);

            for (Answer a : query.getAns()) {
                if (a.getType().equals("A")) {
                    return a.getValue();
                }
            }
            System.out.println("DNS Query did not return any results");
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
     * Retrieves file from a server given parameters
     *
     * @param requestFile file requested
     * @param IPAddress   IP Address of the server retrieved from
     * @param port        port used to access the server retrieved from
     * @return File requested
     */
    public static File getFileFromServer(String requestFile, String IPAddress, int port) {
        Socket socket;
        InputStream inStream;
        OutputStream outStream;
        HTTPGet httpGet;
        HTTPResponse httpResponse;
        byte[] receivedBytes, bytesToSend;
        File file;

        receivedBytes = new byte[MAX_FILE_SIZE];
        httpGet = new HTTPGet("/" + requestFile);
        bytesToSend = httpGet.getBytes();

        try {
            socket = new Socket(IPAddress, port);

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
            if(!checkResponse(httpResponse.getStatusCode())){
                return null;
            }

            file = createFile(requestFile, httpResponse.getData());
            return file;

        } catch (IOException e) {
            System.out.println("Connection to the web server [" + IPAddress
                    + " | " + port + "] failed... \n");
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

}
