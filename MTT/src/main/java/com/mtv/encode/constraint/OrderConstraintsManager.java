package com.mtv.encode.constraint;

import com.mtv.encode.eog.EventOrderGraph;
import com.mtv.encode.eog.EventOrderNode;
import com.mtv.encode.eog.ReadEventNode;
import com.mtv.encode.eog.WriteEventNode;
import com.microsoft.z3.*;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderConstraintsManager {
    // Create all read/write link constraints in the event order graph using given context then store them in the given solver
    public static void CreateOrderConstraints(Context ctx, Solver solver,
                                              ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures,
                                              EventOrderGraph eog) {
        HashMap<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEventTracker = new HashMap<>();
        HashMap<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEventTracker = new HashMap<>();
        ArrayList<String> availableRWLSignatures = new ArrayList<>();
        for (Triplet<String, ReadEventNode, WriteEventNode> RWLSignature: RWLSignatures) {
            CreateReadTracker((ReadEventNode) RWLSignature.getValue(1), readEventTracker);
            CreateWriteTracker((WriteEventNode) RWLSignature.getValue(2), writeEventTracker);
            availableRWLSignatures.add(RWLSignature.getValue(0).toString());
        }

        for (Map.Entry<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEntry: readEventTracker.entrySet()) {
            for (Map.Entry<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEntry: writeEventTracker.entrySet()) {
                if (readEntry.getKey().varPreference.equals(writeEntry.getKey().varPreference)) {
                    CreateNegativeConstraint(solver, ctx, availableRWLSignatures, readEntry, writeEntry);
                }
            }
        }
    }

    private static void CreateReadTracker(ReadEventNode readEventNode, HashMap<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEventTrackerMap) {
        if (readEventTrackerMap.containsKey(readEventNode)) {
            return;
        }
        ArrayList<EventOrderNode> previousNodes = FindAllPreviousNodes(readEventNode);
        ArrayList<EventOrderNode> followingNodes = FindAllFollowingNodes(readEventNode);
        readEventTrackerMap.put(readEventNode, new Pair<>(previousNodes, followingNodes));
    }


    private static void CreateWriteTracker(WriteEventNode writeEventNode, HashMap<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEventTrackerMap) {
        if (writeEventTrackerMap.containsKey(writeEventNode)) {
            return;
        }
        ArrayList<EventOrderNode> previousNodes = FindAllPreviousNodes(writeEventNode);
        ArrayList<EventOrderNode> followingNodes = FindAllFollowingNodes(writeEventNode);
        writeEventTrackerMap.put(writeEventNode, new Pair<>(previousNodes, followingNodes));
    }

    private static ArrayList<EventOrderNode> FindAllPreviousNodes(EventOrderNode node) {
        ArrayList<EventOrderNode> previousNodes = new ArrayList<>();
        ArrayList<EventOrderNode> searchNodes = new ArrayList<>(node.previousNodes);

        while (searchNodes.size() > 0) {
            EventOrderNode searchNode = searchNodes.get(0);
            previousNodes.add(searchNode);
            searchNodes.remove(searchNode);
            searchNodes.addAll(searchNode.previousNodes);
        }
        return previousNodes;
    }
    private static ArrayList<EventOrderNode> FindAllFollowingNodes(EventOrderNode node) {
        ArrayList<EventOrderNode> followingNodes = new ArrayList<>();
        ArrayList<EventOrderNode> searchNodes = new ArrayList<>(node.nextNodes);

        while (searchNodes.size() > 0) {
            EventOrderNode searchNode = searchNodes.get(0);
            followingNodes.add(searchNode);
            searchNodes.remove(searchNode);
            searchNodes.addAll(searchNode.nextNodes);
        }
        return followingNodes;
    }

    private static void CreateNegativeConstraint(Solver solver, Context ctx, ArrayList<String> availableRWLSignatures,
                                                 Map.Entry<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEntry,
                                                 Map.Entry<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEntry) {
        String positiveSignature = RWLConstraintsManager.CreateRWLCSignature(readEntry.getKey(), writeEntry.getKey());
        ArrayList<String> negativeSignatures = CreateNegativeSignatures(readEntry, writeEntry);
        ArrayList<BoolExpr> availableNegativeSignatures = new ArrayList<>();
        for (String negativeSignature: negativeSignatures) {
            if (availableRWLSignatures.contains(negativeSignature)) {
                availableNegativeSignatures.add(ctx.mkBoolConst(negativeSignature));
            }
        }
        if (availableNegativeSignatures.size() > 0) {
            BoolExpr negativeExpression = ctx.mkNot(ctx.mkOr(availableNegativeSignatures.toArray(new BoolExpr[0])));
            BoolExpr fullExpression = ctx.mkImplies(ctx.mkBoolConst(positiveSignature), negativeExpression);
            solver.add(fullExpression);
        }

        ArrayList<String> deducedNegativeSignatures = CreateDeducedNegativeSignatures(readEntry, writeEntry);
        ArrayList<BoolExpr> availableDeducedNegativeSignatures = new ArrayList<>();
        for (String deducedNegativeSignature: deducedNegativeSignatures) {
            if (availableRWLSignatures.contains(deducedNegativeSignature)) {
                availableDeducedNegativeSignatures.add(ctx.mkBoolConst(deducedNegativeSignature));
            }
        }
        if (availableDeducedNegativeSignatures.size() > 0) {
            BoolExpr negativeExpression = ctx.mkNot(ctx.mkOr(availableDeducedNegativeSignatures.toArray(new BoolExpr[0])));
            BoolExpr fullExpression = ctx.mkImplies(ctx.mkBoolConst(positiveSignature), negativeExpression);
            solver.add(fullExpression);
        }
    }
    private static ArrayList<String> CreateNegativeSignatures(Map.Entry<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEntry,
                                                       Map.Entry<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEntry) {
        ArrayList<String> negativeSignatures = new ArrayList<>();
        for (EventOrderNode previousRead: readEntry.getValue().getValue0()) {
            for (EventOrderNode afterWrite: writeEntry.getValue().getValue1()) {
                if ((CheckDescendantNode(previousRead, writeEntry.getKey()) || CheckDescendantNode(writeEntry.getKey(), previousRead))
                        && (CheckDescendantNode(afterWrite, readEntry.getKey()) || CheckDescendantNode(readEntry.getKey(), afterWrite))) {
                    // Duplicate constraints
                } else if (previousRead instanceof ReadEventNode pR_Read) {
                    if (afterWrite instanceof ReadEventNode aW_Read) {
                        // Nothing happens due to no connection established between 2 read nodes
                    } else if (afterWrite instanceof WriteEventNode aW_Write) {
                        if (pR_Read.varPreference.equals(aW_Write.varPreference)
                            && pR_Read.varPreference.equals(readEntry.getKey().varPreference)) {
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(pR_Read, aW_Write));
                        }
                    }
                } else if (previousRead instanceof WriteEventNode pR_Write) {
                    if (afterWrite instanceof ReadEventNode aW_Read) {
                        if (pR_Write != writeEntry.getKey()
                                && pR_Write.varPreference.equals(writeEntry.getKey().varPreference)
                                && pR_Write.varPreference.equals(aW_Read.varPreference)) {
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(aW_Read, pR_Write));
                        }
                    } else if (afterWrite instanceof WriteEventNode aW_Write) {
                        // Nothing happens due to no connection established between 2 write nodes
                    }
                }
            }
        }
        for (EventOrderNode afterRead: readEntry.getValue().getValue1()) {
            for (EventOrderNode previousWrite: writeEntry.getValue().getValue0()) {
                if ((CheckDescendantNode(afterRead, writeEntry.getKey()) || CheckDescendantNode(writeEntry.getKey(), afterRead))
                        && (CheckDescendantNode(previousWrite, readEntry.getKey()) || CheckDescendantNode(readEntry.getKey(), previousWrite))) {
                    // Duplicate constraints
                } else if (afterRead instanceof ReadEventNode aR_Read) {
                    if (previousWrite instanceof ReadEventNode pW_Read) {
                        // Nothing happens due to no connection established between 2 read nodes
                    } else if (previousWrite instanceof WriteEventNode pW_Write) {
                        if (pW_Write.varPreference.equals(writeEntry.getKey().varPreference)
                                && pW_Write.varPreference.equals(aR_Read.varPreference)) {
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(aR_Read, pW_Write));
                        }
                    }
                } else if (afterRead instanceof WriteEventNode aR_Write) {
                    if (previousWrite instanceof ReadEventNode pW_Read) {
                        if (pW_Read.varPreference.equals(aR_Write.varPreference)) {
                            negativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(pW_Read, aR_Write));
                        }
                    } else if (previousWrite instanceof WriteEventNode pW_Write) {
                        // Nothing happens due to no connection established between 2 write nodes
                    }
                }
            }
        }
        return negativeSignatures;
    }

    private static ArrayList<String> CreateDeducedNegativeSignatures(Map.Entry<ReadEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> readEntry,
                                                                    Map.Entry<WriteEventNode, Pair<ArrayList<EventOrderNode>, ArrayList<EventOrderNode>>> writeEntry) {
        /*
        Deduced negative signatures is deduced from previous and following nodes (relative nodes for short) of both read node and write node
        Now we assume that deduced negative signatures requires reading node and relative nodes of write node don't stay in the same thread,
        so we use double check descendant to make sure they come from two different threads.
         */

        ArrayList<String> deducedNegativeSignatures = new ArrayList<>();

        // Stored following nodes of write node
        ArrayList<EventOrderNode> followingWNNodes = FindAllFollowingNodes(writeEntry.getKey());
        // Stored following write nodes of write node which have the same var with read node
        ArrayList<WriteEventNode> followingWNWriteNodes = new ArrayList<>();
        // Add following write nodes of write node to stored
        for (EventOrderNode followingWNNode: followingWNNodes) {
            if (followingWNNode instanceof WriteEventNode followingWNWriteNode
                    && !CheckDescendantNode(followingWNNode, readEntry.getKey())
                    && !CheckDescendantNode(readEntry.getKey(), followingWNNode)
                    && followingWNWriteNode.varPreference.equals(readEntry.getKey().varPreference)) {
                followingWNWriteNodes.add(followingWNWriteNode);
            }
        }

        // Stored previous nodes of read node
        ArrayList<EventOrderNode> previousRNNodes = FindAllPreviousNodes(readEntry.getKey());
        // Stored previous write nodes of read node
        ArrayList<WriteEventNode> previousRNWriteNodes = new ArrayList<>();
        // Add previous write nodes of read node to stored
        for (EventOrderNode previousRNNode: previousRNNodes) {
            if (previousRNNode instanceof WriteEventNode previousRNWriteNode) {
                previousRNWriteNodes.add(previousRNWriteNode);
            }
        }

        ArrayList<ReadEventNode> deducedFollowingReadNodes = new ArrayList<>();
        for (WriteEventNode followingWNWriteNode: followingWNWriteNodes) {
            ArrayList<EventOrderNode> deducedFollowingNodes = FindAllFollowingNodes(followingWNWriteNode);
            for (EventOrderNode deducedFollowingNode: deducedFollowingNodes) {
                if (deducedFollowingNode instanceof ReadEventNode deducedFollowingReadNode
                        && !deducedFollowingReadNodes.contains(deducedFollowingReadNode)) {
                    deducedFollowingReadNodes.add(deducedFollowingReadNode);
                }
            }
        }

        for (ReadEventNode deducedFollowingReadNode: deducedFollowingReadNodes) {
            boolean isDeduced = false;
            for (WriteEventNode previousRNWriteNode: previousRNWriteNodes) {
                if (deducedFollowingReadNode.varPreference.equals(previousRNWriteNode.varPreference)) {
                    if (!isDeduced) {
                        isDeduced = true;
                    } else {
                        deducedNegativeSignatures.add(RWLConstraintsManager.CreateRWLCSignature(deducedFollowingReadNode, previousRNWriteNode));
                    }
                }
            }
        }

        /*
        // TODO: Additional feature that can be used in future
        // All previous nodes of write node
        ArrayList<EventOrderNode> previousWNNodes = FindAllPreviousNodes(writeEntry.getKey());
        // All previous write nodes of write node which have the same var with read node
        ArrayList<WriteEventNode> previousWNWriteNodes = new ArrayList<>();
        for (EventOrderNode previousWNNode: previousWNNodes) {
            if (previousWNNode instanceof WriteEventNode previousWNWriteNode
                    && !CheckDescendantNode(previousWNNode, readEntry.getKey())
                    && !CheckDescendantNode(readEntry.getKey(), previousWNNode)
                    && previousWNWriteNode.varPreference.equals(readEntry.getKey().varPreference)) {
                previousWNWriteNodes.add(previousWNWriteNode);
            }
        }

        // TODO: Additional feature that can be used in future
        // All following nodes of read node
        ArrayList<EventOrderNode> followingRNNodes = FindAllFollowingNodes((readEntry.getKey()));
        // All following write nodes of read node
        ArrayList<WriteEventNode> followingRNWriteNodes = new ArrayList<>();
        for (EventOrderNode followingRNNode: followingRNNodes) {
            if (followingRNNode instanceof WriteEventNode followingRNWriteNode) {
                followingRNWriteNodes.add(followingRNWriteNode);
            }
        }
        */
        return deducedNegativeSignatures;
    }

    // Check if node "check" is a descendant of node "root"
    // Return true if "check" is a descendant of "root", else return false;
    private static boolean CheckDescendantNode(EventOrderNode root, EventOrderNode check) {
        if (check == root) {
            return true;
        }
        boolean result = false;
        for (EventOrderNode nextNode : root.nextNodes) {
            if (CheckDescendantNode(nextNode, check)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
