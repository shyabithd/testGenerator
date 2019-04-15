package generator.ga.archive;

import generator.Properties;
import generator.ga.ChromosomeFactory;
import generator.testcase.TestChromosome;
import generator.testsuite.RandomLengthTestFactory;
import generator.utils.LoggingUtils;
import generator.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ArchiveTestChromosomeFactory implements ChromosomeFactory<TestChromosome> {

  private static final long serialVersionUID = -8499807341782893732L;

  private final static Logger logger = LoggerFactory.getLogger(ArchiveTestChromosomeFactory.class);

  private ChromosomeFactory<TestChromosome> defaultFactory = new RandomLengthTestFactory();

  /**
   * Serialized tests read from disk, eg from previous runs in CTG
   */
  private List<TestChromosome> seededTests;

  public ArchiveTestChromosomeFactory() {
    if (Properties.CTG_SEEDS_FILE_IN != null) {
      //This does happen in CTG
      //seededTests = TestSuiteSerialization.loadTests(Properties.CTG_SEEDS_FILE_IN);
      LoggingUtils.getGeneratorLogger().info("* Loaded {} tests from {}", seededTests.size(), Properties.CTG_SEEDS_FILE_IN);
    }
  }

  @Override
  public TestChromosome getChromosome() {

    if (seededTests != null && !seededTests.isEmpty()) {
      /*
              Ideally, we should populate the archive directly when EvoSuite starts.
              But might be bit tricky based on current archive implementation (which needs executed tests).
              So, easiest approach is to just return tests here, with no mutation on those.
              However, this is done just once per test, as anyway those will end up
              in archive.
       */
      TestChromosome
              test =
              seededTests
                      .remove(seededTests.size() - 1); //pull out one element, 'last' just for efficiency
      test.getTestCase().removeAssertions(); // no assertions are used during search
      return test;
    }

    TestChromosome test = null;
    // double P = (double)Archive.getArchiveInstance().getNumberOfCoveredTargets() / (double)Archive.getArchiveInstance().getNumberOfTargets();
    if (!Archive.getArchiveInstance().isArchiveEmpty()
            && Randomness.nextDouble() < Properties.SEED_CLONE) {
      logger.info("Creating test based on archive");
      test = new TestChromosome();
      test.setTestCase(Archive.getArchiveInstance().getRandomSolution().getTestCase());
      int mutations = Randomness.nextInt(Properties.SEED_MUTATIONS);
      for (int i = 0; i < mutations; i++) {
        test.mutate();
      }
    } else {
      logger.info("Creating random test");
      test = defaultFactory.getChromosome();
    }

    return test;
  }

}
