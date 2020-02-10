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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */


import java.util.ArrayList;
import java.util.List;

import org.emau.icmvc.magic.fhir.datatypes.ConsentTemplateFreeText.FreeTextType;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Person;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.instance.model.api.ICompositeType;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;


/**
 * FHIR Datatype to hold gICS-Template specific information
 * 
 * @author bialkem {@link mosaic-greifswald.de}
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

	@Child(name = "Name", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "name of consent template")
	private StringDt name = new StringDt();

	@Child(name = "version", order = 2, min = 1, max = 1)
	@Description(shortDefinition = "version of consent template")
	private StringDt version = new StringDt();

	@Child(name = "type", order = 3, min = 1, max = 1)
	@Description(shortDefinition = "type of consent template")
	private StringDt type = new StringDt();

	@Child(name = "title", order = 4, min = 1, max = 1)
	@Description(shortDefinition = "title of consent template to be used as label etc.")
	private StringDt title = new StringDt();

	@Child(name = "comment", order = 5, min = 1, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent template")
	private StringDt comment = new StringDt();

	@Child(name = "header", order = 6, min = 0, max = 1)
	@Description(shortDefinition = "header of consent template")
	private StringDt header = new StringDt();

	@Child(name = "footer", order = 7, min = 0, max = 1)
	@Description(shortDefinition = "footer of consent template")
	private StringDt footer = new StringDt();

	@Child(name = "properties", order = 8, min = 0, max = 1)
	@Description(shortDefinition = "properties of consent template")
	private StringDt properties = new StringDt();

	@Child(name = "externProperties", order = 9, min = 0, max = 1)
	@Description(shortDefinition = "externProperties of consent template")
	private StringDt externProperties = new StringDt();

	@Child(name = "contact", order = 10, min = 0, max = 1)
	@Description(shortDefinition = "responsible contact for this consent template")
	private Person contact = new Person();

	@Child(name = "freetext", order = 11, min = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of free text definitions")
	private List<Type> freetextDefs = new ArrayList<Type>();

	// see more under http://hapifhir.io/doc_custom_structures.html

	@Child(name = "modules", order = 12, min = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of assignedModules")
	private List<Type> modules = new ArrayList<Type>();


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
		this.setVersion(version);
		this.setDomainName(domain);
		this.setName(name);
		this.setType(TemplateType.CONSENT);
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
	 * create new instance of ConsentTemplate, using default version "1.0"
	 *
	 */
	public ConsentTemplate()
	{
		// default
		this.setVersion("1.0");
		this.setProperties("");
		this.setExternProperties("");
		this.setComment("");
		this.setFooter("");
		this.setHeader("");
		this.setTitle("");
		this.setType(TemplateType.CONSENT);
	}


	/**
	 * get Name of consent template
	 * 
	 * 
	 * @return Name of consent template
	 */
	public String getName()
	{
		if (name == null)
			name = new StringDt();
		return name.getValue();
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
			domainName = new StringDt();
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
			version = new StringDt();
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
			type = new StringDt();
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
		
		for (TemplateType t : TemplateType.values()) {
	        if (t.name().equals(templateType)) {
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
			title = new StringDt();
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
			comment = new StringDt();
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
			header = new StringDt();
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
			footer = new StringDt();
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
	 * get properties of consent template
	 * 
	 * @return properties of consent template
	 */
	public String getProperties()
	{
		if (properties == null)
			properties = new StringDt();
		return properties.getValue();
	}

	/**
	 * set properties of consent template with 0-n properties separated by semicolon
	 * e.g. validity_period=p1y
	 * 
	 * @param properties
	 *            properties of consent template
	 * @return instance of ConsentTemplate
	 */
	public ConsentTemplate setProperties(String properties)
	{
		if (properties == null)
		{
			throw new NullPointerException("Given template properties of type String is null.");
		}
		this.properties.setValue(properties);
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
			externProperties = new StringDt();
		return externProperties.getValue();
	}

	/**
	 * set externProperties of consent template with 0-n properties separated by semicolon
	 * e.g. validity_period=p1y
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
		contact.addTelecom(new ContactPoint().setValue(eMail)
				.setSystem(org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem.EMAIL));
		contact.addTelecom(new ContactPoint().setValue(telephone)
				.setSystem(org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem.PHONE));

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
		if (freeItem != null && freetextDefs.contains(freeItem))
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
		if (module != null && modules.contains(module))
		{
			modules.remove(module);
		}

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
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((footer == null) ? 0 : footer.hashCode());
		result = prime * result + ((freetextDefs == null) ? 0 : freetextDefs.hashCode());
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((modules == null) ? 0 : modules.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		ConsentTemplate other = (ConsentTemplate) obj;
		if (comment == null)
		{
			if (other.comment != null)
				return false;
		}
		else if (!comment.equals(other.comment))
			return false;
		if (type == null)
		{
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		if (contact == null)
		{
			if (other.contact != null)
				return false;
		}
		else if (!contact.equals(other.contact))
			return false;
		if (domainName == null)
		{
			if (other.domainName != null)
				return false;
		}
		else if (!domainName.equals(other.domainName))
			return false;
		if (footer == null)
		{
			if (other.footer != null)
				return false;
		}
		else if (!footer.equals(other.footer))
			return false;
		if (freetextDefs == null)
		{
			if (other.freetextDefs != null)
				return false;
		}
		else if (!freetextDefs.equals(other.freetextDefs))
			return false;
		if (header == null)
		{
			if (other.header != null)
				return false;
		}
		else if (!header.equals(other.header))
			return false;
		if (modules == null)
		{
			if (other.modules != null)
				return false;
		}
		else if (!modules.equals(other.modules))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (externProperties == null)
		{
			if (other.externProperties != null)
				return false;
		}
		else if (!externProperties.equals(other.externProperties))
			return false;
		if (properties == null)
		{
			if (other.properties != null)
				return false;
		}
		else if (!properties.equals(other.properties))
			return false;
		if (title == null)
		{
			if (other.title != null)
				return false;
		}
		else if (!title.equals(other.title))
			return false;
		if (version == null)
		{
			if (other.version != null)
				return false;
		}
		else if (!version.equals(other.version))
			return false;
		return true;
	}



	@Override
	protected Type typedCopy()
	{
		ConsentTemplate retValue = new ConsentTemplate();
		super.copyValues(retValue);

		retValue.setDomainName(this.getDomainName());
		retValue.setComment(this.getComment());
		retValue.setName(this.getName());
		retValue.setVersion(this.getVersion());

		retValue.setType(this.getType());

		retValue.setHeader(this.getHeader());
		retValue.setFooter(this.getFooter());
		retValue.setTitle(this.getTitle());
		retValue.setContact(this.getContact());

		retValue.setAssignedModules(this.getAssignedModules());
		retValue.setFreetextDefs(this.getFreetextDefs());
		retValue.setExternProperties(this.getExternProperties());
		retValue.setProperties(this.getProperties());

		return retValue;
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(domainName, name, version, type, title, header, footer, externProperties, properties, freetextDefs, modules, contact, comment);
	}

	@Override
	public String toString()
	{

		StringBuilder sb_free = new StringBuilder();

		for (Type item : freetextDefs)
		{
			sb_free.append(((ConsentTemplateFreeText) item).toString());
			if (!(freetextDefs.indexOf(item) == freetextDefs.size() - 1))
			{
				// not last element, add separator
				sb_free.append(";");
			}
		}

		StringBuilder sb_modules = new StringBuilder();

		for (Type item : modules)
		{
			sb_modules.append(((AssignedConsentModule) item).toString());
			if (!(modules.indexOf(item) == modules.size() - 1))
			{
				// not last element, add separator
				sb_modules.append(";");
			}
		}

		return "ConsentTemplate [domainName=" + domainName + ", name=" + name + ", version=" + version + ",type=" + type + ", title="
				+ title + ", comment=" + comment + ", header=" + header + ", footer=" + footer + ", properties="
				+ properties + ", externProperties="
				+ externProperties + ", contact=" + contact + ", freetextDefs=" + sb_free.toString() + ", modules=" + sb_modules.toString()
				+ "]";
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
