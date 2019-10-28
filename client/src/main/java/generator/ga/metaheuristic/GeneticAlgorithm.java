package generator.ga.metaheuristic;

import generator.ga.*;
import generator.ga.archive.Archive;
import generator.ga.bloatcontrol.BloatControlFunction;
import generator.ga.crossover.CrossOverFunction;
import generator.ga.crossover.SinglePointCrossOver;
import generator.ga.populationlimit.PopulationLimit;
import generator.ga.selection.RankSelection;
import generator.ga.selection.SelectionFunction;
import generator.ga.stoppingconditions.MaxGenerationStoppingCondition;
import generator.ga.stoppingconditions.StoppingCondition;
import generator.testcase.TestCase;
import generator.testsuite.TestSuiteChromosome;
import generator.utils.LoggingUtils;
import generator.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generator.Properties;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Abstract superclass of genetic algorithms
 * 
 * @author Gordon Fraser
 */
public abstract class GeneticAlgorithm<T extends Chromosome> implements SearchAlgorithm,
		Serializable {

	private static final long serialVersionUID = 5155609385855093435L;

	private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithm.class);

	protected CrossOverFunction crossoverFunction = new SinglePointCrossOver();
	/** Fitness function to rank individuals */
	protected List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

	/** Selection function to select parents */
	protected SelectionFunction<T> selectionFunction = new RankSelection<T>();

	/** Current population */
	protected List<T> population = new ArrayList<T>();

	/** Generator for initial population */
	protected ChromosomeFactory<T> chromosomeFactory;

	/** Listeners */
	protected transient Set<SearchListener> listeners = new HashSet<SearchListener>();

	/** List of conditions on which to end the search */
	protected transient Set<StoppingCondition> stoppingConditions = new HashSet<StoppingCondition>();

	/** Bloat control, to avoid too long chromosomes */
	protected Set<BloatControlFunction> bloatControl = new HashSet<BloatControlFunction>();

	/** Local search might need a different local objective */
	protected LocalSearchObjective<T> localObjective = new DefaultLocalSearchObjective<>();

	/** The population limit decides when an iteration is done */
	protected PopulationLimit populationLimit = null;//new IndividualPopulationLimit();

	/** Age of the population */
	protected int currentIteration = 0;

	protected double localSearchProbability = Properties.LOCAL_SEARCH_PROBABILITY;

	public GeneticAlgorithm(ChromosomeFactory<T> factory) {
		chromosomeFactory = factory;
		addStoppingCondition(new MaxGenerationStoppingCondition());
		if (Properties.LOCAL_SEARCH_RATE > 0) {}
			//addListener(LocalSearchBudget.getInstance());
	}

	/**
	 * Generate one new generation
	 */
	protected abstract void evolve();

	/**
	 * Local search is only applied every X generations
	 * 
	 * @return a boolean.
	 */
	protected boolean shouldApplyLocalSearch() {
		// If local search is not set to a rate, then we don't use it at all
		if (Properties.LOCAL_SEARCH_RATE <= 0)
			return false;

		if (getAge() % Properties.LOCAL_SEARCH_RATE == 0) {
			if (Randomness.nextDouble() <= localSearchProbability) {
				return true;
			}
		}
		return false;
	}

	protected void disableFirstSecondaryCriterion() {
		if (TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
			TestSuiteChromosome.disableFirstSecondaryObjective();
			logger.info("second secondary criterion enabled");
		}
	}

	protected void enableFirstSecondaryCriterion() {
		if (TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
			TestSuiteChromosome.enableFirstSecondaryObjective();
			logger.info("first secondary criterion enabled");
		}
	}

	/**
	 * enable and disable secondary criteria according to the strategy defined
	 * in the Properties file.
	 * 
	 * @param starvationCounter
	 */
	protected void updateSecondaryCriterion(int starvationCounter) {

		if (Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER > 0
				&& TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {

			double progress = this.progress() * 100.0;

			if (progress > Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER) {
				if (Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION) {
					updateSecondaryObjectiveStarvation(starvationCounter);
				} else {
					enableFirstSecondaryCriterion();
					Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER = 0;
				}
			}
		} else if (Properties.ENABLE_SECONDARY_OBJECTIVE_STARVATION
				&& Properties.ENABLE_SECONDARY_OBJECTIVE_AFTER == 0
				&& TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
			updateSecondaryObjectiveStarvation(starvationCounter);
		}
	}

	private void updateSecondaryObjectiveStarvation(int starvationCounter) {
		if (starvationCounter > Properties.STARVATION_AFTER_GENERATION && !TestSuiteChromosome.isFirstSecondaryObjectiveEnabled()) {
			enableFirstSecondaryCriterion();
		} else {
			if (starvationCounter == 0 && TestSuiteChromosome.isFirstSecondaryObjectiveEnabled()
					&& TestSuiteChromosome.getSecondaryObjectivesSize() > 1) {
				disableFirstSecondaryCriterion();
			}
		}
	}

	/**
	 * Apply local search, starting from the best individual and continue
	 * applying it to all individuals until the local search budget is used up.
	 * 
	 * The population list is re-ordered if needed.
	 */
	protected void applyLocalSearch() {
		if (!shouldApplyLocalSearch())
			return;

		logger.debug("Applying local search");
		//LocalSearchBudget.getInstance().localSearchStarted();

		boolean improvement = false;

		for (Chromosome individual : population) {
			if (isFinished())
				break;

//			if (LocalSearchBudget.getInstance().isFinished()) {
//				logger.debug("Local search budget used up, exiting local search");
//				break;
//			}

			if (individual.localSearch(localObjective)) {
				improvement = true;
			}
		}
		
		if (improvement) {
			//DSEStats.getInstance().reportNewIncrease();
			updateProbability(true);
			logger.debug("Increasing probability of applying LS to " + localSearchProbability);
		} else {
			//DSEStats.getInstance().reportNewDecrease();
			updateProbability(false);
			logger.debug("Decreasing probability of applying LS to " + localSearchProbability);
		}

		if (improvement) {
			// If an improvement occurred to one of the individuals, it could
			// be the case that the improvement was so good, that the individual
			// has surpassed to the previous individual, which makes the population
			// list not sorted any more. 
			if (!populationIsSorted()) {
				this.sortPopulation();
			}
		}
	}

	/**
	 * Returns true if the population is sorted according to the fitness 
	 * values.
	 * 
	 * @return true if the population is sorted (or empty)
	 */
	private boolean populationIsSorted() {
		Chromosome previousIndividual = null;
		for (Chromosome currentIndividual : this.population) {
			if (previousIndividual!=null) {
				if (!isBetterOrEqual(previousIndividual, currentIndividual)) {
					// at least two individuals are not sorted
					return false;
				}
			}
			previousIndividual = currentIndividual;
		}
		// the population is sorted (or empty)
		return true;
	}

	/**
	 * Returns true if the fitness functions are maximization functions
	 * or false if all fitness functions are minimisation functions.
	 * It expects that all fitnessFunctions are minimising or maximising,
	 * it cannot happen that minimization and maximization functions are
	 * together.
	 * 
	 * @return
	 */
	private boolean isMaximizationFunction() {
		return fitnessFunctions.get(0).isMaximizationFunction();
	}

	protected void updateProbability(boolean improvement){
		if (improvement) {
			localSearchProbability *= Properties.LOCAL_SEARCH_ADAPTATION_RATE;
			localSearchProbability = Math.min(localSearchProbability, 1.0);
		} else {
			localSearchProbability /= Properties.LOCAL_SEARCH_ADAPTATION_RATE;
			localSearchProbability = Math.max(localSearchProbability, Double.MIN_VALUE);
		}
		// localSearchProbability = Math.pow(
		// 1.0 + ((1.0 - localSearchProbability) / localSearchProbability) *
		// Math.exp(delta), -1.0);
	}
	
	
	/**
	 * Apply dynamic symbolic execution
	 */
	/*
	 * @Deprecated protected void applyDSE() {
	 * logger.info("Applying DSE at generation " + currentIteration);
	 * DSEBudget.DSEStarted();
	 * 
	 * boolean success = false;
	 * 
	 * for (Chromosome individual : population) { if (isFinished()) break;
	 * 
	 * if (DSEBudget.isFinished()) break;
	 * 
	 * boolean result = individual.applyDSE(this); if(result) success = true; }
	 * 
	 * if(Properties.DSE_ADAPTIVE_PROBABILITY > 0.0) { if(success) {
	 * Properties.DSE_ADAPTIVE_PROBABILITY *= Properties.DSE_ADAPTIVE_RATE;
	 * Properties.DSE_ADAPTIVE_PROBABILITY =
	 * Math.min(Properties.DSE_ADAPTIVE_PROBABILITY, 1.0); } else {
	 * Properties.DSE_ADAPTIVE_PROBABILITY /= Properties.DSE_ADAPTIVE_RATE;
	 * Properties.DSE_ADAPTIVE_PROBABILITY =
	 * Math.max(Properties.DSE_ADAPTIVE_PROBABILITY, Double.MIN_VALUE); }
	 * logger.
	 * info("Updating DSE probability to "+Properties.DSE_ADAPTIVE_PROBABILITY);
	 * } }
	 */
	/**
	 * Set up initial population
	 */
	public abstract void initializePopulation();

	/**
	 * {@inheritDoc}
	 * 
	 * Generate solution
	 */
	@Override
	public abstract void generateSolution();

	/**
	 * Fills the population at first with recycled chromosomes - for more
	 * information see recycleChromosomes() and ChromosomeRecycler - and after
	 * that, the population is filled with random chromosomes.
	 * 
	 * This method guarantees at least a proportion of
	 * Properties.initially_enforeced_Randomness % of random chromosomes
	 * 
	 * @param population_size
	 *            a int.
	 */
	protected void generateInitialPopulation(int population_size) {
		generateRandomPopulation(population_size - population.size());
	}

	/**
	 * This method can be used to kick out chromosomes when the population is
	 * possibly overcrowded
	 * 
	 * Depending on the Property "starve_by_fitness" chromosome are either
	 * kicked out randomly or according to their fitness
	 * 
	 * @param limit
	 *            a int.
	 */
	protected void starveToLimit(int limit) {
		if (Properties.STARVE_BY_FITNESS)
			starveByFitness(limit);
		else
			starveRandomly(limit);
	}

	/**
	 * This method can be used to kick out random chromosomes in the current
	 * population until the given limit is reached again.
	 * 
	 * @param limit
	 *            a int.
	 */
	protected void starveRandomly(int limit) {
		while (population.size() > limit) {
			int removePos = Randomness.nextInt() % population.size();
			population.remove(removePos);
		}
	}

	/**
	 * This method can be used to kick out the worst chromosomes in the current
	 * population until the given limit is reached again.
	 * 
	 * @param limit
	 *            a int.
	 */
	protected void starveByFitness(int limit) {
		calculateFitnessAndSortPopulation();
		for (int i = population.size() - 1; i >= limit; i--) {
			population.remove(i);
		}
	}

	/**
	 * Generate random population of given size
	 * 
	 * @param population_size
	 *            a int.
	 */
	protected void generateRandomPopulation(int population_size) {
		logger.debug("Creating random population");
		for (int i = 0; i < population_size; i++) {
			T individual = chromosomeFactory.getChromosome();
			for (FitnessFunction<?> fitnessFunction : this.fitnessFunctions) {
				individual.addFitness(fitnessFunction);
			}

			population.add(individual);
			if (isFinished())
				break;
		}
		logger.debug("Created " + population.size() + " individuals");
	}

	/**
	 * Delete all current individuals
	 */
	public void clearPopulation() {
		logger.debug("Resetting population");
		population.clear();
	}

	public void addFitnessFunction(FitnessFunction<T> function) {
		fitnessFunctions.add(function);
		localObjective.addFitnessFunction(function);
	}

	public void addFitnessFunctions(List<FitnessFunction<T>> functions) {
		for (FitnessFunction<T> function : functions)
			this.addFitnessFunction(function);
	}

	public FitnessFunction<T> getFitnessFunction() {
		return fitnessFunctions.get(0);
	}


	public List<FitnessFunction<T>> getFitnessFunctions() {
		return fitnessFunctions;
	}

	public int getNumberOfFitnessFunctions() {
		return fitnessFunctions.size();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		int i = 0;
		for (Chromosome c : population) {
			str.append("\n  - test " + i);

			for (FitnessFunction<T> ff : this.fitnessFunctions) {
				DecimalFormat df = new DecimalFormat("#.#####");
				str.append(", " + ff.getClass().getSimpleName().replace("CoverageSuiteFitness", "")
						+ " " + df.format(c.getFitness(ff)));
			}

			i++;
		}

		return str.toString();
	}

	public void setSelectionFunction(SelectionFunction<T> function) {
		selectionFunction = function;
	}


	public SelectionFunction<T> getSelectionFunction() {
		return selectionFunction;
	}

	public void setBloatControl(BloatControlFunction bloat_control) {
		this.bloatControl.clear();
		addBloatControl(bloat_control);
	}

	public void addBloatControl(BloatControlFunction bloat_control) {
		this.bloatControl.add(bloat_control);
	}

	public boolean isTooLong(Chromosome chromosome) {
		for (BloatControlFunction b : bloatControl) {
			if (b.isTooLong(chromosome))
				return true;
		}
		return false;
	}

	/**
	 * Get number of iterations
	 * 
	 * @return Number of iterations
	 */
	public int getAge() {
		return currentIteration;
	}

	/**
	 * Calculate fitness for all individuals
	 */
	protected void calculateFitnessAndSortPopulation() {
		logger.debug("Calculating fitness for " + population.size() + " individuals");

		Iterator<T> iterator = population.iterator();
		while (iterator.hasNext()) {
			T c = iterator.next();
			if (isFinished()) {
				if (c.isChanged())
					iterator.remove();
			} else {
				for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
					fitnessFunction.getFitness(c);
					notifyEvaluation(c);
				}
			}
		}

		// Sort population
		sortPopulation();
	}

	protected void updateBestIndividualFromArchive() {
		if (!Properties.TEST_ARCHIVE)
			return;

		T best = Archive.getArchiveInstance().mergeArchiveAndSolution(getBestIndividual());

		// The archive may contain tests evaluated with a fitness function
		// that is not part of the optimization (e.g. ibranch secondary objective)
		Iterator<FitnessFunction<?>> it = best.getCoverageValues().keySet().iterator();
		while(it.hasNext()) {
			FitnessFunction<?> ff = it.next();
			if(!fitnessFunctions.contains(ff))
				it.remove();
		}
		population.add(0, best);
	}

	/**
	 * <p>
	 * getPopulationSize
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getPopulationSize() {
		return population.size();
	}

	/**
	 * Copy best individuals
	 * 
	 * @return a {@link List} object.
	 */
	@SuppressWarnings("unchecked")
	protected List<T> elitism() {
		logger.debug("Elitism with ELITE = " + Properties.ELITE);

		List<T> elite = new ArrayList<T>();

		for (int i = 0; i < Properties.ELITE; i++) {
			logger.trace("Copying individual " + i + " with fitness "
					+ population.get(i).getFitness());
			elite.add((T) population.get(i).clone());
		}
		logger.trace("Done.");
		return elite;
	}

	/**
	 * Create random individuals
	 *
	 * @return a {@link List} object.
	 */
	protected List<Chromosome> randomism() {
		logger.debug("Randomism");

		List<Chromosome> randoms = new ArrayList<Chromosome>();

		for (int i = 0; i < Properties.ELITE; i++) {
			randoms.add(chromosomeFactory.getChromosome());
		}
		return randoms;
	}

	/**
	 * update archive fitness functions
	 */
	public void updateFitnessFunctionsAndValues() {
		for (FitnessFunction<T> f : fitnessFunctions) {
			f.updateCoveredGoals();
		}

		// Do we actually have to perform yet another fitness evaluation?
		// Yes, if ARCHIVE has been updated, No otherwise.
		if (!Archive.getArchiveInstance().hasBeenUpdated()) {
			return;
		}

		for (T t : population) {
			for (FitnessFunction<T> fitnessFunction : fitnessFunctions) {
				fitnessFunction.getFitness(t);
			}
		}

		Archive.getArchiveInstance().setHasBeenUpdated(false);
	}

	public T getBestIndividual() {

		if (population.isEmpty()) {
			return this.chromosomeFactory.getChromosome();
		}

		// Assume population is sorted
		int max = -1;
		int index = 0;
		for (int i = 0; i < population.size(); ++i) {
			T testSuiteChromosome = population.get(i);
			if( max < ((TestSuiteChromosome) testSuiteChromosome).getTests().size())
			{
				max = ((TestSuiteChromosome) testSuiteChromosome).getTests().size();
				index = i;
			}
		}
		return population.get(index);
	}

	public List<T> getBestIndividuals() {

		List<T> bestIndividuals = new ArrayList<T>();

		if (this.population.isEmpty()) {
			bestIndividuals.add(this.chromosomeFactory.getChromosome());
			return bestIndividuals;
		}

		if (Properties.ALGORITHM == Properties.Algorithm.NSGAII ||
		    Properties.ALGORITHM == Properties.Algorithm.SPEA2)
			return population;

		// Assume population is sorted
		bestIndividuals.add(population.get(0));
		return bestIndividuals;
	}

    public void writeIndividuals(List<T> individuals) {
      if (!Properties.WRITE_INDIVIDUALS) {
        return;
      }

      File dir = new File(Properties.REPORT_DIR);
      if (!dir.exists()) {
        if (!dir.mkdirs()) {
          throw new RuntimeException("Cannot create report dir: " + Properties.REPORT_DIR);
        }
      }

      try {
        File populationFile = new File(
            Properties.REPORT_DIR + File.separator + "pareto_" + this.currentIteration + ".csv");
        populationFile.createNewFile();

        FileWriter fw = new FileWriter(populationFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);

        // header
        List<String> l_string = new ArrayList<String>();

        if (Properties.ALGORITHM == Properties.Algorithm.NSGAII) {
          l_string.add("rank");
        } else if (Properties.ALGORITHM == Properties.Algorithm.SPEA2) {
          l_string.add("strength");
        }

        for (int i = 0; i < this.fitnessFunctions.size(); i++) {
          l_string.add(this.fitnessFunctions.get(i).getClass().getSimpleName());
        }
        out.println(String.join(",", l_string));

        // content
        for (int j = 0; j < individuals.size(); j++) {
          l_string.clear();

          T individual = individuals.get(j);
          if (Properties.ALGORITHM == Properties.Algorithm.NSGAII) {
            l_string.add(Integer.toString(individual.getRank()));
          } else if (Properties.ALGORITHM == Properties.Algorithm.SPEA2) {
            l_string.add(Double.toString(individual.getDistance()));
          }

          for (int i = 0; i < this.fitnessFunctions.size(); i++) {
            l_string.add(Double.toString(individual.getFitness(this.fitnessFunctions.get(i))));
          }

          out.println(String.join(",", l_string));
        }

        out.close();
        bw.close();
        fw.close();

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

	public void setChromosomeFactory(ChromosomeFactory<T> factory) {
		chromosomeFactory = factory;
	}


	public void addListener(SearchListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SearchListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify all search listeners of search start
	 */
	protected void notifySearchStarted() {
		for (SearchListener listener : listeners) {
			listener.searchStarted(this);
		}
	}

	/**
	 * Notify all search listeners of search end
	 */
	protected void notifySearchFinished() {
		for (SearchListener listener : listeners) {
			listener.searchFinished(this);
		}
	}

	/**
	 * Notify all search listeners of iteration
	 */
	protected void notifyIteration() {
		for (SearchListener listener : listeners) {
			listener.iteration(this);
		}
	}

	protected void notifyEvaluation(Chromosome chromosome) {
		for (SearchListener listener : listeners) {
			listener.fitnessEvaluation(chromosome);
		}
	}

	protected void notifyMutation(Chromosome chromosome) {
		for (SearchListener listener : listeners) {
			listener.modification(chromosome);
		}
	}

	/**
	 * Sort the population by fitness
	 *
	 * WARN: used only with singular objective algorithms, multi-objective
	 * algorithms should implement their own 'sort'
	 */
	protected void sortPopulation() {
		if (Properties.SHUFFLE_GOALS)
			Randomness.shuffle(population);

		if (isMaximizationFunction()) {
			Collections.sort(population, Collections.reverseOrder());
		} else {
			Collections.sort(population);
		}
	}

	/**
	 * Accessor for population Chromosome *
	 *
	 * @return a {@link List} object.
	 */
	public List<T> getPopulation() {
		return population;
	}

	/**
	 * Determine if the next generation has reached its size limit
	 *
	 * @param nextGeneration
	 *            a {@link List} object.
	 * @return a boolean.
	 */
	public boolean isNextPopulationFull(List<T> nextGeneration) {
		return false;//populationLimit.isPopulationFull(nextGeneration);
	}

	public void setPopulationLimit(PopulationLimit limit) {
		this.populationLimit = limit;
	}

	/**
	 * Determine whether any of the stopping conditions hold
	 *
	 * @return a boolean.
	 */
	public boolean isFinished() {
		for (StoppingCondition c : stoppingConditions) {
			// logger.error(c + " "+ c.getCurrentValue());
			if (c.isFinished())
				return true;
		}
		return false;
	}

	public void addStoppingCondition(StoppingCondition condition) {
		Iterator<StoppingCondition> it = stoppingConditions.iterator();
		while (it.hasNext()) {
			if (it.next().getClass().equals(condition.getClass())) {
				return;
			}
		}
		logger.debug("Adding new stopping condition");
		stoppingConditions.add(condition);
		addListener(condition);
	}

	public Set<StoppingCondition> getStoppingConditions() {
		return stoppingConditions;
	}

	public void setStoppingCondition(StoppingCondition condition) {
		stoppingConditions.clear();
		logger.debug("Setting stopping condition");
		stoppingConditions.add(condition);
		addListener(condition);
	}

	public void removeStoppingCondition(StoppingCondition condition) {
		Iterator<StoppingCondition> it = stoppingConditions.iterator();
		while (it.hasNext()) {
			if (it.next().getClass().equals(condition.getClass())) {
				it.remove();
				removeListener(condition);
			}
		}
	}

	/**
	 * <p>
	 * resetStoppingConditions
	 * </p>
	 */
	public void resetStoppingConditions() {
		for (StoppingCondition c : stoppingConditions) {
			c.reset();
		}
	}

	/**
	 * <p>
	 * setStoppingConditionLimit
	 * </p>
	 *
	 * @param value
	 *            a int.
	 */
	public void setStoppingConditionLimit(int value) {
		for (StoppingCondition c : stoppingConditions) {
			c.setLimit(value);
		}
	}

	protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
		// if (fitnessFunction.isMaximizationFunction()) {
		if (getFitnessFunction().isMaximizationFunction()) {
			return chromosome1.compareTo(chromosome2) >= 0;
		} else {
			return chromosome1.compareTo(chromosome2) <= 0;
		}
	}

	/*
	 * protected boolean isBetter(Chromosome chromosome1, Chromosome
	 * chromosome2) { if (fitnessFunction.isMaximizationFunction()) { return
	 * chromosome1.compareTo(chromosome2) > 0; } else { return
	 * chromosome1.compareTo(chromosome2) < 0; } }
	 */

	/**
	 * <p>
	 * getBest
	 * </p>
	 *
	 * @param chromosome1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param chromosome2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a {@link org.evosuite.ga.Chromosome} object.
	 */
	/*
	 * protected Chromosome getBest(Chromosome chromosome1, Chromosome
	 * chromosome2) { if (isBetterOrEqual(chromosome1, chromosome2)) return
	 * chromosome1; else return chromosome2; }
	 */

	/**
	 * Prints out all information regarding this GAs stopping conditions
	 *
	 * So far only used for testing purposes in TestSuiteGenerator
	 */
	public void printBudget() {
		LoggingUtils.getGeneratorLogger().info("* GA-Budget:");
		for (StoppingCondition sc : stoppingConditions)
			LoggingUtils.getGeneratorLogger().info("\t- " + sc.toString());
	}

	/**
	 * <p>
	 * getBudgetString
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getBudgetString() {
		String r = "";
		for (StoppingCondition sc : stoppingConditions)
			r += sc.toString() + " ";

		return r;
	}

	/**
	 * Returns the progress of the search.
	 * 
	 * @return a value [0.0, 1.0]
	 */
	protected double progress() {
		long totalbudget = 0;
		long currentbudget = 0;

		for (StoppingCondition sc : this.stoppingConditions) {
			if (sc.getLimit() != 0) {
				totalbudget += sc.getLimit();
				currentbudget += sc.getCurrentValue();
			}
		}

		return (double) currentbudget / (double) totalbudget;
	}

	/*
	 * private void writeObject(ObjectOutputStream oos) throws IOException { if
	 * (listeners.contains(SearchStatistics.getInstance())) {
	 * removeListener(SearchStatistics.getInstance()); oos.defaultWriteObject();
	 * oos.writeObject(Boolean.TRUE); // Write/save additional fields
	 * oos.writeObject(SearchStatistics.getInstance()); } else {
	 * oos.defaultWriteObject(); oos.writeObject(Boolean.FALSE); } }
	 * 
	 * // assumes "static java.util.Date aDate;" declared private void
	 * readObject(ObjectInputStream ois) throws ClassNotFoundException,
	 * IOException { ois.defaultReadObject(); listeners = new
	 * HashSet<SearchListener>(); stoppingConditions = new
	 * HashSet<StoppingCondition>();
	 * 
	 * boolean addStatistics = (Boolean) ois.readObject(); if (addStatistics) {
	 * SearchStatistics.setInstance((SearchStatistics) ois.readObject());
	 * addListener(SearchStatistics.getInstance()); } }
	 */
}
