package com.delivery.entity;

import com.delivery.enums.DeliveryStatus;
import com.delivery.enums.Priority;
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

/**
 * Order Entity representing a delivery order in the system.
 * 
 * Uses PostGIS for spatial operations on delivery location.
 * SRID 4326 is WGS84 (GPS coordinates: latitude/longitude in decimal degrees).
 * This is chosen because:
 * 1. Standard for GPS devices and mapping applications
 * 2. OSRM API uses WGS84 coordinates
 * 3. Allows direct use with Leaflet.js for web mapping
 * 4. Compatible with all major GIS systems (Google Maps, OpenStreetMap, etc.)
 */
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_shipper", columnList = "shipper_id"),
        @Index(name = "idx_order_route", columnList = "route_id"),
        @Index(name = "idx_order_geom", columnList = "geom", unique = false)
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhone;

    @Column(nullable = false, length = 255)
    private String deliveryAddress;

    /**
     * Spatial column storing delivery location as PostGIS Point geometry.
     * SRID 4326 = WGS84 (latitude, longitude in decimal degrees)
     * Index: GIST (Generalized Search Tree) for efficient spatial queries
     */
    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    @JdbcTypeCode(Types.OTHER)
    private Point location;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.NORMAL;

    /**
     * Time window for delivery (customer available hours).
     * Order must be delivered before this time to satisfy SLA.
     */
    @Column(nullable = false)
    private LocalDateTime timeWindowStart;

    @Column(nullable = false)
    private LocalDateTime timeWindowEnd;

    /**
     * Estimated arrival time at delivery location.
     * Updated dynamically by ETA Engine based on shipper's current location and traffic.
     */
    @Column(name = "estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    /**
     * Actual delivery time (populated when order is marked as DELIVERED).
     */
    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    /**
     * Notes or special instructions for the delivery.
     */
    @Column(name = "delivery_notes", length = 500)
    private String deliveryNotes;

    /**
     * Package weight in kilograms.
     */
    @Column(nullable = false)
    private Double weight = 0.0;

    /**
     * Package value for insurance purposes.
     */
    @Column(nullable = false)
    private Double value = 0.0;

    /**
     * Service time at delivery location (in minutes).
     * Default 5 minutes for unloading and confirmation.
     */
    @Column(nullable = false)
    private Integer serviceTime = 5;

    // Relationships
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "shipper_id", nullable = true)
    private ShipperEntity shipper;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = true)
    private RouteEntity route;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
