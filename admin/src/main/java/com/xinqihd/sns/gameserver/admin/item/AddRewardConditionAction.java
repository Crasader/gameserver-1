package com.xinqihd.sns.gameserver.admin.item;

import java.awt.event.ActionEvent;

import com.xinqihd.sns.gameserver.admin.action.game.AbstractAddRowAction;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.reward.RewardCondition;

/**
 * 为宝箱类型的道具增加奖励内容
 * @author wangqi
 *
 */
public class AddRewardConditionAction extends AbstractAddRowAction {
	
	public AddRewardConditionAction() {
		super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add"));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		AddRewardConditionDialog dialog = new AddRewardConditionDialog();
		dialog.setVisible(true);
		
		//Get data here
		RewardCondition newObj = dialog.getSavedRewardCondition();
		if ( newObj != null ) {
			this.tableModel.insertRow(newObj);
		}
	}
	
}
