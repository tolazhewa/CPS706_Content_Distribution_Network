import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by tolaz on 2016-11-07.
 */
public class Interface {

    public static DNSServer local;

    public static void main(String args[]) throws IOException {

        createRecords();

        DatagramSocket serverSocket = new DatagramSocket(9876);

        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            serverSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData());

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();

            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }

    public static void createRecords(){
        local = new DNSServer("Name", "`10.2.0.5");
        local.addRecord(new Record("HerCDN.com", "NSherCDN.com", "NS"));
        local.addRecord(new Record("NSHerCDN.com","10.2.0.4", "A"));
        local.addRecord(new Record("hiscinema.com", "NShiscinema", "NS"));
        local.addRecord(new Record("NShiscinema.com","10.2.0.3", "A"));
    }
}
