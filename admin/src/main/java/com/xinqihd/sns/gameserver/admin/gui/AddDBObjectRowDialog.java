package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.util.WindowUtils;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyDialog;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTable;
import com.xinqihd.sns.gameserver.admin.i18n.ColumnNames;
import com.xinqihd.sns.gameserver.admin.model.MyTableModel;
import com.xinqihd.sns.gameserver.admin.util.ObjectUtil;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

public class AddDBObjectRowDialog extends MyDialog implements ActionListener {
	
	private DBObject prototype = null;
	private MyTable table = null;
	private JXPanel panel = new JXPanel();
	private JXLabel[] labels = null;
	private JXTextField[] fields = null; 
	private JXButton okButton = new JXButton("确定");
	private JXButton cancelButton = new JXButton("取消");
	private HashSet<String> hiddenFields = new HashSet<String>();
	
	public AddDBObjectRowDialog(MyTable table) {
		this.table = table;
	}
	
	/**
	 * Set the prototype object to copy all hidden fields with.
	 * 
	 * @param prototype
	 */
	public void setPrototype(DBObject prototype) {
		this.prototype = prototype;
		
		this.okButton.setActionCommand(ActionName.OK.toString());
		this.okButton.addActionListener(this);
		this.cancelButton.setActionCommand(ActionName.CANCEL.toString());
		this.cancelButton.addActionListener(this);
		
		Set<String> keys = prototype.keySet();
		int columnCount = keys.size();
		
		if ( columnCount > 0 ) { 
			labels = new JXLabel[columnCount];
			fields = new JXTextField[columnCount];
		}
		
		panel.setLayout(new MigLayout("wrap 4", "[15%][35%,grow][15%][35%,grow]"));
		Iterator<String> columnKeyIter = keys.iterator();
		TableModel model = table.getModel();
		
		if ( model instanceof MyTableModel ) {
			hiddenFields.addAll( ((MyTableModel)model).getHiddenFields() );
		}
		for ( int i=0; i<columnCount; i++ ) {
			String columnName = columnKeyIter.next();
			String localName  = ColumnNames.translate(columnName);
			String columnValue = String.valueOf(prototype.get(columnName));
			labels[i] = new JXLabel(localName);
			fields[i] = new JXTextField(columnValue);
			fields[i].setColumns(20);
//			fields[i].setColumns(15);
			fields[i].setText(columnValue);
			if ( hiddenFields.contains(columnName) ) {
				fields[i].setEnabled(false);
			}
			
			panel.add(labels[i], "sizegroup label");
			panel.add(fields[i], "sizegroup field");
		}
		panel.add(okButton, "newline, span, split 2, align center");
		panel.add(cancelButton, "");
		
		this.add(panel);
		this.setSize(600, (columnCount/4+1)*70+70);
		Point p = WindowUtils.getPointForCentering(this);
		this.setLocation(p);
		this.setModal(true);
		this.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( ActionName.OK.toString().equals(e.getActionCommand()) ) {
			//Construct new object and insert here.
			try {
				DBObject newDBObj = MongoUtil.createDBObject();
				Set<String> keys = prototype.keySet();
				int columnCount = keys.size();
				Iterator<String> columnKeyIter = keys.iterator();
				for ( int i=0; i<columnCount; i++ ) {
					String columnName = columnKeyIter.next();
					String columnValue = fields[i].getText();
					Object prototypeObj = prototype.get(columnName);
					Object value = ObjectUtil.parseStringToObject(columnValue, columnValue, prototypeObj.getClass());
					newDBObj.put(columnName, value);
				}
				TableModel model = table.getModel();
				((MyTableModel)model).insertRow(newDBObj);
				this.dispose();
			} catch (Exception e1) {
				String message = e1.getMessage();
				JOptionPane.showMessageDialog(this, "插入失败:"+message, "插入行失败", JOptionPane.ERROR_MESSAGE);
			}
		} else if ( ActionName.CANCEL.toString().equals(e.getActionCommand()) ) {
			this.dispose();
		}
	}

}
