package com.xinqihd.sns.gameserver.admin.undo;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CannotUndoException;

import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class UndoAction extends AbstractAction implements UndoRedoListener {
	
	private MyUndoManager undoManager = null;
	
	public UndoAction(MyUndoManager undoManager) {
		super("", ImageUtil.createImageIcon("Button Undo.png", "撤销操作"));
		this.undoManager = undoManager;
		this.undoManager.addUndoRedoListener(this);
		updateUndoStatus();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ( undoManager.canUndo() ) {
			try {
				undoManager.undo();
			} catch (CannotUndoException e1) {
				e1.printStackTrace();
			}
			updateUndoStatus();
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	public void updateUndoStatus() {
		if ( this.undoManager != null && this.undoManager.canUndo() ) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.undo.UndoRedoListener#undoHappened()
	 */
	@Override
	public void undoHappened() {
		updateUndoStatus();
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.undo.UndoRedoListener#redoHappened()
	 */
	@Override
	public void redoHappened() {
		updateUndoStatus();
	}

}
