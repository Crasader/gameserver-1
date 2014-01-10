package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 
 * @author wangqi
 *
 */
public class EquipmentExportService extends SwingWorker<Void, Integer> {
	
	private int count = 0;
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在导出装备基本数据");
	private Stage stage = Stage.INIT;
	private MyTableModel model = null;
	private ShopDataPriceConfig config = null;
	
	public EquipmentExportService(MyTableModel model, ShopDataPriceConfig config) {
		this.model = model;
		this.config = config;
		
		panel = new JXPanel();
		panel.setLayout(new MigLayout("wrap 1"));
		panel.add(label, "growx, wrap 20");
		panel.add(progressBar, "grow, push");
		this.dialog = new JDialog();
		this.dialog.add(panel);
		this.dialog.setSize(300, 120);
		Point p = WindowUtils.getPointForCentering(dialog);
		this.dialog.setLocation(p);
		this.dialog.setModal(true);
		this.dialog.setResizable(false);
		this.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		try {
			stage = Stage.INIT;
			//initialize
			model.clearAll();
			ArrayList<WeaponPojo> weapons = new ArrayList<WeaponPojo>(EquipManager.getInstance().getWeapons());
			Collections.sort(weapons);
			this.count = weapons.size();
			
		  //导出EXCEL文件
			JFileChooser chooser = new JFileChooser(
					new File("/Users/wangqi/disk/documents/20110418/宝贝战争/游戏运营/游戏改造"));
			int chooseResult = chooser.showSaveDialog(dialog);
			File file = null;
			if ( chooseResult == JFileChooser.APPROVE_OPTION ) {
				file = chooser.getSelectedFile();
			} else {
				JOptionPane.showMessageDialog(dialog, "没有选择导出的文件");
				return null;
			}
			
			publish();
			
			stage = Stage.PRINT_PRICE;
			//生成EXCEL文件
			HSSFWorkbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet("装备数据");
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue("goldenPriceRate");
			cell = row.createCell(1);
			cell.setCellValue(config.getGoldenPriceRate());
			cell = row.createCell(2);
			cell.setCellValue("yuanbaoPriceRate");
			cell = row.createCell(3);
			cell.setCellValue(config.getYuanbaoPriceRate());
			
			row = sheet.createRow(1);
			cell = row.createCell(0);
			cell.setCellValue("simpleDiscount");
			cell = row.createCell(1);
			cell.setCellValue(config.getSimpleDiscount());
			cell = row.createCell(2);
			cell.setCellValue("normalDiscount");
			cell = row.createCell(3);
			cell.setCellValue(config.getNormalDiscount());
			cell = row.createCell(4);
			cell.setCellValue("solidDiscount");
			cell = row.createCell(5);
			cell.setCellValue(config.getSolidDiscount());
			cell = row.createCell(6);
			cell.setCellValue("externalDiscount");
			cell = row.createCell(7);
			cell.setCellValue(config.getEternalDiscount());
			
			//Header
			createHeader(sheet, 2);
			int i=3;
			for ( WeaponPojo weapon : weapons ) {
				int power = (int)EquipCalculator.calculateWeaponPower(weapon);
				int actualPower = (int)calculateWeaponPower(weapon.getAddAttack(), weapon.getAddDefend(), 
						weapon.getAddAgility(), weapon.getAddLuck(), weapon.getAddBlood(), 
						weapon.getAddSkin(), weapon.getRadius(), weapon.getAddBloodPercent());
				if ( power != actualPower ) {
					JOptionPane.showMessageDialog(panel, "内置的战斗力计算与游戏服务器的算法不一致了:power="+power+",mypower="+actualPower, 
							"需要更新后台的战斗力算法", JOptionPane.ERROR_MESSAGE);
				}
				if ( weapon.getName().contains("●") && 
						!weapon.getName().startsWith("黑铁") ) {
					continue;
				}
				weapon.setPower(power);
				EquipmentExportExcelResult result = new EquipmentExportExcelResult();
				result.setWeaponPojo(weapon);
				
				row = sheet.createRow(i);
				int j=0;
				cell = row.createCell(j++);
				cell.setCellValue(StringUtil.toInt(weapon.getId(), 0));
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getName());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getInfo());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getQuality());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getQualityColor().toString());
				cell = row.createCell(j++);
				cell.setCellFormula(calculateWeaponPowerExcel(i+1, weapon));
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddAttack());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddDefend());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddAgility());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddLuck());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddBlood());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddBloodPercent());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddDamage());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getAddSkin());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getRadius());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getsRadius());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getBullet());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getSex().toString());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getSlot().toString());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getTypeName());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.getUserLevel());
				cell = row.createCell(j++);
				cell.setCellValue(weapon.isCanBeRewarded());
				cell = row.createCell(j++);
				//GoldenPrice
				cell.setCellFormula("round(F"+(i+1)+"*$B$1*$B$2, 0)");
				cell = row.createCell(j++);
				//YuanbaoPrice
				cell.setCellFormula("round(F"+(i+1)+"*$D$1*$B$2, 0)");

				//GoldenPrice
				cell = row.createCell(j++);
				cell.setCellFormula("round(F"+(i+1)+"*$B$1*$D$2, 0)");
				//YuanbaoPrice
				cell = row.createCell(j++);
				cell.setCellFormula("round(F"+(i+1)+"*$D$1*$D$2, 0)");

				//GoldenPrice
				cell = row.createCell(j++);
				cell.setCellFormula("round(F"+(i+1)+"*$B$1*$F$2, 0)");
				//YuanbaoPrice
				cell = row.createCell(j++);
				cell.setCellFormula("round(F"+(i+1)+"*$D$1*$F$2, 0)");

				//GoldenPrice
				cell = row.createCell(j++);
				cell.setCellFormula("round(F"+(i+1)+"*$B$1*$H$2, 0)");
				//YuanbaoPrice
				cell = row.createCell(j++);
				cell.setCellFormula("round(F"+(i+1)+"*$D$1*$H$2, 0)");

				model.insertRow(result);
				publish(i++);
			}
			
		  // Write the output to a file
	    FileOutputStream fileOut = new FileOutputStream(file);
	    workbook.write(fileOut);
	    fileOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param sheet
	 */
	private void createHeader(Sheet sheet, int rowNum) {
		Row row = sheet.createRow(rowNum);
		Cell cell = null;
		int i=0;
		cell = row.createCell(i++);
		cell.setCellValue("ID");
		cell = row.createCell(i++);
		cell.setCellValue("名称");
		cell = row.createCell(i++);
		cell.setCellValue("描述");
		cell = row.createCell(i++);
		cell.setCellValue("品质");
		cell = row.createCell(i++);
		cell.setCellValue("颜色");
		//F column
		cell = row.createCell(i++);
		cell.setCellValue("战斗力");
		cell = row.createCell(i++);
		cell.setCellValue("攻击");
		cell = row.createCell(i++);
		cell.setCellValue("防御");
		cell = row.createCell(i++);
		cell.setCellValue("敏捷");
		cell = row.createCell(i++);
		cell.setCellValue("幸运");
		cell = row.createCell(i++);
		cell.setCellValue("血量值");
		cell = row.createCell(i++);
		cell.setCellValue("血量比率");
		cell = row.createCell(i++);
		cell.setCellValue("伤害");
		cell = row.createCell(i++);
		cell.setCellValue("护甲");
		cell = row.createCell(i++);
		cell.setCellValue("半径(宽)");
		cell = row.createCell(i++);
		cell.setCellValue("半径(高)");
		cell = row.createCell(i++);
		cell.setCellValue("子弹");
		cell = row.createCell(i++);
		cell.setCellValue("性别");
		cell = row.createCell(i++);
		cell.setCellValue("卡槽");
		cell = row.createCell(i++);
		cell.setCellValue("类型ID");
		cell = row.createCell(i++);
		cell.setCellValue("用户等级");
		cell = row.createCell(i++);
		cell.setCellValue("是否抽奖");
		cell = row.createCell(i++);
		cell.setCellValue("金币简陋");
		cell = row.createCell(i++);
		cell.setCellValue("元宝简陋");
		cell = row.createCell(i++);
		cell.setCellValue("金币普通");
		cell = row.createCell(i++);
		cell.setCellValue("元宝普通");
		cell = row.createCell(i++);
		cell.setCellValue("金币坚固");
		cell = row.createCell(i++);
		cell.setCellValue("元宝坚固");
		cell = row.createCell(i++);
		cell.setCellValue("金币恒久");
		cell = row.createCell(i++);
		cell.setCellValue("元宝恒久");
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.INIT ) {						
			label.setFont(MainFrame.BIG_FONT);
			progressBar.setMaximum(count);
			progressBar.setStringPainted(true);
			
			dialog.setVisible(true);
		} else if ( stage == Stage.PRINT_PRICE ) {
			label.setText("正在打印装备价格...");
			if ( chunks != null && chunks.size()>0 ) {
				int percent = chunks.get(chunks.size()-1);
				progressBar.setValue(percent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		this.dialog.dispose();
	}

	static enum Stage {
		INIT,
		PRINT_PRICE,
	}

	private static final double calculateWeaponPower(
		int attack, int defend, int agility, int luck, int blood, int skin, int radius, int bloodPercent) {
	  // 战斗力
		double attackIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_ATTACK_INDEX, 1.2);
		double defendIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_DEFEND_INDEX, 1.3);
		double skinIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_SKIN_INDEX, 0.2);
	
		/*
			1.5	200	0.0075
			1.4	190	0.007368421
			1.3	180	0.007222222
			1.2	165	0.007272727
			1.1	150	0.007333333
		 */
		double dpr = (blood*0.5 + skin*skinIndex + attack * attackIndex + defend *defendIndex + agility * 0.8 + luck * 0.8);
		double power = dpr;
		
		float radiusRatio = radius/150.0f;
		power = power + (int)(power*radiusRatio*0.2);
		
		if ( bloodPercent > 0 ) {
			float bloodPerent = bloodPercent/100.0f;
			power = power + (int)(power*(1/bloodPerent));
		}

	  return Math.round(power);
	}
	
	private static final String calculateWeaponPowerExcel(int rowNum, WeaponPojo weapon) {
	  // 战斗力
		Double attackIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_ATTACK_INDEX, 1.2);
		Double defendIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_DEFEND_INDEX, 1.3);
		Double skinIndex = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_SKIN_INDEX, 0.2);
		Double luckTotal = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.BATTLE_CRITICAL_MAX, 4000);
	
		/*
			blood: K
			blood ratio: L
			skin:N
			attack:G
			defend:H
			luck:J
			agility:I
		 */
		String dpr = "(K"+rowNum+"*0.5+" + "N"+rowNum+"*"+skinIndex + "+G"+rowNum+ "*" + attackIndex + "+H"+rowNum+ "*"+defendIndex +
					"+I"+rowNum+ "*0.8"+ "+J"+rowNum+"*0.8"+
				")";
		//String power = dpr +"+" + dpr + "*("+"I"+rowNum+"*"+0.0074+")+" + dpr+"*"+"(J"+rowNum+"/"+luckTotal+"*"+"(1.5+2*J"+rowNum+"/"+luckTotal+"))";
		String power = dpr;
		
		String radiusRatio = "o"+rowNum+"/150.0";
		power = "round("+power + "+("+power+")*("+radiusRatio+")/1.5 , 0)";
		
		if ( weapon.getAddBloodPercent()>0 ) {
			power = "round("+power+"*(1+1/L"+rowNum+"/100), 0)";
		}

	  return power;
	}
}
