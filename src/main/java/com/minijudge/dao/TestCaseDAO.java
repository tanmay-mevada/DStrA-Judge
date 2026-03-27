package com.minijudge.dao;

import com.minijudge.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestCaseDAO {

    // Insert a test case for a problem
    public boolean insertTestCase(int problemId, String input,
                                   String expectedOutput, boolean isSample) {
        String sql = """
            INSERT INTO test_cases (problem_id, input, expected_output, is_sample)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problemId);
            ps.setString(2, input);
            ps.setString(3, expectedOutput);
            ps.setBoolean(4, isSample);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ insertTestCase failed: " + e.getMessage());
        }
        return false;
    }

    // Get all test cases for a problem (hidden + sample)
    public List<String[]> getTestCases(int problemId) {
        List<String[]> cases = new ArrayList<>();
        String sql = "SELECT input, expected_output FROM test_cases WHERE problem_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cases.add(new String[]{
                    rs.getString("input"),
                    rs.getString("expected_output")
                });
            }

        } catch (SQLException e) {
            System.err.println("❌ getTestCases failed: " + e.getMessage());
        }
        return cases;
    }

    // Get only sample test cases (shown to user)
    public List<String[]> getSampleTestCases(int problemId) {
        List<String[]> cases = new ArrayList<>();
        String sql = "SELECT input, expected_output FROM test_cases WHERE problem_id = ? AND is_sample = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cases.add(new String[]{
                    rs.getString("input"),
                    rs.getString("expected_output")
                });
            }

        } catch (SQLException e) {
            System.err.println("❌ getSampleTestCases failed: " + e.getMessage());
        }
        return cases;
    }
}