# Delivery Management Backend

Backend cho hệ thống quản lý giao hàng nội thành tại Đà Nẵng, Việt Nam.

## Tổng Quan Dự Án

```
delivery-management-backend/
├── src/
│   ├── main/
│   │   ├── java/com/delivery/
│   │   │   ├── entity/               # JPA Entities with Spatial Support
│   │   │   ├── enums/                # DeliveryStatus, Priority
│   │   │   ├── repository/           # Spring Data JPA Repositories
│   │   │   ├── service/              # Business Logic
│   │   │   ├── config/               # Spring Configuration
│   │   │   └── controller/           # REST Controllers
│   │   └── resources/
│   │       └── application.yml       # Application Configuration
│   └── test/
│       └── java/com/delivery/        # Unit & Integration Tests
├── pom.xml                           # Maven Build Configuration
└── README.md
```

## Technology Stack

### Core
- **Java 17**
- **Spring Boot 3.2**
- **Spring Data JPA**
- **PostgreSQL 15+** with PostGIS

### Spatial
- **Hibernate Spatial** (ORM for geometry data)
- **JTS (Java Topology Suite)** (geometry operations)
- **PostGIS** (PostgreSQL spatial extension)

### Optimization & Caching
- **Caffeine Cache** (in-memory caching)
- **Genetic Algorithm** (route optimization)
- **Dynamic Insertion** (real-time order handling)

### Real-time
- **WebSocket (STOMP)**
- **Spring Messaging**

### External APIs
- **OSRM (Open Source Routing Machine)** (matrix distances/times)

## Phần 1: Entity Layer (HOÀN THÀNH)

### Entities

#### 1. OrderEntity
- Đại diện cho đơn hàng giao hàng
- **Spatial Column**: `location` (Point, SRID 4326)
- **Indexes**: GIST trên `geom` column cho spatial queries
- **Relationships**: 
  - `@ManyToOne` to ShipperEntity
  - `@ManyToOne` to RouteEntity
- **Time Windows**: `timeWindowStart`, `timeWindowEnd` (SLA constraints)

#### 2. ShipperEntity
- Đại diện cho tài xế giao hàng
- **Real-time Location**: `currentGeom` (Point, SRID 4326)
- **Speed Tracking**: `currentSpeed`, `smoothedSpeed` (EMA)
- **Relationships**:
  - `@OneToMany` to OrderEntity
  - `@OneToOne` to RouteEntity

#### 3. RouteEntity
- Đại diện cho lộ trình giao hàng
- **Order Sequence**: `@OrderColumn("stop_order")` duy trì thứ tự dừng
- **Polyline**: Encoded route geometry từ OSRM
- **Metrics**: `totalDurationSeconds`, `totalDistanceMeters`

### Enums
- **Priority**: NORMAL, URGENT
- **DeliveryStatus**: PENDING, ASSIGNED, PICKED_UP, DELIVERED, CANCELLED

## Tại Sao SRID 4326 (WGS84)?

1. **GPS Standard**: Latitude/Longitude in decimal degrees
2. **API Compatible**: Works with OSRM, Google Maps, OpenStreetMap
3. **PostGIS Native**: Optimized trong PostgreSQL spatial functions
4. **GIST Indexes**: Efficient range queries cho nearby deliveries
5. **Global Scope**: No projection distortion cho Da Nang

## Database Setup

### Prerequisites
```bash
# Cài đặt PostgreSQL 15+
sudo apt-get install postgresql postgresql-contrib

# Cài đặt PostGIS
sudo apt-get install postgresql-15-postgis-3
```

### Create Database & Enable PostGIS
```sql
CREATE DATABASE delivery_db;
\c delivery_db
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
```

### Verify PostGIS Installation
```sql
SELECT PostGIS_Version();
-- Output: POSTGIS="3.x.x" ...
```

## Build & Run

### Maven Build
```bash
mvn clean package
```

### Run Application
```bash
mvn spring-boot:run
```

### Access API
- Base URL: `http://localhost:8080/api`
- WebSocket: `ws://localhost:8080/api/ws-delivery`

## Phần Tiếp Theo

- **Phần 2**: Routing Engine (Genetic Algorithm, TSP Optimization)
- **Phần 3**: Dynamic Insertion & Rerouting
- **Phần 4**: ETA Engine with Adaptive Calculations
- **Phần 5**: WebSocket Real-time Broadcast

## Key Files

- `OrderEntity.java`: Delivery order với spatial location
- `ShipperEntity.java`: Driver với real-time GPS tracking
- `RouteEntity.java`: Optimized delivery route
- `SpatialConfig.java`: JTS GeometryFactory configuration
- `DatabaseConfig.java`: Hibernate Spatial setup
- `OrderRepository.java`: Spatial queries
- `ShipperRepository.java`: Nearest shipper queries
- `RouteRepository.java`: Route lifecycle queries

## License

MIT
