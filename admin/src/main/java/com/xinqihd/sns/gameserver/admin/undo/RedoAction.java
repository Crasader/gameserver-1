package com.xinqihd.sns.gameserver.admin.undo;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CannotRedoException;

import com.xinqihd.sns.gameserver.admin.util.ImageUtil;

public class RedoAction extends AbstractAction implements UndoRedoListener {
	
	private MyUndoManager undoManager = null;
	
	public RedoAction(MyUndoManager undoManager) {
		super("", ImageUtil.createImageIcon("Button Redo.png", "重新操作"));
		this.undoManager = undoManager;
		this.undoManager.addUndoRedoListener(this);
		updateRedoStatus();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ( undoManager.canRedo() ) {
			try {
				undoManager.redo();
			} catch (CannotRedoException e1) {
				e1.printStackTrace();
			}
			updateRedoStatus();
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	public void updateRedoStatus() {
		if ( this.undoManager != null && this.undoManager.canRedo() ) {
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
		updateRedoStatus();
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.admin.undo.UndoRedoListener#redoHappened()
	 */
	@Override
	public void redoHappened() {
		updateRedoStatus();
	}
}
