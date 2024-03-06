package com.mtv.encode.cfg.build;

import com.mtv.encode.ast.ASTNodeFactory;
import com.mtv.encode.ast.FunctionHelper;
import com.mtv.encode.cfg.node.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

public class ControlFlowGraphBuilder {
    public ControlFlowGraphBuilder() {

    }

    public ControlFlowGraph build(IASTFunctionDefinition def) {
        return createSubGraph(def.getBody(), def);
    }

    ControlFlowGraph createSubGraph(IASTStatement statement) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        ControlFlowGraph subCFG;
        if (statement instanceof IASTCompoundStatement comp) {
            // Empty compound case
            IASTStatement[] stmts = comp.getStatements();
            if (stmts.length == 0) {
                EmptyNode empty = new EmptyNode();
                cfg = new ControlFlowGraph(empty, empty);
            } else {
                for (IASTStatement stmt : comp.getStatements()) {
                    subCFG = createSubGraph(stmt);
                    if (subCFG != null && cfg != null)
                        cfg.concat(subCFG);
                }
            }
        } else if (statement instanceof IASTReturnStatement) {
            ReturnNode returnNode = new ReturnNode(statement);
            cfg = new ControlFlowGraph(returnNode, returnNode);
        } else if (statement instanceof IASTDeclarationStatement) {
            cfg = createVariableDeclaration((IASTDeclarationStatement) statement);
        } else if (statement instanceof IASTExpressionStatement) {
            VarAssignedNode varAssignedNode = new VarAssignedNode(statement);
            cfg = new ControlFlowGraph(varAssignedNode, varAssignedNode);
        } else {
            PlainNode plainNode = new PlainNode(statement);
            cfg = new ControlFlowGraph(plainNode, plainNode);
        }
        return cfg;
    }

    ControlFlowGraph createSubGraph(IASTStatement statement, IASTFunctionDefinition def) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        ControlFlowGraph subCFG;
        if (statement instanceof IASTCompoundStatement comp) {
            //Xet truong hop compound rong
            IASTStatement[] stmts = comp.getStatements();
            if (stmts.length == 0) {
                EmptyNode empty = new EmptyNode();
                cfg = new ControlFlowGraph(empty, empty);
            } else {
                for (IASTStatement stmt : comp.getStatements()) {
                    subCFG = createSubGraph(stmt, def);
                    if (subCFG != null && cfg != null)
                        cfg.concat(subCFG);
                }
            }
        } else if (statement instanceof IASTReturnStatement) {
            //Neu ham void -> khong co return statement -> khong can tao ReturnNode
            //Hien tai dang de la EmptyNode neu return statement la void
            if (!FunctionHelper.getFunctionType(def).equals("void")) {
                cfg = createReturnStatement((IASTReturnStatement) statement, def);
            } else {
                EmptyNode emptyNode = new EmptyNode();
                cfg = new ControlFlowGraph(emptyNode, emptyNode);
            }
        } else if (statement instanceof IASTDeclarationStatement) {
            cfg = createVariableDeclaration((IASTDeclarationStatement) statement, def);
        } else if (statement instanceof IASTExpressionStatement) {
            cfg = createExpressionStatement((IASTExpressionStatement) statement, def);
        } else {
            UndefinedNode undefined = new UndefinedNode(statement);
            cfg = new ControlFlowGraph(undefined, undefined);
        }
        return cfg;
    }

    private ControlFlowGraph createReturnStatement(IASTReturnStatement statement, IASTFunctionDefinition def) {
        CFGNode returnNode;
        ControlFlowGraph cfg = null;

        if (!hasCallExpression(statement)) {
            returnNode = new ReturnNode(statement, def);
            cfg = new ControlFlowGraph(returnNode, returnNode);
        } else {
            //Cac truong hop return; (Da xu ly o tren -> khong can thiet)
            if (statement.getReturnArgument() == null) {
                EmptyNode emptyNode = new EmptyNode();
                cfg = new ControlFlowGraph(emptyNode, emptyNode);
            } else {
                //Cac truong hop co gia tri return
                cfg = createFuncCallGraph(statement, def);
                returnNode = new ReturnNode(statement, def);
                cfg.concat(new ControlFlowGraph(returnNode, returnNode));
            }
        }
        return cfg;
    }


    private boolean hasCallExpression(IASTNode statement) {
        boolean result = false;
        IASTNode[] nodes = statement.getChildren();
        for (IASTNode node : nodes) {
            if (node instanceof IASTFunctionCallExpression) {
                return true;
            } else {
                result = hasCallExpression(node);
            }
        }
        return result;
    }

    /**
     * Voi loi goi ham co gia tri tra ve -> chuyen loi goi ham thanh 1 bien
     * Voi loi goi ham void -> noi luon
     *
     * @param: expressionStatement, functionDef
     */
    private ControlFlowGraph createExpressionStatement(IASTExpressionStatement statement, IASTFunctionDefinition def) {
        CFGNode cfgNode;

        ControlFlowGraph cfg = null;

        if (!hasCallExpression(statement)) {
            cfgNode = new VarAssignedNode(statement, def);
            cfg = new ControlFlowGraph(cfgNode, cfgNode);
        } else {
            //Tao ra node moi co chua loi goi ham la 1 bien
            cfg = createFuncCallGraph(statement, def);

            if (statement.getExpression().getChildren().length == 1) {
                //Kiem tra loi goi ham co la void khong, neu la void khong lam gi ca
            } else {
                /*cfgNode = new PlainNode(statement, def);
                cfg.concat(new ControlFlowGraph(cfgNode, cfgNode));*/
            }
            //EndFunctionNode endFunctionNode = new EndFunctionNode();
            //java.cfg.concat(new ControlFlowGraph(endFunctionNode, endFunctionNode));
        }
        return cfg;
    }

    /**
     * @param node, def
     *              Tao ra subGraph chua loi goi ham
     */
    private ControlFlowGraph createFuncCallGraph(IASTNode node, IASTFunctionDefinition def) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        if (node instanceof IASTExpressionStatement) {
            cfg = createFuncCallGraph(((IASTExpressionStatement) node).getExpression(), def);
        } else if (node instanceof IASTDeclarationStatement) {
            cfg = createFuncCallDecl((IASTDeclarationStatement) node, def);
        } else if (node instanceof IASTReturnStatement) {
            cfg = createFuncCallGraph(((IASTReturnStatement) node).getReturnValue(), def);
        } else if (node instanceof IASTBinaryExpression) {
            cfg = createFuncCallBinary((IASTBinaryExpression) node, def);
        } else if (node instanceof IASTUnaryExpression) {
            cfg = createFuncCallGraph(((IASTUnaryExpression) node).getOperand(), def);
        } else if (node instanceof IASTFunctionCallExpression) {
            FunctionCallNode callNode = new FunctionCallNode();
            callNode.setFunctionCall((IASTFunctionCallExpression) node);
            cfg = new ControlFlowGraph(callNode, callNode);
        }
        return cfg;
    }

    private ControlFlowGraph createFuncCallDecl(IASTDeclarationStatement node, IASTFunctionDefinition def) {
        //Da xu ly Declaration -> khong can?
        return null;
    }

    private ControlFlowGraph createFuncCallBinary(IASTBinaryExpression node, IASTFunctionDefinition def) {
        ControlFlowGraph cfg_left = createFuncCallGraph(node.getOperand1(), def);
        ControlFlowGraph cfg_right = createFuncCallGraph(node.getOperand2(), def);

        if (cfg_left == null) {
            System.err.println("null java.cfg");
            return cfg_right;
        } else if (cfg_right == null) {
            System.err.println("null java.cfg");
            return cfg_left;
        } else {
            cfg_left.concat(cfg_right);
            return cfg_left;
        }
    }

    /**
     * @param statement, func
     * @return Tach ra lenh khoi tao thanh cac node noi nhau
     * Vd: int sum = 0, a = b + c;
     * => int sum; sum = 0; int a; a = b + c;
     */
    private ControlFlowGraph createVariableDeclaration(IASTDeclarationStatement statement, IASTFunctionDefinition func) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        IASTEqualsInitializer init;
        IASTName nameVar;
        CPPNodeFactory factory = (CPPNodeFactory) func.getTranslationUnit().getASTNodeFactory();
        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) statement.getDeclaration().copy();

        IASTDeclSpecifier type = simpleDecl.getDeclSpecifier().copy();
        IASTDeclarator[] declarators = simpleDecl.getDeclarators();
        IASTExpression newExpression;

        IASTDeclarator newDeclarator;
        IASTDeclarationStatement newDeclStatement;
        IASTExpressionStatement newExprStatement;
        IASTExpression rightInitClause;
        IASTIdExpression newId;
        CFGNode node;

        for (IASTDeclarator decl : declarators) {
            nameVar = decl.getName().copy();
            init = (IASTEqualsInitializer) decl.getInitializer();
            newDeclStatement = ASTNodeFactory.createDeclarationStatement(nameVar, type);
            node = new VarDeclNode(newDeclStatement, func);
            cfg.concat(new ControlFlowGraph(node, node));
            //Neu nhu co dang: int b = 0; int b = f(x) + f(y);
            if (init != null) {
                newDeclarator = factory.newDeclarator(nameVar);
                rightInitClause = (IASTExpression) init.getChildren()[0].copy();
                newId = factory.newIdExpression(nameVar).copy();
                newExpression = factory.newBinaryExpression(IASTBinaryExpression.op_assign, newId, rightInitClause);
                newExprStatement = (IASTExpressionStatement) factory.newExpressionStatement(newExpression);

                cfg.concat(createSubGraph(newExprStatement, func));
            } else {

            }

        }
        return cfg;
    }

    private ControlFlowGraph createVariableDeclaration(IASTDeclarationStatement statement) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        IASTEqualsInitializer init;
        IASTName nameVar;
        CPPNodeFactory factory = new CPPNodeFactory();
        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) statement.getDeclaration().copy();

        IASTDeclSpecifier type = simpleDecl.getDeclSpecifier().copy();
        IASTDeclarator[] declarators = simpleDecl.getDeclarators();
        IASTExpression newExpression;

        IASTDeclarator newDeclarator;
        IASTDeclarationStatement newDeclStatement;
        IASTExpressionStatement newExprStatement;
        IASTExpression rightInitClause;
        IASTIdExpression newId;
        CFGNode node;

        for (IASTDeclarator decl : declarators) {
            nameVar = decl.getName().copy();
            init = (IASTEqualsInitializer) decl.getInitializer();
            newDeclStatement = ASTNodeFactory.createDeclarationStatement(nameVar, type);
            node = new VarDeclNode(newDeclStatement);
            cfg.concat(new ControlFlowGraph(node, node));
            //Neu nhu co dang: int b = 0; int b = f(x) + f(y);
            if (init != null) {
                newDeclarator = factory.newDeclarator(nameVar);
                rightInitClause = (IASTExpression) init.getChildren()[0].copy();
                newId = factory.newIdExpression(nameVar).copy();
                newExpression = factory.newBinaryExpression(IASTBinaryExpression.op_assign, newId, rightInitClause);
                newExprStatement = (IASTExpressionStatement) factory.newExpressionStatement(newExpression);

                cfg.concat(createSubGraph(newExprStatement));
            }
        }
        return cfg;
    }
}
