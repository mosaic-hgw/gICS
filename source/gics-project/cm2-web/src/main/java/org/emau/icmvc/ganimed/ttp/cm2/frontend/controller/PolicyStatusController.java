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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.CheckConsentConfig.IdMatchingType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.icmvc.ttp.web.model.WebFile;

/**
 * Backing Bean for listing and downloading the policy status for all signerIds
 *
 * @author Arne Blumentritt
 */
@ManagedBean(name = "policyStatusController")
@ViewScoped
public class PolicyStatusController extends AbstractGICSBean implements Serializable
{
	private static final long serialVersionUID = -7356602566712493725L;

	// Mode
	private Mode mode;

	// File
	private WebFile webFile;

	// Options
	private boolean detailedResult;
	private PolicyDTO policy;
	private String signerIdType;
	private RequestDateType requestDateType;
	private Date requestDate;

	// Progress bar
	private int sum;
	private int progress;

	@PostConstruct
	public void init()
	{
		webFile = new WebFile("gICS");
		onNewUpload();
	}

	public void onNewUpload()
	{
		webFile.onNewUpload();
		detailedResult = false;
		policy = null;
		signerIdType = null;
		requestDateType = RequestDateType.NOW;
		requestDate = new Date();

		sum = 0;
		progress = 0;
	}

	public void onLoadAllSignerIds() throws UnknownDomainException, UnknownSignerIdTypeException
	{
		webFile.setColumns(new ArrayList<>(Collections.singletonList(signerIdType)));
		webFile.getElements().clear();

		for (String signerId : service.getAllIdsForSignerIdType(domainSelector.getSelectedDomainName(), signerIdType))
		{
			webFile.addRow(new ArrayList<>(Collections.singleton(signerId)));
		}

		if (webFile.getElements().isEmpty())
		{
			logMessage(getBundle().getString("page.policyStatus.message.warn.noSignerIdsFound"), Severity.WARN);
		}
	}

	public void onDoAction()
	{
		Set<String> inputs = new HashSet<>();
		Map<String, String> resultMap = new HashMap<>();
		List<List<String>> resultElements = new ArrayList<>();
		CheckConsentConfig checkConsentConfig = new CheckConsentConfig(IdMatchingType.AT_LEAST_ONE, false, false, RequestDateType.DATE.equals(requestDateType) ? requestDate : null, true);

		int rowIndex = 0;
		for (List<String> row : webFile.getElements())
		{
			rowIndex++;
			if (row.size() > webFile.getSelectedColumn() && row.get(webFile.getSelectedColumn()) != null)
			{
				inputs.add(row.get(webFile.getSelectedColumn()));
			}
			else
			{
				Object[] args = { rowIndex };
				logMessage(new MessageFormat(getCommonBundle().getString("ui.batch.message.warn.skipped")).format(args), Severity.WARN);
			}
		}
		Object[] args = { webFile.getElements().size() };
		sum = inputs.size();
		progress = 0;

		try
		{
			for (String id : inputs)
			{
				Set<SignerIdDTO> signerIdSet = new HashSet<>();
				signerIdSet.add(new SignerIdDTO(signerIdType, id, new Date(), null));

				if (detailedResult)
				{
					resultMap.put(id, service.getConsentStatusType(signerIdSet, policy.getKey(), checkConsentConfig).toString());
				}
				else
				{
					resultMap.put(id, String.valueOf(service.isConsented(signerIdSet, policy.getKey(), checkConsentConfig)));
				}
				progress = resultMap.size();
			}
			logMessage(new MessageFormat(getBundle().getString("page.policyStatus.message.info")).format(args), Severity.INFO);
		}
		catch (UnknownPolicyException | UnknownDomainException | InvalidVersionException | UnknownSignerIdTypeException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}

		if (!resultMap.isEmpty())
		{
			for (List<String> row : webFile.getElements())
			{
				String value = null;
				if (row.size() > webFile.getSelectedColumn())
				{
					value = resultMap.get(row.get(webFile.getSelectedColumn()));
				}

				List<String> rowCopy = new ArrayList<>(row);
				if (rowCopy.size() <= webFile.getSelectedColumn())
				{
					rowCopy.add(webFile.getSelectedColumn(), null);
				}

				rowCopy.add(webFile.getSelectedColumn() + 1, value);

				resultElements.add(rowCopy);
			}

			webFile.getColumns().add(webFile.getSelectedColumn() + 1, policy.getKey().getName() + " V " + policy.getKey().getVersion());
			webFile.setElements(resultElements);
			webFile.setProcessed(true);
		}
		progress = 0;
	}

	public Integer getProgress()
	{
		if (sum == 0)
		{
			return 0;
		}
		else
		{
			int result = progress * 100 / sum;
			return result == 0 ? 1 : result;
		}
	}

	public List<PolicyDTO> getPolicies() throws UnknownDomainException, InvalidVersionException
	{
		return service.listPolicies(domainSelector.getSelectedDomainName(), false);
	}

	public void chooseUpload()
	{
		init();
		mode = Mode.UPLOAD;
	}

	public void chooseAll()
	{
		init();
		mode = Mode.ALL;
	}

	public void onDownload()
	{
		webFile.onDownload("Policy Status");
	}

	public PolicyDTO getPolicy()
	{
		return policy;
	}

	public void setPolicy(PolicyDTO policy)
	{
		this.policy = policy;
	}

	public String getSignerIdType()
	{
		return signerIdType;
	}

	public void setSignerIdType(String signerIdType)
	{
		this.signerIdType = signerIdType;
	}

	public Boolean getDetailedResult()
	{
		return detailedResult;
	}

	public void setDetailedResult(Boolean detailedResult)
	{
		this.detailedResult = detailedResult;
	}

	public WebFile getWebFile()
	{
		return webFile;
	}

	public Mode getMode()
	{
		return mode;
	}

	public RequestDateType getRequestDateType()
	{
		return requestDateType;
	}

	public void setRequestDateType(RequestDateType requestDateType)
	{
		this.requestDateType = requestDateType;
	}

	public RequestDateType[] getAvailableRequestDateTypes()
	{
		return RequestDateType.values();
	}

	public Date getRequestDate()
	{
		return requestDate;
	}

	public void setRequestDate(Date requestDate)
	{
		this.requestDate = requestDate;
	}

	public enum Mode
	{
		UPLOAD, ALL
	}
	
	public enum RequestDateType
	{
		NOW, DATE
	}
}
