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
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DomainPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;

/**
 * zur gruppierung
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "domain")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_D")
public class Domain implements Serializable, FhirDTOExporter<DomainDTO>
{
	private static final long serialVersionUID = 5437680743128384901L;
	private static final Logger logger = LogManager.getLogger(Domain.class);
	@Id
	@Column(length = 50)
	private String name;
	private String label;
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
	@BatchFetch(BatchFetchType.IN)
	private List<Policy> policies = new ArrayList<>();
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
	@BatchFetch(BatchFetchType.IN)
	private List<Module> modules = new ArrayList<>();
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
	// @BatchFetch(BatchFetchType.IN) auskommentiert wegen bug in eclipselink - die am ct haengenden
	// text-entities werden nicht richtig mitgeladen
	// (javax.resource.ResourceException: IJ000460: Error checking for a transaction)
	private List<ConsentTemplate> consentTemplates = new ArrayList<>();
	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgt die funktion "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties"
	@Transient
	private DomainPropertiesObject propertiesObject = null;
	@Column(name = "PROPERTIES", length = 4095)
	private String propertiesString;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;
	@Column(name = "EXPIRATION_PROPERTIES")
	private String expirationProperties;
	@Transient
	private ExpirationPropertiesObject expirationPropertiesObject = null;
	@Column(name = "CT_VERSION_CONVERTER")
	private String ctVersionConverter;
	@Column(name = "MODULE_VERSION_CONVERTER")
	private String moduleVersionConverter;
	@Column(name = "POLICY_VERSION_CONVERTER")
	private String policyVersionConverter;
	@Lob
	private String logo;
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<SignerIdType> signerIdTypes = new ArrayList<>();
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Timestamp updateTimestamp;
	@Column(name = "FINALISED", nullable = false)
	private boolean finalised;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_D")
	private String fhirID;

	public Domain()
	{}

	public Domain(DomainDTO dto) throws VersionConverterClassException
	{
		super();
		name = dto.getName();
		setLabel(dto.getLabel());
		ctVersionConverter = dto.getCtVersionConverter();
		moduleVersionConverter = dto.getModuleVersionConverter();
		policyVersionConverter = dto.getPolicyVersionConverter();
		propertiesString = dto.getProperties();
		loadPropertiesFromString();
		comment = dto.getComment();
		externProperties = dto.getExternProperties();
		logo = dto.getLogo();

		int orderNumber = 1;
		for (String signerIdType : dto.getSignerIdTypes())
		{
			signerIdTypes.add(new SignerIdType(this, orderNumber++, signerIdType));
		}
		setFinalised(dto.getFinalised());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		createTimestamp = timestamp;
		updateTimestamp = timestamp;
		this.expirationPropertiesObject = new ExpirationPropertiesObject(dto.getExpirationProperties());
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
	}

	/**
	 * this method is called by jpa
	 *
	 * @throws InvalidPropertiesException
	 */
	@PostLoad
	public void loadPropertiesFromString()
	{
		propertiesObject = new DomainPropertiesObject(propertiesString);
		try
		{
			expirationPropertiesObject = new ExpirationPropertiesObject(expirationProperties);
		}
		catch (ParseException e)
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

	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}

	private void setLabel(String label)
	{
		this.label = label != null ? label : name;
	}

	public DomainPropertiesObject getPropertiesObject()
	{
		if (propertiesObject == null)
		{
			loadPropertiesFromString();
		}
		return propertiesObject;
	}

	public String getPropertiesString()
	{
		return propertiesString;
	}

	public String getComment()
	{
		return comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public ExpirationPropertiesObject getExpirationPropertiesObject()
	{
		return expirationPropertiesObject;
	}

	public List<Policy> getPolicies()
	{
		return policies;
	}

	public void setPolicies(List<Policy> policies)
	{
		this.policies = policies;
	}

	public List<Module> getModules()
	{
		return modules;
	}

	public void setModules(List<Module> modules)
	{
		this.modules = modules;
	}

	public List<ConsentTemplate> getConsentTemplates()
	{
		return consentTemplates;
	}

	public void setConsentTemplates(List<ConsentTemplate> consentTemplates)
	{
		this.consentTemplates = consentTemplates;
	}

	public String getCTVersionConverter()
	{
		return ctVersionConverter;
	}

	public String getModuleVersionConverter()
	{
		return moduleVersionConverter;
	}

	public String getPolicyVersionConverter()
	{
		return policyVersionConverter;
	}

	public String getLogo()
	{
		return logo;
	}

	public List<SignerIdType> getSignerIdTypes()
	{
		return signerIdTypes;
	}

	public void setSignerIdTypes(List<SignerIdType> signerIdTypes)
	{
		this.signerIdTypes = signerIdTypes;
	}

	public Timestamp getCreateTimestamp()
	{
		return createTimestamp;
	}

	public Timestamp getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	public boolean getFinalised()
	{
		return finalised;
	}

	private void setFinalised(boolean finalised)
	{
		this.finalised |= finalised; // einmal gesetzt kann nicht rueckgaengig gemacht werden
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public void finalise()
	{
		setFinalised(true);
		updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void updateInUse(String label, String comment, String externProperties, String logo)
	{
		setLabel(label);
		this.comment = comment;
		this.externProperties = externProperties;
		this.logo = logo;
		updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void update(DomainDTO dto) throws ObjectInUseException
	{
		if (finalised)
		{
			throw new ObjectInUseException(
					"domain is finalised and can't be updated anymore. to update non-critical fields please use updateDomainInUse(...)");
		}
		updateInUse(dto.getLabel(), dto.getComment(), dto.getExternProperties(), dto.getLogo());
		this.expirationPropertiesObject = new ExpirationPropertiesObject(dto.getExpirationProperties());
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
		ctVersionConverter = dto.getCtVersionConverter();
		moduleVersionConverter = dto.getModuleVersionConverter();
		policyVersionConverter = dto.getPolicyVersionConverter();
		propertiesString = dto.getProperties();
		loadPropertiesFromString();
		signerIdTypes.clear();

		int order = 1;
		for (String signerIdType : dto.getSignerIdTypes())
		{
			signerIdTypes.add(new SignerIdType(this, order++, signerIdType));
		}
		setFinalised(dto.getFinalised());
	}

	@Override
	public DomainDTO toDTO()
	{
		List<String> sIdTypes = new ArrayList<>();

		signerIdTypes.sort(Comparator.comparing(SignerIdType::getOrderNumber));
		for (SignerIdType signerIdType : signerIdTypes)
		{
			sIdTypes.add(signerIdType.getKey().getName());
		}

		DomainDTO result = new DomainDTO(name, label, ctVersionConverter, moduleVersionConverter, policyVersionConverter, propertiesObject.toString(),
				comment, externProperties, logo, sIdTypes, finalised, new Date(createTimestamp.getTime()), new Date(updateTimestamp.getTime()), expirationPropertiesObject.toDTO(), fhirID);
		return result;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
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
		Domain other = (Domain) obj;
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "domain '" + name + "'";
	}

	public String toLongString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("domain '");
		sb.append(name);
		sb.append("', label = '");
		sb.append(label);
		sb.append("', comment = '");
		sb.append(comment);
		sb.append("', extern properties = '");
		sb.append(externProperties);
		sb.append("', expiration properties = '");
		sb.append(expirationProperties);
		sb.append("', properties: '");
		sb.append(propertiesString);
		sb.append("', ct version converter '");
		sb.append(ctVersionConverter);
		sb.append("', module version converter '");
		sb.append(moduleVersionConverter);
		sb.append("', policy version converter '");
		sb.append(policyVersionConverter);
		sb.append("' has ");
		sb.append(policies.size());
		sb.append(" policies, ");
		sb.append(modules.size());
		sb.append(" modules, ");
		sb.append(consentTemplates.size());
		sb.append(" consent templates");
		sb.append(logo != null && !logo.isEmpty() ? ", a logo and " : ", no logo and ");
		sb.append(signerIdTypes.size());
		sb.append(" signer id types. created at ");
		sb.append(new Date(createTimestamp.getTime()));
		sb.append(" last update at ");
		sb.append(new Date(updateTimestamp.getTime()));
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(". FHIR-ID: ");
		sb.append(fhirID);
		return sb.toString();
	}
}
