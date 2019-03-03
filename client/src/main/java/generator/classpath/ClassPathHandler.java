package generator.classpath;

import generator.Properties;
import generator.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Scanner;

public class ClassPathHandler {

	private static Logger logger = LoggerFactory.getLogger(ClassPathHandler.class);

	private static final ClassPathHandler singleton = new ClassPathHandler();


	/**
	 * The classpath of the project to test, including all
	 * its dependencies
	 */
	private String targetClassPath;
	
	/**
	 * When we start the client, we need to know what is the classpath of EvoSuite.
	 * Problem is, if EvoSuite was called from Eclipse/Maven, when the classpath will
	 * not be part of the system classpath. And might not be feasible to query the
	 * current classloader to get such info, as we cannot make any assumption on its implementation.
	 * So, in those cases, we need to manually specify it
	 */
	private String evosuiteClassPath;


	private ClassPathHandler(){		
	}
	
	public static ClassPathHandler getInstance(){
		return singleton;
	}
	
	public static void resetSingleton(){
		getInstance().targetClassPath = null;
		getInstance().evosuiteClassPath = null;
	}

	public String getEvoSuiteClassPath(){
		if(evosuiteClassPath==null){
			evosuiteClassPath = System.getProperty("java.class.path");
		}
		
		return evosuiteClassPath;
	}
	
	/**
	 * Replace current CP of EvoSuite with the given <code>elements</code> 
	 * @param elements
	 * @throws IllegalArgumentException if values in <code>elements</code> are not valid classpath entries
	 */
	public void setEvoSuiteClassPath(String[] elements) throws IllegalArgumentException{
		String cp = getClassPath(elements);
		evosuiteClassPath = cp;
	}
	
	
	/**
	 * Replace current CP for target project with the given <code>elements</code> 
	 * @param elements
	 * @throws IllegalArgumentException if values in <code>elements</code> are not valid classpath entries
	 */
	public void changeTargetClassPath(String[] elements) throws IllegalArgumentException{
		String cp = getClassPath(elements);
		Properties.CP = cp;
		targetClassPath = cp;
	}

	private String getClassPath(String[] elements) {
		if(elements==null || elements.length==0){
			throw new IllegalArgumentException("No classpath elements");
		}

		String cp = "";
		boolean first = true;
		for (String entry : elements) {
			checkIfValidClasspathEntry(entry);
			if (first) {
				first = false;
			} else {
				cp += File.pathSeparator;
			}
			cp += entry;
		}
		return cp;
	}
	
	/**
	 * Return the classpath of the target project.
	 * This should include also all the third-party jars
	 * it depends on
	 * 
	 *  <p>
	 *  If no classpath has been set so far, the one from the property file
	 *  will be used, if it exists.
	 * 
	 * @return
	 */
	public String getTargetProjectClasspath() {

		if (targetClassPath == null) {
			String line = null;
			if (Properties.CP_FILE_PATH != null) {
				File file = new File(Properties.CP_FILE_PATH);

				try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
					Scanner scanner = new Scanner(in);
					line = scanner.nextLine();
				} catch (Exception e) {
					LoggingUtils.getGeneratorLogger().error("Error while processing " + file.getAbsolutePath() + " : " + e.getMessage());
				}
			}

			targetClassPath = line!=null ? line : Properties.CP;
		}
		
		return targetClassPath;
	}

	public static String writeClasspathToFile(String classpath) {

		try {
			File file = File.createTempFile("EvoSuite_classpathFile",".txt");
			file.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			String line = classpath;
			out.write(line);
			out.newLine();
			out.close();

			return file.getAbsolutePath();

		} catch (Exception e) {
			throw new IllegalStateException("Failed to create tmp file for classpath specification: "+e.getMessage());
		}
	}


	/**
	 * Add classpath entry to the classpath of the target project
	 * 
	 * @param element
	 * @throws IllegalArgumentException
	 */
	public void addElementToTargetProjectClassPath(String element) throws IllegalArgumentException{
		checkIfValidClasspathEntry(element);
		
		getTargetProjectClasspath(); //need to be sure it is initialized
		if(targetClassPath==null || targetClassPath.isEmpty()){
			targetClassPath = element;
		} else {
			
			if(targetClassPath.contains(element)){
				return; //already there, nothing to add
			}
			
			targetClassPath += File.pathSeparator + element;
			Properties.CP = targetClassPath;
		}
	}

	private void checkIfValidClasspathEntry(String element) throws IllegalArgumentException{
		if(element==null || element.isEmpty()){
			throw new IllegalArgumentException("Empty input element");
		}
		
		File file = new File(element);
		if(!file.exists()){
			throw new IllegalArgumentException("Classpath element does not exist on disk at: "+element);
		}
		if(!element.endsWith(".jar") && !file.isDirectory()){
			throw new IllegalArgumentException("A classpath element should either be a jar or a folder: "+element);
		}
	}
	
	/**
	 * Get the project classpath as an array of elements
	 * 
	 * @return a non-null array
	 */
	public String[] getClassPathElementsForTargetProject(){
		String cp = getTargetProjectClasspath();
		if(cp==null){
			return new String[0];
		}
		return cp.split(File.pathSeparator);
	}
	
	/**
	 * This is meant only for running the EvoSuite test cases, whose CUTs will be in the
	 * classpath of EvoSuite itself
	 */
	public void changeTargetCPtoTheSameAsEvoSuite(){
		
		File outDir = new File("target"+File.separator+"classes");
		if(outDir.exists()){
			changeTargetClassPath(new String[]{outDir.getAbsolutePath()});
			
			File testDir = new File("target"+File.separator+"test-classes");
			if(testDir.exists()){
				addElementToTargetProjectClassPath(testDir.getAbsolutePath());
			}
		} else {
			//TODO: just in case... not sure it would work properly
			changeTargetClassPath(getEvoSuiteClassPath().split(File.pathSeparator));
		}
	}
}