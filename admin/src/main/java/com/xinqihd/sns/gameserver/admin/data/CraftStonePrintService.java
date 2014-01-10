package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTablePanel;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;

/**
 * 生成模拟的玩家数据，并进行战斗测试
 * @author wangqi
 *
 */
public class CraftStonePrintService extends SwingWorker<Void, Integer> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在计算合成数据");
	private Stage stage = Stage.INIT;
	private CraftStoneResultModel model = null;
	private CraftStonePrintConfig config = null;
	private int count = 0;
	private MyTablePanel tablePanel;
	
	public CraftStonePrintService(CraftStoneResultModel model, 
			CraftStonePrintConfig config, int count, MyTablePanel tablePanel) {
		this.model = model;
		this.config = config;
		this.count = count;
		this.tablePanel = tablePanel;
		this.model.setTotalCount(count);
		
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
			publish();
			Thread.sleep(100);
			
			stage = Stage.DO_BALANCE_TEST;
			HashMap<Integer, CraftStoneResult> resultMap = new HashMap<Integer, CraftStoneResult>();
			
			double[] qArray = config.getqArray();
			GameDataManager.getInstance().overrideRuntimeValue(
					GameDataKey.FORGE_SIGMA_RATIO, qArray);
			String stoneTypeId = config.getStoneTypeId();
			double luckStone = 0.0;
			for ( int i=0; i<qArray.length; i++ ) {
				double q = qArray[i];
				for ( int k=0; k<count; k++ ) {
					int finalData = (int)EquipCalculator.calculateForgeData(config.getEquipProp(), i+1, stoneTypeId);
					storeResult(luckStone, resultMap, i, finalData);
					publish(k*i+k);
				}
			}
			luckStone = 0.15;
			for ( int i=0; i<qArray.length; i++ ) {
				double q = qArray[i];
				for ( int k=0; k<count; k++ ) {
					int finalData = (int)EquipCalculator.calculateForgeData(config.getEquipProp(), i+1, stoneTypeId);
					storeResult(luckStone, resultMap, i, finalData);
					publish(k*i+k+1);
				}
			}
			luckStone = 0.25;
			for ( int i=0; i<qArray.length; i++ ) {
				double q = qArray[i];
				for ( int k=0; k<count; k++ ) {
					int finalData = (int)EquipCalculator.calculateForgeData(config.getEquipProp(), i+1, stoneTypeId);
					storeResult(luckStone, resultMap, i, finalData);
					publish(k*i+k+2);
				}
			}
			
			int totalResultNumber = resultMap.size();
			HashMap<Integer, CraftStoneResult> compressResultMap = resultMap;

			if ( totalResultNumber > 50 ) {
				compressResultMap = new HashMap<Integer, CraftStoneResult>();
				int unit = totalResultNumber / 20;
				for ( Integer key : resultMap.keySet() ) {
					CraftStoneResult oldValue = resultMap.get(key);
					int newKey = key.intValue()/unit;
					CraftStoneResult newValue = compressResultMap.get(newKey);
					if ( newValue == null ) {
						newValue = new CraftStoneResult();
						compressResultMap.put(newKey, newValue);
					}
					newValue.setFinalData(newKey*unit);
					newValue.addCraftStoneResult(oldValue);
				}
			}

			ArrayList<Integer> keyList = new ArrayList<Integer>(compressResultMap.keySet());
			Collections.sort(keyList);
			for ( Integer key : keyList ) {
				CraftStoneResult result = compressResultMap.get(key);
				this.model.insertRow(result);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void storeResult(double luckStone,
			HashMap<Integer, CraftStoneResult> resultMap, int i, int finalData) {
		int stoneLevel = i+1;
		CraftStoneResult result = resultMap.get(finalData);
		if ( result == null ) {
			result = new CraftStoneResult();
			resultMap.put(finalData, result);
		}
		result.setFinalData(finalData);
		switch ( stoneLevel ) {
			case 1:
				if ( luckStone <= 0.0 ) {
					result.addStone1Count();
				} else if ( luckStone <= 0.15 ) {
					result.addStone1Luck15Count();
				} else if ( luckStone <= 0.25 ) {
					result.addStone1Luck25Count();
				}
				break;
			case 2:
				if ( luckStone <= 0.0 ) {
					result.addStone2Count();
				} else if ( luckStone <= 0.15 ) {
					result.addStone2Luck15Count();
				} else if ( luckStone <= 0.25 ) {
					result.addStone2Luck25Count();
				}
				break;
			case 3:
				if ( luckStone <= 0.0 ) {
					result.addStone3Count();
				} else if ( luckStone <= 0.15 ) {
					result.addStone3Luck15Count();
				} else if ( luckStone <= 0.25 ) {
					result.addStone3Luck25Count();
				}
				break;
			case 4:
				if ( luckStone <= 0.0 ) {
					result.addStone4Count();
				} else if ( luckStone <= 0.15 ) {
					result.addStone4Luck15Count();
				} else if ( luckStone <= 0.25 ) {
					result.addStone4Luck25Count();
				}
				break;
			case 5:
				if ( luckStone <= 0.0 ) {
					result.addStone5Count();
				} else if ( luckStone <= 0.15 ) {
					result.addStone5Luck15Count();
				} else if ( luckStone <= 0.25 ) {
					result.addStone5Luck25Count();
				}
				break;
		}
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
		} else if ( stage == Stage.DO_BALANCE_TEST ) {
			label.setText("正在模拟合成...");
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
		DO_BALANCE_TEST,
	}

}
