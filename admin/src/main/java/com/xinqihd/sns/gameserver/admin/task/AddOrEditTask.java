package com.xinqihd.sns.gameserver.admin.task;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyMiniTablePanel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.TaskFuncId;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class AddOrEditTask extends MyDialog implements ActionListener {
	
	private static final String COMMAND_SCRIPT_SELECT = "scriptSelect";
	private static final String COMMAND_USEDATE_SELECT = "useDateSelect";
	
	private TaskPojo task = new TaskPojo();
	
	private JXLabel idLbl = new JXLabel("任务ID:");
	private JXLabel nameLbl = new JXLabel("名称:");
	private JXLabel descLbl = new JXLabel("描述:");
	private JXLabel targetLbl = new JXLabel("目标:");
	private JXLabel levelLbl = new JXLabel("最高等级:");
	private JXLabel minLevelLbl = new JXLabel("最低等级:");
	private JXLabel seqLbl = new JXLabel("呈现顺序:");
	private JXLabel typeLbl = new JXLabel("任务类型:");
	private JXLabel scriptLbl = new JXLabel("任务钩子:");
	private JXLabel scriptDescLbl = new JXLabel();
	private JXLabel postCheckScriptLbl = new JXLabel("领取前钩子:");
	private JXLabel postScriptLbl = new JXLabel("领取钩子:");
	private JXLabel serverIdLbl = new JXLabel("服务器ID:");
	private JXLabel cond1Lbl = new JXLabel("完成条件1:");
	private JXLabel cond2Lbl = new JXLabel("完成条件2:");
	private JXLabel expLbl = new JXLabel("经验:");
	private JXLabel goldenLbl = new JXLabel("金币:");
	private JXLabel voucherLbl = new JXLabel("礼券:");
	private JXLabel medalLbl = new JXLabel("功勋:");
	private JXLabel guildWealthLbl = new JXLabel("公会财富:");
	private JXLabel guildCreditLbl = new JXLabel("公会贡献:");
	private JXLabel yuanbaoLbl = new JXLabel("元宝:");

	/**
	 * 为活动类型的任务新增加了如下字段
	 */
	private JXLabel useDateLbl = new JXLabel("是否激活日期");
	private JXLabel startDateLbl = new JXLabel("开启日期");
	private JXLabel endDateLbl = new JXLabel("截止日期");
	private JXLabel takeBeginHourLbl = new JXLabel("领取的开始小时");
	private JXLabel takeBeginMinLbl = new JXLabel("领取的开始分钟");
	private JXLabel takeEndHourLbl = new JXLabel("领取的结束小时");
	private JXLabel takeEndMinLbl = new JXLabel("领取的结束分钟");
	private JXLabel enableFuncLbl = new JXLabel("是否激活功能ID");
	private JXLabel funcIdLbl = new JXLabel("功能ID");
	private JXLabel dailyLbl = new JXLabel("是否每日重复");
	private JXLabel channelLbl = new JXLabel("渠道");
	private JXLabel inputCodeLbl = new JXLabel("是否需要激活码");
	private JXLabel giftDescLbl = new JXLabel("奖励描述");
	
	private JXTextField idField = new JXTextField();
	private JXTextField nameField = new JXTextField();
	private JXTextArea descField = new JXTextArea();
	private JXTextField targetField = new JXTextField();
	private JSpinner levelField = new JSpinner();
	private JSpinner minLevelField = new JSpinner();
	private JSpinner seqField = new JSpinner();
	private JXComboBox typeField = new JXComboBox();
	private JXComboBox scriptField = new JXComboBox();
	private JXTextField postScriptField = new JXTextField();
	private JXTextField postCheckScriptField = new JXTextField();
	private JXTextField serverIdField = new JXTextField();
	private JSpinner cond1Field = new JSpinner();
	private JSpinner cond2Field = new JSpinner();
	private JSpinner expField = new JSpinner();
	private JSpinner goldenField = new JSpinner();
	private JSpinner voucherField = new JSpinner();
	private JSpinner medalField = new JSpinner();
	private JSpinner guildWealthField = new JSpinner();
	private JSpinner guildCreditField = new JSpinner();
	private JSpinner yuanbaoField = new JSpinner();
	private JCheckBox broadcastField = new JCheckBox("是否广播");
	private JCheckBox disabeField = new JCheckBox("是否隐藏");
	private JXTextArea giftDescField = new JXTextArea();
	
	/**
	 * 为活动类型的任务新增加了如下字段
	 */
	private JCheckBox useDateField = new JCheckBox();
	private JXDatePicker startDateField = new JXDatePicker(new Date());
	private JXDatePicker endDateField = new JXDatePicker(new Date());
	private JSpinner takeBeginHourField = new JSpinner();
	private JSpinner takeBeginMinField = new JSpinner();
	private JSpinner takeEndHourField = new JSpinner();
	private JSpinner takeEndMinField = new JSpinner();
	private JCheckBox enableFuncField = new JCheckBox();
	private JXComboBox funcIdField = new JXComboBox(TaskFuncId.values());
	private JCheckBox dailyField = new JCheckBox();
	private JXTextField channelField = new JXTextField();
	private JCheckBox userInputField = new JCheckBox();
	private JXTextField inputCodeField = new JXTextField("激活码KEY（英文或数字）");
	
	private JXButton okButton = new JXButton("保存");
	private JXButton cancelButton = new JXButton("取消");
	
	private TaskAwardTableModel awardModel = null;
	private DefaultComboBoxModel typeModel = new DefaultComboBoxModel(TaskType.values());
	private DefaultComboBoxModel scriptModel = new DefaultComboBoxModel();
	private MyMiniTablePanel myTable = new MyMiniTablePanel();
	
	private boolean createdNew = true;
	
	private static final Map<ScriptHook, String> DESC = new LinkedHashMap<ScriptHook, String>();
	static {
		DESC.put(ScriptHook.TASK_ANY_COMBAT,		"完成一定数量战斗(完成条件2)");
		DESC.put(ScriptHook.TASK_ANY_COMBAT_WIN,		"取得一定数量战斗的胜利(完成条件2)");
		DESC.put(ScriptHook.TASK_BEAT_USERS,		"战胜一定数量的对手(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_ATTACKADD10,		"战斗中使用指定数量的附加10%攻击(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_ATTACKADD50,		"战斗中使用指定数量的附加50%攻击(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_ATTACKONE,		"战斗中使用指定数量的附加1次攻击(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_ATTACKTWO,		"战斗中使用指定数量的附加2攻击(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_BLOOD,		"战斗中使用指定数量的加血(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_BRANCHTREE,		"战斗中使用指定数量的三叉戟(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_CHANGEWIND, "战斗中使用指定数量的改变风向(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_FROZEN,		"战斗中使用指定数量的冰冻(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_GUIDE,		"战斗中使用指定数量的引导(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_HIDDEN,		"战斗中使用指定数量的隐藏(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_POW,		"战斗中使用指定数量的大招(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_TEAMHIDE,		"战斗中使用指定数量的团队隐身(完成条件2)");
		DESC.put(ScriptHook.TASK_BUY_ITEM_BY_GOLDEN,		"在商城中用金币购买指定数量的物品(完成条件2)");
		DESC.put(ScriptHook.TASK_BUY_ITEM_BY_MEDAL,		"在商城中用勋章购买指定数量的物品(完成条件2)");
		DESC.put(ScriptHook.TASK_BUY_ITEM_BY_VOUCHER,		"在商城中用礼券购买指定数量的物品(完成条件2)");
		DESC.put(ScriptHook.TASK_BUY_ITEM_BY_YUANBAO,		"在商城中用元宝购买指定数量的物品(完成条件2)");
		DESC.put(ScriptHook.TASK_CRAFT_COMPOSE_FIRE,		"成功熔炼指定数量和等级的火神石(完成条件1:等级,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_CRAFT_COMPOSE_WATER,		"成功熔炼指定数量和等级的水神石(完成条件1:等级,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_CRAFT_COMPOSE_COLOR,		"成功熔炼指定数量的颜色装备(完成条件1)");
		DESC.put(ScriptHook.TASK_CRAFT_COMPOSE_EQUIP,		"成功熔炼指定数量的装备(完成条件1)");
		DESC.put(ScriptHook.TASK_CRAFT_COMPOSE_WEAPON,	"成功熔炼指定数量的武器(完成条件1)");
		DESC.put(ScriptHook.TASK_CRAFT_FORGE_FIRE,		"合成指定数量和等级的火神石(完成条件1:等级,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_CRAFT_FORGE_WATER,		"合成指定数量和等级的水神石(完成条件1:等级,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_LOGIN,		"登陆指定的次数(完成条件2)");
		DESC.put(ScriptHook.TASK_LOGIN_DATE,		"连续登陆指定的天数(完成条件2)");
		DESC.put(ScriptHook.TASK_SINGLE_COMBAT,		"完成指定数量的单人对战(完成条件2)");
		DESC.put(ScriptHook.TASK_SINGLE_COMBAT_WIN,		"赢得指定数量的单人对战场数(完成条件2)");
		DESC.put(ScriptHook.TASK_STRENGTH_CLOTHES,		"强化指定数量的衣服(完成条件1:等级,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_STRENGTH_HAT,		"强化指定数量的帽子(完成条件1:等级,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_STRENGTH_WEAPON,		"强化指定数量的武器(完成条件1:等级,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_TEAM_COMBAT,		"完成指定数量的多人对战(完成条件2)");
		DESC.put(ScriptHook.TASK_TEAM_COMBAT_WIN,		"赢得指定数量的多人对战场数(完成条件2)");
		DESC.put(ScriptHook.TASK_TRAINING,		"完成指定数量的训练场(完成条件2)");
		DESC.put(ScriptHook.TASK_USER_LEVELUP,		"用户升级至指定级别(完成条件1:等级)");
		DESC.put(ScriptHook.TASK_WEAR_CLOTHES,		"用户装备指定数量的服装道具(完成条件2)");
		DESC.put(ScriptHook.TASK_USER_GOLDEN,		"用户的金币达到指定数量(完成条件2)");
		DESC.put(ScriptHook.TASK_USER_YUANBAO,		"用户的元宝达到指定数量(完成条件2)");
		DESC.put(ScriptHook.TASK_USER_POWER,		"用户的战斗力达到指定数值(完成条件2)");
		DESC.put(ScriptHook.TASK_USER_RANK_POWER,		"用户的战斗力排名达到指定名次(完成条件2)");
		DESC.put(ScriptHook.TASK_USER_RANK_WEALTH,		"用户的财富排名达到指定名次(完成条件2)");
		DESC.put(ScriptHook.TASK_ADD_ITEM,		"用户获得了一项新的道具(完成条件1:道具ID,完成条件2:数量)");
		DESC.put(ScriptHook.TASK_USER_BAG_COUNT,		"用户背包中的物品达到指定数量(完成条件2)");
		DESC.put(ScriptHook.TASK_BUFF_TOOL_FLY,		"战斗中使用指定数量的纸飞机(完成条件2)");
		DESC.put(ScriptHook.TASK_SELL_GOOD,		"向商城出售指定数量的商品(完成条件1:道具ID或者0, 完成条件2:数量)");
		DESC.put(ScriptHook.TASK_WEIBO_ACHIEVEMENT,	"发送指定数量的成就类型微博(完成条件2)");
		DESC.put(ScriptHook.TASK_WEIBO_ANYTYPE,		  "发送指定数量的任意类型微博(绑定操作不计算)(完成条件2)");
		DESC.put(ScriptHook.TASK_WEIBO_BOUND,		    "绑定微博(完成条件2必须为1)");
		DESC.put(ScriptHook.TASK_WEIBO_COMBAT,		  "发送指定数量的战斗类型微博(完成条件2)");
		DESC.put(ScriptHook.TASK_WEIBO_FORGE,		    "发送指定数量的强化类型微博(完成条件2)");
		DESC.put(ScriptHook.TASK_WEIBO_LEVELUP,		  "发送指定数量的升级类型微博(完成条件2)");
		DESC.put(ScriptHook.TASK_WEIBO_RANKING,		  "发送指定数量的排名类型微博(完成条件2)");
		//2012-09-28
		DESC.put(ScriptHook.TASK_FRIEND_COMBAT,		  "参加指定数量的好友对战或挑战(完成条件2)");
		DESC.put(ScriptHook.TASK_OFFLINE_COMBAT,		"发起指定数量的离线挑战(完成条件2)");
		DESC.put(ScriptHook.TASK_CAISHEN_PRAY,		  "使用指定数量的祈福功能(完成条件2)");
		DESC.put(ScriptHook.TASK_TREASURE_HUNT,		  "使用指定数量的寻宝功能(完成条件2)");
		DESC.put(ScriptHook.TASK_CHECK_RANKING,		  "查看指定数量的排行榜(完成条件2)");
		DESC.put(ScriptHook.TASK_CHAT_WORLD,		    "使用指定数量的小喇叭聊天(完成条件2)");
		DESC.put(ScriptHook.TASK_ADD_FRIEND,		    "添加指定数量的好友(完成条件2)");
		DESC.put(ScriptHook.TASK_PVE_COMBAT_WIN,		"完成指定数量的团队副本任务(完成条件2)");
		//2012-12-11
		DESC.put(ScriptHook.TASK_CHARGE,		    "完成累计充值任务(完成条件2)");
		DESC.put(ScriptHook.TASK_COLLECT,		    "完成收集类型的任务(完成条件1为物品的ID，完成条件2为收集数量)");
		DESC.put(ScriptHook.TASK_CHARGEFIRST,		"完成首次充值任务(完成条件2必须为1)");
		DESC.put(ScriptHook.TASK_EXPGAIN,		    "完成双倍经验任务(完成条件1为12001300格式，表示12:00-13:00之间启动)");
		DESC.put(ScriptHook.TASK_NOSCRIPT,      "公告性质的任务，无完成条件");
		DESC.put(ScriptHook.TASK_VIP_LEVELUP,		"升级到指定VIP等级(完成条件2)");
		//2013-03-12
		DESC.put(ScriptHook.TASK_JOIN_GUILD,		      "创建或者加入1个公会");
		DESC.put(ScriptHook.TASK_GUILD_COMBAT,		    "完成指定数量的公会对战(完成条件2)");
		DESC.put(ScriptHook.TASK_GUILD_COMBAT_WIN,		"赢得指定数量的公会对战场数(完成条件2)");
	};
	
	public AddOrEditTask() {
		init();
	}
	
	public AddOrEditTask(TaskPojo task, boolean createNew) {
		this.createdNew = createNew;
		if ( task != null ) {
			this.task.setId(task.getId());
			this.task.setName(task.getName());
			this.task.setCondition1(task.getCondition1());
			this.task.setScript(task.getScript());
			this.task.setSeq(task.getSeq());
			this.task.setStep(task.getStep());
			this.task.setTaskTarget(task.getTaskTarget());
			this.task.setBroadcast(task.isBroadcast());
			this.task.setTicket(task.getTicket());
			this.task.setGold(task.getGold());
			this.task.setGongxun(task.getGongxun());
			this.task.setExp(task.getExp());
			this.task.setDesc(task.getDesc());
			this.task.setGiftDesc(task.getGiftDesc());
			this.task.setCaifu(task.getCaifu());
			this.task.setUserLevel(task.getUserLevel());
			this.task.setMinUserLevel(task.getMinUserLevel());
			this.task.setType(task.getType());
			this.task.setStartMillis(task.getStartMillis());
			this.task.setEndMillis(task.getEndMillis());
			this.task.setTakeBeginHour(task.getTakeBeginHour());
			this.task.setTakeBeginMin(task.getTakeBeginMin());
			this.task.setTakeEndHour(task.getTakeEndHour());
			this.task.setTakeEndMin(task.getTakeEndMin());
			this.task.setDaily(task.isDaily());
			this.task.setFuncId(task.getFuncId());
			this.task.setChannel(task.getChannel());
			this.task.setInputCode(task.isInputCode());
			this.task.setInputKey(task.getInputKey());
			this.task.setDisable(task.isDisable());
			this.task.setPostScript(task.getPostScript());
			this.task.setPostCheckScript(task.getPostCheckScript());
			this.task.setServerId(task.getServerId());
			this.task.setGuildWealth(task.getGuildWealth());
			this.task.setGuildCredit(task.getGuildCredit());
			List<Award> awards = task.getAwards();
			for ( Award award: awards ) {
				this.task.addAward(award);
			}
		}
		init();
	}
	
	public void init() {
		if (!createdNew) {
			this.idField.setEnabled(false);
		}
		this.idField.setColumns(20);
		this.descField.setToolTipText("任务描述");
		this.descField.setColumns(100);
		this.descField.setRows(2);
		this.giftDescField.setColumns(100);
		this.giftDescField.setRows(20);
		this.targetField.setToolTipText("任务目标");
		this.levelField.setToolTipText("任务对玩家的等级要求");
		this.seqField.setToolTipText("任务呈现的先后顺序");
		this.cond1Field.setToolTipText("任务完成条件1，一般为等级");
		this.cond2Field.setToolTipText("任务完成条件2，一般为数量");
		this.yuanbaoField.setToolTipText("公会财富奖励");
		this.voucherField.setEnabled(false);
		this.medalField.setEnabled(false);
		this.guildWealthField.setEnabled(true);
		this.guildCreditField.setEnabled(true);
		this.typeField.setModel(typeModel);
		this.scriptField.setModel(scriptModel);
		this.okButton.setActionCommand(ActionName.OK.name());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		this.cancelButton.addActionListener(this);
		this.broadcastField.setToolTipText("勾选后，该任务解锁会向全局用户发送通知");
		this.disabeField.setToolTipText("勾选后，该任务将会隐藏");
		this.takeBeginHourField.setValue(-1);
		this.takeEndHourField.setValue(-1);
		this.startDateField.setEnabled(false);
		this.endDateField.setEnabled(false);
		
		for ( ScriptHook hook : ScriptHook.values() ) {
			if ( hook.name().startsWith("TASK_") ) {
				this.scriptModel.addElement(hook);
			}
		}
		if ( this.task != null ) {
			updateTaskPojo();
		}
		this.awardModel = new TaskAwardTableModel(task.getAwards());
		this.myTable.setTableModel(awardModel);
		this.myTable.setAddRowAction(new AddTaskAwardAction());
		this.myTable.getTable().setEditable(true);
		this.myTable.getTable().addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getClickCount() == 2 ) {
					int rowIndex = myTable.getTable().getSelectedRow();
					if ( rowIndex >= 0 ) {
						int modelRowIndex = myTable.getTable().convertRowIndexToModel(rowIndex);
						Award selectedTaskAward = null;
						selectedTaskAward = (Award)myTable.getTableModel().getRowObject(modelRowIndex);
						
						AddOrEditTaskReward dialog = new AddOrEditTaskReward(selectedTaskAward);
						dialog.setVisible(true);
						
						selectedTaskAward = dialog.getTaskAward();
						if ( selectedTaskAward != null ) {
							awardModel.updateRow(selectedTaskAward, modelRowIndex);
						}
					}
				}
			}
			
		});
		this.scriptField.setActionCommand(COMMAND_SCRIPT_SELECT);
		this.scriptField.addActionListener(this);
		this.useDateField.setActionCommand(COMMAND_USEDATE_SELECT);
		this.useDateField.addActionListener(this);
		
		this.setSize(1100, 700);
		this.setResizable(true);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);

		JXPanel infoPanel = new JXPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("任务基本信息"));
		infoPanel.setLayout(new MigLayout("wrap 10", "[10%][20%][10%][20%][10%][20%][10%][20%]"));
		infoPanel.add(idLbl, "sg lbl");
		infoPanel.add(idField, "sg fd");
		infoPanel.add(nameLbl, "sg lbl");
		infoPanel.add(nameField, "sg fd");
		infoPanel.add(targetLbl, "sg lbl");
		infoPanel.add(targetField, "sg fd");
		infoPanel.add(minLevelLbl, "sg lbl");
		infoPanel.add(minLevelField, "sg fd");
		infoPanel.add(levelLbl, "sg lbl");
		infoPanel.add(levelField, "sg fd");

		//infoPanel.add(descLbl, "sg lbl");
		JScrollPane descPane = new JScrollPane(descField);
		infoPanel.add(descPane, "newline, spanx 6, spany 4, height 150px");
		infoPanel.add(seqLbl, "sg lbl");
		infoPanel.add(seqField, "sg fd");
		infoPanel.add(broadcastField, "newline, sg fd");
		infoPanel.add(disabeField, "sg fd");
		infoPanel.add(postScriptLbl, "newline, sg lbl");
		infoPanel.add(postScriptField, "span, growx");
		infoPanel.add(postCheckScriptLbl, "newline, sg lbl");
		infoPanel.add(postCheckScriptField, "span, growx");
		infoPanel.add(serverIdLbl, "newline, sg lbl");
		infoPanel.add(serverIdField, "span, growx");

		JXPanel activityPanel = new JXPanel();
		activityPanel.setBorder(BorderFactory.createTitledBorder("活动任务设置"));
		activityPanel.setLayout(new MigLayout("wrap 8", "[10%][15%][10%][15%][10%][15%][10%][15%]"));
		activityPanel.add(useDateLbl, "sg lbl1");
		activityPanel.add(useDateField, "sg fb1");
		activityPanel.add(startDateLbl, "sg lbl1");
		activityPanel.add(startDateField, "sg fd1");
		activityPanel.add(endDateLbl, "sg lbl1");
		activityPanel.add(endDateField, "sg fd1");
		activityPanel.add(takeBeginHourLbl, "newline, sg lbl1");
		activityPanel.add(takeBeginHourField, "sg fd1");
		activityPanel.add(takeBeginMinLbl, "sg lbl1");
		activityPanel.add(takeBeginMinField, "sg fd1");
		activityPanel.add(takeEndHourLbl, "sg lbl1");
		activityPanel.add(takeEndHourField, "sg fd1");
		activityPanel.add(takeEndMinLbl, "sg lbl1");
		activityPanel.add(takeEndMinField, "sg fd1");
		activityPanel.add(enableFuncLbl, "sg lbl1");
		activityPanel.add(enableFuncField, "sg fd1");
		activityPanel.add(funcIdLbl, "sg lbl1");
		activityPanel.add(funcIdField, "sg fd1");
		activityPanel.add(dailyLbl, "sg lbl1");
		activityPanel.add(dailyField, "sg fd1");
		activityPanel.add(channelLbl, "sg lbl1");
		activityPanel.add(channelField, "sg fd1");
		activityPanel.add(inputCodeLbl, "sg lbl");
		activityPanel.add(userInputField, "sg fd1");
		activityPanel.add(inputCodeField, "sg fd1");

		JXPanel condPanel = new JXPanel();
		condPanel.setBorder(BorderFactory.createTitledBorder("任务完成条件"));
		condPanel.setLayout(new MigLayout("wrap 6", "[10%][25%][10%][25%][10%][25%]"));
		condPanel.add(scriptLbl, "sg lbl");
		condPanel.add(scriptField, "span, split 4");
		condPanel.add(scriptDescLbl, "growx");
		condPanel.add(typeLbl, "sg lbl");
		condPanel.add(typeField, "sg fd, growx");
		condPanel.add(cond1Lbl, "sg lbl");
		condPanel.add(cond1Field, "sg fd, growx");
		condPanel.add(cond2Lbl, "sg lbl");
		condPanel.add(cond2Field, "sg fd, growx");
		
		JTabbedPane rewardTabPanel = new JTabbedPane();
		JXPanel rewardPanel = new JXPanel();
		rewardPanel.setBorder(BorderFactory.createTitledBorder("任务完成奖励"));
		rewardPanel.setLayout(new MigLayout("wrap 14", 
				"[5%][10%][5%][10%][5%][10%][5%][10%][5%][10%]"));
		rewardPanel.add(expLbl, "sg lbl");
		rewardPanel.add(expField, "sg fd, growx");
		rewardPanel.add(goldenLbl, "sg lbl");
		rewardPanel.add(goldenField, "sg fd, growx");
		rewardPanel.add(yuanbaoLbl, "sg lbl");
		rewardPanel.add(yuanbaoField, "sg fd, growx");
		rewardPanel.add(voucherLbl, "sg lbl");
		rewardPanel.add(voucherField, "sg fd, growx");
		rewardPanel.add(medalLbl, "sg lbl");
		rewardPanel.add(medalField, "sg fd, growx");
		rewardPanel.add(guildCreditLbl, "sg lbl");
		rewardPanel.add(guildCreditField, "sg fd, growx");
		rewardPanel.add(guildWealthLbl, "sg lbl");
		rewardPanel.add(guildWealthField, "sg fd, growx");
		rewardPanel.add(myTable, "span, width 100%, height 30%, growy");
		rewardTabPanel.addTab("实物奖励", rewardPanel);
		JXPanel textPanel = new JXPanel();
		JScrollPane giftDescPane = new JScrollPane(giftDescField);
		textPanel.add(giftDescField);
		rewardTabPanel.addTab("公告信息", textPanel);
		
		infoPanel.setMaximumSize(new Dimension(1000, 300));
		activityPanel.setMaximumSize(new Dimension(1000, 300));
		condPanel.setMaximumSize(new Dimension(1000, 300));
		rewardTabPanel.setMaximumSize(new Dimension(1000, 300));
		
		JPanel globalPanel = new JPanel();
		globalPanel.setLayout(new MigLayout("wrap 1, ins 5px"));
		globalPanel.add(infoPanel, "width 100%, height 40%");
		globalPanel.add(activityPanel, "width 100%, height 20%");
		globalPanel.add(condPanel, "width 100%, height 20%");
		globalPanel.add(rewardTabPanel, "width 100%, height 20%");

		/*
		globalPanel.setLayout(new FlowLayout());
		globalPanel.add(infoPanel);
		globalPanel.add(activityPanel);
		globalPanel.add(condPanel);
		globalPanel.add(rewardTabPanel);
		*/
		
		JScrollPane pane = new JScrollPane(globalPanel);
		pane.setPreferredSize(new Dimension(1200, 700));
		
		this.setLayout(new MigLayout("wrap 1, ins 5px"));
		this.add(pane);

		this.add(okButton, "split 2, align center");
		this.add(cancelButton, "");
	}
	
	public void updateTaskPojo() {
		this.idField.setText(task.getId());
		this.nameField.setText(task.getName());
		this.descField.setText(task.getDesc());
		this.giftDescField.setText(task.getGiftDesc());
		this.targetField.setText(task.getTaskTarget());
		this.levelField.setValue(task.getUserLevel());
		this.minLevelField.setValue(task.getMinUserLevel());
		this.seqField.setValue(task.getSeq());
		this.broadcastField.setSelected(task.isBroadcast());
		this.cond1Field.setValue(task.getCondition1());
		this.cond2Field.setValue(task.getStep());
		ScriptHook hook = ScriptHook.getScriptHook(task.getScript());
		this.setScriptHook(hook);
		this.typeField.setSelectedItem(task.getType());
		this.expField.setValue(task.getExp());
		this.goldenField.setValue(task.getGold());
		this.voucherField.setValue(task.getTicket());
		this.medalField.setValue(task.getGongxun());
		this.guildWealthField.setValue(task.getGuildWealth());
		this.guildCreditField.setValue(task.getGuildCredit());
		this.yuanbaoField.setValue(task.getCaifu());
		this.startDateField.setDate(new Date(task.getStartMillis()));
		this.endDateField.setDate(new Date(task.getEndMillis()));
		if ( task.getStartMillis() > 0 ) {
			this.useDateField.setSelected(true);
		} else {
			this.useDateField.setSelected(false);
		}
		if ( task.getEndMillis() > 0 ) {
			this.useDateField.setSelected(true);
		} else {
			this.useDateField.setSelected(false);
		}
		if ( task.getFuncId() != null ) {
			this.enableFuncField.setSelected(true);
			this.funcIdField.setSelectedItem(task.getFuncId());
		} else {
			this.enableFuncField.setSelected(false);
		}
		this.takeBeginHourField.setValue(task.getTakeBeginHour());
		this.takeBeginMinField.setValue(task.getTakeBeginMin());
		this.takeEndHourField.setValue(task.getTakeEndHour());
		this.takeEndMinField.setValue(task.getTakeEndMin());
		this.dailyField.setSelected(task.isDaily());
		this.channelField.setText(task.getChannel());
		this.userInputField.setSelected(task.isInputCode());
		this.inputCodeField.setText(task.getInputKey());
		this.disabeField.setSelected(this.task.isDisable());
		this.postScriptField.setText(this.task.getPostScript());
		this.postCheckScriptField.setText(this.task.getPostCheckScript());
		this.serverIdField.setText(this.task.getServerId());
	}

	public TaskPojo getTaskPojo() {
		return this.task;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			/*
	private JXTextField idField = new JXTextField();
	private JXTextField nameField = new JXTextField();
	private JXTextField descField = new JXTextField();
	private JXTextField targetField = new JXTextField();
	private JSpinner levelField = new JSpinner();
	private JSpinner seqField = new JSpinner();
	private JXComboBox typeField = new JXComboBox();
	private JXComboBox scriptField = new JXComboBox();
	private JSpinner cond1Field = new JSpinner();
	private JSpinner cond2Field = new JSpinner();
	private JSpinner expField = new JSpinner();
	private JSpinner goldenField = new JSpinner();
	private JSpinner voucherField = new JSpinner();
	private JSpinner medalField = new JSpinner();
	private JSpinner guildField = new JSpinner();
			 */
			this.task.setId(this.idField.getText());
			this.task.setName(this.nameField.getText());
			this.task.setDesc(this.descField.getText());
			if ( StringUtil.checkNotEmpty(this.giftDescField.getText()) ) {
				this.task.setGiftDesc(this.giftDescField.getText());
			} else {
				this.task.setGiftDesc(null);
			}
			this.task.setTaskTarget(this.targetField.getText());
			this.task.setUserLevel((Integer)this.levelField.getValue());
			this.task.setMinUserLevel((Integer)this.minLevelField.getValue());
			this.task.setSeq((Integer)(this.seqField.getValue()));
			this.task.setBroadcast(this.broadcastField.isSelected());
			this.task.setType((TaskType)this.typeField.getSelectedItem());
			this.task.setScript(((ScriptHook)this.scriptField.getSelectedItem()).getHook());
			this.task.setCondition1((Integer)(this.cond1Field.getValue()));
			this.task.setStep((Integer)(this.cond2Field.getValue()));
			this.task.setExp((Integer)(this.expField.getValue()));
			this.task.setGold((Integer)(this.goldenField.getValue()));
			this.task.setTicket((Integer)(this.voucherField.getValue()));
			this.task.setGongxun((Integer)(this.medalField.getValue()));
			this.task.setGuildCredit((Integer)(this.guildCreditField.getValue()));
			this.task.setGuildWealth((Integer)(this.guildWealthField.getValue()));
			this.task.setCaifu((Integer)(this.yuanbaoField.getValue()));
			if ( this.useDateField.isSelected() ) {
				Date startDate = this.startDateField.getDate();
				Date endDate = this.endDateField.getDate();
				if ( startDate != null ) {
					this.task.setStartMillis(startDate.getTime());
				}
				if ( endDate != null ) {
					this.task.setEndMillis(endDate.getTime());
				}
			} else {
				this.task.setStartMillis(0);
				this.task.setEndMillis(0);
			}
			if ( this.enableFuncField.isSelected() ) {
				TaskFuncId funcId = (TaskFuncId)(this.funcIdField.getSelectedItem());
				this.task.setFuncId(funcId);
			}
			int startHour = (Integer)this.takeBeginHourField.getValue();
			int startMin = (Integer)this.takeBeginMinField.getValue();
			int endHour = (Integer)this.takeEndHourField.getValue();
			int endMin = (Integer)this.takeEndMinField.getValue();
			if ( startHour>=0 && endHour>=0 ) {
				this.task.setTakeBeginHour(startHour);
				this.task.setTakeBeginMin(startMin);
				this.task.setTakeEndHour(endHour);
				this.task.setTakeEndMin(endMin);
			}
			this.task.setDaily(this.dailyField.isSelected());
			this.task.setChannel(this.channelField.getText());
			if ( this.userInputField.isSelected() ) {
				this.task.setInputCode(true);
				this.task.setInputKey(this.inputCodeField.getText());
			} else {
				this.task.setInputCode(false);
				this.task.setInputKey("");
			}
			if ( StringUtil.checkNotEmpty(this.postScriptField.getText()) ) {
				this.task.setPostScript(this.postScriptField.getText());
			} else {
				this.task.setPostScript(null);
			}
			if ( StringUtil.checkNotEmpty(this.postCheckScriptField.getText()) ) {
				this.task.setPostCheckScript(this.postCheckScriptField.getText());
			} else {
				this.task.setPostCheckScript(null);
			}
			if ( StringUtil.checkNotEmpty(this.serverIdField.getText()) ) {
				this.task.setServerId(this.serverIdField.getText());
			} else {
				this.task.setServerId(null);
			}
			this.task.setDisable(this.disabeField.isSelected());
			if ( !StringUtil.checkNotEmpty(task.getId()) ) {
				JOptionPane.showMessageDialog(this, "任务ID不存在");
				return;
			} else {
				if ( task.getType() != TaskType.TASK_ACHIVEMENT ) {
					for ( Award award : this.task.getAwards() ) {
						if ( Constant.ACHIEVEMENT.equals(award.type) ) {
							JOptionPane.showMessageDialog(this, "成就奖励只能应用于成就类型的任务中");
							return;
						}
					}
				}
			}
			this.dispose();
		} else if ( ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			this.task = null;
			this.dispose();
		} else if ( COMMAND_SCRIPT_SELECT.equals(e.getActionCommand()) ) {
			ScriptHook hook = ScriptHook.valueOf(
					this.scriptField.getSelectedItem().toString());
			if ( hook != null ) {
				this.setScriptHook(hook);
				this.task.setScript(hook.getHook());
			}
		} else if ( COMMAND_USEDATE_SELECT.equals(e.getActionCommand()) ) {
			if ( this.useDateField.isSelected() ) {
				this.startDateField.setEnabled(true);
				this.endDateField.setEnabled(true);
				if ( this.startDateField.getDate().getTime() == 0 ) {
					this.startDateField.setDate(new Date());
				}
				if ( this.endDateField.getDate().getTime() == 0 ) {
					this.endDateField.setDate(new Date());
				}
			} else {
				this.startDateField.setEnabled(false);
				this.endDateField.setEnabled(false);
			}
		}
	}
	
	private void setScriptHook(ScriptHook hook) {
		if ( hook == null ) return;
		this.scriptField.setSelectedItem(hook);
		switch ( hook ) {
			case TASK_ANY_COMBAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_ANY_COMBAT_WIN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BEAT_USERS:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_ATTACKADD10:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_ATTACKADD50:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_ATTACKONE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_ATTACKTWO:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_BLOOD:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_BRANCHTREE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_CHANGEWIND:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_FROZEN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_GUIDE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_HIDDEN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_POW:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUFF_TOOL_TEAMHIDE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUY_ITEM_BY_GOLDEN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUY_ITEM_BY_MEDAL:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUY_ITEM_BY_VOUCHER:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_BUY_ITEM_BY_YUANBAO:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_CRAFT_COMPOSE_FIRE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_CRAFT_COMPOSE_WATER:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_CRAFT_COMPOSE_COLOR:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(false);
				break;
			case TASK_CRAFT_COMPOSE_EQUIP:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(false);
				break;
			case TASK_CRAFT_COMPOSE_WEAPON:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(false);
				break;
			case TASK_CRAFT_FORGE_FIRE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_CRAFT_FORGE_WATER:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_LOGIN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_LOGIN_DATE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_SINGLE_COMBAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_SINGLE_COMBAT_WIN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_STRENGTH_CLOTHES:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_STRENGTH_HAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_STRENGTH_WEAPON:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_TEAM_COMBAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_TEAM_COMBAT_WIN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_TRAINING:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_USER_LEVELUP:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(false);
				break;
			case TASK_WEAR_CLOTHES:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_USER_GOLDEN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_USER_YUANBAO:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_USER_POWER:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_USER_RANK_POWER:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_USER_RANK_WEALTH:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_ADD_ITEM:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_USER_BAG_COUNT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;		
			case TASK_BUFF_TOOL_FLY:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_SELL_GOOD:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_WEIBO_ACHIEVEMENT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_WEIBO_BOUND:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_WEIBO_ANYTYPE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_WEIBO_COMBAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_WEIBO_FORGE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_WEIBO_LEVELUP:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_WEIBO_RANKING:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_FRIEND_COMBAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_OFFLINE_COMBAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_CAISHEN_PRAY:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_TREASURE_HUNT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_CHECK_RANKING:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;	
			case TASK_CHAT_WORLD:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_ADD_FRIEND:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_PVE_COMBAT_WIN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_CHARGE:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_COLLECT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_CHARGEFIRST:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(false);
				this.cond2Field.setValue(1);
				break;
			case TASK_EXPGAIN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(false);
				this.cond2Field.setValue(1);
				break;
			case TASK_NOSCRIPT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(true);
				this.cond2Field.setEnabled(true);
				this.cond2Field.setValue(1);
				break;
			case TASK_VIP_LEVELUP:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_GUILD_COMBAT:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_GUILD_COMBAT_WIN:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			case TASK_JOIN_GUILD:
				this.scriptDescLbl.setText(DESC.get(hook));
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(true);
				break;
			default:
				this.scriptDescLbl.setText("还没有设定条件");
				this.cond1Field.setEnabled(false);
				this.cond2Field.setEnabled(false);
				break;
		}
	}
	
	private class AddTaskAwardAction extends AbstractAction {
		
		public AddTaskAwardAction() {
			super("", ImageUtil.createImageSmallIcon("Button Add.png", "Add Row"));
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			AddOrEditTaskReward dialog = new AddOrEditTaskReward();
			dialog.setVisible(true);
			
			Award newAward = dialog.getTaskAward();
			if ( newAward != null ) {
				awardModel.insertRow(newAward);
			}
		}
	}
}
