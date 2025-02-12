package org.emau.icmvc.ganimed.ttp.cm2.model;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
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
import java.text.ParseException;
import java.time.format.DateTimeParseException;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpressionExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;

/**
 * objekt fuer die m-n tabelle consent<->policy
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "signed_policy")
@Cacheable(false)
@Cache(isolation = CacheIsolationType.ISOLATED)
@UuidGenerator(name = "FHIR_ID_SP")
public class SignedPolicy implements Serializable, FhirDTOExporter<SignedPolicyDTO>
{
	private static final long serialVersionUID = 8649537030386492059L;
	private static final Logger logger = LogManager.getLogger(SignedPolicy.class);
	@EmbeddedId
	private SignedPolicyKey key;
	@Enumerated
	private ConsentStatus status;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "POLICY_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "POLICY_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "POLICY_VERSION", referencedColumnName = "VERSION") })
	@MapsId("policyKey")
	private Policy policy;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION"),
			@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE"),
			@JoinColumn(name = "CONSENT_VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID") })
	@MapsId("consentKey")
	private Consent consent;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_SP")
	private String fhirID;
	@Column(name = "EXPIRATION_PROPERTIES")
	private String expirationProperties;
	@Transient
	private ExpressionExpirationPropertiesObject expirationPropertiesObject = null;

	public SignedPolicy()
	{}

	public SignedPolicy(Consent consent, Policy policy, ConsentStatus status)
	{
		super();
		this.key = new SignedPolicyKey(consent.getKey(), policy.getKey());
		this.status = status;
		this.consent = consent;
		this.policy = policy;
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public SignedPolicyKey getKey()
	{
		return key;
	}

	public ConsentStatus getStatus()
	{
		return status;
	}

	public Policy getPolicy()
	{
		return policy;
	}

	public Consent getConsent()
	{
		return consent;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public String getExpirationProperties()
	{
		return expirationProperties;
	}

	public void setExpirationProperties(String expirationProperties)
	{
		this.expirationProperties = expirationProperties;
		loadPropertiesFromString();
	}


	public ExpressionExpirationPropertiesObject getExpirationPropertiesObject()
	{
		if (expirationPropertiesObject == null)
		{
			loadPropertiesFromString();
		}
		return expirationPropertiesObject;
	}

	public void setExpirationPropertiesObject(ExpressionExpirationPropertiesObject expirationPropertiesObject)
	{
		this.expirationPropertiesObject = expirationPropertiesObject;
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void loadPropertiesFromString()
	{
		try
		{
			expirationPropertiesObject = new ExpressionExpirationPropertiesObject(expirationProperties);
		}
		catch (ParseException | DateTimeParseException e)
		{
			logger.fatal("exception while parsing expirationProperties '{}'", expirationProperties, e);
		}
	}


	@Override
	public SignedPolicyDTO toDTO() throws InvalidVersionException, InconsistentStatusException, UnknownDomainException
	{
		PolicyKeyDTO pKeyDTO = key.getPolicyKey().toDTO(VersionConverterCache.getPolicyVersionConverter(key.getPolicyKey().getDomainName()));
		ConsentKeyDTO ctKeyDTO = key.getConsentKey().toDTO(VersionConverterCache.getCTVersionConverter(key.getConsentKey().getCtKey().getDomainName()), consent.getVirtualPerson());
		return new SignedPolicyDTO(status, pKeyDTO, ctKeyDTO, fhirID);
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
		SignedPolicy other = (SignedPolicy) obj;
		if (key == null)
		{
			return other.key == null;
		}
		else
			return key.equals(other.key);
	}

	@Override
	public String toString()
	{
		return key + " with value: " + status.toString() + ". FHIR-ID: " + fhirID;
	}
}
