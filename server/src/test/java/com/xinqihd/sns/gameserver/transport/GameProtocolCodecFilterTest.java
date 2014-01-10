package com.xinqihd.sns.gameserver.transport;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo.BseRoleBattleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleConfig.BseRoleConfig;
import com.xinqihd.sns.gameserver.proto.XinqiPropData;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionMessage;

public class GameProtocolCodecFilterTest extends AbstractTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testEmptyMessage() throws Exception {
		// 2. 玩家角色配置数据
		XinqiMessage response = new XinqiMessage();
		
		encodeAndDecode(response, false);
	}

	@Test
	public void testRoleConfig() throws Exception {
		// 2. 玩家角色配置数据
		User user = UserManager.getInstance().createDefaultUser();
		XinqiMessage response = new XinqiMessage();
		BseRoleConfig.Builder roleConfigBuilder = BseRoleConfig.newBuilder();
		roleConfigBuilder.setEffectSwitch(user.isConfigEffectSwitch());
		roleConfigBuilder.setEffectVolume(user.getConfigEffectVolume());
		roleConfigBuilder.setMusicSwitch(user.isConfigMusicSwitch());
		roleConfigBuilder.setMusicVolume(user.getConfigMusicVolume());
		roleConfigBuilder.setGuidestep(0);
		roleConfigBuilder.setHideGlasses(user.isConfigHideGlass());
		roleConfigBuilder.setHideHat(user.isConfigHideHat());
		roleConfigBuilder.setHideSuit(user.isConfigHideSuite());
		response.index = 0;
		response.payload = roleConfigBuilder.build();
		
		encodeAndDecode(response, true);
	}
	
	@Test
	public void testRoleBattleInfo() throws Exception {
		super.setUp();
		GameContext.getTestInstance().reloadContext();
		User user = UserManager.getInstance().createDefaultUser();
		
		// 3. 玩家装备信息
		XinqiMessage roleBattle = new XinqiMessage();
		BseRoleBattleInfo.Builder battleBuilder = BseRoleBattleInfo.newBuilder();
		battleBuilder.setRoleAgility(user.getAgility());
		battleBuilder.setRoleAttack(user.getAttack());
		battleBuilder.setRoleBlood(user.getBlood());
		battleBuilder.setRoleDamage(user.getDamage());
		battleBuilder.setRoleDefend(user.getDefend());
		battleBuilder.setRoleLuck(user.getLuck());
		battleBuilder.setRolePower(user.getPower());
		battleBuilder.setRoleSkin(user.getSkin());
		battleBuilder.setRoleThew(user.getTkew());

		// 4. User's Bag
		Bag bag = user.getBag();
    List<PropData> weaponList = bag.getWearPropDatas();
    List<PropData> othersList = bag.getOtherPropDatas();
    
    for ( PropData propData : weaponList ) {
    	if ( propData != null ) {
    		addBagPropData(battleBuilder, propData);
    	}
    }
    for ( PropData propData : othersList ) {
    	if ( propData != null ) {
    		addBagPropData(battleBuilder, propData);
    	}
    }
		
    roleBattle.payload = battleBuilder.build();
    
    encodeAndDecode(roleBattle, true);
	}
	
	@Test
	public void testRoleBattleInfoProxy() throws Exception {
		super.setUp();
		GameContext.getTestInstance().reloadContext();
		User user = UserManager.getInstance().createDefaultUser();
		
		// 3. 玩家装备信息
		XinqiMessage roleBattle = new XinqiMessage();
		BseRoleBattleInfo.Builder battleBuilder = BseRoleBattleInfo.newBuilder();
		battleBuilder.setRoleAgility(user.getAgility());
		battleBuilder.setRoleAttack(user.getAttack());
		battleBuilder.setRoleBlood(user.getBlood());
		battleBuilder.setRoleDamage(user.getDamage());
		battleBuilder.setRoleDefend(user.getDefend());
		battleBuilder.setRoleLuck(user.getLuck());
		battleBuilder.setRolePower(user.getPower());
		battleBuilder.setRoleSkin(user.getSkin());
		battleBuilder.setRoleThew(user.getTkew());
		
		// 4. User's Bag
		Bag bag = user.getBag();
    List<PropData> weaponList = bag.getWearPropDatas();
    List<PropData> othersList = bag.getOtherPropDatas();
    
    for ( PropData propData : weaponList ) {
    	if ( propData != null ) {
    		addBagPropData(battleBuilder, propData);
    	}
    }
    for ( PropData propData : othersList ) {
    	if ( propData != null ) {
    		addBagPropData(battleBuilder, propData);
    	}
    }
		
    roleBattle.payload = battleBuilder.build();
    
		XinqiProxyMessage proxy = new XinqiProxyMessage();
		proxy.userSessionKey = SessionKey.createSessionKeyFromRandomString();
		proxy.xinqi = roleBattle;
    
    encodeAndDecodeProxyMessage(proxy, true);
	}
	
	@Test
	public void testHearbeat() throws Exception {
		super.setUp();
		GameContext.getTestInstance().reloadContext();

		XinqiProxyMessage proxy = new XinqiProxyMessage();
		proxy.userSessionKey = null;
		proxy.xinqi = null;
    
    encodeAndDecodeProxyMessage(proxy, false);
	}
	
	private void encodeAndDecode(XinqiMessage response, boolean checkDecoder) throws Exception {
		ProtobufEncoder encoder = new ProtobufEncoder();
		ProtobufDecoder decoder = new ProtobufDecoder();
		final ArrayList<Object> results = new ArrayList<Object>();
		
		IoSession session = createNiceMock(IoSession.class);
		expect(session.getTransportMetadata()).andReturn(
				new DefaultTransportMetadata("testprovider", "default", 
						false, true, InetSocketAddress.class, DefaultSocketSessionConfig.class, 
						SessionMessage.class)).anyTimes();
		
		ProtocolEncoderOutput out = createNiceMock(ProtocolEncoderOutput.class);
		out.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				results.add(getCurrentArguments()[0]);
				return null;
			}
		});
		
		replay(session);
		replay(out);
		
		encoder.encode(session, response, out);
		
		verify(session);
		verify(out);
		
		assertTrue(results.get(0) instanceof IoBuffer );
		
		IoBuffer buffer = (IoBuffer)results.get(0);
		results.remove(0);
		
		ProtocolDecoderOutput deout = createNiceMock(ProtocolDecoderOutput.class);
		if ( checkDecoder ) {
			deout.write(anyObject());
			expectLastCall().andAnswer(new IAnswer<Object>() {
				@Override
				public Object answer() throws Throwable {
					results.add(getCurrentArguments()[0]);
					return null;
				}
			}).times(1);
		}
		replay(deout);
		
		decoder.decode(session, buffer, deout);
		
		verify(deout);
		
		if ( checkDecoder ) {
			XinqiMessage decodeMsg = (XinqiMessage)results.get(0);
			assertEquals(response.payload.getClass(), decodeMsg.payload.getClass());
		}
	}

	private void encodeAndDecodeProxyMessage(XinqiProxyMessage response, boolean checkDecoder) throws Exception {
		ProtobufEncoder encoder = new ProtobufEncoder();
		ProtobufDecoder decoder = new ProtobufDecoder();
		final ArrayList<Object> results = new ArrayList<Object>();
		
		IoSession session = createNiceMock(IoSession.class);
		expect(session.getTransportMetadata()).andReturn(
				new DefaultTransportMetadata("testprovider", "default", 
						false, true, InetSocketAddress.class, DefaultSocketSessionConfig.class, 
						SessionMessage.class)).anyTimes();
		
		IoBuffer buffer = (IoBuffer)ProtobufEncoder.encodeXinqiProxyMessage(response);
		
		ProtocolDecoderOutput deout = createNiceMock(ProtocolDecoderOutput.class);
		if ( checkDecoder ) {
			deout.write(anyObject());
			expectLastCall().andAnswer(new IAnswer<Object>() {
				@Override
				public Object answer() throws Throwable {
					results.add(getCurrentArguments()[0]);
					return null;
				}
			}).times(1);
		}
		
		replay(session);
		replay(deout);
		
		decoder.decode(session, buffer, deout);
		
		verify(session);
		verify(deout);
		
		if ( checkDecoder ) {
			XinqiProxyMessage decodeMsg = (XinqiProxyMessage)results.get(0);
			assertEquals(response.userSessionKey, decodeMsg.userSessionKey);
			assertEquals(response.xinqi.payload.getClass(), decodeMsg.xinqi.payload.getClass());
		}
	}
	
	/**
	 * Add a prop data into user's bag.
	 * @param battleBuilder
	 */
	private void addBagPropData(BseRoleBattleInfo.Builder battleBuilder, PropData propData) {
		XinqiPropData.PropData.Builder pd = XinqiPropData.PropData.newBuilder();
		pd.setAgilityLev(propData.getAgilityLev());
		pd.setAttackLev(propData.getAttackLev());
		pd.setBanded(propData.isBanded());
		Pojo pojo = propData.getPojo();
		if ( pojo instanceof WeaponPojo ) {
			WeaponPojo weapon = (WeaponPojo)pojo;
			pd.setColor(weapon.getQualityColor().ordinal());
		}
		pd.setCount(propData.getCount());
		pd.setDefendLev(propData.getDefendLev());
		pd.setDuration(propData.getDuration());
		pd.setId(propData.getItemId());
		pd.setLevel(propData.getLevel());
		pd.setLuckLev(propData.getLuckLev());
		pd.setPropID(propData.getItemId());
		pd.setPropIndate(propData.getPropIndate());
		pd.setPropPew(propData.getPew());
		pd.setSign(propData.getSign());
		pd.setValuetype(propData.getValuetype().ordinal());
		
		XinqiPropData.PropData propdata = pd.build();
		
		battleBuilder.addRoleBagInfo(propdata);
	}
}
