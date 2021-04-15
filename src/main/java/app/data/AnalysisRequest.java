package app.data;

import app.analysis.Centrality;
import app.db.GraphOptions;
import app.graph.Graph;
import app.http.Server;

import java.util.Map;
import java.util.function.Function;

/**
 * Helper data structure that a request body for the /api/network-analysis routes are decoded into
 */
public class AnalysisRequest {
    private final String[] centralities;
    private final Graph data;
    private final boolean centralization;

    public AnalysisRequest(String[] centralities, Graph data, GraphOptions options, boolean centralization) {
        this.centralities = centralities;
        this.data = data != null ? data : Server.db.generateGraph(options);
        this.data.invertEdgeWeights();
        this.data.normalizeEdgeWeights();
        this.centralization = centralization;
    }

    public Graph getGraph() {
        return this.data;
    }

    public String getCentrality(int i) {
        return centralities[i];
    }

    public Function<Graph, Map<String, Double>> getCentralityFunction(int i) {
        switch (centralities[i].toLowerCase()) {
            case "closeness":
                return Centrality::getClosenessCentralities;
            case "betweenness":
                return Centrality::getBetweennessCentralities;
            case "eigenvector":
                return Centrality::getEigenvectorCentralities;
            case "harmonic":
                return Centrality::getHarmonicCentralities;
            case "degree":
                return Centrality::getDegreeCentralities;
            case "weighteddegree":
                return Centrality::getWeightedDegreeCentralities;
        }

        throw new IllegalArgumentException("Specified centrality not implemented");
    }

    public int getCentralitySize() {
        return centralities.length;
    }

    public boolean centralizationRequested(){return this.centralization;}
}
