# Sentinel Log Engine

A high-performance, containerized observability pipeline built from scratch to process, buffer, and visualize system telemetry in real-time.

## Architecture
* **Log Agent (C++):** Lightweight daemon that monitors file systems and pushes telemetry to the buffer.
* **Shock Absorber (Redis):** High-speed queue to prevent data loss during traffic spikes.
* **Log Processor (Java):** Pulls, formats, and safely sanitizes data before long-term storage.
* **Database (ClickHouse):** Columnar database with persistent volume mapping for immortal data storage.
* **Dashboard (Grafana):** Real-time command center mapping system health with pie charts and time-series graphs.

## How to Run
1. Clone this repository.
2. Run `docker-compose up -d --build`.
3. Open Grafana at `http://localhost:3005`.
4. Drop a log into `logs/app.log` and watch the dashboard light up!
