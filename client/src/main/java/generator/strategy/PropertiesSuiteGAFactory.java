package generator.strategy;

import generator.Properties;
import generator.ga.ChromosomeFactory;
import generator.ga.FitnessReplacementFunction;
import generator.ga.archive.ArchiveTestChromosomeFactory;
import generator.ga.crossover.*;
import generator.ga.metaheuristic.GeneticAlgorithm;
import generator.ga.metaheuristic.MonotonicGA;
import generator.ga.metaheuristic.StandardGA;
import generator.ga.selection.*;
import generator.ga.stoppingconditions.*;
import generator.testsuite.TestSuiteChromosome;
import generator.testsuite.TestSuiteChromosomeFactory;
import generator.testsuite.TestSuiteReplacementFunction;
import generator.utils.ArrayUtil;
import generator.utils.ShutdownTestWriter;
import sun.misc.Signal;

@SuppressWarnings("restriction")
public class PropertiesSuiteGAFactory extends PropertiesSearchAlgorithmFactory<TestSuiteChromosome> {

	
	protected ChromosomeFactory<TestSuiteChromosome> getChromosomeFactory() {
		switch (Properties.STRATEGY) {
			case TESTSUITE:
				switch (Properties.TEST_FACTORY) {
					case ARCHIVE:
						logger.info("Using archive chromosome factory");
						return new TestSuiteChromosomeFactory(new ArchiveTestChromosomeFactory());
					default:
						throw new RuntimeException("Unsupported test factory: "
								+ Properties.TEST_FACTORY);
				}
			default:
				throw new RuntimeException("Unsupported test factory: "
						+ Properties.TEST_FACTORY);
		}
	}
	
	protected GeneticAlgorithm<TestSuiteChromosome> getGeneticAlgorithm(ChromosomeFactory<TestSuiteChromosome> factory) {
		switch (Properties.ALGORITHM) {
			case MONOTONIC_GA:
				logger.info("Chosen search algorithm: MonotonicGA");
			{
				MonotonicGA<TestSuiteChromosome> ga = new MonotonicGA<TestSuiteChromosome>(factory);
				if (Properties.REPLACEMENT_FUNCTION == Properties.TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				}
				return ga;
			}
			default:
				logger.info("Chosen search algorithm: StandardGA");
			{
				StandardGA<TestSuiteChromosome> ga = new StandardGA<TestSuiteChromosome>(factory);
				return ga;
			}
		}
	}
	
	protected SelectionFunction<TestSuiteChromosome> getSelectionFunction() {
		switch (Properties.SELECTION_FUNCTION) {
		case ROULETTEWHEEL:
			return new FitnessProportionateSelection<>();
		case TOURNAMENT:
			return new TournamentSelection<>();
		case BINARY_TOURNAMENT:
		    return new BinaryTournamentSelectionCrowdedComparison<>();
		default:
			return new RankSelection<>();
		}
	}
	
	protected CrossOverFunction getCrossoverFunction() {
		switch (Properties.CROSSOVER_FUNCTION) {
		case SINGLEPOINTFIXED:
			return new SinglePointFixedCrossOver();
		case SINGLEPOINTRELATIVE:
			return new SinglePointRelativeCrossOver();
		case SINGLEPOINT:
			return new SinglePointCrossOver();
		case COVERAGE:
			if (Properties.STRATEGY != Properties.Strategy.TESTSUITE)
				throw new RuntimeException(
				        "Coverage crossover function requires test suite mode");

			return new CoverageCrossOver();
		case UNIFORM:
			return new UniformCrossOver();
		default:
			throw new RuntimeException("Unknown crossover function: "
			        + Properties.CROSSOVER_FUNCTION);
		}
	}
	
	@Override
	public GeneticAlgorithm<TestSuiteChromosome> getSearchAlgorithm() {
		ChromosomeFactory<TestSuiteChromosome> factory = getChromosomeFactory();
		
		// FIXXME
		GeneticAlgorithm<TestSuiteChromosome> ga = getGeneticAlgorithm(factory);
//
//		if (Properties.NEW_STATISTICS)
//			ga.addListener(new StatisticsListener());

		// How to select candidates for reproduction
		SelectionFunction<TestSuiteChromosome> selectionFunction = getSelectionFunction();
		selectionFunction.setMaximize(false);
		ga.setSelectionFunction(selectionFunction);

		// When to stop the search
		StoppingCondition stopping_condition = getStoppingCondition();
		ga.setStoppingCondition(stopping_condition);
		// ga.addListener(stopping_condition);
		if (Properties.STOP_ZERO) {
			ga.addStoppingCondition(new ZeroFitnessStoppingCondition());
		}

		if (!(stopping_condition instanceof MaxTimeStoppingCondition)) {
			ga.addStoppingCondition(new GlobalTimeStoppingCondition());
		}

		if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.MUTATION)
		        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.STRONGMUTATION)) {
//			if (Properties.STRATEGY == Properties.Strategy.ONEBRANCH)
//				ga.addStoppingCondition(new MutationTimeoutStoppingCondition());
//			else
//				ga.addListener(new MutationTestPool());
			// } else if (Properties.CRITERION == Criterion.DEFUSE) {
			// if (Properties.STRATEGY == Strategy.EVOSUITE)
			// ga.addListener(new DefUseTestPool());
		}
		ga.resetStoppingConditions();
		ga.setPopulationLimit(getPopulationLimit());

//		// How to cross over
//		CrossOverFunction crossover_function = getCrossoverFunction();
//		ga.setCrossOverFunction(crossover_function);

		// What to do about bloat
		// MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		// ga.setBloatControl(bloat_control);
//
//		if (Properties.CHECK_BEST_LENGTH) {
//			RelativeSuiteLengthBloatControl bloat_control = new org.evosuite.testsuite.RelativeSuiteLengthBloatControl();
//			ga.addBloatControl(bloat_control);
//			ga.addListener(bloat_control);
//		}
		// ga.addBloatControl(new MaxLengthBloatControl());

//		TestSuiteSecondaryObjective.setSecondaryObjectives();

		// Some statistics
		//if (Properties.STRATEGY == Strategy.EVOSUITE)
		//	ga.addListener(SearchStatistics.getInstance());
		// ga.addListener(new MemoryMonitor());
		// ga.addListener(MutationStatistics.getInstance());
		// ga.addListener(BestChromosomeTracker.getInstance());

		if (Properties.DYNAMIC_LIMIT) {
			// max_s = GAProperties.generations * getBranches().size();
			// TODO: might want to make this dependent on the selected coverage
			// criterion
			// TODO also, question: is branchMap.size() really intended here?
			// I think BranchPool.getBranchCount() was intended
//			Properties.SEARCH_BUDGET = Properties.SEARCH_BUDGET
//			        * (BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getNumBranchlessMethods(Properties.TARGET_CLASS) + BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCountForClass(Properties.TARGET_CLASS) * 2);
			stopping_condition.setLimit(Properties.SEARCH_BUDGET);
			logger.info("Setting dynamic length limit to " + Properties.SEARCH_BUDGET);
		}

//		if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE) {
//			org.evosuite.ga.metaheuristics.SearchListener map = BranchCoverageMap.getInstance();
//			ga.addListener(map);
//		}

		if (Properties.SHUTDOWN_HOOK) {
			// ShutdownTestWriter writer = new
			// ShutdownTestWriter(Thread.currentThread());
			ShutdownTestWriter writer = new ShutdownTestWriter();
			ga.addStoppingCondition(writer);
			RMIStoppingCondition rmi = RMIStoppingCondition.getInstance();
			ga.addStoppingCondition(rmi);

			if (Properties.STOPPING_PORT != -1) {
				SocketStoppingCondition ss = new SocketStoppingCondition();
				ss.accept();
				ga.addStoppingCondition(ss);
			}

			// Runtime.getRuntime().addShutdownHook(writer);
			Signal.handle(new Signal("INT"), writer);
		}

//		ga.addListener(new ResourceController());
		return ga;
	}


}
