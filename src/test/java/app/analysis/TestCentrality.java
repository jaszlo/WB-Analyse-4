package app.analysis;

import app.graph.Graph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestCentrality {

	static Graph getTestGraph(String name) {
		return Graph.readGraph(TestCentrality.class.getResourceAsStream("/testgraphen/" + name + ".graph"));
	}

	@Test
	void getHarmonicCentralities() {
		double err = 0.001;
		// test functionality for an unconnected graph
		Graph graph = getTestGraph("unconnected");
		Map<String, Double> values = Centrality.getHarmonicCentralities(graph);
		assertEquals(0.6666, values.get("a"), err);
		assertEquals(0.6666, values.get("b"), err);
		assertEquals(0.6666, values.get("c"), err);
		assertEquals(0, values.get("d"), err);
		// complete graph with 3 vertices
		graph = getTestGraph("K_3");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(1, values.get("a"), err);
		assertEquals(1, values.get("b"), err);
		assertEquals(1, values.get("c"), err);
		// complete graph with 4 vertices
		graph = getTestGraph("K_4");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(1, values.get("a"), err);
		assertEquals(1, values.get("b"), err);
		assertEquals(1, values.get("c"), err);
		assertEquals(1, values.get("d"), err);
		// star like graph with 6 vertices
		graph = getTestGraph("star");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(1, values.get("a"), err);
		assertEquals(0.6, values.get("b"), err);
		assertEquals(0.6, values.get("c"), err);
		assertEquals(0.6, values.get("d"), err);
		assertEquals(0.6, values.get("e"), err);
		assertEquals(0.6, values.get("f"), err);
		// the complete binary tree with 7 vertices
		graph = getTestGraph("bin_tree");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(0.666, values.get("a"), err);
		assertEquals(0.694, values.get("b"), err);
		assertEquals(0.694, values.get("c"), err);
		assertEquals(0.472, values.get("d"), err);
		assertEquals(0.472, values.get("e"), err);
		assertEquals(0.472, values.get("f"), err);
		assertEquals(0.472, values.get("g"), err);
		// a graph that is a single cycle with 4 vertices
		graph = getTestGraph("loop");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(0.833, values.get("a"), err);
		assertEquals(0.833, values.get("b"), err);
		assertEquals(0.833, values.get("c"), err);
		assertEquals(0.833, values.get("d"), err);
		// a graph that is a single cycle with 4 vertices
		graph = getTestGraph("loop");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(0.833, values.get("a"), err);
		assertEquals(0.833, values.get("b"), err);
		assertEquals(0.833, values.get("c"), err);
		assertEquals(0.833, values.get("d"), err);
		// test special case when graph is just 1 vertex
		graph = getTestGraph("one_vertex");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(1, values.get("a"), err);
		// test non trivial 5 vertex graph
		graph = getTestGraph("random1");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(0.708, values.get("a"), err);
		assertEquals(0.708, values.get("b"), err);
		assertEquals(0.875, values.get("c"), err);
		assertEquals(0.750, values.get("d"), err);
		assertEquals(0.541, values.get("e"), err);
		// test spezifikationsgraph
		graph = getTestGraph("spezifikationsgraph");
		values = Centrality.getHarmonicCentralities(graph);
		assertEquals(1.7750, values.get("a1"), err);
		assertEquals(2.4083, values.get("a2"), err);
		assertEquals(1.3499, values.get("b1"), err);
		assertEquals(1.8381, values.get("b2"), err);
		assertEquals(1.6833, values.get("c1"), err);
		assertEquals(1.8381, values.get("c2"), err);
		assertEquals(1.2857, values.get("d1"), err);
		assertEquals(2.0881, values.get("d2"), err);
		assertEquals(2.0881, values.get("e2"), err);
	}

	@Test
	void getEigenvectorCentralities() {
		double err = 0.001;
		int time = 1000; // ms
		// Abnahmetestfall
		Graph graph = getTestGraph("spezifikationsgraph");
		Map<String, Double> values = Centrality.getEigenvectorCentralities(graph, err, time);
		assertEquals(0.137098, values.get("a1"), err);
		assertEquals(0.463508, values.get("a2"), err);
		assertEquals(0.0455334, values.get("b1"), err);
		assertEquals(0.436423, values.get("b2"), err);
		assertEquals(0.0478608, values.get("c1"), err);
		assertEquals(0.436423, values.get("c2"), err);
		assertEquals(0.0117825, values.get("d1"), err);
		assertEquals(0.436423, values.get("d2"), err);
		assertEquals(0.436423, values.get("e2"), err);
		// Vollstädinger Graph mit 4 Knoten
		graph = getTestGraph("K_4");
		values = Centrality.getEigenvectorCentralities(graph, err, time);
		assertEquals(0.5, values.get("a"), err);
		assertEquals(0.5, values.get("b"), err);
		assertEquals(0.5, values.get("c"), err);
		assertEquals(0.5, values.get("d"), err);
		// listengraph mit 5 Knoten
		graph = getTestGraph("random1");
		values = Centrality.getEigenvectorCentralities(graph, err, time);
		assertEquals(0.49715, values.get("a"), err);
		assertEquals(0.49715, values.get("b"), err);
		assertEquals(0.60370, values.get("c"), err);
		assertEquals(0.342485, values.get("d"), err);
		assertEquals(0.154668, values.get("e"), err);
		// a cycle of 4 vertices
		graph = getTestGraph("loop");
		values = Centrality.getEigenvectorCentralities(graph);
		assertEquals(0.5, values.get("a"), err);
		assertEquals(0.5, values.get("b"), err);
		assertEquals(0.5, values.get("c"), err);
		assertEquals(0.5, values.get("d"), err);
		// complete binary tree with 7 vertices
		graph = getTestGraph("bin_tree");
		values = Centrality.getEigenvectorCentralities(graph);
		assertEquals(0.5, values.get("a"), err);
		assertEquals(0.5, values.get("b"), err);
		assertEquals(0.5, values.get("c"), err);
		assertEquals(0.25, values.get("d"), err);
		assertEquals(0.25, values.get("e"), err);
		assertEquals(0.25, values.get("f"), err);
		assertEquals(0.25, values.get("g"), err);
		// star graph
		graph = getTestGraph("star");
		values = Centrality.getEigenvectorCentralities(graph);
		assertEquals(0.7071, values.get("a"), err);
		assertEquals(0.3162, values.get("b"), err);
		assertEquals(0.3162, values.get("c"), err);
		assertEquals(0.3162, values.get("d"), err);
		assertEquals(0.3162, values.get("e"), err);
		assertEquals(0.3162, values.get("f"), err);
		// lin list graph
		graph = getTestGraph("linlist");
		values = Centrality.getEigenvectorCentralities(graph);
		assertEquals(0.2886, values.get("a"), err);
		assertEquals(0.5, values.get("b"), err);
		assertEquals(0.57735, values.get("c"), err);
		assertEquals(0.5, values.get("d"), err);
		assertEquals(0.2886, values.get("e"), err);
		// complete bipartite 3x3 graph
		graph = getTestGraph("complete_bipartite_3x3");
		values = Centrality.getEigenvectorCentralities(graph);
		assertEquals(0.408, values.get("a"), err);
		assertEquals(0.408, values.get("b"), err);
		assertEquals(0.408, values.get("c"), err);
		assertEquals(0.408, values.get("d"), err);
		assertEquals(0.408, values.get("e"), err);
		assertEquals(0.408, values.get("f"), err);
	}

	@Test
	void getClosenessCentralities() {
		Graph graph1 = new Graph();
		graph1.addVertex("A");
		graph1.addVertex("B");
		graph1.addVertex("C");
		graph1.addVertex("D");
		graph1.addVertex("E");
		graph1.setWeight("A", "C", 1);
		graph1.setWeight("A", "D", 1);
		graph1.setWeight("B", "C", 2);
		graph1.setWeight("C", "E", 3);
		graph1.setWeight("D", "E", 2);

		Map<String, Double> cent = Centrality.getClosenessCentralities(graph1);
		assertEquals(0.5, cent.get("A"), 0.001);
		assertEquals((double) 2 / 7, cent.get("B"), 0.001);
		assertEquals(0.5, cent.get("C"), 0.001);
		assertEquals((double) 4 / 9, cent.get("D"), 0.001);
		assertEquals((double) 4 / 13, cent.get("E"), 0.001);

		graph1.removeEdge("B", "C");
		assertThrows(IllegalArgumentException.class, () -> Centrality.getClosenessCentralities(graph1));

		double err = 0.001;
		// abnahmetestfall
		Graph graph = getTestGraph("spezifikationsgraph");
		graph.invertEdgeWeights();
		graph.normalizeEdgeWeights();
		Map<String, Double> values = Centrality.getClosenessCentralities(graph);
		assertEquals(0.86486, values.get("a1"), err);
		assertEquals(0.6857, values.get("b2"), err);
		assertEquals(0.8888, values.get("a2"), err);
		assertEquals(0.4085, values.get("d1"), err);
		assertEquals(0.6575, values.get("e2"), err);
		assertEquals(0.63576, values.get("c1"), err);
		assertEquals(0.6575, values.get("d2"), err);
		assertEquals(0.61935, values.get("b1"), err);
		assertEquals(0.6857, values.get("c2"), err);
		// binary tree
		graph = getTestGraph("bin_tree");
		values = Centrality.getClosenessCentralities(graph);
		assertEquals(0.6, values.get("a"), err);
		assertEquals(0.5454545454545454, values.get("b"), err);
		assertEquals(0.5454545454545454, values.get("c"), err);
		assertEquals(0.375, values.get("d"), err);
		assertEquals(0.375, values.get("e"), err);
		assertEquals(0.375, values.get("f"), err);
		assertEquals(0.375, values.get("g"), err);
		// complete bipartite 3x3
		graph = getTestGraph("complete_bipartite_3x3");
		values = Centrality.getClosenessCentralities(graph);
		assertEquals(0.71428, values.get("a"), err);
		assertEquals(0.71428, values.get("b"), err);
		assertEquals(0.71428, values.get("c"), err);
		assertEquals(0.71428, values.get("d"), err);
		assertEquals(0.71428, values.get("e"), err);
		assertEquals(0.71428, values.get("f"), err);
		// K_4
		graph = getTestGraph("K_4");
		values = Centrality.getClosenessCentralities(graph);
		assertEquals(1.0, values.get("a"), err);
		assertEquals(1.0, values.get("b"), err);
		assertEquals(1.0, values.get("c"), err);
		assertEquals(1.0, values.get("d"), err);
		// linlsit
		graph = getTestGraph("linlist");
		values = Centrality.getClosenessCentralities(graph);
		assertEquals(0.4, values.get("a"), err);
		assertEquals(0.57142, values.get("b"), err);
		assertEquals(0.66666, values.get("c"), err);
		assertEquals(0.57142, values.get("d"), err);
		assertEquals(0.4, values.get("e"), err);
		// loop
		graph = getTestGraph("loop");
		values = Centrality.getClosenessCentralities(graph);
		assertEquals(0.75, values.get("a"), err);
		assertEquals(0.75, values.get("b"), err);
		assertEquals(0.75, values.get("c"), err);
		assertEquals(0.75, values.get("d"), err);
		// random graph
		graph = getTestGraph("random1");
		values = Centrality.getClosenessCentralities(graph);
		assertEquals(0.5714285714285714, values.get("a"), err);
		assertEquals(0.5714285714285714, values.get("b"), err);
		assertEquals(0.8, values.get("c"), err);
		assertEquals(0.666666, values.get("d"), err);
		assertEquals(0.444444, values.get("e"), err);
		// star graph
		graph = getTestGraph("star");
		values = Centrality.getClosenessCentralities(graph);
		assertEquals(1.0, values.get("a"), err);
		assertEquals(0.55555, values.get("b"), err);
		assertEquals(0.55555, values.get("c"), err);
		assertEquals(0.55555, values.get("d"), err);
		assertEquals(0.55555, values.get("e"), err);
		assertEquals(0.55555, values.get("f"), err);
	}

	@Test
	void getBetweennessCentralities() {
		Graph g1 = new Graph();
		g1.addVertex("A");
		g1.addVertex("B");
		g1.addVertex("C");
		g1.addVertex("D");
		g1.addVertex("E");
		g1.setWeight("A", "C", 1);
		g1.setWeight("A", "D", 1);
		g1.setWeight("B", "C", 2);
		g1.setWeight("C", "E", 3);
		g1.setWeight("D", "E", 2);

		Map<String, Double> cent = Centrality.getBetweennessCentralities(g1);
		assertEquals((double) 1 / 3, cent.get("A"), 0.001);
		assertEquals(0.0, cent.get("B"), 0.001);
		assertEquals(0.5, cent.get("C"), 0.001);
		assertEquals((double) 1 / 6, cent.get("D"), 0.001);
		assertEquals(0.0, cent.get("E"), 0.001);

		g1.removeEdge("B", "C");
		assertThrows(IllegalArgumentException.class, () -> Centrality.getBetweennessCentralities(g1));

		double err = 0.001;
		// abnahmetestfall
		Graph graph = getTestGraph("spezifikationsgraph");
		graph.invertEdgeWeights();
		graph.normalizeEdgeWeights();
		Map<String, Double> values = Centrality.getBetweennessCentralities(graph);
		assertEquals(0.53571, values.get("a1"), err);
		assertEquals(0.03571, values.get("b2"), err);
		assertEquals(0.57142, values.get("a2"), err);
		assertEquals(0.0, values.get("d1"), err);
		assertEquals(0.0, values.get("e2"), err);
		assertEquals(0.25, values.get("c1"), err);
		assertEquals(0.0, values.get("d2"), err);
		assertEquals(0.0, values.get("b1"), err);
		assertEquals(0.035714, values.get("c2"), err);
		// complete bipartite
		graph = getTestGraph("complete_bipartite_3x3");
		values = Centrality.getBetweennessCentralities(graph);
		assertEquals(0.099999, values.get("a"), err);
		assertEquals(0.099999, values.get("b"), err);
		assertEquals(0.099999, values.get("c"), err);
		assertEquals(0.099999, values.get("d"), err);
		assertEquals(0.099999, values.get("e"), err);
		assertEquals(0.099999, values.get("f"), err);
		// complete graph with 4 vertices
		graph = getTestGraph("K_4");
		values = Centrality.getBetweennessCentralities(graph);
		assertEquals(0.0, values.get("a"), err);
		assertEquals(0.0, values.get("b"), err);
		assertEquals(0.0, values.get("c"), err);
		assertEquals(0.0, values.get("d"), err);
		// linear list graph
		graph = getTestGraph("linlist");
		values = Centrality.getBetweennessCentralities(graph);
		assertEquals(0.0, values.get("a"), err);
		assertEquals(0.5, values.get("b"), err);
		assertEquals(0.66666, values.get("c"), err);
		assertEquals(0.5, values.get("d"), err);
		assertEquals(0.0, values.get("e"), err);
		// loop graph
		graph = getTestGraph("loop");
		values = Centrality.getBetweennessCentralities(graph);
		assertEquals(0.166666, values.get("a"), err);
		assertEquals(0.166666, values.get("b"), err);
		assertEquals(0.166666, values.get("c"), err);
		assertEquals(0.166666, values.get("d"), err);
		// random graph
		graph = getTestGraph("random1");
		values = Centrality.getBetweennessCentralities(graph);
		assertEquals(0.0, values.get("a"), err);
		assertEquals(0.0, values.get("b"), err);
		assertEquals(0.666666, values.get("c"), err);
		assertEquals(0.5, values.get("d"), err);
		assertEquals(0.0, values.get("e"), err);
		// star graph
		graph = getTestGraph("star");
		values = Centrality.getBetweennessCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(0.0,values.get("b"),err);
		assertEquals(0.0,values.get("c"),err);
		assertEquals(0.0,values.get("d"),err);
		assertEquals(0.0,values.get("e"),err);
		assertEquals(0.0,values.get("f"),err);
	}

	@Test
	void getDegreeCentralities() {
		double err = 0.001;

		Graph graph = getTestGraph("spezifikationsgraph");
		Map<String, Double> values = Centrality.getDegreeCentralities(graph);
		assertEquals(0.6,values.get("a1"),err);
		assertEquals(0.8,values.get("b2"),err);
		assertEquals(1.0,values.get("a2"),err);
		assertEquals(0.2,values.get("d1"),err);
		assertEquals(0.8,values.get("e2"),err);
		assertEquals(0.6,values.get("c1"),err);
		assertEquals(0.8,values.get("d2"),err);
		assertEquals(0.4,values.get("b1"),err);
		assertEquals(0.8,values.get("c2"),err);

		graph = getTestGraph("complete_bipartite_3x3");
		values = Centrality.getDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);
		assertEquals(1.0,values.get("e"),err);
		assertEquals(1.0,values.get("f"),err);
		
		graph = getTestGraph("K_4");
		values = Centrality.getDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);

		graph = getTestGraph("linlist");
		values = Centrality.getDegreeCentralities(graph);
		assertEquals(0.5,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);
		assertEquals(0.5,values.get("e"),err);

		graph = getTestGraph("loop");
		values = Centrality.getDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);

		graph = getTestGraph("random1");
		values = Centrality.getDegreeCentralities(graph);
		assertEquals(0.6666,values.get("a"),err);
		assertEquals(0.6666,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(0.6666,values.get("d"),err);
		assertEquals(0.3333333333333333,values.get("e"),err);

		graph = getTestGraph("star");
		values = Centrality.getDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(0.2,values.get("b"),err);
		assertEquals(0.2,values.get("c"),err);
		assertEquals(0.2,values.get("d"),err);
		assertEquals(0.2,values.get("e"),err);
		assertEquals(0.2,values.get("f"),err);

		graph = getTestGraph("bin_tree");
		values = Centrality.getDegreeCentralities(graph);
		assertEquals(0.6666,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(0.3333,values.get("d"),err);
		assertEquals(0.3333,values.get("e"),err);
		assertEquals(0.3333,values.get("f"),err);
		assertEquals(0.3333,values.get("g"),err);
	}

	@Test
	void getWeightedDegreeCentralities() {
		double err = 0.001;
		//
		Graph graph = getTestGraph("bin_tree");
		Map<String, Double> values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(0.66666,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(0.33333,values.get("d"),err);
		assertEquals(0.33333,values.get("e"),err);
		assertEquals(0.33333,values.get("f"),err);
		assertEquals(0.33333,values.get("g"),err);

		graph = getTestGraph("complete_bipartite_3x3");
		values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);
		assertEquals(1.0,values.get("e"),err);
		assertEquals(1.0,values.get("f"),err);

		graph = getTestGraph("K_4");
		values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);
		
		graph = getTestGraph("linlist");
		values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(0.5,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);
		assertEquals(0.5,values.get("e"),err);

		graph = getTestGraph("loop");
		values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(1.0,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(1.0,values.get("d"),err);

		graph = getTestGraph("random1");
		values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(0.6666,values.get("a"),err);
		assertEquals(0.6666,values.get("b"),err);
		assertEquals(1.0,values.get("c"),err);
		assertEquals(0.6666,values.get("d"),err);
		assertEquals(0.3333,values.get("e"),err);

		graph = getTestGraph("spezifikationsgraph");
		values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(0.52941,values.get("a1"),err);
		assertEquals(0.66666,values.get("b2"),err);
		assertEquals(1.0,values.get("a2"),err);
		assertEquals(0.23529,values.get("d1"),err);
		assertEquals(0.78431,values.get("e2"),err);
		assertEquals(0.54901,values.get("c1"),err);
		assertEquals(0.78431,values.get("d2"),err);
		assertEquals(0.31372,values.get("b1"),err);
		assertEquals(0.66666,values.get("c2"),err);

		graph = getTestGraph("star");
		values = Centrality.getWeightedDegreeCentralities(graph);
		assertEquals(1.0,values.get("a"),err);
		assertEquals(0.2,values.get("b"),err);
		assertEquals(0.2,values.get("c"),err);
		assertEquals(0.2,values.get("d"),err);
		assertEquals(0.2,values.get("e"),err);
		assertEquals(0.2,values.get("f"),err);
	}
}