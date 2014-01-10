package com.xinqihd.sns.gameserver.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.InflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class IOUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);
	
	private static final int BUFFER_LENGTH = 1024;
	
	private static final String SUF_JAR = ".jar";
	/**
	 * 
	 * @param urlOrFilePath It is maybe an URL or an ordinal file.
	 * @param extractDir
	 * @throws IOException
	 */
	public static final void loadClassFromUrl(String urlOrFilePath, String extractDir) {
		File tmpFile = new File(extractDir);
		if ( tmpFile.mkdirs() ) {
			logger.info("Create the class dir : {}", tmpFile );
		}
		
		boolean isUrl = false;
		if ( urlOrFilePath.indexOf("://") > 0 ) {
			isUrl = true;
		}

		if ( isUrl ) {
			//Extract the jar content to given classpath.
			try {
				URL url = new URL(urlOrFilePath);
				IOUtil.extractJAR(url, tmpFile.getAbsolutePath());
				logger.info("Extract url {} to tmp dir {}", urlOrFilePath, tmpFile);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				//Bootstrap.exit(e.getMessage(), -1);
			}
		} else {
			//Copy the dir to given classpath.
			File sourceFile = new File(urlOrFilePath);
			try {
				if ( !urlOrFilePath.endsWith(SUF_JAR) ) {
					if ( !sourceFile.equals(tmpFile) ) { 
						logger.info("Copy dir {} to tmp dir {}", urlOrFilePath, tmpFile);
						IOUtil.copyDir(sourceFile, tmpFile);
					} else {
						logger.info("The source and target dir are the same.");
					}
				} else {
					IOUtil.extractJAR(sourceFile, tmpFile.getAbsolutePath());
					logger.info("Extract file {} to tmp dir {}", urlOrFilePath, tmpFile);
				}
			} catch (IOException e) {
				logger.error("Failed to copy source dir", e);
			}
		}
	}

	/**
	 * Extract the jar file to given dir.
	 * @param jarFileName
	 * @param extractDir
	 */
	public static final void extractJAR(String jarFileName, String extractDir) throws IOException {
		File jarFile = new File(jarFileName);
		FileInputStream fis = new FileInputStream(jarFile);
		extractJAR(new File(jarFileName), extractDir);
	}
	
	/**
	 * Extract the jar file from given url to extract dir.
	 * @param jarUrl
	 * @param extractDir
	 */
	public static final void extractJAR(URL jarUrl, String extractDir) throws IOException {
		InputStream is = jarUrl.openStream();
		File zipFile = new File(System.getProperty("java.io.tmpdir"),
				parseFileNameFromString(jarUrl.getPath()));
		writeStreamToFile(is, zipFile.getAbsolutePath());
		extractJAR(zipFile, extractDir);
	}
	
	/**
	 * Extract the jar file content from given stream to the extract dir.
	 * @param jarStream
	 * @param extractDir
	 */
	public static final void extractJAR(File zFile, String extractDir) throws IOException {
		File extractFile = new File(extractDir);
		if ( !extractFile.exists() ) {
			extractFile.mkdirs();
		}
		if ( !zFile.exists() ) {
			throw new IOException(zFile + " does not exist.");
		}
		ZipFile zipFile = new ZipFile(zFile);
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			byte[] buf = new byte[BUFFER_LENGTH];
			while ( entries.hasMoreElements() ) {
				ZipEntry entry = entries.nextElement();
				String entryName = entry.getName();
				if ( entryName.startsWith("META-INF") ) {
					continue;
				}
				File entryFile = new File(extractDir, entryName);
				//Is a directory?
				if ( entry.isDirectory() ) {
					entryFile.mkdirs();
					continue;
				}
				//Make sure the dir path is ready
				entryFile.getParentFile().mkdirs();
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(entryFile);
					int len;
					InputStream is = zipFile.getInputStream(entry);
					while ( (len=is.read(buf, 0, BUFFER_LENGTH)) > -1 ) {
						fos.write(buf, 0, len);
					}
				} finally {
					fos.close();
				}
			}
		} finally {
			zipFile.close();
		}
	}
	
	/**
	 * Extract the jar file content from given stream to the extract dir.
	 * @param jarStream
	 * @param extractDir
	 */
	public static final void extractJAR(InputStream is, String extractDir) throws IOException {
		File extractFile = new File(extractDir);
		if ( !extractFile.exists() ) {
			extractFile.mkdirs();
		}
		if ( is == null ) {
			throw new IOException("InputStream does not exist.");
		}
		try {
			byte[] buf = new byte[BUFFER_LENGTH];
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry zipEntry = null;
			for ( zipEntry = zis.getNextEntry(); zipEntry!=null; zipEntry = zis.getNextEntry() ) {
				String entryName = zipEntry.getName();
				if ( entryName.startsWith("META-INF") ) {
					continue;
				}
				File entryFile = new File(extractDir, entryName);
				//Is a directory?
				if ( zipEntry.isDirectory() ) {
					entryFile.mkdirs();
					continue;
				}
				//Make sure the dir path is ready
				entryFile.getParentFile().mkdirs();
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(entryFile);
					int len = 0;
					while ( (len=is.read(buf, 0, BUFFER_LENGTH)) > -1 ) {
						fos.write(buf, 0, len);
					}
				} finally {
					fos.close();
					zis.closeEntry();
				}
			}
		} finally {
			is.close();
		}
	}
	
	/**
	 * Compress the string with ZIP method.
	 * @param content
	 * @return
	 */
	public static byte[] compressString(String entryName, String content) {
		try {
			ByteOutputStream bos = new ByteOutputStream(10000);
			ZipOutputStream zos = new ZipOutputStream(bos);
			ZipEntry entry = new ZipEntry(entryName);
			zos.putNextEntry(entry);
			zos.write(content.getBytes("utf8"));
			zos.flush();
			zos.close();
			byte[] fileBytes = new byte[bos.size()];
			System.arraycopy(bos.getBytes(), 0, fileBytes, 0, fileBytes.length);
			return fileBytes;
		} catch (IOException e) {
			logger.warn("Failed to zip string.", e);
		}
		return null;
	}
	
	public static byte[] compressStringZlib(String content) {
		try {
			ByteOutputStream bos = new ByteOutputStream(10000);
			DeflaterOutputStream zos = new DeflaterOutputStream(bos);
			zos.write(content.getBytes("utf8"));
			zos.flush();
			zos.close();
			byte[] fileBytes = new byte[bos.size()];
			System.arraycopy(bos.getBytes(), 0, fileBytes, 0, fileBytes.length);
			return fileBytes;
		} catch (IOException e) {
			logger.warn("Failed to zip string.", e);
		}
		return null;
	}
	
	public static String uncompressString(byte[] bytes) {
		try {
			ByteInputStream bis = new ByteInputStream(bytes, bytes.length);
			ZipInputStream zis = new ZipInputStream(bis);
			ZipEntry entry = zis.getNextEntry();
			int byteSize = 5000, len = 0;
			byte[] contentBytes = new byte[byteSize];
			ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
			if ( entry != null ) {
				while ( (len = zis.read(contentBytes)) != -1 ) {
					bufStream.write(contentBytes, 0, len);
				}
				String content = new String(bufStream.toByteArray(), "utf8");
				return content;
			}
		} catch (IOException e) {
			logger.warn("Failed to unzip string.", e);
		}
		return null;
	}
	
	public static String uncompressStringZlib(byte[] bytes) {
		try {
			ByteInputStream bis = new ByteInputStream(bytes, bytes.length);
			InflaterInputStream zis = new InflaterInputStream(bis);
			int byteSize = 5000, len = 0;
			byte[] contentBytes = new byte[byteSize];
			ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
			while ( (len = zis.read(contentBytes)) != -1 ) {
				bufStream.write(contentBytes, 0, len);
			}
			String content = new String(bufStream.toByteArray(), "utf8");
			return content;
		} catch (IOException e) {
			logger.warn("Failed to unzip string.", e);
		}
		return null;
	}
	
	/**
	 * Write a stream's content to a file.
	 * @param is
	 * @param fileName
	 * @throws IOException
	 */
	public static final void writeStreamToFile(InputStream is, String fileName) throws IOException {
		//Make sure the dir path is ready
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		FileOutputStream fos = null;
		byte[] buf = new byte[BUFFER_LENGTH];
		try {
			fos = new FileOutputStream(file);
			int len;
			while ( (len=is.read(buf, 0, BUFFER_LENGTH)) > -1 ) {
				fos.write(buf, 0, len);
			}
		} finally {
			if ( fos != null ) {
				fos.close();
			}
		}
	}
	
	/**
	 * Read the file raw content into byte array.
	 * @param fileName
	 * @return
	 */
	public static final byte[] readFileBytes(String fileName) throws IOException {
		File file = new File(fileName);
		return readFileBytes(file);
	}
	
	/**
	 * Read the file raw content into byte array.
	 * @param fileName
	 * @return
	 */
	public static final byte[] readFileBytes(File file) throws IOException {
		if ( file.exists() && file.isFile() ) {
			FileInputStream fis = new FileInputStream(file);
			int fileLen = (int)file.length();
			byte[] buf = new byte[fileLen];
			int len = 0;
			while ( len < fileLen ) {
				int n = fis.read(buf, len, fileLen-len);
				if ( n >= 0 ) {
					len += n;
				} else {
					break;
				}
			}
			return buf;
		} else {
			throw new IOException(file+" does not exist or is not file!");
		}
	}
	
	/**
	 * Read the stream raw content into byte array.
	 * @param fileName
	 * @return
	 */
	public static final byte[] readStreamBytes(InputStream is) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[BUFFER_LENGTH];
			int len = 0;
			while ( (len = is.read(buf, 0, BUFFER_LENGTH)) > -1  ) {
				baos.write(buf, 0, BUFFER_LENGTH);
			}
			return buf;
	}
	
	/**
	 * Get the file name from absolute path.
	 * @param absolutePath
	 * @return
	 */
	public static final String parseFileNameFromString(String absolutePath) {
		if ( absolutePath == null ) 
			return null;
		int index = absolutePath.lastIndexOf(File.separatorChar);
		if ( index > -1 ) {
			return absolutePath.substring(index);
		}
		return null;
	}
	
	/**
	 * Copy all contents from source dir to dest dir.
	 * @param sourceDir It must be a dir.
	 * @param destDir It must be a dir. It may not exist.
	 */
	public static final void copyDir(File sourceDir, File destDir) throws IOException {
		if ( sourceDir.equals(destDir) ) {
			return;
		}
		if ( !sourceDir.exists() ) {
			return;
		}
		if ( !destDir.exists() ) {
			destDir.mkdirs();
		}
		File[] files = sourceDir.listFiles();
		for ( File file : files ) {
			if ( file.isFile() ) {
				copyFile(file, destDir);
			} else {
				copyDir(file, new File(destDir, file.getName()));
			}
		}
	}
	
	/**
	 * Copy source file to dest dir.
	 * @param sourceFile The source file.
	 * @param destDir The destination dir, not a file.
	 */
	public static final void copyFile(File sourceFile, File destDir) throws IOException {
		boolean copy = false;
		File destFile = new File(destDir, sourceFile.getName());
		if ( !destFile.exists() ) {
			copy = true;
		} else if ( sourceFile.lastModified() > destFile.lastModified() ) {
			copy = true;
		}
		if ( copy ) {
			BufferedOutputStream bos = null;
			try {
				FileOutputStream fos = new FileOutputStream(destFile);
				bos = new BufferedOutputStream(fos);
				byte[] raw = readFileBytes(sourceFile.getAbsolutePath());
				bos.write(raw);
			} finally {
				bos.close();
				bos = null;
			}
		}
	}
	
}
