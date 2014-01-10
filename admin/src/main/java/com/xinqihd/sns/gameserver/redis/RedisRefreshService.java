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

public class RedisRefreshService extends SwingWorker<RedisTreeTableModel, Integer> {
	
	private RedisTreeTableModel treeTableModel;
	private String host;
	private int port;
	private Jedis jedis = null;
	
	private JDialog dialog = new JDialog();
	private JProgressBar progressBar = new JProgressBar();;
	private JXPanel panel = null;
	private JXLabel label = new JXLabel("连接Redis数据库");
	private Stage stage = Stage.CONNECT;
	private String filterString = null;

	
	public RedisRefreshService(RedisTreeTableModel treeTableModel, String host, int port) {
		this.treeTableModel = treeTableModel;
		this.host = host;
		this.port = port;
		
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
	
	/**
	 * @return the filterString
	 */
	public String getFilterString() {
		return filterString;
	}

	/**
	 * @param filterString the filterString to set
	 */
	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	@Override
	protected RedisTreeTableModel doInBackground() throws Exception {
		publish();
		this.jedis = JedisFactory.createJedis(host, port);
		Thread.sleep(100);
		stage = Stage.READ;
		
		try {
			Set<byte[]> keys = null;
			if ( filterString != null ) {
				keys = jedis.keys(filterString.getBytes());
			} else {
				keys = jedis.keys("*".getBytes());
			}
			if ( keys != null ) {
				DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
				ArrayList<String> keyList = new ArrayList<String>();
				HashMap<String, byte[]> keyMap = new HashMap<String, byte[]>();
				for ( byte[] key : keys ) {
					String keyStr = new String(key);
					keyList.add(keyStr);
					keyMap.put(keyStr, key);
				}
				Collections.sort(keyList);
				progressBar.setMaximum(keyList.size());
				
				int i = 0;
				for ( String keyStr : keyList ) {
					byte[] key = keyMap.get(keyStr);
					String[] userObject = new String[5];
					userObject[0] = new String(key, "ascii");
					byte[] content = StringUtil.hexStringToBytes(userObject[0]);
					StringBuilder buf = new StringBuilder();
					for ( byte b : content ) {
						if ( Character.isJavaIdentifierPart(b) ) {
							buf.append((char)b);
						} else {
							buf.append("0x").append(Integer.toHexString(0xff&b));
						}
					}
					userObject[1] = buf.toString();
					userObject[2] = jedis.type(key);
					userObject[3] = "";
					userObject[4] = jedis.ttl(key).toString();
					DefaultMutableTreeTableNode node = new DefaultMutableTreeTableNode(userObject);
					processRedisValue(node, userObject, key);
					root.add(node);
					publish(i++);
				}
				this.treeTableModel.setRoot(root);
			} else {
				JOptionPane.showMessageDialog(null, "无法链接到Redis服务器");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.treeTableModel;
	}

	private void processRedisValue(
			DefaultMutableTreeTableNode node, String[] userObject, byte[] key) {
		if ( "string".equals(userObject[2]) ) {
			userObject[3] = new String(jedis.get(key));
		} else if ( "list".equals(userObject[2]) ) {
			StringBuilder buf = new StringBuilder(100);
			buf.append('[');
			int size = jedis.llen(key).intValue();
			for ( int i=0; i<size; i++ ) {
				buf.append(new String(jedis.lpop(key)));
			}
			buf.append(']');
			userObject[3] = buf.toString();
		} else if ( "set".equals(userObject[2]) ) {
			StringBuilder buf = new StringBuilder(100);
			buf.append('[');
			Set<byte[]> set = jedis.smembers(key);
			if ( set != null ) {
				for ( byte[] setKey : set ) {
					String childValue = new String(new String(setKey));
					buf.append(childValue);
					String[] childObject = new String[4];
					childObject[0] = childValue;
					childObject[1] = "";
					childObject[2] = "set";
					childObject[3] = "";
					DefaultMutableTreeTableNode child = new DefaultMutableTreeTableNode(childObject);
					node.add(child);
				}
			}
			buf.append(']');
			userObject[3] = buf.toString();
		} else if ( "zset".equals(userObject[2]) ) {
			StringBuilder buf = new StringBuilder(100);
			buf.append('[');
			Set<Tuple> set = jedis.zrangeWithScores(key, 0, -1);
			if ( set != null ) {
				int i=1;
				for ( Tuple tuple : set ) {
					buf.append(tuple.getElement());
					String[] childObject = new String[4];
					childObject[0] = "rank:"+(i++);
					childObject[1] = tuple.getElement();
					childObject[2] = "zset";
					childObject[3] = ""+tuple.getScore();
					DefaultMutableTreeTableNode child = new DefaultMutableTreeTableNode(childObject);
					node.add(child);
				}
			}
			buf.append(']');
		} else if ( "hash".equals(userObject[2]) ) {
			StringBuilder buf = new StringBuilder(100);
			buf.append('[');
			Map<byte[], byte[]> map = jedis.hgetAll(key);
			if ( map != null ) {
				for ( byte[] setKey : map.keySet() ) {
					String childKey = new String(setKey);
					String childValue = new String(jedis.hget(key, setKey));
					buf.append(childKey).append('=').append(childValue);
					String[] childObject = new String[4];
					childObject[0] = childKey;
					childObject[1] = childValue;
					childObject[2] = "set";
					childObject[3] = "";
					DefaultMutableTreeTableNode child = new DefaultMutableTreeTableNode(childObject);
					node.add(child);
				}
			}
			buf.append(']');
			userObject[3] = buf.toString();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<Integer> chunks) {
		if ( stage == Stage.CONNECT ) {						
			label.setText("正在连接Redis数据库...");
			label.setFont(MainFrame.BIG_FONT);
		} else if ( stage == Stage.READ ) {
			label.setFont(MainFrame.BIG_FONT);
			label.setText("正在读取Redis数据库...");
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
		READ,
	}
}
