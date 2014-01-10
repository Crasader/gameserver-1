package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;
import java.util.Iterator;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.proto.XinqiBceArrangeList.BceArrangeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseArrangeList.BseArrangeList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceArrangeListHandler is used for protocol ArrangeList 
 * @author wangqi
 *
 */
public class BceArrangeListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceArrangeListHandler.class);
	
	private static final BceArrangeListHandler instance = new BceArrangeListHandler();
	
	private BceArrangeListHandler() {
		super();
	}

	public static BceArrangeListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceArrangeList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		
		BceArrangeList myRankInfo = (BceArrangeList)request.payload;
		
		int rankTypeInt = myRankInfo.getRankType();
		RankType rankType = RankType.GLOBAL;
		if ( rankTypeInt >=0 && rankTypeInt < RankType.values().length ) {
			rankType = RankType.values()[rankTypeInt];
		}
		
		int scoreTypeInt = myRankInfo.getArrangeType();
		RankScoreType scoreType = RankScoreType.POWER;
		if ( scoreTypeInt >=0 && scoreTypeInt < RankScoreType.values().length ) {
			scoreType = RankScoreType.values()[scoreTypeInt];
		}
		
		int filterInt = myRankInfo.getFilterType();
		RankFilterType filterType = RankFilterType.fromIndex(filterInt);
		if ( filterType == null ) {
			filterType = RankFilterType.TOTAL;
		}
		int startRank = myRankInfo.getStartRank();
		if ( startRank > 0 ) {
			startRank -= 1;
		}
		int endRank = myRankInfo.getEndRank();
		if ( endRank > 0 ) {
			endRank -= 1;
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("BceArrayList query: rankType:{}, scoreType:{}, filterType:{}, start:{}, end:{}", 
					new Object[]{rankType, scoreType, filterType, startRank, endRank});
		}
		String bossId = myRankInfo.getBossid();

		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		RankManager rankManager = RankManager.getInstance();
		Collection<RankUser> rankUsers = rankManager.
				getAllRankUsers(user, rankType, filterType, scoreType, 
						startRank, endRank, bossId);
		int count = rankManager.getTotalRankUserCount(user, rankType, filterType, scoreType, bossId);
		
		logger.debug("Get total {} rank users.", rankUsers.size());
		
		XinqiMessage response = new XinqiMessage();
		BseArrangeList.Builder builder = BseArrangeList.newBuilder();
		if ( rankType == RankType.GLOBAL || rankType == RankType.WORLD || rankType == RankType.PVE ) {
			for ( RankUser rankUser : rankUsers ) {
				if ( rankUser == null ) continue;
				builder.addArrInfo( rankUser.toArrangeInfo(scoreType) );
			}
			builder.setTotalnum(count);
		} else if ( rankType == RankType.FRIEND || rankType == RankType.ONLINE) {
			int totalCount = rankUsers.size();
			builder.setTotalnum(totalCount);
			Iterator<RankUser> iter = rankUsers.iterator();
			for ( int i=0; i<totalCount; i++ ) {
				RankUser rankUser = iter.next();
				if ( i>=startRank ) {
					if ( i<=endRank ) {
						builder.addArrInfo( rankUser.toArrangeInfo(scoreType) );
					} else {
						break;
					}
				}
			}
		}
		response.payload = builder.build();

		GameContext.getInstance().writeResponse(sessionKey, response);
		
		//Run tasks
		TaskManager.getInstance().processUserTasks(user, TaskHook.CHECK_RANKING);
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.ArrangeList, rankType, 
				scoreType, filterType, startRank, endRank);
		
		UserActionManager.getInstance().addUserAction(user.getRoleName(), UserActionKey.ArrangeList);
	}
	
	
}
