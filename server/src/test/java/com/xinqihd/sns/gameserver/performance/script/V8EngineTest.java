package com.xinqihd.sns.gameserver.performance.script;

import java.lang.reflect.InvocationTargetException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.codehaus.janino.ScriptEvaluator;
import org.junit.After;
import org.junit.Before;

import com.xinqihd.sns.gameserver.util.TestUtil;

public class V8EngineTest {

	private String javaSource = 
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
	
	private ScriptEngine eng = null;
	
	private String calcFunction2 = 
		  " function calc() { " 
		+ "   return Math.sin(Math.sqrt(Math.pow(num, 3) * Math.random()*100));" 
		+ " };";
	
	private String calcFunction = 
		  "Math.sin(Math.sqrt(Math.pow(3, 3) * Math.random()*100))" ;
	
	private String janinoScript = 
		  "return Math.sin(Math.sqrt(Math.pow(c, 3) * Math.random()*100));" ;

	public V8EngineTest() {
		// System.setProperty("java.library.path",
		// System.getProperty("user.dir").concat("/lib"));
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void testInvoke() throws Exception {
		eng = new ScriptEngineManager().getEngineByName("jav8");
		Compilable compiler = (Compilable) this.eng;
		CompiledScript script = compiler.compile(calcFunction);
		
		int max = 100000;
		Bindings binding = this.eng.getBindings(ScriptContext.GLOBAL_SCOPE);
		binding.put("num", 3);
		Object r = script.eval(binding);
		System.out.println(r);
		long startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			script.eval(binding);
		}
		long endM = System.currentTimeMillis();
		System.out.println(" V8 engine loop " + max + ":" + (endM-startM));
	}
	
	public void testInvoke2() throws Exception {
		eng = new ScriptEngineManager().getEngineByName("jav8");
  	Object r = this.eng.eval(calcFunction);	
  	System.out.println("invoke2: " + r);
		
		int max = 100000;
		System.out.println(r);
		long startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			this.eng.eval(calcFunction);	
		}
		long endM = System.currentTimeMillis();
		System.out.println(" V8 engine loop " + max + ":" + (endM-startM));
	}
	
	public void testCompiler() throws Exception {
		int loop = 100000;
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				Calc calc = new Calc();
				calc.calc(3);
			}
		}, "Pure java", loop);
		
		String className = "com.xinqihd.sns.gameserver.script.Calc";
		JavaCompilerTest.compileSourceCode(className, javaSource, "classes");
		TestUtil.doPerform(new Runnable() {
			public void run() {
				
				Function call = null;
				try {
					call = (Function)Class.forName("com.xinqihd.sns.gameserver.script.Calc").newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "JavaComiler", loop);
		
		//Janino
		final ScriptEvaluator se = new ScriptEvaluator();
		se.setReturnType(double.class);
		se.setParameters(new String[]{"c"}, new Class[]{double.class});
		se.cook(janinoScript);
		double r = (Double)se.evaluate(new Object[]{3d});

		TestUtil.doPerform(new Runnable() {
			public void run() {
				try {
					se.evaluate(new Object[]{3d});
				} catch (InvocationTargetException e) {
				}
			}
		}, "JaninoScript", loop);
		
//		final Context cx = Context.enter();
//		final Scriptable scope = cx.initStandardObjects();
//		Object result = cx.evaluateString(scope, calcFunction, "<calc>", 3, null);
//		
//		TestUtil.doPerform(new Runnable() {
//			public void run() {
//				cx.evaluateString(scope, calcFunction, "<calc>", 3, null);
//			}
//		}, "Rhino", loop);
	}

	
	private double calc(int num) {
		return Math.sin(Math.sqrt(Math.pow(num, 3) * Math.random()*100));
	}
}
