package com.xinqihd.sns.gameserver.admin.action.game;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskEquipmentExportPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class TaskShopDataGeneratorAction extends AbstractAction {
	
	public TaskShopDataGeneratorAction() {
		super("装备及商城数据生成", ImageUtil.createImageSmallIcon("Shopping Cart.png", "商城数据生成"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_shop_data_generator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				//MainPanel.getInstance().setCenterPanel(TaskShopDataGeneratorPanel.getInstance());
				MainPanel.getInstance().setCenterPanel(TaskEquipmentExportPanel.getInstance());
			}
		});
	}

}
