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
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.ObjectInUseException;

/**
 * freitextfeld-definition zu einem consent-template
 *
 * @author geidell
 *
 */
@Entity
@Table(name = "free_text_def")
@Cache(isolation = CacheIsolationType.PROTECTED)
@UuidGenerator(name = "FHIR_ID_FTD")
public class FreeTextDef implements Serializable, FhirDTOExporter<FreeTextDefDTO>
{
	private static final long serialVersionUID = 415314820296202636L;
	@EmbeddedId
	private FreeTextDefKey key;
	private boolean required;
	private FreeTextType type;
	private String converterString;
	private int pos;
	private String comment;
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "VERSION") })
	@MapsId("consentTemplateKey")
	private ConsentTemplate consentTemplate;
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Timestamp updateTimestamp;
	@Column(name = "FINALISED", nullable = false)
	private boolean finalised;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_FTD")
	private String fhirID;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;
	@Column(name = "LABEL", length = 255)
	private String label;

	public FreeTextDef()
	{}

	public FreeTextDef(ConsentTemplate consentTemplate, FreeTextDefDTO dto) throws FreeTextConverterStringException
	{
		super();
		this.key = new FreeTextDefKey(consentTemplate.getKey(), dto.getName());
		this.required = dto.getRequired();
		this.type = dto.getType();
		this.converterString = dto.getConverterString();
		if (type.equals(FreeTextType.DATE))
		{
			if (converterString == null)
			{
				throw new FreeTextConverterStringException("the converter string must not be null if type is FreeTextType.Date");
			}
			try
			{
				new SimpleDateFormat(converterString);
			}
			catch (IllegalArgumentException e)
			{
				throw new FreeTextConverterStringException(
						"the converter string must be a valid pattern for SimpleDateFormat if type is FreeTextType.Date", e);
			}
		}
		this.comment = dto.getComment();
		this.consentTemplate = consentTemplate;
		setFinalised(dto.getFinalised());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		this.createTimestamp = timestamp;
		this.updateTimestamp = timestamp;
		this.pos = dto.getPos();
		this.label = dto.getLabel();
		this.externProperties = dto.getExternProperties();
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public FreeTextDefKey getKey()
	{
		return key;
	}

	public boolean getRequired()
	{
		return required;
	}

	public FreeTextType getType()
	{
		return type;
	}

	public String getConverterString()
	{
		return converterString;
	}

	public int getPos()
	{
		return pos;
	}

	public String getComment()
	{
		return comment;
	}

	public ConsentTemplate getConsentTemplate()
	{
		return consentTemplate;
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

	public String getExternProperties()
	{
		return externProperties;
	}

	public String getLabel()
	{
		return label;
	}

	public void finalise()
	{
		setFinalised(true);
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void updateInUse(String comment, String externProperties, String label, int pos)
	{
		this.comment = comment;
		this.externProperties = externProperties;
		this.label = label;
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
		this.pos = pos;
	}

	public void update(FreeTextDefDTO dto) throws FreeTextConverterStringException, ObjectInUseException
	{
		if (finalised)
		{
			throw new ObjectInUseException(
					"freetext definition is finalised and can't be updated anymore. to update non-critical fields please use updateFreeTextDefInUse(...)");
		}
		updateInUse(dto.getComment(), dto.getExternProperties(), dto.getLabel(), dto.getPos());
		this.required = dto.getRequired();
		this.type = dto.getType();
		this.converterString = dto.getConverterString();
		if (type.equals(FreeTextType.DATE))
		{
			if (converterString == null)
			{
				throw new FreeTextConverterStringException("the converter string must not be null if type is FreeTextType.Date");
			}
			try
			{
				new SimpleDateFormat(converterString);
			}
			catch (IllegalArgumentException e)
			{
				throw new FreeTextConverterStringException(
						"the converter string must be a valid pattern for SimpleDateFormat if type is FreeTextType.Date", e);
			}
		}
		setFinalised(dto.getFinalised());
	}

	@Override
	public FreeTextDefDTO toDTO()
	{
		FreeTextDefDTO result = new FreeTextDefDTO(key.getName(), required, type, converterString, pos, comment, finalised,
				new Date(createTimestamp.getTime()), new Date(updateTimestamp.getTime()), fhirID, externProperties, label);
		return result;
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
		FreeTextDef other = (FreeTextDef) obj;
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
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with comment '");
		sb.append(comment);
		sb.append("' type '");
		sb.append(type.toString());
		sb.append("' converter string '");
		sb.append(converterString);
		sb.append("' which value is ");
		sb.append(required ? "" : "not ");
		sb.append("required within a consent. is finalised '");
		sb.append(finalised);
		sb.append("' created at ");
		sb.append(new Date(createTimestamp.getTime()));
		sb.append(" last update at ");
		sb.append(new Date(updateTimestamp.getTime()));
		sb.append(". FHIR-ID: ");
		sb.append(fhirID);
		sb.append(", extern properties: ");
		sb.append(externProperties);
		sb.append(", label: ");
		sb.append(label);
		return sb.toString();
	}

	public static class FreeTextDefComparator implements Comparator<FreeTextDef>, Serializable
	{
		private static final long serialVersionUID = -5066372783132922095L;

		@Override
		public int compare(FreeTextDef ftd1, FreeTextDef ftd2)
		{
			return ftd1.pos - ftd2.pos;
		}
	}
}
