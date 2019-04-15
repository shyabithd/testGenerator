package generator;

public class TestGenerationContext {

    private static TestGenerationContext testGenerationContext = new TestGenerationContext();

    private ClassReader classReader;

    public static TestGenerationContext getInstance() {
        return testGenerationContext;
    }

    public void setClassReader(ClassReader classReader) {
        this.classReader = classReader;
    }

    public ClassReader getClassReader() { return classReader; }
}
