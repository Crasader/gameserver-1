package com.xinqihd.sns.gameserver.cron;

import java.io.File;
import java.io.IOException;

import com.xinqihd.sns.gameserver.db.mongo.CDKeyManager;

/**
 * Import the already generated CDKey into game server.
 * 
 * @author wangqi
 *
 */
public class ImportCDKey {
	
	public static void main(String[] args) throws IOException {
		if ( args == null || args.length <= 0 ) {
			System.out.println("You should specify a cdkey file names");
			System.exit(-1);
		}
		String fileName = args[0];
		File file = new File(fileName);
		if ( !file.exists() || file.isDirectory() ) {
			System.out.println("The cdkey file specified does not exist: " + file.getAbsolutePath());
			System.exit(-1);
		} else {
			System.out.println("Import " + file.getAbsolutePath() + " into cdkey system.");
			CDKeyManager cdKeyManger = CDKeyManager.getInstance();
			int count = cdKeyManger.importCDKey(fileName);
			System.out.println("Total import " + count + " cdkey into system");
		}
		System.exit(0);
	}

}
