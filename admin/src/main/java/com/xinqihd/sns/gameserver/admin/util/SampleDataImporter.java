package com.xinqihd.sns.gameserver.admin.util;

import com.xinqihd.sns.gameserver.admin.security.AdminUser;
import com.xinqihd.sns.gameserver.admin.security.AdminUserManager;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

public class SampleDataImporter {

	public static void importAdminUser() {
		AdminUser root = new AdminUser();
		root.setUsername("root");
		root.setPassword("r00t");
		root.setEmail("wangqi@xinqihd.com");
		root.addPriviledge(PriviledgeKey.all_priviledge);
		
		AdminUserManager.getInstance().saveAdminUser(root);
	}
	
	public static void main(String[] args) {
		GlobalConfig.getInstance().overrideProperty("mongdb.host", "localhost");
		SampleDataImporter.importAdminUser();
	}
}
