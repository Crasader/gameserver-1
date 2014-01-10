package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPanel extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(MyPanel.class);
	
	protected MigLayout migLayout = new MigLayout("gap 0 0 0 0");
	
	
	public MyPanel() {
		this.setLayout(migLayout);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

}
