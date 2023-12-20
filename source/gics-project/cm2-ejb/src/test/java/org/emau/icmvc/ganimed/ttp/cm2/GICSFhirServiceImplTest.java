package org.emau.icmvc.ganimed.ttp.cm2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GICSFhirServiceImplTest
{
	static final String CURRENT_VERSION = "2023.1.0";

	static class TestService extends GICSFhirServiceImpl
	{
		private String currentVersion = CURRENT_VERSION;

		@Override
		String getCurrentVersion()
		{
			return currentVersion;
		}

		void setCurrentVersion(String version)
		{
			this.currentVersion = version;
		}
	}

	TestService service;


	@BeforeEach
	void setup()
	{
		service = new TestService();
	}

	@Test
	void testCheckImportVersion()
	{
		assertTrue(service.checkImportVersion("2023.1.0"));
		assertTrue(service.checkImportVersion("2023.1.5")); // ignoring bugfix part
		assertTrue(service.checkImportVersion("2.0.0"));
		assertTrue(service.checkImportVersion("2.1.0"));
		assertTrue(service.checkImportVersion("2.2.0"));
		assertTrue(service.checkImportVersion("2.13.0"));
		assertTrue(service.checkImportVersion("2.13.4"));
		assertTrue(service.checkImportVersion("2.13.x"));

		assertFalse(service.checkImportVersion("2023.2.0"));
		assertFalse(service.checkImportVersion("2024.1.0"));

	}
}