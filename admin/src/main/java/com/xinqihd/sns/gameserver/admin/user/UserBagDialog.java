package com.xinqihd.sns.gameserver.admin.user;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.PropDataListCellRenderer;
import com.xinqihd.sns.gameserver.admin.gui.ext.SelectDialog;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;

/**
 * It implements the GameDataPanel
 * 
 * @author wangqi
 * 
 */
public class UserBagDialog extends MyDialog implements ActionListener {
	
	private static final String COMMAND_ADD_NEW = "addnew";
	private static final String COMMAND_MODIFY_ITEM = "modifyitem";
	private static final String COMMAND_REMOVE_ITEM = "removeitem";
	private static final String COMMAND_WEAR_ITEM = "wearitem";

	private User user = null;
	private Bag  bag  = null;
	
	private JLabel currCountLbl = new JLabel("背包当前数量:");
	private JLabel maxCountLbl = new JLabel("背包最大数量:");
	private JTextField currCountTf = new JTextField(15);
	private JTextField maxCountTf = new JTextField(15);
	private JXList bagList = new JXList();
	private JXButton[] wearBtns = new JXButton[PropDataEquipIndex.values().length];
	private JXLabel userStatusLbl = new JXLabel();
	
	private JXButton addNewItemToBagBtn = new JXButton("向背包中添加装备");
	private JXButton removeItemFromBagBtn = new JXButton("删除背包中的装备");
	private JXButton wearBagItemBtn = new JXButton("装备背包中的武器");
	private JXButton modiBagItemBtn = new JXButton("修改武器属性");
	private JXButton okButton = new JXButton("保存修改");
	private JXButton cancelButton = new JXButton("取消修改");
	
	public UserBagDialog(User user) {
		this.user = user;
		this.bag  = user.getBag();
		
		currCountTf.setText(String.valueOf(bag.getCurrentCount()));
		currCountTf.setEnabled(true);
		maxCountTf.setText(String.valueOf(bag.getMaxCount()));
		maxCountTf.setEnabled(true);

		DefaultListModel listModel = new DefaultListModel();
		bagList.setModel(listModel);
		bagList.setCellRenderer(new PropDataListCellRenderer());
		bagList.setDragEnabled(true);
		bagList.setHighlighters(HighlighterFactory.createAlternateStriping());
		bagList.addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getClickCount() == 2 ) {
					commandWearItem();
				}
			}
			
		});
		JScrollPane bagPane = new JScrollPane(bagList);
		bagPane.setBorder(BorderFactory.createTitledBorder("玩家当前背包内容"));
		
		updateBagStatus();
		updateWearStatus();
		updateUserStatus();
		
//		this.setBorder(BorderFactory.createTitledBorder("玩家" + user.getUsername() + "背包数据"));
		this.setLayout(new MigLayout("ins 10px"));
		this.add(currCountLbl, "");
		this.add(currCountTf, "gapright 10px");
		this.add(maxCountLbl, "");
		this.add(maxCountTf, "wrap");
		
		this.add(bagPane, "dock east, width 30%, height 100%, wrap");
		JXPanel wearPanel = new JXPanel();
		wearPanel.setLayout(new MigLayout("wrap 4"));
		for ( int i=1; i<wearBtns.length; i++ ) {
			if ( i % 4 == 0 ) {
				wearPanel.add(wearBtns[i], "sg wear, wrap, height 36px");
			} else {
				wearPanel.add(wearBtns[i], "sg wear, height 36px");
			}
		}
		this.add(wearPanel, "span, align center");
		
		this.addNewItemToBagBtn.setActionCommand(COMMAND_ADD_NEW);
		this.removeItemFromBagBtn.setActionCommand(COMMAND_REMOVE_ITEM);
		this.wearBagItemBtn.setActionCommand(COMMAND_WEAR_ITEM);
		this.modiBagItemBtn.setActionCommand(COMMAND_MODIFY_ITEM);
		
		this.okButton.setActionCommand(ActionName.OK.name());
		this.cancelButton.setActionCommand(ActionName.CANCEL.name());
		
		this.addNewItemToBagBtn.addActionListener(this);
		this.removeItemFromBagBtn.addActionListener(this);
		this.wearBagItemBtn.addActionListener(this);
		this.modiBagItemBtn.addActionListener(this);
		this.okButton.addActionListener(this);
		this.cancelButton.addActionListener(this);
		
		JXPanel btnPanel = new JXPanel();
		btnPanel.setLayout(new MigLayout("wrap 4"));
		btnPanel.add(addNewItemToBagBtn, "sg btn");
		btnPanel.add(removeItemFromBagBtn, "sg btn");
		btnPanel.add(wearBagItemBtn, "sg btn");
		btnPanel.add(modiBagItemBtn, "sg btn, wrap");
		btnPanel.add(okButton, "span, split 2, align center");
		btnPanel.add(cancelButton, "");
		
		this.userStatusLbl.setBackground(Color.WHITE);
		this.userStatusLbl.setFont(MainFrame.BIG_FONT);
		this.userStatusLbl.setHorizontalTextPosition(JLabel.CENTER);
		this.userStatusLbl.setBorder(BorderFactory.createEtchedBorder());

		
		this.add(userStatusLbl, "newline, span, width 70%, height 100px, align center, wrap");
		this.add(btnPanel, "dock south, span, align center");
		
		this.setModal(true);
		this.setSize(800, 600);
		Point p = WindowUtils.getPointForCentering(this);
		this.setLocation(p);
	}

	public void updateWearStatus() {
		List<PropData> wearedItem = bag.getWearPropDatas();
		for ( int i = 1; i<PropDataEquipIndex.values().length; i++ ) {
			PropData propData = wearedItem.get(i);
			if ( wearBtns[i] == null ) {
				wearBtns[i] = new JXButton();
				wearBtns[i].setActionCommand(PropDataEquipIndex.values()[i].name());
				wearBtns[i].addActionListener(this);
			}
			if ( propData != null ) {
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getItemId());
				wearBtns[i].setIcon(MainFrame.ICON_MAPS.get(weapon.getIcon()));
				wearBtns[i].setText(propData.getName());
			} else {
				wearBtns[i].setIcon(null);
				wearBtns[i].setText(PropDataEquipIndex.values()[i].name());
			}
		}
	}
	
	public void updateBagStatus() {
		DefaultListModel listModel = (DefaultListModel)this.bagList.getModel();
		listModel.removeAllElements();
		List<PropData> propDataList = bag.getOtherPropDatas();
		for ( PropData propData : propDataList ) {
			if ( propData != null ) {
				listModel.addElement(propData);
			}
		}
		this.currCountTf.setText(""+bag.getCurrentCount());
		this.maxCountTf.setText(""+bag.getMaxCount());
	}
	
	public void updateUserStatus() {
		StringBuilder buf = new StringBuilder();
		buf.append("<html>战斗力: ");
		buf.append(this.user.getPower());
		buf.append("<table><tr><td>攻击: </td><td>");
		buf.append(this.user.getAttack());
		buf.append("</td><td>防御: </td><td>");
		buf.append(this.user.getDefend());
		buf.append("</td><td>敏捷: </td><td>");
		buf.append(this.user.getAgility());
		buf.append("</td><td>幸运: </td><td>");
		buf.append(this.user.getLuck());
		buf.append("</td></tr><tr><td>血量: </td><td>");
		buf.append(this.user.getBlood());
		buf.append("</td><td>体力:</td><td>");
		buf.append(this.user.getTkew());
		buf.append("</td><td>伤害:</td><td>");
		buf.append(this.user.getDamage());
		buf.append("</td><td>护甲:</td><td></tr>");
		buf.append(this.user.getSkin());
		buf.append("</table></html>");
		
		this.userStatusLbl.setText(buf.toString());
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( COMMAND_ADD_NEW.equals(e.getActionCommand()) ) {
			AddItemToBagDialog dialog = new AddItemToBagDialog();
			dialog.setVisible(true);
			PropData propData = dialog.getPropData();
			if ( propData != null ) {
				bag.addOtherPropDatas(propData);
			}
			updateBagStatus();
		} else if (COMMAND_REMOVE_ITEM.equals(e.getActionCommand()) ) {
			Object[] propDatas = this.bagList.getSelectedValues();
			if ( propDatas == null || propDatas.length == 0 ) {
				JOptionPane.showMessageDialog(this, "您还没有在背包中选择要删除的项目");
			} else {
				for ( int i=0; i<propDatas.length; i++ ) {
					PropData propData = (PropData)propDatas[i];
					bag.removeOtherPropDatas(propData.getPew());
				}
			}
			updateBagStatus();			
		} else if (COMMAND_WEAR_ITEM.equals(e.getActionCommand()) ) {
			commandWearItem();
		} else if (ActionName.OK.name().equals(e.getActionCommand()) ) {
			int option = JOptionPane.showConfirmDialog(this, "确定要保存更改吗?", 
					"", JOptionPane.YES_NO_OPTION);
			if ( option == JOptionPane.YES_OPTION ) {
				UserManager.getInstance().saveUser(user, false);
				UserManager.getInstance().saveUserBag(user, false);
				this.dispose();
			}
		} else if ( COMMAND_MODIFY_ITEM.equals(e.getActionCommand()) ) {
			AddItemToBagDialog dialog = new AddItemToBagDialog();
			PropData propData = (PropData)bagList.getSelectedValue();
			dialog.setPropData( propData );
			dialog.setVisible(true);
			propData = dialog.getPropData();
			if ( propData != null ) {
				bag.setOtherPropDataAtPew(propData, propData.getPew());
			}
			updateBagStatus();
		} else if (ActionName.CANCEL.name().equals(e.getActionCommand()) ) {
			int option = JOptionPane.showConfirmDialog(this, "确定要放弃更改并退出吗?", 
					"", JOptionPane.YES_NO_OPTION);
			if ( option == JOptionPane.YES_OPTION ) {
				this.dispose();
			}
		} else {
			if ( e.getSource() instanceof JXButton ) {
				JXButton wearBtn = (JXButton)e.getSource();
				String type = wearBtn.getActionCommand();
				PropDataEquipIndex index = PropDataEquipIndex.valueOf(type);
				if ( index != null ) {
					bag.wearPropData(index.index(), -1);
					updateBagStatus();
					updateWearStatus();
					updateUserStatus();
				}
			}
		}
	}
	
	private void wearItem(PropData propData, PropDataEquipIndex index) {
		if ( index == null || index == PropDataEquipIndex.NOTHING ) {
			return;
		}
		PropData oldData = bag.getWearPropDatas().get(index.index());
		if ( oldData != null ) {
			bag.wearPropData(oldData.getPew(), -1);
		}
		bag.wearPropData(propData.getPew(), index.index());
	}
	
	private void commandWearItem() {
		Object[] propDatas = this.bagList.getSelectedValues();
		if ( propDatas == null || propDatas.length == 0 ) {
			JOptionPane.showMessageDialog(this, "您还没有在背包中选择要装备的项目");
		} else {
			for ( int i=0; i<propDatas.length; i++ ) {
				PropData propData = (PropData)propDatas[i];
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getItemId());
				if ( weapon == null ) {
					JOptionPane.showMessageDialog(this, propData.getName()+"不是一件可装备的道具");
				} else {
					EquipType type = weapon.getSlot();
					switch ( type ) {
						case BUBBLE:
							wearItem(propData, PropDataEquipIndex.BUBBLE);
							break;
						case CLOTHES:
							wearItem(propData, PropDataEquipIndex.CLOTH);
							break;
						case DECORATION:
							PropDataEquipIndex index = (PropDataEquipIndex)SelectDialog.chooseSingleObject("请选择装备的位置", 
									PropDataEquipIndex.values());
							wearItem(propData, index);
							break;
						case EXPRESSION:
							index = (PropDataEquipIndex)SelectDialog.chooseSingleObject("请选择装备的位置", 
									PropDataEquipIndex.values());
							wearItem(propData, index);
							break;
						case FACE:
							wearItem(propData, PropDataEquipIndex.FACE);
							break;
						case GIFT_PACK:
							index = (PropDataEquipIndex)SelectDialog.chooseSingleObject("请选择装备的位置", 
									PropDataEquipIndex.values());
							wearItem(propData, index);
							break;
						case GLASSES:
							wearItem(propData, PropDataEquipIndex.GLASS);
							break;
						case HAIR:
							wearItem(propData, PropDataEquipIndex.HAIR);
							break;
						case HAT:
							wearItem(propData, PropDataEquipIndex.HAT);
							break;
						case ITEM:
							index = (PropDataEquipIndex)SelectDialog.chooseSingleObject("请选择装备的位置", 
									PropDataEquipIndex.values());
							wearItem(propData, index);
							break;
						case JEWELRY:
							index = (PropDataEquipIndex)SelectDialog.chooseSingleObject("请选择装备的位置", 
									PropDataEquipIndex.values());
							wearItem(propData, index);
							break;
						case OFFHANDWEAPON:
							wearItem(propData, PropDataEquipIndex.SUBWEAPON);
							break;
						case OTHER:
							index = (PropDataEquipIndex)SelectDialog.chooseSingleObject("请选择装备的位置", 
									PropDataEquipIndex.values());
							wearItem(propData, index);
							break;
						case SUIT:
							wearItem(propData, PropDataEquipIndex.SUIT);
							break;
						case WEAPON:
							wearItem(propData, PropDataEquipIndex.WEAPON);
							break;
						case WING:
							wearItem(propData, PropDataEquipIndex.WING);
							break;
					}
				}
			}
			updateBagStatus();
			updateWearStatus();
			updateUserStatus();
		}
	}
}
