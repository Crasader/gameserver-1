package com.xinqihd.sns.gameserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;

/**
 * 这个类解析http://www.xuexila.com/jzw/网站的脑筋急转弯题目
 * 并导入数据库
 * 
 * @author wangqi
 *
 */
public class ImportPuzzleQuestion {

	/**
	 * 
	 * @param args
	 */
	public static final void main(String[] args) {
		String dir = "fun/";
		File dirFile = new File(dir);
		File[] files = dirFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				if ( name.endsWith(".htm") ) {
					return true;
				}
				return false;
			}
		});
		
		String questionBeginKey = " color=\"#008080\">";
		String questionEndKey = "<";
		String answerBeginKey = "onClick=\"MM_popupMsg('答案：";
		String answerEndKey = "')";
		for ( File file : files ) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(file), "gbk"));
				String line = br.readLine();
				while ( line != null ) {
					if ( line.contains(questionBeginKey) ) {
						String question = StringUtil.substring(line, questionBeginKey, questionEndKey);
						System.out.print(question);
						System.out.print("\t");
					}
					if ( line.contains(answerBeginKey) ) {
						String answer = StringUtil.substring(line, answerBeginKey, answerEndKey);
						System.out.println(answer);
					}
					line = br.readLine();
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
