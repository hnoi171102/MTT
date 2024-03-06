package com.mtv.encode.eog;

import com.mtv.debug.DebugHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventOrderGraph {
    // Assume that every EventOrderGraph only has one start point
    public EventOrderNode startNode;

    // Due to interleaving (multiple threads), EventOrderGraph can have many end points
    public ArrayList<EventOrderNode> endNodes;

    // After this node, program start running threads
    public EventOrderNode interleavingNode;


    public EventOrderGraph() {
        startNode = null;
        endNodes = new ArrayList<>();
    }

    public EventOrderGraph(EventOrderNode startNode, ArrayList<EventOrderNode> endNodes) {
        this.startNode = startNode;
        this.endNodes = endNodes;
    }

    public EventOrderGraph(EventOrderNode startNode) {
        this.startNode = startNode;
        SearchEndNodes();
    }

    public void SearchEndNodes() {
        ArrayList<EventOrderNode> currentEndNodes = new ArrayList<>();
        currentEndNodes.add(startNode);
        ArrayList<EventOrderNode> checkDeadEndResult = CheckDeadEnd(currentEndNodes);
        while (checkDeadEndResult.size() > 0) {
            for (EventOrderNode notDeadEndNode : checkDeadEndResult) {
                currentEndNodes = AddWithoutDuplicate(currentEndNodes, notDeadEndNode.nextNodes);
                currentEndNodes.remove(notDeadEndNode);
            }
            checkDeadEndResult = CheckDeadEnd(currentEndNodes);
        }
        this.endNodes = currentEndNodes;
    }

    public <T> ArrayList<T> AddWithoutDuplicate(ArrayList<T> aL1, ArrayList<T> aL2) {
        Set<T> nodeSet = new LinkedHashSet<>(aL1);
        nodeSet.addAll(aL2);
        return new ArrayList<T>(nodeSet);
    }

    private ArrayList<EventOrderNode> CheckDeadEnd(ArrayList<EventOrderNode> nodeList) {
        ArrayList<EventOrderNode> result = new ArrayList<>();
        for (EventOrderNode node : nodeList) {
            if (node.nextNodes.size() > 0) {
                result.add(node);
            }
        }
        return result;
    }



    public void printEOG() {
        System.out.println("..................................................");
        System.out.println(".........||||||||....||||||||....||||||||.........");
        System.out.println(".........||..........||....||....||...............");
        System.out.println(".........||||........||....||....||..||||.........");
        System.out.println(".........||..........||....||....||....||.........");
        System.out.println(".........||||||||....||||||||....||||||||.........");
        System.out.println("..................................................");
        printEOG(startNode, 0, 2);
        ResetVisited();
        System.out.println("..................................................");
        System.out.println(".........||||||||....||||||||....||||||||.........");
        System.out.println(".........||..........||....||....||...............");
        System.out.println(".........||||........||....||....||..||||.........");
        System.out.println(".........||..........||....||....||....||.........");
        System.out.println(".........||||||||....||||||||....||||||||.........");
        System.out.println("..................................................");
    }

    public void printEOG(EventOrderNode startNode , int initLevel, int step) {
        if (startNode == null || startNode.isVisited) {
            return;
        }
        startNode.isVisited = true;
        startNode.printNode(initLevel);
        startNode.interleavingTracker.PrintTracker(initLevel);

        if (startNode.interleavingTracker.GetMarker() == InterleavingTracker.InterleavingMarker.Begin) {
            for (EventOrderNode nextNode: startNode.nextNodes) {
                printEOG(nextNode, initLevel + step, step);
            }
            for (EventOrderNode endNode: startNode.interleavingTracker.GetRelatedNodes()) {
                if (endNode.nextNodes.size() > 1) {
                    DebugHelper.print("Invalid end interleaving node");
                } else if (endNode.nextNodes.size() == 0) {
                    //DebugHelper.print("No node behind.");
                } else {
                    printEOG(endNode.nextNodes.get(0), initLevel + step, step);
                }
            }
        } else if (startNode.interleavingTracker.GetMarker() == InterleavingTracker.InterleavingMarker.End) {

        } else {
            for (EventOrderNode nextNode: startNode.nextNodes) {
                printEOG(nextNode, initLevel + step, step);
            }
        }
    }

    public void ResetVisited() {
        if (startNode == null) {
            DebugHelper.print("ERROR: Empty start node!");
            return;
        }
        ResetNodeVisited(startNode);
    }

    public void ResetNodeVisited(EventOrderNode node) {
        node.isVisited = false;
        for (EventOrderNode nextNode: node.nextNodes) {
            ResetNodeVisited(nextNode);
        }
    }

    /*
        Connect new EOG to a specific point in current EOG
     */
    public void AddEOG(EventOrderGraph other, EventOrderNode connectPoint) {
        if (!connectPoint.nextNodes.contains(other.startNode)) {
            connectPoint.nextNodes.add(other.startNode);
        }
        if (!other.startNode.previousNodes.contains(connectPoint)) {
            other.startNode.previousNodes.add(connectPoint);
        }
        SearchEndNodes();
    }

    /*
        Connect new EOG to all end points of current EOG
     */
    public void AddEOG(EventOrderGraph other) {
        if (startNode == null) {
            this.startNode = other.startNode;
            this.endNodes = other.endNodes;
        } else {
            for (EventOrderNode endNode : endNodes) {
                ConnectNode(endNode, other.startNode);
            }
        }
        SearchEndNodes();
    }


    public static void ConnectNode(EventOrderNode node1, EventOrderNode node2) {
        if (!node1.nextNodes.contains(node2)) {
            node1.nextNodes.add(node2);
        }
        if (!node2.previousNodes.contains(node1)) {
            node2.previousNodes.add(node1);
        }
    }
}
