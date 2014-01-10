package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.action.RefreshAction;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;

public class MainPanel extends MyPanel {

	private StatusBar statusLabel = new StatusBar();
	
	private Component centerComponent = null;
	
	private static MainPanel instance = null;
	
	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	/**
	 * Create the panel.
	 */
	public MainPanel() {
		init();
		instance = this;
	}
	
	public static MainPanel getInstance() {
		return instance;
	}
	
	public void init() {
		//Setup global KeyInputMap
		this.setLayout(new MigLayout("gap 2, ins 0", "[200px]"));
		
		this.getInputMap().put(KeyStroke.getKeyStroke("F5"), ActionName.REFRESH);
		this.getActionMap().put(ActionName.REFRESH, new RefreshAction());
		
		TaskPanel taskPanel = new TaskPanel();
		
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(false);
		splitPane.setLeftComponent(taskPanel);
		
		this.add(statusLabel, "dock south, width 100%, growx, growy");
//		this.add(splitPane, "dock center, width 100%, height 90%, grow");
		this.add(taskPanel,   "dock west, gap 0, width 200px, height 100%, growy");
	}
	
	public StatusBar getStatusBar() {
		return this.statusLabel;
	}
	
	/**
	 * Set the center component
	 * @param component
	 */
	public void setCenterPanel(Component component) {
		if ( centerComponent != null ) {
			this.remove(centerComponent);
		}
		this.add(component, "dock center, width 90%, height 100%, grow");
//		splitPane.setRightComponent(component);
		centerComponent = component;
		revalidate();
		repaint();
	}
}
