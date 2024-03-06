package com.mtv.encode.cfg.node;

import com.mtv.encode.ast.FunctionHelper;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

public class EndFunctionNode extends CFGNode {
    private IASTFunctionDefinition func;

    public EndFunctionNode(IASTFunctionDefinition func) {
        this.func = func;
    }

    @Override
    public void printNode() {
        System.out.println("}  <--" + FunctionHelper.getFunctionName(func));
    }

    public IASTFunctionDefinition getFunc() {
        return func;
    }

    public void setFunc(IASTFunctionDefinition func) {
        this.func = func;
    }
}
