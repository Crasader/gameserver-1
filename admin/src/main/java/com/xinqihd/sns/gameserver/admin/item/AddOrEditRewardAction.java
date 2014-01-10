package com.xinqihd.sns.gameserver.admin.item;

import java.awt.event.ActionEvent;

import com.xinqihd.sns.gameserver.admin.action.game.AbstractAddRowAction;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

/**
 * 为宝箱类型的道具增加奖励内容
 * @author wangqi
 *
 */
public class AddOrEditRewardAction extends AbstractAddRowAction {
	
	public AddOrEditRewardAction() {
		super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add"));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		AddRewardDialog dialog = new AddRewardDialog(null);
		dialog.setVisible(true);
		
		//Get data here
		Object newObj = dialog.getNewObject();
		if ( newObj != null ) {
			this.tableModel.insertRow(newObj);
		}
	}
	
}
