package runtime;

import java.util.ArrayList;
import java.util.List;

public class LoopCounter {

    private static final LoopCounter singleton = new LoopCounter();

    private boolean activated = true;
    
    /**
     * Number of iterations so far
     */
    private List<Long> counters;


    private LoopCounter(){
        counters = new ArrayList<>();
    }

    public static LoopCounter getInstance(){
        return singleton;
    }

    public void reset(){
        counters.clear();
    }
    
    public void setActive(boolean active) {
    	this.activated = active;
    }

    public boolean isActivated() {
        return activated;
    }

    /**
     * This is called during bytecode instrumentation to determine which index
     * to assign to a new parsed loop
     *
     * @return the next valid index for a new loop
     */
    public int getNewIndex(){
        int index = counters.size();
        counters.add(0l);
        return index;
    }


    /**
     * This is added directly in the instrumented CUT after each loop statement
     *
     * @param index
     * @throws TooManyResourcesException if this loop has executed too many iterations
     * @throws IllegalArgumentException
     */
    public void checkLoop(int index) throws TooManyResourcesException, IllegalArgumentException{
        if(index < 0){
            throw new IllegalArgumentException("Loop index cannot be negative");
        }
        
        if(!activated)
        	return;

        if(RuntimeSettings.maxNumberOfIterationsPerLoop < 0){
            return; //do nothing, no check
        }
        
        //first check initialization
        int size = counters.size();
        if(index >= size){
            for(int i=0; i < 1 + (index - size); i++){
                counters.add(0l);
            }
        }
        assert index < counters.size();

        //do increment
        try {
            long value = counters.get(index) + 1l;
            counters.set(index, value);

            if(value >= RuntimeSettings.maxNumberOfIterationsPerLoop && !isInStaticInit()) {
                this.reset();
                throw new TooManyResourcesException("Loop has been executed more times than the allowed " +
                        RuntimeSettings.maxNumberOfIterationsPerLoop);
            }
        } catch(NullPointerException e) {
        }
    }


    private boolean isInStaticInit() {
        for (StackTraceElement elem : new Throwable().getStackTrace()) {
            if (elem.getMethodName().startsWith("<clinit>"))
                return true;
        }
        return false;
    }
}
