package org.emau.icmvc.ganimed.ttp.cm2.model;

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

import java.io.Serializable;
import java.text.ParseException;
import java.time.format.DateTimeParseException;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;

/**
 * objekt fuer die m-n tabelle module <-> policy
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "module_policy")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_MP")
public class ModulePolicy implements Serializable, FhirDTOExporter<AssignedPolicyDTO>
{
	private static final long serialVersionUID = -5418267636077122007L;
	private static final Logger logger = LogManager.getLogger(ModulePolicy.class);
	@EmbeddedId
	private ModulePolicyKey key;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "M_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"), @JoinColumn(name = "M_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "M_VERSION", referencedColumnName = "VERSION") })
	@MapsId("moduleKey")
	private Module module;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "P_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "P_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "P_VERSION", referencedColumnName = "VERSION") })
	@MapsId("policyKey")
	private Policy policy;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;
	@Column(name = "EXPIRATION_PROPERTIES")
	private String expirationProperties;
	@Transient
	private ExpirationPropertiesObject expirationPropertiesObject = null;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_MCT")
	private String fhirID;

	public ModulePolicy()
	{}

	public ModulePolicy(Module module, Policy policy, AssignedPolicyDTO assignedPolicyDTO)
	{
		super();
		this.module = module;
		this.policy = policy;
		this.key = new ModulePolicyKey(module.getKey(), policy.getKey());
		this.comment = assignedPolicyDTO.getComment();
		this.externProperties = assignedPolicyDTO.getExternProperties();
		this.expirationPropertiesObject = new ExpirationPropertiesObject(assignedPolicyDTO.getExpirationProperties());
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void loadPropertiesFromString()
	{
		try
		{
			expirationPropertiesObject = new ExpirationPropertiesObject(expirationProperties);
		}
		catch (ParseException | DateTimeParseException e)
		{
			logger.fatal("exception while parsing expirationProperties '" + expirationProperties + "'", e);
		}
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public ExpirationPropertiesObject getExpirationPropertiesObject()
	{
		if (expirationPropertiesObject == null)
		{
			loadPropertiesFromString();
		}
		return expirationPropertiesObject;
	}

	public ModulePolicyKey getKey()
	{
		return key;
	}

	public Module getModule()
	{
		return module;
	}

	public Policy getPolicy()
	{
		return policy;
	}

	public String getComment()
	{
		return comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public String getExpirationProperties()
	{
		return expirationProperties;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public void setExpirationPropertiesObject(ExpirationPropertiesObject expirationPropertiesObject)
	{
		this.expirationPropertiesObject = expirationPropertiesObject;
	}

	public void updateInUse(String comment, String externProperties)
	{
		this.comment = comment;
		this.externProperties = externProperties;
	}

	public void update(AssignedPolicyDTO assignedPolicyDTO)
	{
		updateInUse(assignedPolicyDTO.getComment(), assignedPolicyDTO.getExternProperties());
		this.expirationPropertiesObject = new ExpirationPropertiesObject(assignedPolicyDTO.getExpirationProperties());
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
	}

	@Override
	public AssignedPolicyDTO toDTO() throws InvalidVersionException, UnknownDomainException
	{
		return new AssignedPolicyDTO(policy.toDTO(), comment, externProperties, expirationPropertiesObject.toDTO(), fhirID);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (key == null ? 0 : key.hashCode());
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
		ModulePolicy other = (ModulePolicy) obj;
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "ModulePolicy [key=" + key + ", comment=" + comment + ", externProperties=" + externProperties + ", expirationProperties="
				+ expirationProperties + ", expirationPropertiesObject=" + expirationPropertiesObject + ", fhirID=" + fhirID + "]";
	}
}
