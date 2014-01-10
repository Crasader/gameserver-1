package com.xinqihd.sns.gameserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.xinqihd.sns.gameserver.db.mongo.MongoUserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;

public class RestoreGameRole {

	public static void main(String[] args) throws Exception {
		/*
		if ( args == null || args.length <= 0 ) {
			System.err.println("Failed to find the user file");
			return;
		}
		String fileName = args[0];
		*/
		String fileName = "/Users/wangqi/disk/gamerole.txt";
		File file = new File(fileName);
		if ( file.exists() ) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String userStr = br.readLine();
			String bagStr = br.readLine();
			String memberStr = br.readLine();
			while ( userStr != null && bagStr != null ) {
				User user = MongoUserManager.getInstance().convertStringToUser(userStr);
				Bag bag = MongoUserManager.getInstance().convertStringToBag(bagStr);
				if ( StringUtil.checkNotEmpty(memberStr) ) {
					GuildMember member = GuildManager.getInstance().convertStringToGuildMember(memberStr);
					GuildManager.getInstance().saveGuildMember(member);
				}
				user.setBag(bag);
				MongoUserManager.getInstance().saveUser(user, true);
				MongoUserManager.getInstance().saveUserBag(user, true);
				System.out.println("Restore: " + user.getRoleName());
				
				userStr = br.readLine();
				bagStr = br.readLine();
				memberStr = br.readLine();
			}
		} else {
			System.out.println("File " + file.getAbsolutePath() + " does not exist." );
		}
		System.exit(-1);
	}
	
}
