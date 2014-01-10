package com.xinqihd.sns.gameserver.admin;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;

public class GameAdmin {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
						if ( "Nimbus".equals(info.getName()) ) {
//							UIManager.put("nimbusBase", Color.RED);
//							UIManager.put("nimbusBlueGrey", Color.GREEN);
//							UIManager.put("control", Color.BLUE);

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
	}

}
