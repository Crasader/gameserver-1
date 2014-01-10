package com.xinqihd.sns.gameserver.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.util.IOUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class JavaCompiler {
	
	private static final Logger logger = LoggerFactory.getLogger(JavaCompiler.class);
	
	private static boolean isJavacReady = true;
	private static String classpath = null;
	private static String[] javacCommands = {"javac", "-cp", null, "-g", "-d", null, "-encoding", "utf8", 
		"-sourcepath", null, "-source", "1.6", "-target", "1.6", null};
	
	static {
		//Try to check if javac is ready to use.
		try {
			
			Process p = Runtime.getRuntime().exec("javac");
			InputStream is = p.getInputStream();
			byte[] buf = IOUtil.readStreamBytes(is);
			isJavacReady = true;
			String deployDataDir = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.deploy_data_dir);
			if ( deployDataDir == null ) {
				deployDataDir = "../deploy/data";
			}
			classpath = StringUtil.concat(deployDataDir, "/../babywarserver.jar", ":", deployDataDir, "/../bootstrap.jar");
		} catch ( Exception e ) {
			isJavacReady = false;
			logger.error("Failed to find javac");
		}
	}
	
	/**
	 * Compile the java source to destination dir.
	 * 
	 * @param source
	 * @param destDir Please make sure the dest dir exists before compiling
	 */
	public static boolean compile(File source, File destDir) {
		if ( !isJavacReady ) {
			return false;
		}
		try {
			javacCommands[2] = classpath;
			javacCommands[5] = destDir.getAbsolutePath();
			javacCommands[9] = source.getParent();
			javacCommands[14] = source.getAbsolutePath();
			Process p = Runtime.getRuntime().exec(javacCommands);
			int response = 0;
			try {
				response = p.waitFor();
			} catch (InterruptedException e) {
			}
			InputStream is = p.getErrorStream();
			byte[] buf = IOUtil.readStreamBytes(is);
			if ( response != 0 && buf != null ) {
				String errMsg = new String(buf, "utf8");
				if ( logger.isDebugEnabled() ) {
					logger.debug(errMsg);
				}
				return false;
			}
			if ( logger.isDebugEnabled() ) {
				logger.debug("Compile the " + source + " to " + destDir + " succeed. ");
			}
			return true;
		} catch (IOException e) {
			logger.error("Failed to compile source : " + source, e);
			e.printStackTrace();
		}
		return false;
	}

	public static void compileAllScript(File scriptDirFile, File descDirFile) {
		//Try to compile those scripts
		HashSet<ScriptHook> failedHook = new HashSet<ScriptHook>();
		for ( ScriptHook hook : ScriptHook.values() ) {
			if ( hook == ScriptHook.HELLO ) continue;
			if ( hook != ScriptHook.VERSION_CHECK ) continue;
			String sourceFileName = hook.getHook().replace('.', '/').concat(".java");
			File sourceFile = new File(scriptDirFile, sourceFileName);
			try {
				JavaCompiler.compile(sourceFile, descDirFile);
				logger.info("Compiling script: {} ", sourceFile.getAbsolutePath());
			} catch (Exception e) {
				logger.error(sourceFile+" failed to compile", e);
				failedHook.add(hook);
			}
		}
		if ( failedHook.size() > 0 ) {
			for ( ScriptHook hook : failedHook ) {
				logger.warn("Compiling error for script:{}", hook.getHook());
				if ( hook == ScriptHook.HELLO ) continue;
				String sourceFileName = hook.getHook().replace('.', '/').concat(".java");
				File sourceFile = new File(scriptDirFile, sourceFileName);
				try {
					JavaCompiler.compile(sourceFile, descDirFile);
					logger.info("Compiling script: {} ", sourceFile.getAbsolutePath());
				} catch (Exception e) {
					logger.error(sourceFile+" failed to compile", e);
					failedHook.add(hook);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		File srcfile = new File("src/main/script");
		File descfile = new File("target/classes");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, "../deploy");
		JavaCompiler.compileAllScript(srcfile, descfile);
	}
}
