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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.TextType;

/**
 * ein modul ist eine zustimmbare unterteilung eines consents; sie fasst mehrere policies zusammen,
 * denen gewoehnlicherweise gemeinsam zugestimmt wird
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "module")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_M")
public class Module implements Serializable, FhirDTOExporter<ModuleDTO>
{
	private static final long serialVersionUID = -8381863185248692641L;
	@EmbeddedId
	private ModuleKey key;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "NAME")
	@MapsId("domainName")
	private Domain domain;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "TEXT", referencedColumnName = "ID")
	private Text text;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "TITLE", referencedColumnName = "ID")
	private Text title;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<ModuleConsentTemplate> moduleConsentTemplates = new ArrayList<>();
	@OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<ModulePolicy> modulePolicies = new ArrayList<>();
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Timestamp updateTimestamp;
	@Column(name = "LABEL", length = 255)
	private String label;
	@Column(name = "SHORT_TEXT", length = 5000)
	private String shortText;
	@Column(name = "FINALISED", nullable = false)
	private boolean finalised;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_M")
	private String fhirID;

	public Module()
	{}

	public Module(Domain domain, ModuleDTO dto, boolean finaliseRelatedEntities) throws InvalidVersionException, RequirementsNotFullfilledException, UnknownDomainException
	{
		super();
		key = new ModuleKey(VersionConverterCache.getModuleVersionConverter(domain.getName()), dto.getKey());
		text = new Text(key, TextType.MODUL, dto.getText());
		title = new Text(key, TextType.MODULE_TITLE, dto.getTitle());
		comment = dto.getComment();
		externProperties = dto.getExternProperties();
		this.domain = domain;
		shortText = dto.getShortText();
		setLabel(dto.getLabel());
		setFinalised(dto.getFinalised(), finaliseRelatedEntities);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		createTimestamp = timestamp;
		updateTimestamp = timestamp;
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void modifyFhirID()
	{
		if ('-' == fhirID.charAt(8))
		{
			// in fhir sind consenttemplate und module auf den selben objekttyp abgebildet, dort benoertigen aber alle instanzen eine eindeutige id.
			// der uuid-generator wiederum kann nicht in 2 klassen verwendet werden.
			// daher wird die uuid der module leicht modifiziert, damit sie auf keinen fall identisch mit einer eines ct sein kann.
			fhirID = fhirID.substring(1, 9) + fhirID.charAt(0) + fhirID.substring(9);
		}
		fhirID = fhirID.toLowerCase();
	}

	public ModuleKey getKey()
	{
		return key;
	}

	public Domain getDomain()
	{
		return domain;
	}

	public Text getText()
	{
		return text;
	}

	public Text getTitle()
	{
		return title;
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

	public List<ModuleConsentTemplate> getModuleConsentTemplates()
	{
		return moduleConsentTemplates;
	}

	public void setModuleConsentTemplates(List<ModuleConsentTemplate> moduleConsentTemplates)
	{
		this.moduleConsentTemplates = moduleConsentTemplates;
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

	public String getShortText()
	{
		return shortText;
	}

	public void setShortText(String shortText)
	{
		this.shortText = shortText;
	}

	public boolean getFinalised()
	{
		return finalised;
	}

	private void setFinalised(boolean finalised, boolean finaliseRelatedEntities) throws RequirementsNotFullfilledException
	{
		if (finalised)
		{
			if (finaliseRelatedEntities)
			{
				for (ModulePolicy mp : modulePolicies)
				{
					mp.getPolicy().finalise();
				}
			}
			else
			{
				for (ModulePolicy mp : modulePolicies)
				{
					if (!mp.getPolicy().getFinalised())
					{
						String message = key.toString() + " can't be finalised because at least one related policy (" + mp.getPolicy().getKey() + ") isn't finalised";
						throw new RequirementsNotFullfilledException(message);
					}
				}
			}
			this.finalised = true;
		}
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public void finalise(boolean finaliseRelatedEntities) throws RequirementsNotFullfilledException
	{
		setFinalised(true, finaliseRelatedEntities);
		updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void updateInUse(String label, String shortText, String comment, String externProperties, Set<AssignedPolicyDTO> assignedPolicyDTOs)
			throws InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		setLabel(label);
		this.shortText = shortText;
		this.comment = comment;
		this.externProperties = externProperties;
		updateTimestamp = new Timestamp(System.currentTimeMillis());
		if (assignedPolicyDTOs != null)
		{
			for (AssignedPolicyDTO apDTO : assignedPolicyDTOs)
			{
				PolicyKey policyKey = new PolicyKey(VersionConverterCache.getPolicyVersionConverter(domain.getName()), apDTO.getPolicy().getKey());
				boolean found = false;
				for (ModulePolicy mp : modulePolicies)
				{
					if (mp.getPolicy().getKey().equals(policyKey))
					{
						mp.updateInUse(apDTO.getComment(), apDTO.getExternProperties(), apDTO.getExpirationProperties());
						found = true;
					}
				}
				if (!found && finalised)
				{
					throw new InvalidParameterException("unknown assigned policy for this module " + apDTO.getPolicy().getKey());
				}
			}
		}
	}

	public void update(ModuleDTO dto, boolean finaliseRelatedEntities)
			throws ObjectInUseException, RequirementsNotFullfilledException, InvalidParameterException, InvalidVersionException, UnknownDomainException
	{
		if (finalised)
		{
			throw new ObjectInUseException(
					"module is finalised and can't be updated anymore. to update non-critical fields please use updateModuleInUse(...)");
		}
		updateInUse(dto.getLabel(), dto.getShortText(), dto.getComment(), dto.getExternProperties(), dto.getAssignedPolicies());
		title.setText(dto.getTitle());
		text.setText(dto.getText());
		setFinalised(dto.getFinalised(), finaliseRelatedEntities);
	}

	@Override
	public ModuleDTO toDTO() throws InvalidVersionException, UnknownDomainException
	{
		ModuleKeyDTO dtoKey = key.toDTO(VersionConverterCache.getModuleVersionConverter(domain.getName()));
		Set<AssignedPolicyDTO> modulePolicyDTOs = new HashSet<>();
		for (ModulePolicy mp : modulePolicies)
		{
			modulePolicyDTOs.add(mp.toDTO());
		}
		return new ModuleDTO(dtoKey, text.getText(), title.getText(), comment, externProperties, modulePolicyDTOs, label, shortText, finalised,
				new Date(createTimestamp.getTime()), new Date(updateTimestamp.getTime()), fhirID);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (key == null ? 0 : key.hashCode());
		return result;
	}

	@PreRemove
	private void beforeRemove()
	{
		for (ModulePolicy mp : modulePolicies)
		{
			mp.getPolicy().getModulePolicies().remove(mp);
		}
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
		Module other = (Module) obj;
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
		sb.append(", label '");
		sb.append(label);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("' and ");
		sb.append(modulePolicies.size());
		sb.append(" policies. created at ");
		sb.append(new Date(createTimestamp.getTime()));
		sb.append(" last update at ");
		sb.append(new Date(updateTimestamp.getTime()));
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(". FHIR-ID: ");
		sb.append(fhirID);
		return sb.toString();
	}

	public String toLongString()
	{
		StringBuilder sb = new StringBuilder(key.toString());
		sb.append(", label '");
		sb.append(label);
		sb.append(", shortText '");
		sb.append(shortText);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("' and ");
		sb.append(modulePolicies.size());
		sb.append(" policies. created at ");
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
