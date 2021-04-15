package app.data;

import app.graph.Edge;
import app.graph.Graph;
import app.graph.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data class resembling the JSON that we want to parse for a GET at /api/graph
 */
public class AdjacencyList {
    public final Map<String, Map<String, Double>> data;

    public AdjacencyList(Graph g) {
        data = new HashMap<>();
        if (g == null) {
            return;
        }

        for (Vertex v : g.getVertices()) {
            Map<String, Double> adjacentWeight = new HashMap<>();
            for (Edge e : v.getEdges()) {
                adjacentWeight.put(e.oppositeVertex(v).getIdentifier(), e.getWeight());
            }
            data.put(v.getIdentifier(), adjacentWeight);
        }
    }

    public AdjacencyList(Map<String, Map<String, Double>> data) {
        this.data = data;
    }

    public Graph asGraph() {
        if(this.data == null) {
            return null;
        }

        Graph g = new Graph();
        for(String v: data.keySet()) {
            g.addVertex(v);
        }

        for(Map.Entry<String, Map<String, Double>> from : data.entrySet()) {
            for(Map.Entry<String, Double> to: from.getValue().entrySet()) {
                g.setWeight(from.getKey(), to.getKey(), to.getValue());
            }
        }

        return g;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AdjacencyList)) {
            return false;
        }
        AdjacencyList gd = (AdjacencyList) o;

        return Objects.equals(this.data, gd.data);
    }
}
