package script.ai;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.ai.AIAction;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.ai.SessionAIMessage;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BulletTrack;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.geom.BitSetImage;
import com.xinqihd.sns.gameserver.geom.BitmapUtil;
import com.xinqihd.sns.gameserver.geom.SimplePoint;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BattleRoleAttackTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/*
		通过落点推算power和t	
		angle	60
		wind	1
		a=-wind/F	-13.33333333
		sin(angle)	0.866025404
		cos(angle)	0.5
		hitx	400
		hity	323.5577075
		t = sqrt(-(hitx*K/cos(angle)-(hity*K)/sin(angle))/((g*K)/2*sin(angle)-(a*K)/cos(angle)))	1.017163466
		power=-a*t*t*K/(cos(angle)*t) + hitx*K/(cos(angle)*t)	48.8948393
			48.8948393
	 */
	@Test
	public void testCalculatePower() {
		int wind = 1;
		int hitx = 400;
		int hity = 324;
		int angle = 60;
		int power = AIAction.calculatePower(angle, hitx, hity, wind);
		assertTrue(power>0 && power <=100);
	}

	@Test
	public void testCalculatePowerWind0() {
		int wind = 0;
		int hitx = 400;
		int hity = 324;
		int angle = 60;
		int power = AIAction.calculatePower(angle, hitx, hity, wind);
		assertTrue(power>0 && power<=100);
	}

	@Test
	public void testCalculatePowerReal() {
		//AI my position:(1345,0), target:(390,106), angle: 146, power: 68
		int wind = 0;
		int myx = 390;
		int myy = 100;
		int targetx = 1345;
		int targety = 0;
		int angle = 34;
		int power = AIAction.calculatePower(angle, 
				Math.abs(myx-targetx), 
				Math.abs(myy-targety), 
				wind);
		System.out.println("power="+power+" distx="+Math.abs(myx-targetx)+", disty="+Math.abs(myy-targety));
		final File mapFile = new File("../data/map/map_03.png");
		BitSetImage bitSetImage = BitmapUtil.readBitmapToBitSet(mapFile, BitmapUtil.DEFAULT_SCALE, 150);
		ArrayList trackDebugList = new ArrayList();
		BulletTrack track = BitmapUtil.caculateBulletTrack(
				BitmapUtil.DEFAULT_SCALE, 
				myx, myy, power, angle, wind, bitSetImage, 1500, trackDebugList);
		System.out.println(track);
		System.out.println(trackDebugList);
	}
	
	@Test
	public void testFunFromLeft() throws Exception {
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
//		GameContext.getInstance().registerUserSession(session, user, null);
		BseRoundStart.Builder builder = BseRoundStart.newBuilder();
		builder.setSessionId(user.getSessionKey().toString());
		int wind = 0;
		int myX = 390;
		int myY = 100;
		int targetX = 1345;
		int targetY = 0;
		String mapId = "3";
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, 
				"../deploy/data");
		BattleDataLoader4Bitmap.loadBattleMaps();
		user.putUserData(AIManager.BATTLE_MAP_ID, mapId);
		SimplePoint hitPoint = null;
		for ( int i=0; i<10; i++ ) {
			builder.setWind(wind);
			builder.setBoxcount(0);
			builder.setCurRound(0);
			builder.addUserId(user.getSessionKey().toString());
			builder.addPosX(myX);
			builder.addPosY(myY);
			builder.addBlood(100);
			builder.addEnergy(10);
			builder.addUserId(SessionKey.createSessionKeyFromRandomString().toString());
			builder.addPosX(targetX);
			builder.addPosY(targetY);
			builder.addBlood(100);
			builder.addEnergy(10);
			ScriptResult result = BattleRoleAttack.func(new Object[]{session, user, builder.build()});
			BceRoleAttack attack = (BceRoleAttack)TestUtil.getGivenClassObject(result.getResult(), BceRoleAttack.class);
			hitPoint = (SimplePoint)user.getUserData(AIManager.BATTLE_LAST_ROUND_HITPOINT);
			System.out.println(hitPoint);
		}
		assertTrue(hitPoint.getX() > targetX-30 && hitPoint.getX()< targetX+30);
	}
	
	@Test
	public void testFunFromRight() throws Exception {
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
//		GameContext.getInstance().registerUserSession(session, user, null);
		BseRoundStart.Builder builder = BseRoundStart.newBuilder();
		builder.setSessionId(user.getSessionKey().toString());
		int wind = 0;
		int myX = 1345;
		int myY = -30;
		int targetX = 390;
		int targetY = 100;
		String mapId = "3";
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, 
				"../deploy/data");
		BattleDataLoader4Bitmap.loadBattleMaps();
		user.putUserData(AIManager.BATTLE_MAP_ID, mapId);
		SimplePoint hitPoint = null;
		for ( int i=0; i<10; i++ ) {
			builder.setWind(wind);
			builder.setBoxcount(0);
			builder.setCurRound(0);
			builder.addUserId(user.getSessionKey().toString());
			builder.addPosX(myX);
			builder.addPosY(myY);
			builder.addBlood(100);
			builder.addEnergy(10);
			builder.addUserId(SessionKey.createSessionKeyFromRandomString().toString());
			builder.addPosX(targetX);
			builder.addPosY(targetY);
			builder.addBlood(100);
			builder.addEnergy(10);
			ScriptResult result = BattleRoleAttack.func(new Object[]{session, user, builder.build()});
			BceRoleAttack attack = (BceRoleAttack)TestUtil.getGivenClassObject(result.getResult(), BceRoleAttack.class);
			hitPoint = (SimplePoint)user.getUserData(AIManager.BATTLE_LAST_ROUND_HITPOINT);
			System.out.println(hitPoint);
		}
		assertTrue(hitPoint.getX() > targetX-30 && hitPoint.getX()< targetX+30);
	}
	
	private BseRoleAttack getRoleAttack(Collection coll) {
		for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
			SessionAIMessage message = (SessionAIMessage) iterator.next();
			if ( message.getMessage().payload instanceof BseRoleAttack ) {
				return (BseRoleAttack)message.getMessage().payload;
			}
		}
		return null;
	}
}
