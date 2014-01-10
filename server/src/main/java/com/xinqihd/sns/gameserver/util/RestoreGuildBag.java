package com.xinqihd.sns.gameserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.xinqihd.sns.gameserver.guild.GuildBag;
import com.xinqihd.sns.gameserver.guild.GuildManager;

public class RestoreGuildBag {

	public static void main(String[] args) throws Exception {
		/*
		if ( args == null || args.length <= 0 ) {
			System.err.println("Failed to find the user file");
			return;
		}
		String fileName = args[0];
		*/
		String fileName = "/Users/wangqi/disk/guildbag.txt";
		File file = new File(fileName);
		if ( file.exists() ) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String guildStr = br.readLine();
			while ( guildStr != null ) {
				GuildBag guildBag = GuildManager.getInstance().convertStringToGuildBag(guildStr);
				GuildManager.getInstance().saveGuildBagWithoutCheck(guildBag);
				System.out.println("Restore: " + guildBag.get_id());
				
				guildStr = br.readLine();
			}
		} else {
			System.out.println("File " + file.getAbsolutePath() + " does not exist." );
		}
		System.exit(-1);
	}
	
}
