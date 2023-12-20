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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.PolicyStatusWebFile;

/**
 * Backing Bean for listing and downloading the policy status for all signerIds
 *
 * @author Arne Blumentritt
 */
@ManagedBean(name = "policyStatusController")
@ViewScoped
public class PolicyStatusController extends AbstractGICSBean implements Serializable
{
	@Serial
	private static final long serialVersionUID = -7356602566712493725L;

	// Mode
	private Mode mode;

	// File
	private PolicyStatusWebFile webFile;

	// Options
	private boolean detailedResult;
	private PolicyDTO policy;
	private String signerIdType;
	private RequestDateType requestDateType;
	private Date requestDate;
	private List<PolicyDTO> availablePolicies;
	private boolean useHistoricalData;

	// Progress bar
	private int sum;
	private int progress;

	@PostConstruct
	public void init()
	{
		try
		{
			availablePolicies = service.listPolicies(domainSelector.getSelectedDomainName(), true);
			webFile = new PolicyStatusWebFile("gICS", availablePolicies);
			onNewUpload();
		}
		catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onNewUpload()
	{
		webFile.onNewUpload();
		detailedResult = false;
		policy = null;
		signerIdType = null;
		requestDateType = RequestDateType.NOW;
		requestDate = new Date();
		useHistoricalData = false;

		sum = 0;
		progress = 0;
	}

	public void onLoadAllSignerIds() throws UnknownDomainException, UnknownSignerIdTypeException, InvalidParameterException
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

	public void onProcessUploadedPolicies()
	{
		webFile.removeDetectedPolicyColumns();
		for (PolicyDTO policyDTO : webFile.getColumnPolicyMapping().values())
		{
			policy = policyDTO;
			onRequestPolicy();
		}
		webFile.getColumnPolicyMapping().clear();
	}

	public void onRequestPolicy()
	{
		Set<String> inputs = new HashSet<>();

		// Contains the policy result for each signer id
		Map<String, String> policyResults = new HashMap<>();

		List<List<String>> resultTable = new ArrayList<>();
		
		CheckConsentConfig checkConsentConfig = new CheckConsentConfig(IdMatchingType.AT_LEAST_ONE, false, false, RequestDateType.DATE.equals(requestDateType) ? requestDate : null, true,
				useHistoricalData);

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
		Object[] args = { webFile.getElements().size(), getPolicyLabel(policy), policy.getKey().getVersion() };
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
					policyResults.put(id, service.getConsentStatusType(signerIdSet, policy.getKey(), checkConsentConfig).toString());
				}
				else
				{
					policyResults.put(id, String.valueOf(service.isConsented(signerIdSet, policy.getKey(), checkConsentConfig)));
				}
				progress = policyResults.size();
			}
			logMessage(new MessageFormat(getBundle().getString("page.policyStatus.message.info")).format(args), Severity.INFO);
		}
		catch (UnknownPolicyException | UnknownDomainException | InvalidVersionException | UnknownSignerIdTypeException | InvalidParameterException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}

		// add column for current policy with the result from each signer id
		if (!policyResults.isEmpty())
		{
			// iterate over each row (signer id)
			for (List<String> row : webFile.getElements())
			{
				String value = null;
				// if row contains column with signer id (some csv are bad and miss a column in a few rows)
				if (row.size() > webFile.getSelectedColumn())
				{
					// get policy result for signer id
					value = policyResults.get(row.get(webFile.getSelectedColumn()));
				}

				// Append result to end of row
				row.add(webFile.getColumns().size(), value);

				// Add row to result table
				resultTable.add(row);
			}

			// Create column for policy
			String columnName = policy.getKey().getName() + " " + policy.getKey().getVersion();
			if (RequestDateType.DATE.equals(requestDateType))
			{
				columnName += " (" + dateToString(requestDate, "date") + (useHistoricalData ? ", " + getBundle().getString("page.policyStatus.requestDate.historical") : "") + ")";
			}
			// Append column to end of columns list
			webFile.getColumns().add(webFile.getColumns().size(), columnName);
			webFile.setElements(resultTable);
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

	public List<PolicyDTO> getAvailablePolicies()
	{
		return availablePolicies;
	}

	public void chooseUpload() throws InvalidVersionException, UnknownDomainException
	{
		init();
		mode = Mode.UPLOAD;
	}

	public void chooseAll() throws InvalidVersionException, UnknownDomainException
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

	public PolicyStatusWebFile getWebFile()
	{
		return webFile;
	}

	public int getDetectedPoliciesSize()
	{
		return webFile.getColumnPolicyMapping().size();
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

	public boolean isUseHistoricalData()
	{
		return useHistoricalData;
	}

	public void setUseHistoricalData(boolean useHistoricalData)
	{
		this.useHistoricalData = useHistoricalData;
	}
}
