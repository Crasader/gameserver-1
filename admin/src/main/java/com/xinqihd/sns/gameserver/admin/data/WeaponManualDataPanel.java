package com.xinqihd.sns.gameserver.admin.data;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;

import com.xinqihd.sns.gameserver.admin.action.game.MyTableModelDeleteRowAction;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.ext.AddRemoveList;
import com.xinqihd.sns.gameserver.admin.gui.ext.HtmlDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.model.ArrayListModel;
import com.xinqihd.sns.gameserver.battle.BattleBitSetBullet;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 用来生成武器相关的数据
 * @author wangqi
 *
 */
public class WeaponManualDataPanel extends MyPanel implements ActionListener {
	
	private static String COMMAND_IMPORT = "import";
	private static String COMMAND_EXPORT = "export";
	private static String COMMAND_PRINT = "print";
	
	private static WeaponManualDataPanel instance = new WeaponManualDataPanel();
	private JXPanel weaponPanel = new JXPanel();
	private JXButton importButton = new JXButton("导入黑铁武器数值");
	private JXButton exportButton = new JXButton("导出黑铁武器数值");
	private JXButton printResultButton = new JXButton("输出各个等级武器攻击力数值");
	private MyTablePanel myTable = new MyTablePanel();
	private WeaponTableModel tableModel = new WeaponTableModel();
	
	private JXLabel attackLabel = new JXLabel("设定攻击转换为DPR的比率");
	private JXLabel defendLabel = new JXLabel("设定防御转换为DPR的比率");
	private JXLabel luckLabel = new JXLabel("设定幸运转换为DPR的比率");
	private JXLabel agilityLabel = new JXLabel("设定敏捷转换为DPR的比率");
	private JXLabel bloodLabel = new JXLabel("设定血量转换为DPR的比率");
	private JXLabel bulletLabel = new JXLabel("子弹文件路径");
	
	private JXTextField attackField = new JXTextField();
	private JXTextField defendField = new JXTextField();
	private JXTextField luckField = new JXTextField();
	private JXTextField agilityField = new JXTextField();
	private JXTextField bulletField = new JXTextField();
	
	private JXLabel bloodField = new JXLabel("设定血量转换为DPR的比率");
	
	private JXLabel levelLabel = new JXLabel("设定各等级加强数值");
	private AddRemoveList dprList = new AddRemoveList();
	
	private String saveDatabase = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String saveNamespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	private String saveCollection = "equipments_new";

	public WeaponManualDataPanel() {
		init();
	}
	
	public static WeaponManualDataPanel getInstance() {
//		return instance;
		return new WeaponManualDataPanel();
	}
	
	public void init() {		
		Double attackIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_ATTACK_INDEX, 1.3);
		attackField.setText(attackIndex.toString());
		attackField.setToolTipText("1(伤害)=1.3(攻击). 攻击可以表示为DPS的线性关系，我认为0.75倍率较好,即0.75*Attack=DPR");
		Double defendIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_DEFEND_INDEX, 1.3);
		defendField.setText(defendIndex.toString());
		defendField.setToolTipText("1(伤害)=1.5(防御)	.  防御直接增加护甲数值，也采用0.75倍率，即防御=0.75*(2*DPR)");
		Double luckTotal = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_CRITICAL_MAX, 4000);
		luckField.setText(luckTotal.toString());
		luckField.setToolTipText("幸运值的总值，玩家幸运值/总值=暴击率，而暴击倍率等于1.5+2*暴击率，所以幸运值为10000时提升3.5倍DPR");
		agilityField.setText("3740");
		agilityField.setToolTipText("1倍伤害=3740敏捷.  体力默认为210,敏捷值范围为0-10000,体力增量=敏捷值/20,最大可增加500体力");
		bloodField.setText("0.5");
		bloodField.setToolTipText("1倍伤害=187体力.  1倍伤害=0.5倍血量");
		String bulletPath = ConfigManager.getConfigAsString(ConfigKey.bulletDir);
		if ( bulletPath == null ) {
			bulletPath = "../deploy/data";
		}
		bulletField.setText(bulletPath);
		bulletField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveBulletPath();
			}
		});
		ArrayListModel dprListModel = dprList.getListModel();
		//double baseLevelUpRatio = EquipCalculator.calculateStrengthAttack(100, 5)/100.0;
		double[] ratios = GameDataManager.getInstance().getGameDataAsDoubleArray(
				GameDataKey.WEAPON_LEVEL_RATIO);
		for ( int i=0; i<ratios.length; i++ ) {
			dprListModel.insertRow(ratios[i]);
		}
		
		tableModel.setBulletPath(bulletPath);
		tableModel.reload();
		
		myTable.setDelRowAction(new MyTableModelDeleteRowAction(myTable.getTable()));
		myTable.setSaveButtonAction(new WeaponDataSaveAction(this.tableModel, saveDatabase, saveNamespace, saveCollection));
		myTable.setEnableDelRow(false);
		myTable.setEnableAddRow(false);
		myTable.setTableModel(tableModel);
		myTable.setEditable(true);
		
		JPanel paramPanel = new JPanel();
		paramPanel.setLayout(new MigLayout("wrap 4"));
		paramPanel.add(attackLabel, "sg lbl");
		paramPanel.add(attackField, "sg fd, growx, pushx");
		paramPanel.add(defendLabel, "sg lbl");
		paramPanel.add(defendField, "sg fd, growx, pushx");
		paramPanel.add(luckLabel, "sg lbl");
		paramPanel.add(luckField, "sg fd, growx, pushx");
		paramPanel.add(agilityLabel, "sg lbl");
		paramPanel.add(agilityField, "sg fd, growx, pushx");
		
		importButton.setActionCommand(COMMAND_IMPORT);
		importButton.addActionListener(this);
		exportButton.setActionCommand(COMMAND_EXPORT);
		exportButton.addActionListener(this);
		printResultButton.setActionCommand(COMMAND_PRINT);
		printResultButton.addActionListener(this);
		
//		JXPanel dprPanel = new JXPanel();
//		dprPanel.setLayout(new MigLayout("wrap 1"));
//		dprPanel.add(dprLabel, "width 100%");
//		dprPanel.add(new JScrollPane(dprList), "width 100%");
		
		weaponPanel.setLayout(new MigLayout("wrap 3"));
		weaponPanel.add(levelLabel,  "width 30%, wrap");
		weaponPanel.add(dprList, "width 30%");
		weaponPanel.add(paramPanel, "width 40%");
		weaponPanel.add(importButton, "newline, align center");
		weaponPanel.add(exportButton, "");
		weaponPanel.add(printResultButton, "");
		weaponPanel.add(myTable, "newline, span, width 100%, growy, push");
		
		this.setLayout(new MigLayout());
		this.add(weaponPanel, "width 100%, height 100%");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( COMMAND_IMPORT.equals(e.getActionCommand()) ) {
			File currentFileDir = null;
			String lastExportDir = ConfigManager.getConfigAsString(ConfigKey.weaponExportDir);
			if ( lastExportDir != null ) {
				currentFileDir = new File(lastExportDir);
			} else {
				currentFileDir = new File(System.getProperty("user.dir"));
			}
			ConfigManager.saveConfigKeyValue(ConfigKey.weaponExportDir, currentFileDir.getAbsolutePath());

			JFileChooser chooser = new JFileChooser(currentFileDir);
			int select = chooser.showSaveDialog(this);
			if ( select == JFileChooser.APPROVE_OPTION ) {
				File importFile = chooser.getSelectedFile();
				WeaponManualDataImportService service = 
						new WeaponManualDataImportService(importFile, tableModel, 
								(ArrayList<Double>)(this.dprList.getListModel().getList())
								);
				JDialog dialog = service.getDialog();
				service.execute();
				dialog.setVisible(true);
			}

		} else if ( COMMAND_EXPORT.equals(e.getActionCommand()) ) {
			File currentFileDir = null;
			String lastExportDir = ConfigManager.getConfigAsString(ConfigKey.weaponExportDir);
			if ( lastExportDir != null ) {
				currentFileDir = new File(lastExportDir);
			} else {
				currentFileDir = new File(System.getProperty("user.dir"));
			}
			ConfigManager.saveConfigKeyValue(ConfigKey.weaponExportDir, currentFileDir.getAbsolutePath());

			JFileChooser chooser = new JFileChooser(currentFileDir);
			int select = chooser.showSaveDialog(this);
			if ( select == JFileChooser.APPROVE_OPTION ) {
				File exportFile = chooser.getSelectedFile();
				try {
					String[] columns = tableModel.getColumnNames();
					FileWriter fw = new FileWriter(exportFile, false);
					for ( int i=0; i<columns.length; i++ ) {
						fw.append(columns[i]).append('\t');
					}
					fw.append('\n');
					for ( WeaponPojo weapon : tableModel.getWeaponList() ) {
						if ( !weapon.getName().startsWith("黑铁") ) continue;
						fw.append(weapon.getId()).append('\t');
						fw.append(weapon.getName()).append('\t');
						fw.append(String.valueOf(weapon.getUserLevel())).append('\t');
						fw.append(weapon.getTypeName()).append('\t');
						fw.append(weapon.getSlot().toString()).append('\t');
						fw.append(String.valueOf(EquipCalculator.calculateWeaponPower(
								weapon.getAddAttack(), weapon.getAddDefend(), 
								weapon.getAddAgility(), weapon.getAddLuck(),
								weapon.getAddBlood(), weapon.getAddSkin()))).append('\t');
						fw.append(String.valueOf(weapon.getAddAttack())).append('\t');
						fw.append(String.valueOf(weapon.getAddDefend())).append('\t');
						fw.append(String.valueOf(weapon.getAddAgility())).append('\t');
						fw.append(String.valueOf(weapon.getAddLuck())).append('\t');
						fw.append(String.valueOf(weapon.getAddBlood())).append('\t');
						fw.append(String.valueOf(weapon.getAddBloodPercent())).append('\t');
						fw.append(String.valueOf(weapon.getAddSkin())).append('\t');
						String bullet = weapon.getBullet();
						if ( StringUtil.checkNotEmpty(bullet) ) {
							BattleBitSetBullet battbleBullet = 
									BattleDataLoader4Bitmap.getBattleBulletByName(bullet);
							if ( battbleBullet != null ) {
								fw.append(bullet).append('\t');
								fw.append(String.valueOf(battbleBullet.getBullet().getWidth())).append('\t');
								fw.append(String.valueOf(battbleBullet.getBullet().getHeight())).append('\t');
							}
						}
						fw.append('\n');
					}
					fw.close();
				} catch (IOException exp) {
					exp.printStackTrace();
				}
			}
		} else if ( COMMAND_PRINT.equals(e.getActionCommand()) ) {
			//710	黑铁●薇薇安	0	71	weapon		35	10	20	30	0
			//目前攻击力最高的武器为微微安，攻击力35，以此武器做计算参照
			String weaponType = "71";
			//700	黑铁●玄武壳	0	70	weapon		10	20	20	40	0	0	bullet_black	150	150	攻击范围非常大，适合埋人
			//目前中等防御力武器为玄武壳，防御力20
			String defendWeapon = "70";
			double attackRatio = 1.0;
			double criticalRatio = 1.0;
			StringBuilder buf = new StringBuilder(500);
			buf.append("<html><table>");
			buf.append("<tr><td>武器</td><td>攻击力</td><td>防御力</td><td>护甲</td><td>血量</td><td>伤害</td><td>伤害比</td></tr>");
			for ( int i=0; i<=100; i+=10 ) {
				LevelPojo level = LevelManager.getInstance().getLevel(i==0?1:i);
				WeaponPojo maxAttackWeapon = EquipManager.getInstance().
						getWeaponByTypeNameAndUserLevel(weaponType, i);
				WeaponPojo averageDefendWeapon = EquipManager.getInstance().
						getWeaponByTypeNameAndUserLevel(defendWeapon, i);
				User attackUser = new User();
				attackUser.setLevel(level.get_id());
				attackUser.getBag().removeOtherPropDatas(20);
				attackUser.getBag().addOtherPropDatas(maxAttackWeapon.toPropData(10, WeaponColor.WHITE));
				attackUser.getBag().wearPropData(20, 17);
				User beingAttackedUser = new User();
				beingAttackedUser.setLevel(level.get_id());
				beingAttackedUser.getBag().removeOtherPropDatas(20);
				beingAttackedUser.getBag().addOtherPropDatas(averageDefendWeapon.toPropData(10, WeaponColor.WHITE));
				beingAttackedUser.getBag().wearPropData(20, 17);
				
				ArrayList<BuffToolType> tools = new ArrayList<BuffToolType>();
				tools.add(BuffToolType.HurtAdd50);
				int finalHurt = UserCalculator.calculateHurt(attackUser, beingAttackedUser, tools, attackRatio, criticalRatio);
				buf.append("<tr><td>").append(maxAttackWeapon.getName()).append("</td>");
				buf.append("<td>").append(attackUser.getAttack()).append("</td>");
				buf.append("<td>").append(beingAttackedUser.getDefend()).append("</td>");
				buf.append("<td>").append(beingAttackedUser.getSkin()).append("</td>");
				buf.append("<td>").append(beingAttackedUser.getBlood()).append("</td>");
				buf.append("<td>").append(finalHurt).append("</td>");
				buf.append("<td>").append(beingAttackedUser.getBlood()*1.0/finalHurt).append("</td>");
			}
			buf.append("</table></html>");
			HtmlDialog dialog = new HtmlDialog(buf.toString(), 500, 500);
			dialog.setVisible(true);
		}
	}
	
	public void saveBulletPath() {
		String bulletPath = bulletField.getText();
		if ( StringUtil.checkNotEmpty(bulletPath) ) {
			ConfigManager.saveConfigKeyValue(ConfigKey.bulletDir, bulletPath);
		}
	}
	
	/**
	 * @param args
	 */
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
				frame.add(WeaponManualDataPanel.getInstance());
				frame.setSize(800, 800);
				frame.setVisible(true);
			}
		});
	}
}
