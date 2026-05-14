package com.delivery.repository;

import com.delivery.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RouteEntity with route lifecycle queries.
 */
@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

    Optional<RouteEntity> findByRouteCode(String routeCode);

    Optional<RouteEntity> findByShipperId(Long shipperId);

    List<RouteEntity> findByStatus(String status);

    /**
     * Find active routes (currently being executed).
     */
    List<RouteEntity> findByStatusAndActualStartTimeIsNotNull(String status);

    /**
     * Find routes with planned status.
     */
    List<RouteEntity> findByStatusAndScheduledStartTimeBetween(
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    /**
     * Find completed routes for analytics.
     */
    List<RouteEntity> findByStatusAndActualEndTimeBetween(
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    /**
     * Count routes by status.
     */
    long countByStatus(String status);

    /**
     * Get routes that need rerouting (stale or significantly deviated).
     */
    @Query("SELECT r FROM RouteEntity r WHERE r.status = :status " +
           "AND r.actualStartTime IS NOT NULL " +
           "AND DATEDIFF(MINUTE, r.actualStartTime, CURRENT_TIMESTAMP) > :maxMinutes")
    List<RouteEntity> findRoutesNeedingReOptimization(
        @Param("status") String status,
        @Param("maxMinutes") int maxMinutes
    );
}
