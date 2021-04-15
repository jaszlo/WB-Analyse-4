package app.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestVertex {

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
    void getEdgesByVertex() {
        Map<Vertex, Edge> edges = testgraph.getOrderedVertices().get(0).getEdgesByVertex();
        assertEquals(2, edges.size());
        String[] vs = Arrays.stream(edges.get(testgraph.getVertex("2")).getVertices()).sorted().map(Vertex::getIdentifier).toArray(String[]::new);
        Arrays.sort(vs);
        assertArrayEquals(new String[]{"1", "2"}, vs);

        vs = Arrays.stream(edges.get(testgraph.getVertex("3")).getVertices()).sorted().map(Vertex::getIdentifier).toArray(String[]::new);
        Arrays.sort(vs);
        assertArrayEquals(new String[]{"1", "3"}, vs);
    }

    @Test
    void getIdentifier() {
        Vertex v = new Vertex("a");
        assertEquals("a", v.getIdentifier());
    }

    @Test
    void getEdges() {
        Collection<Edge> edges = testgraph.getOrderedVertices().get(0).getEdges();
        assertEquals(2, edges.size());
        assertArrayEquals(new String[]{"2", "3"}, edges.stream()
                .map(e -> e.oppositeVertex(testgraph.getVertex("1")))
                .map(Vertex::getIdentifier).sorted().toArray(String[]::new));
    }

    @Test
    void hasEdgeWith() {
        assertTrue(testgraph.getVertex("1").hasEdgeWith(testgraph.getVertex("2")));
        assertFalse(testgraph.getVertex("2").hasEdgeWith(testgraph.getVertex("3")));
    }

    @Test
    void getEdgeWith() {
        assertEquals(2, testgraph.getVertex("1").getEdgeWith(testgraph.getVertex("2")).getWeight());
    }

    @Test
    void compareTo() {
        Vertex a = new Vertex("a");
        Vertex b = new Vertex("b");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }
}