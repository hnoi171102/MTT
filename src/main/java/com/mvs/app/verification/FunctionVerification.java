package com.mvs.app.verification;

import com.mvs.app.solver.SMTInput;
import com.mvs.app.solver.Z3Runner;
import com.mvs.app.verification.report.Report;
import com.mvs.app.verification.report.VerificationReport;
import com.mvs.app.verification.userassertion.UserInput;
import com.mvs.cfg.build.ASTFactory;
import com.mvs.cfg.build.mvsCFG;
import com.mvs.cfg.index.Var;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FunctionVerification {
    static String SMTINPUT_DIR = "./src/main/resources/smt/";
    public static int UNFOLD_MODE = 0;
    public static int INVARIANT_MODE = 1;

    public FunctionVerification() {
    }

    /**
     * verify a function with pre-condition and post-condition
     * @param function: function to verify
     * @param preCondition
     * @param postCondition
     * @return	verification report
     * @throws IOException
     */

    public static VerificationReport verify(ASTFactory ast, IASTFunctionDefinition function, String preCondition, String postCondition, int nLoops, int mode)
            throws IOException {

        long begin = System.currentTimeMillis();

        mvsCFG cfg = new mvsCFG(function, ast);
        SMTInput smtInput;
        if (mode == INVARIANT_MODE) {
            cfg.invariant();
            cfg.index();
            smtInput = new SMTInput(cfg.getVm().getVariableList(), cfg.createInvariantFormula());
        } else {
//            cfg.ungoto();
            cfg.unfold(nLoops);
            //TuyenKV
            //cfg.printGraph();
            //cfg.unfold(10);
            cfg.index();
            //cfg.printGraph();
            smtInput = new SMTInput(cfg.getVm().getInputList(), cfg.createFormulas());
        }

        // java.cfg.printGraph();
        // java.cfg.printMeta();
        // java.cfg.printFormular(System.out);

        List<String> constraints = new ArrayList<>();
        UserInput userInput = new UserInput();
        ArrayList<Var> params = cfg.getInitVariables();
        params.add(new Var(cfg.getTypeFunction(), "return"));
        userInput.setParameter(params);

        // add pre-condition
        String constraintTemp;
        if (preCondition != null && !preCondition.equals("")) {
            constraintTemp = userInput.createUserAssertion(preCondition, cfg.getNameFunction(), cfg.getVm());
            constraints.add(constraintTemp);
        }

        // add user's assertion
        constraintTemp = userInput.createUserAssertion(postCondition,cfg.getNameFunction(), cfg.getVm());
        constraintTemp = "(not " + constraintTemp + ")";
        constraints.add(constraintTemp);

        smtInput.setConstrainst(constraints);

        long end = System.currentTimeMillis();

        String functionName = cfg.getNameFunction();
        String path = SMTINPUT_DIR + functionName + ".txt";

        FileOutputStream fo = new FileOutputStream(new File(path));
        smtInput.printInputToOutputStreamAssert(fo);

        List<String> result = Z3Runner.runZ3(path);
        
        Report report = new Report();
        report.setListParameter(cfg.getInitVariables());
        report.setFunctionName(cfg.getNameFunction());
        if (nLoops < 100) mode = FunctionVerification.INVARIANT_MODE; //change from true* to true
        report.setMode(mode);
        VerificationReport verReport = report.generateReport(result);

        verReport.print();
        System.out.println("Z3 result:");
        result.forEach(System.out::println);

        verReport.setFunctionName(cfg.getNameFunction());
        verReport.setGenerateConstraintTime((int)(end-begin));
        verReport.setPreCondition(preCondition);
        verReport.setPostCondition(postCondition);
        verReport.setSmtInput(smtInput);


        return verReport;
    }
    public static VerificationReport verify(ASTFactory ast, IASTFunctionDefinition function, String preCondition, String postCondition, int nLoops, int mode, File smtFile)
            throws IOException {

        long begin = System.currentTimeMillis();

        mvsCFG cfg = new mvsCFG(function, ast);
        SMTInput smtInput;
        if (mode == INVARIANT_MODE) {
            cfg.invariant();
            cfg.index();
            smtInput = new SMTInput(cfg.getVm().getVariableList(), cfg.createInvariantFormula());
        } else {
//            cfg.ungoto();
            cfg.unfold(nLoops);
//            cfg.unfold(10);
            cfg.index();
            smtInput = new SMTInput(cfg.getVm().getVariableList(), cfg.createFormulas() );
        }

        // java.cfg.printGraph();
        // java.cfg.printMeta();
        // java.cfg.printFormular(System.out);

        List<String> constraints = new ArrayList<>();
        UserInput userInput = new UserInput();
        ArrayList<Var> params = cfg.getInitVariables();
        params.add(new Var(cfg.getTypeFunction(), "return"));
        userInput.setParameter(params);

        // add pre-condition
        String constraintTemp;
        if (preCondition != null && !preCondition.equals("")) {
            constraintTemp = userInput.createUserAssertion(preCondition, cfg.getNameFunction(), cfg.getVm());
            constraints.add(constraintTemp);
        }

        // add user's assertion
        constraintTemp = userInput.createUserAssertion(postCondition,cfg.getNameFunction(), cfg.getVm());
        constraintTemp = "(not " + constraintTemp + ")";
        constraints.add(constraintTemp);

        smtInput.setConstrainst(constraints);

        long end = System.currentTimeMillis();

        String path = smtFile.getAbsolutePath();

        FileOutputStream fo = new FileOutputStream(smtFile);
        smtInput.printInputToOutputStreamAssert(fo);

        List<String> result = Z3Runner.runZ3(path);

        Report report = new Report();
        report.setListParameter(cfg.getInitVariables());
        report.setFunctionName(cfg.getNameFunction());
        if (nLoops < 100) mode = FunctionVerification.INVARIANT_MODE; //change from true* to true
        report.setMode(mode);
        VerificationReport verReport = report.generateReport(result);

        verReport.print();
        System.out.println("Z3 result:");
        result.forEach(System.out::println);

        verReport.setFunctionName(cfg.getNameFunction());
        verReport.setGenerateConstraintTime((int)(end-begin));
        verReport.setPreCondition(preCondition);
        verReport.setPostCondition(postCondition);
        verReport.setSmtInput(smtInput);

        return verReport;
    }
}
