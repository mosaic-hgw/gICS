package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

import java.util.HashMap;
import java.util.Map;

import org.emau.icmvc.ganimed.ttp.cm2.frontend.testtools.GicsWebTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsentControllerTest extends GicsWebTest
{
	@InjectMocks
	private ConsentController consentController;

	@BeforeEach
	void setUpConsentControllerTest()
	{
		setUpGicsWebTest();
	}

	@Nested
	@DisplayName("Prefill Consent")
	class PrefillConsentTests
	{
		Map<String, String[]> parameters = new HashMap<>();
		
		@Test
		void missingDomain()
		{
			// Act
			consentController.prefillConsent(parameters);
		
			// Assert
			assertTrue(facesContext.getMessageList().getFirst().getSummary().contains(bundleDe.getString("page.consents.prefill.message.missingDomain")), facesContext.getMessageList().getFirst().getSummary());
		}

		@Test
		void hasTemplateVersionButMissingTemplateName()
		{
			// Arrange
			parameters.put("domain", new String[] {"Test"});
			parameters.put("version", new String[] {"Test"});

			// Act
			consentController.prefillConsent(parameters);

			// Assert
			assertTrue(facesContext.getMessageList().getFirst().getSummary().contains(bundleDe.getString("page.consents.prefill.message.missingTemplateName")), facesContext.getMessageList().getFirst().getSummary());
		}

		@Test
		void hasTemplateNameButMissingTemplateVersion()
		{
			// Arrange
			parameters.put("domain", new String[] {"Test"});
			parameters.put("name", new String[] {"Test"});

			// Act
			consentController.prefillConsent(parameters);

			// Assert
			assertTrue(facesContext.getMessageList().getFirst().getSummary().contains(bundleDe.getString("page.consents.prefill.message.missingTemplateVersion")), facesContext.getMessageList().getFirst().getSummary());
		}
	}
}
