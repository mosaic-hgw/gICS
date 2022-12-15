package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.util.Date;

/**
 * qualitaetskontrolleintrag fuer einen consent
 *
 * @author geidell
 *
 */
public class QCDTO extends FhirIdDTO implements Serializable
{
	private static final long serialVersionUID = 4991404489076473605L;
	public static final String AUTO_GENERATED = "###_auto_generated_###";
	public static final String EMPTY_STRING = "";
	private boolean qcPassed = true;
	private String type = null;
	private Date date;
	private String inspector;
	private String comment;
	private String externProperties;

	public QCDTO()
	{
		super(null);
	}

	public QCDTO(boolean qcPassed, String type, Date date, String inspector, String comment, String externProperties, String fhirID)
	{
		super(fhirID);
		this.qcPassed = qcPassed;
		this.type = type;
		this.date = date;
		this.inspector = inspector;
		this.comment = comment;
		this.externProperties = externProperties;
	}

	/**
	 * is read-only because it will be set according to the type and domain-config
	 *
	 * @return
	 */
	public boolean isQcPassed()
	{
		return qcPassed;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getInspector()
	{
		if (inspector != null)
		{
			return inspector;
		}
		else
		{
			return EMPTY_STRING;
		}
	}

	public void setInspector(String inspector)
	{
		this.inspector = inspector;
	}

	public String getComment()
	{
		if (comment != null)
		{
			return comment;
		}
		else
		{
			return EMPTY_STRING;
		}
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public void setExternProperties(String externProperties)
	{
		this.externProperties = externProperties;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (date == null ? 0 : date.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (inspector == null ? 0 : inspector.hashCode());
		result = prime * result + (qcPassed ? 1231 : 1237);
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		QCDTO other = (QCDTO) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (date == null)
		{
			if (other.date != null)
			{
				return false;
			}
		}
		else if (!date.equals(other.date))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (inspector == null)
		{
			if (other.inspector != null)
			{
				return false;
			}
		}
		else if (!inspector.equals(other.inspector))
		{
			return false;
		}
		if (qcPassed != other.qcPassed)
		{
			return false;
		}
		if (type == null)
		{
			if (other.type != null)
			{
				return false;
			}
		}
		else if (!type.equals(other.type))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "QCDTO [qcPassed=" + qcPassed + ", type=" + type + ", date=" + date + ", inspector=" + inspector + ", comment=" + comment + ", externProperties=" + externProperties + ". "
				+ super.toString() + "]";
	}
}
