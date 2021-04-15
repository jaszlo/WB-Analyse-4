package app.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class representing a vertex in a graph structure.
 */
public class Vertex implements Comparable<Vertex> {

    private final String identifier;

    private final HashMap<Vertex, Edge> edges_by_vertex = new HashMap<>();

    /**
     * Constructs a new vertex with the given identifier.
     * @param identifier the identifier
     */
    Vertex(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns a map from vertices connected to this vertex to the edges connecting them.
     * @return the map
     */
    HashMap<Vertex, Edge> getEdgesByVertex() {
        return edges_by_vertex;
    }

    /**
     * Returns this vertex's identifier.
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns all edges connected to this vertex.
     * @return a collection of the edges
     */
    public Collection<Edge> getEdges() {
        return edges_by_vertex.values();
    }

    /**
     * Returns all vertices that are directly connected to this one
     * @return All vertices directly connected to this one.
     */
    public Collection<Vertex> getNeighbors(){
    	return edges_by_vertex.keySet();
    }
    
    /**
     * Returns whether this vertex is connected to the specified vertex by an edge.
     * @param v the vertex to check connection to
     * @return whether the vertices are connected
     */
    public boolean hasEdgeWith(Vertex v) {
        return edges_by_vertex.containsKey(v);
    }

    /**
     * Returns the edge connecting this vertex to the specified vertex, or {@code null} if no such vertex exists.
     * @param v the other vertex to get the edge to
     * @return the edge connecting them or {@code null}
     */
    public Edge getEdgeWith(Vertex v) {
        return edges_by_vertex.get(v);
    }

    /**
     * Compares this vertex to another one lexicographically by identifier.
     * @param o the other vertex
     * @return the result of comparing the identifiers
     */
    @Override
    public int compareTo(Vertex o) {
        return this.getIdentifier().compareTo(o.getIdentifier());
    }

    /**
     * Checks this vertex for equality to another object.
     *
     * A vertex is equal to another one if they have the same identifier.
     * @param o the other object
     * @return whether the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return identifier.equals(vertex.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
    
    /**
     * Returns a set of all vertices that are reachable within {@code steps} steps.
     * 
     * @param steps the maximum number of steps within which vertices should be returned
     * @return all vertices that are reachable within {@code steps} steps. if steps <= 0, then only this vertex is returned
     */
    public Set<Vertex> getReachableWithin(int steps){
         Set<Vertex> visited = new HashSet<>();
         visited.add(this);         
         Set<Vertex> thisIteration = new HashSet<>();
         thisIteration.add(this);
         Set<Vertex> foundVertices = new HashSet<>();        
         for(int i = 0; i < steps; i++) {
        	 for(Vertex v : thisIteration) {
        		 for(Vertex neighbor : v.getNeighbors()) {
        			 if(visited.contains(neighbor) || foundVertices.contains(neighbor))
        				 continue;
        			 foundVertices.add(neighbor);
        		 }
        	 }
        	 thisIteration = foundVertices;
        	 visited.addAll(foundVertices);
        	 foundVertices = new HashSet<>();
         }        
         return visited;
    }
}
