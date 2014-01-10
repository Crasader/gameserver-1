package com.xinqihd.sns.gameserver.admin.item;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyMiniTablePanel;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.ScriptHook;

public class EditRewardDialog extends MyDialog {
	
	private MyMiniTablePanel rewardsTf = new MyMiniTablePanel();
	private AddOrEditRewardAction addRewardAction = new AddOrEditRewardAction();
	private ArrayList<Reward> rewards = null;
	
	public EditRewardDialog(List<Reward> rewards) {
		this.rewards = new ArrayList<Reward>(rewards);
		init();
	}
	
	public void init() {
		TBoxTableModel tboxModel = new TBoxTableModel(
				this.rewards, ScriptHook.ITEM_BOX_RANDOM_BOX.getHook(), 1.0);
		addRewardAction.setTableModel(tboxModel);
		rewardsTf.setTableModel(tboxModel);
		this.rewardsTf.setTitle("宝箱奖励");
		this.rewardsTf.setAddRowAction(addRewardAction);
		
		this.setSize(600, 400);
		this.setResizable(true);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);

		this.setLayout(new MigLayout("wrap 1, ins 15px"));
		this.add(rewardsTf, "width 100%, height 100%");
	}
	
	public List<Reward> getRewards() {
		return this.rewards;
	}
}
