import redis

# Connect to the Redis shock absorber
r = redis.Redis(host='127.0.0.1', port=6379)

# Push a fake log directly into the queue
r.rpush('log_queue', 'CRITICAL: The Python bypass worked! Pipeline is fully operational.')

print("🔥 Log blasted into Redis!")