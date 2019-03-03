package generator.ga;

import java.util.List;

public interface LocalSearchObjective<T extends Chromosome> {

	/**
	 * true if the objective was achieved
	 * 
	 * @return
	 */
	public boolean isDone();

	/**
	 * Returns true if all the fitness functions are maximising functions
	 * (Observe that it is not possible to store simoustaneously maximising and
	 * minimising fitness functions)
	 * 
	 * @return
	 */
	public boolean isMaximizationObjective();

	public boolean hasImproved(T chromosome);

	public boolean hasNotWorsened(T chromosome);

	public int hasChanged(T chromosome);

	public void addFitnessFunction(FitnessFunction<? extends Chromosome> fitness);

	public List<FitnessFunction<? extends Chromosome>> getFitnessFunctions();

}
