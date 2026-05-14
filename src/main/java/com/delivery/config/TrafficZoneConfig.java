package com.delivery.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * TrafficZoneConfig defines traffic zones in Da Nang.
 * 
 * High-traffic zones:
 * 1. Cầu Rồng (Dragon Bridge) - 16.0644°N, 107.5629°E
 * 2. Cầu Sông Hàn (Han River Bridge) - 16.0706°N, 107.5580°E
 * 3. Downtown area - around Hàng Dầu street
 * 4. Beach area (weekends)
 * 
 * Peak hours:
 * - Morning: 7:00 AM - 9:00 AM
 * - Evening: 5:00 PM - 7:00 PM
 * - Weekend: 9:00 AM - 6:00 PM
 * 
 * Traffic zones are represented as polygons (SRID 4326).
 * Each zone has a traffic multiplier (1.0 = normal, 1.5 = heavy).
 */
@Slf4j
@Service
public class TrafficZoneConfig {

    private final GeometryFactory geometryFactory;
    private final Map<String, TrafficZone> trafficZones;

    @Data
    public static class TrafficZone {
        private String name;
        private Polygon boundary; // Polygon in SRID 4326
        private double peakHourMultiplier;
        private double normalHourMultiplier;
        private int startPeakHour; // 0-23
        private int endPeakHour;
    }

    public TrafficZoneConfig() {
        this.geometryFactory = new GeometryFactory();
        this.trafficZones = new HashMap<>();
        initializeTrafficZones();
    }

    /**
     * Initialize traffic zones for Da Nang.
     * Zones defined as polygon geometries.
     */
    private void initializeTrafficZones() {
        // Zone 1: Cầu Rồng (Dragon Bridge)
        // Center: 16.0644°N, 107.5629°E
        TrafficZone dragonBridgeZone = new TrafficZone();
        dragonBridgeZone.setName("Dragon Bridge");
        dragonBridgeZone.setNormalHourMultiplier(1.2);
        dragonBridgeZone.setPeakHourMultiplier(1.8);
        dragonBridgeZone.setStartPeakHour(7);
        dragonBridgeZone.setEndPeakHour(9);

        // Create polygon around bridge (approximately 0.01 degree radius ≈ 1 km)
        Coordinate[] bridgeCoords = new Coordinate[]{
            new Coordinate(107.5529, 16.0544), // SW
            new Coordinate(107.5729, 16.0544), // SE
            new Coordinate(107.5729, 16.0744), // NE
            new Coordinate(107.5529, 16.0744), // NW
            new Coordinate(107.5529, 16.0544)  // Close ring
        };
        Polygon bridgePolygon = geometryFactory.createPolygon(bridgeCoords);
        dragonBridgeZone.setBoundary(bridgePolygon);
        trafficZones.put("dragon_bridge", dragonBridgeZone);

        log.info("Initialized {} traffic zones", trafficZones.size());
    }

    /**
     * Get traffic multiplier for a coordinate at a specific time.
     * 
     * @param latitude  GPS latitude (SRID 4326)
     * @param longitude GPS longitude (SRID 4326)
     * @param hour      Hour of day (0-23)
     * @return Traffic multiplier (1.0 = normal)
     */
    public double getTrafficMultiplier(double latitude, double longitude, int hour) {
        Coordinate point = new Coordinate(longitude, latitude);
        Geometry pointGeom = geometryFactory.createPoint(point);

        for (TrafficZone zone : trafficZones.values()) {
            if (zone.getBoundary().contains(pointGeom)) {
                // Check if in peak hours
                if (hour >= zone.getStartPeakHour() && hour < zone.getEndPeakHour()) {
                    return zone.getPeakHourMultiplier();
                } else {
                    return zone.getNormalHourMultiplier();
                }
            }
        }

        return 1.0; // Normal traffic
    }

    /**
     * Check if coordinate is in high-traffic zone.
     */
    public boolean isHighTrafficZone(double latitude, double longitude) {
        Coordinate point = new Coordinate(longitude, latitude);
        Geometry pointGeom = geometryFactory.createPoint(point);

        for (TrafficZone zone : trafficZones.values()) {
            if (zone.getBoundary().contains(pointGeom)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all traffic zones.
     */
    public Map<String, TrafficZone> getTrafficZones() {
        return trafficZones;
    }
}
