package Hosts;

import DNS.DNSQuery;
import HTTP.HTTPGet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Hosts.ClientPC
 */
public class ClientPC {
    /**
     * main function for the Hosts.ClientPC
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
        HTTPGet hGet;

        recBytes = new byte[1024];
        scan = new Scanner(System.in);
        socket = new DatagramSocket();
        IPAddress = InetAddress.getLocalHost();
        input = scan.nextLine();
        hGet = new HTTPGet(input);

        query = new DNSQuery(input, "A");

        System.out.println(hGet);

        recPacket = new DatagramPacket(recBytes, recBytes.length);
        senPacket = new DatagramPacket(hGet.getBytes(),hGet.getBytes().length,IPAddress,62223);

        socket.send(senPacket);
        socket.receive(recPacket);

        recMessage = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
        System.out.println("Server: " + recMessage);

        socket.close();
    }
}
