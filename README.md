# Mini Online Judge

A desktop-based competitive programming platform built with Java, JavaFX, and PostgreSQL. Students can browse coding problems, write solutions in a built-in code editor, and receive instant automated verdicts by compiling and running their code against hidden test cases in real time — bringing the core LeetCode experience to a standalone Java desktop app.

> Note: This is a simplified version built as an academic project for a Java Programming course. Features like user authentication, leaderboards, and an admin panel are not implemented in this version.

---

## Features

- Browse coding problems filtered by difficulty (Easy, Medium, Hard)
- Built-in code editor with starter templates for Java, Python, and C++
- Real-time auto judge that compiles and runs submitted code against hidden test cases
- Instant verdicts: Accepted, Wrong Answer, Time Limit Exceeded, Compilation Error, Runtime Error
- All problems and submissions stored in a cloud PostgreSQL database
- Dark-themed JavaFX desktop UI

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 17                             |
| GUI          | JavaFX 21                           |
| Database     | PostgreSQL hosted on Neon.tech      |
| DB Driver    | JDBC with HikariCP connection pool  |
| Build Tool   | Maven                               |
| Judge Engine | Java ProcessBuilder                 |

---

## Requirements

Make sure the following are installed before running the project:

- JDK 17 or higher
  Download: https://adoptium.net

- Apache Maven 3.6 or higher
  Download: https://maven.apache.org/download.cgi
  Setup guide: https://maven.apache.org/install.html

- A free Neon.tech PostgreSQL database (no local PostgreSQL installation needed)
  Sign up: https://neon.tech

All Java library dependencies (JavaFX, HikariCP, PostgreSQL JDBC driver, etc.) are declared in pom.xml and will be downloaded automatically by Maven on first build.

---

## Project Structure

```
mini-online-judge/
├── src/
│   └── main/
│       ├── java/com/minijudge/
│       │   ├── App.java                  Entry point, all JavaFX screens
│       │   ├── model/
│       │   │   ├── Problem.java
│       │   │   └── Submission.java
│       │   ├── dao/
│       │   │   ├── ProblemDAO.java
│       │   │   ├── SubmissionDAO.java
│       │   │   └── TestCaseDAO.java
│       │   ├── judge/
│       │   │   └── JudgeEngine.java      Core judging logic
│       │   └── util/
│       │       ├── DBConnection.java     HikariCP connection pool setup
│       │       ├── CreateTables.java     Creates all database tables
│       │       ├── SeedData.java         Inserts sample problems
│       │       └── FixTestCases.java     Utility to clean test case data
│       └── resources/
│           └── db.properties            Database credentials (not committed)
├── pom.xml
├── .gitignore
└── README.md
```

---

## Setup and Installation

### Step 1 - Clone the repository

```bash
git clone https://github.com/yourusername/mini-online-judge.git
cd mini-online-judge
```

### Step 2 - Set up the database

1. Go to https://neon.tech and create a free account
2. Create a new project and name it minijudge
3. Copy the connection string from the dashboard

Create the file src/main/resources/db.properties and fill in your details:

```properties
db.url=jdbc:postgresql://your-neon-host/neondb?sslmode=require
db.username=your_username
db.password=your_password
db.pool.size=5
```

### Step 3 - Install dependencies

```bash
mvn clean install
```

This downloads all required libraries automatically. You should see BUILD SUCCESS at the end.

### Step 4 - Create database tables

```bash
mvn compile exec:java "-Dexec.mainClass=com.minijudge.util.CreateTables"
```

### Step 5 - Seed sample problems

```bash
mvn compile exec:java "-Dexec.mainClass=com.minijudge.util.SeedData"
```

### Step 6 - Run the application

```bash
mvn javafx:run
```

---

## How to Run (Quick Reference)

If the project is already set up, just run:

```bash
mvn javafx:run
```

If you want a clean rebuild before running:

```bash
mvn clean javafx:run
```

---

## How the Judge Works

1. The user writes code in the editor and clicks Submit
2. The code is written to a temporary directory on disk
3. For Java submissions, javac compiles the code using ProcessBuilder
4. The compiled program is executed against each hidden test case
5. The actual output is compared to the expected output after trimming whitespace
6. The final verdict is saved to PostgreSQL and displayed to the user

### Verdict Types

| Verdict | Meaning                                  |
|---------|------------------------------------------|
| AC      | Accepted - all test cases passed         |
| WA      | Wrong Answer - output did not match      |
| TLE     | Time Limit Exceeded - ran too slowly     |
| CE      | Compilation Error - code did not compile |
| RE      | Runtime Error - program crashed          |

---

## Database Schema

| Table        | Purpose                                         |
|--------------|-------------------------------------------------|
| problems     | Problem title, statement, difficulty, limits    |
| test_cases   | Input and expected output per problem           |
| submissions  | User submissions with verdict and runtime       |
| users        | User accounts (created but not used currently)  |
| leaderboard  | Score tracking (created but not used currently) |

---

## Sample Problems Included

| ID | Title              | Difficulty |
|----|--------------------|------------|
| 1  | Hello World        | Easy       |
| 2  | Sum of Two Numbers | Easy       |
| 3  | Reverse a String   | Easy       |
| 4  | Even or Odd        | Easy       |
| 5  | Factorial          | Medium     |

---

## Known Limitations

This is a simplified academic version of an online judge. The following features are not implemented:

- User authentication and registration
- Leaderboard and scoring system
- Admin panel for adding or editing problems
- Syntax highlighting in the code editor
- Runtime and memory usage display on the verdict screen
- Multi-language support is partial: Java works fully, Python and C++ depend on the respective runtime being installed on the machine

---

## Possible Future Improvements

- Add login and registration with BCrypt password hashing
- Build an admin panel to manage problems and test cases
- Integrate RSyntaxTextArea for syntax highlighting in the editor
- Display per-test-case results instead of a single overall verdict
- Add a leaderboard ranked by problems solved and submission speed

---

## Author

Built as an Innovative Assignment for the Java Programming course.

---

## License

This project is for educational purposes only.
