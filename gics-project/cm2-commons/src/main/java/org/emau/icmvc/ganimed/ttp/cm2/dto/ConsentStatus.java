package org.emau.icmvc.ganimed.ttp.cm2.dto;

import java.util.ArrayList;
import java.util.List;

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

/**
 * wie wurde ein modul (und damit die enthaltenen policies) innerhalb eines consents konsentiert?
 * 
 * @author geidell
 * 
 */
public enum ConsentStatus {
	// achtung! zusaetzliche werte unbedingt am ende einfuegen - wird in der db als ordinal gespeichert
	ACCEPTED(ConsentStatusType.ACCEPTED), DECLINED(ConsentStatusType.DECLINED), UNKNOWN(ConsentStatusType.UNKNOWN), NOT_ASKED(
			ConsentStatusType.UNKNOWN), NOT_CHOOSEN(ConsentStatusType.UNKNOWN), REVOKED(ConsentStatusType.DECLINED), INVALIDATED(
			ConsentStatusType.DECLINED);

	private final ConsentStatusType csType;

	private ConsentStatus(ConsentStatusType csType) {
		this.csType = csType;
	}

	public ConsentStatusType getConsentStatusType() {
		return csType;
	}

	public static List<ConsentStatus> getAllConsentStatusForType(ConsentStatusType csType) {
		List<ConsentStatus> result = new ArrayList<ConsentStatus>();
		for (ConsentStatus cs : values()) {
			if (cs.getConsentStatusType().equals(csType)) {
				result.add(cs);
			}
		}
		return result;
	}

	public static String getStringForType(ConsentStatusType csType) {
		StringBuilder sb = new StringBuilder("[");
		List<ConsentStatus> csList = getAllConsentStatusForType(csType);
		if (!csList.isEmpty()) {
			sb.append(csList.get(0).name());
			for (int i = 1; i < csList.size(); i++) {
				sb.append(", ");
				sb.append(csList.get(i).name());
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
