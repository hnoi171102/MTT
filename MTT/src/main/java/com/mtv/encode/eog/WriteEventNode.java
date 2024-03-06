package com.mtv.encode.eog;

import com.mtv.debug.DebugHelper;
import com.mtv.encode.ast.ASTNodeFactory;
import com.mtv.encode.cfg.utils.ExpressionHelper;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;

public class WriteEventNode extends EventOrderNode{
    /*
        Describe the value use to write in this event
     */
    public IASTExpression expression;
    /*
        Read events which are used to provide value for this write event
     */
    public ArrayList<ReadEventNode> readEvents;

    public WriteEventNode(String varPreference, IASTExpression expression, ArrayList<ReadEventNode> readEventNodes) {
        super(varPreference);
        this.expression = expression;
        this.readEvents = readEventNodes;
    }

    public WriteEventNode(String varPreference,IASTExpression expression, ArrayList<ReadEventNode> readEventNodes, ArrayList<EventOrderNode> previous, ArrayList<EventOrderNode> next) {
        super(varPreference, previous, next);
        this.expression = expression;
        this.readEvents = readEventNodes;
    }

    public void AddSuffixToExpression() {
        AddSuffixToExpression(expression, (ArrayList<ReadEventNode>) readEvents.clone());
    }
    private void AddSuffixToExpression(IASTExpression expression, ArrayList<ReadEventNode> readEventNodes_Clone) {
        if (expression instanceof IASTIdExpression) {
            ReadEventNode targetEvent = readEventNodes_Clone.get(readEventNodes_Clone.size() - 1);
            IASTName suffixName = ASTNodeFactory.createName(targetEvent.suffixVarPref);
            readEventNodes_Clone.remove(targetEvent);
            ((IASTIdExpression) expression).setName(suffixName);
        } else {
            for (IASTNode child: expression.getChildren()) {
                if (child instanceof IASTIdExpression) {
                    ReadEventNode targetEvent = readEventNodes_Clone.get(readEventNodes_Clone.size() - 1);
                    IASTName suffixName = ASTNodeFactory.createName(targetEvent.suffixVarPref);
                    readEventNodes_Clone.remove(targetEvent);
                    ((IASTIdExpression) child).setName(suffixName);
                } else if (child instanceof IASTBinaryExpression) {
                    AddSuffixToExpression((IASTExpression) child, readEventNodes_Clone);
                } else if (child instanceof IASTUnaryExpression) {
                    AddSuffixToExpression((IASTExpression) child, readEventNodes_Clone);
                } else {
                    // TODO: Additional cases
                }
            }
        }

    }

    @Override
    public void printNode(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print(" ");
        }
        String marker;
        if (interleavingTracker.GetMarker().toString().equals("Skip")) {
            marker = "-----";
        } else if (interleavingTracker.GetMarker().toString().equals("End")) {
            marker = "End  ";
        } else {
            marker = "Begin";
        }
        DebugHelper.print(marker + " - Write: " + (suffixVarPref == null ? varPreference : suffixVarPref) + " = " + ExpressionHelper.toString(expression));
    }
}
