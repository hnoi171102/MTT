package com.mvs.runbenchmark;

import com.mvs.app.verification.ExportExcel;
import com.mvs.app.verification.FileVerification;
import com.mvs.app.verification.report.VerificationReport;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RunReachSafety {
    static String FLOAT_CDFPL = "./src/main/resources/benchmark/float-cdfpl-func";
    static String FLOAT_BENCHS = "./benchmark/float-benchs";
    static String PTHREAD = "./src/main/resources/benchmark/pthread";
    static String ECA_RERS = "./benchmark/eca-rers2012";

    public static void main(String[] args) throws RowsExceededException, WriteException, IOException {
        ExportExcel exportExcel = new ExportExcel();
        File file = new File(PTHREAD);
        FileVerification fv = new FileVerification();
        List<VerificationReport> reportList = fv.verifyDirectory(file);
        exportExcel.writeExcel(reportList);
    }
}
