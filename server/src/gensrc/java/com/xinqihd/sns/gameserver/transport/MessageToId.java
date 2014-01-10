package com.xinqihd.sns.gameserver.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.proto.*;

/**
 * GENERATED SOURCE CODE DO NOT MODIFY!
 * Translate the given message to its corresponding id. 
 * @author wangqi 
 */ 
public class MessageToId {

  private static Log log = LogFactory.getLog(MessageToId.class); 

  public static int messageToId(MessageLite msg) { 
    if (msg instanceof XinqiBseAddFriend.BseAddFriend ) {
      return 512; 
    }
    else if (msg instanceof XinqiBseAddProp.BseAddProp ) {
      return 513; 
    }
    else if (msg instanceof XinqiBseAddTask.BseAddTask ) {
      return 514; 
    }
    else if (msg instanceof XinqiBseAnswerInvite.BseAnswerInvite ) {
      return 515; 
    }
    else if (msg instanceof XinqiBseArrangeList.BseArrangeList ) {
      return 516; 
    }
    else if (msg instanceof XinqiBseAskRoundOver.BseAskRoundOver ) {
      return 517; 
    }
    else if (msg instanceof XinqiBseAuthState.BseAuthState ) {
      return 518; 
    }
    else if (msg instanceof XinqiBseBattleAlter.BseBattleAlter ) {
      return 519; 
    }
    else if (msg instanceof XinqiBseBattleInit.BseBattleInit ) {
      return 520; 
    }
    else if (msg instanceof XinqiBseBattleOver.BseBattleOver ) {
      return 521; 
    }
    else if (msg instanceof XinqiBseBattlePickBox.BseBattlePickBox ) {
      return 522; 
    }
    else if (msg instanceof XinqiBseBattleReward.BseBattleReward ) {
      return 523; 
    }
    else if (msg instanceof XinqiBseBroadcastMessage.BseBroadcastMessage ) {
      return 524; 
    }
    else if (msg instanceof XinqiBseBulltinList.BseBulltinList ) {
      return 525; 
    }
    else if (msg instanceof XinqiBseBuyProp.BseBuyProp ) {
      return 526; 
    }
    else if (msg instanceof XinqiBseBuyVip.BseBuyVip ) {
      return 527; 
    }
    else if (msg instanceof XinqiBseChangeMap.BseChangeMap ) {
      return 528; 
    }
    else if (msg instanceof XinqiBseChangeSeat.BseChangeSeat ) {
      return 529; 
    }
    else if (msg instanceof XinqiBseCharge.BseCharge ) {
      return 530; 
    }
    else if (msg instanceof XinqiBseChargeList.BseChargeList ) {
      return 531; 
    }
    else if (msg instanceof XinqiBseChat.BseChat ) {
      return 532; 
    }
    else if (msg instanceof XinqiBseChgBtlType.BseChgBtlType ) {
      return 533; 
    }
    else if (msg instanceof XinqiBseClearLeaveMessage.BseClearLeaveMessage ) {
      return 534; 
    }
    else if (msg instanceof XinqiBseClearRole.BseClearRole ) {
      return 535; 
    }
    else if (msg instanceof XinqiBseCompose.BseCompose ) {
      return 536; 
    }
    else if (msg instanceof XinqiBseConfigData.BseConfigData ) {
      return 537; 
    }
    else if (msg instanceof XinqiBseCreateGuild.BseCreateGuild ) {
      return 538; 
    }
    else if (msg instanceof XinqiBseDailyMark.BseDailyMark ) {
      return 539; 
    }
    else if (msg instanceof XinqiBseDailyMarkList.BseDailyMarkList ) {
      return 540; 
    }
    else if (msg instanceof XinqiBseDead.BseDead ) {
      return 541; 
    }
    else if (msg instanceof XinqiBseDelTask.BseDelTask ) {
      return 542; 
    }
    else if (msg instanceof XinqiBseDoubleExpTime.BseDoubleExpTime ) {
      return 543; 
    }
    else if (msg instanceof XinqiBseEditSeat.BseEditSeat ) {
      return 544; 
    }
    else if (msg instanceof XinqiBseEnterGuild.BseEnterGuild ) {
      return 545; 
    }
    else if (msg instanceof XinqiBseEnterHall.BseEnterHall ) {
      return 546; 
    }
    else if (msg instanceof XinqiBseEnterRoom.BseEnterRoom ) {
      return 547; 
    }
    else if (msg instanceof XinqiBseEquipment.BseEquipment ) {
      return 548; 
    }
    else if (msg instanceof XinqiBseError.BseError ) {
      return 549; 
    }
    else if (msg instanceof XinqiBseExitGuild.BseExitGuild ) {
      return 550; 
    }
    else if (msg instanceof XinqiBseFindRoom.BseFindRoom ) {
      return 551; 
    }
    else if (msg instanceof XinqiBseForge.BseForge ) {
      return 552; 
    }
    else if (msg instanceof XinqiBseFriendEquip.BseFriendEquip ) {
      return 553; 
    }
    else if (msg instanceof XinqiBseFriendList.BseFriendList ) {
      return 554; 
    }
    else if (msg instanceof XinqiBseFriendOnlineStatus.BseFriendOnlineStatus ) {
      return 555; 
    }
    else if (msg instanceof XinqiBseFriendsInfo.BseFriendsInfo ) {
      return 556; 
    }
    else if (msg instanceof XinqiBseGameDataKey.BseGameDataKey ) {
      return 557; 
    }
    else if (msg instanceof XinqiBseGhostMove.BseGhostMove ) {
      return 558; 
    }
    else if (msg instanceof XinqiBseGhostMoveStart.BseGhostMoveStart ) {
      return 559; 
    }
    else if (msg instanceof XinqiBseGhostMoveStop.BseGhostMoveStop ) {
      return 560; 
    }
    else if (msg instanceof XinqiBseGiveProp.BseGiveProp ) {
      return 561; 
    }
    else if (msg instanceof XinqiBseGuildAcceptMember.BseGuildAcceptMember ) {
      return 562; 
    }
    else if (msg instanceof XinqiBseGuildBuy.BseGuildBuy ) {
      return 563; 
    }
    else if (msg instanceof XinqiBseGuildContribute.BseGuildContribute ) {
      return 564; 
    }
    else if (msg instanceof XinqiBseGuildCostAlert.BseGuildCostAlert ) {
      return 565; 
    }
    else if (msg instanceof XinqiBseGuildDismiss.BseGuildDismiss ) {
      return 566; 
    }
    else if (msg instanceof XinqiBseGuildFire.BseGuildFire ) {
      return 567; 
    }
    else if (msg instanceof XinqiBseGuildInvite.BseGuildInvite ) {
      return 568; 
    }
    else if (msg instanceof XinqiBseGuildIronLevelup.BseGuildIronLevelup ) {
      return 569; 
    }
    else if (msg instanceof XinqiBseGuildLevelUp.BseGuildLevelUp ) {
      return 570; 
    }
    else if (msg instanceof XinqiBseGuildLimit.BseGuildLimit ) {
      return 571; 
    }
    else if (msg instanceof XinqiBseGuildList.BseGuildList ) {
      return 572; 
    }
    else if (msg instanceof XinqiBseGuildNews.BseGuildNews ) {
      return 573; 
    }
    else if (msg instanceof XinqiBseGuildPosChange.BseGuildPosChange ) {
      return 574; 
    }
    else if (msg instanceof XinqiBseGuildReqCancel.BseGuildReqCancel ) {
      return 575; 
    }
    else if (msg instanceof XinqiBseGuildReqMemberList.BseGuildReqMemberList ) {
      return 576; 
    }
    else if (msg instanceof XinqiBseGuildRequest.BseGuildRequest ) {
      return 577; 
    }
    else if (msg instanceof XinqiBseGuildSearch.BseGuildSearch ) {
      return 578; 
    }
    else if (msg instanceof XinqiBseGuildSetPosName.BseGuildSetPosName ) {
      return 579; 
    }
    else if (msg instanceof XinqiBseGuildShopLevelup.BseGuildShopLevelup ) {
      return 580; 
    }
    else if (msg instanceof XinqiBseGuildTransfer.BseGuildTransfer ) {
      return 581; 
    }
    else if (msg instanceof XinqiBseHallRoomList.BseHallRoomList ) {
      return 582; 
    }
    else if (msg instanceof XinqiBseHallUserList.BseHallUserList ) {
      return 583; 
    }
    else if (msg instanceof XinqiBseHarvestTree.BseHarvestTree ) {
      return 584; 
    }
    else if (msg instanceof XinqiBseHeartbeat.BseHeartbeat ) {
      return 585; 
    }
    else if (msg instanceof XinqiBseInit.BseInit ) {
      return 586; 
    }
    else if (msg instanceof XinqiBseInvite.BseInvite ) {
      return 587; 
    }
    else if (msg instanceof XinqiBseInviteFrd.BseInviteFrd ) {
      return 588; 
    }
    else if (msg instanceof XinqiBseInviteGuildList.BseInviteGuildList ) {
      return 589; 
    }
    else if (msg instanceof XinqiBseInviteRtn.BseInviteRtn ) {
      return 590; 
    }
    else if (msg instanceof XinqiBseItem.BseItem ) {
      return 591; 
    }
    else if (msg instanceof XinqiBseJoinGuild.BseJoinGuild ) {
      return 592; 
    }
    else if (msg instanceof XinqiBseKickUser.BseKickUser ) {
      return 593; 
    }
    else if (msg instanceof XinqiBseLeaveHall.BseLeaveHall ) {
      return 594; 
    }
    else if (msg instanceof XinqiBseLeaveMessage.BseLeaveMessage ) {
      return 595; 
    }
    else if (msg instanceof XinqiBseLeaveRoom.BseLeaveRoom ) {
      return 596; 
    }
    else if (msg instanceof XinqiBseLengthenIndate.BseLengthenIndate ) {
      return 597; 
    }
    else if (msg instanceof XinqiBseLoadProgress.BseLoadProgress ) {
      return 598; 
    }
    else if (msg instanceof XinqiBseLogin.BseLogin ) {
      return 599; 
    }
    else if (msg instanceof XinqiBseLoginLottery.BseLoginLottery ) {
      return 600; 
    }
    else if (msg instanceof XinqiBseLottery.BseLottery ) {
      return 601; 
    }
    else if (msg instanceof XinqiBseLotteryOver.BseLotteryOver ) {
      return 602; 
    }
    else if (msg instanceof XinqiBseMap.BseMap ) {
      return 603; 
    }
    else if (msg instanceof XinqiBseMatchingRoom.BseMatchingRoom ) {
      return 604; 
    }
    else if (msg instanceof XinqiBseMergeProp.BseMergeProp ) {
      return 605; 
    }
    else if (msg instanceof XinqiBseMessage.BseMessage ) {
      return 606; 
    }
    else if (msg instanceof XinqiBseModiTask.BseModiTask ) {
      return 607; 
    }
    else if (msg instanceof XinqiBseMoveProp.BseMoveProp ) {
      return 608; 
    }
    else if (msg instanceof XinqiBseMyRankInfo.BseMyRankInfo ) {
      return 609; 
    }
    else if (msg instanceof XinqiBseNotify.BseNotify ) {
      return 610; 
    }
    else if (msg instanceof XinqiBseOnlineFrd.BseOnlineFrd ) {
      return 611; 
    }
    else if (msg instanceof XinqiBseOnlineReward.BseOnlineReward ) {
      return 612; 
    }
    else if (msg instanceof XinqiBsePRCOpen.BsePRCOpen ) {
      return 613; 
    }
    else if (msg instanceof XinqiBsePickGold.BsePickGold ) {
      return 614; 
    }
    else if (msg instanceof XinqiBsePropInfoList.BsePropInfoList ) {
      return 615; 
    }
    else if (msg instanceof XinqiBsePropList.BsePropList ) {
      return 616; 
    }
    else if (msg instanceof XinqiBseRecentlyFriendList.BseRecentlyFriendList ) {
      return 617; 
    }
    else if (msg instanceof XinqiBseRecentlyMessageList.BseRecentlyMessageList ) {
      return 618; 
    }
    else if (msg instanceof XinqiBseRegister.BseRegister ) {
      return 619; 
    }
    else if (msg instanceof XinqiBseRequestFriendInfo.BseRequestFriendInfo ) {
      return 620; 
    }
    else if (msg instanceof XinqiBseRequestGuildList.BseRequestGuildList ) {
      return 621; 
    }
    else if (msg instanceof XinqiBseRoleAttack.BseRoleAttack ) {
      return 622; 
    }
    else if (msg instanceof XinqiBseRoleBattleInfo.BseRoleBattleInfo ) {
      return 623; 
    }
    else if (msg instanceof XinqiBseRoleChangeDirection.BseRoleChangeDirection ) {
      return 624; 
    }
    else if (msg instanceof XinqiBseRoleConfig.BseRoleConfig ) {
      return 625; 
    }
    else if (msg instanceof XinqiBseRoleInfo.BseRoleInfo ) {
      return 626; 
    }
    else if (msg instanceof XinqiBseRoleMove.BseRoleMove ) {
      return 627; 
    }
    else if (msg instanceof XinqiBseRoleMoveStart.BseRoleMoveStart ) {
      return 628; 
    }
    else if (msg instanceof XinqiBseRoleMoveStop.BseRoleMoveStop ) {
      return 629; 
    }
    else if (msg instanceof XinqiBseRolePower.BseRolePower ) {
      return 630; 
    }
    else if (msg instanceof XinqiBseRoleUseTool.BseRoleUseTool ) {
      return 631; 
    }
    else if (msg instanceof XinqiBseRoomCountdown.BseRoomCountdown ) {
      return 632; 
    }
    else if (msg instanceof XinqiBseRoomInfo.BseRoomInfo ) {
      return 633; 
    }
    else if (msg instanceof XinqiBseRoundStart.BseRoundStart ) {
      return 634; 
    }
    else if (msg instanceof XinqiBseSellProp.BseSellProp ) {
      return 635; 
    }
    else if (msg instanceof XinqiBseSetGuildAnno.BseSetGuildAnno ) {
      return 636; 
    }
    else if (msg instanceof XinqiBseShop.BseShop ) {
      return 637; 
    }
    else if (msg instanceof XinqiBseShopping.BseShopping ) {
      return 638; 
    }
    else if (msg instanceof XinqiBseShowUserInfo.BseShowUserInfo ) {
      return 639; 
    }
    else if (msg instanceof XinqiBseSplitProp.BseSplitProp ) {
      return 640; 
    }
    else if (msg instanceof XinqiBseSvrUnixHour.BseSvrUnixHour ) {
      return 641; 
    }
    else if (msg instanceof XinqiBseSyncPos.BseSyncPos ) {
      return 642; 
    }
    else if (msg instanceof XinqiBseSysChat.BseSysChat ) {
      return 643; 
    }
    else if (msg instanceof XinqiBseSysMessage.BseSysMessage ) {
      return 644; 
    }
    else if (msg instanceof XinqiBseTask.BseTask ) {
      return 645; 
    }
    else if (msg instanceof XinqiBseTaskList.BseTaskList ) {
      return 646; 
    }
    else if (msg instanceof XinqiBseTip.BseTip ) {
      return 647; 
    }
    else if (msg instanceof XinqiBseToolList.BseToolList ) {
      return 648; 
    }
    else if (msg instanceof XinqiBseTransfer.BseTransfer ) {
      return 649; 
    }
    else if (msg instanceof XinqiBseUpdateOnlineStatus.BseUpdateOnlineStatus ) {
      return 650; 
    }
    else if (msg instanceof XinqiBseUseProp.BseUseProp ) {
      return 651; 
    }
    else if (msg instanceof XinqiBseUseTool.BseUseTool ) {
      return 652; 
    }
    else if (msg instanceof XinqiBseUserEnterRoom.BseUserEnterRoom ) {
      return 653; 
    }
    else if (msg instanceof XinqiBseUserGuildData.BseUserGuildData ) {
      return 654; 
    }
    else if (msg instanceof XinqiBseUserLeaveRoom.BseUserLeaveRoom ) {
      return 655; 
    }
    else if (msg instanceof XinqiBseUserReadyStart.BseUserReadyStart ) {
      return 656; 
    }
    else if (msg instanceof XinqiBseVipPeriodList.BseVipPeriodList ) {
      return 657; 
    }
    else if (msg instanceof XinqiBseVisit.BseVisit ) {
      return 658; 
    }
    else if (msg instanceof XinqiBseWaterTree.BseWaterTree ) {
      return 659; 
    }
    else if (msg instanceof XinqiBseWebBuyFinishedOnce.BseWebBuyFinishedOnce ) {
      return 660; 
    }
    else if (msg instanceof XinqiBceAcceptDailyAward.BceAcceptDailyAward ) {
      return 1024; 
    }
    else if (msg instanceof XinqiBceAddFriend.BceAddFriend ) {
      return 1025; 
    }
    else if (msg instanceof XinqiBceAnswerInvite.BceAnswerInvite ) {
      return 1026; 
    }
    else if (msg instanceof XinqiBceArmStrength.BceArmStrength ) {
      return 1027; 
    }
    else if (msg instanceof XinqiBceArrangeList.BceArrangeList ) {
      return 1028; 
    }
    else if (msg instanceof XinqiBceAskRoundOver.BceAskRoundOver ) {
      return 1029; 
    }
    else if (msg instanceof XinqiBceBagTidy.BceBagTidy ) {
      return 1030; 
    }
    else if (msg instanceof XinqiBceBattlePickBox.BceBattlePickBox ) {
      return 1031; 
    }
    else if (msg instanceof XinqiBceBattleReward.BceBattleReward ) {
      return 1032; 
    }
    else if (msg instanceof XinqiBceBattleRewardSelect.BceBattleRewardSelect ) {
      return 1033; 
    }
    else if (msg instanceof XinqiBceBattleStageReady.BceBattleStageReady ) {
      return 1034; 
    }
    else if (msg instanceof XinqiBceBullet.BceBullet ) {
      return 1035; 
    }
    else if (msg instanceof XinqiBceBuyProp.BceBuyProp ) {
      return 1036; 
    }
    else if (msg instanceof XinqiBceBuyTool.BceBuyTool ) {
      return 1037; 
    }
    else if (msg instanceof XinqiBceBuyVip.BceBuyVip ) {
      return 1038; 
    }
    else if (msg instanceof XinqiBceChangeMap.BceChangeMap ) {
      return 1039; 
    }
    else if (msg instanceof XinqiBceChangeSeat.BceChangeSeat ) {
      return 1040; 
    }
    else if (msg instanceof XinqiBceCharge.BceCharge ) {
      return 1041; 
    }
    else if (msg instanceof XinqiBceChat.BceChat ) {
      return 1042; 
    }
    else if (msg instanceof XinqiBceChgBtlType.BceChgBtlType ) {
      return 1043; 
    }
    else if (msg instanceof XinqiBceChgShootMode.BceChgShootMode ) {
      return 1044; 
    }
    else if (msg instanceof XinqiBceClearLeaveMessage.BceClearLeaveMessage ) {
      return 1045; 
    }
    else if (msg instanceof XinqiBceCompose.BceCompose ) {
      return 1046; 
    }
    else if (msg instanceof XinqiBceConfigData.BceConfigData ) {
      return 1047; 
    }
    else if (msg instanceof XinqiBceCreateGuild.BceCreateGuild ) {
      return 1048; 
    }
    else if (msg instanceof XinqiBceDailyMark.BceDailyMark ) {
      return 1049; 
    }
    else if (msg instanceof XinqiBceDead.BceDead ) {
      return 1050; 
    }
    else if (msg instanceof XinqiBceDebug.BceDebug ) {
      return 1051; 
    }
    else if (msg instanceof XinqiBceDoubleExpOver.BceDoubleExpOver ) {
      return 1052; 
    }
    else if (msg instanceof XinqiBceEditSeat.BceEditSeat ) {
      return 1053; 
    }
    else if (msg instanceof XinqiBceEnterGuild.BceEnterGuild ) {
      return 1054; 
    }
    else if (msg instanceof XinqiBceEnterHall.BceEnterHall ) {
      return 1055; 
    }
    else if (msg instanceof XinqiBceEnterRoom.BceEnterRoom ) {
      return 1056; 
    }
    else if (msg instanceof XinqiBceExitGuild.BceExitGuild ) {
      return 1057; 
    }
    else if (msg instanceof XinqiBceFindRoom.BceFindRoom ) {
      return 1058; 
    }
    else if (msg instanceof XinqiBceForge.BceForge ) {
      return 1059; 
    }
    else if (msg instanceof XinqiBceFriendInfo.BceFriendInfo ) {
      return 1060; 
    }
    else if (msg instanceof XinqiBceGhostMove.BceGhostMove ) {
      return 1061; 
    }
    else if (msg instanceof XinqiBceGhostMoveStart.BceGhostMoveStart ) {
      return 1062; 
    }
    else if (msg instanceof XinqiBceGhostMoveStop.BceGhostMoveStop ) {
      return 1063; 
    }
    else if (msg instanceof XinqiBceGuideFinish.BceGuideFinish ) {
      return 1064; 
    }
    else if (msg instanceof XinqiBceGuideStep.BceGuideStep ) {
      return 1065; 
    }
    else if (msg instanceof XinqiBceGuildAcceptMember.BceGuildAcceptMember ) {
      return 1066; 
    }
    else if (msg instanceof XinqiBceGuildBuy.BceGuildBuy ) {
      return 1067; 
    }
    else if (msg instanceof XinqiBceGuildContribute.BceGuildContribute ) {
      return 1068; 
    }
    else if (msg instanceof XinqiBceGuildDismiss.BceGuildDismiss ) {
      return 1069; 
    }
    else if (msg instanceof XinqiBceGuildFire.BceGuildFire ) {
      return 1070; 
    }
    else if (msg instanceof XinqiBceGuildInvite.BceGuildInvite ) {
      return 1071; 
    }
    else if (msg instanceof XinqiBceGuildInviteRsp.BceGuildInviteRsp ) {
      return 1072; 
    }
    else if (msg instanceof XinqiBceGuildIronLevelup.BceGuildIronLevelup ) {
      return 1073; 
    }
    else if (msg instanceof XinqiBceGuildLevelUp.BceGuildLevelUp ) {
      return 1074; 
    }
    else if (msg instanceof XinqiBceGuildLimit.BceGuildLimit ) {
      return 1075; 
    }
    else if (msg instanceof XinqiBceGuildPosChange.BceGuildPosChange ) {
      return 1076; 
    }
    else if (msg instanceof XinqiBceGuildReqCancel.BceGuildReqCancel ) {
      return 1077; 
    }
    else if (msg instanceof XinqiBceGuildReqMemberList.BceGuildReqMemberList ) {
      return 1078; 
    }
    else if (msg instanceof XinqiBceGuildRequest.BceGuildRequest ) {
      return 1079; 
    }
    else if (msg instanceof XinqiBceGuildSearch.BceGuildSearch ) {
      return 1080; 
    }
    else if (msg instanceof XinqiBceGuildSetPosName.BceGuildSetPosName ) {
      return 1081; 
    }
    else if (msg instanceof XinqiBceGuildShopLevelup.BceGuildShopLevelup ) {
      return 1082; 
    }
    else if (msg instanceof XinqiBceGuildTransfer.BceGuildTransfer ) {
      return 1083; 
    }
    else if (msg instanceof XinqiBceHallRoomList.BceHallRoomList ) {
      return 1084; 
    }
    else if (msg instanceof XinqiBceHallUserList.BceHallUserList ) {
      return 1085; 
    }
    else if (msg instanceof XinqiBceHarvestTree.BceHarvestTree ) {
      return 1086; 
    }
    else if (msg instanceof XinqiBceHeartbeat.BceHeartbeat ) {
      return 1087; 
    }
    else if (msg instanceof XinqiBceInit.BceInit ) {
      return 1088; 
    }
    else if (msg instanceof XinqiBceInvite.BceInvite ) {
      return 1089; 
    }
    else if (msg instanceof XinqiBceKickUser.BceKickUser ) {
      return 1090; 
    }
    else if (msg instanceof XinqiBceLeaveHall.BceLeaveHall ) {
      return 1091; 
    }
    else if (msg instanceof XinqiBceLeaveMessage.BceLeaveMessage ) {
      return 1092; 
    }
    else if (msg instanceof XinqiBceLeaveRoom.BceLeaveRoom ) {
      return 1093; 
    }
    else if (msg instanceof XinqiBceLengthenIndate.BceLengthenIndate ) {
      return 1094; 
    }
    else if (msg instanceof XinqiBceLoadProgress.BceLoadProgress ) {
      return 1095; 
    }
    else if (msg instanceof XinqiBceLogin.BceLogin ) {
      return 1096; 
    }
    else if (msg instanceof XinqiBceLottery.BceLottery ) {
      return 1097; 
    }
    else if (msg instanceof XinqiBceMergeProp.BceMergeProp ) {
      return 1098; 
    }
    else if (msg instanceof XinqiBceMoveProp.BceMoveProp ) {
      return 1099; 
    }
    else if (msg instanceof XinqiBceMyRankInfo.BceMyRankInfo ) {
      return 1100; 
    }
    else if (msg instanceof XinqiBceOnlineFrd.BceOnlineFrd ) {
      return 1101; 
    }
    else if (msg instanceof XinqiBceOnlineReward.BceOnlineReward ) {
      return 1102; 
    }
    else if (msg instanceof XinqiBcePRCEnter.BcePRCEnter ) {
      return 1103; 
    }
    else if (msg instanceof XinqiBcePRCOpen.BcePRCOpen ) {
      return 1104; 
    }
    else if (msg instanceof XinqiBcePickGold.BcePickGold ) {
      return 1105; 
    }
    else if (msg instanceof XinqiBceReadyStart.BceReadyStart ) {
      return 1106; 
    }
    else if (msg instanceof XinqiBceRegister.BceRegister ) {
      return 1107; 
    }
    else if (msg instanceof XinqiBceRequestFriendInfo.BceRequestFriendInfo ) {
      return 1108; 
    }
    else if (msg instanceof XinqiBceRobotOver.BceRobotOver ) {
      return 1109; 
    }
    else if (msg instanceof XinqiBceRoleAttack.BceRoleAttack ) {
      return 1110; 
    }
    else if (msg instanceof XinqiBceRoleChangeDirection.BceRoleChangeDirection ) {
      return 1111; 
    }
    else if (msg instanceof XinqiBceRoleMove.BceRoleMove ) {
      return 1112; 
    }
    else if (msg instanceof XinqiBceRoleMoveStart.BceRoleMoveStart ) {
      return 1113; 
    }
    else if (msg instanceof XinqiBceRoleMoveStop.BceRoleMoveStop ) {
      return 1114; 
    }
    else if (msg instanceof XinqiBceRolePower.BceRolePower ) {
      return 1115; 
    }
    else if (msg instanceof XinqiBceRoleUseTool.BceRoleUseTool ) {
      return 1116; 
    }
    else if (msg instanceof XinqiBceRoundOver.BceRoundOver ) {
      return 1117; 
    }
    else if (msg instanceof XinqiBceSellProp.BceSellProp ) {
      return 1118; 
    }
    else if (msg instanceof XinqiBceSellTool.BceSellTool ) {
      return 1119; 
    }
    else if (msg instanceof XinqiBceSetGuildAnno.BceSetGuildAnno ) {
      return 1120; 
    }
    else if (msg instanceof XinqiBceShopping.BceShopping ) {
      return 1121; 
    }
    else if (msg instanceof XinqiBceShowUserInfo.BceShowUserInfo ) {
      return 1122; 
    }
    else if (msg instanceof XinqiBceSplitProp.BceSplitProp ) {
      return 1123; 
    }
    else if (msg instanceof XinqiBceSyncPos.BceSyncPos ) {
      return 1124; 
    }
    else if (msg instanceof XinqiBceTaskReward.BceTaskReward ) {
      return 1125; 
    }
    else if (msg instanceof XinqiBceTransfer.BceTransfer ) {
      return 1126; 
    }
    else if (msg instanceof XinqiBceUseProp.BceUseProp ) {
      return 1127; 
    }
    else if (msg instanceof XinqiBceUserConfig.BceUserConfig ) {
      return 1128; 
    }
    else if (msg instanceof XinqiBceUserGuidAtk.BceUserGuidAtk ) {
      return 1129; 
    }
    else if (msg instanceof XinqiBceVisit.BceVisit ) {
      return 1130; 
    }
    else if (msg instanceof XinqiBceWaterTree.BceWaterTree ) {
      return 1131; 
    }
    else if (msg instanceof XinqiBseFinishAchievement.BseFinishAchievement ) {
      return 661; 
    }
    else if (msg instanceof XinqiBseRedirect.BseRedirect ) {
      return 662; 
    }
    else if (msg instanceof XinqiBseWebview.BseWebview ) {
      return 663; 
    }
    else if (msg instanceof XinqiBseAchievements.BseAchievements ) {
      return 664; 
    }
    else if (msg instanceof XinqiBseExpireEquipments.BseExpireEquipments ) {
      return 665; 
    }
    else if (msg instanceof XinqiBseUserAchievements.BseUserAchievements ) {
      return 666; 
    }
    else if (msg instanceof XinqiBceLoginReady.BceLoginReady ) {
      return 1132; 
    }
    else if (msg instanceof XinqiBceSendGift.BceSendGift ) {
      return 1133; 
    }
    else if (msg instanceof XinqiBseSendGift.BseSendGift ) {
      return 667; 
    }
    else if (msg instanceof XinqiBceBulletTrack.BceBulletTrack ) {
      return 1134; 
    }
    else if (msg instanceof XinqiBseBulletTrack.BseBulletTrack ) {
      return 668; 
    }
    else if (msg instanceof XinqiBseZip.BseZip ) {
      return 669; 
    }
    else if (msg instanceof XinqiBceConfirm.BceConfirm ) {
      return 1135; 
    }
    else if (msg instanceof XinqiBseConfirm.BseConfirm ) {
      return 670; 
    }
    else if (msg instanceof XinqiBceFindFriend.BceFindFriend ) {
      return 1136; 
    }
    else if (msg instanceof XinqiBseFindFriend.BseFindFriend ) {
      return 671; 
    }
    else if (msg instanceof XinqiBceUserRefresh.BceUserRefresh ) {
      return 1137; 
    }
    else if (msg instanceof XinqiBceLogout.BceLogout ) {
      return 1138; 
    }
    else if (msg instanceof XinqiBseLogout.BseLogout ) {
      return 672; 
    }
    else if (msg instanceof XinqiBceWeibo.BceWeibo ) {
      return 1139; 
    }
    else if (msg instanceof XinqiBceRoomUserList.BceRoomUserList ) {
      return 1140; 
    }
    else if (msg instanceof XinqiBseRoomUserList.BseRoomUserList ) {
      return 673; 
    }
    else if (msg instanceof XinqiBceSysMessage.BceSysMessage ) {
      return 1141; 
    }
    else if (msg instanceof XinqiBceForgetPassword.BceForgetPassword ) {
      return 1142; 
    }
    else if (msg instanceof XinqiBceCloseBag.BceCloseBag ) {
      return 1143; 
    }
    else if (msg instanceof XinqiBceTraining.BceTraining ) {
      return 1144; 
    }
    else if (msg instanceof XinqiBceTrack.BceTrack ) {
      return 1145; 
    }
    else if (msg instanceof XinqiBseMailList.BseMailList ) {
      return 674; 
    }
    else if (msg instanceof XinqiBseMailReceive.BseMailReceive ) {
      return 675; 
    }
    else if (msg instanceof XinqiBseMailSend.BseMailSend ) {
      return 676; 
    }
    else if (msg instanceof XinqiBceMailDelete.BceMailDelete ) {
      return 1146; 
    }
    else if (msg instanceof XinqiBceMailSend.BceMailSend ) {
      return 1147; 
    }
    else if (msg instanceof XinqiBseMailDelete.BseMailDelete ) {
      return 677; 
    }
    else if (msg instanceof XinqiBceMailRead.BceMailRead ) {
      return 1148; 
    }
    else if (msg instanceof XinqiBceMailTake.BceMailTake ) {
      return 1149; 
    }
    else if (msg instanceof XinqiBseMailRead.BseMailRead ) {
      return 678; 
    }
    else if (msg instanceof XinqiBseMailTake.BseMailTake ) {
      return 679; 
    }
    else if (msg instanceof XinqiBseRoomUnlock.BseRoomUnlock ) {
      return 680; 
    }
    else if (msg instanceof XinqiBceBossList.BceBossList ) {
      return 1150; 
    }
    else if (msg instanceof XinqiBceBossTakeReward.BceBossTakeReward ) {
      return 1151; 
    }
    else if (msg instanceof XinqiBseBossList.BseBossList ) {
      return 681; 
    }
    else if (msg instanceof XinqiBseBossSync.BseBossSync ) {
      return 682; 
    }
    else if (msg instanceof XinqiBseBossTakeReward.BseBossTakeReward ) {
      return 683; 
    }
    else if (msg instanceof XinqiBseFuncUnlock.BseFuncUnlock ) {
      return 684; 
    }
    else if (msg instanceof XinqiBceCraftPrice.BceCraftPrice ) {
      return 1152; 
    }
    else if (msg instanceof XinqiBseCraftPrice.BseCraftPrice ) {
      return 685; 
    }
    else if (msg instanceof XinqiBceActionLimitQuery.BceActionLimitQuery ) {
      return 1153; 
    }
    else if (msg instanceof XinqiBseActionLimitQuery.BseActionLimitQuery ) {
      return 686; 
    }
    else if (msg instanceof XinqiBceActionLimitBuy.BceActionLimitBuy ) {
      return 1154; 
    }
    else if (msg instanceof XinqiBseActionLimitBuy.BseActionLimitBuy ) {
      return 687; 
    }
    else if (msg instanceof XinqiBceCaishenPray.BceCaishenPray ) {
      return 1155; 
    }
    else if (msg instanceof XinqiBceCaishenQuery.BceCaishenQuery ) {
      return 1156; 
    }
    else if (msg instanceof XinqiBceTreasureHuntBuy.BceTreasureHuntBuy ) {
      return 1157; 
    }
    else if (msg instanceof XinqiBceTreasureHuntQuery.BceTreasureHuntQuery ) {
      return 1158; 
    }
    else if (msg instanceof XinqiBseCaishenPray.BseCaishenPray ) {
      return 688; 
    }
    else if (msg instanceof XinqiBseCaishenQuery.BseCaishenQuery ) {
      return 689; 
    }
    else if (msg instanceof XinqiBseTreasureHuntBuy.BseTreasureHuntBuy ) {
      return 690; 
    }
    else if (msg instanceof XinqiBseTreasureHuntQuery.BseTreasureHuntQuery ) {
      return 691; 
    }
    else if (msg instanceof XinqiBceChargeInternal.BceChargeInternal ) {
      return 1159; 
    }
    else if (msg instanceof XinqiBceOfflinePush.BceOfflinePush ) {
      return 1160; 
    }
    else if (msg instanceof XinqiBceReloadConfig.BceReloadConfig ) {
      return 1161; 
    }
    else if (msg instanceof XinqiBceCyLogin.BceCyLogin ) {
      return 1162; 
    }
    else if (msg instanceof XinqiBceCyRegister.BceCyRegister ) {
      return 1163; 
    }
    else if (msg instanceof XinqiBseCyLogin.BseCyLogin ) {
      return 692; 
    }
    else if (msg instanceof XinqiBseCyRegister.BseCyRegister ) {
      return 693; 
    }
    else if (msg instanceof XinqiBceCreateRole.BceCreateRole ) {
      return 1164; 
    }
    else if (msg instanceof XinqiBseCreateRole.BseCreateRole ) {
      return 694; 
    }
    else if (msg instanceof XinqiBceUserSync.BceUserSync ) {
      return 1165; 
    }
    else if (msg instanceof XinqiBseUserSync.BseUserSync ) {
      return 695; 
    }
    else if (msg instanceof XinqiBceGetValue.BceGetValue ) {
      return 1166; 
    }
    else if (msg instanceof XinqiBseGetValue.BseGetValue ) {
      return 696; 
    }
    else if (msg instanceof XinqiBceBulletin.BceBulletin ) {
      return 1167; 
    }
    else if (msg instanceof XinqiBceChargeCard.BceChargeCard ) {
      return 1168; 
    }
    else if (msg instanceof XinqiBcePropDataQuery.BcePropDataQuery ) {
      return 1169; 
    }
    else if (msg instanceof XinqiBceServerList.BceServerList ) {
      return 1170; 
    }
    else if (msg instanceof XinqiBceUserInput.BceUserInput ) {
      return 1171; 
    }
    else if (msg instanceof XinqiBceUserRoleList.BceUserRoleList ) {
      return 1172; 
    }
    else if (msg instanceof XinqiBceUserStatusList.BceUserStatusList ) {
      return 1173; 
    }
    else if (msg instanceof XinqiBceVipInfo.BceVipInfo ) {
      return 1174; 
    }
    else if (msg instanceof XinqiBseChargeCard.BseChargeCard ) {
      return 697; 
    }
    else if (msg instanceof XinqiBsePropDataQuery.BsePropDataQuery ) {
      return 698; 
    }
    else if (msg instanceof XinqiBseServerList.BseServerList ) {
      return 699; 
    }
    else if (msg instanceof XinqiBseUserInput.BseUserInput ) {
      return 700; 
    }
    else if (msg instanceof XinqiBseUserRoleList.BseUserRoleList ) {
      return 701; 
    }
    else if (msg instanceof XinqiBseUserStatusList.BseUserStatusList ) {
      return 702; 
    }
    else if (msg instanceof XinqiBseVipInfo.BseVipInfo ) {
      return 703; 
    }
    else if (msg instanceof XinqiBsePromotion.BsePromotion ) {
      return 704; 
    }
    else if (msg instanceof XinqiBceSelectRole.BceSelectRole ) {
      return 1175; 
    }
    else if (msg instanceof XinqiBceDeleteRole.BceDeleteRole ) {
      return 1176; 
    }
    else if (msg instanceof XinqiBseDeleteRole.BseDeleteRole ) {
      return 705; 
    }
    else if (msg instanceof XinqiBseBattleStatusUpdate.BseBattleStatusUpdate ) {
      return 706; 
    }
    else if (msg instanceof XinqiBceChangeAutomode.BceChangeAutomode ) {
      return 1177; 
    }
    else if (msg instanceof XinqiBseChangeAutomode.BseChangeAutomode ) {
      return 707; 
    }
    else if (msg instanceof XinqiBceExitGame.BceExitGame ) {
      return 1178; 
    }
    else if (msg instanceof XinqiBseExitGame.BseExitGame ) {
      return 708; 
    }
    else if (msg instanceof XinqiBseModFriend.BseModFriend ) {
      return 709; 
    }
    else if (msg instanceof XinqiBceFillProfile.BceFillProfile ) {
      return 1179; 
    }
    else if (msg instanceof XinqiBseFillProfile.BseFillProfile ) {
      return 710; 
    }
    else if (msg instanceof XinqiBseBattleAddRole.BseBattleAddRole ) {
      return 711; 
    }
    else if (msg instanceof XinqiBceGetUserBiblio.BceGetUserBiblio ) {
      return 1180; 
    }
    else if (msg instanceof XinqiBseGetUserBiblio.BseGetUserBiblio ) {
      return 712; 
    }
    else if (msg instanceof XinqiBceTakeUserBiblioReward.BceTakeUserBiblioReward ) {
      return 1181; 
    }
    else if (msg instanceof XinqiBseTakeUserBiblioReward.BseTakeUserBiblioReward ) {
      return 713; 
    }
    else if (msg instanceof XinqiBceVoiceChat.BceVoiceChat ) {
      return 1182; 
    }
    else if (msg instanceof XinqiBseVoiceChat.BseVoiceChat ) {
      return 714; 
    }
    else if (msg instanceof XinqiBceGuildAbilityList.BceGuildAbilityList ) {
      return 1183; 
    }
    else if (msg instanceof XinqiBceGuildApplyList.BceGuildApplyList ) {
      return 1184; 
    }
    else if (msg instanceof XinqiBceGuildApplyProcess.BceGuildApplyProcess ) {
      return 1185; 
    }
    else if (msg instanceof XinqiBceGuildBag.BceGuildBag ) {
      return 1186; 
    }
    else if (msg instanceof XinqiBceGuildBagEvent.BceGuildBagEvent ) {
      return 1187; 
    }
    else if (msg instanceof XinqiBceGuildChangeAnnounce.BceGuildChangeAnnounce ) {
      return 1188; 
    }
    else if (msg instanceof XinqiBceGuildChangeRole.BceGuildChangeRole ) {
      return 1189; 
    }
    else if (msg instanceof XinqiBceGuildContributeQuery.BceGuildContributeQuery ) {
      return 1190; 
    }
    else if (msg instanceof XinqiBceGuildFacilityLevelList.BceGuildFacilityLevelList ) {
      return 1191; 
    }
    else if (msg instanceof XinqiBceGuildFacilityLevelUp.BceGuildFacilityLevelUp ) {
      return 1192; 
    }
    else if (msg instanceof XinqiBceGuildFacilityList.BceGuildFacilityList ) {
      return 1193; 
    }
    else if (msg instanceof XinqiBceGuildList.BceGuildList ) {
      return 1194; 
    }
    else if (msg instanceof XinqiBceGuildMemberList.BceGuildMemberList ) {
      return 1195; 
    }
    else if (msg instanceof XinqiBceGuildPrivilegeList.BceGuildPrivilegeList ) {
      return 1196; 
    }
    else if (msg instanceof XinqiBceGuildSearchMember.BceGuildSearchMember ) {
      return 1197; 
    }
    else if (msg instanceof XinqiBceGuildShopping.BceGuildShopping ) {
      return 1198; 
    }
    else if (msg instanceof XinqiBseGuildAbilityList.BseGuildAbilityList ) {
      return 715; 
    }
    else if (msg instanceof XinqiBseGuildApplyList.BseGuildApplyList ) {
      return 716; 
    }
    else if (msg instanceof XinqiBseGuildApplyProcess.BseGuildApplyProcess ) {
      return 717; 
    }
    else if (msg instanceof XinqiBseGuildBag.BseGuildBag ) {
      return 718; 
    }
    else if (msg instanceof XinqiBseGuildBagEvent.BseGuildBagEvent ) {
      return 719; 
    }
    else if (msg instanceof XinqiBseGuildChangeAnnounce.BseGuildChangeAnnounce ) {
      return 720; 
    }
    else if (msg instanceof XinqiBseGuildChangeRole.BseGuildChangeRole ) {
      return 721; 
    }
    else if (msg instanceof XinqiBseGuildContributeQuery.BseGuildContributeQuery ) {
      return 722; 
    }
    else if (msg instanceof XinqiBseGuildFacilityLevelList.BseGuildFacilityLevelList ) {
      return 723; 
    }
    else if (msg instanceof XinqiBseGuildFacilityLevelUp.BseGuildFacilityLevelUp ) {
      return 724; 
    }
    else if (msg instanceof XinqiBseGuildFacilityList.BseGuildFacilityList ) {
      return 725; 
    }
    else if (msg instanceof XinqiBseGuildMemberList.BseGuildMemberList ) {
      return 726; 
    }
    else if (msg instanceof XinqiBseGuildPrivilegeList.BseGuildPrivilegeList ) {
      return 727; 
    }
    else if (msg instanceof XinqiBseGuildSearchMember.BseGuildSearchMember ) {
      return 728; 
    }
    else if (msg instanceof XinqiBseGuildShopping.BseGuildShopping ) {
      return 729; 
    }
    else if (msg instanceof XinqiBceGetVoiceChat.BceGetVoiceChat ) {
      return 1199; 
    }
    else if (msg instanceof XinqiBseGetVoiceChat.BseGetVoiceChat ) {
      return 730; 
    }
    else if (msg instanceof XinqiBceGuildApply.BceGuildApply ) {
      return 1200; 
    }
    else if (msg instanceof XinqiBseGuildApply.BseGuildApply ) {
      return 731; 
    }
    else if (msg instanceof XinqiBceForbidUser.BceForbidUser ) {
      return 1201; 
    }
    else if (msg instanceof XinqiBseForbidUser.BseForbidUser ) {
      return 732; 
    }
    else if (msg instanceof XinqiBceGuildBagPut.BceGuildBagPut ) {
      return 1202; 
    }
    else if (msg instanceof XinqiBceGuildBagTake.BceGuildBagTake ) {
      return 1203; 
    }
    else if (msg instanceof XinqiBseGuildBagPut.BseGuildBagPut ) {
      return 733; 
    }
    else if (msg instanceof XinqiBseGuildBagTake.BseGuildBagTake ) {
      return 734; 
    }
    else {
      log.error("No id for message: "+msg.getClass().getName());
    }
    return -1;
  }
}
