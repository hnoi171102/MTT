package com.mtv.encode.cfg.build;

import com.mtv.encode.ast.FunctionHelper;
import com.mtv.encode.ast.IASTVariable;
import com.mtv.encode.cfg.utils.ExpressionHelper;
import com.mtv.encode.cfg.utils.ExpressionModifier;
import com.mtv.debug.DebugHelper;
import com.mtv.encode.ast.ASTFactory;
import com.mtv.encode.cfg.node.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import java.util.ArrayList;



public class MultiFunctionCFGBuilder {
    private ASTFactory ast;

    public MultiFunctionCFGBuilder(ASTFactory ast) {
        this.ast = ast;
    }


    public ControlFlowGraph build(IASTFunctionDefinition func, boolean includeGlobalVars) {
        if (func == null) {
            DebugHelper.print("Function is null. Terminated.");
            return null;
        }
        // Create empty control flow graph
        ControlFlowGraph prvCfg = new ControlFlowGraph();
        // Create CPP node factory, where is used to build node for control flow graph
        CPPNodeFactory factory = new CPPNodeFactory();

        ASTFactory ast = new ASTFactory(func.getTranslationUnit());


        if (includeGlobalVars) {
            ArrayList<IASTDeclaration> declarations = ast.getGlobalVarList();
            ControlFlowGraph subCfg;
            for (IASTDeclaration declaration : declarations) {
                IASTDeclarationStatement statement = factory.newDeclarationStatement(declaration.copy());
                subCfg = (new ControlFlowGraphBuilder()).createSubGraph(statement);
                if (prvCfg == null) {
                    prvCfg = subCfg;
                } else {
                    prvCfg.concat(subCfg);
                }
            }
        }
        BeginFunctionNode beginFunc = new BeginFunctionNode(func);
        prvCfg.concat(new ControlFlowGraph(beginFunc, beginFunc));
        ControlFlowGraph mainFuncCfg = new ControlFlowGraph(func);
        prvCfg.concat(mainFuncCfg);
        EndFunctionNode endFunction = new EndFunctionNode(func);
        prvCfg.concat(new ControlFlowGraph(endFunction, endFunction));
        iterateNode(prvCfg.getStart(), func);

        return prvCfg;
    }


    /**
     * @param node, end, func
     *              Ham duyet java.cfg va xu ly FunctionCallNode
     */
    private CFGNode iterateNode(CFGNode node, IASTFunctionDefinition func) {
        if (node == null) {

        } else if (node instanceof FunctionCallNode) {
            ControlFlowGraph functionGraph = createFuncGraph(((FunctionCallNode) node).getFunctionCall(), func);
            if (functionGraph != null) {
                CFGNode pause = node.getNext();
                node = iterateNode(functionGraph.getStart(), func);
                functionGraph.getExit().setNext(iterateNode(pause, func));
            }
        } else if (node instanceof EndNode) {

        } else if (node instanceof AbortNode) {

        } else if (node instanceof EndFunctionNode) {

        } else {
            node.setNext(iterateNode(node.getNext(), func));
        }
        return node;
    }

    private boolean isVoid(IASTFunctionCallExpression callExpression) {
        String funcName = callExpression.getFunctionNameExpression().toString();
        IASTFunctionDefinition func = FunctionHelper.getFunction(ast.getListFunction(), funcName);
        String type = FunctionHelper.getFunctionType(func);
        return type.equals("void") ? true : false;
    }

    /* Create CFG for function call expression
     * Argument list:
     *  - callExpression: expression of the call, which contains information about the call such as called function's name or arguments
     *  - currentFunc: the function which callExpression is stay inside
     */
    private ControlFlowGraph createFuncGraph(IASTFunctionCallExpression callExpression, IASTFunctionDefinition currentFunc) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        // The function which is called by this expression
        String targetedFuncName = callExpression.getFunctionNameExpression().toString();
        String info = callExpression.getRawSignature();

        if (targetedFuncName.equals("pthread_create")) {
            IASTInitializerClause[] arguments = callExpression.getArguments();
            String threadReference = arguments[0].getRawSignature().substring(1) + "_" + currentFunc.getDeclarator().getName();
            String attributeExpression = arguments[1].getRawSignature();
            String funcReference = arguments[2].getRawSignature();
            String restrictionExpression = arguments[3].getRawSignature();
            ControlFlowGraph funcCFG = new ControlFlowGraph(ast.getFunction(funcReference), ast, false);
            CreateThreadNode createThreadNode = new CreateThreadNode(threadReference, attributeExpression, funcReference, funcCFG, restrictionExpression);
            return new ControlFlowGraph(createThreadNode, createThreadNode);
        } else if (targetedFuncName.equals("pthread_join")) {
            IASTInitializerClause[] arguments = callExpression.getArguments();
            String threadReference = arguments[0].getRawSignature() + "_" + currentFunc.getDeclarator().getName();
            String returnValExpression = arguments[1].getRawSignature();
            JoinThreadNode joinThreadNode = new JoinThreadNode(threadReference, returnValExpression);
            return new ControlFlowGraph(joinThreadNode, joinThreadNode);
        } else if (targetedFuncName.equals("abort")) {
            AbortNode abortNode = new AbortNode();
            return new ControlFlowGraph(abortNode, abortNode);
        } else if (targetedFuncName.equals("assert")) {
            IASTInitializerClause[] arguments = callExpression.getArguments();
            AssertNode assertNode = new AssertNode((IASTExpression) arguments[0]);
            return new ControlFlowGraph(assertNode, assertNode);
        } else {
            IASTFunctionDefinition func = FunctionHelper.getFunction(ast.getListFunction(), targetedFuncName);

            if (func == null) {
                System.err.println("Not found function " + targetedFuncName);
                System.exit(1);
            }

            //add begin function node at the beginning
            BeginFunctionNode beginNode = new BeginFunctionNode(func);
            cfg.concat(new ControlFlowGraph(beginNode, beginNode));

            CPPNodeFactory factory = (CPPNodeFactory) func.getTranslationUnit().getASTNodeFactory();
            //Cho tham so = params
            ControlFlowGraph argGraph = createArguments(callExpression, currentFunc);
            if (argGraph != null) cfg.concat(argGraph);

            //Noi voi than cua ham duoc goi
            ControlFlowGraph funcGraph = new ControlFlowGraph(func);
            cfg.concat(funcGraph);

            //Tao ra node: ham duoc goi = return neu khong phai void
            if (!isVoid(callExpression)) {
                IASTIdExpression left = (IASTIdExpression) ExpressionModifier.changeFunctionCallExpression(callExpression, func);
                IASTName nameRight = factory.newName(("return_" + FunctionHelper.getShortenName(targetedFuncName)).toCharArray());
                IASTIdExpression right = factory.newIdExpression(nameRight);
                IASTBinaryExpression binaryExp = factory.newBinaryExpression(IASTBinaryExpression.op_assign, left, right);
                IASTExpressionStatement statement = factory.newExpressionStatement(binaryExp);

                CFGNode plainNode = new PlainNode(statement); //tao ra plainNode khong co ten ham dang sau
                cfg.concat(new ControlFlowGraph(plainNode, plainNode));
            }

            EndFunctionNode endFunction = new EndFunctionNode(func);
            cfg.concat(new ControlFlowGraph(endFunction, endFunction));

            return cfg;
        }
    }

    /**
     * @param callExpression
     * @param currentFunc    Tra ve cac Node xu ly tham so cua ham (neu co)
     */
    private ControlFlowGraph createArguments(IASTFunctionCallExpression callExpression, IASTFunctionDefinition currentFunc) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        String funcName = callExpression.getFunctionNameExpression().toString();
        CFGNode varAssignedNode;
        IASTFunctionDefinition func = FunctionHelper.getFunction(ast.getListFunction(), funcName);

        ArrayList<IASTVariable> params = FunctionHelper.getParameters(func);
        IASTInitializerClause[] arguments = callExpression.getArguments();
        IASTBinaryExpression expression;
        IASTExpressionStatement statement;
        IASTExpression right;
        IASTName leftName;
        IASTIdExpression left;
        String leftNameStr;
        String offset = "";

        if (arguments.length == 0) return null;
        CPPNodeFactory factory = (CPPNodeFactory) func.getTranslationUnit().getASTNodeFactory();

        for (int i = 0; i < arguments.length; i++) {
            leftNameStr = params.get(i).getName().toString();
            leftNameStr += "_" + funcName;
            offset = "";
            leftNameStr += offset;
            leftName = factory.newName(leftNameStr.toCharArray());
            left = factory.newIdExpression(leftName);
            right = (IASTExpression) ExpressionModifier.changeVariableName((IASTExpression) arguments[i].copy(), currentFunc);
            expression = factory.newBinaryExpression(IASTBinaryExpression.op_assign, left, right);
            statement = factory.newExpressionStatement(expression);
            varAssignedNode = new VarAssignedNode(statement);
            DebugHelper.print(ExpressionHelper.toString(statement));
            cfg.concat(new ControlFlowGraph(varAssignedNode, varAssignedNode));
        }
        return cfg;
    }
}
