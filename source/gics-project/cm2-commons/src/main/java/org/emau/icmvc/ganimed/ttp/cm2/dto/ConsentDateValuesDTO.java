package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;

public class ConsentDateValuesDTO implements Serializable
{
	private static final long serialVersionUID = 7426671516873993949L;
	public static final Date INFINITE_DATE = Date.from(LocalDateTime.of(3000, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
	private Date gicsConsentDate = new Date(0l);
	private Date legalConsentDate = new Date(0l);
	private Date consentExpirationDate = INFINITE_DATE;
	private Date maxPolicyExpirationDate = INFINITE_DATE;
	private Map<ModuleKeyDTO, Date> moduleExpirations = new HashMap<>();
	private Map<ModuleKeyDTO, HashMap<PolicyKeyDTO, Date>> policyExpirations = new HashMap<>();

	public ConsentDateValuesDTO()
	{
		super();
	}

	public ConsentDateValuesDTO(ConsentDateValuesDTO consentExpirationValues)
	{
		super();
		this.gicsConsentDate = new Date(consentExpirationValues.getGicsConsentDate().getTime());
		this.legalConsentDate = new Date(consentExpirationValues.getLegalConsentDate().getTime());
		this.consentExpirationDate = new Date(consentExpirationValues.getConsentExpirationDate().getTime());
		this.maxPolicyExpirationDate = new Date(consentExpirationValues.getMaxPolicyExpirationDate().getTime());
		for (Entry<ModuleKeyDTO, Date> entry : consentExpirationValues.getModuleExpirations().entrySet())
		{
			moduleExpirations.put(entry.getKey(), new Date(entry.getValue().getTime()));
		}
		for (Entry<ModuleKeyDTO, HashMap<PolicyKeyDTO, Date>> entry : consentExpirationValues.getPolicyExpirations().entrySet())
		{
			HashMap<PolicyKeyDTO, Date> policyMap = new HashMap<>();
			for (Entry<PolicyKeyDTO, Date> policyEntry : entry.getValue().entrySet())
			{
				policyMap.put(policyEntry.getKey(), new Date(policyEntry.getValue().getTime()));
			}
			policyExpirations.put(entry.getKey(), policyMap);
		}
	}

	public Date getGicsConsentDate()
	{
		return gicsConsentDate;
	}

	public void setGicsConsentDate(Date gicsConsentDate)
	{
		this.gicsConsentDate = gicsConsentDate;
	}

	public Date getLegalConsentDate()
	{
		return legalConsentDate;
	}

	public void setLegalConsentDate(Date legalConsentDate)
	{
		this.legalConsentDate = legalConsentDate;
	}

	/**
	 * this is the expirationDate of the consent as an object. it may be, that some (see {@link ConsentDateValuesDTO#policyExpirations}) or all (see
	 * {@link ConsentDateValuesDTO#maxPolicyExpirationDate}) policies expire before that date
	 *
	 * @return
	 */
	public Date getConsentExpirationDate()
	{
		return consentExpirationDate;
	}

	public void setConsentExpirationDate(Date consentExpirationDate)
	{
		this.consentExpirationDate = consentExpirationDate;
	}

	public Date getMaxPolicyExpirationDate()
	{
		return maxPolicyExpirationDate;
	}

	public void setMaxPolicyExpirationDate(Date maxPolicyExpirationDate)
	{
		this.maxPolicyExpirationDate = maxPolicyExpirationDate;
	}

	public Map<ModuleKeyDTO, Date> getModuleExpirations()
	{
		return moduleExpirations;
	}

	public void setModuleExpirations(Map<ModuleKeyDTO, Date> moduleExpirations)
	{
		if (moduleExpirations != null)
		{
			this.moduleExpirations = moduleExpirations;
		}
	}

	public Map<ModuleKeyDTO, HashMap<PolicyKeyDTO, Date>> getPolicyExpirations()
	{
		return policyExpirations;
	}

	public void setPolicyExpirations(Map<ModuleKeyDTO, HashMap<PolicyKeyDTO, Date>> policyExpirations)
	{
		if (policyExpirations != null)
		{
			this.policyExpirations = policyExpirations;
		}
	}

	public Date getDateForPolicy(PolicyKeyDTO policyKeyDTO) throws UnknownPolicyException
	{
		for (Entry<ModuleKeyDTO, HashMap<PolicyKeyDTO, Date>> entry : policyExpirations.entrySet())
		{
			Date date = entry.getValue().get(policyKeyDTO);
			if (date != null)
			{
				return date;
			}
		}
		throw new UnknownPolicyException(policyKeyDTO + " is not part of this consent");
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (consentExpirationDate == null ? 0 : consentExpirationDate.hashCode());
		result = prime * result + (gicsConsentDate == null ? 0 : gicsConsentDate.hashCode());
		result = prime * result + (legalConsentDate == null ? 0 : legalConsentDate.hashCode());
		result = prime * result + (maxPolicyExpirationDate == null ? 0 : maxPolicyExpirationDate.hashCode());
		result = prime * result + (moduleExpirations == null ? 0 : moduleExpirations.hashCode());
		result = prime * result + (policyExpirations == null ? 0 : policyExpirations.hashCode());
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
		ConsentDateValuesDTO other = (ConsentDateValuesDTO) obj;
		if (consentExpirationDate == null)
		{
			if (other.consentExpirationDate != null)
			{
				return false;
			}
		}
		else if (!consentExpirationDate.equals(other.consentExpirationDate))
		{
			return false;
		}
		if (gicsConsentDate == null)
		{
			if (other.gicsConsentDate != null)
			{
				return false;
			}
		}
		else if (!gicsConsentDate.equals(other.gicsConsentDate))
		{
			return false;
		}
		if (legalConsentDate == null)
		{
			if (other.legalConsentDate != null)
			{
				return false;
			}
		}
		else if (!legalConsentDate.equals(other.legalConsentDate))
		{
			return false;
		}
		if (maxPolicyExpirationDate == null)
		{
			if (other.maxPolicyExpirationDate != null)
			{
				return false;
			}
		}
		else if (!maxPolicyExpirationDate.equals(other.maxPolicyExpirationDate))
		{
			return false;
		}
		if (moduleExpirations == null)
		{
			if (other.moduleExpirations != null)
			{
				return false;
			}
		}
		else if (!moduleExpirations.equals(other.moduleExpirations))
		{
			return false;
		}
		if (policyExpirations == null)
		{
			if (other.policyExpirations != null)
			{
				return false;
			}
		}
		else if (!policyExpirations.equals(other.policyExpirations))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "ConsentDateValuesDTO [gicsConsentDate=" + gicsConsentDate + ", legalConsentDate=" + legalConsentDate + ", consentExpirationDate=" + consentExpirationDate + ", maxPolicyExpirationDate="
				+ maxPolicyExpirationDate + ", moduleExpirations=" + moduleExpirations + ", policyExpirations=" + policyExpirations + "]";
	}
}
