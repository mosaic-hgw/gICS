package org.emau.icmvc.ganimed.ttp.cm2.version;

import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MajorMinorMaintenanceVersionConverterTest {
	VersionConverter vc;

	@BeforeEach
	void setup()
	{
		vc = new MajorMinorMaintenanceVersionConverter();
	}

	@Test
	void stringToInt() throws InvalidVersionException
	{
		assertEquals(1000000, vc.stringToInt("1.0.0"));
		assertEquals(2000001, vc.stringToInt("2.0.1"));
		assertEquals(2001000, vc.stringToInt("2.1.0"));
		assertEquals(3002001, vc.stringToInt("3.2.1"));
		assertEquals(2023002001, vc.stringToInt("2023.2.1"));
		assertEquals(2146999999, vc.stringToInt("2146.999.999"));
		assertThrows(InvalidVersionException.class, () -> vc.stringToInt("2147.0.0"));
	}

	@Test
	void intToString() throws InvalidVersionException
	{
		assertEquals("1.0.0", vc.intToString(1000000));
		assertEquals("2.0.1", vc.intToString(2000001));
		assertEquals("2.1.0", vc.intToString(2001000));
		assertEquals("3.2.1", vc.intToString(3002001));
		assertEquals("2023.2.1", vc.intToString(2023002001));
		assertEquals("2146.999.999", vc.intToString(2146999999));
		assertThrows(InvalidVersionException.class, () -> vc.intToString(2147000000));
	}

	@Test
	void extractRelevantParts() throws InvalidVersionException
	{
		assertEquals("2.3.4", vc.extractRelevantParts("2.3.4.5"));
		assertEquals("2.3.4", vc.extractRelevantParts("2.3.4"));
		assertThrows(InvalidVersionException.class, () -> vc.extractRelevantParts("2.3"));
	}
}