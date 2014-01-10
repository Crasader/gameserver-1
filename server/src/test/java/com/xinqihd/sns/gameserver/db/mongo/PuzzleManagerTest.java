package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.Puzzle;

public class PuzzleManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGetRandomPuzzle() {
		Puzzle puzzle = PuzzleManager.getInstance().getRandomPuzzle(null);
		System.out.println(puzzle);
		assertNotNull(puzzle);
	}

	public void importFile() throws IOException {
		File file = new File("fun/puzzleword.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		while ( line != null ) {
			String[] fields = line.split("\t");
			if ( fields.length == 2 ) {
				PuzzleManager.getInstance().addPuzzle(fields[0], fields[1]);
			}
			line = br.readLine();
		}
	}

}
