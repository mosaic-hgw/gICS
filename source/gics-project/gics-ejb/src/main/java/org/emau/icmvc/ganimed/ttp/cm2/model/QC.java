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

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.Customizer;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.config.QualityControlConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCTypeStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCProblemDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

/**
 * qualitaetskontrolleintrag; kann ueberschrieben werden (→ automatische history)<br>
 * aktueller wert von "qc_passed" wird benutzt, um nur valide consente zu betrachten<br>
 * eintrag wird fuer jeden consent angelegt, gueltige werte fuer "type" werden in der domainconfig
 * konfiguriert<br>
 * ist dort nichts konfiguriert, wird automatisch ein eintrag mit "qc_passed" = true angelegt
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "qc")
// automatic history
@Customizer(QCHistCustomizer.class)
@Cache(isolation = CacheIsolationType.ISOLATED)
@UuidGenerator(name = "FHIR_ID_QC")
public class QC implements Serializable, FhirDTOExporter<QCDTO>
{
	@Serial
	private static final long serialVersionUID = -1121221171114223597L;
	@EmbeddedId
	private QCKey key;
	@Transient
	private boolean qcPassed;
	@Column(length = 100)
	private String type;
	@Column(length = 100)
	private String inspector;
	private Timestamp timestamp;
	@Column(length = 4095)
	private String comment;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME")
	@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME")
	@JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION")
	@JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE")
	@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID")
	@MapsId("consentKey")
	private Consent consent;

	@SuppressWarnings("FieldMayBeFinal")
	@OneToMany(mappedBy = "qc", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<QCProblem> problems = new ArrayList<>();

	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_QC")
	private String fhirID;

	public QC()
	{
		// for deserialization
	}

	public QC(Consent consent, QCDTO qcDTO, Timestamp date) throws InvalidParameterException
	{
		super();
		key = new QCKey(consent.getKey());
		this.consent = consent;
		update(qcDTO, date);
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void evaluateQCPassed()
	{
		this.qcPassed = getConfig().hasType(type, QCTypeStatus.VALID);
	}

	/**
	 * Simply a convenient delegator to the corresponding domain's QC config.
	 *
	 * @return the corresponding domain's QC config
	 */
	public QualityControlConfig getConfig()
	{
		return getConsent().getConsentTemplate().getDomain().getConfig().getQualityControlConfig();
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public QCKey getKey()
	{
		return key;
	}

	public boolean isQcPassed()
	{
		return qcPassed;
	}

	public String getType()
	{
		return type;
	}

	public String getInspector()
	{
		return inspector;
	}

	public Timestamp getTimestamp()
	{
		return timestamp;
	}

	public String getComment()
	{
		return comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public Consent getConsent()
	{
		return consent;
	}

	public List<QCProblem> getProblems()
	{
		return problems;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	@Override
	public QCDTO toDTO()
	{
		return new QCDTO(qcPassed, type, timestamp, inspector, comment, externProperties,
				getProblems().stream().map(QCProblem::toDTO).toList(), fhirID);
	}

	public void update(QCDTO dto, Timestamp timestamp) throws InvalidParameterException
	{
		update(dto, timestamp, getConfig());
	}

	public void update(QCDTO dto, Timestamp timestamp, QualityControlConfig config) throws InvalidParameterException
	{
		this.timestamp = timestamp;
		type = dto.getType() != null ? dto.getType() : config.getDefaultTypeId();

		if (!config.getValidQcTypeValues().contains(type) && !config.getInvalidQcTypeValues().contains(type))
		{
			throw new InvalidParameterException("The given config type (" + type + ") is not know within this domain: "
					+ "neither as valid " + config.getValidQcTypeValues() + " nor as invalid " + config.getInvalidQcTypeValues() + " config type");
		}
		qcPassed = config.getValidQcTypeValues().contains(type);
		inspector = dto.getInspector();
		comment = dto.getComment();
		externProperties = dto.getExternProperties();

		updateProblems(dto, timestamp);
	}

	public void updateProblems(QCDTO dto, Timestamp timestamp)
	{
		// remove orphaned, update current, and add new problems

		Set<QCProblemDTO.TypeRef> oldProblems = getProblems().stream().map(QCProblem::getTypeRef).collect(Collectors.toSet());
		Set<QCProblemDTO.TypeRef> newProblems = dto.getProblems().stream().map(QCProblemDTO::getTypeRef).collect(Collectors.toSet());

		getProblems().removeIf(p -> !newProblems.contains(p.getTypeRef()));
		getProblems().forEach(p -> p.update(dto.getProblemByTypeRef(p.getTypeRef()), timestamp)); // prefer update over new

		newProblems.removeAll(oldProblems);
		getProblems().addAll(dto.getProblems().stream().filter(p -> newProblems.contains(p.getTypeRef())).map(p -> new QCProblem(p, this)).toList());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QC qc))
			return false;

		return new EqualsBuilder()
				.append(isQcPassed(), qc.isQcPassed())
				.append(getKey(), qc.getKey())
				.append(getType(), qc.getType())
				.append(getInspector(), qc.getInspector())
				.append(getTimestamp(), qc.getTimestamp())
				.append(getComment(), qc.getComment())
				.append(getExternProperties(), qc.getExternProperties())
				.append(getProblems(), qc.getProblems())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getKey())
				.append(isQcPassed())
				.append(getType())
				.append(getInspector())
				.append(getTimestamp())
				.append(getComment())
				.append(getExternProperties())
				.append(getProblems()).toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("key", key)
				.append("qcPassed", qcPassed)
				.append("type", type)
				.append("inspector", inspector)
				.append("timestamp", timestamp)
				.append("comment", comment)
				.append("externProperties", externProperties)
				.append("problems", getProblems())
				.append("fhirID", fhirID)
				.toString();
	}
}
