package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;

import com.google.protobuf.ByteString;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseAchievements.Achievement;
import com.xinqihd.sns.gameserver.proto.XinqiBseAchievements.BseAchievements;
import com.xinqihd.sns.gameserver.proto.XinqiBseAddTask.BseAddTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseDelTask.BseDelTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseTask.BseTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseTaskList;
import com.xinqihd.sns.gameserver.proto.XinqiBseUserAchievements.BseUserAchievements;
import com.xinqihd.sns.gameserver.proto.XinqiBseZip.BseZip;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;
import com.xinqihd.sns.gameserver.task.TaskPostStatus;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.IOUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is used to manage the game's equipment objects.
 * 
 * In Redis, the following data will be stored.
 * 1) task:<userName>:todo     = all task id's set that are going to do. 
 * 2) task:<userName>:finished = all task id's set that are already finished but does not get the reward.
 * 3) task:<userName>:awarded  = all task id's set that are got rewarded.
 * 4) task:<userName>:<taskid>:<field> = the task specific data
 * 
 * @author wangqi
 *
 */
public class TaskManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	
	private static final String COLL_NAME = "tasks";
	
	private static final String INDEX_NAME = "_id";
	
	//The key's prefix used in Redis
	private static final String KEY_PREFIX = "task:";
	private static final String KEY_TODO = ":todo";
	private static final String KEY_FINISHED = ":finished";
	private static final String KEY_AWARDED = ":awarded";
	
	private HashMap<String, TaskPojo> dataMap = 
			new HashMap<String, TaskPojo>();
	
	private EnumMap<TaskType, TreeSet<TaskPojo>> taskListMap = 
			new EnumMap<TaskType, TreeSet<TaskPojo>>(TaskType.class);
	
	private HashMap<Integer, TaskPojo> levelTaskMap = 
			new HashMap<Integer, TaskPojo>();

	private static final TaskManager instance = new TaskManager();
	
	private static final List<TaskPojo> EMPTY_TASK_LIST =
			Collections.unmodifiableList(new ArrayList<TaskPojo>());
	
	private HashMap<Locale, byte[]> compressLuaScriptMap = 
			new HashMap<Locale, byte[]>(); 

	/**
	 * Get the singleton instance
	 * @return
	 */
	public static TaskManager getInstance() {
		return instance;
	}
	
	TaskManager() {
		super(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		synchronized ( dataMap ) {
			dataMap.clear();
			taskListMap.clear();
			levelTaskMap.clear();
			
			for ( Locale locale : GameResourceManager.getInstance().getAllLocales() ) {
				StringBuilder buf = new StringBuilder(10000);
				buf.append("tasks= {\n");
				for ( DBObject obj : list ) {
					TaskPojo task = (TaskPojo)MongoDBUtil.constructObject(obj);
					if ( StringUtil.checkNotEmpty(task.getScript()) ) {
						dataMap.put(task.getId(), task);
						TreeSet<TaskPojo> taskPojos = taskListMap.get(task.getType());
						if ( taskPojos == null ) {
							taskPojos = new TreeSet<TaskPojo>();
							taskListMap.put(task.getType(), taskPojos);
						}
						taskPojos.add(task);
						buf.append(task.toLuaString(locale));
						
						//Store levelup task
						if ( ScriptHook.TASK_USER_LEVELUP.getHook().equals(task.getScript()) ) {
							levelTaskMap.put(task.getUserLevel(), task);
						}
					}
				}
				buf.append("}\n");
				byte[] compressedLuaScript = IOUtil.compressStringZlib(buf.toString());
				compressLuaScriptMap.put(locale, compressedLuaScript);
			}
			
			
			for ( TaskType taskType : TaskType.values() ) {
				TreeSet<TaskPojo> tasks = this.taskListMap.get(taskType);
				if (tasks == null) {
					this.taskListMap.put(taskType, new TreeSet<TaskPojo>());
				}
			}
			logger.debug("Load total {} tasks from database.", dataMap.size());
		}
	}
	
	/**
	 * Get the given task by its id.
	 * @param id
	 * @return
	 */
	public TaskPojo getTaskById(String id) {
		synchronized ( dataMap ) {
			return dataMap.get(id);
		}
	}
	
	/**
	 * Only for test purpose.
	 * @param task
	 */
	public void setTaskById(TaskPojo task) {
		synchronized ( dataMap ) {
			dataMap.put(task.getId(), task);
			TreeSet<TaskPojo> taskPojos = taskListMap.get(task.getType());
			if ( taskPojos == null ) {
				taskPojos = new TreeSet<TaskPojo>();
				taskListMap.put(task.getType(), taskPojos);
			}
			taskPojos.add(task);
		}
	}
	
	/**
	 * Get the underlying task collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<TaskPojo> getTasks() {
		synchronized ( dataMap ) {
			return dataMap.values();
		}
	}
	
	/**
	 * Assign new tasks according to user's level 
	 * If found such a task, check if it exist in todo, finished or awarded set in Redis.
	 * If it exists, return null. Otherwise return the new task and store task id in todo set.
	 * 
	 * @param type
	 * @param user
	 * @return
	 */
	public Collection<TaskPojo> assignNewTask(TaskType type, User user) {
		//Add a new task if available
		int diff = getUserMaxTaskCount(type, user);
		if ( diff > 0 ) {
			logger.debug("task type {} has {} tasks for user", type, diff);
			return this.assignNewTask(type, user, diff, true);
		}
		return null;
	}
	
	/**
	 * Get the new tasks list.
	 * 
	 * @param type
	 * @param user
	 * @param maxCount
	 * @return
	 */
	public List<TaskPojo> assignNewTask(TaskType type, User user, 
			int maxCount) {
		return this.assignNewTask(type, user, maxCount, true);
	}
	
	/**
	 * Assign new tasks according to user's level 
	 * If found such a task, check if it exist in todo, finished or awarded set in Redis.
	 * If it exists, return null. Otherwise return the new task and store task id in todo set.
	 * 
	 * @param seq
	 * @param userLevel
	 * @return
	 */
	public List<TaskPojo> assignNewTask(TaskType type, User user, 
			int maxCount, boolean sendAddTask) {
		int userLevel = user.getLevel();
		//Get the persistent Jedis instance
		Jedis jedisDB = JedisFactory.getJedisDB();
		int count = 0;
				
		TreeSet<TaskPojo> treeSet = this.getTasksForType(type);
		if ( treeSet != null && treeSet.size() > 0 ) {
			String todoSetKey = getTodoSetName(user);
			Set<String> todoSet = jedisDB.smembers(todoSetKey);
			String finishedKey = getFinishedSetName(user);
			Set<String> finishSet = jedisDB.smembers(finishedKey);
			String awardedKey = getAwardedSetName(user);
			Set<String> awardSet = jedisDB.smembers(awardedKey);

			ArrayList<TaskPojo> tasks = new ArrayList<TaskPojo>(10);
			//Try to get all level up tasks first.
			if ( TaskType.TASK_MAIN == type ) {
				Collection<TaskPojo> levelUpTasks = acquireUserLevelUpTasks(user, 
						todoSet, finishSet, awardSet);
				if ( levelUpTasks.size()>0 ) {
					int size = Math.min(levelUpTasks.size(), maxCount);
					Iterator<TaskPojo> iter = levelUpTasks.iterator();
					for ( int i=0; i<size; i++ ) {
						TaskPojo task = iter.next();
						tasks.add(task);
					  //Store the current sequence
						jedisDB.sadd(todoSetKey, task.getId());
						count++;
					}
				}
			}
			
			SortedSet<TaskPojo> taskSet = null;
			TaskPojo query = new TaskPojo();
			query.setUserLevel(userLevel);
			query.setSeq(Integer.MAX_VALUE);
			taskSet = treeSet.headSet(query);
			/*
			if ( maxCount < Integer.MAX_VALUE ) {
				if ( count < maxCount ) {
					TaskPojo query = new TaskPojo();
					query.setUserLevel(userLevel);
					query.setSeq(Integer.MAX_VALUE);
					
					taskSet = treeSet.headSet(query);
				}	
			} else {
				taskSet = treeSet;
			}
			*/
			
			if ( taskSet != null && taskSet.size() > 0 ) {
				Set<String> todoScriptType = new HashSet<String>();
				for ( String taskId : todoSet ) {
					TaskPojo task = TaskManager.getInstance().getTaskById(taskId);
					//The userLevelUp task can be duplicated in user's todo task.
					if ( task != null && 
							task.getType() != TaskType.TASK_ACHIVEMENT && 
							task.getType() != TaskType.TASK_ACTIVITY &&
							!ScriptHook.TASK_USER_LEVELUP.getHook().equals(task.getScript()) ) {
						todoScriptType.add(task.getScript());
					}
				}

				for ( TaskPojo task : taskSet ) {
					if ( task != null ) {
						//Check if the task is already assigned.
						boolean exist = todoSet.contains(task.getId());
						if ( !exist ) {
							exist = finishSet.contains(task.getId());
							if ( !exist ) {
								exist = awardSet.contains(task.getId()); 
							}
						}
						if ( !exist ) {
							exist = todoScriptType.contains(task.getScript());
							if ( !exist ) {
								//The userLevelUp task can be duplicated in user's todo task.
								if ( task.getType() != TaskType.TASK_ACHIVEMENT && 
										task.getType() != TaskType.TASK_ACTIVITY &&
										!ScriptHook.TASK_USER_LEVELUP.getHook().equals(task.getScript()) ) {
									todoScriptType.add(task.getScript());
								}
							}
						}
						/**
						 * Should filter out same script type task
						 */
						if ( !exist ) {
							if ( count < maxCount ) {
								boolean available = checkTaskAvailable(user, task);
								if ( available ) {
									if ( !tasks.contains(task) ) {
										//Store the current sequence
										jedisDB.sadd(todoSetKey, task.getId());
										//System.out.println(task);
										tasks.add(task);										
									}
								} else {
									jedisDB.srem(todoSetKey, task.getId());
									tasks.remove(task);
								}
								logger.debug("#assignNewTask: Assign new task {} to user {} tasklist. available:{}", 
										new Object[]{task.getName(), user.getRoleName(), available});
								count++;
							}
						}
					}
				}
			}

			if ( sendAddTask ) {
				for ( TaskPojo task : tasks ) {
					//Notify client new tasks is coming
					int newId = StringUtil.toInt(task.getId(), -1);
					BseAddTask addTask = BseAddTask.newBuilder().setTaskID(newId).build();	
					GameContext.getInstance().writeResponse(user.getSessionKey(), addTask);
				}
			}
			
			user.addTasks(tasks);
			
			/**
			 * I moved this code from here to BceLoginHandler because 
			 * it causes the ModiTask sent before TaskList.
			 * wangqi 2012-5-10
			 */
		  //Check if the "script.task.UserLevelUp" is finished
			//Because sometimes user may level up more than one levels.
			//Call script to trigger task
			//TaskManager.getInstance().processUserTasks(user, TaskHook.USER_UPGRADE);
			
			return tasks;
		} else {
			return EMPTY_TASK_LIST;
		}
	}
	
	/**
	 * Check if the task is available for this given user.
	 * @param user
	 * @param task
	 * @return
	 */
	public boolean checkTaskAvailable(User user, TaskPojo task) {
		boolean available = true;
		if ( !task.isDisable() ) {
			available = true;
		} else {
			available = false;
		}
		if ( available ) {
			/**
			 * Check the min level condition 
			 */
			if ( task.getMinUserLevel() > 0 ) {
				if ( user.getLevel() >= task.getMinUserLevel() ) {
					available = true;	
				} else {
					available = false;
				}
			} else {
				available = true;
			}
			/**
			 * Check the task date & time setting here
			 * 2012-12-11
			 */
			long endMillis = task.getEndMillis();
			long currentMillis = System.currentTimeMillis();
			if ( endMillis == 0 ) {
				available = true;
			} else if ( currentMillis > endMillis ) {
				available = false;
			}
			if ( available ) {
				/**
				 * Check the channel setting here
				 * 2012-12-11
				 */
				String channel = task.getChannel();
				if ( channel != null && user.getChannel() != null) {
					if ( user.getChannel().contains(channel) ) {
						available = true;
					} else {
						available = false;
					}
				} else {
					available = true;
				}
				if ( available ) {
					/**
					 * Check the serverId setting here
					 * 2013-01-10
					 */
					String serverId = task.getServerId();
					if ( serverId != null && user.getServerPojo() != null) {
						Pattern pattern = Pattern.compile(serverId);
						Matcher matcher = pattern.matcher(user.getServerPojo().getId());
						if ( matcher.find() ) {
							available = true;
						} else {
							available = false;
						}
					} else {
						available = true;
					}
				}
			}
		}
		return available;
	}
	
	/**
	 * When an user login, the system will query and return all tasks that are in 
	 * todo list and in finished list.
	 * 
	 * @param user
	 * @return
	 */
	public Collection<TaskPojo> getUserLoginTasks(User user) {
		String lastDateStr = DateUtil.formatDate(user.getTdate());
		String todayStr = DateUtil.getToday(System.currentTimeMillis());
		if ( lastDateStr == null || !lastDateStr.equals(todayStr) ) {
			logger.debug("Refresh user {}'s daily tasks when login.", user.getRoleName());
			GameContext.getInstance().getTaskManager().refreshDailyTask(user);
			user.setTdate(new Date());
			UserManager.getInstance().saveUser(user, false);
		}
		int taskMaxCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_TASK_NUMBER, 5);
		
		//Get all pending task list.
		Collection<TaskPojo> finishedTaskList = 
				GameContext.getInstance().getTaskManager().acquireFinishedTasks(user);
		user.addTaskFinishedCollection(finishedTaskList);
		
		Collection<TaskPojo> todoTaskList = 
				GameContext.getInstance().getTaskManager().acquireTodoTasks(user);
		user.addTasks(todoTaskList);
		
		for ( TaskType type : TaskType.values() ) {
			Set<TaskPojo> typeTasks = user.getTasks(type);
			Set<TaskPojo> typeFinishedTasks = user.getTaskFinished(type);
			if ( type == TaskType.TASK_ACHIVEMENT || type == TaskType.TASK_RANDOM || 
					type == TaskType.TASK_ACTIVITY) {
				GameContext.getInstance().getTaskManager().
						assignNewTask(type, user, Integer.MAX_VALUE, false);
			} else {
				if ( type == TaskType.TASK_DAILY && user.getGuild() != null ) {
					/**
					 * 公会的活动任务全部显示
					 */
					GameContext.getInstance().getTaskManager().
					assignNewTask(type, user, Integer.MAX_VALUE, false);
				} else {
					int currentCount = typeTasks.size() + typeFinishedTasks.size();
					int diff = taskMaxCount - currentCount;
					if ( diff > 0 ) {
						GameContext.getInstance().getTaskManager().
							assignNewTask(type, user, diff, false);					
					}
					logger.debug("task type {} has {} tasks for user", type, diff);
				}
			}
		}

		logger.debug("Acquire total {} task for user {}", user.getTasks().size(), 
				user.getRoleName());

		Set<TaskPojo> actTasks = user.getTasks(TaskType.TASK_ACTIVITY);
		Set<TaskPojo> delTasks = new HashSet<TaskPojo>();
		ServerPojo serverPojo = user.getServerPojo();
		if ( serverPojo != null ) {
			String serverId = serverPojo.getId();
			for ( TaskPojo task : actTasks ) {
				if ( StringUtil.checkNotEmpty(task.getServerId()) ) {
					Pattern pattern = Pattern.compile(task.getServerId());
					Matcher matcher = pattern.matcher(serverId);
					if ( !matcher.find() ) {
						delTasks.add(task);
					}
				}
			}
		}
		if ( delTasks.size() > 0 ) {
			actTasks.removeAll(delTasks);
			user.getTasks().removeAll(delTasks);
		}
		/*
		ArrayList<TaskPojo> alltasks = new ArrayList<TaskPojo>(maxCount);
		if ( maxCount < user.getTasks().size() ) {
			Iterator<TaskPojo> iter = user.getTasks().iterator();
			for ( int i=0; i<maxCount; i++ ) {
				alltasks.add(iter.next());
			}
		} else {
			alltasks.addAll(user.getTasks());
		}
		*/
		return user.getTasks();
	}
	
	/**
	 * Get all the user's already unlocked achievements.
	 * @param user
	 * @return
	 */
	public ArrayList<String> getAllUnlockedAchievements(User user) {
	  //获取所有已经完成的成就
    String awardedSetName = TaskManager.getInstance().getAwardedSetName(user);
    Jedis jedisDB = JedisFactory.getJedisDB();
    Set<String> awardedSet = jedisDB.smembers(awardedSetName);
    ArrayList<String> list = new ArrayList<String>();
    for ( String id : awardedSet ) {
    	TaskPojo task = this.getTaskById(id);
    	if ( task != null && task.getType() == TaskType.TASK_ACHIVEMENT ) {
    		list.add(id);
    	}
    }
    return list;
	}
	
	/**
	 * Remove last day's daily task from todo, finished and awarded set.
	 * Clean all task specific data.
	 * 
	 * @param user
	 * @return
	 */
	public boolean refreshDailyTask(User user) {
		SortedSet<TaskPojo> dailyTasks = this.getTasksForType(TaskType.TASK_DAILY);
		SortedSet<TaskPojo> actTasks = this.getTasksForType(TaskType.TASK_ACTIVITY);
		SortedSet<TaskPojo> tasks = new TreeSet<TaskPojo>();
		tasks.addAll(dailyTasks);
		for ( TaskPojo task : actTasks ) { 
			if ( task.isDaily() ) {
				tasks.add(task);
			}
		}
		String todoSet = getTodoSetName(user);
		String finishedSet = getFinishedSetName(user);
		String awardedSet = getAwardedSetName(user);

		Jedis jedisDB = JedisFactory.getJedisDB();
		Pipeline pipeline = null;
		try {
			pipeline = jedisDB.pipelined();
			String stepKey = this.getTaskDataKey(user, Field.STEP);
			String dataKey = this.getTaskDataKey(user, Field.DATA);
			for ( TaskPojo task : tasks ) {
				pipeline.srem(todoSet, task.getId());
				pipeline.srem(finishedSet, task.getId());
				pipeline.srem(awardedSet, task.getId());
				
				//deleteTaskData(user, task.getId());
				pipeline.hdel(stepKey, task.getId());
				pipeline.hdel(dataKey, task.getId());
			}
			return true;
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			logger.warn("Failed to refresh daily tasks: {}", e.getMessage());
			return false;
		} finally {
			if ( pipeline != null ) {
				pipeline.sync();
			}
		}
	}
	
	/**
	 * Store specific data for a given user and task. Like the task's step.
	 * 
	 * @param user
	 * @param taskId
	 * @param data
	 * @return
	 */
	public boolean storeTaskSpecificData(User user, String taskId, Field field, Object data) {
		boolean result = true;
		try {
			String taskKey = getTaskDataKey(user, field);
			Jedis jedisDB = JedisFactory.getJedisDB();
			jedisDB.hset(taskKey, taskId, data.toString());
		} catch ( Throwable t ) {
			result = false;
			logger.debug(t.getMessage(), t);
			logger.warn("Failed to storeTaskSpecificData. {}", t.getMessage());
		}
		return result;
	}
	
	/**
	 * Get the task specific data for a given user and task.
	 * @param user
	 * @param taskId
	 * @return
	 */
	public String queryTaskSpecificData(User user, String taskId, Field field) {
		String data = null;
		try {
			String taskKey = getTaskDataKey(user, field);
			Jedis jedisDB = JedisFactory.getJedisDB();
			data = jedisDB.hget(taskKey, taskId);
		} catch ( Throwable t ) {
			logger.debug(t.getMessage(), t);
			logger.warn("Failed to storeTaskSpecificData. {}", t.getMessage());
		}
		return data;
	}
	
	/**
	 * Delete all task specific data.
	 * 
	 * @param user
	 * @param taskId
	 * @return
	 */
	public boolean deleteTaskData(User user, String taskId) {
		boolean result = true;
		try {
			Jedis jedis = JedisFactory.getJedisDB();
			String stepKey = this.getTaskDataKey(user, Field.STEP);
			jedis.hdel(stepKey, taskId);
			String dataKey = this.getTaskDataKey(user, Field.DATA);
			jedis.hdel(dataKey, taskId);
		} catch (Exception e) {
			logger.debug("Failed to delete task data: {}", e.toString());
			result = false;
		}
		return result;
	}
	
	/**
	 * Get given user's all todo tasks and check if there are some tasks 
	 * are finished.
	 * 
	 * @param user
	 * @param parameters
	 */
	public void processUserTasks(User user, TaskHook taskHook, Object ... parameters ) {
		if ( user == null || user.isAI() || user.isProxy() || user.isDefaultUser() ) {
			logger.debug("User is null or user's tasks is null. Ignore tasks processing");
			return;
		}
		//Create a wrapper task list for preventing java.util.ConcurrentModificationException
		//Because the script may delete a finished task from the user.getTasks() list.
		List<TaskPojo> tasks = new ArrayList<TaskPojo>(user.getTasks());
		for ( TaskPojo task : tasks ) {
			String script = task.getScript();
			ScriptHook scriptHook = ScriptHook.getScriptHook(script);
			if ( scriptHook != null ) {
				ScriptManager.getInstance().runScript(scriptHook, user, 
						task, taskHook, parameters);
			} else {
				logger.warn("Cannot find the ScriptHook for script: {}. TaskId:{}", script, task.getId());
			}
		}
	}
	
	/**
	 * Get the task specific data key
	 * @param user
	 * @param task
	 * @return
	 */
	public static final String getTaskDataKey(User user, Field field) {
		return StringUtil.concat(KEY_PREFIX, user.getUsername(), Constant.COLON, field.value());
	}
	
	/**
	 * Get all tasks that are in user's todo set.
	 * @param type
	 * @param user
	 * @return
	 */
	public Collection<TaskPojo> acquireTodoTasks(User user) {
		Collection<TaskPojo> tasks = acquireTasks(user, getTodoSetName(user));
		logger.debug("User's todo task's number is {} ", tasks.size());
		return tasks;
	}
	
	/**
	 * Get all tasks that are already finished by are not taken award.
	 * @param user
	 * @return
	 */
	public Collection<TaskPojo> acquireFinishedTasks(User user) {
		Collection<TaskPojo> tasks = acquireTasks(user, getFinishedSetName(user));
		logger.debug("User's finished task's number is {} ", tasks.size());
		return tasks;
	}
	
	/**
	 * Get all tasks that are already finished by are not taken award.
	 * @param user
	 * @return
	 */
	public Collection<TaskPojo> acquireAwardedTasks(User user, TaskType taskType) {
		//Get the persistent Jedis instance
		String setName = getAwardedSetName(user);
		Jedis jedisDB = JedisFactory.getJedisDB();
		Set<String> taskIdSet = jedisDB.smembers(setName);
		TreeSet<TaskPojo> tasks = new TreeSet<TaskPojo>();
		for ( String taskId : taskIdSet ) {
			TaskPojo taskPojo = this.getTaskById(taskId);
			if ( taskPojo != null && !taskPojo.isDisable() && 
					taskPojo.getType() == taskType ) {
				tasks.add(taskPojo);
				jedisDB.sadd(setName, taskId);
			}
		}
		return tasks;
	}
	
	/**
	 * An user finish a task. The task id will be removed from todo set
	 * and added to finished set.
	 * 
	 * @param user
	 * @param taskId
	 * @return
	 */
	public boolean finishTask(User user, String taskId) {
		//Get the persistent Jedis instance
		boolean result = true;
		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = getTodoSetName(user);
		String finishedSet = getFinishedSetName(user);
		
		//Delete it from todo list and add it to finish list
		Pipeline pipeline = jedisDB.pipelined();
		pipeline.srem(todoSet, taskId);
		pipeline.sadd(finishedSet, taskId);
		pipeline.sync();
		
		//Delete all data related to the task
		deleteTaskData(user, taskId);
				
		//Remove from user's tasklist if exist
		TaskPojo task = this.getTaskById(taskId);
		user.removeTask(task);
		user.addTaskFinished(task);
		
		if ( task.getType() == TaskType.TASK_ACHIVEMENT ) {
			this.takeTaskReward(user, taskId, 0);
			if ( task.isBroadcast() ) {
				String roleName = UserManager.getDisplayRoleName(user.getRoleName());
				String content = Text.text("notice.achievement", roleName, task.getName());
				ChatManager.getInstance().processChatToWorldAsyn(null, content);
			}
		} else {
			boolean success = false;
			if ( task.getType() == TaskType.TASK_ACTIVITY && 
					StringUtil.checkNotEmpty(task.getGiftDesc()) ) {
				success = this.takeTaskReward(user, taskId, 0);
			}
//			if ( success && task.isBroadcast() ) {
//				String roleName = UserManager.getDisplayRoleName(user.getRoleName());
//				String content = Text.text("notice.task", roleName, task.getName());
//				ChatManager.getInstance().processChatToWorldAsyn(null, content);
//			}
		}
//		boolean alreadyFinished = jedisDB.sismember(finishedSet, taskId);
//		if ( alreadyFinished ) {	
//			logger.debug("#finishTask: The task(id:{}) is already finished.", taskId);
//			return false;
//		} else {
//
//		}
		
		//Send the BseModiTask to client
		//Move this block of code the script.task.Step#step() method
		//wangqi 2012-02-09
		/*
		BseModiTask.Builder modiBuilder = BseModiTask.newBuilder();
		modiBuilder.setTaskID(StringUtil.toInt(taskId, 0));
		modiBuilder.setStep(task.getStep());
		XinqiMessage xinqiMsg = new XinqiMessage();
		xinqiMsg.payload = modiBuilder.build();
		
		GameContext.getInstance().writeResponse(user.getSession(), xinqiMsg);
		*/
		
		logger.debug("User {} finish the task(id:{}).", user.getRoleName(), taskId);
		
		return result;
	}
	
	/**
	 * Check if the task's reward is already taken.
	 * @param user
	 * @param taskId
	 * @return
	 */
	public boolean isTaskRewardTaken(User user, String taskId) {
		boolean alreadyTaken = false;
		TaskPojo task = this.getTaskById(taskId);
		//Get the persistent Jedis instance
		Jedis jedisDB = JedisFactory.getJedisDB();
		String awardedSet = getAwardedSetName(user);
				
		alreadyTaken = jedisDB.sismember(awardedSet, taskId);
		return alreadyTaken;
	}
	
	/**
	 * Check if the task's reward is already taken.
	 * @param user
	 * @param taskId
	 * @return
	 */
	public boolean isTaskRewardFinished(User user, String taskId) {
		boolean alreadyTaken = false;
		TaskPojo task = this.getTaskById(taskId);
		//Get the persistent Jedis instance
		Jedis jedisDB = JedisFactory.getJedisDB();
		String finishedSet = getFinishedSetName(user);
				
		alreadyTaken = jedisDB.sismember(finishedSet, taskId);
		return alreadyTaken;
	}
	
	/**
	 * An user takes the reward of an already finished task. The task id should 
	 * be in the Redis finished set.
	 * 
	 * @param taskId
	 * @param choose
	 * @return
	 */
	public boolean takeTaskReward(User user, String taskId, int choose) {
		TaskPojo task = this.getTaskById(taskId);
		//Get the persistent Jedis instance
		Jedis jedisDB = JedisFactory.getJedisDB();
		String finishedSet = getFinishedSetName(user);
		String awardedSet = getAwardedSetName(user);
		
		boolean success = false;
		if ( task == null ) {
			String message = Text.text("task.reward.notfound", taskId);
			SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
			return success;
		} else {
			//Check if the taskId is already in awardedSet
			boolean alreadyTaken = jedisDB.sismember(awardedSet, taskId);
			if ( alreadyTaken ) {
				//Check if the taskId is already in finishedSet
				jedisDB.srem(finishedSet, taskId);
				SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), 
						Text.text("task.reward.taken", task.getName()), 3000);
				logger.warn(" The user {}'s taskid {} is already taken ", user.getRoleName(), taskId);
				success = false;
			} else {
			  //Check if the taskId is already in finishedSet
				Long deleteResult = jedisDB.srem(finishedSet, taskId);
				if ( deleteResult != null && deleteResult.intValue() == 0 ) {
					if ( ScriptHook.TASK_NOSCRIPT.getHook().equals(task.getScript()) ) {
						//空白任务可以直接领取
						finishTask(user, taskId);
						success = true;
					} else {
						String message = Text.text("task.reward.notfinished", task.getName());
						SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
						success = false;
					}
				} else {
					success = true;
				}
				
				if ( success ) {
					//可以领取任务
					/**
					 * Check the task taken date & time
					 * 2012-12-10
					 */
					Calendar startCal = Calendar.getInstance();
					Calendar endCal = Calendar.getInstance();
					int takeBeginHour = task.getTakeBeginHour();
					int takeBeginMin = task.getTakeBeginMin();
					int takeEndHour = task.getTakeEndHour();
					int takeEndMin = task.getTakeEndMin();
					long takeStartMillis = 0l;
					long takeEndMillis = 0l;
					if ( takeBeginHour >= 0 && takeBeginMin >= 0 ) {
						startCal.set(Calendar.HOUR_OF_DAY, takeBeginHour);
						startCal.set(Calendar.MINUTE, takeBeginMin);
						takeStartMillis = startCal.getTimeInMillis();
					}
					if ( takeEndHour >= 0 && takeEndMin >= 0 ) {
						endCal.set(Calendar.HOUR_OF_DAY, takeEndHour);
						endCal.set(Calendar.MINUTE, takeEndMin);
						takeEndMillis = endCal.getTimeInMillis();
					}
					long currentMillis = System.currentTimeMillis();
					success = true;
					if ( takeStartMillis > 0 ) {
						if ( takeStartMillis < currentMillis ) {
							success = true;
						} else {
							success = false;
						}
					}
					if ( success ) {
						if ( takeEndMillis > 0 ) {
							if ( takeEndMillis > currentMillis ) {
								success = true;
							} else {
								success = false;
							}
						}
					}
					
					if ( success ) {
						/**
						 * Execute the post task check script if exist
						 */
						if ( StringUtil.checkNotEmpty(task.getPostCheckScript()) ) {
							ScriptHook hook = ScriptHook.getScriptHook(task.getPostCheckScript());
							ScriptResult result = null;
							if ( hook != null ) {
								result = ScriptManager.getInstance().runScript(hook, user, task);
							} else {
								result = ScriptManager.getInstance().runScript(task.getPostCheckScript(), user, task);
							}
							if ( result != null && result.getType() == Type.SUCCESS_RETURN ) {
								List list = result.getResult();
								TaskPostStatus status = (TaskPostStatus)list.get(0);
								if ( status == TaskPostStatus.SUCCESS ) {
									success = true;
								} else {
									success = false;
								}
							}
						}
					} else {
						SysMessageManager.getInstance().sendClientInfoMessage(
								user, "task.reward.outoftime", XinqiSysMessage.Type.CONFIRM);
					}

					if ( success ) {
						if ( task.isBroadcast() ) {
							String roleName = UserManager.getDisplayRoleName(user.getRoleName());
							String content = Text.text("notice.task", roleName, task.getName());
							ChatManager.getInstance().processChatToWorldAsyn(null, content);
						}
						
						boolean useStatusChanged = true;
						int exp = task.getExp();
						int golden = task.getGold();
						int voucher = task.getTicket(); 
						int medal = task.getGongxun(); 
						int yuanbao = task.getCaifu();
						int guildWealth = task.getGuildWealth();
						int guildCredit = task.getGuildCredit();
						if ( logger.isDebugEnabled() ) {
							logger.debug("User take task(id:{})'s reward: exp={}, golden={}, voucher={}, medal={}, yuanbao={}",
									new Object[]{taskId, exp, golden, voucher, medal, yuanbao});
						}
						if ( exp > 0 ) {
							user.setExp(user.getExp()+exp);
							useStatusChanged = true;
							
							StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.ProduceTask, "Exp", exp,
									taskId, task.getName(), user.getExp());
						}
						if ( golden > 0 ) {
							user.setGolden(user.getGolden()+golden);
							useStatusChanged = true;
							
							StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.ProduceTask, MoneyType.GOLDEN, golden,
									taskId, task.getName(), user.getGolden());
						}
						if ( voucher > 0 ) {
							user.setVoucher(user.getVoucher()+voucher);
							useStatusChanged = true;
						}
						if ( medal > 0 ) {
							user.setMedal(user.getMedal()+medal);
							useStatusChanged = true;
						}
						if ( yuanbao > 0 ) {
							user.setYuanbaoFree(user.getYuanbaoFree()+yuanbao);
							useStatusChanged = true;
							
							String message = Text.text("charge.success", yuanbao, user.getYuanbaoFree());
							SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
							
							StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.ProduceTask, MoneyType.YUANBAO, yuanbao,
									taskId, task.getName(), user.getYuanbaoFree());
						}
						if ( guildWealth > 0 ) {
							Guild guild = user.getGuild();
							if ( guild != null ) {
								guild.setWealth(guild.getWealth()+guildWealth);
								StatClient.getIntance().sendDataToStatServer(user, 
										StatAction.ProduceTask, "GuildWealth", guildWealth,
										taskId, task.getName(), 0);
								GuildManager.getInstance().saveGuild(guild);
								//Should sync the wealth will all online users.
								GuildManager.getInstance().syncGuildWithOnlineUsers(guild);
								/**
								 * 更新个人贡献，比例为财富除以2
								 */
								GuildMember member = user.getGuildMember();
								if ( member != null ) {
									member.setCredit(member.getCredit()+guildWealth/2);
									StatClient.getIntance().sendDataToStatServer(user, 
											StatAction.ProduceTask, "GuildMemberCredit", guildCredit,
											taskId, task.getName(), member.getCredit());
									GuildManager.getInstance().saveGuildMember(member);
								}
							} else {
								StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.ProduceTask, "GuildWealth", guildWealth,
									taskId, task.getName(), 0);
							}
						}
						if ( guildCredit > 0 ) {
							GuildMember member = user.getGuildMember();
							if ( member != null ) {
								member.setMedal(member.getMedal()+guildCredit);
								StatClient.getIntance().sendDataToStatServer(user, 
										StatAction.ProduceTask, "GuildMemberMedal", guildCredit,
										taskId, task.getName(), member.getMedal());
								GuildManager.getInstance().saveGuildMember(member);
							} else {
								StatClient.getIntance().sendDataToStatServer(user, 
										StatAction.ProduceTask, "GuildMemberMedal", guildCredit,
										taskId, task.getName(), 0);
							}
						}
						List<Award> awards = task.getAwards();
						if ( awards != null && awards.size()>0 ) {
							for ( Award award : awards ) {
								if ( Constant.ACHIEVEMENT.equals(award.type) ) {
									int score = award.count;
									user.setAchievement(user.getAchievement()+score);
									//Update the rank data
									RankManager.getInstance().storeGlobalRankData(
										user, RankScoreType.ACHIEVEMENT, System.currentTimeMillis());
								} else if ( Constant.ITEM.equals(award.type) ) {
									useStatusChanged = true;
									//This id format is deprecated
									//wangqi 2012-02-07
									//String itemId = StringUtil.concat(Constant.ITEM, Constant.UNDERLINE, 
									//		award.id, Constant.UNDERLINE, award.lv);
									String itemId = StringUtil.concat(award.id);
									ItemPojo itemPojo = ItemManager.getInstance().getItemById(itemId);
									if ( itemPojo != null ) {
										PropData propData = itemPojo.toPropData();
//										if ( award.lv > 0 ) {
//											propData.setLevel(award.lv);
//										}
										if ( award.count > 0 ) {
											propData.setCount(award.count);
										}
										user.getBag().addOtherPropDatas(propData);
										
										String message = Text.text("notice.get_item", propData.getCount(), propData.getName());
										SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 2500);
										
										StatClient.getIntance().sendDataToStatServer(user, StatAction.ProduceTaskPropData, 
												propData.getName(), propData.getColor(), propData.getLevel(), 
												propData.getCount(), propData.getPropIndate());
									} else {
										logger.warn("ignore null item by id {}", award.id);
									}
								} else if ( Constant.WEAPON.equals(award.type) ) {
									useStatusChanged = true;
									String typeName = String.valueOf(award.typeId);
									WeaponPojo weaponPojo = EquipManager.getInstance().
											getWeaponByTypeNameAndUserLevel(typeName, user.getLevel());
									if ( weaponPojo != null && 
											(weaponPojo.getSex() == Gender.ALL || weaponPojo.getSex() == user.getGender() ) ) {
										PropData propData = weaponPojo.toPropData(award.indate, award.color);
										int upgradeLevel = award.lv - propData.getLevel();
										//Recalculate the PropData's attack ... properties
										if ( upgradeLevel > 0 ) {
											propData.setLevel(award.lv);
											ScriptResult result = GameContext.getInstance().getScriptManager().
													runScript(ScriptHook.WEAPON_LEVEL_UPGRADE, propData, upgradeLevel);
											if ( result.getType() == Type.SUCCESS_RETURN ) {
												propData = (PropData)result.getResult().get(0);
											}
										}
										user.getBag().addOtherPropDatas(propData);
										
										String message = Text.text("notice.get_item", propData.getCount(), propData.getName());
										SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 2500);
										
										StatClient.getIntance().sendDataToStatServer(user, StatAction.ProduceTaskPropData, 
													propData.getName(), propData.getColor(), propData.getLevel(), 
													propData.getCount(), propData.getPropIndate());
									} else {
										logger.warn("ignore null weapon by id {}", award.id);
									}
								}
							}
						}
						//Save user and user's bag to database.
						if ( useStatusChanged ) {
							GameContext.getInstance().getUserManager().saveUser(user, false);
							GameContext.getInstance().getUserManager().saveUserBag(user, false);
						
							//Notify client user's role data is changed.
							//Send the data back to client
							BseRoleInfo roleInfo = user.toBseRoleInfo();
							GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
							logger.debug("The new roleInfo data for user {} is sent to client.", user.getRoleName());
						}

						//Get the persistent Jedis instance
						jedisDB.sadd(awardedSet, taskId);
						jedisDB.srem(finishedSet, taskId);

						/**
						 * Execute the post task script if exist
						 */
						if ( StringUtil.checkNotEmpty(task.getPostScript()) ) {
							ScriptHook hook = ScriptHook.getScriptHook(task.getPostScript());
							ScriptManager.getInstance().runScript(hook, user, task);
						}

						/*
						if ( inFinishedSet ) {
							Pipeline pipleline = jedisDB.pipelined();
							pipleline.srem(finishedSet, taskId);
							pipleline.sadd(awardedSet, taskId);
							pipleline.sync();
						} else {
							logger.info("#takeTaskReward TaskId {} is not in user {}'s finish set. Put it in", 
									taskId, user.getRoleName());
							jedisDB.sadd(awardedSet, taskId);
						}
						*/
					}
				}
			}
		}
						
		//Remove the BseDelTask and add new one if available
		if ( success ) {
			TaskType type = task.getType();
			Set<TaskPojo> typeTasks = user.getTaskFinished(type);
			if ( typeTasks != Constant.EMPTY_SET ) {
				typeTasks.remove(task);
			}
			if ( task.getType() != TaskType.TASK_ACHIVEMENT && 
					task.getType() != TaskType.TASK_RANDOM ) {
					//task.getType() != TaskType.TASK_ACTIVITY ) {
				BseDelTask.Builder modiBuilder = BseDelTask.newBuilder();
				modiBuilder.setTaskID(StringUtil.toInt(taskId, 0));
				XinqiMessage xinqiMsg = new XinqiMessage();
				xinqiMsg.payload = modiBuilder.build();
				GameContext.getInstance().writeResponse(user.getSessionKey(), xinqiMsg);
				
				//Add a new task if available
				if ( task.getType() != TaskType.TASK_ACTIVITY  ) {
					int diff = getUserMaxTaskCount(task.getType(), user);
					if ( diff > 0 ) {
						this.assignNewTask(task.getType(), user, 1);
						/**
					   * Check if the "script.task.UserLevelUp" is finished
						 * Because sometimes user may level up more than one levels, and
			       * User.setLevel can only sent one ModiTask a time. So when user
			       * login, we double check all the others tasks for user upgrading
						 * Call script to trigger task
						 */
						TaskManager.getInstance().processUserTasks(user, TaskHook.USER_UPGRADE);
					}
				}
			}
			logger.debug("User {} take the task(id:{}) reward.", user.getRoleName(), taskId);
		}
		return success;
	}
		
	/**
	 * Delete all task data for the given user.
	 */
	public void deleteUserTasks(User user) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Set<String> keys = jedisDB.keys(StringUtil.concat(KEY_PREFIX, user.getUsername(), Constant.STAR));
		String[] keyArray = keys.toArray(new String[keys.size()]);
		if ( keyArray.length > 0 ) {
			jedisDB.del(keyArray);
		}
	}
	
	/**
	 * Get all tasks from given set.
	 * 
	 * @param user
	 * @param setName
	 * @return
	 */
	public Collection<TaskPojo> acquireTasks(User user, String setName) {
		//Get the persistent Jedis instance
		Jedis jedisDB = JedisFactory.getJedisDB();
		Set<String> taskIdSet = jedisDB.smembers(setName);
		TreeSet<TaskPojo> tasks = new TreeSet<TaskPojo>();
		for ( String taskId : taskIdSet ) {
			TaskPojo taskPojo = this.getTaskById(taskId);
			if ( taskPojo != null && !taskPojo.isDisable() ) {
				tasks.add(taskPojo);
				jedisDB.sadd(setName, taskId);
			}
		}
		return tasks;
	}
	
	/**
	 * Get all level up tasks which level is lower than user's level.
	 * @param user
	 * @return
	 */
	public Collection<TaskPojo> acquireUserLevelUpTasks(User user,
			Set<String> todoTaskIdSet, Set<String> finishTaskIdSet, 
			Set<String> awardTaskIdSet) {
		TreeSet<TaskPojo> tasks = new TreeSet<TaskPojo>();
		if ( user != null ) {
			int level = user.getLevel();
			Jedis jedisDB = JedisFactory.getJedisDB();
			String todoSetKey = getTodoSetName(user);
			String finishSetKey = getFinishedSetName(user);
			String awardSetKey = getAwardedSetName(user);
			if ( todoTaskIdSet == null ) {
				todoTaskIdSet = jedisDB.smembers(todoSetKey);
			}
			if ( finishTaskIdSet == null ) {
				finishTaskIdSet = jedisDB.smembers(finishSetKey);
			}
			if ( awardTaskIdSet == null ) {
				awardTaskIdSet = jedisDB.smembers(awardSetKey);
			}
			for ( int i=level; i>0; i-- ) {
				TaskPojo task = this.levelTaskMap.get(i);
				if ( task == null || todoTaskIdSet.contains(task.getId()) 
						|| finishTaskIdSet.contains(task.getId()) 
						|| awardTaskIdSet.contains(task.getId()) ) {
					break;
				}
				tasks.add(task);
			}
		}
		return tasks;
	}
		
	/**
	 * Get all the tasks in given type.
	 * 
	 * @param type
	 * @return
	 */
	public TreeSet<TaskPojo> getTasksForType(TaskType type) {
		TreeSet<TaskPojo> treeSet = this.taskListMap.get(type);
		return treeSet;
	}

	/**
	 * Get the Redis "finished" set name
	 * @param user
	 * @return
	 */
	public static final String getFinishedSetName(User user) {
		return StringUtil.concat(KEY_PREFIX, user.getUsername(), KEY_FINISHED);
	}
	
	/**
	 * Get the Redis "todo" set name
	 * @param user
	 * @return
	 */
	public static final String getTodoSetName(User user) {
		return StringUtil.concat(KEY_PREFIX, user.getUsername(), KEY_TODO);
	}
	
	/**
	 * Get the Redis "awarded" set name
	 * @param user
	 * @return
	 */
	public static final String getAwardedSetName(User user) {
		return StringUtil.concat(KEY_PREFIX, user.getUsername(), KEY_AWARDED);
	}
	
	/**
	 * Get the total task count for a given users to get.
	 * @param type
	 * @param user
	 * @param taskMaxCount
	 * @return
	 */
	private int getUserMaxTaskCount(TaskType type, User user) {
		int taskMaxCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_TASK_NUMBER, 5);
		Set<TaskPojo> typeTasks = user.getTasks(type);
		Set<TaskPojo> finishedTypeTasks = user.getTaskFinished(type);
		int todoCount = typeTasks.size();
		int finishedCount = finishedTypeTasks.size();
		int count = todoCount + finishedCount;

		int diff = taskMaxCount - count;
		return diff;
	}
	
	/**
	 * Construct Protobuf's BseEquipment data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public BseTask toBseTask(User user) {
		BseTask.Builder builder = BseTask.newBuilder();
		synchronized ( dataMap ) {
			for ( TaskPojo taskPojo : dataMap.values() ) {
				if ( taskPojo.getType() != TaskType.TASK_ACHIVEMENT ) {
					builder.addTasks(taskPojo.toTaskData(user.getGender()));
				}
			}
		}
		return builder.build();
	}
	
	/**
	 * Convert this object to ProtoBuf's TaskProtoInfo
	 * @return
	 */
	public XinqiBseTaskList.TaskProtoInfo toTaskProtoInfo(User user, TaskPojo task) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String keySet = TaskManager.getFinishedSetName(user);
		Set<String> finishedSet = jedisDB.smembers(keySet);
		int step = 0;
		if ( finishedSet.contains(task.getId()) ) {
			step = task.getStep();
		} else {
			step = StringUtil.toInt(this.queryTaskSpecificData(user, task.getId(), Field.STEP), 0);
		}
		XinqiBseTaskList.TaskProtoInfo.Builder builder = 
				XinqiBseTaskList.TaskProtoInfo.newBuilder();
		builder.setTaskID(StringUtil.toInt(task.getId(), 0));
		builder.setStep(step);
		return builder.build();
	}
	
	/**
	 * Construct the BseAchievemnt list to client
	 * @return
	 */
	public BseAchievements toBseAchievement() {
		TreeSet<TaskPojo> achievements = taskListMap.get(TaskType.TASK_ACHIVEMENT);
		BseAchievements.Builder builder = BseAchievements.newBuilder();
		for ( TaskPojo task : achievements ) {
			Achievement.Builder achBuilder = Achievement.newBuilder();
			achBuilder.setId(task.getId());
			achBuilder.setTitle(task.getName());
			achBuilder.setInfo(task.getDesc());
			List<Award> awards = task.getAwards();
			for ( Award award : awards ) {
				if ( Constant.ACHIEVEMENT.equals(award.type) ) {
					if ( award.resource != null ) {
						achBuilder.setIcon(award.resource);
					} else {
						achBuilder.setIcon(Constant.EMPTY);
					}
					achBuilder.setScore(award.count);
					int step = task.getStep();
					if ( step == 0 ) {
						step = task.getCondition1();
					}
					achBuilder.setTotal(step);
					break;
				}
			}
			builder.addAchievements(achBuilder.build());
		}
		return builder.build();
	}
	
	/**
	 * Get all user's achievements status
	 * @return
	 */
	public void sendBseUserAchievements(User user) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		BseUserAchievements.Builder userAchieve = BseUserAchievements.newBuilder();
		
		//Get all unlocked achievements
		String awardSetKey = getAwardedSetName(user);
		Set<String> awardSet = jedisDB.smembers(awardSetKey);
		for ( String taskId : awardSet ) {
    	TaskPojo taskPojo = this.getTaskById(taskId);
    	if ( taskPojo != null && taskPojo.getType() == TaskType.TASK_ACHIVEMENT ) {
	    	userAchieve.addTaskIds(taskId);
	    	int step = taskPojo.getStep();
	    	if ( step <= 0 ) {
	    		step = taskPojo.getCondition1();
	    	}
	    	userAchieve.addStep(step);
    	}
		}
		
		//Get all locked achievement data.
		String key = getTaskDataKey(user, Field.STEP);
		Map<String, String> achMap = jedisDB.hgetAll(key);
		Collection<TaskPojo> tasks = user.getTasks(TaskType.TASK_ACHIVEMENT);
    for ( TaskPojo task : tasks ) {
    	String value = achMap.get(task.getId());
    	int step = 0;
    	if ( value != null ) { 
    		step = StringUtil.toInt(value, 0);
    	}
    	userAchieve.addTaskIds(task.getId());
    	/**
    	 * script.task.UserLevelUp has 0 step and given user level as step
    	 */
    	if ( task.getStep() == 0 ) {
    		userAchieve.addStep(user.getLevel());
    	} else {
    		userAchieve.addStep(step);
    	}
    }

    XinqiMessage xinqi = new XinqiMessage();
    xinqi.payload = userAchieve.build();
    GameContext.getInstance().writeResponse(user.getSessionKey(), xinqi);
	}
	
	/**
	 * Return task list as zip format 
	 * @return
	 */
	public BseZip toBseZip() {
		Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
		BseZip.Builder zipBuilder = BseZip.newBuilder();
		zipBuilder.setName("BseTask");
		byte[] compressLuaScript = compressLuaScriptMap.get(locale);
		if ( compressLuaScript == null ) {
			logger.warn("Failed to find compressed lua script for locale:{}", locale);
			compressLuaScript = compressLuaScriptMap.get(GameResourceManager.DEFAULT_LOCALE);
		}
		ByteString bs = ByteString.copyFrom(compressLuaScript);
		zipBuilder.setPayload(bs);
		return zipBuilder.build();
	}
	
	/**
	 * The task specific data's field
	 * @author wangqi
	 *
	 */
	public static enum Field {
		//All fields
		ALL("*"),
		STEP(":step"),
		DATA(":data");
		
		private String value = null;
		
		Field(String value){
			this.value = value;
		}
		
		public String value() {
			return this.value;
		}
	}
	
	/**
	 * The task's action type.
	 * @author wangqi
	 *
	 */
	public static enum TaskHook {
		LOGIN,
		LOGIN_DATE,
		COMBAT,
		CRAFT_COMPOSE,
		CRAFT_COMPOSE_COLOR,
		CRAFT_COMPOSE_EQUIP,
		CRAFT_FORGE,
		CRAFT_TRANSFER,
		USER_UPGRADE,
		WEAR,
		BUY_ITEM,
		USE_TOOL,
		WEALTH,
		POWER,
		RANK,
		ADD_BAG,
		ADD_BAG_COUNT,
		SELL_GOOD,
		WEIBO,
		EMAIL_VERIFY,
		CAISHEN_PRAY,
		TREASURE_HUNT,
		CHECK_RANKING,
		CHAT_WORLD,
		ADD_FRIEND,
		CHARGE,
		COLLECT,
		EXPGAIN,
		ROLEACTION,
		VIP_LEVEL,
		JOIN_GUILD,
	}
}
