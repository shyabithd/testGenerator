package generator;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class Constructor {

    private List<ClassReader.Parameter> parameters;
    private ClassReader classReader;
    private String constructorName;

    public Constructor(String constructorName, List<ClassReader.Parameter> parameters, ClassReader classReader) {
        this.constructorName = constructorName;
        this.parameters = parameters;
        this.classReader = classReader;
    }

    public DataType[] getGenericParameterTypes() {
        ArrayList<DataType> dataTypes = new ArrayList<>();
        for (ClassReader.Parameter parameter : parameters) {
            dataTypes.add(parameter.type);
        }
        return dataTypes.toArray(new DataType[0]);
    }

    public DataType[] getParameterTypes() {
        return getGenericParameterTypes();
    }

    public ClassReader getClassReader() {
        return classReader;
    }

    public String getName() {
        return constructorName;
    }

    public int getModifiers() {
        return 1;
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public ClassReader.Parameter[] getParameters() {
        return parameters.toArray(new ClassReader.Parameter[0]);
    }
}
