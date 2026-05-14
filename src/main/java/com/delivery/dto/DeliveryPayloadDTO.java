package com.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeliveryPayloadDTO for batch delivery requests.
 * Used when creating multiple orders at once.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPayloadDTO {

    /**
     * List of order IDs to be delivered.
     */
    private List<Long> orderIds;

    /**
     * Shipper ID to assign route to.
     * If null, system will auto-select best shipper.
     */
    private Long shipperId;

    /**
     * Optional: Preferred start time for route.
     */
    private String preferredStartTime;

    /**
     * Number of GA iterations to perform.
     * Default: 1000
     */
    private Integer gaIterations = 1000;

    /**
     * Population size for GA.
     * Default: 100
     */
    private Integer populationSize = 100;
}
