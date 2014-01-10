package script;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.StoneType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmCallback;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.forge.CraftManager;
import com.xinqihd.sns.gameserver.forge.ForgeStatus;
import com.xinqihd.sns.gameserver.forge.TransferStatus;
import com.xinqihd.sns.gameserver.proto.XinqiBseTransfer.BseTransfer;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user want to compose lower level items to higher item.
 * 
 */
public class CraftTransferEquip {
	
	private static final Logger logger = LoggerFactory.getLogger(CraftTransferEquip.class);

	/**
	 * 转移的说明： 1. 普通用户只能转移同级别和颜色的物品 2. VIP用户可以花费元宝转换不同等级和颜色的物品
	 * 
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if (result != null) {
			return result;
		}

		TransferStatus status = TransferStatus.SUCCESS;
		User user = (User) parameters[0];
		Object[] array = (Object[]) parameters[1];
		final PropData srcPropData = (PropData) array[0];
		final PropData targetPropData = (PropData) array[1];
		int price = (Integer)array[2];
		
		try {
			// Check weapon's level first
			boolean crossLevelTransfer = false;
			boolean crossColorTransfer = false;
			int maxWeaponLevel = 0;
			WeaponColor maxWeaponColor = WeaponColor.WHITE;
			
			if (srcPropData.getName().contains("●") && targetPropData.getName().contains("●")) {
				// Only the upgrade of weapons will let the users to pay
				if (srcPropData.getUserLevel() != targetPropData.getUserLevel()) {
					crossLevelTransfer = true;
					maxWeaponLevel = Math.max(srcPropData.getUserLevel(), targetPropData.getUserLevel());
				}
				if ( srcPropData.getWeaponColor().ordinal() != targetPropData.getWeaponColor().ordinal() ) {
					crossColorTransfer = true;
					if ( srcPropData.getWeaponColor().ordinal() > targetPropData.getWeaponColor().ordinal() ) {
						maxWeaponColor = srcPropData.getWeaponColor();
					} else {
						maxWeaponColor = targetPropData.getWeaponColor();
					}
				}
			}
			
			boolean canDoIt = true;
			if ( crossLevelTransfer || crossColorTransfer ) {
				//Check VIP function
				if ( crossLevelTransfer ) {
					canDoIt = VipManager.getInstance().getVipLevelCanTransferCrossLevel(user, maxWeaponLevel);
					if ( !canDoIt ) {
						String weaponLevelName = Text.text("weapon.level.".concat(String.valueOf(maxWeaponLevel)));
						String message = Text.text("craft.transfer.notlevel", weaponLevelName);
						SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(),
								message, 8000);
					}
				}
				if ( crossColorTransfer ) {
					canDoIt = VipManager.getInstance().getVipLevelCanTransferCrossColor(user, maxWeaponColor);
					if ( !canDoIt ) {
						String message = Text.text("craft.transfer.notcolor", maxWeaponColor.getTitle());
						SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(),
								message, 8000);
					}
				}
			}
			
			if ( canDoIt ) {
				boolean payedIt = ShopManager.getInstance().payForSomething(user,
						MoneyType.GOLDEN, price, 1, null, true);
				if ( payedIt ) {
					StatClient.getIntance().sendDataToStatServer(user,
							StatAction.ConsumeTransfer, 
							new Object[]{MoneyType.GOLDEN,
							price, srcPropData.getName(),
							targetPropData.getName(), srcPropData.getLevel()});

					doTransfer(user, srcPropData, targetPropData, price);
				} else {
					BseTransfer.Builder builder = BseTransfer.newBuilder();
					builder.setResult(TransferStatus.FAILURE.ordinal());
					builder.setSrcEquip(srcPropData.toXinqiPropData(user));
					builder.setTarEquip(targetPropData.toXinqiPropData(user));
					GameContext.getInstance().writeResponse(
							user.getSessionKey(), builder.build());
				}
			} else {
				BseTransfer.Builder builder = BseTransfer.newBuilder();
				builder.setResult(TransferStatus.FAILURE.ordinal());
				builder.setSrcEquip(srcPropData.toXinqiPropData(user));
				builder.setTarEquip(targetPropData.toXinqiPropData(user));
				GameContext.getInstance().writeResponse(
						user.getSessionKey(), builder.build());
			}

		} catch (Exception e) {
			logger.warn("Failed to transfter", e.getMessage());
			status = TransferStatus.FAILURE;
		}

		ArrayList list = new ArrayList();
		list.add(status);
		list.add(targetPropData);

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

	/**
	 * @param user
	 * @param srcEquipPew
	 * @param tarEquipPew
	 * @param status
	 * @param bag
	 * @param srcData
	 * @param targetData
	 */
	public static final void doTransfer(User user, PropData srcData, PropData targetData, int price) {
		int srcEquipPew = srcData.getPew();
		int tarEquipPew = targetData.getPew();
		TransferStatus status = TransferStatus.SUCCESS; 
		Bag bag = user.getBag();
		
		BseTransfer.Builder builder = BseTransfer.newBuilder();
		builder.setResult(status.ordinal());
		if ( status == TransferStatus.SUCCESS ) {
			int srcLevel = srcData.getLevel();
			int tarLevel = targetData.getLevel();
			if ( srcLevel != tarLevel ) {
				if ( srcLevel > tarLevel ) {
					targetData.setTotalGolden(targetData.getTotalGolden()+price);
				} else {
					srcData.setTotalGolden(srcData.getTotalGolden()+price);
				}
				
				ScriptManager.getInstance().runScript(ScriptHook.WEAPON_LEVEL_UPGRADE, 
						new Object[]{srcData, tarLevel});
				ScriptManager.getInstance().runScript(ScriptHook.WEAPON_LEVEL_UPGRADE, 
						new Object[]{targetData, srcLevel});
				bag.markChangeFlag(srcEquipPew);
				bag.markChangeFlag(tarEquipPew);
				GameContext.getInstance().getUserManager().saveUserBag(user, false);
			}
			
			builder.setSrcEquip(srcData.toXinqiPropData(user));
			builder.setTarEquip(targetData.toXinqiPropData(user));
		}
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.Transfer, new Object[]{srcData.getName(), targetData.getName(), srcData.getLevel(), targetData.getLevel()});
	}
}
