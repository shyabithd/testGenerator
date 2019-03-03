package generator.result;

import generator.Properties;
import generator.assertion.Assertion;
import generator.ga.FitnessFunction;
import generator.instrumentations.LinePool;
import generator.mutation.Mutation;
import generator.result.TestGenerationResult.Status;
import generator.testcase.ExecutionResult;
import generator.testcase.TestCase;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TestGenerationResultBuilder {

	public static TestGenerationResult buildErrorResult(String errorMessage) {
		TestGenerationResultImpl result = new TestGenerationResultImpl();
		result.setStatus(TestGenerationResult.Status.ERROR);
		result.setErrorMessage(errorMessage);
		getInstance().fillInformationFromConfiguration(result);
		getInstance().fillInformationFromTestData(result);
		getInstance().resetTestData();
		return result;
	}

	public static TestGenerationResult buildTimeoutResult() {
		TestGenerationResultImpl result = new TestGenerationResultImpl();
		result.setStatus(TestGenerationResult.Status.TIMEOUT);
		getInstance().fillInformationFromConfiguration(result);
		getInstance().fillInformationFromTestData(result);
		getInstance().resetTestData();
		return result;
	}

	public static TestGenerationResult buildSuccessResult() {
		TestGenerationResultImpl result = new TestGenerationResultImpl();
		result.setStatus(Status.SUCCESS);
		getInstance().fillInformationFromConfiguration(result);
		getInstance().fillInformationFromTestData(result);
		getInstance().resetTestData();
		return result;
	}
	
	private static TestGenerationResultBuilder instance = null;
	
	private TestGenerationResultBuilder() {
		resetTestData();
	}
	
	public static TestGenerationResultBuilder getInstance() {
		if(instance == null)
			instance = new TestGenerationResultBuilder();
		
		return instance;
	}
	
	private void resetTestData() {
		code = "";
		testCode.clear();
		testCases.clear();
		contractViolations.clear();
	}
	
	private void fillInformationFromConfiguration(TestGenerationResultImpl result) {
		result.setClassUnderTest(Properties.TARGET_CLASS);
		String[] criteria = new String[Properties.CRITERION.length];
		for (int i = 0; i < Properties.CRITERION.length; i++)
		    criteria[i] = Properties.CRITERION[i].name();
		result.setTargetCriterion(criteria);
	}
	
	private void fillInformationFromTestData(TestGenerationResultImpl result) {
		
		Set<MutationInfo> exceptionMutants = new LinkedHashSet<MutationInfo>();

		for(String test : testCode.keySet()) {
			result.setTestCode(test, testCode.get(test));
			result.setContractViolations(test, contractViolations.get(test));
			result.setCoveredLines(test, testLineCoverage.get(test));
			result.setCoveredBranches(test, testBranchCoverage.get(test));
			result.setCoveredMutants(test, testMutantCoverage.get(test));
			result.setComment(test, testComments.get(test));
		}
		
		result.setUncoveredLines(uncoveredLines);
		result.setUncoveredBranches(uncoveredBranches);
		result.setUncoveredMutants(uncoveredMutants);
		result.setExceptionMutants(exceptionMutants);
		result.setTestSuiteCode(code);
        for (Map.Entry<FitnessFunction<?>, Double> e : targetCoverages.entrySet()) {
            result.setTargetCoverage(e.getKey(), e.getValue());
        }

	}
	
	private String code = "";

	private Map<String, String> testCode = new LinkedHashMap<String, String>();

	private Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();
	
	private Map<String, String> testComments = new LinkedHashMap<String, String>();

	private Map<String, Set<Integer>> testLineCoverage = new LinkedHashMap<String, Set<Integer>>();

	private Map<String, Set<BranchInfo>> testBranchCoverage = new LinkedHashMap<String, Set<BranchInfo>>();

	private Map<String, Set<MutationInfo>> testMutantCoverage = new LinkedHashMap<String, Set<MutationInfo>>();

	private Map<String, Set<Failure>> contractViolations = new LinkedHashMap<String, Set<Failure>>();
	
	private Set<Integer> uncoveredLines = LinePool.getAllLines();
	
	private Set<BranchInfo> uncoveredBranches = new LinkedHashSet<BranchInfo>();

	private Set<MutationInfo> uncoveredMutants = new LinkedHashSet<MutationInfo>();

    private LinkedHashMap<FitnessFunction<?>, Double> targetCoverages = new LinkedHashMap<FitnessFunction<?>, Double>();
	
	public void setTestCase(String name, String code, TestCase testCase, String comment, ExecutionResult result) {
		testCode.put(name, code);
		testCases.put(name, testCase);
		Set<Failure> failures = new LinkedHashSet<Failure>();

		if(!Properties.CHECK_CONTRACTS && result.hasUndeclaredException()) {
			int position = result.getFirstPositionOfThrownException();
			Throwable exception = result.getExceptionThrownAtPosition(position);			
			failures.add(new Failure());
		}
		contractViolations.put(name, failures);
		testComments.put(name, comment);
		
		Set<BranchInfo> branchCoverage = new LinkedHashSet<BranchInfo>();

		testBranchCoverage.put(name, branchCoverage);
		uncoveredBranches.removeAll(branchCoverage);
		
		Set<MutationInfo> mutationCoverage = new LinkedHashSet<MutationInfo>();
		for(Assertion assertion : testCase.getAssertions()) {
			for(Mutation m : assertion.getKilledMutations()) {
				mutationCoverage.add(new MutationInfo(m));
			}
		}
		testMutantCoverage.put(name, mutationCoverage);
		uncoveredMutants.removeAll(mutationCoverage);
	}
	
	public void setTestSuiteCode(String code) {
		this.code = code;
	}

}
