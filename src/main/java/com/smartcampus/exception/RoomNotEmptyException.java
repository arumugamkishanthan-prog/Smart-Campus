package com.smartcampus.exception;

/**
 * Thrown when a client attempts to delete a Room that still has active sensors assigned to it.
 * Maps to HTTP 409 Conflict.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }

    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Cannot delete room '" + roomId + "' because it still has "
                + sensorCount + " active sensor(s) assigned to it. "
                + "Please reassign or remove all sensors before deleting this room.");
    }
}
