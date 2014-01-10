package com.xinqihd.sns.gameserver.admin.gui.ext;

import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.util.WindowUtils;

import com.xinqihd.sns.gameserver.admin.action.EscapeAction;

public class HtmlDialog extends JDialog {
	
	private JTextPane text = new JTextPane();
	
	public HtmlDialog(String content, int width, int height) {
		super();
		this.setLayout(new MigLayout("gap 5"));
		this.setSize(width, height);
		Point c = WindowUtils.getPointForCentering(this);
		this.setLocation(c);
		this.setModal(true);
		this.setResizable(true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		text.setEditorKit(new HTMLEditorKit());
		text.setText(content);
		JScrollPane pane = new JScrollPane(text);
		this.add(pane, "width 100%, height 100%");
		text.setEditable(false);
		text.setCaretPosition(0);
		text.setDragEnabled(true);
	}
	
	public JTextPane getTextPane() {
		return text;
	}
}
