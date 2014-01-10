package script.guild;

import java.util.ArrayList;
import java.util.Set;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.guild.GuildPrivilege;
import com.xinqihd.sns.gameserver.guild.GuildRole;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Check if the given user in guild has right privledge to do something.
 * 
					修改公告	职位调整	招收成员	开除成员	建筑升级	公会战  公会仓库
		会长		  √		    √	  		√				√				√				√      √       
		副会长		√	      √				√								√				√      √
		官员											√								√				√      √
		精英																							√      √
		会员																							√
 * 
 * @author wangqi
 *
 */
public class CheckPrivilege {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		GuildPrivilege privilege = (GuildPrivilege)parameters[1];
		GuildMember member = user.getGuildMember();
		boolean hasPrivilege = false;
		if ( member != null ) {
			GuildRole role = member.getRole();
			Set privSet = (Set)GuildListPrivilege.privilegeMap.get(role);
			if ( privSet.contains(privilege) ) {
				hasPrivilege = true;
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(hasPrivilege);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
