package generator.testcase;

import generator.setup.CallContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines the trace data that is collected during execution.
 * 
 * @author Gordon Fraser
 */
public interface ExecutionTrace {

	/**
	 * Add branch to currently active method call
	 * 
	 * @param branch
	 *            a int.
	 * @param true_distance
	 *            a double.
	 * @param false_distance
	 *            a double.
	 * @param bytecode_id
	 *            a int.
	 */
	public void branchPassed(int branch, int bytecode_id, double true_distance, double false_distance);

	/**
	 * Retrieve minimum branch distance to false branch
	 * 
	 * @param branchId
	 *            a int.
	 * @return a double.
	 */
	public double getFalseDistance(int branchId);

	/**
	 * Retrieve minimum branch distance to true branch
	 * 
	 * @param branchId
	 *            a int.
	 * @return a double.
	 */
	public double getTrueDistance(int branchId);

	/**
	 * Retrieve set of branches that evaluated to true
	 * 
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getCoveredTrueBranches();

	/**
	 * Retrieve set of branches that evaluated to false
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getCoveredFalseBranches();

	/**
	 * Retrieve set of branches that were executed
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getCoveredPredicates();

	/**
	 * Retrieve set of definitions that were executed
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getCoveredDefinitions();

	/**
	 * Retrieve execution counts for branches
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Integer> getPredicateExecutionCount();

	/**
	 * Retrieve execution counts for methods
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, Integer> getMethodExecutionCount();

	/**
	 * Retrieve execution counts for definitions
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Integer> getDefinitionExecutionCount();

	/**
	 * Determine if a branch has a true distance stored
	 *
	 * @param predicateId
	 *            a int.
	 * @return a boolean.
	 */
	public boolean hasTrueDistance(int predicateId);

	/**
	 * Determine if a branch has a false distance stored
	 *
	 * @param predicateId
	 *            a int.
	 * @return a boolean.
	 */
	public boolean hasFalseDistance(int predicateId);

	/**
	 * Retrieve map of all minimal true distances
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Double> getTrueDistances();

	/**
	 * Retrieve map of all minimal false distances
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Double> getFalseDistances();

	/**
	 * Retrieve map of all minimal true distances
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Map<CallContext, Double>> getTrueDistancesContext();

	/**
	 * Retrieve map of all minimal false distances
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Map<CallContext, Double>> getFalseDistancesContext();

	/**
	 * Retrieve map of all context method counts
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, Map<CallContext, Integer>> getMethodContextCount();

	/**
	 * Retrieve number of predicate executions
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Map<CallContext, Integer>> getPredicateContextExecutionCount();

	/**
	 * Retrieve the set of line numbers covered
	 *
	 * @param className
	 *            a {@link String} object.
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getCoveredLines(String className);


	public Set<Integer> getCoveredLines();

	/**
	 * Retrieve the set of all line numbers covered
	 *
	 * @return
	 */
	public Set<Integer> getAllCoveredLines();

	/**
	 * Retrieve detailed line coverage count
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, Map<String, Map<Integer, Integer>>> getCoverageData();

	/**
	 * Retrieve return value data
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, Map<String, Map<Integer, Integer>>> getReturnData();

	/**
	 * Retrieve data definitions
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getDefinitionData();

	/**
	 * Retrieve data definitions
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, HashMap<Integer, HashMap<Integer, Object>>> getDefinitionDataObjects();

	/**
	 * Retrieve data uses
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getUseData();

	/**
	 * Retrieve data uses
	 *
	 * @return a {@link Map} object.
	 */
	public Map<String, HashMap<Integer, HashMap<Integer, Object>>> getUseDataObjects();

	/**
	 * Retrieve the data definitions for a given variable
	 *
	 * @param variableName
	 *            a {@link String} object.
	 * @return a {@link Map} object.
	 */
	public Map<Integer, HashMap<Integer, Integer>> getPassedDefinitions(String variableName);

	/**
	 * Retrieve the data uses for a given variable
	 *
	 * @param variableName
	 *            a {@link String} object.
	 * @return a {@link Map} object.
	 */
	public Map<Integer, HashMap<Integer, Integer>> getPassedUses(String variableName);

	/**
	 * Retrieve the exception thrown in this trace
	 *
	 * @return a {@link Throwable} object.
	 */
	public Throwable getExplicitException();

	/**
	 * Retrieve all traced method calls
	 *
	 * @return a {@link List} object.
	 */
	public List<MethodCall> getMethodCalls();

	/**
	 * Retrieve the names of all called methods
	 *
	 * @return a {@link Set} object.
	 */
	public Set<String> getCoveredMethods();

	/**
	 * Retrieve the names of all covered branchless methods
	 *
	 * @return a {@link Set} object.
	 */
	public Set<String> getCoveredBranchlessMethods();

	/**
	 * Retrieve the minimum infection distance for a mutant
	 *
	 * @param mutationId
	 *            a int.
	 * @return a double.
	 */
	public double getMutationDistance(int mutationId);

	/**
	 * Retrieve all minimal infection distances
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Double> getMutationDistances();

	/**
	 * Determine is a mutant was executed
	 *
	 * @param mutationId
	 *            a int.
	 * @return a boolean.
	 */
	public boolean wasMutationTouched(int mutationId);

	/**
	 * Retrieve IDs of all executed mutants
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getTouchedMutants();

	/**
	 * Retrieve IDs of all executed mutants with an infection distance == 0.0
	 *
	 * @return a {@link Set} object.
	 */
	public Set<Integer> getInfectedMutants();

	/**
	 * Reset to 0
	 */
	public void clear();

	/**
	 * Adds Definition-Use-Coverage trace information for the given definition.
	 *
	 * Registers the given caller-Object Traces the occurrence of the given
	 * definition in the passedDefs-field Sets the given definition as the
	 * currently active one for the definitionVariable in the
	 * activeDefinitions-field Adds fake trace information to the currently
	 * active MethodCall in this.stack
	 *
	 * @param caller
	 *            a {@link Object} object.
	 * @param defID
	 *            a int.
	 */
	public void definitionPassed(Object object, Object caller, int defID);

	/**
	 * Add a new method call to stack
	 *
	 * @param className
	 *            a {@link String} object.
	 * @param methodName
	 *            a {@link String} object.
	 * @param caller
	 *            a {@link Object} object.
	 */
	public void enteredMethod(String className, String methodName, Object caller);

	/**
	 * Pop last method call from stack
	 *
	 * @param classname
	 *            a {@link String} object.
	 * @param methodname
	 *            a {@link String} object.
	 */
	public void exitMethod(String classname, String methodname);

	/**
	 * Finish all method calls. This is called when a method is not exited
	 * regularly, but through an exception
	 */
	public void finishCalls();

	public ExecutionTrace getTraceForObject(int objectId);

	/**
	 * Add line to currently active method call
	 *
	 * @param line
	 *            a int.
	 * @param className
	 *            a {@link String} object.
	 * @param methodName
	 *            a {@link String} object.
	 */
	public void linePassed(String className, String methodName, int line);

	/**
	 * Record a mutant execution
	 *
	 * @param mutationId
	 *            a int.
	 * @param distance
	 *            a double.
	 */
	public void mutationPassed(int mutationId, double distance);

	/**
	 * Record a return value
	 *
	 * @param className
	 *            a {@link String} object.
	 * @param methodName
	 *            a {@link String} object.
	 * @param value
	 *            a int.
	 */
	public void returnValue(String className, String methodName, int value);

	/**
	 * Returns a String containing the information in passedDefs and passedUses
	 *
	 * Used for Definition-Use-Coverage-debugging
	 *
	 * @return a {@link String} object.
	 */
	public String toDefUseTraceInformation();

	/**
	 * Returns a String containing the information in passedDefs and passedUses
	 * filtered for a specific variable
	 *
	 * Used for Definition-Use-Coverage-debugging
	 *
	 * @param targetVar
	 *            a {@link String} object.
	 * @return a {@link String} object.
	 */
	public String toDefUseTraceInformation(String targetVar);

	/**
	 * Returns a String containing the information in passedDefs and passedUses
	 * for the given variable
	 *
	 * Used for Definition-Use-Coverage-debugging
	 *
	 * @param var
	 *            a {@link String} object.
	 * @param objectId
	 *            a int.
	 * @return a {@link String} object.
	 */
	public String toDefUseTraceInformation(String var, int objectId);

	/**
	 * Adds Definition-Use-Coverage trace information for the given use.
	 *
	 * Registers the given caller-Object Traces the occurrence of the given use
	 * in the passedUses-field
	 *
	 * @param caller
	 *            a {@link Object} object.
	 * @param useID
	 *            a int.
	 */
	public void usePassed(Object object, Object caller, int useID);

	/**
	 * Set the exception thrown in this trace
	 *
	 * @param explicitException
	 *            a {@link Throwable} object.
	 */
	public void setExplicitException(Throwable explicitException);

	public ExecutionTrace lazyClone();

	/**
	 * <p>
	 * getBranchesTrace
	 * </p>
	 *
	 * @return a {@link List} object.
	 */
	List<ExecutionTraceImpl.BranchEval> getBranchesTrace();

	/**
	 * <p>
	 * getFalseDistancesSum
	 * </p>
	 *
	 * @return a {@link Map} object.
	 */
	Map<Integer, Double> getFalseDistancesSum();

	/**
	 * <p>
	 * getTrueDistancesSum
	 * </p>
	 *
	 * @return a {@link Map} object.
	 */
	Map<Integer, Double> getTrueDistancesSum();

	/**
	 * <p>
	 * getPassedUses
	 * </p>
	 *
	 * @return a {@link Map} object.
	 */
	Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getPassedUses();

	/**
	 * Retrieve the set of all uses by id
	 * 
	 * @return
	 */
	public Set<Integer> getPassedUseIDs();

	/**
	 * Retrieve the set of all definitions by id
	 * 
	 * @return
	 */
	@Deprecated
	public Set<Integer> getPassedDefIDs();

	/**
	 * Record a PUTSTATIC statement
	 * 
	 * @param classNameWithDots
	 * @param fieldName
	 */
	public void putStaticPassed(String classNameWithDots, String fieldName);

	/**
	 * Record a GETSTATIC statement
	 *
	 * @param classNameWithDots
	 * @param fieldName
	 */
	public void getStaticPassed(String classNameWithDots, String fieldName);

	/**
	 * Retrieve a list of those classes that were affected by a PUTSTATIC.
	 *
	 * @return
	 */
	public Set<String> getClassesWithStaticWrites();

	/**
	 * Retrieve a list of those classes that were affected by a GETSTATIC.
	 *
	 * @return
	 */
	public Set<String> getClassesWithStaticReads();

	/**
	 * Logs that a <clinit> was completed during this test execution
	 * 
	 * @param classNameWithDots
	 */
	public void classInitialized(String classNameWithDots);

	/**
	 * Returns the list (with no repetitions) following the order in which the
	 * <clinit> method was finished during this test execution
	 * 
	 * @return
	 */
	public List<String> getInitializedClasses();
}
