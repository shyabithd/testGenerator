package generator.testcase;

import generator.testcase.statement.*;

public abstract class TestVisitor {

	public abstract void visitTestCase(TestCase test);


	public abstract void visitPrimitiveStatement(PrimitiveStatement<?> statement);

	public abstract void visitFieldStatement(FieldStatement statement);

	public abstract void visitMethodStatement(MethodStatement statement);

	public abstract void visitConstructorStatement(ConstructorStatement statement);

	public abstract void visitArrayStatement(ArrayStatement statement);

	public abstract void visitAssignmentStatement(AssignmentStatement statement);

	public abstract void visitNullStatement(NullStatement statement);

	public void visitStatement(Statement statement) {

		if (statement instanceof PrimitiveStatement<?>)
			visitPrimitiveStatement((PrimitiveStatement<?>) statement);
		else if (statement instanceof FieldStatement)
			visitFieldStatement((FieldStatement) statement);
		else if (statement instanceof ConstructorStatement)
			visitConstructorStatement((ConstructorStatement) statement);
		else if (statement instanceof MethodStatement)
			visitMethodStatement((MethodStatement) statement);
		else if (statement instanceof AssignmentStatement)
			visitAssignmentStatement((AssignmentStatement) statement);
		else if (statement instanceof ArrayStatement)
			visitArrayStatement((ArrayStatement) statement);
		else if (statement instanceof NullStatement)
			visitNullStatement((NullStatement) statement);
		else
			throw new RuntimeException("Unknown statement type: " + statement);
	}
}
