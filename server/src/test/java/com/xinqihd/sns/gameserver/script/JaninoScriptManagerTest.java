package com.xinqihd.sns.gameserver.script;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class JaninoScriptManagerTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNormalCall() {
		final JaninoScriptManager manager = new JaninoScriptManager();
		ScriptResult result = manager.runScript(ScriptHook.TEST, 1, 2);
		
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		
		assertEquals(new Double(nativeCall(1, 2)), (Double)result.getResult().get(0));
	}
	
	@Test
	public void testReload() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/test/config");
		File helloFile = new File("src/test/config/script", "Hello.java");
		helloFile.getParentFile().mkdirs();
//		assertTrue(helloFile.exists());
		
		String helloScript = 
		"package script;\n"+
		"import java.util.*;\n"+
		"\n"+
		"import com.xinqihd.sns.gameserver.script.ScriptResult;\n"+
		"import com.xinqihd.sns.gameserver.script.ScriptHook;\n"+
		"\n"+
		"public class Hello {\n"+
    "\n"+
		"	public static ScriptResult func(Object[] context) {\n"+
    "   List list= new ArrayList(); \n"+
		"   list.add(\"hello\"); \n"+
		"   ScriptResult result = new ScriptResult(); \n"+
		"   result.setType(ScriptResult.Type.SUCCESS_RETURN); \n"+
		"   result.setResult(list); \n"+
		"   return result; \n"+
    "   \n"+
    " }\n"+
		"}\n";
		
		String worldScript =
		"package script;\n"+
		"import java.util.*;\n"+
		"\n"+
		"import com.xinqihd.sns.gameserver.script.ScriptResult;\n"+
		"\n"+
		"public class Hello {\n"+
    "\n"+
		"	public static ScriptResult func(Object[] context) {\n"+
		"   List list= new ArrayList(); \n"+
		"   list.add(\"world\"); \n"+
		"   ScriptResult result = new ScriptResult(); \n"+
		"   result.setType(ScriptResult.Type.SUCCESS_RETURN); \n"+
		"   result.setResult(list); \n"+
		"   return result; \n"+
    "   \n"+
    " }\n"+
		"}\n";
		
		FileOutputStream fos = new FileOutputStream(helloFile);
		fos.write(helloScript.getBytes());
		fos.close();
		
		final JaninoScriptManager manager = new JaninoScriptManager();
		ScriptResult result1 = manager.runScript(ScriptHook.HELLO, new HashMap());
		String str1 = (String)result1.getResult().get(0);
		
		//Modify the file.
		fos = new FileOutputStream(helloFile);
		fos.write(worldScript.getBytes());
		fos.close();
		
		manager.reloadScript(ScriptHook.HELLO);
		
		ScriptResult result2 = manager.runScript(ScriptHook.HELLO, new HashMap());
		String str2 = (String)result2.getResult().get(0);
		
		assertEquals("hello", str1);
		assertEquals("world", str2);
	}
	
	/**
	 * Old method:
	 *  Run Script for 1000000. Time:4063, Heap:4.0372314M
   *  Run Java   for 1000000. Time: 757, Heap:      0.0M
   * 
   * New method:
	 *  Run Script for 1000000. Time:2531, Heap:13.715843M
	 *  Run Java   for 1000000. Time: 751, Heap:0.33260345M
	 *  Run Simple Compiler for 1000000. Time:1606, Heap:16.03363M
   * 
   * Use cacheJavaSourceClassLoader
	 *  Run Script for 1000000. Time:3611, Heap:5.4864426M
	 *  Run Java   for 1000000. Time: 336, Heap:0.0M
   * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testStress() throws Exception {
		final Random r = new Random();
		int max = 1000000;
		
		final ScriptManager manager = ScriptManager.getInstance();
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				ScriptResult result = manager.runScript(ScriptHook.TEST, 
						r.nextInt(100), r.nextInt(100));
			}
		}, "Script", max);
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				nativeCall(
						r.nextInt(100), r.nextInt(100));
			}
		}, "Java", max);
		
	}
	
	@Test
	public void testAllScript() throws Exception {
		File scriptDirFile = new File("src/main/script");
		int error = 0;
		File targetDir = new File("target/classes/script");
		//FileUtils.deleteDirectory(targetDir);
		for ( ScriptHook hook : ScriptHook.values() ) {
			if ( hook == ScriptHook.HELLO ) continue;
			String sourceFileName = hook.getHook().replace('.', '/').concat(".java");
			File sourceFile = new File(scriptDirFile, sourceFileName);
			try {
				((JaninoScriptManager)JaninoScriptManager.getInstance()).
					compileScript(hook.getHook(), sourceFile);
			} catch (Exception e) {
				error++;
				System.out.println(sourceFile+" failed to compile");
				e.printStackTrace();
			}
		}
		assertEquals(0, error);
	}

	/**
	 * The java native method call. 
	 * @param a
	 * @param b
	 * @return
	 */
	private double nativeCall(int a, int b) {
		double c = Math.sqrt(Math.pow(a, b))*Math.sin(a+1.0)*Math.cos(b+1.0);
		return c;
	}
}
