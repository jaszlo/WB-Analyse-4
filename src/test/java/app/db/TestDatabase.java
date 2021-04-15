package app.db;

import app.Main;
import app.data.InteractionRequest;
import app.graph.Graph;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static app.analysis.Centrality.flowDistance;
import static org.junit.jupiter.api.Assertions.*;

public class TestDatabase {

    private final String[] testNames1 = {"T0"}; //interactionCount == 0?
    private final String[] testNames2 = {"T1", "T2"}; //expected interactionCount == 1
    private final String[] testNames3 = {"T3", "T4", "T5"}; //expected interactionCount == 3
    private final String[] testNames4 = {"1", "2"};
    private final String[] testNames5 = {"1", "3"};
    private final String[] testNames6 = {"1", "2", "3"};
    private final String[] testNames7 = {"1", "2", "3", "4"};
    private final String[] testNames8 = {"1", "https://www.mysqltutorial.org/mysql-case-function/"};
    private final String[] testNames9 = {"1","1","1"};
    private final String[] testNames10 = {"1","2","3", "https://www.mysqltutorial.org/mysql-case-function/"};


    private final InteractionRequest testData1 = new InteractionRequest(testNames1, 202011301, 15, false);
    private final InteractionRequest testData2 = new InteractionRequest(testNames2, 202011302, 30, false);
    private final InteractionRequest testData3 = new InteractionRequest(testNames3, 202011303, 45, false);
    private final InteractionRequest testData4 = new InteractionRequest(testNames4, 0, 10, false);
    private final InteractionRequest testData5 = new InteractionRequest(testNames4, 0, 20, false);
    private final InteractionRequest testData6 = new InteractionRequest(testNames5, 0, 30, false);
    //private final InteractionRequest testData7 = new InteractionRequest(testNames5, 0, 30, false);
    private final InteractionRequest testData8 = new InteractionRequest(testNames6, 0, 0, false);
    private final InteractionRequest testData9 = new InteractionRequest(testNames7, 0, 0, false);
    private final InteractionRequest testData10 = new InteractionRequest(testNames8, 0, 0, true);
    //private final InteractionRequest testData11 = new InteractionRequest(testNames6, 0, 0, true);
    private final InteractionRequest testData12 = new InteractionRequest(testNames1, 0, 0, true);
    private final InteractionRequest testData13 = new InteractionRequest(testNames9, 0, 0, false);
    private final InteractionRequest testData14 = new InteractionRequest(testNames10, 0, 0, true);


    private Database db = null;

    @BeforeEach
    public void clear() {
        db = new Database(Main.properties);
        if (!db.initTest()) fail();
    }

    @AfterEach
    public void deInit() {
        if (!this.db.deInitTest()) fail();
    }

    @Test
    public void addInteractionsWithMeeting() {
        //missing data interaction data
        boolean containsInput;
        int personCounter = this.db.addInteractions(testData1);
        containsInput = this.db.contains( "T0", "T0", 202011301, 15, 1);
        assertFalse(containsInput);
        assertEquals(0, personCounter);
        this.db.clear();

        //two distinct names
        personCounter = this.db.addInteractions(testData2);
        containsInput = this.db.contains("T1", "T2", 202011302, 30, 2);
        assertTrue(containsInput);
        assertEquals(2, personCounter);
        this.db.clear();

        //more than two name
        personCounter = this.db.addInteractions(testData3);
        containsInput = this.db.contains("T3", "T4", 202011303, 45, 3);
        assertTrue(containsInput);
        containsInput = this.db.contains("T3", "T5", 202011303, 45, 3);
        assertTrue(containsInput);
        containsInput = this.db.contains("T4", "T5", 202011303, 45, 3);
        assertTrue(containsInput);
        assertEquals(3, personCounter);
        this.db.clear();

        //document with more than 2 subjects
        personCounter = this.db.addInteractions(testData14);
        containsInput = this.db.contains("1", "https://www.mysqltutorial.org/mysql-case-function/", 0, 0, 0);
        assertTrue(containsInput);
        containsInput = this.db.contains("2", "https://www.mysqltutorial.org/mysql-case-function/", 0, 0, 0);
        assertTrue(containsInput);
        containsInput = this.db.contains("3", "https://www.mysqltutorial.org/mysql-case-function/", 0, 0, 0);
        assertTrue(containsInput);
        assertEquals(3, personCounter);
        this.db.clear();

        //document with exactly one subject
        personCounter = this.db.addInteractions(testData12);
        containsInput = this.db.contains("T0","T0",0,0,0); //distinctPersons always 0 for documents
        assertFalse(containsInput);
        assertEquals(0, personCounter);
        this.db.clear();

        //multiple same names in InteractionRequest.names
        personCounter = this.db.addInteractions(new InteractionRequest(new String[]{"A","A","B"}, 202011302, 30 ,false));
        containsInput = this.db.contains("A", "B", 202011302, 30, 2);
        assertTrue(containsInput);
        assertEquals(2, personCounter);
        this.db.clear();

        //just one distinct name in InteractionRequest.names
        personCounter = this.db.addInteractions(testData13);
        containsInput = this.db.contains("1", "1", 0, 0, 1);
        assertFalse(containsInput);
        assertEquals(0, personCounter);
        this.db.clear();
    }

    @Test
    public void getAllIds() {
        this.db.addInteractions(testData2);
        this.db.addInteractions(testData3);
        List<String> actual = this.db.getAllIds();
        List<String> expected = List.of("T1", "T2", "T3", "T4", "T5");
        assertEquals(actual, expected);
    }

    @Test
    public void generateGraph() {
        //empty table
        Graph actual = this.db.generateGraph(GraphOptions.INTERACTION_SUM);
        Graph expected = new Graph();
        assertEquals(expected, actual);
        this.db.clear();

        //check INTERACTION_SUM
        expected = new Graph();
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addVertex("3");
        expected.addEdge("1", "2", 2);
        expected.addEdge("1", "3", 1);
        this.db.addInteractions(testData4);
        this.db.addInteractions(testData5);
        this.db.addInteractions(testData6);
        actual = this.db.generateGraph(GraphOptions.INTERACTION_SUM);
        expected.normalizeEdgeWeights();
        assertEquals(expected, actual);
        this.db.clear();

        //check INVERTED_INTERACTION_SUM
        expected = new Graph();
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addVertex("3");
        expected.addEdge("1", "2", 1.0 / 2);
        //edge weight: infinite decimal
        expected.addEdge("1", "3", 1.0 / 3);
        this.db.addInteractions(testData5);
        this.db.addInteractions(testData5);
        this.db.addInteractions(testData6);
        this.db.addInteractions(testData6);
        this.db.addInteractions(testData6);
        actual = this.db.generateGraph(GraphOptions.INVERTED_INTERACTION_SUM);
        expected.normalizeEdgeWeights();
        assertEquals(expected, actual);
        this.db.clear();

        //check mode INTERACTION_TIMES_DURATION
        expected = new Graph();
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addVertex("3");
        //edge weight = number of interactions * sum of duration
        expected.addEdge("1", "2", 60); // 2 * (20 + 10)
        expected.addEdge("1", "3", 30); // 1 * (30)
        this.db.addInteractions(testData4);
        this.db.addInteractions(testData5);
        this.db.addInteractions(testData6);
        actual = this.db.generateGraph(GraphOptions.INTERACTION_TIMES_DURATION);
        expected.normalizeEdgeWeights();
        assertEquals(expected, actual);
        this.db.clear();

        //check mode FLOW_DISTANCE
        expected = new Graph();
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addEdge("1", "2", 1);
        expected.addVertex("3");
        expected.addEdge("1", "3", 1.2);
        expected.addEdge("2", "3", 1.2);
        expected.addVertex("https://www.mysqltutorial.org/mysql-case-function/");
        expected.addEdge("1", "https://www.mysqltutorial.org/mysql-case-function/", 1.7);
        this.db.addInteractions(testData4);
        this.db.addInteractions(testData8);
        this.db.addInteractions(testData10);
        actual = this.db.generateGraph(GraphOptions.FLOW_DISTANCE);
        assertEquals(flowDistance(expected), actual);
        this.db.clear();

        //check filter NAME
        expected = new Graph();
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addVertex("3");
        expected.addEdge("1", "2");
        expected.addEdge("1", "3");
        this.db.addInteractions(testData8);
        SortedMap<String, String> test1 = new TreeMap<>(); //TreeMap is a SortedMap
        test1.put("NAME", "1");
        actual = this.db.generateGraph(GraphOptions.INTERACTION_SUM, test1);
        expected.normalizeEdgeWeights();
        assertEquals(expected, actual);
        this.db.clear();

        //check filter INVERTED_NAME
        expected = new Graph();
        expected.addVertex("2");
        expected.addVertex("3");
        expected.addEdge("2", "3");
        this.db.addInteractions(testData8);
        SortedMap<String, String> test2 = new TreeMap<>();
        test2.put("NOT_NAME", "1");
        actual = this.db.generateGraph(GraphOptions.INTERACTION_SUM, test2);
        expected.normalizeEdgeWeights();
        assertEquals(expected, actual);
        this.db.clear();

        //check filter NAME together with filter NO_NAME
        expected = new Graph();
        expected.addVertex("2");
        expected.addVertex("3");
        expected.addVertex("4");
        expected.addEdge("2", "3");
        expected.addEdge("2", "4");
        this.db.addInteractions(testData9);
        SortedMap<String, String> test3 = new TreeMap<>();
        test3.put("NOT_NAME", "1"); //1 shouldn't be a vertex
        test3.put("NAME", "2"); //edge 3 to 4 shouldn't be there
        actual = this.db.generateGraph(GraphOptions.INTERACTION_SUM, test3);
        expected.normalizeEdgeWeights();
        assertEquals(expected, actual);
    }

    @Test
    public void generateFlowGraph() {
        SortedMap<String, String> sortedEmptyMap = Collections.emptySortedMap();
        //talk
        Graph expected = new Graph();
        expected.addVertex("1");
        expected.addVertex("2");
        expected.addEdge("1", "2", 1);
        this.db.addInteractions(testData4);
        Graph expectedFlow1 = flowDistance(expected);
        Graph actual = this.db.generateFlowGraph(sortedEmptyMap);
        assertEquals(actual, expectedFlow1);

        //meeting
        expected.addVertex("3");
        expected.addEdge("1", "3", 1.2);
        expected.addEdge("2", "3", 1.2);
        this.db.addInteractions(testData8);
        Graph expectedFlow2 = flowDistance(expected);
        actual = this.db.generateFlowGraph(sortedEmptyMap);
        assertEquals(actual, expectedFlow2);

        //document
        expected.addVertex("https://www.mysqltutorial.org/mysql-case-function/");
        expected.addEdge("1", "https://www.mysqltutorial.org/mysql-case-function/", 1.7);
        this.db.addInteractions(testData10);
        Graph expectedFlow3 = flowDistance(expected);
        actual = this.db.generateFlowGraph(sortedEmptyMap);
        assertEquals(actual, expectedFlow3);
    }

    @Test
    public void removePersons() {
        //non existing interaction
        assertEquals(0 , this.db.removePerson("Max"));

        //single interaction: talk -> delete
        this.db.addInteractions(testData2);
        assertEquals(1, this.db.removePerson("T1"));
        assertTrue(this.db.isEmpty());
        this.db.clear();

        //multiple interactions: meeting -> talk
        this.db.addInteractions(testData3);
        assertEquals(1, this.db.removePerson("T3"));
        assertFalse(this.db.contains("T3", "T4", 202011303, 45, 3));
        assertFalse(this.db.contains("T3", "T5", 202011303, 45, 3));
        assertTrue(this.db.contains("T4", "T5", 202011303, 45, 2));
        this.db.clear();

        //file interaction:
        this.db.addInteractions(testData10);
        assertEquals(1, this.db.removePerson("1"));
        assertTrue(this.db.isEmpty());
    }

    @Test
    public void contains() {
        assertFalse(this.db.contains("1", "2",0,10, 2));
        this.db.addInteractions(testData4);
        assertTrue(this.db.contains("1", "2",0,10, 2));
    }

    @Test
    public void clearAndIsEmpty() {
        this.db.addInteractions(testData4);
        assertFalse(this.db.isEmpty());
        this.db.clear();
        assertTrue(this.db.isEmpty());
    }
}

