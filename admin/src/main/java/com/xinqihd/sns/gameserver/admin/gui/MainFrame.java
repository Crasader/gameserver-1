package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JFrame;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXFrame;

import com.xinqihd.sns.gameserver.admin.action.setting.TaskSettingAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.util.MyWindowUtil;
import com.xinqihd.sns.gameserver.chat.ChatSender;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.service.DownloadIconsService;
import com.xinqihd.sns.gameserver.service.InitialService;

public class MainFrame extends JXFrame {
	
	//The screen width
	public static int screenWidth = MyWindowUtil.getScreenSize().width;
	
	//The screen height
	public static int screenHeight = MyWindowUtil.getScreenSize().height;
	
	public static final Font BIG_FONT = new Font("Courier", Font.BOLD, 14);
	
	public static final Font NORMAL_FONT = new Font("Courier", Font.PLAIN, 12);
	
	public static final Font SMALL_FONT = new Font("Courier", Font.PLAIN, 10);
	
	public static final String MONGO_WORKSPACE_DB = "gameadminworkspace";
	public static final String MONGO_WORKSPACE_NS = "server0001";
	
	public static String loginUserName = "root";
	
	//为了加快渲染速度，将所有游戏图标加载后存入这个对照表中
	//参见DownloadIconsService
	public static final HashMap<String, Icon> ICON_MAPS = new HashMap<String, Icon>();
	
	//The main panel
	private static MainPanel mainPanel = new MainPanel();
	
	private static MainFrame instance = null;

	/**
	 * Create the application.
	 */
	public MainFrame() {
		LoginDialog loginDialog = new LoginDialog();
		loginDialog.setVisible(true);
		if ( loginDialog.getLoginResult() ) {
			init();
		}
		instance = this;
	}
	
	public void init() {
		this.setLayout(new MigLayout(""));
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.setSize(new Dimension(screenWidth, screenHeight));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultLookAndFeelDecorated(false);
		
		add(mainPanel, "width 100%, height 100%");
		this.setVisible(true);
		
		//Check if the settings are available
		String mongoHost = ConfigManager.getConfigAsString(ConfigKey.gameMongoConfigHost);
		String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
		String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
		if ( mongoHost == null || database == null || namespace == null ) {
			TaskSettingAction.getInstance().actionPerformed(null);
		} else {
			InitialService initService = new InitialService();
			initService.execute();
		}
		
		//Download the icons
		DownloadIconsService service = new DownloadIconsService();
		service.execute();
		
		try {
			ChatSender.getInstance().stopWorkers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the global MainPanel instance.
	 * @return
	 */
	public static final MainPanel getMainPanel() {
		return mainPanel;
	}
	
	public static final MainFrame getInstance() {
		return instance;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return screenWidth;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return screenHeight;
	}

}
