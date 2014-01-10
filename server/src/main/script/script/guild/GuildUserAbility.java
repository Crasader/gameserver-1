package script.guild;

import java.util.ArrayList;

import sun.misc.Perf.GetPerfAction;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 当玩家登陆时，自动计算公会的技能加成
 * @author wangqi
 *
 */
public class GuildUserAbility {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		GuildMember memeber = user.getGuildMember();
		double[] attackAdded = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_ABILITY_ATTACK);
		double[] defendAdded = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_ABILITY_DEFEND);
		double[] agilityAdded = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_ABILITY_AGILITY);
		double[] luckyAdded = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_ABILITY_LUCKY);
		double[] bloodAdded = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_ABILITY_BLOOD);
		int addedValue = 0;
		if ( memeber != null ) {
			GuildFacility attackFac = memeber.getFacility(GuildFacilityType.ab_attack);
			addedValue = (int)Math.round(user.getAttack() * (getPercent(attackAdded, attackFac.getLevel())));
			user.addValueMap(PropDataEnhanceField.ATTACK, addedValue);
			GuildFacility defendFac = memeber.getFacility(GuildFacilityType.ab_defend);
			addedValue = (int)Math.round(user.getDefend() * (getPercent(defendAdded, defendFac.getLevel())));
			user.addValueMap(PropDataEnhanceField.DEFEND, addedValue);
			GuildFacility agilityFac = memeber.getFacility(GuildFacilityType.ab_agility);
			addedValue = (int)Math.round(user.getAgility() * (getPercent(agilityAdded, agilityFac.getLevel())));
			user.addValueMap(PropDataEnhanceField.AGILITY, addedValue);
			GuildFacility luckyFac = memeber.getFacility(GuildFacilityType.ab_lucky);
			addedValue = (int)Math.round(user.getLuck() * (getPercent(luckyAdded, luckyFac.getLevel())));
			user.addValueMap(PropDataEnhanceField.LUCKY, addedValue);
			GuildFacility bloodFac = memeber.getFacility(GuildFacilityType.ab_blood);
			addedValue = (int)Math.round(user.getBlood() * (getPercent(bloodAdded, bloodFac.getLevel())));
			user.addValueMap(PropDataEnhanceField.BLOOD, addedValue);
		}

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
	private static double getPercent(double[] array, int facLevel) {
		int facIndex = facLevel - 1;
		if ( facIndex < 0 ) return 0;
		if ( facIndex>=array.length ) facIndex = array.length-1;
		return array[facIndex];
	}
}
