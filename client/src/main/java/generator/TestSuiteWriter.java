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
import java.util.List;

public class TestSuiteWriter {

    private final Logger logger = LoggerFactory.getLogger(TestSuiteWriter.class);
    private List<TestCase> testCases = new ArrayList<>();

    public void insertTests(List<TestCase> tests) {
        testCases.addAll(tests);
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
                withTab = withTab.substring(0, withTab.length() - 1);
                testBody += header+withTab;
                testBody += "}" + System.lineSeparator() + System.lineSeparator();
            }
            writer.write(testBody);
            writer.close();
        } catch (IOException e) {
            logger.error("File "+absoluteFilePath+" failed to create");
        }
    }

    private void addHeaders(BufferedWriter bufferedWriter) throws IOException {
        String includes = "#include<"+ Properties.TARGET_CLASS+">\r\n";
        includes += "#include<gtest>\r\n\r\n";
        bufferedWriter.write(includes);
    }
}
