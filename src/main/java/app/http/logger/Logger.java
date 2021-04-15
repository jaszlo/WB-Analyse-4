package app.http.logger;

import com.sun.net.httpserver.HttpExchange;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class contains the logger that is used for logging events within the application.
 */
public class Logger {
    // The current log level the logger will print
    private static LoggerLevel loggerLevel = LoggerLevel.DISABLED;

    /**
     * Sets the logger level that determines how much detail will be logged.
     * @param level The level of detail the logger will print.
     */
    public static void setLevel(LoggerLevel level) {
        loggerLevel = level;
    }

    /**
     * Sets the logger level that determines how much detail will be logged.
     * @param level The level of detail the logger will print.
     */
    public static void setLevel(String level) {
        loggerLevel = LoggerLevel.valueOf(level);
    }

    /**
     * Log a successful server start on a given port
     * @param port Port on which the Server was created
     */
    public static void logStartSuccess(int port) {
        if (loggerLevel == LoggerLevel.DISABLED) {
            return;
        }
        System.out.println("===============================================================================");
        System.out.println("[RUNNING ] Server is running on port " + port + ".");

    }

    /**
     * Log a unsuccessful server start on a given port
     * @param port Port on which the Server could not be created
     */
    public static void logStartError(int port) {
        if (loggerLevel == LoggerLevel.DISABLED) {
            return;
        }
        System.out.println("===============================================================================");
        System.out.println("[ERROR   ] Could not create Server. (Probably the port " + port + " is already being used)");
        logServerShutdown(true);
    }

    /**
     * Log a server shutdown. Also determine if the shutdown was due to an error or intended
     * @param dueToError Boolean flag to tell of the shutdown was intended or not
     */
    public static void logServerShutdown(boolean dueToError) {
        if (loggerLevel == LoggerLevel.DISABLED) {
            return;
        }
        if (dueToError) {
            System.out.println("[SHUTDOWN] Shutting down Server due to an error.");
        } else {
            System.out.println("[SHUTDOWN] This shutdown was willingly triggered via the shutdown route.");
        }
        System.out.println("===============================================================================");
    }

    /**
     * Log an incoming request to the server to a specified amount of detail
     * @param exchange The exchange containing information about the request.
     */
    public static void logRequest(HttpExchange exchange, String requestBody) {
        if (loggerLevel == LoggerLevel.DISABLED) {
            return;
        }

        // Create string for each detail
        String ip = exchange.getRemoteAddress().toString().replaceAll("[/\\[\\]]", "");
        String method = exchange.getRequestMethod();
        String route = exchange.getRequestURI().toString();
        String time = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());
        String headers = exchange.getRequestHeaders().entrySet().toString();
        requestBody = (requestBody == null || requestBody.isEmpty() ? "{  }" : requestBody);

        StringBuilder builder = new StringBuilder();
        switch (loggerLevel) {
            case DETAILED:
                builder.insert(0, String.format("tBody: %s%n",  requestBody));
            case INTERMEDIATE:
                builder.insert(0, String.format("\tHeaders: %s%n", headers));
            case BASIC:
                builder.insert(0, String.format("[REQUEST ] [%-25s %-13s %-36s] at %s%n", ip, method, route, time));
        }
        System.out.print(builder);
    }


    /**
     * Log an outgoing response from the server to a specified amount of detail
     * @param exchange The exchange containing information about the response.
     */
    public static void logResponse(HttpExchange exchange, String responseBody) {
        if (loggerLevel == LoggerLevel.DISABLED) {
            return;
        }

        // Create string for each detail
        String statusCode = String.valueOf(exchange.getResponseCode());
        String time = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());
        String contentType = exchange.getResponseHeaders().get("Content-Type").toString().replace("[", "").replace("]", "").strip();
        String headers = exchange.getResponseHeaders().entrySet().toString();
        String bytes = ((responseBody + headers).getBytes().length) + " Bytes";

        StringBuilder builder = new StringBuilder();
        switch (loggerLevel) {
            case DETAILED:
                builder.insert(0, String.format("tBody: %s%n",  responseBody));
            case INTERMEDIATE:
                builder.insert(0, String.format("\tHeaders: %s%n", headers));
            case BASIC:
                builder.insert(0, String.format("[RESPONSE] [%-25s %-13s %-36s] at %s%n", statusCode, bytes, contentType, time));
        }
        System.out.print(builder);
    }

    /**
     * General log statement
     * @param message The message that will be logged
     * @param level The level required for the message to be logged.
     */
    public static void log(String message, LoggerLevel level) {
        log("MESSAGE", message, level);
    }

    /**
     * Generall log statement
     * @param descriptor The type of message that will be logged
     * @param message The message that will be logged
     * @param level The level required for the message to be logged.
     */
    public static void log(String descriptor, String message, LoggerLevel level) {
        if (loggerLevel.compareTo(level) >= 0) {
            System.out.printf("[%-8s] %s%n", descriptor, message);
        }
    }

    /**
     * Log statement for exceptions
     * @param e Exception to log
     */
    public static void log(Exception e) {
        if (loggerLevel.compareTo(LoggerLevel.BASIC) >= 0) {
            e.printStackTrace(System.out);
        }
    }
}