import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * User
 */
public class Client {
    public static void main(String args[]) throws IOException {

        Scanner scan;
        DatagramSocket socket;
        InetAddress IPAddress;
        DatagramPacket senPacket, recPacket;
        int id, flags, ques, ansrr, authrr, addrr;
        String input, recMessage;
        byte[] inputBytes, recBytes;

        recBytes = new byte[1024];
        scan = new Scanner(System.in);
        socket = new DatagramSocket();
        IPAddress = InetAddress.getLocalHost();
        input = scan.nextLine();
        inputBytes = input.getBytes();
        senPacket = new DatagramPacket(inputBytes, input.length(), IPAddress, 62223);
        recPacket = new DatagramPacket(recBytes, recBytes.length);


        socket.send(senPacket);
        socket.receive(recPacket);

        recMessage = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
        System.out.println("Server: " + recMessage);

        socket.close();
    }
    public static DatagramPacket createDNSRequestPacket(){
        ArrayList<Byte> content;
        DatagramPacket packet;
        Random r;

        r = new Random();
        content = new ArrayList<>();
        int id, flags, ques, ansrr, authrr, addrr;
        ques = 1; ansrr = 0; authrr = 0; addrr = 0;

        id = r.nextInt(65536);
        //System.out.println("id=" + Integer.toBinaryString(id));
        flags = 0x0100;
        //System.out.println("flags=" + Integer.toBinaryString(flags));
        id = id << 16;
        //System.out.println("id(pb)=" + Integer.toBinaryString(id));
        id = id | flags;
        //System.out.println("line1=" + Integer.toBinaryString(line1));

        ques = ques << 16;
        ques = ques | ansrr;
        authrr = authrr << 16;
        authrr = authrr | addrr;

        content






    }
}
