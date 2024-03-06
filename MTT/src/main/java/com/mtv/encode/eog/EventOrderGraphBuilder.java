package com.mtv.encode.eog;

import com.mtv.debug.DebugHelper;
import com.mtv.encode.cfg.build.ControlFlowGraph;
import com.mtv.encode.cfg.node.*;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;
import java.util.HashMap;

public class EventOrderGraphBuilder {
    public static EventOrderGraph Build(ControlFlowGraph baseCFG, HashMap<String, Integer> varSuffixMap, boolean isThreadSuffixPrioritized) {
        EventOrderGraph eog = new EventOrderGraph();
        CFGNode start = baseCFG.getStart();

        HashMap<String, EventOrderGraph> threadCreations = new HashMap<>();
        ArrayList<EventOrderNode> unJoinedNodes = new ArrayList<>();
        ArrayList<EventOrderNode> joinedNodes = new ArrayList<>();

        while (start != null) {
            if (start instanceof CreateThreadNode) {
                // If thread suffix is not prioritized, add suffix to the main EOG before create and give suffix to thread EOG
                if (!isThreadSuffixPrioritized) {
                    AddSuffixVar(eog.startNode, varSuffixMap);
                }
                if (eog.interleavingNode == null) {
                    // Interleaving point is not exist or is reset. Create a new one.
                    // Assume there is only one interleaving node in the EOG
                    // So, when interleaving node is null, there is less than two end point in cfg

                    // When program creates some read/write events then start interleaving,
                    // there must be one end point in cfg, and then we mark it as start interleaving node.
                    // But when program start interleaving without create any read/write events before,
                    // there is not any end point in cfg to mark it as start interleaving node.
                    // So, in that case, we create an empty node in the start and mark it as start interleaving node

                    if (eog.endNodes.size() == 0) {
                        EventOrderNode emptyNode = new EventOrderNode(null) {
                            @Override
                            public void printNode(int level) {
                                DebugHelper.print("Empty event node");
                            }
                        };
                        eog.AddEOG(new EventOrderGraph(emptyNode));
                    }
                    eog.interleavingNode = eog.endNodes.get(0);
                    eog.interleavingNode.interleavingTracker.SetMarker(InterleavingTracker.InterleavingMarker.Begin);
                }
                // Create thread's event order graph then connect it to main event order graph
                EventOrderGraph threadEOG = Build(((CreateThreadNode) start).funcCFG, varSuffixMap, isThreadSuffixPrioritized);
                threadCreations.put(((CreateThreadNode) start).threadReference, threadEOG);
                for (EventOrderNode threadEndNode: threadEOG.endNodes) {
                    threadEndNode.interleavingTracker.SetMarker(InterleavingTracker.InterleavingMarker.End);
                    threadEndNode.interleavingTracker.AddRelatedNode(eog.interleavingNode);
                    eog.interleavingNode.interleavingTracker.AddRelatedNode(threadEndNode);
                    unJoinedNodes.add(threadEndNode);
                }
                eog.AddEOG(threadEOG, eog.interleavingNode);
            } else if (start instanceof JoinThreadNode) {
                EventOrderGraph joinedEOG = threadCreations.get(((JoinThreadNode) start).threadReference);
                for (EventOrderNode joinedNode: joinedEOG.endNodes) {
                    if (unJoinedNodes.contains(joinedNode)) {
                        unJoinedNodes.remove(joinedNode);
                        joinedNodes.add(joinedNode);
                    } else {
                        System.out.println("Can't remove. This is not unjoined node: ");
                        joinedNode.printNode(8);
                    }
                }
            } else if (start instanceof VarAssignedNode) {
                ArrayList<EventOrderNode> connectNodes = new ArrayList<>();
                if (eog.interleavingNode == null) {
                    // When interleaving node is not created, connect new node to EOG's current end node (expect 1 end node)
                    if (eog.endNodes.size() > 1) {
                        DebugHelper.print("Number of end nodes is invalid. Expected 1 and have " + eog.endNodes.size());
                    }
                    for (EventOrderNode endNode: eog.endNodes) {
                        connectNodes.add(endNode);
                    }
                } else if (joinedNodes.size() > 0) {
                    // If there are some threads are joined, store their end nodes then connect them with other nodes behind
                    for (EventOrderNode joinedNode: joinedNodes) {
                        connectNodes.add(joinedNode);
                    }
                    for (EventOrderNode joinedNode: connectNodes) {
                        joinedNodes.remove(joinedNode);
                    }
                } else {
                    // If there are threads joined, continue connecting to the joined point
                    ArrayList<EventOrderNode> endNodes = eog.endNodes;
                    for (EventOrderNode endNode: endNodes) {
                        if (endNode.interleavingTracker.GetMarker() != InterleavingTracker.InterleavingMarker.End) {
                            connectNodes.add(endNode);
                        }
                    }
                    // If there is not any thread joined, connect behind nodes to interleaving point
                    if (connectNodes.size() == 0) {
                        connectNodes.add(eog.interleavingNode);
                    }
                }

                IASTStatement statement = ((VarAssignedNode) start).statement;
                IASTExpression expression = ((IASTExpressionStatement) statement).getExpression();

                if (expression instanceof IASTBinaryExpression) {
                    IASTBinaryExpression binaryExpression = (IASTBinaryExpression) expression;
                    IASTExpression operand1 = binaryExpression.getOperand1();
                    IASTExpression operand2 = binaryExpression.getOperand2();

                    // In each IASTBinaryExpression, there is only one variable which can be written,
                    // but many variables which can be read.
                    String writtenVar = "";
                    ArrayList<String> readVars = new ArrayList<>();

                    if (operand1 instanceof IASTIdExpression) {
                        // Assume that operand1 is a variable (because we store some value in it)
                        writtenVar = ((IASTIdExpression) operand1).getName().toString();

                        // Operand2 can be either a variable or an expression, so we have to split them and check both cases
                        if (operand2 instanceof IASTIdExpression) {
                            String variableName2 = ((IASTIdExpression) operand2).getName().toString();
                            readVars.add(variableName2);
                        } else if (operand2 instanceof IASTBinaryExpression) {
                            IASTBinaryExpression subBinaryExpr = (IASTBinaryExpression) operand2;
                            ArrayList<IASTIdExpression> varExpressions = GetIdExpressionFromBinaryExpression(subBinaryExpr);
                            for (IASTIdExpression varExpression: varExpressions) {
                                readVars.add(varExpression.getName().toString());
                            }
                        } else if (operand2 instanceof IASTUnaryExpression) {
                            IASTUnaryExpression subBinaryExpr = (IASTUnaryExpression) operand2;
                            ArrayList<IASTIdExpression> varExpressions = GetIDExpressionFromUnaryExpression(subBinaryExpr);
                            for (IASTIdExpression varExpression : varExpressions) {
                                readVars.add(varExpression.getName().toString());
                            }
                        } else {
                            // DebugHelper.print("Operand2 in " + ExpressionHelper.toString(statement) + " is not valid");
                        }
                    } else {
                        // DebugHelper.print("Operand1 in " + ExpressionHelper.toString(statement) + " is not an IdExpression");
                    }

                    ArrayList<ReadEventNode> readEventNodes = AddReadVars(eog, readVars, connectNodes);
                    AddWrittenVar(eog, writtenVar, operand2, readEventNodes, connectNodes);
                } else {
                    // This may never happen due to VarAssignedNode is created on a IASTBinaryExpression
                    // DebugHelper.print("Statement " + ExpressionHelper.toString(statement) + " is not a IASTBinaryExpression");
                }
            } else if (start instanceof AssertNode) {
                DebugHelper.print("Assert node");
                start.printNode();
            } else {
                // Other nodes are skipped because we only care about read-write links
            }
            start = start.getNext();
        }
        AddSuffixVar(eog.startNode, varSuffixMap);
        return eog;
    }


    private static ArrayList<ReadEventNode> AddReadVars(EventOrderGraph eog, ArrayList<String> readVars, ArrayList<EventOrderNode> connectPoints) {
        EventOrderGraph readVarsEOG = new EventOrderGraph();
        ArrayList<ReadEventNode> readEvents = new ArrayList<>();
        for (String readVar : readVars) {
            if (readVar != "") {
                ReadEventNode readNode = new ReadEventNode(readVar);
                readEvents.add(readNode);
                readVarsEOG.AddEOG(new EventOrderGraph(readNode));
            }
        }
        if (readVarsEOG.startNode != null) {
            if (connectPoints.size() == 0) {
                eog.AddEOG(readVarsEOG);
            } else {
                // If specific connect points are exist, we have to connect this event to both them and main thread
                ArrayList<EventOrderNode> currentEndPoints = eog.endNodes;
                for (EventOrderNode endNode: eog.endNodes) {
                    if (endNode.interleavingTracker.GetMarker() != InterleavingTracker.InterleavingMarker.End) {
                        connectPoints.add(endNode);
                    }
                }
                for (EventOrderNode connectPoint: connectPoints) {
                    eog.AddEOG(readVarsEOG, connectPoint);
                }
            }
        }
        return readEvents;
    }

    private static void AddWrittenVar(EventOrderGraph eog, String writtenVar, IASTExpression expression, ArrayList<ReadEventNode> readEventNodes, ArrayList<EventOrderNode> connectPoints) {
        if (writtenVar != "") {
            WriteEventNode node = new WriteEventNode(writtenVar, expression, readEventNodes);
            if (readEventNodes.size() > 0) {
                // If there are read events which read Id/Binary/Unary Expression happened before this write event,
                // then connect this write event to the last read event
                eog.AddEOG(new EventOrderGraph(node), readEventNodes.get(readEventNodes.size() - 1));
            } else if (connectPoints.size() > 0) {
                // If there are read events which only read LiteralExpression happened before this write event
                // Then connect this write event to connect point(s) which was given before
                for (EventOrderNode connectPoint: connectPoints) {
                    eog.AddEOG(new EventOrderGraph(node), connectPoint);
                }
            } else {
                eog.AddEOG(new EventOrderGraph(node));
            }
        }
    }

    public static void AddSuffixVar(EventOrderNode node, HashMap<String, Integer> varSuffixMap) {
        if (node == null || varSuffixMap == null) {
            return;
        }

        if (node.suffixVarPref != null) {
            for (EventOrderNode nextNode: node.nextNodes) {
                AddSuffixVar(nextNode, varSuffixMap);
            }
            return;
        }

        if (!varSuffixMap.containsKey(node.varPreference)) {
            varSuffixMap.put(node.varPreference, 0);
        } else {
            varSuffixMap.put(node.varPreference, varSuffixMap.get(node.varPreference) + 1);
        }
        node.AddSuffix(varSuffixMap.get(node.varPreference));
        if (node instanceof WriteEventNode) {
            ((WriteEventNode) node).AddSuffixToExpression();
        }
        if (node.interleavingTracker.GetMarker() == InterleavingTracker.InterleavingMarker.Begin) {
            for (EventOrderNode nextNode: node.nextNodes) {
                AddSuffixVar(nextNode, varSuffixMap);
            }
            for (EventOrderNode endNode: node.interleavingTracker.GetRelatedNodes()) {
                if (endNode.nextNodes.size() > 1) {
                    DebugHelper.print("Invalid end interleaving node");
                } else if (endNode.nextNodes.size() == 0) {
                    //DebugHelper.print("No node behind.");
                } else {
                    AddSuffixVar(endNode.nextNodes.get(0), varSuffixMap);
                }
            }
        } else if (node.interleavingTracker.GetMarker() == InterleavingTracker.InterleavingMarker.End) {

        } else {
            for (EventOrderNode nextNode: node.nextNodes) {
                AddSuffixVar(nextNode, varSuffixMap);
            }
        }

    }

    private static ArrayList<IASTIdExpression> GetIdExpressionFromBinaryExpression(IASTBinaryExpression binaryExpr) {
        ArrayList<IASTIdExpression> idExpressions = new ArrayList<>();
        ArrayList<IASTExpression> operands = new ArrayList<>();
        operands.add(binaryExpr.getOperand2());
        operands.add(binaryExpr.getOperand1());
        for (IASTExpression expr : operands) {
            if (expr instanceof IASTIdExpression) {
                idExpressions.add(((IASTIdExpression)expr));
            } else if (expr instanceof IASTBinaryExpression) {
                idExpressions.addAll(GetIdExpressionFromBinaryExpression((IASTBinaryExpression)expr));
            } else if (expr instanceof IASTUnaryExpression) {
                idExpressions.addAll(GetIDExpressionFromUnaryExpression((IASTUnaryExpression) expr));
            } else if (expr instanceof IASTLiteralExpression) {

            } else {
                DebugHelper.print(expr.getClass().toString());
                // TODO: Additional cases
            }
        }
        return idExpressions;
    }

    private static ArrayList<IASTIdExpression> GetIDExpressionFromUnaryExpression(IASTUnaryExpression unaryExpr) {
        ArrayList<IASTIdExpression> idExpressions = new ArrayList<>();
        ArrayList<IASTExpression> operands = new ArrayList<>();
        operands.add(unaryExpr.getOperand());
        for (IASTExpression expr : operands) {
            if (expr instanceof IASTIdExpression) {
                idExpressions.add(((IASTIdExpression)expr));
            } else if (expr instanceof IASTBinaryExpression) {
                idExpressions.addAll(GetIdExpressionFromBinaryExpression((IASTBinaryExpression)expr));
            } else if (expr instanceof IASTUnaryExpression) {
                idExpressions.addAll(GetIDExpressionFromUnaryExpression((IASTUnaryExpression) expr));
            } else if (expr instanceof IASTLiteralExpression) {

            } else {
                DebugHelper.print(expr.getClass().toString());
                // TODO: Additional cases
            }
        }
        return idExpressions;
    }
}
