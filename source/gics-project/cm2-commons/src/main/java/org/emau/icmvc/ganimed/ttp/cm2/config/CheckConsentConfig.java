package org.emau.icmvc.ganimed.ttp.cm2.config;

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

import java.io.Serializable;
import java.util.Date;

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;

/**
 * configuration for the check whether the given policy is consented by the given person
 * <p>
 * <b>idMatchingType</b><br>
 * match at least one, at least all or exact all of the given signer identifiers; see {@link IdMatchingType}<br>
 * default = {@link IdMatchingType#AT_LEAST_ONE}
 * <p>
 * <b>ignoreVersionNumber</b><br>
 * ignore the version number of the policy<br>
 * default = false
 * <p>
 * <b>unknownStateIsConsideredAsDecline</b><br>
 * if the consent state type "unknown" is encountered, it's considered as "declined" for the "isConsented...()"-functions; see {@link ConsentStatusType}<br>
 * default = false
 * <p>
 * <b>requestDate</b><br>
 * date for when the consent status is requested<br>
 * default = current date
 * <p>
 * <b>useAliases</b><br>
 * should aliases be used when searching for signer ids?<br>
 * will be ignored if {@link CheckConsentConfig#idMatchingType} is {@link IdMatchingType#EXACT}<br>
 * default = true
 * <p>
 * <b>useHistoricalData</b><br>
 * for requests with a request date: what state of knowledge should be used?<br>
 * the stored data within gics at the request date (true) or the current stored data (false)<br>
 * default = false
 *
 *
 * @author geidell
 *
 */
public class CheckConsentConfig implements Serializable
{
	private static final long serialVersionUID = -5925822679041293092L;
	/**
	 * match at least one, at least all or exact all of the given signer identifiers; see {@link IdMatchingType}<br>
	 * default = {@link IdMatchingType#AT_LEAST_ONE}
	 */
	private IdMatchingType idMatchingType = IdMatchingType.AT_LEAST_ONE;
	/**
	 * ignore the version number of the policy<br>
	 * default = false
	 */
	private boolean ignoreVersionNumber = false;
	/**
	 * if the consent state type "unknown" is encountered, it's considered as "declined" for the "isConsented...()"-functions; see {@link ConsentStatusType}<br>
	 * default = false
	 */
	private boolean unknownStateIsConsideredAsDecline = false;
	/**
	 * date for when the consent status is requested<br>
	 * default = current date
	 */
	private Date requestDate = new Date();
	/**
	 * should aliases be used when searching for signer ids?<br>
	 * will be ignored if {@link CheckConsentConfig#idMatchingType} is {@link IdMatchingType#EXACT}<br>
	 * default = true
	 */
	private boolean useAliases = true;
	/**
	 * for requests with a request date: what state of knowledge should be used?<br>
	 * the stored data within gics at the request date (true) or the current stored data (false)<br>
	 * default = false
	 */
	private boolean useHistoricalData = false;

	public CheckConsentConfig()
	{}

	public CheckConsentConfig(IdMatchingType idMatchingType, boolean ignoreVersionNumber, boolean unknownStateIsConsideredAsDecline)
	{
		this.idMatchingType = idMatchingType;
		this.ignoreVersionNumber = ignoreVersionNumber;
		this.unknownStateIsConsideredAsDecline = unknownStateIsConsideredAsDecline;
	}

	public CheckConsentConfig(IdMatchingType idMatchingType, boolean ignoreVersionNumber, boolean unknownStateIsConsideredAsDecline, Date requestDate, boolean useAliases)
	{
		this.idMatchingType = idMatchingType;
		this.ignoreVersionNumber = ignoreVersionNumber;
		this.unknownStateIsConsideredAsDecline = unknownStateIsConsideredAsDecline;
		if (requestDate != null)
		{
			this.requestDate = requestDate;
		}
		this.useAliases = useAliases;
	}

	public CheckConsentConfig(IdMatchingType idMatchingType, boolean ignoreVersionNumber, boolean unknownStateIsConsideredAsDecline, Date requestDate, boolean useAliases, boolean useHistoricalData)
	{
		this.idMatchingType = idMatchingType;
		this.ignoreVersionNumber = ignoreVersionNumber;
		this.unknownStateIsConsideredAsDecline = unknownStateIsConsideredAsDecline;
		if (requestDate != null)
		{
			this.requestDate = requestDate;
		}
		this.useAliases = useAliases;
		this.useHistoricalData = useHistoricalData;
	}

	public IdMatchingType getIdMatchingType()
	{
		return idMatchingType;
	}

	public void setIdMatchingType(IdMatchingType idMatchingType)
	{
		this.idMatchingType = idMatchingType;
	}

	public boolean getIgnoreVersionNumber()
	{
		return ignoreVersionNumber;
	}

	public void setIgnoreVersionNumber(boolean ignoreVersionNumber)
	{
		this.ignoreVersionNumber = ignoreVersionNumber;
	}

	public boolean getUnknownStateIsConsideredAsDecline()
	{
		return unknownStateIsConsideredAsDecline;
	}

	public void setUnknownStateIsConsideredAsDecline(boolean unknownStateIsConsideredAsDecline)
	{
		this.unknownStateIsConsideredAsDecline = unknownStateIsConsideredAsDecline;
	}

	public Date getRequestDate()
	{
		return requestDate;
	}

	public void setRequestDate(Date requestDate)
	{
		this.requestDate = requestDate;
	}

	public boolean isUseAliases()
	{
		return useAliases;
	}

	public void setUseAliases(boolean useAliases)
	{
		this.useAliases = useAliases;
	}

	public boolean isUseHistoricalData()
	{
		return useHistoricalData;
	}

	public void setUseHistoricalData(boolean useHistoricalData)
	{
		this.useHistoricalData = useHistoricalData;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (idMatchingType == null ? 0 : idMatchingType.hashCode());
		result = prime * result + (ignoreVersionNumber ? 1231 : 1237);
		result = prime * result + (requestDate == null ? 0 : requestDate.hashCode());
		result = prime * result + (unknownStateIsConsideredAsDecline ? 1231 : 1237);
		result = prime * result + (useAliases ? 1231 : 1237);
		result = prime * result + (useHistoricalData ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		CheckConsentConfig other = (CheckConsentConfig) obj;
		if (idMatchingType != other.idMatchingType)
		{
			return false;
		}
		if (ignoreVersionNumber != other.ignoreVersionNumber)
		{
			return false;
		}
		if (requestDate == null)
		{
			if (other.requestDate != null)
			{
				return false;
			}
		}
		else if (!requestDate.equals(other.requestDate))
		{
			return false;
		}
		if (unknownStateIsConsideredAsDecline != other.unknownStateIsConsideredAsDecline)
		{
			return false;
		}
		if (useAliases != other.useAliases)
		{
			return false;
		}
		if (useHistoricalData != other.useHistoricalData)
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("check consent config with the following settings:\n");
		sb.append("signer id matching type: ");
		sb.append(idMatchingType);
		sb.append("\nignore version number: ");
		sb.append(ignoreVersionNumber);
		sb.append("\nunknown state is considered as decline: ");
		sb.append(unknownStateIsConsideredAsDecline);
		sb.append("\nuse aliases for signer ids: ");
		sb.append(useAliases);
		sb.append("\nuse historical data for requests with a request date: ");
		sb.append(useHistoricalData);
		sb.append("\nfor date: ");
		sb.append(requestDate.toString());
		return sb.toString();
	}

	/**
	 * <b>AT_LEAST_ONE</b><br>
	 * at least one of the given ids must be related to the person<br>
	 * <b>AT_LEAST_ALL</b><br>
	 * all given ids must be related to the person<br>
	 * <b>EXACT</b><br>
	 * all given ids and no other id must be related to the person<br>
	 *
	 * @author geidell
	 *
	 */
	public enum IdMatchingType
	{
		AT_LEAST_ONE, AT_LEAST_ALL, EXACT;
	}
}
