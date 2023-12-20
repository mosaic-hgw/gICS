package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.icmvc.ttp.web.model.WebFile;
import org.primefaces.event.FileUploadEvent;

public class PolicyStatusWebFile extends WebFile
{
	@Serial
	private static final long serialVersionUID = -8896794849255492785L;

	private static final String NORMALIZE_PATTERN = "\\W|_";
	private static final String VERSION_PATTERN = "([vV\\s])(\\d\\S*)\\s*$";

	// Stores the policy for each user uploaded column
	private final Map<String, PolicyDTO> columnPolicyMapping = new HashMap<>();

	private final List<PolicyDTO> availablePolicies;

	public PolicyStatusWebFile(String tool, List<PolicyDTO> availablePolicies)
	{
		super(tool);
		this.availablePolicies = availablePolicies;
		this.availablePolicies.sort(Comparator.comparing(p -> p.getKey().getVersion()));
	}

	@Override
	public void onUpload(FileUploadEvent event)
	{
		super.onUpload(event);
		generateColumnPolicyMapping();
	}

	private void generateColumnPolicyMapping()
	{
		columnPolicyMapping.clear();
		Pattern versionPattern = Pattern.compile(VERSION_PATTERN);

		for (String column : getColumns())
		{
			String version = null;
			String normalizedColumnName = column;

			// Find version
			Matcher matcher = versionPattern.matcher(normalizedColumnName);
			if (matcher.find())
			{
				String v = matcher.group(1);
				version = matcher.group(2);
				// Remove version from column
				normalizedColumnName = normalizedColumnName.replace(v + version, "");
			}

			// Normalize column name
			normalizedColumnName = normalizedColumnName.replaceAll(NORMALIZE_PATTERN, "");


			// Copy to final string for working with streams
			String finalVersion = version;
			String finalNormalizedColumnName = normalizedColumnName;

			// Try to detect by matching the policy name
			List<PolicyDTO> policiesMatchingNameOrLabel = availablePolicies.stream()
					.filter(p -> p.getKey().getName().replaceAll(NORMALIZE_PATTERN, "").equalsIgnoreCase(finalNormalizedColumnName)).toList();

			// Try to detect by matching the policy label
			if (policiesMatchingNameOrLabel.isEmpty())
			{
				policiesMatchingNameOrLabel = availablePolicies.stream().filter(p -> p.getLabel() != null).filter(p -> p.getLabel().replaceAll(NORMALIZE_PATTERN, "").equalsIgnoreCase(finalNormalizedColumnName)).toList();
			}

			PolicyDTO policy;
			if (!policiesMatchingNameOrLabel.isEmpty())
			{
				// Try to detect version
				policy = policiesMatchingNameOrLabel.stream().filter(p -> p.getKey().getVersion().equals(finalVersion)).findAny().orElse(null);

				// Use latest version if no version defined in upload
				if (policy == null)
				{
					policy = policiesMatchingNameOrLabel.get(policiesMatchingNameOrLabel.size() - 1);
				}

				columnPolicyMapping.put(column, policy);
			}
		}
	}

	public void removeDetectedPolicyColumns()
	{
		getColumns().removeIf(columnPolicyMapping::containsKey);
	}

	public Map<String, PolicyDTO> getColumnPolicyMapping()
	{
		return columnPolicyMapping;
	}

	public List<PolicyDTO> getAvailablePolicies()
	{
		return availablePolicies;
	}
}
