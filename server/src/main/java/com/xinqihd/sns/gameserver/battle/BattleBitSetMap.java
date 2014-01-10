package com.xinqihd.sns.gameserver.battle;

import com.xinqihd.sns.gameserver.config.MapPojo;
import com.xinqihd.sns.gameserver.geom.BitSetImage;

/**
 * Every room will have a BattleMap, which stores the map's destroyed information.
 * 
 * 
 * @author wangqi
 *
 */
public class BattleBitSetMap {
	
	//The map's identity string
	private String mapId = null;
	
	//The map's basic data
	private MapPojo mapPojo = null;
	
	//The map's Geometry
	private BitSetImage mapBitSet = null;
	
	//The middle layer of a map, may be null
	private BitSetImage mapMidBitSet = null;
	
	//The map's geometry width;
	private int width; 
	
	//The map's geometry height;
	private int height;

	/**
	 * @return the mapPojo
	 */
	public MapPojo getMapPojo() {
		return mapPojo;
	}

	/**
	 * @param mapPojo the mapPojo to set
	 */
	public void setMapPojo(MapPojo mapPojo) {
		this.mapPojo = mapPojo;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the mapId
	 */
	public String getMapId() {
		return mapId;
	}

	/**
	 * @param mapId the mapId to set
	 */
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}
	
	/**
	 * @return the mapBitSet
	 */
	public BitSetImage getMapBitSet() {
		return mapBitSet;
	}

	/**
	 * @param mapBitSet the mapBitSet to set
	 */
	public void setMapBitSet(BitSetImage mapBitSet) {
		this.mapBitSet = mapBitSet;
	}

	/**
	 * @return the mapMidBitSet
	 */
	public BitSetImage getMapMidBitSet() {
		return mapMidBitSet;
	}

	/**
	 * @param mapMidBitSet the mapMidBitSet to set
	 */
	public void setMapMidBitSet(BitSetImage mapMidBitSet) {
		this.mapMidBitSet = mapMidBitSet;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BattleBitSetMap [mapId=");
		builder.append(mapId);
		builder.append(", mapPojo=");
		builder.append(mapPojo);
		builder.append(", mapBitSet=");
		builder.append(mapBitSet);
		builder.append(", mapMidBitSet=");
		builder.append(mapMidBitSet);
		builder.append(", width=");
		builder.append(width);
		builder.append(", height=");
		builder.append(height);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Clone a new BattleMap since the geometry will change a lot during battle.
	 */
	@Override
	public BattleBitSetMap clone() {
		BattleBitSetMap newBattleMap = new BattleBitSetMap();
		newBattleMap.setMapId(mapId);
		newBattleMap.setMapPojo(mapPojo);
		newBattleMap.setWidth(width);
		newBattleMap.setHeight(height);
		if ( mapBitSet != null ) {
			newBattleMap.setMapBitSet(mapBitSet.clone());
		}
		if ( mapMidBitSet != null ) {
			newBattleMap.setMapMidBitSet(mapMidBitSet.clone());
		}
		return newBattleMap;
	}
}
