package script.guild;

/**
 * 计算公会设施立即冷却所需要的元宝数量
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.guild.GuildPrivilege;
import com.xinqihd.sns.gameserver.guild.GuildRole;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Check if the given user in guild has right privledge to do something.
 * 
			等级	小时	元宝系数	元宝
			1	24	10	240
			2	48	8	1536
			3	120	6	6480
			4	192	4	12288
			5	360	4	36000

			元宝=元宝系数*等级*等级*小时
 * 
 * @author wangqi
 *
 */
public class CalculateCDYuanbao {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		GuildFacility facility = (GuildFacility)parameters[1];
		
		double yuanbaoPerSecond = GameDataManager.getInstance().
				getGameDataAsDouble(GameDataKey.GUILD_FACILITY_COOLDOWN_YUANBAO, 10/3600);
		int coolDownSecond = GuildManager.getInstance().getGuildFacilityCoolDown(facility);
		int yuanbao = (int)Math.round(yuanbaoPerSecond*coolDownSecond);
		
		ArrayList list = new ArrayList();
		list.add(yuanbao);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
