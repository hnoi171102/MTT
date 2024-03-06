package com.mtv.encode.eog;

import com.mtv.debug.DebugHelper;

import java.util.ArrayList;

public class ReadEventNode extends EventOrderNode{

    public ReadEventNode(String varPreference) {
        super(varPreference);
    }

    public ReadEventNode(String varPreference, ArrayList<EventOrderNode> previous, ArrayList<EventOrderNode> next) {
        super(varPreference, previous, next);
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
        DebugHelper.print(marker + " - Read: " + (suffixVarPref == null ? varPreference : suffixVarPref));
    }
}
