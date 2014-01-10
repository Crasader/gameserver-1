package script.reward;

import java.util.ArrayList;
import java.util.HashSet;

import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * BOSS挑战成功后，可以从生成的四项物品中随机挑选一种
 * 简单难度
    20001	水神石Lv1
		20002	水神石Lv2
		20006	土神石Lv1
		20007	土神石Lv2
		20011	风神石Lv1
		20012	风神石Lv2
		20016	火神石Lv1
		20017	火神石Lv2
		20021	强化石Lv1
		20022	强化石Lv2
		20026	神恩符Lv1
		20027	神恩符Lv2
		20031	黄钻石Lv1
		20032	黄钻石Lv2
		26004	绿色熔炼符
		26005	蓝色熔炼符
		26008	武器熔炼符
		26009	装备熔炼符
		20041	水晶石Lv1
		20042	水晶石Lv2

	 普通难度
	 	20003	水神石Lv3
	 	20008	土神石Lv3
	 	20013	风神石Lv3
	 	20018	火神石Lv3
	 	20023	强化石Lv3
		20028	神恩符Lv3
		20033	黄钻石Lv3
		21001	水神石炼化符
		21002	土神石炼化符
		21003	风神石炼化符
		21004	火神石炼化符
		21005	强化石炼化符
	 	24002	幸运符+15%
	 	26006	粉色熔炼符
		26011	精良装备熔炼符
		20043	水晶石Lv3
	 
	 困难难度
		20004	水神石Lv4
		20005	水神石Lv5
		20009	土神石Lv4
		20010	土神石Lv5
		20014	风神石Lv4
		20015	风神石Lv5
		20019	火神石Lv4
		20020	火神石Lv5
		20024	强化石Lv4
		20025	强化石Lv5
		20029	神恩符Lv4
		20030	神恩符Lv5
		20034	黄钻石Lv4
		20035	黄钻石Lv5
		21006	神恩符炼化符
		24004	幸运符+25%
		26010	精良武器熔炼符
		26007	橙色熔炼符
		26012	紫色熔炼符
		30014	双倍经验卡
		20044	水晶石Lv4
		20045	水晶石Lv5


 * @author wangqi
 *
 */
public class BossItemReward {
	
	private static final String[] SIMPLE_ITEM_IDS = new String[]{
		"20001", "20002", "20006", "20007", "20011", "20012", "20016", "20017", "20021", "20022", "20026", "20027", "20031", "20032", "26004", "26005", "26008", "26009", "20041", "20042",
	};
	private static final String[] NORMAL_ITEM_IDS = new String[]{
		"20003", "20008", "20013", "20018", "20023", "20028", "20033", "21001", "21002", "21003", "21004", "21005", "24002", "26006", "26011", "20043",
	};
	private static final String[] HARD_ITEM_IDS = new String[]{
		"20004", "20005", "20009", "20010", "20014", "20015", "20019", "20020", "20024", "20025", "20029", "20030", "20034", "20035", "21006", "24004", "26010", "26007", "26012", "30014", "20044", //"20045",		
	};
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		Boss boss = (Boss)parameters[1];
		int count = (Integer)parameters[2];
		
		BossPojo bossPojo = boss.getBossPojo();
		ArrayList rewards = new ArrayList();

		HashSet itemSet = new HashSet();
		//for ( int i=0; i<count; i++ ) {
		while ( true ) {
			String itemId = null;
			if ( bossPojo.getLevel() == 0 ) {
				itemId = SIMPLE_ITEM_IDS[(int)(MathUtil.nextDouble()*SIMPLE_ITEM_IDS.length)];
			} else if ( bossPojo.getLevel() == 1 ) {
				itemId = NORMAL_ITEM_IDS[(int)(MathUtil.nextDouble()*NORMAL_ITEM_IDS.length)];
			} else if ( bossPojo.getLevel() == 2 ) {
				itemId = HARD_ITEM_IDS[(int)(MathUtil.nextDouble()*HARD_ITEM_IDS.length)];
			}
			if ( itemId != null ) {
				if ( !itemSet.contains(itemId) ) {
					itemSet.add(itemId);
					ItemPojo item = ItemManager.getInstance().getItemById(itemId);
					if ( item != null ) {
						rewards.add(RewardManager.getInstance().getRewardItem(item));
						if ( rewards.size() > count ) {
							break;
						}
					}
				}
			}
		}

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(rewards);
		return result;
	}

}
