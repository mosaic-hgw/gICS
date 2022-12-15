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

import org.apache.commons.lang3.StringUtils;

public class ConsentScanDTO extends FhirIdDTO implements Serializable
{
	private static final long serialVersionUID = 5338090451626692717L;
	private ConsentKeyDTO consentKey;
	private String base64;
	private String fileType;
	private String fileName;
	private Date uploadDate;

	public ConsentScanDTO()
	{
		super(null);
	}

	public ConsentScanDTO(ConsentScanDTO dto)
	{
		super(dto.getFhirID());
		init(dto.getConsentKey(), dto.getBase64(), dto.getFileType(), dto.getFileName(), dto.getUploadDate());
	}

	public ConsentScanDTO(ConsentKeyDTO consentKey, String base64, String fileType, String fileName, Date uploadDate)
	{
		super(null);
		init(consentKey, base64, fileType, fileName, uploadDate);
	}

	public ConsentScanDTO(String fhirID, ConsentKeyDTO consentKey, String base64, String fileType, String fileName, Date uploadDate)
	{
		super(fhirID);
		init(consentKey, base64, fileType, fileName, uploadDate);
	}

	private void init(ConsentKeyDTO consentKey, String base64, String fileType, String fileName, Date uploadDate)
	{
		this.consentKey = consentKey;
		this.base64 = base64;
		this.fileType = fileType;
		this.fileName = fileName;
		this.uploadDate = uploadDate;
	}

	public ConsentKeyDTO getConsentKey()
	{
		return consentKey;
	}

	public void setConsentKey(ConsentKeyDTO consentKey)
	{
		this.consentKey = consentKey;
	}

	public String getBase64()
	{
		return base64;
	}

	public void setBase64(String base64)
	{
		this.base64 = base64;
	}

	public String getFileType()
	{
		return fileType;
	}

	public void setFileType(String fileType)
	{
		this.fileType = fileType;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public Date getUploadDate()
	{
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate)
	{
		this.uploadDate = uploadDate;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (base64 == null ? 0 : base64.hashCode());
		result = prime * result + (consentKey == null ? 0 : consentKey.hashCode());
		result = prime * result + (fileName == null ? 0 : fileName.hashCode());
		result = prime * result + (fileType == null ? 0 : fileType.hashCode());
		result = prime * result + (uploadDate == null ? 0 : uploadDate.hashCode());
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
		ConsentScanDTO other = (ConsentScanDTO) obj;
		if (base64 == null)
		{
			if (other.base64 != null)
			{
				return false;
			}
		}
		else if (!base64.equals(other.base64))
		{
			return false;
		}
		if (consentKey == null)
		{
			if (other.consentKey != null)
			{
				return false;
			}
		}
		else if (!consentKey.equals(other.consentKey))
		{
			return false;
		}
		if (fileName == null)
		{
			if (other.fileName != null)
			{
				return false;
			}
		}
		else if (!fileName.equals(other.fileName))
		{
			return false;
		}
		if (fileType == null)
		{
			if (other.fileType != null)
			{
				return false;
			}
		}
		else if (!fileType.equals(other.fileType))
		{
			return false;
		}
		if (uploadDate == null)
		{
			if (other.uploadDate != null)
			{
				return false;
			}
		}
		else if (!uploadDate.equals(other.uploadDate))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "consent scan with" +
				" fhirId='" + getFhirID() + "'" +
				" consentKey='" + consentKey + "'" +
				" base64='" + StringUtils.abbreviate(base64, 20) + "'" +
				" filetype='" + fileType + "'" +
				" fileName='" + fileName + "'" +
				" uploadDate='" + uploadDate + "'";
	}
}
