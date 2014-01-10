package com.xinqihd.sns.gameserver.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JavacUtil {
	
	private static final Log log = LogFactory.getLog(JavacUtil.class);
	
	private static boolean isJavacReady = true;
	private static String[] javacCommands = {"javac", "-cp", ".", "-g", "", "-d", ""};
	
	static {
		//Try to check if javac is ready to use.
		try {
			Process p = Runtime.getRuntime().exec("javac");
			InputStream is = p.getInputStream();
			byte[] buf = IOUtil.readStreamBytes(is);
			isJavacReady = true;
		} catch ( Exception e ) {
			isJavacReady = false;
			log.error("Failed to find javac");
		}
	}
	
	/**
	 * Compile the java source to destination dir.
	 * 
	 * @param source
	 * @param destDir Please make sure the dest dir exists before compiling
	 */
	public static boolean compile(String source, String destDir) {
		if ( !isJavacReady ) {
			return false;
		}
		try {
			javacCommands[4] = source;
			javacCommands[6] = destDir;
			Process p = Runtime.getRuntime().exec(javacCommands);
			int response = 0;
			try {
				response = p.waitFor();
			} catch (InterruptedException e) {
			}
			InputStream is = p.getErrorStream();
			byte[] buf = IOUtil.readStreamBytes(is);
			if ( response != 0 && buf != null ) {
				String errMsg = new String(buf);
				if ( log.isDebugEnabled() ) {
					log.debug(errMsg);
				}
				return false;
			}
			if ( log.isDebugEnabled() ) {
				log.debug("Compile the " + source + " to " + destDir + " succeed. ");
			}
			return true;
		} catch (IOException e) {
			log.error("Failed to compile source : " + source, e);
			e.printStackTrace();
		}
		return false;
	}

}
