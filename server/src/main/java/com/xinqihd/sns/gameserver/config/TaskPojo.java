package com.xinqihd.sns.gameserver.config;

import static com.xinqihd.sns.gameserver.config.TaskType.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.mongo.GameResourceManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.Field;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBsePromotion.Promotion;
import com.xinqihd.sns.gameserver.proto.XinqiBseTask;
import com.xinqihd.sns.gameserver.proto.XinqiGift.Gift;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class TaskPojo implements Pojo, Comparable<TaskPojo> {

	private static final long serialVersionUID = -7821132571167647471L;

	//The item id
	private String _id;

	// --------------------------------------- Properties 
	private String name = null; 
	private String desc = null;
	private String taskTarget = null;
	/**
	 * The 'step' is the number of times that given condition should be fufilled.
	 * Exampe: taskTarget="获得20场竞技战斗胜利" step="20" level="16"
	 */
	private int step = 0;
	/**
	 * It's usually used to represents the level condition.
	 */
	private int level = 0;
	/**
	 * 1: Main thread tasks.
	 * 2: Sub thread tasks.
	 * 3: Daily tasks.
	 * 4: Activity tasks.
	 */
	private TaskType type = TASK_MAIN;
	private int exp = 0;
	private int gold = 0;
	private int ticket = 0; 
	private int gongxun = 0; 
	private int caifu = 0;
	//The task sequence number.
	private int seq = 0;
	//The maximun user's level
	private int userLevel = 0;
	//The minumun user's level
	private int minUserLevel = 0;
	//A task can bind a customized script
	private String script = Constant.EMPTY;
	//After the task is taken, if the  
	//postScript is not null, it should be 
	//executed.
	private String postScript = null;
	/**
	 * If this script is not null,
	 * the taskmanager will run this script
	 * before give users task rewards.
	 * If the returned status is FINISHED,
	 * then the task reward will not given,
	 */
	private String postCheckScript = null;
	//When the task is finished, system will 
	//broadcast to all users.
	private boolean broadcast = false;
	/**
	 * 活动开始的日期和截止的日期，如果没有到开始日期
	 * 客户端应该显示一个倒计时的天数，小时和秒数，
	 * 超过截止日期显示已经截止.
	 * 值为0表示不判断
	 */
	private long startMillis = 0l;
	private long endMillis = 0l;
	/**
	 * The task can be associated with 
	 * a game function, like treasure hunt,
	 * top list, or craft. Use the 
	 * funcId to allow open the given
	 * UI directly
	 */
	private TaskFuncId funcId = null;
	/**
	 * 领取任务奖励的时间点，超过时间范围
	 * 不可领取。如果当前时间未达到，
	 * 客户端可以显示：小时：分钟 的倒计时.
	 * 值为0表示不判断
	 */
	private int takeBeginHour = -1;
	private int takeBeginMin = 0;
	private int takeEndHour = -1;
	private int takeEndMin = 0;

	/**
	 * 对于活动任务，如果isDaily为true,
	 * 那么每天会刷新任务的进度。其他任务
	 * 只有DAILY类型的任务会刷新进度。
	 */
	private boolean isDaily = false;
	
	/**
	 * 如果任务和渠道相关，就需要设置渠道
	 * 的匹配字符，比如xiaomi或者uc等
	 */
	private String channel = null;
	
	/**
	 * 输入激活码领取的活动类型
	 */
	private boolean inputCode = false;
	
	/**
	 * 激活码类型 
	 */
	private String inputKey = null;
	
	/**
	 * 
	 */
	private final List<Award> awards = new ArrayList<Award>(2);

	/**
	 * The description for tasks that do not have
	 * real awards. 
	 */
	private String giftDesc = null;
	
	/**
	 * 是否禁用一个任务
	 */
	private boolean isDisable = false;
	
	/**
	 * 是否热门
	 */
	private boolean isHot = false;
	
	/**
	 * 如果不为空，指定这个活动只出现在
	 * 指定的服务器
	 */
	private String serverId = null;
	
	/**
	 * The guild's wealth
	 */
	private int guildWealth = 0;
	
	/**
	 * The guild member's credit
	 */
	private int guildCredit = 0;


	@Override
	public void setId(String id) {
		this._id = id;
	}

	@Override
	public String getId() {
		return _id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @return the taskTarget
	 */
	public String getTaskTarget() {
		return taskTarget;
	}

	/**
	 * @param taskTarget the taskTarget to set
	 */
	public void setTaskTarget(String taskTarget) {
		this.taskTarget = taskTarget;
	}

	/**
	 * @return the step
	 */
	public int getStep() {
		return step;
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(int step) {
		this.step = step;
	}

	/**
	 * @return the level
	 */
	public int getCondition1() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setCondition1(int level) {
		this.level = level;
	}

	/**
	 * @return the parent
	 */
	public TaskType getType() {
		return type;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setType(TaskType parent) {
		this.type = parent;
	}

	/**
	 * @return the exp
	 */
	public int getExp() {
		return exp;
	}

	/**
	 * @param exp the exp to set
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}

	/**
	 * @return the gold
	 */
	public int getGold() {
		return gold;
	}

	/**
	 * @param gold the gold to set
	 */
	public void setGold(int gold) {
		this.gold = gold;
	}

	/**
	 * @return the ticket
	 */
	public int getTicket() {
		return ticket;
	}

	/**
	 * @param ticket the ticket to set
	 */
	public void setTicket(int ticket) {
		this.ticket = ticket;
	}

	/**
	 * @return the gongxun
	 */
	public int getGongxun() {
		return gongxun;
	}

	/**
	 * @param gongxun the gongxun to set
	 */
	public void setGongxun(int gongxun) {
		this.gongxun = gongxun;
	}

	/**
	 * @return the caifu
	 */
	public int getCaifu() {
		return caifu;
	}

	/**
	 * @param caifu the caifu to set
	 */
	public void setCaifu(int caifu) {
		this.caifu = caifu;
	}
	
	/**
	 * @return the awards
	 */
	public List<Award> getAwards() {
		return awards;
	}

	/**
	 * @param awards the awards to set
	 */
	public void addAward(Award award) {
		this.awards.add(award);
	}

	/**
	 * @return the seq
	 */
	public int getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(int seq) {
		this.seq = seq;
	}

	/**
	 * @return the userLevel
	 */
	public int getUserLevel() {
		return userLevel;
	}

	/**
	 * @param userLevel the userLevel to set
	 */
	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	/**
	 * @return the minUserLevel
	 */
	public int getMinUserLevel() {
		return minUserLevel;
	}

	/**
	 * @param minUserLevel the minUserLevel to set
	 */
	public void setMinUserLevel(int minUserLevel) {
		this.minUserLevel = minUserLevel;
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * @return the broadcast
	 */
	public boolean isBroadcast() {
		return broadcast;
	}

	/**
	 * @param broadcast the broadcast to set
	 */
	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}

	/**
	 * @return the startMillis
	 */
	public long getStartMillis() {
		return startMillis;
	}

	/**
	 * @param startMillis the startMillis to set
	 */
	public void setStartMillis(long startMillis) {
		this.startMillis = startMillis;
	}

	/**
	 * @return the endMillis
	 */
	public long getEndMillis() {
		return endMillis;
	}

	/**
	 * @param endMillis the endMillis to set
	 */
	public void setEndMillis(long endMillis) {
		this.endMillis = endMillis;
	}

	/**
	 * @return the funcId
	 */
	public TaskFuncId getFuncId() {
		return funcId;
	}

	/**
	 * @param funcId the funcId to set
	 */
	public void setFuncId(TaskFuncId funcId) {
		this.funcId = funcId;
	}

	/**
	 * @return the takeBeginHour
	 */
	public int getTakeBeginHour() {
		return takeBeginHour;
	}

	/**
	 * @param takeBeginHour the takeBeginHour to set
	 */
	public void setTakeBeginHour(int takeBeginHour) {
		this.takeBeginHour = takeBeginHour;
	}

	/**
	 * @return the takeBeginMin
	 */
	public int getTakeBeginMin() {
		return takeBeginMin;
	}

	/**
	 * @param takeBeginMin the takeBeginMin to set
	 */
	public void setTakeBeginMin(int takeBeginMin) {
		this.takeBeginMin = takeBeginMin;
	}

	/**
	 * @return the takeEndHour
	 */
	public int getTakeEndHour() {
		return takeEndHour;
	}

	/**
	 * @param takeEndHour the takeEndHour to set
	 */
	public void setTakeEndHour(int takeEndHour) {
		this.takeEndHour = takeEndHour;
	}

	/**
	 * @return the takeEndMin
	 */
	public int getTakeEndMin() {
		return takeEndMin;
	}

	/**
	 * @param takeEndMin the takeEndMin to set
	 */
	public void setTakeEndMin(int takeEndMin) {
		this.takeEndMin = takeEndMin;
	}

	/**
	 * @return the isDaily
	 */
	public boolean isDaily() {
		return isDaily;
	}

	/**
	 * @param isDaily the isDaily to set
	 */
	public void setDaily(boolean isDaily) {
		this.isDaily = isDaily;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the inputCode
	 */
	public boolean isInputCode() {
		return inputCode;
	}

	/**
	 * @param inputCode the inputCode to set
	 */
	public void setInputCode(boolean inputCode) {
		this.inputCode = inputCode;
	}

	/**
	 * @return the inputKey
	 */
	public String getInputKey() {
		return inputKey;
	}

	/**
	 * @param inputKey the inputKey to set
	 */
	public void setInputKey(String inputKey) {
		this.inputKey = inputKey;
	}

	/**
	 * @return the giftDesc
	 */
	public String getGiftDesc() {
		return giftDesc;
	}

	/**
	 * @param giftDesc the giftDesc to set
	 */
	public void setGiftDesc(String giftDesc) {
		this.giftDesc = giftDesc;
	}

	/**
	 * @return the isDisable
	 */
	public boolean isDisable() {
		return isDisable;
	}

	/**
	 * @param isDisable the isDisable to set
	 */
	public void setDisable(boolean isDisable) {
		this.isDisable = isDisable;
	}

	/**
	 * @return the postScript
	 */
	public String getPostScript() {
		return postScript;
	}

	/**
	 * @param postScript the postScript to set
	 */
	public void setPostScript(String postScript) {
		this.postScript = postScript;
	}

	/**
	 * @return the postCheckScript
	 */
	public String getPostCheckScript() {
		return postCheckScript;
	}

	/**
	 * @param postCheckScript the postCheckScript to set
	 */
	public void setPostCheckScript(String postCheckScript) {
		this.postCheckScript = postCheckScript;
	}

	/**
	 * @return the serverId
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the guildWealth
	 */
	public int getGuildWealth() {
		return guildWealth;
	}

	/**
	 * @param guildWealth the guildWealth to set
	 */
	public void setGuildWealth(int guildWealth) {
		this.guildWealth = guildWealth;
	}

	/**
	 * @return the guildCredit
	 */
	public int getGuildCredit() {
		return guildCredit;
	}

	/**
	 * @param guildCredit the guildCredit to set
	 */
	public void setGuildCredit(int guildCredit) {
		this.guildCredit = guildCredit;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskPojo other = (TaskPojo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
	
	/**
	 * Convert this object to ProtoBuf's TaskData
	 * @return
	 */
	public XinqiBseTask.TaskData toTaskData(Gender gender) {
		String resName = this.name;
		String resDesc = this.desc;
		String resTaskTarget = this.taskTarget;
		if ( Constant.I18N_ENABLE ) {
			Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
			resName = GameResourceManager.getInstance().getGameResource(
					"tasks_name_".concat(_id), locale, this.name);
			resDesc = GameResourceManager.getInstance().getGameResource(
					"tasks_desc_".concat(_id), locale, this.desc);
			resTaskTarget = GameResourceManager.getInstance().getGameResource(
					"tasks_taskTarget_".concat(_id), locale, this.taskTarget);
		}
		
		XinqiBseTask.TaskData.Builder builder = XinqiBseTask.TaskData.newBuilder();
		builder.setId(_id);
		builder.setName(resName);
		builder.setDesc(resDesc);
		builder.setTaskTarget(resTaskTarget);
		builder.setStep(step);
		builder.setLevel(level);
		builder.setType(type.name());
		builder.setExp(exp);
		builder.setGold(gold);
		builder.setTicket(ticket);
		builder.setGongxun(gongxun);
		builder.setGuildwealth(guildWealth);
		builder.setGuildcredit(guildCredit);
		builder.setCaifu(caifu);
		for ( Award award : awards ) {
			if ( award.sex != null && 
					award.sex != Gender.ALL && 
					award.sex != gender ) {
				continue;
			}
			XinqiBseTask.Award.Builder pbAward = XinqiBseTask.Award.newBuilder();
			pbAward.setId(award.id);
			pbAward.setType(award.type);
			pbAward.setLv(award.lv);
			pbAward.setSex(award.sex.ordinal());
			pbAward.setCount(award.count);
			pbAward.setIndate(award.indate);
			builder.addAwards(pbAward.build());
		}
		return builder.build();
	}
	
	/**
	 * To BsePromotion
	 * @return
	 */
	public Promotion toPromotion(User user) {
		Promotion.Builder builder = Promotion.newBuilder();
		builder.setId(_id);
		builder.setName(name);
		builder.setDesc(desc);
		builder.setTarget(taskTarget);
		int userStep = StringUtil.toInt(TaskManager.getInstance().
				queryTaskSpecificData(user, _id, Field.STEP), 0);
		builder.setStep(userStep);
		builder.setTotal(step);
		builder.setInput(inputCode);
	  /**
		  * 标识是否是新添加的一个活动
		  * 0: nothing
		  * 1: new
		  * 2: taken
		  * 3: timeout
		  * 4: hot
		  */
		long currentTimeMillis = System.currentTimeMillis();
		if ( endMillis > 0 ) {
			builder.setEndsec((int)(endMillis/1000));
		}
		if ( startMillis > 0 ) {
			builder.setStartsec((int)(startMillis/1000));
		}
		int status = 0;
		if ( TaskManager.getInstance().isTaskRewardTaken(user, _id) ) {
			status = 2;
		}
		if ( status == 0 ) {
			/**
			 * First check if the task is timeout.
			 */
			if ( endMillis>0 && endMillis < currentTimeMillis) {
				status = 3;
			}
			if ( status == 0 ) {
//				if ( TaskManager.getInstance().isTaskRewardFinished(user, _id) ) {
//					status = 4;
//				}
				if ( isHot ) {
					status = 4;
				}
				if ( status == 0 ) {
					if ( startMillis > currentTimeMillis ) {
						status = 1;
					}
				}
			}	
		}
		
		builder.setStatus(status);
		/**
		 * Check the task taken date & time
		 * 2012-12-10
		 */
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		int takeStartSec = 0;
		int takeEndSec = 0;
		if ( takeBeginHour >= 0 && takeBeginMin >= 0 ) {
			startCal.set(Calendar.HOUR_OF_DAY, takeBeginHour);
			startCal.set(Calendar.MINUTE, takeBeginMin);
			takeStartSec = (int)(startCal.getTimeInMillis()/1000);
			builder.setTakestartsec(takeStartSec);
		} else {
			builder.setTakestartsec(-1);
		}
		if ( takeEndHour >= 0 && takeEndMin >= 0 ) {
			endCal.set(Calendar.HOUR_OF_DAY, takeEndHour);
			endCal.set(Calendar.MINUTE, takeEndMin);
			takeEndSec = (int)(endCal.getTimeInMillis()/1000);
			builder.setTakeendsec(takeEndSec);
		} else {
			builder.setTakeendsec(-1);
		}
		/**
		 * 如果当前正在take的时间端内，不显示这两个值
		 * 2012-01-10
		 */
		if ( currentTimeMillis > startCal.getTimeInMillis() && 
				currentTimeMillis < endCal.getTimeInMillis() ) {
			builder.setTakestartsec(-1);
			builder.setTakeendsec(-1);
		}
		if ( inputKey != null ) {
			builder.setInputkey(inputKey);
		}
		if ( this.gold > 0 ) {
			Reward reward = RewardManager.getRewardGolden(this.gold);
			builder.addGifts(reward.toGift());
		}
		if ( this.exp > 0 ) {
			Reward reward = RewardManager.getRewardExp(this.exp);
			builder.addGifts(reward.toGift());
		}
		if ( this.caifu > 0 ) {
			Reward reward = RewardManager.getRewardYuanbao(this.caifu);
			builder.addGifts(reward.toGift());
		}
		for ( Award award : this.awards ) {
			if ( Constant.ITEM.equals(award.type) ) {
				Reward reward = new Reward();
				reward.setType(RewardType.ITEM);
				reward.setId(award.id);
				reward.setTypeId(String.valueOf(award.typeId));
				reward.setPropColor(award.color);
				reward.setPropCount(award.count);
				reward.setPropIndate(award.indate);
				reward.setLevel(award.lv);
				builder.addGifts(reward.toGift());
			} else if ( Constant.WEAPON.equals(award.type) ) {
				Reward reward = new Reward();
				reward.setType(RewardType.WEAPON);
				String id = null;
				if ( Constant.ONE_NEGATIVE.equals(award.id) ) {
					id = String.valueOf(award.typeId * 10 + user.getLevel()/10);
					reward.setTypeId(String.valueOf(award.typeId));
				} else {
					id = award.id;
					reward.setTypeId(String.valueOf(Constant.ONE_NEGATIVE));
				}
				reward.setId(id);
				reward.setPropColor(award.color);
				reward.setPropCount(award.count);
				reward.setPropIndate(award.indate);
				reward.setLevel(award.lv);
				builder.addGifts(reward.toGift());
			}
		}
		if ( StringUtil.checkNotEmpty(giftDesc) ) {
			builder.setGiftdesc(giftDesc);
		}
		return builder.build();
	}
	
	@Override
	public int compareTo(TaskPojo o) {
		if ( o == null ) {
			return -1;
		}
		if ( this == o ) {
			return 0;
		}
		if ( this.userLevel != o.userLevel ) {
			return  this.userLevel - o.userLevel;
		} else {
			if ( this.type == o.type ) {
				if ( this.type == TaskType.TASK_ACHIVEMENT ) {
					//achievement type task only use id to compare.
					if ( this._id == null ) {
						return 1;
					} else {
						return this._id.compareTo(o._id);
					}
				} else if ( this.type == TaskType.TASK_ACTIVITY ) {
					//活动任务按照
					if ( this.seq != o.seq ) {
						return this.seq - o.seq;
					} else {
						int diff = (int)(this.startMillis - o.startMillis);
						if ( diff != 0 ) {
							//The new activity should be at first.
							return -diff;
						} else {
							diff = (int)(this.endMillis-o.endMillis);
							if ( diff != 0 ) {
								return diff;
							} else {
								diff = this.name.compareTo(o.name);
								if ( diff != 0 ) {
									return diff;
								} else {
									if ( this._id == null ) {
										return 1;
									} else {
										return this._id.compareTo(o._id);
									}
								}
							}
						}
					}
				} else {
					//优先显示升级任务
					if ( ScriptHook.TASK_USER_LEVELUP.getHook().equals(this.script) && 
							!ScriptHook.TASK_USER_LEVELUP.getHook().equals(o.script) ) {
						return -1;
					} else if ( ScriptHook.TASK_USER_LEVELUP.getHook().equals(o.script) && 
							!ScriptHook.TASK_USER_LEVELUP.getHook().equals(this.script) ) {
						return 1;
					}
					if ( this.seq != o.seq ) {
						return this.seq - o.seq;
					} else {
						if ( this._id == null ) {
							return 1;
						} else {
							return this._id.compareTo(o._id);
						}
					}
				}
			} else {
				//不同类型的任务直接比较ID
				if ( this._id != null ) {
					if ( o._id != null ) {
						return this._id.compareTo(o._id);
					} else {
						return 1;
					}
				} else {
					return -1;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskPojo [_id=");
		builder.append(_id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", step=");
		builder.append(step);
		builder.append(", level=");
		builder.append(level);
		builder.append(", seq=");
		builder.append(seq);
		builder.append(", userLevel=");
		builder.append(userLevel);
		builder.append("]");
		return builder.toString();
	}

	public String toLuaString(Locale locale) {
		String resName = this.name;
		String resDesc = this.desc;
		String resTaskTarget = this.taskTarget;
		if ( Constant.I18N_ENABLE ) {
			resName = GameResourceManager.getInstance().getGameResource(
					"tasks_name_".concat(_id), locale, this.name);
			resDesc = GameResourceManager.getInstance().getGameResource(
					"tasks_desc_".concat(_id), locale, this.desc);
			resTaskTarget = GameResourceManager.getInstance().getGameResource(
					"tasks_taskTarget_".concat(_id), locale, this.taskTarget);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("id").append(_id).append(" = {\n");
		builder.append("\t id=").append(_id).append(",\n");
		builder.append("\t  name=\"");
		builder.append(resName).append("\",\n");
		builder.append("\t  desc=\"");
		String escapeResDesc = Constant.EMPTY;
		if ( resDesc != null ) {
			escapeResDesc = resDesc.replaceAll("\n", "\\\\n");
		}
		builder.append(escapeResDesc).append("\",\n");
		builder.append("\t  taskTarget=\"");
		builder.append(resTaskTarget).append("\",\n");
		builder.append("\t  step=");
		builder.append(step).append(",\n");
		builder.append("\t  level=");
		builder.append(level).append(",\n");
		builder.append("\t  type=\"");
		builder.append(type).append("\",\n");
		builder.append("\t  exp=");
		builder.append(exp).append(",\n");
		builder.append("\t  gold=");
		builder.append(gold).append(",\n");
		builder.append("\t  caifu=");
		builder.append(caifu).append(",\n");
		builder.append("\t  guildwealth=");
		builder.append(guildWealth).append(",\n");
		builder.append("\t  guildcredit=");
		builder.append(guildCredit).append(",\n");
		if ( awards != null ) {
			builder.append("\t awards= {\n");
			for ( Award a : awards ) {
				/**
				 * For all 'weapon' awards, use the id as weapon id
				 * wangqi 2012-08-14
				 */
				if ( Constant.WEAPON.equals(a.type) ) {
					builder.append("\t\t{id=").append(String.valueOf(a.typeId*10)).append(", \n");
				} else {
					builder.append("\t\t{id=").append(a.id).append(", \n");
				}
				builder.append("\t\t type=\"").append(a.type).append("\",\n");
				builder.append("\t\t lv=").append(a.lv).append(",\n");
				builder.append("\t\t sex=").append(a.sex).append(",\n");
				builder.append("\t\t count=").append(a.count).append(",\n");
				builder.append("\t\t indate=").append(a.indate).append(",\n");
				builder.append("\t\t}, \n");
			}
			builder.append("\t}, \n");
		}
		builder.append("},\n");
		return builder.toString();
	}

	/**
	 * The award option
	 * @author wangqi
	 *
	 */
	public static class Award {
		public String type = Constant.ITEM;
		public String id = null;
		public int typeId = 0;
		public int lv = 0;
		/**
		 * sex
		 * 1: female
		 * 2: male
		 * 3: all
		 */
		public Gender sex = Gender.ALL;
		public int count = 1;
		public int indate = 30;
		public WeaponColor color = WeaponColor.WHITE;
		public String resource = null;
	}

}
