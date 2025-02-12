package org.emau.icmvc.magic.fhir.datatypes;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.Base64BinaryDt;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.magic.fhir.datatypes.config.ConsentDomainConfig;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

/**
 * FHIR Datatype to hold gICS-Domain specific information
 *
 * @author bialkem <a href="http://mosaic-greifswald.de">mosaic-greifswald.de</a>
 */
@DatatypeDef(name = "ConsentDomain")
public class ConsentDomain extends Type implements ICompositeType
{
	@Serial
	private static final long serialVersionUID = -7519192229124707347L;

	private static final String DEFAULT_CONVERTER = "org.emau.icmvc.ganimed.ttp.cm2.version.MajorMinorVersionConverter";

	@Child(name = "name", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "name for the consent domain")
	private StringDt name = new StringDt();

	@Child(name = "finalized", order = 1, min = 0, max = 1)
	@Description(shortDefinition = "finalized status for the consent domain")
	private BooleanDt finalized = new BooleanDt();

	@Child(name = "label", order = 2, min = 1, max = 1)
	@Description(shortDefinition = "label for the consent domain")
	private StringDt label = new StringDt();

	@Child(name = "comment", order = 3, min = 0, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent domain")
	private StringDt comment = new StringDt();

	@Child(name = "signerIdType", order = 4, min = 1, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of signed id types to be associated with consent information within the consent domain")
	private List<StringDt> signerIdTypes = new ArrayList<>();

	@Child(name = "policyVersionConverter", order = 5, min = 1, max = 1)
	@Description(shortDefinition = "consent policy Java Version Converter")
	private StringDt policyVersionConverter = new StringDt();

	@Child(name = "moduleVersionConverter", order = 6, min = 1, max = 1)
	@Description(shortDefinition = "consent module Java Version Converter")
	private StringDt moduleVersionConverter = new StringDt();

	@Child(name = "consentTemplateVersionConverter", order = 7, min = 1, max = 1)
	@Description(shortDefinition = "consent Template Java Version Converter")
	private StringDt ctVersionConverter = new StringDt();

	@Child(name = "logo", order = 8, min = 0, max = 1)
	@Description(shortDefinition = "domain logo as base64 encoded stream of bytes")
	private Base64BinaryDt logo = new Base64BinaryDt();

	@Child(name = "properties", order = 9, min = 0, max = 1)
	@Description(shortDefinition = "properties of consent domain")
	private StringDt properties = new StringDt();

	@Child(name = "config", order = 10, min = 0, max = 1)
	@Description(shortDefinition = "configuration of consent domain")
	private ConsentDomainConfig config;

	@Child(name = "externProperties", order = 11, min = 0, max = 1)
	@Description(shortDefinition = "externProperties of consent domain")
	private StringDt externProperties = new StringDt();

	@Child(name = "expirationProperties", order = 12, min = 0, max = 1)
	@Description(shortDefinition = "expirationProperties of consent domain")
	private StringDt expirationProperties = new StringDt();

	@Child(name = "creationDate", order = 13, min = 0, max = 1)
	@Description(shortDefinition = "creation date of consent domain")
	private DateTimeDt creationDate = new DateTimeDt();

	/**
	 * create new instance of ConsentDomain
	 */
	public ConsentDomain()
	{
		// set default values
		setFinalized(false);
		setConsentTemplateVersionConverter(DEFAULT_CONVERTER);
		setModuleVersionConverter(DEFAULT_CONVERTER);
		setPolicyVersionConverter(DEFAULT_CONVERTER);
		setComment("");
		setProperties("");
		setExternProperties("");
		setExpirationProperties("");
	}

	/**
	 * get consent domain name
	 *
	 * @return domain name
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
	 * set consent domain name
	 *
	 * @param domainName
	 *            name of domain
	 * @return instance of consent domain
	 */
	public ConsentDomain setName(String domainName)
	{
		if (domainName == null || domainName.isEmpty())
		{
			throw new NullPointerException("Given domainName of type String is null or empty.");
		}

		name.setValue(domainName);
		return this;
	}

	/**
	 * get consent domain finalized status
	 *
	 * @return domain finalized status
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
	 * set consent domain finalized status
	 *
	 * @param finalstate
	 *            finalized status
	 *            domain finalized status
	 * @return instance of consent domain
	 */
	public ConsentDomain setFinalized(Boolean finalstate)
	{
		if (finalstate == null)
		{
			throw new NullPointerException("Given final state of type Boolean is null.");
		}

		finalized.setValue(finalstate);
		return this;
	}

	/**
	 * get label for consent domain
	 *
	 * @return label of consent domain
	 */
	public String getLabel()
	{
		if (label == null)
		{
			label = new StringDt();
		}
		return label.getValueAsString();
	}

	/**
	 * set label for the consent domain
	 *
	 * @param domainLabel
	 *            label for the consent domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain setLabel(String domainLabel)
	{
		if (domainLabel == null)
		{
			throw new NullPointerException("Given label of type String is null.");
		}

		label.setValue(domainLabel);
		return this;
	}

	/**
	 * get comment to describe purpose of consent domain
	 *
	 * @return comment to describe purpose of consent domain
	 */
	public String getComment()
	{
		if (comment == null)
		{
			comment = new StringDt();
		}
		return comment.getValueAsString();
	}

	/**
	 * set comment to describe purpose of consent domain
	 *
	 * @param domainComment
	 *            comment to describe purpose of consent domain
	 * @return instance of consent domain
	 */
	public ConsentDomain setComment(String domainComment)
	{
		if (domainComment == null)
		{
			throw new NullPointerException("Given Comment of type String is null.");
		}

		comment.setValue(domainComment);
		return this;
	}

	/**
	 * get list of signedidtypes to be associated with consent information within the consent domain
	 *
	 * @return list of signedidtypes (semicolon separated) to be associated with consent information
	 *         within the consent domain
	 */
	public List<String> getSignerIdTypes()
	{
		if (signerIdTypes == null)
		{
			signerIdTypes = new ArrayList<>();
		}

		List<String> result = new ArrayList<>();

		for (StringDt idType : signerIdTypes)
		{
			result.add(idType.getValue());
		}

		return result;
	}

	/**
	 * set signedidtypes to be associated with consent information within the consent domain
	 *
	 * @param domainSignerIdTypes
	 *            list of signed id types (semicolon separated) to be associated with consent
	 *            information within the consent domain
	 * @return instance of consent domain
	 */
	public ConsentDomain setSignerIdTypes(List<String> domainSignerIdTypes)
	{
		if (domainSignerIdTypes != null && !domainSignerIdTypes.isEmpty())
		{
			signerIdTypes = new ArrayList<>();

			for (String idType : domainSignerIdTypes)
			{
				if (!idType.isEmpty())
				{
					signerIdTypes.add(new StringDt(idType));
				}
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
	public ConsentDomain addSignerIdType(String idType)
	{
		if (idType == null || idType.isEmpty())
		{
			throw new NullPointerException("Given idType o type String is null or empty.");
		}
		if (signerIdTypes != null && !signerIdTypes.contains(new StringDt(idType)))
		{
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
	public ConsentDomain removeSignerIdType(String idType)
	{
		if (idType != null && !idType.isEmpty())
		{
			StringDt signerId = new StringDt(idType);
			signerIdTypes.remove(signerId);
		}

		return this;
	}

	/**
	 * get consent Template Java Version Converter
	 *
	 * @return consent template Java Version Converter
	 */
	public String getConsentTemplateVersionConverter()
	{
		if (ctVersionConverter == null)
		{
			ctVersionConverter = new StringDt();
		}
		return ctVersionConverter.getValue();
	}

	/**
	 * set consent template Java Version Converter
	 *
	 * @param consentTemplateVersionConverter
	 *            consent template Java Version Converter
	 * @return instance of consent domain
	 */
	public ConsentDomain setConsentTemplateVersionConverter(String consentTemplateVersionConverter)
	{
		if (consentTemplateVersionConverter == null)
		{
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
	public String getModuleVersionConverter()
	{
		if (moduleVersionConverter == null)
		{
			moduleVersionConverter = new StringDt();
		}
		return moduleVersionConverter.getValue();
	}

	/**
	 * set consent module Java Version Converter
	 *
	 * @param moduleVersionConverter
	 *            consent module Java Version Converter
	 * @return instance of consent domain
	 */
	public ConsentDomain setModuleVersionConverter(String moduleVersionConverter)
	{
		if (moduleVersionConverter == null)
		{
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
	public String getPolicyVersionConverter()
	{
		if (policyVersionConverter == null)
		{
			policyVersionConverter = new StringDt();
		}
		return policyVersionConverter.getValue();
	}

	/**
	 * set consent policy Java Version Converter
	 *
	 * @param policyVersionConverter
	 *            consent policy Java Version Converter
	 * @return instance of consent domain
	 */
	public ConsentDomain setPolicyVersionConverter(String policyVersionConverter)
	{
		if (policyVersionConverter == null)
		{
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
	public byte[] getLogo()
	{
		if (logo == null)
		{
			logo = new Base64BinaryDt();
		}
		return logo.getValue();
	}

	/**
	 * set domain logo as base64 encoded stream of bytes
	 *
	 * @param domainLogo
	 *            as base64 encoded stream of bytes
	 * @return instance of consent domain
	 */
	public ConsentDomain setLogo(byte[] domainLogo)
	{
		if (domainLogo == null || domainLogo.length == 0)
		{
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
	public String getProperties()
	{
		if (properties == null)
		{
			properties = new StringDt();
		}
		return properties.getValue();
	}

	/**
	 * set properties of consent domain with 0-n properties separated by semicolon e.g.
	 * validity_period=p1y
	 *
	 * @param properties
	 *            properties of consent domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain setProperties(String properties)
	{
		if (properties == null)
		{
			throw new NullPointerException("Given domain properties of type String is null.");
		}
		this.properties.setValue(properties);
		return this;
	}

	public ConsentDomainConfig getConfig()
	{
		return config;
	}

	public void setConfig(ConsentDomainConfig config)
	{
		this.config = config;
	}

	/**
	 * get externProperties of consent domain
	 *
	 * @return externProperties of consent domain
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
	 * set externProperties of consent domain with 0-n properties separated by semicolon e.g.
	 * validity_period=p1y
	 *
	 * @param externProperties
	 *            externProperties of consent domain
	 * @return instance of ConsentDomain
	 */
	public ConsentDomain setExternProperties(String externProperties)
	{
		if (externProperties == null)
		{
			throw new NullPointerException("Given domain externProperties of type String is null.");
		}
		this.externProperties.setValue(externProperties);
		return this;
	}

	/**
	 * get expirationProperties of consent domain
	 *
	 * @return expirationProperties of consent domain
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
	 * set expirationProperties of assigned module
	 *
	 * @param expirationProperties
	 *            expirationProperties of consent domain
	 * @return instance of consent domain
	 */
	public ConsentDomain setExpirationProperties(String expirationProperties)
	{
		if (expirationProperties == null)
		{
			throw new NullPointerException("Given expirationProperties of type String is null.");
		}
		this.expirationProperties.setValue(expirationProperties);
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
	public ConsentDomain setCreationDate(Date creationDate)
	{
		this.creationDate = new DateTimeDt(creationDate);
		return this;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentDomain that))
			return false;

		return new EqualsBuilder()
				.append(getName(), that.getName())
				.append(getFinalized(), that.getFinalized())
				.append(getLabel(), that.getLabel())
				.append(getComment(), that.getComment())
				.append(getSignerIdTypes(), that.getSignerIdTypes())
				.append(getPolicyVersionConverter(), that.getPolicyVersionConverter())
				.append(getModuleVersionConverter(), that.getModuleVersionConverter())
				.append(ctVersionConverter, that.ctVersionConverter)
				.append(getLogo(), that.getLogo())
				.append(getProperties(), that.getProperties())
				.append(getConfig(), that.getConfig())
				.append(getExternProperties(), that.getExternProperties())
				.append(getExpirationProperties(), that.getExpirationProperties())
				.append(getCreationDate(), that.getCreationDate()).isEquals();
	}

	@Override public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getName())
				.append(getFinalized())
				.append(getLabel())
				.append(getComment())
				.append(getSignerIdTypes())
				.append(getPolicyVersionConverter())
				.append(getModuleVersionConverter())
				.append(ctVersionConverter)
				.append(getLogo())
				.append(getProperties())
				.append(getConfig())
				.append(getExternProperties())
				.append(getExpirationProperties())
				.append(getCreationDate())
				.toHashCode();
	}

	@Override public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("name", name)
				.append("finalized", finalized)
				.append("label", label)
				.append("comment", comment)
				.append("signerIdTypes", signerIdTypes)
				.append("policyVersionConverter", policyVersionConverter)
				.append("moduleVersionConverter", moduleVersionConverter)
				.append("ctVersionConverter", ctVersionConverter)
				.append("logo", logo)
				.append("properties", properties)
				.append("config", config)
				.append("externProperties", externProperties)
				.append("expirationProperties", expirationProperties)
				.append("creationDate", creationDate)
				.toString();
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(name, label, comment, logo, signerIdTypes, ctVersionConverter,
				moduleVersionConverter, policyVersionConverter, properties, config,
				externProperties, expirationProperties, creationDate);
	}

	@Override
	protected Type typedCopy()
	{
		ConsentDomain retValue = new ConsentDomain();
		copyValues(retValue);

		retValue.setName(getName());
		retValue.setLabel(getLabel());
		retValue.setComment(getComment());
		retValue.setLogo(getLogo());
		retValue.setConsentTemplateVersionConverter(getConsentTemplateVersionConverter());
		retValue.setPolicyVersionConverter(getPolicyVersionConverter());
		retValue.setModuleVersionConverter(getModuleVersionConverter());
		retValue.setSignerIdTypes(getSignerIdTypes());
		retValue.setExternProperties(getExternProperties());
		retValue.setExpirationProperties(getExpirationProperties());
		retValue.setFinalized(getFinalized());
		retValue.setProperties(getProperties());
		retValue.setConfig(getConfig());
		retValue.setCreationDate(getCreationDate());

		return retValue;
	}
}
