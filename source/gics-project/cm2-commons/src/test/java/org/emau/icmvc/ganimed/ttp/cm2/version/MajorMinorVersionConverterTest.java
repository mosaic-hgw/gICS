package org.emau.icmvc.ganimed.ttp.cm2.version;

import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MajorMinorVersionConverterTest
{
	VersionConverter vc;

	@BeforeEach
	void setup()
	{
		vc = new MajorMinorVersionConverter();
	}

	@Test
	void stringToInt() throws InvalidVersionException
	{
		assertEquals(1000, vc.stringToInt("1.0"));
		assertEquals(2001, vc.stringToInt("2.1"));
		assertEquals(2023001, vc.stringToInt("2023.1"));
		assertEquals(2146999, vc.stringToInt("2146.999"));
		assertThrows(InvalidVersionException.class, () -> vc.stringToInt("2147.0"));
	}

	@Test
	void intToString() throws InvalidVersionException
	{
		assertEquals("1.0", vc.intToString(1000));
		assertEquals("2.1", vc.intToString(2001));
		assertEquals("2023.1", vc.intToString(2023001));
		assertEquals("2146.999", vc.intToString(2146999));
		assertThrows(InvalidVersionException.class, () -> vc.intToString(2147000));
	}

	@Test
	void extractRelevantParts() throws InvalidVersionException
	{
		assertEquals("2.3", vc.extractRelevantParts("2.3.4.5"));
		assertEquals("2.3", vc.extractRelevantParts("2.3"));
		assertThrows(InvalidVersionException.class, () -> vc.extractRelevantParts("2"));
	}
}