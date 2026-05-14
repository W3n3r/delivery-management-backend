package com.delivery.service;

import com.delivery.dto.OSRMMatrixResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OSRMClient for calling OSRM (Open Source Routing Machine) API.
 * 
 * OSRM provides:
 * - Distance Matrix: distances and durations between all pairs of locations
 * - Route polylines: encoded route geometry
 * 
 * OSRM Instance: http://router.project-osrm.org/
 * Local instance can be set up with Docker: `docker run -p 5000:5000 osrm/osrm-backend`
 */
@Slf4j
@Service
@AllArgsConstructor
public class OSRMClient {

    private final WebClient webClient;

    /**
     * OSRM API base URL.
     * For production, should be configurable via application.yml
     */
    private static final String OSRM_API_URL = "http://router.project-osrm.org";

    /**
     * Get distance matrix between multiple locations.
     * 
     * OSRM Matrix API:
     * GET /matrix/v1/driving/lng1,lat1;lng2,lat2;...;lngN,latN
     * 
     * Response contains:
     * - distances[i][j]: distance in meters from location i to j
     * - durations[i][j]: duration in seconds from location i to j
     * 
     * @param coordinates List of [longitude, latitude] pairs
     * @return OSRMMatrixResponse with distance and duration matrices
     */
    @Cacheable(value = "distance_matrix", key = "#coordinates.hashCode()")
    public OSRMMatrixResponse getDistanceMatrix(List<double[]> coordinates) {
        try {
            // Build coordinate string: lng1,lat1;lng2,lat2;...
            String coordinateString = coordinates.stream()
                .map(coord -> coord[0] + "," + coord[1])
                .collect(Collectors.joining(";"));

            String url = String.format("%s/matrix/v1/driving/%s", OSRM_API_URL, coordinateString);

            log.info("Calling OSRM Matrix API: {}", url);

            OSRMMatrixResponse response = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(OSRMMatrixResponse.class)
                .block();

            if (response != null && response.isValid()) {
                log.info("OSRM Matrix API response received: {} locations", coordinates.size());
                return response;
            } else {
                log.error("OSRM Matrix API returned invalid response");
                return null;
            }

        } catch (Exception e) {
            log.error("Error calling OSRM Matrix API", e);
            return null;
        }
    }

    /**
     * Get route polyline between multiple waypoints.
     * 
     * OSRM Route API:
     * GET /route/v1/driving/lng1,lat1;lng2,lat2;...;lngN,latN?overview=full
     * 
     * @param coordinates List of waypoints
     * @return Encoded polyline string
     */
    @Cacheable(value = "route_polyline", key = "#coordinates.hashCode()")
    public String getRoutePolyline(List<double[]> coordinates) {
        try {
            String coordinateString = coordinates.stream()
                .map(coord -> coord[0] + "," + coord[1])
                .collect(Collectors.joining(";"));

            String url = String.format("%s/route/v1/driving/%s?overview=full", OSRM_API_URL, coordinateString);

            log.info("Calling OSRM Route API: {}", url);

            String response = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            log.info("OSRM Route API response received");
            return response;

        } catch (Exception e) {
            log.error("Error calling OSRM Route API", e);
            return null;
        }
    }
}
