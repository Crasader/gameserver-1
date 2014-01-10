package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

public class StatusBar extends JPanel {

	private JLabel statusLabel = new JLabel();
	
	private JProgressBar progressBar = new JProgressBar();
	
	public StatusBar() {
		this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		this.setLayout(new MigLayout("insets 0 10 0 10", "[70%][grow,fill]"));
		this.statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(statusLabel);
//		this.progressBar.setPreferredSize(new Dimension(200, 20));
		this.progressBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		this.add(progressBar);
	}
	
	/**
	 * Utility method to update the status bar.
	 * @param message
	 */
	public final void updateStatus(final String message) {
		if ( message != null && message.length() > 0 ) {
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					statusLabel.setText(message);
				}
			});
		}
	}
	
	/**
	 * Utility method to update the progress bar.
	 * @param message
	 */
	public final void updateProgress(final int percent) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				progressBar.setValue(percent);
			}
		});
	}
	
	public final void progressBarAnimationStart() {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				progressBar.setIndeterminate(true);
			}
		});
	}
	
	public final void progressBarAnimationStop() {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				progressBar.setIndeterminate(false);
			}
		});
	}
}
