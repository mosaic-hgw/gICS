package org.emau.icmvc.ganimed.ttp.cm2.dto;

/*
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * contains templatekey, signerids, detected modules with detected consentstates and confidence
 * level
 *
 * @author bialkem
 *
 */
public class ConsentParseResultDTO implements Serializable
{


	/**
	 *
	 */
	private static final long serialVersionUID = -4802029767447176153L;

	private ConsentTemplateKeyDTO detectedTemplateKey;

	// a list of vdetected modules with detected consentstates and confidence
	private List<DetectedModuleDTO> detectedModules = new ArrayList<>();

	// list of detected signerids
	private List<SignerIdDTO> detectedSignerIds = new ArrayList<>();

	private Date detectedPatientSigningDate = null;
	private Date detectedPhysicianSigningDate = null;
	private String detectedPatientSigningPlace = "";
	private String detectedPhysicianSigningPlace = "";

	// list of undetected modules (should be contained in template
	private List<AssignedModuleDTO> undedectedModules;

	// true if template qr code height below specified value
	private Boolean scalingError = false;

	public Boolean getScalingError()
	{
		return scalingError;
	}

	public void setScalingError(Boolean scalingError)
	{
		this.scalingError = scalingError;
	}

	public ConsentParseResultDTO()
	{
		// Do nothing, empty on purpose
	}

	@Override
	public String toString()
	{
		return "ConsentParseResultDTO [detectedTemplateKey=" + detectedTemplateKey + ", detectedModules=" + detectedModules + ", detectedSignerIds=" + detectedSignerIds
				+ ", detectedPatientSigningDate=" + detectedPatientSigningDate + ", detectedPatientSigningPlace=" + detectedPatientSigningPlace
				+ ", detectedPhysicianSigningDate=" + detectedPhysicianSigningDate + ", detectedPhysicianSigningPlace=" + detectedPhysicianSigningPlace
				+ ", undedectedModules=" + undedectedModules + ", scalingError=" + scalingError + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((detectedModules == null) ? 0 : detectedModules.hashCode());
		result = prime * result + ((detectedSignerIds == null) ? 0 : detectedSignerIds.hashCode());
		result = prime * result + ((detectedTemplateKey == null) ? 0 : detectedTemplateKey.hashCode());
		result = prime * result + ((detectedPatientSigningDate == null) ? 0 : detectedPatientSigningDate.hashCode());
		result = prime * result + ((detectedPatientSigningPlace == null) ? 0 : detectedPatientSigningPlace.hashCode());
		result = prime * result + ((detectedPhysicianSigningDate == null) ? 0 : detectedPhysicianSigningDate.hashCode());
		result = prime * result + ((detectedPhysicianSigningPlace == null) ? 0 : detectedPhysicianSigningPlace.hashCode());
		result = prime * result + ((scalingError == null) ? 0 : scalingError.hashCode());
		result = prime * result + ((undedectedModules == null) ? 0 : undedectedModules.hashCode());
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
		ConsentParseResultDTO other = (ConsentParseResultDTO) obj;
		if (detectedModules == null)
		{
			if (other.detectedModules != null)
			{
				return false;
			}
		}
		else if (!detectedModules.equals(other.detectedModules))
		{
			return false;
		}
		if (detectedSignerIds == null)
		{
			if (other.detectedSignerIds != null)
			{
				return false;
			}
		}
		else if (!detectedSignerIds.equals(other.detectedSignerIds))
		{
			return false;
		}
		if (detectedTemplateKey == null)
		{
			if (other.detectedTemplateKey != null)
			{
				return false;
			}
		}
		else if (!detectedTemplateKey.equals(other.detectedTemplateKey))
		{
			return false;
		}
		if (scalingError == null)
		{
			if (other.scalingError != null)
			{
				return false;
			}
		}
		else if (!scalingError.equals(other.scalingError))
		{
			return false;
		}
		if (undedectedModules == null)
		{
			if (other.undedectedModules != null)
			{
				return false;
			}
		}
		else if (!undedectedModules.equals(other.undedectedModules))
		{
			return false;
		}

		if (detectedPhysicianSigningPlace == null)
		{
			if (other.detectedPhysicianSigningPlace != null)
			{
				return false;
			}
		}
		else if (!detectedPhysicianSigningPlace.equals(other.detectedPhysicianSigningPlace))
		{
			return false;
		}

		if (detectedPhysicianSigningDate == null)
		{
			if (other.detectedPhysicianSigningDate != null)
			{
				return false;
			}
		}
		else if (!detectedPhysicianSigningDate.equals(other.detectedPhysicianSigningDate))
		{
			return false;
		}

		if (detectedPatientSigningPlace == null)
		{
			if (other.detectedPatientSigningPlace != null)
			{
				return false;
			}
		}
		else if (!detectedPatientSigningPlace.equals(other.detectedPatientSigningPlace))
		{
			return false;
		}

		if (detectedPatientSigningDate == null)
		{
			if (other.detectedPatientSigningDate != null)
			{
				return false;
			}
		}
		else if (!detectedPatientSigningDate.equals(other.detectedPatientSigningDate))
		{
			return false;
		}

		return true;
	}

	public Date getDetectedPatientSigningDate()
	{
		return detectedPatientSigningDate;
	}

	public void setDetectedPatientSigningDate(Date detectedPatientSigningDate)
	{
		this.detectedPatientSigningDate = detectedPatientSigningDate;
	}

	public Date getDetectedPhysicianSigningDate()
	{
		return detectedPhysicianSigningDate;
	}

	public void setDetectedPhysicianSigningDate(Date detectedPhysicianSigningDate)
	{
		this.detectedPhysicianSigningDate = detectedPhysicianSigningDate;
	}

	public String getDetectedPatientSigningPlace()
	{
		return detectedPatientSigningPlace;
	}

	public void setDetectedPatientSigningPlace(String detectedPatientSigningPlace)
	{
		this.detectedPatientSigningPlace = detectedPatientSigningPlace;
	}

	public String getDetectedPhysicianSigningPlace()
	{
		return detectedPhysicianSigningPlace;
	}

	public void setDetectedPhysicianSigningPlace(String detectedPhysicianSigningPlace)
	{
		this.detectedPhysicianSigningPlace = detectedPhysicianSigningPlace;
	}

	public ConsentTemplateKeyDTO getDetectedTemplateKey()
	{
		return detectedTemplateKey;
	}

	public List<SignerIdDTO> getDetectedSignerIds()
	{
		return detectedSignerIds;
	}

	public List<DetectedModuleDTO> getDetectedModuleStates()
	{
		return detectedModules;
	}

	public void setDetectedModuleStates(List<DetectedModuleDTO> modules)
	{
		detectedModules = modules;
	}

	public void setDetectedTemplateKey(ConsentTemplateKeyDTO detectedTemplateKey)
	{
		this.detectedTemplateKey = detectedTemplateKey;
	}

	public void setDetectedSignerIds(List<SignerIdDTO> toBeUsedSids)
	{
		detectedSignerIds = toBeUsedSids;
	}

	public void addDetectedModuleState(DetectedModuleDTO module)
	{
		if (module != null)
		{
			detectedModules.add(module);
		}
	}


	public void setMissingModules(List<AssignedModuleDTO> missingModules)
	{
		// if some module should be contained in template, but where not detected, mark them as
		// missing
		undedectedModules = missingModules;
	}

	public List<AssignedModuleDTO> getMissingModules()
	{
		return undedectedModules;

	}
}


