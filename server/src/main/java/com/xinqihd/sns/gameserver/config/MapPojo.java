package com.xinqihd.sns.gameserver.config;

import static com.xinqihd.sns.gameserver.config.Constant.*;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.GameResourceManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseMap;

/**
 * This class contains all configurable fields for a map
 * See the map_config.xml
 * 
 * @author wangqi
 *
 */
public class MapPojo implements Pojo, Comparable<MapPojo> {
	
	private static final long serialVersionUID = 529194463722125144L;

	private static final Log log = LogFactory.getLog(MapPojo.class);
	
	private String _id;
	
	private String name;
	
	/**
	 * type 1: Normal Map
	 * type 2: PVE Map
	 * type 3: training map
	*/
	private int type = 1;
	
	//The minimal level required.
	private int reqlv;
	
	private int scrollAreaX;
	
	private int scrollAreaY;
	
	private int scrollAreaWidth;
	
	private int scrollAreaHeight;
	
	private ArrayList<Layer> layers;
	
	private String bgm;
	
	private boolean damage;
	
	private ArrayList<Enemy> bosses;
	
	private ArrayList<Enemy> enemies;
	
	private ArrayList<Point> startPoints;
	
	private boolean isHidden = false;

	// ---------------------------------------
	
	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getReqlv() {
		return reqlv;
	}

	public void setReqlv(int reqlv) {
		this.reqlv = reqlv;
	}

	public int getScrollAreaX() {
		return scrollAreaX;
	}

	public void setScrollAreaX(int scrollAreaX) {
		this.scrollAreaX = scrollAreaX;
	}

	public int getScrollAreaY() {
		return scrollAreaY;
	}

	public void setScrollAreaY(int scrollAreaY) {
		this.scrollAreaY = scrollAreaY;
	}

	public int getScrollAreaWidth() {
		return scrollAreaWidth;
	}

	public void setScrollAreaWidth(int scrollAreaWidth) {
		this.scrollAreaWidth = scrollAreaWidth;
	}

	public int getScrollAreaHeight() {
		return scrollAreaHeight;
	}

	public void setScrollAreaHeight(int scrollAreaHeight) {
		this.scrollAreaHeight = scrollAreaHeight;
	}

	public int getLayerNum() {
		return layers.size();
	}

	public ArrayList<Layer> getLayers() {
		return layers;
	}

	public void setLayers(ArrayList<Layer> layers) {
		this.layers = layers;
	}

	public String getBgm() {
		return bgm;
	}

	public void setBgm(String bgm) {
		this.bgm = bgm;
	}

	public ArrayList<Enemy> getBosses() {
		return bosses;
	}

	public void setBosses(ArrayList<Enemy> bosses) {
		this.bosses = bosses;
	}

	public ArrayList<Enemy> getEnemies() {
		return enemies;
	}

	public void setEnemies(ArrayList<Enemy> enemies) {
		this.enemies = enemies;
	}

	public ArrayList<Point> getStartPoints() {
		return startPoints;
	}

	public void setStartPoints(ArrayList<Point> startPoints) {
		this.startPoints = startPoints;
	}	
	
	/**
	 * @return the damage
	 */
	public boolean isDamage() {
		return damage;
	}
	
	/**
	 * @return the isHidden
	 */
	public boolean isHidden() {
		return isHidden;
	}

	/**
	 * @param isHidden the isHidden to set
	 */
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	/**
	 * Convert this object to ProtoBuf's MapData
	 * @return
	 */
	public XinqiBseMap.MapData toMapData() {
		String resName = this.name;
		if ( Constant.I18N_ENABLE ) {
			Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
			resName = GameResourceManager.getInstance().getGameResource(
					"maps_name_".concat(_id), locale, this.name);
		}
		XinqiBseMap.MapData.Builder builder = XinqiBseMap.MapData.newBuilder();
		builder.setId(_id);
		builder.setName(resName);
		builder.setType(type);
		builder.setReqlv(reqlv);
		builder.setScrollAreaX(scrollAreaX);
		builder.setScrollAreaY(scrollAreaY);
		builder.setScrollAreaWidth(scrollAreaWidth);
		builder.setScrollAreaHeight(scrollAreaHeight);
		for ( Layer layer : layers ) {
			String type = FLOOR;
			if ( layer.id.endsWith(_BG) ) {
				type = BG;
			} else if ( layer.id.endsWith(_MID) ) {
				type = MID;
			} else if ( layer.id.endsWith(_MID1) ) {
				type = MID1;
			}
			XinqiBseMap.Layer pbLayer = XinqiBseMap.Layer.newBuilder().
					setId(layer.id).setNum(layer.num).
					setScrollRate(layer.scrollRate).
					setWidth(layer.width).setHeight(layer.height).
					setType(type).build();
			builder.addLayers(pbLayer);
		}
		builder.setBgm(bgm);
		builder.setDamage(damage);
		for ( Enemy boss : bosses ) {
			XinqiBseMap.Enemy pbEnemy = XinqiBseMap.Enemy.newBuilder().
					setId(boss.id).setX(boss.x).setY(boss.y).build();
			builder.addBosses(pbEnemy);
		}
		for ( Enemy enemy : enemies ) {
			XinqiBseMap.Enemy pbEnemy = XinqiBseMap.Enemy.newBuilder().
					setId(enemy.id).setX(enemy.x).setY(enemy.y).build();
			builder.addEnemies(pbEnemy);
		}
		for ( Point point : startPoints ) {
			XinqiBseMap.Point pbPoint = XinqiBseMap.Point.newBuilder().
					setX(point.x).setY(point.y).build();
			builder.addStartPoints(pbPoint);
		}
		builder.setHidden(isHidden);
		return builder.build();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MapPojo [_id=");
		builder.append(_id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", reqlv=");
		builder.append(reqlv);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @param damage the damage to set
	 */
	public void setDamage(boolean damage) {
		this.damage = damage;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + reqlv;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapPojo other = (MapPojo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (reqlv != other.reqlv)
			return false;
		return true;
	}

	@Override
	public int compareTo(MapPojo o) {
		if ( o == null ) {
			return -1;
		} else {
			int v = this.reqlv - o.reqlv;
			if ( v == 0 ) {
				v = this._id.compareTo(o._id);
			}
			return v;
		}
	}

	public static class Layer {
		public String id;
		public int num;
		public int scrollRate;
		public int width;
		public int height;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Layer [id=");
			builder.append(id);
			builder.append(", num=");
			builder.append(num);
			builder.append(", scrollRate=");
			builder.append(scrollRate);
			builder.append(", width=");
			builder.append(width);
			builder.append(", height=");
			builder.append(height);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	public static class Point {
		public Point() {
		}
		
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public int x;
		public int y;
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Point [x=");
			builder.append(x);
			builder.append(", y=");
			builder.append(y);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	public static class Enemy {
		public String id;
		public int x;
		public int y;
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Enemy [id=");
			builder.append(id);
			builder.append(", x=");
			builder.append(x);
			builder.append(", y=");
			builder.append(y);
			builder.append("]");
			return builder.toString();
		}
		
	}

}
