from fastapi import FastAPI, HTTPException
import redis
import clickhouse_connect

# Initialize the API
app = FastAPI(title="Sentinel Log Engine API")

# 1. Connect to Redis (The Buffer)
# We use this to fetch "real-time" logs that haven't hit the database yet.
redis_client = redis.Redis(host='redis', port=6379, db=0, decode_responses=True)

# 2. Connect to ClickHouse (The Analytics Database)
# We use this to run complex SQL queries over millions of historical logs.
try:
    ch_client = clickhouse_connect.get_client(host='clickhouse', port=8123)
except Exception as e:
    print(f"ClickHouse starting up... {e}")
    ch_client = None


@app.get("/")
def health_check():
    return {"status": "Sentinel API is online and routing traffic."}

@app.get("/api/v1/logs/realtime")
def get_realtime_logs(limit: int = 10):
    """Fetch the absolute newest logs directly from the Redis fast-buffer."""
    try:
        # In the next phase, we will map this to a Redis Stream (XREAD)
        return {
            "source": "Redis", 
            "message": f"Ready to fetch top {limit} logs from memory.",
            "status": "Connected"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/v1/analytics/error-rates")
def get_error_analytics():
    """Run a high-speed aggregation query on ClickHouse."""
    if not ch_client:
        return {"status": "Database booting. Try again in a moment."}
    
    # This is where your System Design skills shine:
    # A real query here would look like: "SELECT count() FROM logs WHERE level = 'ERROR' GROUP BY hour"
    return {
        "source": "ClickHouse",
        "metric": "Error Rates",
        "data": "ClickHouse connected. Ready to process columnar SQL queries."
    }