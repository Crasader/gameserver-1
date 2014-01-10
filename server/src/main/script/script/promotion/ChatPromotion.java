package script.promotion;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Puzzle;
import com.xinqihd.sns.gameserver.db.mongo.PuzzleManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.CommonUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 聊天活动
 * 玩家在聊天中输入“元宵灯谜”，则会弹出一条灯谜
 * 
 * @author wangqi
 *
 */
public class ChatPromotion {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatPromotion.class);
	//开始日期
	private static final Calendar startCal = Calendar.getInstance();
	//结束日期
	private static final Calendar endCal = Calendar.getInstance();
	
	//Redis保存了当前灯谜问题的INDEX
	private static final String REDIS_QUESTION = "puzzle:question:index";
	//保存了已经回答正确的问题INDEX，和回答正确的人的username
	private static final String REDIS_ANSWER = "puzzle:question:right";
	
	static {
		startCal.set(Calendar.YEAR, 2013);
		/**
		 * 元宵灯谜竞猜
		 * 2013-2-24 到 2013-2-25日
		 */
		startCal.set(Calendar.MONTH, 1);
		startCal.set(Calendar.DAY_OF_MONTH, 24);
		startCal.set(Calendar.HOUR_OF_DAY, 9);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		
		endCal.set(Calendar.YEAR, 2013);
		endCal.set(Calendar.MONTH, 1);
		endCal.set(Calendar.DAY_OF_MONTH, 24);
		endCal.set(Calendar.HOUR_OF_DAY, 23);
		endCal.set(Calendar.MINUTE, 59);
		endCal.set(Calendar.SECOND, 59);
	}
	
	private static final String REDIS_PREFIX = "chat_promotion:";

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		String chatContent = (String)parameters[1];
		boolean available = false;
		boolean debugUser = false;
		long currentTimeMillis = System.currentTimeMillis();
		if ( currentTimeMillis >= startCal.getTimeInMillis() && 
				currentTimeMillis <= endCal.getTimeInMillis() ) {
			//logger.debug("The charge promotion is in date:{} to {}", startCal.getTime(), endCal.getTime());
			available = true;
		}
		if ( "心海洋".equals(user.getRoleName()) ) {
			debugUser = true;
		}
		if ( debugUser || available ) {
			Jedis jedisDB = JedisFactory.getJedisDB();
			if ( chatContent != null ) {
				if ( chatContent.contains("灯谜") ) {
					Puzzle puzzle = nextPuzzle(jedisDB);
					ChatManager.getInstance().processChatToWorldAsyn(
							null, puzzle.getQuestion()+
							"(注意：回复时需加入'答:'，例如'孩子丢了（打一字)'，回复'答:亥'。记得使用小喇叭！)");
				} else if ( chatContent.startsWith("答:") || chatContent.startsWith("答：") ) {
					String answer = chatContent.substring(2).trim();
					String indexStr = jedisDB.get(REDIS_QUESTION);
					if ( !StringUtil.checkNotEmpty(indexStr) || "nil".equals(indexStr) ) {
						String content = "问题已经过期，下一个问题:";
						Puzzle puzzle = nextPuzzle(jedisDB);
						if ( puzzle != null ) {
							ChatManager.getInstance().processChatToWorldAsyn(null, content.concat(puzzle.getQuestion()));
						}
					} else {
						int index = StringUtil.toInt(indexStr, -1);
						if ( index >= 0 ) {
							Puzzle puzzle = PuzzleManager.getInstance().getPuzzleByIndex(index);
							if ( puzzle != null ) {
								if ( puzzle.getAnswer().trim().equals(answer) ) {
									//回答正确
									ChatManager.getInstance().processChatToWorldAsyn(null, 
											StringUtil.concat(new Object[]{"#00ff00恭喜玩家'",user.getRoleName(), "'答对了一题，获得6元宝奖励。" +
													"活动结束后，答对数量最多的玩家（不少于50道）将获得100元话费充值卡。"}));
									jedisDB.hset(REDIS_ANSWER, indexStr, user.getUsername());
									user.setYuanbaoFree(user.getYuanbaoFree()+6);
									GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
									//清除当前问题
									jedisDB.del(new String[]{REDIS_QUESTION});
								} else {
									//回答错误，继续抢答
									logger.warn("The puzzle right answer is ‘{}’, user anwser is :‘{}’", answer, puzzle.getAnswer());
								}
							} else {
								logger.warn("The puzzle is null for index:{}", index);
							}
						}
					}
				}
			}
		}
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * @param jedisDB
	 * @return
	 */
	private static Puzzle nextPuzzle(Jedis jedisDB) {
		int index = -1;
		String indexStr = jedisDB.get(REDIS_QUESTION);
		if ( !StringUtil.checkNotEmpty(indexStr) || "nil".equals(indexStr) ) {
			Long sizeLong = jedisDB.hlen(REDIS_ANSWER);
			int totalCount = PuzzleManager.getInstance().getPuzzles().size();
			if ( sizeLong != null && sizeLong.intValue()>=totalCount ){
				ChatManager.getInstance().processChatToWorldAsyn(null, "所有的谜题均已经解决，活动结束了，谢谢参与！");
			} else {
				index = CommonUtil.getRandomInt(totalCount);
				indexStr = String.valueOf(index);
				while ( jedisDB.hexists(REDIS_ANSWER, indexStr) ) {
					index = CommonUtil.getRandomInt(PuzzleManager.getInstance().getPuzzles().size());
					indexStr = String.valueOf(index);
				}
				jedisDB.set(REDIS_QUESTION, String.valueOf(index));
				jedisDB.expire(REDIS_QUESTION, 300);
			}
		} else {
			index = StringUtil.toInt(indexStr, 0);
		}
		if ( index >= 0 ) {
			Puzzle puzzle = PuzzleManager.getInstance().getPuzzleByIndex(index);
			return puzzle;
		} else {
			return null;
		}
	}

}
