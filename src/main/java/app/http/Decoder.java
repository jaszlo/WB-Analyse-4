package app.http;

import app.Main;
import app.data.SVGRequest;
import app.db.GraphOptions;
import app.graph.Graph;
import app.http.logger.Logger;
import app.data.AnalysisRequest;
import app.data.AdjacencyList;
import app.data.InteractionRequest;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements a decoder that decodes objects received via http requests. The decoder uses UTF-8
 * if applicable.
 *
 */
public class Decoder {
    private static final Gson GSON = new Gson();

    /**
     * If possible parses the request body given via the http exchange into the data class InteractionRequest
     * @param exchange The exchange from which the body is decoded
     * @return The body parsed into the InteractionRequest data class
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    public static InteractionRequest asInteraction(HttpExchange exchange){

        String bodyAsString = Decoder.streamAsString(exchange.getRequestBody());
        Logger.logRequest(exchange, bodyAsString);

        JsonObject bodyAsJsonObject = Decoder.strictJsonObject(bodyAsString, "names", "duration", "datetime");

        String[] names = Decoder.strictJsonStringArray(bodyAsJsonObject, "names");

        if (!bodyAsJsonObject.get("datetime").isJsonPrimitive() || !bodyAsJsonObject.getAsJsonPrimitive("datetime").isNumber()) {
            throw new IllegalArgumentException("JSON field datetime is not a long");
        }

        if (!bodyAsJsonObject.get("duration").isJsonPrimitive() || !bodyAsJsonObject.getAsJsonPrimitive("duration").isNumber()) {
            throw new IllegalArgumentException("JSON field duration is not an int");
        }

        boolean isDocument = false;
        if (bodyAsJsonObject.has("document")) {
            if (!bodyAsJsonObject.get("document").isJsonPrimitive() || !bodyAsJsonObject.getAsJsonPrimitive("document").isBoolean()) {
                throw new IllegalArgumentException("JSON field document is not a boolean");
            }
            isDocument = bodyAsJsonObject.get("document").getAsBoolean();
        }

        return new InteractionRequest(names, bodyAsJsonObject.get("datetime").getAsLong(), bodyAsJsonObject.get("duration").getAsInt(), isDocument);
    }

    /**
     * If possible parses the request body given via the http exchange into single String
     * representing a name to delete from the database
     * @param exchange The exchange from which the body is decoded
     * @return The name to delete from the database
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    public static String asToDelete(HttpExchange exchange) {
       String bodyAsString = Decoder.streamAsString(exchange.getRequestBody());
       Logger.logRequest(exchange, bodyAsString);
       JsonObject bodyAsJsonObject = Decoder.strictJsonObject(bodyAsString, "name");

       return strictJsonString(bodyAsJsonObject, "name");
    }

    /**
     * If possible parses the request body given via the http exchange into the data class AnalysisRequest
     * @param exchange The exchange from which the body is decoded
     * @return The body parsed into the AnalysisRequest data class
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    public static AnalysisRequest asAnalysis(HttpExchange exchange){

        String bodyAsString = Decoder.streamAsString(exchange.getRequestBody());
        Logger.logRequest(exchange, bodyAsString);

        JsonObject bodyAsJsonObject = Decoder.strictJsonObject(bodyAsString, "centralities");

        Graph graph;
        if (bodyAsJsonObject.has("data")) {
            graph = GSON.fromJson(bodyAsJsonObject, AdjacencyList.class).asGraph(); // Todo: I have no idea what happens if this "fails"
        } else {
            graph = null;
        }

        String[] centralities = Decoder.strictJsonStringArray(bodyAsJsonObject, "centralities");

        boolean centralization = false;
        if (bodyAsJsonObject.has("centralization")) {
            if (!bodyAsJsonObject.get("centralization").isJsonPrimitive() || !bodyAsJsonObject.getAsJsonPrimitive("centralization").isBoolean()) {
                throw new IllegalArgumentException("JSON field centralization is not a boolean");
            }
            centralization = bodyAsJsonObject.get("centralization").getAsBoolean();
        }

        GraphOptions option = GraphOptions.valueOf(Main.properties.getProperty("default_graph_options"));
        if (bodyAsJsonObject.has("option")) {
            if (!bodyAsJsonObject.get("option").isJsonPrimitive() || !bodyAsJsonObject.getAsJsonPrimitive("option").isString()) {
                throw new IllegalArgumentException("JSON field option is not a string");
            }
            option = GraphOptions.valueOf(bodyAsJsonObject.get("option").getAsString());
        }

        return new AnalysisRequest(centralities, graph, option, centralization);
    }

    /**
     * If possible parses the request body given via the http exchange into the data class SVGRequest
     * @param exchange The exchange from which the body is decoded
     * @return The body parsed into the SVGRequest data class
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    public static SVGRequest asSVG(HttpExchange exchange) {
        String bodyAsString = Decoder.streamAsString(exchange.getRequestBody());
        Logger.logRequest(exchange, bodyAsString);

        JsonObject bodyAsJsonObject = Decoder.strictJsonObject(bodyAsString, "centrality");
        Graph graph;
        if (bodyAsJsonObject.has("data")) {
            graph = GSON.fromJson(bodyAsJsonObject, AdjacencyList.class).asGraph(); // Todo: I have no idea what happens if this "fails"
        } else {
            graph = null;
        }

        Map<String, String> colors;

        if (bodyAsJsonObject.has("colors")) {
            if (!bodyAsJsonObject.get("colors").isJsonObject()) {
                throw new IllegalArgumentException("Value of field data is not of expected type JSON object");
            }

            colors = new HashMap<>();
            for(Map.Entry<String, JsonElement> e :bodyAsJsonObject.getAsJsonObject("colors").entrySet()) {
                if(!e.getValue().isJsonPrimitive() || !e.getValue().getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException("Expected colors to be of type Map<String, String>");
                }

                colors.put(e.getKey(), e.getValue().getAsString());
            }


        } else {
            colors = null;
        }
        String display = "";
        int distance = -1;
        String centrality = strictJsonString(bodyAsJsonObject, "centrality");
        if (bodyAsJsonObject.has("display")) {
            display = strictJsonString(bodyAsJsonObject, "display");
            if (bodyAsJsonObject.has("distance")) {
                distance = Integer.parseInt(strictJsonString(bodyAsJsonObject, "distance"));
            }
        }
        return new SVGRequest(centrality, graph, colors, display, distance, GraphOptions.valueOf(Main.properties.getProperty("default_graph_options")));
    }

    /**
     * If possible parses the request body given via the http exchange into the enum GraphOptions.
     *
     * @param exchange The exchange from which the body is decoded
     * @return The body parsed into the SVGRequest data class
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    public static GraphOptions asGraphOption(HttpExchange exchange) {
        // Set to default
        String bodyAsString = Decoder.streamAsString(exchange.getRequestBody());
        Logger.logRequest(exchange, bodyAsString);

        if (bodyAsString.isEmpty()) {
            throw new IllegalArgumentException("Body is empty");
        }

        JsonObject bodyAsJsonObject = Decoder.strictJsonObject(bodyAsString, "option");

        String option = strictJsonString(bodyAsJsonObject, "option");

        try {
            return GraphOptions.valueOf(option);
        }catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for field 'option'");
        }

    }

    /**
     * Parses the body into a JsonObject but only if said json contains a number of given fields
     * @param json Json as a String to parse into a Json Object
     * @param requiredFields The field required in the Json String for parsing to be seen as successful
     * @return The parsed Json String as a Json Object
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    private static JsonObject strictJsonObject(String json, String... requiredFields) {
        JsonElement bodyAsJsonElement;
        try {
            bodyAsJsonElement = JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Body is not of expected type JSON");
        }

        if (!bodyAsJsonElement.isJsonObject()) {
            throw new IllegalArgumentException("Body is not of expected type JSON object");
        }

        JsonObject bodyAsJsonObject = bodyAsJsonElement.getAsJsonObject();

        for (String requiredKey: requiredFields) {
            if (!bodyAsJsonObject.has(requiredKey)) {
                throw new IllegalArgumentException("JSON is missing field: " + requiredKey);
            }
        }

        return bodyAsJsonObject;
    }

    /**
     * Gets the value for a given key of a Json Object as a String if it is a String.
     * @param json The Json Object from which to extract the value
     * @param fieldName The key value who's value is extracted
     * @return The String in the field
     * @throws IllegalArgumentException If field is not of type String
     */
    private static String strictJsonString(JsonObject json, String fieldName) {
        if (!json.get(fieldName).isJsonPrimitive() || !json.getAsJsonPrimitive(fieldName).isString()) {
            throw new IllegalArgumentException("JSON field " + fieldName + " is not a string");
        }

        return json.get(fieldName).getAsString();
    }

    /**
     * Gets the value for a given key of a Json Object as a String-Array if it is either already an Array or a single string.
     * @param json The Json Object from which to extract the value
     * @param fieldName The key value who's value is extracted
     * @return A String-Array of the value for the Json Element
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    private static String[] strictJsonStringArray(JsonObject json, String fieldName) {
        try {
            return strictJsonStringArray(json.get(fieldName));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("For the field " + fieldName + " the " + e.getMessage());
        }
    }

    /**
     * Gets the value of a Json Element as a String-Array if it is either already an Array or a single string.
     * @param json The Json Element from which to extract the value
     * @return A String-Array of the value for the Json Element
     * @throws IllegalArgumentException if the given input does not match the required format.
     */
    private static String[] strictJsonStringArray(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            return new String[]{json.getAsString()};
        }

        if (!json.isJsonArray()) {
            throw new IllegalArgumentException("JSON element is not an Array");
        }

        JsonArray jsonArray = json.getAsJsonArray();
        String[] elements = new String[jsonArray.size()];
        for (int i = 0; i < elements.length; i++) {
            if (!jsonArray.get(i).isJsonPrimitive() || !jsonArray.get(i).getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Array should only contain Strings");
            }
            elements[i] = jsonArray.get(i).getAsString();
        }

        return elements;
    }

    /**
     * Helper function that converts and consumes an Input Stream into a String
     * @param is Input stream to convert
     * @return String created from the Input String
     * @throws IllegalArgumentException if the stream could not be read
     */
    private static String streamAsString(InputStream is) {
        // Get the request-body as a String

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return rd.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Error while reading body stream.");
    }
}
