package com.xinqihd.sns.gameserver.redis;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.util.WindowUtils;

import redis.clients.jedis.Tuple;

import com.xinqihd.sns.gameserver.admin.gui.MainFrame;
import com.xinqihd.sns.gameserver.admin.user.UserManageAction;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class RedisCleanService extends SwingWorker<Void, Integer> {
	
	private RedisTreeTableModel treeTableModel;
	private Jedis jedis = null;
	
	private JDialog dialog = new JDialog();
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("连接Redis数据库");
	private Stage stage = Stage.CONNECT;

	public RedisCleanService(Jedis jedis) {
		this.treeTableModel = treeTableModel;
		this.jedis = jedis;
		
		panel = new JXPanel();
		panel.setLayout(new MigLayout("wrap 1"));
		panel.add(label, "growx, wrap 20");
		panel.add(progressBar, "grow, push");

		dialog = new JDialog();
		dialog.add(panel);
		dialog.setSize(300, 120);
		Point p = WindowUtils.getPointForCentering(dialog);
		dialog.setLocation(p);
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}

	public Jedis getJedis() {
		return this.jedis;
	}
	
	public JDialog getDialog() {
		return this.dialog;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		publish();
		Thread.sleep(100);
		stage = Stage.DELETE;
		
		try {
			Set<byte[]> keys = jedis.keys("*".getBytes());
			if ( keys != null ) {
				DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
				ArrayList<byte[]> keyList = new ArrayList<byte[]>();
				for ( byte[] key : keys ) {
					keyList.add(key);
				}
				progressBar.setMaximum(keyList.size());
				
				int i = 0;
				for ( byte[] keyStr : keyList ) {
					jedis.del(keyStr);
					publish(i++);
				}
			} else {
				JOptionPane.showMessageDialog(null, "无法链接到Redis服务器");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.CONNECT ) {						
			label.setText("正在连接Redis数据库...");
			label.setFont(MainFrame.BIG_FONT);
		} else if ( stage == Stage.DELETE ) {
			label.setFont(MainFrame.BIG_FONT);
			label.setText("正在删除Redis数据库...");
			progressBar.setStringPainted(true);
			if ( chunks != null && chunks.size()>0 ) {
				int percent = chunks.get(chunks.size()-1);
				progressBar.setValue(percent);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		this.dialog.dispose();
	}
	
	static enum Stage {
		CONNECT,
		DELETE,
	}
}
