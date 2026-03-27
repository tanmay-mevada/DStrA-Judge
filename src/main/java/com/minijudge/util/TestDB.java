package com.minijudge.util;

import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connection works! " + conn);
            conn.close();
        } catch (Exception e) {
            System.err.println("❌ Failed: " + e.getMessage());
        }
    }
}