package com.xinqihd.sns.gameserver.redis;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.TaskShopDataGeneratorPanel;
import com.xinqihd.sns.gameserver.admin.gui.ext.MyPanel;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class RedisConsole extends MyPanel implements ActionListener {
	
	private static final String DELETE = "delete";
	private static final String FILTER = "filter";
	
	private JPanel contentPane;
	private final JScrollPane scrollPane = new JScrollPane();
	private final JXTreeTable treeTable = new JXTreeTable();
	
	private RedisTreeTableModel treeTableModel = new RedisTreeTableModel();
	private final JButton refreshButton = new JButton("刷新数据");
	private final JPanel panel = new JPanel();
	private final JButton cleanButton = new JButton("清除数据");
	private final JButton settingButton = new JButton("设置");
	private final JXTextField delKeyField = new JXTextField("输入待删除的Key");
	private final JButton delButton = new JButton("删除");
	private final JXTextField filterKeyField = new JXTextField("输入待过滤的Key");
	private final JButton filterButton = new JButton("过滤");
	private Jedis jedis = null;
	private String filterString = null;
	
	private static RedisConsole instance = new RedisConsole();
	
	public static RedisConsole getInstance() {
		return instance;
	}

	/**
	 * Create the frame.
	 */
	public RedisConsole() {
		this.filterButton.addActionListener(this);
		this.delButton.addActionListener(this);
		this.filterButton.setActionCommand(FILTER);
		this.delButton.setActionCommand(DELETE);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new MigLayout("wrap 4"));
		btnPanel.add(delKeyField, "width 20%");
		btnPanel.add(delButton, "sg btn");
		btnPanel.add(filterKeyField, "width 20%");
		btnPanel.add(filterButton, "sg btn");
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		
		contentPane.add(btnPanel, BorderLayout.NORTH);
		
		contentPane.add(this.scrollPane, BorderLayout.CENTER);
		this.treeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getButton() == MouseEvent.BUTTON3 ) { 
					Point point = e.getPoint();
					TreePath treePath = treeTable.getPathForLocation(point.x, point.y);
					final DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode)treePath.getLastPathComponent();
					final String[] userObject = (String[])node.getUserObject();
					JPopupMenu menu = new JPopupMenu("删除:"+userObject[0]);
					JMenuItem menuItem = new JMenuItem("删除:"+userObject[0]);
					menuItem.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								byte[] key = userObject[0].getBytes("ascii");
								jedis.del(key);
								treeTableModel.removeNodeFromParent(node);
								//refreshSetting();
							} catch (UnsupportedEncodingException e1) {
								e1.printStackTrace();
							}
						}
					});
					menu.add(menuItem);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		this.treeTable.setTreeTableModel(this.treeTableModel);
		this.treeTable.setSortable(true);
		this.treeTable.getTableHeader().setReorderingAllowed(true);
		this.treeTable.setSortOrder(0, SortOrder.ASCENDING);
		this.scrollPane.setViewportView(this.treeTable);
		
		contentPane.add(this.panel, BorderLayout.SOUTH);
		this.panel.add(this.refreshButton);
		this.refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshSetting();
			}
		});
		this.cleanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] options = {"确定", "取消",};
				int n = JOptionPane.showOptionDialog(treeTable,
						"确定要删除所有主键吗?",
						"选择'确定'将清空Redis数据库",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]);
				if ( n == 0 ) {
					RedisCleanService service = new RedisCleanService(jedis);
					JDialog dialog = service.getDialog();
					service.execute();
					dialog.setVisible(true);
					refreshSetting();
				}
			}
		});
		
		this.panel.add(this.cleanButton);
		
		this.add(contentPane, "width 100%, height 100%");
//		this.panel.add(this.settingButton);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( FILTER.equals(e.getActionCommand()) ) {
			String key = this.filterKeyField.getText();
			if ( key.length()>0 ) {
				filterString = "*"+key+"*";
				refreshSetting();
			}
		} else if ( DELETE.equals(e.getActionCommand()) ) {
			String key = this.delKeyField.getText();
			if ( key.length()>0 ) {
				Long result = jedis.del(key);
				if ( result != null ) {
					JOptionPane.showMessageDialog(this, "成功删除"+result+"条数据");
				} else {
					JOptionPane.showMessageDialog(this, "未找到指定的key");
				}
			}
		}
	}

	/**
	 * Refresh data
	 * @param host
	 * @param port
	 */
	public void refreshSetting() {
		String host = ConfigManager.getConfigAsString(ConfigKey.gameRedisDBHost);
		int port = StringUtil.toInt(ConfigManager.getConfigAsString(ConfigKey.gameRedisDBPort), 6379);
		//int port = 6380;
		
		//Load redis database content
		RedisRefreshService worker = new RedisRefreshService(this.treeTableModel, host, port);
		worker.setFilterString(filterString);
		JDialog dialog = worker.getDialog();
		worker.execute();
		dialog.setVisible(true);
		this.jedis = worker.getJedis();
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable(){
			public void run() {
//				for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
//					if ( "Nimbus".equals(info.getName()) ) {
//						try {
//							UIManager.setLookAndFeel(info.getClassName());
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						break;
//					}
//				}
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(RedisConsole.getInstance());
				frame.setSize(1030, 800);
				frame.setVisible(true);
			}
		});
	}
}
