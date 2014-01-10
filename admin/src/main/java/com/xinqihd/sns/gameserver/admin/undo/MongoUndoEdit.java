package com.xinqihd.sns.gameserver.admin.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.xinqihd.sns.gameserver.admin.model.MyTableModel;

public class MongoUndoEdit extends AbstractUndoableEdit {
	
	private MyTableModel model = null;
	private int row;
	private int column;
	private Object oldValue;
	private Object newValue;
	
	public MongoUndoEdit(MyTableModel model, int row, int column, 
			Object oldValue, Object newValue) {
		this.model = model;
		this.row = row;
		this.column = column;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/* (non-Javadoc)
	 * @see javax.swing.undo.AbstractUndoableEdit#undo()
	 */
	@Override
	public void undo() throws CannotUndoException {
		model.setValueAtWithoutUndo(oldValue, row, column);
	}

	/* (non-Javadoc)
	 * @see javax.swing.undo.AbstractUndoableEdit#redo()
	 */
	@Override
	public void redo() throws CannotRedoException {
		model.setValueAtWithoutUndo(newValue, row, column);
	}

	/* (non-Javadoc)
	 * @see javax.swing.undo.AbstractUndoableEdit#isSignificant()
	 */
	@Override
	public boolean isSignificant() {
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.undo.AbstractUndoableEdit#canUndo()
	 */
	@Override
	public boolean canUndo() {
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.undo.AbstractUndoableEdit#canRedo()
	 */
	@Override
	public boolean canRedo() {
		return true;
	}


}
