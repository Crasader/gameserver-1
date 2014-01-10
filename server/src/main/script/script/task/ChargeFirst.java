package script.task;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 针对首次充值开展的活动
 * 
 * @author wangqi
 *
 */
public class ChargeFirst {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.CHARGE ) {
			//Check if the user charge money for his first time.
			int chargeCount = user.getChargeCount();
			if ( chargeCount == 1 ) {
				TaskStep.step(task, user);
				
				Object[] array = (Object[])parameters[3];
				int yuanbao = (Integer)array[0];
				if ( yuanbao > 0 ) {
					Reward gift = RewardManager.getInstance().getRewardYuanbao(yuanbao);
					String subject = Text.text("charge.first.subject");
					String content = Text.text("charge.first.content", new Object[]{yuanbao, yuanbao});
					ArrayList gifts = new ArrayList();
					gifts.add(gift);
					MailMessageManager.getInstance().sendMail(null, user.get_id(), subject, content, gifts, true);
					
					TaskManager.getInstance().takeTaskReward(user, task.getId(), 0);
				}
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
