package script.guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
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
public class GuildListPrivilege {
	
	public static final HashMap privilegeMap = new HashMap();
	static {
		{
			HashSet set = new HashSet();
			set.add(GuildPrivilege.announce);
			set.add(GuildPrivilege.guildrole);
			set.add(GuildPrivilege.recruit);
			set.add(GuildPrivilege.firememeber);
			set.add(GuildPrivilege.levelup);
			set.add(GuildPrivilege.combat);
			set.add(GuildPrivilege.takebag);
			privilegeMap.put(GuildRole.chief, set);
		}
		{
			HashSet set = new HashSet();
			set.add(GuildPrivilege.announce);
			set.add(GuildPrivilege.guildrole);
			set.add(GuildPrivilege.recruit);
			set.add(GuildPrivilege.firememeber);
			set.add(GuildPrivilege.levelup);
			set.add(GuildPrivilege.combat);
			set.add(GuildPrivilege.takebag);
			privilegeMap.put(GuildRole.director, set);
		}
		{
			HashSet set = new HashSet();
			set.add(GuildPrivilege.recruit);
			set.add(GuildPrivilege.levelup);
			set.add(GuildPrivilege.combat);
			set.add(GuildPrivilege.takebag);
			privilegeMap.put(GuildRole.manager, set);
		}
		{
			HashSet set = new HashSet();
			set.add(GuildPrivilege.combat);
			set.add(GuildPrivilege.takebag);
			privilegeMap.put(GuildRole.elite, set);
		}
		{
			HashSet set = new HashSet();
			set.add(GuildPrivilege.combat);
			privilegeMap.put(GuildRole.member, set);
		}
	}

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		
		ArrayList list = new ArrayList();
		list.add(privilegeMap);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
