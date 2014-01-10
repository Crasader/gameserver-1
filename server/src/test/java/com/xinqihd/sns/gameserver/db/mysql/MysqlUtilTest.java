package com.xinqihd.sns.gameserver.db.mysql;

import static org.junit.Assert.*;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.JndiContextKey;

public class MysqlUtilTest {

	@Before
	public void setUp() throws Exception {
		GameContext.getInstance().initContext();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConnectToMysql() {
		boolean success = MysqlUtil.isMysqlReady(JndiContextKey.mysql_billing_db.name());
		assertEquals(true, success);
	}

	@Test
	public void testQueryToMysql() throws Exception {
		boolean success = MysqlUtil.isMysqlReady(JndiContextKey.mysql_billing_db.name());
		assertEquals(true, success);
		Context context = new InitialContext();
		DataSource datasource = (DataSource)context.lookup(JndiContextKey.mysql_billing_db.name());
		System.out.println(datasource);
		Map rowSet = MysqlUtil.executeQueryFirstRow("select 1", JndiContextKey.mysql_billing_db.name());
		assertEquals(true, rowSet.size()>0);
		Object value = rowSet.get("1");
		assertEquals("1", value);
	}
}
