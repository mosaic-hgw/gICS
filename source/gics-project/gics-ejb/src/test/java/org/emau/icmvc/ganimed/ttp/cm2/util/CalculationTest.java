package org.emau.icmvc.ganimed.ttp.cm2.util;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
 * 							kontakt-ths@uni-greifswald.de
 *
 * 							concept and implementation
 * 							l.geidel, c.hampf
 * 							web client
 * 							a.blumentritt, m.bialke, f.m.moser
 * 							fhir-api
 * 							m.bialke
 * 							docker
 * 							r. schuldt
 *
 * 							The gICS was developed by the University Medicine Greifswald and published
 * 							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 *
 * 							please cite our publications
 * 							https://doi.org/10.1186/s12911-022-02081-4
 * 							https://doi.org/10.1186/s12967-020-02457-y
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * 							http://dx.doi.org/10.3205/17gmds146
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.Serial;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.emau.icmvc.ganimed.ttp.cm2.NotificationManagerBean;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDateValuesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpressionExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentDateValues;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpressionExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.Consent;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.Domain;
import org.emau.icmvc.ganimed.ttp.cm2.model.Module;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModulePolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.Policy;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerIdType;
import org.emau.icmvc.ganimed.ttp.cm2.model.VirtualPerson;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.ExpirationProperties;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.ValidFromProperties;
import org.emau.icmvc.ganimed.ttp.cm2.servicebased.AbstractGicsTest;
import org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CalculationTest extends AbstractGicsTest
{
	private static final Logger logger = LoggerFactory.getLogger(CalculationTest.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

	private static final class TestVP extends VirtualPerson
	{
		@Serial
		private static final long serialVersionUID = 8919952281703494170L;

		private TestVP(long id) throws NoSuchFieldException, IllegalAccessException
		{
			Field idField = this.getClass().getSuperclass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(this, id);
		}
	}

	@Test
	void caclulateDateValues()
			throws VersionConverterClassException, InvalidVersionException, UnknownDomainException, InvalidParameterException, InvalidPropertiesException, RequirementsNotFullfilledException,
			UnknownModuleException, FreeTextConverterStringException, InvalidFreeTextException, MissingRequiredObjectException, NoSuchFieldException, IllegalAccessException, ParseException
	{
		String version = "1.0";
		String domainName = "test";
		String p1 = "p1";
		String t1 = "t1";
		String m1 = "m1";
		String sid1 = "sid1";
		String sidType = "psn";
		Date now = new Date();

		Set<SignerIdDTO> signerIds = Set.of(new SignerIdDTO(sidType, sid1));
		Domain domain = new Domain(
				new DomainDTO(domainName, MajorMinorVersionConverter.class.getName(), MajorMinorVersionConverter.class.getName(), MajorMinorVersionConverter.class.getName(), List.of(
						sidType)));
		VersionConverterCache.addDomain(domain);
		ConsentTemplateKeyDTO consentTemplateKeyDTO = new ConsentTemplateKeyDTO(domainName, t1, version);
		ConsentTemplateDTO templateDTO = new ConsentTemplateDTO(consentTemplateKeyDTO);
		Set<AssignedPolicyDTO> assignedPolicies = new HashSet<>();
		PolicyKeyDTO policyKeyDTO = new PolicyKeyDTO(domainName, p1, version);
		PolicyDTO policyDTO = new PolicyDTO(policyKeyDTO);
		AssignedPolicyDTO assignedPolicyDTO = new AssignedPolicyDTO(policyDTO);
		assignedPolicies.add(assignedPolicyDTO);
		ModuleKeyDTO moduleKeyDTO = new ModuleKeyDTO(domainName, m1, version);
		ModuleDTO moduleDTO = new ModuleDTO(moduleKeyDTO, "", assignedPolicies);
		Map<ModuleKeyDTO, Module> modules = new HashMap<>();
		Module module = new Module(domain, moduleDTO, true);
		module.getModulePolicies().add(new ModulePolicy(module, new Policy(domain, policyDTO), assignedPolicyDTO));
		modules.put(moduleKeyDTO, module);
		Set<AssignedModuleDTO> assignedModules = new HashSet<>();
		assignedModules.add(new AssignedModuleDTO(moduleDTO, null));
		templateDTO.setAssignedModules(assignedModules);
		ConsentTemplate template = new ConsentTemplate(domain, templateDTO, modules, Map.of(), true);
		VirtualPerson virtualPerson = new TestVP(1L);
		ConsentDTO consentDTO = new ConsentDTO();
		consentDTO.setTemplateType(template.getType());
		consentDTO.getModuleStates().put(moduleKeyDTO, new ModuleStateDTO(moduleKeyDTO, ConsentStatus.ACCEPTED, List.of(policyKeyDTO)));

		//legalConsentTimestamp == consentCreationDate
		consentDTO.setKey(new ConsentKeyDTO(consentTemplateKeyDTO, signerIds, now));
		Consent consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		ConsentDateValues consentDateValues = consent.getConsentDateValues();
		Assertions.assertEquals(now.getTime(), consentDateValues.getLegalConsentTimestamp());
		Assertions.assertEquals(ConsentDateValuesDTO.INFINITE_DATE.getTime(), consentDateValues.getConsentExpirationTimestamp());
		Assertions.assertEquals(consent.getCreateTimestamp().getTime(), consentDateValues.getGicsConsentTimestamp());

		//legalConsentTimestamp == validFromDate
		Date newDate = getDatePlusDays(now, 1);
		consentDTO.setValidFromDate(newDate);
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consentDateValues = consent.getConsentDateValues();
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getLegalConsentTimestamp());
		Assertions.assertEquals(ConsentDateValuesDTO.INFINITE_DATE.getTime(), consentDateValues.getConsentExpirationTimestamp());
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getGicsConsentTimestamp());
		consentDTO.setValidFromDate(null);

		//legalConsentDate = validFromProp
		newDate = getDatePlusDays(now, 2);
		template = new ConsentTemplate(domain, templateDTO, modules, Map.of(), true);
		template.setValidFromProperties(ValidFromProperties.VALID_FROM_DATE.toString() + "=" + dateFormat.format(newDate));
		template.loadPropertiesFromString();
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consentDateValues = consent.getConsentDateValues();
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getLegalConsentTimestamp());
		Assertions.assertEquals(ConsentDateValuesDTO.INFINITE_DATE.getTime(), consentDateValues.getConsentExpirationTimestamp());
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getGicsConsentTimestamp());

		//legalConsentDate = validFromPeriodProp
		newDate = getDatePlusDays(now, 3);
		template = new ConsentTemplate(domain, templateDTO, modules, Map.of(), true);
		template.setValidFromProperties(ValidFromProperties.VALID_FROM_PERIOD.toString() + "=P3D");
		template.loadPropertiesFromString();
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consentDateValues = consent.getConsentDateValues();
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getLegalConsentTimestamp());
		Assertions.assertEquals(ConsentDateValuesDTO.INFINITE_DATE.getTime(), consentDateValues.getConsentExpirationTimestamp());
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getGicsConsentTimestamp());
		template.setValidFromProperties("");

		//expirationDate = expirationPeriodProp
		newDate = getDatePlusDays(now, 5, false);
		template = new ConsentTemplate(domain, templateDTO, modules, Map.of(), true);
		template.setExpirationProperties(ExpirationProperties.VALIDITY_PERIOD.toString() + "=P5D");
		template.loadPropertiesFromString();
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consentDateValues = consent.getConsentDateValues();
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getConsentExpirationTimestamp());

		//expirationDate = expirationDateProp of template
		newDate = getDatePlusDays(now, 5, true);
		template = new ConsentTemplate(domain, templateDTO, modules, Map.of(), true);
		template.setExpirationProperties(ExpirationProperties.EXPIRATION_DATE.toString() + "=" + dateFormat.format(newDate));
		template.loadPropertiesFromString();
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consentDateValues = consent.getConsentDateValues();
		Assertions.assertEquals(newDate.getTime(), consentDateValues.getConsentExpirationTimestamp());

		// >>> ExpirationExpression-Tests

		//expirationDate = expirationDateExpression of Module
		module.getModulePolicies().getFirst().setExpirationPropertiesObject(
				new ExpressionExpirationPropertiesObject(ExpirationProperties.EXPIRATION_EXPRESSION.toString() + "=utils.getLocalDateTimeFromString('2030-01-01 00:00', 'yyyy-MM-dd HH:mm')"));
		template = new ConsentTemplate(domain, templateDTO, modules, Map.of(), true);
		template.loadPropertiesFromString();
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consent.calculateAndSetSignedPolicyExpirationDates();
		consentDateValues = consent.getConsentDateValues(true);
		Assertions.assertEquals(getDateFromString("2030-01-01 00:00", "yyyy-MM-dd HH:mm").getTime(),
				consentDateValues.getPolicyExpirations().get(module.getModulePolicies().getFirst().getPolicy().getKey()));

		//expirationDate = expirationDateExpression of AssignedPolicyDTO
		assignedPolicyDTO.setExpirationProperties(
				new ExpressionExpirationPropertiesDTO(null, null, ExpirationProperties.EXPIRATION_EXPRESSION.toString() + "=utils.getLocalDateTimeFromString('2040-01-01 00:00', 'yyyy-MM-dd HH:mm')"));
		//re-initialize module from dto
		module.getModulePolicies().clear();
		module.getModulePolicies().add(new ModulePolicy(module, new Policy(domain, policyDTO), assignedPolicyDTO));
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consent.calculateAndSetSignedPolicyExpirationDates();
		consentDateValues = consent.getConsentDateValues(true);
		Assertions.assertEquals(getDateFromString("2040-01-01 00:00", "yyyy-MM-dd HH:mm").getTime(),
				consentDateValues.getPolicyExpirations().get(module.getModulePolicies().getFirst().getPolicy().getKey()));

		//expirationDate = expirationDateExpression of AssignedPolicyDTO with metaData usage
		assignedPolicyDTO.setExpirationProperties(
				new ExpressionExpirationPropertiesDTO(null, null,
						ExpirationProperties.EXPIRATION_EXPRESSION.toString() + "= metaData.question == 'yes' ? utils.getLocalDateTimeFromString('2040-01-01 00:00', 'yyyy-MM-dd HH:mm') : utils.now()"));
		//re-initialize module from dto
		module.getModulePolicies().clear();
		module.getModulePolicies().add(new ModulePolicy(module, new Policy(domain, policyDTO), assignedPolicyDTO));
		consentDTO.getMetaData().put("question", "yes");
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		consent.calculateAndSetSignedPolicyExpirationDates();
		consentDateValues = consent.getConsentDateValues(true);
		Assertions.assertEquals(getDateFromString("2040-01-01 00:00", "yyyy-MM-dd HH:mm").getTime(),
				consentDateValues.getPolicyExpirations().get(module.getModulePolicies().getFirst().getPolicy().getKey()));

		//expected exception
		assignedPolicyDTO.setExpirationProperties(
				new ExpressionExpirationPropertiesDTO(null, null,
						ExpirationProperties.EXPIRATION_EXPRESSION.toString() + "= utils.getLocalDateTimeFromString('2040.01.01', 'yyyy-MM-dd HH:mm')"));
		//re-initialize module from dto
		module.getModulePolicies().clear();
		module.getModulePolicies().add(new ModulePolicy(module, new Policy(domain, policyDTO), assignedPolicyDTO));
		consent = new Consent(template, consentDTO, modules, virtualPerson, domain.getConfig());
		Consent finalConsent = consent;
		Assertions.assertThrowsExactly(RequirementsNotFullfilledException.class, finalConsent::calculateAndSetSignedPolicyExpirationDates);
	}

	@Nested
	@DisplayName("validity status changed")
	class ValidityChangedTest
	{
		//see mermaid-markdown in comment at the end of file

		@Test
		@DisplayName("P1 - all dates outside of query")
		void Policy1AllDatesOutside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 1);
			Date expDate = getDatePlusDays(refDate, 6);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, refDate, expDate));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(0, changedPolicies.size());
		}

		@Test
		@DisplayName("P2 - starts before but expires inside")
		void Policy2StartsBeforeButExpiresInside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 1);
			Date expDate = getDatePlusDays(refDate, 3);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, refDate, expDate));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(1, changedPolicies.size());
			assertPolicyDates(refDate, policies, changedPolicies, List.of(expDate));
		}

		@Test
		@DisplayName("P3 - starts and expires inside")
		void Policy3StartsAndExpiresInside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 3);
			Date expDate = getDatePlusDays(refDate, 4);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, refDate, expDate));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(1, changedPolicies.size());
			assertPolicyDates(refDate, policies, changedPolicies, List.of(startDate, expDate));
		}

		@Test
		@DisplayName("P4 - starts inside and expires outside")
		void Policy4StartsInsideAndExpiresOutside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 3);
			Date expDate = getDatePlusDays(refDate, 6);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, refDate, expDate));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(1, changedPolicies.size());
			assertPolicyDates(refDate, policies, changedPolicies, List.of(startDate));
		}

		@Test
		@DisplayName("P5 - 1 expires inside and 1 starts inside")
		void Policy5OneExpiresInsideAndOneStartsInside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 2);
			Date expDate = getDatePlusDays(refDate, 3);
			Date startDate2 = getDatePlusDays(refDate, 4);
			Date expDate2 = getDatePlusDays(refDate, 5);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, startDate, expDate), new NewCachedSignedPolicy(startDate2, startDate2, expDate2));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(2, changedPolicies.size());
			assertPolicyDates(startDate, policies, changedPolicies, List.of(expDate));
			assertPolicyDates(startDate2, policies, changedPolicies, List.of(startDate2));
		}

		@Test
		@DisplayName("P6 - 1 expires inside and 1 expires outside")
		void Policy6OneExpiresInsideAndOneExpiresOutside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 2);
			Date expDate = getDatePlusDays(refDate, 3);
			Date startDate2 = getDatePlusDays(refDate, 4);
			Date expDate2 = getDatePlusDays(refDate, 6);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, startDate, expDate), new NewCachedSignedPolicy(startDate2, startDate2, expDate2));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(2, changedPolicies.size());
			assertPolicyDates(startDate, policies, changedPolicies, List.of(expDate));
			assertPolicyDates(startDate2, policies, changedPolicies, List.of(startDate2));
		}

		@Test
		@DisplayName("P7 - 1 expires inside and 1 starts inside but expires outside")
		void Policy7OneExpiresInsideAndOneStartsInsideButExpiresOutside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 1);
			Date expDate = getDatePlusDays(refDate, 3);
			Date startDate2 = getDatePlusDays(refDate, 4);
			Date expDate2 = getDatePlusDays(refDate, 6);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, startDate, expDate), new NewCachedSignedPolicy(startDate2, startDate2, expDate2));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(2, changedPolicies.size());
			assertPolicyDates(startDate, policies, changedPolicies, List.of(expDate));
			assertPolicyDates(startDate2, policies, changedPolicies, List.of(startDate2));
		}

		@Test
		@DisplayName("P8 - 2 expire inside and 1 starts inside but expires outside")
		void Policy8TwoExpireInsideAndOneStartsInsideButExpiresOutside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 1);
			Date expDate = getDatePlusDays(refDate, 3);
			Date startDate2 = getDatePlusDays(refDate, 3);
			Date expDate2 = getDatePlusDays(refDate, 4);
			Date startDate3 = getDatePlusDays(refDate, 4);
			Date expDate3 = getDatePlusDays(refDate, 6);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, startDate, expDate), new NewCachedSignedPolicy(startDate2, startDate2, expDate2), new NewCachedSignedPolicy(startDate3, startDate3, expDate3));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(3, changedPolicies.size());
			assertPolicyDates(startDate, policies, changedPolicies, List.of(expDate));
			assertPolicyDates(startDate2, policies, changedPolicies, List.of(startDate2, expDate2));
			assertPolicyDates(startDate3, policies, changedPolicies, List.of(startDate3));

			//with same policyKey
			policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", false,
					new NewCachedSignedPolicy(startDate, refDate, expDate), new NewCachedSignedPolicy(startDate2, refDate, expDate2), new NewCachedSignedPolicy(startDate3, refDate, expDate3));

			changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(1, changedPolicies.size());
			assertPolicyDates(refDate, policies, changedPolicies, List.of(expDate, startDate2, expDate2, startDate3));
		}


		@Test
		@DisplayName("P9 - both outside")
		void Policy9BothOutside()
		{
			Date refDate = new Date();
			Date startDate = getDatePlusDays(refDate, 0);
			Date expDate = getDatePlusDays(refDate, 1);
			Date startDate2 = getDatePlusDays(refDate, 6);
			Date expDate2 = getDatePlusDays(refDate, 7);
			Date queryStart = getDatePlusDays(refDate, 2);
			Date queryEnd = getDatePlusDays(refDate, 5);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true,
					new NewCachedSignedPolicy(startDate, startDate, expDate), new NewCachedSignedPolicy(startDate2, startDate2, expDate2));

			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, queryStart, queryEnd);
			Assertions.assertEquals(0, changedPolicies.size());
		}


		@Test
		@DisplayName("test big list")
		void testPerfomance()
		{
			try
			{
				Date refDate = new Date();
				int count = 100_000;
				NewCachedSignedPolicy[] newCachedSignedPolicies = new NewCachedSignedPolicy[count];
				for (int i = 0; i < count; i++)
				{
					newCachedSignedPolicies[i] = new NewCachedSignedPolicy(new Date(), getDatePlusDays(new Date(), 1), getDatePlusDays(new Date(), 2));
				}
				List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, "d1", true, newCachedSignedPolicies);
				long start = System.currentTimeMillis();
				CalcUtils.getChangedPolicyValidities(policies, refDate, getDatePlusDays(refDate, 1000));
				logger.info("count: {}, done in {}ms", count, System.currentTimeMillis() - start);
			}
			catch (Exception e)
			{
				Assertions.fail(e.getMessage());
			}
		}

		@Test
		@DisplayName("grouping changed policies for notification and check json")
		void groupChangedPolicies() throws JsonProcessingException, JSONException, VersionConverterClassException, UnknownDomainException, InvalidVersionException
		{
			Date refDate = new Date();
			Date date1 = getDatePlusDays(refDate, 1);
			Date date2 = getDatePlusDays(refDate, 3);
			Date date3 = getDatePlusDays(refDate, 5);
			Date date4 = getDatePlusDays(refDate, 7);
			Date expDate = getDatePlusDays(refDate, 10000);
			Map<Long, Set<SignerId>> signerIdMap = new HashMap<>();
			Map<SignerId, Long> vpMap = new HashMap<>();
			String sidTypeName = "studyPSN";
			Domain domain = new Domain(
					new DomainDTO("d1", MajorMinorVersionConverter.class.getName(), MajorMinorVersionConverter.class.getName(), MajorMinorVersionConverter.class.getName(), List.of(
							sidTypeName)));
			SignerIdType signerIdType = new SignerIdType(domain, sidTypeName);
			VersionConverterCache.addDomain(domain);

			List<ConsentCache.CachedSignedPolicy> policies = createPoliciesForDates(ConsentStatus.ACCEPTED, domain.getName(), true, true,
					new NewCachedSignedPolicy(date1, date1, expDate), new NewCachedSignedPolicy(date2, date2, expDate), new NewCachedSignedPolicy(date3, date3, expDate),
					new NewCachedSignedPolicy(date4, date4, expDate));
			for (int i = 0; i < policies.size(); i++)
			{
				SignerId signerId = new SignerId(signerIdType, String.valueOf(i));
				signerIdMap.put(policies.get(i).getSPKey().getConsentKey().getVirtualPersonId(), new HashSet<>(Set.of(signerId)));
				vpMap.put(signerId, policies.get(i).getSPKey().getConsentKey().getVirtualPersonId());
			}
			List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = CalcUtils.getChangedPolicyValidities(policies, refDate, expDate);
			Map<SignerId, List<CalcUtils.ChangedSignedPolicyValidity>> resultMap = CalcUtils.groupPoliciesBySignerId(changedPolicies, signerIdMap::get, true);
			Assertions.assertEquals(4, resultMap.size());

			for (CalcUtils.ChangedSignedPolicyValidity changedPolicy : changedPolicies)
			{
				Optional<SignerId> signerId = signerIdMap.get(changedPolicy.policy().getSPKey().getConsentKey().getVirtualPersonId()).stream().findFirst();
				Assertions.assertTrue(signerId.isPresent());
				Assertions.assertEquals(1, resultMap.get(signerId.get()).size());
				Assertions.assertEquals(resultMap.get(signerId.get()).getFirst().policy(), changedPolicy.policy());
			}

			//mulitple signerIds per VP - add new SID to VP, should be one more entry now
			signerIdMap.get(1L).add(new SignerId(signerIdType, "sid1"));
			resultMap = CalcUtils.groupPoliciesBySignerId(changedPolicies, signerIdMap::get, true);
			Assertions.assertEquals(5, resultMap.size());

			//check JSON-Content
			Map<Pair<SignerIdDTO, List<SignerIdDTO>>, List<CalcUtils.ChangedSignedPolicyValidity>> resultMapForNoti = new HashMap<>();
			for (Map.Entry<SignerId, List<CalcUtils.ChangedSignedPolicyValidity>> entry : resultMap.entrySet())
			{
				resultMapForNoti.put(Pair.of(entry.getKey().toDTO(), List.of(new SignerIdDTO("aliasType", "alias1"))), entry.getValue());
			}
			PolicyValidityChangedNotificationMessage noti = new PolicyValidityChangedNotificationMessage(resultMapForNoti, "d1", NotificationManagerBean.CLIENT_ID);
			String jsonString = noti.toJson();
			JSONObject jsonObj = new JSONObject(jsonString);
			Assertions.assertTrue(jsonObj.has("clientId"));
			Assertions.assertTrue(jsonObj.has("type"));
			Assertions.assertTrue(jsonObj.has("domain"));
			Assertions.assertEquals(NotificationManagerBean.CLIENT_ID, jsonObj.getString("clientId"));
			Assertions.assertEquals(domain.getName(), jsonObj.getString("domain"));
			Assertions.assertEquals(PolicyValidityChangedNotificationMessage.NOTI_TYPE, jsonObj.getString("type"));
			Assertions.assertTrue(jsonObj.has("affectedPolicies"));
			JSONArray policiesArr = jsonObj.getJSONArray("affectedPolicies");
			for (int i = 0; i < policiesArr.length(); i++)
			{
				JSONObject policiesJson = policiesArr.getJSONObject(i);
				Assertions.assertEquals(sidTypeName, policiesJson.getString("signerIdType"));

				Assertions.assertTrue(policiesJson.has("aliases"));
				JSONArray aliasesArr = policiesJson.getJSONArray("aliases");
				Assertions.assertEquals(1, aliasesArr.length());

				Assertions.assertTrue(policiesJson.has("policies"));
				JSONArray changedPoliciesArr = policiesJson.getJSONArray("policies");
				Assertions.assertEquals(1, changedPoliciesArr.length());
				Assertions.assertTrue(changedPoliciesArr.getJSONObject(0).has("name"));
				Assertions.assertTrue(changedPoliciesArr.getJSONObject(0).has("version"));
				Assertions.assertTrue(changedPoliciesArr.getJSONObject(0).has("changedDates"));
				changedPoliciesArr.getJSONObject(0).getJSONArray("changedDates");
			}
			logger.info("{}", jsonString);
		}

		private List<ConsentCache.CachedSignedPolicy> createPoliciesForDates(ConsentStatus status, String domain, boolean genRandomNames, NewCachedSignedPolicy... dates)
		{
			return createPoliciesForDates(status, domain, genRandomNames, false, dates);
		}

		private List<ConsentCache.CachedSignedPolicy> createPoliciesForDates(ConsentStatus status, String domain, boolean genRandomNames, boolean genVPids, NewCachedSignedPolicy... dates)
		{
			long currentVPId = 1L;
			List<ConsentCache.CachedSignedPolicy> policies = new ArrayList<>();
			for (NewCachedSignedPolicy date : dates)
			{
				policies.add(createCachedSignedPolicy(date.consentDate.getTime(), date.legalConsentDate.getTime(), date.expirationDate.getTime(), status, domain,
						genRandomNames ? UUID.randomUUID().toString() : "t1",
						genRandomNames ? UUID.randomUUID().toString() : "p1", genVPids ? currentVPId++ : currentVPId));
			}
			return policies;
		}

		private Optional<ConsentCache.CachedSignedPolicy> findPolicyByConsentDate(Date date, List<ConsentCache.CachedSignedPolicy> policies)
		{
			for (ConsentCache.CachedSignedPolicy policy : policies)
			{
				if (policy.getGicsConsentDate() == date.getTime())
				{
					return Optional.of(policy);
				}
			}
			return Optional.empty();
		}

		void assertPolicyLegalConsentDate(Date toFindBy, List<ConsentCache.CachedSignedPolicy> policies, Date expected)
		{
			Optional<ConsentCache.CachedSignedPolicy> policy = findPolicyByConsentDate(toFindBy, policies);
			Assertions.assertTrue(policy.isPresent());
			Assertions.assertEquals(expected.getTime(), policy.get().getLegalConsentDate());
		}

		void assertPolicyExpirationDate(Date toFindBy, List<ConsentCache.CachedSignedPolicy> policies, Date expected)
		{
			Optional<ConsentCache.CachedSignedPolicy> policy = findPolicyByConsentDate(toFindBy, policies);
			Assertions.assertTrue(policy.isPresent());
			Assertions.assertEquals(expected.getTime(), policy.get().getConsentExpirationDate());
		}

		void assertPolicyDates(Date toFindBy, List<ConsentCache.CachedSignedPolicy> policies, List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies, List<Date> expectedDates)
		{
			Set<Date> expectedDateSet = new HashSet<>(expectedDates);
			Optional<ConsentCache.CachedSignedPolicy> policy = findPolicyByConsentDate(toFindBy, policies);
			Assertions.assertTrue(policy.isPresent());
			for (CalcUtils.ChangedSignedPolicyValidity changedPolicy : changedPolicies)
			{
				if (changedPolicy.policy().getGicsConsentDate() == policy.get().getGicsConsentDate())
				{
					Assertions.assertEquals(expectedDateSet.size(), changedPolicy.changedDates().size());
					Assertions.assertTrue(changedPolicy.changedDates().containsAll(expectedDateSet));
					return;
				}
			}
			Assertions.fail();
		}

	}

	private Date getDateFromString(String value, String format)
	{
		return ExpressionUtils.getInstance().getDateFromString(value, format);
	}

	private Date getDatePlusDays(Date date, int days)
	{
		return getDatePlusDays(date, days, true);
	}

	private Date getDatePlusDays(Date date, int days, boolean startOfDay)
	{
		if (startOfDay)
		{
			return Date.from(LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		return DateUtils.addDays(date, days);
	}

	record NewCachedSignedPolicy(Date legalConsentDate, Date consentDate, Date expirationDate)
	{
	}
}

/*
## Diagram for Policy Validity Check

```mermaid
gantt
    title Policy Validity Check
    dateFormat  YYYY-MM-DD
    tickInterval 1day
    Abfrage start :milestone, start, 2024-06-03,
    Abfrage ende :milestone, start, 2024-06-06,
    section P1
    P1 (true|false) :a1, 2024-06-02, 5d
    section P2
    P2 (true|false) :2024-06-02, 2d
    section P3
    P3 (true|false) :2024-06-04, 1d
    section P4
    P4 (true|false) :2024-06-04, 3d
    section P5
    P5.1 (true|false) :2024-06-03, 1d
    P5.2 (true|false) :2024-06-05, 1d
    section P6
    P6.1 (true|false) :2024-06-03, 1d
    P6.2 (true|false) :2024-06-05, 2d
    section P7
    P7.1 (true|false) :2024-06-02, 2d
    P7.2 (true|false) :2024-06-05, 2d
    section P8
    P8.1 (true|false) :2024-06-02, 2d
    P8.2 (true|false) :2024-06-04,1d
    P8.3 (true|false) :2024-06-05,2d
    section P9
    P9 (true|false) :a1, 2024-06-07, 1d
    section P10
    P10 (true|false) :a1, 2024-06-01, 1d
```
*/