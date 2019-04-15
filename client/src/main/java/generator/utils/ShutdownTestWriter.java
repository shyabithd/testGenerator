package generator.utils;
import generator.ga.stoppingconditions.StoppingConditionImpl;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * <p>
 * ShutdownTestWriter class.
 * </p>
 * 
 * @author Gordon Fraser
 */
@SuppressWarnings("restriction")
public class ShutdownTestWriter extends StoppingConditionImpl implements SignalHandler {

	private static final long serialVersionUID = -5703624299360241009L;

	private static boolean interrupted = false;

	/* (non-Javadoc)
	 * @see sun.misc.SignalHandler#handle(sun.misc.Signal)
	 */
	/** {@inheritDoc} */
	@Override
	public void handle(Signal arg0) {
		LoggingUtils.getGeneratorLogger().info("\n* User requested search stop");

		// If this is the second Ctrl+C the user _really_ wants to stop...
		if (interrupted)
			System.exit(0);
		interrupted = true;
	}

	/**
	 * <p>
	 * isInterrupted
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public static boolean isInterrupted() {
		return interrupted;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#isFinished()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFinished() {
		return interrupted;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#reset()
	 */
	/** {@inheritDoc} */
	@Override
	public void reset() {
		// interrupted = false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#setLimit(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getLimit()
	 */
	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		// TODO ?
	}

}
