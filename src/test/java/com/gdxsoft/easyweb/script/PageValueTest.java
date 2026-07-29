package com.gdxsoft.easyweb.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class PageValueTest {

	private PageValue pv(Object val) {
		PageValue p = new PageValue();
		p.setValue(val);
		p.autoDetectDataType();
		return p;
	}

	@Test
	public void testNull() {
		PageValue p = new PageValue();
		p.autoDetectDataType();
		assertNull(p.getDataType());
	}

	@Test
	public void testString() {
		assertEquals("String", pv("hello").getDataType());
	}

	@Test
	public void testDate() {
		assertEquals("Date", pv(new Date()).getDataType());
	}

	@Test
	public void testLong() {
		assertEquals("BigInt", pv(100L).getDataType());
	}

	@Test
	public void testInteger() {
		assertEquals("Int", pv(42).getDataType());
	}

	@Test
	public void testShort() {
		assertEquals("Int", pv((short) 1).getDataType());
	}

	@Test
	public void testDouble() {
		assertEquals("Number", pv(3.14).getDataType());
	}

	@Test
	public void testFloat() {
		assertEquals("Number", pv(3.14f).getDataType());
	}

	@Test
	public void testBigDecimal() {
		assertEquals("Number", pv(new BigDecimal("99.99")).getDataType());
	}

	@Test
	public void testByteArray() {
		assertEquals("Binary", pv(new byte[]{1, 2}).getDataType());
	}

	@Test
	public void testByteArrayBoxed() {
		assertEquals("Binary", pv(new Byte[]{1, 2}).getDataType());
	}

	@Test
	public void testUnknownType() {
		assertEquals("StringBuilder", pv(new StringBuilder()).getDataType());
	}

	// toInteger tests
	@Test
	public void testToIntegerFromInteger() {
		PageValue p = new PageValue();
		p.setValue(42);
		assertEquals(Integer.valueOf(42), p.toInteger());
	}

	@Test
	public void testToIntegerFromLong() {
		PageValue p = new PageValue();
		p.setValue(100L);
		assertEquals(Integer.valueOf(100), p.toInteger());
	}

	@Test
	public void testToIntegerFromDouble() {
		PageValue p = new PageValue();
		p.setValue(3.14);
		assertEquals(Integer.valueOf(3), p.toInteger());
	}

	@Test
	public void testToIntegerFromString() {
		PageValue p = new PageValue();
		p.setValue("123");
		assertEquals(Integer.valueOf(123), p.toInteger());
	}

	@Test
	public void testToIntegerFromStringWithDecimal() {
		PageValue p = new PageValue();
		p.setValue("123.45");
		assertEquals(Integer.valueOf(123), p.toInteger());
	}

	@Test
	public void testToIntegerFromNull() {
		PageValue p = new PageValue();
		p.setValue(null);
		assertEquals(null, p.toInteger());
	}

	@Test
	public void testToIntegerFromUndefined() {
		PageValue p = new PageValue();
		p.setValue("undefined");
		assertEquals(null, p.toInteger());
	}

	// toLong tests
	@Test
	public void testToLongFromLong() {
		PageValue p = new PageValue();
		p.setValue(100L);
		assertEquals(Long.valueOf(100), p.toLong());
	}

	@Test
	public void testToLongFromInteger() {
		PageValue p = new PageValue();
		p.setValue(42);
		assertEquals(Long.valueOf(42), p.toLong());
	}

	@Test
	public void testToLongFromString() {
		PageValue p = new PageValue();
		p.setValue("999");
		assertEquals(Long.valueOf(999), p.toLong());
	}

	// toDouble tests
	@Test
	public void testToDoubleFromDouble() {
		PageValue p = new PageValue();
		p.setValue(3.14);
		assertEquals(Double.valueOf(3.14), p.toDouble());
	}

	@Test
	public void testToDoubleFromInteger() {
		PageValue p = new PageValue();
		p.setValue(42);
		assertEquals(Double.valueOf(42.0), p.toDouble());
	}

	@Test
	public void testToDoubleFromString() {
		PageValue p = new PageValue();
		p.setValue("3.14");
		assertEquals(Double.valueOf(3.14), p.toDouble());
	}

	// toBigDecimal tests
	@Test
	public void testToBigDecimalFromBigDecimal() {
		PageValue p = new PageValue();
		BigDecimal bd = new BigDecimal("99.99");
		p.setValue(bd);
		assertEquals(bd, p.toBigDecimal());
	}

	@Test
	public void testToBigDecimalFromDouble() {
		PageValue p = new PageValue();
		p.setValue(3.14);
		assertEquals(new BigDecimal("3.14"), p.toBigDecimal());
	}

	@Test
	public void testToBigDecimalFromString() {
		PageValue p = new PageValue();
		p.setValue("123.456");
		assertEquals(new BigDecimal("123.456"), p.toBigDecimal());
	}

	// Comma-separated number tests
	@Test
	public void testToIntegerWithComma() {
		PageValue p = new PageValue();
		p.setValue("1,234");
		assertEquals(Integer.valueOf(1234), p.toInteger());
	}

	@Test
	public void testToIntegerWithCommaAndDecimal() {
		PageValue p = new PageValue();
		p.setValue("1,234.56");
		assertEquals(Integer.valueOf(1234), p.toInteger());
	}

	@Test
	public void testToLongWithComma() {
		PageValue p = new PageValue();
		p.setValue("1,234,567");
		assertEquals(Long.valueOf(1234567), p.toLong());
	}

	@Test
	public void testToDoubleWithComma() {
		PageValue p = new PageValue();
		p.setValue("1,234.56");
		assertEquals(Double.valueOf(1234.56), p.toDouble());
	}

	@Test
	public void testToBigDecimalWithComma() {
		PageValue p = new PageValue();
		p.setValue("1,234.56");
		assertEquals(new BigDecimal("1234.56"), p.toBigDecimal());
	}

	// Date/Time type detection tests
	@Test
	public void testLocalDate() {
		assertEquals("Date", pv(LocalDate.now()).getDataType());
	}

	@Test
	public void testLocalTime() {
		assertEquals("Time", pv(LocalTime.now()).getDataType());
	}

	@Test
	public void testLocalDateTime() {
		assertEquals("DateTime", pv(LocalDateTime.now()).getDataType());
	}

	@Test
	public void testZonedDateTime() {
		assertEquals("DateTime", pv(ZonedDateTime.now()).getDataType());
	}

	@Test
	public void testOffsetDateTime() {
		assertEquals("DateTime", pv(OffsetDateTime.now(ZoneOffset.UTC)).getDataType());
	}

	@Test
	public void testInstant() {
		assertEquals("DateTime", pv(Instant.now()).getDataType());
	}

	// toDate tests
	@Test
	public void testToDateFromDate() {
		PageValue p = new PageValue();
		Date now = new Date();
		p.setValue(now);
		assertEquals(now, p.toDate(null));
	}

	@Test
	public void testToDateFromLocalDate() {
		PageValue p = new PageValue();
		p.setValue(LocalDate.of(2026, 7, 29));
		Date result = p.toDate(null);
		assertEquals(2026 - 1900, result.getYear());
		assertEquals(6, result.getMonth());
		assertEquals(29, result.getDate());
	}

	@Test
	public void testToDateFromLocalDateTime() {
		PageValue p = new PageValue();
		p.setValue(LocalDateTime.of(2026, 7, 29, 10, 30, 45));
		Date result = p.toDate(null);
		assertEquals(2026 - 1900, result.getYear());
		assertEquals(6, result.getMonth());
		assertEquals(29, result.getDate());
		assertEquals(10, result.getHours());
		assertEquals(30, result.getMinutes());
	}

	@Test
	public void testToDateFromInstant() {
		PageValue p = new PageValue();
		Instant now = Instant.now();
		p.setValue(now);
		assertEquals(Date.from(now), p.toDate(null));
	}

	@Test
	public void testToDateFromLong() {
		PageValue p = new PageValue();
		long timestamp = System.currentTimeMillis();
		p.setValue(timestamp);
		assertEquals(new Date(timestamp), p.toDate(null));
	}

	@Test
	public void testToDateFromNull() {
		PageValue p = new PageValue();
		p.setValue(null);
		assertEquals(null, p.toDate(null));
	}

	@Test
	public void testToDateFromString() {
		PageValue p = new PageValue();
		p.setValue("2026-07-29");
		Date result = p.toDate("enus");
		assertEquals(2026 - 1900, result.getYear());
		assertEquals(6, result.getMonth());
		assertEquals(29, result.getDate());
	}

	@Test
	public void testToDateFromStringZhcn() {
		PageValue p = new PageValue();
		p.setValue("2026-07-29");
		Date result = p.toDate("zhcn");
		assertEquals(2026 - 1900, result.getYear());
		assertEquals(6, result.getMonth());
		assertEquals(29, result.getDate());
	}
}
