#include <iostream>
#include <fstream>
#include <string>
#include <chrono>
#include <thread>
#include <hiredis/hiredis.h> // <--- We need the Redis library

class LogAgent {
private:
    std::string filePath;
    std::streamoff lastPosition = 0;
    redisContext *redis; // <--- The Redis connection pipeline

public:
    LogAgent(std::string path) : filePath(path) {
        // Connect to Redis running on localhost via Docker port mapping
        redis = redisConnect("redis", 6379);
        if (redis == NULL || redis->err) {
            std::cerr << "Redis Connection Error!" << std::endl;
        } else {
            std::cout << "Successfully connected to Redis at 127.0.0.1:6379" << std::endl;
        }
    }

    ~LogAgent() {
        if (redis) redisFree(redis);
    }

    void start() {
        std::cout << "Sentinel Agent started. Monitoring: " << filePath << std::endl;
        while (true) {
            readNewLogs();
            std::this_thread::sleep_for(std::chrono::seconds(1));
        }
    }

private:
    void readNewLogs() {
        std::ifstream file(filePath, std::ios::ate);
        if (!file.is_open()) return;

        std::streamoff currentSize = file.tellg();

        if (currentSize > lastPosition) {
            file.seekg(lastPosition);
            std::string line;
            while (std::getline(file, line)) {
                if (!line.empty()) {
                    shipToBuffer(line);
                }
            }
            lastPosition = file.tellg();
        }
    }

    void shipToBuffer(const std::string& log) {
        if (redis != NULL && !redis->err) {
            // Actually push the log to the 'log_queue' list in Redis!
            redisReply *reply = (redisReply*)redisCommand(redis, "RPUSH log_queue %s", log.c_str());
            if (reply != NULL) {
                freeReplyObject(reply);
                std::cout << "[SHIPPER] Successfully Pushed to Redis: " << log << std::endl;
            }
        }
    }
};

int main() {
    LogAgent agent("/logs/app.log");
    agent.start();
    return 0;
}