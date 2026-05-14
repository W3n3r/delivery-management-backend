package com.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RouteResponse DTO for API responses.
 * Contains route information returned to client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse {

    /**
     * Unique route identifier.
     */
    private Long routeId;

    /**
     * Route code.
     */
    private String routeCode;

    /**
     * Shipper assigned to this route.
     */
    private String shipperName;

    /**
     * Shipper ID.
     */
    private Long shipperId;

    /**
     * Total distance in meters.
     */
    private Double totalDistanceMeters;

    /**
     * Total duration in seconds.
     */
    private Long totalDurationSeconds;

    /**
     * Total number of orders in route.
     */
    private Integer totalOrders;

    /**
     * Number of completed orders.
     */
    private Integer completedOrders;

    /**
     * Route progress percentage.
     */
    private Double progressPercentage;

    /**
     * Encoded polyline for map visualization.
     */
    private String routePolyline;

    /**
     * Fitness score (lower is better).
     */
    private Double fitnessScore;

    /**
     * Route status: PLANNED, ACTIVE, COMPLETED, CANCELLED.
     */
    private String status;

    /**
     * Estimated completion time (ISO 8601).
     */
    private String estimatedCompletionTime;
}
