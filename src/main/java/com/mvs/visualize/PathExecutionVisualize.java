package com.mvs.visualize;

import com.mvs.app.solver.SMTInput;
import com.mvs.app.solver.Z3Runner;
import com.mvs.app.verification.FunctionVerification;
import com.mvs.app.verification.report.DefineFun;
import com.mvs.app.verification.report.Report;
import com.mvs.app.verification.report.VerificationReport;
import com.mvs.app.verification.userassertion.UserInput;
import com.mvs.cfg.build.mvsCFG;
import com.mvs.cfg.index.FormulaCreater;
import com.mvs.cfg.index.Var;
import com.mvs.cfg.index.VariableManage;
import com.mvs.cfg.node.CFGNode;
import com.mvs.cfg.node.DecisionNode;
import com.mvs.cfg.utils.MyCloner;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PathExecutionVisualize {
    mvsCFG mvsCFG;
    int nLoops;
    VerificationReport verificationReport;
    List<List<CFGNode>> listPaths;
    List<VerificationReport> listvr;
    List<CFGNode> nodes;
    List<String> formulas;
    int number;

    public PathExecutionVisualize(mvsCFG mvsCFG, VerificationReport verificationReport) {
        this.mvsCFG = mvsCFG;
        this.verificationReport = verificationReport;
        nodes = new ArrayList<>();
        formulas = new ArrayList<>();
        listPaths = new ArrayList<>();
        listvr = new ArrayList<>();
        number = 0;
    }
    public List<CFGNode> findPathToFail() throws IOException {
        if(this.verificationReport.getParameters() == null){
            return new ArrayList<CFGNode>();
        }
        mvsCFG cfg = this.mvsCFG;
        UserInput userInput = new UserInput();
        ArrayList<Var> params = cfg.getInitVariables();
        params.add(new Var(cfg.getTypeFunction(), "return"));
        userInput.setParameter(params);

        CFGNode start = cfg.getStart();
        CFGNode iter = start;
        List<CFGNode> nodes = new ArrayList<>();

        SMTInput smtInput = this.verificationReport.getSmtInput();
        List<String> formulas = new ArrayList<>();
        for(DefineFun param: this.verificationReport.getParameters()){
            String temp = userInput.createZ3Assertion(param.getExpression(), cfg.getNameFunction(), cfg.getVm());
            formulas.add(temp);
        }
        while(iter != null){
            nodes.add(iter);
            if (iter instanceof DecisionNode){
                IASTExpression condition = ((DecisionNode) iter).getCondition();
                String conditionStr = FormulaCreater.createFormula(condition);
                String notConditionStr = FormulaCreater.createFormula(FormulaCreater.NEGATIVE, conditionStr);
//                String conditionStrWithIndex = userInput.createUserAssertion(conditionStr, cfg.getNameFunction());
//                String notConditionStrWithIndex = userInput.createUserAssertion(notConditionStr, cfg.getNameFunction());

                formulas.add(conditionStr);
                SMTInput decisionNodeSMTInput = new SMTInput(smtInput.getVariableList(),formulas);
//                File tempFile = File.createTempFile("temp", ".tmp");
//                FileOutputStream fo = new FileOutputStream(tempFile);
                File tempFile = new File("smt/temp.txt");
                FileOutputStream fo = new FileOutputStream(tempFile);
                decisionNodeSMTInput.printInputToOutputStreamAssert(fo);

                List<String> result = Z3Runner.runZ3(tempFile.getAbsolutePath());
                Report report = new Report();
                report.setListParameter(cfg.getInitVariables());
                report.setFunctionName(cfg.getNameFunction());
                int mode = 0;
                if (nLoops < 100) mode = FunctionVerification.INVARIANT_MODE; //change from true* to true
                report.setMode(mode);
                VerificationReport verReport = report.generateReport(result);

                if(verReport.getStatus().equals(VerificationReport.FALSE)){
                    iter = ((DecisionNode) iter).getThenNode();
                } else {
                    //remove last formula. Last formula is conditionStr
                    formulas.remove(formulas.size()-1);
                    //add notConditionStr
                    formulas.add(notConditionStr);
                    iter = ((DecisionNode) iter).getElseNode();
                }

            } else {
                String currentFormula = iter.getFormula();
                if (currentFormula != null) {
                    formulas.add(currentFormula);
                }
                iter = iter.getNext();
            }
        }
        return nodes;
    }

    public void findPathsToFail(String preCondition, String postCondition) throws IOException {
        if(this.verificationReport.getParameters() == null){
            return;
        }
        mvsCFG cfg = this.mvsCFG;

        CFGNode start = cfg.getStart();
        CFGNode iter = start;
        dfs(iter, preCondition, postCondition, cfg.getVm());


    }
    public void dfs(CFGNode iter, String preCondition, String postCondition, VariableManage vm) throws IOException {
        if(iter == null){
            mvsCFG cfg =this.mvsCFG;
            SMTInput smtInput = this.verificationReport.getSmtInput();
            SMTInput decisionNodeSMTInput = new SMTInput(smtInput.getVariableList(),formulas);

            List<String> constraints = new ArrayList<>();
            UserInput userInput = new UserInput();
            ArrayList<Var> params = cfg.getInitVariables();
            params.add(new Var(cfg.getTypeFunction(), "return"));
            userInput.setParameter(params);

            // add pre-condition
            String constraintTemp;
            if (preCondition != null && !preCondition.equals("")) {
                constraintTemp = userInput.createUserAssertion(preCondition, cfg.getNameFunction(), vm);
                constraints.add(constraintTemp);
            }

            // add user's assertion
            constraintTemp = userInput.createUserAssertion(postCondition,cfg.getNameFunction(), vm);
            constraintTemp = "(not " + constraintTemp + ")";
            constraints.add(constraintTemp);

            decisionNodeSMTInput.setConstrainst(constraints);

            File tempFile = new File("smt/temp.txt");
            FileOutputStream fo = new FileOutputStream(tempFile);
            decisionNodeSMTInput.printInputToOutputStreamAssert(fo);

            List<String> result = Z3Runner.runZ3(tempFile.getAbsolutePath());
            Report report = new Report();
            report.setListParameter(cfg.getInitVariables());
            report.setFunctionName(cfg.getNameFunction());
            int mode = 0;
            if (nLoops < 100) mode = FunctionVerification.INVARIANT_MODE; //change from true* to true
            report.setMode(mode);
            VerificationReport verReport = report.generateReport(result);
            if (verReport.getStatus().equals(VerificationReport.FALSE)){
                this.getListPaths().add(new ArrayList<>(nodes));
                this.getListvr().add(MyCloner.clone(verReport));
                number++;
            }
            return;
        }
        int nodeStartIndex = nodes.size();
        int formulaStartIndex = formulas.size();
        while(iter != null && !(iter instanceof DecisionNode)){
            nodes.add(iter);
            String currentFormula = iter.getFormula();
            if (currentFormula != null) {
                formulas.add(currentFormula);
            }
            iter = iter.getNext();
        }
        if(iter != null){
            nodes.add(iter);
            IASTExpression condition = ((DecisionNode) iter).getCondition();
            String conditionStr = FormulaCreater.createFormula(condition);
            String notConditionStr = FormulaCreater.createFormula(FormulaCreater.NEGATIVE, conditionStr);

            formulas.add(conditionStr);
            dfs(((DecisionNode) iter).getThenNode(), preCondition, postCondition, vm);
            formulas.remove(formulas.size()-1);
            formulas.add(notConditionStr);
            dfs(((DecisionNode) iter).getElseNode(), preCondition, postCondition, vm);
        } else {
            dfs(iter, preCondition, postCondition, vm);
        }
        nodes = nodes.subList(0, nodeStartIndex);
        formulas = formulas.subList(0, formulaStartIndex);
    }
    public static void print(List<String> formulas){
        for(String formula: formulas){
            System.out.println(formula);
        }
    }

    public List<List<CFGNode>> getListPaths() {
        return listPaths;
    }

    public void setListPaths(List<List<CFGNode>> listPaths) {
        this.listPaths = listPaths;
    }

    public List<VerificationReport> getListvr() {
        return listvr;
    }

    public void setListvr(List<VerificationReport> listvr) {
        this.listvr = listvr;
    }
}
