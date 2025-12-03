# Monitoring Guide

## Monitoring Strategy

### Health Monitoring

#### Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

Response:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

#### Custom System Info

```bash
curl http://localhost:8080/api/system-info
```

Monitors:

- Heap memory usage
- System load
- Uptime
- Java version
- Virtual threads status

### Metrics

#### Actuator Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

Available metrics:

- `jvm.memory.used` - Memory usage
- `jvm.memory.max` - Max memory
- `http.server.requests` - HTTP requests
- `r2dbc.connections` - Database connections
- `process.uptime` - Application uptime

#### Custom Metrics

Add custom metrics using Micrometer:

```java
@Autowired
private MeterRegistry meterRegistry;

        meterRegistry.counter("custom.metric").increment();
```

### Logging

#### Log Levels

**Development:**

```yaml
logging:
  level:
    root: DEBUG
    com.khas: DEBUG
```

**Production:**

```yaml
logging:
  level:
    root: WARN
    com.khas: INFO
```

#### Log Files

Logs are written to:

- Console output
- `logs/application.log`

Configure rotation:

```yaml
logging:
  file:
    name: logs/application.log
  logback:
    rollingpolicy:
      max-file-size: 50MB
      max-history: 10
      total-size-cap: 500MB
```

## Monitoring Tools

### Prometheus (Optional)

Add dependency:

```gradle
implementation 'io.micrometer:micrometer-registry-prometheus'
```

Expose metrics:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
```

### Grafana (Optional)

Visualize metrics with Grafana dashboards.

### ELK Stack (Optional)

For log aggregation:

- Elasticsearch
- Logstash
- Kibana

## Alerting

### Health Check Alerts

Set up alerts for:

- Health endpoint down
- High memory usage
- High CPU usage
- Database connection failures

### Custom Alerts

Monitor:

- Error rate
- Response time
- Failed logins
- Unusual patterns

## Performance Monitoring

### Key Metrics

1. **Response Time**
    - Average response time
    - P95/P99 percentiles
    - Slow endpoints

2. **Throughput**
    - Requests per second
    - Concurrent users
    - Database queries per second

3. **Resource Usage**
    - Memory usage
    - CPU usage
    - Database connections
    - Thread pool usage

### Monitoring Dashboard

Create dashboard with:

- System metrics
- Application metrics
- Database metrics
- Custom business metrics

## Best Practices

1. ✅ Monitor health endpoints regularly
2. ✅ Set up alerts for critical metrics
3. ✅ Log important events
4. ✅ Track error rates
5. ✅ Monitor resource usage
6. ✅ Review logs regularly
7. ✅ Set up dashboards
8. ✅ Configure log rotation

