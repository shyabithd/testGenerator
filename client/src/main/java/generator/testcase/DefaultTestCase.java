package generator.testcase;

import generator.assertion.Assertion;
import generator.ga.ConstructionFailedException;
import generator.testcase.statement.Statement;
import generator.testcase.variable.VariableReference;
import generator.utils.Listener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultTestCase implements TestCase, Serializable {

	@Override
	public int getID() {

		return 0;
	}

	@Override
	public void accept(TestVisitor visitor) {

	}

	@Override
	public void addAssertions(TestCase other) {

	}

	@Override
	public VariableReference addStatement(Statement statement) {

		return null;
	}

	@Override
	public VariableReference addStatement(Statement statement, int position) {

		return null;
	}

	@Override
	public void addStatements(List<? extends Statement> statements) {

	}

	@Override
	public void chop(int length) {

	}

	@Override
	public int sliceFor(VariableReference var) {

		return 0;
	}

	@Override
	public void clearCoveredGoals() {

	}

	@Override
	public boolean contains(Statement statement) {

		return false;
	}

	@Override
	public TestCase clone() {

		return null;
	}

	@Override
	public Set<Class<?>> getAccessedClasses() {

		return null;
	}

	@Override
	public List<Assertion> getAssertions() {

		return null;
	}

	@Override
	public Set<Class<?>> getDeclaredExceptions() {

		return null;
	}

	@Override
	public Set<VariableReference> getDependencies(VariableReference var) {

		return null;
	}

	@Override
	public VariableReference getLastObject(Type type) throws ConstructionFailedException {

		return null;
	}

	@Override
	public VariableReference getLastObject(Type type, int position) throws ConstructionFailedException {

		return null;
	}

	@Override
	public List<VariableReference> getObjects(int position) {

		return null;
	}

	@Override
	public List<VariableReference> getObjects(Type type, int position) {

		return null;
	}

	@Override
	public VariableReference getRandomNonNullNonPrimitiveObject(Type type, int position) throws ConstructionFailedException {

		return null;
	}

	@Override
	public VariableReference getRandomNonNullObject(Type type, int position) throws ConstructionFailedException {

		return null;
	}

	@Override
	public VariableReference getRandomObject() {

		return null;
	}

	@Override
	public VariableReference getRandomObject(int position) {

		return null;
	}

	@Override
	public VariableReference getRandomObject(Type type) throws ConstructionFailedException {

		return null;
	}

	@Override
	public VariableReference getRandomObject(Type type, int position) throws ConstructionFailedException {

		return null;
	}

	@Override
	public Set<VariableReference> getReferences(VariableReference var) {

		return null;
	}

	@Override
	public VariableReference getReturnValue(int position) {

		return null;
	}

	@Override
	public Statement getStatement(int position) {

		return null;
	}

	@Override
	public boolean hasStatement(int position) {

		return false;
	}

	@Override
	public boolean hasAssertions() {

		return false;
	}

	@Override
	public boolean hasCastableObject(Type type) {

		return false;
	}

	@Override
	public boolean hasObject(Type type, int position) {

		return false;
	}

	@Override
	public boolean hasReferences(VariableReference var) {

		return false;
	}

	@Override
	public boolean isAccessible() {

		return false;
	}

	@Override
	public boolean isEmpty() {

		return false;
	}

	@Override
	public boolean isFailing() {

		return false;
	}

	@Override
	public void setFailing() {

	}

	@Override
	public boolean isPrefix(TestCase t) {

		return false;
	}

	@Override
	public boolean isUnstable() {

		return false;
	}

	@Override
	public boolean isValid() {

		return false;
	}

	@Override
	public void remove(int position) {

	}

	@Override
	public void removeAssertion(Assertion assertion) {

	}

	@Override
	public void removeAssertions() {

	}

	@Override
	public void replace(VariableReference var1, VariableReference var2) {

	}

	@Override
	public VariableReference setStatement(Statement statement, int position) {

		return null;
	}

	@Override
	public void setUnstable(boolean unstable) {

	}

	@Override
	public int size() {

		return 0;
	}

	@Override
	public int sizeWithAssertions() {

		return 0;
	}

	@Override
	public String toCode() {

		return null;
	}

	@Override
	public String toCode(Map<Integer, Throwable> exceptions) {

		return null;
	}

	@Override
	public void addListener(Listener<Void> listener) {

	}

	@Override
	public void deleteListener(Listener<Void> listener) {

	}

	@Override
	public Iterator<Statement> iterator() {

		return null;
	}
}