package generator;

import generator.classpath.ClassPathHandler;
import generator.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSuiteWriter {

    private final Logger logger = LoggerFactory.getLogger(TestSuiteWriter.class);
    private List<TestCase> testCases = new ArrayList<>();

    public void insertTests(List<TestCase> tests) {
        testCases.addAll(tests);
    }

    /**
     * Generate a random asserts
     */
    private String generateAsserts(TestCase testCase) {
        ClassReader classReader = Properties.getTargetClassRegression(true);
        String nativeClass = classReader.getNativeClass();
        String mainMethod = Properties.mainMethodWithoutBraces.concat(System.lineSeparator()).concat(testCase.toJavaCode()).concat("}");
        nativeClass = nativeClass.replace(Properties.mainMethod, mainMethod.replaceAll(System.lineSeparator(), System.lineSeparator()+"\t\t"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(classReader.getDefinedclassName() + "Clzz.java"))) {
            writer.flush();
            writer.write(nativeClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> result = Properties.executeCommand(Properties.nativeClassExec);
        return convertToAsserts(result, new ArrayList<>(Arrays.asList(classReader.getDeclaredMethods())), testCase);
    }

    private String convertToAsserts(ArrayList<String> result, ArrayList<ClassReader.Method> methods, TestCase testCase) {

        String code = testCase.toCode();
        for(ClassReader.Method method : methods) {
            String methodName = "####".concat(method.getName()).concat("__Val__");
            for(String resultLine : result) {
                if (resultLine.contains(methodName.substring(4))) {
                    String val = resultLine.substring(methodName.length() - 4);
                    for(String line : code.split(System.lineSeparator())) {
                        if(line.contains(Properties.printCommand) && line.contains(methodName)) {
                            String currentLine = line;
                            line = line.replace(Properties.printCommand, "ASSERT_EQ");
                            line = line.replace("+", ",");
                            if(method.getReturnType().getDataType().equals("string")) {
                                line = line.replace("\"".concat(methodName).concat("\""), "\"".concat(val).concat("\""));
                            } else {
                                line = line.replace("\"".concat(methodName).concat("\""), val);
                            }
                            code = code.replace(currentLine, line);
                            break;
                        }
                    }
                }
            }
        }
        return code;
    }

    public void writeTestSuite(String fileName, String dirPath) {
        String absoluteFilePath = dirPath+ File.separator+fileName+".cpp";
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(absoluteFilePath));
            addHeaders(writer);
            logger.debug(absoluteFilePath+" File Created");
            String testBody = "";
            for (int i = 0; i < testCases.size(); i++)  {
                String header = "TEST(SuiteTest, "+ "test"+ i + ") {" + System.lineSeparator() + System.lineSeparator();
                String withTab = "\t"+testCases.get(i).toCode().replaceAll(System.lineSeparator(), System.lineSeparator()+"\t");
                if(withTab.contains(Properties.printCommand)) {
                    withTab = generateAsserts(testCases.get(i));
                }
                withTab = withTab.substring(0, withTab.length() - 1);
                testBody += header+withTab;
                testBody += "}" + System.lineSeparator() + System.lineSeparator();
            }
            testBody += "int main(int argc, char** argv) {" + System.lineSeparator();
            testBody += "\t\t" + "testing::InitGoogleTest(&argc, argv);"+ System.lineSeparator();
            testBody += "\t\t" + "return RUN_ALL_TESTS();" + System.lineSeparator() + "}" + System.lineSeparator();

            writer.write(testBody);
            writer.close();
        } catch (IOException e) {
            logger.error("File "+absoluteFilePath+" failed to create");
        }
    }

    private void addHeaders(BufferedWriter bufferedWriter) throws IOException {
        String includes = "#include \""+ Properties.TARGET_CLASS+"\"\r\n";
        includes += "#include \"gtest/gtest.h\"\r\n\r\n";
        bufferedWriter.write(includes);
    }
}
