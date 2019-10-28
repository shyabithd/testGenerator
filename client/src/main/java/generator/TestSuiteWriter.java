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
        ArrayList<String> result = Properties.executeCommand(Properties.nativeClassExec +
                Properties.getTargetClassRegression(true).getConstructors().get(0).getName().concat("Clzz.java -exec"));
        return convertToAsserts(result, new ArrayList<>(Arrays.asList(classReader.getDeclaredMethods())), testCase);
    }

    private String convertToAsserts(ArrayList<String> result, ArrayList<ClassReader.Method> methods, TestCase testCase) {

        String code = testCase.toCode();
        for(String line : code.split(System.lineSeparator())) {
            String currentLine = line;
            if(line.contains(Properties.printCommand)) {
                String val = result.get(0).substring(result.get(0).indexOf(" ")+1);
                result.remove(0);
                line = line.replace(Properties.printCommand, "ASSERT_EQ");
                line = line.replace("+", ",");
                line = line.replace(line.substring(line.indexOf("(")+1, line.lastIndexOf("\"")+1), val);
                code = code.replace(currentLine, line);
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
