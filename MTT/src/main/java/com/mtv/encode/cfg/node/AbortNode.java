package com.mtv.encode.cfg.node;

import com.mtv.debug.DebugHelper;

public class AbortNode extends CFGNode{
    @Override
    public void printNode() {
        DebugHelper.print("Abort node");
    }
}
