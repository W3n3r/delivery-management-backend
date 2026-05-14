package com.delivery.repository;

import com.delivery.entity.ShipperEntity;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ShipperEntity with spatial location queries.
 */
@Repository
public interface ShipperRepository extends JpaRepository<ShipperEntity, Long> {

    Optional<ShipperEntity> findByShipperCode(String shipperCode);

    Optional<ShipperEntity> findByPhone(String phone);

    List<ShipperEntity> findByStatus(String status);

    /**
     * Find available shippers (not currently on duty).
     */
    List<ShipperEntity> findByStatusAndCurrentGeomIsNotNull(String status);

    /**
     * Spatial query: Find shippers within a radius of a point.
     * Used to find nearest available shipper for dynamic order assignment.
     */
    @Query("SELECT s FROM ShipperEntity s WHERE " +
           "function('ST_DWithin', s.currentGeom, :point, :distanceMeters) = true " +
           "AND s.status = :status " +
           "ORDER BY function('ST_Distance', s.currentGeom, :point) ASC")
    List<ShipperEntity> findNearestAvailableShippers(
        @Param("point") Point point,
        @Param("distanceMeters") Double distanceMeters,
        @Param("status") String status
    );

    /**
     * Find shippers with location data older than specified minutes.
     * Used to detect stale GPS data.
     */
    @Query("SELECT s FROM ShipperEntity s WHERE " +
           "DATEDIFF(MINUTE, s.lastLocationUpdate, CURRENT_TIMESTAMP) > :minutes")
    List<ShipperEntity> findShippersWithStaleLocation(@Param("minutes") int minutes);

    /**
     * Count available shippers in the system.
     */
    long countByStatus(String status);
}
