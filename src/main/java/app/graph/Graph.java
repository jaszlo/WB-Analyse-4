package app.graph;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * A class representing an undirected, weighted graph.
 */
public class Graph {

    private final HashSet<Edge> edges = new HashSet<>();

    private final HashMap<String, Vertex> vertices = new HashMap<>();

    private final LinkedList<Vertex> orderedVertices = new LinkedList<>();

    private final String description;

    /**
     * Constructs a new empty graph with the given description.
     * @param description the description
     */
    public Graph(String description) {
        this.description = description;
    }

    /**
     * Constructs a new empty graph with the description "None".
     */
    public Graph() {
        this.description = "None";
    }

    /**
     * Creates a graph from an input stream.
     *
     * @param stream The input stream to read
     * @return the created graph
     */
    public static Graph readGraph(InputStream stream) {

        Graph graph = new Graph();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){

            String line = reader.readLine();
            int vertex_amount = -1, vertex_at = -1;
            //Zeilenweise interpretieren
            while (line != null) {
                if (line.charAt(0) != '#') { //Kommetarzeilen ignorieren
                    if (vertex_amount == -1) { //Warten auf Zeile mit Knotenanzahl
                        vertex_amount = Integer.parseInt(line);
                        vertex_at = 1;
                    } else if (vertex_at != -1 && vertex_at <= vertex_amount) { //Knoten einlesen bis Knotenanzahl erreicht
                        vertex_at++;
                        graph.addVertex(line);
                    } else if (vertex_at > vertex_amount) { //Kanten einlesen bis Ende der Datei erreicht ist
                        String[] split = line.split(" ");
                        if (!split[0].equals(split[1]))
                            graph.addEdge(graph.getVertex(split[0]), //Kante in Graph einfügen
                                    graph.getVertex(split[1]), Double.parseDouble(split[2]));
                    }
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return graph;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Adds a new vertex to this graph.
     *
     * @param identifier identifier of the new vertex
     * @return the added Vertex
     * @throws IllegalArgumentException if a vertex with the given identifier already exists
     */
    public Vertex addVertex(String identifier) {
        if (vertices.containsKey(identifier))
            throw new IllegalArgumentException("Couldn't add Vertex: " + identifier + " is already used.");
        Vertex ver = new Vertex(identifier);
        vertices.put(identifier, ver);

        orderedVertices.add(ver);
        Collections.sort(orderedVertices);
        return ver;
    }

    /**
     * Returns the vertex with the specified identifier, or {@code null} if no such vertex exists.
     *
     * @param identifier the desired vertex's identifier
     * @return the vertex, or {@code null} if no such vertex exists
     */
    public Vertex getVertex(String identifier) {
        return vertices.get(identifier);
    }

    /**
     * Returns a collection of all vertices in this graph.
     *
     * @return a collection of all the vertices
     */
    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    /**
     * Returns an ordered list of all vertices in this graph.
     * This ordering is consistent between method calls, but the call is more expensive
     * than {@link #getVertices()}.
     *
     * @return the list of vertices
     */
    public List<Vertex> getOrderedVertices() {
        return orderedVertices;
    }

    /**
     * Returns this weighted graph's adjacency matrix.
     * The order is the one returned by {@link #getOrderedVertices()}. Unconnected entries contain infinity.
     *
     * @return the adjacency matrix
     */
    public double[][] getWeightedAdjacencyMatrix() {
        double[][] mat = new double[vertices.size()][vertices.size()];
        List<Vertex> vs = getOrderedVertices();
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                mat[i][j] = Optional.ofNullable(getEdge(vs.get(i), vs.get(j)))
                        .map(Edge::getWeight).orElse(Double.POSITIVE_INFINITY);
            }
        }
        return mat;
    }

    /**
     * Returns this graph's adjacency matrix as if it were an unweighted graph.
     * The order is the one returned by {@link #getOrderedVertices()}.
     *
     * @return the adjacency matrix
     */
    public double[][] getUnweightedAdjacencyMatrix() {
        return Arrays.stream(getWeightedAdjacencyMatrix())
                .map(Arrays::stream)
                .map(row -> row.map(d -> d == Double.POSITIVE_INFINITY ? 0 : 1))
                .map(DoubleStream::toArray)
                .toArray(double[][]::new);
    }

    /**
     * Removes the vertex with a certain identifier, if present.
     *
     * @param identifier the identifier of the vertex to be removed.
     */
    public void removeVertex(String identifier) {
        if (vertices.containsKey(identifier)) {
            Vertex removal = vertices.get(identifier);
            for (Edge e : removal.getEdges()) {
                Vertex opposite = e.oppositeVertex(removal);
                opposite.getEdgesByVertex().remove(removal);
            }
            removal.getEdgesByVertex().clear();
            vertices.remove(identifier);
            orderedVertices.remove(removal);
        }
    }

    /**
     * Removes the given vertex, if present.
     *
     * @param v the vertex to be removed.
     */
    public void removeVertex(Vertex v) {
        removeVertex(v.getIdentifier());
    }

    /**
     * Adds an edge with a specified weight between two given unconnected vertices.
     *
     * @param v1     one end of the edge
     * @param v2     the other end of the edge
     * @param weight the weight of the edge
     * @return the created edge
     * @throws IllegalArgumentException if the vertices are already connected by an edge.
     */
    public Edge addEdge(Vertex v1, Vertex v2, double weight) {
        if (v1.hasEdgeWith(v2))
            throw new IllegalArgumentException("Could not add Edge between " + v1.getIdentifier() + " and "
                    + v2.getIdentifier() + " because that Edge already exists");
        Edge edge = new Edge(v1, v2, weight);
        edges.add(edge);
        v1.getEdgesByVertex().put(v2, edge);
        v2.getEdgesByVertex().put(v1, edge);
        return edge;
    }

    /**
     * Adds an edge with weight 1 between two given unconnected vertices.
     *
     * @param v1 one end of the edge
     * @param v2 the other end of the edge
     * @return the created edge
     * @throws IllegalArgumentException if the vertices are already connected by an edge.
     */
    public Edge addEdge(Vertex v1, Vertex v2) {
        return addEdge(v1, v2, 1);
    }

    /**
     * Adds an edge with a specified weight between two unconnected vertices given by identifiers.
     *
     * @param identifier1 one end of the edge
     * @param identifier2 the other end of the edge
     * @param weight      the weight of the edge
     * @return the created edge
     * @throws IllegalArgumentException if the vertices are already connected by an edge.
     */
    public Edge addEdge(String identifier1, String identifier2, double weight) {
        if (vertices.containsKey(identifier1) && vertices.containsKey(identifier2)) {
            return addEdge(vertices.get(identifier1), vertices.get(identifier2), weight);
        } else {
            throw new IllegalArgumentException("Could not add Edge since " + identifier1 + " or " + identifier2 + " is not a Vertex");
        }
    }

    /**
     * Adds an edge with a weight 1 between two unconnected vertices given by identifiers.
     *
     * @param identifier1 one end of the edge
     * @param identifier2 the other end of the edge
     * @return the created edge
     * @throws IllegalArgumentException if the vertices are already connected by an edge.
     */
    public Edge addEdge(String identifier1, String identifier2) {
        return addEdge(identifier1, identifier2, 1);
    }

    /**
     * Sets the weight of the edge between two specified vertices, creating an edge if none exists.
     *
     * @param identifier1 the identifier of one of the vertices
     * @param identifier2 the identifier of the other vertex
     * @param weight      the weight to be set
     */
    public void setWeight(String identifier1, String identifier2, double weight) {
        Edge e = getEdge(identifier1, identifier2);
        if (e == null) {
            addEdge(identifier1, identifier2, weight);
        } else {
            e.setWeight(weight);
        }
    }

    /**
     * Removes the specified edge from this graph.
     * @param e the edge to be removed
     */
    public void removeEdge(Edge e) {
        edges.remove(e);
        for (Vertex v : e.getVertices())
            v.getEdgesByVertex().remove(e.oppositeVertex(v));
    }

    /**
     * Removes the edge between the two specified vertices if one exists.
     * @param v1 one end of the edge
     * @param v2 the other end of the edge
     */
    public void removeEdge(Vertex v1, Vertex v2) {
        if (v1.hasEdgeWith(v2))
        {
            removeEdge(v1.getEdgesByVertex().get(v2));
        }
    }

    /**
     * Removes the edge between the two vertices specified by the identifiers if one exists.
     * @param identifier1 the identifier of one of the vertices
     * @param identifier2 the identifier of the other vertex
     * @throws IllegalArgumentException if an identifier does not specify a vertex
     */
    public void removeEdge(String identifier1, String identifier2) {
        if (vertices.containsKey(identifier1) && vertices.containsKey(identifier2)) {
            removeEdge(vertices.get(identifier1), vertices.get(identifier2));
        } else {
            throw new IllegalArgumentException("Could not remove Edge since " + identifier1 + " or " + identifier2 + " is not a Vertex");
        }
    }

    /**
     * Returns the object representing the edge between two vertices specified by identifiers, or {@code null}
     * if they are not connected.
     *
     * @param identifier1 the identifier of one of the vertices
     * @param identifier2 the identifier of the other vertex
     * @return the edge between the vertices
     */
    public Edge getEdge(String identifier1, String identifier2) {
        return getEdge(getVertex(identifier1), getVertex(identifier2));
    }

    /**
     * Returns the object representing the edge between two vertices, or {@code null} if they are not connected.
     *
     * @param v1 one of the vertices
     * @param v2 the other vertex
     * @return the edge between the vertices
     */
    public Edge getEdge(Vertex v1, Vertex v2) {
        return v1.getEdgeWith(v2);
    }

    /**
     * Returns a collection of all the edges in this graph.
     *
     * @return a collection of the edges
     */
    public Collection<Edge> getEdges() {
        return edges;
    }

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(getDescription()).append(":\n");
		for (Vertex v : orderedVertices) {
			s.append(v.getIdentifier()).append(": [");
			s.append(v.getEdges().stream()
					.map((edge) -> edge.oppositeVertex(v).getIdentifier())
					.sorted().collect(Collectors.joining(", ")));
			s.append("]\n");
		}
		return s.toString();
	}

    /**
     * Checks whether this graph is connected.
     * @return whether this graph is connected
     */
    public boolean isConnected() {
        if (this.getVertices().isEmpty()) //by definition an empty graph is connected
            return true;
        HashSet<Vertex> visited = new HashSet<>();
        Queue<Vertex> worklist = new LinkedList<>();
        Vertex start = this.getVertices().iterator().next();
        worklist.add(start);
        while (!worklist.isEmpty()) { //breadth first search
            Vertex next = worklist.poll();
            if (visited.contains(next))
                continue;
            visited.add(next);
            for (Edge e : next.getEdges()) { //add neighbors to worklist
                if (!visited.contains(e.oppositeVertex(next)))
                    worklist.offer(e.oppositeVertex(next));
            }
        }
        return visited.size() == this.getVertices().size();
    }

    /**
     * Replaces all of the edge weights by their multiplicative inverse.
     */
    public void invertEdgeWeights()
    {
        edges.forEach(e -> e.setWeight(1 / e.getWeight()));
    }

    /**
     * Normalizes the edge weights so that the currently largest value is mapped to 1.
     */
    public void normalizeEdgeWeights()
    {
        double max = edges.stream().mapToDouble(Edge::getWeight).max().orElse(1);
        edges.forEach(e -> e.setWeight(e.getWeight() / max));
    }

    /**
     * Checks whether this graph is equal to another object.
     *
     * Two graphs are considered equal if there is a graph isomorphism between them that also preserves vertex
     * identifiers.
     * @param obj the object being compared to
     * @return whether the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Graph g = (Graph) obj;
        if (!this.getOrderedVertices().equals(((Graph) obj).getOrderedVertices()))
        {
            return false;
        }
        for (Vertex v : this.getVertices())
        {
            for (Vertex w : this.getVertices())
            {
                if (v.hasEdgeWith(w) != g.getVertex(v.getIdentifier()).hasEdgeWith(g.getVertex(w.getIdentifier())))
                {
                    return false;
                }
                if (v.hasEdgeWith(w) && getEdge(v, w).getWeight() != g.getEdge(v.getIdentifier(), w.getIdentifier()).getWeight())
                {
                    return false;
                }
            }
        }
        return true;
    }
}
