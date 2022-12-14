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

import java.util.Date;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

/**
 * FHIR Datatype to hold gICS-Policy specific information
 *
 * @author bialkem <a href="http://mosaic-greifswald.de">mosaic-greifswald.de</a>
 */
@DatatypeDef(name = "ConsentPolicy") // , profile =
										// "http://example.com/StructureDefinition/dontuse#ConsentPolicy")
public class ConsentPolicy extends Type implements ICompositeType
{
	private static final long serialVersionUID = -9055304335541419032L;

	@Child(name = "domainName", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "name of the assigned consent domain")
	private StringDt domainName = new StringDt();

	@Child(name = "name", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "name of consent policy")
	private StringDt name = new StringDt();

	@Child(name = "label", order = 2, min = 0, max = 1)
	@Description(shortDefinition = "label of consent policy")
	private StringDt label = new StringDt();

	@Child(name = "version", order = 3, min = 1, max = 1)
	@Description(shortDefinition = "version of consent policy")
	private StringDt version = new StringDt();

	@Child(name = "comment", order = 4, min = 1, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent policy")
	private StringDt comment = new StringDt();

	@Child(name = "externProperties", order = 5, min = 0, max = 1)
	@Description(shortDefinition = "externProperties of consent policy")
	private StringDt externProperties = new StringDt();

	@Child(name = "finalized", order = 6, min = 0, max = 1)
	@Description(shortDefinition = "finalized status for policy")
	private BooleanDt finalized = new BooleanDt();

	@Child(name = "creationDate", order = 7, min = 0, max = 1)
	@Description(shortDefinition = "creation date of consent policy")
	private DateTimeDt creationDate = new DateTimeDt();

	/**
	 * create new instance of ConsentPolicy, using key value default version "1.0"
	 *
	 *
	 */
	public ConsentPolicy()
	{
		// default
		setVersion("1.0");
		setExternProperties("");
		setComment("");
		setFinalized(false);
	}

	/**
	 * get version of consent policy
	 *
	 * @return version of consent policy
	 */
	public String getVersion()
	{
		if (version == null)
		{
			version = new StringDt();
		}
		return version.getValue();
	}

	/**
	 * set version of consent policy
	 *
	 * @param policyversion
	 *            version of consent policy
	 */
	public ConsentPolicy setVersion(String policyversion)
	{
		if (policyversion == null)
		{
			throw new NullPointerException("Given policy version of type String is null.");
		}
		if (policyversion.isEmpty())
		{
			throw new NullPointerException("Given policy version of type String cannot be empty.");
		}
		version.setValue(policyversion);
		return this;
	}

	/**
	 * get Name of consent policy
	 *
	 * @return Name of consent policy
	 */
	public String getName()
	{
		if (name == null)
		{
			name = new StringDt();
		}
		return name.getValue();
	}

	/**
	 * set Name of consent policy
	 *
	 * @param policyname
	 *            Name of consent policy
	 */
	public ConsentPolicy setName(String policyname)
	{
		if (policyname == null)
		{
			throw new NullPointerException("Given name of policy of type String is null.");
		}

		if (policyname.isEmpty())
		{
			throw new NullPointerException("Given policy name of type String cannot be empty.");
		}
		name.setValue(policyname);
		return this;
	}

	/**
	 * get consent policy finalized status
	 *
	 * @return policy finalized status
	 */
	public Boolean getFinalized()
	{
		if (finalized == null)
		{
			finalized = new BooleanDt();
		}
		return finalized.getValue();
	}

	/**
	 * set consent policy finalized status
	 *
	 * @param finalstate
	 *            finalized status
	 *            policy finalized status
	 * @return instance of consent policy
	 */
	public ConsentPolicy setFinalized(Boolean finalstate)
	{
		if (finalstate == null)
		{
			throw new NullPointerException("Given final state of type Boolean is null.");
		}

		finalized.setValue(finalstate);
		return this;
	}

	/**
	 * get Label of consent policy
	 *
	 * @return Label of consent policy
	 */
	public String getLabel()
	{
		if (label == null)
		{
			label = new StringDt();
		}
		return label.getValue();
	}

	/**
	 * set Label of consent policy
	 *
	 * @param policyLabel
	 *            Label of consent policy
	 */
	public ConsentPolicy setLabel(String policyLabel)
	{
		if (policyLabel == null)
		{
			throw new NullPointerException("Given policyLabel of type String is null.");
		}

		if (policyLabel.isEmpty())
		{
			throw new NullPointerException("Given policyLabel of type String cannot be empty.");
		}
		label.setValue(policyLabel);
		return this;
	}

	/**
	 * get Name of assigned consent domain
	 *
	 * @return name of assigned consent domain
	 */
	public String getDomainName()
	{
		if (domainName == null)
		{
			domainName = new StringDt();
		}
		return domainName.getValue();
	}

	/**
	 * set name of assigned consent domain
	 *
	 * @param policyDomainName
	 *            name of assigned consent domain
	 */
	public ConsentPolicy setDomainName(String policyDomainName)
	{
		if (policyDomainName == null)
		{
			throw new NullPointerException("Given name of assigned consent domain of type String is null.");
		}

		if (policyDomainName.isEmpty())
		{
			throw new NullPointerException("Given policy domainName of type String cannot be empty.");
		}

		domainName = new StringDt(policyDomainName);
		return this;
	}

	/**
	 * get comment to describe purpose of consent policy
	 *
	 * @return comment to describe purpose of consent policy
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
	 * set comment to describe purpose of consent policy
	 *
	 * @param policycomment
	 *            comment to describe purpose of consent policy
	 */
	public ConsentPolicy setComment(String policycomment)
	{
		if (policycomment == null)
		{
			throw new NullPointerException("Given Comment of type String is null.");
		}

		comment.setValue(policycomment);
		return this;
	}

	/**
	 * get externProperties of consent policy
	 *
	 * @return externProperties of consent policy
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
	 * set externProperties of consent policy with 0-n properties separated by semicolon e.g.
	 * validity_period=p1y
	 *
	 * @param externProperties
	 *            externProperties of consent policy
	 * @return instance of ConsentPolicy
	 */
	public ConsentPolicy setExternProperties(String externProperties)
	{
		if (externProperties == null)
		{
			throw new NullPointerException("Given policy externProperties of type String is null.");
		}
		this.externProperties.setValue(externProperties);
		return this;
	}

	/**
	 * Return the creation date
	 * @return the creation date
	 */
	public Date getCreationDate()
	{
		if (creationDate == null)
		{
			creationDate = new DateTimeDt();
		}
		return creationDate.getValue();
	}

	/**
	 * Set the creation date
	 * @param creationDate the creation date
	 */
	public ConsentPolicy setCreationDate(Date creationDate)
	{
		this.creationDate = new DateTimeDt(creationDate);
		return this;
	}

	/**
	 * get policies Key String separated with semicolon e.g. "domain;policyname;policyversion"
	 */
	public String toKeyString()
	{
		return getDomainName() + ";" + getName() + ";" + getVersion();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((finalized == null) ? 0 : finalized.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
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
		ConsentPolicy other = (ConsentPolicy) obj;
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
		if (domainName == null)
		{
			if (other.domainName != null)
			{
				return false;
			}
		}
		else if (!domainName.equals(other.domainName))
		{
			return false;
		}
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		if (label == null)
		{
			if (other.label != null)
			{
				return false;
			}
		}
		else if (!label.equals(other.label))
		{
			return false;
		}

		if (finalized == null)
		{
			if (other.finalized != null)
			{
				return false;
			}
		}
		else if (!finalized.equals(other.finalized))
		{
			return false;
		}

		if (version == null)
		{
			if (other.version != null)
			{
				return false;
			}
		}
		else if (!version.equals(other.version))
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
		if (creationDate == null)
		{
			if (other.creationDate != null)
			{
				return false;
			}
		}
		else if (!creationDate.equals(other.creationDate))
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "ConsentPolicy [version=" + version + ", name=" + name + ", label=" + label
				+ ", finalized=" + finalized + ", domainName=" + domainName + ", comment="
				+ comment + ", externProperties=" + externProperties + ", creationDate=" + creationDate + "]";
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(domainName, comment, name, label, version, externProperties, creationDate);
	}

	@Override
	protected Type typedCopy()
	{
		ConsentPolicy retValue = new ConsentPolicy();
		super.copyValues(retValue);
		retValue.setDomainName(getDomainName());
		retValue.setComment(getComment());
		retValue.setName(getName());
		retValue.setLabel(getLabel());
		retValue.setVersion(getVersion());
		retValue.setExternProperties(getExternProperties());
		retValue.setFinalized(getFinalized());
		retValue.setCreationDate((getCreationDate()));
		return retValue;
	}

}
