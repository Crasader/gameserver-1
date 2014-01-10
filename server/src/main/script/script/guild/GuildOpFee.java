package script.guild;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildCraftType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 计算公会的周维持费用
 * 
 * @author wangqi
 */
public class GuildOpFee {
	
	private static final Logger logger = LoggerFactory.getLogger(GuildOpFee.class);
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		Guild guild = (Guild)parameters[1];
		
		int[] fees = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.GUILD_OPFEE);
		int level = guild.getLevel();
		int fee = fees[level-1];

		ArrayList list = new ArrayList();
		list.add(fee);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
