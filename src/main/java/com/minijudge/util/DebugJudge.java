package com.minijudge.util;

import com.minijudge.dao.TestCaseDAO;
import java.util.List;
import java.io.*;
import java.nio.file.*;

public class DebugJudge {
    public static void main(String[] args) throws Exception {

        // Step 1 - Check what's in the test cases
        TestCaseDAO dao = new TestCaseDAO();
        List<String[]> cases = dao.getTestCases(1); // problem_id 1 = Hello World

        System.out.println("=== TEST CASES IN DB ===");
        for (String[] tc : cases) {
            System.out.println("Input:    [" + tc[0] + "]");
            System.out.println("Expected: [" + tc[1] + "]");
            System.out.println("Expected bytes: ");
            for (byte b : tc[1].getBytes()) {
                System.out.print(b + " ");
            }
            System.out.println();
        }

        // Step 2 - Compile and run the correct solution
        String code = """
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """;

        Path tempDir = Files.createTempDirectory("debug_");
        Path sourceFile = tempDir.resolve("Main.java");
        Files.writeString(sourceFile, code);

        Process compile = new ProcessBuilder("javac", sourceFile.toString())
            .directory(tempDir.toFile())
            .redirectErrorStream(true)
            .start();
        compile.waitFor();

        Process run = new ProcessBuilder("java", "Main")
            .directory(tempDir.toFile())
            .redirectErrorStream(true)
            .start();
        run.waitFor();

        String actualOutput = new String(run.getInputStream().readAllBytes());

        System.out.println("\n=== ACTUAL OUTPUT ===");
        System.out.println("Output: [" + actualOutput.trim() + "]");
        System.out.println("Output bytes: ");
        for (byte b : actualOutput.trim().getBytes()) {
            System.out.print(b + " ");
        }
        System.out.println();

        // Step 3 - Compare
        String expected = cases.get(0)[1].trim();
        String actual   = actualOutput.trim();
        System.out.println("\n=== COMPARISON ===");
        System.out.println("Match: " + actual.equals(expected));
        System.out.println("Expected length: " + expected.length());
        System.out.println("Actual length:   " + actual.length());
    }
}