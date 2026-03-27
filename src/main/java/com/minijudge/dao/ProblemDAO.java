package com.minijudge.dao;

import com.minijudge.model.Problem;
import com.minijudge.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemDAO {

    // Get all problems
    public List<Problem> getAllProblems() {
        List<Problem> problems = new ArrayList<>();
        String sql = "SELECT * FROM problems ORDER BY problem_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                problems.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ getAllProblems failed: " + e.getMessage());
        }
        return problems;
    }

    // Get single problem by ID
    public Problem getProblemById(int id) {
        String sql = "SELECT * FROM problems WHERE problem_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("❌ getProblemById failed: " + e.getMessage());
        }
        return null;
    }

    // Insert a new problem
    public boolean insertProblem(Problem p) {
        String sql = """
            INSERT INTO problems (title, statement, difficulty, time_limit_ms, memory_limit_mb, tags)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getTitle());
            ps.setString(2, p.getStatement());
            ps.setString(3, p.getDifficulty());
            ps.setInt(4, p.getTimeLimitMs());
            ps.setInt(5, p.getMemoryLimitMb());
            ps.setString(6, p.getTags());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ insertProblem failed: " + e.getMessage());
        }
        return false;
    }

    // Helper to map a ResultSet row to a Problem object
    private Problem mapRow(ResultSet rs) throws SQLException {
        return new Problem(
            rs.getInt("problem_id"),
            rs.getString("title"),
            rs.getString("statement"),
            rs.getString("difficulty"),
            rs.getInt("time_limit_ms"),
            rs.getInt("memory_limit_mb"),
            rs.getString("tags")
        );
    }
}