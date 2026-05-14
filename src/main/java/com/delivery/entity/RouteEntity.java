package com.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Route Entity representing a delivery route assigned to a shipper.
 * 
 * A route contains an ordered list of deliveries, spatial polyline representation,
 * and logistics metrics (duration, distance).
 */
@Entity
@Table(
    name = "routes",
    indexes = {
        @Index(name = "idx_route_shipper", columnList = "shipper_id"),
        @Index(name = "idx_route_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String routeCode;

    /**
     * Route status: PLANNED, ACTIVE, COMPLETED, CANCELLED
     */
    @Column(nullable = false, length = 20)
    private String status = "PLANNED";

    /**
     * Ordered list of deliveries in this route.
     * @OrderColumn ensures the stopOrder is persisted in database.
     * This index (stopOrder) defines the sequence of delivery stops.
     * 
     * JPA Mapping:
     * - @OneToMany creates a Join Table or FK reference
     * - @OrderColumn(name = "stop_order") persists the list index
     * - Database: orders table will have 'stop_order' column
     */
    @OneToMany(
        mappedBy = "route",
        cascade = CascadeType.REFRESH,
        fetch = jakarta.persistence.FetchType.LAZY,
        orphanRemoval = false
    )
    @OrderColumn(name = "stop_order")
    private List<OrderEntity> orders = new ArrayList<>();

    /**
     * Encoded polyline string from OSRM (Open Source Routing Machine).
     * Represents the complete route path from start to end.
     * 
     * Polyline Algorithm:
     * - Reduces coordinate storage: instead of [lat,lng] pairs, encodes as string
     * - ~5x compression compared to raw coordinates
     * - Example: "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
     * 
     * Decoding (JavaScript example):
     *   function decodePolyline(encoded) {
     *     const points = [];
     *     let index = 0, lat = 0, lng = 0;
     *     while (index < encoded.length) {
     *       let result = 0, shift = 0;
     *       let b;
     *       do {
     *         b = encoded.charCodeAt(index++) - 63;
     *         result |= (b & 0x1f) << shift;
     *         shift += 5;
     *       } while (b >= 0x20);
     *       lat += (result & 1) ? ~(result >> 1) : (result >> 1);
     *       ...
     *     }
     *   }
     */
    @Column(name = "route_polyline", columnDefinition = "TEXT")
    private String routePolyline;

    /**
     * Total estimated duration of route in seconds.
     * Calculated by GA and updated by Dynamic Insertion algorithm.
     */
    @Column(nullable = false)
    private Long totalDurationSeconds = 0L;

    /**
     * Total distance of route in meters.
     * Provided by OSRM matrix calculation.
     */
    @Column(nullable = false)
    private Double totalDistanceMeters = 0.0;

    /**
     * Start point coordinates (pickup/dispatch location).
     * Usually the warehouse or depot.
     */
    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    /**
     * End point coordinates (usually same as start for loop routes).
     */
    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    /**
     * Scheduled start time for this route.
     */
    @Column(name = "scheduled_start_time")
    private LocalDateTime scheduledStartTime;

    /**
     * Actual start time when shipper begins route execution.
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * Scheduled end time (based on estimated duration).
     */
    @Column(name = "scheduled_end_time")
    private LocalDateTime scheduledEndTime;

    /**
     * Actual end time when all deliveries completed.
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * Number of orders completed in this route.
     */
    @Column(nullable = false)
    private Integer completedOrders = 0;

    /**
     * Total number of orders assigned to this route.
     */
    @Column(nullable = false)
    private Integer totalOrders = 0;

    /**
     * GA fitness value for this route.
     * Lower is better (represents cost: time + penalties).
     */
    @Column(name = "fitness_value")
    private Double fitnessValue;

    // Relationships
    /**
     * One-to-One relationship: One Route is assigned to one Shipper.
     * A shipper can have multiple routes, but only one active at a time.
     */
    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "shipper_id", unique = true, nullable = false)
    private ShipperEntity shipper;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate estimated completion time based on current progress.
     */
    public LocalDateTime getEstimatedCompletionTime() {
        if (actualStartTime == null || totalDurationSeconds == null) {
            return scheduledEndTime;
        }
        return actualStartTime.plusSeconds(totalDurationSeconds);
    }

    /**
     * Get progress percentage of route completion.
     */
    public Double getProgressPercentage() {
        if (totalOrders == 0) return 0.0;
        return (double) completedOrders / totalOrders * 100;
    }
}
