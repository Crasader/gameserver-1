package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskCraftPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskCraftTestAction extends AbstractAction {
	
	public TaskCraftTestAction() {
		super("强化及合成数值", ImageUtil.createImageSmallIcon("Expand.png", "强化及合成数值"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_craft_balancer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MainPanel.getInstance().setCenterPanel(TaskCraftPanel.getInstance());
			}
		});
	}

}
