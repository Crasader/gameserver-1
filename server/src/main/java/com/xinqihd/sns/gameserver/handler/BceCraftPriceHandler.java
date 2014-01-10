package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.forge.CraftManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceCraftPrice.BceCraftPrice;
import com.xinqihd.sns.gameserver.proto.XinqiBseCraftPrice.BseCraftPrice;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceCraftPriceHandler is used for query craft operation's price.
 * 
 * @author wangqi
 *
 */
public class BceCraftPriceHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceCraftPriceHandler.class);
	
	private static final BceCraftPriceHandler instance = new BceCraftPriceHandler();
	
	private BceCraftPriceHandler() {
		super();
	}

	public static BceCraftPriceHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceCraftPrice");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceCraftPrice craftPrice = (BceCraftPrice)request.payload;
		int[] auxpews = new int[craftPrice.getAuxpewCount()];
		int action = craftPrice.getAction();
		int targetPew = craftPrice.getPew();
		for ( int i=0; i<auxpews.length; i++ ) {
			auxpews[i] = craftPrice.getAuxpew(i);
		}
		
		//The User object should not be null because GameHandler is checking it.
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
	  /**
	   * 0: 强化
	   * 1: 合成, 将火、水、土、风石头合成到武器上
	   * 2: 熔炼, 合成高等级武器
	   * 3: 转移, 转移武器强化等级
	   */
		switch ( action ) {
			case 0:
			case 1:
			{
				ArrayList list = CraftManager.getInstance().forgeEquipPriceAndRatio(user, targetPew, auxpews);
				if ( list != null ) {
					Integer price = (Integer)list.get(0);
					Double ratio = (Double)list.get(1);
					Double guildRatio = (Double)list.get(2);
					
					BseCraftPrice.Builder builder = BseCraftPrice.newBuilder();
					builder.setPrice(price.intValue());
					builder.setSuccessratio((int)(ratio.doubleValue()*10000));
					if ( guildRatio != null && guildRatio.doubleValue()>0.0 ) {
						builder.setGuildratio((int)(guildRatio.doubleValue()*10000));
					}
					
					GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
				} else {
					resyncBag(user);
				}
				break;
			}
			case 2:
			{
				int[] allpews = new int[auxpews.length+1];
				allpews[0] = targetPew;
				System.arraycopy(auxpews, 0, allpews, 1, auxpews.length);
				ArrayList list = CraftManager.getInstance().composeItemPriceAndRatio(user, allpews);
				if ( list != null ) {
					Integer price = (Integer)list.get(0);
					Double ratio = (Double)list.get(1);
					Double guildRatio = (Double)list.get(2);
					
					BseCraftPrice.Builder builder = BseCraftPrice.newBuilder();
					builder.setPrice(price.intValue());
					builder.setSuccessratio((int)(ratio.doubleValue()*10000));
					if ( guildRatio != null && guildRatio.doubleValue()>0.0 ) {
						builder.setGuildratio((int)(guildRatio.doubleValue()*10000));
					}
					GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
				} else {
					resyncBag(user);
				}
				break;
			}
			case 3:
			{
				int srcPew = auxpews[0];
				ArrayList list = CraftManager.getInstance().transferEquipPriceAndRatio(user, srcPew, targetPew);
				if ( list != null ) {
					Integer price = (Integer)list.get(0);
					Double ratio = (Double)list.get(1);
					Double guildRatio = (Double)list.get(2);
					
					BseCraftPrice.Builder builder = BseCraftPrice.newBuilder();
					builder.setPrice(price.intValue());
					builder.setSuccessratio((int)(ratio.doubleValue()*10000));
					if ( guildRatio != null && guildRatio.doubleValue()>0.0 ) {
						builder.setGuildratio((int)(guildRatio.doubleValue()*10000));
					}
					GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
				} else {
					resyncBag(user);
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param user
	 */
	public void resyncBag(User user) {
		Bag bag = user.getBag();
		GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));
	}
}
