package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "catch-all" exception mapper that intercepts any unexpected runtime errors.
 * Prevents raw Java stack traces from being leaked to API consumers.
 * Maps all unhandled exceptions to HTTP 500 Internal Server Error.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace on the server side for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by GenericExceptionMapper", exception);

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", "An unexpected error occurred on the server. Please try again later.");
        errorBody.put("status", 500);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
