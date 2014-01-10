package com.xinqihd.sns.gameserver.admin.util;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.jdesktop.swingx.util.GraphicsUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.MissingIcon;
import com.xinqihd.sns.gameserver.util.IOUtil;

/**
 * This is the common image utility 
 * @author wangqi
 *
 */
public class ImageUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);
	
	public static final String ASSETS_ICONS_DIR = "http://static.babywar.xinqihd.com/assets/icons/";
	
	public static final String TMP_FILE_PATH = System.getProperty("java.io.tmpdir");
	
	public static final File TMP_ASSETS_ICONS_FILE = new File(TMP_FILE_PATH, "icons");
	
	private static final String IMG_DIR = "/img/";
	
	private static final String ICON_DIR = "/icon/";
	
	private static final String SMALL_ICON_DIR = "/smallicon/";

	/**
	 * Create an ImageIcon from internal resources by name.
	 * @param imageName
	 * @return
	 */
	public static final Icon createImageIcon(String imageName, String desc) {
		return createImageIconFromPath(ICON_DIR.concat(imageName), desc);
	}
	
	public static final Icon createImageSmallIcon(String imageName, String desc) {
		return createImageIconFromPath(SMALL_ICON_DIR.concat(imageName), desc);
	}
	
	public static final Icon createImageIconFromImg(String imageName, String desc) {
		return createImageIconFromPath(IMG_DIR.concat(imageName), desc);
	}
	
	public static final Icon createImageIconFromAssets(String imageName, int maxHeight) {
		File cacheFile = new File(TMP_ASSETS_ICONS_FILE, imageName);
		logger.debug("icon: {}", imageName);
		try {
			if ( !cacheFile.exists() ) {
				downloadImageToFile(cacheFile, ASSETS_ICONS_DIR.concat(imageName));
			}
			FileInputStream fis = new FileInputStream(cacheFile);
			BufferedImage image = ImageIO.read(fis);
			int height = image.getHeight();
			if ( height > maxHeight ) {
				image = GraphicsUtilities.createThumbnail(image, maxHeight);
			}
			ImageIO.write(image, "png", cacheFile);
			ImageIcon icon = new ImageIcon(image);
			return icon;				
		} catch (IOException e) {
			logger.warn("Failed to read: {}", cacheFile.getAbsolutePath());
			logger.error("Exception", e);
			downloadImageToFile(cacheFile, ASSETS_ICONS_DIR.concat(imageName));
		}
		MissingIcon imageIcon = new MissingIcon();
		return imageIcon;
	}
	
	public static final Icon createImageIconFromPath(String imagePath, String desc) {
		logger.debug("icon path: {}", imagePath);
		try {
			InputStream imgIs = ImageUtil.class.getResourceAsStream(imagePath);
			if ( imgIs != null ) {
				InputStream is = new BufferedInputStream(imgIs);
				byte[] bytes = IOUtils.toByteArray(is);
				ImageIcon imageIcon = new ImageIcon(bytes, desc);
				return imageIcon;
			}
		} catch (Exception e) {
			logger.error("Failed to load imageIcon by name: {}. Exception:{}", imagePath, e.getMessage());
		}
		MissingIcon imageIcon = new MissingIcon();
		return imageIcon;
	}
	
	private static final void downloadImageToFile(File file, String urlStr) {
		try {
			URL url = new URL(urlStr);
			InputStream is = url.openStream();
			IOUtil.writeStreamToFile(is, file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
