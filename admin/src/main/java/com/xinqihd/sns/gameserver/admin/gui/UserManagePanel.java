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
import com.xinqihd.sns.gameserver.admin.model.DBObjectTreeTableNode;
import com.xinqihd.sns.gameserver.admin.user.AccountDeleteAction;
import com.xinqihd.sns.gameserver.admin.user.AccountSearchService;
import com.xinqihd.sns.gameserver.admin.user.AccountTreeTableModel;
import com.xinqihd.sns.gameserver.admin.user.ChargeAction;
import com.xinqihd.sns.gameserver.admin.user.GuildMemberDeleteAction;
import com.xinqihd.sns.gameserver.admin.user.GuildMemberSearchService;
import com.xinqihd.sns.gameserver.admin.user.GuildMemberTreeTableModel;
import com.xinqihd.sns.gameserver.admin.user.ManageAccountStatusAction;
import com.xinqihd.sns.gameserver.admin.user.ManageBagAction;
import com.xinqihd.sns.gameserver.admin.user.PushAction;
import com.xinqihd.sns.gameserver.admin.user.SaveAccountAction;
import com.xinqihd.sns.gameserver.admin.user.SaveGuildMemberAction;
import com.xinqihd.sns.gameserver.admin.user.SaveUserAction;
import com.xinqihd.sns.gameserver.admin.user.SendGiftAction;
import com.xinqihd.sns.gameserver.admin.user.SendMultiGiftAction;
import com.xinqihd.sns.gameserver.admin.user.UserDelAction;
import com.xinqihd.sns.gameserver.admin.user.UserSearchService;
import com.xinqihd.sns.gameserver.admin.user.UserTreeRenderer;
import com.xinqihd.sns.gameserver.admin.user.UserTreeTableModel;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoUserManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.GuildMember;

/**
 * User management panel
 * @author wangqi
 *
 */
public class UserManagePanel extends MyPanel 
	implements ActionListener, TreeModelListener {
	
	private static UserManagePanel instance = new UserManagePanel();
	private UserTreeTableModel model = null;
	private AccountTreeTableModel accountModel = null;
	private GuildMemberTreeTableModel guildMemberModel = null;
	private ManageBagAction bagAction = new ManageBagAction(model, this);
	private UserDelAction   delAction = new UserDelAction(model, this);
	private SendGiftAction   sendGiftAction = new SendGiftAction(model, this);
	private SendMultiGiftAction  sendMultiGiftAction = new SendMultiGiftAction(model, this);
	private SaveGuildMemberAction saveGuildMemberAction = null;
	private GuildMemberDeleteAction deleteGuildMemberAction = null;
	private ManageAccountStatusAction loginStatusAction = new ManageAccountStatusAction(model, this);
	private ChargeAction chargeAction = new ChargeAction(this);
	private PushAction pushAction = new PushAction(this);
	
	private JXLabel searchLabel = new JXLabel("昵称");
	private JXTextField inputField = new JXTextField("用户名或ID");
	private JXButton saveButton = new JXButton();
	private JXButton saveAccountButton = new JXButton();
	private JXButton bagButton = new JXButton();
	private JXButton delButton = new JXButton();
	private JXButton giftButton = new JXButton();
	private JXButton loginButton = new JXButton();
	private JXButton chargeButton = new JXButton();
	private JXButton pushButton = new JXButton();
	private JXButton sendMultiButton = new JXButton();
	private JXButton deleteAccountButton = new JXButton();
	private JXButton saveGuildMemberButton = new JXButton();
	private JXButton deleteGuildMemberButton = new JXButton();
	private JCheckBox matchBox = new JCheckBox();
	private JXComboBox serverBox = new JXComboBox(new String[]{
			"192.168.0.77:3443", "game1.babywar:3443", "game2.babywar:3443", "game3.babywar:3443"});
	private MyTreeTable userTable = new MyTreeTable();
	private MyTreeTable accountTable = new MyTreeTable();
	private MyTreeTable guildMemberTable = new MyTreeTable();

	public UserManagePanel() {
		init();
	}
	
	public static UserManagePanel getInstance() {
		return instance;
	}
	
	public void init() {
		model = new UserTreeTableModel();
		List<String> columnNames = new ArrayList<String>();
		columnNames.add("关键字");
		columnNames.add("值");
		columnNames.add("类型");
		model.setColumnIdentifiers(columnNames);
		model.addTreeModelListener(this);
		
		accountModel = new AccountTreeTableModel();
		accountModel.setColumnIdentifiers(columnNames);
		accountModel.addTreeModelListener(this);

		guildMemberModel = new GuildMemberTreeTableModel();
		guildMemberModel.setColumnIdentifiers(columnNames);
		guildMemberModel.addTreeModelListener(this);
		
		saveGuildMemberAction = new SaveGuildMemberAction(guildMemberModel, this);
		deleteGuildMemberAction = new GuildMemberDeleteAction(guildMemberModel, this);
		
		saveButton.setAction(new SaveUserAction(model, this));
		saveAccountButton.setAction(new SaveAccountAction(accountModel, this));
		saveGuildMemberButton.setAction(saveGuildMemberAction);
		
		bagAction.setEnabled(false);
		bagButton.setAction(bagAction);
		delAction.setEnabled(false);
		delAction.setModel(model);
		delButton.setAction(delAction);
		sendGiftAction.setEnabled(false);
		giftButton.setAction(sendGiftAction);
		loginStatusAction.setEnabled(false);
		loginButton.setAction(loginStatusAction);
		chargeAction.setEnabled(false);
		chargeButton.setAction(chargeAction);
		pushAction.setEnabled(false);
		pushButton.setAction(pushAction);
		sendMultiButton.setEnabled(true);
		sendMultiButton.setAction(sendMultiGiftAction);
		deleteAccountButton.setEnabled(true);
		deleteAccountButton.setAction(new AccountDeleteAction(accountModel, this));
		deleteGuildMemberButton.setEnabled(true);
		deleteGuildMemberButton.setAction(deleteGuildMemberAction);

		userTable.setTreeTableModel(model);
		userTable.setHighlighters(HighlighterFactory.createAlternateStriping());
		UserTreeRenderer render = new UserTreeRenderer();
		userTable.setTreeCellRenderer(render);
		userTable.setEditable(true);
		userTable.setColumnControlVisible(true);
		userTable.packAll();
		userTable.addTreeSelectionListener(new UserSelectionListener());
		JScrollPane pane = new JScrollPane(userTable);
		
		accountTable.setTreeTableModel(accountModel);
		accountTable.setHighlighters(HighlighterFactory.createAlternateStriping());
		accountTable.setTreeCellRenderer(new UserTreeRenderer());
		accountTable.setEditable(true);
		accountTable.setColumnControlVisible(true);
		accountTable.packAll();
		accountTable.addTreeSelectionListener(new AccountSelectionListener());
		JScrollPane accountPane = new JScrollPane(accountTable);
		
		guildMemberTable.setTreeTableModel(guildMemberModel);
		guildMemberTable.setHighlighters(HighlighterFactory.createAlternateStriping());
		guildMemberTable.setTreeCellRenderer(new UserTreeRenderer());
		guildMemberTable.setEditable(true);
		guildMemberTable.setColumnControlVisible(true);
		guildMemberTable.packAll();
		guildMemberTable.addTreeSelectionListener(new GuildMemberSelectionListener());
		JScrollPane guildMemberPane = new JScrollPane(guildMemberTable);
		
//		TableColumnExt columnEx = myTable.getColumnExt("值");
//		columnEx.setCellEditor(new DefaultCellEditor(new JComboBox()));
		

		inputField.setColumns(20);
		inputField.setActionCommand(ActionName.OK.name());
		inputField.addActionListener(this);

		this.setLayout(new MigLayout("wrap 2"));
		this.add(searchLabel, "span, split 4");
		this.add(inputField, "");
		this.add(matchBox, "");
		this.add(serverBox, "");
		this.add(saveButton, "newline, split 11");
		this.add(saveAccountButton, "");
		this.add(saveGuildMemberButton, "");
		this.add(bagButton,  "");
		this.add(giftButton,  "");
		this.add(delButton,  "");
		this.add(loginButton, "");
		this.add(chargeButton, "");
		this.add(pushButton, "");
		this.add(deleteAccountButton, "");
		this.add(deleteGuildMemberButton, "");
		this.add(pane, "newline, width 100%, height 60%");
		this.add(accountPane, "newline, split 2, width 50%, height 40%");
		this.add(guildMemberPane, "width 50%, height 40%");

		updateButtonStatus();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand() == ActionName.OK.name() ) {
			UserSearchService service = new UserSearchService(model, inputField.getText(), matchBox.isSelected());
			service.execute();
			AccountSearchService accountServer = new AccountSearchService(
					accountModel, inputField.getText(), matchBox.isSelected());
			accountServer.execute();
			GuildMemberSearchService guildMemberService = new GuildMemberSearchService(
					guildMemberModel, inputField.getText(), matchBox.isSelected());
			guildMemberService.execute();
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
		if ( accountModel.isDataChanged() ) {
			saveAccountButton.setEnabled(true);
		} else {
			saveAccountButton.setEnabled(false);
		}
		if ( guildMemberModel.isDataChanged() ) {
			saveGuildMemberButton.setEnabled(true);
		} else {
			saveGuildMemberButton.setEnabled(false);
		}
		if ( this.userTable.getSelectedRow() != -1 ) {
			delAction.setEnabled(true);
			bagAction.setEnabled(true);
			sendGiftAction.setEnabled(true);
			loginStatusAction.setEnabled(true);
			chargeAction.setEnabled(true);
			pushAction.setEnabled(true);
		} else {
			delAction.setEnabled(false);
			bagAction.setEnabled(false);
			sendGiftAction.setEnabled(false);
			
			chargeAction.setEnabled(false);
			pushAction.setEnabled(false);
		}
		if ( this.accountTable.getSelectedRow() != -1 ) {
			loginStatusAction.setEnabled(true);
		} else {
			loginStatusAction.setEnabled(false);
		}
		if ( this.accountTable.getSelectedRow() != -1 ) {
			deleteAccountButton.setEnabled(true);
		} else {
			deleteAccountButton.setEnabled(false);
		}
		if ( this.guildMemberTable.getSelectedRow() != -1 ) {
			deleteGuildMemberButton.setEnabled(true);
		} else {
			deleteGuildMemberButton.setEnabled(false);
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
	
	class UserSelectionListener implements TreeSelectionListener {

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
						bagAction.setUser(user);
						bagAction.setEnabled(true);
						delAction.setUserId(userId);
						delAction.setEnabled(true);
						sendGiftAction.setUserId(userId);
						sendGiftAction.setEnabled(true);
						loginStatusAction.setEnabled(false);
						deleteAccountButton.setEnabled(false);
						chargeAction.setUserId(userId);
						chargeAction.setEnabled(true);
						pushAction.setUser(user);
						pushAction.setEnabled(true);
						return;
					}
				}
			} else {
				bagAction.setEnabled(false);
				delAction.setEnabled(false);
				sendGiftAction.setEnabled(false);
				loginStatusAction.setEnabled(false);
			}
		}
		
	}
	
	class AccountSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getPath();
			if ( path.getPathCount() > 1 ) {
				DBObjectTreeTableNode node = (DBObjectTreeTableNode)path.getPath()[1];
				Object nodeKey = node.getKey();
				if ( nodeKey instanceof String ) {
					node = (DBObjectTreeTableNode)path.getPath()[1];
					String accountIdStr = node.getKey().toString();
					Account account = (Account)MongoUtil.constructObject(
							(DBObject)node.getUserObject());
					loginStatusAction.setAccount(account);
					loginStatusAction.setEnabled(true);
					deleteAccountButton.setEnabled(true);
				}
			} else {
				loginStatusAction.setEnabled(false);
				deleteAccountButton.setEnabled(false);
			}
		}
		
	}
	
	class GuildMemberSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath path = e.getPath();
			if ( path.getPathCount() > 1 ) {
				DBObjectTreeTableNode node = (DBObjectTreeTableNode)path.getPath()[1];
				Object nodeKey = node.getKey();
				if ( nodeKey instanceof String ) {
					node = (DBObjectTreeTableNode)path.getPath()[1];
					String accountIdStr = node.getKey().toString();
					GuildMember account = (GuildMember)MongoUtil.constructObject(
							(DBObject)node.getUserObject());
					saveGuildMemberAction.setEnabled(true);
					deleteGuildMemberAction.setEnabled(true);
				}
			} else {
				saveGuildMemberAction.setEnabled(false);
				deleteGuildMemberAction.setEnabled(false);
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
				frame.add(UserManagePanel.getInstance());
				frame.setSize(1000, 700);
				frame.setVisible(true);
			}
		});
	}
}
