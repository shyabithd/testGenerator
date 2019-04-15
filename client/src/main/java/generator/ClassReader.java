package generator;

import generator.coverage.branch.Branch;
import generator.coverage.branch.BranchPool;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;
import org.eclipse.core.runtime.CoreException;

import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

public class ClassReader {

    public String getClassName() {
        return className;
    }

    public Field getDeclaredField(String fieldName) {
        return null;
    }

    public Field getField(String name) {
        return getDeclaredField(name);
    }

    public class Parameter {
        public String type;
        public String variableName;
    }

    public class Method {

        public int lineNo;
        public String methodName;
        public String returnType;
        public List<Parameter> parameters = new ArrayList<>();
        public CPPASTBinaryExpression ifCondition;
        public CPPASTSwitchStatement switchCondition;

        public int getModifiers() {
            return 0;
        }

        public DataType[] getGenericParameterTypes() {
            List<DataType> dataTypes = new ArrayList<>();
            dataTypes.add(new DataType());
            return dataTypes.toArray(new DataType[0]);
        }

        public String getName() {
            return "";
        }

        public DataType[] getParameterTypes() {
            return null;
        }

        public ClassReader getDeclaringClass() {
            return null;
        }

        public TypeVariable<?>[] getTypeParameters() {
            return null;
        }

        public DataType getGenericReturnType() {
            return null;
        }

        public DataType getReturnType() {
            return null;
        }

        public String toGenericString() {
            return "";
        }

        public Parameter getParameters() {
            return null;
        }
    }

    public class DataType
    {
        public boolean isPrimitive() {
            return true;
        }

        public ClassReader getClassReader() {
            return null;
        }
    }
    public class Field
    {
        public int getModifiers()
        {
            return 1;
        }

        public String getName() {
            return "";
        }

        public DataType getType() {
            return new DataType();
        }

        public DataType getGenericType() {
            return null;
        }

        public String toGenericString() {
            return "";
        }

        public Class<?> getDeclaringClass() {
            return null;
        }
    }
    private IASTTranslationUnit translationUnitCPP;
    private IASTTranslationUnit translationUnitH;
    private String className;
    private Map<String, List<Method>> methodDefinitions = new HashMap<>();
    private Map<String, String> returnMap = new HashMap<>();

    public String getCanonicalName() {
        return className.concat(".cpp");
    }

    public ClassReader()
    {
        returnMap.put("void", "V");
        returnMap.put("int", "I");
        returnMap.put("float", "F");
        returnMap.put("double", "D");
    }

    public Method[] getDeclaredMethods()
    {
        List<Method> methodList = new ArrayList<>();
        for(Map.Entry<String, List<Method> > entry : methodDefinitions.entrySet())
        {
            methodList.add(entry.getValue().get(0));
        }
        return methodList.toArray(new Method[0]);
    }

    public Field[] getDeclaredFields()
    {
        List<Field> fieldList = new ArrayList<>();
        fieldList.add(new Field());
        return fieldList.toArray(new Field[0]);
    }

    public void readFile(String absoluteFilePath) throws CoreException {
        if(absoluteFilePath.endsWith(".cpp"))
        {
            FileContent fileContent = FileContent.createForExternalFileLocation(absoluteFilePath);
            className = absoluteFilePath.substring(absoluteFilePath.lastIndexOf("\\")+2, absoluteFilePath.lastIndexOf("."));
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
        for(Map.Entry<String, List<Method> > entry : methodDefinitions.entrySet())
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
                    variableList = variableList.substring(0, variableList.lastIndexOf(","));
                    methondName = methondName + variableList + ")" + returnMap.get(method.returnType);
                    Branch b = new Branch(className, methondName, method.lineNo);
                    BranchPool.getInstance(this).addBranchToMap(b);
                }
            }
        }

    }



    private void parseTree(IASTNode node, int index) {
        IASTNode[] children = node.getChildren();
        boolean printContents = true;
        if ((node instanceof CPPASTTranslationUnit)) {
            printContents = false;
        }
        String offset = "";
        try {
            offset = node.getSyntax() != null ? " (offset: " + node.getFileLocation().getNodeOffset() + "," + node.getFileLocation().getNodeLength() + ")" : "";
            printContents = node.getFileLocation().getNodeLength() < 30;
        } catch (ExpansionOverlapsBoundaryException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            offset = "UnsupportedOperationException";
        }

        if (node.getClass().getSimpleName().equals("CPPASTFunctionDefinition")) {
            Method method = new Method();
            method.lineNo = node.getFileLocation().getStartingLineNumber();
            method.returnType = node.getChildren()[0].getRawSignature();
            if(node.getChildren()[1].getChildren()[0].getChildren().length==0)
                method.methodName = node.getChildren()[1].getChildren()[0].getRawSignature();
            else
                method.methodName = node.getChildren()[1].getChildren()[0].getChildren()[1].getRawSignature();
            for(IASTNode iastNode : node.getChildren()[1].getChildren()){
                Parameter parameter = new Parameter();
                if (iastNode.getClass().getSimpleName().equals("CPPASTParameterDeclaration")) {
                    parameter.type = iastNode.getChildren()[0].getRawSignature();
                    parameter.variableName = iastNode.getChildren()[1].getRawSignature();
                    method.parameters.add(parameter);
                }
            }
            for(IASTNode iastNode : node.getChildren()[2].getChildren()){
                insertIfClauses(iastNode, method);
                insertSwitchClauses(iastNode, method);
            }

            insertToMap(method);
        }
        //System.out.println(String.format(new StringBuilder("%1$").append(index * 2).append("s").toString(), new Object[]{"-"}) + node.getClass().getSimpleName() + offset + " -> " + (printContents ? node.getRawSignature().replaceAll("\n", " \\ ") : node.getRawSignature().subSequence(0, 5)));
        for (IASTNode iastNode : children)
            parseTree(iastNode, index + 1);
    }

    private void insertToMap(Method method) {

        if(methodDefinitions.get(method.methodName) != null) {
            methodDefinitions.get(method.methodName).add(method);
        } else {
            List<Method> methodList = new ArrayList<>();
            methodList.add(method);
            methodDefinitions.put(method.methodName, methodList);
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
            doVisit(translationUnitCPP);
        if(translationUnitH != null)
            doVisit(translationUnitH);
    }
    public void doVisit(IASTTranslationUnit iastTranslationUnit) {
        ASTVisitor visitor = new ASTVisitor() {
            public int visit(IASTName name) {
                if ((name.getParent() instanceof CPPASTFunctionDeclarator)) {
                    System.out.println("IASTName: " + name.getClass().getSimpleName() + "(" + name.getRawSignature() + ") - > parent: " + name.getParent().getClass().getSimpleName());
                    System.out.println("-- isVisible: " + isVisible(name));
                }
                return 3;
            }

            public int visit(IASTDeclaration declaration) {
                System.out.println("declaration: " + declaration + " ->  " + declaration.getRawSignature());
                if ((declaration instanceof IASTSimpleDeclaration)) {
                    IASTSimpleDeclaration ast = (IASTSimpleDeclaration) declaration;
                    try {
                        System.out.println("--- type: " + ast.getSyntax() + " (childs: " + ast.getChildren().length + ")");
                        IASTNode typedef = ast.getChildren().length == 1 ? ast.getChildren()[0] : ast.getChildren()[1];
                        System.out.println("------- typedef: " + typedef);
                        IASTNode[] children = typedef.getChildren();
                        if ((children != null) && (children.length > 0))
                            System.out.println("------- typedef-name: " + children[0].getRawSignature());
                    } catch (ExpansionOverlapsBoundaryException e) {
                        e.printStackTrace();
                    }
                    IASTDeclarator[] declarators = ast.getDeclarators();
                    for (IASTDeclarator iastDeclarator : declarators) {
                        System.out.println("iastDeclarator > " + iastDeclarator.getName());
                    }
                    IASTAttribute[] attributes = ast.getAttributes();
                    for (IASTAttribute iastAttribute : attributes) {
                        System.out.println("iastAttribute > " + iastAttribute);
                    }
                }
                if ((declaration instanceof IASTFunctionDefinition)) {
                    IASTFunctionDefinition ast = (IASTFunctionDefinition) declaration;
                    IScope scope = ast.getScope();
                    try {
                        System.out.println("### function() - Parent = " + scope.getParent().getScopeName());
                        System.out.println("### function() - Syntax = " + ast.getSyntax());
                    } catch (DOMException e) {
                        e.printStackTrace();
                    } catch (ExpansionOverlapsBoundaryException e) {
                        e.printStackTrace();
                    }
                    ICPPASTFunctionDeclarator typedef = (ICPPASTFunctionDeclarator) ast.getDeclarator();
                    System.out.println("------- typedef: " + typedef.getName());
                }
                return 3;
            }

            public int visit(IASTTypeId typeId) {
                System.out.println("typeId: " + typeId.getRawSignature());
                return 3;
            }

            public int visit(IASTStatement statement) {
                System.out.println("statement: " + statement.getRawSignature());
                return 3;
            }

            public int visit(IASTAttribute attribute) {
                return 3;
            }
        };
        visitor.shouldVisitNames = true;
        visitor.shouldVisitDeclarations = false;
        visitor.shouldVisitDeclarators = true;
        visitor.shouldVisitAttributes = true;
        visitor.shouldVisitStatements = false;
        visitor.shouldVisitTypeIds = true;
        iastTranslationUnit.accept(visitor);
    }
}
