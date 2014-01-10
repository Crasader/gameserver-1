package com.xinqihd.sns.gameserver.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.util.IOUtil;
import com.xinqihd.sns.gameserver.util.JavacUtil;

/**
 * This classloader will reload a jar file from url into system.
 * 
 * @author wangqi
 * 
 */
public class ReloadClassLoader extends ClassLoader {

	private static final Log log = LogFactory.getLog(ReloadClassLoader.class);
	
	private static final String SUF_JAR = ".jar";
	private static final String SUF_JAVA = ".java";
	private static final String SUF_CLZ = ".class";
	private static final String CLASS_DIR = System.getProperty("user.dir") + File.separator + "classes";

	// The hashset used to keep all classes loaded by this system.
	private HashMap<String, Class> classMap = new HashMap<String, Class>();
	
	private static ReloadClassLoader instance = null;
	
	private URL[] classpathURLs = null;

	/**
	 * Construct a classloader with given URL
	 * 
	 * @param urls
	 */
	private ReloadClassLoader(URL[] urls) {
		super();
		this.classpathURLs = urls;
		//Download all jars from url and put them into class path.
		for ( URL url : classpathURLs ) {
			String urlFile = url.getFile();
			IOUtil.loadClassFromUrl(urlFile, CLASS_DIR);
		}
	}

	/**
	 * @return the classpathURLs
	 */
	public URL[] getClasspathURLs() {
		return classpathURLs;
	}
	
	/**
	 * Add new classpath url
	 * @param url
	 */
	public void addClasspathURL(URL url) {
		if ( classpathURLs == null ) {
			this.classpathURLs = new URL[]{url};
		} else {
			URL[] tmp = new URL[classpathURLs.length+1];
			System.arraycopy(classpathURLs, 0, tmp, 0, classpathURLs.length);
			tmp[classpathURLs.length] = url;
			this.classpathURLs = tmp;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// Try to check if the class is already defined.
		Class clazz = classMap.get(name);
		if (clazz != null) {
			return clazz;
		}

		String filePath = name.replace('.', File.separatorChar);
		// Build objects pointing to the source code (.java) and object
		// code (.class)
		String javaFilename = filePath + SUF_JAVA;
		String classFilename = filePath + SUF_CLZ;
		
		File javaFile = new File(CLASS_DIR, javaFilename);
		File classFile = new File(CLASS_DIR, classFilename);
		
		// Check if the java file can be compiled.
		if ( 
				(javaFile.exists() && !classFile.exists()) || 
				(javaFile.exists() && classFile.exists() && javaFile.lastModified() > classFile.lastModified()) 
			) {
			JavacUtil.compile( javaFile.getAbsolutePath(), CLASS_DIR );
		} 
		if (classFile.exists()) {
			// Let's try to load up the raw bytes, assuming they were
			// properly compiled, or didn't need to be compiled
			try {
				// read the bytes
				byte raw[] = IOUtil.readFileBytes(classFile);
				// try to turn them into a class
				clazz = defineClass(name, raw, 0, raw.length);
			} catch (Exception ie) {
				// This is not a failure! If we reach here, it might
				// mean that we are dealing with a class in a library,
				// such as java.lang.Object
			}
		}

		// Try to load it from system classpath.
		if (clazz == null) {
			clazz = findSystemClass(name);
		}
		// If we still don't have a class, it's an error
		if (clazz == null)
			throw new ClassNotFoundException(name);

		// Otherwise, return the class
		classMap.put(name, clazz);
		return clazz;

	}
	
	/**
	 * Clean and reload all the classes.
	 */
	public synchronized void reload() {
		classMap.clear();
	}
	
	/**
	 * Create a new ReloadClassloader instance
	 * @param urls
	 * @return
	 */
	public static ReloadClassLoader newClassloader(URL... urls) {
		instance = new ReloadClassLoader(urls);
		return instance;
	}
	
	/**
	 * Get the current active ReloadClassLoader.
	 * @return
	 */
	public static ReloadClassLoader currentClassLoader() {
		return instance;
	}

}
