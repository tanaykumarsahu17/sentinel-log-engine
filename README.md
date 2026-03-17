# 🛡️ Sentinel Log Engine

A high-performance, polyglot observability pipeline built from scratch to process, buffer, and visualize system telemetry in real-time. 

Sentinel is a fully containerized microservice architecture designed to handle high-throughput log generation, prevent data loss during traffic spikes, and provide permanent, queryable storage for system diagnostics.



## 🏗️ Architecture & Tech Stack

This pipeline is built using a decoupling strategy, ensuring that if the database slows down, the log generator doesn't crash. 

* **Log Agent (C++):** A lightweight, low-latency daemon that monitors native file systems for new log entries and instantly ships them to the network.
* **The Shock Absorber (Redis):** Acts as a high-speed message queue (`LPUSH`/`BRPOP`). It buffers incoming telemetry to prevent data loss during massive traffic spikes or database restarts.
* **The Processor (Java):** A resilient worker service that pulls raw strings from Redis, sanitizes the data using SQL Prepared Statements to prevent injection/crashing, and formats it for analytics.
* **Long-Term Storage (ClickHouse):** A columnar OLAP database chosen specifically for its lightning-fast aggregation capabilities. Configured with permanent Docker Volumes to ensure data immortality across container lifecycle events.
* **Command Center (Grafana):** Connects directly to ClickHouse to visualize system health via real-time pie charts and time-series pulse graphs.

## 🧠 Key Engineering Decisions

* **Fault Isolation:** By separating the C++ generator from the Java processor via Redis, the system achieves perfect decoupling. If the database goes offline, logs safely stack up in Redis memory until the database returns.
* **Data Immortality:** Implemented Docker Volumes (`clickhouse_data:/var/lib/clickhouse/`) to ensure the ClickHouse database survives complete container destruction.
* **Resilient Parsing:** The Java processor is built with isolated `try-catch` blocks inside its continuous listening loop, ensuring that a single malformed log string never crashes the worker thread.

---

## 🚀 Quick Start Guide

### Prerequisites
* Docker and Docker Compose installed on your host machine.

### 1. Spin Up the Fleet
Clone the repository and build the microservices:
```bash
git clone [https://github.com/yourusername/sentinel-log-engine.git](https://github.com/yourusername/sentinel-log-engine.git)
cd sentinel-log-engine
docker-compose up -d --build.
```

### 2. Access the Command Center
Open your browser and navigate to the Grafana dashboard:
http://localhost:3005
(Note: You may need to configure the initial ClickHouse data source and paste the dashboard SQL queries if running for the first time).

### 3. Fire the Cannon (Test the Pipeline)
To bypass host-to-container file sync delays and test the pipeline natively, teleport into the C++ container and fire a test log:

```bash
# Send an INFO log
docker-compose exec agent sh -c "echo 'INFO: Sentinel system booted successfully' >> /logs/app.log"

# Send a CRITICAL log
docker-compose exec agent sh -c "echo 'CRITICAL: Database connection timeout!' >> /logs/app.log"
```
Watch the Grafana dashboard instantly catch the logs, categorize them by severity, and update the time-series graphs in real-time!

## 🧹 Cleanup
To safely spin down the architecture without losing your permanent ClickHouse data:
```Bash
docker-compose down
```
