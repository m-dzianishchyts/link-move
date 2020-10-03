package com.nhl.link.move.itest;

import com.nhl.link.move.Execution;
import com.nhl.link.move.LmTask;
import com.nhl.link.move.runtime.task.ITaskService;
import com.nhl.link.move.unit.LmIntegrationTest;
import com.nhl.link.move.unit.cayenne.t.*;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.query.SQLSelect;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CreateOrUpdate_ValueConvertersIT extends LmIntegrationTest {

    @Test
	public void test_ById_IntegerToLong() {

		// not specifying "matchById" explicitly ... this should be the default
		LmTask task = etl.service(ITaskService.class).createOrUpdate(Etl6t.class)
				.sourceExtractor("com/nhl/link/move/itest/etl6_to_etl6t_byid.xml").task();

		srcRunSql("INSERT INTO utest.etl6 (ID, NAME) VALUES (45, 'a')");
		srcRunSql("INSERT INTO utest.etl6 (ID, NAME) VALUES (11, 'b')");

		Execution e1 = task.run();
		assertExec(2, 2, 0, 0, e1);
		assertEquals(2, targetScalar("SELECT count(1) from utest.etl6t"));
		assertEquals(1, targetScalar("SELECT count(1) from utest.etl6t WHERE NAME = 'a' AND ID = 45"));
		assertEquals(1, targetScalar("SELECT count(1) from utest.etl6t WHERE NAME = 'b' AND ID = 11"));

		srcRunSql("INSERT INTO utest.etl6 (ID, NAME) VALUES (31, 'c')");
		srcRunSql("UPDATE utest.etl6 SET NAME = 'd' WHERE ID = 45");

		Execution e2 = task.run();
		assertExec(3, 1, 1, 0, e2);
		assertEquals(3, targetScalar("SELECT count(1) from utest.etl6t"));
		assertEquals(1, targetScalar("SELECT count(1) from utest.etl6t WHERE NAME = 'd' AND ID = 45"));
		assertEquals(1, targetScalar("SELECT count(1) from utest.etl6t WHERE NAME = 'c' AND ID = 31"));

		srcRunSql("DELETE FROM utest.etl6 WHERE ID = 45");

		Execution e3 = task.run();
		assertExec(2, 0, 0, 0, e3);
		assertEquals(3, targetScalar("SELECT count(1) from utest.etl6t"));
		assertEquals(1, targetScalar("SELECT count(1) from utest.etl6t WHERE NAME = 'd' AND ID = 45"));

		Execution e4 = task.run();
		assertExec(2, 0, 0, 0, e4);
	}

	@Test
	public void test_ByAttribute_SyncFk() {

		LmTask task = etl.service(ITaskService.class).createOrUpdate(Etl3t.class)
				.sourceExtractor("com/nhl/link/move/itest/etl3_to_etl3t_converters.xml").matchBy(Etl3t.NAME).task();

		srcRunSql("INSERT INTO utest.etl2 (ID, NAME) VALUES (1, 'abc')");
		srcRunSql("INSERT INTO utest.etl3 (E2_ID, NAME) VALUES (1, 'xyz')");
		targetRunSql("INSERT INTO utest.etl2t (ID, NAME) VALUES (1, 'abc')");

		Execution e1 = task.run();
		assertExec(1, 1, 0, 0, e1);

		assertEquals(1, targetScalar("SELECT count(1) from utest.etl3t WHERE E2_ID = 1"));
	}

	@Test
	public void test_ById_IntegerToBoolean() {

		LmTask task = etl.service(ITaskService.class).createOrUpdate(Etl4t.class)
				.sourceExtractor("com/nhl/link/move/itest/etl4_to_etl4t_converters.xml")
				.task();

		srcRunSql("INSERT INTO utest.etl4 (ID, C_BOOLEAN) VALUES (1, true)");
		srcRunSql("INSERT INTO utest.etl4 (ID, C_BOOLEAN) VALUES (2, false)");
		targetRunSql("INSERT INTO utest.etl4t (ID, C_BOOLEAN) VALUES (1, false)");
		targetRunSql("INSERT INTO utest.etl4t (ID, C_BOOLEAN) VALUES (2, true)");

		Execution e1 = task.run();
		assertExec(2, 0, 2, 0, e1);

		assertEquals(1, targetScalar("SELECT count(1) from utest.etl4t WHERE ID = 1 AND C_BOOLEAN = TRUE"));
		assertEquals(1, targetScalar("SELECT count(1) from utest.etl4t WHERE ID = 2 AND C_BOOLEAN = FALSE"));
	}

	@Test
	public void test_ById_StringToEnum() {

		LmTask task = etl.service(ITaskService.class).createOrUpdate(Etl4t.class)
				.sourceExtractor("com/nhl/link/move/itest/etl4_to_etl4t_converters.xml")
                .task();

		srcRunSql("INSERT INTO utest.etl4 (ID, C_ENUM) VALUES (1, 'str1')");
		srcRunSql("INSERT INTO utest.etl4 (ID, C_ENUM) VALUES (2, 'str3')");
		targetRunSql("INSERT INTO utest.etl4t (ID, C_ENUM) VALUES (1, 'str2')");
		targetRunSql("INSERT INTO utest.etl4t (ID, C_ENUM) VALUES (2, null)");

		Execution e1 = task.run();
		assertExec(2, 0, 2, 0, e1);

		assertEquals(1, targetScalar("SELECT count(1) from utest.etl4t WHERE ID = 1 AND C_ENUM = 'str1'"));
		assertEquals(1, targetScalar("SELECT count(1) from utest.etl4t WHERE ID = 2 AND C_ENUM = 'str3'"));
	}

	@Test
	public void test_ById_DecimalToDecimal() {

		LmTask task = etl.service(ITaskService.class).createOrUpdate(Etl8t.class)
				.sourceExtractor("com/nhl/link/move/itest/etl8_to_etl8t_byid.xml").task();

		BigDecimal c1 = new BigDecimal("1.000000"), c2 = new BigDecimal("1.000000000"), c3 = new BigDecimal("1.000000000");
		createOrUpdateEtl8(1, c1, c2, c3);

		Execution e1 = task.run();
		assertExec(1, 1, 0, 0, e1);

		Execution e2 = task.run();
		assertExec(1, 0, 0, 0, e2);

		c1 = new BigDecimal("1.000001");
		createOrUpdateEtl8(1, c1, c2, c3);

		Execution e3 = task.run();
		assertExec(1, 0, 1, 0, e3);
	}

	@Test
	public void test_ById_DecimalToDecimal_Exception() {

		LmTask task = etl.service(ITaskService.class).createOrUpdate(Etl8t.class)
				.sourceExtractor("com/nhl/link/move/itest/etl8_to_etl8t_byid.xml").task();

		BigDecimal c1 = new BigDecimal("1.000000"), c2 = new BigDecimal("1.000000001"), c3 = new BigDecimal("1.000000000");
		createOrUpdateEtl8(1, c1, c2, c3);

		assertThrows(Exception.class, task::run);
	}

	@Test
	public void test_ImplicitJavaTimeConversion() {

		LmTask task = etl.service(ITaskService.class).createOrUpdate(Etl4t_jt.class)
				.sourceExtractor("com/nhl/link/move/itest/etl4_to_etl4t_jt_implicit.xml")
				.task();

		srcRunSql("INSERT INTO utest.etl4 (ID, C_DATE, C_TIME, C_TIMESTAMP) VALUES (1, '2020-01-02', '08:01:03', '2020-03-04 09:01:04')");

		Execution e1 = task.run();
		assertExec(1, 1, 0, 0, e1);

		assertEquals(1, targetScalar("SELECT count(1) from utest.etl4t WHERE ID = 1 AND C_DATE = '2020-01-02' AND C_TIME = '08:01:03' AND C_TIMESTAMP = '2020-03-04 09:01:04'"));
	}

	private void createOrUpdateEtl8(int id, BigDecimal c1, BigDecimal c2, BigDecimal c3) {

		DataRow etl8 = getEtl8(id);
		if (etl8 == null) {
			srcRunSql(String.format("INSERT INTO utest.etl8 (ID, C_DECIMAL1, C_DECIMAL2, C_DECIMAL3) " +
				"VALUES (%d, %s, %s, %s)", id, c1, c2, c3));
		} else {
			srcRunSql(String.format("UPDATE utest.etl8 SET C_DECIMAL1 = %s, C_DECIMAL2 = %s, C_DECIMAL3 = %s " +
					"WHERE id = %d", c1, c2, c3, id));
		}

		etl8 = getEtl8(id);
		assertNotNull(etl8);
		assertEquals(c1, etl8.get("C_DECIMAL1"));
		assertEquals(c2, etl8.get("C_DECIMAL2"));
		assertEquals(c3, etl8.get("C_DECIMAL3"));
	}

	private DataRow getEtl8(int id) {
		return SQLSelect.dataRowQuery("SELECT ID, C_DECIMAL1, C_DECIMAL2, C_DECIMAL3 FROM utest.etl8 WHERE ID = " + id)
				.selectFirst(srcStack.runtime().newContext());
	}
}
