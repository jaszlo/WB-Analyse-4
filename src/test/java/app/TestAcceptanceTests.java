package app;


import app.data.ResponseObject;
import app.db.Database;
import app.http.Request;
import app.http.Server;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test class to test the acceptance tests specified in the specification")
public class TestAcceptanceTests {

    private final double DELTA = 0.001;

    private final String graphGAsJSON =
            """
            {
               "a1": {"a2": 1, "b1": 0.25, "c1": 0.25},
               "b2": {"a2": 0.25, "c2": 0.5, "d2": 0.75, "e2": 0.25},
               "a2": {"b2": 0.25, "a1": 1, "d2": 0.25, "e2": 0.25, "c2": 0.25},
               "d1": {"c1": 0.25},
               "e2": {"a2": 0.25, "b2": 0.25, "c2": 0.75, "d2": 0.25},
               "c1": {"d1": 0.25, "a1": 0.25, "b1": 0.75},
               "d2": {"a2": 0.25, "c2": 0.25, "b2": 0.75, "e2": 0.25},
               "b1": {"a1": 0.25, "c1": 0.75},
               "c2": {"a2": 0.25, "b2": 0.5, "d2": 0.25, "e2": 0.75}
            }
            """;

    private void compareCentralityResult(String expected, String actual) {

        // Prepare expected result for comparison
        JsonElement expectedJsonElement = JsonParser.parseString(expected);

        // Try to parse actual result to JSON
        JsonElement actualJsonElement = null;
        try {
            actualJsonElement = JsonParser.parseString(actual);
        } catch (JsonSyntaxException e) {
            fail("Could not parse body of /api/network-analysis response");
        }
        assertNotNull(actualJsonElement, "Expected non null response");
        assertTrue(actualJsonElement.isJsonObject(), "Expected response to be a JSON Object");


        // Assert that all the expected centralities were calculated
        assertEquals(expectedJsonElement.getAsJsonObject().keySet(), actualJsonElement.getAsJsonObject().keySet(), "Not all centralities were calculated");

        // For all calculated centralities compare the results with 0.1% delta
        for(String centrality: expectedJsonElement.getAsJsonObject().keySet()) {
            assertTrue(actualJsonElement.getAsJsonObject().get(centrality).isJsonObject(), "Expected result for " + centrality + " to be of type JSON object");
            assertEquals(expectedJsonElement.getAsJsonObject().getAsJsonObject(centrality).keySet(), actualJsonElement.getAsJsonObject().getAsJsonObject(centrality).keySet(), "Not all centrality values were calculated");

            Map<String, Double> expectedEntries = expectedJsonElement.getAsJsonObject().getAsJsonObject(centrality).entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsDouble()));

            for (Map.Entry<String, JsonElement> e : actualJsonElement.getAsJsonObject().getAsJsonObject(centrality).entrySet()) {
                assertTrue(e.getValue().isJsonPrimitive(), "Expected centrality value to be of type double");
                assertTrue(e.getValue().getAsJsonPrimitive().isNumber(), "Expected centrality value to be of type double");

                assertEquals(expectedEntries.get(e.getKey()), e.getValue().getAsDouble(), expectedEntries.get(e.getKey()) * DELTA, "Expected centrality value does not match with expected value for id " + e.getKey());
            }
        }
    }

    @BeforeAll
    public void prepareTests() {
        Server.start(5000, new Database(Main.properties));
        Server.db.initTest();
    }

    @Test
    @Order(1)
    @DisplayName("Test if interactions can be added and add interactions for graph G")
    public void addInteractions() {
        String template =
                """
                {
                  "names": ["%s", "%s"],
                  "datetime": %s,
                  "duration": 3600000
                }
                """;

        String[] requestList = {
                String.format(template, "a1", "b1", "1607776910616"),
                String.format(template, "b1", "c1", "1607776910617"),
                String.format(template, "a1", "a2", "1607776910618"),
                String.format(template, "a1", "c1", "1607776910619"),
                String.format(template, "a2", "b2", "1607776910620"),
                String.format(template, "a2", "c2", "1607776910621"),
                String.format(template, "d2", "b2", "1607776910622"),
                String.format(template, "e2", "c2", "1607776910623"),
                String.format(template, "b2", "c2", "1607776910624"),
                String.format(template, "a1", "a2", "1607776910625"),
                String.format(template, "c1", "d1", "1607776910626"),
                String.format(template, "b1", "c1", "1607776910627"),
                String.format(template, "d2", "b2", "1607776910628"),
                String.format(template, "e2", "c2", "1607776910629"),
                String.format(template, "a1", "a2", "1607776910630"),
                String.format(template, "d2", "a2", "1607776910631"),
                String.format(template, "b2", "c2", "1607776910632"),
                String.format(template, "d2", "c2", "1607776910633"),
                String.format(template, "b1", "c1", "1607776910634"),
                String.format(template, "e2", "a2", "1607776910635"),
                String.format(template, "e2", "b2", "1607776910636"),
                String.format(template, "a1", "a2", "1607776910637"),
                String.format(template, "d2", "b2", "1607776910638"),
                String.format(template, "e2", "c2", "1607776910639"),
                String.format(template, "e2", "d2", "1607776910640"),
        };

        JsonElement expected = JsonParser.parseString("{\"added\": 2}");
        for (String request: requestList) {
            ResponseObject response = Request.post("http://localhost:5000/api/interaction", request);
            assertNotNull(response, "Got no response but expected a response");
            assertEquals(200, response.statusCode, "Did not get the correct status code");

            // Assert that response is in the JSON Format
            JsonElement actual = null;
            try {
                actual = JsonParser.parseString(response.bodyAsString());
            } catch (JsonSyntaxException e){
                fail("Server did not reply with a JSON formatted String");
            }

            assertNotNull(actual, "Expected non empty JSON response");

            // Todo: Talk to the customer about change
            assertEquals(expected, actual, "Wrong number of entries added to database");
        }
    }


    @Test
    @Order(2)
    @DisplayName("Test if the generated graph matches with graph G")
    public void generateGraph() {

        String expectedJSON = graphGAsJSON;

        JsonElement expected = JsonParser.parseString(expectedJSON);

        ResponseObject response = Request.get("http://localhost:5000/api/graph");

        // Assert got a response
        assertNotNull(response);
        // Assert that status code is ok
        assertEquals(200, response.statusCode);

        // Assert that response is in the JSON Format
        JsonElement actual = null;
        try {
            actual = JsonParser.parseString(response.bodyAsString());

        } catch (JsonSyntaxException e){
            fail("Could not parse body of /api/graph response");
        }
        assertNotNull(actual);

        // Assert that the correct number of interactions was added
        assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    @DisplayName("Test if closeness centrality is calculated correctly with the explicit graph")
    public void closenessWithGraph() {
        String expectedJSON =
                """
                {
                    "closeness": {
                        "a1": 0.8648648648648649,
                        "b2": 0.6857142857142856,
                        "a2": 0.8888888888888888,
                        "d1": 0.4085106382978724,
                        "e2": 0.6575342465753425,
                        "c1": 0.6357615894039734,
                        "d2": 0.6575342465753425,
                        "b1": 0.6193548387096774,
                        "c2": 0.6857142857142856
                    }
                }
                """;


        String request = String.format("""
                {
                    "centralities": ["closeness"],
                    "data": %s
                }
                """, graphGAsJSON);

        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        compareCentralityResult(expectedJSON, response.bodyAsString());
    }

    @Test
    @Order(3)
    @DisplayName("Test if closeness centrality is calculated correctly with the generated graph")
    public void closenessWithoutGraph() {
        String expectedJSON =
                """
                {
                    "closeness": {
                        "a1": 0.8648648648648649,
                        "b2": 0.6857142857142856,
                        "a2": 0.8888888888888888,
                        "d1": 0.4085106382978724,
                        "e2": 0.6575342465753425,
                        "c1": 0.6357615894039734,
                        "d2": 0.6575342465753425,
                        "b1": 0.6193548387096774,
                        "c2": 0.6857142857142856
                    }
                }
                """;


        String request = """
                {
                    "centralities": ["closeness"]
                }
                """;

        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        compareCentralityResult(expectedJSON, response.bodyAsString());
    }

    @Test
    @Order(4)
    @DisplayName("Test if betweenness centrality is calculated correctly with the explicit graph")
    public void betweennessWithGraph() {
        String expectedJSON =
                """
                {
                    "betweenness": {
                        "a1": 0.5357142857142857,
                        "b2": 0.03571428571428571,
                        "a2": 0.5714285714285714,
                        "d1": 0.0,
                        "e2": 0.0,
                        "c1": 0.25,
                        "d2": 0.0,
                        "b1": 0.0,
                        "c2": 0.03571428571428571
                    }
                }
                """;


        String request = String.format("""
                {
                    "centralities": ["betweenness"],
                    "data": %s
                }
                """, graphGAsJSON);

        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        compareCentralityResult(expectedJSON, response.bodyAsString());
    }

    @Test
    @Order(4)
    @DisplayName("Test if betweeness centrality is calculated correctly with the generated graph")
    public void betweennessWithoutGraph() {
        String expectedJSON =
                """
                {
                    "betweenness": {
                        "a1": 0.5357142857142857,
                        "b2": 0.03571428571428571,
                        "a2": 0.5714285714285714,
                        "d1": 0.0,
                        "e2": 0.0,
                        "c1": 0.25,
                        "d2": 0.0,
                        "b1": 0.0,
                        "c2": 0.03571428571428571
                    }
                }
                """;


        String request = """
                {
                    "centralities": ["betweenness"]
                }
                """;

        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        compareCentralityResult(expectedJSON, response.bodyAsString());
    }

    @Test
    @Order(5)
    @DisplayName("Test if eigenvector centrality is calculated correctly with the explicit graph")
    public void eigenvectorWithGraph() {
        String expectedJSON =
                """
                {
                    "eigenvector": {
                        "a1": 0.13710442313196577,
                        "b2": 0.4364225935824221,
                        "a2": 0.46350795737155703,
                        "d1": 0.011785494846305042,
                        "e2": 0.4364225935824221,
                        "c1": 0.04786769748674075,
                        "d2": 0.4364225935824221,
                        "b1": 0.04553916449180267,
                        "c2": 0.4364225935824221
                    }
                }
                """;



        String request = String.format("""
                {
                    "centralities": ["eigenvector"],
                    "data": %s
                }
                """, graphGAsJSON);

        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        compareCentralityResult(expectedJSON, response.bodyAsString());
    }

    @Test
    @Order(5)
    @DisplayName("Test if eigenvector centrality is calculated correctly with the generated graph")
    public void eigenvectorWithoutGraph() {
        String expectedJSON =
                """
                {
                    "eigenvector": {
                        "a1": 0.13710442313196577,
                        "b2": 0.4364225935824221,
                        "a2": 0.46350795737155703,
                        "d1": 0.011785494846305042,
                        "e2": 0.4364225935824221,
                        "c1": 0.04786769748674075,
                        "d2": 0.4364225935824221,
                        "b1": 0.04553916449180267,
                        "c2": 0.4364225935824221
                    }
                }
                """;


        String request = """
                {
                    "centralities": ["eigenvector"]
                }
                """;

        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        compareCentralityResult(expectedJSON, response.bodyAsString());
    }

    @Test
    @Order(6)
    @DisplayName("Test if a wrong graph format is detected")
    public void wrongGraphFormat() {

        JsonElement expected = JsonParser.parseString("{\"error\": \"Body is not of expected type JSON\"}");

        String request = "{{";
        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response, "Got no response but expected a response");
        assertEquals(400, response.statusCode, "Did not get the correct status code");

        // Assert that response is in the JSON Format
        JsonElement actual = null;
        try {
            actual = JsonParser.parseString(response.bodyAsString());
        } catch (JsonSyntaxException e){
            fail("Server did not reply with a JSON formatted String");
        }

        assertNotNull(actual, "Expected non empty JSON response");

        assertEquals(expected, actual, "Got wrong error message");
    }

    @Test
    @Order(6)
    @DisplayName("Test if a wrong interaction format is detected")
    public void wrongInteractionFormat() {

        JsonElement expected = JsonParser.parseString("{\"error\": \"JSON is missing field: centralities\"}");

        String request = "{\"Jakob\": 7}";
        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response, "Got no response but expected a response");
        assertEquals(400, response.statusCode, "Did not get the correct status code");

        // Assert that response is in the JSON Format
        JsonElement actual = null;
        try {
            actual = JsonParser.parseString(response.bodyAsString());
        } catch (JsonSyntaxException e){
            fail("Server did not reply with a JSON formatted String");
        }

        assertNotNull(actual, "Expected non empty JSON response");

        assertEquals(expected, actual, "Got wrong error message");
    }



    @AfterAll
    public void deInitTests() {
        Server.stop(0);
        Server.db.deInitTest();
    }
}
