package org.emau.icmvc.magic.fhir.datatypes;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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


import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.instance.model.api.ICompositeType;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;

/**
 * FHIR Datatype to hold gICS-Policy specific information
 * 
 * @author bialkem {@link mosaic-greifswald.de}
 */
@DatatypeDef(name = "ConsentPolicy") // , profile =
										// "http://example.com/StructureDefinition/dontuse#ConsentPolicy")
public class ConsentPolicy extends Type implements ICompositeType {

	private static final long serialVersionUID = -9055304335541419032L;

	@Child(name = "domainName", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "name of the assigned consent domain")
	private StringDt domainName = new StringDt();
	

	@Child(name = "name", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "name of consent policy")
	private StringDt name = new StringDt();

	@Child(name = "version", order = 2, min = 1, max = 1)
	@Description(shortDefinition = "version of consent policy")
	private StringDt version = new StringDt();

	
	@Child(name = "comment", order = 3, min = 1, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent policy")
	private StringDt comment = new StringDt();
	

	@Child(name = "externProperties", order = 4, min = 0, max = 1)	
	@Description(shortDefinition = "externProperties of consent policy")
	private StringDt externProperties= new StringDt();

	/**
	 * create new instance of ConsentPolicy
	 * 
	 * @param policyName
	 *            key value: name of Consent policy
	 * @param policyComment
	 *            Comment for consent policy
	 * @param policyVersion
	 *            key value: version for the consent policy
	 * @param domainName
	 *            key value: name of the consent domain this policy will be
	 *            applied to
	 */
	public ConsentPolicy(String policyName, String policyComment, String policyVersion, String domainName) {

		this();

		// set specifica
		this.setName(policyName);
		this.setVersion(policyVersion);
		this.setComment(policyComment);
		this.setDomainName(domainName);		
	}
	
	/**
	 * create new instance of ConsentPolicy
	 * 
	 * @param policyName
	 *            key value: name of Consent policy
	 * @param policyComment
	 *            Comment for consent policy
	 * @param policyVersion
	 *            key value: version for the consent policy
	 * @param domainName
	 *            key value: name of the consent domain this policy will be
	 *            applied to
	 */
	public ConsentPolicy(String policyName, String policyComment, String policyVersion, String domainName, String externProperties) {

		this();

		// set specifica
		this.setName(policyName);
		this.setVersion(policyVersion);
		this.setComment(policyComment);
		this.setDomainName(domainName);
		this.setExternProperties(externProperties);
	}

	/**
	 * create new instance of ConsentPolicy without comment
	 * 
	 * @param policyName
	 *            key value: name of Consent policy
	 * @param policyVersion
	 *            key value: version for the consent policy
	 * @param domainName
	 *            key value: name of the consent domain this policy will be
	 *            applied to
	 */
	public ConsentPolicy(String policyName, String policyVersion, String domainName) {

		this();

		// set specifica
		this.setName(policyName);
		this.setVersion(policyVersion);		
		this.setDomainName(domainName);		
	}

	/**
	 * create new instance of ConsentPolicy, using key value default version
	 * "1.0"
	 *
	 *
	 */
	public ConsentPolicy() {
		// default
		this.setVersion("1.0");
		this.setExternProperties("");
		this.setComment("");
	}

	/**
	 * get version of consent policy
	 * 
	 * @return version of consent policy
	 */
	public String getVersion() {
		if (version == null)
			version = new StringDt();
		return version.getValue();
	}

	/**
	 * set version of consent policy
	 * 
	 * @param policyversion
	 *            version of consent policy
	 */
	public ConsentPolicy setVersion(String policyversion) {
		if (policyversion == null) {
			throw new NullPointerException("Given policy version of type String is null.");
		}
		if (policyversion.isEmpty()) {
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
	public String getName() {
		if (name == null)
			name = new StringDt();
		return name.getValue();
	}

	/**
	 * set Name of consent policy
	 * 
	 * @param policyname
	 *            Name of consent policy
	 */
	public ConsentPolicy setName(String policyname) {
		if (policyname == null) {
			throw new NullPointerException("Given name of policy of type String is null.");
		}

		if (policyname.isEmpty()) {
			throw new NullPointerException("Given policy name of type String cannot be empty.");
		}
		name.setValue(policyname);
		return this;
	}

	/**
	 * get Name of assigned consent domain
	 * 
	 * @return name of assigned consent domain
	 */
	public String getDomainName() {
		if (domainName == null)
			domainName = new StringDt();
		return domainName.getValue();
	}

	/**
	 * set name of assigned consent domain
	 * 
	 * @param policyDomainName
	 *            name of assigned consent domain
	 */
	public ConsentPolicy setDomainName(String policyDomainName) {
		if (policyDomainName == null) {
			throw new NullPointerException("Given name of assigned consent domain of type String is null.");
		}

		if (policyDomainName.isEmpty()) {
			throw new NullPointerException("Given policy domainName of type String cannot be empty.");
		}

		this.domainName=new StringDt(policyDomainName);
		return this;
	}

	/**
	 * get comment to describe purpose of consent policy
	 * 
	 * @return comment to describe purpose of consent policy
	 */
	public String getComment() {
		if (comment == null)
			comment = new StringDt();
		return comment.getValue();
	}

	/**
	 * set comment to describe purpose of consent policy
	 * 
	 * @param policycomment
	 *            comment to describe purpose of consent policy
	 */
	public ConsentPolicy setComment(String policycomment) {
		if (policycomment == null) {
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
	public String getExternProperties() {
		if (externProperties == null)
			externProperties = new StringDt();
		return externProperties.getValue();
	}

	/**
	 * set externProperties of consent policy with 0-n properties separated by semicolon
	 * e.g. validity_period=p1y
	 * 
	 * @param externProperties
	 *            externProperties of consent policy
	 * @return instance of ConsentPolicy
	 */
	public ConsentPolicy setExternProperties(String externProperties) {
		if (externProperties == null) {
			throw new NullPointerException("Given policy externProperties of type String is null.");
		}
		this.externProperties.setValue(externProperties);
		return this;
	}
	

	/**
	 * get policies Key String separated with semicolon e.g.
	 * "domain;policyname;policyversion"
	 */
	public String toKeyString() {
		return getDomainName() + ";" + getName() + ";" + getVersion();
	}

	@Override
	public boolean isEmpty() {

		return ElementUtil.isEmpty(domainName, comment, name, version,externProperties);

	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsentPolicy other = (ConsentPolicy) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (domainName == null) {
			if (other.domainName != null)
				return false;
		} else if (!domainName.equals(other.domainName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		
		if (externProperties == null) {
			if (other.externProperties != null)
				return false;
		} else if (!externProperties.equals(other.externProperties))
			return false;
		
		return true;
	}



	@Override
	public String toString() {
		return "ConsentPolicy [version=" + version + ", name=" + name + ", domainName=" + domainName + ", comment="
				+ comment + ", externProperties="	+ externProperties+"]";
	}

	@Override
	protected Type typedCopy() {
		ConsentPolicy retValue = new ConsentPolicy();
		super.copyValues(retValue);
		retValue.setDomainName(this.getDomainName());
		retValue.setComment(this.getComment());
		retValue.setName(this.getName());
		retValue.setVersion(this.getVersion());
		retValue.setExternProperties(this.getExternProperties());
		return retValue;
	}

}
