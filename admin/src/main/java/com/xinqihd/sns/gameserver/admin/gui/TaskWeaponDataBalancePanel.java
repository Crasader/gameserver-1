package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXPanel;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.data.WeaponBalanceResult;
import com.xinqihd.sns.gameserver.admin.data.WeaponBalanceResultModel;
import com.xinqihd.sns.gameserver.admin.data.WeaponBalanceSettingPanel;
import com.xinqihd.sns.gameserver.admin.data.WeaponBalanceTableRefreshAction;
import com.xinqihd.sns.gameserver.admin.data.WeaponBalanceTestConfig;
import com.xinqihd.sns.gameserver.admin.data.WeaponBalanceTestService;
import com.xinqihd.sns.gameserver.admin.gui.ext.HtmlDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.util.MyWindowUtil;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 游戏战斗数据平衡性计算工具
 * 
 * @author wangqi
 *
 */
public class TaskWeaponDataBalancePanel extends MyPanel implements
		ActionListener {
	
	private static TaskWeaponDataBalancePanel instance = new TaskWeaponDataBalancePanel();
	
	private WeaponBalanceSettingPanel user1Panel = new WeaponBalanceSettingPanel("用户1设置");
	private WeaponBalanceSettingPanel user2Panel = new WeaponBalanceSettingPanel("用户2设置");
	
	private JLabel combatCountLabel = new JLabel("模拟战斗次数", JLabel.RIGHT);
	private JSpinner combatCountField = new JSpinner();
	private JLabel levelLabel = new JLabel("用户等级差", JLabel.RIGHT);
	private JSpinner levelField = new JSpinner();
	private JLabel strengthLabel = new JLabel("强化等级差", JLabel.RIGHT);
	private JSpinner strengthField = new JSpinner();
	private JXButton startBtn = new JXButton("开始模拟");
	
	private WeaponBalanceResultModel myTableModel = new WeaponBalanceResultModel();
	private MyTablePanel myTable = new MyTablePanel();
	private MapEditorRenderFactory displayDialog = new MapEditorRenderFactory();
	
	public TaskWeaponDataBalancePanel() {
		init();
	}
	
	public static TaskWeaponDataBalancePanel getInstance() {
		return instance;
	}
	
	public void init() {
		ToolTipManager.sharedInstance().setDismissDelay(86400000);
		this.combatCountField.setValue(10);
		this.levelField.setValue(5);
		this.strengthField.setValue(5);
		this.startBtn.addActionListener(this);
		this.startBtn.setActionCommand(ActionName.OK.name());
		this.myTable.setTableModel(myTableModel);
		this.myTable.setEnableAddRow(false);
		this.myTable.setEnableDelRow(true);
		this.myTable.setEnableSaveButton(false);
		this.myTable.setEditable(true);
		this.myTable.setEnableRrefresh(true);
		this.myTable.setRefreshAction(new WeaponBalanceTableRefreshAction(myTable));
		this.myTable.getTable().setDefaultRenderer(Object.class, new BlanceResultRenderer());
		this.myTable.getTable().setEditorFactory(displayDialog);
		
		JXPanel userPanel = new JXPanel(new MigLayout("wrap 2"));
		userPanel.add(user1Panel, "width 50%, height 100%, grow");
		userPanel.add(user2Panel, "width 50%, height 100%, grow");

		this.setLayout(new MigLayout("wrap 2"));
		
		this.add(userPanel, "span, width 100%, height 30%");
		
		this.add(combatCountLabel, "span, split 7");
		this.add(combatCountField, "width 10%");
		this.add(levelLabel, "width 10%");
		this.add(levelField, "width 10%");
		this.add(strengthLabel, "width 10%");
		this.add(strengthField, "width 10%");
		this.add(startBtn, "width 10%");
		
		this.add(myTable, "span, width 100%, height 70%, grow");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			WeaponBalanceTestConfig user1Config = this.user1Panel.getWeaponBalanceTestConfig();
			WeaponBalanceTestConfig user2Config = this.user2Panel.getWeaponBalanceTestConfig();
			int count = StringUtil.toInt(combatCountField.getValue().toString(), 1);
			int levelDiff = StringUtil.toInt(levelField.getValue().toString(), 5);
			int strengthDiff = StringUtil.toInt(strengthField.getValue().toString(), 5);
			WeaponBalanceTestService service = new WeaponBalanceTestService(
					myTableModel, user1Config, user2Config, count, levelDiff, 
					strengthDiff, myTable);
			service.execute();
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable(){
			public void run() {
//				for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
//					if ( "Nimbus".equals(info.getName()) ) {
//						try {
//							UIManager.setLookAndFeel(info.getClassName());
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						break;
//					}
//				}
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(TaskWeaponDataBalancePanel.getInstance());
				frame.setSize(800, 800);
				frame.setVisible(true);
			}
		});
	}
	
	private class BlanceResultRenderer extends DefaultTableCellRenderer {
		
		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			JComponent comp = null;
			if ( value instanceof User ) {
				User user = (User)value;
				comp = (JComponent)super.getTableCellRendererComponent(
						table, user.getUsername(), isSelected, hasFocus, row, column);
			} else {
				comp = (JComponent)super.getTableCellRendererComponent(
						table, value.toString(), isSelected, hasFocus, row, column);
				String columnName = table.getColumnName(column);
			}
			return comp;
		}
		
	}
		
	class MapEditorRenderFactory implements MyTableCellEditorFactory {

		/* (non-Javadoc)
		 * @see com.xinqihd.sns.gameserver.admin.gui.ext.MyTableCellEditorFactory#getCellEditor(int, int, java.lang.String, javax.swing.table.TableModel, javax.swing.JTable)
		 */
		@Override
		public TableCellEditor getCellEditor(int row, int column,
				String columnName, TableModel tableModel, JTable table) {
		
			int modelRow = table.convertRowIndexToModel(row);
			final WeaponBalanceResult result = (WeaponBalanceResult)myTableModel.getRowObject(modelRow);
			
			StringBuilder buf = printResult(result);
			
			final HtmlDialog dialog = MyWindowUtil.getHtmlDialog(buf.toString(), 650, 650);
			JXButton simulateBtn = new JXButton("再次模拟");
			simulateBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JTextPane text = dialog.getTextPane();
					WeaponBalanceResult r = WeaponBalanceTestService.simulateCombat(
							result.getUser1(), result.getUser2(),
							result.getConfig1(), result.getConfig2());
					StringBuilder buf = printResult(r);
					text.setText(buf.toString());
				}
			});
			dialog.add(simulateBtn, "newline, align center");
			dialog.setVisible(true);
			
			return null;
		}

		public StringBuilder printResult(final WeaponBalanceResult result) {
			StringBuilder buf = new StringBuilder(200);
			buf.append("<html>");
			User user1 = result.getUser1();
			User user2 = result.getUser2();
			printUserData(user1, result.getUser1Exp(), buf, result.getUser1Win()==0);
			printUserData(user2, result.getUser2Exp(), buf, result.getUser1Win()==1);
		
			buf.append("<br>");
			buf.append("<h2>").append("战斗过程").append("</h2>");
			ArrayList<HashMap<String, Object>> details = 
					((WeaponBalanceResult)result).getDetailCombat();
			boolean user1Win = result.getUser1Win()==0;
			boolean user2Win = result.getUser1Win()==1;
			for ( HashMap<String,Object> detail : details ) {
				int roundNumber = (Integer)detail.get(WeaponBalanceResult.ROUND);
				boolean attackDir = (Boolean)detail.get(WeaponBalanceResult.ATTACK);
				double attackRatio = (Double)detail.get(WeaponBalanceResult.ATT_RATIO);
				double critRatio = (Double)detail.get(WeaponBalanceResult.CRI_RATIO);
				String attacker = result.getUser2().getUsername();
				String attackee = result.getUser1().getUsername();
				int hurt = (Integer)detail.get(WeaponBalanceResult.HURT);
				int blood = (Integer)detail.get(WeaponBalanceResult.BLOOD);
				boolean attackerWin = user1Win;
				boolean attackeeWin = user2Win;
				if ( attackDir ) {
					attackerWin = user2Win;
					attackeeWin = user1Win;
					attacker = result.getUser2().getUsername();
					attackee = result.getUser1().getUsername();
				} else {
					attacker = result.getUser1().getUsername();
					attackee = result.getUser2().getUsername();
				}
				buf.append("<tr><td>回合:").append(roundNumber).append("<td").
					append(attackerWin?" bgcolor='red'>":" bgcolor='white'>").append(attacker).
					append("<td>-><td").append(attackeeWin?" bgcolor='red'>":" bgcolor='white'>").append(attackee).
					append("<td>攻击倍率<td>").append(((int)(attackRatio*100))/100.0).append("<td>暴击倍率<td>"+critRatio).
					append("<td>最终伤害<td>").append(hurt).append("<td>").append(attackee).append("<td>剩余血量<td>").append(blood);

				buf.append("<br>");
			}
			buf.append("</table><br>模拟时间:").append((new Date())).append("<br>");
			buf.append("<br>");
			WeaponBalanceTestConfig config1 = result.getConfig1();
			WeaponBalanceTestConfig config2 = result.getConfig2();
			buf.append(config1).append("<br>");
			buf.append(config2).append("<br>");
			buf.append("</html>");
			return buf;
		}
		
		private void printUserData(User user, int exp, StringBuilder buf, boolean win) {
			buf.append("<h2").append(win?" bgcolor='red'>":">").append(user.getUsername()).append(win?"(胜利)":"").append("</h2>");
			buf.append("<br>获得经验: ").append(exp).append("<br>");
			buf.append("<table><tr>");
			buf.append("<td>等级<td>"+user.getLevel());
			buf.append("<td>血量<td>"+user.getBlood());
			buf.append("<td>攻击<td>"+user.getAttack());
			buf.append("<td>防御<td>"+user.getDefend());
			buf.append("<td>敏捷<td>"+user.getAgility());
			buf.append("<td>幸运<td>"+user.getLuck());
			LevelPojo level = LevelManager.getInstance().getLevel(user.getLevel());
			buf.append("<tr>");
			buf.append("<td>基础护甲<td>"+level.getSkin());
			buf.append("<td>基础血量<td>"+level.getBlood());
			buf.append("<td>基础攻击<td>"+level.getAttack());
			buf.append("<td>基础防御<td>"+level.getDefend());
			buf.append("<td>基础敏捷<td>"+level.getAgility());
			buf.append("<td>基础幸运<td>"+level.getLucky());
			buf.append("</table>");
			Bag bag = user.getBag();
			List<PropData> props = bag.getWearPropDatas();
			PropDataEquipIndex[] indexes = PropDataEquipIndex.values();
			buf.append("<table>");
			for ( int i=0; i<indexes.length; i++) {
				PropDataEquipIndex index = indexes[i];
				PropData prop = props.get(index.index());
				if ( prop != null ) {
					buf.append("<tr>");
					buf.append("<td>").append(index.name()).append("<td>").append(prop.getName());
					buf.append("<td>").append("强化等级").append("<td>").append(prop.getLevel());
					buf.append("<td>").append("+攻击").append("<td>").append(prop.getAttackLev());
					buf.append("<td>").append("+防御").append("<td>").append(prop.getDefendLev());
					buf.append("<td>").append("+敏捷").append("<td>").append(prop.getAgilityLev());
					buf.append("<td>").append("+幸运").append("<td>").append(prop.getLuckLev());
				} else {
//						buf.append("&nbsp;&nbsp;").append(index.name()).append(":").append("").append("<br>");
				}
			}
			buf.append("</table>");
		}
	}
}
