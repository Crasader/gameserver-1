package com.xinqihd.sns.gameserver.admin.undo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class MyUndoManager extends UndoManager {
	
	private List<UndoRedoListener> list = new CopyOnWriteArrayList<UndoRedoListener>(); 

	/* (non-Javadoc)
	 * @see javax.swing.undo.UndoManager#addEdit(javax.swing.undo.UndoableEdit)
	 */
	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		boolean result = super.addEdit(anEdit);
		for ( UndoRedoListener listener: list ) {
			listener.undoHappened();
		}
		return result;
	}

	public void addUndoRedoListener(UndoRedoListener listener) {
		this.list.add(listener);
	}

	/* (non-Javadoc)
	 * @see javax.swing.undo.UndoManager#undo()
	 */
	@Override
	public synchronized void undo() throws CannotUndoException {
		super.undo();
		for ( UndoRedoListener listener: list ) {
			listener.redoHappened();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.undo.UndoManager#redo()
	 */
	@Override
	public synchronized void redo() throws CannotRedoException {
		super.redo();
		for ( UndoRedoListener listener: list ) {
			listener.undoHappened();
		}
	}
	
	
}
