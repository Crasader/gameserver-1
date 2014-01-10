package com.xinqihd.sns.gameserver.admin.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class EscapeAction extends AbstractAction {

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent source = (JComponent)e.getSource();  
    Window window = SwingUtilities.getWindowAncestor(source);  
    window.dispose();  
    //Dialog dialog = (Dialog)source.getFocusCycleRootAncestor();  
    //dialog.dispose();  
    //System.out.println("source = " + source.getClass().getName() + "\n" +  
    //                   "source.focusCycleRootAncestor = " +  
    //           source.getFocusCycleRootAncestor().getClass().getName());  
	}

}
