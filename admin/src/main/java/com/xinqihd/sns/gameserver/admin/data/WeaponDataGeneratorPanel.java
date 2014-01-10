package com.xinqihd.sns.gameserver.admin.data;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.AddRemoveList;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.ArrayListModel;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;

/**
 * 用来生成武器相关的数据
 * @author wangqi
 *
 */
public class WeaponDataGeneratorPanel extends MyPanel implements ActionListener {
	
//	private static String COMMAND_
	
	private static WeaponDataGeneratorPanel instance = new WeaponDataGeneratorPanel();
	private JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
	private JXPanel weaponPanel = new JXPanel();
	private JXLabel kindLabel = new JXLabel("请选择需要添加的武器前缀");
	private AddRemoveList list = null;
	private ArrayListModel listModel = null;
	private JXButton generateButton = new JXButton("生成随机武器数据");
	private MyTablePanel myTable = new MyTablePanel();
	private WeaponTableModel tableModel = new WeaponTableModel();
	
	private JXLabel attackLabel = new JXLabel("设定攻击转换为DPR的比率");
	private JXLabel defendLabel = new JXLabel("设定防御转换为DPR的比率");
	private JXLabel luckLabel = new JXLabel("设定幸运转换为DPR的比率");
	private JXLabel agilityLabel = new JXLabel("设定敏捷转换为DPR的比率");
	private JXLabel bloodLabel = new JXLabel("设定血量转换为DPR的比率");
	
	private JXTextField attackField = new JXTextField();
	private JXTextField defendField = new JXTextField();
	private JXTextField luckField = new JXTextField();
	private JXTextField agilityField = new JXTextField();
	private JXLabel bloodField = new JXLabel("设定血量转换为DPR的比率");
	
	private JXLabel dprLabel = new JXLabel("预设定DPR数值");
	private AddRemoveList dprList = new AddRemoveList(new Integer[]{
			11,
			45,
			130,
			265,
			450,
			685,
			970,
			1305,
			1690,
			2125,
			2610,
	});
	
	private String saveDatabase = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String saveNamespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private String saveCollection = "equipments_new";

	public WeaponDataGeneratorPanel() {
		init();
	}
	
	public static WeaponDataGeneratorPanel getInstance() {
//		return instance;
		return new WeaponDataGeneratorPanel();
	}
	
	public void init() {
		list = new AddRemoveList(new String[]{
				"黑铁",
				"青铜",
				"赤钢",
				"白银",
				"黄金",
				"琥珀",
				"翡翠",
				"水晶",
				"钻石",
				"神圣",
		});
		listModel = list.getListModel();
//		if ( listModel.getSize() <= 0 ) {
//			Set<String> arrays = ConfigManager.getConfigAsStringArray(ConfigKey.genWeaponNamePrefix);
//			for ( String name : arrays ) {
//				listModel.insertRow(name);
//			}
//		}
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setSaveButtonAction(new WeaponDataSaveAction(this.tableModel, saveDatabase, saveNamespace, saveCollection));
		myTable.setEnableDelRow(false);
		myTable.setEnableAddRow(false);
		myTable.setTableModel(tableModel);
		
		attackField.setText("1.3");
		attackField.setToolTipText("1(伤害)=1.3(攻击). 攻击可以表示为DPS的线性关系，我认为0.75倍率较好,即0.75*Attack=DPR");
		defendField.setText("1.5");
		defendField.setToolTipText("1(伤害)=1.5(防御)	.  防御直接增加护甲数值，也采用0.75倍率，即防御=0.75*(2*DPR)");
		luckField.setText("4253.9");
		luckField.setToolTipText("1倍伤害=4253.9幸运.  幸运值为0-10000,表示暴击的倍率和伤害倍率，我认为幸运值即为暴击率，而暴击倍率等于1.5+2*暴击率，所以幸运值为10000时提升3.5倍DPR");
		agilityField.setText("3740");
		agilityField.setToolTipText("1倍伤害=3740敏捷.  体力默认为210,敏捷值范围为0-10000,体力增量=敏捷值/20,最大可增加500体力");
		bloodField.setText("0.5");
		bloodField.setToolTipText("1倍伤害=187体力.  1倍伤害=0.5倍血量");
		
		JPanel paramPanel = new JPanel();
		paramPanel.setLayout(new MigLayout("wrap 2"));
		paramPanel.add(attackLabel, "sg lbl");
		paramPanel.add(attackField, "sg fd, growx, pushx");
		paramPanel.add(defendLabel, "sg lbl");
		paramPanel.add(defendField, "sg fd, growx, pushx");
		paramPanel.add(luckLabel, "sg lbl");
		paramPanel.add(luckField, "sg fd, growx, pushx");
		paramPanel.add(agilityLabel, "sg lbl");
		paramPanel.add(agilityField, "sg fd, growx, pushx");
		
		generateButton.setActionCommand(ActionName.OK.name());
		generateButton.addActionListener(this);
		
//		JXPanel dprPanel = new JXPanel();
//		dprPanel.setLayout(new MigLayout("wrap 1"));
//		dprPanel.add(dprLabel, "width 100%");
//		dprPanel.add(new JScrollPane(dprList), "width 100%");
		
		weaponPanel.setLayout(new MigLayout("wrap 1"));
		weaponPanel.add(kindLabel, "split 3, width 30%");
		weaponPanel.add(dprLabel,  "width 30%, wrap");
		weaponPanel.add(list, "split 3, width 30%");
		weaponPanel.add(dprList, "width 30%");
		weaponPanel.add(paramPanel, "width 40%");
		weaponPanel.add(generateButton, "align center");
		weaponPanel.add(myTable, "width 100%, growy, push");
		
		tabPane.addTab("武器数值生成", ImageUtil.createImageSmallIcon("Chart Bar.png", "data"), weaponPanel);
		
		this.setLayout(new MigLayout());
		this.add(tabPane, "width 100%, height 100%");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand()) ) {
			List<String> nameList = listModel.getList();
			if ( nameList.size() <= 0 ) {
				JOptionPane.showMessageDialog(null, "还没有输入名称前缀");
			} else {
				ConfigManager.saveConfigKeyValue(ConfigKey.genWeaponNamePrefix, nameList);
				double[] params = new double[4];
				params[0] = Double.parseDouble(attackField.getText());
				params[1] = Double.parseDouble(defendField.getText());
				params[2] = Double.parseDouble(luckField.getText());
				params[3] = Double.parseDouble(agilityField.getText());
				List<Integer> dprUnitList = new ArrayList<Integer>();
				int dprSize = dprList.getListModel().getSize();
				for ( int i=0; i<dprSize; i++ ) {
					dprUnitList.add((Integer)dprList.getListModel().getElementAt(i));
				}
				WeaponDataGeneratorService service = new WeaponDataGeneratorService(nameList, dprUnitList, params);
				JDialog dialog = service.getDialog();
				service.execute();
				dialog.setVisible(true);
				
				try {
					List<WeaponPojo> genWeapons = service.get();
					tableModel.setWeapons(genWeapons);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable(){
			public void run() {
				for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
					if ( "Nimbus".equals(info.getName()) ) {
						try {
							UIManager.setLookAndFeel(info.getClassName());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				}
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(WeaponDataGeneratorPanel.getInstance());
				frame.setSize(800, 800);
				frame.setVisible(true);
			}
		});
	}
}
