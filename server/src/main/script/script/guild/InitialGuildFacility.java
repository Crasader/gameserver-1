package script.guild;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 创建公会时，初始化公会设施
 * 
 * @author wangqi
 *
 */
public class InitialGuildFacility {
	
	private static final int MIN_LEVEL = 20;
	private static final int MIN_GOLDEN = 500000;

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		GuildMember member = user.getGuildMember();
		Guild guild = (Guild)parameters[1];
		
		GuildFacility facility = new GuildFacility();
		facility.setLevel(1);
		facility.setType(GuildFacilityType.guild);
		guild.addFacility(facility);
		
		facility = new GuildFacility();
		facility.setLevel(0);
		facility.setType(GuildFacilityType.shop);
		guild.addFacility(facility);
		
		facility = new GuildFacility();
		facility.setLevel(0);
		facility.setType(GuildFacilityType.craft);
		guild.addFacility(facility);
		
		facility = new GuildFacility();
		facility.setLevel(0);
		facility.setType(GuildFacilityType.storage);
		guild.addFacility(facility);
		
		facility = new GuildFacility();
		facility.setLevel(0);
		facility.setType(GuildFacilityType.ability);
		guild.addFacility(facility);
		

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

}
