package app.http;


import app.Main;
import app.data.ResponseObject;
import app.db.Database;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test class to test available routes for the http-server")
public class TestAddInteraction {

    @BeforeAll
    public void prepareTests() {
        Server.start(5000, new Database(Main.properties));
        Server.db.initTest();
    }

    @Test
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


    @AfterAll
    public void deInitTests() {
        Server.stop(0);
        Server.db.deInitTest();
    }
}
