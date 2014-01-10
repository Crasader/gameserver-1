package com.xinqihd.sns.gameserver.performance;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.proto.*;
import com.xinqihd.sns.gameserver.util.CommonUtil;

public class PerformanceTest {
	
	private Object[] msgObjects = {XinqiBseAddFriend.BseAddFriend.getDefaultInstance(),XinqiBseAddProp.BseAddProp.getDefaultInstance(),XinqiBseAddTask.BseAddTask.getDefaultInstance(),XinqiBseAnswerInvite.BseAnswerInvite.getDefaultInstance(),XinqiBseArrangeList.BseArrangeList.getDefaultInstance(),XinqiBseAskRoundOver.BseAskRoundOver.getDefaultInstance(),XinqiBseAuthState.BseAuthState.getDefaultInstance(),XinqiBseBattleAlter.BseBattleAlter.getDefaultInstance(),XinqiBseBattleInit.BseBattleInit.getDefaultInstance(),XinqiBseBattleOver.BseBattleOver.getDefaultInstance(),XinqiBseBattlePickBox.BseBattlePickBox.getDefaultInstance(),XinqiBseBroadcastMessage.BseBroadcastMessage.getDefaultInstance(),XinqiBseBulltinList.BseBulltinList.getDefaultInstance(),XinqiBseBuyProp.BseBuyProp.getDefaultInstance(),XinqiBseChangeMap.BseChangeMap.getDefaultInstance(),XinqiBseChangeSeat.BseChangeSeat.getDefaultInstance(),XinqiBseChat.BseChat.getDefaultInstance(),XinqiBseChgBtlType.BseChgBtlType.getDefaultInstance(),XinqiBseClearLeaveMessage.BseClearLeaveMessage.getDefaultInstance(),XinqiBseClearRole.BseClearRole.getDefaultInstance(),XinqiBseCompose.BseCompose.getDefaultInstance(),XinqiBseCreateGuild.BseCreateGuild.getDefaultInstance(),XinqiBseDailyMark.BseDailyMark.getDefaultInstance(),XinqiBseDead.BseDead.getDefaultInstance(),XinqiBseDelTask.BseDelTask.getDefaultInstance(),XinqiBseDoubleExpTime.BseDoubleExpTime.getDefaultInstance(),XinqiBseEditSeat.BseEditSeat.getDefaultInstance(),XinqiBseEnterGuild.BseEnterGuild.getDefaultInstance(),XinqiBseEnterHall.BseEnterHall.getDefaultInstance(),XinqiBseEnterRoom.BseEnterRoom.getDefaultInstance(),XinqiBseError.BseError.getDefaultInstance(),XinqiBseExitGuild.BseExitGuild.getDefaultInstance(),XinqiBseFindRoom.BseFindRoom.getDefaultInstance(),XinqiBseForge.BseForge.getDefaultInstance(),XinqiBseFriendEquip.BseFriendEquip.getDefaultInstance(),XinqiBseFriendList.BseFriendList.getDefaultInstance(),XinqiBseFriendOnlineStatus.BseFriendOnlineStatus.getDefaultInstance(),XinqiBseFriendsInfo.BseFriendsInfo.getDefaultInstance(),XinqiBseGhostMove.BseGhostMove.getDefaultInstance(),XinqiBseGhostMoveStart.BseGhostMoveStart.getDefaultInstance(),XinqiBseGhostMoveStop.BseGhostMoveStop.getDefaultInstance(),XinqiBseGiveProp.BseGiveProp.getDefaultInstance(),XinqiBseGuildAcceptMember.BseGuildAcceptMember.getDefaultInstance(),XinqiBseGuildBuy.BseGuildBuy.getDefaultInstance(),XinqiBseGuildContribute.BseGuildContribute.getDefaultInstance(),XinqiBseGuildCostAlert.BseGuildCostAlert.getDefaultInstance(),XinqiBseGuildDismiss.BseGuildDismiss.getDefaultInstance(),XinqiBseGuildFire.BseGuildFire.getDefaultInstance(),XinqiBseGuildInvite.BseGuildInvite.getDefaultInstance(),XinqiBseGuildIronLevelup.BseGuildIronLevelup.getDefaultInstance(),XinqiBseGuildLevelUp.BseGuildLevelUp.getDefaultInstance(),XinqiBseGuildLimit.BseGuildLimit.getDefaultInstance(),XinqiBseGuildList.BseGuildList.getDefaultInstance(),XinqiBseGuildNews.BseGuildNews.getDefaultInstance(),XinqiBseGuildPosChange.BseGuildPosChange.getDefaultInstance(),XinqiBseGuildReqCancel.BseGuildReqCancel.getDefaultInstance(),XinqiBseGuildReqMemberList.BseGuildReqMemberList.getDefaultInstance(),XinqiBseGuildRequest.BseGuildRequest.getDefaultInstance(),XinqiBseGuildSearch.BseGuildSearch.getDefaultInstance(),XinqiBseGuildSetPosName.BseGuildSetPosName.getDefaultInstance(),XinqiBseGuildShopLevelup.BseGuildShopLevelup.getDefaultInstance(),XinqiBseGuildTransfer.BseGuildTransfer.getDefaultInstance(),XinqiBseHallRoomList.BseHallRoomList.getDefaultInstance(),XinqiBseHallUserList.BseHallUserList.getDefaultInstance(),XinqiBseHarvestTree.BseHarvestTree.getDefaultInstance(),XinqiBseHeartbeat.BseHeartbeat.getDefaultInstance(),XinqiBseInvite.BseInvite.getDefaultInstance(),XinqiBseInviteFrd.BseInviteFrd.getDefaultInstance(),XinqiBseInviteGuildList.BseInviteGuildList.getDefaultInstance(),XinqiBseInviteRtn.BseInviteRtn.getDefaultInstance(),XinqiBseJoinGuild.BseJoinGuild.getDefaultInstance(),XinqiBseKickUser.BseKickUser.getDefaultInstance(),XinqiBseLeaveHall.BseLeaveHall.getDefaultInstance(),XinqiBseLeaveMessage.BseLeaveMessage.getDefaultInstance(),XinqiBseLeaveRoom.BseLeaveRoom.getDefaultInstance(),XinqiBseLengthenIndate.BseLengthenIndate.getDefaultInstance(),XinqiBseLoadProgress.BseLoadProgress.getDefaultInstance(),XinqiBseLogin.BseLogin.getDefaultInstance(),XinqiBseLottery.BseLottery.getDefaultInstance(),XinqiBseLotteryOver.BseLotteryOver.getDefaultInstance(),XinqiBseMatchingRoom.BseMatchingRoom.getDefaultInstance(),XinqiBseMergeProp.BseMergeProp.getDefaultInstance(),XinqiBseMessage.BseMessage.getDefaultInstance(),XinqiBseModiTask.BseModiTask.getDefaultInstance(),XinqiBseMoveProp.BseMoveProp.getDefaultInstance(),XinqiBseMyRankInfo.BseMyRankInfo.getDefaultInstance(),XinqiBseNotify.BseNotify.getDefaultInstance()};
	private Object[] clientObjects = {XinqiBceAddFriend.BceAddFriend.getDefaultInstance(),XinqiBceAnswerInvite.BceAnswerInvite.getDefaultInstance(),XinqiBceArmStrength.BceArmStrength.getDefaultInstance(),XinqiBceArrangeList.BceArrangeList.getDefaultInstance(),XinqiBceAskRoundOver.BceAskRoundOver.getDefaultInstance(),XinqiBceBagTidy.BceBagTidy.getDefaultInstance(),XinqiBceBattlePickBox.BceBattlePickBox.getDefaultInstance(),XinqiBceBattleStageReady.BceBattleStageReady.getDefaultInstance(),XinqiBceBullet.BceBullet.getDefaultInstance(),XinqiBceBuyProp.BceBuyProp.getDefaultInstance(),XinqiBceBuyTool.BceBuyTool.getDefaultInstance(),XinqiBceChangeMap.BceChangeMap.getDefaultInstance(),XinqiBceChangeSeat.BceChangeSeat.getDefaultInstance(),XinqiBceChat.BceChat.getDefaultInstance(),XinqiBceChgBtlType.BceChgBtlType.getDefaultInstance(),XinqiBceChgShootMode.BceChgShootMode.getDefaultInstance(),XinqiBceClearLeaveMessage.BceClearLeaveMessage.getDefaultInstance(),XinqiBceCompose.BceCompose.getDefaultInstance(),XinqiBceCreateGuild.BceCreateGuild.getDefaultInstance(),XinqiBceDailyMark.BceDailyMark.getDefaultInstance(),XinqiBceDead.BceDead.getDefaultInstance(),XinqiBceDebug.BceDebug.getDefaultInstance(),XinqiBceDoubleExpOver.BceDoubleExpOver.getDefaultInstance(),XinqiBceEditSeat.BceEditSeat.getDefaultInstance(),XinqiBceEnterGuild.BceEnterGuild.getDefaultInstance(),XinqiBceEnterHall.BceEnterHall.getDefaultInstance(),XinqiBceEnterRoom.BceEnterRoom.getDefaultInstance(),XinqiBceExitGuild.BceExitGuild.getDefaultInstance(),XinqiBceFindRoom.BceFindRoom.getDefaultInstance(),XinqiBceForge.BceForge.getDefaultInstance(),XinqiBceFriendInfo.BceFriendInfo.getDefaultInstance(),XinqiBceGhostMove.BceGhostMove.getDefaultInstance(),XinqiBceGhostMoveStart.BceGhostMoveStart.getDefaultInstance(),XinqiBceGhostMoveStop.BceGhostMoveStop.getDefaultInstance(),XinqiBceGuideFinish.BceGuideFinish.getDefaultInstance(),XinqiBceGuideStep.BceGuideStep.getDefaultInstance(),XinqiBceGuildAcceptMember.BceGuildAcceptMember.getDefaultInstance(),XinqiBceGuildBuy.BceGuildBuy.getDefaultInstance(),XinqiBceGuildContribute.BceGuildContribute.getDefaultInstance(),XinqiBceGuildDismiss.BceGuildDismiss.getDefaultInstance(),XinqiBceGuildFire.BceGuildFire.getDefaultInstance(),XinqiBceGuildInvite.BceGuildInvite.getDefaultInstance(),XinqiBceGuildInviteRsp.BceGuildInviteRsp.getDefaultInstance(),XinqiBceGuildIronLevelup.BceGuildIronLevelup.getDefaultInstance(),XinqiBceGuildLevelUp.BceGuildLevelUp.getDefaultInstance(),XinqiBceGuildLimit.BceGuildLimit.getDefaultInstance(),XinqiBceGuildPosChange.BceGuildPosChange.getDefaultInstance(),XinqiBceGuildReqCancel.BceGuildReqCancel.getDefaultInstance(),XinqiBceGuildReqMemberList.BceGuildReqMemberList.getDefaultInstance(),XinqiBceGuildRequest.BceGuildRequest.getDefaultInstance(),XinqiBceGuildSearch.BceGuildSearch.getDefaultInstance(),XinqiBceGuildSetPosName.BceGuildSetPosName.getDefaultInstance(),XinqiBceGuildShopLevelup.BceGuildShopLevelup.getDefaultInstance(),XinqiBceGuildTransfer.BceGuildTransfer.getDefaultInstance(),XinqiBceHallRoomList.BceHallRoomList.getDefaultInstance(),XinqiBceHallUserList.BceHallUserList.getDefaultInstance(),XinqiBceHarvestTree.BceHarvestTree.getDefaultInstance(),XinqiBceHeartbeat.BceHeartbeat.getDefaultInstance(),XinqiBceInit.BceInit.getDefaultInstance(),XinqiBceInvite.BceInvite.getDefaultInstance(),XinqiBceKickUser.BceKickUser.getDefaultInstance(),XinqiBceLeaveHall.BceLeaveHall.getDefaultInstance(),XinqiBceLeaveMessage.BceLeaveMessage.getDefaultInstance(),XinqiBceLeaveRoom.BceLeaveRoom.getDefaultInstance(),XinqiBceLengthenIndate.BceLengthenIndate.getDefaultInstance(),XinqiBceLoadProgress.BceLoadProgress.getDefaultInstance(),XinqiBceLogin.BceLogin.getDefaultInstance(),XinqiBceLottery.BceLottery.getDefaultInstance(),XinqiBceMergeProp.BceMergeProp.getDefaultInstance(),XinqiBceMoveProp.BceMoveProp.getDefaultInstance(),XinqiBceMyRankInfo.BceMyRankInfo.getDefaultInstance(),XinqiBceOnlineFrd.BceOnlineFrd.getDefaultInstance(),XinqiBceOnlineReward.BceOnlineReward.getDefaultInstance(),XinqiBcePRCEnter.BcePRCEnter.getDefaultInstance(),XinqiBcePRCOpen.BcePRCOpen.getDefaultInstance(),XinqiBcePickGold.BcePickGold.getDefaultInstance(),XinqiBceReadyStart.BceReadyStart.getDefaultInstance(),XinqiBceRequestFriendInfo.BceRequestFriendInfo.getDefaultInstance(),XinqiBceRobotOver.BceRobotOver.getDefaultInstance(),XinqiBceRoleAttack.BceRoleAttack.getDefaultInstance(),XinqiBceRoleChangeDirection.BceRoleChangeDirection.getDefaultInstance(),XinqiBceRoleMove.BceRoleMove.getDefaultInstance(),XinqiBceRoleMoveStart.BceRoleMoveStart.getDefaultInstance(),XinqiBceRoleMoveStop.BceRoleMoveStop.getDefaultInstance(),XinqiBceRolePower.BceRolePower.getDefaultInstance(),XinqiBceRoleUseTool.BceRoleUseTool.getDefaultInstance(),XinqiBceRoundOver.BceRoundOver.getDefaultInstance()};
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Since we only need the 'date' part of a time, I will 
	 * try the best method to get it.
	 */
	@Test
	public void testBestDateMethod() {
		int max = 1000000;
		long startM=0l, endM = 0l;
		//Method 1.
		int constant = 86400000;
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			long d = CommonUtil.getTodayMillis();
		}
		endM = System.currentTimeMillis();
		System.out.println("BestDateMethod Method: 1 (benchmark 69)" + (endM-startM));
		
		//Method 2
		Date date = null;
		Calendar cal = null;
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			long d = cal.getTimeInMillis();
//			date = new Date(d);
//			System.out.println(date);
		}
		endM = System.currentTimeMillis();
		System.out.println("BestDateMethod Method: 2 (benchmark 1096)" + (endM-startM));
		
		//Method 3
//		startM = System.currentTimeMillis();
//		for ( int i=0; i<max; i++ ) {
//			jodaDate = DateTime.now().withTime(0, 0, 0, 0);
////			date = new Date(jodaDate.getMillis());
////			System.out.println(date);
//		}
//		endM = System.currentTimeMillis();
//		System.out.println("Method: 3" + (endM-startM));
	}
	
	/**
	 * Compare the hashmap and switch instanceof
	 */
	@Test
	public void testSwitch() {
		int max = 100000;
		long start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			com.xinqihd.sns.gameserver.transport.MessageToId.messageToId((MessageLite)msgObjects[i%msgObjects.length]);
		}
		long end = System.currentTimeMillis();
		System.out.println("Switch total time: " + (end-start));
	}

	@Test
	public void testHashMap() {
		int max = 100000;
		Map map = new HashMap();
		long start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.put("test"+i, "value"+i);
			map.get("test"+i);
			map.remove("test"+i);
		}
		long end = System.currentTimeMillis();
		System.out.println("HashMap total time: " + (end-start));
		
		map = new LinkedHashMap();
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.put("test"+i, "value"+i);
			map.get("test"+i);
			map.remove("test"+i);
		}
		end = System.currentTimeMillis();
		System.out.println("LinkedHashMap total time: " + (end-start));
	}
	
	@Test
	public void testInteger() {
		AtomicInteger atom = new AtomicInteger(0);
		int primt = 0;
		
		long startM = 0l, endM = 0l;
		int max = 10000000;
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			atom.getAndIncrement();
		}
		endM = System.currentTimeMillis();
		System.out.println("AtomicInteger performance: " + (endM - startM));
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			primt++;
		}
		endM = System.currentTimeMillis();
		System.out.println("int performance: " + (endM - startM));
	}
	
	@Test
	public void testSearch() {
		String[] arrays = {"friend", "guild", "rival", "black", "recent"};
		Arrays.sort(arrays);
		Random r = new Random();
		long startM = 0l, endM = 0l;
		int max = 10000000;
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			String expect = arrays[r.nextInt(arrays.length)];
			int idx = Arrays.binarySearch(arrays, expect);
		}
		endM = System.currentTimeMillis();
		System.out.println("binarysearch performance: " + (endM - startM));
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			String expect = arrays[r.nextInt(arrays.length)];
			for ( int idx=0; idx<arrays.length; idx++ ) {
				if ( arrays[idx].equals(expect) ) {
					continue;
				}
			}
		}
		endM = System.currentTimeMillis();
		System.out.println("loopsearch performance: " + (endM - startM));
	}
	
}
