package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Called by CaishenManager to collect daily user pray count.
 * 
 * @author wangqi
 *
 */
public class CaishenPrayLimitDaily {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		int dbTodayBuy = StringUtil.toInt((String)parameters[1], 0);
		
	  //当日可用的购买次数
		//         VIP1	VIP2	VIP3	VIP4	VIP5	VIP6	VIP7	VIP8	VIP9	VIP10
		//购买招财次数	3	5	7	9	30	50	100	200	300	600
		int buyCount = VipManager.getInstance().getVipLevelCaishenBuyCount(user);
		if ( buyCount < 1 ) {
			//Free user can buy 1 time.
			buyCount = 1;
		}
		//购买的价格
		int buyPrice = 2;
		if ( dbTodayBuy > 2 ) {
			//价格逐渐加成, 每日每购买一次增加2元宝消耗，直到达到50元宝上限
			buyPrice += dbTodayBuy*2;
			if ( buyPrice > 50 ) {
				buyPrice = 50;
			}
		}
		//每次祈福获得的金币数量
		int buyValue = 50000;
		if ( user.getGuildMember() != null ) {
			GuildFacility prayFac = user.getGuildMember().getFacility(GuildFacilityType.ab_pray);
			if ( prayFac != null && prayFac.getLevel()>=1 ) {
				int[] goldens = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.GUILD_ABILITY_PRAY);
				buyValue += goldens[prayFac.getLevel()-1];
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(buyCount);
		list.add(buyPrice);
		list.add(buyValue);
		list.add(dbTodayBuy);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
