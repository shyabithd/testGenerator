package runtime.thread;

import runtime.RuntimeSettings;
import runtime.TooManyResourcesException;

public class ThreadCounter {

    private static final ThreadCounter singleton = new ThreadCounter();

    private volatile int counter;

    private ThreadCounter(){
        resetSingleton();
    }

    public static ThreadCounter getInstance(){
        return singleton;
    }

    public synchronized  void resetSingleton(){
        counter = 0;
    }

    public synchronized void checkIfCanStartNewThread() throws TooManyResourcesException {
        if(counter >= RuntimeSettings.maxNumberOfThreads){
            throw new TooManyResourcesException("This test case has tried to start too many threads. "+
                "Maximum allowed per test is "+RuntimeSettings.maxNumberOfThreads+" threads.");
        }
        counter++;
    }
}
