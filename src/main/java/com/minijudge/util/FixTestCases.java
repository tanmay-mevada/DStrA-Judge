package com.minijudge.util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class FixTestCases {
    public static void main(String[] args) {
        String sql = "UPDATE test_cases SET expected_output = TRIM(expected_output)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            System.out.println("✅ Fixed " + rows + " test cases!");
        } catch (Exception e) {
            System.err.println("❌ Failed: " + e.getMessage());
        }
    }
}