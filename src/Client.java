import DNS.DNSQuery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Client
 */
public class Client {
    /**
     * main function for the Client
     * @param args usable arguments
     * @throws IOException accounts IO Errors
     */
    public static void main(String args[]) throws IOException {
        Scanner scan;
        DatagramSocket socket;
        InetAddress IPAddress;
        DatagramPacket senPacket, recPacket;
        String input, recMessage;
        byte[] recBytes;
        DNSQuery query;

        recBytes = new byte[1024];
        scan = new Scanner(System.in);
        socket = new DatagramSocket();
        IPAddress = InetAddress.getLocalHost();
        input = scan.nextLine();
        query = new DNSQuery(input, "A");

        System.out.println(query);

        recPacket = new DatagramPacket(recBytes, recBytes.length);
        senPacket = query.getPacket(IPAddress,62223);

        socket.send(senPacket);
        socket.receive(recPacket);

        recMessage = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
        System.out.println("Server: " + recMessage);

        socket.close();
    }
}
