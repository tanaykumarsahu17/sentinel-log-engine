package com.sentinel;

import redis.clients.jedis.Jedis;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class Processor {

    public static void main(String[] args) {
        System.out.println("Sentinel Java Processor Starting...");

        try (Jedis jedis = new Jedis("redis", 6379)) {
            System.out.println("Connected to Redis Shock Absorber.");

            String dbUrl = "jdbc:ch://clickhouse:8123/default";
            try (Connection conn = DriverManager.getConnection(dbUrl);
                 Statement stmt = conn.createStatement()) {

                System.out.println("Connected to ClickHouse Long-Term Storage.");
                stmt.execute("CREATE TABLE IF NOT EXISTS system_logs (timestamp DateTime, message String) ENGINE = MergeTree() ORDER BY timestamp");

                // PREPARED STATEMENT: Fast and perfectly safe from bad characters
                String insertSql = "INSERT INTO system_logs (timestamp, message) VALUES (now(), ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

                    System.out.println("Listening for new logs...");
                    while (true) {
                        try { // <--- TRY BLOCK MOVED INSIDE THE LOOP
                            String logMessage = jedis.lpop("log_queue");

                            if (logMessage != null) {
                                System.out.println("[PROCESSOR] Pulled from Redis: " + logMessage);

                                // Safely lock the log into the SQL statement
                                pstmt.setString(1, logMessage);
                                pstmt.execute();

                                System.out.println("[PROCESSOR] Saved to ClickHouse.");
                            } else {
                                Thread.sleep(1000);
                            }
                        } catch (Exception innerE) {
                            // If ONE log fails, we catch it here and the loop KEEPS RUNNING!
                            System.err.println("[PROCESSOR WARNING] Dropped a bad log: " + innerE.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fatal Pipeline Error: " + e.getMessage());
        }
    }
}