package com.delivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for JPA, Hibernate Spatial, and PostgreSQL/PostGIS.
 * 
 * Key Points:
 * 1. Hibernate Spatial automatically detects PostGIS through JDBC driver
 * 2. SRID 4326 (WGS84) is used for all geometry columns
 * 3. GIST indexes on geometry columns for efficient spatial queries
 * 4. @OrderColumn is used for maintaining sequence in @OneToMany relationships
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.delivery.repository"
)
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuration handled via application.yml
}
