package script.guild;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 
 * 3.	当公会财富值连续1周不够维护费扣取时，公会自动解散并在所有成员上线
 * 在线时候弹出提示：您的公会xxxx由于财富值过低，公会自动解散！
 * 
 * 解散前3天每次登陆时给予即将解散的提示
 * 
 * @author wangqi
 *
 */
public class OperationFeeCheck {
	
	private static final int OPDAYS = 7;

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		Guild guild = (Guild)parameters[1];
		int level = guild.getLevel();
		
		Boolean success = Boolean.TRUE;
		ScriptResult opFeeResult = GuildOpFee.func(parameters);
		int fee = (Integer)opFeeResult.getResult().get(0);
		Date currentDate = new Date();
		int dayDiff = (int)((currentDate.getTime() - guild.getLastchargetime())/86400000.0f);
		int left = guild.getWealth() - fee;
		if ( dayDiff > OPDAYS ) {
			/**
			 * 公会需要扣除周维持费用
			 */
			if ( left > 0 ) {
				guild.setWealth(left);
				String subject = Text.text("guild.opfee.payed.subject");
				String content = Text.text("guild.opfee.payed.content", new Object[]{
						DateUtil.formatDate(new Date(guild.getLastchargetime())),
						DateUtil.formatDate(currentDate),
						fee, left
				});
				guild.setLastchargetime(System.currentTimeMillis());
				GuildManager.getInstance().saveGuild(guild);
				MailMessageManager.getInstance().sendAdminMail(guild.getUserId(), subject, content, null);
			} else {
				String subject = Text.text("guild.opfee.unpayed.subject");
				String content = Text.text("guild.opfee.unpayed.content", new Object[]{
						DateUtil.formatDate(new Date(guild.getLastchargetime())),
						DateUtil.formatDate(currentDate),
						fee, left
				});
				MailMessageManager.getInstance().sendAdminMail(guild.getUserId(), subject, content, null);
				success = Boolean.FALSE;
			}
			StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildPayOpFee, 
					new Object[]{level, fee, left});
		} else {
			if ( dayDiff >= 5 &&  left < 0 ) {
				String key = "guildopfee:".concat(guild.get_id());
				Jedis jedis = JedisFactory.getJedis();
				String value = jedis.get(key);
				String dayDiffStr = String.valueOf(dayDiff);
				if ( !dayDiffStr.equals(value) ) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(guild.getLastchargetime());
					//间隔7天，再第8天扣除公会费用
					cal.add(Calendar.DAY_OF_MONTH, 8);
					String subject = Text.text("guild.opfee.notify.subject");
					String dateStr = DateUtil.formatDate(cal.getTime());
					String content = Text.text("guild.opfee.notify.content", new Object[]{
							dateStr,
							fee, guild.getWealth(), dateStr,
					});
					GuildManager.getInstance().sendMessageToUser(user.get_id(), subject, content);
					jedis.set(key, dayDiffStr);
					jedis.expire(key, 7200);
				}
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(success);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
