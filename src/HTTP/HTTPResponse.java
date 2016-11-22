package HTTP;

import java.util.ArrayList;

/**
 * HTTP Response
 */
public class HTTPResponse {
    private String requestVersion;
    private String statusCode;
    private String responsePhrase;
    private ArrayList<HeaderLine> headerLines;
    private byte[] data;

    /**
     * constructor for HTTP response
     *
     * @param requestVersion request version of HTTP
     * @param statusCode     status code (200, etc)
     * @param responsePhrase response phrase with the status code
     */
    public HTTPResponse(String requestVersion, String statusCode, String responsePhrase) {
        this.requestVersion = requestVersion;
        this.statusCode = statusCode;
        this.responsePhrase = responsePhrase;
        headerLines = new ArrayList<>();
        this.data = new byte[0];
    }

    /**
     * constructor for HTTP response
     *
     * @param requestVersion request version of HTTP
     * @param statusCode     status code (200, etc)
     * @param responsePhrase response phrase with the status code
     */
    public HTTPResponse(String requestVersion, String statusCode, String responsePhrase, byte[] data) {
        this.requestVersion = requestVersion;
        this.statusCode = statusCode;
        this.responsePhrase = responsePhrase;
        this.headerLines = new ArrayList<>();
        this.data = data;
    }

    /**
     * recreates HTTP response based on bytes
     *
     * @param content bytes to recreate HTTP response
     */
    public HTTPResponse(byte[] content) {
        int i;
        String n, v;

        headerLines = new ArrayList<>();
        requestVersion = ""; statusCode = ""; responsePhrase = ""; n = ""; v = "";
        data = new byte[0];

        for (i=0; (char) content[i] != ' '; i++) {
            requestVersion += (char) content[i];
        }
        for (++i; (char) content[i] != ' '; i++) {
            statusCode += (char) content[i];
        }
        for (++i; (char) content[i] != '\r'; i++) {
            responsePhrase += (char) content[i];
        }
        for (i+=2; !((char)content[i] == '\r' && (char)content[i + 1] == '\n'); i+=2) {
            for (; (char) content[i] != ':'; i++) {
                n += (char) content[i];
            }
            for (i += 2; (char) content[i] != '\r'; i++) {
                v += (char) content[i];
            }
            headerLines.add(new HeaderLine(n, v));
            n = "";
            v = "";
        }
        for (i+=2; (char) content[i] != '\0'; i++) {
            data = addByte(this.data,content[i]);
        }
    }

    /**
     * adds a byte to an array of bytes
     *
     * @param content array of bytes
     * @param toAdd   byte to add
     * @return array of bytes including toAdd
     */
    public static byte[] addByte(byte[] content, byte toAdd) {
        byte[] toRet = new byte[content.length + 1];
        System.arraycopy(content, 0, toRet, 0, content.length);
        toRet[content.length] = toAdd;
        return toRet;
    }

    /**
     * retrieves byte representation of the HTTP response
     *
     * @return byte representation
     */
    public byte[] getBytes() {
        byte[] content;

        content = new byte[0];
        content = addBytes(content, getRequestVersion().getBytes());
        content = addByte(content, (byte) ' ');
        content = addBytes(content, getStatusCode().getBytes());
        content = addByte(content, (byte) ' ');
        content = addBytes(content, getResponsePhrase().getBytes());
        content = addByte(content, (byte) '\r');
        content = addByte(content, (byte) '\n');
        for (HeaderLine a : getHeaderLines()) {
            content = addBytes(content, a.getName().getBytes());
            content = addByte(content, (byte) ':');
            content = addByte(content, (byte) ' ');
            content = addBytes(content, a.getValue().getBytes());
            content = addByte(content, (byte) '\r');
            content = addByte(content, (byte) '\n');
        }
        content = addByte(content, (byte) '\r');
        content = addByte(content, (byte) '\n');
        content = addBytes(content, getData());

        return content;
    }

    /**
     * appends an array of bytes (toAdd) to the end of another array of bytes (content)
     * and returns the combo of both
     *
     * @param content to be added to
     * @param toAdd   to add
     * @return combination of both
     */
    public byte[] addBytes(byte[] content, byte[] toAdd) {
        byte[] toRet = new byte[content.length + toAdd.length];
        System.arraycopy(content, 0, toRet, 0, content.length);
        System.arraycopy(toAdd, 0, toRet, content.length, toAdd.length);
        return toRet;
    }

    /**
     * returns a string showing all the information
     *
     * @return information
     */
    public String toString() {
        String a;

        a = this.getRequestVersion() + " " + this.getStatusCode() + " " + this.getResponsePhrase() + "\\r\\n";
        for (HeaderLine h : this.getHeaderLines()) {
            a += h.getName() + ": " + h.getValue() + "\\r\\n";
        }
        a += "\n\\r\\n\n";
        a += new String(this.getData());
        return a;
    }

    /**
     * returns request version
     *
     * @return request version
     */
    public String getRequestVersion() {
        return requestVersion;
    }

    /**
     * returns status code
     *
     * @return status code
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * returns response phrase
     *
     * @return response phrase
     */
    public String getResponsePhrase() {
        return responsePhrase;
    }

    /**
     * returns header lines
     *
     * @return header lines
     */
    public ArrayList<HeaderLine> getHeaderLines() {
        return headerLines;
    }

    /**
     * returns data
     *
     * @return data
     */
    public byte[] getData() {
        return data;
    }
}
