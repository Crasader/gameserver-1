package com.xinqihd.sns.gameserver.admin.data;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.TaskGameDataPanel;
import com.xinqihd.sns.gameserver.admin.security.PriviledgeKey;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class WeaponDataGeneratorDataAction extends AbstractAction {
	
	public WeaponDataGeneratorDataAction() {
		super("武器数值生成器", ImageUtil.createImageSmallIcon("Chart Bar.png", "武器数值生成器"));
	}
	
	public PriviledgeKey getPriviledge() {
		return PriviledgeKey.task_weapon_data_generator;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				//MainPanel.getInstance().setCenterPanel(WeaponDataGeneratorPanel.getInstance());
				MainPanel.getInstance().setCenterPanel(WeaponManualDataPanel.getInstance());
			}
		});
	}

}
