import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.util.Map;
import java.util.HashMap;
import java.io.ObjectInputStream;
import java.net.http.HttpRequest.BodyPublishers;

public class ApiUsageHalle {

    /**
     * Calculates the interaction graph.
     *
     * @return A map that projects from user id to neighbor user id to the edge weight.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Double>> getGraph() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://goethe.se.uni-hannover.de:9993/api/graph"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/x-java-serialized-object")
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            try (ByteArrayInputStream bis = new ByteArrayInputStream(response.body()); ObjectInput in = new ObjectInputStream(bis)) {
                return ((Map<String, Map<String, Double>>) in.readObject());
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    /**
     * Sends a request with a specifier that determines how edge weights are calculated
     * @param option "count", "duration", "product" or "flow" allowed. Else "count" is returned as default
     * @return A map that projects from user id to neighbor user id to the edge weight.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Double>> getGraph(String option) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://goethe.se.uni-hannover.de:9993/api/graph"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/x-java-serialized-object")
                .POST(BodyPublishers.ofString("{\"option\": \"" + option + "\"}"))
                .build();
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            try (ByteArrayInputStream bis = new ByteArrayInputStream(response.body()); ObjectInput in = new ObjectInputStream(bis)) {
                return ((Map<String, Map<String, Double>>) in.readObject());
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }


    public static void main(String[] args)  {
        System.out.println(getGraph("count"));
        System.out.println(getGraph("duration"));
        System.out.println(getGraph("product"));
        System.out.println(getGraph()); // Is the same as getGraph("count");
    }
}