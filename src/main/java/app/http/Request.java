package app.http;

import app.data.MimeObject;
import app.data.ResponseObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


/**
 * This class contains functions to create HTTP requests easily.
 */
public class Request {
    /**
     * A Function that creates a HTTP-GET request to a given url.
     *
     * @param url The url to create the GET request to
     * @return Returns the response body and status code
     */
    public static ResponseObject get(String url) {
        // Create a http-client which will send the request and handle the response
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            // Get the response body and status code and return it
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return new ResponseObject(response.statusCode(), new MimeObject(response.headers().firstValue("Content-Type").orElse(""), response.body()));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // If an error occurred during reading the response return null
            return null;
        }
    }

    /**
     * A Function that creates a HTTP-POST request to a given url with a given body.
     *
     * @param url  The url to create the a POST request to
     * @param body The body of the POST request
     * @param accept The content type that is expected from the response
     * @return Returns the response body and status code
     */
    public static ResponseObject post(String url, String body, String accept) {

        // Create a http-client which will sent the request and handle the response
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Accept", accept)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            // Get the response body and status code and return it
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return new ResponseObject(response.statusCode(), new MimeObject(response.headers().firstValue("Content-Type").orElse(""), response.body()));

        } catch (IOException | InterruptedException e) {
            // e.printStackTrace();
            // If an error occurred during reading the response return null
            return null;
        }
    }

    /**
     * A Function that creates a HTTP-POST request to a given url with a given body.
     *
     * @param url  The url to create the a POST request to
     * @param body The body of the POST request
     * @return Returns the response body and status code
     */
    public static ResponseObject post(String url, String body) {
        return post(url, body, "application/json");
    }
}
