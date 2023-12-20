package org.emau.icmvc.ganimed.ttp.cm2;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2023 Trusted Third Party of the University Medicine Greifswald -
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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ImportResultDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FhirExportMode;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter;
import org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule;
import org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.PatientConsentStatus;
import org.emau.icmvc.magic.fhir.datatypes.AssignedConsentPolicy;
import org.emau.icmvc.magic.fhir.datatypes.ConsentDomain;
import org.emau.icmvc.magic.fhir.datatypes.ConsentModule;
import org.emau.icmvc.magic.fhir.datatypes.ConsentPolicy;
import org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate;
import org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText;
import org.emau.icmvc.magic.fhir.resources.ExchangeFormatDefinition;
import org.hl7.fhir.r4.model.Type;

@WebService(name = "gicsFhirService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(GICSFhirService.class)
public class GICSFhirServiceImpl extends GICSServiceBase implements GICSFhirService
{
	private final FhirContext ctx = FhirContext.forR4();

	// worklist for user feedback
	private List<ConsentTemplateKeyDTO> addedTemplates;
	private List<ModuleKeyDTO> addedModules;
	private List<PolicyKeyDTO> addedPolicies;
	private List<DomainDTO> addedDomains;

	private List<ConsentTemplateKeyDTO> updatedTemplates;
	private List<ModuleKeyDTO> updatedModules;
	private List<PolicyKeyDTO> updatedPolicies;
	private List<DomainDTO> updatedDomains;

	private List<ConsentTemplateKeyDTO> ignoredTemplates;
	private List<ModuleKeyDTO> ignoredModules;
	private List<PolicyKeyDTO> ignoredPolicies;
	private List<DomainDTO> ignoredDomains;

	// QC types
	private final static String DEFAULT_VALID_QC_TYPES = "VALID_QC_TYPES=not_checked,checked_no_faults,checked_minor_faults";
	private final static String DEFAULT_INVALID_QC_TYPES = "INVALID_QC_TYPES=checked_major_faults,invalidated";
	private final static String DEFAULT_QC_TYPE = "DEFAULT_QC_TYPE=not_checked";

	protected void checkAllowedDomain(ImportResultDTO importResult) throws InvalidExchangeFormatException
	{
		try
		{
			for (DomainDTO d : importResult.getUpdatedDomains())
			{
				checkAllowedDomain(d.getName());
			}
			for (DomainDTO d : importResult.getAddedDomains())
			{
				checkAllowedDomain(d.getName());
			}
			for (ConsentTemplateKeyDTO t : importResult.getAddedTemplates())
			{
				checkAllowedDomain(t);
			}
			for (ConsentTemplateKeyDTO t : importResult.getUpdatedTemplates())
			{
				checkAllowedDomain(t);
			}
			for (ModuleKeyDTO m : importResult.getAddedModules())
			{
				checkAllowedDomain(m.getDomainName());
			}
			for (ModuleKeyDTO m : importResult.getUpdatedModules())
			{
				checkAllowedDomain(m);
			}
			for (PolicyKeyDTO p : importResult.getAddedPolicies())
			{
				checkAllowedDomain(p);
			}
			for (PolicyKeyDTO p : importResult.getUpdatedPolicies())
			{
				checkAllowedDomain(p);
			}
		}
		catch (UnknownDomainException e)
		{
			throw new InvalidExchangeFormatException(e.getMessage(), e);
		}
	}

	@Override
	public ImportResultDTO previewImportDefinition(String definition, boolean allowUpdates, String fileFormat) throws InvalidExchangeFormatException
	{
		ImportResultDTO result = processDefinition(definition, allowUpdates, fileFormat, true);
		checkAllowedDomain(result);
		logger.debug("verarbeitung erfolgreich");
		return result;
	}

	@Override
	public ImportResultDTO importDefinition(String definition, boolean allowUpdates, String fileFormat) throws InvalidExchangeFormatException
	{
		ImportResultDTO result = processDefinition(definition, allowUpdates, fileFormat, false);
		checkAllowedDomain(result);
		logger.debug("verarbeitung erfolgreich");
		return result;
	}

	private ImportResultDTO processDefinition(String definition, boolean allowUpdates, String fileFormat, boolean preview) throws InvalidExchangeFormatException
	{
		logger.debug("###########################################");
		logger.info("importing received fhir import definition.");
		registerFhirTypes();
		logger.info("Allow Updates: " + allowUpdates);

		resetWorkList();

		try
		{
			ExchangeFormatDefinition importInfo = validateFormat(definition, fileFormat);
			processImportedDomainInfo(importInfo.getDomain(), allowUpdates, preview);
			String domainName = importInfo.getDomain().getName();
			processImportedPolicyInfo(domainName, importInfo.getPolicies(), allowUpdates, preview);
			processImportedModuleInfo(domainName, importInfo.getModules(), allowUpdates, preview);
			processImportedTemplateInfo(domainName, importInfo.getTemplates(), allowUpdates, preview);

			logImportResult();

			ImportResultDTO result = new ImportResultDTO();
			result.setAddedTemplates(addedTemplates);
			result.setAddedModules(addedModules);
			result.setAddedPolicies(addedPolicies);
			result.setAddedDomains(addedDomains);

			result.setUpdatedTemplates(updatedTemplates);
			result.setUpdatedModules(updatedModules);
			result.setUpdatedPolicies(updatedPolicies);
			result.setUpdatedDomains(updatedDomains);

			result.setIgnoredTemplates(ignoredTemplates);
			result.setIgnoredModules(ignoredModules);
			result.setIgnoredPolicies(ignoredPolicies);
			result.setIgnoredDomains(ignoredDomains);

			return result;
		}
		catch (RuntimeException e)
		{
			if (e.getCause() instanceof InvalidExchangeFormatException)
			{
				throw (InvalidExchangeFormatException) e.getCause();
			}
			throw e;
		}
	}

	private void logImportResult()
	{
		logger.debug("Domains added: " + addedDomains.size());
		logger.debug("Policies added: " + addedPolicies.size());
		logger.debug("Modules added: " + addedModules.size());
		logger.debug("Templates added: " + addedTemplates.size());
		logger.debug("Domains updated: " + updatedDomains.size());
		logger.debug("Policies updated: " + updatedPolicies.size());
		logger.debug("Modules updated: " + updatedModules.size());
		logger.debug("Templates updated: " + updatedTemplates.size());
		logger.debug("Domains ignored: " + ignoredDomains.size());
		logger.debug("Policies ignored: " + ignoredPolicies.size());
		logger.debug("Modules ignored: " + ignoredModules.size());
		logger.debug("Templates ignored: " + ignoredTemplates.size());
	}

	/***
	 * reset lists for added / updated import items
	 */
	private void resetWorkList()
	{
		addedTemplates = new ArrayList<>();
		addedModules = new ArrayList<>();
		addedPolicies = new ArrayList<>();
		addedDomains = new ArrayList<>();

		updatedTemplates = new ArrayList<>();
		updatedModules = new ArrayList<>();
		updatedPolicies = new ArrayList<>();
		updatedDomains = new ArrayList<>();

		ignoredTemplates = new ArrayList<>();
		ignoredModules = new ArrayList<>();
		ignoredPolicies = new ArrayList<>();
		ignoredDomains = new ArrayList<>();
	}

	@Override
	public String exportDefinition(String domainName, FhirExportMode exportMode, List<String> itemList, boolean exportLogo, String fileFormat) throws InternalException, InvalidExchangeFormatException
	{
		try
		{
			checkAllowedDomain(domainName);
		}
		catch (UnknownDomainException e)
		{
			throw new InternalException(e.getMessage(), e);
		}
		ExchangeFormatDefinition def = prepareExport();

		DomainDTO domain;
		try
		{
			domain = dad.getDomain(domainName);

			switch (exportMode)
			{
				case ALL:
					exportDefinition(domain, def, exportLogo);
					break;
				case DOMAIN:
					exportDomainDefinition(domain, def, exportLogo);
					break;
				case POLICIES:
					exportDomainDefinition(domain, def, exportLogo);
					exportPoliciesDefinition(domain, def, itemList);
					break;
				case MODULES:
					exportDomainDefinition(domain, def, exportLogo);
					exportModulesDefinition(domain, def, itemList);
					break;
				case TEMPLATES:
					exportDomainDefinition(domain, def, exportLogo);
					exportTemplatesDefinition(domain, def, itemList);
					break;
				default:
					logger.debug("unknown type of export");
					break;
			}
		}
		catch (UnknownDomainException e)
		{
			throw new InternalException(e.getMessage());
		}

		return convertToExchangeFormat(def, fileFormat);
	}

	private ExchangeFormatDefinition prepareExport()
	{
		if (logger.isInfoEnabled())
		{
			logger.info("preparing export fhir definition");
		}
		registerFhirTypes();

		return new ExchangeFormatDefinition(getCurrentVersion());
	}

	/**
	 * convert ExchangeFormatDefinition to string represenation using fhir hapi
	 *
	 * @param def
	 *            ExchangeFormatDefinition to string represenation using fhir hapi
	 * @param fileFormat
	 *            format of definition (e.g. xml or json)
	 * @return string version of def
	 * @throws InvalidExchangeFormatException
	 */
	private String convertToExchangeFormat(ExchangeFormatDefinition def, String fileFormat) throws InvalidExchangeFormatException
	{
		if (fileFormat != null && fileFormat.equalsIgnoreCase("xml"))
		{
			// default: use XML export
			return getFhirContext().newXmlParser().encodeResourceToString(def);
		}
		else if (fileFormat != null && fileFormat.equalsIgnoreCase("json"))
		{
			// use JSON
			return getFhirContext().newJsonParser().encodeResourceToString(def);
		}
		else
		{
			throw new InvalidExchangeFormatException("Unsupported export fileFormat: " + fileFormat);
		}
	}

	/**
	 * export exchangeformat containing all info about given domain, polices,
	 * modules and templates
	 *
	 * @param domain
	 *            instance of domain
	 * @param def
	 *            target exchange format
	 * @param exportLogo
	 *            TRUE if logo should be exported
	 * @return exchangeformat
	 * @throws UnknownDomainException
	 *             domain is not known
	 */
	private ExchangeFormatDefinition exportDefinition(DomainDTO domain, ExchangeFormatDefinition def,
			Boolean exportLogo) throws UnknownDomainException
	{
		logger.debug("Exporting ALL");
		def.setDomain(convertToFhirConsentDomain(domain, exportLogo));

		def.setPolicies(convertToFhirConsentPolices(dad.listPolicies(domain.getName(), false)));

		def.setModules(convertToFhirConsentModules(dad.listModules(domain.getName(), false)));

		def.setTemplates(convertToFhirConsentTemplates(dad.listConsentTemplates(domain.getName(), false)));

		return def;
	}

	/**
	 * export definition of policies for given domain, exports only a subset of
	 * policies as specified in itemlist
	 *
	 * @param domain
	 *            domaindto
	 * @param def
	 *            target
	 * @param itemList
	 * @return exchangeformat with policy info
	 * @throws InternalException
	 */
	private void exportPoliciesDefinition(DomainDTO domain, ExchangeFormatDefinition def,
			List<String> itemList) throws InternalException
	{
		logger.debug("Exporting POLICIES");
		try
		{
			List<PolicyDTO> policies = new ArrayList<>();

			if (itemList != null && !itemList.isEmpty())
			{
				for (String keystring : itemList)
				{
					PolicyKeyDTO key = (PolicyKeyDTO) convertKeystringTo(PolicyKeyDTO.class, keystring);
					policies.add(dad.getPolicy(key));
				}
			}
			else
			{
				policies = dad.listPolicies(domain.getName(), false);
			}

			def.setPolicies(convertToFhirConsentPolices(policies));
		}
		catch (UnknownPolicyException | UnknownDomainException | InvalidVersionException e)
		{
			logger.error(e.getLocalizedMessage());
			throw new InternalException("Unable to create export format MODE.POLICIES: " + e.getMessage());
		}
	}

	/**
	 * export definition of modules and relevant policies for given domain, ,
	 * exports only a subset of modules as specified in itemlist
	 *
	 * @param domain
	 *            domaindto
	 * @param def
	 *            target
	 * @param itemList
	 * @return exchangeformat with module info
	 * @throws InternalException
	 */
	private void exportModulesDefinition(DomainDTO domain, ExchangeFormatDefinition def,
			List<String> itemList) throws InternalException
	{
		logger.debug("Exporting MODULES");
		try
		{
			List<ModuleDTO> modules = new ArrayList<>();
			// get relevant policies only
			List<PolicyDTO> policies = new ArrayList<>();

			if (itemList != null && !itemList.isEmpty())
			{
				for (String keystring : itemList)
				{
					ModuleKeyDTO key = (ModuleKeyDTO) convertKeystringTo(ModuleKeyDTO.class, keystring);
					modules.add(dad.getModule(key));
				}
			}
			else
			{
				modules = dad.listModules(domain.getName(), false);
			}

			// get relevant policies only

			for (ModuleDTO m : modules)
			{
				for (AssignedPolicyDTO modulePolicy : m.getAssignedPolicies())
				{
					if (!policies.contains(modulePolicy.getPolicy()))
					{
						policies.add(modulePolicy.getPolicy());
					}
				}
			}

			def.setModules(convertToFhirConsentModules(modules));
			def.setPolicies(convertToFhirConsentPolices(policies));
		}
		catch (UnknownDomainException | UnknownModuleException | InvalidVersionException e)
		{
			logger.error(e.getLocalizedMessage());
			throw new InternalException("Unable to create export format MODE.MODULES: " + e.getMessage());
		}
	}

	/**
	 * export definition of templates and relevant modules, policies for given domain, , exports
	 * only a subset of templates as specified in itemlist
	 *
	 * @param domain
	 *            domaindto
	 * @param def
	 *            target
	 * @param itemList
	 * @return exchangeformat with template info
	 * @throws InternalException
	 */
	private ExchangeFormatDefinition exportTemplatesDefinition(DomainDTO domain, ExchangeFormatDefinition def, List<String> itemList)
			throws InternalException
	{
		logger.debug("Exporting TEMPLATES");
		try
		{
			List<ConsentTemplateDTO> templates = new ArrayList<>();

			if (itemList != null && !itemList.isEmpty())
			{
				for (String keystring : itemList)
				{
					ConsentTemplateKeyDTO key = (ConsentTemplateKeyDTO) convertKeystringTo(ConsentTemplateKeyDTO.class, keystring);
					templates.add(dad.getConsentTemplate(key));
				}
			}
			else
			{
				templates = dad.listConsentTemplates(domain.getName(), false);
			}

			// get relevant modules
			List<ModuleDTO> modules = new ArrayList<>();
			// get relevant policies only
			List<PolicyDTO> policies = new ArrayList<>();

			for (ConsentTemplateDTO t : templates)
			{
				// add relevant modules
				for (AssignedModuleDTO assignedModule : t.getAssignedModules())
				{
					// ModuleDTO m = assignedModule.getModule();
					if (!modules.contains(assignedModule.getModule()))
					{
						modules.add(assignedModule.getModule());
					}
					// add relevant policies

					for (AssignedPolicyDTO p : assignedModule.getModule().getAssignedPolicies())
					{
						if (!policies.contains(p.getPolicy()))
						{
							policies.add(p.getPolicy());
						}
					}
				}
			}
			def.setTemplates(convertToFhirConsentTemplates(templates));
			def.setModules(convertToFhirConsentModules(modules));
			def.setPolicies(convertToFhirConsentPolices(policies));
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException e)
		{
			logger.error(e.getLocalizedMessage());
			throw new InternalException("Unable to create export format MODE.TEMPLATES: " + e.getMessage());
		}
		return def;
	}

	/**
	 * export definition of specific domain
	 *
	 * @param domain
	 *            domaindto
	 * @param def
	 *            target
	 * @param exportLogo
	 *            true if export shall include logo
	 * @return exchangeformat with domain info
	 */
	private void exportDomainDefinition(DomainDTO domain, ExchangeFormatDefinition def, Boolean exportLogo)
	{
		logger.debug("Exporting DOMAIN");
		def.setDomain(convertToFhirConsentDomain(domain, exportLogo));
	}

	private List<Type> convertToFhirConsentTemplates(List<ConsentTemplateDTO> listConsentTemplates)
	{
		List<Type> converted = new ArrayList<>();

		for (ConsentTemplateDTO gicsTempl : listConsentTemplates)
		{
			ConsentTemplate fhirTempl = new ConsentTemplate();

			// Templateinfo
			fhirTempl.setDomainName(gicsTempl.getKey().getDomainName());
			fhirTempl.setName(gicsTempl.getKey().getName());
			fhirTempl.setVersion(gicsTempl.getKey().getVersion());
			if (gicsTempl.getComment() != null)
			{
				fhirTempl.setComment(gicsTempl.getComment());
			}
			if (gicsTempl.getTitle() != null)
			{
				fhirTempl.setTitle(gicsTempl.getTitle());
			}
			if (gicsTempl.getFooter() != null)
			{
				fhirTempl.setFooter(gicsTempl.getFooter());
			}
			if (gicsTempl.getHeader() != null)
			{
				fhirTempl.setHeader(gicsTempl.getHeader());
			}
			if (gicsTempl.getExpirationProperties() != null)
			{
				fhirTempl.setExpirationProperties(new ExpirationPropertiesObject(gicsTempl.getExpirationProperties()).toPropertiesString());
			}
			if (gicsTempl.getExternProperties() != null)
			{
				fhirTempl.setExternProperties(gicsTempl.getExternProperties());
			}
			if (gicsTempl.getLabel() != null)
			{
				fhirTempl.setLabel(gicsTempl.getLabel());
			}
			if (gicsTempl.getVersionLabel() != null)
			{
				fhirTempl.setVersionLabel(gicsTempl.getVersionLabel());
			}
			if (gicsTempl.getType() != null)
			{
				fhirTempl.setType(String.valueOf(gicsTempl.getType()));
			}
			fhirTempl.setFinalized(gicsTempl.getFinalised());

			// Freetexts
			for (FreeTextDefDTO gicsFt : gicsTempl.getFreeTextDefs())
			{
				ConsentTemplateFreeText fhirFt = new ConsentTemplateFreeText();

				if (gicsFt.getName() != null)
				{
					fhirFt.setName(gicsFt.getName());
				}

				fhirFt.setRequired(gicsFt.getRequired());

				if (gicsFt.getComment() != null)
				{
					fhirFt.setComment(gicsFt.getComment());
				}

				if (gicsFt.getConverterString() != null)
				{
					fhirFt.setConverterString(gicsFt.getConverterString());
				}

				if (gicsFt.getType() != null)
				{
					fhirFt.setType(convertToFhirFreeTextType(gicsFt.getType()));
				}

				if (gicsFt.getExternProperties() != null)
				{
					fhirFt.setExternProperties(gicsFt.getExternProperties());
				}

				if (gicsFt.getLabel() != null)
				{
					fhirFt.setLabel(gicsFt.getLabel());
				}

				fhirFt.setPosition(gicsFt.getPos());

				fhirTempl.addFreetext(fhirFt);
			}

			// Liste AssModule
			for (AssignedModuleDTO gicsModul : gicsTempl.getAssignedModules())
			{
				AssignedConsentModule fhirModul = new AssignedConsentModule();

				fhirModul.setModuleKey(gicsModul.getModule().getKey().getDomainName() + ";" + gicsModul.getModule().getKey().getName() + ";"
						+ gicsModul.getModule().getKey().getVersion());

				fhirModul.setOrderNumber(gicsModul.getOrderNumber());

				fhirModul.setMandatory(gicsModul.getMandatory());

				if (gicsModul.getComment() != null)
				{
					fhirModul.setComment(gicsModul.getComment());
				}

				// available patientconsenstatus
				List<PatientConsentStatus> displayOptions = new ArrayList<>();

				for (ConsentStatus gicsStatus : gicsModul.getDisplayCheckboxes())
				{
					displayOptions.add(convertToFhirConsentStatus(gicsStatus));
				}

				fhirModul.setDisplayCheckBoxes(displayOptions);

				if (gicsModul.getDefaultConsentStatus() != null)
				{
					fhirModul.setDefaultConsentStatus(convertToFhirConsentStatus(gicsModul.getDefaultConsentStatus()));
				}

				if (gicsModul.getExternProperties() != null)
				{
					fhirModul.setExternProperties(gicsModul.getExternProperties());
				}

				if (gicsModul.getExpirationProperties() != null)
				{
					fhirModul.setExpirationProperties(new ExpirationPropertiesObject(gicsModul.getExpirationProperties()).toPropertiesString());
				}

				fhirTempl.addAssignedModule(fhirModul);
			}

			converted.add(fhirTempl);
		}

		return converted;
	}

	/**
	 * convert gics FreeTextType to fhir format FreeTextType
	 *
	 * @param gicsType
	 * @return
	 */
	private org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType convertToFhirFreeTextType(FreeTextType gicsType)
	{
		switch (gicsType)
		{
			case STRING:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.STRING;

			case DATE:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.DATE;

			case BOOLEAN:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.BOOLEAN;

			case INTEGER:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.INTEGER;

			case DOUBLE:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.DOUBLE;

		}
		return null;
	}

	/**
	 * convert gics ConsentStatus to FhirFormat PatientConsentStatus
	 *
	 * @param status
	 * @return
	 */
	private PatientConsentStatus convertToFhirConsentStatus(ConsentStatus status)
	{
		// TODO nicht mehr notwendig ->aufraumen
		switch (status)
		{
			case ACCEPTED:
				return PatientConsentStatus.ACCEPTED;

			case DECLINED:
				return PatientConsentStatus.DECLINED;

			case UNKNOWN:
				return PatientConsentStatus.UNKNOWN;

			case NOT_ASKED:
				return PatientConsentStatus.NOT_ASKED;

			case NOT_CHOSEN:
				return PatientConsentStatus.NOT_CHOSEN;

			case WITHDRAWN:
				return PatientConsentStatus.WITHDRAWN;

			case INVALIDATED:
				return PatientConsentStatus.INVALIDATED;

			case REFUSED:
				return PatientConsentStatus.REFUSED;

			case EXPIRED:
				return PatientConsentStatus.EXPIRED;
		}
		return PatientConsentStatus.UNKNOWN;
	}

	/**
	 * convert gics modules to fhir export modules
	 *
	 * @param listModules
	 * @return converted modules
	 */
	private List<Type> convertToFhirConsentModules(List<ModuleDTO> listModules)
	{
		List<Type> converted = new ArrayList<>();

		for (ModuleDTO m : listModules)
		{
			List<Type> listOfPolicies = new ArrayList<>();
			// Liste AssModule
			for (AssignedPolicyDTO assigendPolicy : m.getAssignedPolicies())
			{
				AssignedConsentPolicy fhirAssignedPolicy = new AssignedConsentPolicy();

				fhirAssignedPolicy.setPolicyKeyFromDTO(assigendPolicy.getPolicy().getKey());

				if (assigendPolicy.getComment() != null && !assigendPolicy.getComment().isEmpty())
				{
					fhirAssignedPolicy.setComment(assigendPolicy.getComment());
				}

				if (assigendPolicy.getExternProperties() != null && !assigendPolicy.getExternProperties().isEmpty())
				{
					fhirAssignedPolicy.setExternProperties(assigendPolicy.getExternProperties());
				}

				if (assigendPolicy.getExpirationProperties() != null)
				{

					fhirAssignedPolicy.setExpirationProperties(new ExpirationPropertiesObject(assigendPolicy.getExpirationProperties()).toPropertiesString());
				}

				listOfPolicies.add(fhirAssignedPolicy);
			}
			ConsentModule cm = new ConsentModule();

			cm.setAssignedPolicies(listOfPolicies);
			cm.setModuleName(m.getKey().getName());
			cm.setModuleVersion(m.getKey().getVersion());
			cm.setDomainName(m.getKey().getDomainName());
			// cm.setPolicyKeys(policyKeys);
			if (m.getComment() != null)
			{
				cm.setModuleComment(m.getComment());
			}
			if (m.getShortText() != null)
			{
				cm.setModuleShortText(m.getShortText());
			}

			if (m.getExternProperties() != null)
			{
				cm.setExternProperties(m.getExternProperties());
			}
			if (m.getLabel() != null)
			{
				cm.setModuleLabel(m.getLabel());
			}

			if (m.getText() != null)
			{
				cm.setModuleText(m.getText());
			}

			if (m.getTitle() != null)
			{
				cm.setModuleTitle(m.getTitle());
			}

			cm.setModuleFinalized(m.getFinalised());

			converted.add(cm);
		}

		return converted;
	}

	/**
	 * convert gics PolicyDTOs to gics fhir policies
	 *
	 * @param listPolicies
	 * @return converted policies
	 */
	private List<Type> convertToFhirConsentPolices(List<PolicyDTO> listPolicies)
	{
		List<Type> converted = new ArrayList<>();

		for (PolicyDTO p : listPolicies)
		{
			ConsentPolicy cp = new ConsentPolicy();
			cp.setName(p.getKey().getName());
			cp.setVersion(p.getKey().getVersion());
			cp.setDomainName(p.getKey().getDomainName());
			if (p.getComment() != null)
			{
				cp.setComment(p.getComment());
			}
			if (p.getExternProperties() != null)
			{
				cp.setExternProperties(p.getExternProperties());
			}
			if (p.getLabel() != null)
			{
				cp.setLabel(p.getLabel());
			}
			converted.add(cp);
		}
		return converted;
	}

	private ConsentDomain convertToFhirConsentDomain(DomainDTO existingDomain, Boolean exportLogo)
	{
		ConsentDomain convert = new ConsentDomain();

		convert.setName(existingDomain.getName());
		if (existingDomain.getComment() != null)
		{
			convert.setComment(existingDomain.getComment());
		}
		convert.setLabel(existingDomain.getLabel());

		// try to get logo from db

		if (exportLogo)
		{
			String logoString = existingDomain.getLogo();
			if (logoString != null && !logoString.isEmpty())
			{
				byte[] logoBytes = Base64.getDecoder().decode(logoString);
				convert.setLogo(logoBytes);
			}
		}

		convert.setPolicyVersionConverter(existingDomain.getPolicyVersionConverter());
		convert.setModuleVersionConverter(existingDomain.getModuleVersionConverter());
		convert.setConsentTemplateVersionConverter(existingDomain.getCtVersionConverter());

		convert.setSignerIdTypes(existingDomain.getSignerIdTypes());

		if (existingDomain.getExternProperties() != null)
		{
			convert.setExternProperties(existingDomain.getExternProperties());
		}
		if (existingDomain.getExpirationProperties() != null)
		{
			convert.setExpirationProperties(new ExpirationPropertiesObject(existingDomain.getExpirationProperties()).toPropertiesString());
		}

		if (existingDomain.getProperties() != null)
		{
			String properties = existingDomain.getProperties();
			if (properties.startsWith("domain properties: "))
			{
				properties = properties.replaceFirst("domain properties: ", "");
			}

			convert.setProperties(properties);
		}
		convert.setFinalized(existingDomain.getFinalised());

		return convert;
	}

	/**
	 * @param domainName
	 * @param templates
	 * @param allowUpdates
	 * @param preview
	 *            true if the processing should be simulated and not persited
	 */
	private void processImportedTemplateInfo(String domainName, List<Type> templates, boolean allowUpdates, boolean preview)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("####################################");
		}

		if (logger.isInfoEnabled())
		{
			logger.info(templates.size() + " templates to be imported.");
		}

		for (Type t : templates)
		{
			org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate importedTemplate = (org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate) t;

			if (logger.isDebugEnabled())
			{
				logger.debug("Importing template: " + importedTemplate.toString());
			}

			ConsentTemplateKeyDTO importTemplateKeyDTO = (ConsentTemplateKeyDTO) convertKeystringTo(ConsentTemplateKeyDTO.class, importedTemplate.toKeyString());
			// check domain name
			if (domainName.equals(importedTemplate.getDomainName()))
			{
				ConsentTemplateDTO importTemplateDTO = null;

				try
				{
					Set<FreeTextDefDTO> listeFreeTextDtos = new HashSet<>();
					Set<AssignedModuleDTO> listeAssModuleDtos = new HashSet<>();

					// Check if assigned modules exist
					List<Type> templateModules = importedTemplate.getAssignedModules();

					for (Type module : templateModules)
					{
						AssignedConsentModule checkModule = (AssignedConsentModule) module;
						if (!preview)
						{
							// skip in preview
							dad.getModule((ModuleKeyDTO) convertKeystringTo(ModuleKeyDTO.class, checkModule.getModuleKey()));
						}
					}

					// convert imported freetext def to dto-list
					int pos = 0;

					for (Type current : importedTemplate.getFreetextDefs())
					{
						int currentPosition = pos;

						ConsentTemplateFreeText ft = (ConsentTemplateFreeText) current;

						// if import contains specific order info: use it, else order by importorder
						if (ft.getPosition() != null)
						{
							currentPosition = ft.getPosition();
						}

						logger.debug(FreeTextType.valueOf(ft.getType().toString()));

						listeFreeTextDtos.add(new FreeTextDefDTO(ft.getName(), ft.getRequired(), FreeTextType.valueOf(ft.getType().toString()),
								ft.getConverterString(), currentPosition, ft.getComment(), false, new Date(), new Date(),
								null, ft.getExternProperties(), ft.getLabel()));
						pos++;
					}

					boolean error = false;
					// convert imported assigned modules to dtos
					for (Type current : importedTemplate.getAssignedModules())
					{
						AssignedConsentModule acm = (AssignedConsentModule) current;
						ModuleKeyDTO mkdto = (ModuleKeyDTO) convertKeystringTo(ModuleKeyDTO.class, acm.getModuleKey());

						// list display checkboxes
						List<ConsentStatus> displayCheckboxes = new ArrayList<>();

						for (PatientConsentStatus status : acm.getDisplayCheckBoxes())
						{
							logger.debug("display consent status " + status.toString());
							displayCheckboxes.add(ConsentStatus.valueOfIncludingObsolete(status.toString()));
						}

						AssignedModuleDTO gicsAssMod = new AssignedModuleDTO();

						try
						{
							gicsAssMod.setModule(dad.getModule(mkdto));
						}
						catch (UnknownDomainException | UnknownModuleException maybe)
						{
							String msg = "Error while processing assigned module " + mkdto.getName();
							if (!preview)
							{
								logger.error(msg, maybe);
								error = true;
								break;
							}
							else
							{
								boolean found = false;
								for (ModuleKeyDTO moduleKeyDTO : addedModules)
								{
									if (moduleKeyDTO.equals(mkdto))
									{
										gicsAssMod.setModule(new ModuleDTO(mkdto));
										found = true;
										break;
									}
								}
								if (!found)
								{
									logger.error(msg, maybe);
									error = true;
									break;
								}
							}
						}

						gicsAssMod.setMandatory(acm.getMandatory());
						gicsAssMod.setDisplayCheckboxes(displayCheckboxes);
						gicsAssMod.setOrderNumber(acm.getOrderNumber());

						if (acm.getExternProperties() != null)
						{
							gicsAssMod.setExternProperties(acm.getExternProperties());
						}

						if (acm.getExpirationProperties() != null)
						{
							gicsAssMod.setExpirationProperties(new ExpirationPropertiesObject(acm.getExpirationProperties()).toDTO());
						}

						if (acm.getComment() != null)
						{
							gicsAssMod.setComment(acm.getComment());
						}
						if (acm.getDefaultConsentStatus() != null)
						{
							gicsAssMod.setDefaultConsentStatus(ConsentStatus.valueOfIncludingObsolete(acm.getDefaultConsentStatus()));
						}

						listeAssModuleDtos.add(gicsAssMod);

						// parent ass modules are not supported
					}
					if (error)
					{
						ignoredTemplates.add(importTemplateKeyDTO);
						continue;
					}

					// convert imported template to templateDTO
					importTemplateDTO = new ConsentTemplateDTO(
							importTemplateKeyDTO,
							importedTemplate.getTitle(),
							new ExpirationPropertiesObject(importedTemplate.getExpirationProperties()).toDTO(),
							importedTemplate.getComment(),
							importedTemplate.getExternProperties(),
							ConsentTemplateType.valueOf(String.valueOf(importedTemplate.getType())),
							importedTemplate.getHeader(),
							importedTemplate.getFooter(),
							// LATER add scanbase64 here
							"",
							// LATER add scanFileType here
							"",
							listeAssModuleDtos,
							listeFreeTextDtos,
							importedTemplate.getLabel(),
							importedTemplate.getVersionLabel(),
							// FMM: the finalized status of the template is intentionally ignored, see
							// https://git.icm.med.uni-greifswald.de/ths/gics-project/-/issues/357
							// templates (as well as modules and policies) always should be considered as
							// not finalized on import otherwise you'll get an error on importing finalized templates
							// when previously existing children of this template are not yet finalized
							// (e.g. when referred to from another non-finalized template):
							// "template X can't be finalised because at least one related module
							// isn't finalised"
							false /* importedTemplate.getFinalized() */,
							new Date(),
							new Date(),
							// FHIR ID by templates wird nicht importiert
							null,
							null,
							// no support for mapped consent templates
							null, null, null);

					// existing, update if applicable
					ConsentTemplateDTO existingTemplate = dad.getConsentTemplate(importTemplateKeyDTO);

					// changes occured?
					if (existingTemplate != null && !existingTemplate.equalsForFhirSerice(importTemplateDTO))
					{
						if (allowUpdates)
						{
							if (existingTemplate.getFinalised())
							{
								// finalised, only selected changes allowed
								if (!preview)
								{
									dad.updateConsentTemplateInUse(importTemplateDTO);
									logger.info("Template " + existingTemplate.getKey().toString() + " updated.");
								}
							}
							else
							{
								// not finalised, change all
								if (!preview)
								{
									dad.updateConsentTemplate(importTemplateDTO, false);
									logger.info("Template " + existingTemplate.getKey().toString() + " replaced.");
								}
							}
							updatedTemplates.add(importTemplateKeyDTO);
						}
						else
						{
							logger.info("Changes in template " + existingTemplate.getKey().toString() + " ignored. AllowUpdates=false");
							ignoredTemplates.add(existingTemplate.getKey());
						}
					}
					else
					{
						logger.info("No changes detected. Template ignored.");
						ignoredTemplates.add(importTemplateKeyDTO);
					}
				}
				catch (UnknownConsentTemplateException maybe)
				{
					try
					{
						if (!preview)
						{
							finaliseDomain(importTemplateKeyDTO.getDomainName());
							dad.addConsentTemplate(importTemplateDTO, false);
							logger.info("template " + importedTemplate.toKeyString() + " added.");
						}
						addedTemplates.add(importTemplateKeyDTO);
					}
					catch (DuplicateEntryException | FreeTextConverterStringException | InvalidPropertiesException | UnknownModuleException | InvalidParameterException | InvalidVersionException |
						   RequirementsNotFullfilledException | UnknownDomainException | UnknownConsentTemplateException e)
					{
						ignoredTemplates.add(importTemplateKeyDTO);
						String errorMessage = "Error while processing template " + importedTemplate.toKeyString();
						logger.error(errorMessage, e);
					}
				}
				catch (UnknownDomainException maybe)
				{
					if (!preview)
					{
						ignoredTemplates.add(importTemplateKeyDTO);
						String errorMessage = "Error while processing template " + importedTemplate.toKeyString();
						logger.error(errorMessage, maybe);
					}
					else
					{
						boolean found = false;
						for (DomainDTO domainDTO : addedDomains)
						{
							if (domainDTO.getName().equals(domainName))
							{
								addedTemplates.add(importTemplateKeyDTO);
								found = true;
								break;
							}
						}
						if (!found)
						{
							ignoredTemplates.add(importTemplateKeyDTO);
							String errorMessage = "Error while processing template " + importedTemplate.toKeyString();
							logger.error(errorMessage, maybe);
						}
					}
				}
				catch (InvalidVersionException | DuplicateEntryException | FreeTextConverterStringException | InvalidPropertiesException | UnknownModuleException | InvalidFreeTextException
						| ObjectInUseException | DateTimeParseException | ParseException | InvalidParameterException | RequirementsNotFullfilledException e)
				{
					ignoredTemplates.add(importTemplateKeyDTO);
					String errorMessage = "Error while processing template " + importedTemplate.toKeyString();
					logger.error(errorMessage, e);
				}
			}
			else
			{
				ignoredTemplates.add(importTemplateKeyDTO);
				logger.error("Template key contains unexpected domain name: " + importedTemplate.getDomainName());
			}
		}
	}

	/**
	 * @param domainName
	 * @param modules
	 * @param allowUpdates
	 * @param preview
	 *            true if the processing should be simulated and not persited
	 */
	private void processImportedModuleInfo(String domainName, List<Type> modules, boolean allowUpdates, boolean preview)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("####################################");
		}
		if (logger.isInfoEnabled())
		{
			logger.info(modules.size() + " modules to be imported.");
		}

		for (Type t : modules)
		{
			ConsentModule currentModule = (ConsentModule) t;

			if (logger.isDebugEnabled())
			{
				logger.debug("Importing module: " + currentModule.toString());
			}

			ModuleKeyDTO moduleKeyDTO = new ModuleKeyDTO(currentModule.getDomainName(), currentModule.getModuleName(), currentModule.getModuleVersion());
			if (domainName.equals(currentModule.getDomainName()))
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("domainName " + domainName + " ok");
				}

				if (logger.isDebugEnabled())
				{
					if (currentModule.getDeprecatedPolicyKeys() != null && !currentModule.getDeprecatedPolicyKeys().isEmpty())
					{
						logger.debug("imported module contains deprecated policykeys. will be converted to assignedPolicy");
					}
				}

				// key existing and/or references unknown policy?
				ModuleDTO newModule = null;
				try
				{
					newModule = createModule(currentModule, preview);
				}
				catch (InvalidExchangeFormatException e)
				{
					ignoredModules.add(moduleKeyDTO);
					String errorMessage = "Error while processing module " + currentModule.toKeyString();
					logger.error(errorMessage, e);
					continue;
				}

				try
				{
					ModuleDTO existingModule = dad.getModule(moduleKeyDTO);
					// module update necessary or number of policies different?
					if (!newModule.equalsForFhirSerice(existingModule))
					{
						if (allowUpdates)
						{
							if (existingModule.getFinalised())
							{
								// finalised, only selected changes allowed
								if (!preview)
								{
									dad.updateModuleInUse(
											existingModule.getKey(),
											newModule.getLabel(),
											newModule.getShortText(),
											newModule.getExternProperties(),
											newModule.getComment(),
											newModule.getAssignedPolicies());
								}
							}
							else
							{
								// not finalised, change all
								if (!preview)
								{
									dad.updateModule(newModule, false);
								}
							}
							logger.info("Module " + existingModule.getKey().toString() + " updated.");
							updatedModules.add(existingModule.getKey());
						}
						else
						{
							logger.info("Module " + existingModule.getKey().toString() + " ignored. AllowUpdates=false");
							ignoredModules.add(existingModule.getKey());
						}
					}
					else
					{
						logger.info("Module ignored: no changes detected: " + existingModule.toString());
						ignoredModules.add(existingModule.getKey());
					}
				}
				catch (UnknownModuleException maybe)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("module: " + currentModule.toKeyString() + " not found, creating it");
					}

					// not existing, add module
					if (!preview)
					{
						// domain has to be finalised, before modules can be added
						try
						{
							finaliseDomain(domainName);
							dad.addModule(newModule, false);
							logger.info("module " + newModule.getKey().toString() + " added");
						}
						catch (UnknownDomainException | DuplicateEntryException | InvalidVersionException | RequirementsNotFullfilledException | UnknownPolicyException e)
						{
							ignoredModules.add(moduleKeyDTO);
							String errorMessage = "Error while processing module " + currentModule.toKeyString();
							logger.error(errorMessage, e);
						}
					}
					addedModules.add(newModule.getKey());
				}
				catch (UnknownDomainException maybe)
				{
					if (!preview)
					{
						ignoredModules.add(moduleKeyDTO);
						String errorMessage = "Error while processing module " + currentModule.toKeyString();
						logger.error(errorMessage, maybe);
					}
					else
					{
						boolean found = false;
						for (DomainDTO domainDTO : addedDomains)
						{
							if (domainDTO.getName().equals(domainName))
							{
								addedModules.add(moduleKeyDTO);
								found = true;
								break;
							}
						}
						if (!found)
						{
							ignoredModules.add(moduleKeyDTO);
							String errorMessage = "Error while processing module " + currentModule.toKeyString();
							logger.error(errorMessage, maybe);
						}
					}
				}
				catch (InvalidParameterException | InvalidVersionException | DuplicateEntryException | ObjectInUseException | RequirementsNotFullfilledException
						| UnknownPolicyException e)
				{
					ignoredModules.add(moduleKeyDTO);
					String errorMessage = "Error while processing module " + currentModule.toKeyString();
					logger.error(errorMessage, e);
				}
			}
			else
			{
				ignoredModules.add(moduleKeyDTO);
				logger.error("Module key contains unexpected domain name: " + currentModule.getDomainName());
			}
			logger.info("-----------------------------------------");
		}
	}

	/**
	 *
	 * @param currentModule
	 * @param preview
	 * @return
	 * @throws InvalidExchangeFormatException
	 */
	private ModuleDTO createModule(ConsentModule currentModule, boolean preview) throws InvalidExchangeFormatException
	{
		List<Type> assignedPolicies = currentModule.getAssignedPolicies();
		Set<AssignedPolicyDTO> policies = new HashSet<>();

		try
		{
			if (currentModule.getDeprecatedPolicyKeys() != null && currentModule.getDeprecatedPolicyKeys().size() > 0)
			{

				for (String policykey : currentModule.getDeprecatedPolicyKeys())
				{
					// nur key, da restliche information durch deprecated variante fehlen
					PolicyDTO existingGicsPolicy = dad.getPolicy((PolicyKeyDTO) convertKeystringTo(PolicyKeyDTO.class, policykey));
					AssignedPolicyDTO gicsAssignedPolicyDTO = new AssignedPolicyDTO(existingGicsPolicy);
					policies.add(gicsAssignedPolicyDTO);
				}

			}
			else
			{
				policies = createAssignedPolicies(currentModule, preview, assignedPolicies);
			}
		}
		catch (UnknownDomainException e)
		{
			if (preview)
			{
				logger.debug("Due to preview mode, the specified domain does not exist yet. Skipping this policy key for module " + currentModule.getModuleName());
			}
			else
			{
				String msg = "Invalid policy domain detected in module " + currentModule.getModuleName();
				logger.error(msg);
				throw new InvalidExchangeFormatException(e.getMessage() + "; " + msg);
			}
		}
		catch (UnknownPolicyException e)
		{
			if (preview)
			{
				logger.info("The imported module contains unknow policies. Potential module update detected.");
			}
			else
			{
				String msg = "The imported module contains unknow policies. Please specify policies before adding to a module.";
				logger.error(msg);
				throw new InvalidExchangeFormatException(e.getMessage() + "; " + msg);
			}
		}
		catch (InvalidVersionException e)
		{
			String msg = "The imported module or policy contains an invalid version string:" + e.getMessage();
			logger.error(msg, e);
			throw new InvalidExchangeFormatException(e.getMessage() + "; " + msg, e);
		}

		// FMM: the finalized status of the module is intentionally ignored, see
		// https://git.icm.med.uni-greifswald.de/ths/gics-project/-/issues/357
		return new ModuleDTO(new ModuleKeyDTO(currentModule.getDomainName(), currentModule.getModuleName(), currentModule.getModuleVersion()),
				currentModule.getModuleText(), currentModule.getModuleTitle(), currentModule.getModuleComment(), currentModule.getExternProperties(),
				policies,
				currentModule.getModuleLabel(), currentModule.getModuleShortText(), false, new Date(), new Date(),
				// TODO in Klaerung ob fhirID exportiert werden muss. @bialkem
				null);
	}

	private Set<AssignedPolicyDTO> createAssignedPolicies(ConsentModule currentModule, boolean preview, List<Type> assignedPolicies) throws InvalidExchangeFormatException
	{
		Set<AssignedPolicyDTO> policies = new HashSet<>();

		for (Type typedPolicy : assignedPolicies)
		{
			AssignedConsentPolicy policy = (AssignedConsentPolicy) typedPolicy;
			PolicyKeyDTO key = (PolicyKeyDTO) convertKeystringTo(PolicyKeyDTO.class, policy.getPolicyKeyString());
			PolicyDTO existingGicsPolicy = null;
			try
			{
				existingGicsPolicy = dad.getPolicy(key);
			}
			catch (UnknownDomainException | UnknownPolicyException | InvalidVersionException maybe)
			{
				String msg = "Invalid policy " + policy.getPolicyKeyString() + " in module " + currentModule.getModuleName();
				if (!preview)
				{
					logger.error(msg, maybe);
					throw new InvalidExchangeFormatException(msg, maybe);
				}
				else
				{
					boolean found = false;
					for (PolicyKeyDTO policyKeyDTO : addedPolicies)
					{
						if (policyKeyDTO.equals(key))
						{
							existingGicsPolicy = new PolicyDTO(key);
							found = true;
							break;
						}
					}
					if (!found)
					{
						logger.error(msg, maybe);
						throw new InvalidExchangeFormatException(msg, maybe);
					}
				}
			}

			AssignedPolicyDTO gicsAssignedPolicyDTO = new AssignedPolicyDTO(existingGicsPolicy);

			if (policy.getComment() != null && !policy.getComment().isEmpty())
			{
				gicsAssignedPolicyDTO.setComment(policy.getComment());
			}

			if (policy.getExternProperties() != null && !policy.getExternProperties().isEmpty())
			{
				gicsAssignedPolicyDTO.setExternProperties(policy.getExternProperties());
			}

			try
			{
				if (policy.getExpirationProperties() != null && !policy.getExpirationProperties().isEmpty())
				{
					gicsAssignedPolicyDTO.setExpirationProperties(new ExpirationPropertiesObject(policy.getExpirationProperties()).toDTO());
				}
			}
			catch (ParseException e)
			{
				logger.debug("Invalid Expiration Format for assignedpolicy in module " + currentModule.toKeyString() + " " + e.getMessage());
				throw new InvalidExchangeFormatException(e.getMessage());
			}

			policies.add(gicsAssignedPolicyDTO);
		}

		return policies;
	}

	/**
	 * convert keystring to target type
	 *
	 * @param key
	 * @return string converted to KeyDTO for Policies, Modules or Templates
	 */
	private Object convertKeystringTo(Class<?> clazz, String key)
	{
		String[] keyArr = key.split(";");

		if (clazz.equals(PolicyKeyDTO.class))
		{
			return new PolicyKeyDTO(keyArr[0], keyArr[1], keyArr[2]);
		}

		if (clazz.equals(ModuleKeyDTO.class))
		{
			return new ModuleKeyDTO(keyArr[0], keyArr[1], keyArr[2]);
		}

		if (clazz.equals(ConsentTemplateKeyDTO.class))
		{
			return new ConsentTemplateKeyDTO(keyArr[0], keyArr[1], keyArr[2]);
		}

		return new Object();
	}

	/**
	 * @param domainName
	 * @param policies
	 * @param allowUpdates
	 * @param preview
	 *            true if the processing should be simulated and not persited
	 */
	private void processImportedPolicyInfo(String domainName, List<Type> policies, boolean allowUpdates, boolean preview)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("####################################");
		}

		if (logger.isInfoEnabled())
		{
			logger.info(policies.size() + " policies to be imported.");
		}

		for (Type t : policies)
		{
			ConsentPolicy p = (ConsentPolicy) t;
			PolicyKeyDTO keyDTO = new PolicyKeyDTO(p.getDomainName(), p.getName(), p.getVersion());
			if (domainName.equals(p.getDomainName()))
			{
				// FMM: the finalized status of the policy is intentionally ignored, see
				// https://git.icm.med.uni-greifswald.de/ths/gics-project/-/issues/357
				PolicyDTO newPolicy = new PolicyDTO(keyDTO, p.getComment(), p.getExternProperties(), p.getLabel(), false, new Date(), new Date(),
						// do not import or export fhir id
						null);
				try
				{
					// update policy if possible
					PolicyDTO existingPolicy = dad.getPolicy(keyDTO);

					if (allowUpdates)
					{
						// updates policy
						if (!existingPolicy.equalsForFhirSerice(newPolicy))
						{
							// changes detected
							if (existingPolicy.getFinalised())
							{
								// finalised=label, comment,extprop editable
								if (!preview)
								{
									dad.updatePolicyInUse(existingPolicy.getKey(), p.getLabel(), p.getExternProperties(), p.getComment());
									logger.info("Policy " + p.toKeyString() + " updated.");
								}
							}
							else
							{
								// all editable
								if (!preview)
								{
									dad.updatePolicy(newPolicy);
									logger.info("Policy " + p.toKeyString() + " updated.");
								}
							}
							updatedPolicies.add(keyDTO);
						}
						else
						{
							logger.info("no changes deteced for policy " + p.toKeyString());
							ignoredPolicies.add(existingPolicy.getKey());
						}
					}
					else
					{
						logger.info("Updates not allowed for policy " + p.toKeyString());
						ignoredPolicies.add(existingPolicy.getKey());
					}
				}
				catch (UnknownPolicyException maybe)
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("module: " + p.toKeyString() + " not found, creating it");
					}

					// not existing, add module
					if (!preview)
					{
						// domain has to be finalised, before modules can be added
						try
						{
							finaliseDomain(domainName);
							dad.addPolicy(newPolicy);
							logger.info("Policy " + p.toKeyString() + " added.");
						}
						catch (UnknownDomainException | DuplicateEntryException | InvalidVersionException | RequirementsNotFullfilledException e)
						{
							ignoredPolicies.add(keyDTO);
							String errorMessage = "Error while processing policy " + p.toKeyString();
							logger.error(errorMessage, e);
						}
					}
					addedPolicies.add(keyDTO);
				}
				catch (UnknownDomainException maybe)
				{
					if (!preview)
					{
						ignoredPolicies.add(keyDTO);
						String errorMessage = "Error while processing policy " + p.toKeyString();
						logger.error(errorMessage, maybe);
					}
					else
					{
						boolean found = false;
						for (DomainDTO domainDTO : addedDomains)
						{
							if (domainDTO.getName().equals(domainName))
							{
								addedPolicies.add(keyDTO);
								found = true;
								break;
							}
						}
						if (!found)
						{
							ignoredPolicies.add(keyDTO);
							String errorMessage = "Error while processing policy " + p.toKeyString();
							logger.error(errorMessage, maybe);
						}
					}
				}
				catch (InvalidVersionException | ObjectInUseException e)
				{
					ignoredPolicies.add(keyDTO);
					String errorMessage = "Error while processing policy " + p.toKeyString();
					logger.error(errorMessage, e);
				}
			}
			else
			{
				ignoredPolicies.add(keyDTO);
				logger.error("Policy key contains unexpected domain name: " + p.getDomainName());
			}
			if (logger.isDebugEnabled())
			{
				logger.debug("-----------------------------------------");
			}
		}
	}

	/**
	 * process domain import info and add / update domains as specified
	 *
	 * @param newDomain
	 *            imported domain info
	 * @param allowUpdates
	 *            true if update of selected domain infos is allowed
	 * @param preview
	 *            true if the processing should be simulated and not persited
	 */
	private void processImportedDomainInfo(ConsentDomain newDomain, boolean allowUpdates, boolean preview)
	{
		String logo = null;
		byte[] logoBytes = newDomain.getLogo();
		if (logoBytes != null && logoBytes.length > 0)
		{
			logo = Base64.getEncoder().encodeToString(logoBytes);
		}

		// fix properties-string problems
		newDomain.setProperties(removeBracketsAndSpaces(newDomain.getProperties()));

		// default:not finalized
		DomainDTO newDomainDTO = new DomainDTO(newDomain.getName(), newDomain.getLabel(),
				newDomain.getConsentTemplateVersionConverter(), newDomain.getModuleVersionConverter(),
				newDomain.getPolicyVersionConverter(), newDomain.getProperties(), newDomain.getComment(),
				newDomain.getExternProperties(), logo, newDomain.getSignerIdTypes(), false, new Date(), new Date(),
				/* expirationProperties intentionally will be set later (could throw a ParseException!) */
				null,
				// fhirid muss nicht exportiert oder importiert werden, allein der gics entscheidet
				null);

		// check if import domain contains QC properties
		if (!newDomainDTO.getProperties().contains("DEFAULT_QC_TYPE"))
		{
			// add default QC types
			StringBuilder properties = new StringBuilder(newDomainDTO.getProperties());
			properties.append(";").append(DEFAULT_VALID_QC_TYPES);
			properties.append(";").append(DEFAULT_INVALID_QC_TYPES);
			properties.append(";").append(DEFAULT_QC_TYPE);
			newDomainDTO.setProperties(properties.toString());
		}

		DomainDTO existingDomain = null;

		try
		{
			// the order of requesting the domain (possibly throwing an UnknownDomainException)
			// and creating an expiration properties object (possibly throwing a ParseException)
			// is important and influences the later logic: e.g. creating the e.p.o. first would leave
			// the existingDomain null which would be missed in the list of ignoredDomains afterwards
			existingDomain = dad.getDomain(newDomain.getName());
			newDomainDTO.setExpirationProperties(new ExpirationPropertiesObject(newDomain.getExpirationProperties()).toDTO());

			String propertyPrefix = "domain properties: ";
			if (existingDomain.getProperties() != null &&
					existingDomain.getProperties().startsWith(propertyPrefix))
			{
				String propertyWithoutPrefix = existingDomain.getProperties().replace(propertyPrefix, "");
				existingDomain.setProperties(propertyWithoutPrefix);
			}

			if (!existingDomain.equalsForFhirSerice(newDomainDTO))
			{
				if (allowUpdates)
				{
					if (existingDomain.getFinalised())
					{
						// update necessary, keep current property settings
						if (!preview)
						{
							dad.updateDomainInUse(existingDomain.getName(), newDomainDTO.getLabel(), newDomainDTO.getLogo(),
									newDomainDTO.getExternProperties(), newDomainDTO.getComment());
							logger.info("Domain updated: " + existingDomain.getName());
						}
					}
					// not finalised, everything editable
					else
					{
						if (!preview)
						{
							dad.updateDomain(newDomainDTO);
							logger.info("Domain updated: " + newDomainDTO.getName());
						}
					}
					updatedDomains.add(existingDomain);
				}
				// updates not allowed
				else
				{
					ignoredDomains.add(existingDomain);
					logger.warn("Updating of consent domain info not allowed. Use allowUpdates=true to force update.");
				}
			}
			else
			{
				logger.info("No changes detected. Domain ignored: " + existingDomain.getName());
				ignoredDomains.add(existingDomain);
			}
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				newDomainDTO.setExpirationProperties(new ExpirationPropertiesObject(newDomain.getExpirationProperties()).toDTO());
				if (!preview)
				{
					dad.addDomain(newDomainDTO);
					logger.info("Domain added " + newDomainDTO);
				}
				addedDomains.add(newDomainDTO);
			}
			catch (DuplicateEntryException | VersionConverterClassException | ParseException e)
			{
				// potential unreachable due to prior check if domain exists
				// potential unreachable due to specific default values in fhir
				// format for version converter classes
				// existingDomain always is null here!
				ignoredDomains.add(newDomainDTO);
				logger.error(e.getLocalizedMessage());
			}
		}
		catch (ObjectInUseException | ParseException e)
		{
			ignoredDomains.add(existingDomain);
			logger.error(e.getLocalizedMessage());
		}
	}

	private static String removeBracketsAndSpaces(String cleanMe)
	{
		// fix [ ] problem in domainproperties
		String fixed = cleanMe;
		if (StringUtils.isNotEmpty(fixed))
		{
			// remove brackets and empty spaces
			fixed = fixed.replaceAll("\\[", "");
			fixed = fixed.replaceAll("\\]", "");
			fixed = fixed.replaceAll(" ", "");
		}
		return fixed;
	}

	/**
	 * Finalize the given domain, e.g. before adding policies or modules to the dad.
	 *
	 * @param domainName
	 *            the name of the domain to finalize
	 * @throws UnknownDomainException
	 */
	public void finaliseDomain(String domainName) throws UnknownDomainException
	{
		if (!dad.getDomain(domainName).getFinalised())
		{
			logger.info("finalizing domain: " + domainName);
			checkAllowedDomain(domainName);
			dad.finaliseDomain(domainName);
		}
	}

	/**
	 * validate provided import format if fhir-parsable and correct support gics-version
	 *
	 * @param definition
	 *            definition string to be valdidated
	 * @param fileFormat
	 *            format of definition (e.g. xml or json)
	 * @return parsed and validated import info
	 * @throws InvalidExchangeFormatException
	 */

	private ExchangeFormatDefinition validateFormat(String definition, String fileFormat) throws InvalidExchangeFormatException
	{
		try
		{
			ExchangeFormatDefinition importDef = null;
			// Try to parse as fhir ressource

			if (fileFormat != null && fileFormat.equalsIgnoreCase("xml"))
			{
				importDef = getFhirContext().newXmlParser().parseResource(ExchangeFormatDefinition.class, definition);
			}
			else if (fileFormat != null && fileFormat.equalsIgnoreCase("json"))
			{
				importDef = getFhirContext().newJsonParser().parseResource(ExchangeFormatDefinition.class, definition);
			}
			else
			{
				throw new InvalidExchangeFormatException("Unsupported import fileFormat: " + fileFormat);
			}
			// compare gics version and format version
			if (importDef != null)
			{
				if (checkImportVersion(importDef.getSupportedVersion()))
				{
					// validate template names
					try
					{
						for (Type t : importDef.getTemplates())
						{
							ConsentTemplate template = (ConsentTemplate) t;

							// force calling getName() and getLabel() to ensure,
							// that these methods won't throw exceptions later
							// (in the case of duplicate names or labels when ignoring case)
							String info = "domain: '" + template.getDomainName() + "', "
									+ "name: '" + template.getName() + "', "
									+ "label: '" + template.getLabel() + "', "
									+ "version: '" + template.getVersion() + "'";

							// ensure that a template alwas has a non-empty name
							if (template.getName() == null || template.getName().isEmpty())
							{
								throw new InvalidExchangeFormatException("Missing name in template (" + info + ")");
							}
						}
						return importDef;
					}
					catch (RuntimeException e)
					{
						if (e.getCause() instanceof InvalidExchangeFormatException)
						{
							throw (InvalidExchangeFormatException) e.getCause();
						}
						throw e;
					}
				}
				else
				{
					throw new InvalidExchangeFormatException("Invalid version of import format (" + importDef.getSupportedVersion() + ").");
				}
			}
			else
			{
				throw new InvalidExchangeFormatException("Invalid import format.");
			}

		}
		catch (DataFormatException e)
		{
			throw new InvalidExchangeFormatException("Unable to parse importformat. " + fileFormat + " expected. " + e.getMessage());
		}
	}

	/**
	 * get current version of gics
	 *
	 * @return versionInfo
	 */
	String getCurrentVersion()
	{
		String version = "unable to read project version";

		String resourceName = "version.properties";
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		try
		{
			InputStream resourceStream = loader.getResourceAsStream(resourceName);
			props.load(resourceStream);

			version = props.getProperty("version");
		}
		catch (IOException e)
		{
			logger.error("Cannot read project version", e);
		}
		logger.debug("Current gics version: " + version);

		return version;
	}

	/**
	 * {@return true if the current gICS version is not lower than the specified version ignoring the third Bugfix part in the version string}
	 *
	 * @param version the version string to test for compatibility with the current version
	 */
	boolean checkImportVersion(String version)
	{
		String currentVersion = getCurrentVersion();

		try
		{
			if (new MajorMinorVersionConverter().compareRelevantParts(currentVersion, version) >= 0) // ignore maintenance part
			{
				logger.info("Version Check: success (gICS=" + currentVersion + ", format=" + version + ")");
				return true;
			}
			logger.error("Version Check: error (gICS=" + currentVersion + ", format=" + version + "). Please install newer version of gICS.");
		}
		catch (InvalidVersionException e)
		{
			logger.error("Version Check: error (gICS=" + currentVersion + ", format=" + version + "). Invalid Version format: " + version, e);
		}

		return false;
	}

	/**
	 * get current FHIR context for FHIR HAPI
	 *
	 * @return fhir context
	 */
	private FhirContext getFhirContext()
	{
		return ctx;
	}

	/**
	 * register custom FHIR types to be able to process gics datatrypes and resources with FHIR HAPI
	 * NOTE: this function is obsolete after sucessfull registration of custom fhir types under
	 * https://www.hl7.org/fhir
	 */
	private void registerFhirTypes()
	{
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentDomain.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentPolicy.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentModule.class);

		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.AssignedConsentPolicy.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate.class);

		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.resources.ExchangeFormatDefinition.class);

		if (logger.isDebugEnabled())
		{
			logger.debug("7 custom fhir types and resources registered.");
		}
	}

}
