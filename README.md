# test-spring-boot-app

Application for various test scenarios with Kubernetes deployment via Flux OCI Repository.

## Features

- Spring Boot 3.2.0 with Java 21
- Built-in monitoring with Actuator and Prometheus metrics
- OpenTelemetry tracing support
- H2 in-memory database for testing
- OpenAPI/Swagger documentation
- Graceful shutdown and health checks

## Build and Deployment

This project uses GitHub Actions to build OCI images and Kubernetes manifests, then pushes them to GitHub Container Registry (GHCR). The Kubernetes manifests are packaged as OCI artifacts and deployed using Flux.

### Prerequisites

1. **GitHub Repository Settings**:
   - Enable GitHub Actions
   - Ensure `GITHUB_TOKEN` has package write permissions

2. **Kubernetes Cluster with Flux**:
   - Flux v2 installed and configured

### Automatic Deployment Flow

1. **Code Push/Tag**: Push to `main` branch or create a version tag (e.g., `v1.0.0`)
2. **GitHub Actions**: Automatically builds and pushes:
   - Application OCI image: `ghcr.io/k8s-lovers-korea/test-spring-boot-app`
   - Kubernetes manifests OCI artifact: `ghcr.io/k8s-lovers-korea/test-spring-boot-app-manifests`
3. **Flux Deployment**: Flux monitors the OCI repository and automatically deploys updates

### Manual Deployment

#### 1. Setup Flux OCI Repository (One-time setup)

```bash
# Apply Flux configuration to your cluster
kubectl apply -f flux/oci-repository.yaml
kubectl apply -f flux/kustomization.yaml
```

#### 2. Local Development

```bash
# Run tests
./gradlew test

# Build application
./gradlew bootJar

# Run locally
./gradlew bootRun

# Build Docker image
docker build -t test-spring-boot-app .

# Run with Docker
docker run -p 8080:8080 test-spring-boot-app
```

#### 3. Deploy to Kubernetes (without Flux)

```bash
# Apply Kubernetes manifests directly
kubectl apply -k k8s/

# Or using kubectl with specific image
kubectl apply -f k8s/
kubectl set image deployment/test-spring-boot-app test-spring-boot-app=ghcr.io/k8s-lovers-korea/test-spring-boot-app:latest
```

## API Endpoints

- **Application**: `http://localhost:8080`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/prometheus`
- **API Documentation**: `http://localhost:8080/swagger-ui.html`
- **H2 Console** (dev only): `http://localhost:8080/h2-console`

## Configuration

The application uses different configurations for different environments:

- **Development**: `src/main/resources/application.yml`
- **Production**: Configuration in `k8s/configmap.yaml`

Key differences in production:
- H2 console disabled
- Reduced log verbosity
- Lower tracing sampling rate
- Health endpoint security

## Monitoring

The application includes comprehensive monitoring:

- **Health Checks**: Kubernetes readiness/liveness probes
- **Metrics**: Prometheus metrics via `/actuator/prometheus`
- **Tracing**: OpenTelemetry support (configure OTLP endpoint)
- **Logging**: Structured logging with trace correlation

## Flux OCI Repository Architecture

This project demonstrates a GitOps approach using Flux OCI repositories:

1. **Source Code** → GitHub Repository
2. **CI/CD** → GitHub Actions builds and pushes:
   - Application image to GHCR
   - Kubernetes manifests as OCI artifact to GHCR
3. **Deployment** → Flux monitors OCI repository and applies manifests to Kubernetes

### Benefits:

- **Immutable Deployments**: Manifests are versioned and immutable
- **Reduced Git Pollution**: No manifest updates in source repository
- **Enhanced Security**: Signed OCI artifacts (optional)
- **Simplified GitOps**: Direct manifest deployment without Git repository polling

A comprehensive Spring Boot application designed for various test scenarios including performance testing, thread management, monitoring, and observability.

## Features

✅ Spring Boot Actuator - Application monitoring and restart detection
✅ In-Memory CRUD Operations - H2 database with JPA entities
✅ Thread Testing Endpoints - Block threads and simulate hangs
✅ Continuous Logging - Structured logging for debugging
✅ OpenTelemetry Integration - Distributed tracing support
✅ Prometheus Metrics - Application metrics via Actuator
✅ REST API - Complete set of endpoints for testing scenarios

## Quick Start

### Prerequisites
- Java 21+
- Gradle (wrapper included)
- Docker (optional, for containerized run)

### Build and Run (Gradle)
```bash
# Build the application JAR
./gradlew clean bootJar

# Run the application
java -jar build/libs/test-spring-boot-app-0.0.1-SNAPSHOT.jar

# Or run directly with Gradle (dev mode)
./gradlew bootRun
```

The application will start on port 8080 by default.

## Run with Docker

A production-grade multi-stage Dockerfile is provided.

```bash
# Build the container image
docker build -t k8slovers/test-spring-boot-app:latest .

# Run the container (map port 8080)
docker run --rm -p 8080:8080 \
  -e JAVA_OPTS="-Xms256m -Xmx512m" \
  --name test-spring-boot-app \
  k8slovers/test-spring-boot-app:latest
```

Optional environment variables:
- SPRING_PROFILES_ACTIVE=prod
- OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
- LOGGING_LEVEL_ROOT=INFO

For Apple Silicon or specific architectures you can add: `--platform=linux/amd64` when building/running.

## Swagger / OpenAPI 문서

아래 경로에서 자동 생성된 API 문서를 확인할 수 있습니다.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

컨트롤러에 요약과 설명이 추가되어 있어, 각 엔드포인트의 목적과 파라미터를 쉽게 확인할 수 있습니다.

## API Endpoints

### Health & Monitoring
- GET /actuator/health - Application health status
- GET /actuator/metrics - Application metrics
- GET /actuator/prometheus - Prometheus metrics
- GET /actuator/restart-monitor - Application restart information
- GET /actuator/info - Application information

### CRUD Operations (TestEntity)
- GET /api/entities - Get all entities
- GET /api/entities/{id} - Get entity by ID
- POST /api/entities - Create new entity
- PUT /api/entities/{id} - Update entity
- DELETE /api/entities/{id} - Delete entity
- GET /api/entities/search?name={name} - Search entities by name

### Test Scenarios
- GET /api/test/health - Basic health check
- POST /api/test/block-thread?seconds={n} - Exhaust request thread pool and block threads (default: 30)
- POST /api/test/hang?seconds={n} - Hang thread for n seconds (default: 90)
- POST /api/test/cpu-intensive?seconds={n} - CPU intensive task (default: 10)
- GET /api/test/thread-status - Check thread status and locks

### Database Console
- GET /h2-console - H2 database web console (available in dev mode)

## Usage Examples

### Basic Health Check
```bash
curl http://localhost:8080/api/test/health
```

### Create and Manage Entities
```bash
# Create entity
curl -X POST http://localhost:8080/api/entities \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Entity", "description": "A sample entity"}'

# Get all entities
curl http://localhost:8080/api/entities

# Search entities
curl "http://localhost:8080/api/entities/search?name=Test"
```

### Thread Testing Scenarios
```bash
# Block/exhaust threads for 60 seconds
curl -X POST "http://localhost:8080/api/test/block-thread?seconds=60"

# Hang a thread for 120 seconds
curl -X POST "http://localhost:8080/api/test/hang?seconds=120"

# Run CPU intensive task for 30 seconds
curl -X POST "http://localhost:8080/api/test/cpu-intensive?seconds=30"

# Check thread status
curl http://localhost:8080/api/test/thread-status
```

### Monitoring and Metrics
```bash
# Application health
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Application restart information
curl http://localhost:8080/actuator/restart-monitor

# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## Configuration

### Application Properties
Key configuration options in `application.yml`:

- Server Port: `server.port` (default: 8080)
- Database: H2 in-memory database with console enabled
- Logging: Structured logging with trace correlation
- Actuator: All endpoints exposed for monitoring
- OpenTelemetry: OTLP exporter configured (endpoint: http://localhost:4317)

### Environment Variables
- OTEL_EXPORTER_OTLP_ENDPOINT - OTLP endpoint for tracing (default: http://localhost:4317)
- SPRING_PROFILES_ACTIVE - Active Spring profiles
- JAVA_OPTS - Extra JVM options (e.g., `-Xms256m -Xmx512m`)

## Logging

The application provides comprehensive logging:

- Heartbeat logs every 30 seconds
- System status every minute (memory, threads)
- Database status every 2 minutes
- Detailed system info every 5 minutes
- Request/response logging for all API calls
- Trace correlation with OpenTelemetry span IDs

Logs are written to:
- Console (structured format)
- File: `logs/test-spring-boot-app.log`

Note: Inside Docker, the file path is within the container. Prefer reading logs from container stdout with `docker logs` unless you mount a volume.

## Observability

### OpenTelemetry Tracing
- Automatic instrumentation for HTTP requests
- Custom spans for service methods
- Trace correlation in logs
- OTLP export support

### Prometheus Metrics
Available at `/actuator/prometheus`:
- HTTP request metrics
- JVM metrics (memory, threads, GC)
- Custom application metrics

### Actuator Endpoints in Swagger
Actuator endpoints are included in Swagger UI. Browse them under the "Actuator" group at http://localhost:8080/swagger-ui.html (requires springdoc-openapi-starter-actuator and `springdoc.show-actuator=true`).

## Testing

### Run Tests
```bash
./gradlew test
```

### Integration Testing
The application includes integration tests that verify:
- Application context loading
- Actuator endpoints
- Basic functionality

### Manual Testing Scenarios
1. Thread Pool Testing: Use `/api/test/block-thread` to block/exhaust threads and test thread pool behavior
2. Timeout Testing: Use `/api/test/hang` to simulate long-running requests
3. Performance Testing: Use `/api/test/cpu-intensive` for CPU load testing
4. Monitoring: Monitor metrics via `/actuator/prometheus` during load tests
5. Tracing: Verify distributed tracing in your tracing backend
6. Restart Detection: Monitor `/actuator/restart-monitor` for application restarts

## Architecture

```
├── Controller Layer
│   ├── TestEntityController (CRUD operations)
│   └── TestScenariosController (Test endpoints)
├── Service Layer
│   ├── TestEntityService (Business logic)
│   └── LoggingService (Scheduled logging)
├── Repository Layer
│   └── TestEntityRepository (JPA repository)
├── Configuration
│   ├── OpenTelemetryConfig (Tracing setup)
│   └── RestartMonitorEndpoint (Custom actuator endpoint)
└── Model
    └── TestEntity (JPA entity)
```

## Technology Stack

- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- Spring Boot Actuator
- OpenTelemetry + Micrometer
- Prometheus (via Actuator endpoint)
- Logback
- Gradle (Kotlin DSL)
- JUnit 5

## Development

### Project Structure
```
src/
├── main/java/com/k8sloverskorea/testspringbootapp/
│   ├── TestSpringBootApplication.java
│   ├── config/
│   ├── controller/
│   ├── model/
│   ├── repository/
│   └── service/
├── main/resources/
│   └── application.yml
└── test/java/
    └── TestSpringBootApplicationTests.java
```

### Adding New Features
1. Create new controllers in `controller/` package
2. Add business logic in `service/` package
3. Define data models in `model/` package
4. Configure new dependencies in `build.gradle.kts`
5. Update `application.yml` for new configurations

## Troubleshooting

### Common Issues
1. Port already in use: Change `server.port` in `application.yml`
2. OpenTelemetry connection failed: Ensure your collector is running or disable tracing
3. Database connection issues: H2 runs in-memory, check JPA configuration
4. Memory issues: Adjust JVM heap size with `JAVA_OPTS` (e.g., `-Xmx512m`)

### Debug Mode
Run with additional logging:
```bash
java -jar build/libs/test-spring-boot-app-0.0.1-SNAPSHOT.jar --logging.level.com.k8sloverskorea=DEBUG
```

## License

This project is created for testing and educational purposes.
