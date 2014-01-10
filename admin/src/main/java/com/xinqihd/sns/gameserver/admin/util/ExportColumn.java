package com.xinqihd.sns.gameserver.admin.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

/**
 * 将数据库中所有的字段导出，可以翻译为中文名字
 * @author wangqi
 *
 */
public class ExportColumn {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "mongos.babywar.xinqihd.com";
		int    port = 27017;
		String database = "babywar";
		String namespace = "server0001";
		String[] colls = {"users", "bags", "maps", "equipments", 
				"shops", "tasks", "relations", "items", "dailymarks", 
				"charges", "vipperiods"};
		
		LinkedHashSet<String> columns = new LinkedHashSet<String>();
		DBObject query = MongoUtil.createDBObject();
		for ( int i=0; i<colls.length; i++ ) {
			List<DBObject> results = MongoUtil.queryAllFromMongo(query, database, namespace, 
					colls[i], null, null, 0, 1);
			if ( results.size()>0 ) {
				DBObject obj = results.get(0);
				Set<String> set = obj.keySet();
				columns.addAll(set);
			}
		}
		for ( String str : columns ) {
			System.out.println(str+"=");
		}
	}

}
