package app.http;


import app.Main;
import app.db.Database;
import app.data.ResponseObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.*;


import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test class to test available routes for the http-server")
public class TestServer {

    @BeforeAll
    public void prepareTests() {
        Server.start(5000, new Database(Main.properties));
        Server.db.initTest();
    }

    @Test
    @Order(1)
    @DisplayName("Test basic route / for hello world")
    public void testApiHandlerRoot() {
        ResponseObject response = Request.get("http://localhost:5000/");

        assertNotNull(response);
        assertEquals(200, response.statusCode);
        assertEquals("hello world", response.bodyAsString());
    }

    @Test
    @Order(2)
    @DisplayName("Test if basic interactions of 5 people can be posted to /api/interaction")
    public void testApiHandlerInteraction_AddFivePeopleInteractions_TenAdded() {
        JsonElement expected = JsonParser.parseString("{\"added\": 5}");

        String request = """
                {
                  "names": ["a2", "b2", "c2", "d2", "e2"],
                  "datetime": 123456789124,
                  "duration": 122
                }
                """;
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

        assertEquals(expected, actual, "Wrong number of entries added to database");
    }

    @Test
    @Order(3)
    @DisplayName("Test if basic interactions of people can be posted individually to /api/interaction")
    public void testApiHandlerInteraction_AddSingleInteractions() {
        String template =
                """
                {
                  "names": ["%s", "%s"],
                  "datetime": %s,
                  "duration": 122
                }
                """;

        String[] requestList = {
                // Weight of four
                String.format(template, "a1", "a2", "123456789123"),
                String.format(template, "a1", "a2", "123456789223"),
                String.format(template, "a1", "a2", "123456789323"),
                String.format(template, "a1", "a2", "123456789423"),

                // Weight of three
                String.format(template, "b1", "c1", "123456789123"),
                String.format(template, "b1", "c1", "123456789223"),
                String.format(template, "b1", "c1", "123456789323"),

                String.format(template, "b1", "a1", "123456789123"),
                String.format(template, "c1", "a1", "123456789223"),

                String.format(template, "d1", "c1", "123456789123"),

                // Weight of three (1 already in DB)
                String.format(template, "b2", "d2", "123456789223"),
                String.format(template, "b2", "d2", "123456789323"),

                String.format(template, "b2", "c2", "123456789223"),

                // Weight of three (1 already in DB)
                String.format(template, "e2", "c2", "123456789223"),
                String.format(template, "e2", "c2", "123456789323"),
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

            assertEquals(expected, actual, "Wrong number of entries added to database");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test if invalid request method status code is returned from /api/graph when not using post")
    public void testApiHandlerInteraction_WrongRequestMethod_StatusCode405() {
        JsonElement expected = JsonParser.parseString("{\"error\": \"Method Not Allowed\"}");

        ResponseObject response = Request.get("http://localhost:5000/api/interaction");

        assertNotNull(response, "Got no response but expected a response");
        assertEquals(405, response.statusCode, "Did not get the correct status code");

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
    @Order(5)
    @DisplayName("Test if bad request status code is returned from /api/graph when given a JSON is empty")
    public void testApiHandlerInteraction_SendEmptyJson_StatusCode400() {
        JsonElement expected = JsonParser.parseString("{\"error\": \"JSON is missing field: names\"}");

        String request = "{}";
        ResponseObject response = Request.post("http://localhost:5000/api/interaction", request);

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
    @DisplayName("Test if bad request status code is returned from /api/graph when given a text not in JSON format")
    public void testApiHandlerInteraction_NotJsonFormat_StatusCode400() {
        JsonElement expected = JsonParser.parseString("{\"error\": \"Body is not of expected type JSON\"}");

        String request = "{{";
        ResponseObject response = Request.post("http://localhost:5000/api/interaction", request);

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
    @Order(7)
    @DisplayName("Test if bad request status code is returned from /api/graph when given JSON has the wrong fields")
    public void testApiHandlerInteraction_WrongFields_StatusCode400() {
        JsonElement expected = JsonParser.parseString("{\"error\": \"JSON is missing field: names\"}");

        String request = "{\"Jakob\": 7}";
        ResponseObject response = Request.post("http://localhost:5000/api/interaction", request);

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
    @Order(8)
    @DisplayName("Test if basic graph with no special options is returned from /api/graph")
    public void testApiHandlerGraph_NoOptions_Graph() {
        String expectedJSON = """
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
            //e.printStackTrace();
        }
        assertNotNull(actual);

        // Assert that the correct number of interactions was added
        assertEquals(expected, actual);
    }

    @Test
    @Order(9)
    @DisplayName("Test if basic closeness centrality map is returned from /api/network-analysis")
    public void testApiHandlerNetworkAnalysis_OnlyClosenessCentrality() {
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
        JsonElement expected = JsonParser.parseString(expectedJSON);
        String request = "{\"centralities\": [\"closeness\"]}";
        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        // Assert that response is in the JSON Format
        JsonElement actual = null;
        try {
            actual = JsonParser.parseString(response.bodyAsString());
        } catch (JsonSyntaxException e) {
            fail("Could not parse body of /api/network-analysis response");
        }
        assertNotNull(actual);
        // Assert that the correct number of interactions was added
        assertEquals(expected, actual);
    }

    @Test
    @Order(10)
    @DisplayName("Test if basic closeness centrality map is returned from /api/network-analysis as serialized map")
    public void testApiHandlerNetworkAnalysis_OnlyClosenessCentralityAsSerialized() {

        Map<String, Map<String, Double>> expected = Map.of(
                "closeness", Map.ofEntries(
                        entry("a1",0.8648648648648649),
                        entry("b2",0.6857142857142856),
                        entry("a2",0.8888888888888888),
                        entry("d1",0.4085106382978724),
                        entry("e2",0.6575342465753425),
                        entry("c1",0.6357615894039734),
                        entry("d2",0.6575342465753425),
                        entry("b1",0.6193548387096774),
                        entry("c2",0.6857142857142856)
                )
        );



        String request = "{\"centralities\": [\"closeness\"]}";
        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request, "application/x-java-serialized-object");

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        // Assert that response is in the JSON Format
        Object actual = response.bodyAsObject();
        assertNotNull(actual);
        // Assert that the correct number of interactions was added
        assertEquals(expected, actual);
    }

    @Test
    @Order(11)
    @DisplayName("Test if invalid body-text is detected from /api/network-analysis")
    public void testApiHandlerNetworkAnalysis_InvalidJSON() {
        String expectedJSON = "{\"error\":\"Body is not of expected type JSON object\"}";
        JsonElement expected = JsonParser.parseString(expectedJSON);
        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", "");

        assertNotNull(response);
        assertEquals(400, response.statusCode);

        // Assert that response is in the JSON Format
        JsonElement actual = null;
        try {
            actual = JsonParser.parseString(response.bodyAsString());
        } catch (JsonSyntaxException e){
            fail();
        }

        assertNotNull(actual);
        // Assert that the correct number of interactions was added
        assertEquals(expected, actual);
    }

    @Test
    @Order(12)
    @DisplayName("Test if valid body with missing values is detected from /api/network-analysis")
    public void testApiHandlerNetworkAnalysis_NoCentralities() {
        String expectedJSON = "{}";

        JsonElement expected = JsonParser.parseString(expectedJSON);
        String request = "{\"centralities\": []}";
        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", request);

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        // Assert that response is in the JSON Format
        JsonElement actual = null;
        try {
            actual = JsonParser.parseString(response.bodyAsString());
        } catch (JsonSyntaxException e){
            fail();
        }

        assertNotNull(actual);
        // Assert that the correct number of interactions was added
        assertEquals(expected, actual);
    }

    @Test
    @Order(13)
    @DisplayName("Test if empty body is handled by /api/network-analysis")
    public void testApiHandlerNetworkAnalysis_EmptyBody() {
        String expectedJSON = "{\"error\":\"JSON is missing field: centralities\"}";

        JsonElement expected = JsonParser.parseString(expectedJSON);
        ResponseObject response = Request.post("http://localhost:5000/api/network-analysis", "{}");

        assertNotNull(response);
        assertEquals(400, response.statusCode);

        // Assert that response is in the JSON Format
        JsonElement actual = null;
        try {
            actual = JsonParser.parseString(response.bodyAsString());
        } catch (JsonSyntaxException e){
            fail();
        }

        assertNotNull(actual);
        // Assert that the correct number of interactions was added
        assertEquals(expected, actual);
    }

    @AfterAll
    public void deInitTests() {
        Server.stop(0);
        Server.db.deInitTest();
    }
}
