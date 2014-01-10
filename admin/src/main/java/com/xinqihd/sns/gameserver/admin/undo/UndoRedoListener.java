package com.xinqihd.sns.gameserver.admin.undo;

public interface UndoRedoListener {

	public void undoHappened();
	
	public void redoHappened();
	
}
