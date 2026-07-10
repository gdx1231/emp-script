package com.gdxsoft.easyweb.define.group;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.gdxsoft.easyweb.define.database.Field;
import com.gdxsoft.easyweb.define.database.Table;
import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UXml;

/**
 * Test MySQL to PostgreSQL DDL conversion using import_module.zip.
 *
 * Validates that SqlTable correctly generates PostgreSQL-compatible DDL
 * from MySQL-source table definitions, including:
 * - Identity fields mapping to serial/bigserial/smallserial
 * - VARCHAR/TEXT/CHAR type mappings
 * - Primary key constraint naming
 * - Index generation and filtering (Bug 2 fix: operator precedence)
 * - BIGINT/SMALLINT identity case-insensitive comparison (Bug 1 fix)
 */
public class SqlTableMySqlToPostgreSqlTest {

    private static Table admMenuTable;
    private static Table admMenuSupplyTable;
    private static SqlTable sqlTableMenu;
    private static SqlTable sqlTableSupply;

    @BeforeAll
    public static void setUp() throws Exception {
        URL url = SqlTableMySqlToPostgreSqlTest.class.getClassLoader()
                .getResource("import_module.zip");
        assertNotNull(url, "import_module.zip not found in test resources");

        java.util.List<String> extracted = UFile.unZipFile(url.getPath());
        String extractDir = new File(extracted.get(0)).getParent();

        String tableXmlPath = extractDir + "/table.xml";
        File tableXmlFile = new File(tableXmlPath);
        assertTrue(tableXmlFile.exists(), "table.xml should exist after extraction");

        Document docTable = UXml.retDocument(tableXmlPath);
        NodeList nl = UXml.retNodeList(docTable, "Tables/Table");
        assertEquals(2, nl.getLength(), "Should have 2 tables");

        // Parse adm_menu
        admMenuTable = new Table();
        admMenuTable.fromXml((Element) nl.item(0));
        assertEquals("adm_menu", admMenuTable.getName());
        assertEquals("MYSQL", admMenuTable.getDatabaseType());

        // Parse adm_menu_supply
        admMenuSupplyTable = new Table();
        admMenuSupplyTable.fromXml((Element) nl.item(1));
        assertEquals("adm_menu_supply", admMenuSupplyTable.getName());

        // Generate PostgreSQL DDL
        sqlTableMenu = new SqlTable();
        sqlTableMenu.createSqlTable(admMenuTable, "POSTGRESQL");

        sqlTableSupply = new SqlTable();
        sqlTableSupply.createSqlTable(admMenuSupplyTable, "POSTGRESQL");
    }

    // ======================== DDL structure tests ========================

    @Test
    public void testDdlGeneratedForPostgreSql() {
        String ddl = sqlTableMenu.getCreate();
        assertNotNull(ddl);
        assertFalse(ddl.trim().isEmpty());

        assertTrue(ddl.contains("CREATE TABLE"), "Should be CREATE TABLE");
        assertTrue(ddl.contains("serial"), "INT identity should map to serial");
        assertTrue(ddl.contains("primary key"), "Should have PRIMARY KEY");

        // MySQL-specific syntax MUST be absent
        assertFalse(ddl.contains("ENGINE="), "No MySQL ENGINE");
        assertFalse(ddl.contains("AUTO_INCREMENT"), "No MySQL AUTO_INCREMENT");
        assertFalse(ddl.contains("`"), "No MySQL backticks");
        assertFalse(ddl.contains("CHARSET="), "No MySQL CHARSET");
        assertFalse(ddl.contains("COLLATE="), "No MySQL COLLATE");
    }

    @Test
    public void testIdentityFieldIsSerialNotAutoIncrement() {
        String ddl = sqlTableMenu.getCreate();
        assertTrue(ddl.contains("serial"), "Identity should be serial");
        assertTrue(ddl.contains("NOT NULL"), "Serial should be NOT NULL");
        assertFalse(ddl.toUpperCase().contains("AUTO_INCREMENT"),
                "No AUTO_INCREMENT in PostgreSQL DDL");
    }

    @Test
    public void testVarcharAndCharLengthPreserved() {
        String ddl = sqlTableMenu.getCreate();
        assertTrue(ddl.contains("VARCHAR(240)"), "VARCHAR(240) preserved");
        assertTrue(ddl.contains("VARCHAR(500)"), "VARCHAR(500) preserved");
        assertTrue(ddl.contains("CHAR(3)"), "CHAR(3) preserved");
        assertTrue(ddl.contains("CHAR(36)"), "CHAR(36) preserved");
    }

    @Test
    public void testTextFieldHasNoLength() {
        String ddl = sqlTableMenu.getCreate();
        assertTrue(ddl.contains("TEXT"), "TEXT type present");
        assertFalse(ddl.contains("TEXT("), "TEXT should not have length");
    }

    @Test
    public void testPrimaryKeyConstraintNamed() {
        String ddl = sqlTableMenu.getCreate();
        assertTrue(ddl.contains("PK_adm_menu"), "PK renamed to PK_adm_menu");
    }

    @Test
    public void testAllFieldsPresent() {
        String ddl = sqlTableMenu.getCreate();
        String[] expected = {
                "MNU_ID", "MNU_TEXT", "MNU_PID", "MNU_LVL", "MNU_ORD",
                "MNU_CMD", "MNU_ICON", "MNU_GRP", "MNU_TAG", "MNU_HLP",
                "MNU_TEXT_EN", "MNU_UNID", "MNU_REF", "mnu_py", "MNU_INNER_GRP"
        };
        for (String f : expected) {
            assertTrue(ddl.contains(f), "Field '" + f + "' should be in DDL");
        }
    }

    @Test
    public void testIndexGenerated() {
        ArrayList<String> indexes = sqlTableMenu.getIndexes();
        assertNotNull(indexes);
        assertEquals(1, indexes.size(),
                "adm_menu should have 1 index (MNU_GRP_IDX)");

        String idx = indexes.get(0);
        assertTrue(idx.contains("adm_menu_MNU_GRP_IDX"), "Index name present");
        assertTrue(idx.contains("MNU_GRP"), "MNU_GRP in index");
        assertTrue(idx.contains("MNU_PID"), "MNU_PID in index");
    }

    @Test
    public void testCommentsGenerated() {
        ArrayList<String> comments = sqlTableMenu.getComments();
        assertNotNull(comments);
        assertTrue(comments.size() > 0, "Should have comments");
        for (String c : comments) {
            assertTrue(c.contains("COMMENT ON COLUMN"),
                    "PostgreSQL comment syntax: " + c);
        }
    }

    // ======================== adm_menu_supply tests ========================

    @Test
    public void testCompositePrimaryKeyDdl() {
        String ddl = sqlTableSupply.getCreate();
        assertTrue(ddl.contains("MNU_ID"));
        assertTrue(ddl.contains("SUP_ID"));
        assertTrue(ddl.contains("primary key"), "Has composite PK");
    }

    @Test
    public void testSupplyTableNoMySqlSyntax() {
        String ddl = sqlTableSupply.getCreate();
        assertFalse(ddl.contains("ENGINE="));
        assertFalse(ddl.contains("CHARSET="));
        assertFalse(ddl.contains("`"));
    }

    // ======================== Table metadata tests ========================

    @Test
    public void testTableMetadata() {
        assertEquals("adm_menu", admMenuTable.getName());
        assertEquals("MYSQL", admMenuTable.getDatabaseType());
        assertEquals("TABLE", admMenuTable.getTableType());
        // Note: XML attribute is misspelled "SchamaName" so getSchemaName() is null
        assertEquals(15, admMenuTable.getFields().size());
    }

    @Test
    public void testPkMetadata() {
        assertNotNull(admMenuTable.getPk());
        assertEquals(1, admMenuTable.getPk().getPkFields().size());
        // PK identity info is in the Fields collection, not PkField elements
        Field fullField = admMenuTable.getFields().get("MNU_ID");
        assertNotNull(fullField, "MNU_ID should be in Fields");
        assertEquals("MNU_ID", fullField.getName());
        assertTrue(fullField.isPk());
        assertTrue(fullField.isIdentity(), "MNU_ID should be identity");
        assertFalse(fullField.isNull());
    }

    @Test
    public void testFieldTypes() {
        HashMap<String, Field> fields = admMenuTable.getFields();
        assertEquals("INT", fields.get("MNU_ID").getDatabaseType());
        assertEquals("VARCHAR", fields.get("MNU_TEXT").getDatabaseType());
        assertEquals("CHAR", fields.get("MNU_GRP").getDatabaseType());
        assertEquals("TEXT", fields.get("MNU_HLP").getDatabaseType());
    }

    // ======================== Helper: create test field ========================

    private static Field createField(String name, String dbType, boolean identity,
            boolean pk, boolean nullable, int columnSize) {
        Field f = new Field();
        f.setName(name);
        f.setDatabaseType(dbType);
        f.setIdentity(identity);
        f.setPk(pk);
        f.setNull(nullable);
        f.setColumnSize(columnSize);
        // Description must match Name to skip comment generation
        // (avoids NPE in createTableComment)
        f.setDescription(name);
        return f;
    }

    private static void addFieldToTable(Table t, Field f) {
        t.getFields().put(f.getName(), f);
        t.getFields().getFieldList().add(f.getName());
        if (f.isPk()) {
            t.getPk().getPkFields().add(f);
        }
    }

    // ======================== Regression: Bug 1 fix ========================
    // "bigint".equals(mapTo.getName()) was case-sensitive;
    // mapTo.getName() returns UPPERCASE. Fixed to equalsIgnoreCase.

    @Test
    public void testBigintIdentityMapsToBigserial() throws Exception {
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_bigint");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        Field f = createField("id", "BIGINT", true, true, false, 19);
        addFieldToTable(t, f);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "POSTGRESQL");
        String ddl = st.getCreate();

        assertTrue(ddl.contains("bigserial"),
                "BIGINT identity should be bigserial (not serial): " + ddl.trim());
        assertFalse(ddl.contains(" serial "),
                "BIGINT identity must NOT be plain serial: " + ddl.trim());
    }

    @Test
    public void testSmallintIdentityMapsToSmallserial() throws Exception {
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_smallint");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        Field f = createField("id", "SMALLINT", true, true, false, 5);
        addFieldToTable(t, f);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "POSTGRESQL");
        String ddl = st.getCreate();

        assertTrue(ddl.contains("smallserial"),
                "SMALLINT identity should be smallserial (not serial): " + ddl.trim());
        assertFalse(ddl.contains(" serial "),
                "SMALLINT identity must NOT be plain serial: " + ddl.trim());
    }

    // ======================== Regression: Bug 2 fix ========================
    // Operator precedence: idx.isUnique() && startsWith("pk_") || endsWith("_pk")
    // Fixed to: idx.isUnique() && (startsWith("pk_") || endsWith("_pk"))

    @Test
    public void testIndexFilteringOperatorPrecedence() throws Exception {
        // Unique index ending with "_pk" that is NOT the PK should be preserved
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_idx_filter");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        addFieldToTable(t, createField("col_a", "INT", false, true, false, 10));
        addFieldToTable(t, createField("col_b", "VARCHAR", false, false, true, 100));

        // Unique index ending with "_pk", NOT the primary key
        com.gdxsoft.easyweb.define.database.TableIndex idx =
                new com.gdxsoft.easyweb.define.database.TableIndex();
        idx.setIndexName("uq_my_idx_pk");
        idx.setUnique(true);
        com.gdxsoft.easyweb.define.database.IndexField idxF =
                new com.gdxsoft.easyweb.define.database.IndexField();
        idxF.setName("col_b");
        idxF.setAsc(true);
        idx.getIndexFields().add(idxF);
        t.getIndexes().add(idx);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "POSTGRESQL");

        ArrayList<String> indexes = st.getIndexes();
        assertEquals(0, indexes.size(),
                "Unique index ending with '_pk' IS correctly filtered (PK duplicate)");
    }

    @Test
    public void testNonUniqueIndexEndingWithPkNotFiltered() throws Exception {
        // Non-unique index ending with "_pk" must NOT be filtered
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_non_unique");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        addFieldToTable(t, createField("id", "INT", false, true, false, 10));
        addFieldToTable(t, createField("name", "VARCHAR", false, false, true, 200));

        // Non-unique index ending with "_pk"
        com.gdxsoft.easyweb.define.database.TableIndex idx =
                new com.gdxsoft.easyweb.define.database.TableIndex();
        idx.setIndexName("idx_name_pk");
        idx.setUnique(false);
        com.gdxsoft.easyweb.define.database.IndexField idxF =
                new com.gdxsoft.easyweb.define.database.IndexField();
        idxF.setName("name");
        idxF.setAsc(true);
        idx.getIndexFields().add(idxF);
        t.getIndexes().add(idx);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "POSTGRESQL");

        ArrayList<String> indexes = st.getIndexes();
        assertEquals(1, indexes.size(),
                "Non-unique index ending with '_pk' must not be filtered out");
        assertTrue(indexes.get(0).contains("idx_name_pk"));
    }

    // ======================== Regression: databaseType alias normalization ========================
    // "POSTGRES" (missing "QL") should normalize to "POSTGRESQL" for TypesMap.xml lookup

    @Test
    public void testPostgresAliasNormalized() throws Exception {
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_alias");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        Field f = createField("id", "INT", true, true, false, 10);
        addFieldToTable(t, f);

        // "POSTGRES" (short form, missing "QL") — should be normalized to "POSTGRESQL"
        SqlTable st = new SqlTable();
        st.createSqlTable(t, "POSTGRES");
        String ddl = st.getCreate();

        assertNotNull(ddl);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should be generated");
        assertTrue(ddl.contains("serial"), "INT identity should become serial");
        assertEquals("POSTGRESQL", st.getDatabaseType(),
                "POSTGRES alias should be normalized to POSTGRESQL");
    }

    @Test
    public void testPostgresAliasWithSqlAlsoWorks() throws Exception {
        // "PostgreSql" (mixed case) should also work
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_mixedcase");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        Field f = createField("id", "INT", true, true, false, 10);
        addFieldToTable(t, f);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "PostgreSql");
        String ddl = st.getCreate();

        assertNotNull(ddl);
        assertTrue(ddl.contains("serial"));
        assertEquals("POSTGRESQL", st.getDatabaseType(),
                "Mixed case should normalize to POSTGRESQL");
    }

    @Test
    public void testPgAliasNormalized() throws Exception {
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_pg_alias");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        Field f = createField("id", "INT", true, true, false, 10);
        addFieldToTable(t, f);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "pg");
        String ddl = st.getCreate();

        assertNotNull(ddl);
        assertTrue(ddl.contains("serial"));
        assertEquals("POSTGRESQL", st.getDatabaseType());
    }

    // ======================== Regression: PRIMARY index name filtering ========================
    // MySQL's default PK index is named "PRIMARY" — must be filtered like pk_/xxx_pk

    @Test
    public void testPrimaryNamedIndexFiltered() throws Exception {
        // "PRIMARY" is a reserved word in PostgreSQL; must be filtered out
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_primary_idx");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        addFieldToTable(t, createField("id", "INT", false, true, false, 10));

        com.gdxsoft.easyweb.define.database.TableIndex idx =
                new com.gdxsoft.easyweb.define.database.TableIndex();
        idx.setIndexName("PRIMARY");
        idx.setUnique(true);
        com.gdxsoft.easyweb.define.database.IndexField idxF =
                new com.gdxsoft.easyweb.define.database.IndexField();
        idxF.setName("id");
        idxF.setAsc(true);
        idx.getIndexFields().add(idxF);
        t.getIndexes().add(idx);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "POSTGRESQL");

        ArrayList<String> indexes = st.getIndexes();
        assertEquals(0, indexes.size(),
                "Unique index named 'PRIMARY' must be filtered out (PK already created)");
    }

    @Test
    public void testPrimaryLowercaseAlsoFiltered() throws Exception {
        Table t = new Table();
        t.initBlankFrame();
        t.setName("test_primary_lower");
        t.setDatabaseType("MYSQL");
        t.getPk().setPkName("PRIMARY");

        addFieldToTable(t, createField("id", "INT", false, true, false, 10));

        com.gdxsoft.easyweb.define.database.TableIndex idx =
                new com.gdxsoft.easyweb.define.database.TableIndex();
        idx.setIndexName("primary"); // lowercase
        idx.setUnique(true);
        com.gdxsoft.easyweb.define.database.IndexField idxF =
                new com.gdxsoft.easyweb.define.database.IndexField();
        idxF.setName("id");
        idxF.setAsc(true);
        idx.getIndexFields().add(idxF);
        t.getIndexes().add(idx);

        SqlTable st = new SqlTable();
        st.createSqlTable(t, "POSTGRESQL");

        assertEquals(0, st.getIndexes().size(),
                "Lowercase 'primary' should also be filtered");
    }
}
