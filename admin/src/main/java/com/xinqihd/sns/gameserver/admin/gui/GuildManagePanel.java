package com.xinqihd.sns.gameserver.admin.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.admin.action.ActionName;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyTreeTable;
import com.xinqihd.sns.gameserver.admin.guild.GuildBagDeleteAction;
import com.xinqihd.sns.gameserver.admin.guild.GuildBagSaveAction;
import com.xinqihd.sns.gameserver.admin.guild.GuildBagSearchService;
import com.xinqihd.sns.gameserver.admin.guild.GuildBagTreeTableModel;
import com.xinqihd.sns.gameserver.admin.guild.GuildDeleteAction;
import com.xinqihd.sns.gameserver.admin.guild.GuildSaveAction;
import com.xinqihd.sns.gameserver.admin.guild.GuildSearchService;
import com.xinqihd.sns.gameserver.admin.guild.GuildTreeTableModel;
import com.xinqihd.sns.gameserver.admin.model.DBObjectTreeTableNode;
import com.xinqihd.sns.gameserver.admin.user.UserTreeRenderer;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoUserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;

/**
 * User management panel
 * @author wangqi
 *
 */
public class GuildManagePanel extends MyPanel 
	implements ActionListener, TreeModelListener {
	
	private static GuildManagePanel instance = new GuildManagePanel();
	
	private GuildTreeTableModel model = null;
	private GuildBagTreeTableModel guildBagModel = null;

	private GuildDeleteAction delGuildAction = null;
	private GuildSaveAction saveGuildAction = null;
	private GuildBagDeleteAction delGuildBagAction = null;
	private GuildBagSaveAction saveGuildBagAction = null;
	
	private JXLabel searchLabel = new JXLabel("公会名");
	private JXTextField inputField = new JXTextField("公会名");
	private JXButton saveButton = new JXButton();
	private JXButton delButton = new JXButton();
	private JXButton saveGuildBagButton = new JXButton();
	private JXButton delGuildBagButton = new JXButton();
	private JCheckBox matchBox = new JCheckBox();
	private JXComboBox serverBox = new JXComboBox(new String[]{
			"192.168.0.77:3443", "game1.babywar:3443", "game2.babywar:3443", "game3.babywar:3443"});
	private MyTreeTable guildTable = new MyTreeTable();
	private MyTreeTable guildBagTable = new MyTreeTable();

	public GuildManagePanel() {
		init();
	}
	
	public static GuildManagePanel getInstance() {
		return instance;
	}
	
	public void init() {
		model = new GuildTreeTableModel();
		List<String> columnNames = new ArrayList<String>();
		columnNames.add("关键字");
		columnNames.add("值");
		columnNames.add("类型");
		model.setColumnIdentifiers(columnNames);
		model.addTreeModelListener(this);

		guildBagModel = new GuildBagTreeTableModel();
		guildBagModel.setColumnIdentifiers(columnNames);
		guildBagModel.addTreeModelListener(this);
		
		saveGuildAction = new GuildSaveAction(model, this);
		saveButton.setAction(saveGuildAction);
		delGuildAction = new GuildDeleteAction(model, this);
		delButton.setAction(delGuildAction);
		
		saveGuildBagAction = new GuildBagSaveAction(model, this);
		saveGuildBagButton.setAction(saveGuildAction);
		delGuildBagAction = new GuildBagDeleteAction(model, this);
		delGuildBagButton.setAction(delGuildAction);
		
		delGuildAction.setEnabled(false);
		delButton.setAction(delGuildAction);

		guildTable.setTreeTableModel(model);
		guildTable.setHighlighters(HighlighterFactory.createAlternateStriping());
		UserTreeRenderer render = new UserTreeRenderer();
		guildTable.setTreeCellRenderer(render);
		guildTable.setEditable(true);
		guildTable.setColumnControlVisible(true);
		guildTable.packAll();
		guildTable.addTreeSelectionListener(new GuildSelectionListener());
		JScrollPane pane = new JScrollPane(guildTable);
		
		guildBagTable.setTreeTableModel(guildBagModel);
		guildBagTable.setHighlighters(HighlighterFactory.createAlternateStriping());
		guildBagTable.setTreeCellRenderer(new UserTreeRenderer());
		guildBagTable.setEditable(true);
		guildBagTable.setColumnControlVisible(true);
		guildBagTable.packAll();
		guildBagTable.addTreeSelectionListener(new GuildSelectionListener());
		JScrollPane guildBagPane = new JScrollPane(guildBagTable);
		
//		TableColumnExt columnEx = myTable.getColumnExt("值");
//		columnEx.setCellEditor(new DefaultCellEditor(new JComboBox()));
		

		inputField.setColumns(20);
		inputField.setActionCommand(ActionName.OK.name());
		inputField.addActionListener(this);

		this.setLayout(new MigLayout("wrap 2"));
		this.add(searchLabel, "span, split 6");
		this.add(inputField, "");
		this.add(matchBox, "");
		this.add(serverBox, "");
		this.add(saveButton, "");
		this.add(delButton,  "");
		this.add(pane, "newline, width 100%, height 60%");
		this.add(guildBagPane, "newline, width 100%, height 40%");

		updateButtonStatus();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand() == ActionName.OK.name() ) {
			GuildSearchService service = new GuildSearchService(
					model, inputField.getText(), matchBox.isSelected());
			service.execute();
			GuildBagSearchService bagService = new GuildBagSearchService(
					guildBagModel, inputField.getText(), matchBox.isSelected());
			bagService.execute();
		}
	}

	/**
	 * Update the button status
	 */
	public void updateButtonStatus() {
		if ( model.isDataChanged() ) {
			saveButton.setEnabled(true);
		} else {
			saveButton.setEnabled(false);
		}
		if ( this.guildTable.getSelectedRow() != -1 ) {
			delGuildAction.setEnabled(true);
		} else {
			delGuildAction.setEnabled(false);
		}
		if ( guildBagModel.isDataChanged() ) {
			saveGuildBagButton.setEnabled(true);
		} else {
			saveGuildBagButton.setEnabled(false);
		}
		if ( this.guildBagTable.getSelectedRow() != -1 ) {
			delGuildBagAction.setEnabled(true);
		} else {
			delGuildBagAction.setEnabled(false);
		}
	}
	
	// ---------------------------------------------- TreeNodeListener

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		updateButtonStatus();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		updateButtonStatus();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		updateButtonStatus();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		updateButtonStatus();
	}
	
	
	public String getGameServerId() {
		return this.serverBox.getSelectedItem().toString();
	}
	
	// --------------------------------------------- TreeSelectionListener
	
	class GuildSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getPath();
			if ( path.getPathCount() > 1 ) {
				DBObjectTreeTableNode node = (DBObjectTreeTableNode)path.getPath()[1];
				Object nodeKey = node.getKey();
				if ( nodeKey instanceof UserId ) {
					UserId userId = (UserId)node.getKey();
					User user = ((MongoUserManager)(UserManager.getInstance())).
							constructUserObject((DBObject)node.getUserObject());
					if ( userId != null ) {
						delGuildAction.setEnabled(true);
						return;
					}
				}
			} else {
				delGuildAction.setEnabled(false);
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		//MongoUtil.initMongo("mongos.babywar.xinqihd.com", 3443);		
		EventQueue.invokeLater(new Runnable(){
			public void run() {
				/*
				for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
					if ( "Nimbus".equals(info.getName()) ) {
						try {
							UIManager.setLookAndFeel(info.getClassName());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				}
				*/
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(GuildManagePanel.getInstance());
				frame.setSize(1000, 700);
				frame.setVisible(true);
			}
		});
	}
}
