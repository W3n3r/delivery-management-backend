package com.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OSRM Matrix API Response.
 * Contains distance and duration matrices between multiple locations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OSRMMatrixResponse {

    /**
     * Response code: "Ok" for successful request
     */
    private String code;

    /**
     * 2D array of distances in meters between all pairs of locations.
     * distances[i][j] = distance from location i to location j
     */
    private Double[][] distances;

    /**
     * 2D array of durations in seconds between all pairs of locations.
     * durations[i][j] = duration from location i to location j
     */
    private Double[][] durations;

    /**
     * List of locations used in the request.
     * Matched coordinates returned by OSRM.
     */
    private Double[][] matchedCoordinates;

    /**
     * Check if response is valid
     */
    public boolean isValid() {
        return "Ok".equals(code) && distances != null && durations != null;
    }

    /**
     * Get distance between two locations in meters
     */
    public Double getDistance(int fromIndex, int toIndex) {
        if (distances == null || fromIndex < 0 || toIndex < 0 ||
            fromIndex >= distances.length || toIndex >= distances[0].length) {
            return Double.MAX_VALUE;
        }
        return distances[fromIndex][toIndex];
    }

    /**
     * Get duration between two locations in seconds
     */
    public Double getDuration(int fromIndex, int toIndex) {
        if (durations == null || fromIndex < 0 || toIndex < 0 ||
            fromIndex >= durations.length || toIndex >= durations[0].length) {
            return Double.MAX_VALUE;
        }
        return durations[fromIndex][toIndex];
    }
}
