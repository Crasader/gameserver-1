package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXList;

import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.WeaponOrItemListCellRenderer;
import com.xinqihd.sns.gameserver.admin.util.MixComparator;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;

/**
 * 为用户选择武器或者道具提供一个列表
 * @author wangqi
 *
 */
public class WeaponAndItemPanel extends MyPanel implements ActionListener {
	
	public static final int ENABLE_WEAPON = 1;
	public static final int ENABLE_ITEM = 2;
	
	private static final String COMMAND_WEAPON = "weapon";
	private static final String COMMAND_ITEM = "item";
	
	private JRadioButton weaponBtn = new JRadioButton("武器");
	private JRadioButton itemBtn = new JRadioButton("道具");
	private DefaultListModel listModel = new DefaultListModel();
	private JXList list = new JXList(listModel);
	private int option = 3;
	private boolean enabled = true;
	
	public WeaponAndItemPanel() {
		this(3);
	}
	
	public WeaponAndItemPanel(int option) {
		this.option = option;
		init();
	}
	
	public void init() {
		ButtonGroup group = new ButtonGroup();
		group.add(weaponBtn);
		group.add(itemBtn);
		
		this.weaponBtn.setEnabled(true);
		this.itemBtn.setEnabled(true);
		switch ( option ) {
			case ENABLE_WEAPON:
				this.weaponBtn.setSelected(true);
				Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
				for ( WeaponPojo pojo : weapons ) {
					listModel.addElement(pojo);
				}
				break;
			case ENABLE_ITEM:
				this.itemBtn.setEnabled(true);
				this.itemBtn.setSelected(true);
				Collection<ItemPojo> items = ItemManager.getInstance().getItems();
				for ( ItemPojo pojo : items ) {
					listModel.addElement(pojo);
				}
				break;
			case 3:
				this.weaponBtn.setEnabled(true);
				this.weaponBtn.setSelected(true);
				weapons = EquipManager.getInstance().getWeapons();
				for ( WeaponPojo pojo : weapons ) {
					listModel.addElement(pojo);
				}
				this.weaponBtn.setEnabled(true);
				this.itemBtn.setEnabled(true);
				break;
		}
		
		this.weaponBtn.setActionCommand(COMMAND_WEAPON);
		this.weaponBtn.addActionListener(this);
		this.itemBtn.setActionCommand(COMMAND_ITEM);
		this.itemBtn.addActionListener(this);
		
		this.list.setCellRenderer(new WeaponOrItemListCellRenderer());
		this.list.setComparator(new Comparator<Pojo>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Pojo o1, Pojo o2) {
				if ( o1 != null && o1.getId() != null && o2 != null && o2.getId() != null ) {
					MixComparator comp = new MixComparator();
					return comp.compare(o1.getId(), o2.getId());
				}
				return -1;
			}
			
		});
		this.list.setAutoCreateRowSorter(true);
		this.list.setSortOrder(SortOrder.ASCENDING);
		
		JScrollPane pane = new JScrollPane(list);
		
		this.setLayout(new MigLayout("wrap 2"));
		this.add(weaponBtn, "span, split 2, align center");
		this.add(itemBtn, "");
		this.add(pane,  "span, width 100%, height 90%");
		this.setBorder(BorderFactory.createEtchedBorder());
	}
	
	public Object[] getSelectedValues() {
		if ( enabled ) {
			return list.getSelectedValues();
		} else {
			return null;
		}
	}
	
	public Object getSelectedValue() {
		if ( enabled ) {
			return list.getSelectedValue();
		} else {
			return null;
		}
	}
	
	public JXList getList() {
		return this.list;
	}
	
	public boolean isWeaponSelected() {
		return this.weaponBtn.isSelected();
	}
	
	public void addListSelectionListener(ListSelectionListener listener) {
		this.list.addListSelectionListener(listener);
	}
	
	/**
	 * 
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
//		this.itemBtn.setEnabled(enabled);
//		this.weaponBtn.setEnabled(enabled);
//		this.list.setEnabled(enabled);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( COMMAND_WEAPON.equals(e.getActionCommand()) ) {
			listModel.removeAllElements();
			Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
			for ( WeaponPojo pojo : weapons ) {
				listModel.addElement(pojo);
			}
		} else if ( COMMAND_ITEM.equals(e.getActionCommand()) ) {
			listModel.removeAllElements();
			Collection<ItemPojo>   items   = ItemManager.getInstance().getItems();
			for ( ItemPojo pojo : items ) {
				listModel.addElement(pojo);
			}
		}
	}
	
}
