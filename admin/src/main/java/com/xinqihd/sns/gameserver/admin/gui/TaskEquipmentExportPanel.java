package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;

import com.xinqihd.sns.gameserver.admin.data.EquipmentExportService;
import com.xinqihd.sns.gameserver.admin.data.EquipmentImportService;
import com.xinqihd.sns.gameserver.admin.data.ShopDataImportItemPriceService;
import com.xinqihd.sns.gameserver.admin.data.ShopDataPriceConfig;
import com.xinqihd.sns.gameserver.admin.data.ShopDataPriceFromDBSaveService;
import com.xinqihd.sns.gameserver.admin.data.ShopDataPriceResultModel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;

/**
 * 将系统中的装备、道具数据以及相应的商品数据全部导出为外部TXT文件
 * 
 * @author wangqi
 *
 */
public class TaskEquipmentExportPanel extends MyPanel implements ActionListener {
	
	private static final String COMMAND_EXPORT_EQUIPMENT = "exportEquipment";
	private static final String COMMAND_IMPORT_EQUIPMENT = "importEquipment";
	private static final String COMMAND_SAVE_PRICE = "savePrice";
	private static final String COMMAND_SAVE_CONFIG = "saveConfig";
	private static final String COMMAND_IMPORT_ITEM = "importItemPrice";
	
	private static TaskEquipmentExportPanel instance = new TaskEquipmentExportPanel(); 
	
	private JXLabel goldenToPowerLbl = new JXLabel("金币兑换率");
	private JXLabel medalToPowerLbl = new JXLabel("勋章兑换率");
	private JXLabel voucherToPowerLbl = new JXLabel("礼券兑换率");
	private JXLabel yuanbaoToPowerLbl = new JXLabel("元宝兑换率");
	private JXTextField goldenToPowerField = new JXTextField();
	private JXTextField medalToPowerField =  new JXTextField();
	private JXTextField voucherToPowerField = new JXTextField();
	private JXTextField yuanbaoToPowerField = new JXTextField();
	
	private JXLabel randomRangeLbl = new JXLabel("价格随机区间");
	private JXLabel randomRangeToLbl = new JXLabel("->");
	private JXTextField randomMinField = new JXTextField();
	private JXTextField randomMaxField = new JXTextField();
	
	private JXButton exportEquipmentButton = new JXButton("导出基本装备数值到EXCEL");
	private JXButton importEquipmentButton = new JXButton("从EXCEL导入基本装备数值");
	private JXButton savePriceButton = new JXButton("更新商城数据");
	private JXButton importItemButton = new JXButton("导入道具数据");
	private JXButton saveConfigButton = new JXButton("保存配置数据");
	
	private MyTablePanel myTable = new MyTablePanel();
	
	private JXLabel simpleTimesLbl = new JXLabel("简单");
	private JXLabel normalTimesLbl = new JXLabel("普通");
	private JXLabel solidTimesLbl = new JXLabel("坚固");
	private JXLabel eternalTimesLbl = new JXLabel("永久");
	private JXTextField simpleTimesField = new JXTextField();
	private JXTextField normalTimesField = new JXTextField();
	private JXTextField solidTimesField = new JXTextField();
	private JXTextField eternalTimesField = new JXTextField();
	
	private JXLabel simpleTimesDiscountLbl = new JXLabel("简单");
	private JXLabel normalTimesDiscountLbl = new JXLabel("普通");
	private JXLabel solidTimesDiscountLbl = new JXLabel("坚固");
	private JXLabel eternalTimesDiscountLbl = new JXLabel("永久");
	private JXTextField simpleTimesDiscountField = new JXTextField();
	private JXTextField normalTimesDiscountField = new JXTextField();
	private JXTextField solidTimesDiscountField = new JXTextField();
	private JXTextField eternalTimesDiscountField = new JXTextField();
	
	private ShopDataPriceResultModel model = new ShopDataPriceResultModel();
		
	public TaskEquipmentExportPanel() {
		init();
	}
	
	public static TaskEquipmentExportPanel getInstance() {
		return instance;
	}

	public void init() {
		this.goldenToPowerField.setColumns(20);
		this.goldenToPowerField.setText("2");
		this.medalToPowerField.setText("0.4");
		this.voucherToPowerField.setText("1.0");
		this.yuanbaoToPowerField.setText("0.2");
		this.myTable.setEnableAddRow(false);
		this.myTable.setEnableDelRow(true);
		this.myTable.setEnableRrefresh(false);
		this.myTable.setTableModel(model);
		this.exportEquipmentButton.addActionListener(this);
		this.exportEquipmentButton.setActionCommand(COMMAND_EXPORT_EQUIPMENT);
		this.importEquipmentButton.addActionListener(this);
		this.importEquipmentButton.setActionCommand(COMMAND_IMPORT_EQUIPMENT);
		this.savePriceButton.addActionListener(this);
		this.savePriceButton.setActionCommand(COMMAND_SAVE_PRICE);
		this.savePriceButton.setEnabled(false);
		this.saveConfigButton.addActionListener(this);
		this.saveConfigButton.setActionCommand(COMMAND_SAVE_CONFIG);
		this.saveConfigButton.setEnabled(false);
		this.importItemButton.addActionListener(this);
		this.importItemButton.setActionCommand(COMMAND_IMPORT_ITEM);
		this.importItemButton.setEnabled(false);
		this.randomMinField.setText("0");
		this.randomMaxField.setText("0");
		
		int simpleTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SIMPLE, 30);
		int normalTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_NORMAL, 100);
		int solidTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_SOLID, 200);
		int eternalTimes = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.WEAPON_INDATE_ETERNAL, Integer.MAX_VALUE);
		this.simpleTimesField.setText(String.valueOf(simpleTimes));
		this.normalTimesField.setText(String.valueOf(normalTimes));
		this.solidTimesField.setText(String.valueOf(solidTimes));
		this.eternalTimesField.setText(String.valueOf(eternalTimes));
		this.simpleTimesDiscountField.setText("1.0");
		this.normalTimesDiscountField.setText("3.0");
		this.solidTimesDiscountField.setText("5.0");
		this.eternalTimesDiscountField.setText("10.0");
		
		JXPanel pricePanel = new JXPanel();
		pricePanel.setBorder(BorderFactory.createTitledBorder("设定单位DPR对应的价格参数"));
		pricePanel.setLayout(new MigLayout("wrap 8"));
		pricePanel.add(goldenToPowerLbl, "sg lbl");
		pricePanel.add(goldenToPowerField, "sg field");
		pricePanel.add(medalToPowerLbl, "sg lbl");
		pricePanel.add(medalToPowerField, "sg field");
		pricePanel.add(voucherToPowerLbl, "sg lbl");
		pricePanel.add(voucherToPowerField, "sg field");
		pricePanel.add(yuanbaoToPowerLbl, "sg lbl");
		pricePanel.add(yuanbaoToPowerField, "sg field");
		pricePanel.add(randomRangeLbl, "sg lbl");
		pricePanel.add(randomMinField, "sg field");
		pricePanel.add(randomRangeToLbl, "sg lbl");
		pricePanel.add(randomMaxField, "sg field");
		
		JXPanel qualityPanel = new JXPanel();
		qualityPanel.setBorder(BorderFactory.createTitledBorder("设定四种品质武器的有效次数和价格折扣"));
		qualityPanel.setLayout(new MigLayout("wrap 8"));
		qualityPanel.add(simpleTimesLbl, "sg lbl");
		qualityPanel.add(simpleTimesField, "sg field");
		qualityPanel.add(normalTimesLbl, "sg lbl");
		qualityPanel.add(normalTimesField, "sg field");
		qualityPanel.add(solidTimesLbl, "sg lbl");
		qualityPanel.add(solidTimesField, "sg field");
		qualityPanel.add(eternalTimesLbl, "sg lbl");
		qualityPanel.add(eternalTimesField, "sg field");
		
		qualityPanel.add(simpleTimesDiscountLbl, "sg lbl");
		qualityPanel.add(simpleTimesDiscountField, "sg field");
		qualityPanel.add(normalTimesDiscountLbl, "sg lbl");
		qualityPanel.add(normalTimesDiscountField, "sg field");
		qualityPanel.add(solidTimesDiscountLbl, "sg lbl");
		qualityPanel.add(solidTimesDiscountField, "sg field");
		qualityPanel.add(eternalTimesDiscountLbl, "sg lbl");
		qualityPanel.add(eternalTimesDiscountField, "sg field");
		
		this.setLayout(new MigLayout("wrap 2"));
		this.add(pricePanel, "span, width 100%");
		this.add(qualityPanel, "span, width 100%");
		this.add(exportEquipmentButton, "span, split 5, align center");
		this.add(importEquipmentButton, "");
		this.add(savePriceButton, "");
		this.add(importItemButton, "");
		this.add(saveConfigButton, "");
		this.add(myTable, "newline, span, width 100%, height 80%");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( COMMAND_EXPORT_EQUIPMENT.equals(e.getActionCommand()) ) {
			ShopDataPriceConfig config = parsePriceConfig();
			
			EquipmentExportService service = new EquipmentExportService(model, config);
			JDialog dialog = service.getDialog();
			service.execute();
			dialog.setVisible(true);
		} else if ( COMMAND_IMPORT_EQUIPMENT.equals(e.getActionCommand()) ) {
				ShopDataPriceConfig config = parsePriceConfig();
				
				EquipmentImportService service = new EquipmentImportService(model, config);
				JDialog dialog = service.getDialog();
				service.execute();
				dialog.setVisible(true);
		} else if ( COMMAND_SAVE_PRICE.equals(e.getActionCommand()) ) {
			int option = JOptionPane.showConfirmDialog(this, "您确定要将商品数据导入数据库吗?");
			if ( option == JOptionPane.YES_OPTION ) {
				ShopDataPriceFromDBSaveService service = new ShopDataPriceFromDBSaveService(model);
				JDialog dialog = service.getDialog();
				service.execute();
				dialog.setVisible(true);
			}
		} else if ( COMMAND_SAVE_CONFIG.equals(e.getActionCommand()) ) {
			int simpleTimes = Integer.parseInt(this.simpleTimesField.getText());
			int normalTimes = Integer.parseInt(this.normalTimesField.getText());
			int solidTimes = Integer.parseInt(this.solidTimesField.getText());
			int eternalTimes = Integer.parseInt(this.eternalTimesField.getText());
			
			GameDataManager manager = GameDataManager.getInstance();
			manager.setValueToDatabase(GameDataKey.WEAPON_INDATE_SIMPLE, simpleTimes);
			manager.setValueToDatabase(GameDataKey.WEAPON_INDATE_NORMAL, normalTimes);
			manager.setValueToDatabase(GameDataKey.WEAPON_INDATE_SOLID, solidTimes);
			manager.setValueToDatabase(GameDataKey.WEAPON_INDATE_ETERNAL, eternalTimes);
			
			JOptionPane.showMessageDialog(this, "配置数据已经保存");
		} else if ( COMMAND_IMPORT_ITEM.equals(e.getActionCommand()) ) {
			int option = JOptionPane.showConfirmDialog(this, "您确定要将道具价格导入数据库吗?");
			if ( option == JOptionPane.YES_OPTION ) {
				ShopDataImportItemPriceService service = new ShopDataImportItemPriceService();
				JDialog dialog = service.getDialog();
				service.execute();
				dialog.setVisible(true);
			}			
		}
	}

	private ShopDataPriceConfig parsePriceConfig() {
		double goldenUnitPrice = Double.parseDouble(this.goldenToPowerField.getText());
		double medalUnitPrice = Double.parseDouble(this.medalToPowerField.getText());
		double voucherUnitPrice = Double.parseDouble(this.voucherToPowerField.getText());
		double yuanbaoUnitPrice = Double.parseDouble(this.yuanbaoToPowerField.getText());
		int randomMin = Integer.parseInt(this.randomMinField.getText());
		int randomMax = Integer.parseInt(this.randomMaxField.getText());
		double simpleDiscount = Double.parseDouble(this.simpleTimesDiscountField.getText());
		double normalDiscount = Double.parseDouble(this.normalTimesDiscountField.getText());
		double solidDiscount = Double.parseDouble(this.solidTimesDiscountField.getText());
		double eternalDiscount = Double.parseDouble(this.eternalTimesDiscountField.getText());
		
		ShopDataPriceConfig config = new ShopDataPriceConfig();
		config.setGoldenPriceRate(goldenUnitPrice);
		config.setMedalPriceRate(medalUnitPrice);
		config.setVoucherPriceRate(voucherUnitPrice);
		config.setYuanbaoPriceRate(yuanbaoUnitPrice);
		config.setRandomMin(randomMin);
		config.setRandomMax(randomMax);
		config.setSimpleDiscount(simpleDiscount);
		config.setNormalDiscount(normalDiscount);
		config.setSolidDiscount(solidDiscount);
		config.setEternalDiscount(eternalDiscount);
		
		return config;
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
				frame.add(TaskEquipmentExportPanel.getInstance());
				frame.setSize(1030, 800);
				frame.setVisible(true);
			}
		});
	}
}
