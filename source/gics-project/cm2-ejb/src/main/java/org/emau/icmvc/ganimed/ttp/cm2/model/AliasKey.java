package org.emau.icmvc.ganimed.ttp.cm2.model;

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
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * zusammengesetzter primaerschluessel fuer die n-m tabelle signer id <-> signer id (fuer merge von personen)
 *
 * @author geidell
 *
 */
@Embeddable
public class AliasKey implements Serializable
{
	private static final long serialVersionUID = -746409243164767401L;
	// siehe alias
	// @Column(insertable = false, updatable = false)
	// private SignerIdKey origSignerIdKey;
	// @Column(insertable = false, updatable = false)
	// private SignerIdKey aliasSignerIdKey;
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;

	@Transient
	private SignerIdKey origSignerIdKey;
	@Transient
	private SignerIdKey aliasSignerIdKey;
	@Column(name = "ORIG_SIT_DOMAIN_NAME", length = 50)
	private String origSITDomainName;
	@Column(name = "ORIG_SIT_NAME", length = 100)
	private String origSITName;
	@Column(name = "ORIG_SI_VALUE")
	private String origSIValue;
	@Column(name = "ALIAS_SIT_DOMAIN_NAME", length = 50)
	private String aliasSITDomainName;
	@Column(name = "ALIAS_SIT_NAME", length = 100)
	private String aliasSITName;
	@Column(name = "ALIAS_SI_VALUE")
	private String aliasSIValue;

	public AliasKey()
	{}

	public AliasKey(SignerIdKey origSignerIdKey, SignerIdKey aliasSignerIdKey)
	{
		super();
		this.origSignerIdKey = origSignerIdKey;
		this.aliasSignerIdKey = aliasSignerIdKey;
		this.createTimestamp = new Timestamp(System.currentTimeMillis());

		this.origSITDomainName = origSignerIdKey.getSignerIdTypeKey().getDomainName();
		this.origSITName = origSignerIdKey.getSignerIdTypeKey().getName();
		this.origSIValue = origSignerIdKey.getValue();
		this.aliasSITDomainName = aliasSignerIdKey.getSignerIdTypeKey().getDomainName();
		this.aliasSITName = aliasSignerIdKey.getSignerIdTypeKey().getName();
		this.aliasSIValue = aliasSignerIdKey.getValue();
	}

	public SignerIdKey getOrigSignerIdKey()
	{
		if (origSignerIdKey == null)
		{
			origSignerIdKey = new SignerIdKey(new SignerIdTypeKey(origSITDomainName, origSITName), origSIValue);
		}
		return origSignerIdKey;
	}

	public void setOrigSignerIdKey(SignerIdKey origSignerIdKey)
	{
		this.origSignerIdKey = origSignerIdKey;
		this.origSITDomainName = origSignerIdKey.getSignerIdTypeKey().getDomainName();
		this.origSITName = origSignerIdKey.getSignerIdTypeKey().getName();
		this.origSIValue = origSignerIdKey.getValue();
	}

	public SignerIdKey getAliasSignerIdKey()
	{
		if (aliasSignerIdKey == null)
		{
			aliasSignerIdKey = new SignerIdKey(new SignerIdTypeKey(aliasSITDomainName, aliasSITName), aliasSIValue);
		}
		return aliasSignerIdKey;
	}

	public void setAliasSignerIdKey(SignerIdKey aliasSignerIdKey)
	{
		this.aliasSignerIdKey = aliasSignerIdKey;
		this.aliasSITDomainName = aliasSignerIdKey.getSignerIdTypeKey().getDomainName();
		this.aliasSITName = aliasSignerIdKey.getSignerIdTypeKey().getName();
		this.aliasSIValue = aliasSignerIdKey.getValue();
	}

	public Timestamp getCreateTimestamp()
	{
		return createTimestamp;
	}

	public void setCreateTimestamp(Timestamp createTimestamp)
	{
		this.createTimestamp = createTimestamp;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (aliasSignerIdKey == null ? 0 : aliasSignerIdKey.hashCode());
		result = prime * result + (createTimestamp == null ? 0 : createTimestamp.hashCode());
		result = prime * result + (origSignerIdKey == null ? 0 : origSignerIdKey.hashCode());
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
		AliasKey other = (AliasKey) obj;
		if (aliasSignerIdKey == null)
		{
			if (other.aliasSignerIdKey != null)
			{
				return false;
			}
		}
		else if (!aliasSignerIdKey.equals(other.aliasSignerIdKey))
		{
			return false;
		}
		if (createTimestamp == null)
		{
			if (other.createTimestamp != null)
			{
				return false;
			}
		}
		else if (!createTimestamp.equals(other.createTimestamp))
		{
			return false;
		}
		if (origSignerIdKey == null)
		{
			if (other.origSignerIdKey != null)
			{
				return false;
			}
		}
		else if (!origSignerIdKey.equals(other.origSignerIdKey))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "AliasKey [origSignerIdKey=" + origSignerIdKey + ", aliasSignerIdKey=" + aliasSignerIdKey + ", createTimestamp=" + createTimestamp + "]";
	}
}
