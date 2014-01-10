package com.xinqihd.sns.gameserver.chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Chat Sender is responsible for sending a chat message to users.
 * It will read the messages from database and send them out.
 * 
 * message BseChat {
 *  //消息类型   0:当前 1:私聊 2:工会 3:小喇叭 4:大喇叭 5:小队
 *  required int32 msgType = 1;
 *  //消息内容
    required string msgContent = 2;
    //消息的发送者
    optional string usrId = 3;
    //消息发送者的昵称
    optional string usrNickname = 4;
   }
 * 
 * @author wangqi
 *
 */
public class ChatSender {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatSender.class);
	
	private ExecutorService service = null;;
	
	private Worker[] workers = null;
	
	private ExecutorService fakeService = null;
	
	private FakeSender sender = null;
	
	private static ChatSender instance = new ChatSender();
	
	/**
	 * 
	 */
	ChatSender() {
		int numWorkers = 1; //Constant.CPU_CORES;
		if ( numWorkers < 1 ) {
			numWorkers = 1;
		}
		service = Executors.newFixedThreadPool(numWorkers);
		workers = new Worker[numWorkers];
		if ( logger.isDebugEnabled() ) {
			logger.debug("Create ChatSender with #{} workers.", workers.length );
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Worker();
			service.submit(workers[i]);
		}
		
		if ( Boolean.getBoolean("usefakesender") ) {
			fakeService = Executors.newFixedThreadPool(1);
			sender = new FakeSender();
			fakeService.submit(sender);
		}
	}
	
	/**
	 * Get the ChatSender instance
	 * @return
	 */
	public static final ChatSender getInstance() {
		return instance;
	}
	
	/**
	 * Stop all internal workers
	 */
	public void stopWorkers() {
		if ( logger.isDebugEnabled() ) {
			logger.debug("Stop chat workers");
		}
		for (int i = 0; i < workers.length; i++) {
			if ( workers[i] != null ) {
				workers[i].stopWorker();
			}
		}
		service.shutdown();
		
		if ( sender != null ) {
			sender.stopIt();
			sender = null;
			fakeService.shutdown();
			fakeService = null;
		}
	}
	
	/**
	 * Send a system global wide message to all online users.
	 * 
	 * @param message
	 */
	public void sendSystemMessage(String message) {
		Jedis jedis = JedisFactory.getJedis();
		jedis.rpush(ChatType.ChatSystem.name(), message);
	}
	
	/**
	 * Internal worker for the real jobs.
	 * @author wangqi
	 *
	 */
	private static class Worker implements Runnable {
		
		private volatile boolean isStop = false;
		
		public Worker() {
		}
		
		/**
		 * Stop the worker.
		 */
		public void stopWorker() {
			isStop = true;
		}
		
		public void run() {
			while ( !isStop ) {
				Jedis jedis = JedisFactory.getJedis();
				List<String> messages = jedis.blpop(Integer.MAX_VALUE, ChatType.allTypes());
				if ( messages != null && messages.size() > 1 ) {
					Stat.getInstance().chatReceived++;
					String channel = messages.get(0);
					ChatType chatType = ChatType.valueOf(channel);
					String message = messages.get(1);
					if ( logger.isDebugEnabled() ) {
						logger.debug("Worker: channel[{}], chatType[{}], message[{}]", new Object[]{channel, chatType, message});
					}
					switch ( chatType ) {
						//The system admin can send a global messages.
						case ChatSystem:
						//The global world messages.
						case ChatWorld:
							//Get all online users
							//Send them the message.
							List<SessionKey> allOnlineUsers = GameContext.getInstance().findAllOnlineUsers();
							if ( allOnlineUsers != null ) {
								String gameAdminName = Text.text("gameadmin");
								sendBseChatToSessions(chatType, message, allOnlineUsers, gameAdminName);
							}
							break;
						//The hall messages.
						case ChatCurrent:
							break;
						//The virtual server messages.
						case ChatServer:
							break;
						//The guild messages.
						case ChatGuild:
							break;
						//The room messages.
						case ChatRoom:
							break;
						//The private to private messages.
						case ChatPrivate:
							break;
						default:
							break;
					}
				}
			}
		} //run...
	} //Worker...
	
	/**
	 * It is used to generate 'fake' system messages.
	 */
	private static class FakeSender implements Runnable {
		static int index = 0;
		volatile boolean isStop = false;
		User fakeUser = new User();
		String[] boxes = {"粽子宝箱", "超值大宝箱", "织女宝箱", "狮子座宝箱", "阿波罗神罐"};
		ArrayList<TaskPojo> tasks = new ArrayList<TaskPojo>(
				TaskManager.getInstance().getTasksForType(TaskType.TASK_ACHIVEMENT));
		
		public FakeSender() {
			fakeUser.setRoleName("公告");
		}
		
		public void run() {
			while ( !isStop ) {
				try {
					String userName = AIManager.getRandomAIName();
					/*
					{"notice.open_box", "[公告]：玩家'{}'人品大爆发，从{}中开出了{}"},
					{"notice.get_war_item", "[公告]：玩家'{}'通过战斗获得强化石Lv{}"},
					//玩家'{}'在进行水晶球占卜时，幸运的获得史诗品质器灵镜盾
					{"notice.zhanpu", "[公告]：玩家'{}'在进行水晶球占卜时，幸运的获得{}"},
					//恭喜玩家[某某]成功熔炼出幸运手镯+4
					{"notice.melt_item", "[公告]：恭喜玩家'{}'成功熔炼出幸运手镯+{}！"},
					//玩家'{}'成功将泡泡手雷强化到9级，战斗力又上了一个新台阶
					{"notice.strength", "[公告]：玩家'{}'成功将{}强化到{}级，战斗力又上了一个新台阶。"},
					{"notice.ranking.total.power",   "[消息]:玩家'{}'的战斗力全球总排名上升了{}位，现排名第{}"},
					{"notice.ranking.total.level",   "[消息]:玩家'{}'的等级全球总排名上升了{}位，现排名第{}"},
					{"notice.ranking.total.yuanbao", "[消息]:玩家'{}'的元宝全球总排名上升了{}位，现排名第{}"},
					{"notice.ranking.total.medal",   "[消息]:玩家'{}'的勋章全球总排名上升了{}位，现排名第{}"},
					{"notice.openItemBox", "玩家'{}'人品爆发，获得了{}{}枚"},
					{"notice.openEquipBox", "玩家'{}'人品爆发，获得了强{}装备'{}'"},
					{"notice.achievement", "热烈庆祝玩家'{}'解锁了'{}'成就"},
					{"notice.welcome", "#ff0000热烈欢迎玩家'{}'加入游戏大家庭，鼓爪!"},
					 */
					int index = (int)(MathUtil.nextDouble() * 3);
					int level = (int)(MathUtil.nextDouble() * 50);
					fakeUser.setLevelSimple(level);
					String message = null;
					switch ( index ) {
						case 0:
							WeaponPojo weapon = EquipManager.getInstance().getRandomWeaponByGenderAndLevel(Gender.MALE, fakeUser);
							if ( weapon != null ) {
								message = Text.text("notice.open_box", userName, boxes[index%boxes.length], weapon.getName());
							}
							break;
						case 1:
							weapon = EquipManager.getInstance().getRandomWeaponByGenderAndLevel(Gender.MALE, fakeUser);
							if ( weapon != null ) {
								int strength = (int)Math.round(MathUtil.nextDouble() * 10);
								message = Text.text("notice.strength", userName, weapon.getName(), strength);
							}
							break;
						case 2:
							message = Text.text("notice.achievement", userName, tasks.get(level%tasks.size()).getName());
							break;
					}
					if ( message != null ) {
						List<SessionKey> allOnlineUsers = GameContext.getInstance().findAllOnlineUsers();
						if ( allOnlineUsers != null ) {
							logger.debug("#chatWorld: {}", message);
							sendBseChatToSessions(ChatType.ChatWorld, message, allOnlineUsers, fakeUser.getRoleName());
						}
					}
					Thread.sleep(10000);
				} catch (Throwable e) {
					if ( logger.isDebugEnabled() ) {
						logger.error(e.toString(), e);
					}
				}
			}
		}
		
		public void stopIt() {
			isStop = true;
		}
	}
	
	/**
	 * Create a BseChat.
	 * @param chatType
	 * @param message
	 * @return
	 */
	private static void sendBseChatToSessions(ChatType chatType, 
			String message, List<SessionKey> sessions, String nickName) {
		
		BseChat.Builder chatBuilder = BseChat.newBuilder();
		chatBuilder.setMsgType(chatType.ordinal());
		chatBuilder.setMsgContent(message);
		chatBuilder.setUsrNickname(nickName);
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = chatBuilder.build();
		Jedis jedis = JedisFactory.getJedis();
		for (Iterator iter = sessions.iterator(); iter.hasNext();) {
			SessionKey userKey = (SessionKey) iter.next();
			String aiBytes = jedis.hget(userKey.toString(), SessionManager.H_ISAI);
			if ( SessionManager.V_TRUE.equals(aiBytes)) {
				//ignore ai users.
			} else {
				GameContext.getInstance().writeResponse(userKey, xinqi);
			}
		}
	}
}
