package com.minijudge.dao;

import com.minijudge.model.Submission;
import com.minijudge.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO {

    // Save a new submission and return its generated ID
    public int insertSubmission(Submission s) {
        String sql = """
            INSERT INTO submissions (problem_id, language, code, verdict)
            VALUES (?, ?, ?, 'PENDING')
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getProblemId());
            ps.setString(2, s.getLanguage());
            ps.setString(3, s.getCode());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.err.println("❌ insertSubmission failed: " + e.getMessage());
        }
        return -1;
    }

    // Update verdict and runtime after judging
    public boolean updateVerdict(int subId, String verdict, int runtimeMs) {
        String sql = """
            UPDATE submissions
            SET verdict = ?, runtime_ms = ?
            WHERE sub_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, verdict);
            ps.setInt(2, runtimeMs);
            ps.setInt(3, subId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ updateVerdict failed: " + e.getMessage());
        }
        return false;
    }

    // Get all submissions for a problem
    public List<Submission> getSubmissionsByProblem(int problemId) {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT * FROM submissions WHERE problem_id = ? ORDER BY submitted_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Submission s = new Submission();
                s.setSubId(rs.getInt("sub_id"));
                s.setProblemId(rs.getInt("problem_id"));
                s.setLanguage(rs.getString("language"));
                s.setCode(rs.getString("code"));
                s.setVerdict(rs.getString("verdict"));
                s.setRuntimeMs(rs.getInt("runtime_ms"));
                list.add(s);
            }

        } catch (SQLException e) {
            System.err.println("❌ getSubmissionsByProblem failed: " + e.getMessage());
        }
        return list;
    }
}