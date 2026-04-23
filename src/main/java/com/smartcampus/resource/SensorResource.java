package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-RS Resource class managing the /api/v1/sensors collection.
 *
 * We use @Consumes(MediaType.APPLICATION_JSON) on the POST method to explicitly
 * declare the expected content type. If a client sends data in a different format
 * (e.g., text/plain or application/xml), JAX-RS will automatically return a
 * 415 Unsupported Media Type response without the request even reaching our method.
 * This provides built-in content negotiation and input validation.
 *
 * For filtering, we use @QueryParam("type") rather than encoding the type in the URL
 * path (e.g., /sensors/type/CO2) because:
 * - Query parameters are the standard way to filter/search collections in REST
 * - The path /sensors/type/CO2 implies "type" is a sub-resource, which is semantically wrong
 * - Query parameters can be optional, combined, and extended without changing the URL structure
 * - Multiple filters can be easily added: ?type=CO2&status=ACTIVE
 */
@Path("/sensors")
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * Returns all sensors, optionally filtered by type.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(dataStore.getSensors().values());

        // Apply type filter if provided
        if (type != null && !type.trim().isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the referenced roomId exists.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Validate required fields
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Bad Request\",\"message\":\"Sensor ID is required.\",\"status\":400}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Check if sensor already exists
        if (dataStore.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Conflict\",\"message\":\"A sensor with ID '"
                            + sensor.getId() + "' already exists.\",\"status\":409}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validate that the referenced room exists (dependency validation)
        if (sensor.getRoomId() == null || !dataStore.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // Add the sensor to the data store
        dataStore.addSensor(sensor);

        // Link the sensor to its room
        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room != null) {
            room.addSensorId(sensor.getId());
        }

        // Build the Location URI for the newly created resource
        URI createdUri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();

        return Response.created(createdUri).entity(sensor).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Retrieves a specific sensor by its ID.
     */
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Not Found\",\"message\":\"Sensor with ID '"
                            + sensorId + "' was not found.\",\"status\":404}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * Sub-Resource Locator for sensor readings.
     * Delegates handling of /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     *
     * This is a Sub-Resource Locator because it has @Path but NO HTTP method annotation.
     * JAX-RS will call this method to get an instance of SensorReadingResource, then
     * dispatch the request to the appropriate method on that sub-resource.
     *
     * Benefits of the Sub-Resource Locator pattern:
     * - Separates concerns: readings logic is in its own class
     * - Reduces complexity in this class
     * - Allows SensorReadingResource to be independently maintained and tested
     * - Scales better for large APIs with deep nesting
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsSubResource(@PathParam("sensorId") String sensorId) {
        // Validate that the sensor exists before delegating
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new javax.ws.rs.NotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
