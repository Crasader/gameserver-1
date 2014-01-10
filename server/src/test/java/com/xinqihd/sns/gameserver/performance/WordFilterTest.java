package com.xinqihd.sns.gameserver.performance;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class WordFilterTest {
	
	@Test
	public void testFilterByLoop() throws Exception {
		String wordFileName = "../deploy/word.txt";
		final TreeSet<String> wordSet = new TreeSet<String>();
		ChatManager.getInstance().importWord(wordFileName, wordSet);
		
		TestUtil.doPerform(new Runnable(){
			public void run() {
				String word = "我Fuck你妈的共产党";
				String lower = word.toLowerCase();
				for ( String w : wordSet ) {
					lower = lower.replaceAll(w, "*");
				}
				//System.out.println(lower);				
			}
		}, "array loop", 10000);
	}
	
	@Test
	public void testFilterByRegex() throws Exception {
		String wordFileName = "../deploy/word.txt";
		TreeSet<String> wordSet = new TreeSet<String>();
		ChatManager.getInstance().importWord(wordFileName, wordSet);
		final String regex = ChatManager.getInstance().getFilteredWordRegex(wordSet);
		final Pattern pattern = Pattern.compile(regex);
		
		TestUtil.doPerform(new Runnable(){
			public void run() {
				String word = "我Fuck你妈的共产党";
				String lower = word.toLowerCase();
				Matcher matcher = pattern.matcher(lower);
				lower = matcher.replaceAll("*");
				//System.out.println(lower);				
			}
		}, "regex", 10000);
	}
}
