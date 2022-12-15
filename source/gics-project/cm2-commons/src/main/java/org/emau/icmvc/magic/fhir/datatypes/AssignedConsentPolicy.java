package org.emau.icmvc.magic.fhir.datatypes;

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

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

/**
 * FHIR Datatype to hold assigned policy specific information for templates
 *
 * @author bialkem
 *
 */
@DatatypeDef(name = "AssignedConsentPolicy")
public class AssignedConsentPolicy extends Type implements ICompositeType
{
	/**
	 *
	 */
	private static final long serialVersionUID = -2690710316156165258L;

	@Child(name = "policyKey", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "unique policy key")
	private StringDt policyKey = new StringDt();

	@Child(name = "comment", order = 1, min = 0, max = 1)
	@Description(shortDefinition = "comment to describe purpose of AssignedConsentPolicy")
	private StringDt comment = new StringDt();

	@Child(name = "externProperties", order = 2, min = 0, max = 1)
	@Description(shortDefinition = "externProperties of assigned policies")
	private StringDt externProperties = new StringDt();

	@Child(name = "expirationProperties", order = 3, min = 0, max = 1)
	@Description(shortDefinition = "expirationProperties of assigned policies")
	private StringDt expirationProperties = new StringDt();

	public AssignedConsentPolicy()
	{}

	/**
	 * create new instance of AssignedConsentPolicy using only obligatory parameters
	 *
	 * @param policyKey
	 *            policy reference as semicolon separated string
	 */
	public AssignedConsentPolicy(String policyKey)
	{
		setPolicyKey(policyKey);
		setComment("");
		setExternProperties("");
		setExpirationProperties("");
	}





	public AssignedConsentPolicy setPolicyKeyFromDTO(PolicyKeyDTO key)
	{
		setPolicyKey(key.getDomainName() + ";" + key.getName() + ";" + key.getVersion());
		return this;
	}

	/***
	 *
	 * create new instance of AssignedConsentPolicy using additional parameters
	 *
	 * @param policyKey
	 *            policy reference as semicolon separated string
	 * @param comment
	 *            comment
	 * @param externProperties
	 *            externProperties for assigned policy
	 * @param expirationProperties
	 *            expirationProperties for assigned policy
	 */
	public AssignedConsentPolicy(String policyKey, String comment, String externProperties, String expirationProperties)
	{
		this(policyKey);
		setComment(comment);
		setExternProperties(externProperties);
		setExpirationProperties(expirationProperties);
	}

	/**
	 * get comment to describe purpose of AssignedConsentPolicy
	 *
	 * @return comment to describe purpose of AssignedConsentPolicy
	 */
	public String getComment()
	{
		if (comment == null)
		{
			comment = new StringDt();
		}
		return comment.getValue();
	}

	/**
	 * set comment to describe purpose of AssignedConsentPolicy
	 *
	 * @param moduleComment
	 *            comment to describe purpose of AssignedConsentPolicy
	 * @return instance of AssignedConsentPolicy
	 */
	public AssignedConsentPolicy setComment(String moduleComment)
	{
		if (moduleComment == null)
		{
			throw new NullPointerException("Given AssignedConsentPolicy comment of type String is null.");
		}

		comment.setValue(moduleComment);
		return this;
	}

	/**
	 * get policy reference as semicolon separated string
	 *
	 * @return referenced policy key
	 */
	public String getPolicyKeyString()
	{
		if (policyKey == null)
		{
			policyKey = new StringDt();
		}

		return policyKey.getValue();
	}

	/**
	 * setpolicy reference as semicolon separated string
	 *
	 * @param consentpolicykey
	 *            referenced policy to be assigned to module, as semicolon separated string
	 * @return instance of AssignedConsentPolicy
	 */
	public AssignedConsentPolicy setPolicyKey(String consentpolicykey)
	{
		if (consentpolicykey == null || consentpolicykey.isEmpty())
		{
			throw new NullPointerException("Given policy key of type String is null or empty.");
		}

		policyKey.setValue(consentpolicykey);

		return this;
	}

	/**
	 * get externProperties of assigned policy
	 *
	 * @return externProperties of assigned policy
	 */
	public String getExternProperties()
	{
		if (externProperties == null)
		{
			externProperties = new StringDt();
		}
		return externProperties.getValue();
	}

	/**
	 * set externProperties of assigned policy with 0-n properties separated by semicolon e.g.
	 * validity_period=p1y
	 *
	 * @param externProperties
	 *            externProperties of assigned policy
	 * @return instance of Assigned policy
	 */
	public AssignedConsentPolicy setExternProperties(String externProperties)
	{
		if (externProperties == null)
		{
			throw new NullPointerException("Given externProperties of type String is null.");
		}

		this.externProperties.setValue(externProperties);
		return this;
	}

	/**
	 * get expirationProperties of assigned policy
	 *
	 * @return expirationProperties of assigned policy
	 */
	public String getExpirationProperties()
	{
		if (expirationProperties == null)
		{
			expirationProperties = new StringDt();
		}
		return expirationProperties.getValue();
	}

	/**
	 * set expirationProperties of assigned policy
	 *
	 * @param expirationProperties
	 *            expirationProperties of assigned policy
	 * @return instance of Assigned policy
	 */
	public AssignedConsentPolicy setExpirationProperties(String expirationProperties)
	{
		if (expirationProperties == null)
		{
			throw new NullPointerException("Given expirationProperties of type String is null.");
		}
		this.expirationProperties.setValue(expirationProperties);
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((expirationProperties == null) ? 0 : expirationProperties.hashCode());
		result = prime * result + ((policyKey == null) ? 0 : policyKey.hashCode());
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
		AssignedConsentPolicy other = (AssignedConsentPolicy) obj;
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

		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("key=" + policyKey + ";");
		sb.append("comment=" + getComment() + ";");
		sb.append("externalproperties=" + getExternProperties());
		sb.append("expirationProperties=" + getExpirationProperties());

		return sb.toString();
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(comment, policyKey, externProperties, expirationProperties);
	}

	@Override
	protected Type typedCopy()
	{
		AssignedConsentPolicy retValue = new AssignedConsentPolicy();
		super.copyValues(retValue);

		retValue.setComment(getComment());
		retValue.setPolicyKey(getPolicyKeyString());
		retValue.setExternProperties(getExternProperties());
		retValue.setExpirationProperties(getExpirationProperties());
		return retValue;
	}
}
