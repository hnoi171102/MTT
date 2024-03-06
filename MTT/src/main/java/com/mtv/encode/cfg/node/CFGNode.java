package com.mtv.encode.cfg.node;

public abstract class CFGNode {
    protected CFGNode next;

    public CFGNode() {

    }

    public CFGNode(CFGNode next) {
        this.next = next;
    }

    public CFGNode getNext() {
        return next;
    }

    public void setNext(CFGNode next) {
        this.next = next;
    }

    public abstract void printNode();

}
