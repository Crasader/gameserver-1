package com.xinqihd.sns.gameserver.admin.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stolen from
 * :http://www.cordinc.com/blog/2010/12/cut-and-paste-from-java-swing.html
 * Thanks the author.
 * 
 * KeyAdapter to detect Windows standard cut, copy and paste keystrokes on a
 * JTable and put them to the clipboard in Excel friendly plain text format.
 * Assumes that null represents an empty column for cut operations. Replaces
 * line breaks and tabs in copied cells to spaces in the clipboard.
 * 
 * @see java.awt.event.KeyAdapter
 * @see javax.swing.JTable
 */
public class ClipboardKeyAdapter extends KeyAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(ClipboardKeyAdapter.class);

	private static final String LINE_BREAK = "\n";
	private static final String CELL_BREAK = "\t";
	private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit()
			.getSystemClipboard();

	private final JTable table;

	public ClipboardKeyAdapter(JTable table) {
		this.table = table;
	}

	@Override
	public void keyReleased(KeyEvent event) {
		if (event.isControlDown()) {
			if (event.getKeyCode() == KeyEvent.VK_C) { // Copy
				cancelEditing();
				copyToClipboard(false);
			} else if (event.getKeyCode() == KeyEvent.VK_X) { // Cut
				cancelEditing();
				copyToClipboard(true);
			} else if (event.getKeyCode() == KeyEvent.VK_V) { // Paste
				cancelEditing();
				pasteFromClipboard();
			}
		}
	}

	private void copyToClipboard(boolean isCut) {
		int numCols = table.getSelectedColumnCount();
		int numRows = table.getSelectedRowCount();
		int[] rowsSelected = table.getSelectedRows();
		int[] colsSelected = table.getSelectedColumns();
		if (numRows != rowsSelected[rowsSelected.length - 1] - rowsSelected[0] + 1
				|| numRows != rowsSelected.length
				|| numCols != colsSelected[colsSelected.length - 1] - colsSelected[0]
						+ 1 || numCols != colsSelected.length) {

			JOptionPane.showMessageDialog(null, "无效的拷贝",
					"无效的拷贝", JOptionPane.ERROR_MESSAGE);
			return;
		}

		StringBuffer excelStr = new StringBuffer();
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				Object value = table.getValueAt(rowsSelected[i], colsSelected[j]);
				excelStr.append(escape(value));
				if (isCut) {
					table.setValueAt(null, rowsSelected[i], colsSelected[j]);
				}
				if (j < numCols - 1) {
					excelStr.append(CELL_BREAK);
				}
			}
			excelStr.append(LINE_BREAK);
		}

		StringSelection sel = new StringSelection(excelStr.toString());
		CLIPBOARD.setContents(sel, sel);
	}

	private void pasteFromClipboard() {
		int startRow = table.getSelectedRows()[0];
		int startCol = table.getSelectedColumns()[0];

		String pasteString = "";
		try {
			pasteString = (String) (CLIPBOARD.getContents(this)
					.getTransferData(DataFlavor.stringFlavor));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "无效的粘贴",
					"无效的粘贴", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String[] lines = pasteString.split("\r|\n");
		for (int i = 0; i < lines.length; i++) {
			String[] cells = lines[i].split(CELL_BREAK);
			for (int j = 0; j < cells.length; j++) {
				logger.debug("cell[j]:{}", cells[j]);
				if (table.getRowCount() > startRow + i
						&& table.getColumnCount() > startCol + j) {
					Object obj = table.getValueAt(startRow+i, startCol+j);
					Class targetClass = String.class;
					if ( obj != null ) {
						targetClass = obj.getClass();
					}
					String cellValue = cells[j];
					String escapeCellValue = cellValue.replaceAll("\"", "");
					Object value = ObjectUtil.parseStringToObject(escapeCellValue, cellValue, targetClass);
					table.setValueAt(value, startRow + i, startCol + j);
				}
			}
		}
	}

	private void cancelEditing() {
		if (table.getCellEditor() != null) {
			table.getCellEditor().cancelCellEditing();
		}
	}

	private String escape(Object cell) {
		return cell.toString().replace(LINE_BREAK, " ").replace(CELL_BREAK, " ");
	}
}
