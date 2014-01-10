package com.xinqihd.sns.gameserver.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.handler.*;
import com.xinqihd.sns.gameserver.proto.*;

/**
 * GENERATE SOURCE CODE. DO NOT MODIFY!
 * Get to proper message object according to the given message type.
 * @author wangqi
 *
 */
public class MessageToHandler extends SimpleChannelHandler {

  private static Log log = LogFactory.getLog(MessageToHandler.class); 

  public static SimpleChannelHandler messageToHandler(Object msgObject) {
  	XinqiMessage message = null;
  	if ( msgObject instanceof XinqiMessage ) {
  		message = (XinqiMessage)msgObject;
  	} else {
  		if ( log.isWarnEnabled() ) {
  			log.warn("msgObject is not XinqiMessage.");
  		}
  	}
    if (message.payload instanceof XinqiBceAcceptDailyAward.BceAcceptDailyAward ) {
  		return BceAcceptDailyAwardHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceAddFriend.BceAddFriend ) {
  		return BceAddFriendHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceAnswerInvite.BceAnswerInvite ) {
  		return BceAnswerInviteHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceArmStrength.BceArmStrength ) {
  		return BceArmStrengthHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceArrangeList.BceArrangeList ) {
  		return BceArrangeListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceAskRoundOver.BceAskRoundOver ) {
  		return BceAskRoundOverHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBagTidy.BceBagTidy ) {
  		return BceBagTidyHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBattlePickBox.BceBattlePickBox ) {
  		return BceBattlePickBoxHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBattleReward.BceBattleReward ) {
  		return BceBattleRewardHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBattleRewardSelect.BceBattleRewardSelect ) {
  		return BceBattleRewardSelectHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBattleStageReady.BceBattleStageReady ) {
  		return BceBattleStageReadyHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBullet.BceBullet ) {
  		return BceBulletHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBuyProp.BceBuyProp ) {
  		return BceBuyPropHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBuyTool.BceBuyTool ) {
  		return BceBuyToolHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBuyVip.BceBuyVip ) {
  		return BceBuyVipHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChangeMap.BceChangeMap ) {
  		return BceChangeMapHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChangeSeat.BceChangeSeat ) {
  		return BceChangeSeatHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCharge.BceCharge ) {
  		return BceChargeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChat.BceChat ) {
  		return BceChatHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChgBtlType.BceChgBtlType ) {
  		return BceChgBtlTypeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChgShootMode.BceChgShootMode ) {
  		return BceChgShootModeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceClearLeaveMessage.BceClearLeaveMessage ) {
  		return BceClearLeaveMessageHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCompose.BceCompose ) {
  		return BceComposeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceConfigData.BceConfigData ) {
  		return BceConfigDataHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCreateGuild.BceCreateGuild ) {
  		return BceCreateGuildHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceDailyMark.BceDailyMark ) {
  		return BceDailyMarkHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceDead.BceDead ) {
  		return BceDeadHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceDebug.BceDebug ) {
  		return BceDebugHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceDoubleExpOver.BceDoubleExpOver ) {
  		return BceDoubleExpOverHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceEditSeat.BceEditSeat ) {
  		return BceEditSeatHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceEnterGuild.BceEnterGuild ) {
  		return BceEnterGuildHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceEnterHall.BceEnterHall ) {
  		return BceEnterHallHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceEnterRoom.BceEnterRoom ) {
  		return BceEnterRoomHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceExitGuild.BceExitGuild ) {
  		return BceExitGuildHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceFindRoom.BceFindRoom ) {
  		return BceFindRoomHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceForge.BceForge ) {
  		return BceForgeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceFriendInfo.BceFriendInfo ) {
  		return BceFriendInfoHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGhostMove.BceGhostMove ) {
  		return BceGhostMoveHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGhostMoveStart.BceGhostMoveStart ) {
  		return BceGhostMoveStartHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGhostMoveStop.BceGhostMoveStop ) {
  		return BceGhostMoveStopHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuideFinish.BceGuideFinish ) {
  		return BceGuideFinishHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuideStep.BceGuideStep ) {
  		return BceGuideStepHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildAcceptMember.BceGuildAcceptMember ) {
  		return BceGuildAcceptMemberHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildBuy.BceGuildBuy ) {
  		return BceGuildBuyHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildContribute.BceGuildContribute ) {
  		return BceGuildContributeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildDismiss.BceGuildDismiss ) {
  		return BceGuildDismissHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildFire.BceGuildFire ) {
  		return BceGuildFireHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildInvite.BceGuildInvite ) {
  		return BceGuildInviteHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildInviteRsp.BceGuildInviteRsp ) {
  		return BceGuildInviteRspHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildIronLevelup.BceGuildIronLevelup ) {
  		return BceGuildIronLevelupHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildLevelUp.BceGuildLevelUp ) {
  		return BceGuildLevelUpHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildLimit.BceGuildLimit ) {
  		return BceGuildLimitHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildPosChange.BceGuildPosChange ) {
  		return BceGuildPosChangeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildReqCancel.BceGuildReqCancel ) {
  		return BceGuildReqCancelHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildReqMemberList.BceGuildReqMemberList ) {
  		return BceGuildReqMemberListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildRequest.BceGuildRequest ) {
  		return BceGuildRequestHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildSearch.BceGuildSearch ) {
  		return BceGuildSearchHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildSetPosName.BceGuildSetPosName ) {
  		return BceGuildSetPosNameHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildShopLevelup.BceGuildShopLevelup ) {
  		return BceGuildShopLevelupHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildTransfer.BceGuildTransfer ) {
  		return BceGuildTransferHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceHallRoomList.BceHallRoomList ) {
  		return BceHallRoomListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceHallUserList.BceHallUserList ) {
  		return BceHallUserListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceHarvestTree.BceHarvestTree ) {
  		return BceHarvestTreeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceHeartbeat.BceHeartbeat ) {
  		return BceHeartbeatHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceInit.BceInit ) {
  		return BceInitHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceInvite.BceInvite ) {
  		return BceInviteHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceKickUser.BceKickUser ) {
  		return BceKickUserHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLeaveHall.BceLeaveHall ) {
  		return BceLeaveHallHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLeaveMessage.BceLeaveMessage ) {
  		return BceLeaveMessageHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLeaveRoom.BceLeaveRoom ) {
  		return BceLeaveRoomHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLengthenIndate.BceLengthenIndate ) {
  		return BceLengthenIndateHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLoadProgress.BceLoadProgress ) {
  		return BceLoadProgressHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLogin.BceLogin ) {
  		return BceLoginHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLottery.BceLottery ) {
  		return BceLotteryHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceMergeProp.BceMergeProp ) {
  		return BceMergePropHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceMoveProp.BceMoveProp ) {
  		return BceMovePropHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceMyRankInfo.BceMyRankInfo ) {
  		return BceMyRankInfoHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceOnlineFrd.BceOnlineFrd ) {
  		return BceOnlineFrdHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceOnlineReward.BceOnlineReward ) {
  		return BceOnlineRewardHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBcePRCEnter.BcePRCEnter ) {
  		return BcePRCEnterHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBcePRCOpen.BcePRCOpen ) {
  		return BcePRCOpenHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBcePickGold.BcePickGold ) {
  		return BcePickGoldHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceReadyStart.BceReadyStart ) {
  		return BceReadyStartHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRegister.BceRegister ) {
  		return BceRegisterHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRequestFriendInfo.BceRequestFriendInfo ) {
  		return BceRequestFriendInfoHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRobotOver.BceRobotOver ) {
  		return BceRobotOverHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoleAttack.BceRoleAttack ) {
  		return BceRoleAttackHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoleChangeDirection.BceRoleChangeDirection ) {
  		return BceRoleChangeDirectionHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoleMove.BceRoleMove ) {
  		return BceRoleMoveHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoleMoveStart.BceRoleMoveStart ) {
  		return BceRoleMoveStartHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoleMoveStop.BceRoleMoveStop ) {
  		return BceRoleMoveStopHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRolePower.BceRolePower ) {
  		return BceRolePowerHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoleUseTool.BceRoleUseTool ) {
  		return BceRoleUseToolHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoundOver.BceRoundOver ) {
  		return BceRoundOverHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSellProp.BceSellProp ) {
  		return BceSellPropHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSellTool.BceSellTool ) {
  		return BceSellToolHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSetGuildAnno.BceSetGuildAnno ) {
  		return BceSetGuildAnnoHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceShopping.BceShopping ) {
  		return BceShoppingHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceShowUserInfo.BceShowUserInfo ) {
  		return BceShowUserInfoHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSplitProp.BceSplitProp ) {
  		return BceSplitPropHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSyncPos.BceSyncPos ) {
  		return BceSyncPosHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceTaskReward.BceTaskReward ) {
  		return BceTaskRewardHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceTransfer.BceTransfer ) {
  		return BceTransferHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUseProp.BceUseProp ) {
  		return BceUsePropHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUserConfig.BceUserConfig ) {
  		return BceUserConfigHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUserGuidAtk.BceUserGuidAtk ) {
  		return BceUserGuidAtkHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceVisit.BceVisit ) {
  		return BceVisitHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceWaterTree.BceWaterTree ) {
  		return BceWaterTreeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLoginReady.BceLoginReady ) {
  		return BceLoginReadyHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSendGift.BceSendGift ) {
  		return BceSendGiftHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBulletTrack.BceBulletTrack ) {
  		return BceBulletTrackHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceConfirm.BceConfirm ) {
  		return BceConfirmHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceFindFriend.BceFindFriend ) {
  		return BceFindFriendHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUserRefresh.BceUserRefresh ) {
  		return BceUserRefreshHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceLogout.BceLogout ) {
  		return BceLogoutHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceWeibo.BceWeibo ) {
  		return BceWeiboHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceRoomUserList.BceRoomUserList ) {
  		return BceRoomUserListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSysMessage.BceSysMessage ) {
  		return BceSysMessageHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceForgetPassword.BceForgetPassword ) {
  		return BceForgetPasswordHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCloseBag.BceCloseBag ) {
  		return BceCloseBagHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceTraining.BceTraining ) {
  		return BceTrainingHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceTrack.BceTrack ) {
  		return BceTrackHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceMailDelete.BceMailDelete ) {
  		return BceMailDeleteHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceMailSend.BceMailSend ) {
  		return BceMailSendHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceMailRead.BceMailRead ) {
  		return BceMailReadHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceMailTake.BceMailTake ) {
  		return BceMailTakeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBossList.BceBossList ) {
  		return BceBossListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBossTakeReward.BceBossTakeReward ) {
  		return BceBossTakeRewardHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCraftPrice.BceCraftPrice ) {
  		return BceCraftPriceHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceActionLimitQuery.BceActionLimitQuery ) {
  		return BceActionLimitQueryHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceActionLimitBuy.BceActionLimitBuy ) {
  		return BceActionLimitBuyHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCaishenPray.BceCaishenPray ) {
  		return BceCaishenPrayHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCaishenQuery.BceCaishenQuery ) {
  		return BceCaishenQueryHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceTreasureHuntBuy.BceTreasureHuntBuy ) {
  		return BceTreasureHuntBuyHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceTreasureHuntQuery.BceTreasureHuntQuery ) {
  		return BceTreasureHuntQueryHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChargeInternal.BceChargeInternal ) {
  		return BceChargeInternalHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceOfflinePush.BceOfflinePush ) {
  		return BceOfflinePushHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceReloadConfig.BceReloadConfig ) {
  		return BceReloadConfigHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCyLogin.BceCyLogin ) {
  		return BceCyLoginHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCyRegister.BceCyRegister ) {
  		return BceCyRegisterHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceCreateRole.BceCreateRole ) {
  		return BceCreateRoleHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUserSync.BceUserSync ) {
  		return BceUserSyncHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGetValue.BceGetValue ) {
  		return BceGetValueHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceBulletin.BceBulletin ) {
  		return BceBulletinHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChargeCard.BceChargeCard ) {
  		return BceChargeCardHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBcePropDataQuery.BcePropDataQuery ) {
  		return BcePropDataQueryHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceServerList.BceServerList ) {
  		return BceServerListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUserInput.BceUserInput ) {
  		return BceUserInputHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUserRoleList.BceUserRoleList ) {
  		return BceUserRoleListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceUserStatusList.BceUserStatusList ) {
  		return BceUserStatusListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceVipInfo.BceVipInfo ) {
  		return BceVipInfoHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceSelectRole.BceSelectRole ) {
  		return BceSelectRoleHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceDeleteRole.BceDeleteRole ) {
  		return BceDeleteRoleHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceChangeAutomode.BceChangeAutomode ) {
  		return BceChangeAutomodeHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceExitGame.BceExitGame ) {
  		return BceExitGameHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceFillProfile.BceFillProfile ) {
  		return BceFillProfileHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGetUserBiblio.BceGetUserBiblio ) {
  		return BceGetUserBiblioHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceTakeUserBiblioReward.BceTakeUserBiblioReward ) {
  		return BceTakeUserBiblioRewardHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceVoiceChat.BceVoiceChat ) {
  		return BceVoiceChatHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildAbilityList.BceGuildAbilityList ) {
  		return BceGuildAbilityListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildApplyList.BceGuildApplyList ) {
  		return BceGuildApplyListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildApplyProcess.BceGuildApplyProcess ) {
  		return BceGuildApplyProcessHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildBag.BceGuildBag ) {
  		return BceGuildBagHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildBagEvent.BceGuildBagEvent ) {
  		return BceGuildBagEventHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildChangeAnnounce.BceGuildChangeAnnounce ) {
  		return BceGuildChangeAnnounceHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildChangeRole.BceGuildChangeRole ) {
  		return BceGuildChangeRoleHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildContributeQuery.BceGuildContributeQuery ) {
  		return BceGuildContributeQueryHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildFacilityLevelList.BceGuildFacilityLevelList ) {
  		return BceGuildFacilityLevelListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildFacilityLevelUp.BceGuildFacilityLevelUp ) {
  		return BceGuildFacilityLevelUpHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildFacilityList.BceGuildFacilityList ) {
  		return BceGuildFacilityListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildList.BceGuildList ) {
  		return BceGuildListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildMemberList.BceGuildMemberList ) {
  		return BceGuildMemberListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildPrivilegeList.BceGuildPrivilegeList ) {
  		return BceGuildPrivilegeListHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildSearchMember.BceGuildSearchMember ) {
  		return BceGuildSearchMemberHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildShopping.BceGuildShopping ) {
  		return BceGuildShoppingHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGetVoiceChat.BceGetVoiceChat ) {
  		return BceGetVoiceChatHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildApply.BceGuildApply ) {
  		return BceGuildApplyHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceForbidUser.BceForbidUser ) {
  		return BceForbidUserHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildBagPut.BceGuildBagPut ) {
  		return BceGuildBagPutHandler.getInstance();
    }
    else if (message.payload instanceof XinqiBceGuildBagTake.BceGuildBagTake ) {
  		return BceGuildBagTakeHandler.getInstance();
    }
  	return null;
  }
  
}
