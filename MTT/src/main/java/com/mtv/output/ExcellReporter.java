package com.mtv.output;


import java.time.LocalDateTime;
import java.util.ArrayList;

public class ExcellReporter {
    public String programName;
    public LocalDateTime dateTime;
    public ArrayList<String> sourceCode;
    public String verificationResult;
    public long constraintsGenerated;
    public long consGenTime;
    public long solveTime;

    public ExcellReporter(String programName, LocalDateTime dateTime, ArrayList<String> sourceCode,
                          String verificationResult, long constraintsGenerated,
                          long consGenTime, long solveTime) {
        this.programName = programName;
        this.dateTime = dateTime;
        this.sourceCode = sourceCode;
        this.verificationResult = verificationResult;
        this.constraintsGenerated = constraintsGenerated;
        this.consGenTime = consGenTime;
        this.solveTime = solveTime;
    }
}
