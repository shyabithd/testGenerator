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
package generator.result;

import java.io.Serializable;
import java.util.Arrays;

public class Failure implements Serializable {

	private static final long serialVersionUID = -6308624160029658643L;

	private String className;
	
	private String methodName;
	
	private String exceptionName;

	private String exceptionMessage;

	private StackTraceElement[] stackTrace;

	private int lineNo;
	
	public Failure() {

	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getExceptionName() {
		return exceptionName;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	public int getLineNo() {
		return lineNo;
	}

	@Override
	public String toString() {
		return "Failure [className=" + className + ", methodName=" + methodName
				+ ", exceptionName=" + exceptionName + ", exceptionMessage="
				+ exceptionMessage + ", stackTrace="
				+ Arrays.toString(stackTrace) + ", lineNo=" + lineNo + "]";
	}
	
	
}
