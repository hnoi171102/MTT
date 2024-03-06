package com.mvs.cfg.index;

public class Var {

    private String type;
    private String name;
    private int ssaIndex = 0; //mac dinh -1
    private int threadIndex = 0;
    private int wrIndex = -1;
    private boolean isDuplicated;

    public int getIndexInvariant() {
        return indexInvariant;
    }

    public void setIndexInvariant(int indexInvariant) {
        this.indexInvariant = indexInvariant;
    }

    private int indexInvariant = -1; //the index use in java.invariant

    public Var() {
    }

    public Var(String type, String name) {
        this.type = type;
        this.name = name;
        this.isDuplicated = false;
    }

    public Var(String type, String name, int ssaIndex, int threadIndex, int wrIndex) {
        this.type = type;
        this.name = name;
        this.ssaIndex = ssaIndex;
        this.threadIndex = threadIndex;
        this.wrIndex = wrIndex;
        this.isDuplicated = false;
    }

    public Var(Var other) {
        name = other.name;
        type = other.type;
        ssaIndex = other.ssaIndex;
        threadIndex = other.threadIndex;
        wrIndex = other.wrIndex;
        isDuplicated = other.isDuplicated;
    }

    public boolean getIsDuplicated() {
        return isDuplicated;
    }

    public void setIsDuplicated(boolean restart) {
        this.isDuplicated = restart;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSsaIndex() {
        return ssaIndex;
    }

    public void setSsaIndex(int ssaIndex) {
        this.ssaIndex = ssaIndex;
    }

    public int getThreadIndex() {
        return threadIndex;
    }

    public void setThreadIndex(int threadIndex) {
        this.threadIndex = threadIndex;
    }

    public int getWrIndex() {
        return wrIndex;
    }

    public void setWrIndex(int wrIndex) {
        this.wrIndex = wrIndex;
    }

    public void increaseSSA() {
        ssaIndex++;
    }
    public void decreaseSSA() {
        ssaIndex--;
    }

    public void increaseThreadIndex() {
        threadIndex++;
    }

    public void increaseWrIndex() {
        wrIndex++;
    }

    public void decreaseWrIndex() {
        wrIndex--;
    }

    public void resetWrIndex() {
        wrIndex = -1;
    }

    public void resetSSA() {
        ssaIndex = -1;
    }

    public void resetThreadIndex() {
        threadIndex = 0;
    }

    public void reset() {
        resetSSA();
        resetThreadIndex();
        resetWrIndex();
    }

    public String getVariableWithIndex() {
        if (ssaIndex == -3) return name;
        if (wrIndex == 0) {
            return name + "_" + "w" + threadIndex + ssaIndex;
        } else if (wrIndex == 1) {
            return name + "_" + "r" + threadIndex + ssaIndex;
        }
        return name + "_" + threadIndex + ssaIndex;
    }

    // -3 danh dau bien nhan gia tri tra ve cua ham
    public String getVarWithIndex() {
        if (ssaIndex == -3) return name;
        return name + "_" + ssaIndex;
    }

    public Var clone() {
        return new Var(this);
    }

    @Override
    public String toString() {
        return "Var{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", ssaIndex=" + ssaIndex +
                ", threadIndex=" + threadIndex +
                ", wrIndex=" + wrIndex +
                ", isDuplicated=" + isDuplicated +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Var)) return false;
        Var other = (Var) obj;
        return other.name.equals(this.name) && other.ssaIndex == this.ssaIndex && other.threadIndex == this.threadIndex;
    }

    public boolean hasInitialized() {
        return ssaIndex > -1;
    }

}
