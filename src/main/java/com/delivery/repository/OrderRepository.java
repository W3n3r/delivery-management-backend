package com.delivery.repository;

import com.delivery.entity.OrderEntity;
import com.delivery.enums.DeliveryStatus;
import com.delivery.enums.Priority;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OrderEntity with custom spatial and temporal queries.
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    List<OrderEntity> findByStatus(DeliveryStatus status);

    List<OrderEntity> findByPriority(Priority priority);

    List<OrderEntity> findByShipperId(Long shipperId);

    List<OrderEntity> findByRouteId(Long routeId);

    /**
     * Find orders with pending status (PENDING or ASSIGNED).
     */
    List<OrderEntity> findByStatusIn(List<DeliveryStatus> statuses);

    /**
     * Find orders created within a time window.
     */
    List<OrderEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Spatial query: Find orders within a radius of a point.
     * Uses PostGIS ST_DWithin function for efficiency.
     * Distance in meters (SRID 4326 uses meter distances).
     */
    @Query("SELECT o FROM OrderEntity o WHERE " +
           "function('ST_DWithin', o.location, :point, :distanceMeters) = true")
    List<OrderEntity> findOrdersWithinRadius(
        @Param("point") Point point,
        @Param("distanceMeters") Double distanceMeters
    );

    /**
     * Spatial query: Find orders within a polygon area.
     * Uses PostGIS ST_Within function.
     */
    @Query("SELECT o FROM OrderEntity o WHERE " +
           "function('ST_Within', o.location, :polygon) = true")
    List<OrderEntity> findOrdersWithinArea(@Param("polygon") Object polygon);

    /**
     * Find unassigned orders with pending status.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.status = 'PENDING' AND o.shipper IS NULL")
    List<OrderEntity> findUnassignedOrders();

    /**
     * Find urgent orders that are not yet assigned.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.priority = 'URGENT' AND o.status = 'PENDING'")
    List<OrderEntity> findUrgentPendingOrders();

    /**
     * Count orders by status.
     */
    long countByStatus(DeliveryStatus status);

    /**
     * Find orders with time window expiring soon.
     */
    List<OrderEntity> findByTimeWindowEndBeforeAndStatusNot(
        LocalDateTime endTime,
        DeliveryStatus status
    );
}
