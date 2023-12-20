package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

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

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.cm2.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.DuplicateEntryException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.component.WebExpiration;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.Versions;
import org.primefaces.event.FileUploadEvent;

/**
 * Backing Bean for Domains View
 *
 * @author Arne Blumentritt
 */
@ManagedBean(name = "domainController")
@ViewScoped
public class DomainController extends AbstractConsentController implements Serializable
{
	@Serial
	private static final long serialVersionUID = 1285364203849974795L;
	private DomainDTO selectedDomain;

	// Versioning options
	private Boolean versionLevelDetailed;

	// Additional properties
	private Boolean scanMandatory;
	private int scanSizeLimit;
	private Boolean revokeIsPermanent;
	private Boolean useLatestVersion;
	private Boolean sendNotificationsWeb;
	private boolean qualityControlOptional;
	private String newSignerIdType;
	private WebExpiration expiration;
	private Boolean statisticDocumentDetails;
	private Boolean statisticPolicyDetails;

	public static String parseProperty(String properties, DomainProperties key)
	{
		return Arrays.stream(properties.split(";"))
				.filter(line -> line.contains(key.name()))
				.map(line -> line + "=") // ensure splitting by "=" always yields at least two arguments
				.findFirst().orElse("").split("=")[1];
	}

	public void onShowDetails(DomainDTO domain)
	{
		selectedDomain = domain;

		scanMandatory = !Boolean.parseBoolean(parseProperty(selectedDomain.getProperties(), DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS));
		scanSizeLimit = Integer.parseInt(parseProperty(selectedDomain.getProperties(), DomainProperties.SCANS_SIZE_LIMIT)) / 1024 / 1024;
		revokeIsPermanent = Boolean.valueOf(parseProperty(selectedDomain.getProperties(), DomainProperties.REVOKE_IS_PERMANENT));
		useLatestVersion = Boolean.valueOf(parseProperty(selectedDomain.getProperties(), DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST));
		sendNotificationsWeb = Boolean.valueOf(parseProperty(selectedDomain.getProperties(), DomainProperties.SEND_NOTIFICATIONS_WEB));
		statisticDocumentDetails = Boolean.valueOf(parseProperty(selectedDomain.getProperties(), DomainProperties.STATISTIC_DOCUMENT_DETAILS));
		statisticPolicyDetails = Boolean.valueOf(parseProperty(selectedDomain.getProperties(), DomainProperties.STATISTIC_POLICY_DETAILS));
		qualityControlOptional = parseProperty(selectedDomain.getProperties(), DomainProperties.VALID_QC_TYPES).contains(NOT_CHECKED);
		expiration = new WebExpiration();
		expiration.getEditExpiration().setExpirationProperties(selectedDomain.getExpirationProperties());

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
		qualityControlOptional = true;
		expiration = new WebExpiration();
		statisticDocumentDetails = true;
		statisticPolicyDetails = true;

		versionLevelDetailed = false;
		pageMode = PageMode.NEW;
	}

	public void onEdit(DomainDTO domain)
	{
		onShowDetails(domain);
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

		selectedDomain.setExpirationProperties(expiration.getEditExpiration().getExpirationProperties());

		try
		{
			Object[] args = { selectedDomain.getLabel() };

			if (pageMode == PageMode.EDIT)
			{
				if (selectedDomain.getFinalised())
				{
					manager.updateDomainInUse(selectedDomain.getName(), selectedDomain.getLabel(), selectedDomain.getLogo(), selectedDomain.getExternProperties(),
							selectedDomain.getComment());
				}
				else
				{
					selectedDomain.setProperties(generatePropertiesString());
					setVersioning();

					manager.updateDomain(selectedDomain);
				}
				logMessage(new MessageFormat(getBundle().getString("page.domains.message.info.updated")).format(args), Severity.INFO);
			}
			else
			{
				if (StringUtils.isEmpty(selectedDomain.getName()))
				{
					selectedDomain.setName(selectedDomain.getLabel());
				}
				selectedDomain.setProperties(generatePropertiesString());
				setVersioning();

				manager.addDomain(selectedDomain);
				logMessage(new MessageFormat(getBundle().getString("page.domains.message.info.added")).format(args), Severity.INFO);
			}

			domainSelector.loadDomains();
			domainSelector.setSelectedDomain(selectedDomain.getName());
			selectedDomain = null;
		}
		catch (DuplicateEntryException e)
		{
			logMessage(getBundle().getString("page.domains.message.error.duplicate"), Severity.WARN);
		}
		catch (InvalidParameterException e)
		{
			Object[] args = { selectedDomain.getName() };
			logMessage(new MessageFormat(getBundle().getString("page.domains.message.error.invalidParameterDomainName")).format(args), Severity.WARN);
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
			manager.deleteDomain(selectedDomain.getName());
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
			long size = service.countConsentsForDomainWithFilter(domainName, paginationConfig);
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
			int size = service.listConsentTemplates(domainName, false).size();
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
			int size = service.listModules(domainName, false).size();
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
			int size = service.listPolicies(domainName, false).size();
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
			return service.listConsentTemplates(domain.getName(), false).isEmpty();
		}
		return true;
	}

	private String generatePropertiesString()
	{
		StringBuilder sb = new StringBuilder();
		appendPropertyString(sb, DomainProperties.SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS, String.valueOf(!scanMandatory));
		appendPropertyString(sb, DomainProperties.SCANS_SIZE_LIMIT, String.valueOf(scanSizeLimit * 1024 * 1024));
		appendPropertyString(sb, DomainProperties.REVOKE_IS_PERMANENT, String.valueOf(revokeIsPermanent));
		appendPropertyString(sb, DomainProperties.TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST, String.valueOf(useLatestVersion));
		appendPropertyString(sb, DomainProperties.SEND_NOTIFICATIONS_WEB, String.valueOf(sendNotificationsWeb));
		appendPropertyString(sb, DomainProperties.STATISTIC_DOCUMENT_DETAILS, String.valueOf(statisticDocumentDetails));
		appendPropertyString(sb, DomainProperties.STATISTIC_POLICY_DETAILS, String.valueOf(statisticPolicyDetails));

		String validQcTypes = (qualityControlOptional ? NOT_CHECKED + "," : "") + CHECKED_NO_FAULTS + "," + CHECKED_MINOR_FAULTS;
		String invalidQcTypes = (qualityControlOptional ? "" : NOT_CHECKED + ",") + CHECKED_MAJOR_FAULTS + "," + INVALIDATED;
		appendPropertyString(sb, DomainProperties.VALID_QC_TYPES, validQcTypes);
		appendPropertyString(sb, DomainProperties.INVALID_QC_TYPES, invalidQcTypes);
		appendPropertyString(sb, DomainProperties.DEFAULT_QC_TYPE, NOT_CHECKED);

		return sb.toString();
	}

	private void setVersioning()
	{
		if (!versionLevelDetailed)
		{
			selectedDomain.setModuleVersionConverter(selectedDomain.getCtVersionConverter());
			selectedDomain.setPolicyVersionConverter(selectedDomain.getCtVersionConverter());
		}
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

	private void appendPropertyString(StringBuilder sb, DomainProperties key, String value)
	{
		sb.append(key.name());
		sb.append('=');
		sb.append(value);
		sb.append(';');
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

	public Boolean getQualityControlOptional()
	{
		return qualityControlOptional;
	}

	public void setQualityControlOptional(Boolean qualityControlOptional)
	{
		this.qualityControlOptional = qualityControlOptional;
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

	public WebExpiration getExpiration()
	{
		return expiration;
	}

	public void setExpiration(WebExpiration expiration)
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
}
