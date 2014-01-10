package com.xinqihd.sns.gameserver.config;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface Constant {
	
	/**
	 * If the I18N_ENABLE is true,
	 * then the server will detect language setting of clients 
	 * and return localized messages. It will cost a little more
	 * resources than not using it.
	 */
	boolean I18N_ENABLE = true;
	
	String DEFAULT_LOCALE = "zh_CN";
	
	Charset charset = Charset.forName("utf8");
	
	String SPACE = " ";
	String QUOTE = "\"";
	String EQUAL = "=";
	String COMMA = ",";
	String BRACE_B = "{";
	String BRACE_E = "}";
	String NEWLINE = "\n";
	String EMPTY = "";
	String UNDERLINE = "_";
	String DASH = "-";
	String TAB = "\t";
	String QUESTION = "?";
	String COLON = ":";
	String DOT = ".";
	String STAR = "*";
	
	//MapPojo
	String MAP = "map";
	String ID = "id";
	String NAME = "name";
	String TYPE = "type";
	String REQLV = "reqlv";
	String SCROLLAREA = "scrollArea";
	String LAYERNUM = "layerNum";
	String LAYERS = "layers";
	String LAYER = "layer";
	String BGM = "bgm";
	String DAMAGE = "damage";
	String BOSSID = "bossid";
	String BOSS = "boss";
	String ENEMYID = "enemyid";
	String ENEMY = "enemy";
	String BOSSPOINT = "bossPoints";
	String POINT = "point";
	String ENEMYPOINT = "enemyPoints";
	String STARTPOINT = "startPoints";
	String LEVEL = "level";
	String ITEMS = "items";
	String DESC = "desc";
	String DAY_NUM = "daynum";
	String STEP = "step";
	String NUMBER = "number";
	String DAILYMARK_REWARD = "dailymark_reward";
	
	//ShopPojo
	String SHOP = "shop";
	String INFO = "info";
	String TYPEID = "typeid";
	String PROP_INFO = "prop_info";
	String GOLD_TYPE = "gold_type";
	String BUYPRICE = "buyprice";
	String PRICE = "price";
	String TIMES = "times";
	String BAND = "band";
	String DISCOUNT = "discount";
	String SELL = "sell";
	String HOT = "hot";
	String SHOP_ID = "shop_id";
	String LIMIT_COUNT = "limit_count";
	String LIMIT_GROUP = "limit_group";
	
	//EquimentPojo
	String AVATAR="avatar";
	String BUBBLE="bubble";
	String BUBBLES="bubbles";
	String CLOTHES="clothes";
	String DECORATION="decoration";
	String DECORATIONS="decorations";
	String EQUIPMENT="equipment";
	String EXPRESSION="expression";
	String EXPRESSIONS="expressions";
	String FACE="face";
	String FACES="faces";
	String GLASSES="glasses";
	String HAIR="hair";
	String HAIRS="hairs";
	String HAT="hat";
	String HATS="hats";
	String INDEX="index";
	String JEWELRIES="jewelries";
	String JEWELRY="jewelry";
	String OFFHANDWEAPON="offhandweapon";
	String OFFHANDWEAPONS="offhandweapons";
	String PART="part";
	String SUIT="suit";
	String SUITS="suits";
	String WEAPON="weapon";
	String WEAPONS="weapons";
	String WING="wing";
	String WINGS="wings";
	String ADD_AGILITY="add_agility";
	String ADD_ATTACK="add_attack";
	String ADD_BLOOD="add_blood";
	String ADD_DAMAGE="add_damage";
	String ADD_DEFEND="add_defend";
	String ADD_LUCK="add_luck";
	String ADD_SKIN="add_skin";
	String ADD_THEW="add_thew";
	String AUTODESTORY="autoDestory";
	String AUTODIRECTION="autoDirection";
	String BLOOD_PERCENT="blood_percent";
	String BULLET="bullet";
	String EQUIP_TYPE="equip_type";
	String EXPBLEND="expBlend";
	String EXPSE="expSe";
	String ICON="icon";
	String INDATE1="indate1";
	String INDATE2="indate2";
	String INDATE3="indate3";
	String LV="lv";
	String POWER="power";
	String QUALITY="quality";
	String RADIUS="radius";
	String SAUTODIRECTION="sAutoDirection";
	String SRADIUS="sRadius";
	String S_NAME="s_name";
	String SEX="sex";
	String SIGN="sign";
	String SLOT="slot";
	String SPECIALACTION="specialAction";
	String UNUSED1="unused1";
	String UNUSED2="unused2";
	String UNUSED3="unused3";
	String OTHER = "other";
	
	String X_A = "x";
	String Y_A = "y";
	String WIDTH_A = "width";
	String HEIGHT_A = "height";
	String W_A = "w";
	String H_A = "h";
	String NUM_A = "num";
	String SCROLLRATE_A = "scrollRate";
	
	String SEQ = "seq";
	String USER_LEVEL = "userLevel";
	String SCRIPT = "script";
	String Q = "q";
	
	String ENC_UTF8 = "utf-8";
	String ENC_GBK = "GBK";
	String PATH_SEP = "/";
	
	//For the http last-modified header: Wed, 08 Jan 2003 23:11:55 GMT
	SimpleDateFormat HTTP_LM = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	String HEAD = "HEAD";
	String HTTP = " HTTP";
	
	//Cache byte array size
	int LUA_CACHE_SIZE = 2048;
	int CACHE_HTTP_SIZE = 80000;
	int ZOO_CACHE_SIZE = 2048000;
	int ZOO_BIG_CACHE_SIZE = 512000;
	//Zoo Keeper root path
	String ZOO_MAP_ROOT = "/map_config.xml";
	String ZOO_SHOP_ROOT = "/shop_multi_currency.dat";
	String ZOO_EQUIP_ROOT = "/equipment_config.xml";
	String ZOO_ITEMS_ROOT = "/item_config.xml";
	String ZOO_TASK_ROOT  = "/task_config.xml";
	String ZOO_DAILYMARK = "/dailymark_reward.dat";
	String ZOO_EQUIP = "/equipment_config.xml";
	String ZOO_FARM = "/farm_config.xml";
	String ZOO_ITEM = "/item_config.xml";
	String ZOO_PACK = "/pack_config.xml";
	String ZOO_PATH = "/path_config.xml";
	String ZOO_TASK = "/task_config.xml";
	String ZOO_ABTEST = "/_abtest";
	int    ZOO_PERCENT = 10000;
	String ZOO_DEFAULT = "/_default";
	
	//Items config
	String CONSUMABLE="consumable";
	String ITEM="item";
	String REWARD="reward";

	//task config
	String AWARD="award";
	String CAIFU="caifu";
	String CAIFUTASK="caifutask";
	String COUNT="count";
//	String DESC="desc";
	String ENCODING="encoding";
	String EXP="exp";
	String GOLD="gold";
	String GONGXUN="gongxun";
//	String ID="id";
	String INDATE="indate";
//	String LEVEL="level";
//	String LV="lv";
//	String NAME="name";
	String PARENT="parent";
//	String SEX="sex";
//	String STEP="step";
	String TASK="task";
	String TASKTARGET="taskTarget";
	String TASKLIST="tasklist";
	String TASKS="tasks";
	String TICKET="ticket";
//	String TYPE="type";
	String VERSION="version";

	int DAY_MILLIS = 86400;
	int TIME_OFFSET = 28800;
	int TIME_OFFSET_NEXT = 57600;
	int BEST_PRIME = 92821;
	
	int BAG_WEAR_COUNT = 20;
	int BAG_MAX_COUNT = 70;
	
	String _ID = "_id";
	String LOGIN_USERNAME = "login.username";
	String ONE = "1";
	String ONE_NEGATIVE = "-1";
	
	String LIST = "list";
	String LOCALHOST = "127.0.0.1";
	String HOSTNAME = "hostname";
	
	//Used for storing User object into IoSession.
	String SESSION_KEY = "iosession.sessionkey";
	String USER_KEY = "iosession.user";

	String REDIS_SESSION = "session:";
	
	String RPC_SERVER_KEY = "rpcserverid";
	
	byte[] EMPTY_BYTES = new byte[0];
	
	int CPU_CORES = Runtime.getRuntime().availableProcessors();
		
	//Mongo database operators
	String OP_UNSET = "$unset";
	String OP_SET = "$set";
	String OP_INC = "$inc";
	
	String CLASS = "class";
	
	//Used by ConfigXmlImporter
	int POINT_BOSS = 1;
	int POINT_ENEMY = 2;
	int POINT_START = 3;

	String CATALOGS = "catalogs";
	
	String _BG = "_bg";
	String BG = "bg";
	String _MID = "_mid";
	String MID = "mid";
	String _MID1 = "_mid1";
	String MID1 = "mid1";
	String FLOOR = "floor";
	
	String LV_FOR_STONE="Lv";
	
	String ZERO = "0";
	
	String DOWN = ".down";
	
	int WHOLE_DAY_SECONDS = 86400;
	
	int HALF_DAY_SECONDS = 43200;
	
	int QUARTER_DAY_SECONDS = 21600;
	
	int HALF_HOUR_SECONDS = 1800;
	
	String FORGET_PASS_KEY = "forget:";
	
	//The default password for an AI user.
	String AI_USER_PASSWORD = "2sFaw5WD23vlR*^$";
	
	String ACHIEVEMENT = "achievement";
	
	Set EMPTY_SET = Collections.unmodifiableSet(new HashSet());
	
}
