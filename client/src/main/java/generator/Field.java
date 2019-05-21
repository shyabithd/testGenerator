package generator;

public class Field
{
    private String fieldName;

    public Field(String fieldName, String type, ClassReader classReader) {
        this.fieldName = fieldName;
        this.dataType = new DataType(type, classReader);
        this.classReader = classReader;
    }

    public int getModifiers()
    {
        return 1;
    }

    public String getName() {
        return fieldName;
    }

    public DataType getType() {
        return dataType;
    }

    public DataType getGenericType() {
        return dataType;
    }

    public String toGenericString() {
        return fieldName;
    }

    public ClassReader getDeclaringClass() {
        return classReader;
    }
    private DataType dataType;
    private ClassReader classReader;
}
