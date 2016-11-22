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
 * HerCND.com's Web Server
 */
public class HerWebServer {

    public final static int PORT = 62223;

    public static void main(String args[]) {
        Socket mySocket;
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                mySocket = serverSocket.accept();
                new Thread(new HerHandleSocketRequest(mySocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Could not access port...");
            e.printStackTrace();
        }
    }
}

class HerHandleSocketRequest implements Runnable {

    public final static int MAX_FILE_SIZE = 1024;

    private final Socket socket;

    public HerHandleSocketRequest(Socket socket) {
        this.socket = socket;
    }

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

    public synchronized byte[] getFileBytes(String fileName) {
        try {
            switch (fileName) {
                case "/F1":
                    return Files.readAllBytes(Paths.get("rsc/HerCDNContents/file1.txt"));
                case "/F2":
                    return Files.readAllBytes(Paths.get("rsc/HerCDNContents/file2.txt"));
                case "/F3":
                    return Files.readAllBytes(Paths.get("rsc/HerCDNContents/file3.txt"));
                case "/F4":
                    return Files.readAllBytes(Paths.get("rsc/HerCDNContents/file4.txt"));
                case "/F5":
                    return Files.readAllBytes(Paths.get("rsc/HerCDNContents/file5.txt"));
                default:
                    System.out.println("Requested file " + fileName.substring(1) + " not found");
                    return null;
            }
        } catch (IOException e) {
            System.out.println("couldn't retrieve file. Error: \n" + e);
            return null;
        }

    }
}