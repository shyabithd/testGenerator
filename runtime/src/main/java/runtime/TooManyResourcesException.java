package runtime;

public class TooManyResourcesException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public TooManyResourcesException(){
        super();
    }

    public TooManyResourcesException(String msg){
        super(msg);
    }
}
