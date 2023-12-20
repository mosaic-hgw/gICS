package org.emau.icmvc.ganimed.ttp.cm2.dto;

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

import java.util.Date;

/**
 * history fuer die qualitaetskontrolleintraege fuer einen consent
 *
 * @author geidell
 *
 */
public class QCHistoryDTO extends QCDTO
{
	private static final long serialVersionUID = -7104867149344327499L;
	private Date startDate;
	private Date endDate;

	public QCHistoryDTO()
	{}

	public QCHistoryDTO(boolean qcPassed, String type, Date date, String inspector, String comment, String externProperties, Date startDate, Date endDate, String fhirID)
	{
		super(qcPassed, type, date, inspector, comment, externProperties, fhirID);
		setStartDate(startDate);
		setEndDate(endDate);
	}

	public QCHistoryDTO(QCHistoryDTO dto)
	{
		this(dto.isQcPassed(), dto.getType(), dto.getDate(), dto.getInspector(), dto.getComment(), dto.getExternProperties(), dto.getStartDate(), dto.getEndDate(), dto.getFhirID());
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		if (startDate != null)
		{
			this.startDate = new Date(startDate.getTime());
		}
		else
		{
			this.startDate = null;
		}
	}

	public Date getEndDate()
	{
		return endDate;
	}

	public void setEndDate(Date endDate)
	{
		if (endDate != null)
		{
			this.endDate = new Date(endDate.getTime());
		}
		else
		{
			this.endDate = null;
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (endDate == null ? 0 : endDate.hashCode());
		result = prime * result + (startDate == null ? 0 : startDate.hashCode());
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
		QCHistoryDTO other = (QCHistoryDTO) obj;
		if (endDate == null)
		{
			if (other.endDate != null)
			{
				return false;
			}
		}
		else if (!endDate.equals(other.endDate))
		{
			return false;
		}
		if (startDate == null)
		{
			if (other.startDate != null)
			{
				return false;
			}
		}
		else if (!startDate.equals(other.startDate))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "QCHistoryDTO [startDate=" + startDate + ", endDate=" + endDate + "] of " + super.toString();
	}
}
