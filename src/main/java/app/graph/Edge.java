package app.graph;

/**
 * A class representing an edge in an undirected, weighted graph structure.
 */
public class Edge {

    private final Vertex[] vertices = new Vertex[2];

    private double weight;

    /**
     * Constructs a new edge with a given weight between two given vertices.
     * @param v1 one end of the edge
     * @param v2 the other end of the edge
     * @param weight the weight of the edge
     */
    Edge(Vertex v1, Vertex v2, double weight) {
        vertices[0] = v1;
        vertices[1] = v2;
        this.setWeight(weight);
    }

    /**
     * Returns the vertices that are connected by this edge.
     * @return an array containing the ends of the edge
     */
    public Vertex[] getVertices() {
        return vertices;
    }

    /**
     * Returns the vertex connected to the passed vertex by this edge.
     * @param v one of the ends of this edge
     * @return the other end
     * @throws IllegalArgumentException if the vertex is not one of the ends of this edge
     */
    public Vertex oppositeVertex(Vertex v) {
        if (v != vertices[0] && v != vertices[1])
        {
            throw new IllegalArgumentException("This edge does not connect to the passed vertex.");
        }
        return vertices[0] == v ? vertices[1] : vertices[0];
    }

    /**
     * Returns this edge's weight if {@code weighted} is true, and 1 otherwise.
     * @param weighted whether the graph should be treated as weighted
     * @return the weight or 1
     */
    public double getWeight(boolean weighted) {
        return weighted ? weight : 1;
    }

    /**
     * Returns this edge's weight.
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets this edge's weight.
     * @param weight the new weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

}
