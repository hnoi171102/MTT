package com.mvs.cfg.index;

import com.mvs.cfg.node.PlainNode;
import com.mvs.cfg.utils.ExpressionHelper;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Indexer {
    private static CPPNodeFactory factory = new CPPNodeFactory();

    public static IASTNode index(IASTNode node, VariableManage vm) {
        if (node instanceof IASTDeclarationStatement) {
            node = indexDeclarationStatement((IASTDeclarationStatement) node, vm); //cau lenh khoi tao

        } else if (node instanceof IASTExpressionStatement) { //cau lenh gan va so sanh
            node = indexExpressionStatement((IASTExpressionStatement) node, vm);

        } else if (node instanceof IASTBinaryExpression) { //phep gan va so sanh
            node = indexIASTBinaryExpression((IASTBinaryExpression) node, vm);

        } else if (node instanceof IASTUnaryExpression) { // i++
            node = indexUranyExpression((IASTUnaryExpression) node, vm);

        } else if (node instanceof IASTIdExpression) { //bien (khong tinh trong phep khoi tao)
            node = indexIdExpression((IASTIdExpression) node, vm);

        } else if (node instanceof IASTReturnStatement) {
            node = indexReturnStatement((IASTReturnStatement) node, vm);
        }
        return node;
    }

    public static IASTNode indexInvariant(IASTNode node, VariableManage vm) {
        if (node instanceof IASTExpressionStatement) { //cau lenh gan va so sanh
            node = indexExpressionStatementInvariant((IASTExpressionStatement) node, vm);

        } else if (node instanceof IASTBinaryExpression) { //phep gan va so sanh
            node = indexIASTBinaryExpressionInvariant((IASTBinaryExpression) node, vm);

        } else if (node instanceof IASTUnaryExpression) { // i++
            node = indexUranyExpressionInvariant((IASTUnaryExpression) node, vm);

        } else if (node instanceof IASTIdExpression) { //bien (khong tinh trong phep khoi tao)
            node = indexIdExpressionInvariant((IASTIdExpression) node, vm);
        }
        return node;
    }

    private static IASTNode indexExpressionStatementInvariant(IASTExpressionStatement node, VariableManage vm) {
        // After done index a invariant node, increase var that exist in invariant
        for (Var var : vm.getVariableList()) {
            if (var.getIndexInvariant() != -1) {
                //variable.increase();
            }
        }
        IASTExpression expression = node.getExpression().copy();
        IASTExpressionStatement newNode = factory.newExpressionStatement((IASTExpression) indexInvariant(expression, vm));
        return newNode;
    }

    private static IASTNode indexIASTBinaryExpressionInvariant(IASTBinaryExpression node, VariableManage vm) {
        boolean isAssignment = (node.getOperator() == IASTBinaryExpression.op_assign);
        IASTExpression right = null;
        if (node.getOperand2() instanceof IASTIdExpression) {
            right = (IASTExpression) indexVariableInvariant(((IASTIdExpression) node.getOperand2()).copy(), vm);
        } else  {
            right = (IASTExpression) indexInvariant(node.getOperand2().copy(), vm);
        }

        IASTExpression left = null;
        if (node.getOperand1() instanceof IASTIdExpression) { // neu la 1 bien gan
            left = (IASTExpression) indexVariableInvariant((IASTIdExpression) node.getOperand1().copy(), vm);
        } else  { //neu la binary expression gan
            left = (IASTExpression) indexInvariant (node.getOperand1().copy(), vm);
        }
        IASTBinaryExpression newNode = factory.newBinaryExpression(node.getOperator(), left, right);
        return newNode;
        //}
    }

    private static IASTNode indexVariableInvariant(IASTIdExpression node, VariableManage vm) {
        String name = ExpressionHelper.toString(node);
        Var var = vm.getVariable(name);
        if (var == null) return node;
        var.increaseSSA();
        var.setIndexInvariant(var.getSsaIndex());
        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        var.decreaseSSA();
        IASTIdExpression newNode = factory.newIdExpression(nameId);
        return newNode;
    }

    private static IASTNode indexUranyExpressionInvariant(IASTUnaryExpression node, VariableManage vm) {
        return indexInvariant(changeUnarytoBinary(node),vm);
    }
    private static IASTNode indexIdExpressionInvariant(IASTIdExpression node, VariableManage vm) {
        String name = ExpressionHelper.toString(node);
        Var var = vm.getVariable(name);
        if (var == null) return node;
        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        IASTIdExpression newExp = factory.newIdExpression(nameId);
        return newExp;
    }
    

    /**
     * Cau lenh return
     *
     * @param node
     * @param vm
     * @return
     */
    private static IASTNode indexExpressionStatement(IASTExpressionStatement node, VariableManage vm) {
        IASTExpression expression = node.getExpression().copy();
        IASTExpressionStatement newNode = factory.newExpressionStatement((IASTExpression) index(expression, vm));
        return newNode;
    }

    /**
     * Khoi tao bien
     *
     * @param statement
     * @param vm
     * @return
     */
    private static IASTNode indexDeclarationStatement(IASTDeclarationStatement statement, VariableManage vm) {
        String name = "";
        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) statement.getDeclaration().copy();
        for (IASTNode run : simpleDecl.getChildren()) {
            // type
            if (run instanceof IASTDeclSpecifier) {
            }

            if (run instanceof IASTDeclarator) {
                IASTEqualsInitializer init = (IASTEqualsInitializer) (((IASTDeclarator) run).getInitializer());
                if (init != null) {
                    IASTInitializerClause initClause = (IASTInitializerClause) index(init.getChildren()[0], vm);
                    init.setInitializerClause(initClause);
                }
                IASTName nameVar = ((IASTDeclarator) run).getName();
                name = nameVar.toString();
                Var var = vm.getVariable(name);

                // xu ly rieng cho bien cuc bo duoc khoi tao trong ham con
                if (var == null) return statement;

                //Bien Global
                if (var.getIsDuplicated()) {
                    /*var.increaseSSA();*/
                    var.setWrIndex(0);
                }

                IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
                ((IASTDeclarator) run).setName(nameId);
            }
        }
        IASTDeclarationStatement newNode = factory.newDeclarationStatement(simpleDecl);
        return newNode;
    }

    /**
     * Phep gan va so sanh
     *
     * @param node
     * @param vm
     * @return
     */
    private static IASTNode indexIASTBinaryExpression(IASTBinaryExpression node, VariableManage vm) {
        boolean isAssignment = (node.getOperator() == IASTBinaryExpression.op_assign);
        if (isAssignment) { //neu la phep gan
            // System.out.println(ExpressionHelper.toString(node));
            IASTExpression right = (IASTExpression) index (node.getOperand2().copy(), vm);
            IASTExpression left = null;
            if (node.getOperand1() instanceof IASTIdExpression) { // neu la 1 bien gan
                left = (IASTExpression) indexVariable((IASTIdExpression) node.getOperand1().copy(), vm);
            } else if (node.getOperand1() instanceof IASTBinaryExpression) { //neu la binary expression gan
                left = (IASTExpression) index (node.getOperand1().copy(), vm);
            }
            IASTBinaryExpression newNode = factory.newBinaryExpression(node.getOperator(), left, right);
            return newNode;
        } else { //neu la phep so sanh, thuong la ve phai cua bieu thuc -> bien read
            IASTExpression left = node.getOperand1().copy(); //set bien read
            IASTExpression right = node.getOperand2().copy();
            IASTBinaryExpression newNode = factory.newBinaryExpression(node.getOperator(), (IASTExpression) index(left, vm), (IASTExpression) index(right, vm));
            return newNode;
        }
    }

    /**
     * Bien
     *
     * @param node
     * @param vm
     * @return
     */
    private static IASTNode indexVariable(IASTIdExpression node, VariableManage vm) {
        String name = ExpressionHelper.toString(node);
        Var var = vm.getVariable(name);
        if (var == null) return node;
        /*if (var.getIndexInvariant() != -1) { //have change index in inv
            if (var.getIndex() < var.getIndexInvariant()) {
                var.setIndex(var.getIndexInvariant() + 1);
            } else {
                var.increase();
            }
        } else { //dont have in invariant
            var.increase();
        }*/
        int currentThreadIndex = vm.getCurrentThreadIndex();
        var.setWrIndex(0);
        var.increaseSSA();
        var.setThreadIndex(currentThreadIndex);
        vm.addInputVarList(var);

        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        IASTIdExpression newNode = factory.newIdExpression(nameId);
        return newNode;
    }

    /**
     * Bien
     *
     * @param node
     * @param vm
     * @return
     */
    private static IASTNode indexUranyExpression(IASTUnaryExpression node, VariableManage vm) {
        return index(changeUnarytoBinary(node), vm);
    }

    /**
     * Chuyen doi cac phep ++, -- thanh phep +, -
     * @param node
     * @return
     */
    private static IASTExpression changeUnarytoBinary(IASTUnaryExpression node) {
        IASTExpression operand = ((IASTUnaryExpression) node).getOperand().copy();
        int operator = changeOperator(node.getOperator());
        if (operator == 0) {
            return node.getOperand();
        }

        IASTLiteralExpression number = factory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "1");
        IASTExpression right = factory.newBinaryExpression(operator, operand, number);
        IASTExpression expression = factory.newBinaryExpression(IASTBinaryExpression.op_assign, operand, right);
        return expression;
    }

    /**
     * Chuyen doi cac phep ++, -- thanh phep +, -
     * @param unaryOp
     * @return
     */
    private static int changeOperator(int unaryOp) {
        int binaryOp = 0;
        if (unaryOp == IASTUnaryExpression.op_postFixDecr || unaryOp == IASTUnaryExpression.op_prefixDecr) {
            binaryOp = IASTBinaryExpression.op_minus;
        } else if (unaryOp == IASTUnaryExpression.op_postFixIncr || unaryOp == IASTUnaryExpression.op_prefixIncr) {
            binaryOp = IASTBinaryExpression.op_plus;
        }
        return binaryOp;
    }

    /**
     * Xu ly return statement
     * @param returnState
     * @param vm
     * @return
     */
    private static IASTNode indexReturnStatement(IASTReturnStatement returnState, VariableManage vm) {
        return null; //da xu ly return statement o returnNode
    }

    /**
     * Gan cho cac bien ve phai (khong tang index)
     * @param node
     * @param vm
     * @return
     */
    private static IASTNode indexIdExpression(IASTIdExpression node, VariableManage vm) {
        String name = ExpressionHelper.toString(node);
        Var var = vm.getVariable(name);
        if (var == null) return node;
        /*if (var.getIndexInvariant() > var.getIndex()) {
            var.setIndex(var.getIndexInvariant());
        }*/
        int currentThreadIndex = vm.getCurrentThreadIndex();
        var.setWrIndex(1);
        var.increaseSSA();
        var.setThreadIndex(currentThreadIndex);
        vm.addInputVarList(var);
        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        IASTIdExpression newExp = factory.newIdExpression(nameId);
        return newExp;
    }

}
