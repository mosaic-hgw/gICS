package org.emau.icmvc.ganimed.ttp.cm2.version;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
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
 * format: "x.y"; 0 <= x <= 999; 0 <= y <= 999<br>
 * also valid: "x"; 0 <= x <= 999<br>
 * attention: a.0b = a.b
 * 
 * @author geidell
 * 
 */
public class MajorMinorVersionConverter extends VersionConverter {

	private static final String DELIMITER = ".";

	@Override
	public String intToString(int version) throws InvalidVersionException {
		if (version < 0 || version > 999999) {
			throw new InvalidVersionException("version must be between 0 and 999.999 - current value is " + version);
		}
		return version / 1000 + DELIMITER + version % 1000;
	}

	@Override
	public int stringToInt(String version) throws InvalidVersionException {
		if(version == null) {
			throw new InvalidVersionException("invalid version format '" + version + "' - it should be x.y were 0 <= x <=999 and 0 <= y <= 999");
		}
		String[] parts = version.split("\\.");
		if (parts.length == 0 || parts.length > 2) {
			throw new InvalidVersionException("invalid version format '" + version + "' - it should be x.y were 0 <= x <=999 and 0 <= y <= 999");
		}
		int result;
		try {
			result = Integer.valueOf(parts[0]) * 1000;
			if (result < 0 || result > 999000) {
				throw new InvalidVersionException("invalid version format '" + version + "' - it should be x.y were 0 <= x <=999 and 0 <= y <= 999");
			}
			if (parts.length == 2) {
				int y = Integer.valueOf(parts[1]);
				if (y < 0 || y > 999) {
					throw new InvalidVersionException("invalid version format '" + version
							+ "' - it should be x.y were 0 <= x <=999 and 0 <= y <= 999");
				}
				result += y;
			}
		} catch (NumberFormatException e) {
			throw new InvalidVersionException("invalid version format '" + version + "' - it should be x.y were 0 <= x <=999 and 0 <= y <= 999");
		}
		return result;
	}
}
