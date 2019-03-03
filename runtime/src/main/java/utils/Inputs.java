package utils;

public class Inputs {

    public static void checkNull(Object... inputs) throws IllegalArgumentException{
        if(inputs == null){
            throw new IllegalArgumentException("No inputs to check");
        }
        for(int i=0; i<inputs.length; i++){
            Object obj = inputs[i];
            if(obj==null){
                throw new IllegalArgumentException("Null input in position "+i);
            }
        }
    }
}
