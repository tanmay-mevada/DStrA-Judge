package com.minijudge.model;

public class Problem {
    private int problemId;
    private String title;
    private String statement;
    private String difficulty;
    private int timeLimitMs;
    private int memoryLimitMb;
    private String tags;

    public Problem() {}

    public Problem(int problemId, String title, String statement,
                   String difficulty, int timeLimitMs, int memoryLimitMb, String tags) {
        this.problemId = problemId;
        this.title = title;
        this.statement = statement;
        this.difficulty = difficulty;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitMb = memoryLimitMb;
        this.tags = tags;
    }

    public int getProblemId()        { return problemId; }
    public String getTitle()         { return title; }
    public String getStatement()     { return statement; }
    public String getDifficulty()    { return difficulty; }
    public int getTimeLimitMs()      { return timeLimitMs; }
    public int getMemoryLimitMb()    { return memoryLimitMb; }
    public String getTags()          { return tags; }

    public void setProblemId(int problemId)       { this.problemId = problemId; }
    public void setTitle(String title)            { this.title = title; }
    public void setStatement(String statement)    { this.statement = statement; }
    public void setDifficulty(String difficulty)  { this.difficulty = difficulty; }
    public void setTimeLimitMs(int timeLimitMs)   { this.timeLimitMs = timeLimitMs; }
    public void setMemoryLimitMb(int mb)          { this.memoryLimitMb = mb; }
    public void setTags(String tags)              { this.tags = tags; }

    @Override
    public String toString() { return title; }
}