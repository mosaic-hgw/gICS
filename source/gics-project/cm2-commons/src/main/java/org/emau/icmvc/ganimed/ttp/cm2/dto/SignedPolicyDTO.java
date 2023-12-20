package org.emau.icmvc.ganimed.ttp.cm2.dto;

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

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;

/**
 * objekt fuer die m-n tabelle consent<->policy
 *
 * @author geidell
 *
 */
public class SignedPolicyDTO extends FhirIdDTO implements Serializable, DomainRelated
{
	@Serial
	private static final long serialVersionUID = -5193741629914435075L;
	private ConsentStatus status;
	private PolicyKeyDTO policyKey;
	private ConsentKeyDTO consentKey;

	public SignedPolicyDTO()
	{
		super(null);
	}

	public SignedPolicyDTO(ConsentStatus status, PolicyKeyDTO policyKey, ConsentKeyDTO consentKey, String fhirID)
	{
		super(fhirID);
		this.status = status;
		setPolicyKey(policyKey);
		setConsentKey(consentKey);
	}

	public SignedPolicyDTO(SignedPolicyDTO dto)
	{
		this(dto.getStatus(), dto.getPolicyKey(), dto.getConsentKey(), dto.getFhirID());
	}

	public ConsentStatus getStatus()
	{
		return status;
	}

	public void setStatus(ConsentStatus status)
	{
		this.status = status;
	}

	public PolicyKeyDTO getPolicyKey()
	{
		return policyKey;
	}

	public void setPolicyKey(PolicyKeyDTO policyKey)
	{
		if (policyKey != null)
		{
			this.policyKey = new PolicyKeyDTO(policyKey);
		}
		else
		{
			this.policyKey = null;
		}
	}

	public ConsentKeyDTO getConsentKey()
	{
		return consentKey;
	}

	public void setConsentKey(ConsentKeyDTO consentKey)
	{
		if (consentKey != null)
		{
			this.consentKey = new ConsentKeyDTO(consentKey);
		}
		else
		{
			this.consentKey = null;
		}
	}

	@Override
	public String getDomainName()
	{
		return getPolicyKey().getDomainName();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (consentKey == null ? 0 : consentKey.hashCode());
		result = prime * result + (policyKey == null ? 0 : policyKey.hashCode());
		result = prime * result + (status == null ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		SignedPolicyDTO other = (SignedPolicyDTO) obj;
		if (consentKey == null)
		{
			if (other.consentKey != null)
			{
				return false;
			}
		}
		else if (!consentKey.equals(other.consentKey))
		{
			return false;
		}
		if (policyKey == null)
		{
			if (other.policyKey != null)
			{
				return false;
			}
		}
		else if (!policyKey.equals(other.policyKey))
		{
			return false;
		}
		if (status != other.status)
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("policy '");
		sb.append(policyKey);
		sb.append("' is signed with value: ");
		sb.append(status == null ? "'null'" : status.toString());
		sb.append(" within consent '");
		sb.append(consentKey);
		sb.append("'");
		sb.append(super.toString());
		return sb.toString();
	}
}
