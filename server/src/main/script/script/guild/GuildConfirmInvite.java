package script.guild;

import java.util.Map;

import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.Status;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class GuildConfirmInvite {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		
		boolean success = false;
		User user = (User)parameters[0];
		Map map = (Map)parameters[1];
		
		doJoin(success, user, map);
		
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * @param success
	 * @param user
	 * @param map
	 */
	public static void doJoin(boolean success, User user, Map map) {
		String guildId = (String)map.get("guildId");
		if ( guildId != null ) {
			Guild guild = GuildManager.getInstance().queryGuildById(guildId);
			if ( guild != null ) {
				Status status = GuildManager.getInstance().joinGuild(user, guild, true);
				success = status.isSuccess();
			}
		}
		if ( !success ) {
			SysMessageManager.getInstance().sendClientInfoMessage(
					user.getSessionKey(), "guild.apply.full", Type.NORMAL);
		} else {
			SysMessageManager.getInstance().sendClientInfoMessage(
					user.getSessionKey(), "guild.apply.success", Type.NORMAL);
		}
	}
}
