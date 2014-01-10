package com.xinqihd.sns.gameserver.admin.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextField;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.AddRemoveList;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.model.ArrayListModel;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 用于强化和合成面板的收集概率的通用面板
 * @author wangqi
 *
 */
public class CraftProxPanel extends MyPanel implements ActionListener {
	
	//收集的概率的数量
	private int range = 10;
	private ArrayList<Double> proxList = new ArrayList<Double>(range);

	private JXLabel proxLbl = new JXLabel("正太分布概率参数:");
	JXLabel uLbl = new JXLabel("U:");
	JXLabel qLbl = new JXLabel("Q:");
	private JXTextField qField = new JXTextField("1.0");
	private JXTextField uField = new JXTextField("0.0");
	private JXButton    genBtn = new JXButton("生成概率");
	private JXLabel  proxLabel = new JXLabel("概率列表");
	private AddRemoveList proxAddRemoveList = null;
	
	public CraftProxPanel() {
		this(null);
	}
	
	public CraftProxPanel(double[] ratios) {
		if ( ratios != null ) {
			this.range = ratios.length;
			for ( double d : ratios ) {
				this.proxList.add(d);
			}
		}
		init();
	}
	
	public void init() {
		this.uLbl.setHorizontalAlignment(JLabel.RIGHT);
		this.qLbl.setHorizontalAlignment(JLabel.RIGHT);
		this.genBtn.addActionListener(this);
		this.genBtn.setActionCommand(ActionName.OK.name());
		this.qField.setText("1.0");
		this.uField.setText("0.0");
		proxAddRemoveList = new AddRemoveList(this.proxList.toArray(new Double[0]));
		
		this.setLayout(new MigLayout("wrap 2, ins 0"));
		
		this.add(proxLbl, "");
		this.add(uLbl,   "span, split 5, width 10%");
		this.add(uField, "width 10%");
		this.add(qLbl,   "width 10%");
		this.add(qField, "width 10%");
		this.add(genBtn, "width 10%");
		this.add(proxLabel, "newline, span");
		this.add(proxAddRemoveList, "span, width 100%");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.name().equals(e.getActionCommand())) {
			double u = Double.parseDouble(uField.getText());
			double q = Double.parseDouble(qField.getText());
			int max = 10000;
			final int[] ratio = new int[range];
			for ( int i=0; i<max; i++ ) {
				int r = (int)(MathUtil.nextGaussionDouble(u, q) * range);
				ratio[r%range]++;
			}
			double total = 0.0;
			ArrayListModel listModel = proxAddRemoveList.getListModel();
			this.proxList.clear();
			listModel.clear();
			for ( int i=0; i<ratio.length; i++ ) {
				double r = (ratio[i]*1.0/max);
				listModel.insertRow(r);
				total += r;
			}
			System.out.println("total:"+total);
		}
	}
	
	/**
	 * 获取最终的概率列表
	 * @return
	 */
	public List<Double> getProxList() {
		return (List<Double>)this.proxAddRemoveList.getListModel().getList();
	}
}
