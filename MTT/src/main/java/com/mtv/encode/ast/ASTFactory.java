package com.mtv.encode.ast;

import com.mtv.debug.DebugHelper;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
import org.eclipse.core.runtime.CoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ASTFactory {

    private static IASTTranslationUnit translationUnit;
    private String fileLocation = "";

    public ASTFactory(IASTTranslationUnit ast) {
        translationUnit = ast;
    }

    public ASTFactory(String fileLocation) {
        FileContent fileContent = FileContent.createForExternalFileLocation(fileLocation);
        IncludeFileContentProvider includeFile = IncludeFileContentProvider.getEmptyFilesProvider();
        IParserLogService log = new DefaultLogService();
        String[] includePaths = new String[0];
        IScannerInfo info = new ScannerInfo(new HashMap<String, String>(), includePaths);
        try {
            translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info, includeFile, null, 0, log);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public static void PrintAST() {
        ASTPrinter.print(translationUnit);
    }
    private static void printTree(IASTNode node, int index) {
        IASTNode[] children = node.getChildren();

        for (int i = 0; i < index; i++) {
            System.out.print(" ");
        }

        DebugHelper.print("-" + node.getClass().getSimpleName() + " -> " + node.getRawSignature());
        for (IASTNode iastNode : children)
            printTree(iastNode, index + 2);
    }

    public IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }

    public void setTranslationUnit(IASTTranslationUnit tranUnit) {
        translationUnit = tranUnit;
    }

    public void setFileLocation(String fileName) {
        fileLocation = fileName;
    }

    public List<String> getFunctionSignatures() {
        if (translationUnit == null) return null;
        List<String> funcList = new ArrayList<>();
        for (IASTNode run : translationUnit.getDeclarations()) {
            if (run instanceof IASTFunctionDefinition) {
                String name = ((IASTFunctionDefinition) run).getDeclarator().getName().toString();
                funcList.add(name);
            }
        }
        return funcList;
    }

    public ArrayList<IASTFunctionDefinition> getListFunction() {
        if (translationUnit == null) return null;
        ArrayList<IASTFunctionDefinition> funcList = new ArrayList<>();
        for (IASTNode run : translationUnit.getDeclarations()) {
            if (run instanceof IASTFunctionDefinition) {
                funcList.add((IASTFunctionDefinition) run);
            }
        }
        return funcList;
    }

    public ArrayList<String> getGlobalVarStrList() {
        ArrayList<String> result = new ArrayList<>();
        for (IASTDeclaration decl : this.getGlobalVarList()) {
            IASTDeclarator[] declarators = ((IASTSimpleDeclaration) decl).getDeclarators();
            for (IASTDeclarator declarator : declarators) {
                String tmp = declarator.getName().toString();
                result.add(tmp);
            }
        }
        return result;
    }

    public ArrayList<IASTDeclaration> getGlobalVarList() {
        if (translationUnit == null) return null;
        ArrayList<IASTDeclaration> varList = new ArrayList<>();
        for (IASTDeclaration run : translationUnit.getDeclarations()) {
            if (run instanceof IASTSimpleDeclaration) {
                boolean isVar = true;
                IASTDeclarator[] declors = ((IASTSimpleDeclaration) run).getDeclarators();
                for (IASTDeclarator decl : declors) {
                    if (decl instanceof IASTStandardFunctionDeclarator) {
                        isVar = false;
                    }
                    if (isVar)  {
                        if (!varList.contains(run)) {
                            varList.add(run);
                        }
                    }
                }
            }
        }
        return varList;
    }

    public IASTFunctionDefinition getFunction(int index) {
        ArrayList<IASTFunctionDefinition> funcList = getListFunction();
        if(index < funcList.size()){
            return funcList.get(index);
        }
        return null;
    }

    public IASTFunctionDefinition getFunction(String name) {
        String funcName = null;
        ArrayList<String> funcNameList = new ArrayList<>();
        ArrayList<IASTFunctionDefinition> funcList = getListFunction();
        for (IASTFunctionDefinition func : funcList) {
            funcName = func.getDeclarator().getName().toString();
            funcNameList.add(funcName);
            if (name.equals(funcName)) {
                return func;
            }
        }
        System.exit(1);
        return null;
    }
    public IASTFunctionDefinition getMain() {
        return FunctionHelper.getFunction(this.getListFunction(), "main");
    }
}
