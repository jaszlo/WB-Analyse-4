package app.http;

import app.Main;
import app.data.MimeObject;
import app.db.Database;
import app.http.logger.Logger;
import app.http.logger.LoggerLevel;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Contains the http server for the rest API.
 */
public class Server {
	private static HttpServer server = null;
	private static ExecutorService executor = null;
	public static Database db = null;


	/**
	 * Starts a new local server with the given port. It can handle simple
	 * HTTP requests.
	 *
	 * @param port Port that the server should operate on
	 * @param db Database used for storing interactions and generating graphs
	 */
	public static void start(int port, Database db) {
		Server.db = db;
		if(server != null) {
			Logger.logStartError(port);
			return;
		}

		// Create authenticator for 'protected' routes
		BasicAuthenticator authenticator = new BasicAuthenticator("GET") {
			@Override
			public boolean checkCredentials(String user, String pwd) {
				return user.equals(Main.properties.getProperty("admin_user")) && pwd.equals(Main.properties.getProperty("admin_pass"));
			}
		};

		try {
			// Creates a new HTTP server with the given port.
			server = HttpServer.create(new InetSocketAddress(port), 0);

			// Set execution mode to parallel.
			executor = java.util.concurrent.Executors.newCachedThreadPool();
			server.setExecutor(executor);

			// Create new end points
			server.createContext("/", RequestHandlers::rootRequests);
			server.createContext("/admin", RequestHandlers::apiHandlerAdminPage).setAuthenticator(authenticator);
			server.createContext("/api/interaction", RequestHandlers::apiHandlerInteraction);
			server.createContext("/api/graph", RequestHandlers::apiHandlerGraph);
			server.createContext("/api/network-analysis", RequestHandlers::apiHandlerNetworkAnalysis);
			server.createContext("/api/graph-svg", RequestHandlers::apiHandlerGraphSVG);
			server.createContext("/shutdown", RequestHandlers::shutdown).setAuthenticator(authenticator);
			server.createContext("/api/ids", RequestHandlers::apiHandlerIds);

			Logger.logStartSuccess(port);

		} catch (IOException e) {
			Logger.logStartError(port);
			System.exit(1);
		}
		server.start();
	}

	/**
	 * Method that stops the server
	 *
	 * @param delay Delay after how many seconds the server should stop and all active connections should be terminated
	 */
	public static void stop(int delay) {
		server.stop(delay);
		executor.shutdown();
		server = null;
		executor = null;
	}

	/**
	 * Sends a response to the given exchange with a given status code and response-body.
	 *
	 * @param exchange The exchange to which will be responded.
	 * @param rCode    The status code of the response.
	 * @param response The object that is send to the recipient.
	 * @param contentTypes List of types by priority that should be used for the response encoding.
	 */
	public static void sendResponse(HttpExchange exchange, int rCode, Object response, List<String> contentTypes) {

		MimeObject responseObject;
		try {
			responseObject = Encoder.encode(response, contentTypes);
		} catch (IllegalArgumentException e) {
			sendInternalError(exchange, e.getMessage());
			return;
		}

		try {
			OutputStream os = exchange.getResponseBody();
			exchange.getResponseHeaders().set("Content-Type", responseObject.type);
			exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Risky misky
			exchange.sendResponseHeaders(rCode, responseObject.content.length);
			os.write(responseObject.content);
			Logger.logResponse(exchange, new String(responseObject.content));
			os.close();
		} catch (IOException e) {
			sendInternalError(exchange);
			Logger.log("ERROR", "Error while sending response", LoggerLevel.BASIC);
			e.printStackTrace();
		}
		exchange.close();
	}

	/**
	 * Sends a response to the given exchange with a given status code and response-body.
	 *
	 * @param exchange The exchange to which will be responded.
	 * @param rCode    The status code of the response.
	 * @param response The object that is send to the recipient.
	 * @param contentType Type that should be used for the response encoding.
	 */
	public static void sendResponse(HttpExchange exchange, int rCode, Object response, String contentType) {
		sendResponse(exchange, rCode, response, List.of(contentType));
	}

	/**
	 * Sends a response to the given exchange with a given status code and response-body. The type of the body gets
	 * inferred by the requested mime-type.
	 *
	 * @param exchange The exchange to which will be responded.
	 * @param rCode    The status code of the response.
	 * @param response The body-content of the response.
	 */
	public static void sendResponse(HttpExchange exchange, int rCode, Object response) {
		sendResponse(exchange, rCode, response, exchange.getRequestHeaders().get("Accept"));
	}

	/**
	 * Function that returns a 500 HTTP-status code and lets the user know that some internal
	 * server error has happened and their request could not be handled correctly.
	 *
	 * @param exchange The request that will be handled.
	 * @param errorMessage Error message for the User.
	 */
	public static void sendInternalError(HttpExchange exchange, String errorMessage) {
		JsonObject response = new JsonObject();
		response.addProperty("error", errorMessage);
		sendResponse(exchange, 500, response.toString(), "application/json");
	}

	/**
	 * Function that returns a 500 HTTP-status code and lets the user know that some internal
	 * server error has happened and their request could not be handled correctly with a generic error message.
	 *
	 * @param exchange The request that will be handled.
	 */
	public static void sendInternalError(HttpExchange exchange) {
		sendInternalError(exchange, "Oops, this should not have happened.");
	}

	/**
	 * Function that returns a 405 HTTP-status code and lets the user know that the request
	 * method used is not allowed for the chosen route.
	 *
	 * @param exchange The request that will be handled.
	 */
	public static void sendMethodNotAllowed(HttpExchange exchange) {
		String response = "{\"error\": \"Method Not Allowed\"}";
		sendResponse(exchange, 405, response, "application/json");

	}

	/**
	 * Function that returns a 400 HTTP-status code and lets the user know that the parameters
	 * of the request were not in the correct format.
	 *
	 * @param exchange The request that will be handled.
	 */
	public static void sendBadRequest(HttpExchange exchange, String errorMessage) {
		JsonObject response = new JsonObject();
		response.addProperty("error", errorMessage);
		sendResponse(exchange, 400, response.toString(), "application/json");
	}
}