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

    public final static int MAX_FILE_SIZE = 1024;
    public final static int PORT = 62220;

    public static void main(String args[]){
        Socket mySocket;
        InputStream inStream;
        OutputStream outStream;
        HTTPGet httpGet;
        HTTPResponse httpResponse;
        byte[] receivedBytes, fileBytes;
        ServerSocket serverSocket;

        receivedBytes = new byte[MAX_FILE_SIZE];

        try {
            serverSocket = new ServerSocket(PORT);
            mySocket = serverSocket.accept();

            outStream = mySocket.getOutputStream();
            inStream = mySocket.getInputStream();

            inStream.read(receivedBytes);
            httpGet = new HTTPGet(receivedBytes);

            fileBytes = getFileBytes(httpGet.getUrl());

            if(fileBytes == null)
                httpResponse = new HTTPResponse("HTTP/1.1","404","Not Found");
            else
                httpResponse = new HTTPResponse("HTTP/1.1","200","OK",fileBytes);

            outStream.write(httpResponse.getBytes());

            serverSocket.close();
            mySocket.close();
            outStream.close();
            inStream.close();
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }

    public static byte[] getFileBytes(String fileName) {
        try {
            if (fileName.substring(1).equals("index.html"))
                return Files.readAllBytes(Paths.get("rsc/HisCinemaContents/index.html"));
        }
        catch(IOException e){
            System.out.println("couldn't retrieve file. Error: " + e);
        }
        return null;
    }
}
