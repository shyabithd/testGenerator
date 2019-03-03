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
