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

import java.io.Serial;
import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;

/**
 * objekt fuer die m-n tabelle mapped consent template <-> consent template
 *
 * @author moser
 *
 */
@Entity
@Table(name = "mapped_consent_template")
@Cache(isolation = CacheIsolationType.PROTECTED)
public class MappedConsentTemplate implements Serializable
{
	@Serial
	private static final long serialVersionUID = -6454284653847570151L;

	@EmbeddedId
	private MappedConsentTemplateKey key;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "FROM_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "FROM_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "FROM_VERSION", referencedColumnName = "VERSION") })
	@MapsId("consentTemplateKeyFrom")
	private ConsentTemplate consentTemplateFrom;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "TO_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "TO_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "TO_VERSION", referencedColumnName = "VERSION") })
	@MapsId("consentTemplateKeyTo")
	private ConsentTemplate consentTemplateTo;

	public MappedConsentTemplate()
	{}

	public MappedConsentTemplate(ConsentTemplate consentTemplateFrom, ConsentTemplate consentTemplateTo)
	{
		this.consentTemplateFrom = consentTemplateFrom;
		this.consentTemplateTo = consentTemplateTo;
		this.key = new MappedConsentTemplateKey(consentTemplateFrom.getKey(), consentTemplateTo.getKey());
		// dto currently has no additional attributes
	}

	public MappedConsentTemplateKey getKey()
	{
		return key;
	}

	public ConsentTemplate getConsentTemplateFrom()
	{
		return consentTemplateFrom;
	}

	public ConsentTemplate getConsentTemplateTo()
	{
		return consentTemplateTo;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof MappedConsentTemplate that))
			return false;

		return new EqualsBuilder().append(getKey(), that.getKey()).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37).append(getKey()).toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this)
				.append("key", key)
				.append("consentTemplateFrom", consentTemplateFrom)
				.append("consentTemplateTo", consentTemplateTo)
				.toString();
	}
}
