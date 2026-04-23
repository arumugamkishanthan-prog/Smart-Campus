package com.smartcampus.repository;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store for the Smart Campus API.
 * Uses ConcurrentHashMap for thread-safety as JAX-RS resource classes
 * are instantiated per-request by default.
 *
 * No database is used — all data lives in memory and is lost on server restart.
 */
public class DataStore {

    // Singleton instance
    private static final DataStore INSTANCE = new DataStore();

    // In-memory collections
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // Private constructor — singleton pattern
    private DataStore() {
        seedData();
    }

    /**
     * Returns the singleton instance of the DataStore.
     */
    public static DataStore getInstance() {
        return INSTANCE;
    }

    /**
     * Pre-seeds the data store with sample data for demonstration and testing.
     */
    private void seedData() {
        // Seed Rooms
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("ENG-102", "Engineering Lab", 30);
        Room room3 = new Room("SCI-201", "Science Lecture Hall", 120);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
        rooms.put(room3.getId(), room3);

        // Seed Sensors
        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "ACTIVE", 415.0, "LIB-301");
        Sensor sensor3 = new Sensor("OCC-001", "Occupancy", "ACTIVE", 25.0, "ENG-102");
        Sensor sensor4 = new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "ENG-102");

        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);
        sensors.put(sensor3.getId(), sensor3);
        sensors.put(sensor4.getId(), sensor4);

        // Link sensors to rooms
        room1.addSensorId("TEMP-001");
        room1.addSensorId("CO2-001");
        room2.addSensorId("OCC-001");
        room2.addSensorId("TEMP-002");

        // Seed some readings
        List<SensorReading> temp001Readings = new ArrayList<>();
        temp001Readings.add(new SensorReading("r1-temp001", System.currentTimeMillis() - 60000, 21.8));
        temp001Readings.add(new SensorReading("r2-temp001", System.currentTimeMillis() - 30000, 22.1));
        temp001Readings.add(new SensorReading("r3-temp001", System.currentTimeMillis(), 22.5));
        sensorReadings.put("TEMP-001", temp001Readings);

        List<SensorReading> co2001Readings = new ArrayList<>();
        co2001Readings.add(new SensorReading("r1-co2001", System.currentTimeMillis() - 45000, 410.0));
        co2001Readings.add(new SensorReading("r2-co2001", System.currentTimeMillis(), 415.0));
        sensorReadings.put("CO2-001", co2001Readings);
    }

    // ==================== Room Operations ====================

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room removeRoom(String id) {
        return rooms.remove(id);
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // ==================== Sensor Operations ====================

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public Sensor removeSensor(String id) {
        return sensors.remove(id);
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // ==================== SensorReading Operations ====================

    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
