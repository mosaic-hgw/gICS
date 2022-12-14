package org.emau.icmvc.ganimed.ttp.cm2.model;

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

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * zusammengesetzter primaerschluessel fuer die m-n tabelle consent<->policy
 * 
 * @author geidell
 *
 */
@Embeddable
public class SignedPolicyKey implements Serializable
{
	private static final long serialVersionUID = -5100724403162681995L;
	@Column(insertable = false, updatable = false)
	private ConsentKey consentKey;
	@Column(insertable = false, updatable = false)
	private PolicyKey policyKey;

	public SignedPolicyKey()
	{}

	public SignedPolicyKey(ConsentKey consentKey, PolicyKey policyKey)
	{
		super();
		this.consentKey = consentKey;
		this.policyKey = policyKey;
	}

	public ConsentKey getConsentKey()
	{
		return consentKey;
	}

	public PolicyKey getPolicyKey()
	{
		return policyKey;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((consentKey == null) ? 0 : consentKey.hashCode());
		result = prime * result + ((policyKey == null) ? 0 : policyKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignedPolicyKey other = (SignedPolicyKey) obj;
		if (consentKey == null)
		{
			if (other.consentKey != null)
				return false;
		}
		else if (!consentKey.equals(other.consentKey))
			return false;
		if (policyKey == null)
		{
			if (other.policyKey != null)
				return false;
		}
		else if (!policyKey.equals(other.policyKey))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(policyKey);
		sb.append(" within ");
		sb.append(consentKey);
		return sb.toString();
	}
}
