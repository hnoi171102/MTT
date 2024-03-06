package com.mtv.encode.eog;

import java.util.ArrayList;

/*
    EventOrderNode is the present of a read/write event in EventOrderGraph
    Each node has two pointer lists which point in its previous nodes and next nodes
 */
public abstract class EventOrderNode {
    // Original var preference. For examples: x, y, m, n...
    public String varPreference;
    // Var preference with suffix. For examples: x_1, y_2...
    public String suffixVarPref;

    public ArrayList<EventOrderNode> previousNodes;
    public ArrayList<EventOrderNode> nextNodes;

    public InterleavingTracker interleavingTracker;
    public boolean isVisited = false;

    public EventOrderNode(String varPreference) {
        this.varPreference = varPreference;
        this.interleavingTracker = new InterleavingTracker();
        this.previousNodes = new ArrayList<>();
        this.nextNodes = new ArrayList<>();
    }

    public EventOrderNode(String varPreference, ArrayList<EventOrderNode> previous, ArrayList<EventOrderNode> next) {
        this.varPreference = varPreference;
        this.interleavingTracker = new InterleavingTracker();
        this.previousNodes = (previous != null? previous : new ArrayList<>());
        this.nextNodes = (next != null? next : new ArrayList<>());
    }

    public void AddSuffix(int suffix) {
        suffixVarPref = varPreference + "_" + suffix;
    }
    public abstract void printNode(int level);
}
