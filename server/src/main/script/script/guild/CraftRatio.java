package script.guild;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildCraftType;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 公会铁匠铺的百分比加成
 * 
 * @author wangqi
 */
public class CraftRatio {
	
	private static final Logger logger = LoggerFactory.getLogger(CraftRatio.class);
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		GuildCraftType type = (GuildCraftType)parameters[1];
		
		Guild guild = user.getGuild();
		double ratio = 0.0;
		if ( guild != null ) {
			if ( type == GuildCraftType.COMPOSE_COLOR ) {
				
			} else if ( type == GuildCraftType.COMPOSE_EQUIP ) {
				
			} else if ( type == GuildCraftType.COMPOSE_EQUIP_WITH_STONE ) {
				
			} else if ( type == GuildCraftType.COMPOSE_STONE ) {
				
			} else if ( type == GuildCraftType.COMPOSE_STRENGTH ) {
				/**
				 * 首先检查公会铁匠铺的等级
				 */
				double[] ratios = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_CRAFT_STRENGTH);
				GuildFacility fac = guild.getFacility(GuildFacilityType.craft);
				if ( fac != null ) {
					int index = guild.getFacility(GuildFacilityType.craft).getLevel();
					if ( index>=ratios.length ) {
						index = ratios.length-1;
					}
					/**
					 * 其次检验玩家贡献度等级
					 */
					ScriptResult checkResult = UserCreditCheck.func(new Object[]{user, fac});
					if ( checkResult.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
						 List list = checkResult.getResult();
						 boolean success = (Boolean)list.get(0);
						 int craftLevel = (Integer)list.get(2);
						 if ( success ) {
							 index = craftLevel;
						 } else {
							 index = -1;
						 }
					}
					if ( index >= 0 ) {
						ratio = ratios[index];
					} else {
						ratio = 0;
					}
				}				
			} else if ( type == GuildCraftType.COMPOSE_TRANSFER ) {
				
			}
		}
		ArrayList list = new ArrayList();
		list.add(ratio);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
