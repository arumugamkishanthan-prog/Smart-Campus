package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * JAX-RS Resource class managing the /api/v1/rooms collection.
 *
 * Regarding the JAX-RS resource lifecycle: By default, JAX-RS creates a new instance
 * of this resource class for every incoming request (per-request lifecycle). This means
 * instance fields are not shared across requests. To share state, we use the singleton
 * DataStore which is accessed via DataStore.getInstance(). Since DataStore uses
 * ConcurrentHashMap, it is thread-safe for concurrent access from multiple request threads.
 *
 * When returning a list of rooms, we return the full room objects rather than just IDs.
 * This reduces the number of round-trips the client needs to make (no need to follow up
 * with individual GET requests for each room). The trade-off is increased bandwidth usage,
 * but for this use case the room objects are small enough that this is acceptable.
 */
@Path("/rooms")
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(dataStore.getRooms().values());
        return Response.ok(rooms).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with a Location header pointing to the new resource.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        // Validate required fields
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Bad Request\",\"message\":\"Room ID is required.\",\"status\":400}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Check if room already exists
        if (dataStore.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Conflict\",\"message\":\"A room with ID '"
                            + room.getId() + "' already exists.\",\"status\":409}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        dataStore.addRoom(room);

        // Build the Location URI for the newly created resource
        URI createdUri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();

        return Response.created(createdUri).entity(room).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Not Found\",\"message\":\"Room with ID '"
                            + roomId + "' was not found.\",\"status\":404}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room. Blocks deletion if the room still has sensors assigned (409 Conflict).
     *
     * This DELETE operation is idempotent: if a client mistakenly sends the exact same
     * DELETE request multiple times, the first call removes the room and returns 204,
     * and subsequent calls also return 204 because the room no longer exists. The end
     * state is the same regardless of how many times the request is sent — the room
     * does not exist. This is consistent with the HTTP specification which states that
     * DELETE should be idempotent.
     */
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);

        // Idempotent: if room doesn't exist, return 204 (already deleted or never existed)
        if (room == null) {
            return Response.noContent().build();
        }

        // Business Logic Constraint: cannot delete a room with active sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }

        dataStore.removeRoom(roomId);
        return Response.noContent().build();
    }
}
