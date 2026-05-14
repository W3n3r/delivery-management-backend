package com.delivery.service;

import com.delivery.dto.CandidateSolution;
import com.delivery.dto.OSRMMatrixResponse;
import com.delivery.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * RoutingEngine implements Genetic Algorithm for TSP (Traveling Salesman Problem).
 * 
 * Algorithm Flow:
 * 1. Initialize population with random solutions
 * 2. Calculate fitness for each solution
 * 3. Selection: Tournament selection of best solutions
 * 4. Crossover: OX1 (Order Crossover) for TSP
 * 5. Mutation: Swap mutation to explore neighborhood
 * 6. Repeat for N generations until convergence
 * 
 * Key Parameters:
 * - Population Size: 100
 * - Generations: 1000
 * - Crossover Rate: 0.8
 * - Mutation Rate: 0.1
 * - Tournament Size: 5
 */
@Slf4j
@Service
@AllArgsConstructor
public class RoutingEngine {

    private final CostCalculator costCalculator;
    private final OSRMClient osrmClient;

    private static final Random RANDOM = new Random();
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 1000;
    private static final double CROSSOVER_RATE = 0.8;
    private static final double MUTATION_RATE = 0.1;
    private static final int TOURNAMENT_SIZE = 5;

    /**
     * Solve TSP using Genetic Algorithm.
     * 
     * @param orders List of delivery orders
     * @param distanceMatrix OSRM distance matrix
     * @return Best route found by GA
     */
    public CandidateSolution solveTSPGenetic(List<OrderEntity> orders, OSRMMatrixResponse distanceMatrix) {
        log.info("Starting GA optimization for {} orders", orders.size());

        // Step 1: Initialize population
        List<CandidateSolution> population = initializePopulation(orders, POPULATION_SIZE);

        // Step 2: GA iterations
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // Calculate fitness for all solutions
            for (CandidateSolution solution : population) {
                costCalculator.calculateFitness(solution, distanceMatrix);
            }

            // Sort by fitness (ascending - lower is better)
            population.sort((a, b) -> a.getFitness().compareTo(b.getFitness()));

            // Log progress every 100 generations
            if (generation % 100 == 0) {
                log.info("Generation {}: Best fitness = {}", generation, population.get(0).getFitness());
            }

            // Check convergence (if best solution hasn't improved for 50 generations)
            if (generation > 50 && hasConverged(population)) {
                log.info("GA converged at generation {}", generation);
                break;
            }

            // Step 3: Create new population through selection, crossover, mutation
            List<CandidateSolution> newPopulation = new ArrayList<>();

            // Elitism: Keep top 10% of population
            int eliteSize = Math.max(1, POPULATION_SIZE / 10);
            for (int i = 0; i < eliteSize; i++) {
                newPopulation.add(population.get(i).deepCopy());
            }

            // Fill rest of population with offspring
            while (newPopulation.size() < POPULATION_SIZE) {
                // Selection: Tournament selection
                CandidateSolution parent1 = tournamentSelection(population, TOURNAMENT_SIZE);
                CandidateSolution parent2 = tournamentSelection(population, TOURNAMENT_SIZE);

                // Crossover
                CandidateSolution offspring;
                if (RANDOM.nextDouble() < CROSSOVER_RATE) {
                    offspring = ox1Crossover(parent1, parent2);
                } else {
                    offspring = parent1.deepCopy();
                }

                // Mutation
                if (RANDOM.nextDouble() < MUTATION_RATE) {
                    mutateSwap(offspring);
                }

                newPopulation.add(offspring);
            }

            population = newPopulation;
        }

        // Return best solution
        population.sort((a, b) -> a.getFitness().compareTo(b.getFitness()));
        return population.get(0);
    }

    /**
     * Initialize population with random solutions.
     */
    private List<CandidateSolution> initializePopulation(List<OrderEntity> orders, int populationSize) {
        List<CandidateSolution> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            // Create random permutation of orders
            List<OrderEntity> randomOrders = new ArrayList<>(orders);
            Collections.shuffle(randomOrders);

            CandidateSolution solution = CandidateSolution.builder()
                .orders(randomOrders)
                .totalDistance(0.0)
                .totalDuration(0L)
                .fitness(Double.MAX_VALUE)
                .penalties(0.0)
                .timeWindowViolations(0)
                .capacityViolations(0)
                .build();

            population.add(solution);
        }

        return population;
    }

    /**
     * Tournament selection: randomly select tournamentSize individuals
     * and return the one with best fitness.
     */
    private CandidateSolution tournamentSelection(List<CandidateSolution> population, int tournamentSize) {
        CandidateSolution best = null;

        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = RANDOM.nextInt(population.size());
            CandidateSolution candidate = population.get(randomIndex);

            if (best == null || candidate.getFitness() < best.getFitness()) {
                best = candidate;
            }
        }

        return best;
    }

    /**
     * OX1 (Order Crossover) for TSP.
     * 
     * Process:
     * 1. Select two random crossover points
     * 2. Copy segment between points from parent1 to offspring
     * 3. Fill remaining positions with genes from parent2 in order
     */
    private CandidateSolution ox1Crossover(CandidateSolution parent1, CandidateSolution parent2) {
        List<OrderEntity> orders1 = parent1.getOrders();
        List<OrderEntity> orders2 = parent2.getOrders();
        int size = orders1.size();

        // Select random crossover points
        int point1 = RANDOM.nextInt(size);
        int point2 = RANDOM.nextInt(size);
        if (point1 > point2) {
            int temp = point1;
            point1 = temp;
            point2 = temp;
        }

        // Create offspring
        List<OrderEntity> offspring = new ArrayList<>(Collections.nCopies(size, null));

        // Copy segment from parent1
        for (int i = point1; i < point2; i++) {
            offspring.set(i, orders1.get(i));
        }

        // Fill remaining from parent2
        int offspringIndex = point2;
        for (int parentIndex = point2; parentIndex < size + point2; parentIndex++) {
            int p2Index = parentIndex % size;
            OrderEntity gene = orders2.get(p2Index);

            if (!offspring.contains(gene)) {
                offspring.set(offspringIndex % size, gene);
                offspringIndex++;
            }
        }

        return CandidateSolution.builder()
            .orders(offspring)
            .totalDistance(0.0)
            .totalDuration(0L)
            .fitness(Double.MAX_VALUE)
            .penalties(0.0)
            .timeWindowViolations(0)
            .capacityViolations(0)
            .build();
    }

    /**
     * Swap mutation: randomly swap two adjacent orders.
     */
    private void mutateSwap(CandidateSolution solution) {
        List<OrderEntity> orders = solution.getOrders();
        int index1 = RANDOM.nextInt(orders.size());
        int index2 = RANDOM.nextInt(orders.size());

        // Swap
        OrderEntity temp = orders.get(index1);
        orders.set(index1, orders.get(index2));
        orders.set(index2, temp);
    }

    /**
     * Check if population has converged.
     * Convergence: best solution hasn't improved significantly.
     */
    private boolean hasConverged(List<CandidateSolution> population) {
        // Simple check: if all solutions have similar fitness
        double bestFitness = population.get(0).getFitness();
        double avgFitness = population.stream()
            .mapToDouble(CandidateSolution::getFitness)
            .average()
            .orElse(Double.MAX_VALUE);

        return (avgFitness - bestFitness) / bestFitness < 0.01; // Less than 1% variance
    }
}
