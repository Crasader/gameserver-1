package script.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.VipPojo;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 本类根据玩家累计充值的金额，给予相应的VIP等级，并设置不同等级的福利
 * 
 * @author wangqi
 *
 */
public class VipLevelQuery {
	
	private static final Logger logger = LoggerFactory.getLogger(VipLevelQuery.class);

	//TODO
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
		int chargedYuanbao = user.getChargedYuanbao();
		Collection vipPojos = VipManager.getInstance().getVipPojos();
		VipPojo vip = VipManager.getInstance().getVipPojoByYuanbao(chargedYuanbao);
		
		HashMap map = new HashMap();
		for (Iterator iterator = vipPojos.iterator(); iterator.hasNext();) {
			VipPojo vipPojo = (VipPojo) iterator.next();
			int diff = (vipPojo.getYuanbaoPrice() - chargedYuanbao)/10;
			if ( diff <= 0 ) {
				map.put(String.valueOf(vipPojo.getId()), String.valueOf(0));
			} else {
				map.put(String.valueOf(vipPojo.getId()), String.valueOf(diff));
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(map);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
