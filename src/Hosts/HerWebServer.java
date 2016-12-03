package Hosts;

import HTTP.HTTPGet;
import HTTP.HTTPResponse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

    private final static int MAX_FILE_SIZE = 1024 * 4;
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
        BufferedInputStream bis;
        int current;
        byte[] receivedBytes, fileBytes;
        File file;

        receivedBytes = new byte[MAX_FILE_SIZE];

        try {
            outStream = socket.getOutputStream();
            inStream = socket.getInputStream();

            inStream.read(receivedBytes);
            httpGet = new HTTPGet(receivedBytes);

            System.out.println("\n\nReceived request:\n" + httpGet);

            file = new File("rsc/HerCDNContents" + httpGet.getUrl());
            bis = new BufferedInputStream(new FileInputStream(file));

            if (!file.exists()) {
                httpResponse = new HTTPResponse("HTTP/1.1", "404", "Not Found");
                outStream.write(httpResponse.getBytes());
                outStream.flush();
            } else {
                httpResponse = new HTTPResponse("HTTP/1.1", "200", "OK");
                fileBytes = new byte[(int) file.length()];
                bis.read(fileBytes, 0, fileBytes.length);
                httpResponse.addHeaderLine("Content-Length", Integer.toString(fileBytes.length));

                outStream.write(httpResponse.getBytes());
                outStream.flush();

                System.out.println("\nSent response:\n" + httpResponse);

                inStream.read();

                for (current = 0; current < fileBytes.length - 1024 * 16; current += 1024 * 16) {
                    outStream.write(fileBytes, current, 1024 * 16);
                }
                outStream.write(fileBytes, current, fileBytes.length - current);
                outStream.flush();
            }

            outStream.close();
            inStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}