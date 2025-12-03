# Performance Tuning Guide

## Performance Optimization Strategies

### JVM Tuning

#### Heap Size Configuration

For 1GB RAM server:

```bash
-Xms256m -Xmx384m
```

For 2GB RAM server:

```bash
-Xms512m -Xmx768m
```

#### Garbage Collector

**Serial GC** (Recommended for low-resource):

```bash
-XX:+UseSerialGC
```

**G1GC** (For better throughput):

```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100
-XX:G1HeapRegionSize=1m
```

#### Metaspace

```bash
-XX:MetaspaceSize=64m
-XX:MaxMetaspaceSize=128m
```

### Application Tuning

#### Lazy Initialization

Enable in production:

```yaml
spring:
  main:
    lazy-initialization: true
```

#### Connection Pool

Optimize database connection pool:

```yaml
spring:
  r2dbc:
    pool:
      initial-size: 2
      max-size: 10
      max-idle-time: 30m
```

#### Virtual Threads

Enable for better concurrency:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

## Monitoring Performance

### Use System Info Endpoint

```bash
curl http://localhost:8080/api/system-info
```

Monitor:

- Heap usage
- System load
- Uptime
- Thread status

### Actuator Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

Key metrics:

- `jvm.memory.used`
- `jvm.memory.max`
- `http.server.requests`
- `r2dbc.connections`

## Database Performance

### Indexing

Ensure proper indexes:

```sql
CREATE INDEX idx_username ON users (username);
CREATE INDEX idx_email ON users (email);
CREATE INDEX idx_deleted_at ON users (deleted_at);
```

### Query Optimization

- Use reactive queries efficiently
- Avoid N+1 queries
- Use connection pooling
- Monitor slow queries

## Native Image Performance

### Build Native Image

```bash
make build-native
```

### Benefits

- **Boot Time**: < 100ms (vs 2-5 seconds)
- **Memory**: ~80-150MB (vs 350-450MB)
- **Startup**: Instant

## Best Practices

1. ✅ Monitor memory usage regularly
2. ✅ Adjust heap size based on actual usage
3. ✅ Use native image for production
4. ✅ Optimize database queries
5. ✅ Keep connection pools small
6. ✅ Enable lazy initialization
7. ✅ Use virtual threads
8. ✅ Monitor with Actuator

