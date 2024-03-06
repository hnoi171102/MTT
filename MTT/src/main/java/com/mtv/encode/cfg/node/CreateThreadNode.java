package com.mtv.encode.cfg.node;

import com.mtv.encode.cfg.build.ControlFlowGraph;
import com.mtv.debug.DebugHelper;

public class CreateThreadNode extends CFGNode {

    public CreateThreadNode() {
        DebugHelper.print("Create thread node is created");
    }
    public CreateThreadNode(String threadReference, String attributesExpression, String funcReference, ControlFlowGraph funcCFG, String restrictionExpression) {
        this.threadReference = threadReference;
        this.attributesExpression = attributesExpression;
        this.funcReference = funcReference;
        this.funcCFG = funcCFG;
        this.restrictionExpression = restrictionExpression;
    }

    public String threadReference;
    public String attributesExpression;
    public String funcReference;

    public ControlFlowGraph funcCFG;
    public String restrictionExpression;

    @Override
    public void printNode() {
        DebugHelper.print("Create thread: ");
        DebugHelper.print("\tThread reference: " + threadReference);
        DebugHelper.print("\tAttributes expression: " + attributesExpression);
        DebugHelper.print("\tFunction reference: " + funcReference);
        funcCFG.printGraph(8);
        DebugHelper.print("\tRestriction expression: " + restrictionExpression);

    }
}
