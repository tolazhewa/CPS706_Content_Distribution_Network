package Hosts;

import HTTP.HTTPGet;
import HTTP.HTTPResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HerCND.com's WebServer
 */
public class HerWebServer {

    private final static int PORT = 40080; //port listened to

    /**
     * Main method to instantiate HerCDN.com's Web Server
     *
     * @param args potential arguments from users to use (unused)
     */
    public static void main(String args[]) {
        Socket mySocket;
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                mySocket = serverSocket.accept();
                //make a new thread to handle request
                new Thread(new HerHandleSocketRequest(mySocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Could not access port...");
            e.printStackTrace();
        }
    }
}

/**
 * class that handles the web server's requests
 */
class HerHandleSocketRequest implements Runnable {

    private final static int MAX_FILE_SIZE = 1024 * 128;
    private final Socket socket;

    /**
     * constructor that defines the socket
     *
     * @param socket socket being handled
     */
    public HerHandleSocketRequest(Socket socket) {
        this.socket = socket;
    }

    /**
     * run method that handles requests
     */
    public void run() {
        InputStream inStream;
        OutputStream outStream;
        HTTPGet httpGet;
        HTTPResponse httpResponse;
        byte[] receivedBytes, fileBytes;

        receivedBytes = new byte[MAX_FILE_SIZE];

        try {
            outStream = socket.getOutputStream();
            inStream = socket.getInputStream();

            inStream.read(receivedBytes);
            httpGet = new HTTPGet(receivedBytes);

            System.out.println("\nReceived request:\n" + httpGet);
            fileBytes = Files.readAllBytes(Paths.get("rsc/HerCDNContents/"
                    + httpGet.getUrl()));

            if (fileBytes == null)
                httpResponse = new HTTPResponse("HTTP/1.1", "404", "Not Found");
            else
                httpResponse = new HTTPResponse("HTTP/1.1", "200", "OK", fileBytes);

            outStream.write(httpResponse.getBytes());
            System.out.println("\n\nSent response:\n" + httpResponse);
            outStream.close();
            inStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}