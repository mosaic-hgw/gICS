package org.emau.icmvc.ganimed.ttp.cm2.dto.enums;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * wie wurde ein modul (und damit die enthaltenen policies) innerhalb eines consents konsentiert?
 *
 * @author geidell
 *
 */
public enum ConsentStatus
{
	// achtung! zusaetzliche werte unbedingt am ende einfuegen - wird in der db als ordinal
	// gespeichert
	ACCEPTED(ConsentStatusType.ACCEPTED), DECLINED(ConsentStatusType.DECLINED), UNKNOWN(ConsentStatusType.UNKNOWN), NOT_ASKED(ConsentStatusType.UNKNOWN), NOT_CHOSEN(ConsentStatusType.UNKNOWN,
			"NOT_CHOOSEN"), WITHDRAWN(ConsentStatusType.DECLINED, "REVOKED"), INVALIDATED(ConsentStatusType.DECLINED), REFUSED(ConsentStatusType.UNKNOWN), EXPIRED(ConsentStatusType.UNKNOWN);

	private final ConsentStatusType csType;
	private final String obsolete;

	ConsentStatus(ConsentStatusType csType)
	{
		this(csType, null);
	}

	ConsentStatus(ConsentStatusType csType, String obsolete)
	{
		this.csType = csType;
		this.obsolete = obsolete;
	}

	public ConsentStatusType getConsentStatusType()
	{
		return csType;
	}

	public String getObsolete()
	{
		return obsolete;
	}

	/**
	 * Returns the enum constant of this type with the specified name or an obsoleted form of a former name.
	 *
	 * @param name
	 *            the name or an obsoleted form of a former name
	 * @return enum constant with the specified name or its obsoleted form
	 * @see #valueOf(String)
	 *
	 * @throws IllegalArgumentException
	 *             – if this enum type has no constant with the specified name including obsoleted forms of a former names.
	 */
	public static ConsentStatus valueOfIncludingObsolete(String name)
	{
		try
		{
			return ConsentStatus.valueOf(name);
		}
		catch (IllegalArgumentException e)
		{
			ConsentStatus status = Arrays.stream(ConsentStatus.values()).filter(s -> name.equals(s.getObsolete())).findFirst().orElse(null);

			if (status == null)
			{
				throw e;
			}
			return status;
		}
	}

	public static List<ConsentStatus> getAllConsentStatusForType(ConsentStatusType csType)
	{
		List<ConsentStatus> result = new ArrayList<>();
		for (ConsentStatus cs : values())
		{
			if (cs.getConsentStatusType().equals(csType))
			{
				result.add(cs);
			}
		}
		return result;
	}

	public static String getStringForType(ConsentStatusType csType)
	{
		StringBuilder sb = new StringBuilder("[");
		List<ConsentStatus> csList = getAllConsentStatusForType(csType);
		if (!csList.isEmpty())
		{
			sb.append(csList.get(0).name());
			for (int i = 1; i < csList.size(); i++)
			{
				sb.append(", ");
				sb.append(csList.get(i).name());
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
