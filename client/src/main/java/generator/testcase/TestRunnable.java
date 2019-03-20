package generator.testcase;

import generator.testcase.statement.Statement;
import generator.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.thread.KillSwitch;
import runtime.thread.ThreadStopper;

import generator.Properties;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * TestRunnable class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestRunnable implements InterfaceTestRunnable {

	private static final Logger logger = LoggerFactory.getLogger(TestRunnable.class);

	private static ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

	private final TestCase test;

	private final Scope scope;

	protected boolean runFinished;

	/**
	 * Map a thrown exception ('value') with the the position ('key') in the
	 * test sequence in which it was thrown from.
	 */
	protected Map<Integer, Throwable> exceptionsThrown = new HashMap<>();

	protected Set<ExecutionObserver> observers;

	protected final ThreadStopper threadStopper;

	public TestRunnable(TestCase tc, Scope scope, Set<ExecutionObserver> observers) {
		test = tc;
		this.scope = scope;
		this.observers = observers;
		runFinished = false;

		KillSwitch killSwitch = new KillSwitch() {
			@Override
			public void setKillSwitch(boolean kill) {
				ExecutionTracer.setKillSwitch(kill);
			}
		};
		Set<String> threadsToIgnore = new LinkedHashSet<>();
		threadsToIgnore.add(TestCaseExecutor.TEST_EXECUTION_THREAD);
		threadsToIgnore.addAll(Arrays.asList(Properties.IGNORE_THREADS));

		threadStopper = new ThreadStopper(killSwitch, threadsToIgnore, Properties.TIMEOUT);
	}

	/**
	 * <p>
	 * After the test case is executed, if any SUT thread is still running, we
	 * will wait for their termination. To identify which thread belong to SUT,
	 * before test case execution we should check which are the threads that are
	 * running.
	 * </p>
	 * <p>
	 * WARNING: The sandbox might prevent accessing thread informations, so best
	 * to call this method from outside this class
	 * </p>
	 */
	public void storeCurrentThreads() {
		threadStopper.storeCurrentThreads();
	}

	/**
	 * Try to kill (and then join) the SUT threads. Killing the SUT threads is
	 * important, because some spawn threads could just wait on objects/locks,
	 * and so make the test case executions always last TIMEOUT ms.
	 */
	public void killAndJoinClientThreads() throws IllegalStateException {
		threadStopper.killAndJoinClientThreads();
	}

	/**
	 * Inform all observers that we are going to execute the input statement
	 *
	 * @param s
	 *            the statement to execute
	 */
	protected void informObservers_before(Statement s) {
		ExecutionTracer.disable();
		try {
			for (ExecutionObserver observer : observers) {
				observer.beforeStatement(s, scope);
			}
		} finally {
			ExecutionTracer.enable();
		}
	}

	/**
	 * Inform all observers that input statement has been executed
	 *
	 * @param s
	 *            the executed statement
	 * @param exceptionThrown
	 *            the exception thrown when executing the statement, if any (can
	 *            be null)
	 */
	protected void informObservers_after(Statement s, Throwable exceptionThrown) {
		ExecutionTracer.disable();
		try {
			for (ExecutionObserver observer : observers) {
				observer.afterStatement(s, scope, exceptionThrown);
			}
		} finally {
			ExecutionTracer.enable();
		}
	}

	protected void informObservers_finished(ExecutionResult result) {
		ExecutionTracer.disable();
		try {
			for (ExecutionObserver observer : observers) {
				observer.testExecutionFinished(result, scope);
			}
		} finally {
			ExecutionTracer.enable();
		}
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult call() {

		exceptionsThrown.clear();

		runFinished = false;
		ExecutionResult result = new ExecutionResult(test, null);
		// TODO: Moved this to TestCaseExecutor so it is not part of the test execution timeout
		//		Runtime.getInstance().resetRuntime();
		ExecutionTracer.enable();

		PrintStream out = (Properties.PRINT_TO_SYSTEM ? System.out : new PrintStream(byteStream));
		byteStream.reset();

		if (!Properties.PRINT_TO_SYSTEM) {
			LoggingUtils.muteCurrentOutAndErrStream();
		}

		threadStopper.startRecordingTime();

		/*
		 *  need AtomicInteger as we want to get latest updated value even if exception is thrown in the 'try' block.
		 *  we practically use it as wrapper for int, which we can then pass by reference
		 */
		AtomicInteger num = new AtomicInteger(0);

		try {
			if(Properties.REPLACE_CALLS){
				//ShutdownHookHandler.getInstance().initHandler();
			}

			executeStatements(result, out, num);
		} catch (ThreadDeath e) {// can't stop these guys
			logger.info("Found error in " + test.toCode(), e);
			throw e; // this needs to be propagated
		} catch (TimeoutException | TestCaseExecutor.TimeoutExceeded e) {
			logger.info("Test timed out!");
		} catch (Throwable e) {

			logger.info("Exception at statement " + num + "! " + e);
			for (StackTraceElement elem : e.getStackTrace()) {
				logger.info(elem.toString());
			}
			if (e instanceof InvocationTargetException) {
				logger.info("Cause: " + e.getCause().toString(), e);
				e = e.getCause();
			}
			if (e instanceof AssertionError) {
				logger.error("Assertion Error in evosuitecode, for statement \n"
				        + test.getStatement(num.get()).getCode() + " \n which is number: "
				        + num + " testcase \n" + test.toCode(), e);
				throw (AssertionError) e;
			}

			logger.error("Suppressed/ignored exception during test case execution on class "
			                     + Properties.TARGET_CLASS + ": " + e.getMessage(), e);
		} finally {
			if (!Properties.PRINT_TO_SYSTEM) {
				LoggingUtils.restorePreviousOutAndErrStream();
			}
			if(Properties.REPLACE_CALLS){
				/*
				 * For simplicity, we call it here. Ideally, we could call it among the
				 * statements, with "non-safe" version, to check if any exception is thrown.
				 * But that would be quite a bit of work, which maybe is not really warranted 
				 */
				//ShutdownHookHandler.getInstance().safeExecuteAddedHooks();
			}
			
			runFinished = true;
		}

		//result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
		result.setExecutionTime(System.currentTimeMillis() - threadStopper.getStartTime());
		result.setExecutedStatements(num.get());
		result.setThrownExceptions(exceptionsThrown);
		//result.setReadProperties(org.evosuite.runtime.System.getAllPropertiesReadSoFar());
		//result.setWasAnyPropertyWritten(org.evosuite.runtime.System.wasAnyPropertyWritten());
		
		return result;
	}

	private void executeStatements(ExecutionResult result, PrintStream out,
			AtomicInteger num) throws TimeoutException,
			InvocationTargetException, IllegalAccessException,
			InstantiationException {
		
		for (Statement s : test) {

			if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
				logger.info("Thread interrupted at statement " + num + ": " + s.getCode());
				throw new TimeoutException();
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Executing statement " + s.getCode());
			}

			ExecutionTracer.statementExecuted();
			informObservers_before(s);

			/*
			 * Here actually execute a statement of the SUT
			 */
			Throwable exceptionThrown = s.execute(scope, out);

			if (exceptionThrown != null) {
				// if internal error, then throw exception
				// -------------------------------------------------------

				// -------------------------------------------------------

				/*
				 * This is implemented in this way due to ExecutionResult.hasTimeout()
				 */
				if (exceptionThrown instanceof TestCaseExecutor.TimeoutExceeded) {
					logger.debug("Test timed out!");
					exceptionsThrown.put(test.size(), exceptionThrown);
					result.setThrownExceptions(exceptionsThrown);
					result.reportNewThrownException(test.size(), exceptionThrown);
					//result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
					break;
				}

				// keep track if the exception and where it was thrown
				exceptionsThrown.put(num.get(), exceptionThrown);

				// check if it was an explicit exception
				// --------------------------------------------------------
				if (ExecutionTracer.getExecutionTracer().getLastException() == exceptionThrown) {
					result.explicitExceptions.put(num.get(), true);
				} else {
					result.explicitExceptions.put(num.get(), false);
				}
				// --------------------------------------------------------

				printDebugInfo(s, exceptionThrown);
				// --------------------------------------------------------

				/*
				 * If an exception is thrown, we stop the execution of the test case, because the internal state could be corrupted, and not
				 * possible to verify the behavior of any following function call. Predicate should be true by default
				 */
				if (Properties.BREAK_ON_EXCEPTION) {
					informObservers_after(s, exceptionThrown);
					break;
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Done statement " + s.getCode());
			}

			informObservers_after(s, exceptionThrown);

			num.incrementAndGet();
		} // end of loop
		informObservers_finished(result);
		//TODO
	}

	private void printDebugInfo(Statement s, Throwable exceptionThrown) {
		// some debugging info
		// --------------------------------------------------------

		if (logger.isDebugEnabled()) {
			logger.debug("Exception thrown in statement: " + s.getCode()
			        + " - " + exceptionThrown.getClass().getName() + " - "
			        + exceptionThrown.getMessage());
			for (StackTraceElement elem : exceptionThrown.getStackTrace()) {
				logger.debug(elem.toString());
			}
			if (exceptionThrown.getCause() != null) {
				logger.debug("Cause: "
				        + exceptionThrown.getCause().getClass().getName()
				        + " - " + exceptionThrown.getCause().getMessage());
				for (StackTraceElement elem : exceptionThrown.getCause().getStackTrace()) {
					logger.debug(elem.toString());
				}
			} else {
				logger.debug("Cause is null");
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Map<Integer, Throwable> getExceptionsThrown() {
		HashMap<Integer, Throwable> copy = new HashMap<Integer, Throwable>();
		copy.putAll(exceptionsThrown);
		return copy;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isRunFinished() {
		return runFinished;
	}

}
