package runtime;

public class Runtime {

	private static final Runtime singleton = new Runtime();


	protected Runtime(){		
	}

	public synchronized static Runtime getInstance(){
		return singleton;
	}

	public synchronized static void resetSingleton(){
		singleton.resetRuntime();
	}

	/**
	 * Resets all simulated classes to an initial default state (so that it
	 * seems they have never been used by previous test case executions)
	 * 
	 */
	public void resetRuntime() {
        LoopCounter.getInstance().reset();
	}

}
