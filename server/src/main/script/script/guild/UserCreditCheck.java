package script.guild;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 玩家在使用公会设施前需要检查玩家的贡献是否满足公会设施的要求
 * 
 * @author wangqi
 */
public class UserCreditCheck {
	
	private static final Logger logger = LoggerFactory.getLogger(UserCreditCheck.class);
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		GuildFacility facility = (GuildFacility)parameters[1];
		GuildMember member = user.getGuildMember();
		int[][] minCredits = GameDataManager.getInstance().getGameDataAsIntArrayArray(
				GameDataKey.GUILD_FACILITY_MIN_CREDIT);
		int index = facility.getType().index();
		int[] minCredit = minCredits[index];
		
		int levelIndex = facility.getLevel()-1;
		Boolean success = Boolean.FALSE;
		if ( levelIndex >= 0 && member.getCredit() >= minCredit[levelIndex] ) {
			success = Boolean.TRUE;
		} else {
			for ( int i=facility.getLevel()-1; i>=0; i-- ) {
				if ( member.getCredit() >= minCredit[i] ) {
					levelIndex = i;
					break;
				}
			}
		}
		String message = null;
		if ( levelIndex >= 0 ) {
			success = Boolean.TRUE;
		} else {
			success = Boolean.FALSE;
			message = Text.text("guild.facility.unmeetcredit", new Object[]{member.getCredit(), minCredit[0]});
		}
		
		ArrayList list = new ArrayList();
		list.add(success);
		list.add(message);
		list.add(levelIndex);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
