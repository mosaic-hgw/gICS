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

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownAliasException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;

/**
 * Backing Bean for Signer Details Component
 *
 * @author Arne Blumentritt, Martin Bialke
 */
@ManagedBean(name = "signerController")
@ViewScoped
public class SignerController extends AbstractGICSBean
{
	private SignerIdDTO signer;
	private SignerIdDTO mergeFirst = new SignerIdDTO();
	private SignerIdDTO mergeSecond = new SignerIdDTO();
	private int mergeMain;
	private List<SignerIdDTO> aliases;
	private List<SignerIdDTO> signerIdsForAlias;
	private boolean hasConsentsWithSameLegalDate = false;
	private List<SignedPolicyDTO> currentSignedPolicies = new ArrayList<>();
	private Map<PolicyKeyDTO, PolicyDTO> policies;
	private Map<ConsentKeyDTO, ConsentLightDTO> consents;
	private Map<ConsentTemplateKeyDTO, ConsentTemplateDTO> templates;
	private List<SelectItem> policyFilterOptions;
	private Date maxDate;

	public void init(SignerIdDTO signer)
	{
		this.signer = signer;
		currentSignedPolicies.clear();

		try
		{
			aliases = service.getAliasesForSignerId(domainSelector.getSelectedDomainName(), signer);
			signerIdsForAlias = service.getSignerIdsForAlias(domainSelector.getSelectedDomainName(), signer);
			Set<SignerIdDTO> signerIdDTO = new HashSet<>(Collections.singletonList(signer));
			CheckConsentConfig config = new CheckConsentConfig();

			// Load policy filter
			policyFilterOptions = new ArrayList<>();
			SelectItemGroup accepted = new SelectItemGroup(getBundle().getString("page.consents.signer.policies.title.ACCEPTED"));
			accepted.setSelectItems(new ArrayList<>(List.of(ConsentStatus.values()))
					.stream()
					.filter(cs -> ConsentStatusType.ACCEPTED.equals(cs.getConsentStatusType()))
					.map(cs -> new SelectItem(cs.name(), getCommonBundle().getString("model.consent.status.admin." + cs.name())))
					.toArray(SelectItem[]::new));
			policyFilterOptions.add(accepted);
			SelectItemGroup declined = new SelectItemGroup(getBundle().getString("page.consents.signer.policies.title.DECLINED"));
			declined.setSelectItems(new ArrayList<>(List.of(ConsentStatus.values()))
					.stream()
					.filter(cs -> ConsentStatusType.DECLINED.equals(cs.getConsentStatusType()))
					.map(cs -> new SelectItem(cs.name(), getCommonBundle().getString("model.consent.status.admin." + cs.name())))
					.toArray(SelectItem[]::new));
			policyFilterOptions.add(declined);
			SelectItemGroup unknown = new SelectItemGroup(getBundle().getString("page.consents.signer.policies.title.UNKNOWN"));
			unknown.setSelectItems(new ArrayList<>(List.of(ConsentStatus.values()))
					.stream()
					.filter(cs -> ConsentStatusType.UNKNOWN.equals(cs.getConsentStatusType()))
					.map(cs -> new SelectItem(cs.name(), getCommonBundle().getString("model.consent.status.admin." + cs.name())))
					.toArray(SelectItem[]::new));
			policyFilterOptions.add(unknown);

			// Map policyDTOs
			policies = service.listPolicies(domainSelector.getSelectedDomainName(), true).stream().collect(Collectors.toMap(PolicyDTO::getKey, p -> p));

			// Get current signed policies
			currentSignedPolicies = service.getCurrentPolicyStatesForSignerIds(domainSelector.getSelectedDomainName(), signerIdDTO, config);

			// Map consents
			consents = service.getAllConsentsForSignerIds(domainSelector.getSelectedDomainName(), signerIdDTO, true).stream().collect(Collectors.toMap(ConsentLightDTO::getKey, c -> c));

			// Map templates
			templates = new HashMap<>();
			consents.keySet().forEach(c ->
			{
				try
				{
					templates.put(c.getConsentTemplateKey(), service.getConsentTemplate(c.getConsentTemplateKey()));
				}
				catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException | InvalidParameterException e)
				{
					logger.error(e.getLocalizedMessage());
				}
			});

			// Create max date for not expired
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			maxDate = sdf.parse("01.01.2900");

			// Check if consents have equal legalDate
			Set<Date> legalConsentDates = new HashSet<>();
			for (ConsentLightDTO consent : consents.values())
			{
				if (legalConsentDates.contains(consent.getConsentDates().getLegalConsentDate()))
				{
					hasConsentsWithSameLegalDate = true;
					break;
				}
				legalConsentDates.add(consent.getConsentDates().getLegalConsentDate());
			}
		}
		catch (UnknownSignerIdTypeException | InvalidParameterException | UnknownSignerIdException | InvalidVersionException | InconsistentStatusException | UnknownDomainException | ParseException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onNewMerge()
	{
		mergeFirst = signer;
		mergeSecond = new SignerIdDTO();
		mergeMain = 0;
	}

	public void onSaveMerge()
	{
		try
		{
			SignerIdDTO mainId = mergeMain == 1 ? mergeFirst : mergeSecond;
			SignerIdDTO aliasId = mergeMain == 1 ? mergeSecond : mergeFirst;

			service.addAlias(domainSelector.getSelectedDomainName(), mainId, aliasId);

			Object[] args = { mainId.getIdType(), mainId.getId(), aliasId.getIdType(), aliasId.getId() };
			logMessage(new MessageFormat(getBundle().getString("page.consents.signer.merge.message.info.success")).format(args), Severity.INFO);
		}
		catch (InvalidParameterException | UnknownDomainException | UnknownSignerIdException | UnknownSignerIdTypeException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDeactivateAlias(SignerIdDTO alias)
	{
		try
		{
			service.deactivateAlias(domainSelector.getSelectedDomainName(), signer, alias);
			Object[] args = { alias.getIdType(), alias.getId() };
			logMessage(new MessageFormat(getBundle().getString("page.consents.signer.deactivateAlias.message.info.success")).format(args), Severity.INFO);
		}
		catch (InvalidParameterException | UnknownAliasException | UnknownDomainException | UnknownSignerIdException | UnknownSignerIdTypeException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public List<SignerIdDTO> getAliases()
	{
		return aliases;
	}

	public List<SignerIdDTO> getSignerIdsForAlias()
	{
		return signerIdsForAlias;
	}

	public SignerIdDTO getMergeFirst()
	{
		return mergeFirst;
	}

	public void setMergeFirst(SignerIdDTO mergeFirst)
	{
		this.mergeFirst = mergeFirst;
	}

	public SignerIdDTO getMergeSecond()
	{
		return mergeSecond;
	}

	public void setMergeSecond(SignerIdDTO mergeSecond)
	{
		this.mergeSecond = mergeSecond;
	}

	public int getMergeMain()
	{
		return mergeMain;
	}

	public void setMergeMain(int mergeMain)
	{
		this.mergeMain = mergeMain;
	}

	public boolean isHasConsentsWithSameLegalDate()
	{
		return hasConsentsWithSameLegalDate;
	}

	public List<SignedPolicyDTO> getCurrentSignedPolicies()
	{
		return currentSignedPolicies;
	}

	public Map<PolicyKeyDTO, PolicyDTO> getPolicies()
	{
		return policies;
	}

	public List<SelectItem> getPolicyFilterOptions()
	{
		return policyFilterOptions;
	}

	public Map<ConsentKeyDTO, ConsentLightDTO> getConsents()
	{
		return consents;
	}

	public Map<ConsentTemplateKeyDTO, ConsentTemplateDTO> getTemplates()
	{
		return templates;
	}

	public Date expires(Date date)
	{
		return date.before(maxDate) ? date : null;
	}
}
