package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS filter that logs every incoming HTTP request and outgoing HTTP response.
 * Implements both ContainerRequestFilter and ContainerResponseFilter for
 * cross-cutting logging concerns without polluting individual resource methods.
 *
 * Using filters for cross-cutting concerns like logging is advantageous because:
 * - It follows the Single Responsibility Principle
 * - It avoids duplicating Logger.info() calls in every resource method
 * - It is automatically applied to all endpoints without manual registration
 * - It can be enabled/disabled in one place
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Logs the HTTP method and URI of every incoming request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("REQUEST: " + requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri());
    }

    /**
     * Logs the HTTP status code of every outgoing response.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("RESPONSE: " + requestContext.getMethod() + " "
                + requestContext.getUriInfo().getRequestUri()
                + " -> " + responseContext.getStatus() + " " + responseContext.getStatusInfo());
    }
}
