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
 * HisCinema.com's WebServer
 */
public class HisWebServer {

    public final static int PORT = 62220; //port listened to

    /**
     * Main method to instantiate HerCDN.com's Web Server
     *
     * @param args potential arguments from users to use (unused)
     */
    public static void main(String args[]){
        Socket mySocket;
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                mySocket = serverSocket.accept();
                new Thread(new HisHandleSocketRequest(mySocket)).start();
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
class HisHandleSocketRequest implements Runnable {

    public final static int MAX_FILE_SIZE = 1024;
    private final Socket socket;

    /**
     * constructor that defines the socket
     *
     * @param socket socket being handled
     */
    public HisHandleSocketRequest(Socket socket) {
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

            fileBytes = getFileBytes(httpGet.getUrl());

            if (fileBytes == null)
                httpResponse = new HTTPResponse("HTTP/1.1", "404", "Not Found");
            else
                httpResponse = new HTTPResponse("HTTP/1.1", "200", "OK", fileBytes);

            outStream.write(httpResponse.getBytes());

            outStream.close();
            inStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Looks through the files to find the requested file,
     * if found reads and returns the byte array of the file.
     *
     * @param fileName file requested
     * @return byte array of the file
     */
    public synchronized byte[] getFileBytes(String fileName) {
        try {
            if (fileName.substring(1).equals("index.html"))
                return Files.readAllBytes(
                        Paths.get("rsc/HisCinemaContents/index.html"));
        }
        catch(IOException e){
            System.out.println("couldn't retrieve file. Error: \n" + e);
        }
        return null;
    }
}