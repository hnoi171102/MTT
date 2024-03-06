package com.mvs.cfg.node;

import com.mvs.cfg.index.Indexer;
import com.mvs.cfg.index.VariableManage;
import com.mvs.cfg.utils.ExpressionHelper;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class InvariantNode extends PlainNode {

    public InvariantNode() {
    }
    public InvariantNode(IASTStatement stm) {
        super(stm);
    }

    public InvariantNode(IASTStatement stm, IASTFunctionDefinition func) {
        super(stm, func);
    }

    public void index(VariableManage vm) {
        super.setStatement((IASTStatement) Indexer.indexInvariant(super.getStatement(), vm));
    }

    public void printNode() {
        if (super.getStatement() != null) {
            String expresstion_type = super.getStatement().getClass().getSimpleName();
            System.out.print("InvariantNode: ");
            System.out.println(ExpressionHelper.toString(super.getStatement()));
        } else System.out.println(this);

    }
}
