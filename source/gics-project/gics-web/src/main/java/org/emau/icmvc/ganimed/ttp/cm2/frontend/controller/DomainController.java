package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeAction;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeError;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeField;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeOccurrence;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCTypeStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.TimeProperty;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.Versions;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;

import static org.emau.icmvc.ganimed.ttp.cm2.util.SystemKeys.SYSPROP_CHECK_POLICY_CHANGED_AT_TIME;
import static org.emau.icmvc.ganimed.ttp.cm2.util.SystemKeys.SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES;

/**
 * Backing Bean for Domains View
 *
 * @author Arne Blumentritt
 */
@ViewScoped
@Named("domainController")
public class DomainController extends AbstractConsentController
{
	@Serial
	private static final long serialVersionUID = 1285364203849974795L;
	private DomainDTO selectedDomain;

	private List<QCType> qcTypes;
	private QCType selectedQcType;
	private boolean selectedQcTypeIsNew;

	private List<QCProblemType> qcProblemTypes;
	private QCProblemType selectedQcProblemType;
	private boolean selectedQcProblemTypeIsNew;
	private String selectedQcProblemTypeIdPlaceholder;
	private Map<String, String> selectedQcProblemTypeLabelPlaceholder = new HashMap<>();

	private List<QCProblemTypeAction> qcProblemTypeActions;
	private QCProblemTypeAction selectedQcProblemTypeAction;
	private boolean selectedQcProblemTypeActionIsNew;

	// Versioning options
	private Boolean versionLevelDetailed;

	// Additional properties
	private Boolean scanMandatory;
	private int scanSizeLimit;
	private Boolean revokeIsPermanent;
	private Boolean useLatestVersion;
	private Boolean sendNotificationsWeb;
	private Boolean sendNotificationsOnPolicyValidityChanged;
	private String newSignerIdType;
	private TimeProperty expiration;
	private Boolean statisticDocumentDetails;
	private Boolean statisticPolicyDetails;
	private boolean enableChromePdfExport;
	private String chromedriverPath;
	private String embeddedCss;

	private static final Pattern NC_NAME_PATTERN_WITH_EMPTY = Pattern.compile("([A-Za-z_][A-Za-z0-9_\\-.]*)?");

	@PostConstruct
	private void init()
	{
		pageMode = PageMode.READ;
	}

	public void onNew()
	{
		selectedDomain = new DomainDTO();
		selectedDomain.setSignerIdTypes(new ArrayList<>());
		selectedDomain.setCtVersionConverter(Versions.MAJOR_MINOR);
		selectedDomain.setModuleVersionConverter(Versions.MAJOR_MINOR);
		selectedDomain.setPolicyVersionConverter(Versions.MAJOR_MINOR);

		scanMandatory = false;
		scanSizeLimit = 10;
		revokeIsPermanent = false;
		useLatestVersion = false;
		sendNotificationsWeb = false;
		sendNotificationsOnPolicyValidityChanged = false;
		expiration = new TimeProperty();
		statisticDocumentDetails = true;
		statisticPolicyDetails = true;
		versionLevelDetailed = false;
		enableChromePdfExport = false;

		QCType notChecked = new QCType(NOT_CHECKED, QCTypeStatus.VALID);
		notChecked.setLabel("de", getBundle().getString("model.consent.qc.type.not_checked"));
		notChecked.setLabel("en", getBundleFor("en").getString("model.consent.qc.type.not_checked"));
		QCType checkedNoFaults = new QCType(CHECKED_NO_FAULTS, QCTypeStatus.VALID);
		checkedNoFaults.setLabel("de", getBundle().getString("model.consent.qc.type.checked_no_faults"));
		checkedNoFaults.setLabel("en", getBundleFor("en").getString("model.consent.qc.type.checked_no_faults"));
		QCType checkedMinorFaults = new QCType(CHECKED_MINOR_FAULTS, QCTypeStatus.VALID);
		checkedMinorFaults.setLabel("de", getBundle().getString("model.consent.qc.type.checked_minor_faults"));
		checkedMinorFaults.setLabel("en", getBundleFor("en").getString("model.consent.qc.type.checked_minor_faults"));
		QCType checkedMajorFaults = new QCType(CHECKED_MAJOR_FAULTS, QCTypeStatus.INVALID);
		checkedMajorFaults.setLabel("de", getBundle().getString("model.consent.qc.type.checked_major_faults"));
		checkedMajorFaults.setLabel("en", getBundleFor("en").getString("model.consent.qc.type.checked_major_faults"));
		QCType invalidated = new QCType(INVALIDATED, QCTypeStatus.INVALID);
		invalidated.setLabel("de", getBundle().getString("model.consent.qc.type.invalidated"));
		invalidated.setLabel("en", getBundleFor("en").getString("model.consent.qc.type.invalidated"));
		qcTypes = new ArrayList<>(Arrays.asList(notChecked, checkedNoFaults, checkedMinorFaults, checkedMajorFaults, invalidated));
		selectedDomain.getConfig().getQualityControlConfig().setDefaultType(notChecked);
		qcProblemTypes = new ArrayList<>();
		qcProblemTypeActions = new ArrayList<>();

		pageMode = PageMode.NEW;
	}

	public void onEdit(DomainDTO domain)
	{
		selectedDomain = domain;
		scanMandatory = selectedDomain.getConfig().getScansConfig().isMandatory();
		scanSizeLimit = selectedDomain.getConfig().getScansConfig().getSizeLimit() / 1024 / 1024;
		revokeIsPermanent = selectedDomain.getConfig().getPoliciesConfig().isPermanentRevoke();
		useLatestVersion = selectedDomain.getConfig().getPoliciesConfig().isTakeHighestVersionInsteadOfNewest();
		sendNotificationsWeb = selectedDomain.getConfig().getNotificationsConfig().isSendFromWeb();
		sendNotificationsOnPolicyValidityChanged = selectedDomain.getConfig().getNotificationsConfig().isSendPolicyValidityChanged();
		statisticDocumentDetails = selectedDomain.getConfig().getStatisticConfig().isCalculateDocumentDetails();
		statisticPolicyDetails = selectedDomain.getConfig().getStatisticConfig().isCalculatePolicyDetails();
		enableChromePdfExport = selectedDomain.getConfig().getApplicationConfig().isEnableChromePdfExport();
		chromedriverPath = selectedDomain.getConfig().getApplicationConfig().getChromedriverPath();
		embeddedCss = selectedDomain.getConfig().getApplicationConfig().getEmbeddedCss();
		expiration = new TimeProperty();
		expiration.getEditTimeProperty().setExpirationProperties(selectedDomain.getExpirationProperties());
		qcTypes = new ArrayList<>(selectedDomain.getConfig().getQualityControlConfig().getTypes());
		qcProblemTypes = new ArrayList<>(selectedDomain.getConfig().getQualityControlConfig().getProblemTypes());
		qcProblemTypeActions = new ArrayList<>(selectedDomain.getConfig().getQualityControlConfig().getProblemTypeActions());

		versionLevelDetailed = true;
		pageMode = PageMode.EDIT;
	}

	public void onSave()
	{
		if (StringUtils.isNotEmpty(newSignerIdType))
		{
			selectedDomain.getSignerIdTypes().add(newSignerIdType);
			newSignerIdType = null;
		}

		if (selectedDomain.getSignerIdTypes().isEmpty())
		{
			logMessage(getBundle().getString("page.domains.message.warn.signerIdTypeMissing"), Severity.WARN);
			return;
		}

		selectedDomain.setExpirationProperties(expiration.getEditTimeProperty().getExpirationProperties());
		selectedDomain.getConfig().getQualityControlConfig().setTypes(new HashSet<>(qcTypes));
		selectedDomain.getConfig().getQualityControlConfig().setProblemTypes(new HashSet<>(qcProblemTypes));
		selectedDomain.getConfig().getQualityControlConfig().setProblemTypeActions(new HashSet<>(qcProblemTypeActions));

		updateChromeDriverPath();

		try
		{
			Object[] args = { selectedDomain.getLabel() };

			setConfig();
			setVersioning();

			if (pageMode == PageMode.EDIT)
			{
				if (selectedDomain.getFinalised())
				{
					getManager().updateDomainInUse(selectedDomain.getName(), selectedDomain.getLabel(), selectedDomain.getLogo(),
							selectedDomain.getExternProperties(), selectedDomain.getExpirationProperties(),
							selectedDomain.getComment(), selectedDomain.getConfig());
				}
				else
				{
					getManager().updateDomain(selectedDomain);
				}
				logMessage(new MessageFormat(getBundle().getString("page.domains.message.info.updated")).format(args), Severity.INFO);
			}
			else
			{
				if (StringUtils.isEmpty(selectedDomain.getName()))
				{
					selectedDomain.setName(selectedDomain.getLabel());
				}
				selectedDomain.setName(selectedDomain.getName().trim());
				getManager().addDomain(selectedDomain);
				logMessage(new MessageFormat(getBundle().getString("page.domains.message.info.added")).format(args), Severity.INFO);
			}

			domainSelector.loadDomains();
			domainSelector.setSelectedDomain(selectedDomain.getName());
			selectedDomain = null;
			pageMode = PageMode.READ;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("page.domains.message.error.duplicate"), Severity.WARN);
		}
		catch (InvalidParameterException e)
		{
			logger.debug(e.getParameterName(), e);
			if ("domainConfig".equals(e.getParameterName()))
			{
				Object[] args = { selectedDomain.getName(), e.getMessage() };
				logMessage(new MessageFormat(getBundle().getString("page.domains.message.error.invalidParameterDomainConfig")).format(args), Severity.WARN);
			}
			else
			{
				Object[] args = { selectedDomain.getName() };
				logMessage(new MessageFormat(getBundle().getString("page.domains.message.error.invalidParameterDomainName")).format(args), Severity.WARN);
			}
		}
		catch (VersionConverterClassException | UnknownDomainException | ObjectInUseException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDelete() throws UnknownDomainException
	{
		try
		{
			getManager().deleteDomain(selectedDomain.getName());
			Object[] args = { selectedDomain.getName() };
			logMessage(new MessageFormat(getBundle().getString("page.domains.message.info.deleted")).format(args), Severity.INFO);
			domainSelector.loadDomains();
			selectedDomain = null;
		}
		catch (ObjectInUseException | InvalidParameterException e)
		{
			logMessage(getBundle().getString("page.domains.message.error.deleteInUse"), Severity.WARN);
		}
	}

	public String countConsents(String domainName, String templateType)
	{
		PaginationConfig paginationConfig = new PaginationConfig();
		paginationConfig.setTemplateType(ConsentTemplateType.valueOf(templateType));
		try
		{
			long size = getService().countConsentsForDomainWithFilter(domainName, paginationConfig);
			return size > 0 ? String.valueOf(size) : "-";
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			return null;
		}
	}

	public String countTemplates(String domainName)
	{
		try
		{
			int size = getService().listConsentTemplates(domainName, false).size();
			return size > 0 ? String.valueOf(size) : "-";
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			return null;
		}
	}

	public String countModules(String domainName)
	{
		try
		{
			int size = getService().listModules(domainName, false).size();
			return size > 0 ? String.valueOf(size) : "-";
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			return null;
		}
	}

	public String countPolicies(String domainName)
	{
		try
		{
			int size = getService().listPolicies(domainName, false).size();
			return size > 0 ? String.valueOf(size) : "-";
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			return null;
		}
	}

	public void onCancel()
	{
		selectedDomain = null;
		pageMode = PageMode.READ;
	}

	public void onUploadLogo(FileUploadEvent event)
	{
		selectedDomain.setLogo(Base64.getEncoder().encodeToString(event.getFile().getContent()));
	}

	public void onDeleteLogo()
	{
		selectedDomain.setLogo(null);
	}

	public void onNewSignerIdType()
	{
		if (StringUtils.isNotEmpty(newSignerIdType))
		{
			selectedDomain.getSignerIdTypes().add(newSignerIdType);
			newSignerIdType = null;
		}
	}

	public void removeSignerIdType(String index)
	{
		selectedDomain.getSignerIdTypes().remove(Integer.parseInt(index));
	}

	public Boolean isDeletable(DomainDTO domain) throws UnknownDomainException, InvalidVersionException, InvalidParameterException
	{
		if (domain.getName() != null)
		{
			return getService().listConsentTemplates(domain.getName(), false).isEmpty();
		}
		return true;
	}

	public void onNewQcType()
	{
		selectedQcType = new QCType();
		selectedQcTypeIsNew = true;
	}

	public void onEditQcType(QCType qcType)
	{
		selectedQcType = qcType;
		selectedQcTypeIsNew = false;
	}

	public void onSaveQcType()
	{
		if (selectedQcTypeIsNew)
		{
			qcTypes.add(selectedQcType);
		}
	}

	public void onRemoveQcType(QCType qcType)
	{
		qcTypes.remove(qcType);
	}

	public void onSetDefaultQcType(QCType qcType)
	{
		selectedDomain.getConfig().getQualityControlConfig().setDefaultType(qcType);
	}

	public void onNewQcProblemType()
	{
		selectedQcProblemType = new QCProblemType();
		selectedQcProblemTypeIsNew = true;
		selectedQcProblemType.setField(QCProblemTypeField.TEMPLATE_NAME);
		selectedQcProblemType.setError(QCProblemTypeError.MISSING);
		onChangeProblemTypeValues();
	}

	public void onEditQcProblemType(QCProblemType qcProblemType)
	{
		selectedQcProblemType = qcProblemType;
		selectedQcProblemTypeIsNew = false;
	}

	public void onSaveQcProblemType()
	{
		if (selectedQcProblemTypeIsNew)
		{
			// Replace empty id/labels with placeholders
			selectedQcProblemType.setId(StringUtils.isEmpty(selectedQcProblemType.getId()) ? selectedQcProblemTypeIdPlaceholder : selectedQcProblemType.getId());
			selectedQcProblemType.getLabels().replaceAll((lang, label) -> StringUtils.isEmpty(label) ? selectedQcProblemTypeLabelPlaceholder.get(lang) : label);

			qcProblemTypes.add(selectedQcProblemType);
		}
	}

	public void onRemoveQcProblemType(QCProblemType qcProblemType)
	{
		qcProblemTypes.remove(qcProblemType);
	}

	public void onChangeProblemTypeValues()
	{
		if (selectedQcProblemType.getField() != null && selectedQcProblemType.getError() != null)
		{
			selectedQcProblemTypeIdPlaceholder = selectedQcProblemType.getField().name().toLowerCase() + "_" + selectedQcProblemType.getError().name().toLowerCase();
			selectedQcProblemTypeLabelPlaceholder.clear();
			languageBean.getSupportedLanguages().forEach(lang -> selectedQcProblemTypeLabelPlaceholder.put(lang,
					getBundleFor(lang).getString("model.qc.problem.type.field." + selectedQcProblemType.getField().name()) + " " + lowerCaseFirstLetter(
							getBundleFor(lang).getString("model.qc.problem.type.error." + selectedQcProblemType.getError().name()))));
		}
	}

	private String lowerCaseFirstLetter(String string)
	{
		String firstLetter = String.valueOf(string.charAt(0)).toLowerCase();
		return firstLetter + string.substring(1);
	}

	public void onNewQcAction()
	{
		selectedQcProblemTypeAction = new QCProblemTypeAction();
		selectedQcProblemTypeActionIsNew = true;
	}

	public void onEditQcAction(QCProblemTypeAction qcProblemTypeAction)
	{
		selectedQcProblemTypeAction = qcProblemTypeAction;
		selectedQcProblemTypeActionIsNew = false;
	}

	public void onSaveQcAction()
	{
		if (selectedQcProblemTypeActionIsNew)
		{
			qcProblemTypeActions.add(selectedQcProblemTypeAction);
		}
		if (selectedQcProblemType != null)
		{
			selectedQcProblemType.setAction(selectedQcProblemTypeAction);
		}
	}

	public void onRemoveQcAction(QCProblemTypeAction selectedQcProblemTypeAction)
	{
		qcProblemTypeActions.remove(selectedQcProblemTypeAction);
	}

	private void setConfig()
	{
		DomainConfig config = selectedDomain.getConfig();

		config.getScansConfig().setMandatory(scanMandatory);
		config.getScansConfig().setSizeLimit(scanSizeLimit * 1024 * 1024);
		config.getPoliciesConfig().setPermanentRevoke(revokeIsPermanent);
		config.getPoliciesConfig().setTakeHighestVersionInsteadOfNewest(useLatestVersion);
		config.getNotificationsConfig().setSendFromWeb(sendNotificationsWeb);
		config.getStatisticConfig().setCalculateDocumentDetails(statisticDocumentDetails);
		config.getStatisticConfig().setCalculatePolicyDetails(statisticPolicyDetails);
		config.getApplicationConfig().setEnableChromePdfExport(enableChromePdfExport);
		config.getApplicationConfig().setChromedriverPath(chromedriverPath);
		config.getApplicationConfig().setEmbeddedCss(embeddedCss);
		config.getNotificationsConfig().setSendPolicyValidityChanged(sendNotificationsOnPolicyValidityChanged);
	}

	private void setVersioning()
	{
		if (!versionLevelDetailed)
		{
			selectedDomain.setModuleVersionConverter(selectedDomain.getCtVersionConverter());
			selectedDomain.setPolicyVersionConverter(selectedDomain.getCtVersionConverter());
		}
	}

	public StreamedContent getTestChromePDF()
	{
		updateChromeDriverPath();

		String pdfPrintUrl = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURL().toString().replace("domains.xhtml", "print-test.xhtml")
				+ ";jsessionid="
				+ FacesContext.getCurrentInstance().getExternalContext().getSessionId(true);
		
		return getChromeDriverService().getPDFFromUrl(pdfPrintUrl);
	}

	private void updateChromeDriverPath()
	{
		// Set ChromeDriver path if specified
		if (StringUtils.isNotEmpty(chromedriverPath))
		{
			System.setProperty("webdriver.chrome.driver", chromedriverPath);
		}
		else
		{
			System.clearProperty("webdriver.chrome.driver");
		}
	}
	
	public boolean fixedExpirationDateIsExceeded(ExpirationPropertiesDTO expirationProperties)
	{
		return expirationProperties.getFixedExpirationDate() != null && expirationProperties.getFixedExpirationDate().before(new Date());
	}
	
	public void onProcessChangedPolicies(DomainDTO domainDTO)
	{
		try
		{
			getManager().processPolicyValidityChanges(Collections.singletonList(domainDTO));
		}
		catch (UnknownSignerIdTypeException | VersionConverterClassException | UnknownDomainException | InvalidVersionException | UnknownSignerIdException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public String getCheckChangedPoliciesStartTime()
	{
		return System.getProperty(SYSPROP_CHECK_POLICY_CHANGED_AT_TIME);
	}

	public boolean getCheckChangedPoliciesPeriodIsDaily()
	{
		return "1440".equals(System.getProperty(SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES));
	}

	public String getCheckChangedPoliciesPeriod()
	{
		return System.getProperty(SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES);
	}

	@Override
	public DomainDTO getSelectedDomain()
	{
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain)
	{
		this.selectedDomain = selectedDomain != null ? selectedDomain : this.selectedDomain;
	}

	public List<DomainDTO> getDomains()
	{
		return domainSelector.getDomains();
	}

	public Boolean getVersionLevelDetailed()
	{
		return versionLevelDetailed;
	}

	public void setVersionLevelDetailed(Boolean versionLevelDetailed)
	{
		this.versionLevelDetailed = versionLevelDetailed;
	}

	public Boolean getScanMandatory()
	{
		return scanMandatory;
	}

	public void setScanMandatory(Boolean scanMandatory)
	{
		this.scanMandatory = scanMandatory;
	}

	public int getScanSizeLimit()
	{
		return scanSizeLimit;
	}

	public void setScanSizeLimit(int scanSizeLimit)
	{
		this.scanSizeLimit = scanSizeLimit;
	}

	public Boolean getRevokeIsPermanent()
	{
		return revokeIsPermanent;
	}

	public void setRevokeIsPermanent(Boolean revokeIsPermanent)
	{
		this.revokeIsPermanent = revokeIsPermanent;
	}

	public Boolean getUseLatestVersion()
	{
		return useLatestVersion;
	}

	public void setUseLatestVersion(Boolean useLatestVersion)
	{
		this.useLatestVersion = useLatestVersion;
	}

	public Boolean getSendNotificationsWeb()
	{
		return sendNotificationsWeb;
	}

	public void setSendNotificationsWeb(Boolean sendNotificationsWeb)
	{
		this.sendNotificationsWeb = sendNotificationsWeb;
	}

	public String getNewSignerIdType()
	{
		return newSignerIdType;
	}

	public void setNewSignerIdType(String newSignerIdType)
	{
		this.newSignerIdType = newSignerIdType;
	}

	public TimeProperty getExpiration()
	{
		return expiration;
	}

	public void setExpiration(TimeProperty expiration)
	{
		this.expiration = expiration;
	}

	public Boolean getStatisticDocumentDetails()
	{
		return statisticDocumentDetails;
	}

	public void setStatisticDocumentDetails(Boolean statisticDocumentDetails)
	{
		this.statisticDocumentDetails = statisticDocumentDetails;
	}

	public Boolean getStatisticPolicyDetails()
	{
		return statisticPolicyDetails;
	}

	public void setStatisticPolicyDetails(Boolean statisticPolicyDetails)
	{
		this.statisticPolicyDetails = statisticPolicyDetails;
	}

	public boolean isEnableChromePdfExport()
	{
		return enableChromePdfExport;
	}

	public void setEnableChromePdfExport(boolean enableChromePdfExport)
	{
		this.enableChromePdfExport = enableChromePdfExport;
	}

	public String getChromedriverPath()
	{
		return chromedriverPath;
	}

	public void setChromedriverPath(String chromedriverPath)
	{
		this.chromedriverPath = chromedriverPath;
	}

	public String getEmbeddedCss()
	{
		return embeddedCss;
	}

	public void setEmbeddedCss(String embeddedCss)
	{
		this.embeddedCss = embeddedCss;
	}

	public QCType getSelectedQcType()
	{
		return selectedQcType;
	}

	public void setSelectedQcType(QCType selectedQcType)
	{
		this.selectedQcType = selectedQcType;
	}

	public QCTypeStatus[] getAvailableQcTypeStatus()
	{
		return QCTypeStatus.values();
	}

	public QCProblemType getSelectedQcProblemType()
	{
		return selectedQcProblemType;
	}

	public void setSelectedQcProblemType(QCProblemType selectedQcProblemType)
	{
		this.selectedQcProblemType = selectedQcProblemType;
	}

	public QCProblemTypeError[] getAvailableQCProblemTypeErrors()
	{
		return QCProblemTypeError.values();
	}

	public QCProblemTypeField[] getAvailableQCProblemTypeFields()
	{
		return QCProblemTypeField.values();
	}

	public QCProblemTypeOccurrence[] getAvailableQCProblemTypeOccurrences()
	{
		return QCProblemTypeOccurrence.values();
	}

	public QCProblemTypeAction getSelectedQcProblemTypeAction()
	{
		return selectedQcProblemTypeAction;
	}

	public void setSelectedQcProblemTypeAction(QCProblemTypeAction selectedQcProblemTypeAction)
	{
		this.selectedQcProblemTypeAction = selectedQcProblemTypeAction;
	}

	public List<QCType> getQcTypes()
	{
		return qcTypes;
	}

	public void setQcTypes(List<QCType> qcTypes)
	{
		this.qcTypes = qcTypes;
	}

	public String getSelectedQcProblemTypeIdPlaceholder()
	{
		return selectedQcProblemTypeIdPlaceholder;
	}

	public void setSelectedQcProblemTypeIdPlaceholder(String selectedQcProblemTypeIdPlaceholder)
	{
		this.selectedQcProblemTypeIdPlaceholder = selectedQcProblemTypeIdPlaceholder;
	}

	public Map<String, String> getSelectedQcProblemTypeLabelPlaceholder()
	{
		return selectedQcProblemTypeLabelPlaceholder;
	}

	public void setSelectedQcProblemTypeLabelPlaceholder(Map<String, String> selectedQcProblemTypeLabelPlaceholder)
	{
		this.selectedQcProblemTypeLabelPlaceholder = selectedQcProblemTypeLabelPlaceholder;
	}

	public boolean isSelectedQcTypeIsNew()
	{
		return selectedQcTypeIsNew;
	}

	public boolean isSelectedQcProblemTypeIsNew()
	{
		return selectedQcProblemTypeIsNew;
	}

	public boolean isSelectedQcProblemTypeActionIsNew()
	{
		return selectedQcProblemTypeActionIsNew;
	}

	public List<QCProblemType> getQcProblemTypes()
	{
		return qcProblemTypes;
	}

	public void setQcProblemTypes(List<QCProblemType> qcProblemTypes)
	{
		this.qcProblemTypes = qcProblemTypes;
	}

	public List<QCProblemTypeAction> getQcProblemTypeActions()
	{
		return qcProblemTypeActions;
	}

	public void setQcProblemTypeActions(List<QCProblemTypeAction> qcProblemTypeActions)
	{
		this.qcProblemTypeActions = qcProblemTypeActions;
	}

	public Pattern getValidIdPatternWithEmpty()
	{
		return NC_NAME_PATTERN_WITH_EMPTY;
	}
	
	public ConsentTemplateType[] getAvailableTemplateTypes()
	{
		return ConsentTemplateType.values();
	}

	public Boolean getSendNotificationsOnPolicyValidityChanged()
	{
		return sendNotificationsOnPolicyValidityChanged;
	}

	public void setSendNotificationsOnPolicyValidityChanged(Boolean sendNotificationsOnPolicyValidityChanged)
	{
		this.sendNotificationsOnPolicyValidityChanged = sendNotificationsOnPolicyValidityChanged;
	}
}
