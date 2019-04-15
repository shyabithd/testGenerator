package generator;

import generator.utils.Randomness;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to get private fields/methods to construct statements in the generated tests
 *
 * Created by Andrea Arcuri on 22/02/15.
 */
public class ReflectionFactory {

    private final ClassReader target;
    private final List<ClassReader.Field> fields;
    private final List<ClassReader.Method> methods;


    public ReflectionFactory(ClassReader target) throws IllegalArgumentException{
        this.target = target;
        if(target==null){
            throw new IllegalArgumentException("Target class cannot be null");
        }

        fields = new ArrayList<>();
        methods = new ArrayList<>();

        for(ClassReader.Method m : Reflection.getDeclaredMethods(target)){
            //if(Modifier.isPrivate(m.getModifiers()) && !m.isBridge() && !m.isSynthetic()){
                //only interested in private methods, as the others can be called directly
                methods.add(m);
           // }
        }

        //do not use reflection on JEE injection
        List<Field> toSkip = null;
        if(Properties.JEE){
            //toSkip = Injector.getAllFieldsToInject(target);
        }

        for(ClassReader.Field f : Reflection.getDeclaredFields(target)){
            if(Modifier.isPrivate(f.getModifiers())
                    && (toSkip==null || ! toSkip.contains(f))
                    && !f.getName().equals("serialVersionUID")
                    // read/writeObject must not be invoked directly, otherwise it raises a java.io.NotActiveException
                    && !f.getName().equals("writeObject")
                    && !f.getName().equals("readObject")
                    // final primitives cannot be changed
                    && !(Modifier.isFinal(f.getModifiers()) && f.getType().isPrimitive())
                    // changing final strings also doesn't make much sense
                    && !(Modifier.isFinal(f.getModifiers()) && f.getType().equals(String.class))
                    //static fields lead to just too many problems... although this could be set as a parameter
                    && !Modifier.isStatic(f.getModifiers())
                    ) {
                fields.add(f);
            }
        }
    }

    public int getNumberOfUsableFields(){
        return fields.size();
    }

    public boolean hasPrivateFieldsOrMethods(){
        return  !(fields.isEmpty() && methods.isEmpty());
    }

    public boolean nextUseField(){
        if(fields.isEmpty()){
            return false;
        }
        if(methods.isEmpty()){
            assert !fields.isEmpty();
            return true;
        }

        assert !fields.isEmpty() && !methods.isEmpty();

        int tot = fields.size() + methods.size();
        double ratio = (double)fields.size() / (double) tot;

        return Randomness.nextDouble() <= ratio;
    }

    public ClassReader.Field nextField() throws IllegalStateException{
        if(fields.isEmpty()){
            throw new IllegalStateException("No private field");
        }
        return Randomness.choice(fields);
    }

    public ClassReader.Method nextMethod()  throws IllegalStateException{
        if(methods.isEmpty()){
            throw new IllegalStateException("No private method");
        }
        return Randomness.choice(methods);
    }

    public ClassReader getReflectedClass(){
        return target;
    }
}

