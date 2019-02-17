/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package generator.ga;

import java.util.List;

/**
 * Represents a local search objective that will be used during local search to
 * assess the success (or failure) of a local search to a given chromosome 
 * (it could be TestSuiteChromosome or TestChromosome).
 * 
 * The local search objective contains a list of fitness functions that are used
 * to compute the fitness of an individual.
 *
 * @author Gordon Fraser
 */
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
