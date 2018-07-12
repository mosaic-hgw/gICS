package org.emau.icmvc.ganimed.ttp.cm2;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ExportMode;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule;
import org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.PatientConsentStatus;
import org.emau.icmvc.magic.fhir.datatypes.ConsentDomain;
import org.emau.icmvc.magic.fhir.datatypes.ConsentModule;
import org.emau.icmvc.magic.fhir.datatypes.ConsentPolicy;
import org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate;
import org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText;
import org.emau.icmvc.magic.fhir.resources.ExchangeFormatDefinition;
import org.hl7.fhir.dstu3.model.Type;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;

@WebService(name = "gicsFhirService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(GICSFhirService.class)
@PersistenceContext(name = "gics")
/**
 * implementation of the gics-fhir-service
 *
 * @author bialkem
 *
 */
public class GICSFhirServiceImpl implements GICSFhirService
{

	private static final Logger logger = Logger.getLogger(GICSFhirServiceImpl.class);
	@EJB
	private DAO dao;
	private final FhirContext ctx = FhirContext.forDstu3();

	private static List<ModuleKeyDTO> templateModuleKeys = null;

	private static List<AssignedModuleDTO> listeAssModuleDtos = null;

	private static List<FreeTextDefDTO> listeFreeTextDtos = null;

	public GICSFhirServiceImpl()
	{}

	@Override
	public void importDefinition(String xmlFhirFormat, boolean allowUpdates)
			throws InvalidExchangeFormatException, InternalException
	{

		if (logger.isDebugEnabled())
		{
			logger.debug("###########################################");
		}

		if (logger.isInfoEnabled())
		{
			logger.info("importing received fhir import definition.");
		}

		registerFhirTypes();

		if (logger.isInfoEnabled())
		{
			logger.info("Allow Updates: " + allowUpdates);
		}

		try
		{

			ExchangeFormatDefinition importInfo = validateFormat(xmlFhirFormat);
			processImportedDomainInfo(importInfo.getDomain(), allowUpdates);
			String domainName = importInfo.getDomain().getName();

			processImportedPolicyInfo(domainName, importInfo.getPolicies(), allowUpdates);
			processImportedModuleInfo(domainName, importInfo.getModules(), allowUpdates);
			processImportedTemplateInfo(domainName, importInfo.getTemplates(), allowUpdates);

		}
		catch (UnknownDomainException | NullPointerException | DuplicateEntryException | VersionConverterClassException
				| InvalidVersionException e)
		{

			logger.error("InvalidExchangeFormatException: " + e.getMessage());
			e.printStackTrace();

			throw new InvalidExchangeFormatException(e.getMessage());
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public String exportDefinition(String domainName, ExportMode exportMode, List<String> itemList, Boolean exportLogo) throws InternalException
	{
		ExchangeFormatDefinition def = prepareExport();
		DomainDTO domain;
		try
		{
			domain = dao.getDomain(domainName);

			switch (exportMode)
			{
				case ALL:
					def = exportDefinition(domain, def, exportLogo);
					break;
				case DOMAIN:
					def = exportDomainDefinition(domain, def, exportLogo);
					break;
				case POLICIES:
					def = exportDomainDefinition(domain, def, exportLogo);
					def = exportPoliciesDefinition(domain, def, itemList);
					break;
				case MODULES:
					def = exportDomainDefinition(domain, def, exportLogo);
					def = exportModulesDefinition(domain, def, itemList);
					break;
				case TEMPLATES:
					def = exportDomainDefinition(domain, def, exportLogo);
					def = exportTemplatesDefinition(domain, def, itemList);

					break;
				default:
					logger.debug("unknown type of export");
					break;
			}
		}
		catch (UnknownDomainException | InternalException e)
		{
			throw new InternalException(e.getMessage());
		}

		return convertToExchangeFormat(def);
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
	 * @return string version of def
	 */
	private String convertToExchangeFormat(ExchangeFormatDefinition def)
	{
		return getFhirContext().newXmlParser().encodeResourceToString(def);
	}

	/**
	 * export exchangeformat containing all info about given domain, polices, modules and templates
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
	 * @throws InternalException
	 *             version invalid or conflict
	 */
	private ExchangeFormatDefinition exportDefinition(DomainDTO domain, ExchangeFormatDefinition def, Boolean exportLogo) throws UnknownDomainException, InternalException
	{
		logger.debug("Exporting ALL");
		try
		{
			def.setDomain(convertToFhirConsentDomain(domain, exportLogo));

			def.setPolicies(convertToFhirConsentPolices(dao.listPolicies(domain.getName())));

			def.setModules(convertToFhirConsentModules(dao.listModules(domain.getName())));

			def.setTemplates(convertToFhirConsentTemplates(dao.listConsentTemplates(domain.getName())));

		}
		catch (VersionConverterClassException | InvalidVersionException e)
		{
			e.printStackTrace();
			throw new InternalException("Unable to create export format MODE.ALL " + e.getMessage());
		}
		return def;
	}

	/**
	 * export definition of policies for given domain, exports only a subset of policies as
	 * specified in itemlist
	 * 
	 * @param domain
	 *            domaindto
	 * @param def
	 *            target
	 * @param itemList
	 * @return exchangeformat with policy info
	 * @throws InternalException
	 */
	private ExchangeFormatDefinition exportPoliciesDefinition(DomainDTO domain, ExchangeFormatDefinition def, List<String> itemList) throws InternalException
	{
		logger.debug("Exporting POLICIES");
		try
		{
			List<PolicyDTO> policies = new ArrayList<PolicyDTO>();

			if (itemList != null && itemList.size() > 0)
			{
				for (String keystring : itemList)
				{
					PolicyKeyDTO key = (PolicyKeyDTO) convertKeystringTo(PolicyKeyDTO.class, keystring);
					policies.add(dao.getPolicy(key));
				}
			}
			else
			{
				policies = dao.listPolicies(domain.getName());
			}

			def.setPolicies(convertToFhirConsentPolices(policies));
		}
		catch (UnknownPolicyException | UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
		{
			e.printStackTrace();
			throw new InternalException("Unable to create export format MODE.POLICIES: " + e.getMessage());
		}

		return def;
	}

	/**
	 * export definition of modules and relevant policies for given domain, , exports only a subset
	 * of modules as specified in itemlist
	 * 
	 * @param domain
	 *            domaindto
	 * @param def
	 *            target
	 * @param itemList
	 * @return exchangeformat with module info
	 * @throws InternalException
	 */
	private ExchangeFormatDefinition exportModulesDefinition(DomainDTO domain, ExchangeFormatDefinition def, List<String> itemList) throws InternalException
	{
		logger.debug("Exporting MODULES");
		try
		{

			List<ModuleDTO> modules = new ArrayList<ModuleDTO>();
			// get relevant policies only
			List<PolicyDTO> policies = new ArrayList<PolicyDTO>();

			if (itemList != null && itemList.size() > 0)
			{
				for (String keystring : itemList)
				{
					ModuleKeyDTO key = (ModuleKeyDTO) convertKeystringTo(ModuleKeyDTO.class, keystring);
					modules.add(dao.getModule(key));
				}
			}
			else
			{
				modules = dao.listModules(domain.getName());
			}

			// get relevant policies only

			for (ModuleDTO m : modules)
			{
				for (PolicyDTO modulePolicy : m.getPolicies())
				{
					if (!policies.contains(modulePolicy))
					{
						policies.add(modulePolicy);
					}
				}
			}

			def.setModules(convertToFhirConsentModules(modules));
			def.setPolicies(convertToFhirConsentPolices(policies));
		}
		catch (UnknownDomainException | UnknownModuleException | VersionConverterClassException | InvalidVersionException e)
		{
			e.printStackTrace();
			throw new InternalException("Unable to create export format MODE.MODULES: " + e.getMessage());
		}
		return def;
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
	private ExchangeFormatDefinition exportTemplatesDefinition(DomainDTO domain, ExchangeFormatDefinition def, List<String> itemList) throws InternalException
	{
		logger.debug("Exporting TEMPLATES");
		try
		{
			List<ConsentTemplateDTO> templates = new ArrayList<ConsentTemplateDTO>();

			if (itemList != null && itemList.size() > 0)
			{
				for (String keystring : itemList)
				{
					ConsentTemplateKeyDTO key = (ConsentTemplateKeyDTO) convertKeystringTo(ConsentTemplateKeyDTO.class, keystring);
					templates.add(dao.getConsentTemplate(key));
				}
			}
			else
			{
				templates = dao.listConsentTemplates(domain.getName());
			}

			// get relevant modules
			List<ModuleDTO> modules = new ArrayList<ModuleDTO>();
			// get relevant policies only
			List<PolicyDTO> policies = new ArrayList<PolicyDTO>();

			for (ConsentTemplateDTO t : templates)
			{
				// add relevant modules
				for (AssignedModuleDTO assignedModule : t.getAssignedModules())
				{
					ModuleDTO m = assignedModule.getModule();
					if (!modules.contains(m))
					{
						modules.add(m);
					}
					// add relevant policies
					for (PolicyDTO p : m.getPolicies())
					{
						if (!policies.contains(p))
						{
							policies.add(p);
						}
					}
				}
			}
			def.setTemplates(convertToFhirConsentTemplates(templates));
			def.setModules(convertToFhirConsentModules(modules));
			def.setPolicies(convertToFhirConsentPolices(policies));
		}
		catch (UnknownDomainException | UnknownConsentTemplateException | VersionConverterClassException | InvalidVersionException e)
		{
			e.printStackTrace();
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
	private ExchangeFormatDefinition exportDomainDefinition(DomainDTO domain, ExchangeFormatDefinition def, Boolean exportLogo)
	{
		logger.debug("Exporting DOMAIN");
		return def.setDomain(convertToFhirConsentDomain(domain,exportLogo));
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
				fhirTempl.setComment(gicsTempl.getComment());
			if (gicsTempl.getTitle() != null)
				fhirTempl.setTitle(gicsTempl.getTitle());
			if (gicsTempl.getFooter() != null)
				fhirTempl.setFooter(gicsTempl.getFooter());
			if (gicsTempl.getHeader() != null)
				fhirTempl.setHeader(gicsTempl.getHeader());
			
			if (gicsTempl.getPropertiesString() != null)
				fhirTempl.setProperties(gicsTempl.getPropertiesString());
			if (gicsTempl.getExternProperties() != null)
				fhirTempl.setExternProperties(gicsTempl.getExternProperties());
			
			if (gicsTempl.getType() != null)
				fhirTempl.setType(String.valueOf(gicsTempl.getType()));			

			// Freetexts
			List<FreeTextDefDTO> gicsFreeTexts = gicsTempl.getFreeTextDefs();

			for (FreeTextDefDTO gicsFt : gicsFreeTexts)
			{

				ConsentTemplateFreeText fhirFt = new ConsentTemplateFreeText();

				if (gicsFt.getName() != null)
					fhirFt.setName(gicsFt.getName());

				fhirFt.setRequired(gicsFt.getRequired());

				if (gicsFt.getComment() != null)
					fhirFt.setComment(gicsFt.getComment());
				if (gicsFt.getConverterString() != null)
					fhirFt.setConverterString(gicsFt.getConverterString());

				if (gicsFt.getType() != null)
					fhirFt.setType(convertToFhirFreeTextType(gicsFt.getType()));

				fhirTempl.addFreetext(fhirFt);
			}

			// Liste AssModule
			List<AssignedModuleDTO> gicsAssModules = gicsTempl.getAssignedModules();

			for (AssignedModuleDTO gicsModul : gicsAssModules)
			{

				AssignedConsentModule fhirModul = new AssignedConsentModule();

				fhirModul.setModuleKey(gicsModul.getModule().getKey().getDomainName() + ";"
						+ gicsModul.getModule().getKey().getName() + ";" + gicsModul.getModule().getKey().getVersion());

				fhirModul.setOrderNumber(gicsModul.getOrderNumber());

				fhirModul.setMandatory(gicsModul.getMandatory());

				if (gicsModul.getComment() != null)
					fhirModul.setComment(gicsModul.getComment());

				// available patientconsenstatus
				List<PatientConsentStatus> displayOptions = new ArrayList<>();

				for (ConsentStatus gicsStatus : gicsModul.getDisplayCheckboxes())
				{
					displayOptions.add(convertToFhirConsentStatus(gicsStatus));
				}

				fhirModul.setDisplayCheckBoxes(displayOptions);

				if (gicsModul.getDefaultConsentStatus() != null)
					fhirModul.setDefaultConsentStatus(convertToFhirConsentStatus(gicsModul.getDefaultConsentStatus()));

				fhirTempl.addAssignedModule(fhirModul);
			}

			converted.add(fhirTempl);
		}

		return converted;
	}

	/**
	 * convert gics FreeTextType to fhir format FreeTextType
	 * 
	 * @param type
	 * @return
	 */
	private org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType convertToFhirFreeTextType(FreeTextType gicsType)
	{

		switch (gicsType)
		{
			case String:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.String;

			case Date:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.Date;

			case Boolean:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.Boolean;

			case Integer:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.Integer;

			case Double:
				return org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType.Double;

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

			case NOT_CHOOSEN:
				return PatientConsentStatus.NOT_CHOOSEN;

			case REVOKED:
				return PatientConsentStatus.REVOKED;

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
	 * convert gics modules to gics fhir modules
	 * 
	 * @param listModules
	 * @return converted modules
	 */
	private List<Type> convertToFhirConsentModules(List<ModuleDTO> listModules)
	{
		List<Type> converted = new ArrayList<>();

		for (ModuleDTO m : listModules)
		{

			List<String> policyKeys = new ArrayList<>();

			for (PolicyDTO p : m.getPolicies())
			{
				policyKeys.add(p.getKey().getDomainName() + ";" + p.getKey().getName() + ";" + p.getKey().getVersion());
			}

			ConsentModule cm = new ConsentModule();

			cm.setModuleName(m.getKey().getName());
			cm.setModuleVersion(m.getKey().getVersion());
			cm.setDomainName(m.getKey().getDomainName());
			cm.setPolicyKeys(policyKeys);
			if (m.getComment() != null)
			{
				cm.setModuleComment(m.getComment());
			}
			
			if (m.getExternProperties() != null)
			{
				cm.setExternProperties(m.getExternProperties());
			}

			if (m.getText() != null)
			{
				cm.setModuleText(m.getText());
			}

			if (m.getTitle() != null)
			{
				cm.setModuleTitle(m.getTitle());
			}

			converted.add(cm);
		}

		return converted;
	}

	/**
	 * convert gics policies to gics fhir policies
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

			converted.add(cp);
		}

		return converted;
	}

	private ConsentDomain convertToFhirConsentDomain(DomainDTO existingDomain, Boolean exportLogo)
	{

		ConsentDomain convert = new ConsentDomain();

		convert.setName(existingDomain.getName());
		if (existingDomain.getComment() != null)
			convert.setComment(existingDomain.getComment());
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
		
		if(existingDomain.getExternProperties()!=null)
			convert.setExternProperties(existingDomain.getExternProperties());
		
		if(existingDomain.getProperties()!=null)
			convert.setProperties(existingDomain.getProperties());

		return convert;
	}

	/**
	 * 
	 * @param domainName
	 * @param templates
	 * @param allowUpdates
	 * @throws InvalidExchangeFormatException
	 * @throws UnknownDomainException
	 * @throws VersionConverterClassException
	 * @throws InvalidVersionException
	 */
	private void processImportedTemplateInfo(String domainName, List<Type> templates, boolean allowUpdates)
			throws InvalidExchangeFormatException, InvalidVersionException, VersionConverterClassException,
			UnknownDomainException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("####################################");
		}

		if (logger.isInfoEnabled())
		{
			logger.info(templates.size() + " templates to be imported.");
		}

		// TODO testen domain name mismatch

		for (Type t : templates)
		{
			org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate importedTemplate = (org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate) t;

			if (logger.isDebugEnabled())
			{
				logger.debug("Importing template: " + importedTemplate.toString());
			}

			// check domain name
			if (domainName.equals(importedTemplate.getDomainName()))
			{

				if (logger.isDebugEnabled())
				{
					logger.debug("domainName " + domainName + " ok");
				}

				ConsentTemplateKeyDTO importTemplateKeyDTO = null;

				try
				{

					// check assigned modules existing?
					List<Type> templateModules = importedTemplate.getAssignedModules();

					templateModuleKeys = new ArrayList<>();

					for (Type module : templateModules)
					{
						AssignedConsentModule checkModule = (AssignedConsentModule) module;
						ModuleDTO m = dao.getModule(
								(ModuleKeyDTO) convertKeystringTo(ModuleKeyDTO.class, checkModule.getModuleKey()));
						templateModuleKeys.add(m.getKey());
					}

					// convert imported freetext def to dto-list

					listeFreeTextDtos = new ArrayList<FreeTextDefDTO>();

					int pos = 0;

					for (Type current : importedTemplate.getFreetextDefs())
					{

						ConsentTemplateFreeText ft = (ConsentTemplateFreeText) current;
						logger.debug(FreeTextType.valueOf(ft.getType().toString()));

						listeFreeTextDtos.add(new FreeTextDefDTO(ft.getName(), ft.getRequired(),
								FreeTextType.valueOf(ft.getType().toString()), ft.getConverterString(), pos++,
								ft.getComment()));

					}

					// convert imported assigned modules to dtos

					listeAssModuleDtos = new ArrayList<AssignedModuleDTO>();

					for (Type current : importedTemplate.getAssignedModules())
					{

						AssignedConsentModule acm = (AssignedConsentModule) current;
						ModuleKeyDTO mkdto = (ModuleKeyDTO) convertKeystringTo(ModuleKeyDTO.class, acm.getModuleKey());

						// list display checkboxes
						List<ConsentStatus> displayCheckboxes = new ArrayList<ConsentStatus>();

						for (PatientConsentStatus status : acm.getDisplayCheckBoxes())
						{

							logger.debug("display consent status " + status.toString());
							displayCheckboxes.add(ConsentStatus.valueOf(status.toString()));

						}

						try
						{
							AssignedModuleDTO gicsAssMod = new AssignedModuleDTO();
							gicsAssMod.setModule(dao.getModule(mkdto));
							gicsAssMod.setMandatory(acm.getMandatory());
							gicsAssMod.setDisplayCheckboxes(displayCheckboxes);
							gicsAssMod.setOrderNumber(acm.getOrderNumber());

							if (acm.getComment() != null)
								gicsAssMod.setComment(acm.getComment());
							if (acm.getDefaultConsentStatus() != null)
								gicsAssMod.setDefaultConsentStatus(ConsentStatus.valueOf(acm.getDefaultConsentStatus()));

							listeAssModuleDtos.add(gicsAssMod);

							// parent ass modules are not supported
							// external properties are not supported

						}
						catch (VersionConverterClassException | InvalidVersionException | UnknownModuleException e)
						{

							// nothing should be thrown at this point
							e.printStackTrace();
						}
					}

					// check template key already existing?

					importTemplateKeyDTO = (ConsentTemplateKeyDTO) convertKeystringTo(ConsentTemplateKeyDTO.class,
							importedTemplate.toKeyString());
					ConsentTemplateDTO existingTemplate = dao.getConsentTemplate(importTemplateKeyDTO);

					if (allowUpdates)
					{

						// template already in use?
						List<ConsentLightDTO> consentedTemplates = dao
								.getAllConsentsForConsentTemplate(existingTemplate.getKey());

						if (!consentedTemplates.isEmpty())
						{

							if (logger.isInfoEnabled())
							{
								logger.info("Unable to update template " + importedTemplate.toKeyString()
										+ ". Template already in use.");
							}

						}
						else
						{

							// not in use, proceed
							if (logger.isDebugEnabled())
							{
								logger.debug("updating template details for " + importedTemplate.toKeyString());
							}

							// change further details if necessary
							if ((existingTemplate.getTitle() != null && !existingTemplate.getTitle().equals(importedTemplate.getTitle()))
									|| (existingTemplate.getPropertiesString() != null && !existingTemplate.getPropertiesString().equals(importedTemplate.getProperties()))
									|| (existingTemplate.getComment() != null && !existingTemplate.getComment().equals(importedTemplate.getComment()))
									|| (existingTemplate.getFooter() != null && !existingTemplate.getFooter().equals(importedTemplate.getFooter()))
									|| (existingTemplate.getHeader() != null && !existingTemplate.getHeader().equals(importedTemplate.getHeader()))
									|| (existingTemplate.getExternProperties() != null && !existingTemplate.getHeader().equals(importedTemplate.getExternProperties())))
							{

								

								// TODO scanbase64 is not supported at this
								// moment

								// TODO scanFileType is not supported at
								// this
								// moment

							
								dao.deleteConsentTemplate(existingTemplate.getKey());
								dao.addConsentTemplate(

										new ConsentTemplateDTO(importTemplateKeyDTO, importedTemplate.getTitle(),
												importedTemplate.getProperties(), importedTemplate.getComment(),												
												importedTemplate.getExternProperties(),												
												ConsentTemplateType.valueOf(String.valueOf(importedTemplate.getType())),

												importedTemplate.getHeader(), importedTemplate.getFooter(),

												// TODO add scanbase64 here
												existingTemplate.getScanBase64(),
												// TODO add scanFileType here
												existingTemplate.getScanFileType(), listeAssModuleDtos, listeFreeTextDtos));

							}

							if (logger.isDebugEnabled())
							{
								logger.debug("Template " + importedTemplate.toKeyString() + " updated.");
							}
						}

					}

				}
				catch (UnknownDomainException e)
				{
					throw new InvalidExchangeFormatException(
							"template key contains unexpected domainname: " + importedTemplate.getDomainName());

				}
				catch (UnknownConsentTemplateException e)
				{
					// common case -> add it
					if (importTemplateKeyDTO != null)
					{

						try
						{
							dao.addConsentTemplate(

									new ConsentTemplateDTO(importTemplateKeyDTO, importedTemplate.getTitle(),
											importedTemplate.getProperties(), importedTemplate.getComment(),

											importedTemplate.getExternProperties(),
											
											ConsentTemplateType.valueOf(String.valueOf(importedTemplate.getType())),

											importedTemplate.getHeader(), importedTemplate.getFooter(),

											// TODO add scanbase64 here
											"",
											// TODO add scanFileType here
											"", listeAssModuleDtos, listeFreeTextDtos));

							logger.info("template " + importedTemplate.toKeyString() + " added.");
						}
						catch (Exception e1)
						{
							throw new InvalidExchangeFormatException("Unable to add consent template "
									+ importedTemplate.toKeyString() + " " + e1.getMessage());
						}

					}

				}
				catch (InvalidVersionException e)
				{
					throw new InvalidExchangeFormatException(
							"template key contains unexpected Version Format: " + e.getMessage());

				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);

					throw new InternalErrorException(e.getMessage());
				}

			}
			else
			{

				throw new InvalidExchangeFormatException(
						"template key contains unexpected domainname: " + importedTemplate.getDomainName());
			}
			if (logger.isDebugEnabled())
			{
				logger.debug("-----------------------------------------");
			}
		}
	}

	/**
	 * 
	 * @param domainName
	 * @param modules
	 * @param allowUpdates
	 * @throws InvalidExchangeFormatException
	 */
	private void processImportedModuleInfo(String domainName, List<Type> modules, boolean allowUpdates)
			throws InvalidExchangeFormatException
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

			if (domainName.equals(currentModule.getDomainName()))
			{

				if (logger.isDebugEnabled())
				{
					logger.debug("domainName " + domainName + " ok");
				}

				
				// key existing?
				try
				{
					ModuleDTO existingModule = dao.getModule(new ModuleKeyDTO(currentModule.getDomainName(),
							currentModule.getModuleName(), currentModule.getModuleVersion()));
					if (allowUpdates)
					{
						ModuleDTO newModule = createModule(currentModule);
						if (!newModule.equals(existingModule))
						{
							try
							{
								dao.deleteModule(existingModule.getKey());
								dao.addModule(newModule);
								if (logger.isDebugEnabled())
								{
									logger.debug("module " + newModule.getKey().toString() + " updated");
								}

							}
							catch (ObjectInUseException e)
							{
								logger.warn(existingModule + " is in use and therefore can't be altered or updated.");
							}
							catch (UnknownDomainException | UnknownPolicyException | DuplicateEntryException
									| VersionConverterClassException | InvalidVersionException e)
							{
								logger.error("Updating module failed: " + currentModule.toKeyString(), e);
								throw new InvalidExchangeFormatException("Unable to update module: "
										+ currentModule.toKeyString() + ", " + e.getMessage());
							}
						}
					}
				}
				catch (UnknownModuleException maybe)
				{
					ModuleDTO newModule = createModule(currentModule);
					try
					{
						dao.addModule(newModule);
						if (logger.isDebugEnabled())
						{
							logger.debug("module " + newModule.getKey().toString() + " added");
						}

					}
					catch (InvalidVersionException | VersionConverterClassException | DuplicateEntryException
							| UnknownDomainException | UnknownPolicyException e)
					{
						logger.error("Adding module failed: " + currentModule.toKeyString(), e);
						throw new InvalidExchangeFormatException(
								"Unable to add module: " + currentModule.toKeyString() + ", " + e.getMessage());
					}
				}
				catch (UnknownDomainException | VersionConverterClassException | InvalidVersionException e)
				{
					logger.error("Error while retriving module: " + currentModule.toKeyString(), e);
					throw new InvalidExchangeFormatException(
							"Unable to get module: " + currentModule.toKeyString() + ", " + e.getMessage());
				}
			}
			else
			{
				throw new InvalidExchangeFormatException(
						"module key contains unexpected domainname: " + currentModule.getDomainName());
			}

			if (logger.isDebugEnabled())
			{
				logger.debug("-----------------------------------------");
			}
		}
	}

	private ModuleDTO createModule(ConsentModule currentModule) throws InvalidExchangeFormatException
	{
		List<String> policyKeyStrings = currentModule.getPolicyKeys();
		List<PolicyDTO> policies = new ArrayList<PolicyDTO>();

		for (String keyString : policyKeyStrings)
		{
			try
			{
				policies.add(dao.getPolicy((PolicyKeyDTO) convertKeystringTo(PolicyKeyDTO.class, keyString)));
			}
			catch (UnknownDomainException | UnknownPolicyException | VersionConverterClassException
					| InvalidVersionException e)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("One or more policy key(s) are not available for the module "
							+ currentModule.toKeyString() + " " + e.getMessage());
				}
				throw new InvalidExchangeFormatException(e.getMessage());
			}
		}
		return new ModuleDTO(
				new ModuleKeyDTO(currentModule.getDomainName(), currentModule.getModuleName(),
						currentModule.getModuleVersion()),
				currentModule.getModuleText(), currentModule.getModuleTitle(), currentModule.getModuleComment(), currentModule.getExternProperties(),
				policies);
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
	 * 
	 * @param domainName
	 * @param policies
	 * @param allowUpdates
	 * @throws InvalidExchangeFormatException
	 */
	private void processImportedPolicyInfo(String domainName, List<Type> policies, boolean allowUpdates)
			throws InvalidExchangeFormatException, UnknownDomainException, DuplicateEntryException,
			VersionConverterClassException, InvalidVersionException
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
			if (domainName.equals(p.getDomainName()))
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("domainName " + domainName + " ok");
				}

				boolean updatePerformed = false;
				
				// key existing?
				try
				{

					// hier mit
					PolicyDTO existingPolicy = dao
							.getPolicy(new PolicyKeyDTO(p.getDomainName(), p.getName(), p.getVersion()));

					if (allowUpdates)
					{

						dao.updatePolicy(existingPolicy.getKey(), p.getExternProperties(), p.getComment());

						updatePerformed = true;

						// Testen ob das klappt oder ob löschen/neu besser ist
					}

					if (updatePerformed)
					{
						if (logger.isDebugEnabled())
						{
							logger.debug("Policy " + existingPolicy.getKey().toString() + " updated.");
						}
					}

				}
				catch (UnknownPolicyException e)
				{

					// create new policy though it now exists
					dao.addPolicy(new PolicyDTO(new PolicyKeyDTO(p.getDomainName(), p.getName(), p.getVersion()),
							p.getComment(), p.getExternProperties()));
					if (logger.isDebugEnabled())
					{
						logger.debug("Policy " + p.toKeyString() + " added.");
					}

				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					throw new InvalidExchangeFormatException(
							"Unable to add policy: " + p.toKeyString() + ", " + e1.getMessage());
				}

			}
			else
			{
				// TODO testen domain name mismatch
				throw new InvalidExchangeFormatException(
						"Policy key contains unexpected domainname: " + p.getDomainName());
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
	 * @param domain
	 *            imported domain info
	 * @param allowUpdates
	 *            true if update of selected domain infos is allowed
	 * 
	 */
	private void processImportedDomainInfo(ConsentDomain newDomain, boolean allowUpdates)
	{

		boolean updatesPerformed = false;

		String newLabel = newDomain.getLabel();
		String newComment = newDomain.getComment();

		try
		{
			// domain already exists?
			DomainDTO existingDomain = dao.getDomain(newDomain.getName());
			Boolean labelChanged = !Objects.equals(newLabel, existingDomain.getLabel());
			Boolean commentChanged = !Objects.equals(newComment, existingDomain.getComment());
			Boolean externalPropChanged = !Objects.equals(newDomain.getExternProperties(), existingDomain.getExternProperties());

			if (logger.isDebugEnabled())
			{
				logger.debug("allowUpdates = " + allowUpdates + "; domainLabelChanged = " + labelChanged + "; domainCommentChanged = " + commentChanged);
			}

			String newLogo = null;
			byte[] logoBytes = newDomain.getLogo();
			if (logoBytes != null && logoBytes.length > 0)
			{
				newLogo = Base64.getEncoder().encodeToString(logoBytes);
			}
			String existingLogo = existingDomain.getLogo();
			Boolean logoChanged = !Objects.equals(newLogo, existingLogo);
			

			// differences in label, comment or logo
			if (labelChanged || commentChanged || logoChanged ||externalPropChanged)
			{
				// updates allowed
				if (allowUpdates)
				{
					// update necessary, keep current property settings
					dao.updateDomain(existingDomain.getName(), newLabel, newLogo, newDomain.getExternProperties(), newComment);
					updatesPerformed = true;
				}
				// updates not allowed
				else
				{
					logger.warn("Updating of consent domain info not allowed. Use allowUpdates=true to force update.");
				}
			}

		}
		catch (UnknownDomainException e)
		{
			// add new domain
			String logo = null;
			byte[] logoBytes = newDomain.getLogo();
			if (logoBytes != null && logoBytes.length > 0)
			{
				logo = Base64.getEncoder().encodeToString(logoBytes);
			}

			try
			{

				dao.addDomain(
						new DomainDTO(newDomain.getName(), newLabel, newDomain.getConsentTemplateVersionConverter(),
								newDomain.getModuleVersionConverter(), newDomain.getPolicyVersionConverter(),
								newDomain.getProperties(), newComment, newDomain.getExternProperties(), logo, newDomain.getSignerIdTypes()));

			}
			catch (DuplicateEntryException e1)
			{
				// potential unreachable due to prior check if domain exists
				e1.printStackTrace();
			}
			catch (VersionConverterClassException e1)
			{
				// potential unreachable due to specific default values in fhir
				// format for version converter classes
				e1.printStackTrace();
			}
		}

		if (updatesPerformed)
		{
			if (logger.isInfoEnabled())
			{
				logger.info("Domain information for domain '" + newDomain.getName() + "' successfully updated.");
			}
		}
	}

	/**
	 * validate provided import format if fhir-parsable and correct support
	 * gics-version
	 * 
	 * @param fhirFormat
	 *            formatstring to be valdidated
	 * @return parsed and validated import info
	 * @throws InvalidExchangeFormatException
	 * @throws UnsupportedEncodingException
	 */

	private ExchangeFormatDefinition validateFormat(String fhirFormat)
			throws InvalidExchangeFormatException, UnsupportedEncodingException
	{
		try
		{
			// Try to parse as fhir ressource
			ExchangeFormatDefinition importDef = getFhirContext().newXmlParser()
					.parseResource(ExchangeFormatDefinition.class, fhirFormat);

			// compare gics version and format version
			if (checkImportVersion(importDef.getSupportedVersion()))
			{
				return importDef;
			}

			throw new InvalidExchangeFormatException("Unsupported version of import format.");

		}
		catch (DataFormatException e)
		{
			throw new InvalidExchangeFormatException("Unable to parse importformat. " + e.getMessage());
		}
	}

	/**
	 * get current version of gics
	 * 
	 * @return versionInfo
	 */
	private String getCurrentVersion()
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
			e.printStackTrace();
		}

		logger.debug("Current gics version: " + version);

		return version;
	}


	/**
	 * check if the given version info corresponds
	 * 
	 * @param versionInfo
	 * @return
	 */
	private boolean checkImportVersion(String versionInfo)
	{
		String version = getCurrentVersion();

		if (logger.isDebugEnabled() && version != null && versionInfo != null)
		{
			logger.debug("import format version: " + versionInfo);

			logger.debug("current version of gics: " + version);

			if (version.startsWith(versionInfo.substring(0, versionInfo.lastIndexOf("."))))
			{
				logger.debug("import version ok");
				return true;
			}

			logger.debug("import version not ok");
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
	 * register custom FHIR types to be able to process gics datatrypes and
	 * resources with FHIR HAPI NOTE: this function is obsolete after sucessfull
	 * registration of custom fhir types under https://www.hl7.org/fhir
	 */
	private void registerFhirTypes()
	{
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentDomain.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentPolicy.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentModule.class);

		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.AssignedConsentModule.class);
		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.datatypes.ConsentTemplate.class);

		getFhirContext().registerCustomType(org.emau.icmvc.magic.fhir.resources.ExchangeFormatDefinition.class);

		if (logger.isDebugEnabled())
		{
			logger.debug("7 custom fhir types and resources registered.");
		}
	}


}
