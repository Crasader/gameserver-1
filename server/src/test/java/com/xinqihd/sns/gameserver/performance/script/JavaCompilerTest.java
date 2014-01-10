package com.xinqihd.sns.gameserver.performance.script;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class JavaCompilerTest {
	
	private String sourceCode = 
			"package com.xinqihd.sns.gameserver.script;\n"+
					"\n"+
					"import java.util.concurrent.Callable;\n"+
					"\n"+
					"class Calc implements Callable<Double> {\n"+
					"\n"+
					"	@Override\n"+
					"	public Double call() throws Exception {\n"+
					"		return Math.sin(Math.sqrt(Math.pow(3, 3) * Math.random()*100));\n"+
					"	}\n"+
					"}\n";


	public void testCompiler() throws Exception {

		String className = "com.xinqihd.sns.gameserver.script.Calc";
		
		compileSourceCode(className, sourceCode, "classes");
		
		Callable call = (Callable)Class.forName("com.xinqihd.sns.gameserver.script.Calc").newInstance();
		System.out.println("calc: " + call.call());
	}
	
	/**
	 * Compile the java source code dynamically if it does not exists.
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public static Class compileSourceCode(String className, String sourceCode, String outDir) {
		
		//Try to find the class
		URL url = String.class.getResource("/"+className.replace('.', '/')+".class");
		
		if ( url != null ) {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
			}
		}
		
		/*Creating dynamic java source code file object*/
		SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject (className, sourceCode) ;
		JavaFileObject javaFileObjects[] = new JavaFileObject[]{fileObject} ;
		
		/* Prepare a list of compilation units (java source code file objects) to input to compilation task*/
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);
		
		/*Prepare any compilation options to be used during compilation*/
		//In this example, we are asking the compiler to place the output files under bin folder.
		/*
		  -g                         Generate all debugging info
		  -g:none                    Generate no debugging info
		  -g:{lines,vars,source}     Generate only some debugging info
		  -nowarn                    Generate no warnings
		  -verbose                   Output messages about what the compiler is doing
		  -deprecation               Output source locations where deprecated APIs are used
		  -classpath <path>          Specify where to find user class files and annotation processors
		  -cp <path>                 Specify where to find user class files and annotation processors
		  -sourcepath <path>         Specify where to find input source files
		  -bootclasspath <path>      Override location of bootstrap class files
		  -extdirs <dirs>            Override location of installed extensions
		  -endorseddirs <dirs>       Override location of endorsed standards path
		  -proc:{none,only}          Control whether annotation processing and/or compilation is done.
		  -processor <class1>[,<class2>,<class3>...] Names of the annotation processors to run; bypasses default discovery process
		  -processorpath <path>      Specify where to find annotation processors
		  -d <directory>             Specify where to place generated class files
		  -s <directory>             Specify where to place generated source files
		  -implicit:{none,class}     Specify whether or not to generate class files for implicitly referenced files
		  -encoding <encoding>       Specify character encoding used by source files
		  -source <release>          Provide source compatibility with specified release
		  -target <release>          Generate class files for specific VM version
		  -version                   Version information
		  -help                      Print a synopsis of standard options
		  -Akey[=value]              Options to pass to annotation processors
		  -X                         Print a synopsis of nonstandard options
		  -J<flag>                   Pass <flag> directly to the runtime system
		  -Werror                    Terminate compilation if warnings occur
		  @<filename>                Read options and filenames from file
		 */
		String[] compileOptions = new String[]{"-d", outDir, "-g", "-nowarn"} ;
		Iterable<String> compilationOptionss = Arrays.asList(compileOptions);
		
		/*Instantiating the java compiler*/
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		/**
		 * Retrieving the standard file manager from compiler object, which is used to provide
		 * basic building block for customizing how a compiler reads and writes to files.
		 *
		 * The same file manager can be reopened for another compiler task.
		 * Thus we reduce the overhead of scanning through file system and jar files each time
		 */
		StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);

		/*Create a diagnostic controller, which holds the compilation problems*/
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		/*Create a compilation task from compiler by passing in the required input objects prepared above*/
		CompilationTask compilerTask = compiler.getTask(null, stdFileManager, diagnostics, compilationOptionss, null, compilationUnits) ;

		//Perform the compilation by calling the call method on compilerTask object.
		boolean status = compilerTask.call();
		
		if (!status){//If compilation error occurs
	    /*Iterate through each compilation problem and print it*/
	    for (Diagnostic diagnostic : diagnostics.getDiagnostics()){
	        System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
	    }
		}
		
		/*
		 * If we need to compile another set of compilation units, just create another compilation 
		 * task by passing the new set of compilation units and execute the call method on it.
		 */
		
		//Finally close the fileManager instance to flush out anything that is there in the buffer.
		try {
      stdFileManager.close() ;//Close the file manager
		} catch (IOException e) {
		      e.printStackTrace();
		}
		
		try {
			return Class.forName("com.xinqihd.sns.gameserver.script.Calc");
		} catch (ClassNotFoundException e) {
		}
		return null;
	}
}
