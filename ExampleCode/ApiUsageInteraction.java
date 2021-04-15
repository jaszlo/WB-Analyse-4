import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.Collection;
import java.util.Map;

import static java.util.Map.entry;

import java.util.List;
import java.util.stream.Collectors;

import java.nio.charset.StandardCharsets;


/*
Changelog:

1.0.0
 - Added entire file

2.0.0
 - Removed redundant suppression of warnings
 - Changed parameter ids to type Collection. This is less restrictive than List so it is
   backwards compatible.
 - Added new argument to the add interaction method for the FLOW analysis as discussed. To avoid unnecessary
   constraint we have decided to set the format of the ids for a document interaction as
   [userId, userId, ..., userId, documentId]. The only relevant use case for you is probably [userId, documentId].
 - Added convenience methods
 */

public class ApiUsageInteraction {

    /**
     * Adds given ids as interactions to the analyse4 db.
     *
     * @param ids        Ids of the subjects that interacted. IF isDocument is set to true the given collection
     *                   shall be ordered (List, SortedSet, ArrayList, ... are fine, e.g. Set is not).
     * @param datetime   Start time of the interaction as an UNIX timestamp in milliseconds
     *                   (See https://currentmillis.com/tutorials/system-currentTimeMillis.html)
     * @param duration   The duration of the interaction in milliseconds
     * @param isDocument If set to true the last id in the interaction is interpreted as an id of a document
     *                   (e.g. an URL). The interaction is interpreted as a read/write operation on a document.
     */
    private static void addInteraction(Collection<String> ids, long datetime, int duration, boolean isDocument) {
        // Characters that need to be escaped in the JSON strings (\ is left out because it needs to get escaped first)
        Map<String, String> specialChars = Map.ofEntries(
                entry("\b", "\\b"),
                entry("\f", "\\f"),
                entry("\n", "\\n"),
                entry("\r", "\\r"),
                entry("\t", "\\t"),
                entry("\"", "\\\"")
        );

        // Insert arguments into JSON template and escape special characters in given ids
        String body = String.format("{\"names\": [%s], \"datetime\": %d, \"duration\":%d, \"document\": %b}", ids.stream().map(s -> {
            s = s.replace("\\", "\\\\");
            for (Map.Entry<String, String> entry : specialChars.entrySet()) {
                s = s.replace(entry.getKey(), entry.getValue());
            }
            return "\"" + s + "\"";
        }).collect(Collectors.joining(",")), datetime, duration, isDocument);

        System.out.println(body);

        // Create an http-client which will send the request and handle the response
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://goethe.se.uni-hannover.de:9993/api/interaction"))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();


        // Send the interaction asynchronously to avoid long wait times that block the caller
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).thenApply(response -> {
            if (response.statusCode() != 200) {
                System.err.println(response.body()); // Todo: If you want error handling do it here
                // In our opinion the loss of a single interaction is not that relevant
                // so no error handling is performed for now.
            }
            return null;
        });

    }


    /**
     * Adds given ids as interactions to the analyse4 db as a meeting (talk).
     *
     * @param ids      Ids of the users that interacted
     * @param datetime Start time of the interaction as an UNIX timestamp in milliseconds
     *                 (See https://currentmillis.com/tutorials/system-currentTimeMillis.html)
     * @param duration The duration of the interaction in milliseconds
     */
    public static void addMeetingInteraction(Collection<String> ids, long datetime, int duration) {
        addInteraction(ids, datetime, duration, false);
    }


    /**
     * Adds given the interaction to the analyse4 db as a document interaction (read/write).
     *
     * @param userId     Id of the user that interacted with the document
     * @param documentId Id of the document that the user interacted with
     * @param datetime   Start time of the interaction as an UNIX timestamp in milliseconds
     *                   (See https://currentmillis.com/tutorials/system-currentTimeMillis.html)
     */
    public static void addDocumentInteraction(String userId, String documentId, long datetime) {
        addInteraction(List.of(userId, documentId), datetime, 0, true);
    }


    // Deprecated since addMeetingInteraction should be used
    @Deprecated
    public static void addInteraction(Collection<String> ids, long datetime, int duration) {
        addMeetingInteraction(ids, datetime, duration);
    }


    public static void main(String[] args) {

        addMeetingInteraction(List.of("<user id 1>", "<user id 2>"), System.currentTimeMillis(), 600000);
        addDocumentInteraction("<user id>", "<document id e.g. the link>", System.currentTimeMillis());

        // Since the request is asynchronous we need to wait for it to finish in this example code.
        // As soon as the main function returns the request is aborted. In your application you do
        // not need to wait.
        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {

        }
    }
}