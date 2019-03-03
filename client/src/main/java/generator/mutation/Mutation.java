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
package generator.mutation;

public class Mutation implements Comparable<Mutation> {

	private final int id;

	private final String className;

	private final String methodName;

	private final String mutationName;

//	private final BytecodeInstruction original;
//
//	private final InsnList mutation;
//
//	private final InsnList infection;

	private final int lineNo = -1;

	/**
	 * <p>
	 * Constructor for Mutation.
	 * </p>
	 * 
	 * @param className
	 *            a {@link String} object.
	 * @param methodName
	 *            a {@link String} object.
	 * @param mutationName
	 *            a {@link String} object.
	 * @param id
	 *            a int.
	 */
	public Mutation(String className, String methodName, String mutationName, int id) {
		this.className = className;
		this.methodName = methodName;
		this.mutationName = mutationName;
		this.id = id;
	}

	/**
	 * <p>
	 * Constructor for Mutation.
	 * </p>
	 *
	 * @param className
	 *            a {@link String} object.
	 * @param methodName
	 *            a {@link String} object.
	 * @param mutationName
	 *            a {@link String} object.
	 * @param id
	 *            a int.
	 */


	/**
	 * <p>
	 * Getter for the field <code>id</code>.
	 * </p>
	 *
	 * @return a int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * <p>
	 * Getter for the field <code>className</code>.
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * <p>
	 * Getter for the field <code>methodName</code>.
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getMethodName() {
		return methodName;
	}

	public int getLineNumber() {
		return lineNo;
	}

	/**
	 * <p>
	 * getOperandSize
	 * </p>
	 *
	 * @return a int.
	 */
	public int getOperandSize() {
		return 0;
	}

	/**
	 * <p>
	 * Getter for the field <code>mutationName</code>.
	 * </p>
	 *
	 * @return a {@link String} object.
	 */
	public String getMutationName() {
		return mutationName + " ("+id + "): " + ", line "; }


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Mutation " + id + ": " + className + "." + methodName + ":" + lineNo
		        + " - " + mutationName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + id;
		result = prime * result + lineNo;
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((mutationName == null) ? 0 : mutationName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mutation other = (Mutation) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (id != other.id)
			return false;
		if (lineNo != other.lineNo)
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (mutationName == null) {
			if (other.mutationName != null)
				return false;
		} else if (!mutationName.equals(other.mutationName))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Mutation o) {
		return lineNo - o.lineNo;
	}

}
