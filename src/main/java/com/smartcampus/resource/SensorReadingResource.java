package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Sub-Resource class for managing sensor readings.
 * This class is NOT annotated with @Path — it is instantiated by the sub-resource
 * locator method in SensorResource.
 *
 * Each instance is scoped to a specific sensor via the sensorId passed in the constructor.
 * This pattern cleanly separates the readings logic from the sensor management logic,
 * making the codebase more maintainable compared to defining every nested path
 * (sensors/{id}/readings/{rid}) in one massive controller class.
 */
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the historical log of all readings for this sensor.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> readings = dataStore.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to this sensor's history.
     *
     * Side Effect: Updates the parent Sensor's currentValue field to ensure
     * data consistency across the API. This means any subsequent GET on the
     * sensor will reflect the most recent reading value.
     *
     * State Constraint: Sensors in "MAINTENANCE" status cannot accept readings.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Not Found\",\"message\":\"Sensor with ID '"
                            + sensorId + "' was not found.\",\"status\":404}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // State Constraint: sensors in MAINTENANCE cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // Auto-generate ID and timestamp if not provided
        SensorReading newReading = SensorReading.createNew(reading.getValue());

        // Persist the reading
        dataStore.addReading(sensorId, newReading);

        // Side Effect: update the sensor's currentValue to reflect the latest reading
        sensor.setCurrentValue(newReading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}
