package generator.strategy;

import generator.Properties;
import generator.ga.Chromosome;
import generator.ga.metaheuristic.GeneticAlgorithm;
import generator.ga.populationlimit.IndividualPopulationLimit;
import generator.ga.populationlimit.PopulationLimit;
import generator.ga.populationlimit.SizePopulationLimit;
import generator.ga.populationlimit.StatementsPopulationLimit;
import generator.ga.stoppingconditions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PropertiesSearchAlgorithmFactory<T extends Chromosome>  {

	protected static final Logger logger = LoggerFactory.getLogger(PropertiesSearchAlgorithmFactory.class);


	protected PopulationLimit getPopulationLimit() {
		switch (Properties.POPULATION_LIMIT) {
		case INDIVIDUALS:
			return new IndividualPopulationLimit();
		case TESTS:
			return new SizePopulationLimit();
		case STATEMENTS:
			return new StatementsPopulationLimit();
		default:
			throw new RuntimeException("Unsupported population limit");
		}
	}
	
	protected StoppingCondition getStoppingCondition() {
		logger.info("Setting stopping condition: " + Properties.STOPPING_CONDITION);
		switch (Properties.STOPPING_CONDITION) {
		case MAXGENERATIONS:
			return new MaxGenerationStoppingCondition();
		case MAXFITNESSEVALUATIONS:
			return new MaxFitnessEvaluationsStoppingCondition();
		case MAXTIME:
			return new MaxTimeStoppingCondition();
		case MAXTESTS:
			return new MaxTestsStoppingCondition();
		case MAXSTATEMENTS:
			return new MaxStatementsStoppingCondition();
		case TIMEDELTA:
			return new TimeDeltaStoppingCondition();
		default:
			logger.warn("Unknown stopping condition: " + Properties.STOPPING_CONDITION);
			return new MaxGenerationStoppingCondition();
		}
	}
	
	public abstract GeneticAlgorithm<?> getSearchAlgorithm();
}
