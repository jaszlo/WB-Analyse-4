package app.analysis;

import app.graph.Graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * A class containing utility methods for calculating the Freeman centralization of a graph.
 */
public class Centralization {

    private Centralization() {} //prevent initialization

    /**
     * Calculates the Freeman centralization of a given graph with a certain centrality measure.
     * @param graph the graph
     * @param centralityMeasure a function computing a map from a representation of the vertices to their centralities
     * @return the centralization
     */
    public static double getCentralization(Graph graph, Function<Graph, Map<String,Double>> centralityMeasure)
    {
        var centralities = centralityMeasure.apply(graph).values();

        Graph star = new Graph("star"); // the star graph is the most centralized graph possible
        for (int i = 0; i < graph.getVertices().size(); i++)
        {
            star.addVertex(Integer.toString(i));
            if (i != 0)
            {
                star.setWeight("0", Integer.toString(i), 1);
            }
        }
        var maxCentralizationValues = centralityMeasure.apply(star).values();

        return sumOfDiffs(centralities)/sumOfDiffs(maxCentralizationValues); // normalize with the sum of differences in the star graph
    }

    /**
     * @param values Returns the sum of the differences of the values in the given collection to their maximum.
     * @return the sum of differences
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    private static double sumOfDiffs(Collection<Double> values)
    {
        double max = Collections.max(values);
        return values.stream().mapToDouble(c -> max - c).sum();
    }
}
