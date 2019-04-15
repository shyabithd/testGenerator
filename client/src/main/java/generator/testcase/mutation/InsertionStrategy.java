package generator.testcase.mutation;

import generator.testcase.TestCase;

public interface InsertionStrategy {

	public int insertStatement(TestCase test, int lastPosition);
}
