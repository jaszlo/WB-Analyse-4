package app.data;

/**
 * Helper data structure that a response body for the routes are partially encoded into
 */
public class MimeObject {
    public final String type;
    public final byte[] content;

    public MimeObject(String type, byte[] content) {
        this.type = type;
        this.content = content;
    }

}
