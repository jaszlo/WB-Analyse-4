package app.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Helper data structure that a request body for the routes are encoded into
 */
public class ResponseObject {
    public final int statusCode;
    private final MimeObject body;

    public ResponseObject(int statusCode, MimeObject body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public String bodyAsString() {
        return new String(body.content, StandardCharsets.UTF_8);
    }

    public Object bodyAsObject() {
        try(ByteArrayInputStream bis = new ByteArrayInputStream(this.body.content); ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
