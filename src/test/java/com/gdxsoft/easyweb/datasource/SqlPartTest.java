package com.gdxsoft.easyweb.datasource;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SqlPartTest {

	@Test
	public void testNormalSql() {
		SqlPart part = new SqlPart();
		part.setSql("SELECT a, b FROM t1 WHERE a > 1 ORDER BY b");

		assertEquals("a, b", part.getFields().trim());
		assertEquals("t1", part.getTableName().trim());
		assertEquals("a > 1", part.getWhere().trim());
		assertEquals("b", part.getOrderBy().trim());
	}

	@Test
	public void testNoWhere() {
		SqlPart part = new SqlPart();
		part.setSql("SELECT a, b FROM t1");

		assertEquals("a, b", part.getFields().trim());
		assertEquals("t1", part.getTableName().trim());
		assertEquals("1=1", part.getWhere().trim());
		assertEquals("", part.getOrderBy().trim());
		assertEquals("", part.getGroupBy().trim());
		assertEquals("", part.getHaving().trim());
	}

	@Test
	public void testNoWhereWithOrderBy() {
		SqlPart part = new SqlPart();
		part.setSql("SELECT a, b FROM t1 ORDER BY a DESC");

		assertEquals("t1", part.getTableName().trim());
		assertEquals("1=1", part.getWhere().trim());
		assertEquals("a DESC", part.getOrderBy().trim());
	}

	@Test
	public void testNoWhereWithGroupByHaving() {
		SqlPart part = new SqlPart();
		part.setSql("SELECT a, count(*) FROM t1 GROUP BY a HAVING count(*) > 1");

		assertEquals("t1", part.getTableName().trim());
		assertEquals("1=1", part.getWhere().trim());
		assertEquals("a", part.getGroupBy().trim());
		assertEquals("count(*) > 1", part.getHaving().trim());
	}

	@Test
	public void testNoWhereWithGroupByHavingOrderBy() {
		SqlPart part = new SqlPart();
		part.setSql("SELECT a, count(*) FROM t1 GROUP BY a HAVING count(*) > 1 ORDER BY a");

		assertEquals("t1", part.getTableName().trim());
		assertEquals("1=1", part.getWhere().trim());
		assertEquals("a", part.getGroupBy().trim());
		assertEquals("count(*) > 1", part.getHaving().trim());
		assertEquals("a", part.getOrderBy().trim());
	}

	@Test
	public void testRebuildSqlNoWhere() {
		SqlPart part = new SqlPart();
		part.setSql("SELECT a, b FROM t1");

		String rebuilt = part.rebuildSql("a DESC", "b > 10");
		assertTrue(rebuilt.contains("WHERE"));
		assertTrue(rebuilt.contains("1=1"));
		assertTrue(rebuilt.contains("AND"));
		assertTrue(rebuilt.contains("b > 10"));
		assertTrue(rebuilt.contains("ORDER BY a DESC"));
	}
}
