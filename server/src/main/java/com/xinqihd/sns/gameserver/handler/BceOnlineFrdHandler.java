package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseOnlineFrd.BseOnlineFrd;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceOnlineFrdHandler is used for protocol OnlineFrd 
 * @author wangqi
 *
 */
public class BceOnlineFrdHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceOnlineFrdHandler.class);
	
	private static final BceOnlineFrdHandler instance = new BceOnlineFrdHandler();
	
	private BceOnlineFrdHandler() {
		super();
	}

	public static BceOnlineFrdHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceOnlineFrd");
		}
		
		BseOnlineFrd.Builder builder = BseOnlineFrd.newBuilder();
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		UserManager manager = GameContext.getInstance().getUserManager();
		
		for ( RelationType relationType : RelationType.values() ) {
			Relation relation = user.getRelation(relationType);
			if ( relation != null ) {
				Collection<People> people = relation.listPeople();
				for ( People p : people ) {
					builder.addFrdId(p.getId().toString());
					builder.addFrdName(p.getUsername());
					//TODO support level in People
					builder.addLevel(p.getLevel());
					builder.addWincount(p.getWin());
					builder.addTotalcount(p.getWin()+p.getLose());
				}
			}
		}
		BseOnlineFrd onlineFrd = builder.build();
		
		XinqiMessage response = new XinqiMessage();
		response.payload = onlineFrd;
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.OnlineFrd, onlineFrd.getFrdIdCount());
	}
	
	
}
