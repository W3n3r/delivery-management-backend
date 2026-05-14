package com.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Individual represents a single member in GA population.
 * Similar to CandidateSolution but used within GA context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Individual {

    /**
     * Chromosome: Array of order indices representing route sequence.
     * Example: [0, 3, 1, 5, 2, 4, 0]
     */
    private int[] chromosome;

    /**
     * Fitness value for this individual.
     * Lower is better (cost-minimization).
     */
    private Double fitness;

    /**
     * Create a copy of this individual.
     */
    public Individual copy() {
        return Individual.builder()
            .chromosome(this.chromosome.clone())
            .fitness(this.fitness)
            .build();
    }
}
