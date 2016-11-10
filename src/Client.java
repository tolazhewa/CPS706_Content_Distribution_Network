import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Client
 */
public class Client {
    public static void main(String args[]) throws IOException {

        Scanner scan;
        DatagramSocket socket;
        InetAddress IPAddress;
        DatagramPacket senPacket, recPacket;
        String input, recMessage;
        byte[] inputBytes, recBytes;


        recBytes = new byte[1024];
        scan = new Scanner(System.in);
        socket = new DatagramSocket();
        IPAddress = InetAddress.getLocalHost();
        input = scan.nextLine();
        inputBytes = input.getBytes();
        recPacket = new DatagramPacket(recBytes, recBytes.length);
        senPacket = createDNSRequestPacket(IPAddress,62223, inputBytes);


        socket.send(senPacket);
        socket.receive(recPacket);

        recMessage = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
        System.out.println("Server: " + recMessage);

        socket.close();
    }

    public static DatagramPacket createDNSRequestPacket(InetAddress IPAddress, int port, byte[] inputBytes){
        byte content[];
        DatagramPacket packet;
        Random r;

        r = new Random();
        content = new byte[0];
        int id, flags, ques, ansrr, authrr, addrr, end;
        ques = 1; ansrr = 0; authrr = 0; addrr = 0; end = 1;

        id = r.nextInt(65536);
        //System.out.println("id=" + Integer.toBinaryString(id));
        flags = 0x0100;
        //System.out.println("flags=" + Integer.toBinaryString(flags));
        id <<= 16;
        //System.out.println("id(pb)=" + Integer.toBinaryString(id));
        id |= flags;
        //System.out.println("line1=" + Integer.toBinaryString(line1));

        content = addBytes(content, getByteFromInt(id));


        ques <<= 16;
        ques |= ansrr;
        content = addBytes(content, getByteFromInt(ques));

        authrr <<= 16;
        authrr |= addrr;
        content = addBytes(content, getByteFromInt(authrr));

        content = addBytes(content, inputBytes);

        end <<= 16;
        end |= 1;
        content = addBytes(content, getByteFromInt(end));

        // Print the bytes in binary
        for(int i = 0; i < content.length; i++){
            System.out.println("byte[" + i + "] = " + Integer.toBinaryString((content[i] & 0xFF) + 0x100).substring(1));
        }
        //*/
        packet = new DatagramPacket(content, content.length, IPAddress,port);

        return packet;

    }

    public static byte[] addBytes(byte[] content, byte[] toAdd){
        byte[] toRet = new byte[content.length + toAdd.length];
        for(int i = 0; i < content.length; i++){
            toRet[i] = content[i];
        }
        for(int i = 0; i < toAdd.length; i++){
            toRet[i+content.length] = toAdd[i];
        }
        return toRet;
    }

    public static byte[] addByte(byte[] content, byte toAdd){
        byte[] toRet = new byte[content.length + 1];
        for(int i = 0; i < content.length; i++){
            toRet[i] = content[i];
        }
        toRet[content.length] = toAdd;
        return toRet;
    }



    public static byte[] getByteFromInt(int input){
        byte[] conv = new byte[4];
        conv[3] = (byte)(input & 0xff);
        input >>= 8;
        conv[2] = (byte)(input & 0xff);
        input >>= 8;
        conv[1] = (byte)(input & 0xff);
        input >>= 8;
        conv[0] = (byte) input;
        return conv;
    }
}
