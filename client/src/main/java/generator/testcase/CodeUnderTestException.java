package generator.testcase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeUnderTestException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(CodeUnderTestException.class);
	
	/**
	 * <p>Constructor for CodeUnderTestException.</p>
	 *
	 * @param cause a {@link Throwable} object.
	 */
	public CodeUnderTestException(Throwable cause){
		super(cause);
	}

	/**
	 * Used by code calling VariableReference.setObject/2 and .getObject()/1
	 *
	 * @param e a {@link Throwable} object.
	 * @return only there to make the compiler happy, this method always throws an exception
	 * @throws IllegalAccessException if any.
	 * @throws IllegalArgumentException if any.
	 * @throws NullPointerException if any.
	 * @throws ExceptionInInitializerError if any.
	 * @throws AssertionError if e wasn't one of listed for types
	 */
	@Deprecated
	public static Error throwException(Throwable e) throws IllegalAccessException, IllegalArgumentException, NullPointerException, ExceptionInInitializerError{
		if(e instanceof CodeUnderTestException){
			e=e.getCause();
		}
		if(e instanceof IllegalAccessException){
			throw (IllegalAccessException)e;
		}else if(e instanceof IllegalArgumentException){
			throw (IllegalArgumentException)e;
		}else if(e instanceof NullPointerException){
			throw (NullPointerException)e;
		}else if(e instanceof ArrayIndexOutOfBoundsException){
			throw (ArrayIndexOutOfBoundsException)e;
		}else if(e instanceof ExceptionInInitializerError){
			throw (ExceptionInInitializerError)e;
		}else{
			logger.error("We expected the exception to be one of the listed but it was ", e);
			throw new AssertionError("We expected the exception to be one of the listed but it was " + e.getClass());
		}
	}
}
