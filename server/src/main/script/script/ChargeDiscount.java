package script;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;

/**
 * 为自由充值计算元宝的折扣
 * 充值元宝折扣表：
		10 RMB =100
		20 RMB= 210
		30 RMB= 330
		50 RMB= 580
		100 RMB=1160
		300 RMB= 3750
		500 RMB= 6500
		1000 RMB=15000
 * 
 * @author wangqi
 *
 */
public class ChargeDiscount {

	private static final Logger logger = LoggerFactory.getLogger(ChargeDiscount.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if (result != null) {
			return result;
		}
		
		User user = (User)parameters[0];
		float rmb = (Float)parameters[1];
		float discount = 10.0f;
		if ( rmb < 20 ) {
			discount = 10.0f;
		} else if ( rmb <= 20 ) {
			discount = 9.4f;
		} else if ( rmb <= 30 ) {
			discount = 9.0f;
		} else if ( rmb <= 50 ) {
			discount = 8.5f;
		} else if ( rmb <= 100 ) {
			discount = 8.4f;
		} else if ( rmb <= 300 ) {
			discount = 7.5f;
		} else if ( rmb <= 400 ) {
			discount = 7.2f;
		} else if ( rmb <= 500 ) {
			discount = 7.0f;
		} else if ( rmb <= 1000 ) {
			discount = 5.0f;
		}
		int finalYuanbao = Math.round(rmb * 10 * ( 1+1-discount*0.1f));
		/**
		10 RMB =100
		20 RMB= 210
		30 RMB= 330
		50 RMB= 580
		100 RMB=1160
		300 RMB= 3750
		500 RMB= 6500
		1000 RMB=15000
		 */
		if ( rmb == 20 && finalYuanbao < 210 ) {
			finalYuanbao = 210;
		} else if ( rmb == 30 && finalYuanbao < 330 ) {
			finalYuanbao = 330;
		} else if ( rmb == 50 && finalYuanbao < 580) {
			finalYuanbao = 580;
		} else if ( rmb == 100 && finalYuanbao < 1160 ) {
			finalYuanbao = 1160;
		} else if ( rmb == 300 && finalYuanbao < 3750) {
			finalYuanbao = 3750;
		} else if ( rmb == 500 && finalYuanbao < 6500) {
			finalYuanbao = 6500;
		} else if ( rmb == 1000 && finalYuanbao < 15000) {
			finalYuanbao = 15000;
		}
		
		ArrayList list = new ArrayList();
		list.add(finalYuanbao);

		result = new ScriptResult();
		result.setResult(list);
		result.setType(Type.SUCCESS_RETURN);

		return result;
	}
}
