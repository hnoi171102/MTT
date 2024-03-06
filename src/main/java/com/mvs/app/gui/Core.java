package com.mvs.app.gui;

import com.mvs.app.solver.SMTInput;
import com.mvs.app.solver.Z3Runner;
import com.mvs.app.utils.PrefixToInfix;
import com.mvs.app.verification.userassertion.UserInput;
import com.mvs.cfg.build.ASTFactory;
import com.mvs.cfg.build.ControlFlowGraph;
import com.mvs.cfg.build.mvsCFG;
import com.mvs.cfg.index.Var;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Core {
    private String[] methodSignatures;
    private String filepath;

    public int getNLoops() {
        return nLoops;
    }

    public void setNLoops(int nLoops) {
        this.nLoops = nLoops;
    }

    private int nLoops = 1;
    int[] lineNumberOfMethods;
    static String SMTINPUT_DIR = "./src/main/resources/smt/";
    List<ControlFlowGraph> CFGList;
    ASTFactory ast;

    List<String> result;

    public String[] getMethodSignatures() {
        this.methodSignatures = ast.getFunctionSignatures().toArray(new String[0]);
        return methodSignatures;
    }

    public Core() {
    }

    public Core(String filepath) {
        this.filepath = filepath;
        ast = new ASTFactory(filepath);
    }


    public List<String> runSolver(String userAssertion, String preCondition) throws Exception {

        //	int index = find(methodSignatures, methodSignature);
        int numberOfMethods = methodSignatures.length;
        System.out.println("number of methods: " + numberOfMethods);
        //get last function
        if (numberOfMethods < 1) {
            return null;
        }
        MainPanelWithSourceCodeHL.function = ast.getListFunction().get(numberOfMethods - 1);

        mvsCFG cfg = new mvsCFG(MainPanelWithSourceCodeHL.function, ast);
        PrintStream printStream;
        try {
            printStream = new PrintStream(new File("metaSMT.txt"));
            cfg.printMetaSMT(printStream);
            printStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        cfg.unfold(nLoops);
        cfg.index();

        SMTInput smtInput;
        smtInput = new SMTInput(cfg.getVm().getInputList(), cfg.createFormulas());

        String constraintTemp;

        List<String> constraints = new ArrayList<>();
        UserInput userInput = new UserInput();
        ArrayList<Var> params = cfg.getInitVariables();
        params.add(new Var(cfg.getTypeFunction(), "return"));
        userInput.setParameter(params);

        // add pre-condition
        if (preCondition != null && !preCondition.equals("")) {
            constraintTemp = userInput.createUserAssertion(preCondition, cfg.getNameFunction(), cfg.getVm());
            constraints.add(constraintTemp);
        }

        // add user's assertion
        constraintTemp = userInput.createUserAssertion(userAssertion, cfg.getNameFunction(), cfg.getVm());
        constraintTemp = "(not " + constraintTemp + ")";
        constraints.add(constraintTemp);

        smtInput.setConstrainst(constraints);

        //long end = System.currentTimeMillis();

        String functionName = cfg.getNameFunction();
        String path = SMTINPUT_DIR + functionName + ".txt";

        FileOutputStream fo = new FileOutputStream(new File(path));
        smtInput.printInputToOutputStreamAssert(fo);

        result = Z3Runner.runZ3(path);
        return analyseSolverResult(params);
    }

    public List<String> analyseSolverResult(ArrayList<Var> parameters) {
        List<String> result1 = new ArrayList<>();
        result1.add(result.get(0));

        for (Var v : parameters) {
            String varName = v.getName() + "_0";
            for (int i = 1; i < result.size(); i++) {

                if (result.get(i).contains(varName)) {
                    String valueLine = result.get(i + 1);

                    valueLine = getValue(valueLine);

                    result1.add(v.getName() + " = " + valueLine);
                    break;
                }
            }
        }

        for (int i = 1; i < result.size(); i++) {

            if (result.get(i).contains("return")) {
                String valueLine = result.get(i + 1);

                valueLine = getValue(valueLine);
                result1.add("return = " + valueLine);
                break;
            }
        }

        return result1;
    }

    private String getValue(String valueLine) {
        valueLine = valueLine.replace('(', ' ');
        valueLine = valueLine.replace(')', ' ');
        valueLine = valueLine.trim();

        return PrefixToInfix.prefixToInfix(valueLine);
    }

    public List<String> getSolverLog() {
        return result;
    }

    public int[] getLineNumberOfMethods() {
        if (lineNumberOfMethods == null) {
            System.err.println("null");
            System.exit(1);
        }
        return lineNumberOfMethods;
    }

    public Core setLoop(int nLoops) {
        setNLoops(nLoops);
        return this;
    }

    public void create()
            throws FileNotFoundException {

        List<IASTFunctionDefinition> functionList = ast.getListFunction();
        if (functionList == null) {
            System.out.println("null");
        }

        int nMethods = 0;
        if (functionList != null) {
            nMethods = functionList.size();
        }
        methodSignatures = new String[nMethods];
        lineNumberOfMethods = new int[nMethods];
        CFGList = new ArrayList<>();

        mvsCFG cfg = new mvsCFG();
        for (int i = 0; i < nMethods; i++) {
            methodSignatures[i] = functionList.get(i).getDeclarator().getName().toString();
            lineNumberOfMethods[i] = functionList.get(i).getDeclarator().getFileLocation().getStartingLineNumber();
            CFGList.add(cfg.build(functionList.get(i)));
        }
    }

}
