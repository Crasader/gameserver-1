package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceShopping;
import com.xinqihd.sns.gameserver.proto.XinqiBceShopping.BceShopping;
import com.xinqihd.sns.gameserver.proto.XinqiBseShopping;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceShoppingHandler is used for protocol Shopping 
 * 
 * type = 购买金钱类别(元宝0, 礼金1, 勋章2) << 16 + 商品类别(推荐0, 热买1, 武器2, 服饰3, 道具4, 形象5, 礼包6)
 * @author wangqi
 *
 */
public class BceShoppingHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceShoppingHandler.class);
	
	private static final BceShoppingHandler instance = new BceShoppingHandler();
	
	private BceShoppingHandler() {
		super();
	}

	public static BceShoppingHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceShopping");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		XinqiMessage request = (XinqiMessage)message;
		
		XinqiBceShopping.BceShopping bceShopping = (BceShopping) request.payload;
		int gender = bceShopping.getGender();
		int goldType = bceShopping.getMoneytype();
		int catalogId = bceShopping.getCatalogid();
		List<Integer> typeList = bceShopping.getStoneTypeList();
		int typeSize = 0;
		if ( typeList != null ) {
			typeSize = typeList.size();
		}
		String[] types = new String[typeSize];
		for ( int i=0; i<typeSize; i++ ) {
			types[i] = String.valueOf(typeList.get(i));
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("BceShopping: gender={}, goldType={}, catalogId={}, types={}", 
					new Object[]{gender, goldType, catalogId, typeList});
		}
		
		Collection<ShopPojo> shopPojoList = GameContext.getInstance().getShopManager().
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, goldType, gender, user);
		
		XinqiBseShopping.BseShopping.Builder builder = XinqiBseShopping.BseShopping.newBuilder();
		
		for ( ShopPojo shopPojo : shopPojoList ) {
			builder.addGoodsInfo(shopPojo.toGoodsInfo());
		}
		
		logger.debug("BceShopping: send totoal good info to client: {}", shopPojoList.size());
		
		builder.setType(catalogId);
		builder.setGender(gender);
		XinqiMessage response = new XinqiMessage();
	  response.payload = builder.build();
		response.type = MessageToId.messageToId(response.payload);
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		
	}
	
	
}
