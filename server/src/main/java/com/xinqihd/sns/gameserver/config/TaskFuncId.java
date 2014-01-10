package com.xinqihd.sns.gameserver.config;

import static com.xinqihd.sns.gameserver.config.TaskType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.mongo.GameResourceManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseTask;
import com.xinqihd.sns.gameserver.script.ScriptHook;

/**
* 任务可能关联到相关的游戏功能
* 上，用funcid表示准备激活的
* 游戏界面.
* 
*/
public enum TaskFuncId  {	
	//寻宝
	treasure,
	//祈福
	pray,
	//充值
	charge,
	//VIP
	vip,
	//背包
	bag,
	//铁匠铺-强化
	craftstrength,
  //铁匠铺-合成
	craftforge,
  //铁匠铺-熔炼
	craftcompose,
	//铁匠铺-转移
	crafttransfer,
	//名人堂
	toplist,
	//聊天
	chat,
	//社交
	social,
	//成就
	achievement,
	//体力
	action,
	//游戏设置
	setting,
	//商城
	shop,
	//完善信息
	profile,
	//新浪微博
	sinaweibo,
	//腾讯微薄
	qqweibo,
	//人人
	renren
	
}
