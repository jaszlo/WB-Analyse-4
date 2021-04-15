package app.analysis;

import app.graph.Edge;
import app.graph.Graph;
import app.graph.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class containing static methods for computing different centrality measures on graphs.
 */
public class Centrality {
    private Centrality() {} //prevent initialization

    /**
     * Calculates the closeness centralities of all vertices in the given graph.
     *
     * @param graph a connected, non-empty graph without negative loops
     * @return a map from vertex identifiers to closeness centralities
     * @throws IllegalArgumentException if the graph is disconnected
     */
    public static Map<String, Double> getClosenessCentralities(Graph graph) {
        if (!graph.isConnected()) //check for connectedness
            throw new IllegalArgumentException("Closeness Centrality is undefined for an unconnected graph.");

        double[][] dists = graph.getWeightedAdjacencyMatrix();
		floydWarshallInPlace(dists); // calculate shortest paths

        HashMap<String, Double> result = new HashMap<>();
        List<Vertex> vertices = graph.getOrderedVertices();
        for (int i = 0; i < vertices.size(); i++) {
            double total = Arrays.stream(dists[i]).reduce(Double::sum).orElseThrow(); // sum of shortest paths to all nodes
            result.put(vertices.get(i).getIdentifier(), (dists.length - 1) / total);
        }
        return result;
    }
    
    /**
     * 
     * Calculates approximations of the eigenvector centralities of all vertices in the given graph.
	 *
	 * The method terminates either when it takes longer than {@code maxTime} or {@code trials} iterations have taken
	 * place.
     * 
     * @param graph a non-empty, connected graph
     * @param maxTime the maximum amount of time this method is allowed to run in milliseconds
     * @param err stop when the distance to the sought centralities is less than err
     * @return a map from vertex identifiers to the approximation of the eigenvector centralities
     */
    public static Map<String,Double> getEigenvectorCentralities(Graph graph, double err, int maxTime){
    	List<Set<Vertex>> bipPart = Utility.bipartitePartition(graph);
    	if(bipPart != null) {
    		List<Vertex> blue = new ArrayList<>(bipPart.get(0));
    		List<Vertex> red = new ArrayList<>(bipPart.get(1));
    		double[][] A = new double[blue.size()][red.size()];
    		for(int i = 0; i < A.length; i++) {
    			for(int j = 0; j < A[i].length; j++) {
    				if(blue.get(i).hasEdgeWith(red.get(j)))
    					A[i][j] = 1;
    			}
    		}
    		double[][] transpose = Utility.transpose(A);
    		double[][] product1 = Utility.multiply(A, transpose);
    		double[][] product2 = Utility.multiply(transpose, A);
    		double[] b1 = powerIteration(product1, err, maxTime / 3);
    		double[] b2 = powerIteration(product2, err, maxTime/ 3);
    		double[] composition = Utility.composeVectors(b1, b2);
    		Utility.normalize(composition);
    		HashMap<String,Double> result = new HashMap<>();
    		for(int i = 0; i < blue.size(); i++)
    			result.put(blue.get(i).getIdentifier(), composition[i]);
    		for(int i = 0; i < red.size(); i++)
    			result.put(red.get(i).getIdentifier(), composition[i+blue.size()]);
    		return result;
    		
    	}else {
			double[] b = powerIteration(graph.getUnweightedAdjacencyMatrix(), err, maxTime);
			HashMap<String,Double> result = new HashMap<>();
			//extract the centrality values from b
			List<Vertex> vertices = graph.getOrderedVertices();
			for(int i = 0; i < vertices.size(); i++)
				result.put(vertices.get(i).getIdentifier(),b[i]);
			return result;
    	}
	}

	/**
	 *
	 * Calculates the eigenvector centralities for this graph with error = 0.00001 and maxTime = 1000
	 *
	 * @param graph a non-empty, connected graph
	 * @return The approximation of the eigenvector centralities for each vertex. The method terminates either
	 * when it takes longer than maxTime or trials iterations have taken place
	 */
	public static Map<String,Double> getEigenvectorCentralities(Graph graph){
		return Centrality.getEigenvectorCentralities(graph, 0.00001,1000);
	}
	
	/**
	 * Performs power iteration on a given matrix, and thus returns an approximation of the dominant eigenvector.
	 *
	 * @param matrix The matrix on which power iteration is performed. preferably non bipartite, as power iteration might diverge then.
	 * @param err If all components of consecutive vectors dont change by err, then stop
	 * @param maxTime the maximum amount of time in milliseconds the method is allowed to run
	 * @return an approximation of the dominant eigenvector
	 */
	 private static double[] powerIteration(double[][] matrix, double err, int maxTime){
			long startTime = System.currentTimeMillis();
			double[] lastB = new double[matrix.length];
			double[][] b = new double[1][matrix.length];
			//initialize b to have strictly positive components
			Arrays.fill(b[0], 1);
			Utility.normalize(b[0]);
			//power iteration
			while(System.currentTimeMillis() - startTime < maxTime) {
				b=Utility.multiply(b, matrix);
				Utility.normalize(b[0]);
				double maxErr = 0;
				for(int j = 0; j < lastB.length; j++) {
					double diff = Math.abs(lastB[j] - b[0][j]);
					if(diff > maxErr)
						maxErr = diff;
				}
				if(maxErr < err)
					break;
				lastB=b[0];
			}		
			return b[0];
		}

	/**
	 * Calculates the betweenness centralities for the given connected graph.
	 *
	 * @param graph a connected, non-empty graph without negative loops
	 * @return a map from vertex identifiers to betweenness centralities
	 * @throws IllegalArgumentException if the graph is disconnected
	 */
	public static Map<String, Double> getBetweennessCentralities(Graph graph)
	{
		if (!graph.isConnected())
		{
			throw new IllegalArgumentException("Betweenness Centrality is undefined for an unconnected graph.");
		}
		double[][] dists = graph.getWeightedAdjacencyMatrix();
		Set<Integer>[][] nexts = new HashSet[dists.length][dists.length]; // store possible next nodes for shortest paths
		for (var row : nexts)
		{
			for (int i = 0; i < row.length; i++)
			{
				row[i] = new HashSet<>();
			}
		}
		for (int i = 0; i < dists.length; i++)
		{
			dists[i][i] = 0;
			for (int j = 0; j < dists.length; j++)
			{
				if (dists[i][j] > 0)
				{
					nexts[i][j].add(j);
				}
			}
		}

		for (int k = 0; k < dists.length; k++) // use Floyd-Warshall algorithm with path reconstruction
		{
			for (int i = 0; i < dists.length; i++)
			{
				for (int j = 0; j < dists.length; j++)
				{
					double d = dists[i][k] + dists[k][j];
					if (d < dists[i][j])
					{
						dists[i][j] = d;
						nexts[i][j] = new HashSet<>(nexts[i][k]);
					}
					else if (d < Double.POSITIVE_INFINITY && d == dists[i][j])
					{
						nexts[i][j].addAll(nexts[i][k]);
					}
				}
			}
		}

		// reconstruct paths and count
		Map<String, Double> result = new HashMap<>();
		List<Vertex> vertices = graph.getOrderedVertices();
		for (Vertex v : graph.getVertices())
		{
			result.put(v.getIdentifier(), 0.0);
		}
		for (int s = 0; s < dists.length; s++)
		{
			for (int t = 0; t < dists.length; t++)
			{
				if (s != t)
				{
					List<List<Integer>> paths = reconstructPaths(nexts, s, t);
					int l = paths.size();
					for (var path : paths)
					{
						for (int i : path)
						{
							if (i != s && i != t)
							{
								result.merge(vertices.get(i).getIdentifier(), (double) 1 / l, Double::sum);
							}
						}
					}
				}

			}
		}
		result.replaceAll((s, c) -> c / ((dists.length - 1)*(dists.length - 2))); // normalize to [0, 1]
		return result;
	}

	/**
	 * Reconstructs the shortest paths from u to v in a graph after the Floyd-Warshall algorithm has been executed.
	 * @param nexts a matrix where the entry (i, j) contains the possible next nodes in the shortest path from i to j
	 * @param u the beginning of the paths
	 * @param v the end of the paths
	 * @return the shortest paths from u to v as lists of vertex indices
	 */
	private static List<List<Integer>> reconstructPaths(Set<Integer>[][] nexts, int u, int v)
	{
		if (nexts[u][v].isEmpty())
		{
			if (u == v)
			{
				return List.of(List.of(u));
			}
			return List.of();
		}
		List<List<Integer>> paths = new LinkedList<>();
		for (int k : nexts[u][v])
		{
			for (var rest : reconstructPaths(nexts, k, v))
			{
				List<Integer> path = new LinkedList<>(rest);
				path.add(0, u);
				paths.add(path);
			}
		}
		return paths;
	}
    
    /**
     * Calculates the harmonic centralities of the vertices in the given graph.
     *
     * @param graph a non-empty graph without negative loops
     * @return a map from vertex identifiers to harmonic centralities
     */
    public static Map<String, Double> getHarmonicCentralities(Graph graph) {
    	HashMap<String, Double> result = new HashMap<>();
    	//special case of graph consisting of 1 vertex is defined as 1
    	if(graph.getVertices().size() == 1) {
    		result.put(graph.getVertices().iterator().next().getIdentifier(),1.0);
    	}else {
	    	//do floyd warshall to calculate shortest paths of all pairs
	        double[][] dists = graph.getWeightedAdjacencyMatrix();
	        floydWarshallInPlace(dists);
	        //sum inverses of shortest paths
	        List<Vertex> vertices = graph.getOrderedVertices();
	        for (int i = 0; i < vertices.size(); i++) {
	        	double total = 0;
	        	for(int j = 0; j < vertices.size(); j++) {
	        		if(vertices.get(j) == vertices.get(i))
	        			continue;
	        		total+= 1/ dists[i][j];
	        	}
	            result.put(vertices.get(i).getIdentifier(), total / (dists.length - 1));
	        }
    	}
        return result;
    }

	/**
	 * Calculates the lengths of the shortest paths between all nodes in a graph using the Floyd-Warshall algorithm.
	 * @param dists the adjacency matrix of a graph
	 */
    private static void floydWarshallInPlace(double[][] dists)
	{
		for (int i = 0; i < dists.length; i++) {
			dists[i][i] = 0;
		}
		for (int k = 0; k < dists.length; k++) // use Floyd-Warshall algorithm to find shortest paths
		{
			for (int i = 0; i < dists.length; i++) {
				for (int j = 0; j < dists.length; j++) {
					if (dists[i][j] > dists[i][k] + dists[k][j]) {
						dists[i][j] = dists[i][k] + dists[k][j];
					}
				}
			}
		}
	}

	/**
	 * Calculates the flow distances for an appropriately weighted graph using a shortest path algorithm.
	 * @param graph a graph with information flow values on the edges
	 * @return a complete graph with the flow distance between two vertices as the edge weight between them
	 */
	public static Graph flowDistance(Graph graph)
	{
		double[][] dists = graph.getWeightedAdjacencyMatrix();
		floydWarshallInPlace(dists); // calculate shortest paths
		Graph flow = new Graph(graph.getDescription() + "-flow");
		for (Vertex v : graph.getVertices())
		{
			flow.addVertex(v.getIdentifier());
		}
		List<Vertex> vs = graph.getOrderedVertices();
		for (int i = 0; i < vs.size(); i++) // generate complete graph with flow distances on edges
		{
			for (int j = 0; j < vs.size(); j++)
			{
				if (i != j)
				{
					flow.setWeight(vs.get(i).getIdentifier(), vs.get(j).getIdentifier(), dists[i][j]);
				}
			}
		}
		return flow;
	}

	/**
	 * Calculates the normalized degree centralities for a given graph.
	 *
	 * @param graph an arbitrary graph
	 * @return a map from vertex identifiers to degree centralities
	 */
	public static Map<String, Double> getDegreeCentralities(Graph graph)
	{
		Map<String, Double> result = new HashMap<>();
		for (Vertex v : graph.getVertices())
		{
			result.put(v.getIdentifier(), (double) v.getEdges().size());
		}
		double max = Collections.max(result.values());
		result.replaceAll((k, v) -> v / max);
		return result;
	}

	/**
	 * Calculates the normalized weighted degree centralities for a given graph.
	 *
	 * @param graph an arbitrary graph
	 * @return a map from vertex identifiers to degree centralities
	 */
	public static Map<String, Double> getWeightedDegreeCentralities(Graph graph)
	{
		Map<String, Double> result = new HashMap<>();
		for (Vertex v : graph.getVertices())
		{
			result.put(v.getIdentifier(), v.getEdges().stream().mapToDouble(Edge::getWeight).map(w -> 1 / w).sum());
		}
		double max = Collections.max(result.values());
		result.replaceAll((k, v) -> v / max);
		return result;
	}
	
}
