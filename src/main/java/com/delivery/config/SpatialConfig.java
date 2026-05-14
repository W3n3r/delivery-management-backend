package com.delivery.config;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for JTS (Java Topology Suite) geometry operations.
 * 
 * GeometryFactory is the primary factory for creating geometry objects.
 * SRID 4326 specifies WGS84 coordinate system.
 * 
 * Why SRID 4326 (WGS84)?:
 * - Standard GPS coordinate system (latitude, longitude in decimal degrees)
 * - Used by all major mapping APIs (Google Maps, OSRM, OpenStreetMap)
 * - Accurate for global positioning
 * - Easy conversion to/from decimal degrees
 * - Supports spatial queries with GIST indexes in PostGIS
 */
@Configuration
public class SpatialConfig {

    /**
     * Create a GeometryFactory for SRID 4326 (WGS84).
     * This factory is used to create Point, Polygon, and other geometry objects.
     * 
     * SRID 4326 coordinates are in format: Point(longitude, latitude)
     * Note: Longitude comes FIRST, then latitude!
     * This is standard for PostGIS and WGS84.
     */
    @Bean
    public GeometryFactory geometryFactory() {
        PrecisionModel precisionModel = new PrecisionModel();
        return new GeometryFactory(precisionModel, 4326);
    }
}
