package org.emau.icmvc.ganimed.ttp.cm2.model;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
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
import java.text.SimpleDateFormat;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.FreeTextConverterStringException;

/**
 * freitext feld definition zu einem consent-template
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "free_text_def")
@Cache(isolation = CacheIsolationType.PROTECTED)
public class FreeTextDef implements Serializable {

	private static final long serialVersionUID = 469494132639384044L;
	@EmbeddedId
	private FreeTextDefKey key;
	private boolean required;
	private FreeTextType type;
	private String converterString;
	@OrderColumn
	private int pos;
	private String comment;
	@ManyToOne
	@JoinColumns({ @JoinColumn(name = "DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "VERSION") })
	@MapsId("consentTemplateKey")
	private ConsentTemplate consentTemplate;

	public FreeTextDef() {
	}

	public FreeTextDef(ConsentTemplate consentTemplate, String name, boolean required, FreeTextType type, String converterString, String comment) {
		super();
		this.key = new FreeTextDefKey(consentTemplate.getKey(), name);
		this.required = required;
		this.type = type;
		this.converterString = converterString;
		this.comment = comment;
		this.consentTemplate = consentTemplate;
	}

	public FreeTextDef(ConsentTemplate consentTemplate, FreeTextDefDTO dto) throws FreeTextConverterStringException {
		super();
		this.key = new FreeTextDefKey(consentTemplate.getKey(), dto.getName());
		this.required = dto.getRequired();
		this.type = dto.getType();
		this.converterString = dto.getConverterString();
		if (type.equals(FreeTextType.Date)) {
			if (converterString == null) {
				throw new FreeTextConverterStringException("the converter string must not be null if type is FreeTextType.Date");
			}
			try {
				new SimpleDateFormat(converterString);
			} catch (IllegalArgumentException e) {
				throw new FreeTextConverterStringException(
						"the converter string must be a valid pattern for SimpleDateFormat if type is FreeTextType.Date", e);
			}
		}
		this.comment = dto.getComment();
		this.consentTemplate = consentTemplate;
	}

	public FreeTextDefKey getKey() {
		return key;
	}

	public boolean getRequired() {
		return required;
	}

	public FreeTextType getType() {
		return type;
	}

	public String getConverterString() {
		return converterString;
	}

	public String getComment() {
		return comment;
	}

	public ConsentTemplate getConsentTemplate() {
		return consentTemplate;
	}

	public FreeTextDefDTO toDTO() {
		FreeTextDefDTO result = new FreeTextDefDTO();
		result.setName(key.getName());
		result.setComment(comment);
		result.setConverterString(converterString);
		result.setRequired(required);
		result.setType(type);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((converterString == null) ? 0 : converterString.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + (required ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		FreeTextDef other = (FreeTextDef) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (converterString == null) {
			if (other.converterString != null)
				return false;
		} else if (!converterString.equals(other.converterString))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (required != other.required)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
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
		sb.append("required within a consent");
		return sb.toString();
	}
}
