package app.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TestEdge {

    private Edge testedge;
    private final Vertex v1 = new Vertex("a");
    private final Vertex v2 = new Vertex("b");

    @BeforeEach
    void setUp() {
        testedge = new Edge(v1, v2, 2);
    }

    @Test
    void getVertices() {
        Vertex[] vs = testedge.getVertices().clone();
        Arrays.sort(vs);
        assertEquals(2, vs.length);
        assertEquals(v1, vs[0]);
        assertEquals(v2, vs[1]);
    }

    @Test
    void oppositeVertex() {
        assertEquals(v1, testedge.oppositeVertex(v2));
        assertEquals(v2, testedge.oppositeVertex(v1));
        assertThrows(IllegalArgumentException.class, () -> testedge.oppositeVertex(new Vertex("c")));
    }

    @Test
    void getWeight() {
        assertEquals(2, testedge.getWeight());
    }

    @Test
    void getWeightWithBool() {
        assertEquals(2, testedge.getWeight(true));
        assertEquals(1, testedge.getWeight(false));
    }

    @Test
    void setWeight() {
        testedge.setWeight(3);
        assertEquals(3, testedge.getWeight());
    }
}