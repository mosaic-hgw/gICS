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
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;

/**
 * FHIR Datatype to hold gICS-Module specific information
 * 
 * @author bialkem {@link mosaic-greifswald.de}
 */

@DatatypeDef(name = "ConsentModule")//, profile = "http://example.com/StructureDefinition/dontuse#ConsentModule")
public class ConsentModule  extends Type implements ICompositeType  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7059402489018382911L;

	@Child(name = "domainName", order = 0, min = 1, max = 1)	
	@Description(shortDefinition = "name of the assigned consent domain")
	private StringDt domainName = new StringDt();
		
	@Child(name = "name", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "name of consent module")
	private StringDt _name = new StringDt();
	
	@Child(name = "version", order = 2, min = 1, max = 1)
	@Description(shortDefinition = "version of consent module")
	private StringDt _version = new StringDt();
	
	@Child(name = "title", order = 3, min = 0, max = 1)
	@Description(shortDefinition = "title of consent module to be used as label etc.")
	private StringDt _title = new StringDt();
	
	@Child(name = "text", order = 4, min = 0, max = 1)
	@Description(shortDefinition = "text of consent module to be used as displayed content")
	private StringDt _text = new StringDt();
	
	
	@Child(name = "comment", order = 5, min = 0, max = 1)
	@Description(shortDefinition = "comment to describe purpose of consent module")
	private StringDt _comment = new StringDt();
		
	@Child(name = "policyKey", order = 6, min = 1, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "policy keys (as concatenated string) assigned to the consent module")
	private List<StringDt> policyKeys = new ArrayList<StringDt>();
	
	@Child(name = "externProperties", order = 7, min = 0, max = 1)	
	@Description(shortDefinition = "externProperties of consent module")
	private StringDt externProperties= new StringDt();
	
	/**
	 * create new instance of ConsentModule
	 * 
	 * @param moduleName
	 *            name of Consent module
	 * @param moduleComment
	 *            Comment for consent module
	 * @param moduleVersion
	 *            version for the consent module
	 * @param domainName
	 *            name of the consent domain this module will be applied to
	 * @param moduleText
	 *            text of the consent module
	 * @param moduleTitle
	 *            title of the consent module
	 */
	public ConsentModule(String moduleName, String moduleComment, String moduleVersion, String domainName,
			String moduleText, String moduleTitle) {

		this();

		// set specifica
		this.setModuleName(moduleName);
		this.setModuleVersion(moduleVersion);
		this.setModuleComment(moduleComment);
		this.setDomainName(domainName);
		this.setModuleText(moduleText);
		this.setModuleTitle(moduleTitle);
	}

	/**
	 * create new instance of ConsentModule assigning the specified policies
	 * 
	 * @param moduleName
	 *            name of Consent module
	 * @param moduleComment
	 *            Comment for consent module
	 * @param moduleVersion
	 *            version for the consent module
	 * @param domainName
	 *            name of the consent domain this module will be applied to
	 * @param moduleText
	 *            text of the consent module
	 * @param moduleTitle
	 * 
	 * @param policyKeys
	 *            list of assigned policies in policykey format
	 */
	public ConsentModule(String moduleName, String moduleComment, String moduleVersion, String domainName,
			String moduleText, String moduleTitle, List<String> policyKeys) {

		this();
		this.setModuleName(moduleName);
		this.setModuleVersion(moduleVersion);
		this.setModuleComment(moduleComment);
		this.setDomainName(domainName);
		this.setModuleText(moduleText);
		this.setModuleTitle(moduleTitle);
		this.setPolicyKeys(policyKeys);
	}
	
	/**
	 * create new instance of ConsentModule assigning the specified policies
	 * 
	 * @param moduleName
	 *            name of Consent module
	 * @param moduleComment
	 *            Comment for consent module
	 * @param moduleVersion
	 *            version for the consent module
	 * @param domainName
	 *            name of the consent domain this module will be applied to
	 * @param moduleText
	 *            text of the consent module
	 * @param moduleTitle
	 * 
	 * @param policyKeys	  
	 *            list of assigned policies in policykey format
	 * @param externProperties
	 *            externProperties
	 */
	public ConsentModule(String moduleName, String moduleComment, String moduleVersion, String domainName,
			String moduleText, String moduleTitle, List<String> policyKeys,String externProperties) {

		this(moduleName,moduleComment,moduleVersion,domainName,moduleText,moduleTitle,policyKeys);
		this.setExternProperties(externProperties);
	}

	/**
	 * Create new instance of ConsentModule using default version "1.0"
	 * 
	 * @param moduleName
	 *            name of Consent module
	 * @param moduleComment
	 *            Comment for consent module
	 * @param domainName
	 *            name of the consent domain this module will be applied to
	 * @param moduleText
	 *            text of the consent module
	 * @param moduleTitle
	 *
	 */
	public ConsentModule(String moduleName, String moduleComment, String domainName, String moduleText,
			String moduleTitle) {

		this();
		this.setModuleName(moduleName);
		this.setModuleComment(moduleComment);
		this.setDomainName(domainName);
		this.setModuleText(moduleText);
		this.setModuleTitle(moduleTitle);

	}

	/**
	 * create new instance of ConsentModule, using default version "1.0"
	 *
	 */
	public ConsentModule() {
		// default
		this.setModuleVersion("1.0");
		this.setExternProperties("");
		this.setModuleComment("");
	}



	/**
	 * get version of consent module
	 * 
	 * @return version of consent module
	 */
	public String getModuleVersion() {
		if (_version == null)
			_version = new StringDt();
		return _version.getValue();
	}

	/**
	 * set version of consent module
	 * 
	 * @param version
	 *            version of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleVersion(String version) {
		if (version == null) {
			throw new NullPointerException("Given module version of type String is null.");
		}
		if (version.isEmpty()) {
			throw new NullPointerException("Given module version of type String cannot be empty.");
		}
		_version.setValue(version);
		return this;
	}

	

	/**
	 * get Name of consent module
	 * 
	 * @return Name of consent module
	 */
	public String getModuleName() {
		if (_name == null)
			_name = new StringDt();
		return _name.getValue();
	}

	/**
	 * set Name of consent module
	 * 
	 * @param name
	 *            Name of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleName(String name) {
		if (name == null) {
			throw new NullPointerException("Given name of module of type String is null.");
		}

		if (name.isEmpty()) {
			throw new NullPointerException("Given policy name of type String cannot be empty.");
		}
		_name.setValue(name);
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
	 * @param domainName
	 *            name of assigned consent domain
	 * @return instance of ConsentModule
	 */
	public ConsentModule setDomainName(String domainName) {
		if (domainName == null) {
			throw new NullPointerException("Given name of assigned consent domain of type String is null.");
		}

		if (domainName.isEmpty()) {
			throw new NullPointerException("Given name of domain of type String cannot be empty.");
		}

		this.domainName=new StringDt(domainName);
		return this;
	}



	/**
	 * get comment to describe purpose of consent module
	 * 
	 * @return comment to describe purpose of consent module
	 */
	public String getModuleComment() {
		if (_comment == null)
			_comment = new StringDt();
		return _comment.getValue();
	}

	/**
	 * set comment to describe purpose of consent module
	 * 
	 * @param comment
	 *            comment to describe purpose of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleComment(String comment) {
		if (comment == null) {
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
	public String getModuleTitle() {
		if (_title == null)
			_title = new StringDt();
		return _title.getValue();

	}

	/**
	 * set title of consent module to be used as label etc.
	 * 
	 * @param title
	 *            title of consent module to be used as label etc.
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleTitle(String title) {
		if (title == null) {
			throw new NullPointerException("Given module title of type String is null.");
		}
		_title.setValue(title);
		return this;
	}


	/**
	 * get text of consent module to be used as label etc.
	 * 
	 * @return text of consent module to be used as displayed content
	 */
	public String getModuleText() {
		if (_text == null)
			_text = new StringDt();

		return _text.getValue();
	}

	/**
	 * set text of consent module to be used as displayed content
	 * 
	 * @param text
	 *            text of consent module to be used as displayed content
	 * @return instance of ConsentModule
	 */
	public ConsentModule setModuleText(String text) {
		if (text == null) {
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
	public String getExternProperties() {
		if (externProperties == null)
			externProperties = new StringDt();
		return externProperties.getValue();
	}

	/**
	 * set externProperties of consent module with 0-n properties separated by semicolon
	 *  
	 * @param externProperties
	 *            externProperties of consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setExternProperties(String externProperties) {
		if (externProperties == null) {
			throw new NullPointerException("Given module externProperties of type String is null.");
		}
		this.externProperties.setValue(externProperties);
		return this;
	}


	/**
	 * get list of consent policies  assigned to consent module
	 * 
	 * @return list of consent policies  assigned to consent module
	 */
	public List<String> getPolicyKeys() {
		
		List<String> resultlist = new ArrayList<String>();
		
		for (StringDt key : policyKeys) {
			resultlist.add(key.getValue());
		}
		
		return resultlist;
	}

	/**
	 * set list of consent policy keys to consent module
	 * 
	 * @param policykeys
	 *            list of consent policies keys to be assigned to consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule setPolicyKeys(List<String> policykeys) {
		if (policykeys != null && policykeys.size() > 0) {
			
			this.policyKeys = new ArrayList<StringDt>();
			
			for (String key : policykeys) {
				this.policyKeys.add(new StringDt(key));
			}
			
		}
		return this;
	}
	
	/**
	 * assign given consent policy  to module
	 * 
	 * @param policy
	 *            consent policy to be assigned to consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule addPolicy(ConsentPolicy policy) {
		if (policy == null || policy.isEmpty())
			throw new NullPointerException("Given policy  of type ConsentPolicy is null or empty.");
		if(policyKeys!=null && !policyKeys.contains(new StringDt(policy.toKeyString()))){
			policyKeys.add(new StringDt(policy.toKeyString()));
		}
		
		return this;
	}
	
	/**
	 * assign given consent policy key  to module
	 * 
	 * @param policyKey
	 *            consent policy key to be assigned to consent module
	 * @return instance of ConsentModule
	 */
	public ConsentModule addPolicyKey(String policyKey) {
		if (policyKey == null || policyKey.isEmpty())
			throw new NullPointerException("Given policy  key of type String is null or empty.");
		
		StringDt key = new StringDt(policyKey);
		
		if(policyKeys!=null && !policyKeys.contains(key)){
			policyKeys.add(key);
		}
		
		return this;
	}

		
	/**
	 * remove assigned policy from module
	 * 
	 * @param policy
	 *            consent policy to be removed from module
	 * @return instance of ConsentModule
	 */
	public ConsentModule removePolicy(ConsentPolicy policy) {
		if (policy != null) {			
			StringDt key = new StringDt(policy.toKeyString());
			if (policyKeys.contains(key)) {
				policyKeys.remove(key);
			}
			policyKeys.remove(new StringDt(policy.toKeyString()));
		}
		return this;
	}
	
	/**
	 * remove assigned policy key from module
	 * 
	 * @param policyKey
	 *            consent policy key to be removed from module
	 * @return instance of ConsentModule
	 */
	public ConsentModule removePolicyKey(String policyKey) {
		if (policyKey != null) {
			
			StringDt key = new StringDt(policyKey);
			if (policyKeys.contains(key)) {
				policyKeys.remove(key);
			}			
		}
		
		return this;
	}
		
	/**
	 * get module Key as String separated with semicolon e.g. "domain;module name;module version"
	 */
	public String toKeyString()
	{
		return getDomainName()+";"+getModuleName()+";"+getModuleVersion();
	}
	
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + ((_version == null) ? 0 : _version.hashCode());
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
		ConsentModule other = (ConsentModule) obj;
		if (domainName == null) {
			if (other.domainName != null)
				return false;
		} else if (!domainName.equals(other.domainName))
			return false;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		if (_version == null) {
			if (other._version != null)
				return false;
		} else if (!_version.equals(other._version))
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
		
		StringBuilder sb = new StringBuilder();
		
		for (String p : this.getPolicyKeys()) {
			sb.append(p);
		}
		
		return "ConsentModule [_version=" + _version + ", _name=" + _name + ", _domainName=" + domainName
				+ ", _comment=" + _comment + ", externProperties="	+ externProperties+ ", _title=" + _title + ", _text=" + _text + ", _policies=" +sb.toString()
				+ "]";
	}

	@Override
	public boolean isEmpty() {

		return ElementUtil.isEmpty(domainName, _comment, externProperties, _name, _version,_text,_title,policyKeys);

	}

	

	@Override
	protected Type typedCopy() {
		ConsentModule retValue = new ConsentModule();
		super.copyValues(retValue);
		retValue.setDomainName(this.getDomainName());
		retValue.setModuleComment(this.getModuleComment());
		retValue.setModuleName(this.getModuleName());
		retValue.setModuleVersion(this.getModuleVersion());
		retValue.setModuleText(this.getModuleText());
		retValue.setModuleTitle(this.getModuleTitle());
		retValue.setPolicyKeys(this.getPolicyKeys());
		retValue.setExternProperties(this.getExternProperties());
		return retValue;
	}
	

}
