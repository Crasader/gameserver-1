package script.guild;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.CommonUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Check if the guild can be created by given user.
 * 
 * 创建公会条件：
     A）玩家等级≥20级
     B）需要500000金币
 * 
 * @author wangqi
 *
 */
public class CreateCheck {
	
	private static final int MIN_LEVEL = 20;

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		String guildName = (String)parameters[1];
		
		Boolean success = Boolean.FALSE;
		String message = null;
		int minGolden = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.GUILD_CREATE_GOLDEN, 500000);
		if ( user.getLevel() < MIN_LEVEL ) {
			message = Text.text("guild.create.nolevel", MIN_LEVEL);
		} else if ( user.getGolden() < minGolden ) {
			message = Text.text("guild.create.nogolden", minGolden);
		} else {
			//Check if the user is already in a guild
			GuildMember member = user.getGuildMember();
			if ( member != null ) {
				success = Boolean.FALSE;
				message = Text.text("guild.create.alreadyin");
			} else if ( StringUtil.checkNotEmpty(guildName) ) {
			  //Check if the name is duplicate
				int guildNameCount = CommonUtil.countString(guildName);
				if ( guildNameCount < 1 || guildNameCount>5 ) {
					message = Text.text("guild.create.namesize");
				} else {
					if ( GuildManager.getInstance().checkGuildIdExist(guildName) ) {
						message = Text.text("guild.create.namedup", guildName);	
					} else {
						success = Boolean.TRUE;
					}
				}
			} else {
				message = Text.text("guild.create.noname");
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(success);
		list.add(message);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
