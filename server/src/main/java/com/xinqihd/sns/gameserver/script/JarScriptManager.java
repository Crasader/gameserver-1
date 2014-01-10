package com.xinqihd.sns.gameserver.script;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.codehaus.commons.compiler.CompileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.bootstrap.ReloadClassLoader;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;

/**
 * This script manager check the script jar file and loaded all classes
 * @author wangqi
 *
 */
public class JarScriptManager extends ScriptManager {
	
	private static final Logger logger = LoggerFactory.getLogger(JarScriptManager.class);
	
	private static final String SCRIPT_METHOD = "func";
	
	private static final HashMap<String, CompiledClass> compiledMap = new HashMap<String, CompiledClass>();
	
	private static final IllegalArgumentException SCRIPT_NOT_FOUND = new IllegalArgumentException("Script not found");
	
	private static final int SCRIPT_CHECK_TIMEOUT = 10000;
	
	private static final Class ObjectArrayClass = (new Object[0]).getClass();
	
	private File scriptDirFile = null;
	
	private File cacheDirFile = null;
	
	private URL[] classpathURL = null;
	
	
	/**
	 * Construct the ScriptManager with given script dir.
	 */
	protected JarScriptManager() {
		String scriptDirPath = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_SCRIPT_DIR);
		if ( scriptDirPath == null ) {
			logger.warn("I don't find the script dir. Use 'src/main/script' as default.");
			this.scriptDirFile = new File("src/main/script");
		} else {
			this.scriptDirFile = new File(scriptDirPath);
//			String scriptCacheDir = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_TMP_DIR);
//			if ( scriptCacheDir == null ) {
//				scriptCacheDir = System.getProperty("java.io.tmpdir");
//				logger.info("script extracted dir: {}", scriptCacheDir);
//			} else {
//				logger.info("Use {} as script file path", scriptCacheDir);
//			}
//			File scriptCacheDirFile = new File(scriptCacheDir, "scripts");
		}
		String cacheDir = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_TMP_DIR);
		if ( cacheDir == null ) {
			cacheDir = System.getProperty("java.io.tmpdir");
		}
		this.cacheDirFile = new File(cacheDir, "script");
		try {
			this.classpathURL = new URL[]{this.scriptDirFile.toURL()};
		} catch (MalformedURLException e) {
			logger.warn("Failed to convert the scriptDir to URL", e);
		}
		logger.info("The script cache dir: {}", this.cacheDirFile );
		
	}


	/**
	 * Run the script and return the result.
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws CompileException 
	 * @throws ScanException 
	 */
	@Override
	protected ScriptResult doRunScript(ScriptHook hook, Object... parameters) 
			throws SecurityException, NoSuchMethodException, 
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, 
			ClassNotFoundException, IOException, CompileException {
		
		return doRunScript(hook.getHook(), parameters);
	}
	
	/**
	 * Run the script and return the result.
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws CompileException 
	 * @throws ScanException 
	 */
	@Override
	protected ScriptResult doRunScript(String script, Object... parameters) 
			throws SecurityException, NoSuchMethodException, 
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, 
			ClassNotFoundException, IOException, CompileException {
		
		boolean scriptNotFound = false;
		if ( script == null ) {
			logger.warn("Null script cannot run.");
			scriptNotFound = true;
		} else {
			CompiledClass compiledClass = compiledMap.get(script);
			if ( compiledClass == null ) {
				/**
				 * Check if the class is loaded by system
				 */
				String sourceFileName = script.replace('.', '/').concat(".java");
				File sourceFile = new File(scriptDirFile, sourceFileName);
				compiledClass = compileScript(script, sourceFile);
				compiledMap.put(script, compiledClass);
				logger.info("Script {} is loaded from classpath", script);
			} else {
				//Only check the script file when checking timeout.
				if ( compiledClass.lastCheckTime > 0 && 
						compiledClass.lastCheckTime + SCRIPT_CHECK_TIMEOUT < System.currentTimeMillis() ) {
					String sourceFileName = script.replace('.', '/').concat(".java");
					File sourceFile = new File(scriptDirFile, sourceFileName);
					if ( sourceFile.lastModified() > compiledClass.fileLastModified ) {
						logger.info("Script file {} is modified. Re-compile it.", sourceFile);
						compiledClass = compileScript(script, sourceFile);
					}
					compiledClass.lastCheckTime = System.currentTimeMillis();
				}
			}
			if ( compiledClass != null && compiledClass.loadedClass != null ) {
				Method method = compiledClass.loadedClass.getDeclaredMethod(SCRIPT_METHOD, ObjectArrayClass);
				ScriptResult result = (ScriptResult) method.invoke(compiledClass.loadedClass, new Object[]{parameters});
				return result;
			}
		}

		ScriptResult result = new ScriptResult();
		result.setType(Type.SCRIPT_FAIL);
		result.setResult(EMPTY_LIST);
		result.setCause(SCRIPT_NOT_FOUND);
		return result;
	}

	/**
	 * Force the underlying script engine reload a given script.
	 * @param hook
	 * @return
	 */
	public boolean reloadScript(ScriptHook hook) {
		if ( compiledMap.remove(hook) != null ) {
			return true;
		}
		return false;
	}
	
	/**
	 * Compiled the script file and return its class.
	 * 
	 * @param hook
	 * @param compiledClass
	 * @param sourceFile
	 * @return
	 * @throws CompileException
	 * @throws ScanException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Override
	public CompiledClass compileScript(String script,
			File sourceFile) throws CompileException,
			ClassNotFoundException, IOException {
		//load the class
		if ( sourceFile.exists() && sourceFile.canRead() ) {
			try {
				JavaClassLoader javaSourceLoader = 
						new JavaClassLoader(JarScriptManager.class.getClassLoader(), 
								this.cacheDirFile, this.classpathURL);
				
				CompiledClass compiledClass = new CompiledClass();
				compiledClass.fileLastModified = sourceFile.lastModified();
				compiledClass.loadedClass = javaSourceLoader.findClass(script);
				compiledClass.lastCheckTime = System.currentTimeMillis();
				
				compiledMap.put(script, compiledClass);
				
				logger.info("Load script {} from source path {}.", script, scriptDirFile.getAbsolutePath());
				return compiledClass;
			} catch (Exception e) {
				logger.warn("Failed to compile sourceFile {}, exception: ", sourceFile, e);
			}
		} else {
			String className = script;
			try {
				Class clazz = Class.forName(className);
				CompiledClass compiled = new CompiledClass();
				compiled.loadedClass = clazz;
				return compiled;
			} catch (Exception e) {
				logger.warn("Cannot find script file {}", sourceFile.getAbsolutePath());
			}
			//throw SCRIPT_NOT_FOUND;
		}
		return null;
	}
	
	public static void main(String args[]) throws Exception {

	}
}
