package generator;

public class DataType
{
    public DataType(String dataType, ClassReader classReader) {
        this.dataType = dataType;
        this.classReader = classReader;
    }
    public boolean isPrimitive() {
        if(!dataType.equals("class"))
            return true;
        else
            return false;
    }
    public ClassReader getClassReader() {
        return classReader;
    }
    public String getDataType() {
        return this.dataType;
    }

    private ClassReader classReader;
    private String dataType;

    public String toString() {
        return dataType;
    }
}
