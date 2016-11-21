package DNS;

/**
 * Authoritative Name Server response class
 */
public class Authority {
    String name;
    String type;
    String aClass;
    int TTL;
    short dataLength;
    String value;

    /**
     * Constructor to make a authorityRR based on all the information provided
     * @param name name
     * @param type type
     * @param aClass class
     * @param TTL Time To Live
     * @param dataLength length of the upcoming data in value
     * @param value the nameserver returned
     */
    public Authority(String name, String type, String aClass, int TTL, short dataLength,
                     String value){
        this.name = name;
        this.type = type;
        this.aClass = aClass;
        this.TTL = TTL;
        this.dataLength = dataLength;
        this.value = value;

    }

    /**
     * adds a byte to an array of bytes
     * @param content array of bytes
     * @param toAdd byte to add
     * @return array of bytes including toAdd
     */
    public static byte[] addByte(byte[] content, byte toAdd) {
        byte[] toRet = new byte[content.length + 1];
        System.arraycopy(content, 0, toRet, 0, content.length);
        toRet[content.length] = toAdd;
        return toRet;
    }

    /**
     * gets 4 byte array representation of an integer
     *
     * @param input integer
     * @return 4 byte array
     */
    public static byte[] getByteFromInt(int input) {
        byte[] conv = new byte[4];
        conv[3] = (byte) (input & 0xff);
        input >>= 8;
        conv[2] = (byte) (input & 0xff);
        input >>= 8;
        conv[1] = (byte) (input & 0xff);
        input >>= 8;
        conv[0] = (byte) input;
        return conv;
    }

    /**
     * packages all the information here into an array of bytes and returns it
     * @return array of bytes containing all the information here
     */
    public byte[] getBytes(){
        byte[] content;

        content = new byte[0];
        content = addBytes(content, name.getBytes());
        content = addByte(content, (byte)0);
        content = addBytes(content, getByteFromShort(getTypeValue(type)));
        content = addBytes(content, getByteFromShort(getClassValue(aClass)));
        content = addBytes(content, getByteFromInt(TTL));
        content = addBytes(content, getByteFromShort(dataLength));
        content = addBytes(content, value.getBytes());

        return content;
    }

    /**
     * returns the bit representation (in the form of a short) of the String representation of Type
     * @param type String representation of type
     * @return short representation of type
     */
    private short getTypeValue(String type) {
        switch(type){
            case "A": return 1;
            case "NS": return 2;
            case "CNAME": return 5;
            case "SOA": return 6;
            case "MX": return 15;
            case "AAAA": return 26;
            default: System.out.println("Unknown Type"); return 0;
        }
    }

    /**
     * get an array of 2 bytes from a short
     * @param input input to be turned into 2 bytes
     * @return 2 bytes encompassing the short
     */
    public byte[] getByteFromShort(short input) {
        byte[] conv = new byte[2];
        conv[1] = (byte)(input & 0xff);
        input >>= 8;
        conv[0] = (byte)(input & 0xff);
        return conv;
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
     * gets short (16-bit) representation of class based on the string representation
     * @param qClass String representation
     * @return short representation
     */
    private short getClassValue(String qClass) {
        if(qClass.equals("IN"))
            return 1;
        System.out.println("Unknown Class");
        return 0;
    }

    /**
     * getter for name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * getter for the type of query
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * getter for the class of the query
     * @return class of the query
     */
    public String getaClass() {
        return aClass;
    }

    /**
     * return the time to live
     * @return time to live
     */
    public int getTTL() {
        return TTL;
    }

    /**
     * returns data length
     * @return data length
     */
    public short getDataLength() {
        return dataLength;
    }

    /**
     * returns value
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * prints out all properties of the class
     * @return string containing all the info about this class
     */
    public String toString(){
        return "\t\tName: " + getName() + "\n\t\tType: " + getType() + "\n\t\tClass: " + getaClass()
                + "\n\t\tTime to live: " + getTTL() + "\n\t\tData length: " + getDataLength()
                + "\n\t\tValue: " + getValue() + "\n";
    }
}
