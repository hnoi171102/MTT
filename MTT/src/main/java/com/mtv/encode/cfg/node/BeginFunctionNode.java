package com.mtv.encode.cfg.node;

import com.mtv.encode.ast.FunctionHelper;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;

public class BeginFunctionNode extends CFGNode {
    private IASTFunctionDefinition funcDefinition;

    public BeginFunctionNode(IASTFunctionDefinition function) {
        funcDefinition = function;
    }
    public String getName() {
        return FunctionHelper.getFunctionName(funcDefinition);
    }

    @Override
    public void printNode() {
        System.out.println(getName() + " {");
    }

}
