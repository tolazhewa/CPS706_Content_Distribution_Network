package HTTP;

import java.util.ArrayList;

/**
 * HTTP GET request
 */
public class HTTPGet {
    private String method;
    private String url;
    private String version;
    private ArrayList<HeaderLine> headerLines;
    private String data;

    /**
     * Constructor to initialize an HTTP GET request
     * @param method method of the packet
     * @param url url of the file requested
     * @param version version of HTTP used
     * @param data any data to go alongside the header
     */
    public HTTPGet(String method, String url, String version, String data){
        this.method = method;
        this.url = url;
        this.version = version;
        headerLines = new ArrayList<>();
        this.data = data;
    }

    /**
     * Constructor to initialize an HTTP GET request (there is no data)
     * @param method method of the packet
     * @param url url of the file requested
     * @param version version of HTTP used
     */
    public HTTPGet(String method, String url, String version){
        this.method = method;
        this.url = url;
        this.version = version;
        headerLines = new ArrayList<>();
        data = "";
    }

    /**
     * Constructor to initialize an HTTP GET request
     * the request has been defaulted "GET" and "HTTP/1.1"
     * @param url url of the file requested
     */
    public HTTPGet(String url){
        this.method = "GET";
        this.url = url;
        this.version = "HTTP/1.1";
        headerLines = new ArrayList<>();
        data = "";
    }

    /**
     * Recreates a HTTP GET request based on bytes
     * @param content bytes of content
     */
    public HTTPGet(byte[] content){
        int i;
        String n,v;

        headerLines = new ArrayList<>();
        method = ""; url = ""; version = ""; data = ""; n = ""; v = "";

        for(i = 0; (char)content[i] != ' '; i++) {
            method += (char)content[i];
        }
        for(++i; (char)content[i] != ' '; i++) {
            url += (char)content[i];
        }
        for(++i; (char)content[i] != '\r'; i++) {
            version += (char)content[i];
        }
        for(i+=2; !((char)content[i] == '\r' && (char)content[i+1] == '\n'); i+=2){
            for(; (char)content[i] != ':'; i++){
                n += (char)content[i];
            }
            for(i+=2; (char)content[i] != '\r';i++){
                v += (char)content[i];
            }
            headerLines.add(new HeaderLine(n,v));
            n = ""; v = "";
        }
        for(i+=2; (char)content[i] != '\0'; i++){
            data += (char)content[i];
        }
    }

    /**
     * retrieves byte representation of the HTTP GET request
     * @return byte representation
     */
    public byte[] getBytes(){
        byte[] content;

        content = new byte[0];
        content = addBytes(content, getMethod().getBytes());
        content = addByte(content, (byte)' ');
        content = addBytes(content, getUrl().getBytes());
        content = addByte(content, (byte)' ');
        content = addBytes(content, getVersion().getBytes());
        content = addByte(content, (byte)'\r');
        content = addByte(content, (byte)'\n');
        for(HeaderLine a: getHeaderLines()){
            content = addBytes(content, a.getName().getBytes());
            content = addByte(content, (byte)':');
            content = addByte(content, (byte)' ');
            content = addBytes(content, a.getValue().getBytes());
            content = addByte(content, (byte)'\r');
            content = addByte(content, (byte)'\n');
        }
        content = addByte(content, (byte)'\r');
        content = addByte(content, (byte)'\n');
        content = addBytes(content, getData().getBytes());

        return content;
    }

    /**
     * adds a byte to an array of bytes
     * @param content array of bytes
     * @param toAdd byte to add
     * @return array of bytes including toAdd
     */
    public static byte[] addByte(byte[] content, byte toAdd) {
        byte[] toRet = new byte[content.length + 1];
        System.arraycopy(content,0,toRet,0,content.length);
        toRet[content.length] = toAdd;
        return toRet;
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
        System.arraycopy(content,0,toRet,0,content.length);
        System.arraycopy(toAdd,0,toRet,content.length,toAdd.length);
        return toRet;
    }

    /**
     * returns a string showing all the information
     * @return information
     */
    public String toString(){
        String a;

        a = this.getMethod() + " " + this.getUrl() + " " + this.getVersion() + "\\r\\n";
        for(HeaderLine h: getHeaderLines()){
            a += h.toString();
        }
        a += "\n\\r\\n";
        a += getData();
        return a;
    }

    /**
     * returns method used to get
     * @return method
     */
    public String getMethod() {
        return method;
    }

    /**
     * returns the URL of the file requested
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * returns the version of HTTP used
     * @return HTTP version
     */
    public String getVersion() {
        return version;
    }

    /**
     * returns the header lines
     * @return Header Lines
     */
    public ArrayList<HeaderLine> getHeaderLines() {
        return headerLines;
    }

    /**
     * returns any data attached to the request
     * @return data
     */
    public String getData() {
        return data;
    }
}
