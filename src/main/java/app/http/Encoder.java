package app.http;


import app.data.MimeObject;
import app.graph.Graph;
import app.http.logger.Logger;
import app.http.logger.LoggerLevel;
import app.data.AdjacencyList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class implements an encoder that encodes objects for sending via http requests/responses. The encoder uses UTF-8
 * for all encodings if applicable.
 *
 */
@SuppressWarnings("rawtypes")
public class Encoder {

    private static final Gson GSON = new Gson();

    /**
     * Returns the given object encoded in a byte array. Given the list of mime types, the first supported type is
     * being used. The chosen mime type is returned as well.
     *
     * @param object The object to encode.
     * @param mimeTypes Mime types ordered by priority.
     * @return Encoded object as byte[] and chosen mime type wrapped in an object.
     */
    public static MimeObject encode(Object object, List<String> mimeTypes) {
        List<String> mimeTypesWithFallback = mimeTypes != null? new ArrayList<>(mimeTypes): new ArrayList<>();
        mimeTypesWithFallback.add("application/json");

        for(String mimeType: mimeTypesWithFallback) {
            if(mimeType.toLowerCase().startsWith("text")) {
                if(object instanceof String)        return new MimeObject(mimeType, toText((String) object));
                if(object instanceof InputStream)   return new MimeObject(mimeType, toText((InputStream) object));
            } else if(mimeType.toLowerCase().startsWith("image/svg+xml")) {
                if(object instanceof String)        return new MimeObject(mimeType, toSvg((String) object));
            } else if(mimeType.toLowerCase().startsWith("application/x-java-serialized-object")) {
                if(object instanceof Map)           return new MimeObject(mimeType, toSerialize((Map) object));
                if(object instanceof Graph)         return new MimeObject(mimeType, toSerialize((Graph) object));
            } else if(mimeType.toLowerCase().startsWith("application/json")) {
                if(object instanceof String)        return new MimeObject(mimeType, toJson((String) object));
                if(object instanceof Graph)         return new MimeObject(mimeType, toJson((Graph) object));
                if(object instanceof JsonElement)   return new MimeObject(mimeType, toJson((JsonElement) object));
                if(object instanceof Map)           return new MimeObject(mimeType, toJson((Map) object));
                if(object instanceof List)          return new MimeObject(mimeType, toJson((List) object));
            }
        }
        Logger.log("Error", "Could not find fitting encoding method for any type of " + mimeTypes, LoggerLevel.BASIC);
        throw new IllegalArgumentException("Could not find fitting encoding method for any type of " + mimeTypes);
    }

    /**
     * Returns the given string encoded encoded with UTF-8 in a byte array.
     *
     * @param s String to encode.
     * @return Encoded string.
     */
    private static byte[] toText(String s) {
        return stringToBytes(s);
    }

    /**
     * Returns the given InputStream encoded as text in a byte array. the stream is getting read, but not closed.
     *
     * @param s Stream to encode.
     * @return Encoded InputStream.
     */
    private static byte[] toText(InputStream s) {
        return toText(new Scanner(s, StandardCharsets.UTF_8).useDelimiter("\\A").next());
    }



    /**
     * Returns the given svg string encoded with UTF-8.
     *
     * @param s String to encode.
     * @return Encoded string.
     */
    private static byte[] toSvg(String s) {
        return stringToBytes(s);
    }



    /**
     * Returns the given graph encoded as a serialized java object. The graph gets serialized as a map
     * (Map[String: vertex id 1 -> Map[String: vertex id 2 -> Double: edge weight]: neighbors]) for cross project support.
     *
     * @param g Graph to encode.
     * @return Encoded string.
     */
    private static byte[] toSerialize(Graph g) {
        return toSerialize((Object) new AdjacencyList(g).data);
    }

    /**
     * Returns the given map encoded as a serialized java object.
     *
     * @param m Map to encode.
     * @return Encoded map.
     */
    private static byte[] toSerialize(Map m) {
        return toSerialize((Object) m);
    }

    /**
     * Returns the given object encoded as a serialized java object.
     *
     * @param o Object to encode.
     * @return Encoded object.
     */
    private static byte[] toSerialize(Object o) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(o);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("Could not serialize object: " + o);
    }



    /**
     * Returns the given string with UTF-8 in a byte array. It is assumed that the string is in the JSON format.
     *
     * @param s String to encode.
     * @return Encoded string.
     */
    private static byte[] toJson(String s) {
        return stringToBytes(s);
    }


    /**
     * Returns the given list encoded as a JSON UTF-8 string in a byte array.
     *
     * @param l List to encode.
     * @return Encoded List
     */
    private static byte[] toJson(List<?> l) {
        return stringToBytes(GSON.toJson(l));
    }

    /**
     * Returns the given graph encoded as a JSON UTF-8 string in a byte array.
     * (Map[String: vertex id 1 -> Map[String: vertex id 2 -> Double: edge weight]: neighbors]) for cross platform support.
     *
     * @param g Graph to encode.
     * @return Encoded graph.
     */
    private static byte[] toJson(Graph g) {
        // Create a parsable graph data structure for GSON
        AdjacencyList data = new AdjacencyList(g);
        return stringToBytes(GSON.toJsonTree(data).getAsJsonObject().getAsJsonObject("data").toString());
    }

    /**
     * Returns the given JsonElement encoded as JSON UTF-8 string in a byte array.
     *
     * @param j JsonElement to encode.
     * @return Encoded JsonElement.
     */
    private static byte[] toJson(JsonElement j) {
        return stringToBytes(j.toString());
    }

    /**
     * Returns the given map encoded as JSON UTF-8 string in a byte array.
     *
     * @param m Map to encode.
     * @return Encoded map.
     */
    private static byte[] toJson(Map m) {
        return stringToBytes(GSON.toJson(m));
    }



    /**
     * Returns the given string encoded as an UTF-8 string in a byte array.
     *
     * @param s String to encode.
     * @return Encoded string.
     */
    private static byte[] stringToBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

}
