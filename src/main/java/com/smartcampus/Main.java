package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

/**
 * Main entry point for the Smart Campus API server.
 * Uses Grizzly HTTP server with Jersey JAX-RS implementation.
 *
 * The ResourceConfig scans the com.smartcampus package and all sub-packages
 * to automatically discover and register:
 * - Resource classes (@Path annotated)
 * - Exception Mappers (@Provider annotated)
 * - Filters (@Provider annotated)
 */
public class Main {

    // Base URI — the API is served under /api/v1 as required
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    /**
     * Creates and starts the Grizzly HTTP server with Jersey configuration.
     */
    public static HttpServer startServer() {
        // ResourceConfig scans all sub-packages of com.smartcampus
        // This automatically registers resources, exception mappers, and filters
        ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus.resource",
                          "com.smartcampus.exception",
                          "com.smartcampus.filter");

        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = startServer();
        System.out.println("============================================");
        System.out.println("  Smart Campus API started successfully!");
        System.out.println("============================================");
        System.out.println("  Base URI:  " + BASE_URI);
        System.out.println("  Discovery: " + BASE_URI);
        System.out.println("  Rooms:     " + BASE_URI + "rooms");
        System.out.println("  Sensors:   " + BASE_URI + "sensors");
        System.out.println("============================================");
        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.shutdown();
    }
}