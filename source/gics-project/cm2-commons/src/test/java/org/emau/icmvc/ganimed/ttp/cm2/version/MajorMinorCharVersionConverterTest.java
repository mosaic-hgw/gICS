package org.emau.icmvc.ganimed.ttp.cm2.version;

import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MajorMinorCharVersionConverterTest
{
	VersionConverter vc;

	@BeforeEach
	void setup()
	{
		vc = new MajorMinorCharVersionConverter();
	}

	@Test
	void stringToInt() throws InvalidVersionException
	{
		assertEquals(1000000, vc.stringToInt("1.0.a"));
		assertEquals(2000001, vc.stringToInt("2.0.b"));
		assertEquals(2001000, vc.stringToInt("2.1.a"));
		assertEquals(3002001, vc.stringToInt("3.2.b"));
		assertEquals(2023002000, vc.stringToInt("2023.2.a"));
		assertEquals(2146999025, vc.stringToInt("2146.999.z"));
		assertThrows(InvalidVersionException.class, () -> vc.stringToInt("2147.0.a"));
	}

	@Test
	void intToString() throws InvalidVersionException
	{
		assertEquals("1.0.a", vc.intToString(1000000));
		assertEquals("2.0.b", vc.intToString(2000001));
		assertEquals("2.1.a", vc.intToString(2001000));
		assertEquals("3.2.b", vc.intToString(3002001));
		assertEquals("2023.2.a", vc.intToString(2023002000));
		assertEquals("2146.999.z", vc.intToString(2146999025));
		assertThrows(InvalidVersionException.class, () -> vc.intToString(2146999026));
		assertThrows(InvalidVersionException.class, () -> vc.intToString(2147000000));
	}

	@Test
	void extractRelevantParts() throws InvalidVersionException
	{
		assertEquals("2.3.a", vc.extractRelevantParts("2.3.a.5"));
		assertEquals("2.3.b", vc.extractRelevantParts("2.3.b"));
		assertThrows(InvalidVersionException.class, () -> vc.extractRelevantParts("2.3"));
	}
}