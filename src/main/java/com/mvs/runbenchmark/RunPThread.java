package com.mvs.runbenchmark;

import com.mvs.app.verification.ExportExcel;
import com.mvs.app.verification.FileVerification;
import com.mvs.app.verification.FunctionVerification;
import com.mvs.app.verification.report.VerificationReport;
import jxl.write.DateFormat;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RunPThread {

    public static void main(String[] args) throws WriteException, IOException {
        //get current Date time string
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String dateStr = dateFormat.format(date);
        ExportExcel exportExcel = new ExportExcel("mvs Report" + dateStr + ".xls");

        File file = new File("./src/main/resources/benchmark/pthread/triangular_1.c");
        FileVerification fv = new FileVerification();
        List<VerificationReport> reportList = fv.verifyDirectory(file, FunctionVerification.UNFOLD_MODE);
        exportExcel.writeExcel(reportList);
    }
}
