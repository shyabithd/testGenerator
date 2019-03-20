package generator.testcase;

import java.util.Map;
import java.util.concurrent.Callable;

public interface InterfaceTestRunnable extends Callable<ExecutionResult>{
	//#TODO steenbuck add javadoc
	/**
	 * <p>getExceptionsThrown</p>
	 *
	 * @return a {@link Map} object.
	 */
	public Map<Integer, Throwable> getExceptionsThrown(); 
	
	/**
	 * <p>isRunFinished</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRunFinished();
}
