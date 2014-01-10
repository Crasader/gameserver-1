package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.proto.XinqiBceReloadConfig.BceReloadConfig;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It implements the GameDataPanel
 * @author wangqi
 *
 */
public class TaskReloadConfigPanel extends MyPanel implements ActionListener {
	
	public static final String[] CONFIG_NAMES = {
		 "maps", "tips", "charges", "gamedata", "tasks", "vips", 
		 "shops", "equips", "items", "levels", "gameres", "bosses", "rewards", 
		 "promotions", "cdkeys", "exits", "servers", "biblio"
	};
	
	private static final Logger logger = LoggerFactory.getLogger(TaskReloadConfigPanel.class);
	
	private static TaskReloadConfigPanel instance = new TaskReloadConfigPanel();
	
	private static final String COMMAND_BACKUP = "backup";
	private static final String COMMAND_RESTORE = "restore";
		
	private final JLabel lblGameServer = new JLabel("游戏服务器地址");
	private final JLabel lblGamePort = new JLabel("游戏服务器端口");
	
	private final JComboBox gameServerField = new JComboBox(new String[]{"192.168.0.77","test.babywar.xinqihd.com","game1.babywar","game2.babywar","game3.babywar"});
	private final JComboBox gameServerPort = new JComboBox(new Integer[]{3443});
	
	private final JButton okButton = new JButton("重载选中配置数据");
	
	private final JCheckBox[] backupCollections = new JCheckBox[CONFIG_NAMES.length];
	
	public TaskReloadConfigPanel() {
		init();
	}
		
	public void init() {
		JXPanel settingPanel = new JXPanel();
		TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "服务器设定");
		border.setTitleFont(MainFrame.BIG_FONT);
		settingPanel.setBorder(border);
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		
		settingPanel.setLayout(new MigLayout("wrap 4, gap 10px, alignx center", ""));
		
		settingPanel.add(this.lblGameServer, "sg lb");
		settingPanel.add(this.gameServerField, "sg fd, growx, pushx");
		settingPanel.add(this.lblGamePort,  "sg lb");
		settingPanel.add(this.gameServerPort, "sg fd, growx, pushx");
		
		JXPanel configPanel = new JXPanel();
		TitledBorder backupBorder =  BorderFactory.createTitledBorder("配置文件");
		backupBorder.setTitleFont(MainFrame.BIG_FONT);
		configPanel.setBorder(backupBorder);
		configPanel.setLayout(new MigLayout("wrap 4, align center"));
		JXPanel collPanel = new JXPanel();
		collPanel.setLayout(new MigLayout("wrap 4, align center"));
		collPanel.setBorder(BorderFactory.createEtchedBorder());
		for ( int i=0; i<CONFIG_NAMES.length; i++ ) {
			String collection = CONFIG_NAMES[i];
			backupCollections[i] = new JCheckBox(collection);
			collPanel.add(backupCollections[i], "sg checkbox");
		}
		configPanel.add(collPanel, "align center, width 100%");
		configPanel.add(this.okButton, "newline, span, split 2, alignx center, aligny bottom");
		
		this.setLayout(new MigLayout("wrap 1"));
		this.add(settingPanel, "width 100%");
		this.add(configPanel,  "width 100%");
	}
	
	public static TaskReloadConfigPanel getInstance() {
		return new TaskReloadConfigPanel();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals(ActionName.OK.name()) ) {
			
			String gameServer = this.gameServerField.getSelectedItem().toString();
			String gameServerPort = this.gameServerPort.getSelectedItem().toString();
			
			BceReloadConfig.Builder builder = BceReloadConfig.newBuilder();
			for ( JCheckBox checkBox : backupCollections ) {
				if ( checkBox.isSelected() ) {
					String config = checkBox.getText();
					builder.addConfigname(config);
				}
			}
			GameClient client = new GameClient(gameServer, StringUtil.toInt(gameServerPort, 3443));
			XinqiMessage msg = new XinqiMessage();
			msg.payload = builder.build();
			client.sendMessageToServer(msg);
			JOptionPane.showConfirmDialog(this, "已经通知"+gameServer+"重新装载配置文件");
		}
	}
	
}
