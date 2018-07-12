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


import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.instance.model.api.ICompositeType;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.Base64BinaryDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;

/**
 * FHIR Datatype to hold gICS-Domain specific information
 * 
 * @author bialkem {@link mosaic-greifswald.de}
 */
@DatatypeDef(name = "ConsentDomain")
public class ConsentDomain extends Type implements ICompositeType {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7519192229124707347L;

	
	@Child(name = "name", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "name for the consent domain")
	private StringDt name = new StringDt();
	
	@Child(name = "label", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "label for the consent domain")
	private StringDt label = new StringDt();

	@Child(name = "comment", order = 2, min = 0, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent domain")
	private StringDt comment = new StringDt();

	@Child(name = "signerIdType", order = 3, min = 1, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of signed id types to be associated with consent information within the consent domain")
	private List<StringDt> signerIdTypes = new ArrayList<StringDt>();

	@Child(name = "policyVersionConverter", order = 4, min = 1, max = 1)
	@Description(shortDefinition = "consent policy Java Version Converter")
	private StringDt policyVersionConverter = new StringDt();

	@Child(name = "moduleVersionConverter", order = 5, min = 1, max = 1)	
	@Description(shortDefinition = "consent module Java Version Converter")
	private StringDt moduleVersionConverter = new StringDt();

	@Child(name = "consentTemplateVersionConverter", order = 6, min = 1, max = 1)	
	@Description(shortDefinition = "consent Template Java Version Converter")
	private StringDt ctVersionConverter = new StringDt();

	@Child(name = "logo", order = 7, min = 0, max = 1)
	@Description(shortDefinition = "domain logo as base64 encoded stream of bytes")
	private Base64BinaryDt logo = new Base64BinaryDt();
	
	@Child(name = "properties", order = 8, min = 0, max = 1)	
	@Description(shortDefinition = "properties of consent domain")
	private StringDt properties= new StringDt();

	
	@Child(name = "externProperties", order = 9, min = 0, max = 1)	
	@Description(shortDefinition = "externProperties of consent domain")
	private StringDt externProperties= new StringDt();


	private String defaultConverter = "org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter";
	
	/**
	 * create new instance of ConsentDomain
	 */
	public ConsentDomain() {
		
		// set default values
		this.setConsentTemplateVersionConverter(defaultConverter);
		this.setModuleVersionConverter(defaultConverter);
		this.setPolicyVersionConverter(defaultConverter);
		this.setComment("");
		this.setProperties("");
		this.setExternProperties("");

	}

	/**
	 * create new instance of ConsentDomain
	 * 
	 * @param domainName
	 *            name of Consent Domain
	 * @param domainLabel
	 *            label for the consent domain
	 * @param domainComment
	 *            Comment for consent domain
	 * @param domainSignerIdTypes
	 *            list of signedIdTypes (semicolon separated) to be associated
	 *            with consent information within the consent domain
	 */
	public ConsentDomain(String domainName, String domainLabel, String domainComment, List<String> domainSignerIdTypes) {
		// init default values
		this();
		// set specifica
		this.setName(domainName);
		this.setLabel(domainLabel);
		this.setComment(domainComment);
		this.setSignerIdTypes(domainSignerIdTypes);
	}
	
	/**
	 * create new instance of ConsentDomain
	 * 
	 * @param domainName
	 *            name of Consent Domain
	 * @param domainLabel
	 *            label for the consent domain
	 * @param domainComment
	 *            Comment for consent domain
	 * @param domainSignerIdTypes
	 *            list of signedIdTypes (semicolon separated) to be associated
	 *            with consent information within the consent domain
	 * @param properties
	 *            domain properties
	 * @param externProperties
	 *            domain extern properties
	 
	 */
	public ConsentDomain(String domainName, String domainLabel, String domainComment, List<String> domainSignerIdTypes,String properties,String externProperties) {
		
		this(domainName,domainLabel,domainComment,domainSignerIdTypes);
		// set specifica
		this.setProperties(properties);
		this.setExternProperties(externProperties);		
	}

	/**
	 * get consent domain name
	 * 
	 * @return domain name
	 */
	public String getName() {
		if (name == null)
			name = new StringDt();
		return name.getValue();
	}

	/**
	 * set consent domain name
	 * 
	 * @param domainName
	 *            name of domain
	 * @return instance of consent domain
	 */
	public ConsentDomain setName(String domainName) {
		if (domainName == null || domainName.isEmpty()) {
			throw new NullPointerException("Given domainName of type String is null or empty.");
		}

		this.name.setValue(domainName);
		return this;
	}

	/**
	 * get label for consent domain
	 * 
	 * @return label of consent domain
	 */
	public String getLabel() {
		if (label == null)
			label = new StringDt();
		return label.getValueAsString();
	}

	/**
	 * set label for the consent domain
	 * 
	 * @param domainLabel
	 *            label for the consent domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain setLabel(String domainLabel) {
		if (domainLabel == null) {
			throw new NullPointerException("Given label of type String is null.");
		}

		this.label.setValue(domainLabel);
		return this;
	}

	/**
	 * get comment to describe purpose of consent domain
	 * 
	 * @return comment to describe purpose of consent domain
	 */
	public String getComment() {
		if (comment == null)
			comment = new StringDt();
		return comment.getValueAsString();
	}

	/**
	 * set comment to describe purpose of consent domain
	 * 
	 * @param domainComment
	 *            comment to describe purpose of consent domain
	 * @return instance of consent domain
	 */
	public ConsentDomain setComment(String domainComment) {
		if (domainComment == null) {
			throw new NullPointerException("Given Comment of type String is null.");
		}

		this.comment.setValue(domainComment);
		return this;
	}

	
	/**
	 * get list of signedidtypes to be associated with consent information
	 * within the consent domain
	 * 
	 * @return list of signedidtypes (semicolon separated) to be associated with
	 *         consent information within the consent domain
	 */
	public List<String> getSignerIdTypes() {
		if (signerIdTypes == null)
			signerIdTypes = new ArrayList<StringDt>();
		
		List<String> result = new ArrayList<String>();
		
		for (StringDt idType : signerIdTypes) {
			result.add(idType.getValue());
		}
		
		return result;
	}

	

	/**
	 * set signedidtypes to be associated with consent information within the
	 * consent domain
	 * 
	 * @param domainSignerIdTypes
	 *            list of signed id types (semicolon separated) to be associated
	 *            with consent information within the consent domain
	 * @return instance of consent domain
	 */
	public ConsentDomain setSignerIdTypes(List<String> domainSignerIdTypes) {
	
		if (domainSignerIdTypes != null && domainSignerIdTypes.size() > 0) {
			
			this.signerIdTypes = new ArrayList<StringDt>();
			
			for (String idType : domainSignerIdTypes) {
				
				if(!idType.isEmpty())
					this.signerIdTypes.add(new StringDt(idType));
			}
			
		}
		
		return this;
	}
	
	/**
	 * add signer id type for consent domain
	 * 
	 * @param idType
	 *            signer id type (feel free to define) to be added to domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain addSignerIdType(String idType) {
		if (idType == null || idType.isEmpty())
			throw new NullPointerException("Given idType o type String is null or empty.");
		if(signerIdTypes!=null && !signerIdTypes.contains(new StringDt(idType))){
			signerIdTypes.add(new StringDt(idType));
		}
		
		return this;
	}

		
	/**
	 * remove signed id type
	 * 
	 * @param idType
	 *            signer id type to be removed from consent domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain removeSignerIdType(String idType) {
		if(idType != null && !idType.isEmpty())
		{
			StringDt signerId = new StringDt(idType);
			
			if (signerIdTypes.contains(signerId)) {
				
				signerIdTypes.remove(signerId);
			}
		}
		
		return this;
	}

	/**
	 * get consent Template Java Version Converter
	 * 
	 * @return consent template Java Version Converter
	 */
	public String getConsentTemplateVersionConverter() {
		if (ctVersionConverter == null)
			ctVersionConverter = new StringDt();
		return ctVersionConverter.getValue();
	}

	/**
	 * set consent template Java Version Converter
	 * 
	 * @param consentTemplateVersionConverter
	 *            consent template Java Version Converter
	 * @return instance of consent domain
	 */
	public ConsentDomain setConsentTemplateVersionConverter(String consentTemplateVersionConverter) {
		if (consentTemplateVersionConverter == null) {
			throw new NullPointerException("Given consent template VersionConverter of type String is null.");
		}

		ctVersionConverter.setValue(consentTemplateVersionConverter);
		return this;
	}

		/**
	 * get consent module Java Version Converter
	 * 
	 * @return consent module Java Version Converter
	 */
	public String getModuleVersionConverter() {
		if (moduleVersionConverter == null)
			moduleVersionConverter = new StringDt();
		return moduleVersionConverter.getValue();
	}

	/**
	 * set consent module Java Version Converter
	 * 
	 * @param moduleVersionConverter
	 *            consent module Java Version Converter
	 * @return instance of consent domain
	 */
	public ConsentDomain setModuleVersionConverter(String moduleVersionConverter) {
		if (moduleVersionConverter == null) {
			throw new NullPointerException("Given moduleVersionConverter of type String is null.");
		}

		this.moduleVersionConverter.setValue(moduleVersionConverter);
		return this;
	}

	
	/**
	 * get consent policy Java Version Converter
	 * 
	 * @return consent policy Java Version Converter
	 */
	public String getPolicyVersionConverter() {
		if (policyVersionConverter == null)
			policyVersionConverter = new StringDt();
		return policyVersionConverter.getValue();
	}

	/**
	 * set consent policy Java Version Converter
	 * 
	 * @param policyVersionConverter
	 *            consent policy Java Version Converter
	 * @return instance of consent domain
	 */
	public ConsentDomain setPolicyVersionConverter(String policyVersionConverter) {
		if (policyVersionConverter == null) {
			throw new NullPointerException("Given policyVersionConverter of type String is null.");
		}

		this.policyVersionConverter.setValue(policyVersionConverter);
		return this;
	}

	/**
	 * get domain logo as base64 encoded stream of bytes
	 * 
	 * @return domain logo as base64 encoded stream of bytes
	 */
	public byte[] getLogo() {
		if (logo == null)
			logo = new Base64BinaryDt();
		return logo.getValue();
	}

	/**
	 * set domain logo as base64 encoded stream of bytes
	 * 
	 * @param domainLogo
	 *            as base64 encoded stream of bytes
	 * @return instance of consent domain
	 */
	public ConsentDomain setLogo(byte[] domainLogo) {
		if (domainLogo == null || domainLogo.length == 0) {
			throw new NullPointerException("Given logo of type byte[] is null or empty.");
		}

		logo.setValue(domainLogo);
		return this;
	}
	
	/**
	 * get properties of consent domain 
	 * 
	 * @return properties of consent domain
	 */
	public String getProperties() {
		if (properties == null)
			properties = new StringDt();
		return properties.getValue();
	}

	/**
	 * set properties of consent domain with 0-n properties separated by semicolon
	 * e.g. validity_period=p1y
	 * 
	 * @param properties
	 *            properties of consent domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain setProperties(String properties) {
		if (properties == null) {
			throw new NullPointerException("Given domain properties of type String is null.");
		}
		this.properties.setValue(properties);
		return this;
	}
	
	/**
	 * get externProperties of consent domain 
	 * 
	 * @return externProperties of consent domain
	 */
	public String getExternProperties() {
		if (externProperties == null)
			externProperties = new StringDt();
		return externProperties.getValue();
	}

	/**
	 * set externProperties of consent domain with 0-n properties separated by semicolon
	 * e.g. validity_period=p1y
	 * 
	 * @param externProperties
	 *            externProperties of consent domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain setExternProperties(String externProperties) {
		if (externProperties == null) {
			throw new NullPointerException("Given domain externProperties of type String is null.");
		}
		this.externProperties.setValue(externProperties);
		return this;
	}
	
	

	@Override
	public boolean isEmpty() {
		return ElementUtil.isEmpty(name, label, comment, logo, signerIdTypes, ctVersionConverter, moduleVersionConverter, policyVersionConverter,properties,externProperties);
	}

	@Override
	public String toString() {

		String logoString = "no";

		if (logo.getValue() != null && logo.getValue().length > 0)
			logoString = "yes";

		return "ConsentDomain [_label=" + label + ", _comment=" + comment + ", _signerIdTypes=" + signerIdTypes
				+ ", _ctVersionConverter=" + ctVersionConverter + ", _moduleVersionConverter="
				+ moduleVersionConverter + ", _policyVersionConverter=" + policyVersionConverter + ", _logo="
				+ logoString + ", properties=" + properties + ", exterProperties=" + externProperties+  "]";
	}

	@Override
	protected Type typedCopy() {
		ConsentDomain retValue = new ConsentDomain();
		super.copyValues(retValue);
		retValue.setName(this.getName());
		retValue.setLabel(this.getLabel());
		retValue.setComment(this.getComment());
		retValue.setLogo(this.getLogo());
		retValue.setConsentTemplateVersionConverter(this.getConsentTemplateVersionConverter());
		retValue.setPolicyVersionConverter(this.getPolicyVersionConverter());
		retValue.setModuleVersionConverter(this.getModuleVersionConverter());
		retValue.setSignerIdTypes(this.getSignerIdTypes());
		retValue.setExternProperties(this.getExternProperties());
		retValue.setProperties(this.getProperties());
		
		return retValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((ctVersionConverter == null) ? 0 : ctVersionConverter.hashCode());
		result = prime * result + ((defaultConverter == null) ? 0 : defaultConverter.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((logo == null) ? 0 : logo.hashCode());
		result = prime * result + ((moduleVersionConverter == null) ? 0 : moduleVersionConverter.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((policyVersionConverter == null) ? 0 : policyVersionConverter.hashCode());
		result = prime * result + ((signerIdTypes == null) ? 0 : signerIdTypes.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
		ConsentDomain other = (ConsentDomain) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (ctVersionConverter == null) {
			if (other.ctVersionConverter != null)
				return false;
		} else if (!ctVersionConverter.equals(other.ctVersionConverter))
			return false;
		if (defaultConverter == null) {
			if (other.defaultConverter != null)
				return false;
		} else if (!defaultConverter.equals(other.defaultConverter))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (logo == null) {
			if (other.logo != null)
				return false;
		} else if (!logo.equals(other.logo))
			return false;
		if (moduleVersionConverter == null) {
			if (other.moduleVersionConverter != null)
				return false;
		} else if (!moduleVersionConverter.equals(other.moduleVersionConverter))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (policyVersionConverter == null) {
			if (other.policyVersionConverter != null)
				return false;
		} else if (!policyVersionConverter.equals(other.policyVersionConverter))
			return false;
		if (signerIdTypes == null) {
			if (other.signerIdTypes != null)
				return false;
		} else if (!signerIdTypes.equals(other.signerIdTypes))
			return false;

		if (externProperties == null) {
				if (other.externProperties != null)
					return false;
			} else if (!externProperties.equals(other.externProperties))
				return false;

		if (properties == null) {
				if (other.properties != null)
					return false;
			} else if (!properties.equals(other.properties))
				return false;
		return true;
	}
	
	
}
