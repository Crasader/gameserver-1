package script;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Check the user's battle performance and give user a remark.
 * 
 * @author wangqi
 *
 */
public class BattleRemark {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleRemark.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 4);
		if ( result != null ) {
			return result;
		}
		Battle battle = (Battle)parameters[0];
		BattleUser battleUser = (BattleUser)parameters[1];
		int round = (Integer)parameters[2];
		Long roundMillis = (Long)parameters[3];
		int seconds = (int)(roundMillis.longValue()/1000);
		String percent = "51%";
		if ( seconds < 10 ) {
			percent = "95%";
		} else if ( seconds < 20 ) {
			percent = "80%";
		} else if ( seconds < 40 ) {
			percent = "70%";	
		} else if ( seconds < 60 ) {
			percent = "60%";
		}
		final User user = battleUser.getUser();
		if ( user.getBattleCount() == 1 ) {
			//New user's first combat
			final String text = Text.text("remark.firstbattle", new Object[]{seconds, percent, 
					DateUtil.formatDateTime(new Date())});
			GameContext.getInstance().scheduleTask(new Runnable() {
				public void run() {
					SysMessageManager.getInstance().sendClientInfoWeiboMessage(user.getSessionKey(), 
							text, text, Type.WEIBO);
				}
			}, 5, TimeUnit.SECONDS);
		}

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
