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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;

/**
 * @author hampfc
 */
@Entity
@Table(name = "consent_scan")
@Cache(isolation = CacheIsolationType.ISOLATED)
@UuidGenerator(name = "FHIR_ID_CS")
public class ConsentScan implements Serializable, FhirDTOExporter<ConsentScanDTO>
{
	private static final long serialVersionUID = -3633198230380118356L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "CONSENT_DATE", referencedColumnName = "CONSENT_DATE"),
			@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "VIRTUAL_PERSON_ID"),
			@JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "CT_DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "CT_NAME"),
			@JoinColumn(name = "CT_VERSION", referencedColumnName = "CT_VERSION") })
	private Consent consent;

	@Lob
	@Column(name = "SCANBASE64")
	private String scanBase64;

	@Column(name = "FILETYPE")
	private String fileType;

	@Id
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_CS")
	private String fhirID;

	@Column(name = "FILENAME")
	private String fileName;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UPLOAD_DATE", nullable = true)
	private Date uploadDate;

	public ConsentScan()
	{}

	public ConsentScan(Consent consent, String scanBase64, String fileType, String fileName) throws InvalidParameterException
	{
		super();

		if (StringUtils.isNotEmpty(scanBase64) && !isBase64(scanBase64))
		{
			throw new InvalidParameterException("The given string contains invalid characters. Expected base64 string.");
		}

		this.scanBase64 = scanBase64;
		this.fileType = fileType;
		this.consent = consent;
		this.fileName = fileName == null ? "" : fileName;
		this.uploadDate = new Date();
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public String getContent()
	{
		return scanBase64;
	}

	public String getFileType()
	{
		return fileType;
	}

	public String getFileName()
	{
		return fileName;
	}

	public Date getUploadDate()
	{
		return uploadDate;
	}

	public Consent getConsent()
	{
		return consent;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public void update(String scanBase64, String fileType, String fileName) throws InvalidParameterException
	{
		if (StringUtils.isNotEmpty(scanBase64) && !isBase64(scanBase64))
		{
			throw new InvalidParameterException("The given string contains invalid characters. Expected base64 string.");
		}

		this.scanBase64 = scanBase64;
		this.fileType = fileType;
		this.fileName = fileName == null ? "" : fileName;
		this.uploadDate = new Date();
	}

	@Override
	public ConsentScanDTO toDTO() throws InvalidVersionException, UnknownDomainException
	{
		return new ConsentScanDTO(fhirID, consent.getKey().toDTO(VersionConverterCache.getCTVersionConverter(consent.getConsentTemplate().getDomain().getName()), consent.getVirtualPerson()),
				scanBase64, fileType, fileName, uploadDate);
	}

	// https://stackoverflow.com/a/23955827
	private boolean isBase64(String value)
	{
		if (value == null || value.length() == 0 || value.length() % 4 != 0
				|| value.indexOf(' ') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\t') >= 0)
		{
			return false;
		}

		int index = value.length() - 1;
		if (value.endsWith("="))
		{
			index--;
		}

		if (value.endsWith("=="))
		{
			index--;
		}

		for (int i = 0; i <= index; i++)
		{
			if (isInvalidBase64Char(value.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}

	private static boolean isInvalidBase64Char(int value)
	{
		if (value >= 48 && value <= 57)
		{
			return false;
		}

		if (value >= 65 && value <= 90)
		{
			return false;
		}

		if (value >= 97 && value <= 122)
		{
			return false;
		}

		return value != 43 && value != 47;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (fhirID == null ? 0 : fhirID.hashCode());
		result = prime * result + (scanBase64 == null ? 0 : scanBase64.hashCode());
		result = prime * result + (fileType == null ? 0 : fileType.hashCode());
		result = prime * result + (consent == null ? 0 : consent.hashCode());
		result = prime * result + (fileName == null ? 0 : fileName.hashCode());
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

		if (obj == null)
		{
			return false;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}

		ConsentScan other = (ConsentScan) obj;
		if (fhirID == null)
		{
			if (other.fhirID != null)
			{
				return false;
			}
		}
		else if (!fhirID.equals(other.fhirID))
		{
			return false;
		}

		if (scanBase64 == null)
		{
			if (other.scanBase64 != null)
			{
				return false;
			}
		}
		else if (!scanBase64.equals(other.scanBase64))
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
		else
		{
			return fileType.equals(other.fileType);
		}

		if (consent == null)
		{
			if (other.consent != null)
			{
				return false;
			}
		}
		else
		{
			return consent.equals(other.consent);
		}

		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("consent scan (base64) for ").append(fhirID);
		if (fileName != null)
		{
			sb = sb.append(", FileName: " + fileName);
		}
		if (uploadDate != null)
		{
			sb = sb.append(", UploadDate: " + uploadDate);
		}
		return sb.toString();
	}
}
