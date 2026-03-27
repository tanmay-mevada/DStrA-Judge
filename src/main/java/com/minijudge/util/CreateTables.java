package com.minijudge.util;

import java.sql.Connection;
import java.sql.Statement;

public class CreateTables {
    public static void main(String[] args) {
        String[] queries = {

            "CREATE TABLE IF NOT EXISTS users (" +
            "user_id SERIAL PRIMARY KEY," +
            "username VARCHAR(50) UNIQUE NOT NULL," +
            "email VARCHAR(100) UNIQUE NOT NULL," +
            "password_hash VARCHAR(255) NOT NULL," +
            "role VARCHAR(10) DEFAULT 'student'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            "CREATE TABLE IF NOT EXISTS problems (" +
            "problem_id SERIAL PRIMARY KEY," +
            "title VARCHAR(100) NOT NULL," +
            "statement TEXT NOT NULL," +
            "difficulty VARCHAR(10) NOT NULL," +
            "time_limit_ms INT DEFAULT 2000," +
            "memory_limit_mb INT DEFAULT 256," +
            "tags VARCHAR(200)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            "CREATE TABLE IF NOT EXISTS test_cases (" +
            "tc_id SERIAL PRIMARY KEY," +
            "problem_id INT REFERENCES problems(problem_id) ON DELETE CASCADE," +
            "input TEXT NOT NULL," +
            "expected_output TEXT NOT NULL," +
            "is_sample BOOLEAN DEFAULT FALSE)",

            "CREATE TABLE IF NOT EXISTS submissions (" +
            "sub_id SERIAL PRIMARY KEY," +
            "user_id INT," +
            "problem_id INT REFERENCES problems(problem_id) ON DELETE CASCADE," +
            "language VARCHAR(20) NOT NULL," +
            "code TEXT NOT NULL," +
            "verdict VARCHAR(10) DEFAULT 'PENDING'," +
            "runtime_ms INT," +
            "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            "CREATE TABLE IF NOT EXISTS leaderboard (" +
            "user_id INT PRIMARY KEY," +
            "problems_solved INT DEFAULT 0," +
            "score INT DEFAULT 0," +
            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        };

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String query : queries) {
                stmt.execute(query);
            }
            System.out.println("✅ All tables created successfully!");

        } catch (Exception e) {
            System.err.println("❌ Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}