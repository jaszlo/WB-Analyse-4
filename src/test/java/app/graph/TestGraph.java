package app.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestGraph {

    private Graph testgraph;

    @BeforeEach
    void setUp() {
        testgraph = new Graph("testgraph");
        testgraph.addVertex("1");
        testgraph.addVertex("3");
        testgraph.addVertex("2");
        testgraph.addEdge("1", "2", 2);
        testgraph.addEdge("1", "3", 1);
    }

    @Test
    void addVertex() {
        assertNull(testgraph.getVertex("4"));
        testgraph.addVertex("4");
        assertNotNull(testgraph.getVertex("4"));
    }

    @Test
    void getVertex() {
        assertEquals("1", testgraph.getVertex("1").getIdentifier());
        assertNull(testgraph.getVertex("4"));
    }

    @Test
    void setWeight() {
        assertEquals(2, testgraph.getEdge("1", "2").getWeight());
        testgraph.setWeight("1", "2", 3);
        assertEquals(3, testgraph.getEdge("1", "2").getWeight());

        assertNull(testgraph.getEdge("2", "3"));
        testgraph.setWeight("2", "3", 3);
        assertEquals(3, testgraph.getEdge("3", "2").getWeight());
    }

    @Test
    void removeEdge() {
        assertNotNull(testgraph.getEdge("1", "2"));
        testgraph.removeEdge("1", "2");
        assertNull(testgraph.getEdge("1", "2"));
    }

    @Test
    void getWeightedAdjacencyMatrix() {
        final double INF = Double.POSITIVE_INFINITY;
        assertArrayEquals(new double[][] {{INF, 2, 1}, {2, INF, INF}, {1, INF, INF}}, testgraph.getWeightedAdjacencyMatrix());
    }

    @Test
    void getUnweightedAdjacencyMatrix() {
        assertArrayEquals(new double[][] {{0, 1, 1}, {1, 0, 0}, {1, 0, 0}}, testgraph.getUnweightedAdjacencyMatrix());
    }

    @Test
    void getDescription() {
        assertEquals("testgraph", testgraph.getDescription());
    }

    @Test
    void getVertices() {
        Collection<Vertex> vertices = testgraph.getVertices();
        assertEquals(3, vertices.size());
        assertTrue(vertices.contains(testgraph.getVertex("1")));
        assertTrue(vertices.contains(testgraph.getVertex("2")));
        assertTrue(vertices.contains(testgraph.getVertex("3")));
        assertFalse(vertices.contains(testgraph.getVertex("4")));

        testgraph.removeVertex("1");
        assertEquals(2, vertices.size());
    }

    @Test
    void getOrderedVertices() {
        List<Vertex> vertices = testgraph.getOrderedVertices();
        assertEquals(testgraph.getVertex("1"), vertices.get(0));
        assertEquals(testgraph.getVertex("2"), vertices.get(1));
        assertEquals(testgraph.getVertex("3"), vertices.get(2));

        testgraph.removeVertex("1");
        assertEquals(2, vertices.size());
    }

    @Test
    void removeVertexByIdentifier() {
        assertNotNull(testgraph.getVertex("1"));
        testgraph.removeVertex("1");
        assertNull(testgraph.getVertex("1"));
    }

    @Test
    void removeVertexByObject() {
        Vertex v = testgraph.getVertex("1");
        assertNotNull(v);
        assertEquals("1", v.getIdentifier());
        testgraph.removeVertex(v);
        assertNull(testgraph.getVertex("1"));
    }

    @Test
    void addEdgeByVerticesAndWeight() {
        assertEquals(3, testgraph.addEdge(testgraph.getVertex("2"),
                testgraph.getVertex("3"), 3).getWeight());
        assertThrows(IllegalArgumentException.class, () -> testgraph.addEdge(testgraph.getVertex("2"),
                testgraph.getVertex("3"), 4));
    }

    @Test
    void addEdgeByVertices() {
        assertEquals(1, testgraph.addEdge(testgraph.getVertex("2"),
                testgraph.getVertex("3")).getWeight());
        assertThrows(IllegalArgumentException.class, () -> testgraph.addEdge(testgraph.getVertex("2"),
                testgraph.getVertex("3")));
    }

    @Test
    void addEdgeByIdentifiersAndWeight() {
        assertEquals(3, testgraph.addEdge("2", "3", 3).getWeight());
        assertThrows(IllegalArgumentException.class, () -> testgraph.addEdge("2", "3", 3));
    }

    @Test
    void addEdgeByIdentifiers() {
        assertEquals(1, testgraph.addEdge("2", "3").getWeight());
        assertThrows(IllegalArgumentException.class, () -> testgraph.addEdge("2", "3"));
    }

    @Test
    void getEdgeByIdentifiers() {
        Edge e = testgraph.getEdge("1", "2");
        assertNotNull(e);
        assertEquals(2, e.getWeight());

        assertNull(testgraph.getEdge("2", "3"));

        assertEquals(testgraph.getEdge("1", "2"), testgraph.getEdge("2", "1"));
    }

    @Test
    void getEdgeByVertices() {
        Vertex v1 = testgraph.getVertex("1");
        Vertex v2 = testgraph.getVertex("2");
        Vertex v3 = testgraph.getVertex("3");

        Edge e = testgraph.getEdge(v1, v2);
        assertNotNull(e);
        assertEquals(2, e.getWeight());

        assertNull(testgraph.getEdge(v2, v3));
    }

    @Test
    void getEdges() {
        Collection<Edge> edges = testgraph.getEdges();
        assertEquals(2, edges.size());
        assertTrue(edges.contains(testgraph.getEdge("1", "2")));
        assertTrue(edges.contains(testgraph.getEdge("1", "3")));
    }

    @Test
    void testToString() {
        assertLinesMatch(List.of("testgraph:", "1: [2, 3]", "2: [1]", "3: [1]"), Arrays.asList(testgraph.toString().split("\n")));
    }

    @Test
    void testEquals() {
        Graph g = testgraph;
        setUp();
        assertEquals(g, testgraph);
        assertNotSame(g, testgraph);
    }

    @Test
    void invertEdgeWeights() {
        testgraph.invertEdgeWeights();
        assertEquals(0.5, testgraph.getEdge("1", "2").getWeight(), 0.001);
        assertEquals(1, testgraph.getEdge("1", "3").getWeight(), 0.001);
    }

    @Test
    void normalizeEdgeWeights() {
        testgraph.normalizeEdgeWeights();
        assertEquals(1, testgraph.getEdge("1", "2").getWeight(), 0.001);
        assertEquals(0.5, testgraph.getEdge("1", "3").getWeight(), 0.001);
    }

}