package com.mtv.encode.cfg.build;

import com.mtv.encode.cfg.node.*;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.ast.ASTFactory;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;


public class ControlFlowGraph {
    protected IASTFunctionDefinition func;
    protected CFGNode start;
    protected CFGNode exit;

    public ControlFlowGraph() {
    }

    public ControlFlowGraph(IASTFunctionDefinition def) {
        ControlFlowGraph cfg = build(def);
        start = cfg.getStart();
        exit = cfg.getExit();
        func = def;
    }

    public ControlFlowGraph(CFGNode start, CFGNode exit) {
        this.start = start;
        this.exit = exit;
    }

    public ControlFlowGraph(IASTFunctionDefinition def, ASTFactory ast, boolean includeGlobalVars) {
        ControlFlowGraph cfg = build(def, ast, includeGlobalVars);
        if (cfg == null) {
            DebugHelper.print("CFG is empty");
            System.exit(1);
        }
        start = cfg.getStart();
        exit = cfg.getExit();
        func = def;
    }

    private static void printSpace(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }
    }

    private static void print(CFGNode start, int level) {
        if (start != null) {
            printSpace(level);
        }
        if (start == null) {
            return;
        } else if (start instanceof EmptyNode) {
            start.printNode();
            print(start.getNext(), level);
        } else if (start instanceof EndNode) {
            start.printNode();
            print(start.getNext(), level);
        } else if (start instanceof CreateThreadNode){
            start.printNode();
            print(start.getNext(), level);
        } else if (start instanceof JoinThreadNode) {
            start.printNode();
            print(start.getNext(), level);
        } else if (start instanceof BeginFunctionNode) {
            start.printNode();
            print(start.getNext(), level);
        } else {
            start.printNode();
            print(start.getNext(), level);
        }
    }

    public void concat(ControlFlowGraph other) {
        if (start == null || exit == null) {
            start = other.start;
        } else {
            exit.setNext(other.start);

        }
        exit = other.exit;
    }

    public ControlFlowGraph build(IASTFunctionDefinition def) {
        return (new ControlFlowGraphBuilder()).build(def);
    }

    public ControlFlowGraph build(IASTFunctionDefinition def, ASTFactory ast, boolean includeGlobalVars) {
        MultiFunctionCFGBuilder multiCFG = new MultiFunctionCFGBuilder(ast);
        ControlFlowGraph cfg = multiCFG.build(def, includeGlobalVars);
        return cfg;
    }

    public CFGNode getStart() {
        return start;
    }

    public void setStart(CFGNode start) {
        this.start = start;
    }

    public CFGNode getExit() {
        return exit;
    }

    public void printGraph() {
        if (this != null)
            System.out.println("==================================================");
            System.out.println("=========||||||||====||||||||====||||||||=========");
            System.out.println("=========||==========||==========||===============");
            System.out.println("=========||==========||||========||==||||=========");
            System.out.println("=========||==========||==========||====||=========");
            System.out.println("=========||||||||====||==========||||||||=========");
            System.out.println("==================================================");
            print(start, 0);
            System.out.println("==================================================");
            System.out.println("=========||||||||====||||||||====||||||||=========");
            System.out.println("=========||==========||==========||===============");
            System.out.println("=========||==========||||========||==||||=========");
            System.out.println("=========||==========||==========||====||=========");
            System.out.println("=========||||||||====||==========||||||||=========");
            System.out.println("==================================================");
    }

    public void printGraph(int level) {
        if (this != null) {
            print(start, level);
        }
    }
}
