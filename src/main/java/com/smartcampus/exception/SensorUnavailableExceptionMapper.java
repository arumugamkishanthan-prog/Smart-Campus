package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException to HTTP 403 Forbidden.
 * Triggered when attempting to post a reading to a sensor in MAINTENANCE status.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("error", "Forbidden");
        errorBody.put("message", exception.getMessage());
        errorBody.put("status", 403);

        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
