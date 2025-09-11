# Spring Boot Observability Dashboard - PromQL Queries

## Overview
This document provides comprehensive PromQL queries for monitoring a Spring Boot application with container label `{container_name="spring-boot-observability"}`. The queries cover HTTP server metrics, HTTP client metrics, system resources, and application performance.

---

## 1. Service Uptime & Application Metrics

### Application Ready Time
```promql
application_ready_time_seconds{container_name="spring-boot-observability",main_application_class="net.samitkumar.spring_boot_observability.SpringBootObservabilityApplication"} / 3600
```

### Application Uptime
```promql
process_uptime_seconds{container_name="spring-boot-observability"} / 3600
```

### Application Start Time
```promql
application_started_time_seconds{container_name="spring-boot-observability",main_application_class="net.samitkumar.spring_boot_observability.SpringBootObservabilityApplication"}
```

---

## 2. HTTP Server Requests Monitoring

### Request Count by Endpoint
```promql
sum by (uri) (rate(http_server_requests_seconds_count{container_name="spring-boot-observability"}[5m]))
```

### Specific Endpoints
```promql
# /actuator endpoints
sum(http_server_requests_seconds_count{container_name="spring-boot-observability",uri=~"/actuator.*"})

# /metadata
sum(http_server_requests_seconds_count{container_name="spring-boot-observability",uri="/metadata"})

# /ping
sum(http_server_requests_seconds_count{container_name="spring-boot-observability",uri="/ping"})
```

### Response Time Analysis
```promql
# Average response time per endpoint
sum by (uri) (rate(http_server_requests_seconds_sum{container_name="spring-boot-observability"}[5m])) / sum by (uri) (rate(http_server_requests_seconds_count{container_name="spring-boot-observability"}[5m]))

# 95th percentile response time
histogram_quantile(0.95, sum by (uri, le) (rate(http_server_requests_seconds_bucket{container_name="spring-boot-observability"}[5m])))

# Maximum response time
http_server_requests_seconds_max{container_name="spring-boot-observability"}
```

### Success/Failure Rates
```promql
# Success rate by endpoint
sum by (uri) (rate(http_server_requests_seconds_count{container_name="spring-boot-observability",outcome="SUCCESS"}[5m])) / sum by (uri) (rate(http_server_requests_seconds_count{container_name="spring-boot-observability"}[5m])) * 100

# Error rate by endpoint
sum by (uri) (rate(http_server_requests_seconds_count{container_name="spring-boot-observability",outcome!="SUCCESS"}[5m])) / sum by (uri) (rate(http_server_requests_seconds_count{container_name="spring-boot-observability"}[5m])) * 100
```

---

## 3. HTTP Client Requests Monitoring

### Request Count to Third-Party Services
```promql
# Total requests per client
sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m]))

# Requests per endpoint
sum by (client_name, uri) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m]))
```

### Client Response Time Analysis
```promql
# Average response time per client
sum by (client_name) (rate(http_client_requests_seconds_sum{container_name="spring-boot-observability"}[5m])) / sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m]))

# 95th percentile response time
histogram_quantile(0.95, sum by (client_name, le) (rate(http_client_requests_seconds_bucket{container_name="spring-boot-observability"}[5m])))

# Maximum response time
http_client_requests_seconds_max{container_name="spring-boot-observability"}
```

### Client Success/Failure Analysis
```promql
# Success rate per client
sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability",outcome="SUCCESS"}[5m])) / sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m])) * 100

# Error rate per client
sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability",outcome!="SUCCESS"}[5m])) / sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m])) * 100

# HTTP status code distribution
sum by (client_name, status) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m]))
```

### Client Exception Analysis
```promql
# Exception rate per client
sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability",exception!="none"}[5m])) / sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m])) * 100

# Exception types
sum by (client_name, exception) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m]))
```

---

## 4. Memory Utilization

### Heap Memory Usage
```promql
# Heap memory usage percentage
(sum(jvm_memory_used_bytes{container_name="spring-boot-observability",area="heap"}) / sum(jvm_memory_max_bytes{container_name="spring-boot-observability",area="heap",id="G1 Old Gen"})) * 100

# Memory usage by area
jvm_memory_used_bytes{container_name="spring-boot-observability",area="heap",id="G1 Eden Space"}
jvm_memory_used_bytes{container_name="spring-boot-observability",area="heap",id="G1 Old Gen"}
jvm_memory_used_bytes{container_name="spring-boot-observability",area="heap",id="G1 Survivor Space"}
```

### Non-Heap Memory
```promql
# Non-heap memory usage
sum(jvm_memory_used_bytes{container_name="spring-boot-observability",area="nonheap"})

# Metaspace usage
jvm_memory_used_bytes{container_name="spring-boot-observability",area="nonheap",id="Metaspace"}
```

---

## 5. CPU Utilization

### Process CPU Usage
```promql
# Process CPU usage percentage
process_cpu_usage{container_name="spring-boot-observability"} * 100

# System CPU usage
system_cpu_usage{container_name="spring-boot-observability"} * 100

# CPU time
rate(process_cpu_time_ns_total{container_name="spring-boot-observability"}[5m]) / 1000000000
```

### System Load
```promql
# 1-minute load average
system_load_average_1m{container_name="spring-boot-observability"}

# CPU count
system_cpu_count{container_name="spring-boot-observability"}
```

---

## 6. Disk Space Monitoring

### Free Disk Space
```promql
# Free disk space in GB
disk_free_bytes{container_name="spring-boot-observability"} / 1024 / 1024 / 1024

# Disk usage percentage
(1 - (disk_free_bytes{container_name="spring-boot-observability"} / disk_total_bytes{container_name="spring-boot-observability"})) * 100

# Total disk space in GB
disk_total_bytes{container_name="spring-boot-observability"} / 1024 / 1024 / 1024
```

---

## 7. I/O Details

### File Descriptors
```promql
# Open file descriptors
process_files_open_files{container_name="spring-boot-observability"}

# Max file descriptors
process_files_max_files{container_name="spring-boot-observability"}

# File descriptor usage percentage
(process_files_open_files{container_name="spring-boot-observability"} / process_files_max_files{container_name="spring-boot-observability"}) * 100
```

### Buffer I/O
```promql
# Direct buffer memory used
jvm_buffer_memory_used_bytes{container_name="spring-boot-observability",id="direct"}

# Mapped buffer memory used
jvm_buffer_memory_used_bytes{container_name="spring-boot-observability",id="mapped"}

# Buffer count
jvm_buffer_count_buffers{container_name="spring-boot-observability"}
```

---

## 8. Thread Information

### Active vs Total Threads
```promql
# Total live threads
jvm_threads_live_threads{container_name="spring-boot-observability"}

# Daemon threads
jvm_threads_daemon_threads{container_name="spring-boot-observability"}

# Non-daemon threads
jvm_threads_live_threads{container_name="spring-boot-observability"} - jvm_threads_daemon_threads{container_name="spring-boot-observability"}

# Thread pool active threads
executor_active_threads{container_name="spring-boot-observability"}

# Thread pool size
executor_pool_size_threads{container_name="spring-boot-observability"}

# Thread pool max threads
executor_pool_max_threads{container_name="spring-boot-observability"}
```

### Thread States
```promql
# Threads by state
jvm_threads_states_threads{container_name="spring-boot-observability"}

# Runnable threads
jvm_threads_states_threads{container_name="spring-boot-observability",state="runnable"}

# Blocked threads
jvm_threads_states_threads{container_name="spring-boot-observability",state="blocked"}

# Waiting threads
jvm_threads_states_threads{container_name="spring-boot-observability",state="waiting"}
```

---

## 9. Garbage Collection Metrics

### GC Performance
```promql
# GC overhead percentage
jvm_gc_overhead{container_name="spring-boot-observability"} * 100

# GC pause time
rate(jvm_gc_pause_seconds_sum{container_name="spring-boot-observability"}[5m])

# Memory allocated by GC
rate(jvm_gc_memory_allocated_bytes_total{container_name="spring-boot-observability"}[5m])
```

---

## 10. Alerting Queries

### SLA Compliance
```promql
# 95% of requests under 1 second
histogram_quantile(0.95, sum by (client_name, le) (rate(http_client_requests_seconds_bucket{container_name="spring-boot-observability"}[5m]))) < 1

# High error rate alert (>5%)
sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability",outcome!="SUCCESS"}[5m])) / sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m])) > 0.05

# Slow response alert (>2 seconds average)
sum by (client_name) (rate(http_client_requests_seconds_sum{container_name="spring-boot-observability"}[5m])) / sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m])) > 2
```

### Resource Alerts
```promql
# High memory usage (>80%)
(sum(jvm_memory_used_bytes{container_name="spring-boot-observability",area="heap"}) / sum(jvm_memory_max_bytes{container_name="spring-boot-observability",area="heap",id="G1 Old Gen"})) > 0.8

# High CPU usage (>80%)
process_cpu_usage{container_name="spring-boot-observability"} > 0.8

# Low disk space (<10% free)
(disk_free_bytes{container_name="spring-boot-observability"} / disk_total_bytes{container_name="spring-boot-observability"}) < 0.1
```

---

## 11. Top Performance Analysis

### Top Slowest Endpoints
```promql
topk(10, sum by (client_name, uri) (rate(http_client_requests_seconds_sum{container_name="spring-boot-observability"}[5m])) / sum by (client_name, uri) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m])))
```

### Most Called Endpoints
```promql
topk(10, sum by (client_name, uri) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m])))
```

### Client Reliability Score
```promql
(sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability",outcome="SUCCESS"}[5m])) / sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m]))) * 100
```

---

## 12. Time-based Analysis

### Request Patterns
```promql
# Hourly request patterns
sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[1h]))

# Daily request volume
sum by (client_name) (increase(http_client_requests_seconds_count{container_name="spring-boot-observability"}[1d]))

# Peak request times
max_over_time(sum by (client_name) (rate(http_client_requests_seconds_count{container_name="spring-boot-observability"}[5m]))[1h:1m])
```

---

## Usage Notes

- All queries include the `{container_name="spring-boot-observability"}` label filter
- Time ranges (e.g., `[5m]`, `[1h]`) can be adjusted based on your monitoring needs
- Use these queries in Grafana dashboards for comprehensive application monitoring
- Set up alerts based on the alerting queries for proactive monitoring
- Adjust threshold values in alerting queries based on your SLA requirements

---

## Dashboard Panel Suggestions

### Overview Panel
- Application uptime (single stat)
- Total requests (single stat)
- Error rate (single stat)
- Response time (single stat)

### HTTP Server Panel
- Request count by endpoint (bar chart)
- Response time by endpoint (line chart)
- Success/failure rate (pie chart)
- Request rate over time (line chart)

### HTTP Client Panel
- Client request count (bar chart)
- Client response time (line chart)
- Client error rate (line chart)
- Exception types (table)

### System Resources Panel
- Memory usage (gauge)
- CPU usage (gauge)
- Disk usage (gauge)
- Thread count (line chart)

### Performance Panel
- Top slowest endpoints (table)
- Most called endpoints (table)
- GC overhead (line chart)
- Load average (line chart)
