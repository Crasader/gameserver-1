package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import script.CraftForgeEquip;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.admin.util.ObjectUtil;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.forge.ForgeStatus;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 模拟强化石强化的过程
 * @author wangqi
 *
 */
public class StrengthTestService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在生成模拟玩家数据");
	private Stage stage = Stage.INIT;
	private StrengthTestResultModel model = null;
	private StrengthTestConfig config = null;
	private int count = 0;
	private MyTablePanel tablePanel;
	private LinkedHashMap<Integer, StrengthTestResult> resultMap = 
			new LinkedHashMap<Integer, StrengthTestResult>();
	
	String luckyStone15 = String.valueOf(GameDataManager.getInstance().
			getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY1, 24002));
	String luckyStone25 = String.valueOf(GameDataManager.getInstance().
			getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCKY2, 24004));
	String godStoneId = String.valueOf(GameDataManager.getInstance().
			getGameDataAsInt(GameDataKey.CRAFT_STONE_GOD, 24001));
	String strengthStoneId = String.valueOf(GameDataManager.getInstance().
			getGameDataAsInt(GameDataKey.CRAFT_STONE_STRENGTH, 20005));
	
	public StrengthTestService( 
			StrengthTestConfig config, MyTablePanel tablePanel) {
		this.model = model;
		this.config = config;
		this.count = config.getMaxTry();
		this.tablePanel = tablePanel;
		
		panel = new JXPanel();
		panel.setLayout(new MigLayout("wrap 1"));
		panel.add(label, "growx, wrap 20");
		panel.add(progressBar, "grow, push");
		
		dialog = new JDialog();
		dialog.add(panel);
		dialog.setSize(300, 120);
		Point p = WindowUtils.getPointForCentering(dialog);
		dialog.setLocation(p);
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				
		model = new StrengthTestResultModel();
		tablePanel.setTableModel(model);
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		try {
			stage = Stage.INIT;
			//initialize
			publish();
			Thread.sleep(200);
			
			stage = Stage.DO_TEST;
			User user = new User();
			user.setGolden(Integer.MAX_VALUE);
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
			PropData equipProp = weapon.toPropData(100, WeaponColor.WHITE);
			ArrayList<PropData> stones = new ArrayList<PropData>(6);
			ItemManager manager = ItemManager.getInstance();
			if ( config.isUse15Lucky() ) {
				stones.add(manager.getItemById(luckyStone15).toPropData());
			}
			if ( config.isUse25Lucky() ) {
				stones.add(manager.getItemById(luckyStone25).toPropData());
			}
			if ( config.isUseGod() ) {
				stones.add(manager.getItemById(godStoneId).toPropData());
			}
			model.setTotalCount(config.getMaxTry());
			for ( int i=0; i<4; i++ ) {
				ItemPojo item = manager.getItemByTypeIdAndLevel(strengthStoneId, config.getStoneLevel()+1);
				stones.add(item.toPropData());
			}
			//
			List<Double> stoneRatios = config.getProxList();
			double[] ratios = new double[stoneRatios.size()];
			for ( int i=0; i<ratios.length; i++ ) {
				ratios[i] = (Double)ObjectUtil.convertValue(stoneRatios.get(i), Double.class);
			}
			GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.STRENGTH_STONE_RATIO, ratios);
			PropData origProp = equipProp.clone();
			int lastEquipLevel = equipProp.getLevel();
			for ( int i=0; i<count; i++ ) {
				StrengthTestResult sr = resultMap.get(equipProp.getLevel());
				if ( sr == null ) {
					sr = new StrengthTestResult();
					sr.setLevelDesc(""+(equipProp.getLevel()+1));
					resultMap.put(equipProp.getLevel(), sr);
				}
				sr.addCostMoney(config.getBasePrice());
				sr.addTryCount();
				//强化
				ScriptResult result = CraftForgeEquip.func(
						new Object[]{user, new Object[]{equipProp, stones.toArray(new PropData[0])}});
				List list = result.getResult();
				ForgeStatus status = (ForgeStatus)list.get(0);
				equipProp = (PropData)list.get(1);
				if (status == ForgeStatus.SUCCESS ) {
					sr.addSuccessCount();
					sr.setCostMoney(sr.getCostMoney()+config.getBasePrice());
					sr.setIncreaseRatio(equipProp.getAttackLev()*1.0/origProp.getAttackLev());
				}
				if ( equipProp.getLevel() < lastEquipLevel ) {
					sr.addDownLevelCount();
				}
				lastEquipLevel = equipProp.getLevel();
				publish(i);
			}
			for ( StrengthTestResult r: resultMap.values() ) {
				r.setLevelDesc(r.getLevelDesc());
				model.insertRow(r);
			}
			resultMap.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
		} else if ( stage == Stage.DO_TEST ) {
			label.setText("正在模拟强化...");
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
		this.tablePanel.getTable().packAll();
	}

	static enum Stage {
		INIT,
		DO_TEST,
	}

}
