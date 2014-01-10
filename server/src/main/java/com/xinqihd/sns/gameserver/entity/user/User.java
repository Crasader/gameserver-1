package com.xinqihd.sns.gameserver.entity.user;

import static com.xinqihd.sns.gameserver.config.Constant.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.MapPojo.Point;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.config.VipPojo;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.SecureLimitManager;
import com.xinqihd.sns.gameserver.db.mongo.SecureLimitManager.LimitType;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.invite.Invite;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo.BseRoleBattleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseToolList.BseToolList;
import com.xinqihd.sns.gameserver.proto.XinqiFriendInfoLite.FriendInfoLite;
import com.xinqihd.sns.gameserver.proto.XinqiRoleInfo.RoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.proto.XinqiUserData.UserData;
import com.xinqihd.sns.gameserver.proto.XinqiUserExData.UserExData;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

public class User extends BasicUser implements Serializable {

	private static final long serialVersionUID = 5977485934554543435L;
	
	private static final Logger logger = LoggerFactory.getLogger(User.class);
	
	//Our customized userid (shardkey)
	private UserId _id = null;
	
  //The unique device id for the user.
	private String uuid = EMPTY;
	
	// 用户名
	private String username = EMPTY;
	// 国家
	private String country = EMPTY;
  // 用户别名
	private String roleName = EMPTY;

	// 性别 1: female 2: male, 
	private Gender gender = Gender.FEMALE;
	
	// 用户上传的头像路径
	private String iconurl = null;
  
	// 玩家等级
	private int level = 0;
	
  //  是否为vip玩家, true/false
	private boolean isvip = false;
  //  vip的级别
	private int     viplevel = 0;
	
  // 密码
	protected String password = EMPTY;
	//邮箱
	private String email = EMPTY;
		
	//玩家的当日总行动点数
	private int roleTotalAction = 200;
	
	//玩家当前的行动点数，不需要保存到Mongo数据库
	private transient int roleAction = 0;
	
	//客户端的类型，采用数组类型，可以添加手机
	private String client = EMPTY;
	//经纬度
	private Location loc = new Location();
	//注册日期
	private Date cdate = new Date();
	//最后访问日期
	private Date ldate = new Date();
	//任务刷新日期
	private Date tDate = new Date();
	//总游戏分钟数
	private int totalmin = 0;
	//玩家胜率 0 - 100
	private int winOdds = 0;
  //玩家胜场
	private int wins = 0;
	//总战斗次数
	private int battleCount = 0;
	//失败次数
	private int failcount = 0;
	//来源渠道
	private String channel = EMPTY;
  
  // 金币
  private int golden = -1;
  // 绑定的元宝
  private int yuanbao = -1;
  // 非绑定的元宝
  private int yuanbaoFree = -1;
  // 礼券
  private int voucher = -1;
  // 勋章
  private int medal = -1;
  
  // 玩家经验值
  private int exp = -1;
  // 战斗力
  private int power = -1;
  // 玩家的攻击值
  private int attack = -1;
  // 玩家的防御值
  private int defend = -1;
  // 玩家的敏捷值
  private int agility = -1;
  // 玩家的幸运值
  private int luck = -1;
  // 玩家血量
  private int blood = -1;
  // 玩家体力
  private int tkew = -1;
  // 玩家的伤害值（基础伤害值从ZooKeeper中读取)
  private int damage = -1;
  // 玩家护甲
  private int skin = -1;
  // 如果不为空，则表示玩家所属的公会ID
  private String guildId = null;

  //  vip开始日期，用来统计用户的VIP级别
  private Date    vipbdate = null;
  //  vip结束日期
  private Date    vipedate = null;
  //  vip经验，用来做vip升级使用，每天自动增长。如果vip过期则每天自动下降，直到0
  private int     vipexp = 0;

  // 隐藏帽子
  private boolean configHideHat = false;
  // 隐藏眼镜
  private boolean configHideGlass = false;
  // 隐藏套装
  private boolean configHideSuite = false;
  // 完成新手引导
  private boolean configLeadFinish = false;
  // 音量开关
  private boolean configMusicSwitch = true;
  // 音效开关
  private boolean configEffectSwitch = true;
  // 音量大小
  private int configMusicVolume = 50;
  // 音效大小
  private int configEffectVolume = 50;
    
  // Promotion related
  //连续登录天数
  private int continuLoginTimes = 0;
  
  //剩余每日抽奖次数
  private int remainLotteryTimes = 0;
  
  //Extra added value
  private HashMap<PropDataEnhanceField, Integer> valueMap = 
  		new HashMap<PropDataEnhanceField, Integer>();
  
  //User's friends, guild commrades, recent contacts and blacklist users.
  private HashMap<RelationType, Relation> relations = 
  		new HashMap<RelationType, Relation>();
  
  //User's bag.
  private Bag bag = new Bag();
  
  private int maxToolCount = GameDataManager.getInstance().
  		getGameDataAsInt(GameDataKey.USER_TOOL_MAX, 3);
  
  private int currentToolCount = 0;
  
  //This is the abtest key stored for user.
  private String abtest = null;
  
  //The user's belonging server.
  private String serverId = null;
  
  //The tools user can use in combat.
  private List<BuffToolType> tools = new ArrayList<BuffToolType>(3);
  
  //Indiate if it is an AI user.
  private boolean isAI = false;
  
  //The user account's login status
  private UserLoginStatus loginStatus = UserLoginStatus.NORMAL;
  
  //The login status desc
  private String loginStatusDesc = Constant.EMPTY;
  
  private UserStatus status = UserStatus.NORMAL;
  
  //User's achievement
  private int achievement = 0;
  
  //The user has not finish his information yet
  private boolean isGuest = false;
  
  //The user need the tutorial
  private boolean tutorial = false;
  
  //The user's email is verified.
  private boolean verifiedEmail = false;
  
  //The total number of users that is killed.
  private int totalKills = 0;
  
  //The screen resoluation like 960x640
  private String screen = Constant.EMPTY;
  
  /**
   * For iOS devices, there is a deviceToken returned by 
   * Apple's APN service for remote pushing.
   */
  private String deviceToken = null;
  
  /**
   * 0-32 bit tutorial mark. If given bit
   * marked as 1, the tutorial step is already passed
   * by user.
   */
  private int tutorialMark = 0;
  
  /**
   * The cumulated value of yuanbao that user charged.
   */
  private int chargedYuanbao = 0;
  
  /**
   * 统计用户充值次数
   */
  private int chargeCount = 0;
  
  /**
   * The fields that relates to accountName
   */
  private String accountName = null;
  
  /**
   * Check if the user is an admin user
   */
  private boolean isAdmin = false;
  
  /**
   * The user's biblio object
   */
  private UserBiblio biblio = null;
 
  /**
   * Store the Weibo token in this map. Now support
   * 1. sina
   * 2. qq
   */
  private HashMap<String, String> weiboTokenMap = 
  		new HashMap<String, String>();
  // --------------------------------- For internal use only
  
  //This is the session key assigned when users login.
  private transient SessionKey sessionKey = null;
  
  //This is used to track the modified field.
  private transient Set<UserChangeFlag> changeFields = 
  		Collections.synchronizedSet(EnumSet.noneOf(UserChangeFlag.class));

  private transient IoSession session = null;
  
  //The user's current todo tasks. It is filled when user login.
  private transient EnumMap<TaskType, HashSet<TaskPojo>> tasks = 
  		new EnumMap<TaskType, HashSet<TaskPojo>>(TaskType.class);
  
  //The user's current finished tasks. It is filled when user login.
  private transient EnumMap<TaskType, HashSet<TaskPojo>> finishedTasks = 
  		new EnumMap<TaskType, HashSet<TaskPojo>>(TaskType.class);
  
  private transient TreeSet<TaskPojo> allTasks = 
  		new TreeSet<TaskPojo>();
  
  //This is the battle rewards set after a battle is over.
  private transient List<Reward> battleRewards = null;
  
  //This is the user login reward
  private transient ArrayList<Reward> loginRewards = null;
  
  //This is the user online reward
  private transient ArrayList<Reward> onlineRewards = null;
  
  /**
   * It is used in DESK game mode
   */
  private transient boolean isProxy = false;
  
  private transient SessionKey proxySessionKey = null;
    
  /**
   * Store user's specific data.
   */
  private transient HashMap<String, Object> data = 
  		new HashMap<String, Object>();
  
  /**
   * The user's current expRate
   */
  private transient float expRate = 0f;
  
  /**
   * The user's locale
   */
  private transient Locale userLocale = Locale.SIMPLIFIED_CHINESE;
  
  /**
   * It is the default user template
   */
  private transient boolean isDefaultUser = false;
  
  /**
   * The last pending invite that this user received.
   */
  private transient Invite lastInvite = null;
  
  /**
   * If he is a PVE boss?
   */
  private transient boolean isBoss = false;
  
  /**
   * The current user's unlock status.
   */
  private transient Collection<Unlock> unlocks = null;
  
  /**
   * The user's belong gameserver id
   */
  private transient String gameserverId = null;
  
  //Flag is used to check if use the modified flag tracking.
  public static final boolean USE_CHANGE_FLAG = true;
  
  /**
   * The server that the user is in.
   */
  private transient ServerPojo serverPojo = null;
  
  /**
   * The account object that this belongs to.
   */
  private transient Account account = null;
  
  /**
   * The guild that the user belongs to.
   */
  private transient Guild guild = null;
  
  /**
   * The guild member for this user.
   */
  private transient GuildMember guildMember = null;

  public User() {
  	this.bag.setParentUser(this);
  }
  
  //--------------------------------- Properties method

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.PASSWORD);
			}
		}
		this.password = password;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.EMAIL);
			}
		}
		this.email = email;
	}

	/**
	 * @return the client
	 */
	public String getClient() {
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(String client) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CLIENT);
			}
		}
		this.client = client;
	}

	/**
	 * @return the locX
	 */
	public Location getLocation() {
		return loc;
	}

	/**
	 * @param locX the locX to set
	 */
	public void setLocation(Location loc) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.LOC);
			}
		}
		this.loc = loc;
	}

	/**
	 * @return the cdate
	 */
	public Date getCdate() {
		return cdate;
	}

	/**
	 * @param cdate the cdate to set
	 */
	public void setCdate(Date cdate) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CDATE);
			}
		}
		this.cdate = cdate;
	}

	/**
	 * @return the ldate
	 */
	public Date getLdate() {
		return ldate;
	}

	/**
	 * @param ldate the ldate to set
	 */
	public void setLdate(Date ldate) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.LDATE);
			}
		}
		this.ldate = ldate;
	}
	
	/**
	 * @return the ldate
	 */
	public Date getTdate() {
		return tDate;
	}

	/**
	 * @param ldate the ldate to set
	 */
	public void setTdate(Date tDate) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.TDATE);
			}
		}
		this.tDate = tDate;
	}
	
	/**
	 * @return the totalmin
	 */
	public int getTotalmin() {
		return totalmin;
	}

	/**
	 * @param totalmin the totalmin to set
	 */
	public void setTotalmin(int totalmin) {
		if ( this.totalmin == totalmin ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.TOTALMIN);
			}
		}
		this.totalmin = totalmin;
	}

	/**
	 * @return the winOdds
	 */
	public int getWinOdds() {
		return winOdds;
	}

	/**
	 * @param winOdds the winOdds to set
	 */
	public void setWinOdds(int winOdds) {
		if ( this.winOdds == winOdds ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.WINODDS);
			}
		}
		this.winOdds = winOdds;
	}

	/**
	 * @return the wins
	 */
	public int getWins() {
		return wins;
	}

	/**
	 * @param wins the wins to set
	 */
	public void setWins(int wins) {
		if ( this.wins == wins ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.WINS);
			}
		}
		this.wins = wins;
	}

	/**
	 * @return the battleCount
	 */
	public int getBattleCount() {
		return battleCount;
	}

	/**
	 * @param battleCount the battleCount to set
	 */
	public void setBattleCount(int battleCount) {
		if ( this.battleCount == battleCount ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.BATTLECOUNT);
			}
		}
		this.battleCount = battleCount;
	}

	/**
	 * @return the failcount
	 */
	public int getFailcount() {
		return failcount;
	}

	/**
	 * @param failcount the failcount to set
	 */
	public void setFailcount(int failcount) {
		if ( this.failcount == failcount ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.FAILCOUNT);
			}
		}
		this.failcount = failcount;
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
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CHANNEL);
			}
		}
		this.channel = channel;
	}

	/**
	 * @return the golden
	 */
	public int getGolden() {
		return golden;
	}

	/**
	 * Check if the daily golden exceeds secure limit.
	 * @param golden
	 */
	public void setGolden(int golden) {
		int oldGolden = this.getGolden();
		if ( oldGolden < 0 ) oldGolden = 0;
		boolean passCheck = SecureLimitManager.getInstance().setValueForUser(
				this, LimitType.GOLDEN, oldGolden, golden);
		if ( passCheck ) {
			setGoldenSecure(golden);
		}
	}
	
	/**
	 * @param golden the golden to set
	 */
	public void setGoldenSecure(int golden) {
		if ( this.golden == golden ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.GOLDEN);
			}
		}
		//Call the TaskHook
		TaskManager.getInstance().processUserTasks(this, 
				TaskHook.WEALTH, MoneyType.GOLDEN, golden);

		this.golden = golden;
	}
	
	/**
	 * It is for the MongoUserManager#constructUserObject 
	 * @param golden the golden to set
	 */
	public void setGoldenSimple(int golden) {
		this.golden = golden;
	}

	/**
	 * @return the yuanbao
	 */
	public int getYuanbao() {
		return yuanbao;
	}
	
	/**
	 * Check if the yuanbao exceeds the secure limit first.
	 * @param yuanbao
	 */
	public void setYuanbao(int yuanbao) {
		if ( this.yuanbao == yuanbao ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.YUANBAO);
			}
		}
		this.yuanbao = yuanbao;
		if ( this.yuanbao < 0 ) {
			this.yuanbao = 0;
		}
		
		//Call the TaskHook
		TaskManager.getInstance().processUserTasks(this, 
				TaskHook.WEALTH, MoneyType.YUANBAO, yuanbao);
		
		//Update the rank data
		RankManager rankManager = RankManager.getInstance();
		RankUser rankUser = rankManager.queryUserCurrentRank(this, 
				RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH, null);
		if ( rankUser == null || rankUser.getScore() < this.yuanbao ) {
			//The user has the highest wealth number. 
			//Update all the ranking zset.
			RankManager.getInstance().storeGlobalRankData(
					this, RankScoreType.WEALTH, System.currentTimeMillis());
		} else {
			rankUser = rankManager.queryUserCurrentRank(this, 
					RankType.GLOBAL, RankFilterType.MONTHLY, RankScoreType.WEALTH, null);
			if ( rankUser == null || rankUser.getScore() < this.yuanbao ) {
				//The user has the monthly highest rank
				RankManager.getInstance().storeGlobalRankData(
						this, RankScoreType.WEALTH, System.currentTimeMillis(), 
						new RankFilterType[]{RankFilterType.MONTHLY, RankFilterType.DAILY});				
			} else {
				rankUser = rankManager.queryUserCurrentRank(this, 
						RankType.GLOBAL, RankFilterType.DAILY, RankScoreType.WEALTH, null);
				if ( rankUser == null || rankUser.getScore() < this.yuanbao ) {
					//The user has the daily highest rank
					RankManager.getInstance().storeGlobalRankData(
							this, RankScoreType.WEALTH, System.currentTimeMillis(), 
							new RankFilterType[]{RankFilterType.DAILY});
				}
			}
		}
	}

	/**
	 * @return the yuanbaoBound
	 */
	public int getYuanbaoFree() {
		return yuanbaoFree;
	}

	/**
	 * @param yuanbaoFree the yuanbaoBound to set
	 */
	public void setYuanbaoFree(int yuanbaoFree) {
		int oldYuanbaoFree = this.getYuanbaoFree();
		if ( oldYuanbaoFree < 0 ) oldYuanbaoFree = 0;
		
		boolean passCheck = SecureLimitManager.getInstance().setValueForUser(
				this, LimitType.YUANBAO, oldYuanbaoFree, yuanbaoFree);
		if ( passCheck ) {
			setYuanbaoFreeSecure(yuanbaoFree);
		}
	}
	
	/**
	 * This method donot check the limit.
	 * @param yuanbaoFree the yuanbao to set
	 */
	public void setYuanbaoFreeSecure(int yuanbaoFree) {
		if ( this.yuanbaoFree == yuanbaoFree ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.YUANBAO_FREE);
			}
		}
		this.yuanbaoFree = yuanbaoFree;		
	}
	
	
	/**
	 * It is for the MongoUserManager#constructUserObject
	 * @param yuanbao the yuanbao to set
	 */
	public void setYuanbaoSimple(int yuanbao) {
		this.yuanbao = yuanbao;
	}
	
	/**
	 * It is for the MongoUserManager#constructUserObject
	 * @param yuanbao the yuanbao to set
	 */
	public void setYuanbaoFreeSimple(int yuanbaoFree) {
		this.yuanbaoFree = yuanbaoFree;
	}
	
	/**
	 * Subtract the yuanbao from 
	 * @param yuanbaoPrice
	 */
	public void payYuanbao(int yuanbaoPrice) {
		/**
		 * 优先使用非绑定的元宝消费
		 */
		if ( yuanbaoPrice <= 0 ) return;
		if ( this.yuanbaoFree >= yuanbaoPrice ) {
			setYuanbaoFree(this.yuanbaoFree - yuanbaoPrice);
		} else {
			int diff = yuanbaoPrice - this.yuanbaoFree;
			setYuanbaoFree(0);
			setYuanbao(yuanbao - diff);
		}
	}
	
	/**
	 * Subtract the yuanbao from 
	 * @param yuanbaoPrice
	 */
	public boolean canPayYuanbao(int yuanbaoPrice) {
		/**
		 * 优先使用非绑定的元宝消费
		 */
		if ( this.yuanbaoFree + this.yuanbao >= yuanbaoPrice ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return the voucher
	 */
	public int getVoucher() {
		return voucher;
	}

	/**
	 * @param voucher the voucher to set
	 */
	public void setVoucher(int voucher) {
		if ( this.voucher == voucher ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VOUCHER);
			}
		}
		this.voucher = voucher;
	}

	/**
	 * @return the medal
	 */
	public int getMedal() {
		/**
		 * Use the guild medal as the medal value
		 */
		//return medal;
		if ( guildMember != null ) {
			return guildMember.getMedal();
		}
		return 0;
	}

	/**
	 * @param medal the medal to set
	 */
	public void setMedal(int medal) {
		/**
		 * Use the guild medal as user's medal
		 */
		/*
		if ( this.medal == medal ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.MEDAL);
			}
		}
		boolean isNewUser = (this.medal == -1);
		this.medal = medal;
		*/
		//Update the rank data
		/*
		if ( !isNewUser ) {
			RankManager.getInstance().storeGlobalRankData(
				this, RankScoreType.MEDAL, System.currentTimeMillis());
		}
		*/
		if ( guildMember != null && medal != guildMember.getMedal()) {
			guildMember.setMedal(medal);
		}
	}

	/**
	 * @return the exp
	 */
	public int getExp() {
		return exp;
	}
	
	/**
	 * Check if the exp exceeds limit
	 * @param newExp
	 */
	public void setExp(int newExp) {
		int oldExp = this.getExp();
		if ( oldExp < 0 ) oldExp = 0;
		boolean passCheck = SecureLimitManager.getInstance().setValueForUser(
				this, LimitType.EXP, oldExp, newExp);
		if ( passCheck ) {
			setExpSecure(newExp);
		}
		StatClient.getIntance().sendDataToStatServer(this, StatAction.ExpGrow, level, oldExp, newExp, passCheck);
	}

	/**
	 * @param newExp the exp to set
	 */
	public void setExpSecure(int newExp) {		
		if ( this.level < 1 ) this.level = 1;
		if ( this.level >= LevelManager.MAX_LEVEL ) {
			logger.debug("User {} reach the max level.", username);
			return;
		}
		int originalLevel = this.level;
		int leftExp = newExp;
		int finalLevel = this.level;
		for ( int i=this.level; i<=LevelManager.MAX_LEVEL; i++ ) {
			LevelPojo level = LevelManager.getInstance().getLevel(i);
			finalLevel = i;
			if ( leftExp >= level.getExp() ) {
				leftExp -= level.getExp();
			} else {
				break;
			}
		}

		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.EXP);
			}
		}
		this.exp = leftExp;
		
		if ( finalLevel > this.level ) {
			/*
			if ( USE_CHANGE_FLAG ) {
				synchronized (changeFields) {
					changeFields.add(UserChangeFlag.LEVEL);
				}
			}
			this.level = finalLevel;
			
			if ( logger.isDebugEnabled() ) {
				logger.debug("User {} upgrade from level {} to level {}", 
					new Object[]{this.username, originalLevel, finalLevel});
			}
			
			//Call script to upgrade properties
			ScriptManager.getInstance().runScript(ScriptHook.USER_LEVEL_UPGRADE, this, 
					(finalLevel-originalLevel) );

			//Call script to trigger task
			TaskManager.getInstance().processUserTasks(this, TaskHook.USER_UPGRADE);
			*/
			this.setLevel(finalLevel);
		}
	}
	
	/**
	 * Set the simple exp.
	 * @param newExp
	 */
	public void setExpSimple(int newExp) {
		this.exp = newExp;
	}
	
	/**
	 * @return the power
	 */
	public int getPower() {
		return power;
	}

	/**
	 * @param power the power to set
	 */
	public void setPower(int power) {
		if ( this.power == power ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.POWER);
			}
		}
		this.power = power;
		
		/**
		 * This should be manually sent
		 */
		/*
		if ( !isDefaultUser ) {
			//Update the rank data
			RankManager.getInstance().storeGlobalRankData(
				this, RankScoreType.POWER, System.currentTimeMillis());
			
			//This is the task that use absolute power value
			TaskManager.getInstance().processUserTasks(this, 
					TaskHook.POWER, power);
		}
		*/
	}
	
	/**
	 * It is for the MongoUserManager
	 * @param power
	 */
	public void setPowerSimple(int power) {
		this.power = power;
	}

	/**
	 * @return the attack
	 */
	public int getAttack() {
		return attack;
	}

	/**
	 * @param attack the attack to set
	 */
	public void setAttack(int attack) {
		if ( this.attack == attack ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ATTACK);
			}
		}
		if ( attack > 0 ) {
			this.attack = attack;
		} else {
			this.attack = 0;
		}
	}

	/**
	 * @return the defend
	 */
	public int getDefend() {
		return defend;
	}

	/**
	 * @param defend the defend to set
	 */
	public void setDefend(int defend) {
		if ( this.defend == defend ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.DEFEND);
			}
		}
		if ( defend > 0 ) {
			this.defend = defend;
		} else {
			this.defend = 0;
		}
	}

	/**
	 * @return the agility
	 */
	public int getAgility() {
		return agility;
	}

	/**
	 * @param agility the agility to set
	 */
	public void setAgility(int agility) {
		if ( this.agility == agility ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.AGILITY);
			}
		}
		if ( agility > 0 ) {
			this.agility = agility;
		} else {
			this.agility = 0;
		}
	}

	/**
	 * @return the luck
	 */
	public int getLuck() {
		return luck;
	}

	/**
	 * @param luck the luck to set
	 */
	public void setLuck(int luck) {
		if ( this.luck == luck ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.LUCK);
			}
		}
		if ( luck > 0 ) {
			this.luck = luck;
		} else {
			this.luck = 0;
		}
	}

	/**
	 * @return the blood
	 */
	public int getBlood() {
		return blood;
	}

	/**
	 * @param blood the blood to set
	 */
	public void setBlood(int blood) {
		if ( this.blood == blood ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.BLOOD);
			}
		}
		if ( blood > 0 ) {
			this.blood = blood;
		} else {
			this.blood = 0;
		}
	}

	/**
	 * @return the tkew
	 */
	public int getTkew() {
		return tkew;
	}

	/**
	 * @param tkew the tkew to set
	 */
	public void setTkew(int tkew) {
		if ( this.tkew == tkew ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.TKEW);
			}
		}
		if ( tkew > 0 ) {
			this.tkew = tkew;
		} else {
			this.tkew = 0;
		}
	}

	/**
	 * @return the damage
	 */
	public int getDamage() {
		return damage;
	}

	/**
	 * @param damage the damage to set
	 */
	public void setDamage(int damage) {
		if ( this.damage == damage ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.DAMAGE);
			}
		}
		if ( damage > 0 ) {
			this.damage = damage;
		} else {
			this.damage = 0;
		}
	}

	/**
	 * @return the skin
	 */
	public int getSkin() {
		return skin;
	}

	/**
	 * @param skin the skin to set
	 */
	public void setSkin(int skin) {
		if ( this.skin == skin ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.SKIN);
			}
		}
		if ( skin > 0 ) {
			this.skin = skin;
		} else {
			this.skin = 0;
		}
	}

	/**
	 * @return the vipbdate
	 */
	public Date getVipbdate() {
		return vipbdate;
	}

	/**
	 * @param vipbdate the vipbdate to set
	 */
	public void setVipbdate(Date vipbdate) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VIPBDATE);
			}
		}
		this.vipbdate = vipbdate;
	}

	/**
	 * @return the vipedate
	 */
	public Date getVipedate() {
		return vipedate;
	}

	/**
	 * @param vipedate the vipedate to set
	 */
	public void setVipedate(Date vipedate) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VIPEDATE);
			}
		}
		this.vipedate = vipedate;
	}

	/**
	 * @return the vipexp
	 */
	public int getVipexp() {
		return vipexp;
	}

	/**
	 * @param vipexp the vipexp to set
	 */
	public void setVipexp(int vipexp) {
		if ( this.vipexp == vipexp ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VIPEXP);
			}
		}
		this.vipexp = vipexp;
	}
	
	/**
	 * @return the vipexp
	 */
	public String getGuildId() {
		return guildId;
	}

	/**
	 * @param vipexp the vipexp to set
	 */
	public void setGuildId(String guildId) {
		if ( this.guildId == guildId ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.GUILDID);
			}
		}
		this.guildId = guildId;
	}

	/**
	 * @return the chargedYuanbao
	 */
	public int getChargedYuanbao() {
		return chargedYuanbao;
	}

	/**
	 * @param chargedYuanbao the chargedYuanbao to set
	 */
	public void setChargedYuanbao(int chargedYuanbao) {
		if ( this.chargedYuanbao == chargedYuanbao ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CHARGED_YUANBAO);
			}
		}
		this.chargedYuanbao = chargedYuanbao;
	}

	/**
	 * @return the chargeCount
	 */
	public int getChargeCount() {
		return chargeCount;
	}

	/**
	 * @param chargeCount the chargeCount to set
	 */
	public void setChargeCount(int chargeCount) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CHARGED_COUNT);
			}
		}
		this.chargeCount = chargeCount;
	}

	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ACCOUNT_NAME);
			}
		}
		this.accountName = accountName;
	}

	/**
	 * @return the configHideHat
	 */
	public boolean isConfigHideHat() {
		return configHideHat;
	}

	/**
	 * @param configHideHat the configHideHat to set
	 */
	public void setConfigHideHat(boolean configHideHat) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGHIDEHAT);
			}
		}
		this.configHideHat = configHideHat;
	}

	/**
	 * @return the configHideGlass
	 */
	public boolean isConfigHideGlass() {
		return configHideGlass;
	}

	/**
	 * @param configHideGlass the configHideGlass to set
	 */
	public void setConfigHideGlass(boolean configHideGlass) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGHIDEGLASS);
			}
		}
		this.configHideGlass = configHideGlass;
	}

	/**
	 * @return the configHideSuite
	 */
	public boolean isConfigHideSuite() {
		return configHideSuite;
	}

	/**
	 * @param configHideSuite the configHideSuite to set
	 */
	public void setConfigHideSuite(boolean configHideSuite) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGHIDESUITE);
			}
		}
		this.configHideSuite = configHideSuite;
	}

	/**
	 * @return the configLeadFinish
	 */
	public boolean isConfigLeadFinish() {
		return configLeadFinish;
	}

	/**
	 * @param configLeadFinish the configLeadFinish to set
	 */
	public void setConfigLeadFinish(boolean configLeadFinish) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGLEADFINISH);
			}
		}
		this.configLeadFinish = configLeadFinish;
	}

	/**
	 * @return the configMusicSwitch
	 */
	public boolean isConfigMusicSwitch() {
		return configMusicSwitch;
	}

	/**
	 * @param configMusicSwitch the configMusicSwitch to set
	 */
	public void setConfigMusicSwitch(boolean configMusicSwitch) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGMUSICSWITCH);
			}
		}
		this.configMusicSwitch = configMusicSwitch;
	}

	/**
	 * @return the configEffectSwitch
	 */
	public boolean isConfigEffectSwitch() {
		return configEffectSwitch;
	}

	/**
	 * @param configEffectSwitch the configEffectSwitch to set
	 */
	public void setConfigEffectSwitch(boolean configEffectSwitch) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGEFFECTSWITCH);
			}
		}
		this.configEffectSwitch = configEffectSwitch;
	}

	/**
	 * @return the configMusicVolume
	 */
	public int getConfigMusicVolume() {
		return configMusicVolume;
	}

	/**
	 * @param configMusicVolume the configMusicVolume to set
	 */
	public void setConfigMusicVolume(int configMusicVolume) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGMUSICVOLUME);
			}
		}
		this.configMusicVolume = configMusicVolume;
	}

	/**
	 * @return the configEffectVolume
	 */
	public int getConfigEffectVolume() {
		return configEffectVolume;
	}

	/**
	 * @param configEffectVolume the configEffectVolume to set
	 */
	public void setConfigEffectVolume(int configEffectVolume) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONFIGEFFECTVOLUME);
			}
		}
		this.configEffectVolume = configEffectVolume;
	}

	/**
	 * @return the continuLoginTimes
	 */
	public int getContinuLoginTimes() {
		return continuLoginTimes;
	}

	/**
	 * @param continuLoginTimes the continuLoginTimes to set
	 */
	public void setContinuLoginTimes(int continuLoginTimes) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CONTINULOGINTIMES);
			}
		}
		this.continuLoginTimes = continuLoginTimes;
	}

	/**
	 * @return the remainLotteryTimes
	 */
	public int getRemainLotteryTimes() {
		return remainLotteryTimes;
	}

	/**
	 * @param remainLotteryTimes the remainLotteryTimes to set
	 */
	public void setRemainLotteryTimes(int remainLotteryTimes) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.REMAINLOTTERYTIMES);
			}
		}
		this.remainLotteryTimes = remainLotteryTimes;
	}

	/**
	 * @return the deviceToken
	 */
	public String getDeviceToken() {
		return deviceToken;
	}

	/**
	 * @param deviceToken the deviceToken to set
	 */
	public void setDeviceToken(String deviceToken) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.DEVICETOKEN);
			}
		}
		this.deviceToken = deviceToken;
	}

	/**
	 * @return the tutorialMark
	 */
	public int getTutorialMark() {
		return tutorialMark;
	}

	/**
	 * @param tutorialMark the tutorialMark to set
	 */
	public void setTutorialMark(int tutorialMark) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.TUTORIALMARK);
			}
		}
		this.tutorialMark = tutorialMark;
	}

	/**
	 * @return the roleTotalAction
	 */
	public int getRoleTotalAction() {
		return roleTotalAction;
	}

	/**
	 * @param roleTotalAction the roleTotalAction to set
	 */
	public void setRoleTotalAction(int roleTotalAction) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ROLETOTALACTIONS);
			}
		}
		this.roleTotalAction = roleTotalAction;
	}

	/**
	 * @return the achievement
	 */
	public int getAchievement() {
		return achievement;
	}

	/**
	 * @param achievement the achievement to set
	 */
	public void setAchievement(int achievement) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ACHIEVEMENT);
			}
		}
		this.achievement = achievement;
	}
	
	// -------------- transient

	/**
	 * Get the internal relation list. Please do not modify it.
	 * @return the relations
	 */
	public Collection<Relation> getRelations() {
		if ( relations == null ) {
			return null;
		} else {
			return relations.values();
		}
	}
	
	/**
	 * Get the specific relation type. 
	 * @param type
	 * @return
	 */
	public Relation getRelation(RelationType type) {
		return this.relations.get(type);
	}
	
	/**
	 * Remove a relation from underlying list.
	 * @param relation
	 */
	public void removeRelation(Relation relation) {
		relations.remove(relation.getType());
	}

	/**
	 * Add the relation to underlying list. Please not
	 * every type of relation can only be added once.
	 * If you add the same type again, original relation
	 * will be deleted.
	 * 
	 * @param relations the relations to set
	 */
	public void addRelation(Relation relation) {
		relation.set_id(_id);
		this.relations.put(relation.getType(), relation);
	}

	/**
	 * @return the bag
	 */
	public Bag getBag() {
		return bag;
	}

	/**
	 * @param bag the bag to set
	 */
	public void setBag(Bag bag) {
		this.bag = bag;
	}

	/**
	 * @return the sessionKey
	 */
	public SessionKey getSessionKey() {
		return sessionKey;
	}

	/**
	 * @param sessionKey the sessionKey to set
	 */
	public void setSessionKey(SessionKey sessionKey) {
		this.sessionKey = sessionKey;
	}

	/**
	 * @return the abtest
	 */
	public String getAbtest() {
		return abtest;
	}

	/**
	 * @param abtest the abtest to set
	 */
	public void setAbtest(String abtest) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ABTEST);
			}
		}
		this.abtest = abtest;
	}
  
	/**
	 * @return the _id
	 */
	public UserId get_id() {
		return _id;
	}
	
	/**
	 * @param _id the _id to set
	 */
	public void set_id(UserId _id) {
		this._id = _id;
	}
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.USERNAME);
			}
		}
		this.username = username;
	}
	
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.COUNTRY);
			}
		}
		this.country = country;
	}
	
	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		if ( this.roleName == null ) {
			this.setRoleName(username);
		}
		return roleName;
	}
	
	/**
	 * @param roleName the roleName to set
	 */
	public void setRoleName(String roleName) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ROLENAME);
			}
		}
		this.roleName = roleName;
	}
	
	
	/**
	 * @return the roleName
	 */
	public String getServerId() {
		return serverId;
	}
	
	/**
	 * @param roleName the roleName to set
	 */
	public void setServerId(String serverId) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.SERVERID);
			}
		}
		this.serverId = serverId;
	}
	
	/**
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}
	
	/**
	 * @param gender the gender to set
	 */
	public void setGender(Gender gender) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.GENDER);
			}
		}
		this.gender = gender;
	}
	
	/**
	 * @return the iconurl
	 */
	public String getIconurl() {
		return iconurl;
	}
	
	/**
	 * @param iconurl the iconurl to set
	 */
	public void setIconurl(String iconurl) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ICONURL);
			}
		}
		this.iconurl = iconurl;
	}
	
	/**
	 * @return the weiboTokenMap
	 */
	public HashMap<String, String> getWeiboTokenMap() {
		return weiboTokenMap;
	}

	/**
	 * Add the weibo token
	 * @param weiboSource
	 * @param weiboToken
	 */
	public void addWeiboToken(String weiboSource, String weiboToken) {
		if ( weiboSource != null ) {
			if ( USE_CHANGE_FLAG ) {
				synchronized (changeFields) {
					changeFields.add(UserChangeFlag.WEIBO);
				}
			}
			if ( weiboToken != null ) {
				this.weiboTokenMap.put(weiboSource, weiboToken);
			} else {
				this.weiboTokenMap.remove(weiboSource);
			}
		}
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}
	
	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		if ( this.level == level ) return;
		
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.LEVEL);
			}
		}
		int originalLevel = this.level;
		if ( logger.isDebugEnabled() ) {
			logger.debug("User {} upgrade from level {} to level {}", 
				new Object[]{this.username, this.level, level});
		}
		this.level = level;
						
		//Call script to upgrade properties
		ScriptManager.getInstance().runScript(ScriptHook.USER_LEVEL_UPGRADE, this, 
				(this.level-originalLevel) );
		
		//Update the rank data
		updatePowerRanking();
		
		if ( !isAI ) {
			//Send BattleRoleInfo to update blood
			BseRoleBattleInfo info = this.toBseRoleBattleInfo(false);
			GameContext.getInstance().writeResponse(sessionKey, info);
			
			//Update the rank data
			/*
			RankManager.getInstance().storeGlobalRankData(
				this, RankScoreType.LEVEL, System.currentTimeMillis());
				*/
			
			//Send user a level-up gift box
			ScriptManager.getInstance().runScript(
					ScriptHook.USER_LEVEL_PROCESSING, this);
			
			String levelStr = String.valueOf(this.level);
			String weiboKey = StringUtil.concat("weibo.levelup.", MathUtil.nextFakeInt(3));
			String weibo = Text.text(weiboKey, levelStr);
			if ( this.level>0 && this.level % 10 == 0 ) {
				String messageKey = "user.level.up".concat(levelStr);
				String message = Text.text(messageKey, levelStr, DateUtil.formatDateTime(new Date()));
				SysMessageManager.getInstance().
					sendClientInfoWeiboMessage(this.getSessionKey(), message, weibo, Type.LEVEL_UP);
			} else {
				String messageKey = "user.level.up";
				String message = Text.text(messageKey, levelStr, DateUtil.formatDateTime(new Date()));
				SysMessageManager.getInstance().
					sendClientInfoWeiboMessage(this.getSessionKey(), message, weibo, Type.LEVEL_UP);
			}
			
			/**
			 * Should not add new tasks when users level up,
			 * it will exceed the limit.
			 * wangqi 2012-07-17
			 */
			/*
			 * //Call script to trigger task
			for ( TaskType taskType : TaskType.values() ) {
				if ( taskType == TaskType.TASK_ACHIVEMENT || 
						taskType == TaskType.TASK_RANDOM ) continue;
				TaskManager.getInstance().assignNewTask(taskType, this);				
			}
			*/
			TaskManager.getInstance().processUserTasks(this, TaskHook.USER_UPGRADE);
			
			StatClient.getIntance().sendDataToStatServer(this, 
					StatAction.Levelup, this.level);
			
			UserActionManager.getInstance().addUserAction(this.getRoleName(), 
					UserActionKey.Levelup, String.valueOf(this.level));
		}
	}
	
	/**
	 * The MongoUserManager query the level from database 
	 * and call this method to set it without triggering
	 * task scripts.
	 * 
	 * @param level
	 */
	public void setLevelSimple(int level) {
		this.level = level;
	}
	
	/**
	 * @return the isvip
	 */
	public boolean isVip() {
		return isvip;
	}
	
	/**
	 * @param isvip the isvip to set
	 */
	public void setIsvip(boolean isvip) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ISVIP);
			}
		}
		this.isvip = isvip;
	}
	/**
	 * @return the viplevel
	 */
	public int getViplevel() {
		return viplevel;
	}
	
	/**
	 * @param viplevel the viplevel to set
	 */
	public void setViplevel(int viplevel) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VIPLEVEL);
			}
		}
		this.viplevel = viplevel;
	}

  /**
	 * @return the maxToolCount
	 */
	public int getMaxToolCount() {
		return maxToolCount;
	}

	/**
	 * @param maxToolCount the maxToolCount to set
	 */
	public void setMaxToolCount(int maxToolCount) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.MAX_TOOL_COUNT);
			}
		}
		this.maxToolCount = maxToolCount;
	}
	
	/**
	 * Check if the user is admin
	 * @return
	 */
	public boolean isAdmin() {
		return isAdmin;
	}
	
	/**
	 * Set the user to admin user. 
	 * @param isAdmin
	 * @return
	 */
	public void setAdmin(boolean isAdmin) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ISADMIN);
			}
		}
		this.isAdmin = isAdmin;
	}

	/**
	 * @return the currentToolCount
	 */
	public int getCurrentToolCount() {
		return currentToolCount;
	}

	/**
	 * @param currentToolCount the currentToolCount to set
	 */
	public void setCurrentToolCount(int currentToolCount) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.CURRENT_TOOL_COUNT);
			}
		}
		this.currentToolCount = currentToolCount;
	}
	
	/**
	 * @return the tools
	 */
	public List<BuffToolType> getTools() {
		return tools;
	}
	
	/**
	 * @return the status
	 */
	public UserStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(UserStatus status) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.USER_STATUS);
			}
		}
		this.status = status;
	}

	/**
	 * @param tools the tools to set
	 */
	public boolean addTool(BuffToolType tool) {
		//Try to find if there is an empty position
		int emptyIndex = -1;
		int length = this.tools.size();
		for ( int i=0; i<length; i++ ) {
			BuffToolType toolType = this.tools.get(i);
			if ( toolType == null ) {
				emptyIndex = i;
				break;
			}
		}
		if ( emptyIndex == -1 ) {
			emptyIndex = length;
			if ( emptyIndex < this.maxToolCount ) {
				this.tools.add(null);
			}
		}
		if ( emptyIndex >= this.maxToolCount ) {
			//There is too many items.
			logger.debug("User {} buffTool bag is full: {}.", this.username, emptyIndex);
			return false;
		} else {
			if ( USE_CHANGE_FLAG ) {
				synchronized (changeFields) {
					changeFields.add(UserChangeFlag.TOOLS);
				}
			}
			this.tools.set(emptyIndex, tool);
			this.setCurrentToolCount(this.getCurrentToolCount()+1);
			
			if ( logger.isDebugEnabled() ) {
				logger.debug("The {} is added to the user {}'s toolbox. The currentToolCount is {}", 
					new Object[]{tool, username, currentToolCount});
			}
		}
		return true;
	}
	
	/**
	 * @param tools the tools to set
	 */
	public void setTool(int index, BuffToolType tool) {
		if (index >= 0 && index < this.getMaxToolCount() ) {
			logger.debug("User {} wants to set {} to the toolbox", username, tool);
			if ( index >= this.tools.size() ) {
				int diff = index - this.tools.size() + 1;
				for ( int i=0; i<diff; i++ ) {
					this.tools.add(null);
				}
			}
			BuffToolType oldTool = this.tools.get(index);
			this.tools.set(index, tool);
			if ( USE_CHANGE_FLAG ) {
				synchronized (changeFields) {
					changeFields.add(UserChangeFlag.TOOLS);
				}
			}
			if ( oldTool == null ) {
				this.setCurrentToolCount(this.getCurrentToolCount()+1);
			}
		}
	}
	
	/**
	 * @param tools the tools to set
	 */
	public void removeTool(int index) {
		if (index >= 0 && index < this.tools.size() ) {
			logger.debug("User {} wants to remove {} from toolbox", username, index);
			if ( USE_CHANGE_FLAG ) {
				synchronized (changeFields) {
					changeFields.add(UserChangeFlag.TOOLS);
				}
			}
			this.tools.set(index, null);
			this.setCurrentToolCount(this.getCurrentToolCount()-1);
		}
	}

	/**
	 * @return the session
	 */
	public IoSession getSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(IoSession session) {
		this.session = session;
	}
	
	/**
	 * @return the tasks
	 */
	public Set<TaskPojo> getTasks() {
		return this.allTasks;
	}
	
	/**
	 * @return the tasks
	 */
	public Set<TaskPojo> getTasks(TaskType taskType) {
		Set<TaskPojo> taskSet = tasks.get(taskType);
		if ( taskSet == null ) {
			return (Set<TaskPojo>)EMPTY_SET;
		}
		return taskSet;
	}
	
	/**
	 * Get all finished tasks
	 * @param taskType
	 * @return
	 */
	public Set<TaskPojo> getTaskFinished(TaskType taskType) {
		Set<TaskPojo> taskSet = finishedTasks.get(taskType);
		if ( taskSet == null ) {
			return (Set<TaskPojo>)EMPTY_SET;
		}
		return taskSet;
	}
	
	/**
	 * Remove the tasks from user's task list.
	 * @param task
	 */
	public void removeTask(TaskPojo task) {
		if ( task != null ) {
			this.allTasks.remove(task);
			HashSet<TaskPojo> tasks = this.tasks.get(task.getType());
			if ( tasks != null ) {
				tasks.remove(task);
			}
		}
	}
	
	/**
	 * Add all tasks to internal set.
	 * @param newTasks
	 */
	public void addTasks(Collection<TaskPojo> newTasks) {
		if ( newTasks != null ) {
			for ( TaskPojo task : newTasks ) {
				TaskType taskType = task.getType();
				HashSet<TaskPojo> taskSet = this.tasks.get(taskType);
				if ( taskSet == null ) {
					taskSet = new HashSet<TaskPojo>();
					this.tasks.put(taskType, taskSet);
				}
				taskSet.add(task);
			}
			this.allTasks.addAll(newTasks);
		}
	}
	
	/**
	 * Add all tasks to internal set.
	 * @param newTasks
	 */
	public void addTaskFinishedCollection(Collection<TaskPojo> tasks) {
		if ( tasks != null ) {
			for ( TaskPojo task : tasks ) {
				addTaskFinished(task);
			}
		}
	}
	
	/**
	 * Add all tasks to internal set.
	 * @param newTasks
	 */
	public void addTaskFinished(TaskPojo task) {
		if ( task != null ) {
			TaskType taskType = task.getType();
			HashSet<TaskPojo> taskSet = this.finishedTasks.get(taskType);
			if ( taskSet == null ) {
				taskSet = new HashSet<TaskPojo>();
				this.finishedTasks.put(taskType, taskSet);
			}
			taskSet.add(task);
		}
	} 
	
	/**
	 * Remove all existing tasks
	 */
	public void clearTasks() {
		this.tasks.clear();
		this.allTasks.clear();
		this.finishedTasks.clear();
	}

	/**
	 * @return the battleRewards
	 */
	public List<Reward> getBattleRewards() {
		return battleRewards;
	}

	/**
	 * @param battleRewards the battleRewards to set
	 */
	public void setBattleRewards(List<Reward> battleRewards) {
		this.battleRewards = battleRewards;
	}

	/**
	 * @return the loginRewards
	 */
	public ArrayList<Reward> getLoginRewards() {
		return loginRewards;
	}

	/**
	 * @param loginRewards the loginRewards to set
	 */
	public void setLoginRewards(ArrayList<Reward> loginRewards) {
		this.loginRewards = loginRewards;
	}

	/**
	 * @return the onlineRewards
	 */
	public ArrayList<Reward> getOnlineRewards() {
		return onlineRewards;
	}

	/**
	 * @param onlineRewards the onlineRewards to set
	 */
	public void setOnlineRewards(ArrayList<Reward> onlineRewards) {
		this.onlineRewards = onlineRewards;
	}

	/**
	 * @return the expRate
	 */
	public float getExpRate() {
		return expRate;
	}

	/**
	 * @param expRate the expRate to set
	 */
	public void setExpRate(float expRate) {
		this.expRate = expRate;
	}

	/**
	 * @return the isAI
	 */
	public boolean isAI() {
		return isAI;
	}

	/**
	 * @param isAI the isAI to set
	 */
	public void setAI(boolean isAI) {
		this.isAI = isAI;
	}

	/**
	 * @return the serverPojo
	 */
	public ServerPojo getServerPojo() {
		return serverPojo;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	/**
	 * @param serverPojo the serverPojo to set
	 */
	public void setServerPojo(ServerPojo serverPojo) {
		this.serverPojo = serverPojo;
	}

	/**
	 * Get user's specific data
	 * @param key
	 * @return
	 */
	public Object getUserData(String key) {
		return this.data.get(key);
	}
	
	/**
	 * Put user's specific data
	 * @param Key
	 * @param value
	 */
	public void putUserData(String key, Object value) {
		if ( value != null ) {
			this.data.put(key, value);
		} else {
			this.data.remove(key);
		}
	}
	
	/**
	 * Clear user's specific data.
	 */
	public void clearUserData() {
		this.data.clear();
	}
	
	/**
	 * Get user login status
	 * @return
	 */
	public UserLoginStatus getLoginStatus() {
		return this.loginStatus;
	}
	
	/**
	 * Set new login status
	 * @param loginStatus
	 */
	public void setLoginStatus(UserLoginStatus loginStatus) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.LOGIN_STATUS);
			}
		}
		this.loginStatus = loginStatus;
	}
	
	/**
	 * @return the loginStatusDesc
	 */
	public String getLoginStatusDesc() {
		return loginStatusDesc;
	}

	/**
	 * @param loginStatusDesc the loginStatusDesc to set
	 */
	public void setLoginStatusDesc(String loginStatusDesc) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.LOGIN_STATUS_DESC);
			}
		}
		this.loginStatusDesc = loginStatusDesc;
	}

	/**
	 * @return the isGuest
	 */
	public boolean isGuest() {
		return isGuest;
	}

	/**
	 * @param isGuest the isGuest to set
	 */
	public void setGuest(boolean isGuest) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.ISGUEST);
			}
		}
		this.isGuest = isGuest;
	}

	/**
	 * @return the valueMap
	 */
	public HashMap<PropDataEnhanceField, Integer> getValueMap() {
		return valueMap;
	}

	/**
	 * @param valueMap the valueMap to set
	 */
	public void setValueMap(HashMap<PropDataEnhanceField, Integer> valueMap) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VALUEMAP);
			}
		}
		this.valueMap = valueMap;
	}
	
	/**
	 * Add value to map
	 * @param field
	 * @param value
	 */
	public void addValueMap(PropDataEnhanceField field, int value) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VALUEMAP);
			}
		}
		valueMap.put(field, value);
	}
	
	/**
	 * Get the given field's value
	 * @param field
	 * @return
	 */
	public int getValueMapFieldValue(PropDataEnhanceField field) {
		Integer value = valueMap.get(field);
		if ( value != null ) {
			return value.intValue();
		}
		return 0;
	}

	/**
	 * @return the tutorial
	 */
	public boolean isTutorial() {
		return tutorial;
	}

	/**
	 * @param tutorial the tutorial to set
	 */
	public void setTutorial(boolean tutorial) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.TUTORIAL);
			}
		}
		this.tutorial = tutorial;
	}
	
	/**
	 * @return the verifiedEmail
	 */
	public boolean isVerifiedEmail() {
		return verifiedEmail;
	}

	/**
	 * @param verifiedEmail the verifiedEmail to set
	 */
	public void setVerifiedEmail(boolean verifiedEmail) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.VERIFIED);
			}
		}
		this.verifiedEmail = verifiedEmail;
	}	


	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.UUID);
			}
		}
		this.uuid = uuid;
	}
	
	/**
	 * @return the totalKills
	 */
	public int getTotalKills() {
		return totalKills;
	}

	/**
	 * @param totalKills the totalKills to set
	 */
	public void setTotalKills(int totalKills) {
		if ( this.totalKills != totalKills ) {
			if ( USE_CHANGE_FLAG ) {
				synchronized (changeFields) {
					changeFields.add(UserChangeFlag.TOTALKILL);
				}
			}
			this.totalKills = totalKills;
			
			//Update the rank data
			GameContext.getInstance().scheduleTask(new Runnable() {
				@Override
				public void run() {
					RankManager.getInstance().storeGlobalRankData(
							User.this, RankScoreType.KILL, System.currentTimeMillis());
				}
			}, 5, TimeUnit.SECONDS);
		}
	}	
	
	/**
	 * @param totalKills the totalKills to set
	 */
	public void setTotalKillSimple(int totalKills) {
		this.totalKills = totalKills;
	}	

	/**
	 * @return the screen
	 */
	public String getScreen() {
		return screen;
	}

	/**
	 * @param screen the screen to set
	 */
	public void setScreen(String screen) {
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				changeFields.add(UserChangeFlag.SCREEN);
			}
		}
		this.screen = screen;
	}
	
	// --------------------------------------------------Transient fields


	/**
	 * @return the isProxy
	 */
	public boolean isProxy() {
		return isProxy;
	}

	/**
	 * @param isProxy the isProxy to set
	 */
	public void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}

	/**
	 * @return the isBoss
	 */
	public boolean isBoss() {
		return isBoss;
	}

	/**
	 * @param isBoss the isBoss to set
	 */
	public void setBoss(boolean isBoss) {
		this.isBoss = isBoss;
	}

	/**
	 * @return the lastInvite
	 */
	public Invite getLastInvite() {
		return lastInvite;
	}

	/**
	 * @param lastInvite the lastInvite to set
	 */
	public void setLastInvite(Invite lastInvite) {
		this.lastInvite = lastInvite;
	}

	/**
	 * @return the proxySessionKey
	 */
	public SessionKey getProxySessionKey() {
		return proxySessionKey;
	}

	/**
	 * If this user is a proxy user, set the sessionKey of the user that 
	 * the IoSession should be redirected to.
	 * 
	 * @param proxySessionKey the proxySessionKey to set
	 */
	public void setProxySessionKey(SessionKey proxySessionKey) {
		this.proxySessionKey = proxySessionKey;
	}

	/**
	 * @return the userLocale
	 */
	public Locale getUserLocale() {
		return userLocale;
	}

	/**
	 * @param userLocale the userLocale to set
	 */
	public void setUserLocale(Locale userLocale) {
		this.userLocale = userLocale;
	}

	/**
	 * @return the isDefaultUser
	 */
	public boolean isDefaultUser() {
		return isDefaultUser;
	}

	/**
	 * @param isDefaultUser the isDefaultUser to set
	 */
	public void setDefaultUser(boolean isDefaultUser) {
		this.isDefaultUser = isDefaultUser;
	}
	
	/**
	 * @return the unlocks
	 */
	public Collection<Unlock> getUnlocks() {
		return unlocks;
	}

	/**
	 * @param unlocks the unlocks to set
	 */
	public void setUnlocks(Collection<Unlock> unlocks) {
		this.unlocks = unlocks;
	}

	/**
	 * @return the roleAction
	 */
	public int getRoleAction() {
		return roleAction;
	}

	/**
	 * @param roleAction the roleAction to set
	 */
	public void setRoleAction(int roleAction) {
		this.roleAction = roleAction;
	}

	/**
	 * @return the biblio
	 */
	public UserBiblio getBiblio() {
//		if ( biblio == null ) {
//			biblio = new UserBiblio();
//			biblio.setId(_id);
//			biblio.setRoleName(roleName);
//		}
		return biblio;
	}

	/**
	 * @param biblio the biblio to set
	 */
	public void setBiblio(UserBiblio biblio) {
		if ( biblio != null ) {
			biblio.setId(_id);
			biblio.setRoleName(roleName);
		}
		this.biblio = biblio;
	}

	/**
	 * @return the gameserverId
	 */
	public String getGameserverId() {
		return gameserverId;
	}

	/**
	 * @param gameserverId the gameserverId to set
	 */
	public void setGameserverId(String gameserverId) {
		this.gameserverId = gameserverId;
	}

	/**
	 * @return the guild
	 */
	public Guild getGuild() {
		return guild;
	}

	/**
	 * @param guild the guild to set
	 */
	public void setGuild(Guild guild) {
		this.guild = guild;
	}

	/**
	 * @return the guildMember
	 */
	public GuildMember getGuildMember() {
		return guildMember;
	}

	/**
	 * @param guildMember the guildMember to set
	 */
	public void setGuildMember(GuildMember guildMember) {
		this.guildMember = guildMember;
	}

	/**
	 * Update user's power ranking
	 */
	public void updatePowerRanking() {
		RankManager.getInstance().storeGlobalRankData(
				this, RankScoreType.POWER, System.currentTimeMillis());
			//This is the task that use absolute power value
			TaskManager.getInstance().processUserTasks(this, 
					TaskHook.POWER, this.getPower());
	}
	
	/**
	 * ===============================================================================
	 */
	
	/**
	 * 
	 * @return
	 */
	public int getAttackTotal() {
		int baseValue = this.attack;
		baseValue += getAttackAdded();
		return baseValue;
	}

	/**
	 * @param baseValue
	 */
	public int getAttackAdded() {
		Integer addedValue = getValueMapFieldValue(PropDataEnhanceField.ATTACK);
		if ( addedValue != null ) {
			return addedValue.intValue();
		}
		return 0;
	}
	
	public int getDefendTotal() {
		int baseValue = this.defend;
		baseValue += getDefendAdded();
		return baseValue;
	}

	/**
	 * @param baseValue
	 * @return
	 */
	public int getDefendAdded() {
		Integer addedValue = getValueMapFieldValue(PropDataEnhanceField.DEFEND);
		if ( addedValue != null ) {
			return addedValue.intValue();
		}
		return 0;
	}
	
	public int getAgilityTotal() {
		int baseValue = this.agility;
		baseValue += getAgilityAdded();
		return baseValue;
	}

	/**
	 * @param baseValue
	 */
	public int getAgilityAdded() {
		Integer addedValue = getValueMapFieldValue(PropDataEnhanceField.AGILITY);
		if ( addedValue != null ) {
			return addedValue.intValue();
		}
		return 0;
	}
	
	public int getLuckyTotal() {
		int baseValue = this.luck;
		baseValue += getLuckyAdded();
		return baseValue;
	}

	/**
	 * @param baseValue
	 */
	public int getLuckyAdded() {
		Integer addedValue = getValueMapFieldValue(PropDataEnhanceField.LUCKY);
		if ( addedValue != null ) {
			return addedValue.intValue();
		}
		return 0;
	}
	
	public int getBloodTotal() {
		int baseValue = this.blood;
		baseValue += getBloodAdded();
		return baseValue;
	}

	/**
	 * @param baseValue
	 */
	public int getBloodAdded() {
		Integer addedValue = getValueMapFieldValue(PropDataEnhanceField.BLOOD);
		if ( addedValue != null ) {
			return addedValue.intValue();
		}
		return 0;
	}
	
	public int getTkewTotal() {
		int baseTkew = this.tkew;
		baseTkew += getTkewAdded();
		return baseTkew;
	}

	/**
	 * 
	 */
	public int getTkewAdded() {
		Integer addedValue = getValueMapFieldValue(PropDataEnhanceField.AGILITY);
		if ( addedValue != null ) {
			int tkew = (int)UserCalculator.calculateThewWithoutBase(addedValue.intValue());
			return tkew;
		}
		return 0;
	}
	
	public int getPowerAdded() {
		int powerAdded = (int)EquipCalculator.calculateWeaponPower(
				getAttackAdded(), getDefendAdded(), getAgilityAdded(), getLuckyAdded(), getBloodAdded(), 0);
		return powerAdded;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("username \t ");
		builder.append(username);
		builder.append(" \t country \t ");
		builder.append(country);
		builder.append(" \t roleName \t ");
		builder.append(roleName);
		builder.append(" \t gender \t ");
		builder.append(gender);
		builder.append(" \t iconurl \t ");
		builder.append(iconurl);
		builder.append(" \t level \t ");
		builder.append(level);
		builder.append(" \t isvip \t ");
		builder.append(isvip);
		builder.append(" \t viplevel \t ");
		builder.append(viplevel);
		builder.append(" \t password \t ");
		builder.append(password);
		builder.append(" \t email \t ");
		builder.append(email);
		builder.append(" \t client \t ");
		builder.append(client);
		builder.append(" \t loc \t ");
		builder.append(loc);
		builder.append(" \t cdate \t ");
		builder.append(cdate);
		builder.append(" \t ldate \t ");
		builder.append(ldate);
		builder.append(" \t totalmin \t ");
		builder.append(totalmin);
		builder.append(" \t winOdds \t ");
		builder.append(winOdds);
		builder.append(" \t wins \t ");
		builder.append(wins);
		builder.append(" \t battleCount \t ");
		builder.append(battleCount);
		builder.append(" \t failcount \t ");
		builder.append(failcount);
		builder.append(" \t channel \t ");
		builder.append(channel);
		builder.append(" \t golden \t ");
		builder.append(golden);
		builder.append(" \t yuanbao \t ");
		builder.append(yuanbao);
		builder.append(" \t voucher \t ");
		builder.append(voucher);
		builder.append(" \t medal \t ");
		builder.append(medal);
		builder.append(" \t exp \t ");
		builder.append(exp);
		builder.append(" \t power \t ");
		builder.append(power);
		builder.append(" \t attack \t ");
		builder.append(attack);
		builder.append(" \t defend \t ");
		builder.append(defend);
		builder.append(" \t agility \t ");
		builder.append(agility);
		builder.append(" \t luck \t ");
		builder.append(luck);
		builder.append(" \t blood \t ");
		builder.append(blood);
		builder.append(" \t tkew \t ");
		builder.append(tkew);
		builder.append(" \t damage \t ");
		builder.append(damage);
		builder.append(" \t skin \t ");
		builder.append(skin);
		builder.append(" \t vipbdate \t ");
		builder.append(vipbdate);
		builder.append(" \t vipedate \t ");
		builder.append(vipedate);
		builder.append(" \t vipexp \t ");
		builder.append(vipexp);
		builder.append(" \t continuLoginTimes \t ");
		builder.append(continuLoginTimes);
		builder.append(" \t remainLotteryTimes \t ");
		builder.append(remainLotteryTimes);
		builder.append(" \t relations \t ");
		builder.append(relations);
		builder.append(" \t bag \t ");
		builder.append(bag);
		builder.append(" \t maxToolCount \t ");
		builder.append(maxToolCount);
		builder.append(" \t currentToolCount \t ");
		builder.append(currentToolCount);
		builder.append(" \t abtest \t ");
		builder.append(abtest);
		builder.append(" \t tools \t ");
		builder.append(tools);
		return builder.toString();
	}

	/**
	 * It is a facility method to quickly convert User object to 
	 * protobuf object.
	 * 
	 * @return
	 */
	public BseRoleInfo toBseRoleInfo() {
		if ( this._id == null ) return null;
		try {
			BseRoleInfo.Builder roleInfoBuilder = BseRoleInfo.newBuilder();
			roleInfoBuilder.setContinuLoginTimes(this.getContinuLoginTimes());
			roleInfoBuilder.setRemainLotteryTimes(this.getRemainLotteryTimes());
			String roleName = UserManager.getDisplayRoleName(this.roleName);
			roleInfoBuilder.setRoleName(roleName);
			roleInfoBuilder.setGender(this.getGender().ordinal());
			roleInfoBuilder.setRoleCoin(this.getYuanbao());
			if ( this.getYuanbaoFree() > 0 ) {
				roleInfoBuilder.setYuanbaoFree(this.getYuanbaoFree());
			}
			roleInfoBuilder.setRoleExp(this.getExp());
			roleInfoBuilder.setRoleGiftMoney(this.getVoucher());
			roleInfoBuilder.setRoleGold(this.getGolden());
			roleInfoBuilder.setRoleLevel(this.getLevel());
			roleInfoBuilder.setRoleWinOdds(this.getWinOdds());
			roleInfoBuilder.setRoleWins(this.getWins());
			roleInfoBuilder.setVip(this.isVip());
			roleInfoBuilder.setRoleMedal(this.getMedal());
			/**
			 * Use the medal as the guild credit
			 * 2013-03-05
			 */
			if ( guildMember != null ) {
				roleInfoBuilder.setGuildcredit(guildMember.getCredit());
				roleInfoBuilder.setGuildmedal(guildMember.getMedal());
			}

			roleInfoBuilder.setTutorial(this.isTutorial());
			if ( this.getSessionKey() != null ) {
				roleInfoBuilder.setSessionid(sessionKey.toString());
			}
			if ( this.isVip() ) {
				roleInfoBuilder.setViplevel(this.getViplevel());
				//this.getViplevel() is from 1, array is from 0
				VipPojo vipPojo = VipManager.getInstance().getVipPojoById(this.getViplevel()+1);
				if ( vipPojo != null ) {
					int chargedYuanbao = this.chargedYuanbao;
					int nextLevelYuanbao = vipPojo.getYuanbaoPrice();
					int diff = (nextLevelYuanbao-chargedYuanbao)/10;
					if ( diff < 0 ) {
						diff = 0;
					}
					roleInfoBuilder.setVipmoney(diff);
				}
				if ( this.vipbdate != null ) {
					roleInfoBuilder.setVipbdate((int)(this.vipbdate.getTime()/1000));
				}
				if ( this.vipedate != null ) {
					roleInfoBuilder.setVipedate((int)(this.vipedate.getTime()/1000));
				}
			} else {
				roleInfoBuilder.setVipmoney(1);
			}
			int roleAction = RoleActionManager.getInstance().getRoleActionPoint(
					this, System.currentTimeMillis());
			this.roleAction = roleAction;
			roleInfoBuilder.setRoleAction(this.roleTotalAction-roleAction);
			roleInfoBuilder.setRoleTotalAction(this.roleTotalAction);
			roleInfoBuilder.setRoleArrange(0);
			LevelPojo level = LevelManager.getInstance().getLevel(this.level);
			roleInfoBuilder.setRoleMaxExp(level.getExp());
			roleInfoBuilder.setUserid(this.get_id().toString());
			Account account = this.getAccount();
			if ( account != null && account.getEmail() != null ) {
				roleInfoBuilder.setEmail(account.getEmail());
			}
			if ( this.guild != null ) {
				roleInfoBuilder.setGuildName(this.guild.getTitle());
			}
			if ( this.guildMember != null ) {
				roleInfoBuilder.setGuildRole(this.guildMember.getRole().getTitle());
			}
			return roleInfoBuilder.build();
		} catch (Exception e) {
			logger.warn("toBseRoleInfo fail");
			if ( logger.isDebugEnabled() ) {
				logger.debug(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * It is a facility method to quickly convert User object to 
	 * protobuf object.
	 * 
	 * @return
	 */
	public BseRoleBattleInfo toBseRoleBattleInfo() {
		return toBseRoleBattleInfo(true);
	}
	
	/**
	 * It is a facility method to quickly convert User object to 
	 * protobuf object.
	 * 
	 * @return
	 */
	public BseRoleBattleInfo toBseRoleBattleInfo(boolean sendBag) {
		try {
			BseRoleBattleInfo.Builder roleInfoBuilder = BseRoleBattleInfo.newBuilder();
			roleInfoBuilder.setRoleAttack(this.getAttack());
			roleInfoBuilder.setRoleDefend(this.getDefend());
			roleInfoBuilder.setRoleAgility(this.getAgility());
			roleInfoBuilder.setRoleLuck(this.getLuck());
			roleInfoBuilder.setRoleBlood(this.getBlood());
			roleInfoBuilder.setRoleThew(this.getTkew());
			roleInfoBuilder.setRoleDamage(this.getDamage());
			roleInfoBuilder.setRoleSkin(this.getSkin());
			roleInfoBuilder.setRolePower(this.getPower());
			List<BuffToolType> tools = this.getTools();
			int toolSize = tools.size();
			for ( int i=0; i<toolSize; i++ ) {
				BuffToolType tool = tools.get(i);
				if ( tool != null ) {
					roleInfoBuilder.addTools(tool.id());
				} else {
					roleInfoBuilder.addTools(0);
				}
			}
			if ( valueMap.size() > 0 ) {
				int attack = getValueMapFieldValue(PropDataEnhanceField.ATTACK);
				if ( attack > 0 ) roleInfoBuilder.setGuildAttack(attack);
				int defend = getValueMapFieldValue(PropDataEnhanceField.DEFEND);
				if ( defend > 0 ) roleInfoBuilder.setGuildDefend(defend);
				int agility = getValueMapFieldValue(PropDataEnhanceField.AGILITY);
				if ( agility > 0 ) roleInfoBuilder.setGuildAgility(agility);
				int lucky = getValueMapFieldValue(PropDataEnhanceField.LUCKY);
				if ( lucky > 0 ) roleInfoBuilder.setGuildLuck(lucky);
				int blood = getValueMapFieldValue(PropDataEnhanceField.BLOOD);
				if ( blood > 0 ) roleInfoBuilder.setGuildBlood(blood);
				int tkew = getTkewAdded();
				if ( tkew > 0 ) roleInfoBuilder.setGuildThew(tkew);
				int power = getPowerAdded();
				if ( power > 0 ) roleInfoBuilder.setGuildPower(power);
			}
			
			if ( sendBag ) {
				Bag bag = this.getBag();
				List<PropData> wearList = bag.getWearPropDatas();
				for ( PropData propData : wearList ) {
					if ( propData != null ) {
						roleInfoBuilder.addRoleBagInfo(propData.toXinqiPropData(this));
					}
				}
				List<PropData> otherList = bag.getOtherPropDatas();
				for ( PropData propData : otherList ) {
					if ( propData != null ) {
						logger.debug("User {} bag pew:{}, propData: {}", new Object[]{
								this.roleName, propData.getPew(), propData.getName()});
						roleInfoBuilder.addRoleBagInfo(propData.toXinqiPropData(this));
					}
				}
			}
			return roleInfoBuilder.build();
		} catch (Exception e) {
			logger.warn("BseRoleBattleInfo fail {}", e.getMessage());
			if ( logger.isDebugEnabled() ) {
				logger.debug(e.getMessage(), e);
			}
		}
		return null;
	}
	
	/**
	 * Make the RoleInfo according to User data when battle begin.
	 * Note: filter out those expired propData.
	 * @param user
	 * @return
	 */
	public RoleInfo toRoleInfo(SessionKey battleRoomSessionKey, 
			int campId, int roomIndex, Point startPoint) {
		
		RoleInfo.Builder builder = RoleInfo.newBuilder();
		builder.setCampId(campId);
		SessionKey userSessionKey = GameContext.getInstance().findSessionKeyByUserId(_id);
		if ( isProxy() ) {
			SessionKey proxySessionKey = GameContext.getInstance().getSessionManager().findSessionKeyByProxyUserId(_id);
			if ( proxySessionKey != null ) {
				builder.setSessionId(proxySessionKey.toString());	
			} else {
				builder.setSessionId(Constant.EMPTY);
			}
		} else {
			if ( userSessionKey == null ) {
				builder.setSessionId(Constant.EMPTY);
			} else {
				builder.setSessionId(userSessionKey.toString());
			}
		}
		builder.setUserId(_id.toString());
		String roleName = UserManager.getDisplayRoleName(this.roleName);
		builder.setUserName(roleName);
		builder.setGender(this.getGender().ordinal());
		builder.setLevel(this.getLevel());
		builder.setExp(this.getExp());
		builder.setWinOdds(this.getWinOdds());
		builder.setWins(this.getWins());
		builder.setRoomIdx(roomIndex);
		//TODO moveSpeed?
		builder.setMoveSpeed(2);
		builder.setTool1(0);
		builder.setTool2(0);
		builder.setTool3(0);
		List<BuffToolType> tools = this.getTools();
		if ( tools.size() >= 3 ) {
			if ( tools.get(0) != null ) {
				builder.setTool1(tools.get(0).id());
			}
			if ( tools.get(1) != null ) {
				builder.setTool1(tools.get(1).id());
			}
			if ( tools.get(2) != null ) {
				builder.setTool1(tools.get(2).id());
			}
		} else
		if ( tools.size() >= 2 ) {
			if ( tools.get(0) != null ) {
				builder.setTool1(tools.get(0).id());
			}
			if ( tools.get(1) != null ) {
				builder.setTool2(tools.get(1).id());
			}
		} else
		if ( tools.size() >= 1 ) {
			if ( tools.get(0) != null ) {
				builder.setTool1(tools.get(0).id());
			}
		}
		builder.setAttack(this.getAttack());
		builder.setDefend(this.getDefend());
		builder.setAgility(this.getAgility());
		builder.setLuck(this.getLuck());
		builder.setBlood(this.getBlood());
		/**
		 * Note: the Tkew should be displayed 
		 * the total value
		 */
		builder.setThew(this.getTkew());
		builder.setDamage(this.getDamage());
		builder.setSkin(this.getSkin());
		builder.setPower(this.getPower());
		
		if ( valueMap.size() > 0 ) {
			int attack = getValueMapFieldValue(PropDataEnhanceField.ATTACK);
			if ( attack > 0 ) builder.setGuildAttack(attack);
			int defend = getValueMapFieldValue(PropDataEnhanceField.DEFEND);
			if ( defend > 0 ) builder.setGuildDefend(defend);
			int agility = getValueMapFieldValue(PropDataEnhanceField.AGILITY);
			if ( agility > 0 ) {
				builder.setGuildAgility(agility);
			}
			int lucky = getValueMapFieldValue(PropDataEnhanceField.LUCKY);
			if ( lucky > 0 ) builder.setGuildLuck(lucky);
			int blood = getValueMapFieldValue(PropDataEnhanceField.BLOOD);
			if ( blood > 0 ) builder.setGuildBlood(blood);
			int tkew = getTkewAdded();
			if ( tkew > 0 ) builder.setGuildThew(tkew);
			int power = getPowerAdded();
			if ( power > 0 ) builder.setGuildPower(power);
		}
		
		Bag bag = this.getBag();
		List<PropData> wearList = bag.getWearPropDatas();
		for ( PropData propData : wearList ) {
			if ( propData != null && !propData.isExpire() ) {
				builder.addEquip(propData.toXinqiPropData(this));
			}
		}
		//Do not need it in combat.
//		List<PropData> otherList = bag.getOtherPropDatas();
//		for ( PropData propData : otherList ) {
//			if ( propData != null ) {
//				builder.addEquip(propData.toXinqiPropData(this.getAbtest()));
//			}
//		}
		
		builder.setHideHat(this.isConfigHideHat());
		builder.setHideGlasses(this.isConfigHideGlass());
		builder.setHideSuit(this.isConfigHideSuite());
		builder.setBattleCount(this.getBattleCount());
		builder.setBattleRoomIdx(roomIndex);
		builder.setYellowDmd(this.isVip());
		builder.setYellowDmdYear(this.isVip());
		builder.setYellowDmdLv(this.getViplevel());
		
		if ( startPoint != null ) {
			builder.setStartPosX(startPoint.x);
			builder.setStartPosY(startPoint.y);
		}
		
	  //遭遇强敌标记
		builder.setLessLv5(false);
		//用户类型：0：玩家 1：机器人，其他：敌人ID
		if ( this.isAI ) {
			builder.setRoleTypeID(1);
		} else {
			builder.setRoleTypeID(0);
		}
		//builder.setGuildID(guildId);
		if ( guild != null ) {
			builder.setGuildName(guild.getTitle());
		}
		return builder.build();
	}
	
	/**
	 * Convert the user's toolbox into BseToolList
	 * @return
	 */
	public BseToolList toBseToolList() {
		//Sync with client weather succeed or not.
		BseToolList.Builder listBuilder = BseToolList.newBuilder();
		List<BuffToolType> tools = this.getTools();
		if ( tools.size() > 0 ) {
			BuffToolType tool = tools.get(0);
			if ( tool != null ) {
				listBuilder.setTool1(tool.id());
			} else {
				listBuilder.setTool1(0);
			}
		} else {
			listBuilder.setTool1(0);
		}
		if ( tools.size() > 1 ) {
			BuffToolType tool = tools.get(1);
			if ( tool != null ) {
				listBuilder.setTool2(tool.id());
			} else {
				listBuilder.setTool2(0);
			}
		} else {
			listBuilder.setTool2(0);
		}
		if ( tools.size() > 2 ) {
			BuffToolType tool = tools.get(2);
			if ( tool != null ) {
				listBuilder.setTool3(tool.id());
			} else {
				listBuilder.setTool3(0);
			}
		} else {
			listBuilder.setTool3(0);
		}
		return listBuilder.build();
	}
	
	/**
	 * To Protocol Buffer's UserData
	 * @return
	 */
	public UserData toUserData() {
		UserData.Builder builder = UserData.newBuilder();
		builder.setRoleLevel(this.level);
		int maxExp = ScriptManager.getInstance().
				runScriptForInt(ScriptHook.NEXT_LEVEL_EXP, this.level);
		builder.setRoleMaxExp(maxExp);
		builder.setRoleAttack(this.attack);
		builder.setRoleDefend(this.defend);
		builder.setRoleAgility(this.agility);
		builder.setRoleLuck(this.luck);
		builder.setRoleBlood(this.blood);
		builder.setRoleThew(this.tkew);
		builder.setRoleDamage(this.damage);
		builder.setRoleSkin(this.skin);
		builder.setRolePower(this.power);
		builder.setWinodds(this.winOdds);
		builder.setHideGlasses(this.configHideGlass);
		builder.setHideHat(this.configHideHat);
		builder.setHideSuit(this.configHideSuite);
		if ( this.isvip ) {
			builder.setViplevel(this.viplevel);
		} else {
			builder.setViplevel(0);
		}
		return builder.build();
	}
	
	/**
	 * To Protocol Buffer's UserExData
	 * @return
	 */
	public UserExData toUserExData() {
		UserExData.Builder builder = UserExData.newBuilder();
	  //角色经验值
    builder.setRoleExp(this.exp);
    //角色拥有的金币（游戏币）
    builder.setRoleGold(this.golden);
    //角色拥有的货币（充值币）
    builder.setRoleCoin(this.yuanbao);
    //角色排名
    RankUser rankUser = RankManager.getInstance().getCurrentRankUser(
    		this, RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
    builder.setRoleArrange(rankUser.getRank());
    //角色胜率
    builder.setRoleWinOdds(this.winOdds);
    //角色胜场
    builder.setRoleWins(this.wins);
    //角色状态标志位： 1 - 可领取当日奖励
    builder.setSign(0);
    //Send VIP data
    builder.setPoint(this.viplevel);
    //绑定元宝
    builder.setRoleBindCoin(0);
    //
    builder.setIsMoneyAdd(false);
    //攻击模式：0：键盘 1：鼠标
    builder.setShootMode(0);
		return builder.build();
	}
	
	/**
	 * 
	 * @return
	 */
	public FriendInfoLite toFriendInfoLite(RelationType type) {
		FriendInfoLite.Builder builder = FriendInfoLite.newBuilder();
		builder.setUid( this._id.toString() );
		builder.setNickName( this.username );
		builder.setLevel(this.level);
		SessionKey sessionKey = GameContext.getInstance().findSessionKeyByUserId(_id);
		builder.setOnline(sessionKey != null);
		builder.setWinOdds(this.winOdds);
		builder.setOpenid(Constant.EMPTY);
		if ( this.iconurl != null ) {
			builder.setHeadurl(this.iconurl);
		} else {
			builder.setHeadurl(Constant.EMPTY);
		}
		builder.setFriendtype(type.ordinal());
		builder.setPower(this.power);
		builder.setIsYellowDmd(this.isvip);
		builder.setYellowDmdLvl(this.viplevel);
		
		return builder.build();
	}
	
	// ------------------------------------------- Methods
	/**
	 * 
	 * Clear all fields modified flag after saving.
	 */
	public UserChangeFlag[] clearModifiedFlag() {
		UserChangeFlag[] flags = null;
		if ( USE_CHANGE_FLAG ) {
			synchronized (changeFields) {
				flags = new UserChangeFlag[changeFields.size()];
				changeFields.toArray(flags);
				changeFields.clear();
			}
		}
		return flags;
	}
	
	/**
	 * User's location
	 * @author wangqi
	 *
	 */
	public static class Location {
		public int x;
		public int y;

		@Override
		public int hashCode() {
			return Constant.BEST_PRIME*Constant.BEST_PRIME+Constant.BEST_PRIME*x+y;
		}
		@Override
		public boolean equals(Object other) {
			if ( this == other ) {
				return true;
			}
			if ( other == null || this.getClass() != other.getClass() ) {
				return false;
			}
			Location otherLoc = (Location)other;
			return this.x == otherLoc.x && this.y == otherLoc.y;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((abtest == null) ? 0 : abtest.hashCode());
		result = prime * result + agility;
		result = prime * result + attack;
		result = prime * result + ((bag == null) ? 0 : bag.hashCode());
		result = prime * result + battleCount;
		result = prime * result + blood;
		result = prime * result + ((cdate == null) ? 0 : cdate.hashCode());
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result + (configEffectSwitch ? 1231 : 1237);
		result = prime * result + configEffectVolume;
		result = prime * result + (configHideGlass ? 1231 : 1237);
		result = prime * result + (configHideHat ? 1231 : 1237);
		result = prime * result + (configHideSuite ? 1231 : 1237);
		result = prime * result + (configLeadFinish ? 1231 : 1237);
		result = prime * result + (configMusicSwitch ? 1231 : 1237);
		result = prime * result + configMusicVolume;
		result = prime * result + continuLoginTimes;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + damage;
		result = prime * result + defend;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + exp;
		result = prime * result + failcount;
		result = prime * result + gender.ordinal();
		result = prime * result + golden;
		result = prime * result + ((iconurl == null) ? 0 : iconurl.hashCode());
		result = prime * result + (isvip ? 1231 : 1237);
		result = prime * result + ((ldate == null) ? 0 : ldate.hashCode());
		result = prime * result + level;
		result = prime * result + ((loc == null) ? 0 : loc.hashCode());
		result = prime * result + luck;
		result = prime * result + medal;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + power;
		result = prime * result + ((relations == null) ? 0 : relations.hashCode());
		result = prime * result + remainLotteryTimes;
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
		result = prime * result + skin;
		result = prime * result + tkew;
		result = prime * result + ((tools == null) ? 0 : tools.hashCode());
		result = prime * result + totalmin;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((vipbdate == null) ? 0 : vipbdate.hashCode());
		result = prime * result + ((vipedate == null) ? 0 : vipedate.hashCode());
		result = prime * result + vipexp;
		result = prime * result + viplevel;
		result = prime * result + voucher;
		result = prime * result + winOdds;
		result = prime * result + wins;
		result = prime * result + yuanbao;
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
		User other = (User) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (abtest == null) {
			if (other.abtest != null)
				return false;
		} else if (!abtest.equals(other.abtest))
			return false;
		if (agility != other.agility)
			return false;
		if (attack != other.attack)
			return false;
		if (bag == null) {
			if (other.bag != null)
				return false;
		} else if (!bag.equals(other.bag))
			return false;
		if (battleCount != other.battleCount)
			return false;
		if (blood != other.blood)
			return false;
		if (cdate == null) {
			if (other.cdate != null)
				return false;
		} else if (!cdate.equals(other.cdate))
			return false;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (client == null) {
			if (other.client != null)
				return false;
		} else if (!client.equals(other.client))
			return false;
		if (configEffectSwitch != other.configEffectSwitch)
			return false;
		if (configEffectVolume != other.configEffectVolume)
			return false;
		if (configHideGlass != other.configHideGlass)
			return false;
		if (configHideHat != other.configHideHat)
			return false;
		if (configHideSuite != other.configHideSuite)
			return false;
		if (configLeadFinish != other.configLeadFinish)
			return false;
		if (configMusicSwitch != other.configMusicSwitch)
			return false;
		if (configMusicVolume != other.configMusicVolume)
			return false;
		if (continuLoginTimes != other.continuLoginTimes)
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (damage != other.damage)
			return false;
		if (defend != other.defend)
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (exp != other.exp)
			return false;
		if (failcount != other.failcount)
			return false;
		if (gender != other.gender)
			return false;
		if (golden != other.golden)
			return false;
		if (iconurl == null) {
			if (other.iconurl != null)
				return false;
		} else if (!iconurl.equals(other.iconurl))
			return false;
		if (isvip != other.isvip)
			return false;
		if (ldate == null) {
			if (other.ldate != null)
				return false;
		} else if (!ldate.equals(other.ldate))
			return false;
		if (level != other.level)
			return false;
		if (loc == null) {
			if (other.loc != null)
				return false;
		} else if (!loc.equals(other.loc))
			return false;
		if (luck != other.luck)
			return false;
		if (medal != other.medal)
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (power != other.power)
			return false;
		if (relations == null) {
			if (other.relations != null)
				return false;
		} else if (!relations.equals(other.relations))
			return false;
		if (remainLotteryTimes != other.remainLotteryTimes)
			return false;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		if (skin != other.skin)
			return false;
		if (tkew != other.tkew)
			return false;
		if (tools == null) {
			if (other.tools != null)
				return false;
		} else if (!tools.equals(other.tools))
			return false;
		if (totalmin != other.totalmin)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (vipbdate == null) {
			if (other.vipbdate != null)
				return false;
		} else if (!vipbdate.equals(other.vipbdate))
			return false;
		if (vipedate == null) {
			if (other.vipedate != null)
				return false;
		} else if (!vipedate.equals(other.vipedate))
			return false;
		if (vipexp != other.vipexp)
			return false;
		if (viplevel != other.viplevel)
			return false;
		if (voucher != other.voucher)
			return false;
		if (winOdds != other.winOdds)
			return false;
		if (wins != other.wins)
			return false;
		if (yuanbao != other.yuanbao)
			return false;
		return true;
	}
	
	@Override
	public User clone() {
		User user = new User();
		
		return user;
	}
}
