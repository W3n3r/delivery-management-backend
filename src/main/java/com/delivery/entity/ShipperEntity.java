package com.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sql.types.Types;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shipper Entity representing a delivery driver in the system.
 * 
 * Tracks shipper's current location (currentGeom), assigned route, and active orders.
 * currentGeom is updated in real-time via GPS tracking.
 */
@Entity
@Table(
    name = "shippers",
    indexes = {
        @Index(name = "idx_shipper_status", columnList = "status"),
        @Index(name = "idx_shipper_geom", columnList = "current_geom", unique = false)
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ShipperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String shipperCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(length = 50)
    private String vehicleNumber;

    @Column(length = 50)
    private String vehicleType;

    /**
     * Maximum capacity of vehicle in kilograms.
     */
    @Column(nullable = false)
    private Double capacityWeight = 50.0;

    /**
     * Current real-time location of shipper.
     * Updated via GPS tracking from mobile app.
     */
    @Column(name = "current_geom", columnDefinition = "geometry(Point,4326)")
    @JdbcTypeCode(Types.OTHER)
    private Point currentGeom;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    /**
     * Current speed of shipper in km/h.
     * Updated from GPS data. If < 5 km/h (considered stationary), use default 25 km/h.
     */
    @Column(name = "current_speed", nullable = false)
    private Double currentSpeed = 0.0;

    /**
     * Smoothed speed using Exponential Moving Average (EMA).
     * Prevents GPS jitter from causing erratic ETA updates.
     * Formula: S_new = α * S_gps + (1-α) * S_previous (α = 0.3)
     */
    @Column(name = "smoothed_speed")
    private Double smoothedSpeed = 0.0;

    /**
     * Shipper's current status: AVAILABLE, ON_DUTY, ON_BREAK, OFF_DUTY
     */
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE";

    /**
     * Total orders delivered in current shift.
     */
    @Column(nullable = false)
    private Integer ordersDelivered = 0;

    /**
     * Total distance traveled in current shift (km).
     */
    @Column(nullable = false)
    private Double totalDistanceTraveled = 0.0;

    /**
     * Total working time in current shift (minutes).
     */
    @Column(nullable = false)
    private Integer totalWorkingMinutes = 0;

    /**
     * Last GPS update timestamp.
     * Used to detect stale location data.
     */
    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    /**
     * Assignment timestamp for current route.
     */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Relationships
    /**
     * One-to-Many relationship: One Shipper can have many Orders.
     * A shipper can be assigned multiple orders in a route.
     */
    @OneToMany(
        mappedBy = "shipper",
        cascade = CascadeType.REFRESH,
        fetch = jakarta.persistence.FetchType.LAZY
    )
    private List<OrderEntity> orders = new ArrayList<>();

    /**
     * One-to-One relationship: One Shipper has one active Route.
     * When a route is completed, a new route can be assigned.
     */
    @OneToOne(
        mappedBy = "shipper",
        cascade = CascadeType.REFRESH,
        fetch = jakarta.persistence.FetchType.LAZY
    )
    private RouteEntity currentRoute;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
