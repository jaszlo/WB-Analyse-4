package app.analysis;

import app.graph.Edge;
import app.graph.Graph;
import app.graph.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Utility {

	/**
	 * Partitions the graph into two vertex disjunct sets S1, S2 such that there are no edges
	 * between the vertices in S1 and respectively in S2
	 * 
	 * @param graph An arbitrary graph to be partitioned
	 * @return The bipartite partition of the graph if it is bipartite. If the graph is not bipartite, null is returned
	 */
    public static  List<Set<Vertex>> bipartitePartition(Graph graph) {
    	List<Set<Vertex>> partition = new ArrayList<>();
    	HashMap<Vertex,Boolean> coloring = new HashMap<>();
        if (graph.getVertices().isEmpty()) //by definition an empty graph is bipartite
            return partition;
        HashSet<Vertex> vertices = new HashSet<>(graph.getVertices());
        while(!vertices.isEmpty()) { //do a partition for each connection component
        	HashSet<Vertex> visited = new HashSet<>();
	        Stack<Vertex> worklist = new Stack<>();
	        Vertex next = vertices.iterator().next();
	        coloring.put(next, true); //first color can be arbitrarily chosen
	        worklist.add(next);
	        while (!worklist.isEmpty()) { //depth first search
	            next = worklist.pop();
	            if(visited.contains(next))
	            	continue;
	            visited.add(next);
	            boolean color = coloring.get(next);
	            vertices.remove(next);
	            for (Edge e : next.getEdges()) { //add neighbors to worklist
	            	Vertex neighbor = e.oppositeVertex(next);
	            	boolean neighborColor = !color;
	            	//check if no color contradiction arises
	                if(coloring.containsKey(neighbor) && coloring.get(neighbor) != neighborColor) 
	                	return null;
	                coloring.put(neighbor,neighborColor);
	                if(!visited.contains(neighbor))
	                	worklist.push(neighbor);	                
	            }
	        }
        }
        //create the partition sets with each vertex of same color in the same set
        Set<Vertex> blue = new HashSet<>();
        Set<Vertex> red = new HashSet<>();
        partition.add(blue);
        partition.add(red);
        for(Map.Entry<Vertex,Boolean> entry : coloring.entrySet()) {
        	if(entry.getValue())
        		blue.add(entry.getKey());
        	else
        		red.add(entry.getKey());
        }
        return partition;
    }
	
    /**
     * Multiplies two Matrices and returns the result.
     * 
     * @param A matrix on the left side of the multiplication
     * @param B matrix on the right side of the multiplication
     * @throws IllegalArgumentException if the number of columns in A doesn't match the number of rows in B.
     * @return The matrix A*B
     */
	public static double[][] multiply(double[][] A, double[][] B){
		if(A[0].length != B.length) 
			throw new IllegalArgumentException("Wrong dimensions!");
		double[][] result = new double[A.length][B[0].length];
		for(int i = 0; i < result.length; i++) {
			for(int j = 0; j < result[i].length; j++) {
				for(int k = 0; k < A[0].length; k++) {
					result[i][j]+=A[i][k]*B[k][j];
				}
			}
		}
		return result;
	}

	/**
	 * Checks if a vector is the zero vector
	 * 
	 * @param V the vector to be tested
	 * @return Whether V is the zero vector
	 */
	public static boolean isZero(double[] V) {
		for(double v : V) {
			if(v != 0)
				return false;
		}
		return true;
	}
	
	
	/**
	 * Normalizes a vector 
	 * 
	 * @param V the vector to be normalized
	 * @throws IllegalArgumentException if V is the zero vector i.e. all entries are 0
	 */
	public static void normalize(double[] V) {
		double abs = 0;
		for(double v : V) 
			abs+= v*v;
		abs = Math.sqrt(abs);
		if(abs == 0) 
			throw new IllegalArgumentException("Can't normalize zero vector!");
		for(int i = 0; i< V.length; i++)
			V[i] /= abs;
	}
	
	/**
	 * Return the transpose of a Matrix A
	 * 
	 * @param A the matrix to be transposed
	 * @return The transpose A_t of A
	 */
	public static double[][] transpose(double[][] A){
		double[][] transpose = new double[A[0].length][A.length];
		for(int i = 0; i < A.length; i++) {
			for(int j = 0; j < A[i].length; j++)
				transpose[j][i] = A[i][j];
		}
		return transpose;
	}
	
	/**
	 * Multiplies the Vector V with a scalar lambda
	 * 
	 * @param V the vector to be modified
	 * @param lambda the scalar V is multiplied with
	 */
	public static void multiplyWithScalar(double[] V, double lambda) {
		for(int i = 0; i < V.length; i++)
			V[i]*=lambda;
	}
	
	/**
	 * Returns the composition of two vectors
	 * @param v1 first vector
	 * @param v2 second vector
	 * @return v1°v2
	 */
	public static double[] composeVectors(double[] v1, double[] v2) {
		double[] composition = new double[v1.length+v2.length];

		System.arraycopy(v1, 0, composition, 0, v1.length);
		System.arraycopy(v2, 0, composition, v1.length, v2.length);
		return composition;
	}

}
