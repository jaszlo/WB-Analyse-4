package app.data;

import app.db.GraphOptions;
import app.graph.Graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SVGRequest extends AnalysisRequest {
    private final Map<String, String> DEFAULT_COLORS = Map.ofEntries(
            Map.entry("graph_bgcolor", "#121212"),
            Map.entry("graph_fontcolor", "#272727"),
            Map.entry("node_fontcolor", "#aaaaaa"),
            Map.entry("edge_color", "#272727"),
            Map.entry("edge_fontcolor", "#272727"),
            Map.entry("node_color_min", "#ff0000"),
            Map.entry("node_color_max", "#00ff00")
    );
    private final Map<String, String> colors;
    private final String display;
    private final int distance;

    public SVGRequest(String centrality, Graph data, Map<String, String> colors, String display, int distance, GraphOptions options) {
        super(new String[]{centrality}, data, options, false);

        Map<String, String> withDefaultColors = new HashMap<>(DEFAULT_COLORS);
        if(colors != null) withDefaultColors.putAll(colors);
        this.colors = withDefaultColors;
        this.display = display;
        this.distance = distance;
    }

    @Override
    public String getCentrality(int i) {
        return super.getCentrality(0);
    }

    public String getCentrality() {
        return this.getCentrality(0);
    }

    @Override
    public Function<Graph, Map<String, Double>> getCentralityFunction(int i) {
        return super.getCentralityFunction(0);
    }

    public Function<Graph, Map<String, Double>> getCentralityFunction() {
        return this.getCentralityFunction(0);
    }

    public Map<String, String> getColors() {
        return Collections.unmodifiableMap(this.colors);
    }

    public String getDisplay() { return this.display; }

    public int getDistance() { return this.distance; }
}
