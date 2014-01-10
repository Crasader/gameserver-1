package com.xinqihd.sns.gameserver.script;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.commons.compiler.CompileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The ScriptManager will provide a common managed interface
 * for different kinds of script language.
 * 
 * @author wangqi
 *
 */
public abstract class ScriptManager {

	protected static final Logger logger = LoggerFactory.getLogger(ScriptManager.class);
	
	//private static ScriptManager instance = new JaninoScriptManager();
	private static ScriptManager instance = null;
	
	public static final List<Object> EMPTY_LIST = new ArrayList<Object>();
	
	/**
	 * Get the system default ScriptManager.
	 * @return
	 */
	public static ScriptManager getInstance() {
		if (instance == null) {
			instance = new JaninoScriptManager();
		}
		return instance;
	}

	/**
	 * Run the specified hook with given context.
	 *  
	 * @param hook
	 * @param context
	 * @return
	 */
	public ScriptResult runScript(ScriptHook hook, Object ... parameters) {
		try {
			ScriptResult result = doRunScript(hook, parameters);
			return result;
		} catch (Throwable t ) {
			logger.warn("Script {} have exception: {}", hook, t.getMessage());
			logger.warn("Script exception: ", t);
			ScriptResult result = new ScriptResult();
			result.setType(ScriptResult.Type.SCRIPT_FAIL);
			result.setResult(EMPTY_LIST);
			result.setCause(t);
			return result;
		}
	}
	
	/**
	 * Run the specified hook with given context.
	 *  
	 * @param hook
	 * @param context
	 * @return
	 */
	public ScriptResult runScript(String hookStr, Object ... parameters) {
		try {
			ScriptResult result = doRunScript(hookStr, parameters);
			return result;
		} catch (Throwable t ) {
			logger.warn("Script {} have exception: {}", hookStr, t.getMessage());
			logger.warn("Script exception: ", t);
			ScriptResult result = new ScriptResult();
			result.setType(ScriptResult.Type.SCRIPT_FAIL);
			result.setResult(EMPTY_LIST);
			result.setCause(t);
			return result;
		}
	}
	
	/**
	 * A facility method to get the int value of a script.
	 * @param hook
	 * @param context
	 * @return
	 */
	public int runScriptForInt(ScriptHook hook, Object ...parameters) {
		ScriptResult result = runScript(hook, parameters);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			return (Integer)result.getResult().get(0);
		}
		return Integer.MIN_VALUE;
	}
	
	/**
	 * A facility method to get the Object value of a script.
	 * @param hook
	 * @param context
	 * @return
	 */
	public Object runScriptForObject(ScriptHook hook, Object ...parameters) {
		ScriptResult result = runScript(hook, parameters);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			return result.getResult().get(0);
		}
		return null;
	}
	
	/**
	 * A facility method to get the int value of a script.
	 * @param hook
	 * @param context
	 * @return
	 */
	public boolean runScriptForBoolean(ScriptHook hook, Object ... parameters ) {
		ScriptResult result = runScript(hook, parameters);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			return (Boolean)result.getResult().get(0);
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Check if the parameters are not null and at least have 'size' amount.
	 * 
	 * @param parameters
	 * @param size
	 * @return
	 */
	public static ScriptResult checkParameters(Object[] parameters, int size) {
		if ( parameters == null || parameters.length < size ) {
			ScriptResult result = new ScriptResult();
			result.setType(ScriptResult.Type.SCRIPT_FAIL);
			result.setResult(null);
			result.setError(StringUtil.concat("Parameter should be at least ", size));
			return result;
		}
		return null;
	}
	
	/**
	 * Force the underlying script engine reload a given script.
	 * @param hook
	 * @return
	 */
	public abstract boolean reloadScript(ScriptHook hook);
	
	/**
	 * Call the script object and get the result.
	 * 
	 * @param script
	 * @return
	 */
	protected abstract ScriptResult doRunScript(ScriptHook hook, Object ... parameters)
			throws SecurityException, NoSuchMethodException, 
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, 
			CompileException, ClassNotFoundException, IOException;
	
	/**
	 * Call the script object and get the result.
	 * 
	 * @param script
	 * @return
	 */
	protected abstract ScriptResult doRunScript(String script, Object ... parameters)
			throws SecurityException, NoSuchMethodException, 
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, 
			CompileException, ClassNotFoundException, IOException;

	/**
	 * 
	 * @param script
	 * @param sourceFile
	 * @return
	 * @throws CompileException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public abstract CompiledClass compileScript(String script, File sourceFile)
			throws CompileException, ClassNotFoundException, IOException;
	
	/**
	 * The compiled object wrapper
	 * @author wangqi
	 *
	 */
	public static class CompiledClass {
		long fileLastModified = -1l;
		long lastCheckTime = -1l;
		Class loadedClass = null;
	}
		
}
