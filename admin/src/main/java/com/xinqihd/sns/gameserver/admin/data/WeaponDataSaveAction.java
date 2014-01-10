package com.xinqihd.sns.gameserver.admin.data;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;

import com.xinqihd.sns.gameserver.admin.util.ImageUtil;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.service.CopyMongoCollectionService;

/**
 * 将生成的Weapon数据保存到new_equipments表中
 * @author wangqi
 *
 */
public class WeaponDataSaveAction extends AbstractAction {
	
	private String targetDatabase;
	private String targetNamespace;
	private String targetCollection;
	private Collection<WeaponPojo> weapons;

	public WeaponDataSaveAction(
			WeaponTableModel tableModel, 
			String targetDatabase, String targetNamespace, String targetCollection) {
		super("", ImageUtil.createImageSmallIcon("Folder.png", "Save"));
		this.weapons = tableModel.getWeaponList();
		this.targetDatabase = targetDatabase;
		this.targetNamespace = targetNamespace;
		this.targetCollection = targetCollection;
	}
	
	public void setIcon(Icon icon) {
		super.putValue(Action.SMALL_ICON, icon);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		WeaponDataSaveService service = new WeaponDataSaveService(
				this.weapons, 
				targetDatabase, targetNamespace, targetCollection
				);
		JDialog dialog = service.getDialog();
		service.execute();
		dialog.setVisible(true);
	}
	
}
