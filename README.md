# Test Spring Boot Application

A comprehensive Spring Boot application designed for various test scenarios including performance testing, thread management, monitoring, and observability.

## Features

✅ **Spring Boot Actuator** - Application monitoring and restart detection  
✅ **In-Memory CRUD Operations** - H2 database with JPA entities  
✅ **Thread Testing Endpoints** - Block threads and simulate hangs  
✅ **Continuous Logging** - Structured logging for debugging  
✅ **OpenTelemetry Integration** - Distributed tracing support  
✅ **Prometheus Metrics** - Application metrics via Actuator  
✅ **REST API** - Complete set of endpoints for testing scenarios  

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Build and Run
```bash
# Build the application
mvn clean package

# Run the application
java -jar target/test-spring-boot-app-0.0.1-SNAPSHOT.jar

# Or run with Maven
mvn spring-boot:run
```

The application will start on port 8080 by default.

## API Endpoints

### Health & Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /actuator/restart-monitor` - Application restart information
- `GET /actuator/info` - Application information

### CRUD Operations (TestEntity)
- `GET /api/entities` - Get all entities
- `GET /api/entities/{id}` - Get entity by ID
- `POST /api/entities` - Create new entity
- `PUT /api/entities/{id}` - Update entity
- `DELETE /api/entities/{id}` - Delete entity
- `GET /api/entities/search?name={name}` - Search entities by name

### Test Scenarios
- `GET /api/test/health` - Basic health check
- `POST /api/test/block-thread?seconds={n}` - Block thread for n seconds (default: 30)
- `POST /api/test/hang?seconds={n}` - Hang thread for n seconds (default: 90)
- `POST /api/test/cpu-intensive?seconds={n}` - CPU intensive task (default: 10)
- `GET /api/test/thread-status` - Check thread status and locks

### Database Console
- `GET /h2-console` - H2 database web console (available in dev mode)

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
# Block a thread for 60 seconds (useful for testing thread pools)
curl -X POST "http://localhost:8080/api/test/block-thread?seconds=60"

# Hang a thread for 120 seconds (useful for timeout testing)
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

- **Server Port**: `server.port` (default: 8080)
- **Database**: H2 in-memory database with console enabled
- **Logging**: Structured logging with trace correlation
- **Actuator**: All endpoints exposed for monitoring
- **OpenTelemetry**: Jaeger exporter configured (endpoint: http://localhost:14250)

### Environment Variables
- `OTEL_EXPORTER_JAEGER_ENDPOINT` - Jaeger endpoint for tracing
- `SPRING_PROFILES_ACTIVE` - Active Spring profiles

## Logging

The application provides comprehensive logging:

- **Heartbeat logs** every 30 seconds
- **System status** every minute (memory, threads)
- **Database status** every 2 minutes
- **Detailed system info** every 5 minutes
- **Request/response logging** for all API calls
- **Trace correlation** with OpenTelemetry span IDs

Logs are written to:
- Console (structured format)
- File: `logs/test-spring-boot-app.log`

## Observability

### OpenTelemetry Tracing
- Automatic instrumentation for HTTP requests
- Custom spans for service methods
- Trace correlation in logs
- Jaeger export support

### Prometheus Metrics
Available at `/actuator/prometheus`:
- HTTP request metrics
- JVM metrics (memory, threads, GC)
- Custom application metrics
- Database connection pool metrics

### Actuator Endpoints
Full actuator endpoint exposure for monitoring:
- Health checks
- Metrics
- Environment info
- Configuration properties
- Thread dumps
- Heap dumps

## Testing

### Run Tests
```bash
mvn test
```

### Integration Testing
The application includes integration tests that verify:
- Application context loading
- Actuator endpoints
- Basic functionality

### Manual Testing Scenarios

1. **Thread Pool Testing**: Use `/api/test/block-thread` to block threads and test thread pool behavior
2. **Timeout Testing**: Use `/api/test/hang` to simulate long-running requests
3. **Performance Testing**: Use `/api/test/cpu-intensive` for CPU load testing
4. **Monitoring**: Monitor metrics via `/actuator/prometheus` during load tests
5. **Tracing**: Verify distributed tracing in Jaeger UI
6. **Restart Detection**: Monitor `/actuator/restart-monitor` for application restarts

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

- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database
- **Spring Boot Actuator** - Monitoring and management
- **OpenTelemetry** - Distributed tracing
- **Micrometer** - Metrics collection
- **Prometheus** - Metrics export
- **Logback** - Logging framework
- **Maven** - Build tool
- **JUnit 5** - Testing framework

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
4. Configure new dependencies in `pom.xml`
5. Update `application.yml` for new configurations

## Troubleshooting

### Common Issues

1. **Port already in use**: Change `server.port` in `application.yml`
2. **OpenTelemetry connection failed**: Ensure Jaeger is running or disable tracing
3. **Database connection issues**: H2 runs in-memory, check JPA configuration
4. **Memory issues**: Adjust JVM heap size with `-Xmx` parameter

### Debug Mode
Run with additional logging:
```bash
java -jar target/test-spring-boot-app-0.0.1-SNAPSHOT.jar --logging.level.com.k8sloverskorea=DEBUG
```

## License

This project is created for testing and educational purposes.
