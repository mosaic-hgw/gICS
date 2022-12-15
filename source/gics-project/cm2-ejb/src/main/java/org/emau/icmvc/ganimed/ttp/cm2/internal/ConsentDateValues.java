package org.emau.icmvc.ganimed.ttp.cm2.internal;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDateValuesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownPolicyException;
import org.emau.icmvc.ganimed.ttp.cm2.model.ConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleConsentTemplate;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModuleKey;
import org.emau.icmvc.ganimed.ttp.cm2.model.ModulePolicy;
import org.emau.icmvc.ganimed.ttp.cm2.model.PolicyKey;

public class ConsentDateValues implements Serializable
{
	private static final long serialVersionUID = 3926519285636638051L;
	private static final Logger logger = LogManager.getLogger(ConsentDateValues.class);
	public static final Date START_DATE = new Date(0l);
	private Long gicsConsentTimestamp = 0l;
	private Long legalConsentTimestamp = 0l;
	private Long consentExpirationTimestamp = Long.MAX_VALUE;
	private Map<PolicyKey, Long> policyExpirations = new HashMap<>();
	private ConsentDateValuesDTO consentDateValuesDTO = null;
	private final Object consentDateValuesDTOSyncObject = new Object();

	public ConsentDateValues()
	{
		super();
	}

	public Long getGicsConsentTimestamp()
	{
		return gicsConsentTimestamp;
	}

	public void setGicsConsentTimestamp(Long gicsConsentTimestamp)
	{
		this.gicsConsentTimestamp = gicsConsentTimestamp;
	}

	public Long getLegalConsentTimestamp()
	{
		return legalConsentTimestamp;
	}

	public void setLegalConsentTimestamp(Long legalConsentTimestamp)
	{
		this.legalConsentTimestamp = legalConsentTimestamp;
	}

	public Long getConsentExpirationTimestamp()
	{
		return consentExpirationTimestamp;
	}

	public void setConsentExpirationTimestamp(Long consentExpirationTimestamp)
	{
		this.consentExpirationTimestamp = consentExpirationTimestamp;
	}

	public Map<PolicyKey, Long> getPolicyExpirations()
	{
		return policyExpirations;
	}

	public void setPolicyExpirations(Map<PolicyKey, Long> policyExpirations)
	{
		this.policyExpirations = policyExpirations;
	}

	public Long getTimestampForPolicy(PolicyKey policyKey) throws UnknownPolicyException
	{
		Long result = policyExpirations.get(policyKey);
		if (result == null)
		{
			throw new UnknownPolicyException(policyKey + " is not part of this consent");
		}
		return result;
	}

	public ConsentDateValuesDTO calculateConsentDateValuesDTO(ConsentTemplate template)
	{
		ConsentDateValuesDTO result = new ConsentDateValuesDTO();
		Date legalConsentDate = new Date(legalConsentTimestamp.longValue());
		Date consentExpiration = new Date(consentExpirationTimestamp.longValue());
		result.setLegalConsentDate(legalConsentDate);
		result.setGicsConsentDate(new Date(gicsConsentTimestamp.longValue()));
		result.setConsentExpirationDate(consentExpiration);
		try
		{
			Date maxPolicyExpiration = START_DATE;
			for (ModuleConsentTemplate moduleCTP : template.getModuleConsentTemplates())
			{
				ModuleKey moduleKey = moduleCTP.getModule().getKey();
				Date moduleExpiration = moduleCTP.getExpirationPropertiesObject().getExpirationDateForConsentDate(legalConsentDate);
				moduleExpiration = moduleExpiration.before(consentExpiration) ? moduleExpiration : consentExpiration;
				ModuleKeyDTO moduleKeyDTO = moduleKey
						.toDTO(VersionConverterCache.getModuleVersionConverter(template.getKey().getDomainName()));
				result.getModuleExpirations().put(moduleKeyDTO, moduleExpiration);
				HashMap<PolicyKeyDTO, Date> policyMap = new HashMap<>();
				result.getPolicyExpirations().put(moduleKeyDTO, policyMap);
				for (ModulePolicy mp : moduleCTP.getModule().getModulePolicies())
				{
					PolicyKeyDTO policyKeyDTO = mp.getPolicy().getKey()
							.toDTO(VersionConverterCache.getPolicyVersionConverter(template.getKey().getDomainName()));
					Date policyExpiration = new Date(policyExpirations.get(mp.getPolicy().getKey()).longValue());
					policyExpiration = policyExpiration.before(moduleExpiration) ? policyExpiration : moduleExpiration;
					policyMap.put(policyKeyDTO, policyExpiration);
					if (policyExpiration.after(maxPolicyExpiration))
					{
						maxPolicyExpiration = policyExpiration;
					}
				}
			}
			result.setMaxPolicyExpirationDate(maxPolicyExpiration);
		}
		catch (InvalidVersionException | UnknownDomainException impossible)
		{
			logger.fatal("impossible exception while calculating policy expiration dates", impossible);
		}
		return result;
	}

	public ConsentDateValuesDTO toDTO(ConsentTemplate template)
	{
		synchronized (consentDateValuesDTOSyncObject)
		{
			if (consentDateValuesDTO == null)
			{
				consentDateValuesDTO = calculateConsentDateValuesDTO(template);
			}
		}
		return consentDateValuesDTO;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (consentExpirationTimestamp == null ? 0 : consentExpirationTimestamp.hashCode());
		result = prime * result + (gicsConsentTimestamp == null ? 0 : gicsConsentTimestamp.hashCode());
		result = prime * result + (legalConsentTimestamp == null ? 0 : legalConsentTimestamp.hashCode());
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
		ConsentDateValues other = (ConsentDateValues) obj;
		if (consentExpirationTimestamp == null)
		{
			if (other.consentExpirationTimestamp != null)
			{
				return false;
			}
		}
		else if (!consentExpirationTimestamp.equals(other.consentExpirationTimestamp))
		{
			return false;
		}
		if (gicsConsentTimestamp == null)
		{
			if (other.gicsConsentTimestamp != null)
			{
				return false;
			}
		}
		else if (!gicsConsentTimestamp.equals(other.gicsConsentTimestamp))
		{
			return false;
		}
		if (legalConsentTimestamp == null)
		{
			if (other.legalConsentTimestamp != null)
			{
				return false;
			}
		}
		else if (!legalConsentTimestamp.equals(other.legalConsentTimestamp))
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
		return "ConsentDateValues [gicsConsentTimestamp=" + gicsConsentTimestamp + ", legalConsentTimestamp=" + legalConsentTimestamp + ", consentExpirationTimestamp=" + consentExpirationTimestamp
				+ ", policyExpirations=" + policyExpirations + "]";
	}
}
