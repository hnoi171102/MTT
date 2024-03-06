package com.mvs.cfg.index;

import com.mvs.cfg.build.ASTFactory;
import com.mvs.cfg.node.CFGNode;
import com.mvs.cfg.node.PlainNode;
import com.mvs.cfg.utils.ExpressionHelper;
import com.mvs.cfg.utils.FunctionHelper;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.model.MethodInfo;

import java.util.ArrayList;

/**
 * new VariableManage()
 * new VariableManage(IASTFunctionDefinition func)
 * build(IASTFunctionDefinition func): void
 * getvariableList() : ArrayList<Var>
 * getVar(int index) : Var
 * getVar(String name) : Var
 * addVar(Var var) : void
 * addVar(String type, String name, String funcName, int index) : void
 *
 * @author va
 */

public class VariableManage {
    private ArrayList<Var> variableList;

    private ArrayList<Var> inputList; //sau khi encode

    private int currentThreadIndex = 0;

    public VariableManage() {
        this.variableList = new ArrayList<>();
        this.inputList = new ArrayList<>();
        this.currentThreadIndex = 0;
    }

    public VariableManage(IASTFunctionDefinition func) {
        this.variableList = new ArrayList<>();
        this.inputList = new ArrayList<>();
        this.currentThreadIndex = 0;
        build(func);
    }

    public void resetSsaIndex() {
        for (Var var : variableList) {
            var.setSsaIndex(0);
        }
    }

    public void setThreadIndex(int threadIndex) {
        for (Var var : variableList) {
            var.setThreadIndex(threadIndex);
        }
    }

    public void setCurrentThreadIndex(int threadIndex) {
        this.currentThreadIndex = threadIndex;
    }

    public int getCurrentThreadIndex() {
        return this.currentThreadIndex;
    }

    public void increaseCurrentThreadIndex() {
        this.currentThreadIndex++;
    }

    public void addInputVarList(Var var) {
        inputList.add(var.clone());
    }

    public ArrayList<Var> getInputList() {
        return inputList;
    }

    /**
     * @return
     */
    public ArrayList<Var> getVariableList() {
        return variableList;
    }

    public void setVariableList(ArrayList<Var> variableList) {
        this.variableList = variableList;
    }

    public void setInputList(ArrayList<Var> inputList) {
        this.inputList = inputList;
    }

    /**
     * them bien moi vao day VariableManage
     * luu y neu da co trong danh sach => danh dau isDuplicated == true;
     *
     * @param otherVM
     */
    public void concat(VariableManage otherVM) {
        ArrayList<Var> otherList = otherVM.getVariableList();
        for (Var var : otherList) {
            if (!(isHas(var.getName()))) {
                this.getVariableList().add(var);
            } else {
                var.setIsDuplicated(true);
            }
        }
    }

    public void addVar(Var var) {
        variableList.add(var);
    }

    public void addVar(String type, String name, String funcName, int ssaIndex, int threadIndex, int wrIndex) {
        Var var = new Var(type, name + "_" + FunctionHelper.getShortenName(funcName), ssaIndex, threadIndex, wrIndex);
        variableList.add(var);
    }

    public Var getVar(int index) {
        return inputList.get(index);
    }

    public Var getVar(String name) {
        for (Var var : variableList) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }

    /**
     * kiem tra xem bien truyen vao da co trong danh sach chua?
     *
     * @param name
     * @return true/ false
     */
    public boolean isHas(String name) {
        if (this.variableList == null) return false;
        for (Var var : this.variableList) {
            if (name.equals(var.getName())) {
                return true;
            }
        }
        return false;
    }

    public int getSize() {
        return this.variableList.size();
    }

    public int getInputListSize() {
        return this.inputList.size();
    }

    public void printList() {
        if (this.variableList == null) {
            System.out.println("NULL");
        }
        for (Var var : this.variableList) {
            System.out.println(var.getVarWithIndex());
            //System.out.println(var.getName());
        }
    }

    /**
     * tao variableList voi dau vao
     */

    public void build(IASTFunctionDefinition func) {
        ArrayList<Var> params = getParameters(func);
        ArrayList<Var> localVars = new ArrayList<>();
        ArrayList<Var> globalVars = getGlobalVars(func);

        String funcName = FunctionHelper.getShortenName(func);

        localVars = getLocalVar(func, funcName, localVars);
        for (Var var : globalVars) {
            this.variableList.add(var);
        }
        for (Var param : params) {
            this.variableList.add(param);
        }
        for (Var var : localVars) {
            this.variableList.add(var);
        }
        //Neu function khac void, them bien return
        if (!(FunctionHelper.getFunctionType(func).equals("void"))) {
            this.variableList.add(getReturn(func));
        }

    }

    /**
     * Lay cac bien global
     *
     * @param func
     * @return
     */

    private ArrayList<Var> getGlobalVars(IASTFunctionDefinition func) {
        ASTFactory ast = new ASTFactory(func.getTranslationUnit());
        ArrayList<Var> variableList = new ArrayList<>();
        ArrayList<IASTDeclaration> declarations = ast.getGlobarVarList();
        for (IASTDeclaration declaration : declarations) {
            IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declaration;
            String type = simpDecl.getDeclSpecifier().toString();
            //Chu y truong hop int a, b, c, ...;
            for (IASTDeclarator declarator : simpDecl.getDeclarators()) {
                String nameVar = declarator.getName().toString();
                Var var = new Var(type, nameVar);
                variableList.add(var);
            }
        }
        return variableList;
    }

    private Var getReturn(IASTFunctionDefinition func) {
        IASTNode typeFunction = func.getDeclSpecifier();
        Var var = new Var(typeFunction.getRawSignature(), "return" + "_" + FunctionHelper.getShortenName(func));

        return var;
    }

    /**
     * tra ve danh sach tham so truyen vao ham (neu co)

     * @return List Var
     */
    private ArrayList<Var> getParameters(IASTFunctionDefinition func) {
        ArrayList<Var> params = new ArrayList<>();
        IASTNode[] nodes = func.getDeclarator().getChildren();

        IASTParameterDeclaration paramDecl = null;
        for (IASTNode node : nodes) {
            if (node instanceof IASTParameterDeclaration) {
                if (node.getRawSignature().equals("void")) {
                    return params;
                }
                paramDecl = (IASTParameterDeclaration) node;

                IASTNode[] paramDecls = paramDecl.getChildren();
                for (int i = 0; i < paramDecls.length; i++) {
                    if (paramDecls[i] instanceof IASTSimpleDeclSpecifier
                            && paramDecls[i + 1] instanceof IASTDeclarator) {
                        Var var = new Var(paramDecls[i].getRawSignature(),
                                paramDecls[i + 1].getRawSignature() + "_" +
                                        FunctionHelper.getShortenName(func), -1, -1, -1);
                        params.add(var);
                    }
                }
            }
        }
        return params;
    }

    /**
     * @param node
     * @param funcName
     * @param list
     * @return danh sach bien dia phuong trong ham
     */
    private ArrayList<Var> getLocalVar(IASTNode node, String funcName, ArrayList<Var> list) {
        // find init
        IASTNode[] children = node.getChildren();
        String type;
        String name;
        Var var;

        IASTDeclarator[] declarations;

        if (node instanceof IASTSimpleDeclaration) {
            int init = -1;
            type = ((IASTSimpleDeclaration) node).getDeclSpecifier().getRawSignature();

            declarations = ((IASTSimpleDeclaration) node).getDeclarators();
            name = declarations[0].getName().getRawSignature();

            var = new Var(type, name + "_" + FunctionHelper.getShortenName(funcName), init, init, init);
            list.add(var);
        }
        /*
         * Neu co chua loi goi ham, them vao bien co ten giong loi goi ham
         * Ex: a = sum(3); -> a = sum_3
         */
        if (node instanceof IASTFunctionCallExpression) {
            if (!((IASTFunctionCallExpression) node).getExpressionType().toString().equals("void")) {
                //System.err.println(((IASTFunctionCallExpression) node).getExpressionType());
                IASTFunctionCallExpression call = (IASTFunctionCallExpression) node;
                String callName = call.getFunctionNameExpression().toString();
                String params = "";
                if (call.getArguments().length > 0) {
                    for (IASTNode param : FunctionHelper.getArguments(call)) {
                        params += "_" + param.toString();
                    }

                }
                var = new Var(call.getExpressionType().toString(), callName + params, 0, -1, -1);
                list.add(var);
            }
        }
        // return

        for (IASTNode run : children) {
            getLocalVar(run, funcName, list);
        }
        return list;
    }

    public Var getVariable(int ssaIndex) {
        return variableList.get(ssaIndex);
    }

    public Var getVariable(String name) {
        for (Var var : variableList) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }


    /*
     * add bien toan cuc
     */

//	public void getGloble( IASTNode node){
//		IASTNode[] children = node.getChildren();
//		if (node instanceof IASTIdExpression && !(isHas(node.getRawSignature()))){
//			String name = node.getRawSignature();
//			Var var = new Var();
//			this.variableList.add(var);
//		}
//		
//		for ( IASTNode run : children){
//			getGloble(run);
//		}
//	}

    public void setThreadIndexWithNode(PlainNode node) {
        String threadName = "";
        if (node instanceof PlainNode) {
            PlainNode plainNode = (PlainNode) node;
            String funcName = plainNode.getFunc().getScope().toString();
            threadName = String.valueOf(funcName.charAt(funcName.length() - 1));
        }

        if (node instanceof IASTIdExpression) {
            String name = ((IASTIdExpression) node).getRawSignature();
            Var var = getVariable(name);
            if (var != null) {
                if (!threadName.isEmpty()) {
                    int threadIndex = Integer.parseInt(threadName);
                    var.setThreadIndex(threadIndex);
                } else {
                    var.setThreadIndex(0);
                }
            }
        }
    }

}
