package org.emau.icmvc.ganimed.ttp.cm2.dto;

/*
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;

/**
 * detected module results from parse consent scan
 *
 * @author bialkem
 *
 */
public class DetectedModuleDTO implements Serializable
{
	private static final long serialVersionUID = 9098296439476425872L;

	private ModuleKeyDTO key;

	// define confidence of the result
	private PARSING_RESULT_CONFIDENCE confidence;

	// a list if more than one consentstatus was marked on the parsed consent
	private final List<ConsentStatus> consentStatus = new ArrayList<>();

	public DetectedModuleDTO()
	{
		confidence = PARSING_RESULT_CONFIDENCE.OK;
	}

	public DetectedModuleDTO(ModuleKeyDTO moduleKey, List<ConsentStatus> consentStates, PARSING_RESULT_CONFIDENCE confidence)
	{
		setKey(moduleKey);
		setConsentStatus(consentStates);
		this.confidence = confidence;
	}

	public DetectedModuleDTO(DetectedModuleDTO dto)
	{
		this(dto.getKey(), dto.getConsentStatus(), dto.getParseResult());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (confidence == null ? 0 : confidence.hashCode());
		result = prime * result + (consentStatus == null ? 0 : consentStatus.hashCode());
		result = prime * result + (key == null ? 0 : key.hashCode());
		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;

		for (ConsentStatus s : consentStatus)
		{
			sb.append("[" + i + "]='" + s);

			if (i != consentStatus.size() - 1)
			{
				sb.append(";");
			}
		}
		return "DetectedModuleDTO [key=" + key + ", parseResult=" + confidence + ", consentStatus=" + sb.toString() + "]";
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
		DetectedModuleDTO other = (DetectedModuleDTO) obj;
		if (confidence != other.confidence)
		{
			return false;
		}
		if (consentStatus == null)
		{
			if (other.consentStatus != null)
			{
				return false;
			}
		}
		else if (!consentStatus.equals(other.consentStatus))
		{
			return false;
		}
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

	public PARSING_RESULT_CONFIDENCE getParseResult()
	{
		return confidence;
	}

	public void setParseResultConfidence(PARSING_RESULT_CONFIDENCE confidence)
	{
		this.confidence = confidence;
	}

	public List<ConsentStatus> getConsentStatus()
	{
		return consentStatus;
	}

	public void setConsentStatus(List<ConsentStatus> consentStatus)
	{
		if (this.consentStatus != consentStatus)
		{
			this.consentStatus.clear();
			if (consentStatus != null)
			{
				this.consentStatus.addAll(consentStatus);
			}
		}
	}

	public ModuleKeyDTO getKey()
	{
		return key;
	}

	public void setKey(ModuleKeyDTO key)
	{
		if (key != null)
		{
			this.key = new ModuleKeyDTO(key);
		}
		else
		{
			this.key = null;
		}
	}

	public enum PARSING_RESULT_CONFIDENCE
	{
		OK, REVISE_TOO_MANY, REVISE_EMPTY
	}
}
