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
package generator.testcase.variable;

import generator.testcase.TestCase;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * This class represents a variable in a test case
 * 
 * TODO: Store generic types in this variable - we know at creation what it is
 * (from method calls)
 * 
 * @author Gordon Fraser
 */
public interface VariableReference extends Comparable<VariableReference>, Serializable {

	/**
	 * The position of the statement, defining this VariableReference, in the
	 * testcase.
	 * 
	 * @return a int.
	 */
	public int getStPosition();

	/**
	 * Distance metric used to select variables for mutation based on how close
	 * they are to the SUT
	 * 
	 * @return a int.
	 */
	public int getDistance();

	/**
	 * Set the distance metric
	 * 
	 * @param distance
	 *            a int.
	 */
	public void setDistance(int distance);

	/**
	 * Create a copy of the current variable
	 * 
	 * @return a {@link VariableReference} object.
	 */
	public abstract VariableReference clone();

	public abstract VariableReference clone(TestCase newTest);

	public abstract VariableReference copy(TestCase newTest, int offset);

	/**
	 * Return simple class name
	 * 
	 * @return a {@link String} object.
	 */
	public String getSimpleClassName();

	/**
	 * Return class name
	 *
	 * @return a {@link String} object.
	 */
	public String getClassName();

	/**
	 * <p>
	 * getComponentName
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getComponentName();

	/**
	 * <p>
	 * getComponentType
	 * </p>
	 *
	 * @return a {@link Type} object.
	 */
	public Type getComponentType();

	public TestCase getTestCase();

	/**
	 * Return true if variable is an enumeration
	 *
	 * @return a boolean.
	 */
	public boolean isEnum();

	/**
	 * Return true if variable is a primitive type
	 *
	 * @return a boolean.
	 */
	public boolean isPrimitive();

	/**
	 * Return true if variable is void
	 *
	 * @return a boolean.
	 */
	public boolean isVoid();

	/**
	 * Return true if variable is an array
	 *
	 * @return a boolean.
	 */
	public boolean isArray();

	/**
	 * Return true if this is an index into an array variable
	 *
	 * @return a boolean
	 */
	public boolean isArrayIndex();

	/**
	 * Return true if this is a reference to a public field
	 *
	 * @return a boolean
	 */
	public boolean isFieldReference();

	/**
	 * Return true if variable is a string
	 *
	 * @return a boolean.
	 */
	public boolean isString();

	/**
	 * Return true if type of variable is a primitive wrapper
	 *
	 * @return a boolean.
	 */
	public boolean isWrapperType();

	/**
	 * Return true if we can validly access this variable. This might not be the case for a field reference if the owner class is not accessible
	 *
	 * @return
	 */
	public boolean isAccessible();

	/**
	 * Return true if other type can be assigned to this variable
	 *
	 * @param other
	 *            Right hand side of the assignment
	 * @return a boolean.
	 */
	public boolean isAssignableFrom(Type other);

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 *
	 * @param other
	 *            Left hand side of the assignment
	 * @return a boolean.
	 */
	public boolean isAssignableTo(Type other);

	/**
	 * Return true if other type can be assigned to this variable
	 *
	 * @param other
	 *            Right hand side of the assignment
	 * @return a boolean.
	 */
	public boolean isAssignableFrom(VariableReference other);

	/**
	 * Return true if this variable can by assigned to a variable of other type
	 *
	 * @param other
	 *            Left hand side of the assignment
	 * @return a boolean.
	 */
	public boolean isAssignableTo(VariableReference other);

	/**
	 * Return type of this variable
	 *
	 * @return a {@link Type} object.
	 */
	public Type getType();

	/**
	 * Set type of this variable
	 *
	 * @param type
	 *            a {@link Type} object.
	 */
	public void setType(Type type);

	/**
	 * Return raw class of this variable
	 *
	 * @return a {@link Class} object.
	 */
	public Class<?> getVariableClass();

	/**
	 * Return raw class of this variable's component
	 *
	 * @return a {@link Class} object.
	 */
	public Class<?> getComponentClass();

	/**
	 * <p>
	 * getOriginalCode
	 * </p>
	 *
	 * @return the code this variable reference stems from or null if it was
	 *         generated.
	 */
	public String getOriginalCode();

	/**
	 * Set the code fragment that defined this variable reference if imported
	 * from an existing test case.
	 *
	 * @param code
	 *            The code fragment that defined this variable reference.
	 */
	public void setOriginalCode(String code);

	/**
	 * {@inheritDoc}
	 *
	 * Return string representation of the variable
	 */
	@Override
	public String toString();

	/**
	 * Return name for source code representation
	 *
	 * @return a {@link String} object.
	 */
	public String getName();

	/**
	 * <p>
	 * getAdditionalVariableReference
	 * </p>
	 *
	 * @return a {@link VariableReference} object.
	 */
	public VariableReference getAdditionalVariableReference();

	/**
	 * <p>
	 * setAdditionalVariableReference
	 * </p>
	 *
	 * @param var
	 *            a {@link VariableReference} object.
	 */
	public void setAdditionalVariableReference(VariableReference var);

	/**
	 * <p>
	 * replaceAdditionalVariableReference
	 * </p>
	 *
	 * @param var1
	 *            a {@link VariableReference} object.
	 * @param var2
	 *            a {@link VariableReference} object.
	 */
	public void replaceAdditionalVariableReference(VariableReference var1,
                                                   VariableReference var2);

	/**
	 * <p>
	 * changeClassLoader
	 * </p>
	 *
	 * @param loader
	 *            a {@link ClassLoader} object.
	 */
	public void changeClassLoader(ClassLoader loader);

	/**
	 * <p>
	 * getDefaultValue
	 * </p>
	 *
	 * @return a {@link Object} object.
	 */
	public Object getDefaultValue();

	/**
	 * <p>
	 * getDefaultValueString
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getDefaultValueString();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareTo(VariableReference other);

	/**
	 * <p>
	 * same
	 * </p>
	 * 
	 * @param r
	 *            a {@link VariableReference} object.
	 * @return a boolean.
	 */
	public boolean same(VariableReference r);
}
