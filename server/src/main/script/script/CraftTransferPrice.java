package script;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildCraftType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * The user want to compose lower level items to higher item.
 * 
 */
public class CraftTransferPrice {
	
	private static final Logger logger = LoggerFactory.getLogger(CraftTransferPrice.class);

	/**
	 * 转移的说明： 1. 普通用户只能转移同级别和颜色的物品 
	 * 2. VIP用户可以花费元宝转换不同等级和颜色的物品
	 * 
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if (result != null) {
			return result;
		}

		User user = (User) parameters[0];
		Object[] array = (Object[]) parameters[1];
		final PropData srcPropData = (PropData) array[0];
		final PropData targetPropData = (PropData) array[1];
		int price = 0;
		double ratio = 1.0;
		double guildAddRatio = 0.0;
		
		ratio = transferRatio(user, srcPropData, targetPropData);
		price = transferPrice(user, srcPropData, targetPropData);
		guildAddRatio = (Double)ScriptManager.getInstance().runScriptForObject(
				ScriptHook.GUILD_CRAFT_ADDRATIO,  new Object[]{user, 
				GuildCraftType.COMPOSE_TRANSFER});
		
		ArrayList list = new ArrayList();
		list.add(price);
		list.add(ratio);
		list.add(guildAddRatio);

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
	/**
	 * Calculate the transfer ratio.
	 * @param user
	 * @param srcPropData
	 * @param targetPropData
	 * @return
	 */
	public static double transferRatio(User user, final PropData srcPropData,
			final PropData targetPropData) {
		int maxStrLevel = Math.max(srcPropData.getLevel(), targetPropData.getLevel());
		double[][] ratioTable = GameDataManager.getInstance().
				getGameDataAsDoubleArrayArray(GameDataKey.CRAFT_TRANSFER_RATIO);
		int levelIndex = maxStrLevel - 1;
		if ( levelIndex < 0 ) levelIndex = 0;
		if ( levelIndex>ratioTable.length ) levelIndex = ratioTable.length-1;
		double[] ratios = ratioTable[levelIndex];
		int vipIndex = 0;
		double ratio = 0.0;
		if ( user.isVip() ) {
			vipIndex = user.getViplevel();
			if ( vipIndex > ratios.length ) vipIndex = ratios.length;
			ratio = ratios[vipIndex];
		} else {
			ratio = ratios[0];
		}
		/**
		 * Check the weaponColor
		 */
		int colorIndex = Math.max(srcPropData.getWeaponColor().ordinal(), 
				targetPropData.getWeaponColor().ordinal());
		if ( colorIndex >= WeaponColor.PINK.ordinal() ) {
			ratio /= colorIndex;
		}
		return ratio;
	}

	/**
	 * @param user
	 * @param srcPropData
	 * @param targetPropData
	 * @return
	 */
	public static final int transferPrice(User user, final PropData srcPropData,
			final PropData targetPropData) {
		int price;
		int targetGoldenPrice = ShopManager.getInstance().
				findPriceForPropData(user, targetPropData, MoneyType.GOLDEN, null, null, false);
		int srcGoldenPrice = ShopManager.getInstance().
				findPriceForPropData(user, srcPropData, MoneyType.GOLDEN, null, null, false);
		/*
		int targetLevel = targetPropData.getLevel();
		int srcLevel = srcPropData.getLevel();
		int maxLevel = Math.max(targetLevel, srcLevel)+1;
		int goldenPrice = Math.round(Math.max(targetGoldenPrice, srcGoldenPrice) 
				* maxLevel * maxLevel * 0.05f);
		*/
		int goldenPrice = Math.round( (targetGoldenPrice+srcGoldenPrice)*0.3f );
		if ( goldenPrice < 20 ) goldenPrice = 20;
		price = goldenPrice;

		try {
			//用户等级差
			int userLevelDiff = Math.abs(targetPropData.getUserLevel() - srcPropData.getUserLevel())/10;
			int extraPrice = Math.round( goldenPrice  * userLevelDiff * userLevelDiff * 0.3f);
			if (extraPrice <= 0) {
				price += 10;
			} else {
				price += extraPrice;
			}
			//强化等级差
			if (srcPropData.getLevel() != targetPropData.getLevel()) {
				int levelDiff = Math.abs(targetPropData.getLevel() - srcPropData.getLevel());
				extraPrice = Math.round(goldenPrice * levelDiff * levelDiff * 0.3f);
				if (extraPrice <= 0) {
					price += 10;
				} else {
					price += extraPrice;
				}
			}
			if ( srcPropData.getWeaponColor().ordinal() != targetPropData.getWeaponColor().ordinal() ) {
				int colorDiff = Math.abs(targetPropData.getWeaponColor().ordinal() - srcPropData.getWeaponColor().ordinal());
				if ( colorDiff < 0 ) colorDiff = 0;
				price +=  (int)Math.round(goldenPrice * colorDiff * colorDiff * 0.2f);
			}
			/*
			StatClient.getIntance().sendDataToStatServer(user,
					StatAction.ConsumeTransfer, new Object[]{MoneyType.GOLDEN, price,
					srcPropData.getName(), targetPropData.getName(), srcPropData.getLevel()});
			*/
		} catch (Exception e) {
			logger.warn("Failed to transfter", e.getMessage());
		}
		return price;
	}

}
