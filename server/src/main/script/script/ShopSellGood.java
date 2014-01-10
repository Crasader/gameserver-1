package script;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;

/**
 * When an user wants to sell his good to Shop, this script will
 * calculate the final golden price.
 * 
 * @author wangqi
 *
 */
public class ShopSellGood {

	private static final Logger logger = LoggerFactory.getLogger(ShopSellGood.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if (result != null) {
			return result;
		}
		
		User user = (User)parameters[0];
		PropData propData = (PropData)parameters[1];
		ShopPojo goldenPojo = (ShopPojo)parameters[2];
		ShopPojo defaultPojo = (ShopPojo)parameters[3];

		List prices = null;

		BuyPrice simple = new BuyPrice();
		int simpleRatio = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.SHOP_PRICE_SIMPLE_RATIO, 1);
		int yuanbaoToGolden = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.YUANBAO_TO_GOLDEN_RATIO, 100);
		double yuanbaoUnit = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.SHOP_DPR_TO_YUANBAO, 0.006667);
		simple.validTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SIMPLE, 30);
		simple.price = (int)Math.round(propData.getPower() * simpleRatio * yuanbaoUnit * yuanbaoToGolden);
		
		/*
		int unitPrice = ShopManager.getInstance().
				findPriceForPropData(user, propData, MoneyType.GOLDEN, null, null, true);
		*/
		double ratio = (propData.getPropIndate() - propData.getPropUsedTime()) * 1.0 /propData.getPropIndate();
		//int finalPrice = (int)(0.5 * ratio * unitPrice);
		int finalPrice = (int)(0.5 * ratio * simple.price);
		if ( finalPrice <= 0 ) {
			finalPrice = 100;
		}

		ArrayList list = new ArrayList();
		list.add(finalPrice);

		result = new ScriptResult();
		result.setResult(list);
		result.setType(Type.SUCCESS_RETURN);

		return result;
	}
}
