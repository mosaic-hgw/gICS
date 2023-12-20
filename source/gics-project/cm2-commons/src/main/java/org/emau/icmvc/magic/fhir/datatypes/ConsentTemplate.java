package org.emau.icmvc.magic.fhir.datatypes;

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


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Type;

/**
 * FHIR Datatype to hold gICS-Template specific information
 *
 * @author bialkem <a href="http://mosaic-greifswald.de">mosaic-greifswald.de</a>
 */
@DatatypeDef(name = "ConsentTemplate")
public class ConsentTemplate extends Type implements ICompositeType
{
	/**
	 *
	 */
	private static final long serialVersionUID = 598750034364495400L;

	@Child(name = "domainName", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "name of the assigned consent domain")
	private StringDt domainName = new StringDt();

	@Child(name = "Name", order = 1, min = 0, max = 1)
	@Description(shortDefinition = "name of consent template")
	private StringDt name = new StringDt();

	// tolerate and respect lower-case 'name' attribute
	@Child(name = "name", order = 2, min = 0, max = 1)
	@Description(shortDefinition = "name of consent template")
	private StringDt nameLowerCase = new StringDt();

	@Child(name = "Label", order = 3, min = 0, max = 1)
	@Description(shortDefinition = "label of consent template")
	private StringDt label = new StringDt();

	// tolerate and respect lower-case 'label' attribute
	@Child(name = "label", order = 4, min = 0, max = 1)
	@Description(shortDefinition = "label of consent template")
	private StringDt labelLowerCase = new StringDt();

	@Child(name = "version", order = 5, min = 1, max = 1)
	@Description(shortDefinition = "version of consent template")
	private StringDt version = new StringDt();

	@Child(name = "type", order = 6, min = 1, max = 1)
	@Description(shortDefinition = "type of consent template")
	private StringDt type = new StringDt();

	@Child(name = "title", order = 7, min = 1, max = 1)
	@Description(shortDefinition = "title of consent template to be used as label etc.")
	private StringDt title = new StringDt();

	@Child(name = "comment", order = 8, min = 1, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent template")
	private StringDt comment = new StringDt();

	@Child(name = "header", order = 9, min = 0, max = 1)
	@Description(shortDefinition = "header of consent template")
	private StringDt header = new StringDt();

	@Child(name = "footer", order = 10, min = 0, max = 1)
	@Description(shortDefinition = "footer of consent template")
	private StringDt footer = new StringDt();

	@Child(name = "expirationProperties", order = 11, min = 0, max = 1)
	@Description(shortDefinition = "expirationProperties of consent template")
	private StringDt expirationProperties = new StringDt();

	@Child(name = "externProperties", order = 12, min = 0, max = 1)
	@Description(shortDefinition = "externProperties of consent template")
	private StringDt externProperties = new StringDt();

	@Child(name = "contact", order = 13, min = 0, max = 1)
	@Description(shortDefinition = "responsible contact for this consent template")
	private Person contact = new Person();

	@Child(name = "freetext", order = 14, min = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of free text definitions")
	private List<Type> freetextDefs = new ArrayList<>();

	// see more under http://hapifhir.io/doc_custom_structures.html

	@Child(name = "modules", order = 15, min = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of assignedModules")
	private List<Type> modules = new ArrayList<>();

	@Child(name = "finalized", order = 16, min = 0, max = 1)
	@Description(shortDefinition = "finalized status for template")
	private BooleanDt finalized = new BooleanDt();

	@Child(name = "versionLabel", order = 17, min = 1, max = 1)
	@Description(shortDefinition = "version label of consent template")
	private StringDt versionLabel = new StringDt();

	@Child(name = "creationDate", order = 18, min = 0, max = 1)
	@Description(shortDefinition = "creation date of consent template")
	private DateTimeDt creationDate = new DateTimeDt();

	/**
	 * create new instance of ConsentTemplate
	 *
	 * @param domain
	 * @param name
	 * @param version
	 */
	public ConsentTemplate(String domain, String name, String version)
	{
		this();
		setVersion(version);
		setDomainName(domain);
		setName(name);
		// no label given, default use name as label
		setLabel(name);
		// in contrast to the (name) label do not use any default value for the version label but leave it null
		this.setType(TemplateType.CONSENT);
	}

	/**
	 * create new instance of ConsentTemplate
	 *
	 * @param domain
	 * @param name
	 * @param label
	 * @param version
	 * @param versionLabel
	 */
	public ConsentTemplate(String domain, String name, String version, String label, String versionLabel)
	{
		this(domain, name, version);
		setLabel(label);
		setVersionLabel(versionLabel);
	}

	/**
	 * create new instance of ConsentTemplate
	 *
	 * @param domain
	 * @param name
	 * @param version
	 * @param type
	 */
	public ConsentTemplate(String domain, String name, String version, TemplateType type)
	{
		this(domain, name, version);
		this.setType(type);
	}

	/**
	 * create new instance of ConsentTemplate
	 *
	 * @param domain
	 * @param name
	 * @param version
	 * @param type
	 * @param label
	 * @param versionLabel
	 */
	public ConsentTemplate(String domain, String name, String version, TemplateType type, String label, String versionLabel)
	{
		this(domain, name, version, label, versionLabel);
		this.setType(type);
	}

	/**
	 * create new instance of ConsentTemplate, using default version "1.0"
	 */
	public ConsentTemplate()
	{
		// default
		setVersion("1.0");
		setExpirationProperties("");
		setExternProperties("");
		setComment("");
		setFooter("");
		setHeader("");
		setTitle("");
		setFinalized(false);
		this.setType(TemplateType.CONSENT);
	}

	/**
	 * get Name of consent template
	 *
	 * @return Name of consent template
	 */
	public String getName()
	{
		if (name() == null)
		{
			name = new StringDt();
		}
		return name.getValue();
	}

	/** Returns the {@link #name} field if not null. Otherwise fall back to returning {@link #nameLowerCase}
	 *
	 * @return the {@link #name} if not null or the {@link #nameLowerCase} otherwise
	 */
	private StringDt name()
	{
		if (name == null || (name.getValue() == null && nameLowerCase != null))
		{
			name = nameLowerCase;
			nameLowerCase = new StringDt();
		}
		else if (name != null && nameLowerCase != null && name.getValue() != null && nameLowerCase.getValue() != null)
		{
			if (name.getValue().equals(nameLowerCase.getValue()))
			{
				nameLowerCase = new StringDt();
			}
			else
			{
				throw new RuntimeException(new InvalidExchangeFormatException("Conflicting definitions for the name ('" + name.getValue() + "' vs. '" + nameLowerCase.getValue() + "') in template"));
			}
		}

		return name;
	}

	/**
	 * set Name of consent template
	 *
	 * @param templateName
	 *            Name of consent template
	 * @return instance of consent template
	 */
	public ConsentTemplate setName(String templateName)
	{
		if (templateName == null)
		{
			throw new NullPointerException("Given name of template of type String is null.");
		}

		if (templateName.isEmpty())
		{
			throw new NullPointerException("Given template name of type String cannot be empty.");
		}
		name.setValue(templateName);
		nameLowerCase = new StringDt();
		return this;
	}

	/**
	 * get consent template finalized status
	 *
	 * @return template finalized status
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
	 * set consent template finalized status
	 *
	 * @param finalstate
	 *            finalized status
	 *            domain finalized status
	 * @return instance of consent domain
	 */
	public ConsentTemplate setFinalized(Boolean finalstate)
	{
		if (finalstate == null)
		{
			throw new NullPointerException("Given final state of type Boolean is null.");
		}

		finalized.setValue(finalstate);
		return this;
	}

	/**
	 * get Label of consent template
	 *
	 *
	 * @return Label of consent template
	 */
	public String getLabel()
	{
		if (label() == null)
		{
			label = new StringDt();
		}
		return label.getValue();
	}

	/** Returns the {@link #label} field if not null. Otherwise fall back to returning {@link #labelLowerCase}
	 *
	 * @return the {@link #label} if not null or the {@link #labelLowerCase} otherwise
	 */
	private StringDt label()
	{
		if (label == null || (label.getValue() == null && labelLowerCase != null))
		{
			label = labelLowerCase;
			labelLowerCase = new StringDt();
		}
		else if (label != null && labelLowerCase != null && label.getValue() != null && labelLowerCase.getValue() != null)
		{
			if (label.getValue().equals(labelLowerCase.getValue()))
			{
				labelLowerCase = new StringDt();
			}
			else
			{
				throw new RuntimeException(new InvalidExchangeFormatException("Conflicting definitions for the label ('" + label.getValue() + "' vs. '" + labelLowerCase.getValue() + "') in template"));
			}
		}

		return label;
	}

	/**
	 * set Label of consent template
	 *
	 * @param templateLabel
	 *            Label of consent template
	 * @return instance of consent template
	 */
	public ConsentTemplate setLabel(String templateLabel)
	{
		if (templateLabel == null)
		{
			throw new NullPointerException("Given Label of template of type String is null.");
		}

		if (templateLabel.isEmpty())
		{
			throw new NullPointerException("Given template Label of type String cannot be empty.");
		}
		label.setValue(templateLabel);
		labelLowerCase = new StringDt();
		return this;
	}

	/**
	 * get version label of consent template
	 *
	 *
	 * @return version label of consent template
	 */
	public String getVersionLabel()
	{
		if (versionLabel == null)
		{
			versionLabel = new StringDt();
		}
		return versionLabel.getValue();
	}

	/**
	 * set version label of consent template
	 *
	 * @param templateVersionLabel
	 *            version Label of consent template
	 * @return instance of consent template
	 */
	public ConsentTemplate setVersionLabel(String templateVersionLabel)
	{
		// in contrast to the (name) label do not use any default value for the version label but leave it null
		// that's why omit any checks whether templateVersionLabel is null or empty
		versionLabel.setValue(templateVersionLabel);
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
	 * @param templateDomainName
	 *            name of assigned consent domain
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setDomainName(String templateDomainName)
	{
		if (templateDomainName == null)
		{
			throw new NullPointerException("Given name of assigned consent domain of type String is null.");
		}

		if (templateDomainName.isEmpty())
		{
			throw new NullPointerException("Given template domainName of type String cannot be empty.");
		}

		domainName = new StringDt(templateDomainName);
		return this;
	}

	/**
	 * get version of consent template
	 *
	 * @return version of consent template
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
	 * set version of consent template
	 *
	 * @param templateVersion
	 *            version of consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setVersion(String templateVersion)
	{
		if (templateVersion == null)
		{
			throw new NullPointerException("Given template version of type String is null.");
		}
		if (templateVersion.isEmpty())
		{
			throw new NullPointerException("Given template version of type String cannot be empty.");
		}
		version.setValue(templateVersion);
		return this;
	}

	/**
	 * get type of consent template
	 *
	 *
	 * @return type of consent template
	 */
	public TemplateType getType()
	{
		if (type == null)
		{
			type = new StringDt();
		}
		return TemplateType.valueOf(type.getValue());
	}

	/**
	 * set type of consent template
	 *
	 * @param templateType
	 *            type of consent template
	 *
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setType(TemplateType templateType)
	{
		if (templateType == null)
		{
			throw new NullPointerException("Given type of template is null.");
		}
		type.setValue(templateType.toString());
		return this;
	}

	/**
	 * set type of consent template
	 *
	 * @param templateType
	 *            string type of consent template
	 *
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setType(String templateType)
	{
		if (templateType == null || templateType.isEmpty())
		{
			throw new NullPointerException("Given type of template is null or empty.");
		}

		for (TemplateType t : TemplateType.values())
		{
			if (t.name().equals(templateType))
			{
				type.setValue(templateType);
				return this;
			}
		}

		return this;
	}

	/**
	 * get title of consent template to be used as label etc.
	 *
	 * @return title of consent template
	 */
	public String getTitle()
	{
		if (title == null)
		{
			title = new StringDt();
		}
		return title.getValue();

	}

	/**
	 * set title of consent template to be used as label etc.
	 *
	 * @param templateTitle
	 *            title of consent template to be used as label etc.
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setTitle(String templateTitle)
	{
		if (templateTitle == null)
		{
			throw new NullPointerException("Given template title of type String is null.");
		}
		title.setValue(templateTitle);
		return this;
	}

	/**
	 * get comment to describe purpose of consent template
	 *
	 * @return comment to describe purpose of consent template
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
	 * set comment to describe purpose of consent template
	 *
	 * @param templateComment
	 *            comment to describe purpose of consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setComment(String templateComment)
	{
		if (templateComment == null)
		{
			throw new NullPointerException("Given template comment of type String is null.");
		}

		comment.setValue(templateComment);
		return this;
	}

	/**
	 * get header of consent template
	 *
	 * @return header of consent template
	 */
	public String getHeader()
	{
		if (header == null)
		{
			header = new StringDt();
		}
		return header.getValue();
	}

	/**
	 * set header of consent template to be used as label etc.
	 *
	 * @param templateHeader
	 *            header of consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setHeader(String templateHeader)
	{
		if (templateHeader == null)
		{
			throw new NullPointerException("Given template header of type String is null.");
		}
		header.setValue(templateHeader);
		return this;
	}

	/**
	 * get footer of consent template
	 *
	 * @return footer of consent template
	 */
	public String getFooter()
	{
		if (footer == null)
		{
			footer = new StringDt();
		}
		return footer.getValue();
	}

	/**
	 * set footer of consent template to be used as label etc.
	 *
	 * @param templateFooter
	 *            footer of consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setFooter(String templateFooter)
	{
		if (templateFooter == null)
		{
			throw new NullPointerException("Given template footer of type String is null.");
		}
		footer.setValue(templateFooter);
		return this;
	}

	/**
	 * get expirationProperties of consent template
	 *
	 * @return expirationProperties of consent template
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
	 * set expirationProperties of consent template e.g. validity_period=p1y
	 *
	 * @param expirationProperties
	 *            expirationProperties of consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setExpirationProperties(String expirationProperties)
	{
		if (expirationProperties == null)
		{
			throw new NullPointerException("Given template expirationProperties of type String is null.");
		}
		this.expirationProperties.setValue(expirationProperties);
		return this;
	}

	/**
	 * get externProperties of consent template
	 *
	 * @return externProperties of consent template
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
	 * set externProperties of consent template with 0-n properties separated by semicolon e.g.
	 * validity_period=p1y
	 *
	 * @param externProperties
	 *            externProperties of consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setExternProperties(String externProperties)
	{
		if (externProperties == null)
		{
			throw new NullPointerException("Given template externProperties of type String is null.");
		}
		this.externProperties.setValue(externProperties);
		return this;
	}

	/**
	 * get responsible contact for this consent template
	 *
	 * @return responsible contact for this consent template
	 */
	public Person getContact()
	{
		return contact;
	}

	/**
	 * set responsible contact for this consent template
	 *
	 * @param templateContact
	 *            responsible contact for this consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setContact(Person templateContact)
	{
		contact = templateContact;
		return this;
	}

	/**
	 * set responsible contact for this consent template
	 *
	 * @param familyName
	 * @param givenName
	 * @param telephone
	 * @param eMail
	 * @param address
	 *            Address in FHIR format
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setContact(String familyName, String givenName, String telephone, String eMail, Address address)
	{
		contact = new Person().addAddress(address);
		contact.addName(new HumanName().addGiven(givenName).setFamily(familyName));
		contact.addTelecom(new ContactPoint().setValue(eMail).setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.EMAIL));
		contact.addTelecom(new ContactPoint().setValue(telephone).setSystem(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem.PHONE));

		return this;
	}

	/**
	 * get current list of assigned FreeTextsDefinitions as ArrayList
	 *
	 * @return list of assigned FreeTextsDefinitions as ArrayList
	 */
	public List<Type> getFreetextDefs()
	{
		return freetextDefs;
	}

	/**
	 * set list of ConsentTemplateFreeText and update respective fhir list of references
	 *
	 * @param listOfFrees
	 *            list of ConsentTemplateFreeText for this consent template
	 * @return current instance of ConsentTemplate
	 */
	public ConsentTemplate setFreetextDefs(List<Type> listOfFrees)
	{
		if (listOfFrees == null)
		{
			throw new NullPointerException("Given list of ConsentTemplateFreeText is null.");
		}
		for (Type item : listOfFrees)
		{
			// add and update referencelist
			addFreetext((ConsentTemplateFreeText) item);
		}

		return this;
	}

	/**
	 * add new ConsentTemplateFreeText to list of ConsentTemplateFreeTexts and invoke update of
	 * internal fhir reference list
	 *
	 * @param freeItem
	 *            ConsentTemplateFreeText item to be added
	 * @return current instance of ConsentTemplate
	 */
	public ConsentTemplate addFreetext(ConsentTemplateFreeText freeItem)
	{
		// add current item to internal arrayylist if not already a part of
		if (freeItem != null && !freetextDefs.contains(freeItem))
		{
			freetextDefs.add(freeItem);
		}

		return this;
	}

	/**
	 * remove ConsentTemplateFreeText from list of ConsentTemplateFreeTexts and invoke update of
	 * internal fhir reference list
	 *
	 * @param freeItem
	 *            ConsentTemplateFreeText item to be removed
	 * @return current instance of ConsentTemplate
	 */
	public ConsentTemplate removeFreetext(ConsentTemplateFreeText freeItem)
	{
		// add current item to internal arrayylist if not already a part of
		if (freeItem != null)
		{
			freetextDefs.remove(freeItem);
		}

		return this;
	}

	/**
	 * get current list of assigned Modules as ArrayList
	 *
	 * @return list of assigned modules as ArrayList
	 */
	public List<Type> getAssignedModules()
	{
		return modules;
	}

	/**
	 * set list of AssignedConsentModule and update respective fhir list of references
	 *
	 * @param listOfModules
	 *            list of AssignedConsentModule for this consent template
	 * @return current instance of ConsentTemplate
	 */
	public ConsentTemplate setAssignedModules(List<Type> listOfModules)
	{
		if (listOfModules == null)
		{
			throw new NullPointerException("Given list of AssignedConsentModule is null.");
		}
		for (Type item : listOfModules)
		{
			// add and update referencelist
			addAssignedModule((AssignedConsentModule) item);
		}

		return this;
	}

	/**
	 * add new AssignedConsentModule to list of AssignedConsentModules and invoke update of internal
	 * fhir reference list
	 *
	 * @param module
	 *            AssignedConsentModule item to be added
	 * @return current instance of ConsentTemplate
	 */
	public ConsentTemplate addAssignedModule(AssignedConsentModule module)
	{
		// add current item to internal array list if not already a part of
		if (module != null && !modules.contains(module))
		{
			modules.add(module);
		}

		return this;
	}

	/**
	 * remove AssignedConsentModule from list of AssignedConsentModules and invoke update of
	 * internal fhir reference list
	 *
	 * @param module
	 *            AssignedConsentModule item to be removed
	 * @return current instance of ConsentTemplate
	 */
	public ConsentTemplate removeAssignedModule(AssignedConsentModule module)
	{
		// add current item to internal array list if not already a part of
		if (module != null)
		{
			modules.remove(module);
		}

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
	public ConsentTemplate setCreationDate(Date creationDate)
	{
		this.creationDate = new DateTimeDt(creationDate);
		return this;
	}

	/**
	 * get template Key as String separated with semicolon e.g. "domain;template name;template
	 * version"
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
		result = prime * result + ((contact == null) ? 0 : contact.hashCode());
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((expirationProperties == null) ? 0 : expirationProperties.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((finalized == null) ? 0 : finalized.hashCode());
		result = prime * result + ((footer == null) ? 0 : footer.hashCode());
		result = prime * result + ((freetextDefs == null) ? 0 : freetextDefs.hashCode());
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((label() == null) ? 0 : label().hashCode());
		result = prime * result + ((versionLabel == null) ? 0 : versionLabel.hashCode());
		result = prime * result + ((modules == null) ? 0 : modules.hashCode());
		result = prime * result + ((name() == null) ? 0 : name().hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		ConsentTemplate other = (ConsentTemplate) obj;
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
		if (contact == null)
		{
			if (other.contact != null)
			{
				return false;
			}
		}
		else if (!contact.equals(other.contact))
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
		if (footer == null)
		{
			if (other.footer != null)
			{
				return false;
			}
		}
		else if (!footer.equals(other.footer))
		{
			return false;
		}
		if (freetextDefs == null)
		{
			if (other.freetextDefs != null)
			{
				return false;
			}
		}
		else if (!freetextDefs.equals(other.freetextDefs))
		{
			return false;
		}
		if (header == null)
		{
			if (other.header != null)
			{
				return false;
			}
		}
		else if (!header.equals(other.header))
		{
			return false;
		}
		if (label() == null)
		{
			if (other.label() != null)
			{
				return false;
			}
		}
		else if (!label().equals(other.label()))
		{
			return false;
		}
		if (versionLabel == null)
		{
			if (other.versionLabel != null)
			{
				return false;
			}
		}
		else if (!versionLabel.equals(other.versionLabel))
		{
			return false;
		}
		if (modules == null)
		{
			if (other.modules != null)
			{
				return false;
			}
		}
		else if (!modules.equals(other.modules))
		{
			return false;
		}
		if (name() == null)
		{
			if (other.name() != null)
			{
				return false;
			}
		}
		else if (!name().equals(other.name()))
		{
			return false;
		}
		if (title == null)
		{
			if (other.title != null)
			{
				return false;
			}
		}
		else if (!title.equals(other.title))
		{
			return false;
		}
		if (type == null)
		{
			if (other.type != null)
			{
				return false;
			}
		}
		else if (!type.equals(other.type))
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
	protected Type typedCopy()
	{
		ConsentTemplate retValue = new ConsentTemplate();
		super.copyValues(retValue);

		retValue.setDomainName(getDomainName());
		retValue.setComment(getComment());
		retValue.setName(getName());
		retValue.setLabel(getLabel());
		retValue.setVersion(getVersion());
		retValue.setVersionLabel(getVersionLabel());

		retValue.setType(getType());

		retValue.setHeader(getHeader());
		retValue.setFooter(getFooter());
		retValue.setTitle(getTitle());
		retValue.setContact(getContact());

		retValue.setAssignedModules(getAssignedModules());
		retValue.setFreetextDefs(getFreetextDefs());
		retValue.setExternProperties(getExternProperties());
		retValue.setFinalized(getFinalized());
		retValue.setExpirationProperties(getExpirationProperties());
		retValue.setCreationDate(getCreationDate());

		return retValue;
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(domainName, name(), label(), versionLabel, finalized, version, type, title, header, footer, externProperties, expirationProperties, freetextDefs, modules,
				contact, comment);
	}

	@Override
	public String toString()
	{
		StringBuilder sb_free = new StringBuilder();

		for (Type item : freetextDefs)
		{
			sb_free.append(item.toString());
			if (freetextDefs.indexOf(item) != freetextDefs.size() - 1)
			{
				// not last element, add separator
				sb_free.append(";");
			}
		}

		StringBuilder sb_modules = new StringBuilder();

		for (Type item : modules)
		{
			sb_modules.append(item.toString());
			if (modules.indexOf(item) != modules.size() - 1)
			{
				// not last element, add separator
				sb_modules.append(";");
			}
		}

		return "ConsentTemplate [domainName=" + domainName + ", finalized=" + finalized + ", name=" + name()
				+ ", label=" + label() + ",version=" + version + ",versionLabel=" + versionLabel + ",type=" + type
				+ ", title=" + title + ", comment=" + comment + ", header=" + header + ", footer=" + footer
				+ ", expirationProperties=" + expirationProperties + ", externProperties=" + externProperties
				+ ", contact=" + contact + ", creationDate=" + creationDate + ", freetextDefs=" + sb_free
				+ ", modules=" + sb_modules + "]";
	}

	/**
	 * internal enumeration to differentiate types of consent templates
	 *
	 * internal use only
	 *
	 * @author bialkem
	 *
	 */
	public enum TemplateType
	{
		CONSENT, REVOCATION, REFUSAL
	}
}
