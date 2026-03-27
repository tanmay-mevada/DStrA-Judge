package com.minijudge.model;

public class Submission {
    private int subId;
    private int problemId;
    private String language;
    private String code;
    private String verdict;
    private int runtimeMs;

    public Submission() {}

    public Submission(int problemId, String language, String code) {
        this.problemId = problemId;
        this.language = language;
        this.code = code;
        this.verdict = "PENDING";
    }

    public int getSubId()         { return subId; }
    public int getProblemId()     { return problemId; }
    public String getLanguage()   { return language; }
    public String getCode()       { return code; }
    public String getVerdict()    { return verdict; }
    public int getRuntimeMs()     { return runtimeMs; }

    public void setSubId(int subId)         { this.subId = subId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }
    public void setLanguage(String lang)    { this.language = lang; }
    public void setCode(String code)        { this.code = code; }
    public void setVerdict(String verdict)  { this.verdict = verdict; }
    public void setRuntimeMs(int ms)        { this.runtimeMs = ms; }
}