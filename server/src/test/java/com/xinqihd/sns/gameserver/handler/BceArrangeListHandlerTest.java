package com.xinqihd.sns.gameserver.handler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiArrangeInfo.ArrangeInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBceArrangeList.BceArrangeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseArrangeList.BseArrangeList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BceArrangeListHandlerTest {

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMessageProcess() throws Exception {
		JedisUtil.deleteAllKeys();
		BceArrangeListHandler handler = BceArrangeListHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		XinqiMessage xinqi = new XinqiMessage();
		
		BceArrangeList.Builder builder = BceArrangeList.newBuilder();
		builder.setStartRank(0);
		builder.setEndRank(7);
		builder.setRankType(RankScoreType.POWER.ordinal());
		builder.setFilterType(RankFilterType.TOTAL.index());
		builder.setArrangeType(RankType.GLOBAL.ordinal());
		
		xinqi.payload = builder.build();
		
		handler.messageProcess(session, xinqi, SessionKey.createSessionKeyFromRandomString());
		
		BseArrangeList bse = null;
		for ( XinqiMessage x : list ) {
			if ( x.payload instanceof BseArrangeList ) {
				bse = (BseArrangeList)x.payload;
			}
		}
		assertEquals(1, bse.getTotalnum());
		List<ArrangeInfo> infos = bse.getArrInfoList();
		for ( ArrangeInfo info : infos ) {
			System.out.println(info.getRank()+":"+info.getScore()+":"+info.getName());
		}
	}
	
	@Test
	public void testStartRankEndRank() throws Exception {
		JedisUtil.deleteAllKeys();
		
		BceArrangeListHandler handler = BceArrangeListHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		//insert rank
		User user = new User();
		for ( int i=0; i<120; i++ ) {
			user.setUsername("test-"+i);
			user.setRoleName("test-"+i);
			user.setPowerSimple((int)(MathUtil.nextDouble()*1000));
			RankManager.getInstance().storeGlobalRankData(user, RankScoreType.POWER, System.currentTimeMillis());
		}
		
		XinqiMessage xinqi = new XinqiMessage();
		
		BceArrangeList.Builder builder = BceArrangeList.newBuilder();
		int startRank = 10;
		builder.setStartRank(startRank);
		builder.setEndRank(17);
		builder.setRankType(RankScoreType.POWER.ordinal());
		builder.setFilterType(RankFilterType.TOTAL.index());
		builder.setArrangeType(RankType.GLOBAL.ordinal());
		
		xinqi.payload = builder.build();
		
		handler.messageProcess(session, xinqi, SessionKey.createSessionKeyFromRandomString());
		
		BseArrangeList bse = null;
		for ( XinqiMessage x : list ) {
			if ( x.payload instanceof BseArrangeList ) {
				bse = (BseArrangeList)x.payload;
			}
		}
		assertEquals(100, bse.getTotalnum());
		List<ArrangeInfo> infos = bse.getArrInfoList();
		int i = 0;
		for ( ArrangeInfo info : infos ) {
			System.out.println(info.getRank()+":"+info.getScore()+":"+info.getName());
			assertEquals(startRank+(i++), info.getRank());
		}
	}

	@Test
	public void testTotalRankCount() throws Exception {
		BceArrangeListHandler handler = BceArrangeListHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		JedisUtil.deleteAllKeys();
		//insert power rank
		int powerRankCount = 20;
		User user = new User();
		for ( int i=0; i<powerRankCount; i++ ) {
			user.setUsername("test-"+i);
			user.setPowerSimple(powerRankCount-i);
			RankManager.getInstance().storeGlobalRankData(user, RankScoreType.POWER, System.currentTimeMillis());
		}
		//insert wealth rank
		int wealthRankCount = 25;
		for ( int i=0; i<wealthRankCount; i++ ) {
			user.setUsername("test-"+i);
			user.setYuanbaoSimple(wealthRankCount-i);
			RankManager.getInstance().storeGlobalRankData(user, RankScoreType.WEALTH, System.currentTimeMillis());
		}
		
		//Check power rank count
		{
			XinqiMessage xinqi = new XinqiMessage();
			BceArrangeList.Builder builder = BceArrangeList.newBuilder();
			int startRank = 10;
			builder.setStartRank(startRank);
			builder.setEndRank(startRank+7);
			builder.setRankType(RankScoreType.POWER.ordinal());
			builder.setFilterType(RankFilterType.TOTAL.index());
			builder.setArrangeType(RankType.GLOBAL.ordinal());
			
			xinqi.payload = builder.build();
			
			handler.messageProcess(session, xinqi, SessionKey.createSessionKeyFromRandomString());
			
			BseArrangeList bse = null;
			for ( XinqiMessage x : list ) {
				if ( x.payload instanceof BseArrangeList ) {
					bse = (BseArrangeList)x.payload;
				}
			}
			List<ArrangeInfo> infos = bse.getArrInfoList();
			for ( ArrangeInfo info : infos ) {
				System.out.println(info.getRank()+":"+info.getScore()+":"+info.getName());
			}
			assertEquals(powerRankCount, bse.getTotalnum());
			assertEquals(8, bse.getArrInfoCount());
		}
		
		//Check wealth rank count
		{
			XinqiMessage xinqi = new XinqiMessage();
			BceArrangeList.Builder builder = BceArrangeList.newBuilder();
			int startRank = 10;
			builder.setStartRank(startRank);
			builder.setEndRank(startRank+7);
			builder.setRankType(RankScoreType.WEALTH.ordinal());
			builder.setFilterType(RankFilterType.TOTAL.index());
			builder.setArrangeType(RankType.GLOBAL.ordinal());
			
			xinqi.payload = builder.build();
			
			handler.messageProcess(session, xinqi, SessionKey.createSessionKeyFromRandomString());
			
			BseArrangeList bse = null;
			for ( XinqiMessage x : list ) {
				if ( x.payload instanceof BseArrangeList ) {
					bse = (BseArrangeList)x.payload;
				}
			}
			assertEquals(wealthRankCount, bse.getTotalnum());
			assertEquals(8, bse.getArrInfoCount());
		}
	}
	
	@Test
	public void testFriendTotalRankCount() throws Exception {
		BceArrangeListHandler handler = BceArrangeListHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		JedisUtil.deleteAllKeys();
		//insert power rank
		int powerRankCount = 40;
		UserId userId = new UserId("test-001");
		User user = new User();
		Relation friend = new Relation();
		friend.set_id(userId);
		friend.setType(RelationType.FRIEND);
		for ( int i=0; i<powerRankCount; i++ ) {
			user.setUsername("friend-"+i);
			user.setPowerSimple(powerRankCount-i);
			if ( i<20 ) {
				People p = new People();
				p.setId(new UserId(user.getUsername()));
				p.setUsername(user.getUsername());
				friend.addPeople(p);
			}
			RankManager.getInstance().storeGlobalRankData(user, RankScoreType.POWER, System.currentTimeMillis());
		}
		user.set_id(userId);
		user.setUsername("test-001");
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		user.addRelation(friend);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserRelation(user.getRelations());
		
		//Check global power rank count
		{
			XinqiMessage xinqi = new XinqiMessage();
			BceArrangeList.Builder builder = BceArrangeList.newBuilder();
			int startRank = 10;
			builder.setStartRank(startRank);
			builder.setEndRank(startRank+7);
			builder.setRankType(RankScoreType.POWER.ordinal());
			builder.setFilterType(RankFilterType.TOTAL.index());
			builder.setArrangeType(RankType.GLOBAL.ordinal());
			
			xinqi.payload = builder.build();
			
			handler.messageProcess(session, xinqi, user.getSessionKey());
			
			BseArrangeList bse = null;
			for ( XinqiMessage x : list ) {
				if ( x.payload instanceof BseArrangeList ) {
					bse = (BseArrangeList)x.payload;
				}
			}
			List<ArrangeInfo> infos = bse.getArrInfoList();
			for ( ArrangeInfo info : infos ) {
				System.out.println(info.getRank()+":"+info.getScore()+":"+info.getName());
			}
			assertEquals(40, bse.getTotalnum());
			assertEquals(8, bse.getArrInfoCount());
		}
		
		//Check power rank count
		{
			XinqiMessage xinqi = new XinqiMessage();
			BceArrangeList.Builder builder = BceArrangeList.newBuilder();
			int startRank = 10;
			builder.setStartRank(startRank);
			builder.setEndRank(startRank+7);
			builder.setRankType(RankScoreType.POWER.ordinal());
			builder.setFilterType(RankFilterType.TOTAL.index());
			builder.setArrangeType(RankType.FRIEND.ordinal());
			
			xinqi.payload = builder.build();
			
			handler.messageProcess(session, xinqi, user.getSessionKey());
			
			BseArrangeList bse = null;
			for ( XinqiMessage x : list ) {
				if ( x.payload instanceof BseArrangeList ) {
					bse = (BseArrangeList)x.payload;
				}
			}
			List<ArrangeInfo> infos = bse.getArrInfoList();
			for ( ArrangeInfo info : infos ) {
				System.out.println(info.getRank()+":"+info.getScore()+":"+info.getName());
			}
			assertEquals(20, bse.getTotalnum());
			assertEquals(8, bse.getArrInfoCount());
		}

		//Check power rank count page
		{
			XinqiMessage xinqi = new XinqiMessage();
			BceArrangeList.Builder builder = BceArrangeList.newBuilder();
			int startRank = 1;
			builder.setStartRank(startRank);
			builder.setEndRank(startRank+7);
			builder.setRankType(RankScoreType.POWER.ordinal());
			builder.setFilterType(RankFilterType.TOTAL.index());
			builder.setArrangeType(RankType.FRIEND.ordinal());

			xinqi.payload = builder.build();
			
			handler.messageProcess(session, xinqi, user.getSessionKey());
			
			BseArrangeList bse = null;
			for ( XinqiMessage x : list ) {
				if ( x.payload instanceof BseArrangeList ) {
					bse = (BseArrangeList)x.payload;
				}
			}
			List<ArrangeInfo> infos = bse.getArrInfoList();
			for ( ArrangeInfo info : infos ) {
				System.out.println(info.getRank()+":"+info.getScore()+":"+info.getName());
			}
			assertEquals(20, bse.getTotalnum());
			assertEquals(8, bse.getArrInfoCount());
		}
	}
}
