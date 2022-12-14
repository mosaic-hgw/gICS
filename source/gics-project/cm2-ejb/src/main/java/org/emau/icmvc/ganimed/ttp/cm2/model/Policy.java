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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;

/**
 * eine policy ist die kleinstmoegliche unterteilung eines consents; sie repraesentiert eine atomare, zustimmbare einheit
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "policy")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_P")
public class Policy implements Serializable, FhirDTOExporter<PolicyDTO>
{
	private static final long serialVersionUID = 5393443631949125332L;
	@EmbeddedId
	private PolicyKey key;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "NAME")
	@MapsId("domainName")
	private Domain domain;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;
	// keine explizite relation zu signed policies! die wuerden sonst bei fast jedem zugriff auf das template geladen (muessten mit in equals und hashcode rein)
	@OneToMany(mappedBy = "policy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<ModulePolicy> modulePolicies = new ArrayList<>();
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Timestamp updateTimestamp;
	@Column(name = "LABEL", length = 255)
	private String label;
	@Column(name = "FINALISED", nullable = false)
	private boolean finalised;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_P")
	private String fhirID;

	public Policy()
	{}

	public Policy(Domain domain, PolicyDTO dto) throws InvalidVersionException, UnknownDomainException
	{
		super();
		this.key = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domain.getName()), dto.getKey());
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		this.domain = domain;
		setLabel(dto.getLabel());
		setFinalised(dto.getFinalised());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		this.createTimestamp = timestamp;
		this.updateTimestamp = timestamp;
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public PolicyKey getKey()
	{
		return key;
	}

	public Domain getDomain()
	{
		return domain;
	}

	public String getComment()
	{
		return comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public List<ModulePolicy> getModulePolicies()
	{
		return modulePolicies;
	}

	public void setModulePolicies(List<ModulePolicy> modulePolicies)
	{
		this.modulePolicies = modulePolicies;
	}

	public Timestamp getCreateTimestamp()
	{
		return createTimestamp;
	}

	public Timestamp getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	public String getLabel()
	{
		return label;
	}

	private void setLabel(String label)
	{
		this.label = label != null ? label : key.getName();
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
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void updateInUse(String label, String comment, String externProperties)
	{
		this.comment = comment;
		this.externProperties = externProperties;
		setLabel(label);
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void update(PolicyDTO dto) throws ObjectInUseException
	{
		if (finalised)
		{
			throw new ObjectInUseException(
					"policy is finalised and can't be updated anymore. to update non-critical fields please use updatePolicyInUse(...)");
		}
		updateInUse(dto.getLabel(), dto.getComment(), dto.getExternProperties());
		setFinalised(dto.getFinalised());
	}

	@Override
	public PolicyDTO toDTO() throws InvalidVersionException, UnknownDomainException
	{
		PolicyKeyDTO dtoKey = key.toDTO(VersionConverterCache.getPolicyVersionConverter(domain.getName()));
		PolicyDTO result = new PolicyDTO(dtoKey, comment, externProperties, label, finalised, new Date(createTimestamp.getTime()),
				new Date(updateTimestamp.getTime()), fhirID);
		return result;
	}

	@PreRemove
	private void beforeRemove()
	{
		domain.getPolicies().remove(this);
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
		Policy other = (Policy) obj;
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
		StringBuilder sb = new StringBuilder(key.toString());
		sb.append(", label = '");
		sb.append(label);
		sb.append("', comment = '");
		sb.append(comment);
		sb.append("', extern properties = '");
		sb.append(externProperties);
		sb.append("' created at ");
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
