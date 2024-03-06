package com.mtv.encode.constraint;

import com.mtv.encode.eog.EventOrderGraph;
import com.mtv.encode.eog.ReadEventNode;
import com.mtv.encode.eog.WriteEventNode;
import com.microsoft.z3.*;
import com.mtv.encode.ast.ASTFactory;
import org.eclipse.cdt.core.dom.ast.*;
import org.javatuples.Triplet;

import java.util.ArrayList;

public class ConstraintManager {
    public Context ctx;
    public Solver solver;

    public ConstraintManager(Context ctx, Solver solver) {
        this.ctx = ctx;
        this.solver = solver;
    }
    public void BuildConstraints(EventOrderGraph eog, ASTFactory astFactory) throws Exception {
        if (eog.startNode == null) {
            return;
        }

        ArrayList<IASTDeclaration> globalVars = astFactory.getGlobalVarList();

        // Constraints are separated into 3 parts:
        // Write constraints
        WriteConstraintsManager.CreateWriteConstraints(ctx, solver, eog, globalVars);
        // Read/Write link constraints
        ArrayList<Triplet<String, ReadEventNode, WriteEventNode>> RWLSignatures = RWLConstraintsManager.CreateRWLC_ProgramFromProgram(ctx, solver, eog, globalVars);
        // Order constraints
        OrderConstraintsManager.CreateOrderConstraints(ctx, solver, RWLSignatures, eog);
    }
}
