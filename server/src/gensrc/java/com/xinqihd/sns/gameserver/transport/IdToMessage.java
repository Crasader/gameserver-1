package com.xinqihd.sns.gameserver.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.proto.*;

/**
 * GENERATED SOURCE CODE DO NOT MODIFY!
 * Translate the given int id to its coresponding message. 
 * @author wangqi 
 */ 
public class IdToMessage {

  private static Log log = LogFactory.getLog(IdToMessage.class); 

  public static MessageLite idToMessage(int id) { 
    MessageLite message = null;
    switch(id) {
    case 512: 
      message = XinqiBseAddFriend.BseAddFriend.getDefaultInstance(); 
      break;
    case 513: 
      message = XinqiBseAddProp.BseAddProp.getDefaultInstance(); 
      break;
    case 514: 
      message = XinqiBseAddTask.BseAddTask.getDefaultInstance(); 
      break;
    case 515: 
      message = XinqiBseAnswerInvite.BseAnswerInvite.getDefaultInstance(); 
      break;
    case 516: 
      message = XinqiBseArrangeList.BseArrangeList.getDefaultInstance(); 
      break;
    case 517: 
      message = XinqiBseAskRoundOver.BseAskRoundOver.getDefaultInstance(); 
      break;
    case 518: 
      message = XinqiBseAuthState.BseAuthState.getDefaultInstance(); 
      break;
    case 519: 
      message = XinqiBseBattleAlter.BseBattleAlter.getDefaultInstance(); 
      break;
    case 520: 
      message = XinqiBseBattleInit.BseBattleInit.getDefaultInstance(); 
      break;
    case 521: 
      message = XinqiBseBattleOver.BseBattleOver.getDefaultInstance(); 
      break;
    case 522: 
      message = XinqiBseBattlePickBox.BseBattlePickBox.getDefaultInstance(); 
      break;
    case 523: 
      message = XinqiBseBattleReward.BseBattleReward.getDefaultInstance(); 
      break;
    case 524: 
      message = XinqiBseBroadcastMessage.BseBroadcastMessage.getDefaultInstance(); 
      break;
    case 525: 
      message = XinqiBseBulltinList.BseBulltinList.getDefaultInstance(); 
      break;
    case 526: 
      message = XinqiBseBuyProp.BseBuyProp.getDefaultInstance(); 
      break;
    case 527: 
      message = XinqiBseBuyVip.BseBuyVip.getDefaultInstance(); 
      break;
    case 528: 
      message = XinqiBseChangeMap.BseChangeMap.getDefaultInstance(); 
      break;
    case 529: 
      message = XinqiBseChangeSeat.BseChangeSeat.getDefaultInstance(); 
      break;
    case 530: 
      message = XinqiBseCharge.BseCharge.getDefaultInstance(); 
      break;
    case 531: 
      message = XinqiBseChargeList.BseChargeList.getDefaultInstance(); 
      break;
    case 532: 
      message = XinqiBseChat.BseChat.getDefaultInstance(); 
      break;
    case 533: 
      message = XinqiBseChgBtlType.BseChgBtlType.getDefaultInstance(); 
      break;
    case 534: 
      message = XinqiBseClearLeaveMessage.BseClearLeaveMessage.getDefaultInstance(); 
      break;
    case 535: 
      message = XinqiBseClearRole.BseClearRole.getDefaultInstance(); 
      break;
    case 536: 
      message = XinqiBseCompose.BseCompose.getDefaultInstance(); 
      break;
    case 537: 
      message = XinqiBseConfigData.BseConfigData.getDefaultInstance(); 
      break;
    case 538: 
      message = XinqiBseCreateGuild.BseCreateGuild.getDefaultInstance(); 
      break;
    case 539: 
      message = XinqiBseDailyMark.BseDailyMark.getDefaultInstance(); 
      break;
    case 540: 
      message = XinqiBseDailyMarkList.BseDailyMarkList.getDefaultInstance(); 
      break;
    case 541: 
      message = XinqiBseDead.BseDead.getDefaultInstance(); 
      break;
    case 542: 
      message = XinqiBseDelTask.BseDelTask.getDefaultInstance(); 
      break;
    case 543: 
      message = XinqiBseDoubleExpTime.BseDoubleExpTime.getDefaultInstance(); 
      break;
    case 544: 
      message = XinqiBseEditSeat.BseEditSeat.getDefaultInstance(); 
      break;
    case 545: 
      message = XinqiBseEnterGuild.BseEnterGuild.getDefaultInstance(); 
      break;
    case 546: 
      message = XinqiBseEnterHall.BseEnterHall.getDefaultInstance(); 
      break;
    case 547: 
      message = XinqiBseEnterRoom.BseEnterRoom.getDefaultInstance(); 
      break;
    case 548: 
      message = XinqiBseEquipment.BseEquipment.getDefaultInstance(); 
      break;
    case 549: 
      message = XinqiBseError.BseError.getDefaultInstance(); 
      break;
    case 550: 
      message = XinqiBseExitGuild.BseExitGuild.getDefaultInstance(); 
      break;
    case 551: 
      message = XinqiBseFindRoom.BseFindRoom.getDefaultInstance(); 
      break;
    case 552: 
      message = XinqiBseForge.BseForge.getDefaultInstance(); 
      break;
    case 553: 
      message = XinqiBseFriendEquip.BseFriendEquip.getDefaultInstance(); 
      break;
    case 554: 
      message = XinqiBseFriendList.BseFriendList.getDefaultInstance(); 
      break;
    case 555: 
      message = XinqiBseFriendOnlineStatus.BseFriendOnlineStatus.getDefaultInstance(); 
      break;
    case 556: 
      message = XinqiBseFriendsInfo.BseFriendsInfo.getDefaultInstance(); 
      break;
    case 557: 
      message = XinqiBseGameDataKey.BseGameDataKey.getDefaultInstance(); 
      break;
    case 558: 
      message = XinqiBseGhostMove.BseGhostMove.getDefaultInstance(); 
      break;
    case 559: 
      message = XinqiBseGhostMoveStart.BseGhostMoveStart.getDefaultInstance(); 
      break;
    case 560: 
      message = XinqiBseGhostMoveStop.BseGhostMoveStop.getDefaultInstance(); 
      break;
    case 561: 
      message = XinqiBseGiveProp.BseGiveProp.getDefaultInstance(); 
      break;
    case 562: 
      message = XinqiBseGuildAcceptMember.BseGuildAcceptMember.getDefaultInstance(); 
      break;
    case 563: 
      message = XinqiBseGuildBuy.BseGuildBuy.getDefaultInstance(); 
      break;
    case 564: 
      message = XinqiBseGuildContribute.BseGuildContribute.getDefaultInstance(); 
      break;
    case 565: 
      message = XinqiBseGuildCostAlert.BseGuildCostAlert.getDefaultInstance(); 
      break;
    case 566: 
      message = XinqiBseGuildDismiss.BseGuildDismiss.getDefaultInstance(); 
      break;
    case 567: 
      message = XinqiBseGuildFire.BseGuildFire.getDefaultInstance(); 
      break;
    case 568: 
      message = XinqiBseGuildInvite.BseGuildInvite.getDefaultInstance(); 
      break;
    case 569: 
      message = XinqiBseGuildIronLevelup.BseGuildIronLevelup.getDefaultInstance(); 
      break;
    case 570: 
      message = XinqiBseGuildLevelUp.BseGuildLevelUp.getDefaultInstance(); 
      break;
    case 571: 
      message = XinqiBseGuildLimit.BseGuildLimit.getDefaultInstance(); 
      break;
    case 572: 
      message = XinqiBseGuildList.BseGuildList.getDefaultInstance(); 
      break;
    case 573: 
      message = XinqiBseGuildNews.BseGuildNews.getDefaultInstance(); 
      break;
    case 574: 
      message = XinqiBseGuildPosChange.BseGuildPosChange.getDefaultInstance(); 
      break;
    case 575: 
      message = XinqiBseGuildReqCancel.BseGuildReqCancel.getDefaultInstance(); 
      break;
    case 576: 
      message = XinqiBseGuildReqMemberList.BseGuildReqMemberList.getDefaultInstance(); 
      break;
    case 577: 
      message = XinqiBseGuildRequest.BseGuildRequest.getDefaultInstance(); 
      break;
    case 578: 
      message = XinqiBseGuildSearch.BseGuildSearch.getDefaultInstance(); 
      break;
    case 579: 
      message = XinqiBseGuildSetPosName.BseGuildSetPosName.getDefaultInstance(); 
      break;
    case 580: 
      message = XinqiBseGuildShopLevelup.BseGuildShopLevelup.getDefaultInstance(); 
      break;
    case 581: 
      message = XinqiBseGuildTransfer.BseGuildTransfer.getDefaultInstance(); 
      break;
    case 582: 
      message = XinqiBseHallRoomList.BseHallRoomList.getDefaultInstance(); 
      break;
    case 583: 
      message = XinqiBseHallUserList.BseHallUserList.getDefaultInstance(); 
      break;
    case 584: 
      message = XinqiBseHarvestTree.BseHarvestTree.getDefaultInstance(); 
      break;
    case 585: 
      message = XinqiBseHeartbeat.BseHeartbeat.getDefaultInstance(); 
      break;
    case 586: 
      message = XinqiBseInit.BseInit.getDefaultInstance(); 
      break;
    case 587: 
      message = XinqiBseInvite.BseInvite.getDefaultInstance(); 
      break;
    case 588: 
      message = XinqiBseInviteFrd.BseInviteFrd.getDefaultInstance(); 
      break;
    case 589: 
      message = XinqiBseInviteGuildList.BseInviteGuildList.getDefaultInstance(); 
      break;
    case 590: 
      message = XinqiBseInviteRtn.BseInviteRtn.getDefaultInstance(); 
      break;
    case 591: 
      message = XinqiBseItem.BseItem.getDefaultInstance(); 
      break;
    case 592: 
      message = XinqiBseJoinGuild.BseJoinGuild.getDefaultInstance(); 
      break;
    case 593: 
      message = XinqiBseKickUser.BseKickUser.getDefaultInstance(); 
      break;
    case 594: 
      message = XinqiBseLeaveHall.BseLeaveHall.getDefaultInstance(); 
      break;
    case 595: 
      message = XinqiBseLeaveMessage.BseLeaveMessage.getDefaultInstance(); 
      break;
    case 596: 
      message = XinqiBseLeaveRoom.BseLeaveRoom.getDefaultInstance(); 
      break;
    case 597: 
      message = XinqiBseLengthenIndate.BseLengthenIndate.getDefaultInstance(); 
      break;
    case 598: 
      message = XinqiBseLoadProgress.BseLoadProgress.getDefaultInstance(); 
      break;
    case 599: 
      message = XinqiBseLogin.BseLogin.getDefaultInstance(); 
      break;
    case 600: 
      message = XinqiBseLoginLottery.BseLoginLottery.getDefaultInstance(); 
      break;
    case 601: 
      message = XinqiBseLottery.BseLottery.getDefaultInstance(); 
      break;
    case 602: 
      message = XinqiBseLotteryOver.BseLotteryOver.getDefaultInstance(); 
      break;
    case 603: 
      message = XinqiBseMap.BseMap.getDefaultInstance(); 
      break;
    case 604: 
      message = XinqiBseMatchingRoom.BseMatchingRoom.getDefaultInstance(); 
      break;
    case 605: 
      message = XinqiBseMergeProp.BseMergeProp.getDefaultInstance(); 
      break;
    case 606: 
      message = XinqiBseMessage.BseMessage.getDefaultInstance(); 
      break;
    case 607: 
      message = XinqiBseModiTask.BseModiTask.getDefaultInstance(); 
      break;
    case 608: 
      message = XinqiBseMoveProp.BseMoveProp.getDefaultInstance(); 
      break;
    case 609: 
      message = XinqiBseMyRankInfo.BseMyRankInfo.getDefaultInstance(); 
      break;
    case 610: 
      message = XinqiBseNotify.BseNotify.getDefaultInstance(); 
      break;
    case 611: 
      message = XinqiBseOnlineFrd.BseOnlineFrd.getDefaultInstance(); 
      break;
    case 612: 
      message = XinqiBseOnlineReward.BseOnlineReward.getDefaultInstance(); 
      break;
    case 613: 
      message = XinqiBsePRCOpen.BsePRCOpen.getDefaultInstance(); 
      break;
    case 614: 
      message = XinqiBsePickGold.BsePickGold.getDefaultInstance(); 
      break;
    case 615: 
      message = XinqiBsePropInfoList.BsePropInfoList.getDefaultInstance(); 
      break;
    case 616: 
      message = XinqiBsePropList.BsePropList.getDefaultInstance(); 
      break;
    case 617: 
      message = XinqiBseRecentlyFriendList.BseRecentlyFriendList.getDefaultInstance(); 
      break;
    case 618: 
      message = XinqiBseRecentlyMessageList.BseRecentlyMessageList.getDefaultInstance(); 
      break;
    case 619: 
      message = XinqiBseRegister.BseRegister.getDefaultInstance(); 
      break;
    case 620: 
      message = XinqiBseRequestFriendInfo.BseRequestFriendInfo.getDefaultInstance(); 
      break;
    case 621: 
      message = XinqiBseRequestGuildList.BseRequestGuildList.getDefaultInstance(); 
      break;
    case 622: 
      message = XinqiBseRoleAttack.BseRoleAttack.getDefaultInstance(); 
      break;
    case 623: 
      message = XinqiBseRoleBattleInfo.BseRoleBattleInfo.getDefaultInstance(); 
      break;
    case 624: 
      message = XinqiBseRoleChangeDirection.BseRoleChangeDirection.getDefaultInstance(); 
      break;
    case 625: 
      message = XinqiBseRoleConfig.BseRoleConfig.getDefaultInstance(); 
      break;
    case 626: 
      message = XinqiBseRoleInfo.BseRoleInfo.getDefaultInstance(); 
      break;
    case 627: 
      message = XinqiBseRoleMove.BseRoleMove.getDefaultInstance(); 
      break;
    case 628: 
      message = XinqiBseRoleMoveStart.BseRoleMoveStart.getDefaultInstance(); 
      break;
    case 629: 
      message = XinqiBseRoleMoveStop.BseRoleMoveStop.getDefaultInstance(); 
      break;
    case 630: 
      message = XinqiBseRolePower.BseRolePower.getDefaultInstance(); 
      break;
    case 631: 
      message = XinqiBseRoleUseTool.BseRoleUseTool.getDefaultInstance(); 
      break;
    case 632: 
      message = XinqiBseRoomCountdown.BseRoomCountdown.getDefaultInstance(); 
      break;
    case 633: 
      message = XinqiBseRoomInfo.BseRoomInfo.getDefaultInstance(); 
      break;
    case 634: 
      message = XinqiBseRoundStart.BseRoundStart.getDefaultInstance(); 
      break;
    case 635: 
      message = XinqiBseSellProp.BseSellProp.getDefaultInstance(); 
      break;
    case 636: 
      message = XinqiBseSetGuildAnno.BseSetGuildAnno.getDefaultInstance(); 
      break;
    case 637: 
      message = XinqiBseShop.BseShop.getDefaultInstance(); 
      break;
    case 638: 
      message = XinqiBseShopping.BseShopping.getDefaultInstance(); 
      break;
    case 639: 
      message = XinqiBseShowUserInfo.BseShowUserInfo.getDefaultInstance(); 
      break;
    case 640: 
      message = XinqiBseSplitProp.BseSplitProp.getDefaultInstance(); 
      break;
    case 641: 
      message = XinqiBseSvrUnixHour.BseSvrUnixHour.getDefaultInstance(); 
      break;
    case 642: 
      message = XinqiBseSyncPos.BseSyncPos.getDefaultInstance(); 
      break;
    case 643: 
      message = XinqiBseSysChat.BseSysChat.getDefaultInstance(); 
      break;
    case 644: 
      message = XinqiBseSysMessage.BseSysMessage.getDefaultInstance(); 
      break;
    case 645: 
      message = XinqiBseTask.BseTask.getDefaultInstance(); 
      break;
    case 646: 
      message = XinqiBseTaskList.BseTaskList.getDefaultInstance(); 
      break;
    case 647: 
      message = XinqiBseTip.BseTip.getDefaultInstance(); 
      break;
    case 648: 
      message = XinqiBseToolList.BseToolList.getDefaultInstance(); 
      break;
    case 649: 
      message = XinqiBseTransfer.BseTransfer.getDefaultInstance(); 
      break;
    case 650: 
      message = XinqiBseUpdateOnlineStatus.BseUpdateOnlineStatus.getDefaultInstance(); 
      break;
    case 651: 
      message = XinqiBseUseProp.BseUseProp.getDefaultInstance(); 
      break;
    case 652: 
      message = XinqiBseUseTool.BseUseTool.getDefaultInstance(); 
      break;
    case 653: 
      message = XinqiBseUserEnterRoom.BseUserEnterRoom.getDefaultInstance(); 
      break;
    case 654: 
      message = XinqiBseUserGuildData.BseUserGuildData.getDefaultInstance(); 
      break;
    case 655: 
      message = XinqiBseUserLeaveRoom.BseUserLeaveRoom.getDefaultInstance(); 
      break;
    case 656: 
      message = XinqiBseUserReadyStart.BseUserReadyStart.getDefaultInstance(); 
      break;
    case 657: 
      message = XinqiBseVipPeriodList.BseVipPeriodList.getDefaultInstance(); 
      break;
    case 658: 
      message = XinqiBseVisit.BseVisit.getDefaultInstance(); 
      break;
    case 659: 
      message = XinqiBseWaterTree.BseWaterTree.getDefaultInstance(); 
      break;
    case 660: 
      message = XinqiBseWebBuyFinishedOnce.BseWebBuyFinishedOnce.getDefaultInstance(); 
      break;
    case 1024: 
      message = XinqiBceAcceptDailyAward.BceAcceptDailyAward.getDefaultInstance(); 
      break;
    case 1025: 
      message = XinqiBceAddFriend.BceAddFriend.getDefaultInstance(); 
      break;
    case 1026: 
      message = XinqiBceAnswerInvite.BceAnswerInvite.getDefaultInstance(); 
      break;
    case 1027: 
      message = XinqiBceArmStrength.BceArmStrength.getDefaultInstance(); 
      break;
    case 1028: 
      message = XinqiBceArrangeList.BceArrangeList.getDefaultInstance(); 
      break;
    case 1029: 
      message = XinqiBceAskRoundOver.BceAskRoundOver.getDefaultInstance(); 
      break;
    case 1030: 
      message = XinqiBceBagTidy.BceBagTidy.getDefaultInstance(); 
      break;
    case 1031: 
      message = XinqiBceBattlePickBox.BceBattlePickBox.getDefaultInstance(); 
      break;
    case 1032: 
      message = XinqiBceBattleReward.BceBattleReward.getDefaultInstance(); 
      break;
    case 1033: 
      message = XinqiBceBattleRewardSelect.BceBattleRewardSelect.getDefaultInstance(); 
      break;
    case 1034: 
      message = XinqiBceBattleStageReady.BceBattleStageReady.getDefaultInstance(); 
      break;
    case 1035: 
      message = XinqiBceBullet.BceBullet.getDefaultInstance(); 
      break;
    case 1036: 
      message = XinqiBceBuyProp.BceBuyProp.getDefaultInstance(); 
      break;
    case 1037: 
      message = XinqiBceBuyTool.BceBuyTool.getDefaultInstance(); 
      break;
    case 1038: 
      message = XinqiBceBuyVip.BceBuyVip.getDefaultInstance(); 
      break;
    case 1039: 
      message = XinqiBceChangeMap.BceChangeMap.getDefaultInstance(); 
      break;
    case 1040: 
      message = XinqiBceChangeSeat.BceChangeSeat.getDefaultInstance(); 
      break;
    case 1041: 
      message = XinqiBceCharge.BceCharge.getDefaultInstance(); 
      break;
    case 1042: 
      message = XinqiBceChat.BceChat.getDefaultInstance(); 
      break;
    case 1043: 
      message = XinqiBceChgBtlType.BceChgBtlType.getDefaultInstance(); 
      break;
    case 1044: 
      message = XinqiBceChgShootMode.BceChgShootMode.getDefaultInstance(); 
      break;
    case 1045: 
      message = XinqiBceClearLeaveMessage.BceClearLeaveMessage.getDefaultInstance(); 
      break;
    case 1046: 
      message = XinqiBceCompose.BceCompose.getDefaultInstance(); 
      break;
    case 1047: 
      message = XinqiBceConfigData.BceConfigData.getDefaultInstance(); 
      break;
    case 1048: 
      message = XinqiBceCreateGuild.BceCreateGuild.getDefaultInstance(); 
      break;
    case 1049: 
      message = XinqiBceDailyMark.BceDailyMark.getDefaultInstance(); 
      break;
    case 1050: 
      message = XinqiBceDead.BceDead.getDefaultInstance(); 
      break;
    case 1051: 
      message = XinqiBceDebug.BceDebug.getDefaultInstance(); 
      break;
    case 1052: 
      message = XinqiBceDoubleExpOver.BceDoubleExpOver.getDefaultInstance(); 
      break;
    case 1053: 
      message = XinqiBceEditSeat.BceEditSeat.getDefaultInstance(); 
      break;
    case 1054: 
      message = XinqiBceEnterGuild.BceEnterGuild.getDefaultInstance(); 
      break;
    case 1055: 
      message = XinqiBceEnterHall.BceEnterHall.getDefaultInstance(); 
      break;
    case 1056: 
      message = XinqiBceEnterRoom.BceEnterRoom.getDefaultInstance(); 
      break;
    case 1057: 
      message = XinqiBceExitGuild.BceExitGuild.getDefaultInstance(); 
      break;
    case 1058: 
      message = XinqiBceFindRoom.BceFindRoom.getDefaultInstance(); 
      break;
    case 1059: 
      message = XinqiBceForge.BceForge.getDefaultInstance(); 
      break;
    case 1060: 
      message = XinqiBceFriendInfo.BceFriendInfo.getDefaultInstance(); 
      break;
    case 1061: 
      message = XinqiBceGhostMove.BceGhostMove.getDefaultInstance(); 
      break;
    case 1062: 
      message = XinqiBceGhostMoveStart.BceGhostMoveStart.getDefaultInstance(); 
      break;
    case 1063: 
      message = XinqiBceGhostMoveStop.BceGhostMoveStop.getDefaultInstance(); 
      break;
    case 1064: 
      message = XinqiBceGuideFinish.BceGuideFinish.getDefaultInstance(); 
      break;
    case 1065: 
      message = XinqiBceGuideStep.BceGuideStep.getDefaultInstance(); 
      break;
    case 1066: 
      message = XinqiBceGuildAcceptMember.BceGuildAcceptMember.getDefaultInstance(); 
      break;
    case 1067: 
      message = XinqiBceGuildBuy.BceGuildBuy.getDefaultInstance(); 
      break;
    case 1068: 
      message = XinqiBceGuildContribute.BceGuildContribute.getDefaultInstance(); 
      break;
    case 1069: 
      message = XinqiBceGuildDismiss.BceGuildDismiss.getDefaultInstance(); 
      break;
    case 1070: 
      message = XinqiBceGuildFire.BceGuildFire.getDefaultInstance(); 
      break;
    case 1071: 
      message = XinqiBceGuildInvite.BceGuildInvite.getDefaultInstance(); 
      break;
    case 1072: 
      message = XinqiBceGuildInviteRsp.BceGuildInviteRsp.getDefaultInstance(); 
      break;
    case 1073: 
      message = XinqiBceGuildIronLevelup.BceGuildIronLevelup.getDefaultInstance(); 
      break;
    case 1074: 
      message = XinqiBceGuildLevelUp.BceGuildLevelUp.getDefaultInstance(); 
      break;
    case 1075: 
      message = XinqiBceGuildLimit.BceGuildLimit.getDefaultInstance(); 
      break;
    case 1076: 
      message = XinqiBceGuildPosChange.BceGuildPosChange.getDefaultInstance(); 
      break;
    case 1077: 
      message = XinqiBceGuildReqCancel.BceGuildReqCancel.getDefaultInstance(); 
      break;
    case 1078: 
      message = XinqiBceGuildReqMemberList.BceGuildReqMemberList.getDefaultInstance(); 
      break;
    case 1079: 
      message = XinqiBceGuildRequest.BceGuildRequest.getDefaultInstance(); 
      break;
    case 1080: 
      message = XinqiBceGuildSearch.BceGuildSearch.getDefaultInstance(); 
      break;
    case 1081: 
      message = XinqiBceGuildSetPosName.BceGuildSetPosName.getDefaultInstance(); 
      break;
    case 1082: 
      message = XinqiBceGuildShopLevelup.BceGuildShopLevelup.getDefaultInstance(); 
      break;
    case 1083: 
      message = XinqiBceGuildTransfer.BceGuildTransfer.getDefaultInstance(); 
      break;
    case 1084: 
      message = XinqiBceHallRoomList.BceHallRoomList.getDefaultInstance(); 
      break;
    case 1085: 
      message = XinqiBceHallUserList.BceHallUserList.getDefaultInstance(); 
      break;
    case 1086: 
      message = XinqiBceHarvestTree.BceHarvestTree.getDefaultInstance(); 
      break;
    case 1087: 
      message = XinqiBceHeartbeat.BceHeartbeat.getDefaultInstance(); 
      break;
    case 1088: 
      message = XinqiBceInit.BceInit.getDefaultInstance(); 
      break;
    case 1089: 
      message = XinqiBceInvite.BceInvite.getDefaultInstance(); 
      break;
    case 1090: 
      message = XinqiBceKickUser.BceKickUser.getDefaultInstance(); 
      break;
    case 1091: 
      message = XinqiBceLeaveHall.BceLeaveHall.getDefaultInstance(); 
      break;
    case 1092: 
      message = XinqiBceLeaveMessage.BceLeaveMessage.getDefaultInstance(); 
      break;
    case 1093: 
      message = XinqiBceLeaveRoom.BceLeaveRoom.getDefaultInstance(); 
      break;
    case 1094: 
      message = XinqiBceLengthenIndate.BceLengthenIndate.getDefaultInstance(); 
      break;
    case 1095: 
      message = XinqiBceLoadProgress.BceLoadProgress.getDefaultInstance(); 
      break;
    case 1096: 
      message = XinqiBceLogin.BceLogin.getDefaultInstance(); 
      break;
    case 1097: 
      message = XinqiBceLottery.BceLottery.getDefaultInstance(); 
      break;
    case 1098: 
      message = XinqiBceMergeProp.BceMergeProp.getDefaultInstance(); 
      break;
    case 1099: 
      message = XinqiBceMoveProp.BceMoveProp.getDefaultInstance(); 
      break;
    case 1100: 
      message = XinqiBceMyRankInfo.BceMyRankInfo.getDefaultInstance(); 
      break;
    case 1101: 
      message = XinqiBceOnlineFrd.BceOnlineFrd.getDefaultInstance(); 
      break;
    case 1102: 
      message = XinqiBceOnlineReward.BceOnlineReward.getDefaultInstance(); 
      break;
    case 1103: 
      message = XinqiBcePRCEnter.BcePRCEnter.getDefaultInstance(); 
      break;
    case 1104: 
      message = XinqiBcePRCOpen.BcePRCOpen.getDefaultInstance(); 
      break;
    case 1105: 
      message = XinqiBcePickGold.BcePickGold.getDefaultInstance(); 
      break;
    case 1106: 
      message = XinqiBceReadyStart.BceReadyStart.getDefaultInstance(); 
      break;
    case 1107: 
      message = XinqiBceRegister.BceRegister.getDefaultInstance(); 
      break;
    case 1108: 
      message = XinqiBceRequestFriendInfo.BceRequestFriendInfo.getDefaultInstance(); 
      break;
    case 1109: 
      message = XinqiBceRobotOver.BceRobotOver.getDefaultInstance(); 
      break;
    case 1110: 
      message = XinqiBceRoleAttack.BceRoleAttack.getDefaultInstance(); 
      break;
    case 1111: 
      message = XinqiBceRoleChangeDirection.BceRoleChangeDirection.getDefaultInstance(); 
      break;
    case 1112: 
      message = XinqiBceRoleMove.BceRoleMove.getDefaultInstance(); 
      break;
    case 1113: 
      message = XinqiBceRoleMoveStart.BceRoleMoveStart.getDefaultInstance(); 
      break;
    case 1114: 
      message = XinqiBceRoleMoveStop.BceRoleMoveStop.getDefaultInstance(); 
      break;
    case 1115: 
      message = XinqiBceRolePower.BceRolePower.getDefaultInstance(); 
      break;
    case 1116: 
      message = XinqiBceRoleUseTool.BceRoleUseTool.getDefaultInstance(); 
      break;
    case 1117: 
      message = XinqiBceRoundOver.BceRoundOver.getDefaultInstance(); 
      break;
    case 1118: 
      message = XinqiBceSellProp.BceSellProp.getDefaultInstance(); 
      break;
    case 1119: 
      message = XinqiBceSellTool.BceSellTool.getDefaultInstance(); 
      break;
    case 1120: 
      message = XinqiBceSetGuildAnno.BceSetGuildAnno.getDefaultInstance(); 
      break;
    case 1121: 
      message = XinqiBceShopping.BceShopping.getDefaultInstance(); 
      break;
    case 1122: 
      message = XinqiBceShowUserInfo.BceShowUserInfo.getDefaultInstance(); 
      break;
    case 1123: 
      message = XinqiBceSplitProp.BceSplitProp.getDefaultInstance(); 
      break;
    case 1124: 
      message = XinqiBceSyncPos.BceSyncPos.getDefaultInstance(); 
      break;
    case 1125: 
      message = XinqiBceTaskReward.BceTaskReward.getDefaultInstance(); 
      break;
    case 1126: 
      message = XinqiBceTransfer.BceTransfer.getDefaultInstance(); 
      break;
    case 1127: 
      message = XinqiBceUseProp.BceUseProp.getDefaultInstance(); 
      break;
    case 1128: 
      message = XinqiBceUserConfig.BceUserConfig.getDefaultInstance(); 
      break;
    case 1129: 
      message = XinqiBceUserGuidAtk.BceUserGuidAtk.getDefaultInstance(); 
      break;
    case 1130: 
      message = XinqiBceVisit.BceVisit.getDefaultInstance(); 
      break;
    case 1131: 
      message = XinqiBceWaterTree.BceWaterTree.getDefaultInstance(); 
      break;
    case 661: 
      message = XinqiBseFinishAchievement.BseFinishAchievement.getDefaultInstance(); 
      break;
    case 662: 
      message = XinqiBseRedirect.BseRedirect.getDefaultInstance(); 
      break;
    case 663: 
      message = XinqiBseWebview.BseWebview.getDefaultInstance(); 
      break;
    case 664: 
      message = XinqiBseAchievements.BseAchievements.getDefaultInstance(); 
      break;
    case 665: 
      message = XinqiBseExpireEquipments.BseExpireEquipments.getDefaultInstance(); 
      break;
    case 666: 
      message = XinqiBseUserAchievements.BseUserAchievements.getDefaultInstance(); 
      break;
    case 1132: 
      message = XinqiBceLoginReady.BceLoginReady.getDefaultInstance(); 
      break;
    case 1133: 
      message = XinqiBceSendGift.BceSendGift.getDefaultInstance(); 
      break;
    case 667: 
      message = XinqiBseSendGift.BseSendGift.getDefaultInstance(); 
      break;
    case 1134: 
      message = XinqiBceBulletTrack.BceBulletTrack.getDefaultInstance(); 
      break;
    case 668: 
      message = XinqiBseBulletTrack.BseBulletTrack.getDefaultInstance(); 
      break;
    case 669: 
      message = XinqiBseZip.BseZip.getDefaultInstance(); 
      break;
    case 1135: 
      message = XinqiBceConfirm.BceConfirm.getDefaultInstance(); 
      break;
    case 670: 
      message = XinqiBseConfirm.BseConfirm.getDefaultInstance(); 
      break;
    case 1136: 
      message = XinqiBceFindFriend.BceFindFriend.getDefaultInstance(); 
      break;
    case 671: 
      message = XinqiBseFindFriend.BseFindFriend.getDefaultInstance(); 
      break;
    case 1137: 
      message = XinqiBceUserRefresh.BceUserRefresh.getDefaultInstance(); 
      break;
    case 1138: 
      message = XinqiBceLogout.BceLogout.getDefaultInstance(); 
      break;
    case 672: 
      message = XinqiBseLogout.BseLogout.getDefaultInstance(); 
      break;
    case 1139: 
      message = XinqiBceWeibo.BceWeibo.getDefaultInstance(); 
      break;
    case 1140: 
      message = XinqiBceRoomUserList.BceRoomUserList.getDefaultInstance(); 
      break;
    case 673: 
      message = XinqiBseRoomUserList.BseRoomUserList.getDefaultInstance(); 
      break;
    case 1141: 
      message = XinqiBceSysMessage.BceSysMessage.getDefaultInstance(); 
      break;
    case 1142: 
      message = XinqiBceForgetPassword.BceForgetPassword.getDefaultInstance(); 
      break;
    case 1143: 
      message = XinqiBceCloseBag.BceCloseBag.getDefaultInstance(); 
      break;
    case 1144: 
      message = XinqiBceTraining.BceTraining.getDefaultInstance(); 
      break;
    case 1145: 
      message = XinqiBceTrack.BceTrack.getDefaultInstance(); 
      break;
    case 674: 
      message = XinqiBseMailList.BseMailList.getDefaultInstance(); 
      break;
    case 675: 
      message = XinqiBseMailReceive.BseMailReceive.getDefaultInstance(); 
      break;
    case 676: 
      message = XinqiBseMailSend.BseMailSend.getDefaultInstance(); 
      break;
    case 1146: 
      message = XinqiBceMailDelete.BceMailDelete.getDefaultInstance(); 
      break;
    case 1147: 
      message = XinqiBceMailSend.BceMailSend.getDefaultInstance(); 
      break;
    case 677: 
      message = XinqiBseMailDelete.BseMailDelete.getDefaultInstance(); 
      break;
    case 1148: 
      message = XinqiBceMailRead.BceMailRead.getDefaultInstance(); 
      break;
    case 1149: 
      message = XinqiBceMailTake.BceMailTake.getDefaultInstance(); 
      break;
    case 678: 
      message = XinqiBseMailRead.BseMailRead.getDefaultInstance(); 
      break;
    case 679: 
      message = XinqiBseMailTake.BseMailTake.getDefaultInstance(); 
      break;
    case 680: 
      message = XinqiBseRoomUnlock.BseRoomUnlock.getDefaultInstance(); 
      break;
    case 1150: 
      message = XinqiBceBossList.BceBossList.getDefaultInstance(); 
      break;
    case 1151: 
      message = XinqiBceBossTakeReward.BceBossTakeReward.getDefaultInstance(); 
      break;
    case 681: 
      message = XinqiBseBossList.BseBossList.getDefaultInstance(); 
      break;
    case 682: 
      message = XinqiBseBossSync.BseBossSync.getDefaultInstance(); 
      break;
    case 683: 
      message = XinqiBseBossTakeReward.BseBossTakeReward.getDefaultInstance(); 
      break;
    case 684: 
      message = XinqiBseFuncUnlock.BseFuncUnlock.getDefaultInstance(); 
      break;
    case 1152: 
      message = XinqiBceCraftPrice.BceCraftPrice.getDefaultInstance(); 
      break;
    case 685: 
      message = XinqiBseCraftPrice.BseCraftPrice.getDefaultInstance(); 
      break;
    case 1153: 
      message = XinqiBceActionLimitQuery.BceActionLimitQuery.getDefaultInstance(); 
      break;
    case 686: 
      message = XinqiBseActionLimitQuery.BseActionLimitQuery.getDefaultInstance(); 
      break;
    case 1154: 
      message = XinqiBceActionLimitBuy.BceActionLimitBuy.getDefaultInstance(); 
      break;
    case 687: 
      message = XinqiBseActionLimitBuy.BseActionLimitBuy.getDefaultInstance(); 
      break;
    case 1155: 
      message = XinqiBceCaishenPray.BceCaishenPray.getDefaultInstance(); 
      break;
    case 1156: 
      message = XinqiBceCaishenQuery.BceCaishenQuery.getDefaultInstance(); 
      break;
    case 1157: 
      message = XinqiBceTreasureHuntBuy.BceTreasureHuntBuy.getDefaultInstance(); 
      break;
    case 1158: 
      message = XinqiBceTreasureHuntQuery.BceTreasureHuntQuery.getDefaultInstance(); 
      break;
    case 688: 
      message = XinqiBseCaishenPray.BseCaishenPray.getDefaultInstance(); 
      break;
    case 689: 
      message = XinqiBseCaishenQuery.BseCaishenQuery.getDefaultInstance(); 
      break;
    case 690: 
      message = XinqiBseTreasureHuntBuy.BseTreasureHuntBuy.getDefaultInstance(); 
      break;
    case 691: 
      message = XinqiBseTreasureHuntQuery.BseTreasureHuntQuery.getDefaultInstance(); 
      break;
    case 1159: 
      message = XinqiBceChargeInternal.BceChargeInternal.getDefaultInstance(); 
      break;
    case 1160: 
      message = XinqiBceOfflinePush.BceOfflinePush.getDefaultInstance(); 
      break;
    case 1161: 
      message = XinqiBceReloadConfig.BceReloadConfig.getDefaultInstance(); 
      break;
    case 1162: 
      message = XinqiBceCyLogin.BceCyLogin.getDefaultInstance(); 
      break;
    case 1163: 
      message = XinqiBceCyRegister.BceCyRegister.getDefaultInstance(); 
      break;
    case 692: 
      message = XinqiBseCyLogin.BseCyLogin.getDefaultInstance(); 
      break;
    case 693: 
      message = XinqiBseCyRegister.BseCyRegister.getDefaultInstance(); 
      break;
    case 1164: 
      message = XinqiBceCreateRole.BceCreateRole.getDefaultInstance(); 
      break;
    case 694: 
      message = XinqiBseCreateRole.BseCreateRole.getDefaultInstance(); 
      break;
    case 1165: 
      message = XinqiBceUserSync.BceUserSync.getDefaultInstance(); 
      break;
    case 695: 
      message = XinqiBseUserSync.BseUserSync.getDefaultInstance(); 
      break;
    case 1166: 
      message = XinqiBceGetValue.BceGetValue.getDefaultInstance(); 
      break;
    case 696: 
      message = XinqiBseGetValue.BseGetValue.getDefaultInstance(); 
      break;
    case 1167: 
      message = XinqiBceBulletin.BceBulletin.getDefaultInstance(); 
      break;
    case 1168: 
      message = XinqiBceChargeCard.BceChargeCard.getDefaultInstance(); 
      break;
    case 1169: 
      message = XinqiBcePropDataQuery.BcePropDataQuery.getDefaultInstance(); 
      break;
    case 1170: 
      message = XinqiBceServerList.BceServerList.getDefaultInstance(); 
      break;
    case 1171: 
      message = XinqiBceUserInput.BceUserInput.getDefaultInstance(); 
      break;
    case 1172: 
      message = XinqiBceUserRoleList.BceUserRoleList.getDefaultInstance(); 
      break;
    case 1173: 
      message = XinqiBceUserStatusList.BceUserStatusList.getDefaultInstance(); 
      break;
    case 1174: 
      message = XinqiBceVipInfo.BceVipInfo.getDefaultInstance(); 
      break;
    case 697: 
      message = XinqiBseChargeCard.BseChargeCard.getDefaultInstance(); 
      break;
    case 698: 
      message = XinqiBsePropDataQuery.BsePropDataQuery.getDefaultInstance(); 
      break;
    case 699: 
      message = XinqiBseServerList.BseServerList.getDefaultInstance(); 
      break;
    case 700: 
      message = XinqiBseUserInput.BseUserInput.getDefaultInstance(); 
      break;
    case 701: 
      message = XinqiBseUserRoleList.BseUserRoleList.getDefaultInstance(); 
      break;
    case 702: 
      message = XinqiBseUserStatusList.BseUserStatusList.getDefaultInstance(); 
      break;
    case 703: 
      message = XinqiBseVipInfo.BseVipInfo.getDefaultInstance(); 
      break;
    case 704: 
      message = XinqiBsePromotion.BsePromotion.getDefaultInstance(); 
      break;
    case 1175: 
      message = XinqiBceSelectRole.BceSelectRole.getDefaultInstance(); 
      break;
    case 1176: 
      message = XinqiBceDeleteRole.BceDeleteRole.getDefaultInstance(); 
      break;
    case 705: 
      message = XinqiBseDeleteRole.BseDeleteRole.getDefaultInstance(); 
      break;
    case 706: 
      message = XinqiBseBattleStatusUpdate.BseBattleStatusUpdate.getDefaultInstance(); 
      break;
    case 1177: 
      message = XinqiBceChangeAutomode.BceChangeAutomode.getDefaultInstance(); 
      break;
    case 707: 
      message = XinqiBseChangeAutomode.BseChangeAutomode.getDefaultInstance(); 
      break;
    case 1178: 
      message = XinqiBceExitGame.BceExitGame.getDefaultInstance(); 
      break;
    case 708: 
      message = XinqiBseExitGame.BseExitGame.getDefaultInstance(); 
      break;
    case 709: 
      message = XinqiBseModFriend.BseModFriend.getDefaultInstance(); 
      break;
    case 1179: 
      message = XinqiBceFillProfile.BceFillProfile.getDefaultInstance(); 
      break;
    case 710: 
      message = XinqiBseFillProfile.BseFillProfile.getDefaultInstance(); 
      break;
    case 711: 
      message = XinqiBseBattleAddRole.BseBattleAddRole.getDefaultInstance(); 
      break;
    case 1180: 
      message = XinqiBceGetUserBiblio.BceGetUserBiblio.getDefaultInstance(); 
      break;
    case 712: 
      message = XinqiBseGetUserBiblio.BseGetUserBiblio.getDefaultInstance(); 
      break;
    case 1181: 
      message = XinqiBceTakeUserBiblioReward.BceTakeUserBiblioReward.getDefaultInstance(); 
      break;
    case 713: 
      message = XinqiBseTakeUserBiblioReward.BseTakeUserBiblioReward.getDefaultInstance(); 
      break;
    case 1182: 
      message = XinqiBceVoiceChat.BceVoiceChat.getDefaultInstance(); 
      break;
    case 714: 
      message = XinqiBseVoiceChat.BseVoiceChat.getDefaultInstance(); 
      break;
    case 1183: 
      message = XinqiBceGuildAbilityList.BceGuildAbilityList.getDefaultInstance(); 
      break;
    case 1184: 
      message = XinqiBceGuildApplyList.BceGuildApplyList.getDefaultInstance(); 
      break;
    case 1185: 
      message = XinqiBceGuildApplyProcess.BceGuildApplyProcess.getDefaultInstance(); 
      break;
    case 1186: 
      message = XinqiBceGuildBag.BceGuildBag.getDefaultInstance(); 
      break;
    case 1187: 
      message = XinqiBceGuildBagEvent.BceGuildBagEvent.getDefaultInstance(); 
      break;
    case 1188: 
      message = XinqiBceGuildChangeAnnounce.BceGuildChangeAnnounce.getDefaultInstance(); 
      break;
    case 1189: 
      message = XinqiBceGuildChangeRole.BceGuildChangeRole.getDefaultInstance(); 
      break;
    case 1190: 
      message = XinqiBceGuildContributeQuery.BceGuildContributeQuery.getDefaultInstance(); 
      break;
    case 1191: 
      message = XinqiBceGuildFacilityLevelList.BceGuildFacilityLevelList.getDefaultInstance(); 
      break;
    case 1192: 
      message = XinqiBceGuildFacilityLevelUp.BceGuildFacilityLevelUp.getDefaultInstance(); 
      break;
    case 1193: 
      message = XinqiBceGuildFacilityList.BceGuildFacilityList.getDefaultInstance(); 
      break;
    case 1194: 
      message = XinqiBceGuildList.BceGuildList.getDefaultInstance(); 
      break;
    case 1195: 
      message = XinqiBceGuildMemberList.BceGuildMemberList.getDefaultInstance(); 
      break;
    case 1196: 
      message = XinqiBceGuildPrivilegeList.BceGuildPrivilegeList.getDefaultInstance(); 
      break;
    case 1197: 
      message = XinqiBceGuildSearchMember.BceGuildSearchMember.getDefaultInstance(); 
      break;
    case 1198: 
      message = XinqiBceGuildShopping.BceGuildShopping.getDefaultInstance(); 
      break;
    case 715: 
      message = XinqiBseGuildAbilityList.BseGuildAbilityList.getDefaultInstance(); 
      break;
    case 716: 
      message = XinqiBseGuildApplyList.BseGuildApplyList.getDefaultInstance(); 
      break;
    case 717: 
      message = XinqiBseGuildApplyProcess.BseGuildApplyProcess.getDefaultInstance(); 
      break;
    case 718: 
      message = XinqiBseGuildBag.BseGuildBag.getDefaultInstance(); 
      break;
    case 719: 
      message = XinqiBseGuildBagEvent.BseGuildBagEvent.getDefaultInstance(); 
      break;
    case 720: 
      message = XinqiBseGuildChangeAnnounce.BseGuildChangeAnnounce.getDefaultInstance(); 
      break;
    case 721: 
      message = XinqiBseGuildChangeRole.BseGuildChangeRole.getDefaultInstance(); 
      break;
    case 722: 
      message = XinqiBseGuildContributeQuery.BseGuildContributeQuery.getDefaultInstance(); 
      break;
    case 723: 
      message = XinqiBseGuildFacilityLevelList.BseGuildFacilityLevelList.getDefaultInstance(); 
      break;
    case 724: 
      message = XinqiBseGuildFacilityLevelUp.BseGuildFacilityLevelUp.getDefaultInstance(); 
      break;
    case 725: 
      message = XinqiBseGuildFacilityList.BseGuildFacilityList.getDefaultInstance(); 
      break;
    case 726: 
      message = XinqiBseGuildMemberList.BseGuildMemberList.getDefaultInstance(); 
      break;
    case 727: 
      message = XinqiBseGuildPrivilegeList.BseGuildPrivilegeList.getDefaultInstance(); 
      break;
    case 728: 
      message = XinqiBseGuildSearchMember.BseGuildSearchMember.getDefaultInstance(); 
      break;
    case 729: 
      message = XinqiBseGuildShopping.BseGuildShopping.getDefaultInstance(); 
      break;
    case 1199: 
      message = XinqiBceGetVoiceChat.BceGetVoiceChat.getDefaultInstance(); 
      break;
    case 730: 
      message = XinqiBseGetVoiceChat.BseGetVoiceChat.getDefaultInstance(); 
      break;
    case 1200: 
      message = XinqiBceGuildApply.BceGuildApply.getDefaultInstance(); 
      break;
    case 731: 
      message = XinqiBseGuildApply.BseGuildApply.getDefaultInstance(); 
      break;
    case 1201: 
      message = XinqiBceForbidUser.BceForbidUser.getDefaultInstance(); 
      break;
    case 732: 
      message = XinqiBseForbidUser.BseForbidUser.getDefaultInstance(); 
      break;
    case 1202: 
      message = XinqiBceGuildBagPut.BceGuildBagPut.getDefaultInstance(); 
      break;
    case 1203: 
      message = XinqiBceGuildBagTake.BceGuildBagTake.getDefaultInstance(); 
      break;
    case 733: 
      message = XinqiBseGuildBagPut.BseGuildBagPut.getDefaultInstance(); 
      break;
    case 734: 
      message = XinqiBseGuildBagTake.BseGuildBagTake.getDefaultInstance(); 
      break;
    default:
      log.error("No message type for id: " + id);
    }
    return message;
  }
}
