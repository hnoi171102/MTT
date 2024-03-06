package com.mtv.encode.ast;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;

public class IASTVariable {
    private IASTIdExpression var;
    private IASTDeclSpecifier type;

    public IASTVariable(IASTDeclSpecifier typeVar, IASTIdExpression varId) {
        type = typeVar;
        var = varId;
    }

    public IASTName getName() {
        return var.getName();
    }


}
