package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2022 Trusted Third Party of the University Medicine Greifswald -
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
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *  
 * 							Selected functionalities of gICS were developed as
 * 							part of the following research projects:
 * 							- MAGIC (funded by the DFG HO 1937/5-1)
 * 							- MIRACUM (funded by the German Federal Ministry of Education and Research 01ZZ1801M)
 * 							- NUM-CODEX (funded by the German Federal Ministry of Education and Research 01KX2021)
 * 
 * 							please cite our publications
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
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
	private final List<PolicyDTO> acceptedPolicies = new ArrayList<>();
	private final List<PolicyDTO> declinedPolicies = new ArrayList<>();
	private final List<PolicyDTO> unknownPolicies = new ArrayList<>();
	private List<SignerIdDTO> aliases;

	public void init(SignerIdDTO signer)
			throws UnknownDomainException, UnknownSignerIdTypeException, VersionConverterClassException, InvalidVersionException, UnknownPolicyException, InvalidParameterException,
			UnknownSignerIdException
	{
		this.signer = signer;

		acceptedPolicies.clear();
		declinedPolicies.clear();
		unknownPolicies.clear();

		aliases = service.getAliasesForSignerId(domainSelector.getSelectedDomainName(), signer);
		Set<SignerIdDTO> signerIdDTO = new HashSet<>(Collections.singletonList(signer));
		CheckConsentConfig config = new CheckConsentConfig();
		Set<PolicyKeyDTO> signerPolicies = new HashSet<>();

		// Get all policies of the signer and his aliases
		for (SignedPolicyDTO policy : service.getPolicyStatesForSignerIds(domainSelector.getSelectedDomainName(), signerIdDTO, true))
		{
			signerPolicies.add(policy.getPolicyKey());
		}

		Set<SignerIdDTO> signerSet = new HashSet<>(Collections.singletonList(signer));

		// Get the current consentStatusType for each of these policies
		for (PolicyKeyDTO policy : signerPolicies)
		{
			switch (service.getConsentStatusType(signerSet, policy, config))
			{
				case ACCEPTED:
					acceptedPolicies.add(service.getPolicy(policy));
					break;
				case DECLINED:
					declinedPolicies.add(service.getPolicy(policy));
					break;
				case UNKNOWN:
					unknownPolicies.add(service.getPolicy(policy));
			}
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

			Object[] args = { mainId.getIdType(), mainId.getId(), aliasId.getIdType(), aliasId.getId()};
			logMessage(new MessageFormat(getBundle().getString("page.consents.signer.merge.message.info.success")).format(args), Severity.INFO);
		}
		catch (InvalidParameterException | UnknownDomainException | UnknownSignerIdException | UnknownSignerIdTypeException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}
	
	public List<SignerIdDTO> getAliases()
	{
		return aliases;
	}

	public List<PolicyDTO> getAcceptedPolicies()
	{
		return acceptedPolicies;
	}

	public List<PolicyDTO> getDeclinedPolicies()
	{
		return declinedPolicies;
	}

	public List<PolicyDTO> getUnknownPolicies()
	{
		return unknownPolicies;
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
}
