package com.xinqihd.sns.gameserver.admin.data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 备份所有的配置数据库
 * @author wangqi
 *
 */
public class WeaponDataGeneratorService extends SwingWorker<List<WeaponPojo>, Void> {
	
	private JDialog dialog = null;
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("正在生成数据");
	private Stage stage = Stage.INIT;
	private int totalCount = 0;
	
	private String database = ConfigManager.getConfigAsString(ConfigKey.mongoConfigDBName);
	private String namespace = ConfigManager.getConfigAsString(ConfigKey.mongoConfigNamespace);
	
	private List<WeaponPojo> origWeaponList = null;
	private ArrayList<WeaponPojo> genWeaponList = null;
	private ArrayList<String> namePrefixes = new ArrayList<String>();
	private ArrayList<Integer> dprList = new ArrayList<Integer>();
	
	private static final int ATTACK = 0;
	private static final int DEFEND = 1;
	private static final int AGILITY = 2;
	private static final int LUCK = 3;
	
	private double attackUnit = 1.3;
	private double defendUnit = 1.5;
	private double luckUnit = 4253.9;
	private double agilityUnit = 3740;
	
	//总能力表示为DPR的2倍
	private int totalDprRatio = 2;
	
	public WeaponDataGeneratorService(List<String> namePrefixes, List<Integer> dprList, double[] params) {
		this.origWeaponList = new ArrayList<WeaponPojo>();
		List<DBObject> list = MongoUtil.queryAllFromMongo(null, database, namespace, "equipments", null);
		for ( DBObject obj : list ) {
			WeaponPojo weapon = (WeaponPojo)MongoUtil.constructObject(obj);
			this.origWeaponList.add(weapon);
		}
		Collections.sort(this.origWeaponList);
		
		if ( namePrefixes != null ) {
			this.namePrefixes.addAll(namePrefixes);
		}
		if ( dprList != null ) {
			this.dprList.addAll(dprList);
		}
		
		attackUnit = params[0];
		defendUnit = params[1];
		luckUnit = params[2];
		agilityUnit = params[3];
		
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
		
		this.progressBar.setIndeterminate(true);
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	 
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected List<WeaponPojo>  doInBackground() throws Exception {
		this.genWeaponList = new ArrayList<WeaponPojo>(origWeaponList.size()*namePrefixes.size());
		
		/**
			0: 45.59%
			1: 32.42%
			2: 15.36%
			3: 5.18%
			4: 1.25%
			5: 0.18%
			6: 0.01%
			7: 0.01%
			8: 0.0%
			9: 0.0%
		 */
		Random random = new Random();
		for ( int i=0; i<origWeaponList.size(); i++ ) {
			//首先判断该道具应该有哪些属性最佳，numberOfGood为1时道具能力非常集中，
			//numberOfGood为4时各个属性较平均
			int numberOfGood = MathUtil.nextGaussionInt(0, 10) + 1;
			if ( numberOfGood>4 ) {
				numberOfGood = 4;
			}
			
			ArrayList<Integer> capability = new ArrayList<Integer>();
			capability.add(ATTACK);
			capability.add(DEFEND);
			capability.add(LUCK);
			capability.add(AGILITY);
			
			WeaponPojo weapon = origWeaponList.get(i);
			if ( weapon.getSlot() == EquipType.WEAPON ) {
				//武器类型默认为攻击强或者防御强
				double q = random.nextDouble();
				if ( q >= 0.5 ) {
					capability.set(0, DEFEND);
					capability.set(1, ATTACK);
				}
			} else {
				Collections.shuffle(capability);
			}
			
			int[] capTypes = new int[capability.size()];
			for ( int x=0; x<capTypes.length; x++ ) {
				capTypes[x] = capability.get(x);
			}
			double[] dprRatios = getDprCapability(capTypes, numberOfGood);
			for ( int j=0; j<namePrefixes.size(); j++ ) {
				WeaponPojo pojo = origWeaponList.get(i).clone();
				pojo.setId(String.valueOf(i*namePrefixes.size()+j));
				pojo.setName(namePrefixes.get(j)+"●"+pojo.getName());
				pojo.setsName(namePrefixes.get(j)+"●"+pojo.getsName());
				pojo.setTypeName(String.valueOf(i));
				pojo.setUserLevel(j*10);
				pojo.setAddAgility(0);
				pojo.setAddAttack(0);
				pojo.setAddBlood(0);
				pojo.setAddBloodPercent(0);
				pojo.setAddDamage(0);
				pojo.setAddDefend(0);
				pojo.setAddSkin(0);
				pojo.setAddThew(0);
				
				/**
				 * 对于武器类型，保持原有DPR数值，对于其他类型
				 * 将DPR缩减为原有数值的1/10，以保持数值平衡
				 */
				double slotTypeRatio = 1.0;
				if ( weapon.getSlot() != EquipType.WEAPON ) {
					slotTypeRatio = 0.1;
				}
				
				if ( pojo != null ) {
					for ( int x=0; x<capTypes.length; x++ ) {
						int capType = capTypes[x];
						switch ( capType ) {
							case ATTACK:
								pojo.setAddAttack( (int)(dprRatios[x] * totalDprRatio * attackUnit * this.dprList.get(j) * slotTypeRatio ));
								break;
							case DEFEND:
								pojo.setAddDefend( (int)(dprRatios[x] * totalDprRatio * defendUnit * this.dprList.get(j) * slotTypeRatio));
								break;
							case AGILITY:
								pojo.setAddAgility( (int)(dprRatios[x] * totalDprRatio * agilityUnit * slotTypeRatio));
								break;
							case LUCK:
								pojo.setAddLuck( (int)(dprRatios[x] * totalDprRatio * luckUnit * slotTypeRatio));
								break;
						}
					}
				}
				genWeaponList.add(pojo);
			}
		}
		return genWeaponList;
	}
	
	public double[] getDprCapability(int[] capTypes, int numberOfGood) {
		double[] dprRatios = new double[4];

		switch (numberOfGood) {
			case 1:
				dprRatios[0] = (0.6 + MathUtil.nextGaussionDouble(0, 1.0)*0.2);
				dprRatios[1] = (1.0 - dprRatios[0])/3;
				dprRatios[2] = dprRatios[1]+getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*dprRatios[1];
				dprRatios[3] = dprRatios[2]+getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*dprRatios[1];
				break;
			case 2:
				dprRatios[0] = (0.4 + MathUtil.nextGaussionDouble(0, 1.0)*0.1);
				dprRatios[1] = (0.4 + getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*0.1);
				dprRatios[2] = (1.1 - dprRatios[0] - dprRatios[1])/2;
				dprRatios[3] = dprRatios[2]+getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*dprRatios[2];
				break;
			case 3:
				dprRatios[0] = (0.3 + MathUtil.nextGaussionDouble(0, 1.0)*0.1);
				dprRatios[1] = (0.3 + getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*0.1);
				dprRatios[2] = (0.3 + getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*0.1);
				dprRatios[3] = (1.0 - dprRatios[0] - dprRatios[1] - dprRatios[2]);
				break;
			case 4:
				dprRatios[0] = (0.25 + getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*0.01);
				dprRatios[1] = (0.25 + getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*0.01);
				dprRatios[2] = (0.25 + getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*0.01);
				dprRatios[3] = (0.25 + getRandomSign()*MathUtil.nextGaussionDouble(0, 1.0)*0.01);
				break;
		}
		System.out.println("dpr: " + dprRatios[0]+","+dprRatios[1]+","+dprRatios[2]+","+dprRatios[3]+
				", sum="+(dprRatios[0]+dprRatios[1]+dprRatios[2]+dprRatios[3]));
		
		return dprRatios;
	}
	
	public int getRandomSign() {
		double d = MathUtil.nextDouble();
		if ( d < 0.5 ) {
			return 1;
		} else {
			return -1;
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
		READ_DATA,
		SAVE_DATA,
	}
	
//	public static void main(String[] args ) {
//		WeaponDataGeneratorService service = new WeaponDataGeneratorService(null, null);
//		int good = 1;
//		int loop = 1000;
//		for ( int i=0; i<loop; i++ ) {
//			service.setCapability(null, good, null);
//		}
//	}
}
	
