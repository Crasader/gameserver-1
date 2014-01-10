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
public class InitialUserFacility {
	
	private static final int MIN_LEVEL = 20;
	private static final int MIN_GOLDEN = 500000;

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		GuildMember member = (GuildMember)parameters[1];
		
		/**
		 * 为玩家技能添加子技能
		 */
		GuildFacility subfac = new GuildFacility();
		subfac.setLevel(0);
		subfac.setType(GuildFacilityType.ab_attack);
		subfac.setEnabled(false);
		//facility.addFacility(subfac);
		member.addFacility(subfac);
		
		subfac = new GuildFacility();
		subfac.setLevel(0);
		subfac.setType(GuildFacilityType.ab_defend);
		//facility.addFacility(subfac);
		member.addFacility(subfac);

		subfac = new GuildFacility();
		subfac.setLevel(0);
		subfac.setType(GuildFacilityType.ab_agility);
		subfac.setEnabled(false);
		//facility.addFacility(subfac);
		member.addFacility(subfac);

		subfac = new GuildFacility();
		subfac.setLevel(0);
		subfac.setType(GuildFacilityType.ab_lucky);
		subfac.setEnabled(false);
		//facility.addFacility(subfac);
		member.addFacility(subfac);

		subfac = new GuildFacility();
		subfac.setLevel(0);
		subfac.setType(GuildFacilityType.ab_pray);
		subfac.setEnabled(false);
		//facility.addFacility(subfac);
		member.addFacility(subfac);

		subfac = new GuildFacility();
		subfac.setLevel(0);
		subfac.setType(GuildFacilityType.ab_treasure);
		subfac.setEnabled(false);
		//facility.addFacility(subfac);
		member.addFacility(subfac);

		subfac = new GuildFacility();
		subfac.setLevel(0);
		subfac.setType(GuildFacilityType.ab_blood);
		subfac.setEnabled(false);
		//facility.addFacility(subfac);
		member.addFacility(subfac);

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

}
