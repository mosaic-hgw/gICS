package org.emau.icmvc.ganimed.ttp.cm2.util;

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



import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;

public class StatisticKeys
{
	public static final String DOMAINS = "domains";
	public static final String TEMPLATES = "templates";
	public static final String MODULES = "modules_with_versions";
	public static final String MODULES_WITHOUT_VERSIONS = "modules_without_versions";
	public static final String POLICIES = "policies";
	public static final String DOCUMENTS = "documents";
	public static final String CONSENTS = "informed_consents";
	public static final String REVOCATIONS = "withdrawals";
	public static final String REFUSALS = "refusals";
	public static final String SIGNED_POLICIES = "signed_policies";
	public static final String SIGNER_IDS = "signer_ids";
	public static final String QC_VALID = "qc_valid";
	public static final String QC_INVALID = "qc_invalid";
	public static final String QC = "qc";
	public static final String PER_QC_TYPE = ".per_qctype.";
	public static final String PER_DOMAIN = ".per_domain.";
	public static final String PER_IDTYPE = ".per_idtype.";
	public static final String PER_POLICY = ".per_policy.";
	public static final String PER_STATUS = ".per_status.";
	public static final String PER_TEMPLATE = ".per_template.";
	public static final String CALCULATION_TIME = "calculation_time";
	public static final String DOCUMENTS_WITH_SCANS = "documents_with_scans";
	public static final String DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE = "documents_with_digital_patientsignature";
	public static final String DOCUMENTS_EXPIRED_FULL = "documents_expired_full";
	
	private String key;
	
	public StatisticKeys(String key)
	{
		this.key = key;
	}
	
	public StatisticKeys perDomain(String domainName)
	{
		key += PER_DOMAIN + domainName;
		return this;
	}

	public StatisticKeys perIdType(String idType)
	{
		key += PER_IDTYPE + idType;
		return this;
	}

	public StatisticKeys perPolicy(PolicyKeyDTO policy)
	{
		key += PER_POLICY + policy.getName() + policy.getVersion();
		return this;
	}

	public StatisticKeys perStatus(ConsentStatusType status)
	{
		key += PER_STATUS + status.name();
		return this;
	}
	
	public StatisticKeys perQcType(String qcType)
	{
		key += PER_QC_TYPE + qcType;
		return this;
	}

	public StatisticKeys perTemplate(ConsentTemplateKeyDTO templateKeyDTO)
	{
		key += PER_TEMPLATE + templateKeyDTO.getName() + templateKeyDTO.getVersion();
		return this;
	}
	
	public String build()
	{
		return key;
	}
}
