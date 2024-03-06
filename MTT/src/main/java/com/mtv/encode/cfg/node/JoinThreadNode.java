package com.mtv.encode.cfg.node;

import com.mtv.debug.DebugHelper;

public class JoinThreadNode extends CFGNode {
    public JoinThreadNode(String threadReference, String retvalExpression) {
        this.threadReference = threadReference;
        this.retvalExpression = retvalExpression;
    }
    public String threadReference;
    public String retvalExpression;
    @Override
    public void printNode() {
        DebugHelper.print("Join thread: ");
        DebugHelper.print("\tThread reference: " + threadReference);
        DebugHelper.print("\tRetval expression: " + retvalExpression);

    }
}
