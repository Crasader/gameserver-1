package com.xinqihd.sns.gameserver.db.mongo;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.BattleTool;
import com.xinqihd.sns.gameserver.util.Text;

public class BattleToolManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(BattleToolManager.class);
	
	private static final String BATTLETOOL_COLL_NAME = "battletools";
	
	private static final String BATTLETOOL_NAME_NAME     = "name";
	private static final String BATTLETOOL_DESC_NAME     = "desc";
	private static final String BATTLETOOL_THEW_NAME     = "thew";
	private static final String BATTLETOOL_PRICE_NAME    = "price";
	
	private static ConcurrentHashMap<String, BattleTool> dataMap = 
			new ConcurrentHashMap<String, BattleTool>();
	
	private static BattleToolManager instance = new BattleToolManager();
	
	BattleToolManager() {
		super(BATTLETOOL_COLL_NAME, BATTLETOOL_NAME_NAME);
		reload();
	}
	
	/**
	 * Get the singleton instance of BattleToolManager
	 * @return
	 */
	public static BattleToolManager getInstance() {
		return instance;
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				BATTLETOOL_COLL_NAME, null);
		for ( DBObject obj : list ) {
			BattleTool battleTool = new BattleTool();
			battleTool.setName( (String)obj.get(BATTLETOOL_NAME_NAME) );
			battleTool.setDesc( Text.text(battleTool.getName()) ) ;
			battleTool.setThewCost( (Integer)obj.get(BATTLETOOL_THEW_NAME) );
			battleTool.setThewCost( (Integer)obj.get(BATTLETOOL_PRICE_NAME) );
			dataMap.put(battleTool.getName(), battleTool);
		}
	}
	
	/**
	 * Save sample data into database.
	 * @param args
	 */
	public static void main(String ...args ) {
		/**
		 * 名称    消耗体力     金币
		 * 引导			120       600
		 * 传送			150       200
		 * 生命恢复		150       600
		 * 激怒			120				600
		 * 隐身			50				150
		 * 团队隐身		150				200
		 * 团队恢复		170				500
		 * 冻结冰弹		150				500
		 * 改变风向		50				150
		 */
		String[] names = {
				"tool.guide", "tool.fly", "tool.recover", "tool.energy", "tool.hidden", "tool.allhidden", "tool.allrecover", "tool.iced", "tool.wind"
		};
		Integer[] thew = {
				120, 150, 150, 120, 50, 150, 170, 150, 50
		};
		Integer[] cost = {
				600, 200, 600, 600, 150, 200, 500, 500, 150
		};
		for ( int i=0; i<names.length; i++ ) {
			DBObject obj = MongoDBUtil.createDBObject();
			obj.put(BATTLETOOL_NAME_NAME, names[i]);
			obj.put(BATTLETOOL_DESC_NAME, Text.text(names[i]));
			obj.put(BATTLETOOL_THEW_NAME, thew[i]);
			obj.put(BATTLETOOL_PRICE_NAME, cost[i]);
			
			DBObject query = MongoDBUtil.createDBObject(BATTLETOOL_NAME_NAME, names[i]);
			MongoDBUtil.saveToMongo(query, obj, instance.databaseName, instance.namespace, BATTLETOOL_COLL_NAME, true);
		}
	}
}
