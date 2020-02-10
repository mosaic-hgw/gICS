package org.emau.icmvc.ganimed.ttp.cm2.dto;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.FreeTextType;

/**
 * freitext
 * 
 * @author geidell
 * 
 */
public class FreeTextDefDTO implements Serializable {

	private static final long serialVersionUID = -5624268522251227994L;
	private String name;
	private boolean required;
	private FreeTextType type;
	private String converterString;
	private int pos;
	private String comment;

	public FreeTextDefDTO() {
	}

	public FreeTextDefDTO(String name, boolean required, FreeTextType type, String converterString, int pos, String comment) {
		super();
		this.name = name;
		this.required = required;
		this.type = type;
		this.converterString = converterString;
		this.pos = pos;
		this.comment = comment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public FreeTextType getType() {
		return type;
	}

	public void setType(FreeTextType type) {
		this.type = type;
	}

	public String getConverterString() {
		return converterString;
	}

	public void setConverterString(String converterString) {
		this.converterString = converterString;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((converterString == null) ? 0 : converterString.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + pos;
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
		FreeTextDefDTO other = (FreeTextDefDTO) obj;
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pos != other.pos)
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
		sb.append("FreeTextDTO with name '");
		sb.append(name);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("' type '");
		sb.append((type == null) ? "null" : type.toString());
		sb.append("' converter string '");
		sb.append(converterString);
		sb.append("' at pos ");
		sb.append(pos);
		sb.append(" which value is ");
		sb.append(required ? "" : "not ");
		sb.append("required within a consent");
		return sb.toString();
	}
}
