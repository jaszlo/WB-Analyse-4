package app.http;

import app.data.ResponseObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test class to test the request class creating http requests")
public class TestRequest {
    @Test
    @DisplayName("Testing if get request returns valid response code 200")
    public void testGetValidUrl_200() {
        ResponseObject response = Request.get("http://httpbin.org/base64/VGVzdA==");

        assertNotNull(response);
        assertEquals(200, response.statusCode);
        assertEquals("Test", response.bodyAsString());
    }

    @Test
    @DisplayName("Test if get request is invalid request method 405")
    public void testGetMethodNotAllowed_405() {
        ResponseObject response = Request.get("http://httpbin.org/status/405");

        assertNotNull(response);
        assertEquals(405, response.statusCode);
    }

    @Test
    @DisplayName("Test if get request is invalid request 400")
    public void testGetBadRequest_400() {
        ResponseObject response = Request.get("http://httpbin.org/status/400");

        assertNotNull(response);
        assertEquals(400, response.statusCode);
    }

    @Test
    @DisplayName("Test if post request returns valid response code 200")
    public void testPostValidUrl_200() {
        ResponseObject response = Request.post("http://httpbin.org/post", "Test");

        assertNotNull(response);
        assertEquals(200, response.statusCode);

        JsonElement actual = JsonParser.parseString(response.bodyAsString());
        String expected = "Test";

        assertTrue(actual.isJsonObject());
        assertTrue(actual.getAsJsonObject().has("data"));
        assertEquals(expected, actual.getAsJsonObject().get("data").getAsString());
    }

    @Test
    @DisplayName("Test if post request is invalid request method 405")
    public void testPostBadRequest_405() {
        ResponseObject response = Request.post("http://httpbin.org/status/405", "Test");

        assertNotNull(response);
        assertEquals(405, response.statusCode);
    }

    @Test
    @DisplayName("Test if post request is invalid request 400")
    public void testPostBadRequest_400() {
        ResponseObject response = Request.post("http://httpbin.org/status/400", "Test");

        assertNotNull(response);
        assertEquals(400, response.statusCode);
    }
}
