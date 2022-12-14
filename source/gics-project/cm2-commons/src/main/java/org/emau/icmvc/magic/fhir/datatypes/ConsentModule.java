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
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

/**
 * FHIR Datatype to hold gICS-Module specific information
 *
 * @author bialkem <a href="http://mosaic-greifswald.de">mosaic-greifswald.de</a>
 */

@DatatypeDef(name = "ConsentModule") // , profile =
										// "http://example.com/StructureDefinition/dontuse#ConsentModule")
public class ConsentModule extends Type implements ICompositeType
{
	private static final long serialVersionUID = 7059402489018382911L;

	@Child(name = "domainName", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "name of the assigned consent domain")
	private StringDt domainName = new StringDt();

	@Child(name = "name", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "name of consent module")
	private StringDt _name = new StringDt();

	@Child(name = "label", order = 2, min = 0, max = 1)
	@Description(shortDefinition = "label of consent module")
	private StringDt _label = new StringDt();

	@Child(name = "version", order = 3, min = 1, max = 1)
	@Description(shortDefinition = "version of consent module")
	private StringDt _version = new StringDt();

	@Child(name = "title", order = 4, min = 0, max = 1)
	@Description(shortDefinition = "title of consent module to be used as label etc.")
	private StringDt _title = new StringDt();

	@Child(name = "shortText", order = 5, min = 0, max = 1)
	@Description(shortDefinition = "shortText of consent module to be used as infobox etc.")
	private StringDt _shortText = new StringDt();

	@Child(name = "text", order = 6, min = 0, max = 1)
	@Description(shortDefinition = "text of consent module to be used as displayed content")
	private StringDt _text = new StringDt();

	@Child(name = "comment", order = 7, min = 0, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent module")
	private StringDt _comment = new StringDt();

	// fuer abwartskomp
	@Child(name = "policyKey", order = 8, min = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "policy keys (as concatenated string) assigned to the consent module")
	private List<StringDt> policyKeys = new ArrayList<>();

	// neu
	@Child(name = "policies", order = 9, min = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of assignedPolicies")
	private List<Type> assignedPolicies = new ArrayList<>();

	@Child(name = "externProperties", order = 10, min = 0, max = 1)
	@Description(shortDefinition = "externProperties of consent module")
	private StringDt externProperties = new StringDt();

	@Child(name = "finalized", order = 11, min = 0, max = 1)
	@Description(shortDefinition = "finalized status for module")
	private BooleanDt finalized = new BooleanDt();

	@Child(name = "creationDate", order = 12, min = 0, max = 1)
	@Description(shortDefinition = "creation date of consent module")
	private DateTimeDt creationDate = new DateTimeDt();

	/**
	 * create new instance of ConsentModule, using default version "1.0"
	 *
	 */
	public ConsentModule()
	{
		// default
		setModuleVersion("1.0");
		setExternProperties("");
		setModuleComment("");
		setModuleShortText("");
		setModuleFinalized(false);
	}

	/**
	 * get version of consent module
	 *
	 * @return version of consent module
	 */
	public String getModuleVersion()
	{
		if (_version == null)
		{
			_version = new StringDt();
		}
		return _version.getValue();
	}

	/**
	 * set version of consent module
	 *
	 * @param version
	 *            version of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleVersion(String version)
	{
		if (version == null)
		{
			throw new NullPointerException("Given module version of type String is null.");
		}
		if (version.isEmpty())
		{
			throw new NullPointerException("Given module version of type String cannot be empty.");
		}
		_version.setValue(version);
		return this;
	}

	/**
	 * get consent module finalized status
	 *
	 * @return module finalized status
	 */
	public Boolean getModuleFinalized()
	{
		if (finalized == null)
		{
			finalized = new BooleanDt();
		}
		return finalized.getValue();
	}

	/**
	 * set consent module finalized status
	 *
	 * @param finalstate
	 *            finalized status
	 *            module finalized status
	 * @return instance of consent module
	 */
	public ConsentModule setModuleFinalized(Boolean finalstate)
	{
		if (finalstate == null)
		{
			throw new NullPointerException("Given final state of type Boolean is null.");
		}

		finalized.setValue(finalstate);
		return this;
	}

	/**
	 * get current list of assigned Modules as ArrayList
	 *
	 * @return list of assigned modules as ArrayList
	 */
	public List<Type> getAssignedPolicies()
	{
		return assignedPolicies;
	}

	/**
	 *
	 * @return list of deprecatedPolicy Keys
	 */
	public List<String> getDeprecatedPolicyKeys()
	{
		List<String> strings = new ArrayList<>();
		for (StringDt p : policyKeys)
		{
			strings.add(p.getValue());
		}
		return strings;
	}

	/**
	 * set list of AssignedConsentPolicies and update respective fhir list of references
	 *
	 * @param listOfPolicies
	 *            list of AssignedConsentPolicies for this consent module
	 * @return current instance of ConsentModule
	 */
	public ConsentModule setAssignedPolicies(List<Type> listOfPolicies)
	{
		if (listOfPolicies == null)
		{
			throw new NullPointerException("Given list of AssignedConsentPolicies is null.");
		}
		for (Type item : listOfPolicies)
		{
			// add and update referencelist
			addAssignedPolicy((AssignedConsentPolicy) item);
		}

		return this;
	}

	public ConsentModule setAssignedPoliciesFromKeyString(List<String> policyKeyStrings)
	{
		if (policyKeyStrings == null)
		{
			throw new NullPointerException("Given list of policyKeyStrings is null.");
		}
		for (String key : policyKeyStrings)
		{
			// add and update referencelist
			addAssignedPolicy(new AssignedConsentPolicy(key));
		}

		return this;
	}


	/**
	 * add new AssignedConsentPolicy to list of AssignedConsentPolicy and invoke update of internal
	 * fhir reference list
	 *
	 * @param item
	 *            AssignedConsentPolicy item to be added
	 * @return current instance of ConsentModule
	 */
	public ConsentModule addAssignedPolicy(AssignedConsentPolicy item)
	{
		// add current item to internal array list if not already a part of
		if (item != null && !assignedPolicies.contains(item))
		{
			assignedPolicies.add(item);
		}

		return this;
	}



	/**
	 * get label of consent module
	 *
	 * @return label of consent module
	 */
	public String getModuleLabel()
	{
		if (_label == null)
		{
			_label = new StringDt();
		}
		return _label.getValue();
	}

	/**
	 * set label of consent module
	 *
	 * @param label
	 *            label of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleLabel(String label)
	{
		if (label == null)
		{
			throw new NullPointerException("Given label of module of type String is null.");
		}

		if (label.isEmpty())
		{
			throw new NullPointerException("Given modul label type String cannot be empty.");
		}
		_label.setValue(label);
		return this;
	}

	/**
	 * get Name of consent module
	 *
	 * @return Name of consent module
	 */
	public String getModuleName()
	{
		if (_name == null)
		{
			_name = new StringDt();
		}
		return _name.getValue();
	}

	/**
	 * set Name of consent module
	 *
	 * @param name
	 *            Name of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleName(String name)
	{
		if (name == null)
		{
			throw new NullPointerException("Given name of module of type String is null.");
		}

		if (name.isEmpty())
		{
			throw new NullPointerException("Given module name of type String cannot be empty.");
		}
		_name.setValue(name);
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
	 * @param domainName
	 *            name of assigned consent domain
	 * @return instance of ConsentModule
	 */
	public ConsentModule setDomainName(String domainName)
	{
		if (domainName == null)
		{
			throw new NullPointerException("Given name of assigned consent domain of type String is null.");
		}

		if (domainName.isEmpty())
		{
			throw new NullPointerException("Given name of domain of type String cannot be empty.");
		}

		this.domainName = new StringDt(domainName);
		return this;
	}

	/**
	 * get comment to describe purpose of consent module
	 *
	 * @return comment to describe purpose of consent module
	 */
	public String getModuleComment()
	{
		if (_comment == null)
		{
			_comment = new StringDt();
		}
		return _comment.getValue();
	}

	/**
	 * set comment to describe purpose of consent module
	 *
	 * @param comment
	 *            comment to describe purpose of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleComment(String comment)
	{
		if (comment == null)
		{
			throw new NullPointerException("Given module comment of type String is null.");
		}

		_comment.setValue(comment);
		return this;
	}

	/**
	 * get title of consent module to be used as label etc.
	 *
	 * @return title of consent module
	 */
	public String getModuleTitle()
	{
		if (_title == null)
		{
			_title = new StringDt();
		}
		return _title.getValue();

	}

	/**
	 * set title of consent module to be used as label etc.
	 *
	 * @param title
	 *            title of consent module to be used as label etc.
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleTitle(String title)
	{
		if (title == null)
		{
			throw new NullPointerException("Given module title of type String is null.");
		}
		_title.setValue(title);
		return this;
	}

	/**
	 * get shorttext of consent module
	 *
	 * @return shorttext of consent module
	 */
	public String getModuleShortText()
	{
		if (_shortText == null)
		{
			_shortText = new StringDt();
		}

		return _shortText.getValue();
	}

	/**
	 * set shorttext of consent module to be used as displayed content
	 *
	 * @param shorttext
	 *            shorttext of consent module to be used as displayed content
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleShortText(String shorttext)
	{
		if (shorttext == null)
		{
			throw new NullPointerException("Given module shorttext of type String is null.");
		}
		_shortText.setValue(shorttext);
		return this;
	}

	/**
	 * get text of consent module to be used as label etc.
	 *
	 * @return text of consent module to be used as displayed content
	 */
	public String getModuleText()
	{
		if (_text == null)
		{
			_text = new StringDt();
		}

		return _text.getValue();
	}

	/**
	 * set text of consent module to be used as displayed content
	 *
	 * @param text
	 *            text of consent module to be used as displayed content
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleText(String text)
	{
		if (text == null)
		{
			throw new NullPointerException("Given module text of type String is null.");
		}
		_text.setValue(text);
		return this;
	}

	/**
	 * get externProperties of consent module
	 *
	 * @return externProperties of consent module
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
	 * set externProperties of consent module with 0-n properties separated by semicolon
	 *
	 * @param externProperties
	 *            externProperties of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setExternProperties(String externProperties)
	{
		if (externProperties == null)
		{
			throw new NullPointerException("Given module externProperties of type String is null.");
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
	public ConsentModule setCreationDate(Date creationDate)
	{
		this.creationDate = new DateTimeDt(creationDate);
		return this;
	}

	/**
	 * get list of consent policies assigned to consent module
	 *
	 * @return list of consent policies assigned to consent module
	 */
	/*
	 * public List<String> getPolicyKeys()
	 * {
	 * List<String> resultlist = new ArrayList<>();
	 *
	 * for (StringDt key : policyKeys)
	 * {
	 * resultlist.add(key.getValue());
	 * }
	 *
	 * return resultlist;
	 * }
	 */

	/**
	 * set list of consent policy keys to consent module
	 *
	 * @param policykeys
	 *            list of consent policies keys to be assigned to consent module
	 * @return instance of ConsentModule
	 */
	// public ConsentModule setPolicyKeys(List<String> policykeys)
	// {
	// if (policykeys != null && !policykeys.isEmpty())
	// {
	// policyKeys = new ArrayList<>();
	//
	// for (String key : policykeys)
	// {
	// policyKeys.add(new StringDt(key));
	// }
	//
	// }
	// return this;
	// }

	/**
	 * assign given consent policy to module
	 *
	 * @param policy
	 *            consent policy to be assigned to consent module
	 * @return instance of ConsentModule
	 */
	// public ConsentModule addPolicy(ConsentPolicy policy)
	// {
	// if (policy == null || policy.isEmpty())
	// {
	// throw new NullPointerException("Given policy of type ConsentPolicy is null or empty.");
	// }
	// if (policyKeys != null && !policyKeys.contains(new StringDt(policy.toKeyString())))
	// {
	// policyKeys.add(new StringDt(policy.toKeyString()));
	// }
	//
	// return this;
	// }

	/**
	 * assign given consent policy key to module
	 *
	 * @param policyKey
	 *            consent policy key to be assigned to consent module
	 * @return instance of ConsentModule
	 */
	// public ConsentModule addPolicyKey(String policyKey)
	// {
	// if (policyKey == null || policyKey.isEmpty())
	// {
	// throw new NullPointerException("Given policy key of type String is null or empty.");
	// }
	//
	// StringDt key = new StringDt(policyKey);
	//
	// if (policyKeys != null && !policyKeys.contains(key))
	// {
	// policyKeys.add(key);
	// }
	//
	// return this;
	// }

	/**
	 * remove assigned policy from module
	 *
	 * @param policy
	 *            consent policy to be removed from module
	 * @return instance of ConsentModule
	 */
	// public ConsentModule removePolicy(ConsentPolicy policy)
	// {
	// if (policy != null)
	// {
	// StringDt key = new StringDt(policy.toKeyString());
	// if (policyKeys.contains(key))
	// {
	// policyKeys.remove(key);
	// }
	// policyKeys.remove(new StringDt(policy.toKeyString()));
	// }
	// return this;
	// }

	/**
	 * remove assigned policy key from module
	 *
	 * @param policyKey
	 *            consent policy key to be removed from module
	 * @return instance of ConsentModule
	 */
	// public ConsentModule removePolicyKey(String policyKey)
	// {
	// if (policyKey != null)
	// {
	// StringDt key = new StringDt(policyKey);
	// if (policyKeys.contains(key))
	// {
	// policyKeys.remove(key);
	// }
	// }
	//
	// return this;
	// }

	/**
	 * get module Key as String separated with semicolon e.g. "domain;module name;module version"
	 */
	public String toKeyString()
	{
		return getDomainName() + ";" + getModuleName() + ";" + getModuleVersion();
	}



	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_comment == null) ? 0 : _comment.hashCode());
		result = prime * result + ((_label == null) ? 0 : _label.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + ((_shortText == null) ? 0 : _shortText.hashCode());
		result = prime * result + ((_text == null) ? 0 : _text.hashCode());
		result = prime * result + ((_title == null) ? 0 : _title.hashCode());
		result = prime * result + ((_version == null) ? 0 : _version.hashCode());
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		// result = prime * result + ((policyKeys == null) ? 0 : policyKeys.hashCode());
		result = prime * result + ((assignedPolicies == null) ? 0 : assignedPolicies.hashCode());
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
		ConsentModule other = (ConsentModule) obj;
		if (_comment == null)
		{
			if (other._comment != null)
			{
				return false;
			}
		}
		else if (!_comment.equals(other._comment))
		{
			return false;
		}
		if (_label == null)
		{
			if (other._label != null)
			{
				return false;
			}
		}
		else if (!_label.equals(other._label))
		{
			return false;
		}
		if (_name == null)
		{
			if (other._name != null)
			{
				return false;
			}
		}
		else if (!_name.equals(other._name))
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
		if (_shortText == null)
		{
			if (other._shortText != null)
			{
				return false;
			}
		}
		else if (!_shortText.equals(other._shortText))
		{
			return false;
		}
		if (_text == null)
		{
			if (other._text != null)
			{
				return false;
			}
		}
		else if (!_text.equals(other._text))
		{
			return false;
		}
		if (_title == null)
		{
			if (other._title != null)
			{
				return false;
			}
		}
		else if (!_title.equals(other._title))
		{
			return false;
		}
		if (_version == null)
		{
			if (other._version != null)
			{
				return false;
			}
		}
		else if (!_version.equals(other._version))
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
		// if (policyKeys == null)
		// {
		// if (other.policyKeys != null)
		// {
		// return false;
		// }
		// }
		// else if (!policyKeys.equals(other.policyKeys))
		// {
		// return false;
		// }
		if (assignedPolicies == null)
		{
			if (other.assignedPolicies != null)
			{
				return false;
			}
		}
		else if (!assignedPolicies.equals(other.assignedPolicies))
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
		StringBuilder sb = new StringBuilder();

		// for (String p : getPolicyKeys())
		// {
		// sb.append(p);
		// }

		for (Type t : getAssignedPolicies())
		{
			sb.append(((AssignedConsentPolicy) t).getPolicyKeyString()).append(";");
		}

		return "ConsentModule [_version=" + _version + ", _name=" + _name + ", finalised=" + finalized
				+ ", _label=" + _label + ", _domainName=" + domainName + ", _comment=" + _comment
				+ ", externProperties=" + externProperties + ", creationDate=" + creationDate + ", _title="
				+ _title + ", _shortText=" + _shortText + ", _text=" + _text + ", _policies=" + sb + "]";
	}

	@Override
	public boolean isEmpty()
	{
		// return ElementUtil.isEmpty(domainName, _comment, externProperties, finalized, _name,
		// _label, _version, _text, _title, _shortText, policyKeys);
		return ElementUtil.isEmpty(domainName, _comment, externProperties, finalized,
				_name, _label, _version, _text, _title, _shortText, assignedPolicies, creationDate);
	}

	@Override
	protected Type typedCopy()
	{
		ConsentModule retValue = new ConsentModule();
		copyValues(retValue);

		retValue.setDomainName(getDomainName());
		retValue.setModuleComment(getModuleComment());
		retValue.setModuleName(getModuleName());
		retValue.setModuleLabel(getModuleLabel());
		retValue.setModuleVersion(getModuleVersion());
		retValue.setModuleText(getModuleText());
		retValue.setModuleTitle(getModuleTitle());
		// retValue.setPolicyKeys(getPolicyKeys());
		retValue.setAssignedPolicies(getAssignedPolicies());
		retValue.setExternProperties(getExternProperties());
		retValue.setModuleShortText(getModuleShortText());
		retValue.setModuleFinalized(getModuleFinalized());
		retValue.setCreationDate(getCreationDate());

		return retValue;
	}
}
