package org.emau.icmvc.ganimed.ttp.cm2.model;

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
<<<<<<< HEAD
 *  							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 *
=======
 * 							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
 * 
>>>>>>> branch '2.15.x' of https://git.icm.med.uni-greifswald.de/ths/gics-project.git
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

import java.io.Serializable;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;

/**
 * objekt fuer die m-n tabelle consent template <-> module
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "module_consent_template")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_MCT")
public class ModuleConsentTemplate implements Serializable, Comparable<ModuleConsentTemplate>, FhirDTOExporter<AssignedModuleDTO>
{
	private static final long serialVersionUID = -6454219515407700151L;
	private static final Logger logger = LogManager.getLogger(ModuleConsentTemplate.class);
	@EmbeddedId
	private ModuleConsentTemplateKey key;
	private boolean mandatory;
	/**
	 * die liste aus AssignedModuleDTO als bitfeld
	 */
	private long displayCheckboxes;
	@Enumerated
	private ConsentStatus defaultConsentStatus;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "M_DOMAIN", referencedColumnName = "DOMAIN_NAME"), @JoinColumn(name = "M_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "M_VERSION", referencedColumnName = "VERSION") })
	@MapsId("moduleKey")
	private Module module;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "VERSION") })
	@MapsId("consentTemplateKey")
	private ConsentTemplate consentTemplate;
	@Column(name = "ORDER_NUMBER")
	private Integer orderNumber;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "PARENT_M_DOMAIN", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "PARENT_M_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "PARENT_M_VERSION", referencedColumnName = "VERSION") })
	private Module parent;
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

	public ModuleConsentTemplate()
	{}

	public ModuleConsentTemplate(ConsentTemplate consentTemplate, Module module, AssignedModuleDTO assignedModuleDTO, Module parent)
	{
		super();
		this.module = module;
		this.consentTemplate = consentTemplate;
		this.key = new ModuleConsentTemplateKey(consentTemplate.getKey(), module.getKey());
		this.mandatory = assignedModuleDTO.getMandatory();
		this.displayCheckboxes = listToLong(assignedModuleDTO.getDisplayCheckboxes());
		this.defaultConsentStatus = assignedModuleDTO.getDefaultConsentStatus();
		this.orderNumber = assignedModuleDTO.getOrderNumber();
		this.parent = parent;
		this.comment = assignedModuleDTO.getComment();
		this.externProperties = assignedModuleDTO.getExternProperties();
		this.expirationPropertiesObject = new ExpirationPropertiesObject(assignedModuleDTO.getExpirationProperties());
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

	public ModuleConsentTemplateKey getKey()
	{
		return key;
	}

	public boolean getMandatory()
	{
		return mandatory;
	}

	public long getDisplayCheckboxes()
	{
		return displayCheckboxes;
	}

	public List<ConsentStatus> getDisplayCheckboxesList()
	{
		List<ConsentStatus> result = new ArrayList<>();
		long temp = displayCheckboxes;
		int i = 0;
		ConsentStatus[] allStatus = ConsentStatus.values();
		// i == allStatus.length muesste eigentlich eine exception werfen
		while (temp > 0 && i < allStatus.length)
		{
			if ((temp & 1l) == 1l)
			{
				result.add(allStatus[i]);
			}
			temp >>= 1;
			i++;
		}
		return result;
	}

	public ConsentStatus getDefaultConsentStatus()
	{
		return defaultConsentStatus;
	}

	public Module getModule()
	{
		return module;
	}

	public ConsentTemplate getConsentTemplate()
	{
		return consentTemplate;
	}

	public Integer getOrderNumber()
	{
		return orderNumber;
	}

	public Module getParent()
	{
		return parent;
	}

	public String getComment()
	{
		return comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	private long listToLong(List<ConsentStatus> list)
	{
		long result = 0;
		for (ConsentStatus status : list)
		{
			result += 1 << status.ordinal();
		}
		return result;
	}

	public void updateInUse(String comment, String externProperties, ExpirationPropertiesDTO expirationProperties)
	{
		this.comment = comment;
		this.externProperties = externProperties;
		updateExpiration(expirationProperties);
	}

	public void update(AssignedModuleDTO assignedModuleDTO, Module parent)
	{
		updateInUse(assignedModuleDTO.getComment(), assignedModuleDTO.getExternProperties(), assignedModuleDTO.getExpirationProperties());
		this.mandatory = assignedModuleDTO.getMandatory();
		this.displayCheckboxes = listToLong(assignedModuleDTO.getDisplayCheckboxes());
		this.defaultConsentStatus = assignedModuleDTO.getDefaultConsentStatus();
		this.orderNumber = assignedModuleDTO.getOrderNumber();
		this.parent = parent;
	}

	private void updateExpiration(ExpirationPropertiesDTO expiration)
	{
		// for finalized templates only allow to update future expiration properties (existing as well as new date must be in the future)
		this.expirationPropertiesObject = expirationPropertiesObject.createMergedExpirationProperties(expiration, consentTemplate.getFinalised());
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
	}

	@Override
	public AssignedModuleDTO toDTO() throws InvalidVersionException, UnknownDomainException
	{
		ModuleKeyDTO parentKeyDTO = null;
		if (parent != null)
		{
			parentKeyDTO = parent.getKey().toDTO(VersionConverterCache.getModuleVersionConverter(module.getDomain().getName()));
		}
		return new AssignedModuleDTO(module.toDTO(), mandatory, defaultConsentStatus, getDisplayCheckboxesList(), orderNumber, parentKeyDTO, comment,
				externProperties, expirationPropertiesObject.toDTO(), fhirID);
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
		ModuleConsentTemplate other = (ModuleConsentTemplate) obj;
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
	public int compareTo(ModuleConsentTemplate o)
	{
		if (orderNumber < o.getOrderNumber())
		{
			return -1;
		}
		else if (orderNumber.equals(o.getOrderNumber()))
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(", default value: '");
		sb.append(defaultConsentStatus == null ? "null" : defaultConsentStatus.toString());
		sb.append("' is mandatory: ");
		sb.append(mandatory);
		sb.append(", has order number ");
		sb.append(orderNumber);
		sb.append(", as parent: '");
		sb.append(parent);
		sb.append("', should show checkboxes for ");
		List<ConsentStatus> checkBoxList = getDisplayCheckboxesList();
		for (ConsentStatus consentStatus : checkBoxList)
		{
			sb.append(" ");
			sb.append(consentStatus.toString());
		}
		sb.append("', has as comment '");
		sb.append(comment);
		sb.append("' and externProperties '");
		sb.append(externProperties);
		sb.append("' and expirationProperties '");
		sb.append(expirationProperties);
		sb.append("'");
		sb.append(". FHIR-ID: ");
		sb.append(fhirID);
		return sb.toString();
	}
}
