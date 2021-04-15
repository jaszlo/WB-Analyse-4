import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.io.ObjectInputStream;
import static java.util.Map.entry;


public class ApiUsageEditor {

    /**
     * Calculates the interaction graph for the given ids.
     *
     *
     * @param ids Ids that the graph should contain.
     *
     * @return A map that projects from user id to neighbor user id to the edge weight.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Double>> getGraph(List<String> ids) {
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
                return ((Map<String, Map<String, Double>>) in.readObject()).entrySet().stream().filter(e -> ids.contains(e.getKey())).map(e -> entry(e.getKey(), e.getValue().entrySet().stream().filter(x -> ids.contains(x.getKey())).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue())))).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    /**
     * Sends a request with a specifier that determines how edge weights are calculated
     * @param option "count", "duration", "product" or "flow" allowed. Else "count" is returned as default
     * @param ids Ids that the graph should contain.
     * @return A map that projects from user id to neighbor user id to the edge weight.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Double>> getGraph(List<String> ids, String option) {
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
                return ((Map<String, Map<String, Double>>) in.readObject()).entrySet().stream().filter(e -> ids.contains(e.getKey())).map(e -> entry(e.getKey(), e.getValue().entrySet().stream().filter(x -> ids.contains(x.getKey())).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue())))).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    public static final List<String> supportedCentralities = List.of("closeness", "betweenness", "eigenvector", "harmonic", "degree", "weighteddegree");

    /**
     * Calculates the network centrality for the requested centrality. Supported are currently "closeness", "betweenness", "eigenvector" and "harmonic".
     * Centralities are only returned for the given ids.
     *
     * @param ids Ids that the centralities should be calculated for.
     * @param centrality Centrality to calculate.
     * @return A map that projects from user ids to the centrality value.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Double> getCentrality(String centrality, List<String> ids) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://goethe.se.uni-hannover.de:9993/api/network-analysis"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/x-java-serialized-object")
                .POST(BodyPublishers.ofString("{\"centralities\": [\"" + centrality + "\"]}"))
                .build();
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() != 200) {
                System.out.println(new String(response.body(), "utf8"));
                return new HashMap<>();
            }
            try (ByteArrayInputStream bis = new ByteArrayInputStream(response.body()); ObjectInput in = new ObjectInputStream(bis)) {
                return ((Map<String, Map<String, Double>>) in.readObject()).get(centrality).entrySet().stream().filter(e -> ids.contains(e.getKey())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    /**
     * Calculates the colors for each user for a map that projects from user ids to the centrality value.
     *
     * @param centrality Map that projects from user ids to the centrality value.
     * @return A Map that projects from user ids to the corresponding color.
     */
    public static Map<String, String> centralityToColor(Map<String, Double> centrality) {

        double min = centrality.isEmpty() ? 0: Collections.min(centrality.values());
        double max = centrality.isEmpty() ? 0: Collections.max(centrality.values());

        return centrality.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
                e -> getColor(max != min ? (e.getValue() - min) / (max - min): 0.5)
        ));
    }

    private static String getColor(double weight) {

        java.awt.Color minColor = java.awt.Color.decode("#ff0000");
        java.awt.Color maxColor = java.awt.Color.decode("#00ff00");

        float[] hsbMinColor = new float[3];
        float[] hsbMaxColor = new float[3];
        float[] hsbResult   = new float[3];

        java.awt.Color.RGBtoHSB(minColor.getRed(), minColor.getGreen(), minColor.getBlue(), hsbMinColor);
        java.awt.Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), hsbMaxColor);

        float diff = (Math.abs(hsbMaxColor[0] - hsbMinColor[0]) < 0.5) ? (hsbMaxColor[0] - hsbMinColor[0]) : hsbMaxColor[0] - hsbMinColor[0] - Math.signum(hsbMaxColor[0] - hsbMinColor[0]);
        hsbResult[0] = (float) (hsbMinColor[0] + (diff * weight) + 1) % 1;

        for(int i = 1; i < 3; i++) {
            diff = hsbMaxColor[i] - hsbMinColor[i];
            hsbResult[i] = (float) (hsbMinColor[i] + (diff * weight));
        }


        return String.format("#%06X", java.awt.Color.HSBtoRGB(hsbResult[0], hsbResult[1], hsbResult[2]) & 0x00FFFFFF);
    }

    public static void main(String[] args) {
        List<String> ids = List.of("23456789", "45678901", "67890123");
        // Print values and colors for harmonic centrality
        Map<String, Double> result = getCentrality("harmonic", ids);
        System.out.println(result);
        System.out.println(centralityToColor(result));

        // Print graph with edges and weights
        System.out.println(getGraph(ids, "INTERACTION_SUM"));
        System.out.println(getGraph(ids, "DURATION_SUM"));
        System.out.println(getGraph(ids, "INTERACTION_TIMES_DURATION"));
        System.out.println(getGraph(ids)); // Is the same as getGraph("count");

        // Print values and colors for all centralities
        for(String centrality: supportedCentralities) {
            System.out.println(centrality + ":");
            result = getCentrality(centrality, ids);
            System.out.println(result);
            System.out.println(centralityToColor(result));
        }
    }
}