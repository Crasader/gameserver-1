package com.xinqihd.sns.gameserver.battle;

import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.MapPojo;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.geom.BitSetImage;
import com.xinqihd.sns.gameserver.geom.BitmapUtil;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Load the map and bullet geometry data into system.
 * @author wangqi
 *
 */
public class BattleDataLoader4Bitmap {

	private static final Logger logger = LoggerFactory.getLogger(BattleDataLoader4Bitmap.class);
	
	private static final String DAT = ".dat";
	
	private static final String MAP = "map";
	
	private static final String BULLET = "bullet";
	
	private static int counter = 0;
	
	private static boolean alreadyLoadBattleMap = false;
	
	private static boolean alreadyLoadBullet = false;
	
	private static final ArrayList<BattleBitSetMap> battleMapList = new 
			ArrayList<BattleBitSetMap>();
	
	private static final HashMap<String, BattleBitSetMap> battleMaps = new 
			HashMap<String, BattleBitSetMap>();
	

	private static final HashMap<String, BattleBitSetBullet> battleBullets = new 
			HashMap<String, BattleBitSetBullet>();
	
	/**
	 * Load all battle maps from file system
	 */
	public static boolean loadBattleMaps() {
		if ( alreadyLoadBattleMap ) {
			return true;
		}
		boolean result = false;
		Collection<MapPojo> mapPojoList = GameContext.getInstance().getMapManager().getMaps();
		if ( mapPojoList == null ) {
			logger.warn("Failed to load map pojos");
			return result;
		}
		HashMap<String, MapPojo> mapPojos = new HashMap<String, MapPojo>();
		for ( Pojo pojo : mapPojoList ) {
			MapPojo mapPojo = (MapPojo)pojo;
			mapPojos.put(mapPojo.getId(), mapPojo);
		}
		if ( mapPojos.size() == 0) {
			logger.warn("Failed to load map pojos from system");
			return result;
		}
		
		String deployDataDir = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.deploy_data_dir);
		logger.debug("Load map from {}", deployDataDir);
		File mapDir = new File(deployDataDir, MAP);
		if ( mapDir.exists() && mapDir.isDirectory() ) {
			File[] mapFiles = mapDir.listFiles(new DatFileNameFilter(DAT));
			int index = 0;
			for ( File mapFile : mapFiles ) {
				String originalMapId = substring(mapFile.getName(), "map_", DAT);
				String mapId = originalMapId;
				if ( mapId.charAt(0) == '0' ) {
					mapId = mapId.substring(1, mapId.length());
				}
				if ( mapId.contains("@small") ) {
					logger.debug("Ignore small map file {}", mapFile);
				} else if ( mapId.contains("_mid") ) {
					logger.debug("Ignore middle map file {}", mapFile);
				} else if ( mapId.contains("_bg") ) {
					logger.debug("Ignore bg map file {}", mapFile);
				} else {
					BitSetImage bitSetImage = BitSetImage.fromFile(mapFile);
					BattleBitSetMap battleMap = new BattleBitSetMap();
					battleMap.setMapBitSet(bitSetImage);
					MapPojo mapPojo = mapPojos.get(mapId);
					if ( mapPojo == null ) {
						logger.info("Failed to find mapPojo for {}", mapFile);
						continue;
					}
					battleMap.setMapPojo(mapPojo);
					battleMap.setWidth(mapPojo.getScrollAreaWidth());
					battleMap.setHeight(mapPojo.getScrollAreaHeight());
					battleMap.setMapId(mapId);
					
					String midMapFileName = concat("map_", originalMapId, "_mid", DAT);
					File midMapFile = new File(mapDir, midMapFileName);
					if ( midMapFile.exists() ) {
						logger.debug("Load map mid file too {}", midMapFileName);
						BitSetImage midBitSetImage = BitSetImage.fromFile(midMapFile);
						battleMap.setMapMidBitSet(midBitSetImage);
					}
					battleMapList.add(battleMap);
					logger.debug("BattleMap: index:{}, id:{}", index, battleMap.getMapId());
					battleMaps.put(mapId, battleMap);
					//logger.info("Successfully load map file {}", mapFile);
					result = true;
					
					alreadyLoadBattleMap = true;
				}
			}
		} else {
			logger.warn("Deploy data map dir {} is invalid", mapDir.getAbsolutePath());
		}
		return result;
	}
	
	/**
	 * Get all the BattleMap objects. It is not cloned so do not modify the 
	 * BattleMap object in the list.
	 * 
	 * @return
	 */
	public static ArrayList<BattleBitSetMap> getBattleMapList() {
		return battleMapList;
	}
	
	/**
	 * Get the psedu-random map clone from system. 
	 * @return
	 */
	public static BattleBitSetMap getRandomBattleMap(Battle battle) {
//		counter = (counter++) % battleMapList.size();
//		return battleMapList.get(counter).clone();

		//Call script
		ScriptResult result = GameContext.getInstance().getScriptManager().
				runScript(ScriptHook.PICK_BATTLE_MAP, battle, battleMapList.toArray());
		
		/**
		 * Start a new ground.
		 */
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			BattleBitSetMap battleMap = (BattleBitSetMap)result.getResult().get(0);
			return battleMap;
		}
		return null;
	}
	
	/**
	 * Get the BattleBitSetMap by its mapId
	 */
	public static BattleBitSetMap getBattleMapById(String mapId) {
		BattleBitSetMap map = battleMaps.get(mapId);
		if ( map != null ) {
			if ( BattleManager.getInstance().isUseDistributed() ) {
				return map;
			} else {
				return map.clone();
			}
		}
		return null;
	}
	
	/**
	 * Load all battle bullets data from file system.
	 */
	public static boolean loadBattleBullet() {
		if ( alreadyLoadBullet ) {
			return true;
		}
		
		boolean result = false;
		
		String deployDataDir = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.deploy_data_dir);
		logger.debug("Load bullet from {}", deployDataDir);
		File bulletDir = new File(deployDataDir, BULLET);
		if ( bulletDir.exists() && bulletDir.isDirectory() ) {
			File[] bulletFiles = bulletDir.listFiles(new DatFileNameFilter("_area.dat"));
			for ( File bulletFile : bulletFiles ) {
				String bulletName = substring(bulletFile.getName(), null, "_area");
				BitSetImage bulletImage = BitSetImage.fromFile(bulletFile);
				BattleBitSetBullet battleBullet = new BattleBitSetBullet();
				battleBullet.setBulletName(bulletName);
				battleBullet.setBullet(bulletImage);
				
				String sBulletFileName = concat(bulletName, "_sArea.dat");
				File sBulletFile = new File(bulletDir, sBulletFileName);
				if ( sBulletFile.exists() ) {
					logger.debug("Load super bullet file too {}", sBulletFileName);
					BitSetImage sBulletImage = BitSetImage.fromFile(sBulletFile);
					battleBullet.setsBullet(sBulletImage);
				}
				battleBullets.put(bulletName, battleBullet);
				logger.info("load bullet {}, width:{}", battleBullet.getBulletName(), battleBullet.getBullet().getWidth());
				result = true;
				alreadyLoadBullet = true;
			}
		} else {
			logger.warn("Deploy data bullet dir {} is invalid", bulletDir.getAbsolutePath());
		}
		return result;
	}
	
	/**
	 * Get the BattleBullet by name
	 * @param bulletName
	 * @return
	 */
	public static BattleBitSetBullet getBattleBulletByName(String bulletName) {
		BattleBitSetBullet bullet = battleBullets.get(bulletName);
		if ( bullet != null ) {
			if ( BattleManager.getInstance().isUseDistributed() ) {
				return bullet;
			} else {
				return bullet.clone();
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Get the BattleBullet by name
	 * @param bulletName
	 * @return
	 */
	public static Collection<BattleBitSetBullet> getBattleBullets() {
		return battleBullets.values();
	}

	/**
	 * Filter all files without '.dat' extension out.
	 * @author wangqi
	 *
	 */
	private static class DatFileNameFilter implements FilenameFilter {
		
		private String ext;
		
		public DatFileNameFilter(String ext) {
			this.ext = ext;
		}

		@Override
		public boolean accept(File dir, String name) {
			if ( name.endsWith(ext) ) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * An utility for filter all png files.
	 * @author wangqi
	 *
	 */
	private static class PngFilenameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			if ( name.endsWith(".png") ) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * An utility to preprocess all the combat maps
	 * @param args
	 */
	public static void main(String... args ) throws Exception {
		//String mapFilePath = "../data/map";
		String mapFilePath = "/Users/wangqi/disk/projects/snsgames/babywar/clientupdate/trunk/maps";
		//String bulletPath = "../../client/game/Build/Prefab/bullet";
		String bulletPath = "/Users/wangqi/disk/projects/snsgames/babywar/clientupdate/trunk/bullet";
		//String dir = "/Users/wangqi/disk/projects/snsgames/babywar/server";
		String dir = "..";
		String mapOutputFilePath = dir + "/deploy/data/map";
		String bulletOutputFilePath = dir + "/deploy/data/bullet";
		if ( args.length > 0 ) {
			mapFilePath = args[0];
		}
		if ( args.length > 1 ) {
			bulletPath = args[1];
		}
		if ( args.length > 2 ) {
			mapOutputFilePath = args[2];
		}
		if ( args.length > 3 ) {
			bulletOutputFilePath = args[3];
		}
		PngFilenameFilter pngFilter = new PngFilenameFilter();
		
		logger.info("Process all the map files at directory: {}. It will output to {}", 
				mapFilePath, mapOutputFilePath);
		
		File mapFileDir = new File(mapFilePath);
		if ( !mapFileDir.exists() || !mapFileDir.isDirectory() ) {
			logger.warn("Map file directory does not exist.");
			System.exit(-1);
		}
		File outputDir = new File(mapOutputFilePath);
		if ( !outputDir.exists() || !outputDir.isDirectory() ) {
			logger.warn("Output directory does not exist. Create it.");
			outputDir.mkdirs();
		}
		File[] imageFiles = mapFileDir.listFiles( pngFilter );
		//Process all map data
		for ( File imageFile : imageFiles ) {
			BitSetImage bitSetImage = BitmapUtil.readBitmapToBitSet(imageFile, 
					BitmapUtil.DEFAULT_SCALE, 150);
			File datOutputFile = new File(outputDir, imageFile.getName().replace(".png", "").concat(".dat"));
			bitSetImage.toFile(datOutputFile);
		}
		
		//Now process the bullet data
		logger.info("Process all the bullet files at directory: {}. It will output to {}", 
				bulletPath, bulletOutputFilePath);
		File bulletDir = new File(bulletPath);
		if ( !bulletDir.exists() || !bulletDir.isDirectory() ) {
			logger.warn("Bullet file directory does not exist.");
			System.exit(-1);
		}
		outputDir = new File(bulletOutputFilePath);
		if ( !outputDir.exists() || !outputDir.isDirectory() ) {
			logger.warn("Output directory does not exist. Create it.");
			outputDir.mkdirs();
		}
		File[] bulletSubDirs = bulletDir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if ( pathname.isDirectory() && !pathname.getName().startsWith(".") ) {
					return true;
				}
				return false;
			}
		});
		for ( File bulletSubDir : bulletSubDirs ) {
			File[] bulletFiles = bulletSubDir.listFiles( pngFilter );
			for ( File bulletFile : bulletFiles ) {
				if ( bulletFile.getName().equals("area.png") || 
						bulletFile.getName().equals("sArea.png") ) {
					String prefix = bulletFile.getParentFile().getName().concat("_");
					BitSetImage bitSetImage = BitmapUtil.readBitmapToBitSet(bulletFile, BitmapUtil.DEFAULT_SCALE, 150);
					File datOutputFile = new File(outputDir, prefix+bulletFile.getName().replace(".png", "").concat(".dat"));
					bitSetImage.toFile(datOutputFile);
				}
			}
		}
		
	}
}
