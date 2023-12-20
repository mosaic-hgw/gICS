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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.cm2.exceptions.NoValueException;

/**
 * ein consent ist ein ausgefuelltes und unterschriebenes konsentdokument (consent template)
 *
 * @author geidell
 *
 */
public class ConsentDTO extends ConsentLightDTO implements Serializable
{
	private static final long serialVersionUID = 3503483452976332942L;
	private String patientSignatureBase64;
	private String physicianSignatureBase64;
	private final List<ConsentScanDTO> scans = new ArrayList<>();
	private final List<FreeTextValDTO> freeTextVals = new ArrayList<>();

	public ConsentDTO()
	{
		super();
	}

	public ConsentDTO(ConsentKeyDTO key)
	{
		super(key);
	}

	public ConsentDTO(ConsentLightDTO lightDTO)
	{
		super(lightDTO);
	}

	public ConsentDTO(ConsentDTO dto)
	{
		super(dto);
		setPatientSignatureBase64(dto.getPatientSignatureBase64());
		setPhysicianSignatureBase64(dto.getPhysicianSignatureBase64());
		setScans(dto.getScans());
		setFreeTextVals(dto.getFreeTextVals());
	}

	public String getPatientSignatureBase64()
	{
		return patientSignatureBase64;
	}

	public void setPatientSignatureBase64(String patientSignatureBase64)
	{
		this.patientSignatureBase64 = patientSignatureBase64;
	}

	public String getPhysicianSignatureBase64()
	{
		return physicianSignatureBase64;
	}

	public void setPhysicianSignatureBase64(String physicianSignatureBase64)
	{
		this.physicianSignatureBase64 = physicianSignatureBase64;
	}

	public List<ConsentScanDTO> getScans()
	{
		return scans;
	}

	public void setScans(List<ConsentScanDTO> scans)
	{
		if (this.scans != scans)
		{
			this.scans.clear();
			if (scans != null)
			{
				for (ConsentScanDTO scan : scans)
				{
					this.scans.add(new ConsentScanDTO(scan));
				}
			}
		}
	}

	public void addScan(ConsentScanDTO consentScanDTO)
	{
		scans.add(consentScanDTO);
	}

	public List<FreeTextValDTO> getFreeTextVals()
	{
		return freeTextVals;
	}

	public void setFreeTextVals(List<FreeTextValDTO> freeTextVals)
	{
		if (this.freeTextVals != freeTextVals)
		{
			this.freeTextVals.clear();
			if (freeTextVals != null)
			{
				for (FreeTextValDTO dto : freeTextVals)
				{
					this.freeTextVals.add(new FreeTextValDTO(dto));
				}
			}
		}
	}

	public FreeTextValDTO getFreeTextValForDef(String freeTextDefName) throws NoValueException
	{
		for (FreeTextValDTO freeTextValDTO : freeTextVals)
		{
			if (freeTextValDTO.getFreeTextDefName().equals(freeTextDefName))
			{
				return freeTextValDTO;
			}
		}
		throw new NoValueException("no freeTextValue found for FreeTextDef " + freeTextDefName);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (freeTextVals == null ? 0 : freeTextVals.hashCode());
		result = prime * result + (patientSignatureBase64 == null ? 0 : patientSignatureBase64.hashCode());
		result = prime * result + (physicianSignatureBase64 == null ? 0 : physicianSignatureBase64.hashCode());
		result = prime * result + (scans == null ? 0 : scans.hashCode());
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
		ConsentDTO other = (ConsentDTO) obj;
		if (freeTextVals == null)
		{
			if (other.freeTextVals != null)
			{
				return false;
			}
		}
		else if (!freeTextVals.equals(other.freeTextVals))
		{
			return false;
		}
		if (patientSignatureBase64 == null)
		{
			if (other.patientSignatureBase64 != null)
			{
				return false;
			}
		}
		else if (!patientSignatureBase64.equals(other.patientSignatureBase64))
		{
			return false;
		}
		if (physicianSignatureBase64 == null)
		{
			if (other.physicianSignatureBase64 != null)
			{
				return false;
			}
		}
		else if (!physicianSignatureBase64.equals(other.physicianSignatureBase64))
		{
			return false;
		}
		if (scans == null)
		{
			if (other.scans != null)
			{
				return false;
			}
		}
		else if (!scans.equals(other.scans))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append(", ");
		sb.append(scans.size());
		sb.append(" scans and ");
		sb.append(freeTextVals.size());
		sb.append(" free text values");
		return sb.toString();
	}
}
