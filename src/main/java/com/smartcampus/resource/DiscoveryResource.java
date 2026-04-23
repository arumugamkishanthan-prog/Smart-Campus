package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root "Discovery" endpoint for the Smart Campus API.
 * Provides essential API metadata including versioning info, administrative
 * contact details, and a map of primary resource collection URIs (HATEOAS).
 *
 * The provision of "Hypermedia" links is a hallmark of advanced RESTful design
 * because it allows clients to navigate the API dynamically without hardcoding
 * URLs, making the API self-describing and reducing coupling between client
 * and server. This is far superior to static documentation as the links are
 * always current and reflect the actual state of the API.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscovery(@Context UriInfo uriInfo) {
        String baseUri = uriInfo.getBaseUri().toString();

        Map<String, Object> discovery = new LinkedHashMap<>();
        discovery.put("name", "Smart Campus API");
        discovery.put("version", "1.0");
        discovery.put("description", "RESTful API for managing campus rooms, sensors, and sensor readings "
                + "as part of the university's Smart Campus initiative.");

        // Administrative contact details
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("department", "School of Computer Science and Engineering");
        contact.put("institution", "University of Westminster");
        discovery.put("contact", contact);

        // HATEOAS links to primary resource collections
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", baseUri + "rooms");
        links.put("sensors", baseUri + "sensors");
        discovery.put("links", links);

        return Response.ok(discovery).build();
    }
}
