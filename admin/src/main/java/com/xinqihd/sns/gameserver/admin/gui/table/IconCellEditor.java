package com.xinqihd.sns.gameserver.admin.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyAbstractCellEditor;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class IconCellEditor extends MyAbstractCellEditor {
	
	private Object cellValue = null;
	
	@Override
	public Object getCellEditorValue() {
		return this.cellValue;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponentAtModel(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.cellValue = value;
		
		DefaultListModel listModel = new DefaultListModel();
//		File iconDir = ImageUtil.TMP_ASSETS_ICONS_FILE;
//		File[] iconFiles = iconDir.listFiles(new FilenameFilter() {
//			
//			@Override
//			public boolean accept(File dir, String name) {
//				if ( name.endsWith(".png") ) {
//					return true;
//				}
//				return false;
//			}
//		});
//		for ( int i=0; i<iconFiles.length; i++ ) {
//			listModel.addElement(iconFiles[i]);
//		}
		for ( String iconName : MainFrame.ICON_MAPS.keySet() ) {
			listModel.addElement(iconName);
		}
		
		ListSelectDialog dialog = new ListSelectDialog(listModel, new IconCellRenderer());
		int selectIndex = dialog.getList().getSelectedIndex();
		if ( selectIndex > -1 ) {
			Object selectValue = listModel.get(selectIndex);
			String iconId = selectValue.toString();
			this.cellValue = iconId;
			table.getModel().setValueAt(cellValue, row, column);
		}
		
		return null;
	}
	
	private String convertFilenameToId(File file) {
		String fileName = file.getName();
		String iconId = fileName.substring(0, fileName.length()-4);
		return iconId;
	}

}
