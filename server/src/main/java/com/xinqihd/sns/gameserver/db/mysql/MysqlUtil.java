package com.xinqihd.sns.gameserver.db.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class MysqlUtil {
	
	private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	
	private static final Logger logger = LoggerFactory.getLogger(MysqlUtil.class);
	
	private static HashMap<String, BoneCPDataSource> dataSourceMap = new HashMap<String, BoneCPDataSource>();
	
	private static HashMap<String, Boolean> dataSourceReadyMap = new HashMap<String, Boolean>();


	/**
	 * Initialize the connection pool.
	 */
	public static final void init(String database, String username, String password, 
			String server, int maxConn, int minConn, String jndi) {
		String dbUrl = StringUtil.concat("jdbc:mysql://", server, ":3306/", database, 
				"?connectTimeout=", 10000, "&useUnicode=true&characterEncoding=utf8");
		
		logger.info("Try to connect to Mysql with url: {}", dbUrl);
		
	  // create a new configuration object
	 	BoneCPConfig config = new BoneCPConfig();
	  // set the JDBC url
	 	config.setJdbcUrl(dbUrl);
	 	config.setPartitionCount(2);
	 	config.setMaxConnectionsPerPartition(maxConn);
	 	config.setMinConnectionsPerPartition(minConn);
	 	config.setUsername(username);
	 	config.setPassword(password);
		
	  // setup the connection pool
		try {
			BoneCPDataSource dataSource = new BoneCPDataSource(config);
			Context jndiContext = GameContext.getInstance().getJndiContext();
			jndiContext.bind(jndi, dataSource);
			dataSourceMap.put(jndi, dataSource);
			dataSourceReadyMap.put(jndi, Boolean.TRUE);
		} catch (Exception e) {
			logger.warn("The connection to Mysql database is unavailable. Exception:{}", e.getMessage());
			dataSourceReadyMap.put(jndi, Boolean.FALSE);
		}
	}
	
	/**
	 * Check to see if the Mysql connection is ready for use.
	 * @return
	 */
	public static final boolean isMysqlReady(String jndi) {
		Boolean value = dataSourceReadyMap.get(jndi);
		if ( value == null ) {
			return false;
		} else {
			return value.booleanValue();
		}
	}
	
	/**
	 * Try to execute the updating sql and return result.
	 * @param sqlUpdate
	 * @return
	 */
	public static final boolean executeUpdate(String sqlUpdate, String jndi) {
		boolean success = false;
		if ( !isMysqlReady(jndi) ) {
			return success;
		}
		Connection conn = null;
		Statement stat = null;
		try {
			BoneCPDataSource dataSource = dataSourceMap.get(jndi);
			conn = dataSource.getConnection();
			stat = conn.createStatement();
			stat.executeUpdate(sqlUpdate);
			success = true;
		} catch (SQLException e) {
			success = false;
			logger.warn("Failed to execute sql: {}. Exception {}", sqlUpdate, e.getMessage());
		} finally {
			if ( stat != null ) {
				try {
					stat.close();
				} catch (SQLException e) {
				}
			}
			if ( conn != null ) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return success;
	}
	
	/**
	 * The query result will be read into heap memory so do not 
	 * query a big table.
	 * 
	 * @param sqlQuery
	 * @return
	 */
	public static final String executeQueryFirstRow(String sqlQuery, String columnName, String jndi) {
		if ( !isMysqlReady(jndi) ) {
			return null;
		}
		Connection conn = null;
		Statement stat = null;
		ResultSet resultSet = null;
		try {
			BoneCPDataSource dataSource = dataSourceMap.get(jndi);
			conn = dataSource.getConnection();
			stat = conn.createStatement();
			resultSet = stat.executeQuery(sqlQuery);
			if ( resultSet.next() ) {
				return resultSet.getString(columnName);
			}
		} catch (SQLException e) {
			logger.warn("Failed to execute sql: {}. Exception {}", sqlQuery, 
					e.getMessage());
		} finally {
			if ( stat != null ) {
				try {
					stat.close();
				} catch (SQLException e) {
				}
			}
			if ( conn != null ) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}
	
	/**
	 * The query result will be read into heap memory so do not 
	 * query a big table.
	 * 
	 * @param sqlQuery
	 * @return
	 */
	public static final Map<String, String> executeQueryFirstRow(String sqlQuery, String jndi) {
		if ( !isMysqlReady(jndi) ) {
			return null;
		}
		Connection conn = null;
		Statement stat = null;
		ResultSet resultSet = null;
		try {
			BoneCPDataSource dataSource = dataSourceMap.get(jndi);
			conn = dataSource.getConnection();
			stat = conn.createStatement();
			resultSet = stat.executeQuery(sqlQuery);
			HashMap<String, String> map = new HashMap<String, String>(); 
			if ( resultSet.next() ) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int count = rsmd.getColumnCount();
				for ( int i=1; i<=count; i++ ) {
					map.put(rsmd.getColumnName(i), resultSet.getString(i));
				}
				return map;
			}
		} catch (SQLException e) {
			logger.warn("Failed to execute sql: {}. Exception {}", sqlQuery, 
					e.getMessage());
		} finally {
			if ( stat != null ) {
				try {
					stat.close();
				} catch (SQLException e) {
				}
			}
			if ( conn != null ) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}
	
	/**
	 * The query result will be read into heap memory so do not 
	 * query a big table.
	 * 
	 * @param sqlQuery
	 * @return
	 */
	public static final ArrayList<Map<String, String>> executeQueryAllRows(String sqlQuery, String jndi) {
		if ( !isMysqlReady(jndi) ) {
			return null;
		}
		Connection conn = null;
		Statement stat = null;
		ResultSet resultSet = null;
		try {
			BoneCPDataSource dataSource = dataSourceMap.get(jndi);
			conn = dataSource.getConnection();
			stat = conn.createStatement();
			resultSet = stat.executeQuery(sqlQuery);
			ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>(); 
			while ( resultSet.next() ) {
				HashMap<String, String> map = new HashMap<String, String>();
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int count = rsmd.getColumnCount();
				for ( int i=1; i<=count; i++ ) {
					map.put(rsmd.getColumnName(i), resultSet.getString(i));
				}
				rows.add(map);
			}
			return rows;
		} catch (SQLException e) {
			logger.warn("Failed to execute sql: {}. Exception {}", sqlQuery, 
					e.getMessage());
		} finally {
			if ( stat != null ) {
				try {
					stat.close();
				} catch (SQLException e) {
				}
			}
			if ( conn != null ) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}
		
	/**
	 * Destroy and shutdown the connection pool.
	 */
	public static void destroy() {
		for ( BoneCPDataSource dataSource : dataSourceMap.values() ) {
			if ( dataSource != null ) {
				dataSource.close();
				dataSource = null;
				logger.info("The connection to Mysql database is destroyed.");
			}			
		}
	}
	
}
