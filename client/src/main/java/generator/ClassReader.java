package generator;

import generator.coverage.branch.Branch;
import generator.coverage.branch.BranchPool;
import generator.utils.LoggingUtils;
import generator.utils.Randomness;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import runtime.Random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

public class ClassReader {

    private List<Field> fieldList = new ArrayList<>();
    private List<Constructor> constructors = new ArrayList<>();

    public List<Constructor> getConstructors() { return constructors; }

    public String getClassName() {
        return className;
    }

    public Field getDeclaredField(String fieldName) {
        return fieldList.stream().filter(field -> fieldName.equals(field.getName()))
                .findAny()
                .orElse(null);
    }

    public Field getField(String name) {
        return getDeclaredField(name);
    }

    public class Parameter {
        public DataType type;
        public String variableName;
        public Object value;

        public Parameter clone() {
            Parameter parameter = new Parameter();
            parameter.value = this.value;
            parameter.variableName = this.variableName;
            parameter.type = this.type;
            return parameter;
        }
    }

    public class Method {

        public int lineNo;
        public String methodName;
        public DataType returnType;
        public List<Parameter> parameters = new ArrayList<>();
        public CPPASTBinaryExpression ifCondition;
        public CPPASTSwitchStatement switchCondition;
        public ClassReader classReader;
        public List<String> conditionList = new ArrayList<>();

        public int getModifiers() {
            return 0;
        }

        public Method clone() {
            Method method = new Method();
            for(int i =0; i < this.parameters.size(); ++i) {
                method.parameters.add(this.parameters.get(i).clone());
            }
            method.lineNo = this.lineNo;
            method.returnType = this.returnType;
            method.classReader = this.classReader;
            method.methodName = this.methodName;
            return method;
        }
        public DataType[] getGenericParameterTypes() {
            List<DataType> dataTypes = new ArrayList<>();
            for (Parameter parameter : parameters) {
                dataTypes.add(parameter.type);
            }
            return dataTypes.toArray(new DataType[0]);
        }

        public String getName() {
            if(methodName.contains(":")) {
                return methodName.substring(methodName.lastIndexOf(":")+1, methodName.indexOf("(")).trim();
            }
            return methodName.substring(methodName.lastIndexOf(" "), methodName.indexOf("(")).trim();
        }

        public DataType[] getParameterTypes() {
            return getTypeParameters();
        }

        public ClassReader getDeclaringClass() {
            return classReader;
        }

        public DataType[] getTypeParameters() {
           ArrayList<DataType> dataTypes = new ArrayList<>();
           for(Parameter parameter : parameters) {
               dataTypes.add(parameter.type);
           }
           return dataTypes.toArray(new DataType[0]);
        }

        public DataType getGenericReturnType() {
            return returnType;
        }

        public DataType getReturnType() {
            return returnType;
        }

        public String toGenericString() {
            return getName();
        }

        public Parameter[] getParameters() {
            return parameters.toArray(new Parameter[0]);
        }
    }

    private IASTTranslationUnit translationUnitCPP;
    private IASTTranslationUnit translationUnitH;
    private String className;
    private Map<String, List<Method>> methods = new HashMap<>();
    private Map<String, String> returnMap = new HashMap<>();

    public String getCanonicalName() {
        return className.concat(".cpp");
    }

    private void executeMake() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "make main");
        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal != 0) {
                LoggingUtils.getGeneratorLogger().info("* " + output);
                System.exit(0);
            }

        } catch (IOException | InterruptedException e) {
            LoggingUtils.getGeneratorLogger().info("* " + e.getMessage());
            System.exit(0);
        }
    }

    public ClassReader()
    {
        returnMap.put("void", "V");
        returnMap.put("int", "I");
        returnMap.put("float", "F");
        returnMap.put("double", "D");
        executeMake();
    }

    public Method[] getDeclaredMethods()
    {
        List<Method> methodList = new ArrayList<>();
        for(Map.Entry<String, List<Method> > entry : methods.entrySet())
        {
            for (int i =0; i < entry.getValue().size(); i++) {
                methodList.add(entry.getValue().get(i));
            }
        }
        return methodList.toArray(new Method[0]);
    }

    public Field[] getDeclaredFields() {
        return fieldList.toArray(new Field[0]);
    }

    public void readFile(String absoluteFilePath) throws CoreException {
        if(absoluteFilePath.endsWith(".cpp"))
        {
            FileContent fileContent = FileContent.createForExternalFileLocation(absoluteFilePath);
            className = absoluteFilePath.substring(absoluteFilePath.lastIndexOf(Path.SEPARATOR )+1, absoluteFilePath.lastIndexOf("."));
            Map definedSymbols = new HashMap();
            String[] includePaths = new String[0];
            IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
            IParserLogService log = new DefaultLogService();
            IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();
            int opts = 8;
            translationUnitCPP = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info, emptyIncludes, null, opts, log);
        }
        String headerPath = absoluteFilePath.replaceAll(".cpp", ".h");
        FileContent fileContent = FileContent.createForExternalFileLocation(headerPath);
        if(fileContent != null) {
            Map definedSymbols = new HashMap();
            String[] includePaths = new String[0];
            IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
            IParserLogService log = new DefaultLogService();
            IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();
            int opts = 8;
            translationUnitH = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info, emptyIncludes, null, opts, log);
        }
    }

    public IASTPreprocessorIncludeStatement[] getIncludes() {
        List<IASTPreprocessorIncludeStatement> includeList = new ArrayList<IASTPreprocessorIncludeStatement>();
        if(translationUnitCPP != null) {
            includeList = Arrays.stream(translationUnitCPP.getIncludeDirectives()).collect(Collectors.toList());
        }
        if(translationUnitH != null) {
            includeList.addAll(Arrays.stream(translationUnitH.getIncludeDirectives()).collect(Collectors.toList()));
        }

        IASTPreprocessorIncludeStatement[] iastPreprocessorIncludeStatements = new IASTPreprocessorIncludeStatement[includeList.size()];
        return includeList.toArray(iastPreprocessorIncludeStatements);
    }

    public boolean isVisible(IASTNode current) {
        IASTNode declator = current.getParent().getParent();
        IASTNode[] children = declator.getChildren();
        for (IASTNode iastNode : children) {
            if ((iastNode instanceof ICPPASTVisibilityLabel)) {
                return 1 == ((ICPPASTVisibilityLabel) iastNode).getVisibility();
            }
        }
        return false;
    }

    public void parseTree() {
        if(translationUnitCPP != null)
            parseTree(translationUnitCPP, 1);
        if(translationUnitH != null)
            parseTree(translationUnitH, 1);
        for(Map.Entry<String, List<Method> > entry : methods.entrySet())
        {
            if(entry.getValue().size() == 1)
            {
                BranchPool.getInstance(this).addBranchlessMethod(className, entry.getKey(), entry.getValue().get(0).lineNo);
            }
            else
            {
                for(Method method : entry.getValue())
                {
                    String methondName = entry.getKey()+"(";
                    String variableList = "";
                    for(Parameter parameter : method.parameters)
                    {
                        variableList = variableList + returnMap.get(parameter.type) + ",";
                    }
                    if(method.parameters.size() != 0) {
                        variableList = variableList.substring(0, variableList.lastIndexOf(","));
                    }
                    methondName = methondName + variableList + ")" + returnMap.get(method.returnType);
                    Branch b = new Branch(className, methondName, method.lineNo);
                    BranchPool.getInstance(this).addBranchToMap(b);
                }
            }
        }
        visit();
    }



    private void parseTree(IASTNode node, int index) {
        IASTNode[] children = node.getChildren();
        if (node.getClass().getSimpleName().equals("CPPASTFunctionDefinition")) {
            if (!node.getChildren()[0].getRawSignature().equals("")) {
                Method method = new Method();
                method.lineNo = node.getFileLocation().getStartingLineNumber();
                method.returnType = new DataType(node.getChildren()[0].getRawSignature(), this);
                method.methodName = node.getChildren()[0].getRawSignature() + " " + node.getChildren()[1].getChildren()[0].getRawSignature() + "(";
                for (IASTNode iastNode : node.getChildren()[1].getChildren()) {
                    Parameter parameter = new Parameter();
                    if (iastNode.getClass().getSimpleName().equals("CPPASTParameterDeclaration")) {
                        parameter.type = new DataType(iastNode.getChildren()[0].getRawSignature(), this);
                        parameter.variableName = iastNode.getChildren()[1].getRawSignature();
                        method.parameters.add(parameter);
                        method.methodName = method.methodName + iastNode.getChildren()[0].getRawSignature() + ",";
                    }
                }
                int i = method.methodName.lastIndexOf(',');
                if (i > 0)
                    method.methodName = method.methodName.substring(0, method.methodName.lastIndexOf(',')) + ")";
                else
                    method.methodName = method.methodName + ")";
                List<HashMap<String, String>> list = new ArrayList<>();
                for(IASTNode iastNode : node.getChildren()[2].getChildren()) {
                    Method elseMeth = method.clone();
                    HashMap<String, String> branchMap = new HashMap<>();
                    list.add(branchMap);
                    insertConditions(iastNode, method, elseMeth, branchMap);
                    //insertIfClauses(iastNode, method);
                    //insertSwitchClauses(iastNode, method);
                }
                //if (!Properties.GOALORI) {
                    insertToMap(method);
                //}
                if (Properties.GOALORI) {
                    for(int j = 0; j < list.size() - 1; ++j) {
                        int size = method.getParameters().length;
                        String[] arr = new String[size];
                        allCombination(size, arr, method, list.get(j), 0);
                    }
                }
            }
        }
        //System.out.println(String.format(new StringBuilder("%1$").append(index * 2).append("s").toString(), new Object[]{"-"}) + node.getClass().getSimpleName() + offset + " -> " + (printContents ? node.getRawSignature().replaceAll("\n", " \\ ") : node.getRawSignature().subSequence(0, 5)));
        for (IASTNode iastNode : children)
            parseTree(iastNode, index + 1);
    }

    private void allCombination(int n, String arr[], Method method, HashMap<String, String> branchMap, int i) {

        if (i == n) {
            Method method1 = method.clone();
            for (int k = 0; k < i; k++) {
                method1.getParameters()[k].value = arr[k];
            }
            insertToMap(method1);
            return;
        }

        arr[i] = branchMap.get(method.parameters.get(i).variableName.concat("False"));
        allCombination(n, arr, method, branchMap, i + 1);

        arr[i] = branchMap.get(method.parameters.get(i).variableName.concat("True"));
        allCombination(n, arr, method, branchMap, i + 1);
    }

    private void insertParamDefault(CPPASTBinaryExpression cppastBinaryExpression, Method method, Method elseMethod, HashMap<String, String> branchMap) {

        int ops = cppastBinaryExpression.getOperator();
        String type1 = cppastBinaryExpression.getOperand1().getExpressionType().toString();
        if (cppastBinaryExpression.getOperand1() instanceof CPPASTBinaryExpression) {
            insertParamDefault((CPPASTBinaryExpression) cppastBinaryExpression.getOperand1(), method, elseMethod, branchMap);
        }
        String var1 = cppastBinaryExpression.getOperand1().toString();
        String type2 = cppastBinaryExpression.getOperand1().getExpressionType().toString();
        if (cppastBinaryExpression.getOperand2() instanceof CPPASTBinaryExpression) {
            insertParamDefault((CPPASTBinaryExpression) cppastBinaryExpression.getOperand2(), method, elseMethod, branchMap);
        }
        String var2 = cppastBinaryExpression.getOperand2().toString();
        if (cppastBinaryExpression.getOperand1() instanceof CPPASTBinaryExpression ||
        cppastBinaryExpression.getOperand2() instanceof CPPASTBinaryExpression) {
            return;
        }
        String varName1 = "", varName2="";
        String var3 ="";
        int val = 0;
        int elseval;
        switch (ops) {
            case 10:
                if (cppastBinaryExpression.getOperand2() instanceof CPPASTLiteralExpression) {
                    val = Integer.parseInt(var2) + 1;
                    varName1 = cppastBinaryExpression.getOperand1().toString();
                    var1 = String.valueOf(val);
                    elseval = Integer.parseInt(var2) - 1;
                    var3 = String.valueOf(elseval);
                } else if (cppastBinaryExpression.getOperand1() instanceof CPPASTLiteralExpression) {
                    val = Integer.parseInt(var1) + 1;
                    varName2 = cppastBinaryExpression.getOperand2().toString();
                    var2 = String.valueOf(val);
                    elseval = Integer.parseInt(var2) - 1;
                    var3 = String.valueOf(elseval);
                } else {
                    val = Randomness.nextInt(9999999);
                    varName1 = cppastBinaryExpression.getOperand1().toString();
                    varName2 = cppastBinaryExpression.getOperand2().toString();
                    var1 = String.valueOf(val);
                    var2 = String.valueOf(val + 1);
                    var3 = String.valueOf(val - 1);
                }
                break;
            case 8:
            case 9:
            case 11:
            case 15:
                if (cppastBinaryExpression.getOperand2() instanceof CPPASTLiteralExpression) {
                    val = Integer.parseInt(var2) - 1;
                    varName1 = cppastBinaryExpression.getOperand1().toString();
                    var1 = String.valueOf(val);
                    elseval = Integer.parseInt(var2) + 1;
                    var3 = String.valueOf(elseval);
                    branchMap.put(varName1.concat("True"), var1);
                    branchMap.put(varName1.concat("False"), var3);
                } else if (cppastBinaryExpression.getOperand1() instanceof CPPASTLiteralExpression) {
                    val = Integer.parseInt(var1) - 1;
                    varName2 = cppastBinaryExpression.getOperand2().toString();
                    var2 = String.valueOf(val);
                    elseval = Integer.parseInt(var2) + 1;
                    var3 = String.valueOf(elseval);
                    branchMap.put(varName1.concat("True"), var2);
                    branchMap.put(varName1.concat("False"), var3);
                } else {
                    val = Randomness.nextInt(9999999);
                    varName1 = cppastBinaryExpression.getOperand1().toString();
                    varName2 = cppastBinaryExpression.getOperand2().toString();
                    var1 = String.valueOf(val);
                    var2 = String.valueOf(val + 1);
                    var3 = String.valueOf(val - 1);
                    branchMap.put(varName1.concat("True"), var2);
                    branchMap.put(varName1.concat("False"), var3);
                }
                break;
        }

        if (!Properties.GOALORI) {
            for(int i =0; i< method.parameters.size(); ++i) {
                if(!"".equals(method.parameters.get(i).value)) {
                    if (method.parameters.get(i).variableName.equals(varName1)) {
                        method.parameters.get(i).value = var1;
                        elseMethod.parameters.get(i).value = var3;
                        break;
                    } else if (method.parameters.get(i).variableName.equals(varName2)) {
                        method.parameters.get(i).value = var2;
                        elseMethod.parameters.get(i).value = var3;
                        break;
                    }
                }
            }
        }
    }
    private void insertConditions(IASTNode iastNode, Method method, Method elseMethod, HashMap<String, String> branchMap) {
        if(iastNode instanceof CPPASTIfStatement) {
            CPPASTIfStatement cppastIfStatement = (CPPASTIfStatement) iastNode;
            if (cppastIfStatement.getThenClause() != null) {
                CPPASTBinaryExpression cppastBinaryExpression = (CPPASTBinaryExpression) cppastIfStatement.getConditionExpression();
                if (cppastBinaryExpression.getOperand1() instanceof CPPASTUnaryExpression) {
                    insertParamDefault((CPPASTBinaryExpression) ((CPPASTUnaryExpression) cppastBinaryExpression.getOperand1()).getOperand(), method, elseMethod, branchMap);
                } else {
                    insertParamDefault(cppastBinaryExpression, method, elseMethod, branchMap);
                }
                if (cppastBinaryExpression.getOperand2() instanceof CPPASTUnaryExpression) {
                    insertParamDefault((CPPASTBinaryExpression) ((CPPASTUnaryExpression) cppastBinaryExpression.getOperand2()).getOperand(), method, elseMethod, branchMap);
                } else {
                    insertParamDefault(cppastBinaryExpression, method, elseMethod, branchMap);
                }
                insertToMap(elseMethod);
                insertToMap(method);
                if (cppastIfStatement.getThenClause() instanceof IASTCompoundStatement) {
                    IASTCompoundStatement iastStatementCmp = (IASTCompoundStatement) cppastIfStatement.getThenClause();
                    for (IASTStatement iastStatement : iastStatementCmp.getStatements()) {
                        if (iastStatement instanceof CPPASTIfStatement) {
                            insertConditions(iastStatement, method, elseMethod, branchMap);
                        }
                    }
                }
            }
            if (cppastIfStatement.getElseClause() != null) {
                elseMethod = elseMethod.clone();
                method = method.clone();
                if (cppastIfStatement.getThenClause() instanceof IASTCompoundStatement) {
                    IASTCompoundStatement iastStatementCmp = (IASTCompoundStatement) cppastIfStatement.getThenClause();
                    for (IASTStatement iastStatement : iastStatementCmp.getStatements()) {
                        if (iastStatement instanceof CPPASTIfStatement) {
                            insertConditions(iastStatement, method, elseMethod, branchMap);
                        }
                    }
                } else {
                    CPPASTBinaryExpression cppastBinaryExpression = (CPPASTBinaryExpression) ((CPPASTIfStatement) cppastIfStatement.getElseClause()).getConditionExpression();
                    if (cppastBinaryExpression.getOperand1() instanceof CPPASTUnaryExpression) {
                        insertParamDefault((CPPASTBinaryExpression) ((CPPASTUnaryExpression) cppastBinaryExpression.getOperand1()).getOperand(), method, elseMethod, branchMap);
                    } else {
                        insertParamDefault(cppastBinaryExpression, method, elseMethod, branchMap);
                    }
                    if (cppastBinaryExpression.getOperand2() instanceof CPPASTUnaryExpression) {
                        insertParamDefault((CPPASTBinaryExpression) ((CPPASTUnaryExpression) cppastBinaryExpression.getOperand2()).getOperand(), method, elseMethod, branchMap);
                    } else {
                        insertParamDefault(cppastBinaryExpression, method, elseMethod, branchMap);
                    }
                    insertToMap(elseMethod);
                    insertToMap(method);
                    if (((CPPASTIfStatement) cppastIfStatement.getElseClause()).getThenClause() instanceof IASTCompoundStatement) {
                        IASTCompoundStatement iastStatementCmp = (IASTCompoundStatement) ((CPPASTIfStatement) cppastIfStatement.getElseClause()).getThenClause();
                        for (IASTStatement iastStatement : iastStatementCmp.getStatements()) {
                            if (iastStatement instanceof CPPASTIfStatement) {
                                insertConditions(iastStatement, method, elseMethod, branchMap);
                            }
                        }
                    }
                }
            }
        }
    }

    private void insertToMap(Method method) {

        method.classReader = this;
        if(methods.get(method.methodName) != null) {
            methods.get(method.methodName).add(method);
        } else {
            List<Method> methodList = new ArrayList<>();
            methodList.add(method);
            methods.put(method.methodName, methodList);
        }
    }

    private void insertSwitchClauses(IASTNode iastNode, Method currentMethod) {
        List<Parameter> parameters = currentMethod.parameters;
        if(iastNode instanceof CPPASTSwitchStatement) {
            CPPASTSwitchStatement cppastSwitchStatement = (CPPASTSwitchStatement) iastNode;
            currentMethod.switchCondition = cppastSwitchStatement;
        }
    }

    private void insertIfClauses(IASTNode iastNode, Method currentMethod) {

        List<Parameter> parameters = currentMethod.parameters;
        if(iastNode instanceof CPPASTIfStatement) {
            CPPASTIfStatement cppastIfStatement = (CPPASTIfStatement) iastNode;
            if (cppastIfStatement.getThenClause() != null) {
                IASTNode[] childNodes = cppastIfStatement.getThenClause().getChildren();
                IASTExpression iastExpression = cppastIfStatement.getConditionExpression();
                CPPASTBinaryExpression cppastBinaryExpression = (CPPASTBinaryExpression) iastExpression;
                for (IASTNode childNode : childNodes) {
                    Method method = new Method();
                    method.lineNo = childNode.getFileLocation().getStartingLineNumber();
                    method.parameters = parameters;
                    method.methodName = currentMethod.methodName;
                    method.returnType = currentMethod.returnType;
                    method.ifCondition = cppastBinaryExpression;
                    method.conditionList = currentMethod.conditionList;
                    insertToMap(method);
                    insertIfClauses(childNode, currentMethod);
                }
            }
            if(cppastIfStatement.getElseClause() != null) {
                IASTNode[] childNodes = cppastIfStatement.getElseClause().getChildren();
                IASTExpression iastExpression = cppastIfStatement.getConditionExpression();
                CPPASTBinaryExpression cppastBinaryExpression = (CPPASTBinaryExpression) iastExpression;
                for (IASTNode childNode : childNodes) {
                    Method method = new Method();
                    method.lineNo = childNode.getFileLocation().getStartingLineNumber();
                    method.parameters = parameters;
                    method.methodName = currentMethod.methodName;
                    method.returnType = currentMethod.returnType;
                    method.ifCondition = cppastBinaryExpression;
                    insertToMap(method);
                    insertIfClauses(childNode, currentMethod);
                }
            }
        }
    }

    public void visit() {
        if(translationUnitCPP != null)
            doVisit(translationUnitCPP, this, fieldList);
        if(translationUnitH != null)
            doVisit(translationUnitH, this, fieldList);
    }
    public void doVisit(IASTTranslationUnit iastTranslationUnit, ClassReader classReader, List<Field> fieldList) {
        ASTVisitor visitor = new ASTVisitor() {
            public int visit(IASTName name) {
//                if ((name.getParent() instanceof CPPASTFunctionDeclarator)) {
//                    System.out.println("IASTName: " + name.getClass().getSimpleName() + "(" + name.getRawSignature() + ") - > parent: " + name.getParent().getClass().getSimpleName());
//                    System.out.println("-- isVisible: " + isVisible(name));
//                }
                return 3;
            }

            public int visit(IASTDeclaration declaration) {
                //System.out.println("declaration: " + declaration + " ->  " + declaration.getRawSignature());
                if (declaration.getRawSignature().contains("private"))
                {
                    isPrivate = true;
                }
                else if (declaration.getRawSignature().contains("public"))
                {
                    isPrivate = false;
                }
                if ((declaration instanceof IASTSimpleDeclaration)) {
                    IASTSimpleDeclaration ast = (IASTSimpleDeclaration) declaration;
                    try {
                        //System.out.println("--- type: " + ast.getSyntax() + " (childs: " + ast.getChildren().length + ")");
                        if (ast.getSyntax().getImage().equals("class")) {
                            definedclassName = ast.getSyntax().getNext().getImage();
                        }
                        if (!ast.getSyntax().getImage().equals("class") && !ast.getSyntax().getImage().equals("enum") && !ast.getSyntax().getImage().equals("struct")) {
                            IASTNode typedef = ast.getChildren().length == 1 ? ast.getChildren()[0] : ast.getChildren()[1];
                            //System.out.println("------- hello: " + ast.getSyntax());
                            String dataType = ast.getSyntax().getImage();
                            if(definedclassName!= null && definedclassName.equals(dataType)) {
                                List<Parameter> parameters = new ArrayList<>();
                                String paramList = "";
                                String callList = "";
                                for (ICPPASTParameterDeclaration parameterDeclaration : ((CPPASTFunctionDeclarator) ast.getChildren()[1]).getParameters()) {
                                    Parameter parameter = new Parameter();
                                    parameter.type = new DataType(parameterDeclaration.getChildren()[0].getRawSignature(), classReader);
                                    parameter.variableName = parameterDeclaration.getChildren()[1].getRawSignature();
                                    parameters.add(parameter);
                                    paramList += parameter.type + " " + parameter.variableName + ", ";
                                    callList += parameter.variableName + ", ";
                                }
                                if(!paramList.equals("")) {
                                    paramList = paramList.substring(0, paramList.lastIndexOf(", "));
                                    callList = callList.substring(0, callList.lastIndexOf(", "));
                                }

                                nativeClass += "\t\tpublic "+ definedclassName +"(" + paramList + "){\r\n";
                                nativeClass += "\t\t\tallocate(" + callList + ");\r\n\t\t}\r\n";
                                nativeClass += "\t\tprivate native void allocate(" + paramList + ");\r\n";

                                Constructor constructor = new Constructor(definedclassName, parameters,classReader);
                                constructors.add(constructor);
                            }
                            String name = ast.getSyntax().getNext().toString();
                            Field field = new Field(name, dataType, classReader);
                            fieldList.add(field);
                        }
                        IASTNode typedef = ast.getChildren().length == 1 ? ast.getChildren()[0] : ast.getChildren()[1];
                        if (ast.getSyntax().getImage().equals("enum") || ast.getSyntax().getImage().equals("struct")) {
                            isClass = false;
                        }
                        if (ast.getSyntax().getImage().equals("class")) {
                            isClass = true;
                            nativeClass += "@Properties(\r\n";
                            nativeClass += "\tvalue=@Platform(include={\""+ className+".h\""+"},\r\n";
                            nativeClass += "\t\t\t\tlinkpath = {\""+ System.getenv("TESTPATH") + "\"},\r\n";
                            nativeClass += "\t\t\t\tlink="+ "\""+ "Test" + "\""+ "),\r\n";
                            nativeClass += "\ttarget=\""+ ast.getSyntax().getNext().getImage() +"Clzz\"\r\n)\r\n";
                            nativeClass += "public class " + ast.getSyntax().getNext().getImage() +"Clzz {\r\n";
                            nativeClass += "\tpublic static " + typedef + " extends Pointer {\r\n";
                            nativeClass += "\t\tstatic {\r\n\t\t\tLoader.load();\r\n\t\t}\r\n";
                        } else if (!isPrivate && ast.getSyntax().getImage().contains("int") && isClass) {
                            nativeClass += "\t\tpublic native @ByVal " + declaration.getRawSignature() +"\r\n";
                        } else if (!isPrivate && ast.getSyntax().getImage().contains("string") && isClass) {
                            nativeClass += "\t\tpublic native @StdString String "+ declaration.getRawSignature() +"\r\n";
                        } else if (ast.getSyntax().toString().contains("void")) {
                            nativeClass += "\t\tpublic native "+ declaration.getRawSignature() +"\r\n";
                        }
                        //System.out.println("------- typedef: " + typedef);
                        IASTNode[] children = typedef.getChildren();
                       // if ((children != null) && (children.length > 0))
                            //System.out.println("------- typedef-name: " + children[0].getRawSignature());
                    } catch (ExpansionOverlapsBoundaryException e) {
                        e.printStackTrace();
                    }
//                    IASTDeclarator[] declarators = ast.getDeclarators();
//                    for (IASTDeclarator iastDeclarator : declarators) {
//                        System.out.println("iastDeclarator > " + iastDeclarator.getName());
//                    }
//                    IASTAttribute[] attributes = ast.getAttributes();
//                    for (IASTAttribute iastAttribute : attributes) {
//                        System.out.println("iastAttribute > " + iastAttribute);
//                    }
                }
                if ((declaration instanceof IASTFunctionDefinition)) {
                    IASTFunctionDefinition ast = (IASTFunctionDefinition) declaration;
                    //IScope scope = ast.getScope();

                    ICPPASTFunctionDeclarator typedef = (ICPPASTFunctionDeclarator) ast.getDeclarator();
                    //System.out.println("------- typedef: " + typedef.getName());

                    try {
                        String param = "";
                        for (ICPPASTParameterDeclaration var : typedef.getParameters()) {
                            param += var.getOriginalNode().getRawSignature();
                        }
                        if (!isPrivate && ast.getSyntax().toString().contains("int") && isClass) {
                            nativeClass += "\t\tpublic native @ByVal int " + typedef.getName() + "(" + param + ");\r\n";
                        } else if (!isPrivate && ast.getSyntax().toString().contains("string") && isClass) {
                            nativeClass += "\t\tpublic native @StdString String "+ typedef.getName() +"();\r\n";
                        }
                    } catch (ExpansionOverlapsBoundaryException e) {
                        e.printStackTrace();
                    }
                }
                return 3;
            }

            public int visit(IASTTypeId typeId) {
                //System.out.println("typeId: " + typeId.getRawSignature());
                return 3;
            }

            public int visit(IASTStatement statement) {
                //System.out.println("statement: " + statement.getRawSignature());
                return 3;
            }

            public int visit(IASTAttribute attribute) {
                return 3;
            }
        };
        visitor.shouldVisitNames = true;
        visitor.shouldVisitDeclarations = true;
        visitor.shouldVisitDeclarators = true;
        visitor.shouldVisitAttributes = true;
        visitor.shouldVisitStatements = false;
        visitor.shouldVisitTypeIds = true;
        iastTranslationUnit.accept(visitor);
    }

    public String getNativeClass() {
        String retVal = "import org.bytedeco.javacpp.Loader;\n"
                .concat("import org.bytedeco.javacpp.Pointer;\n")
                .concat("import org.bytedeco.javacpp.annotation.*;\n\n")
                .concat(nativeClass)
                .concat("\t}\r\n\r\n\t")
                .concat(Properties.mainMethod)
                .concat("\r\n}");
        return retVal;
    }

    public String getDefinedclassName() {return  definedclassName;}
    private String definedclassName;
    private String nativeClass = "";
    private boolean isPrivate, isClass;
}
