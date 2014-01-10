package com.xinqihd.sns.gameserver.handler;

import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBossInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseBossList.BseBossList;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceBossListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceBossTakeRewardHandler.class);
	
	private static final BceBossListHandler instance = new BceBossListHandler();
	
	private BceBossListHandler() {
		super();
	}

	public static BceBossListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceBossList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		//BceBossList mailRead = (BceBossList)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		boolean isRoomLocked = BceEnterRoomHandler.checkRoomLock(user, RoomType.PVE_ROOM);
		if ( !isRoomLocked ) {
			Set<Boss> bosses = BossManager.getInstance().getAllBossInstance(user);
			if ( bosses.size() <= 0 ) {
				SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.nothing", Type.NORMAL);
			} else {
				BseBossList.Builder bseBossList = BseBossList.newBuilder();
				for ( Boss boss : bosses ) {
					XinqiBossInfo.BossInfo bossInfo = boss.toXinqiBossInfo(user);
					if ( bossInfo != null ) {
						bseBossList.addBosslist(bossInfo);
					}
				}
				GameContext.getInstance().writeResponse(sessionKey, bseBossList.build());
			}
		}
	}
	
}
