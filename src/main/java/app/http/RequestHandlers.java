package app.http;

import app.Main;
import app.analysis.Centralization;
import app.data.AnalysisRequest;
import app.data.InteractionRequest;
import app.data.SVGRequest;
import app.db.GraphOptions;
import app.graph.Graph;
import app.graph.Utils;
import app.http.logger.Logger;
import app.http.logger.LoggerLevel;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing functions to handle request for specific routes
 * /                        -   rootRequests                (GET)
 * /admin                   -   apiHandlerAdmin             (GET)
 * /shutdown                -   shutdown                    (GET)
 * /api/interaction         -   apiHandlerInteraction       (POST)
 * /api/graph               -   apiHandlerGraph             (GET, POST)
 * /api/network-analysis    -   apiHandlerNetworkAnalysis   (POST)
 * /api/graph-svg           -   apiHandlerGraphSvg          (GET)
 * /api/ids                 -   apiHandlerIds               (GET)
 */
public class RequestHandlers {

    /**
     * Function called to handle a request GET at /.
     * It can be used for a basic check whether the api is available
     *
     * @param exchange The request that will be handled.
     */
    public static void rootRequests(HttpExchange exchange) {
        Logger.logRequest(exchange, "{  }");

        // Only handle GET requests
        if (!exchange.getRequestMethod().equals("GET")) {
            Server.sendMethodNotAllowed(exchange);
            return;
        }

        String response = "hello world";
        Server.sendResponse(exchange, 200, response, "text/plain");

    }

    /**
     * Function called to handle a POST request at /api/interaction.
     * Handles POST and DELETE requests.
     * POST:   If valid the interaction data from the body is added to the database via POST requests.
     * DELETE: If a valid id could be parsed from the body the person gets deleted from the database.
     *
     * @param exchange The request that will be handled.
     */
    public static void apiHandlerInteraction(HttpExchange exchange) {

        // Handle POST requests
        if (exchange.getRequestMethod().equals("POST")) {
            InteractionRequest data;
            try {
                data = Decoder.asInteraction(exchange);
            } catch (IllegalArgumentException e) {
                Server.sendBadRequest(exchange, e.getMessage());
                return;
            } catch (Exception e) {
                Server.sendInternalError(exchange, e.getMessage());
                Logger.log(e);
                return;
            }

            // Add the given interaction to the database
            int interactionCount;
            try {
                interactionCount = Server.db.addInteractions(data);
            } catch (Exception e) {
                Server.sendInternalError(exchange, e.getMessage());
                Logger.log(e);
                return;
            }

            // Create Json response
            JsonObject response = new JsonObject();
            response.addProperty("added", interactionCount);

            Server.sendResponse(exchange, 200, response, "application/json");

        // Handle DELETE requests
        } else if (exchange.getRequestMethod().equals("DELETE")) {
            String toDelete;
            int deleted;
            try {
                toDelete = Decoder.asToDelete(exchange);
                deleted = Server.db.removePerson(toDelete);
            } catch (IllegalArgumentException e) {
                Server.sendBadRequest(exchange, e.getMessage());
                return;
            } catch (Exception e) {
                Server.sendInternalError(exchange, e.getMessage());
                Logger.log(e);
                return;
            }

            // Create Json response
            JsonObject response = new JsonObject();
            response.addProperty("deleted", deleted);

            Server.sendResponse(exchange, 200, response, "application/json");
        } else {
            Server.sendMethodNotAllowed(exchange);
        }
    }

    /**
     * Function called to handle a POST request at /api/ids.
     * Handles only GET requests.
     * Returns all IDs stored in the Database
     *
     * @param exchange The request that will be handled.
     */
    public static void apiHandlerIds(HttpExchange exchange) {
        Logger.logRequest(exchange, "{  }");

        // Only handle GET requests
        if (!exchange.getRequestMethod().equals("GET")) {
            Server.sendMethodNotAllowed(exchange);
            return;
        }

        List<String> ids;
        try {
            ids = Server.db.getAllIds();
        } catch (Exception e) {
            Server.sendInternalError(exchange, e.getMessage());
            Logger.log(e);
            return;
        }

        Server.sendResponse(exchange, 200, ids, "application/json");
    }

    /**
     * Function called to handle a GET request at /api/graph.
     * It generates a graph from the interaction data in the db and returns it as a JSON
     *
     * @param exchange The request that will be handled.
     */
    public static void apiHandlerGraph(HttpExchange exchange) {
        Logger.logRequest(exchange, "{  }");

        GraphOptions option;
        // Only handle GET or POST requests
        if (exchange.getRequestMethod().equals("GET")) {
            option = GraphOptions.valueOf(Main.properties.getProperty("default_graph_options"));
        } else if (exchange.getRequestMethod().equals("POST")) {
            try {
                option = Decoder.asGraphOption(exchange);
            } catch (IllegalArgumentException e) {
                Server.sendBadRequest(exchange, e.getMessage());
                Logger.log(e);
                return;
            } catch (Exception e) {
                Server.sendInternalError(exchange, e.getMessage());
                Logger.log(e);
                return;
            }
        } else {
            Server.sendMethodNotAllowed(exchange);
            return;
        }

        Graph graph;
        try {
            graph = Server.db.generateGraph(option);
        } catch(Exception e) {
            Server.sendInternalError(exchange, e.getMessage());
            Logger.log(e);
            return;
        }

        Server.sendResponse(exchange, 200, graph);
    }

    /**
     * Function called to handle a POST request at /api/network-analysis.
     * Performs analyses on a given graph or if no graph analyses the graph of the database.
     *
     * @param exchange The request that will be handled.
     */
    public static void apiHandlerNetworkAnalysis(HttpExchange exchange) {
        // Only handle POST requests
        if (!exchange.getRequestMethod().equals("POST")) {
            Server.sendMethodNotAllowed(exchange);
            return;
        }

        AnalysisRequest data;
        try {
            data = Decoder.asAnalysis(exchange);
        } catch(IllegalArgumentException e) {
            Server.sendBadRequest(exchange, e.getMessage());
            return;
        } catch(Exception e) {
            Server.sendInternalError(exchange, e.getMessage());
            Logger.log(e);
            return;
        }

        Map<String, Map<String, Double>> resultObject = new HashMap<>();
        Map<String, Double> centralizations = new HashMap<>();
        if(data.centralizationRequested()) {
            resultObject.put("centralization", centralizations);
        }

        for(int i = 0; i < data.getCentralitySize(); i++) {
            try {
                resultObject.put(data.getCentrality(i), data.getCentralityFunction(i).apply(data.getGraph()));
                if(data.centralizationRequested()) {
                    centralizations.put(data.getCentrality(i), Centralization.getCentralization(data.getGraph(), data.getCentralityFunction(i)));
                }
            } catch(IllegalArgumentException e) {
                Server.sendBadRequest(exchange, e.getMessage());
                return;
            } catch(Exception e) {
                Server.sendInternalError(exchange, e.getMessage());
                Logger.log(e);
                return;
            }
        }

        // Return all calculated centralities as a JSON
        Server.sendResponse(exchange, 200, resultObject);
    }

    /**
     * Function called to handle a POST request at /api/graph-svg.
     * It generates a graph from the interaction data in the db and returns it as an SVG
     *
     * @param exchange The request that will be handled.
     */
    public static void apiHandlerGraphSVG (HttpExchange exchange) {

        // Only handle POST requests
        if (!exchange.getRequestMethod().equals("POST")) {
            Server.sendMethodNotAllowed(exchange);
            return;
        }

        SVGRequest data;
        try {
            data = Decoder.asSVG(exchange);
        } catch (IllegalArgumentException e) {
            Server.sendBadRequest(exchange, e.getMessage());
            return;
        } catch (Exception e) {
            Server.sendInternalError(exchange, e.getMessage());
            Logger.log(e);
            return;
        }

        if (data.getCentralitySize() != 1) {
            Server.sendBadRequest(exchange, "Exactly one centrality needs to be specified");
            return;
        }

        String svgFile;
        try {
            svgFile = Utils.graphToSvgWithExternalGraphviz(data);
        } catch (IllegalArgumentException e) {
            Server.sendBadRequest(exchange, e.getMessage());
            return;
        } catch(IOException e) {
            Logger.log("Hint", "Graphviz is probably not installed natively or in wsl, using backup.", LoggerLevel.BASIC);
            Logger.log(e);
            try {
                svgFile = Utils.graphToSvg(data);
            } catch(Exception e2) {
                Logger.log(e2);
                svgFile = """
                <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 203 43" width="203" height="43">
                 \s
                  <rect x="0" y="0" width="203" height="43" fill="#ffffff"></rect><g transform="translate(10 10) rotate(0 91.5 11.5)"><text x="0" y="18" font-family="Helvetica, Segoe UI Emoji" font-size="20px" fill="#000000" text-anchor="start" style="white-space: pre;" direction="ltr">Error</text></g></svg>
                """;
            }
        } catch(Exception e) {
            Logger.log(e);
            Server.sendInternalError(exchange, e.getMessage());
            return;
        }

        Server.sendResponse(exchange, 200, svgFile, "image/svg+xml");

    }

    /**
     * Function called to handle a POST request at /admin.
     * It response an html page that contains some control-elements for this api.
     *
     * @param exchange The request that will be handled.
     */
    public static void apiHandlerAdminPage(HttpExchange exchange) {
        Logger.logRequest(exchange, "{  }");

        // Only handle GET requests
        if (!exchange.getRequestMethod().equals("GET")) {
            Server.sendMethodNotAllowed(exchange);
            return;
        }

        try(InputStream s = RequestHandlers.class.getResourceAsStream("/index.html")) {
            Server.sendResponse(exchange, 200, s, "text/html");
        } catch (IOException e) {
            Server.sendInternalError(exchange, "Error while reading html file. Please contact an administrator.");
        }
    }

    /**
     * Function called to shutdown the server
     *
     * @param exchange The request that will be handled.
     */
    public static void shutdown(HttpExchange exchange) {
        Logger.logRequest(exchange, "{  }");

        String response = "[TERMINATING] Shutting down the Server. This will take a few seconds.";
        Server.sendResponse(exchange, 200, response, "text/plain");

        new Thread(() -> Server.stop(0)).start();
    }
}