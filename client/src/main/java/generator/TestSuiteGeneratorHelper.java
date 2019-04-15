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
package generator;

import generator.coverage.branch.Branch;
import generator.rmi.ClientServices;
import generator.statistics.RuntimeVariable;
import generator.strategy.TestGenerationStrategy;
import generator.strategy.WholeTestSuiteStrategy;
import generator.testsuite.TestSuiteChromosome;
import generator.utils.LoggingUtils;

import java.util.Map;
import java.util.Set;

/**
 * Created by sina on 06/04/2017.
 */
public class TestSuiteGeneratorHelper {

  static void printTestCriterion(Properties.Criterion criterion) {
    switch (criterion) {
      case WEAKMUTATION:
        LoggingUtils.getGeneratorLogger().info("  - Mutation testing (weak)");
        break;
      case ONLYMUTATION:
        LoggingUtils.getGeneratorLogger().info("  - Only Mutation testing (weak)");
        break;
      case STRONGMUTATION:
      case MUTATION:
        LoggingUtils.getGeneratorLogger().info("  - Mutation testing (strong)");
        break;
      case DEFUSE:
        LoggingUtils.getGeneratorLogger().info("  - All DU Pairs");
        break;
      case STATEMENT:
        LoggingUtils.getGeneratorLogger().info("  - Statement Coverage");
        break;
      case RHO:
        LoggingUtils.getGeneratorLogger().info("  - Rho Coverage");
        break;
      case AMBIGUITY:
        LoggingUtils.getGeneratorLogger().info("  - Ambiguity Coverage");
        break;
      case ALLDEFS:
        LoggingUtils.getGeneratorLogger().info("  - All Definitions");
        break;
      case EXCEPTION:
        LoggingUtils.getGeneratorLogger().info("  - Exception");
        break;
      case ONLYBRANCH:
        LoggingUtils.getGeneratorLogger().info("  - Only-Branch Coverage");
        break;
      case METHODTRACE:
        LoggingUtils.getGeneratorLogger().info("  - Method Coverage");
        break;
      case METHOD:
        LoggingUtils.getGeneratorLogger().info("  - Top-Level Method Coverage");
        break;
      case METHODNOEXCEPTION:
        LoggingUtils.getGeneratorLogger().info("  - No-Exception Top-Level Method Coverage");
        break;
      case LINE:
        LoggingUtils.getGeneratorLogger().info("  - Line Coverage");
        break;
      case ONLYLINE:
        LoggingUtils.getGeneratorLogger().info("  - Only-Line Coverage");
        break;
      case OUTPUT:
        LoggingUtils.getGeneratorLogger().info("  - Method-Output Coverage");
        break;
      case INPUT:
        LoggingUtils.getGeneratorLogger().info("  - Method-Input Coverage");
        break;
      case BRANCH:
        LoggingUtils.getGeneratorLogger().info("  - Branch Coverage");
        break;
      case CBRANCH:
        LoggingUtils.getGeneratorLogger().info("  - Context Branch Coverage");
        break;
      case IBRANCH:
        LoggingUtils.getGeneratorLogger().info("  - Interprocedural Context Branch Coverage");
        break;
      case TRYCATCH:
        LoggingUtils.getGeneratorLogger().info("  - Try-Catch Branch Coverage");
        break;
      case REGRESSION:
        LoggingUtils.getGeneratorLogger().info("  - Regression");
        break;
      default:
        throw new IllegalArgumentException("Unrecognized criterion: " + criterion);
    }
  }

  static TestGenerationStrategy getTestGenerationStrategy() {
    switch (Properties.STRATEGY) {
      case TESTSUITE:
        return new WholeTestSuiteStrategy();
      default:
        throw new RuntimeException("Unsupported strategy: " + Properties.STRATEGY);
    }
  }

  private static int getBytecodeCount(RuntimeVariable v, Map<RuntimeVariable, Set<Integer>> m) {
    Set<Integer> branchSet = m.get(v);
    return (branchSet == null) ? 0 : branchSet.size();
  }

  static void getBytecodeStatistics() {

  }

  static void printTestCriterion() {
    if (Properties.CRITERION.length > 1) {
      LoggingUtils.getGeneratorLogger().info("* Test criteria:");
    } else {
      LoggingUtils.getGeneratorLogger().info("* Test criterion:");
    }
    for (int i = 0; i < Properties.CRITERION.length; i++) {
      printTestCriterion(Properties.CRITERION[i]);
    }
  }

  public static void addAssertions(TestSuiteChromosome tests) {
  }

}
