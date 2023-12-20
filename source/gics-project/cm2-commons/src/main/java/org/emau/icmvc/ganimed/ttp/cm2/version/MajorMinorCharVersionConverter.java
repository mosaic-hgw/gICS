package org.emau.icmvc.ganimed.ttp.cm2.version;

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


import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;

/**
 * format: "x.y.z"; 0 <= x <= 2146; 0 <= y <= 999; 'a' <= z <= 'z'<br>
 * also valid:<br>
 * "x.y"; 0 <= x <= 2146; 0 <= y <= 999<br>
 * "x"; 0 <= x <= 2146<br>
 * attention: a.0b = a.b
 * 
 * @author geidell
 * 
 */
public class MajorMinorCharVersionConverter extends VersionConverter
{
	private static final String MSG = "invalid version format '%s' - it should be x.y.z were x is between 0 and 2146, y is between 0 and 999 and z is a char from 'a' to 'z'";

	@Override
	public String intToString(int version) throws InvalidVersionException
	{
		if (version < 0 || version > 2146999025)
		{
			throw new InvalidVersionException("version must be between 0 and 2146999025 - current value is " + version);
		}
		String result = version / 1000000 + DELIMITER + (version / 1000) % 1000 + DELIMITER;
		version %= 1000;
		if (version < 0 || version > 25)
		{
			throw new InvalidVersionException("last part of version must be between 0 and 25 - current value is " + version);
		}
		return result + (char) ('a' + version);
	}

	@Override
	public int stringToInt(String version) throws InvalidVersionException
	{
		if (version == null)
		{
			throw new InvalidVersionException(String.format(MSG, "null"));
		}
		String[] parts = version.split("\\.");
		if (parts.length != numberOfParts())
		{
			throw new InvalidVersionException(String.format(MSG, version));
		}
		int result;
		try
		{
			result = Integer.valueOf(parts[0]) * 1000000;
			if (result < 0 || result > 2146000000)
			{
				throw new InvalidVersionException(String.format(MSG, version));
			}
			if (parts.length > 1)
			{
				int y = Integer.valueOf(parts[1]);
				if (y < 0 || y > 999)
				{
					throw new InvalidVersionException(String.format(MSG, version));
				}
				result += y * 1000;
			}
			if (parts.length == 3)
			{
				if (parts[2].length() > 1)
				{
					throw new InvalidVersionException(String.format(MSG, version));
				}
				char z = parts[2].charAt(0);
				result += z - 'a';
			}
		}
		catch (NumberFormatException e)
		{
			throw new InvalidVersionException(String.format(MSG, version));
		}
		return result;
	}

	@Override
	public int numberOfParts()
	{
		return 3;
	}
}
