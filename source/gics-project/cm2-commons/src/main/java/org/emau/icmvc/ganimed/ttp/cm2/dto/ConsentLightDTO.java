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

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;

/**
 * ein consent ist ein ausgefuelltes und unterschriebenes konsentdokument (consent template)<br>
 * das light dto enthaelt keine scans und keine freetextvals um sowohl datenmenge als auch
 * db-zugriffe zu minimieren<br>
 *
 * @author geidell
 *
 */
public class ConsentLightDTO extends FhirIdDTO implements Serializable, DomainRelated
{
	@Serial
	private static final long serialVersionUID = -7225102544986077075L;
	public static final String NO_REAL_SIGNATURE = "no real signature";
	public static final String NO_SIGNATURE = "no signature";
	private ConsentKeyDTO key;
	private boolean patientSignatureIsFromGuardian;
	private boolean hasPatientSignature;
	private String physicianId;
	private Date patientSigningDate;
	private String patientSigningPlace;
	private Date physicianSigningDate;
	private String physicianSigningPlace;
	private String comment;
	private String externProperties;
	private ConsentTemplateType templateType;
	private final Map<ModuleKeyDTO, ModuleStateDTO> moduleStates = new HashMap<>();
	private Date creationDate;
	private Date updateDate;
	private Date validFromDate;
	private QCDTO qualityControl;
	private ConsentDateValuesDTO consentDates = new ConsentDateValuesDTO();
	private ExpirationPropertiesDTO expirationProperties = new ExpirationPropertiesDTO();

	public ConsentLightDTO()
	{
		super(null);
	}

	public ConsentLightDTO(ConsentKeyDTO key)
	{
		super(null);
		setKey(key);
	}

	public ConsentLightDTO(ConsentLightDTO lightDTO)
	{
		super(lightDTO.getFhirID());
		setKey(lightDTO.getKey());
		setComment(lightDTO.getComment());
		setExternProperties(lightDTO.getExternProperties());
		setExpirationProperties(lightDTO.getExpirationProperties());
		setModuleStates(lightDTO.getModuleStates());
		setPatientSignatureIsFromGuardian(lightDTO.getPatientSignatureIsFromGuardian());
		setHasPatientSignature(lightDTO.getHasPatientSignature());
		setPatientSigningDate(lightDTO.getPatientSigningDate());
		setPatientSigningPlace(lightDTO.getPatientSigningPlace());
		setPhysicianId(lightDTO.getPhysicianId());
		setPhysicianSigningDate(lightDTO.getPhysicianSigningDate());
		setPhysicianSigningPlace(lightDTO.getPhysicianSigningPlace());
		setTemplateType(lightDTO.getTemplateType());
		setValidFromDate(lightDTO.getValidFromDate());
		setCreationDate(lightDTO.getCreationDate());
		setUpdateDate(lightDTO.getUpdateDate());
		setQualityControl(lightDTO.getQualityControl());
		setFhirID(lightDTO.getFhirID());
		setConsentDates(lightDTO.getConsentDates());
	}

	public ConsentKeyDTO getKey()
	{
		return key;
	}

	public void setKey(ConsentKeyDTO key)
	{
		if (key != null)
		{
			this.key = key;
		}
		else
		{
			this.key = null;
		}
	}

	public boolean getPatientSignatureIsFromGuardian()
	{
		return patientSignatureIsFromGuardian;
	}

	public void setPatientSignatureIsFromGuardian(boolean patientSignatureIsFromGuardian)
	{
		this.patientSignatureIsFromGuardian = patientSignatureIsFromGuardian;
	}

	public boolean getHasPatientSignature()
	{
		return hasPatientSignature;
	}

	public void setHasPatientSignature(boolean hasPatientSignature)
	{
		this.hasPatientSignature = hasPatientSignature;
	}

	public String getPhysicianId()
	{
		return physicianId;
	}

	public void setPhysicianId(String physicianId)
	{
		this.physicianId = physicianId;
	}

	public Date getPatientSigningDate()
	{
		return patientSigningDate;
	}

	public void setPatientSigningDate(Date patientSigningDate)
	{
		if (patientSigningDate != null)
		{
			this.patientSigningDate = new Date(patientSigningDate.getTime());
		}
		else
		{
			this.patientSigningDate = null;
		}
	}

	public String getPatientSigningPlace()
	{
		return patientSigningPlace;
	}

	public void setPatientSigningPlace(String patientSigningPlace)
	{
		this.patientSigningPlace = patientSigningPlace;
	}

	public Date getPhysicianSigningDate()
	{
		return physicianSigningDate;
	}

	public void setPhysicianSigningDate(Date physicianSigningDate)
	{
		if (physicianSigningDate != null)
		{
			this.physicianSigningDate = new Date(physicianSigningDate.getTime());
		}
		else
		{
			this.physicianSigningDate = null;
		}
	}

	public String getPhysicianSigningPlace()
	{
		return physicianSigningPlace;
	}

	public void setPhysicianSigningPlace(String physicianSigningPlace)
	{
		this.physicianSigningPlace = physicianSigningPlace;
	}

	public String getComment()
	{
		return comment;
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

	public ConsentTemplateType getTemplateType()
	{
		return templateType;
	}

	public void setTemplateType(ConsentTemplateType templateType)
	{
		this.templateType = templateType;
	}

	public Map<ModuleKeyDTO, ModuleStateDTO> getModuleStates()
	{
		return moduleStates;
	}

	public void setModuleStates(Map<ModuleKeyDTO, ModuleStateDTO> moduleStates)
	{
		if (this.moduleStates != moduleStates)
		{
			this.moduleStates.clear();
			if (moduleStates != null)
			{
				for (Entry<ModuleKeyDTO, ModuleStateDTO> entry : moduleStates.entrySet())
				{
					this.moduleStates.put(new ModuleKeyDTO(entry.getKey()), new ModuleStateDTO(entry.getValue()));
				}
			}
		}
	}

	public Date getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(Date creationDate)
	{
		if (creationDate != null)
		{
			this.creationDate = new Date(creationDate.getTime());
		}
		else
		{
			this.creationDate = null;
		}
	}

	public Date getUpdateDate()
	{
		return updateDate;
	}

	public void setUpdateDate(Date updateDate)
	{
		if (updateDate != null)
		{
			this.updateDate = new Date(updateDate.getTime());
		}
		else
		{
			this.updateDate = null;
		}
	}

	public Date getValidFromDate()
	{
		return validFromDate;
	}

	public void setValidFromDate(Date validFromDate)
	{
		if (validFromDate != null)
		{
			this.validFromDate = new Date(validFromDate.getTime());
		}
		else
		{
			this.validFromDate = null;
		}
	}

	public QCDTO getQualityControl()
	{
		return qualityControl;
	}

	public void setQualityControl(QCDTO qualityControl)
	{
		if (qualityControl != null)
		{
			this.qualityControl = new QCDTO(qualityControl);
		}
		else
		{
			this.qualityControl = null;
		}
	}

	public ConsentDateValuesDTO getConsentDates()
	{
		return consentDates;
	}

	public void setConsentDates(ConsentDateValuesDTO consentDates)
	{
		if (consentDates != null)
		{
			this.consentDates = new ConsentDateValuesDTO(consentDates);
		}
		else
		{
			this.consentDates = new ConsentDateValuesDTO();
		}
	}

	public ExpirationPropertiesDTO getExpirationProperties()
	{
		return expirationProperties;
	}

	public void setExpirationProperties(ExpirationPropertiesDTO expirationProperties)
	{
		if (expirationProperties != null)
		{
			this.expirationProperties = new ExpirationPropertiesDTO(expirationProperties);
		}
		else
		{
			this.expirationProperties = new ExpirationPropertiesDTO();
		}
	}

	@Override
	public String getDomainName()
	{
		return getKey().getConsentTemplateKey().getDomainName();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (consentDates == null ? 0 : consentDates.hashCode());
		result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
		result = prime * result + (expirationProperties == null ? 0 : expirationProperties.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (hasPatientSignature ? 1231 : 1237);
		result = prime * result + (key == null ? 0 : key.hashCode());
		result = prime * result + (moduleStates == null ? 0 : moduleStates.hashCode());
		result = prime * result + (patientSignatureIsFromGuardian ? 1231 : 1237);
		result = prime * result + (patientSigningDate == null ? 0 : patientSigningDate.hashCode());
		result = prime * result + (patientSigningPlace == null ? 0 : patientSigningPlace.hashCode());
		result = prime * result + (physicianId == null ? 0 : physicianId.hashCode());
		result = prime * result + (physicianSigningDate == null ? 0 : physicianSigningDate.hashCode());
		result = prime * result + (physicianSigningPlace == null ? 0 : physicianSigningPlace.hashCode());
		result = prime * result + (qualityControl == null ? 0 : qualityControl.hashCode());
		result = prime * result + (templateType == null ? 0 : templateType.hashCode());
		result = prime * result + (updateDate == null ? 0 : updateDate.hashCode());
		result = prime * result + (validFromDate == null ? 0 : validFromDate.hashCode());
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
		ConsentLightDTO other = (ConsentLightDTO) obj;
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
		if (consentDates == null)
		{
			if (other.consentDates != null)
			{
				return false;
			}
		}
		else if (!consentDates.equals(other.consentDates))
		{
			return false;
		}
		if (creationDate == null)
		{
			if (other.creationDate != null)
			{
				return false;
			}
		}
		else if (!creationDate.equals(other.creationDate))
		{
			return false;
		}
		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
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
		if (hasPatientSignature != other.hasPatientSignature)
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
		if (moduleStates == null)
		{
			if (other.moduleStates != null)
			{
				return false;
			}
		}
		else if (!moduleStates.equals(other.moduleStates))
		{
			return false;
		}
		if (patientSignatureIsFromGuardian != other.patientSignatureIsFromGuardian)
		{
			return false;
		}
		if (patientSigningDate == null)
		{
			if (other.patientSigningDate != null)
			{
				return false;
			}
		}
		else if (!patientSigningDate.equals(other.patientSigningDate))
		{
			return false;
		}
		if (patientSigningPlace == null)
		{
			if (other.patientSigningPlace != null)
			{
				return false;
			}
		}
		else if (!patientSigningPlace.equals(other.patientSigningPlace))
		{
			return false;
		}
		if (physicianId == null)
		{
			if (other.physicianId != null)
			{
				return false;
			}
		}
		else if (!physicianId.equals(other.physicianId))
		{
			return false;
		}
		if (physicianSigningDate == null)
		{
			if (other.physicianSigningDate != null)
			{
				return false;
			}
		}
		else if (!physicianSigningDate.equals(other.physicianSigningDate))
		{
			return false;
		}
		if (physicianSigningPlace == null)
		{
			if (other.physicianSigningPlace != null)
			{
				return false;
			}
		}
		else if (!physicianSigningPlace.equals(other.physicianSigningPlace))
		{
			return false;
		}
		if (qualityControl == null)
		{
			if (other.qualityControl != null)
			{
				return false;
			}
		}
		else if (!qualityControl.equals(other.qualityControl))
		{
			return false;
		}
		if (templateType != other.templateType)
		{
			return false;
		}
		if (updateDate == null)
		{
			if (other.updateDate != null)
			{
				return false;
			}
		}
		else if (!updateDate.equals(other.updateDate))
		{
			return false;
		}
		if (validFromDate == null)
		{
			if (other.validFromDate != null)
			{
				return false;
			}
		}
		else if (!validFromDate.equals(other.validFromDate))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("', ");
		sb.append(moduleStates.size());
		sb.append(" module states, qc is ");
		sb.append(qualityControl != null ? qualityControl.isQcPassed() ? "" : "not" : "null");
		sb.append(" passed, ");
		sb.append(consentDates);
		sb.append(", validFromDate ");
		sb.append(validFromDate);
		sb.append(" and ");
		sb.append(expirationProperties);
		sb.append(" ");
		sb.append(super.toString());
		return sb.toString();
	}
}
