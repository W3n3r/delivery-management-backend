package com.delivery.dto;

import com.delivery.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * CandidateSolution represents a chromosome in Genetic Algorithm.
 * Each solution is a permutation of delivery orders (a complete route).
 * 
 * GA Process:
 * 1. Initialize population with random solutions
 * 2. Calculate fitness for each solution
 * 3. Selection: Choose best solutions for breeding
 * 4. Crossover: Combine two parents to create offspring (OX1 for TSP)
 * 5. Mutation: Randomly swap adjacent stops to explore neighborhood
 * 6. Repeat for N generations until convergence
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSolution {

    /**
     * Order of delivery stops. Index 0 is depot (warehouse/start point).
     * Subsequent indices represent delivery order sequence.
     * 
     * Example: [0, 3, 1, 5, 2, 4, 0]
     * - Start at depot (0)
     * - Go to order 3 -> 1 -> 5 -> 2 -> 4
     * - Return to depot (0)
     */
    private List<OrderEntity> orders;

    /**
     * Total route distance in meters.
     * Calculated from OSRM distance matrix.
     */
    private Double totalDistance = 0.0;

    /**
     * Total route duration in seconds.
     * Includes travel time + service time at each delivery.
     */
    private Long totalDuration = 0L;

    /**
     * Fitness score for this solution.
     * Lower is better.
     * 
     * Fitness = distance_cost + time_cost + penalties
     * where:
     * - distance_cost = total_distance (in units)
     * - time_cost = total_duration (in seconds)
     * - penalties = time window violations + capacity violations + urgent order delays
     */
    private Double fitness = Double.MAX_VALUE;

    /**
     * Penalties accumulated for constraint violations.
     * 
     * Types of penalties:
     * 1. Time Window Violation: If delivery arrives after timeWindowEnd
     * 2. Capacity Violation: If total weight exceeds shipper capacity
     * 3. Urgent Order Penalty: If urgent order is delayed beyond threshold
     */
    private Double penalties = 0.0;

    /**
     * Count of time window violations in this route.
     */
    private Integer timeWindowViolations = 0;

    /**
     * Count of capacity violations in this route.
     */
    private Integer capacityViolations = 0;

    /**
     * Create a deep copy of this solution.
     * Used for offspring creation in GA crossover.
     */
    public CandidateSolution deepCopy() {
        return CandidateSolution.builder()
            .orders(new ArrayList<>(this.orders))
            .totalDistance(this.totalDistance)
            .totalDuration(this.totalDuration)
            .fitness(this.fitness)
            .penalties(this.penalties)
            .timeWindowViolations(this.timeWindowViolations)
            .capacityViolations(this.capacityViolations)
            .build();
    }

    /**
     * Check if this solution is feasible (no violations).
     */
    public boolean isFeasible() {
        return timeWindowViolations == 0 && capacityViolations == 0;
    }

    /**
     * Get route as encoded polyline string.
     * Used for visualization on map.
     * Placeholder - will be populated by OSRM.
     */
    private String routePolyline;

    public String getRoutePolyline() {
        return routePolyline;
    }

    public void setRoutePolyline(String polyline) {
        this.routePolyline = polyline;
    }
}
