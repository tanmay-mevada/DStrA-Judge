package com.minijudge.util;

import com.minijudge.dao.ProblemDAO;
import com.minijudge.dao.TestCaseDAO;
import com.minijudge.model.Problem;

public class SeedData {
    public static void main(String[] args) {

        ProblemDAO problemDAO = new ProblemDAO();
        TestCaseDAO testCaseDAO = new TestCaseDAO();

        // ── Problem 1: Hello World ──────────────────────────────
        Problem p1 = new Problem();
        p1.setTitle("Hello World");
        p1.setStatement(
            "Print exactly: Hello, World!"
        );
        p1.setDifficulty("Easy");
        p1.setTimeLimitMs(2000);
        p1.setMemoryLimitMb(256);
        p1.setTags("basics");
        problemDAO.insertProblem(p1);
        int id1 = getLastId(problemDAO);

        testCaseDAO.insertTestCase(id1, "", "Hello, World!", true);
        testCaseDAO.insertTestCase(id1, "", "Hello, World!", false);
        System.out.println("✅ Problem 1 inserted: Hello World");

        // ── Problem 2: Sum of Two Numbers ───────────────────────
        Problem p2 = new Problem();
        p2.setTitle("Sum of Two Numbers");
        p2.setStatement(
            "Given two integers A and B on a single line, print their sum.\n\n" +
            "Input: Two space-separated integers\n" +
            "Output: Their sum\n\n" +
            "Example:\nInput: 3 5\nOutput: 8"
        );
        p2.setDifficulty("Easy");
        p2.setTimeLimitMs(2000);
        p2.setMemoryLimitMb(256);
        p2.setTags("math,basics");
        problemDAO.insertProblem(p2);
        int id2 = getLastId(problemDAO);

        testCaseDAO.insertTestCase(id2, "3 5",   "8",  true);
        testCaseDAO.insertTestCase(id2, "10 20", "30", false);
        testCaseDAO.insertTestCase(id2, "-1 1",  "0",  false);
        System.out.println("✅ Problem 2 inserted: Sum of Two Numbers");

        // ── Problem 3: Reverse a String ─────────────────────────
        Problem p3 = new Problem();
        p3.setTitle("Reverse a String");
        p3.setStatement(
            "Given a string, print it reversed.\n\n" +
            "Input: A single string\n" +
            "Output: The reversed string\n\n" +
            "Example:\nInput: hello\nOutput: olleh"
        );
        p3.setDifficulty("Easy");
        p3.setTimeLimitMs(2000);
        p3.setMemoryLimitMb(256);
        p3.setTags("strings");
        problemDAO.insertProblem(p3);
        int id3 = getLastId(problemDAO);

        testCaseDAO.insertTestCase(id3, "hello",  "olleh",  true);
        testCaseDAO.insertTestCase(id3, "java",   "avaj",   false);
        testCaseDAO.insertTestCase(id3, "racecar","racecar", false);
        System.out.println("✅ Problem 3 inserted: Reverse a String");

        // ── Problem 4: Even or Odd ──────────────────────────────
        Problem p4 = new Problem();
        p4.setTitle("Even or Odd");
        p4.setStatement(
            "Given an integer, print Even if it is even, otherwise print Odd.\n\n" +
            "Example:\nInput: 4\nOutput: Even"
        );
        p4.setDifficulty("Easy");
        p4.setTimeLimitMs(2000);
        p4.setMemoryLimitMb(256);
        p4.setTags("math,basics");
        problemDAO.insertProblem(p4);
        int id4 = getLastId(problemDAO);

        testCaseDAO.insertTestCase(id4, "4",  "Even", true);
        testCaseDAO.insertTestCase(id4, "7",  "Odd",  false);
        testCaseDAO.insertTestCase(id4, "0",  "Even", false);
        System.out.println("✅ Problem 4 inserted: Even or Odd");

        // ── Problem 5: Factorial ────────────────────────────────
        Problem p5 = new Problem();
        p5.setTitle("Factorial");
        p5.setStatement(
            "Given a non-negative integer N, print its factorial.\n\n" +
            "Example:\nInput: 5\nOutput: 120"
        );
        p5.setDifficulty("Medium");
        p5.setTimeLimitMs(2000);
        p5.setMemoryLimitMb(256);
        p5.setTags("math,recursion");
        problemDAO.insertProblem(p5);
        int id5 = getLastId(problemDAO);

        testCaseDAO.insertTestCase(id5, "5",  "120", true);
        testCaseDAO.insertTestCase(id5, "0",  "1",   false);
        testCaseDAO.insertTestCase(id5, "10", "3628800", false);
        System.out.println("✅ Problem 5 inserted: Factorial");

        System.out.println("\n🎉 All problems seeded successfully!");
    }

    // Helper to get the last inserted problem's ID
    private static int getLastId(ProblemDAO dao) {
        var all = dao.getAllProblems();
        return all.get(all.size() - 1).getProblemId();
    }
}