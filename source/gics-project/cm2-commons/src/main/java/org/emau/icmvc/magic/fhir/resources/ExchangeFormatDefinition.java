package org.emau.icmvc.magic.fhir.resources;

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

import org.emau.icmvc.magic.fhir.datatypes.ConsentDomain;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.Type;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;


/**
 * FHIR Resource to hold   information to configure consent domains, policies, modules and templates
 * @author bialkem
 *
 */
@ResourceDef(name="ExchangeFormatDefinition", profile = "http://example.com/StructureDefinition/dontuse#ExchangeFormatDefinition")
public class ExchangeFormatDefinition extends DomainResource{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2027225898044791842L;
	
	@Child(name = "supportedVersion", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "supported gICS version")	
	private StringDt supportedVersion = new StringDt();
	
	
	
	@Child(name = "domain", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "information for a single consent domain")
	private ConsentDomain domain = new ConsentDomain();

	@Child(name = "policies", order = 2, min = 1, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "information for 1-n consent policies")	
	private List<Type> policies = new ArrayList<Type>();
	
	@Child(name = "modules", order = 3, min = 1, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "information for 1-n consent modules ")	
	private List<Type> modules = new ArrayList<Type>();
	
	@Child(name = "templates", order = 4, min = 1, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "information for 1-n consent templates")	
	private List<Type> templates = new ArrayList<Type>();
	
	/**
	 * create new instance of ExchangeFormatDefinition
	 * @param version 
	 */
	public ExchangeFormatDefinition(String version){
		supportedVersion=new StringDt(version);
	}
	
	/**
	 * create new instance of ExchangeFormatDefinition
	 */
	public ExchangeFormatDefinition(){
		supportedVersion=new StringDt("2.8.x");
	}

	/**
	 * get supported gICS-version for the format specification
	 * @return supported gICS-Version
	 */
	public String getSupportedVersion() {
		return supportedVersion.getValue();
	}

	/**
	 * get consent domain information from the format definition
	 * @return consent domain information from format definition
	 */
	public ConsentDomain getDomain() {
		if (domain == null)
			domain = new ConsentDomain();
		return domain;
	}
	
	/**
	 * set consent domain information for format definition
	 * @param domainInfo consent domain information
	 * @return current instance of ExchangeFormatDefinition
	 */
	public ExchangeFormatDefinition setDomain(ConsentDomain domainInfo) {
		if (domainInfo == null)
			throw new NullPointerException("Given ConsentDomain information is null.");
		
		this.domain=domainInfo;		
		
		return this;
	}
	
	/**
	 * get current list of specified consent policies
	 * @return current list of specified consent policies
	 */
	public List<Type> getPolicies() {
		return policies;
	}
	
	/**
	 * set current list of specified consent policies
	 * @param policies list of specified consent policies
	 * @return current instance of ExchangeFormatDefinition
	 */
	public ExchangeFormatDefinition setPolicies(List<Type> policies) {
		if (policies == null) {
			throw new NullPointerException("Given list of policies is null.");
		}
		
		this.policies=policies;
		
		return this;
	}
	
	/**
	 * get current list of specified consent modules
	 * @return get current list of specified consent modules
	 */
	public List<Type> getModules() {
		return modules;
	}

	/**
	 * get current list of specified consent modules
	 * @param modules list of specified consent policies
	 * @return current instance of ExchangeFormatDefinition
	 */
	public ExchangeFormatDefinition setModules(List<Type> modules) {
		if (modules == null) {
			throw new NullPointerException("Given list of modules is null.");
		}
		
		this.modules = modules;
		
		return this;
	}
	
	/**
	 * get current list of specified consent templates
	 * @return list of specified consent templates
	 */
	public List<Type> getTemplates() {
		return templates;
	}

	/**
	 * set current list of specified consent modules
	 * @param templates list of specified consent templates
	 * @return current instance of ExchangeFormatDefinition
	 */
	public ExchangeFormatDefinition setTemplates(List<Type> templates) {
		if (templates == null) {
			throw new NullPointerException("Given list of templates is null.");
		}
		
		this.templates = templates;
		
		return this;
	}
	
	
	 @Override
	public String toString() {
		return "ExchangeFormatDefinition [supportedVersion=" + getSupportedVersion() + ", domain=" + domain + ", policies="
				+ policies + ", modules=" + modules + ", templates=" + templates + "]";
	}

	@Override
	    public FhirVersionEnum getStructureFhirVersionEnum() {
	        return FhirVersionEnum.DSTU3;
	    }
	
	@Override
	public boolean isEmpty() {
	
		return ElementUtil.isEmpty(getDomain(), getSupportedVersion(), 
				getPolicies(), getModules(),getTemplates());
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((modules == null) ? 0 : modules.hashCode());
		result = prime * result + ((policies == null) ? 0 : policies.hashCode());
		result = prime * result + ((supportedVersion == null) ? 0 : supportedVersion.hashCode());
		result = prime * result + ((templates == null) ? 0 : templates.hashCode());
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
		ExchangeFormatDefinition other = (ExchangeFormatDefinition) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (modules == null) {
			if (other.modules != null)
				return false;
		} else if (!modules.equals(other.modules))
			return false;
		if (policies == null) {
			if (other.policies != null)
				return false;
		} else if (!policies.equals(other.policies))
			return false;
		if (supportedVersion == null) {
			if (other.supportedVersion != null)
				return false;
		} else if (!supportedVersion.equals(other.supportedVersion))
			return false;
		if (templates == null) {
			if (other.templates != null)
				return false;
		} else if (!templates.equals(other.templates))
			return false;
		return true;
	}

	@Override
	public DomainResource copy() {
		
		ExchangeFormatDefinition def = new ExchangeFormatDefinition();
		
		super.copyValues(def);
		
		def.setDomain(domain);
		def.setPolicies(policies);
		def.setModules(modules);
		def.setTemplates(templates);
		
		return def;		
	}

	@Override
	public ResourceType getResourceType() {
		// TODO ADD ResourceType here
		return null;
	}
	


}
