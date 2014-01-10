package com.xinqihd.sns.gameserver.admin.gui.table;

import java.util.Comparator;

import javax.swing.table.TableRowSorter;

import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.util.MixComparator;

public class MyTableRowSorter extends TableRowSorter<MyTableModel> {

	/* (non-Javadoc)
	 * @see javax.swing.table.TableRowSorter#getComparator(int)
	 */
	@Override
	public Comparator<Object> getComparator(int column) {
		return new MixComparator();
	}

}
