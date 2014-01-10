package com.xinqihd.sns.gameserver.util;

import java.io.File;
import java.io.FileWriter;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;

public class BackupGameRole {

	public static void main(String[] args) throws Exception {
		/*
		if ( args == null || args.length <= 0 ) {
			System.err.println("Failed to find the user file");
			return;
		}
		String fileName = args[0];
		*/
		String roleName = "s朵朵";
		String fileName = "/Users/wangqi/disk/gamerole.txt";
		File file = new File(fileName);
		FileWriter fw = new FileWriter(file);

		User user = UserManager.getInstance().queryUserByRoleName(roleName);
		Bag bag = UserManager.getInstance().queryUserBag(user);
		try {
			//Every time when user logout the game, save the user status into database
			String userStr = UserManager.getInstance().convertUserToString(user);
			String bagStr = UserManager.getInstance().convertBagToString(bag);
			fw.append(userStr).append("\n");
			fw.append(bagStr).append("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		fw.close();
		System.exit(-1);
	}
	
}
