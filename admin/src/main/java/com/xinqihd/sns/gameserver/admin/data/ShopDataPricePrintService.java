package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 
 * @author wangqi
 *
 */
public class ShopDataPricePrintService extends SwingWorker<Void, Integer> {
	
	private int count = 0;
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在打印装备数据");
	private Stage stage = Stage.INIT;
	private MyTableModel model = null;
	private ShopDataPriceConfig config = null;
	
	public ShopDataPricePrintService(MyTableModel model, ShopDataPriceConfig config) {
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
			publish();
			
			stage = Stage.PRINT_PRICE;
			int i=0;
			for ( WeaponPojo weapon : weapons ) {
				int power = (int)EquipCalculator.calculateWeaponPower(weapon);

				weapon.setPower(power);
				int randomMin = config.getRandomMin();
				int randomMax = config.getRandomMax();
				if ( randomMax > randomMin && randomMin >= 0 ) {
					power += randomMin + (MathUtil.nextDouble()*(randomMax-randomMin)); 
				}
				ShopDataPricePrintResult result = new ShopDataPricePrintResult();
				result.setWeaponPojo(weapon);
				result.setGoldenPrice((int)Math.round(power * config.getGoldenPriceRate() * config.getSimpleDiscount()));
				result.setMedalPrice((int)Math.round(power * config.getMedalPriceRate() * config.getSimpleDiscount()));
				result.setVoucherPrice((int)Math.round(power * config.getVoucherPriceRate() * config.getSimpleDiscount()));
				result.setYuanbaoPrice((int)Math.round(power * config.getYuanbaoPriceRate() * config.getSimpleDiscount()));
				
				result.setNormalGoldPrice((int)Math.round(power * config.getGoldenPriceRate() * config.getNormalDiscount()));
				result.setNormalMedalPrice((int)Math.round(power * config.getMedalPriceRate() * config.getNormalDiscount()));
				result.setNormalVoucherPrice((int)Math.round(power * config.getVoucherPriceRate() * config.getNormalDiscount()));
				result.setNormalYuanbaoPrice((int)Math.round(power * config.getYuanbaoPriceRate() * config.getNormalDiscount()));
				
				result.setSolidGoldPrice((int)Math.round(power * config.getGoldenPriceRate() * config.getSolidDiscount()));
				result.setSolidMedalPrice((int)Math.round(power * config.getMedalPriceRate() * config.getSolidDiscount()));
				result.setSolidVoucherPrice((int)Math.round(power * config.getVoucherPriceRate() * config.getSolidDiscount()));
				result.setSolidYuanbaoPrice((int)Math.round(power * config.getYuanbaoPriceRate() * config.getSolidDiscount()));
				
				result.setEternalGoldPrice((int)Math.round(power * config.getGoldenPriceRate() * config.getEternalDiscount()));
				result.setEternalMedalPrice((int)Math.round(power * config.getMedalPriceRate() * config.getEternalDiscount()));
				result.setEternalVoucherPrice((int)Math.round(power * config.getVoucherPriceRate() * config.getEternalDiscount()));
				result.setEternalYuanbaoPrice((int)Math.round(power * config.getYuanbaoPriceRate() * config.getEternalDiscount()));
				
				model.insertRow(result);
				publish(i++);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

}
