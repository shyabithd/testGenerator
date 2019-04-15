package generator.coverage.input;

import com.sun.org.apache.bcel.internal.classfile.ConstantValue;
import generator.testcase.CodeUnderTestException;
import generator.testcase.ExecutionObserver;
import generator.testcase.ExecutionResult;
import generator.testcase.Scope;
import generator.testcase.statement.EntityWithParametersStatement;
import generator.testcase.statement.Statement;
import generator.testcase.variable.ArrayIndex;
import generator.testcase.variable.FieldReference;
import generator.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Jose Miguel Rojas
 */
public class InputObserver extends ExecutionObserver {

    private Map<Integer, Set<InputCoverageGoal>> inputCoverage = new LinkedHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(InputObserver.class);

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
     */
    @Override
    public void output(int position, String output) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope)
     */
    @Override
    public void beforeStatement(Statement statement, Scope scope) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#afterStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
     */
    @Override
    public void afterStatement(Statement statement, Scope scope,
                               Throwable exception) {
        if (statement instanceof EntityWithParametersStatement) {
            EntityWithParametersStatement parameterisedStatement = (EntityWithParametersStatement)statement;
            List<VariableReference> parRefs = parameterisedStatement.getParameterReferences();

            List<Object> argObjects = new ArrayList<>(parRefs.size());
            for (VariableReference parRef : parRefs) {
                Object parObject = null;
                try {
                    if (parRef instanceof ArrayIndex || parRef instanceof FieldReference) {
                        parObject = parRef.getObject(scope);
                    } else {
                        parObject = parRef.getObject(scope);
                    }
                } catch (CodeUnderTestException e) {
                    e.printStackTrace();
                }
                argObjects.add(parObject);
            }
            assert parRefs.size() == argObjects.size();
            String className  = parameterisedStatement.getDeclaringClassName();
            String methodDesc = parameterisedStatement.getDescriptor();
            String methodName = parameterisedStatement.getMethodName();

            inputCoverage.put(statement.getPosition(), InputCoverageGoal.createCoveredGoalsFromParameters(className, methodName, methodDesc, argObjects));
            // argumentsValues.put((EntityWithParametersStatement) statement, argObjects);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#testExecutionFinished(org.evosuite.testcase.ExecutionResult)
     */
    @Override
    public void testExecutionFinished(ExecutionResult r, Scope s) {
        logger.info("Attaching argumentsValues map to ExecutionResult");
        r.setInputGoals(inputCoverage);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#clear()
     */
    @Override
    public void clear() {
        logger.info("Clearing InputObserver data");
        inputCoverage = new LinkedHashMap<>();
    }

}
