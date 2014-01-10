package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.Puzzle;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.util.CommonUtil;

/**
 * 
 * @author wangqi
 *
 */
public final class PuzzleManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(PuzzleManager.class);
	
	private static final String COLL_NAME = "puzzles";
	private static final String QUESTION_NAME = "question";
	private static final String ANSWER_NAME = "answer";

	private static final PuzzleManager instance = new PuzzleManager();
	
	private ArrayList<Puzzle> dataList = new ArrayList<Puzzle>();
	
	private PuzzleManager() {
		super(COLL_NAME, QUESTION_NAME);
		reload();
	}
	
	public static PuzzleManager getInstance() {
		return instance;
	}

	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		dataList.clear();
		
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		
		for ( DBObject result : list ) {
			Puzzle puzzle = (Puzzle)MongoDBUtil.constructObject(result);
			dataList.add(puzzle);
		}
		
		logger.debug("Load total {} puzzles from database", dataList.size());
	}
	
	/**
	 * Get the random puzzle for given user
	 * @param user
	 * @return
	 */
	public Puzzle getRandomPuzzle(User user) {
		int index = CommonUtil.getRandomInt(dataList.size());
		return dataList.get(index);
	}
	
	/**
	 * Get the puzzle at given index.
	 * @param index
	 * @return
	 */
	public Puzzle getPuzzleByIndex(int index) {
		return dataList.get(index);
	}

	/**
	 * Get all the puzzles in database
	 * @return
	 */
	public ArrayList<Puzzle> getPuzzles() {
		return dataList;
	}
	
	/**
	 * Add a new puzzle
	 * @param question
	 * @param answer
	 */
	public void addPuzzle(String question, String answer) {
		Puzzle puzzle = new Puzzle();
		puzzle.setQuestion(question);
		puzzle.setAnswer(answer);
		
		DBObject query = MongoDBUtil.createDBObject(QUESTION_NAME, question);
		DBObject dbObj = MongoDBUtil.createMapDBObject(puzzle);
		MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, 
				COLL_NAME, isSafeWrite);
	}
}
