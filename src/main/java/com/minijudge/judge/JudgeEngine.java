package com.minijudge.judge;

import com.minijudge.dao.SubmissionDAO;
import com.minijudge.dao.TestCaseDAO;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class JudgeEngine {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final TestCaseDAO testCaseDAO = new TestCaseDAO();

    public String judge(int subId, int problemId, String language, String code, int timeLimitMs) {

        String verdict = "AC";
        long totalRuntime = 0;

        try {
            // Step 1 — Write code to a temp file
            Path tempDir = Files.createTempDirectory("judge_");
            String fileName = getFileName(language);
            Path sourceFile = tempDir.resolve(fileName);
            Files.writeString(sourceFile, code);

            // Step 2 — Compile (for Java)
            if (language.equals("Java")) {
                ProcessBuilder compilePb = new ProcessBuilder("javac", sourceFile.toString())
                        .directory(tempDir.toFile())
                        .redirectErrorStream(true);
                compilePb.environment().remove("_JAVA_OPTIONS");
                compilePb.environment().remove("JAVA_TOOL_OPTIONS");
                compilePb.environment().remove("JDK_JAVA_OPTIONS");
                Process compile = compilePb.start();

                String compileOutput = new String(compile.getInputStream().readAllBytes());
                int exitCode = compile.waitFor();

                if (exitCode != 0) {
                    submissionDAO.updateVerdict(subId, "CE", 0);
                    cleanup(tempDir);
                    return "CE"; // Compilation Error
                }
            }

            // Step 3 — Get test cases from DB
            List<String[]> testCases = testCaseDAO.getTestCases(problemId);

            if (testCases.isEmpty()) {
                submissionDAO.updateVerdict(subId, "AC", 0);
                cleanup(tempDir);
                return "AC";
            }

            // Step 4 — Run against each test case
            for (String[] tc : testCases) {
                String input = tc[0];
                String expectedOutput = tc[1].trim();

                ProcessBuilder pb = buildRunCommand(language, tempDir, fileName);
                pb.directory(tempDir.toFile());
                pb.redirectErrorStream(true);

                long start = System.currentTimeMillis();
                Process run = pb.start();

                // Feed input
                try (OutputStream os = run.getOutputStream()) {
                    os.write(input.getBytes());
                }

                // Wait with time limit
                boolean finished = run.waitFor(timeLimitMs, java.util.concurrent.TimeUnit.MILLISECONDS);
                long elapsed = System.currentTimeMillis() - start;
                totalRuntime = Math.max(totalRuntime, elapsed);

                if (!finished) {
                    run.destroyForcibly();
                    verdict = "TLE"; // Time Limit Exceeded
                    break;
                }

                // Read output
                String actualOutput = new String(run.getInputStream().readAllBytes()).trim();

                if (!actualOutput.equals(expectedOutput)) {
                    verdict = "WA"; // Wrong Answer
                    break;
                }
            }

            // Step 5 — Save verdict to DB
            submissionDAO.updateVerdict(subId, verdict, (int) totalRuntime);
            cleanup(tempDir);

        } catch (Exception e) {
            System.err.println("❌ Judge error: " + e.getMessage());
            verdict = "RE"; // Runtime Error
            submissionDAO.updateVerdict(subId, "RE", 0);
        }

        return verdict;
    }

    private String getFileName(String language) {
        return switch (language) {
            case "Java" -> "Main.java";
            case "Python" -> "main.py";
            case "C++" -> "main.cpp";
            default -> "Main.java";
        };
    }

    private ProcessBuilder buildRunCommand(String language, Path dir, String fileName) {
        ProcessBuilder pb = switch (language) {
            case "Java" -> new ProcessBuilder("java", "-XX:+UseSerialGC", "Main").directory(dir.toFile());
            case "Python" -> new ProcessBuilder("python", dir.resolve(fileName).toString());
            case "C++" -> new ProcessBuilder(dir.resolve("main").toString());
            default -> new ProcessBuilder("java", "Main").directory(dir.toFile());
        };

        // Remove _JAVA_OPTIONS so it doesn't pollute stdout
        pb.environment().remove("_JAVA_OPTIONS");
        pb.environment().remove("JAVA_TOOL_OPTIONS");
        pb.environment().remove("JDK_JAVA_OPTIONS");

        return pb;
    }

    private void cleanup(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("⚠️ Cleanup failed: " + e.getMessage());
        }
    }
}