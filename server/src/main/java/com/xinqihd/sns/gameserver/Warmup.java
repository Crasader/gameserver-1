package com.xinqihd.sns.gameserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.entity.user.PropData;

/**
 * Warm up the gameserver before it takes request.
 * @author wangqi
 *
 */
public class Warmup {
	
	private static final Log log = LogFactory.getLog(Warmup.class);
	
	private static final int MAX = 1000000;

	/**
	 * Warm-up the gameserver.
	 */
	public static final void warmup() {
//		warmupMapObject();
//		log.info("Warmup the server system.");
	}
	
	/**
	 * Warm-up the MapObject#putAll(Object) method.
	 */
	private static final void warmupMapObject() {
		MapDBObject dbObject = new MapDBObject();
		PropData bean = new PropData();
		try {
			for ( int i=0; i<MAX; i++ ) {
				dbObject.putAll(bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
