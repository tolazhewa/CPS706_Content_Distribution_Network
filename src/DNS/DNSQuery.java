package DNS;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

/**
 * DNS.DNSQuery class embodies DNS packets and contains all the information from a DNS
 * packet in it
 */
public class DNSQuery {
    private short transactionID; //randomly generated ID that identifies the query
    private short flags; //flags that determine what kind of query it is
    private short questions; //number of questions
    private short answerRRs; //number of answers
    private short authorityRRs; //number of authority RRs
    private short additionalRRs; //number of any additional info
    private Question[] ques; //array of questions
    private Answer[] ans; //array of answers
    private Authority[] auth; //array of authority RRs

    /**
     * Creating a basic request query
     * @param name the name of the domain that IP address is inquired for
     * @param type the type of inquiry
     */
    public DNSQuery(String name, String type){
        Random r;

        r = new Random();
        this.transactionID = (short)r.nextInt(65536);
        this.flags = 0x0100;
        this.questions = 0x0001;
        this.answerRRs = 0x0000;
        this.authorityRRs = 0x0000;
        this.additionalRRs = 0x0000;
        ques = new Question[questions];
        ques[0] = new Question(name,type,"IN");
        ans = new Answer[answerRRs];
        auth = new Authority[authorityRRs];
    }

    /**
     * Recreating DNS Query using the bytes
     * All the information will be put into approperiate variables
     * @param content byte array that contains all the information
     */
    public DNSQuery(byte[] content) {
        //Declarations
        String name, type, value, xClass;
        short i, dataLength;
        int ttl;

        //initializing the variables.
        name = "";
        type = "";
        value = "";
        i = 12;

        //getting all the information from the bytes
        transactionID = getShortFromTwoBytes(content[0], content[1]);
        flags = getShortFromTwoBytes(content[2], content[3]);
        questions = getShortFromTwoBytes(content[4], content[5]);
        answerRRs = getShortFromTwoBytes(content[6], content[7]);
        authorityRRs = getShortFromTwoBytes(content[8], content[9]);
        additionalRRs = getShortFromTwoBytes(content[10], content[11]);
        ques = new Question[questions];
        ans = new Answer[answerRRs];
        auth = new Authority[authorityRRs];

        //adding all the information to the arrays of data structures
        for (short k = 0; k < questions; k++) {
            for (; content[i] != 0; i++) {
                name += (char) content[i];
            } i++;
            type = getTypeString(getShortFromTwoBytes(content[i], content[i + 1])); i+=2;
            ques[k] = new Question(name, type, "IN");
            name = "";
            type = "";
        }

        for(short k = 0; k < answerRRs; k++) {
            for(; content[i] != 0; i++){
                name += (char) content[i];
            } i++;
            type = getTypeString(getShortFromTwoBytes(content[i], content[i + 1])); i+=2;
            xClass = getClassString(getShortFromTwoBytes(content[i], content[i + 1])); i+=2;
            ttl = getIntFromFourBytes(content[i], content[i+1], content[i+2], content[i+3]); i+=4;
            dataLength = getShortFromTwoBytes(content[i],content[i+1]); i+=2;
            for(int j = 0; j < dataLength; j++){
                value += (char)content[i+j];
            } i+=dataLength;

            ans[k] = new Answer(name,type,xClass,ttl,dataLength,value);

            name = "";
            type = "";
            value = "";
        }

        for(short k = 0; k < authorityRRs; k++) {
            for(; content[i] != 0; i++){
                name += (char) content[i];
            } i++;
            type = getTypeString(getShortFromTwoBytes(content[i], content[i + 1])); i+=2;
            xClass = getClassString(getShortFromTwoBytes(content[i], content[i + 1])); i+=2;
            ttl = getIntFromFourBytes(content[i], content[i+1], content[i+2], content[i+3]); i+=4;
            dataLength = getShortFromTwoBytes(content[i],content[i+1]); i+=2;
            for(int j = 0; j < dataLength; j++){
                value += (char)content[i+j];
            } i+=dataLength;

            auth[k] = new Authority(name,type,xClass,ttl,dataLength,value);

            name = "";
            type = "";
            value = "";
        }
    }



    /**
     * Packages the entire DNS query into an array of bytes. Followed by
     * creating the datagram packet to send
     * @param IPAddress Destination IP address of the receiver
     * @param port Destination port of the receiver
     * @return the datagram to send
     */
    public DatagramPacket getPacket(InetAddress IPAddress, int port){
        DatagramPacket packet;
        byte content[];

        content = new byte[0];

        content = addBytes(content,getByteFromShort(transactionID));
        content = addBytes(content,getByteFromShort(flags));
        content = addBytes(content,getByteFromShort(questions));
        content = addBytes(content,getByteFromShort(answerRRs));
        content = addBytes(content,getByteFromShort(authorityRRs));
        content = addBytes(content,getByteFromShort(additionalRRs));
        for(int i = 0; i < this.questions; i++) {
            content = addBytes(content,ques[i].getBytes());
        }
        for(int i = 0; i < this.answerRRs; i++) {
            content = addBytes(content,ans[i].getBytes());
        }
        for(int i = 0; i < this.authorityRRs; i++) {
            content = addBytes(content,auth[i].getBytes());
        }

        packet = new DatagramPacket(content, content.length, IPAddress,port);
        return packet;
    }

    /**
     * Gives string representation of Class
     * @param qClass short rep.
     * @return String rep.
     */
    private String getClassString(short qClass) {
        if(qClass == 1)
            return "IN";
        System.out.println("Unknown Class");
        return "";
    }

    /**
     * Creating an answer in the packet
     * @param name name
     * @param type type
     * @param aClass class
     * @param TTL Time To Live
     * @param dataLength Length of the answer
     * @param value answer
     */
    public void addAnswer(String name, String type, String aClass, int TTL, short dataLength,String value){
        Answer answer[];

        answerRRs++;
        answer = new Answer[answerRRs];
        System.arraycopy(ans, 0, answer, 0, answerRRs - 1);
        answer[answerRRs - 1] = new Answer(name,type,aClass,TTL,dataLength,value);
        ans = answer;

        flags |= 0x8080; // turns on the response bit and recursion available bit
    }



    /**
     * adds an authority nameserver's info to the array
     * @param name name of the server
     * @param type type of inquiry
     * @param aClass class of the inquiry
     * @param TTL Time to live
     * @param dataLength length of the upcoming data
     * @param value the value returned by authoritative nameserver
     */
    public void addAuthority(String name, String type, String aClass, int TTL,
                             short dataLength, String value){
        Authority authority[];

        authorityRRs++;
        authority = new Authority[authorityRRs];

        System.arraycopy(auth, 0, authority, 0, authorityRRs - 1);
        authority[answerRRs - 1] = new Authority(name,type,aClass,TTL,dataLength,value);
        auth = authority;

        flags |= 0x8080; // turns on the response bit and recursion available bit
    }

    /**
     * appends an array of bytes (toAdd) to the end of another array of bytes (content)
     * and returns the combo of both
     * @param content to be added to
     * @param toAdd to add
     * @return combination of both
     */
    public byte[] addBytes(byte[] content, byte[] toAdd) {
        byte[] toRet = new byte[content.length + toAdd.length];
        System.arraycopy(content, 0, toRet, 0, content.length);
        System.arraycopy(toAdd, 0, toRet, content.length, toAdd.length);
        return toRet;
    }

    /**
     * creates a short from two bytes
     * @param one first byte
     * @param two second byte
     * @return short
     */
    public short getShortFromTwoBytes(byte one, byte two) {
        short toRet;
        toRet = one;
        toRet <<= 8;
        toRet |= two;
        return toRet;
    }

    /**
     * creates an integer from four bytes
     * @param one first byte
     * @param two second byte
     * @param three third byte
     * @param four fourth byte
     * @return integer
     */
    public int getIntFromFourBytes(byte one, byte two, byte three, byte four) {
        int toRet;
        toRet = one; toRet <<= 8;
        toRet |= two; toRet <<= 8;
        toRet |= three; toRet <<= 8;
        toRet |= four;
        return toRet;
    }

    /**
     * get an array of 2 bytes from a short
     * @param input input to be turned into 2 bytes
     * @return 2 bytes encompassing the short
     */
    public byte[] getByteFromShort(short input){
        byte[] conv = new byte[2];
        conv[1] = (byte)(input & 0xff);
        input >>= 8;
        conv[0] = (byte)(input & 0xff);
        return conv;
    }

    /**
     * Given the value of the Type, gives you the string representation of the type
     * @param value the short representation
     * @return string representation
     */
    private String getTypeString(short value) {
        switch(value){
            case 1: return "A";
            case 2: return "NS";
            case 5: return "CNAME";
            case 6: return "SOA";
            case 9: return "R";
            case 15: return "MX";
            case 26: return "AAAA";
            default: System.out.println("Unknown Value"); return "";
        }
    }

    /**
     * getter for Transaction ID
     * @return transaction ID
     */
    public short getTransactionID() {
        return transactionID;
    }

    /**
     * getter for flags
     * @return flags
     */
    public short getFlags() {
        return flags;
    }

    /**
     * getter for the number of questions
     * @return number of questions
     */
    public short getQuestions() {
        return questions;
    }

    /**
     * getter for number of authority name server requests
     * @return number of authority name server requests
     */
    public short getAuthorityRRs() {
        return authorityRRs;
    }

    /**
     * getter for number of answers
     * @return number of answers
     */
    public short getAnswerRRs() {
        return answerRRs;
    }

    /**
     * getter for number of Additional RRs
     * @return number of Additional RRs
     */
    public short getAdditionalRRs() {
        return additionalRRs;
    }

    /**
     * getter for questions
     * @return question
     */
    public Question[] getQues() {
        return ques;
    }

    /**
     * getter for answers
     * @return answers
     */
    public Answer[] getAns() {
        return ans;
    }

    /**
     * getter for authorities
     * @return authorities
     */
    public Authority[] getAuth() {
        return auth;
    }


    /**
     * prints out all properties of the class
     * @return string containing all the info about this class
     */
    public String toString(){
        String toRet = "Transaction ID: " + getTransactionID()
                + "\nFlags: " + String.format("%16s", Integer.toBinaryString(getFlags())).replace(' ', '0')
                + "\nQuestios: " + getQuestions()
                + "\nDNS.Answer RRs: " + getAnswerRRs()
                + "\nDNS.Authority RRs: " + getAuthorityRRs()
                + "\nAdditional RRs: " + getAdditionalRRs()
                + "\nQueries:";
        for(int i = 0; i < getQuestions(); i++){
            toRet += "\n\tQuery #" + (i+1) + ": ";
            toRet += "\n" + getQues()[i].toString();
        }
        if(getAnswerRRs() > 0)
            toRet += "\nAnswers:";
        for (short i = 0; i < getAnswerRRs(); i++) {
            toRet += "\n\tDNS.Answer #" + (i + 1) + ": ";
            toRet += "\n" + getAns()[i].toString();
        }
        if(getAuthorityRRs() > 0)
            toRet += "\nAuthoritative Nameservers:";
        for (short i = 0; i < getAuthorityRRs(); i++) {
            toRet += "\n\tDNS.Authority #" + (i + 1) + ": ";
            toRet += "\n" + getAuth()[i].toString();
        }
        return toRet;
    }

}
