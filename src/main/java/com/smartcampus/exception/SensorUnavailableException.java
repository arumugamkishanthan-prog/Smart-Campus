package com.smartcampus.exception;

public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' is currently in '" + status
                + "' state and cannot accept new readings. "
                + "The sensor must be in 'ACTIVE' status to record measurements.");
    }
}
