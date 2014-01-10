package com.xinqihd.sns.gameserver.admin;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;

public class GameAdminApplet extends JApplet {

	/**
	 * Create the applet.
	 */
	public GameAdminApplet() {

	}

  public void init() {
      //Execute a job on the event-dispatching thread; creating this applet's GUI.
      try {
          SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
        				try {
        					for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
        						if ( "Nimbus".equals(info.getName()) ) {
//        							UIManager.put("nimbusBase", Color.RED);
//        							UIManager.put("nimbusBlueGrey", Color.GREEN);
//        							UIManager.put("control", Color.BLUE);

        							UIManager.setLookAndFeel(info.getClassName());
        							break;
        						}
        					}
        					
        					MainFrame frame = new MainFrame();
        				} catch (Exception e) {
        					e.printStackTrace();
        				}
              }
          });
      } catch (Exception e) {
          System.err.println("createGUI didn't complete successfully");
      }
  }
}
