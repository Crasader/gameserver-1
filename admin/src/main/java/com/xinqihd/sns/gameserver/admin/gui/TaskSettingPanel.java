package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.action.game.TaskGameDataAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.user.UserManageAction;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.service.BackupMongoConfigService;
import com.xinqihd.sns.gameserver.service.InitialService;
import com.xinqihd.sns.gameserver.service.RestoreMongoConfigService;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskSettingPanel extends MyPanel implements ActionListener {
	
	public static final String[] BACKUP_COLLECTIONS = {
		"maps", "tips", "dailymarks", "charges", "gamedata", 
		"tasks", "vips", "shops_new", "equipments_new", "items", 
		"levels", "gameres", "bosses", "servers", "rewards", 
		"promotions", "cdkeys", "exits", "rewardlevels"
	};
	
	private static final Logger logger = LoggerFactory.getLogger(TaskSettingPanel.class);
	
	private static TaskSettingPanel instance = new TaskSettingPanel();
	
	private static final String COMMAND_BACKUP = "backup";
	private static final String COMMAND_RESTORE = "restore";
	
	private final String[] defaultSettings = {
			"192.168.0.1",
			"27017",
			"192.168.0.1",
			"27017",
			"redis.babywar.xinqihd.com",
			"6379",
			"redisdb.babywar.xinqihd.com",
			"6379",
			"mysql.babywar.xinqihd.com",
			"3306",
			"babywar",
			"server0001",
			
			"g1.babywar.xinqihd.com",
			"3443",
	};
	
	private final JLabel lblMongo = new JLabel("Mongo用户数据库地址");
	private final JLabel lblMongoPort = new JLabel("Mongo端口");
	private final JLabel lblmongoDBName     = new JLabel("用户数据库名");
	private final JLabel lblmongoNamespace = new JLabel("用户命名空间");
	private final JLabel lblMongoConfig = new JLabel("Mongo配置用户数据库地址");
	private final JLabel lblMongoConfigPort = new JLabel("Mongo配置数据库端口");
	private final JLabel lblmongoConfigDBName     = new JLabel("配置数据库名");
	private final JLabel lblmongoConfigNamespace = new JLabel("配置命名空间");
	private final JLabel lblRedis = new JLabel("Redis闪存地址");
	private final JLabel lblRedisPort = new JLabel("Redis闪存端口");
	private final JLabel lblRedisDB = new JLabel("Redis数据库地址");
	private final JLabel lblRedisDBPort = new JLabel("Redis数据库端口");
	
	private final JLabel lblMysqlHost = new JLabel("MySQL数据库地址");
	private final JLabel lblMysqlPort = new JLabel("MySql数据库端口");
	
	private final JLabel lblGameServer = new JLabel("游戏服务器地址");
	private final JLabel lblGamePort = new JLabel("游戏服务器端口");
	
	private final JComboBox comboMongo = new JComboBox();
	private final JComboBox comboMongoPort = new JComboBox();
	private final JComboBox comboMongoDB = new JComboBox();
	private final JComboBox comboMongoNS = new JComboBox();
	
	private final JComboBox comboMongoConfig = new JComboBox();
	private final JComboBox comboMongoConfigPort = new JComboBox();
	private final JComboBox comboMongoConfigDB = new JComboBox();
	private final JComboBox comboMongoConfigNS = new JComboBox();
	
	private final JComboBox comboRedis = new JComboBox();
	private final JComboBox comboRedisPort = new JComboBox();
	
	private final JComboBox comboRedisDB = new JComboBox();
	private final JComboBox comboRedisDBPort = new JComboBox();
	
	private final JComboBox comboMysqlHost = new JComboBox();
	private final JComboBox comboMysqlPort = new JComboBox();
	
	
	private final JComboBox gameServerField = new JComboBox();
	private final JComboBox gameServerPort = new JComboBox();
	
	private final JButton okButton = new JButton("应用");
	private final JButton resetButton = new JButton("重置");
	
	private final JCheckBox cleanBeforeRestore = new JCheckBox("导入前清空数据库");
	
	private final JXButton backupButton = new JXButton("备份配置数据");
	private final JXButton restoreButton = new JXButton("恢复配置数据");
	private final JCheckBox[] backupCollections = new JCheckBox[BACKUP_COLLECTIONS.length];
	
	public TaskSettingPanel() {
		init();
	}
		
	public void init() {
		JXPanel settingPanel = new JXPanel();
		TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "服务器设定");
		border.setTitleFont(MainFrame.BIG_FONT);
		settingPanel.setBorder(border);
		
		resetDefault();
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.resetButton.setActionCommand(ActionName.CANCEL.name());
		this.resetButton.addActionListener(this);
		
		this.comboMongo.setEditable(true);
		this.comboMongoPort.setEditable(true);
		this.comboMongoConfig.setEditable(true);
		this.comboMongoConfigPort.setEditable(true);
		this.comboRedis.setEditable(true);
		this.comboRedisPort.setEditable(true);
		this.comboRedisDB.setEditable(true);
		this.comboRedisDBPort.setEditable(true);
		this.comboMongoDB.setEditable(true);
		this.comboMongoConfigDB.setEditable(true);
		
		this.comboMongo.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameMongoHost));
		this.comboMongoPort.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameMongoPort));
		this.comboMongoDB.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.mongoDBName));
		this.comboMongoNS.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.mongoNamespace));
		this.comboMongoConfig.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameMongoConfigHost));
		this.comboMongoConfigPort.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameMongoConfigPort));
		this.comboMongoConfigDB.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName));
		this.comboRedis.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameRedisHost));
		this.comboRedisPort.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameRedisPort));
		this.comboRedisDB.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameRedisDBHost));
		this.comboRedisDBPort.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameRedisDBPort));
		this.comboMysqlHost.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.mysqlHost));
		this.comboMysqlPort.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.mysqlPort));
		this.comboMongoNS.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace));
		this.gameServerField.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gameHost));
		this.gameServerPort.setSelectedItem(ConfigManager.getConfigAsString(ConfigKey.gamePort));
		
		settingPanel.setLayout(new MigLayout("wrap 4, gap 10px, alignx center", ""));
		
		settingPanel.add(this.lblGameServer, "sg lb");
		settingPanel.add(this.gameServerField, "sg fd, growx, pushx");
		settingPanel.add(this.lblGamePort,  "sg lb");
		settingPanel.add(this.gameServerPort, "sg fd, growx, pushx");
		
		settingPanel.add(this.lblMongo, "sg lb");
		settingPanel.add(this.comboMongo, "sg fd, growx, pushx");
		settingPanel.add(this.lblMongoPort,  "sg lb");
		settingPanel.add(this.comboMongoPort, "sg fd, growx, pushx");
		settingPanel.add(this.lblmongoDBName,  "sg lb");
		settingPanel.add(this.comboMongoDB, "sg fd, growx, pushx");
		settingPanel.add(this.lblmongoNamespace,  "sg lb");
		settingPanel.add(this.comboMongoNS, "sg fd, growx, pushx");
		
		settingPanel.add(this.lblMongoConfig, "sg lb");
		settingPanel.add(this.comboMongoConfig, "sg fd, growx, pushx");
		settingPanel.add(this.lblMongoConfigPort,  "sg lb");
		settingPanel.add(this.comboMongoConfigPort, "sg fd, growx, pushx");
		settingPanel.add(this.lblmongoConfigDBName,  "sg lb");
		settingPanel.add(this.comboMongoConfigDB, "sg fd, growx, pushx");
		settingPanel.add(this.lblmongoConfigNamespace,  "sg lb");
		settingPanel.add(this.comboMongoConfigNS, "sg fd, growx, pushx");
		
		settingPanel.add(this.lblRedis, "sg lb");
		settingPanel.add(this.comboRedis, "sg fd, growx, pushx");
		settingPanel.add(this.lblRedisPort,  "sg lb");
		settingPanel.add(this.comboRedisPort, "sg fd, growx, pushx");
		settingPanel.add(this.lblRedisDB, "sg lb");
		settingPanel.add(this.comboRedisDB, "sg fd, growx, pushx");
		settingPanel.add(this.lblRedisDBPort,  "sg lb");
		settingPanel.add(this.comboRedisDBPort, "sg fd, growx, pushx");
		settingPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "span, width 100%");
		
		settingPanel.add(this.lblMysqlHost,  "sg lb");
		settingPanel.add(this.comboMysqlHost, "sg fd, growx, pushx");
		settingPanel.add(this.lblMysqlPort,  "sg lb");
		settingPanel.add(this.comboMysqlPort, "sg fd, growx, pushx");
		settingPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "span, width 100%");
		
		settingPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "span, width 100%");
				
		settingPanel.add(this.okButton, "span, split 2, alignx center, aligny bottom");
		settingPanel.add(this.resetButton, "aligny bottom");
		
		JXPanel backupPanel = new JXPanel();
		TitledBorder backupBorder =  BorderFactory.createTitledBorder("数据备份");
		backupBorder.setTitleFont(MainFrame.BIG_FONT);
		backupPanel.setBorder(backupBorder);
		backupPanel.setLayout(new MigLayout("wrap 4, align center"));
		JXPanel collPanel = new JXPanel();
		collPanel.setLayout(new MigLayout("wrap 4, align center"));
		collPanel.setBorder(BorderFactory.createEtchedBorder());
		for ( int i=0; i<BACKUP_COLLECTIONS.length; i++ ) {
			String collection = BACKUP_COLLECTIONS[i];
			backupCollections[i] = new JCheckBox(collection);
			collPanel.add(backupCollections[i], "sg checkbox");
		}
		backupPanel.add(collPanel, "align center, width 100%");
		this.backupButton.setActionCommand(COMMAND_BACKUP);
		this.backupButton.addActionListener(this);
		this.restoreButton.setActionCommand(COMMAND_RESTORE);
		this.restoreButton.addActionListener(this);
		
		backupPanel.add(this.cleanBeforeRestore, "newline, span");
		backupPanel.add(this.backupButton, "span, split 2, align center");
		backupPanel.add(this.restoreButton, "");
		
		this.setLayout(new MigLayout("wrap 1"));
		this.add(settingPanel, "width 100%");
		this.add(backupPanel,  "width 100%");
	}
	
	public static TaskSettingPanel getInstance() {
		return new TaskSettingPanel();
	}
	
	public void resetDefault() {
		this.comboMongo.setModel(new DefaultComboBoxModel(new Object[]{"192.168.0.1", "localhost"}));
		this.comboMongoPort.setModel(new DefaultComboBoxModel(new Object[]{"27017"}));
		this.comboMongoDB.setModel(new DefaultComboBoxModel(new Object[]{"babywar"}));
		this.comboMongoNS.setModel(new DefaultComboBoxModel(new Object[]{"server0001"}));
		
		this.comboMongoConfig.setModel(new DefaultComboBoxModel(new Object[]{"192.168.0.1", "localhost"}));
		this.comboMongoConfigPort.setModel(new DefaultComboBoxModel(new Object[]{"27017"}));
		this.comboMongoConfigDB.setModel(new DefaultComboBoxModel(new Object[]{"babywarcfg"}));
		this.comboMongoConfigNS.setModel(new DefaultComboBoxModel(new Object[]{"server0001"}));
		
		this.comboRedis.setModel(new DefaultComboBoxModel(new Object[]{"192.168.0.1", "localhost"}));
		this.comboRedisPort.setModel(new DefaultComboBoxModel(new Object[]{"6379"}));
		
		this.comboRedisDB.setModel(new DefaultComboBoxModel(new Object[]{"192.168.0.1", "localhost"}));
		this.comboRedisDBPort.setModel(new DefaultComboBoxModel(new Object[]{"6379"}));
		
		this.comboMysqlHost.setModel(new DefaultComboBoxModel(new Object[]{"192.168.0.1", "localhost"}));
		this.comboMysqlPort.setModel(new DefaultComboBoxModel(new Object[]{"3306"}));
		
		ComboBoxModel model1 = new DefaultComboBoxModel(new Object[]{
				"g1.babywar.xinqihd.com", "g2.babywar.xinqihd.com", "g3.babywar.xinqihd.com", 
				"test.babywar.xinqihd.com", "localhost"});
		this.gameServerField.setModel(model1);
		this.gameServerField.setEditable(true);
		ComboBoxModel model2 = new DefaultComboBoxModel(new Object[]{"3443"});
		this.gameServerPort.setModel(model2);
		this.gameServerPort.setEditable(true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals(ActionName.OK.name()) ) {
			boolean isInitialized = true;
			String mongoHost = ConfigManager.getConfigAsString(ConfigKey.gameMongoHost);
			if ( mongoHost == null ) {
				isInitialized = false;
			}
			
			mongoHost = this.comboMongo.getSelectedItem().toString();
			logger.debug("mongoHost: {}",mongoHost);
			String mongoPort = this.comboMongoPort.getSelectedItem().toString();
			String mongoDBName = this.comboMongoDB.getSelectedItem().toString();
			String mongoDBNamespace = this.comboMongoNS.getSelectedItem().toString();
			String mongoConfigHost = this.comboMongoConfig.getSelectedItem().toString();
			String mongoConfigPort = this.comboMongoConfigPort.getSelectedItem().toString();
			String mongoConfigDB = this.comboMongoConfigDB.getSelectedItem().toString();
			String mongoConfigNS = this.comboMongoConfigNS.getSelectedItem().toString();
			String redisHost = this.comboRedis.getSelectedItem().toString();
			String redisPort = this.comboRedisPort.getSelectedItem().toString();
			String redisDBHost = this.comboRedisDB.getSelectedItem().toString();
			String redisDBPort = this.comboRedisDBPort.getSelectedItem().toString();
			String mysqlHost = this.comboMysqlHost.getSelectedItem().toString();
			String mysqlPort = this.comboMysqlPort.getSelectedItem().toString();
			
			String gameServer = this.gameServerField.getSelectedItem().toString();
			String gameServerPort = this.gameServerPort.getSelectedItem().toString();
			
			ConfigManager.saveConfigKeyValue(ConfigKey.gameMongoHost, mongoHost);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameMongoPort, mongoPort);
			ConfigManager.saveConfigKeyValue(ConfigKey.mongoDBName, mongoDBName);
			ConfigManager.saveConfigKeyValue(ConfigKey.mongoNamespace, mongoDBNamespace);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameMongoConfigHost, mongoConfigHost);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameMongoConfigPort, mongoConfigPort);
			ConfigManager.saveConfigKeyValue(ConfigKey.mongoConfigDBName, mongoConfigDB);
			ConfigManager.saveConfigKeyValue(ConfigKey.mongoConfigNamespace, mongoConfigNS);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameRedisHost, redisHost);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameRedisPort, redisPort);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameRedisDBHost, redisDBHost);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameRedisDBPort, redisDBPort);
			ConfigManager.saveConfigKeyValue(ConfigKey.mysqlHost, mysqlHost);
			ConfigManager.saveConfigKeyValue(ConfigKey.mysqlPort, mysqlPort);
			ConfigManager.saveConfigKeyValue(ConfigKey.gameHost, gameServer);
			ConfigManager.saveConfigKeyValue(ConfigKey.gamePort, gameServerPort);
			
			//Apply the change to system.
			GlobalConfig config = GlobalConfig.getInstance();
			config.overrideProperty("mongdb.host", mongoHost);
			config.overrideProperty("mongdb.port", mongoPort);
			config.overrideProperty(GlobalConfigKey.mongo_configdb_host, mongoConfigHost);
			config.overrideProperty(GlobalConfigKey.mongo_configdb_port, mongoConfigPort);
			config.overrideProperty("jedis.master.host", redisHost);
			config.overrideProperty("jedis.master.port", redisPort);
			config.overrideProperty(GlobalConfigKey.jedis_db_host, redisDBHost);
			config.overrideProperty(GlobalConfigKey.jedis_db_port, redisDBPort);
			
			//Initialize MongoUtil
			MongoUtil.initUserMongo(mongoHost, StringUtil.toInt(mongoPort, 27017));
			MongoUtil.initCfgMongo(mongoConfigHost, StringUtil.toInt(mongoConfigPort, 27017));
			//Initialize Jedis
			JedisFactory.initJedis();
			
			InitialService initService = new InitialService();
			initService.execute();
			
			if ( isInitialized ) {
				JOptionPane.showConfirmDialog(this, "您需要重启程序才能使改动生效", 
						"提示信息", JOptionPane.OK_OPTION);
			}
			
			Action action = new UserManageAction();
			action.actionPerformed(null);
		} else if ( e.getActionCommand().equals(ActionName.CANCEL.name()) ) {
			resetDefault();
		} else if ( COMMAND_BACKUP.equals(e.getActionCommand()) ) {
			String prevDir = ConfigManager.getConfigAsString(ConfigKey.backupDir);
			JFileChooser fileChooser = null;
			if ( prevDir != null ) {
				fileChooser = new JFileChooser(new File(prevDir));
			} else {
				fileChooser = new JFileChooser();
			}
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fileChooser.showSaveDialog(this);
			if ( returnVal == JFileChooser.APPROVE_OPTION ) {
				File dir = fileChooser.getSelectedFile();
				ConfigManager.saveConfigKeyValue(ConfigKey.backupDir, dir.getAbsolutePath());
				ArrayList<String> collections = new ArrayList<String>();
				for ( JCheckBox cb : backupCollections ) {
					if ( cb.isSelected() ) {
						collections.add(cb.getText());
					}
				}
				BackupMongoConfigService service = new BackupMongoConfigService(dir, collections.toArray(new String[0]));
				service.execute();
				service.getDialog().setVisible(true);
			}
		} else if ( COMMAND_RESTORE.equals(e.getActionCommand()) ) {
			String prevDir = ConfigManager.getConfigAsString(ConfigKey.backupDir);
			JFileChooser fileChooser = null;
			if ( prevDir != null ) {
				fileChooser = new JFileChooser(new File(prevDir));
			} else {
				fileChooser = new JFileChooser();
			}
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fileChooser.showSaveDialog(this);
			if ( returnVal == JFileChooser.APPROVE_OPTION ) {
				File dir = fileChooser.getSelectedFile();
				ConfigManager.saveConfigKeyValue(ConfigKey.backupDir, dir.getAbsolutePath());
				boolean cleanOldData = this.cleanBeforeRestore.isSelected();
				ArrayList<String> collections = new ArrayList<String>();
				for ( JCheckBox cb : backupCollections ) {
					if ( cb.isSelected() ) {
						collections.add(cb.getText());
					}
				}
				RestoreMongoConfigService service = new RestoreMongoConfigService(dir, 
						cleanOldData, collections.toArray(new String[0]));
				service.execute();
				service.getDialog().setVisible(true);
			}
		}

	}
	
}
