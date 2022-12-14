package org.emau.icmvc.ganimed.ttp.cm2.config;

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

import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;

/**
 * possible properties for a consent domain
 *
 * @author geidell
 *
 */
public enum DomainProperties
{
	/**
	 * If there are more than one signed policies for a policy, instead the most recent, the one with the highest version number is considered the current one.<br/>
	 * default = false
	 */
	TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST,
	/**
	 * If set to true, a single signed policy with the state "declined" voids all signed policies for that policy (even if they are newer).<br/>
	 * default = false
	 */
	REVOKE_IS_PERMANENT,
	/**
	 * By default, if at least one policy is accepted within a consent document, a scan of either patient and physician signature or of the whole document is required. Set this property to true to
	 * skip this validation check.<br/>
	 * default = false
	 */
	SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS,
	/**
	 * Size limit for scans in bytes.<br/>
	 * default = 10485760 (10 MB)
	 */
	SCANS_SIZE_LIMIT,
	/**
	 * Should the shortest possible period of validity be used or the most specific one (period of validity of domain < the one of consent template < module < policy)<br/>
	 * A validity date at the consent (if one exists) will always be the upper limit for the period of validity.<br/>
	 * default = false
	 */
	TAKE_MOST_SPECIFIC_PERIOD_OF_VALIDITY_INSTEAD_OF_SHORTEST,
	/**
	 * comma separated list of valid qc types, {@link QCDTO.AUTO_GENERATED} is always part of this list<br/>
	 * default = list containing {@link QCDTO.AUTO_GENERATED}
	 */
	VALID_QC_TYPES,
	/**
	 * comma separated list of invalid qc types<br/>
	 * default = empty list
	 */
	INVALID_QC_TYPES,
	/**
	 * one of the above types<br/>
	 * default = {@link QCDTO.AUTO_GENERATED}
	 */
	DEFAULT_QC_TYPE,
	/**
	 * Use notifications when adding consents or changing QC from Web interface.<br/>
	 * default = false
	 */
	SEND_NOTIFICATIONS_WEB,
	/**
	 * Calculate the document details (isExpired, hasDigitalSignature) which requires iterating over all ConsentLightDTOs.<br/>
	 * default = true
	 */
	STATISTIC_DOCUMENT_DETAILS,
	/**
	 * Calculate the policy details (how many signed policies for each policy and domain) which requires fetching the policy status for all signers<br/>
	 * default = true
	 */
	STATISTIC_POLICY_DETAILS
}
