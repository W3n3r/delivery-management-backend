package com.delivery.service;

import com.delivery.dto.CandidateSolution;
import com.delivery.dto.OSRMMatrixResponse;
import com.delivery.entity.OrderEntity;
import com.delivery.entity.ShipperEntity;
import com.delivery.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * CostCalculator computes fitness scores for candidate solutions.
 * 
 * Fitness Formula:
 * Fitness = Distance_Cost + Time_Cost + Penalties
 * 
 * where:
 * - Distance_Cost = total_distance / 1000 (kilometers)
 * - Time_Cost = total_duration / 60 (minutes)
 * - Penalties = time_window_violations * 1000 + capacity_violations * 5000 + urgent_delays * 2000
 */
@Slf4j
@Service
@AllArgsConstructor
public class CostCalculator {

    // Cost weights
    private static final double DISTANCE_WEIGHT = 1.0;        // Cost per km
    private static final double TIME_WEIGHT = 0.5;            // Cost per minute
    private static final double TIME_WINDOW_PENALTY = 1000.0; // Penalty per violation
    private static final double CAPACITY_PENALTY = 5000.0;    // Penalty per violation
    private static final double URGENT_DELAY_PENALTY = 2000.0; // Penalty per minute delayed

    /**
     * Calculate fitness score for a candidate solution.
     * 
     * @param solution Candidate solution to evaluate
     * @param distanceMatrix OSRM distance/duration matrix
     */
    public void calculateFitness(CandidateSolution solution, OSRMMatrixResponse distanceMatrix) {
        if (solution.getOrders() == null || solution.getOrders().isEmpty()) {
            solution.setFitness(Double.MAX_VALUE);
            return;
        }

        // Initialize metrics
        double totalDistance = 0.0;
        long totalDuration = 0;
        double penalties = 0.0;
        int timeWindowViolations = 0;
        int capacityViolations = 0;

        // Calculate route metrics from distance matrix
        for (int i = 0; i < solution.getOrders().size() - 1; i++) {
            // Get distance and duration from matrix
            totalDistance += distanceMatrix.getDistance(i, i + 1);
            totalDuration += distanceMatrix.getDuration(i, i + 1).longValue();
        }

        // Check time window constraints
        long currentTime = 0;
        for (OrderEntity order : solution.getOrders()) {
            currentTime += (currentTime == 0 ? 0 : distanceMatrix.getDuration(0, 1).longValue());

            if (order.getTimeWindowEnd() != null) {
                long windowEndSeconds = order.getTimeWindowEnd().getTime();
                if (currentTime > windowEndSeconds) {
                    timeWindowViolations++;
                    penalties += TIME_WINDOW_PENALTY * (currentTime - windowEndSeconds) / 60.0;
                }
            }
        }

        // Check urgent order delays
        for (OrderEntity order : solution.getOrders()) {
            if (Priority.URGENT.equals(order.getPriority())) {
                // If urgent order is not in first 3 positions, apply penalty
                if (solution.getOrders().indexOf(order) > 3) {
                    penalties += URGENT_DELAY_PENALTY;
                }
            }
        }

        // Calculate fitness
        double distanceCost = (totalDistance / 1000.0) * DISTANCE_WEIGHT;
        double timeCost = (totalDuration / 60.0) * TIME_WEIGHT;
        double fitness = distanceCost + timeCost + penalties;

        // Update solution
        solution.setTotalDistance(totalDistance);
        solution.setTotalDuration(totalDuration);
        solution.setPenalties(penalties);
        solution.setTimeWindowViolations(timeWindowViolations);
        solution.setCapacityViolations(capacityViolations);
        solution.setFitness(fitness);
    }

    /**
     * Calculate additional costs for traffic zones.
     * Different areas have different traffic patterns.
     */
    public double getTrafficCost(double latitude, double longitude, LocalDateTime time) {
        // TODO: Implement traffic zone lookup
        // High traffic zones: Cầu Rồng (16.0644°N, 107.5629°E), Cầu Sông Hàn
        // Apply higher costs during peak hours (7-9 AM, 5-7 PM)
        return 1.0; // Default multiplier
    }
}
