package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskEquipmentPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskGameDataPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskEquipmentAction extends AbstractAction {
	
	public TaskEquipmentAction() {
		super("游戏装备数据设定(新)", ImageUtil.createImageSmallIcon("Puzzle.png", "游戏装备数据设定(新)"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_game_equipment;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskEquipmentPanel.getInstance());
			}
		});
	}

}
