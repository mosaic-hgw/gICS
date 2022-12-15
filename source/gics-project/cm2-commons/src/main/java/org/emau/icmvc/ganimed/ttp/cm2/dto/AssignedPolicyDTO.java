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
import java.util.Comparator;

/**
 * objekt fuer die m-n tabelle module <-> policy
 *
 * @author geidell
 */
public class AssignedPolicyDTO extends FhirIdDTO implements Serializable, Comparable<AssignedPolicyDTO>
{
	private static final long serialVersionUID = 7181774427028531682L;
	private PolicyDTO policy;
	private String comment;
	private String externProperties;
	private ExpirationPropertiesDTO expirationProperties;

	public AssignedPolicyDTO()
	{
		super(null);
	}

	public AssignedPolicyDTO(PolicyDTO policy)
	{
		super(null);
		this.policy = policy;
	}

	public AssignedPolicyDTO(PolicyDTO policy, String comment, String externProperties, ExpirationPropertiesDTO expirationProperties, String fhirID)
	{
		super(fhirID);
		this.policy = policy;
		this.comment = comment;
		this.externProperties = externProperties;
		this.expirationProperties = expirationProperties;
	}

	public PolicyDTO getPolicy()
	{
		return policy;
	}

	public void setPolicy(PolicyDTO policy)
	{
		this.policy = policy;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public void setExternProperties(String externProperties)
	{
		this.externProperties = externProperties;
	}

	public ExpirationPropertiesDTO getExpirationProperties()
	{
		return expirationProperties;
	}

	public void setExpirationProperties(ExpirationPropertiesDTO expirationProperties)
	{
		this.expirationProperties = expirationProperties;
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (expirationProperties == null ? 0 : expirationProperties.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (policy == null ? 0 : policy.hashCode());
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
		AssignedPolicyDTO other = (AssignedPolicyDTO) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (policy == null)
		{
			if (other.policy != null)
			{
				return false;
			}
		}
		else if (!policy.equals(other.policy))
		{
			return false;
		}
		return true;
	}

	public boolean equalsForFhirSerice(Object obj)
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
		AssignedPolicyDTO other = (AssignedPolicyDTO) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (policy == null)
		{
			if (other.policy != null)
			{
				return false;
			}
		}
		else if (!policy.equalsForFhirSerice(other.policy))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "AssignedPolicyDTO [policy=" + policy + ", comment=" + comment + ", externProperties=" + externProperties + ", expirationProperties=" + expirationProperties + "]";
	}

	private static final Comparator<AssignedPolicyDTO> COMPARATOR = Comparator
			.comparing((AssignedPolicyDTO ap) -> ap.getPolicy().getLabel(), Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing((AssignedPolicyDTO ap) -> ap.getPolicy().getKey().getName())
			.thenComparing((AssignedPolicyDTO ap) -> ap.getPolicy().getKey().getVersion());

	@Override public int compareTo(AssignedPolicyDTO o)
	{
		return COMPARATOR.compare(this, o);
	}
}
