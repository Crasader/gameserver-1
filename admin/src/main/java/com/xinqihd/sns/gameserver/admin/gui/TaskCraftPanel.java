package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;

import com.xinqihd.sns.gameserver.admin.data.CraftProxPanel;
import com.xinqihd.sns.gameserver.admin.data.CraftStonePrintConfig;
import com.xinqihd.sns.gameserver.admin.data.CraftStonePrintService;
import com.xinqihd.sns.gameserver.admin.data.CraftStoneResultModel;
import com.xinqihd.sns.gameserver.admin.data.StrengthTestConfig;
import com.xinqihd.sns.gameserver.admin.data.StrengthTestService;
import com.xinqihd.sns.gameserver.admin.gui.ext.AddRemoveList;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.gui.table.EquipAndItemRenderer;
import com.xinqihd.sns.gameserver.admin.model.ArrayListModel;
import com.xinqihd.sns.gameserver.admin.util.MyWindowUtil;
import com.xinqihd.sns.gameserver.admin.util.ObjectUtil;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;

/**
 * 用于强化和熔炼的数值计算
 * 
 * @author wangqi
 *
 */
public class TaskCraftPanel extends MyPanel implements ActionListener {
	
	private static final String COMMAND_TEST_STRENGTH  = "test_strength";
	private static final String COMMAND_PRINT_STRENGTH = "print_strength";
	private static final String COMMAND_SAVE_STRENGTH  = "save_strength";
	private static final String COMMAND_SAVE_CRAFT  = "save_craft";
	private static final String COMMAND_PRINT_CRAFT  = "print_craft";
	
	private static TaskCraftPanel instance = new TaskCraftPanel();
	
	//强化面板
	private JXPanel forgePanel = new JXPanel();
	//合成面板
	private JXPanel craftPanel = new JXPanel();
	
	private CraftProxPanel forgeProxPanel = new CraftProxPanel(
			GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.STRENGTH_STONE_RATIO));
	
	//强化测试
	private JXLabel strengthPriceLbl = new JXLabel("强化的基础价格(金币):");
	private JXTextField strengthPriceField = new JXTextField("金币的基础价格");
	private JXLabel strengthPrice1Lbl = new JXLabel("强化1-5等级:");
	private JXTextField strengthPrice1Field = new JXTextField("等级加成");
	private JXLabel strengthPrice2Lbl = new JXLabel("强化6-10等级:");
	private JXTextField strengthPrice2Field = new JXTextField("等级加成");
	private JXLabel strengthPrice3Lbl = new JXLabel("强化11-12等级:");
	private JXTextField strengthPrice3Field = new JXTextField("等级加成");
	
	private JCheckBox lucky15Field = new JCheckBox("15%幸运石");
	private JCheckBox lucky25Field = new JCheckBox("25%幸运石");
	private JCheckBox godField     = new JCheckBox("神恩符");
	private JXLabel   stoneLabel   = new JXLabel("选择强化石:");
	private JComboBox stoneField     = new JComboBox(new String[]{
			"1级强化石","2级强化石","3级强化石","4级强化石","5级强化石"
	});
	private JXLabel   maxTryLabel = new JXLabel("最大测试次数:");
	private JSpinner  maxTryField = new JSpinner();
	
	private JXButton  testStrBtn  = new JXButton("模拟测试");
	private JXButton  printStrBtn = new JXButton("打印概率");
	private JXButton  saveStrBtn  = new JXButton("保存数据");
	
	private MyTablePanel strengthTablePanel = new MyTablePanel();
	
	//合成功能测试
	private JXLabel attackStoneTimeLbl = new JXLabel("火神石(攻击)范围:");
	private JXLabel defendStoneTimeLbl = new JXLabel("土神石(防御)范围:");
	private JXLabel agilityStoneTimeLbl = new JXLabel("风神石(敏捷)范围:");
	private JXLabel luckyStoneTimeLbl = new JXLabel("水神石(幸运)范围:");
	private JXTextField[] attackStoneTimeField = new JXTextField[2];
	private JXTextField[] defendStoneTimeField = new JXTextField[2];
	private JXTextField[] luckRangeFields = new JXTextField[2];
	private JXTextField[] agilityRangeField = new JXTextField[2];
	private AddRemoveList qArrayListField = null;
	private JXLabel craftPriceLbl = new JXLabel("合成价格(金币):");
	private JXTextField craftPriceField = new JXTextField();
	private JXLabel craftLuckyTimesLbl = new JXLabel("合成石幸运符乘数");
	private JXTextField craftLuckyTimesField = new JXTextField();
	private JXButton saveCraftButton = new JXButton("保存合成配置数据");
	
	private JXLabel selectCraftStoneTypeLbl = new JXLabel("选择合成石类型:");
	private JComboBox selectCraftStoneTypeField = new JComboBox(new String[]{
		"火神石", "土神石", "水神石", "风神石"
	});
	private JXLabel simulateCraftStoneLbl = new JXLabel("输入模拟次数:");
	private JXTextField simulateCraftStoneField = new JXTextField();
	private JXLabel selectWeaponLbl = new JXLabel("选择待模拟的武器:");
	private DefaultComboBoxModel selectWeaponModel = new DefaultComboBoxModel();
	private JComboBox selectWeaponField = new JComboBox(selectWeaponModel);
	private JXLabel selectWeaponLevelLbl = new JXLabel("强化等级(1-12):");
	private JSpinner selectWeaponLevelField = new JSpinner();
	
	private JXButton printButton = new JXButton("输出模拟结果");
	
	public TaskCraftPanel() {
		init();
	}
	
	public static TaskCraftPanel getInstance() {
		return instance;
	}
	
	public void init() {
		TitledBorder forgeTitle = BorderFactory.createTitledBorder("强化数值计算");
		TitledBorder craftTitle = BorderFactory.createTitledBorder("合成数值计算");
		forgeTitle.setTitleFont(MainFrame.BIG_FONT);
		craftTitle.setTitleFont(MainFrame.BIG_FONT);
		this.forgePanel.setBorder(forgeTitle);
		craftPanel.setBorder(craftTitle);
		int price = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.PRICE_CRAFT_FORGE, 2000);
		this.strengthPriceField.setText(String.valueOf(price));
		double strBase = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.STRENGTH_BASE_RATIO, 1.02);
		double strNormal = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.STRENGTH_NORMAL_RATIO, 1.05);
		double strAdvance = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.STRENGTH_ADVANCE_RATIO, 1.1);
		this.strengthPrice1Field.setText(String.valueOf(strBase));
		this.strengthPrice2Field.setText(String.valueOf(strNormal));
		this.strengthPrice3Field.setText(String.valueOf(strAdvance));
		this.testStrBtn.addActionListener(this);
		this.testStrBtn.setActionCommand(COMMAND_TEST_STRENGTH);
		this.printStrBtn.addActionListener(this);
		this.printStrBtn.setActionCommand(COMMAND_PRINT_STRENGTH);
		this.saveStrBtn.addActionListener(this);
		this.saveStrBtn.setActionCommand(COMMAND_SAVE_STRENGTH);
		this.maxTryField.setValue(1000000);
		this.strengthTablePanel.setEnableAddRow(false);
		this.strengthTablePanel.setEnableRrefresh(false);
		this.strengthTablePanel.setEnableDelRow(false);
		
		this.forgePanel.setLayout(new MigLayout(""));
		this.forgePanel.add(forgeProxPanel, "width 100%, height 20%");
		this.craftPanel.setLayout(new MigLayout("wrap 4"));
				
		forgePanel.add(strengthPriceLbl, "newline, span, split 2, width 50%");
		forgePanel.add(strengthPriceField, "width 50%");
		forgePanel.add(strengthPrice1Lbl, "newline, span, split 6");
		forgePanel.add(strengthPrice1Field, "");
		forgePanel.add(strengthPrice2Lbl, "");
		forgePanel.add(strengthPrice2Field, "");
		forgePanel.add(strengthPrice3Lbl, "");
		forgePanel.add(strengthPrice3Field, "");
		
		forgePanel.add(lucky15Field, "span, split 3, align center");
		forgePanel.add(lucky25Field, "");
		forgePanel.add(godField, "");
		forgePanel.add(stoneLabel, "newline, span, split 4, align center");
		forgePanel.add(stoneField, "");
		forgePanel.add(maxTryLabel, "");
		forgePanel.add(maxTryField, "");
		
		forgePanel.add(testStrBtn, "span, align center, split 3");
		forgePanel.add(printStrBtn, "");
		forgePanel.add(saveStrBtn, "");
		
		forgePanel.add(strengthTablePanel, "span, width 100%, height 50%, grow");
		
		//合成数值测试
		double[] qArray = GameDataManager.getInstance().getGameDataAsDoubleArray(
				GameDataKey.FORGE_SIGMA_RATIO);
		
		int craftPrice = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.PRICE_CRAFT_COMPOSE, 2000);
		double luckyTimes = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.FORGE_LUCKY_TIMES, 6.0);
		this.craftPriceField.setText(String.valueOf(craftPrice));
		this.simulateCraftStoneField.setText("100000");
		this.saveCraftButton.addActionListener(this);
		this.saveCraftButton.setActionCommand(COMMAND_SAVE_CRAFT);
		this.printButton.addActionListener(this);
		this.printButton.setActionCommand(COMMAND_PRINT_CRAFT);
		Double[] qArrayObj = new Double[qArray.length];
		for ( int i=0; i<qArray.length; i++ ) {
			qArrayObj[i] = qArray[i];
		}
		this.qArrayListField = new AddRemoveList(qArrayObj);
		this.qArrayListField.setBorder(BorderFactory.createTitledBorder("合成石合成范围(1.0+该值)"));
		this.craftLuckyTimesField.setText(String.valueOf(luckyTimes));
		
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeaponsBySlot(EquipType.WEAPON);
		for ( WeaponPojo weapon : weapons ) {
			selectWeaponModel.addElement(weapon);
		}
		this.selectWeaponField.setRenderer(new EquipAndItemRenderer());
		this.selectWeaponField.setEditable(false);
		this.selectWeaponField.setSelectedIndex(0);
		
		JXPanel attackPanel = new JXPanel();
		attackPanel.setBorder(BorderFactory.createTitledBorder("火神石(攻击)范围:"));
		attackPanel.setLayout(new MigLayout("wrap 4, inset 0"));
		for ( int i=0; i<qArray.length; i+=2 ) {
			this.attackStoneTimeField[i] = new JXTextField();
			this.attackStoneTimeField[i].setText(String.valueOf(1.0));
			this.attackStoneTimeField[i+1] = new JXTextField();
			this.attackStoneTimeField[i+1].setText(String.valueOf(1.0+qArray[i]));
			JXLabel label = new JXLabel("级"+(i/2+1));
			JXLabel toLbl = new JXLabel("->");
			attackPanel.add(label, "");
			attackPanel.add(this.attackStoneTimeField[i], "sg fd, width 40%");
			attackPanel.add(toLbl, "");
			attackPanel.add(this.attackStoneTimeField[i+1], "sg fd, width 40%");
		}
		this.craftPanel.add(attackPanel, "span 2, width 50%");
		
		JXPanel defendPanel = new JXPanel();
		defendPanel.setBorder(BorderFactory.createTitledBorder("土神石(防御)范围:"));
		defendPanel.setLayout(new MigLayout("wrap 4, inset 0"));
		for ( int i=0; i<qArray.length; i+=2 ) {
			this.defendStoneTimeField[i] = new JXTextField();
			this.defendStoneTimeField[i].setText(String.valueOf(1.0));
			this.defendStoneTimeField[i+1] = new JXTextField();
			this.defendStoneTimeField[i+1].setText(String.valueOf(1.0+qArray[i]));
			JXLabel label = new JXLabel("级"+(i/2+1));
			JXLabel toLbl = new JXLabel("->");
			defendPanel.add(label, "");
			defendPanel.add(this.defendStoneTimeField[i], "sg fd, width 40%");
			defendPanel.add(toLbl, "");
			defendPanel.add(this.defendStoneTimeField[i+1], "sg fd, width 40%");
		}
		this.craftPanel.add(defendPanel, "span 2, width 50%");
		
		JXPanel luckPanel = new JXPanel();
		luckPanel.setBorder(BorderFactory.createTitledBorder("水神石(幸运)范围:"));
		luckPanel.setLayout(new MigLayout("wrap 4, inset 0"));
		for ( int i=0; i<qArray.length; i+=2 ) {
			this.luckRangeFields[i] = new JXTextField();
			this.luckRangeFields[i].setText(String.valueOf(1.0));
			this.luckRangeFields[i+1] = new JXTextField();
			this.luckRangeFields[i+1].setText(String.valueOf(1.0+qArray[i]));
			JXLabel label = new JXLabel("级"+(i/2+1));
			JXLabel toLbl = new JXLabel("->");
			luckPanel.add(label, "");
			luckPanel.add(this.luckRangeFields[i], "sg fd, width 40%");
			luckPanel.add(toLbl, "");
			luckPanel.add(this.luckRangeFields[i+1], "sg fd, width 40%");
		}
		this.craftPanel.add(luckPanel, "span 2, width 50%");
		
		JXPanel agilityPanel = new JXPanel();
		agilityPanel.setBorder(BorderFactory.createTitledBorder("风神石(敏捷)范围:"));
		agilityPanel.setLayout(new MigLayout("wrap 4, inset 0"));
		this.agilityRangeField = new JXTextField[qArray.length];
		for ( int i=0; i<qArray.length; i+=2 ) {
			this.agilityRangeField[i] = new JXTextField();
			this.agilityRangeField[i].setText(String.valueOf(1.0));
			this.agilityRangeField[i+1] = new JXTextField();
			this.agilityRangeField[i+1].setText(String.valueOf(1.0+qArray[i]));
			JXLabel label = new JXLabel("级"+(i/2+1));
			JXLabel toLbl = new JXLabel("->");
			agilityPanel.add(label, "");
			agilityPanel.add(this.agilityRangeField[i], "sg fd, width 40%");
			agilityPanel.add(toLbl, "");
			agilityPanel.add(this.agilityRangeField[i+1], "sg fd, width 40%");
		}
		this.craftPanel.add(agilityPanel, "span 2, width 50%");
		this.craftPanel.add(craftPriceLbl, "align right");
		this.craftPanel.add(craftPriceField, "span 2, growx, wrap");
		this.craftPanel.add(qArrayListField, "newline, span, growx");
		this.craftPanel.add(craftLuckyTimesLbl, "newline, span, split 2");
		this.craftPanel.add(craftLuckyTimesField, "");
		this.craftPanel.add(saveCraftButton, "span, align center");
		
		JXPanel printCraftPanel = new JXPanel();
		printCraftPanel.setBorder(BorderFactory.createTitledBorder("打印合成数值"));
		printCraftPanel.setLayout(new MigLayout("wrap 4", "[20%][45%][20%][15%]"));
		printCraftPanel.add(selectCraftStoneTypeLbl, "");
		printCraftPanel.add(selectCraftStoneTypeField, "sg field");
		printCraftPanel.add(simulateCraftStoneLbl, "");
		printCraftPanel.add(simulateCraftStoneField, "sg field");
		printCraftPanel.add(selectWeaponLbl, "");
		printCraftPanel.add(selectWeaponField, "sg field");
		printCraftPanel.add(selectWeaponLevelLbl, "");
		printCraftPanel.add(selectWeaponLevelField, "sg field");
		printCraftPanel.add(printButton, "newline, span, align center");
		
		this.craftPanel.add(printCraftPanel, "span, width 100%");
		
		this.setLayout(new MigLayout("wrap 2"));
		this.add(forgePanel, "width 50%, height 100%");
		this.add(craftPanel, "width 50%, height 100%");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( COMMAND_TEST_STRENGTH.equals(e.getActionCommand()) ) {
			StrengthTestConfig config = new StrengthTestConfig();
			config.setBasePrice(Integer.parseInt(this.strengthPriceField.getText()));
			config.setMaxTry((Integer)this.maxTryField.getValue());
			config.setUse15Lucky(this.lucky15Field.isSelected());
			config.setUse25Lucky(this.lucky25Field.isSelected());
			config.setProxList(this.forgeProxPanel.getProxList());
			config.setUseGod(godField.isSelected());
			config.setStoneLevel(stoneField.getSelectedIndex());
			StrengthTestService service = new StrengthTestService(config, strengthTablePanel);
			JDialog dialog = service.getDialog();
			service.execute();
			dialog.setVisible(true);
		} else if ( COMMAND_SAVE_STRENGTH.equals(e.getActionCommand()) ) {
			int option = JOptionPane.showConfirmDialog(this, "确定将修改的强化数值保存到数据库中吗?");
			if ( option == JOptionPane.YES_OPTION ) {
				//保存概率
				List<Double> proxList = this.forgeProxPanel.getProxList();
				double[] ratios = new double[proxList.size()];
				for ( int i=0; i<ratios.length; i++ ) {
					ratios[i] = (Double)ObjectUtil.convertValue(proxList.get(i), Double.class);
				}
				GameDataManager manager = GameDataManager.getInstance();
				//Restore default values
				manager.reload();
				double[] defaultValue = manager.getGameDataAsDoubleArray(GameDataKey.STRENGTH_STONE_RATIO);
				manager.setValueToDatabase(GameDataKey.STRENGTH_STONE_RATIO, ratios, defaultValue);
				//保存基础价格
				int defaultPrice = manager.getGameDataAsInt(GameDataKey.PRICE_CRAFT_FORGE, 2000);
				int price = Integer.parseInt(this.strengthPriceField.getText());
				manager.setValueToDatabase(GameDataKey.PRICE_CRAFT_FORGE, price, defaultPrice);
				//保存强化倍率
				double base = Double.parseDouble(this.strengthPrice1Field.getText());
				double normal = Double.parseDouble(this.strengthPrice2Field.getText());
				double advance = Double.parseDouble(this.strengthPrice3Field.getText());
				
				manager.setValueToDatabase(GameDataKey.STRENGTH_BASE_RATIO, base, 1.02);
				manager.setValueToDatabase(GameDataKey.STRENGTH_NORMAL_RATIO, normal, 1.05);
				manager.setValueToDatabase(GameDataKey.STRENGTH_ADVANCE_RATIO, advance, 1.1);
			} 
		} else if ( COMMAND_PRINT_STRENGTH.equals(e.getActionCommand()) ) {
			StringBuilder buf = new StringBuilder(200);
			buf.append("<html><table>");
			buf.append("<th><strong>强化级别");
			for ( int stoneLevel = 1; stoneLevel < 6; stoneLevel++ ) {
				buf.append("<th><strong>强化石").append(stoneLevel).append("级");
				buf.append("<th><strong>幸%15");
				buf.append("<th><strong>幸%25");
			}

			for ( int targetLevel = 1; targetLevel < 20; targetLevel++ ) {
				buf.append("<tr><td>强化").append(targetLevel);
				for ( int stoneLevel = 1; stoneLevel < 6; stoneLevel++ ) {
					double successRatio = EquipCalculator.calculateStrengthStoneSuccessRatio(stoneLevel, targetLevel);
					double luck15SuccessRatio = EquipCalculator.calculateStrengthWithLuckyStoneRatio(null, new double[]{successRatio}, 0.15);
					double luck25SuccessRatio = EquipCalculator.calculateStrengthWithLuckyStoneRatio(null, new double[]{successRatio}, 0.25);
					buf.append("<td>").append(Math.round(successRatio*100000)/1000.0+"%").append("</td>");
					buf.append("<td>").append(Math.round(luck15SuccessRatio*100000)/1000.0+"%").append("</td>");
					buf.append("<td>").append(Math.round(luck25SuccessRatio*100000)/1000.0+"%").append("</td>");
				}
			}

			buf.append("</table></html>");
			
			JDialog dialog = MyWindowUtil.getHtmlDialog(buf.toString(), 580, 500);
			dialog.setVisible(true);
		} else if ( COMMAND_SAVE_CRAFT.equals(e.getActionCommand()) ) {
			//保存合成数值
			double[] qArray = getqArrayValue();
			if ( qArray.length < 5 ) {
				JOptionPane.showMessageDialog(this, "合成石等级概率的列表需要至少5个(与合成石的等级数一致)");
				return;
			}
			double[] attackRange = new double[2];
			attackRange[0] = Double.parseDouble(this.attackStoneTimeField[0].getText());
			attackRange[1] = Double.parseDouble(this.attackStoneTimeField[1].getText());
			double[] defendRange = new double[2];
			defendRange[0] = Double.parseDouble(this.defendStoneTimeField[0].getText());
			defendRange[1] = Double.parseDouble(this.defendStoneTimeField[1].getText());
			double[] agilityRange = new double[2];
			agilityRange[0] = Double.parseDouble(this.agilityRangeField[0].getText());
			agilityRange[1] = Double.parseDouble(this.agilityRangeField[1].getText());
			double[] luckRange = new double[2];
			luckRange[0] = Double.parseDouble(this.luckRangeFields[0].getText());
			luckRange[1] = Double.parseDouble(this.luckRangeFields[1].getText());
			int craftPrice = Integer.parseInt(this.craftPriceField.getText());
			GameDataManager manager = GameDataManager.getInstance();
			manager.reload();

			manager.setValueToDatabase(
					GameDataKey.FORGE_SIGMA_RATIO, qArray);
			manager.setValueToDatabase(
					GameDataKey.PRICE_CRAFT_COMPOSE, craftPrice); 
			
			double luckyTimes = Double.parseDouble(this.craftLuckyTimesField.getText());
			manager.setValueToDatabase(GameDataKey.FORGE_LUCKY_TIMES, luckyTimes);
		} else if ( COMMAND_PRINT_CRAFT.equals(e.getActionCommand()) ) {
			double[] qArray = getqArrayValue();
			if ( qArray.length < 5 ) {
				JOptionPane.showMessageDialog(this, "合成石等级概率的列表需要至少5个(与合成石的等级数一致)");
				return;
			}
			double[] attackRange = new double[2];
			attackRange[0] = Double.parseDouble(this.attackStoneTimeField[0].getText());
			attackRange[1] = Double.parseDouble(this.attackStoneTimeField[1].getText());
			double[] defendRange = new double[2];
			defendRange[0] = Double.parseDouble(this.defendStoneTimeField[0].getText());
			defendRange[1] = Double.parseDouble(this.defendStoneTimeField[1].getText());
			double[] agilityRange = new double[2];
			agilityRange[0] = Double.parseDouble(this.agilityRangeField[0].getText());
			agilityRange[1] = Double.parseDouble(this.agilityRangeField[1].getText());
			double[] luckRange = new double[2];
			luckRange[0] = Double.parseDouble(this.luckRangeFields[0].getText());
			luckRange[1] = Double.parseDouble(this.luckRangeFields[1].getText());
			double luckyTimes = Double.parseDouble(this.craftLuckyTimesField.getText());
			
			GameDataManager.getInstance().overrideRuntimeValue(
					GameDataKey.FORGE_LUCKY_TIMES, luckyTimes);
			
			WeaponPojo weapon = (WeaponPojo)this.selectWeaponField.getSelectedItem();
			PropData equip    = weapon.toPropData(0, WeaponColor.WHITE);
			int      equipLevel = Integer.parseInt(this.selectWeaponLevelField.getValue().toString());
			equip = EquipCalculator.weaponUpLevel(equip, equipLevel);
			
			MyTablePanel craftTable = new MyTablePanel();
			final CraftStoneResultModel model = new CraftStoneResultModel();
			craftTable.setTableModel(model);
			CraftStonePrintConfig config = new CraftStonePrintConfig();
			config.setqArray(qArray);
			config.setEquipProp(equip);
			//"随机类型", "火神石", "土神石", "水神石", "风神石"
			int selectIndex = selectCraftStoneTypeField.getSelectedIndex();
			String stoneTypeId = null;
			LOOP:
			while ( stoneTypeId == null ) {
				switch ( selectIndex ) {
					case 0:
						stoneTypeId = ItemManager.attackStoneId;
						break;
					case 1:
						stoneTypeId = ItemManager.defendStoneId;
						break;
					case 2:
						stoneTypeId = ItemManager.luckStoneId;
						break;
					case 3:
						stoneTypeId = ItemManager.agilityStoneId;
						break;
				}
			}
			config.setStoneTypeId(stoneTypeId);
			int count = Integer.parseInt(simulateCraftStoneField.getText());
			CraftStonePrintService service = new CraftStonePrintService(
					model, config, count, craftTable);
			JDialog dialog = service.getDialog();
			service.execute();
			dialog.setVisible(true);
			
			JDialog resultDialog = MyWindowUtil.getCenterDialog(1000, 600, 
					craftTable, null);
			JXLabel descLabel = new JXLabel("合成石类型: " + this.selectCraftStoneTypeField.getSelectedItem() +
					",选择装备: " + weapon.getName() + ",等级:" + equip.getLevel() + 
					", 攻击:" + equip.getAttackLev() + ", 防御:" + equip.getDefendLev() + 
					", 敏捷: " + equip.getAgilityLev() + ", 幸运:" + equip.getLuckLev());
			
			JRadioButton percentBtn = new JRadioButton("百分比模式");
			percentBtn.setSelected(true);
			JRadioButton dataBtn = new JRadioButton("数值模式");
			ButtonGroup btnGroup = new ButtonGroup();
			btnGroup.add(percentBtn);
			btnGroup.add(dataBtn);
			JXPanel buttonPanel = new JXPanel();
			buttonPanel.setLayout(new MigLayout(""));
			buttonPanel.add(descLabel, "");
			buttonPanel.add(percentBtn, "newline");
			buttonPanel.add(dataBtn, "");
			percentBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					model.setPercentMode(true);
				}
			});
			dataBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					model.setPercentMode(false);
				}
			});
			resultDialog.add(buttonPanel, "dock north");
			resultDialog.setVisible(true);
		}
	}

	private double[] getqArrayValue() {
		ArrayListModel qArrayListModel = this.qArrayListField.getListModel();
		List qArrayList = qArrayListModel.getList();
		double[] qArray = new double[qArrayList.size()];
		for ( int i=0; i<qArray.length; i++ ) {
			qArray[i] = Double.parseDouble(qArrayList.get(i).toString());
		}
		return qArray;
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
				frame.add(TaskCraftPanel.getInstance());
				frame.setSize(1100, 800);
				frame.setVisible(true);
			}
		});
	}
}
