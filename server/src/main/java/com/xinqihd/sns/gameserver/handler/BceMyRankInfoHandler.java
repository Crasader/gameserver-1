package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceMyRankInfo.BceMyRankInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceMyRankInfoHandler is used for protocol MyRankInfo 
 * @author wangqi
 *
 */
public class BceMyRankInfoHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceMyRankInfoHandler.class);
	
	private static final BceMyRankInfoHandler instance = new BceMyRankInfoHandler();
	
	private BceMyRankInfoHandler() {
		super();
	}

	public static BceMyRankInfoHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceMyRankInfo");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceMyRankInfo myRankInfo = (BceMyRankInfo)request.payload;
		
		int rankTypeInt = myRankInfo.getArrangeType();
		RankType rankType = RankType.GLOBAL;
		if ( rankTypeInt >=0 && rankTypeInt < RankType.values().length ) {
			rankType = RankType.values()[rankTypeInt];
		}
		
		int scoreTypeInt = myRankInfo.getRankType();
		RankScoreType scoreType = RankScoreType.POWER;
		if ( scoreTypeInt >=0 && scoreTypeInt < RankScoreType.values().length ) {
			scoreType = RankScoreType.values()[scoreTypeInt];
		}
		
		int filterInt = myRankInfo.getFilterType();
		RankFilterType filterType = RankFilterType.fromIndex(filterInt);
		if ( filterType == null ) {
			filterType = RankFilterType.TOTAL;
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		RankManager rankManager = RankManager.getInstance();
		RankUser rankUser = rankManager.getCurrentRankUser(user, rankType, 
				filterType, scoreType);
		
		XinqiMessage response = new XinqiMessage();
		response.payload = rankUser.toBseMyRankInfo();
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.MyRankInfo, rankType, filterType, scoreType, rankUser.getRank(), rankUser.getScore());
	}
	
	
}
