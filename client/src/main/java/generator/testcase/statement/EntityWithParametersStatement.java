package generator.testcase.statement;

import generator.ClassReader;
import generator.testcase.TestCase;
import generator.testcase.variable.ArrayIndex;
import generator.testcase.variable.VariableReference;
import generator.utils.Randomness;
import utils.Inputs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Andrea Arcuri on 04/07/15.
 */
public abstract class EntityWithParametersStatement extends AbstractStatement{

	private static final long serialVersionUID = 2971944785047056480L;
	protected final List<VariableReference> parameters;
    protected final Annotation[][] parameterAnnotations;
    protected final Annotation[] annotations;

    protected EntityWithParametersStatement(TestCase tc, ClassReader.DataType type, List<VariableReference> parameters,
                                            Annotation[] annotations, Annotation[][] parameterAnnotations) throws IllegalArgumentException{
        super(tc,type);
        this.parameters = parameters;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
        validateInputs();
    }

    protected EntityWithParametersStatement(TestCase tc, VariableReference retval, List<VariableReference> parameters,
                                            Annotation[] annotations, Annotation[][] parameterAnnotations) throws IllegalArgumentException{
        super(tc);
        this.parameters = parameters;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
        validateInputs();
    }

    /**
     * Constructor needed for Functional Mocks where the number of input parameters
     * might vary during the search, ie not constant, and starts with 0
     * @param tc
     * @param retval
     */
    protected EntityWithParametersStatement(TestCase tc, VariableReference retval){
        super(tc);
        this.parameters = new ArrayList<>();
        this.annotations=null;
        this.parameterAnnotations=null;
    }

    /**
     * Constructor needed for Functional Mocks where the number of input parameters
     * might vary during the search, ie not constant, and starts with 0
     * @param tc
     * @param type
     */
    protected EntityWithParametersStatement(TestCase tc, ClassReader.DataType type){
        super(tc, type);
        this.parameters = new ArrayList<>();
        this.annotations=null;
        this.parameterAnnotations=null;
    }

    private void validateInputs() throws IllegalArgumentException{
        Inputs.checkNull(parameters);
        for(VariableReference ref : parameters){
            Inputs.checkNull(ref);
        }
        if(parameterAnnotations!=null){
            if(parameterAnnotations.length != parameters.size()){
                throw new IllegalArgumentException("Size mismatched");
            }
        }
    }

    public List<VariableReference> getParameterReferences() {
        return Collections.unmodifiableList(parameters);
    }

    /* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
    /** {@inheritDoc} */
    public void replace(VariableReference var1, VariableReference var2) {

        if (retval.equals(var1)) {
            retval = var2;
            // TODO: Notify listener?
        }

        for (int i = 0; i < parameters.size(); i++) {

            if (parameters.get(i).equals(var1))
                parameters.set(i, var2);
            else
                parameters.get(i).replaceAdditionalVariableReference(var1, var2);
        }
    }

    public List<VariableReference> getUniqueVariableReferences() {
        List<VariableReference> references = new ArrayList<>();
        references.add(retval);
        references.addAll(parameters);
        for (VariableReference param : parameters) {
            if (param instanceof ArrayIndex)
                references.add(((ArrayIndex) param).getArray());
        }
        return references;

    }

    @Override
    public Set<VariableReference> getVariableReferences() {
        Set<VariableReference> references = new LinkedHashSet<>();
        references.add(retval);
        for (VariableReference param : parameters) {
            if(param == null){
                /*
                    This could happen while building a functional mock, and creation
                    of its input values lead to a forward check of properties
                 */
                continue;
            }
            references.add(param);
            if (param.getAdditionalVariableReference() != null)
                references.add(param.getAdditionalVariableReference());
        }
        return references;
    }


    @Override
    public int getNumParameters() {
        return parameters.size();
    }

    public void replaceParameterReference(VariableReference var, int numParameter) throws IllegalArgumentException{
        Inputs.checkNull(var);
        if(numParameter<0 || numParameter>= parameters.size()){
            throw new IllegalArgumentException("Out of range index "+numParameter+" from list of size "+parameters.size());
        }

        parameters.set(numParameter, var);
    }

    /**
     * Check if the given var is bounded in this method/constructor as input parameter
     * @param var
     * @return
     */
    public boolean isBounded(VariableReference var) throws IllegalArgumentException{
        Inputs.checkNull(var);

        if(parameterAnnotations==null){
            return false;
        }

        for(int i=0; i<parameters.size(); i++){
            if(parameters.get(i).equals(var)){

                break;
            }
        }

        return false;
    }

    protected int getNumParametersOfType(VariableReference variableReference) {
        int num = 0;
        for(VariableReference var : parameters) {
            if(var.getVariableClass().equals(variableReference))
                num++;
        }
        return num;
    }

    protected boolean mutateParameter(TestCase test, int numParameter) {

        // replace a parameter
        VariableReference parameter = parameters.get(numParameter);

        List<VariableReference> objects = test.getObjects(parameter.getType(),getPosition());
        objects.remove(parameter);
        objects.remove(getReturnValue());

        NullStatement nullStatement = new NullStatement(test, parameter.getType());
        Statement copy = null;



        // If there are fewer objects than parameters of that type,
        // we consider adding an instance
        if(getNumParametersOfType(parameter) + 1 < objects.size()) {
            Statement originalStatement = test.getStatement(parameter.getStPosition());
            copy = originalStatement.clone(test);
            if (originalStatement instanceof PrimitiveStatement<?>) {
                ((PrimitiveStatement<?>)copy).delta();
            }
            objects.add(copy.getReturnValue());
        }

        if (objects.isEmpty())
            return false;

        VariableReference replacement = Randomness.choice(objects);
        if (replacement == nullStatement.getReturnValue()) {
            test.addStatement(nullStatement, getPosition());
        } else if (copy != null && replacement == copy.getReturnValue()) {
            test.addStatement(copy, getPosition());
        }
        replaceParameterReference(replacement, numParameter);
        return true;
    }

    public abstract String getDeclaringClassName();

    public abstract String getMethodName();

    public abstract String getDescriptor();

}
