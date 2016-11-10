import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Local Domain Name Server
 */
public class LDNS {

    public static ArrayList<Record> records;

    public static void main(String args[]) throws IOException {
        instantiate();

        DatagramSocket serverSocket;
        byte[] receiveData, sendData;
        String message, addition;
        InetAddress IP;
        int port;
        Scanner scan;
        DatagramPacket receivePacket, sendPacket;

        receiveData = new byte[1024];
        sendData = new byte[1024];
        serverSocket = new DatagramSocket(62223);
        scan = new Scanner(System.in);


        while(true) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
            IP = receivePacket.getAddress();
            port = receivePacket.getPort();

            System.out.println("data: " + message + " port: " + port + " IP: " + IP);
            addition = scan.nextLine();
            message = message + addition;
            sendData = message.getBytes();
            System.out.println("Message to send: " + message);
            sendPacket = new DatagramPacket(sendData, message.length(), IP, port);
            serverSocket.send(sendPacket);
        }
    }

    public static void instantiate(){
        records = new ArrayList<>();
        records.add(new Record("herCDN.com","NSherCDN.com","NS"));
        records.add(new Record("NSherCDN.com","10.5.0.6","A"));
        records.add(new Record("hiscinema.com","NShiscinema.com","NS"));
        records.add(new Record("NShiscinema.com","10.5.0.3","A"));
    }

}
