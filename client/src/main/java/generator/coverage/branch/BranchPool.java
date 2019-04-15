package generator.coverage.branch;

import generator.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BranchPool {

	private static Logger logger = LoggerFactory.getLogger(BranchPool.class);

	// maps className -> method inside that class -> list of branches inside
	// that method
	private Map<String, Map<String, List<Branch>>> branchMap = new HashMap<String, Map<String, List<Branch>>>();

	// set of all known methods without a Branch
	private Map<String, Map<String, Integer>> branchlessMethods = new HashMap<String, Map<String, Integer>>();

	// maps the branchIDs assigned by this pool to their respective Branches
	private Map<Integer, Branch> branchIdMap = new HashMap<Integer, Branch>();

	// number of known Branches - used for actualBranchIds
	private int branchCounter = 0;

	private static Map<ClassReader, BranchPool> instanceMap = new HashMap<ClassReader, BranchPool>();

	public static BranchPool getInstance(ClassReader classReader) {
		if (!instanceMap.containsKey(classReader)) {
			instanceMap.put(classReader, new BranchPool());
		}

		return instanceMap.get(classReader);
	}
	// fill the pool

	/**
	 * Gets called by the CFGMethodAdapter whenever it detects a method without
	 * any branches.
	 * 
	 * @param methodName
	 *            Unique methodName - consisting of <className>.<methodName> -
	 *            of a method without Branches
	 * @param className
	 *            a {@link String} object.
	 */
	public void addBranchlessMethod(String className, String methodName,
	        int lineNumber) {
		if (!branchlessMethods.containsKey(className))
			branchlessMethods.put(className, new HashMap<String, Integer>());
		branchlessMethods.get(className).put(methodName, lineNumber);
	}

	public void addBranchToMap(Branch b) {

		logger.info("Adding to map the branch {}", b);

		String className = b.getClassName();
		String methodName = b.getMethodName();

		if (!branchMap.containsKey(className))
			branchMap.put(className, new HashMap<String, List<Branch>>());
		if (!branchMap.get(className).containsKey(methodName))
			branchMap.get(className).put(methodName, new ArrayList<Branch>());
		branchMap.get(className).get(methodName).add(b);
	}


	public int getBranchCountForMethod(String className, String methodName) {
		if (branchMap.get(className) == null)
			return 0;
		if (branchMap.get(className).get(methodName) == null)
			return 0;

		return branchMap.get(className).get(methodName).size();
	}

	public int getNonArtificialBranchCountForMethod(String className,
	        String methodName) {
		if (branchMap.get(className) == null)
			return 0;
		if (branchMap.get(className).get(methodName) == null)
			return 0;

		int num = 0;
		for (Branch b : branchMap.get(className).get(methodName)) {
			if (!b.isInstrumented())
				num++;
		}

		return num;
	}

	/**
	 * Returns the number of known Branches for a given class
	 *
	 * @return The number of currently known Branches inside the given class
	 * @param className
	 *            a {@link String} object.
	 */
	public int getBranchCountForClass(String className) {
		if (branchMap.get(className) == null)
			return 0;
		int total = 0;
		for (String method : branchMap.get(className).keySet()) {
			total += branchMap.get(className).get(method).size();
		}
		return total;
	}

	/**
	 * Returns the number of known Branches for a given class
	 *
	 * @return The number of currently known Branches inside the given class
	 * @param prefix
	 *            a {@link String} object.
	 */
	public int getBranchCountForPrefix(String prefix) {
		int num = 0;
		for (String className : branchMap.keySet()) {
			if (className.startsWith(prefix)) {
				logger.info("Found matching class for branch count: " + className + "/"
				        + prefix);
				for (String method : branchMap.get(className).keySet()) {
					num += branchMap.get(className).get(method).size();
				}
			}
		}
		return num;
	}

	/**
	 * Returns the number of known Branches for a given class
	 *
	 * @return The number of currently known Branches inside the given class
	 * @param prefix
	 *            a {@link String} object.
	 */
	public Set<Integer> getBranchIdsForPrefix(String prefix) {
		Set<Integer> ids = new HashSet<>();
		Set<Branch> sutBranches = new HashSet<>();
		for (String className : branchMap.keySet()) {
			if (className.startsWith(prefix)) {
				logger.info("Found matching class for branch ids: " + className + "/"
				        + prefix);
				for (String method : branchMap.get(className).keySet()) {
					sutBranches.addAll(branchMap.get(className).get(method));
				}
			}
		}

		for (Integer id : branchIdMap.keySet()) {
			if(sutBranches.contains(branchIdMap.get(id))){
				ids.add(id);
			}
		}

		return ids;
	}

	/**
	 * Returns the number of known Branches for a given class
	 *
	 * @return The number of currently known Branches inside the given class
	 * @param prefix
	 *            a {@link String} object.
	 */
	public int getBranchCountForMemberClasses(String prefix) {
		int num = 0;
		for (String className : branchMap.keySet()) {
			if (className.equals(prefix) || className.startsWith(prefix + "$")) {
				logger.info("Found matching class for branch count: " + className + "/"
				        + prefix);
				for (String method : branchMap.get(className).keySet()) {
					num += branchMap.get(className).get(method).size();
				}
			}
		}
		return num;
	}

	/**
	 * Returns the number of currently known Branches
	 *
	 * @return The number of currently known Branches
	 */
	public int getBranchCounter() {
		return branchCounter;
	}

	public int getNumArtificialBranches() {
		int num = 0;
		for (Branch b : branchIdMap.values()) {
			if (b.isInstrumented())
				num++;
		}

		return num;
	}

	/**
	 * Returns the Branch object associated with the given branchID
	 *
	 * @param branchId
	 *            The ID of a branch
	 * @return The branch, or null if it does not exist
	 */
	public Branch getBranch(int branchId) {

		return branchIdMap.get(branchId);
	}

	public Collection<Branch> getAllBranches() {
		return branchIdMap.values();
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 *
	 * @return A set with all unique methodNames of methods without Branches.
	 * @param className
	 *            a {@link String} object.
	 */
	public Set<String> getBranchlessMethods(String className) {
		if (!branchlessMethods.containsKey(className))
			return new HashSet<String>();

		return branchlessMethods.get(className).keySet();
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 *
	 * @return A set with all unique methodNames of methods without Branches.
	 * @param className
	 *            a {@link String} object.
	 */
	public Set<String> getBranchlessMethodsPrefix(String className) {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$")) {
				methods.addAll(branchlessMethods.get(name).keySet());
			}
		}

		return methods;
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 *
	 * @return A set with all unique methodNames of methods without Branches.
	 * @param className
	 *            a {@link String} object.
	 */
	public Set<String> getBranchlessMethodsMemberClasses(String className) {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$")) {
				methods.addAll(branchlessMethods.get(name).keySet());
			}
		}

		return methods;
	}

	public int getBranchlessMethodLineNumber(String className, String methodName) {
		// check if the given method is branchless
		if (branchlessMethods.get(className) != null
		        && branchlessMethods.get(className).get(className + "." + methodName) != null) {
			return branchlessMethods.get(className).get(className + "." + methodName);
		}
		// otherwise consult the branchMap and return the lineNumber of the earliest Branch

		return branchlessMethods.get(className).get(className + "." + methodName);
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 *
	 * @return A set with all unique methodNames of methods without Branches.
	 */
	public Set<String> getBranchlessMethods() {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			methods.addAll(branchlessMethods.get(name).keySet());
		}

		return methods;
	}

	public boolean isBranchlessMethod(String className, String methodName) {
		Map<String, Integer> methodMap = branchlessMethods.get(className);
		if(methodMap != null) {
			return methodMap.containsKey(methodName);
		}
		return false;
	}

	/**
	 * Returns the number of methods without Branches for class className
	 *
	 * @return The number of methods without Branches.
	 * @param className
	 *            a {@link String} object.
	 */
	public int getNumBranchlessMethods(String className) {
		if (!branchlessMethods.containsKey(className))
			return 0;
		return branchlessMethods.get(className).size();
	}

	/**
	 * Returns the number of methods without Branches for class className
	 *
	 * @return The number of methods without Branches.
	 * @param className
	 *            a {@link String} object.
	 */
	public int getNumBranchlessMethodsPrefix(String className) {
		int num = 0;
		for (String name : branchlessMethods.keySet()) {
			if (name.startsWith(className))
				num += branchlessMethods.get(name).size();
		}
		return num;
	}

	/**
	 * Returns the number of methods without Branches for class className
	 *
	 * @return The number of methods without Branches.
	 * @param className
	 *            a {@link String} object.
	 */
	public int getNumBranchlessMethodsMemberClasses(String className) {
		int num = 0;
		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$"))
				num += branchlessMethods.get(name).size();
		}
		return num;
	}

	/**
	 * Returns the total number of methods without branches in the instrumented
	 * classes
	 *
	 * @return
	 */
	public int getNumBranchlessMethods() {
		int num = 0;
		for (String name : branchlessMethods.keySet()) {
			num += branchlessMethods.get(name).size();
		}
		return num;
	}

	/**
	 * Returns a Set containing all classes for which this pool knows Branches
	 * for as Strings
	 *
	 * @return a {@link Set} object.
	 */
	public Set<String> knownClasses() {
		Set<String> r = new HashSet<String>();
		r.addAll(branchMap.keySet());
		r.addAll(branchlessMethods.keySet());

		if (logger.isDebugEnabled()) {
			logger.debug("Known classes: " + r);
		}

		return r;
	}

	/**
	 * Returns a Set containing all methods in the class represented by the
	 * given String for which this pool knows Branches for as Strings
	 *
	 * @param className
	 *            a {@link String} object.
	 * @return a {@link Set} object.
	 */
	public Set<String> knownMethods(String className) {
		Set<String> r = new HashSet<String>();
		Map<String, List<Branch>> methods = branchMap.get(className);
		if (methods != null)
			r.addAll(methods.keySet());

		return r;
	}

	/**
	 * Returns a List containing all Branches in the given class and method
	 *
	 * Should no such Branch exist an empty List is returned
	 *
	 * @param className
	 *            a {@link String} object.
	 * @param methodName
	 *            a {@link String} object.
	 * @return a {@link List} object.
	 */
	public List<Branch> retrieveBranchesInMethod(String className,
	        String methodName) {
		List<Branch> r = new ArrayList<Branch>();
		if (branchMap.get(className) == null)
			return r;
		List<Branch> branches = branchMap.get(className).get(methodName);
		if (branches != null)
			r.addAll(branches);
		return r;
	}


	/**
	 * Reset all the data structures used to keep track of the branch
	 * information
	 */
	public void reset() {
		branchCounter = 0;
		branchMap.clear();
		branchlessMethods.clear();
		branchIdMap.clear();
	}


	public void clear() {
		branchCounter = 0;
		branchMap.clear();
		branchIdMap.clear();
		branchlessMethods.clear();
	}

	public void clear(String className) {
		branchMap.remove(className);
		branchlessMethods.remove(className);
	}

	public void clear(String className, String methodName) {
		int numBranches = 0;

		if (branchMap.containsKey(className)) {
			if (branchMap.get(className).containsKey(methodName))
				numBranches = branchMap.get(className).get(methodName).size();
			branchMap.get(className).remove(methodName);
		}
		if (branchlessMethods.containsKey(className))
			branchlessMethods.get(className).remove(methodName);
		logger.info("Resetting branchCounter from " + branchCounter + " to "
		        + (branchCounter - numBranches));
		branchCounter -= numBranches;
	}

}
