package app;


import app.db.Database;
import app.http.logger.Logger;
import app.http.Server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Main class.
 */
public class Main {

    public static final Properties properties;

    /**
     * Load the config on startup.
     */
    static {
        File config = new File("config.xml");
        Properties defaultProps = new Properties();
        Properties customProps = new Properties();

        if(config.isFile()) {
            try(FileInputStream fi = new FileInputStream(config)) {
                customProps.loadFromXML(fi);
            } catch (IOException e) {
                System.err.println("Error: Could not read custom config, using default instead");
                e.printStackTrace();
            }
        }

        try(InputStream is = Main.class.getResourceAsStream("/config.xml")) {
            defaultProps.loadFromXML(is);
        } catch (IOException e) {
            System.err.println("Error: Could not read default config, aborting");
            e.printStackTrace();
        }

        properties = defaultProps;
        properties.putAll(customProps);


        try(FileOutputStream fi = new FileOutputStream(config)) {
            properties.storeToXML(fi, null, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Starts the server with the preferred configurations.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger.setLevel(properties.getProperty("logger_level"));
        Server.start(Integer.parseInt(properties.getProperty("port")), new Database(Main.properties));
        // Implicitly ait for server to stop
    }
}
