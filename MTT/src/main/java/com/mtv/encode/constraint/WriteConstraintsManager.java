package com.mtv.encode.constraint;

import com.microsoft.z3.*;
import com.mtv.encode.eog.EventOrderGraph;
import com.mtv.encode.eog.EventOrderNode;
import com.mtv.encode.eog.WriteEventNode;
import org.eclipse.cdt.core.dom.ast.*;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;


public class WriteConstraintsManager {
    // Create all write constraints in the event order graph using given context then store them in the given solver

    // Check point has the variable named "check" and type of "bool" must be defined globally in the program.
    // Check variable must be assigned to the negative value of input assert or negative input of abort.
    // Check variable can only be written. Reading check variable can cause several bugs.
    private final static String checkPointName = "check";
    private final static String checkPointType = "bool";

    public static void CreateWriteConstraints(Context ctx, Solver solver, EventOrderGraph eventOrderGraph, ArrayList<IASTDeclaration> globalVars) throws Exception {
        EventOrderNode trackNode = eventOrderGraph.startNode;
        CreateWriteConstraint(ctx, solver, trackNode, globalVars);
        eventOrderGraph.ResetVisited();
    }
    private static void CreateWriteConstraint(Context ctx, Solver solver, EventOrderNode node, ArrayList<IASTDeclaration> globalVars) throws Exception {
        if (node == null) {
            return;
        }

        if (node.isVisited) return;
        node.isVisited = true;

        if (node instanceof WriteEventNode writeNode) {
            if (writeNode.varPreference.equals(checkPointName)) {
                solver.add(ctx.mkEq(ctx.mkBoolConst(writeNode.suffixVarPref), ctx.mkBool(true)));
            }
            Expr readExpr = CreateCalculation(ctx, writeNode.expression, globalVars);
            if (readExpr instanceof IntExpr intReadExpr) {
                IntExpr writeVar = ctx.mkIntConst(writeNode.suffixVarPref);
                solver.add(ctx.mkEq(intReadExpr, writeVar));
            } else if (readExpr instanceof BoolExpr boolReadExpr) {
                BoolExpr writeVar = ctx.mkBoolConst(writeNode.suffixVarPref);
                solver.add(ctx.mkEq(boolReadExpr, writeVar));
            } else {
                throw new IllegalArgumentException("Type of " + readExpr.getClass().toString() + " is not supported");
            }
        }

        ArrayList<EventOrderNode> nextNodes = node.nextNodes;
        for (EventOrderNode nextNode: nextNodes) {
            CreateWriteConstraint(ctx, solver, nextNode, globalVars);
        }
    }
    private static Expr CreateCalculation(Context ctx, IASTExpression expression, ArrayList<IASTDeclaration> globalVars) throws Exception {
        if (expression instanceof IASTIdExpression) {
            String idType = "";
            String idName = ((IASTIdExpression) expression).getName().toString();
            String rawIdName = idName.substring(0, idName.lastIndexOf("_"));

            if (rawIdName.equals(checkPointName)) {
                throw new IllegalAccessException("Check variable cannot be read.");
            }

            for (IASTDeclaration globalVar: globalVars) {
                IASTDeclarator[] declarators = ((IASTSimpleDeclaration)globalVar).getDeclarators();
                for (IASTDeclarator declarator: declarators) {
                    String name = declarator.getName().toString();
                    if (rawIdName.equals(name)) {
                        idType = ((IASTSimpleDeclaration) globalVar).getDeclSpecifier().toString();
                    }
                }
            }
            switch (idType) {
                case "int":
                    return ctx.mkIntConst(((IASTIdExpression)expression).getName().toString());
                case "bool":
                    return ctx.mkBoolConst(((IASTIdExpression)expression).getName().toString());
                default:
                    if (idType.equals("")) {
                        // If idType is not set, that means this variable is not global
                        // Assume all local variables are int
                        return ctx.mkIntConst(((IASTIdExpression)expression).getName().toString());
                    } else {
                        throw new IllegalArgumentException("Type of " + idType + " is not supported");
                    }
            }
        } else if (expression instanceof IASTBinaryExpression binaryExpression) {
            IASTExpression operand1 = binaryExpression.getOperand1();
            IASTExpression operand2 = binaryExpression.getOperand2();
            Expr expr1 = CreateCalculation(ctx, operand1, globalVars);
            Expr expr2 = CreateCalculation(ctx, operand2, globalVars);
            switch (binaryExpression.getOperator()) {
                case 1:
                    return ctx.mkMul(expr1, expr2);
                case 2:
                    return ctx.mkDiv(expr1, expr2);
                case 3:
                    return ctx.mkMod(expr1, expr2);
                case 4:
                    return ctx.mkAdd(expr1, expr2);
                case 5:
                    return ctx.mkSub(expr1, expr2);
                case 8:
                    return ctx.mkLt(expr1, expr2);
                case 9:
                    return ctx.mkGt(expr1, expr2);
                case 10:
                    return ctx.mkLe(expr1, expr2);
                case 11:
                    return ctx.mkGe(expr1, expr2);
                case 15:
                    return ctx.mkAnd(expr1, expr2);
                case 16:
                    return ctx.mkOr(expr1, expr2);
                case 28:
                    return ctx.mkEq(expr1, expr2);
                case 29:
                    return ctx.mkNot(ctx.mkEq(expr1, expr2));
                default:
                    throw new Exception("Operator " + binaryExpression.getOperator() + " not supported.");
            }
        } else if (expression instanceof IASTUnaryExpression unaryExpression) {
            return CreateCalculation(ctx, unaryExpression.getOperand(), globalVars);
        } else if (expression instanceof IASTLiteralExpression literalExpression) {
            return LiteralExprToSMTExpr(literalExpression, ctx);
        } else {
            System.out.println(expression.getClass().toString());
            return null;
        }
    }

    private static Expr LiteralExprToSMTExpr(IASTLiteralExpression literalExpression, Context ctx) throws Exception {
        if (literalExpression.toString().matches("-?\\d+")) {
            return ctx.mkInt(literalExpression.toString());
        } else if (literalExpression.toString().matches("(true)|(false)")){
            if (literalExpression.toString().equals("true"))
                return ctx.mkBool(true);
            else return ctx.mkBool(false);
        } else {
            throw new InvalidAttributeValueException("Literal value " + literalExpression.toString() + " is not supported");
        }
    }
}
