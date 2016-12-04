import HTTP.HTTPGet;
import HTTP.HTTPResponse;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HisCinema.com's WebServer
 */
public class HisWebServer {

    public final static int PORT = 40081; //port listened to

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

    public final static int MAX_FILE_SIZE = 1024 * 4;
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

            file = new File("rsc/HisCinemaContents" + httpGet.getUrl());
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