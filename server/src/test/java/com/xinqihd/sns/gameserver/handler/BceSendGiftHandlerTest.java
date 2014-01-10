package com.xinqihd.sns.gameserver.handler;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceSendGift.BceSendGift;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;

public class BceSendGiftHandlerTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, "localhost:0");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_RPC_SERVERID, "localhost:0");

		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.deploy_data_dir, "../deploy/data");
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMessageProcess() throws Exception {
		BceSendGiftHandler handler = BceSendGiftHandler.getInstance();
		
		IoSession session = createMock(IoSession.class);
		
		XinqiMessage xinqi = new XinqiMessage();
		
		BceSendGift.Builder builder = BceSendGift.newBuilder();
		for ( Reward reward : prepareRewards() ) {
			builder.addGift(reward.toGift());
		}
		UserId toUserId = new UserId("test-001");
		builder.setFromUserName("GameAdmin");
		builder.setToUserIdStr(toUserId.toString());
		
		xinqi.payload = builder.build();
		
		handler.messageProcess(session, xinqi, null);
	}
	
	@Test
	public void testGameClientSendGift() throws Exception {
		XinqiMessage xinqi = new XinqiMessage();
		
		BceSendGift.Builder builder = BceSendGift.newBuilder();
		for ( Reward reward : prepareRewards() ) {
			builder.addGift(reward.toGift());
		}
		UserId toUserId = new UserId("EXIA");
		builder.setFromUserName("GameAdmin");
		builder.setToUserIdStr(toUserId.toString());
		
		xinqi.payload = builder.build();

		GameClient client = new GameClient("babywar.xinqihd.com", 3443);
		client.sendMessageToServer(xinqi);
	}

	private Collection<Reward> prepareRewards() {
		Reward reward = new Reward();
		reward.setId(UserManager.basicUserGiftBoxId);
		reward.setPropColor(WeaponColor.WHITE);
		reward.setPropCount(1);
		reward.setLevel(1);
		reward.setPropIndate(0);
		reward.setType(RewardType.ITEM);
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		rewards.add(reward);
		return rewards;
	}
}
