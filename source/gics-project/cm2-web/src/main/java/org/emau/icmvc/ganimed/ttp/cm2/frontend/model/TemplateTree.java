package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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
import java.util.Comparator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedPolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.icmvc.ttp.web.controller.AbstractBean;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class TemplateTree extends AbstractBean implements Serializable
{
	private static final long serialVersionUID = 8216994479288745258L;

	private transient TreeNode tree;

	private Boolean showExternProperties = true;
	private Boolean showExpiration = true;
	private Boolean showDraft = true;
	private Boolean showComment = false;

	public TemplateTree(ConsentTemplateDTO template)
	{
		// Template
		tree = new DefaultTreeNode(new TemplateTreeNode(template.getKey(), template.getLabel(), template.getVersionLabelAndVersion(), template.getFinalised(), template.getExternProperties(),
				template.getExpirationProperties(), template.getComment()), null);
		tree.setExpanded(true);

		// Modules
		for (AssignedModuleDTO module : template.getAssignedModules().stream().sorted(Comparator.comparing(AssignedModuleDTO::getOrderNumber)).collect(Collectors.toList()))
		{
			TreeNode moduleNode = new DefaultTreeNode(
					new TemplateTreeNode(module.getModule().getKey(), module.getModule().getLabel(), module.getModule().getFinalised(),
							(StringUtils.isNotEmpty(module.getExternProperties()) ? module.getExternProperties() + " " : "")
									+ (StringUtils.isNotEmpty(module.getModule().getExternProperties()) ? module.getModule().getExternProperties() : ""), module.getExpirationProperties(),
							module.getMandatory(), module.getModule().getComment()),
					tree);
			moduleNode.setExpanded(true);

			// Policies
			for (AssignedPolicyDTO policy : module.getModule().getAssignedPolicies().stream().sorted().collect(Collectors.toList()))
			{
				TreeNode policyNode = new DefaultTreeNode(
						new TemplateTreeNode(policy.getPolicy().getKey(), policy.getPolicy().getLabel(), policy.getPolicy().getFinalised(),
								(StringUtils.isNotEmpty(policy.getExternProperties()) ? policy.getExternProperties() + " " : "") + policy.getPolicy().getExternProperties(),
								policy.getExpirationProperties(), policy.getPolicy().getComment()),
						moduleNode);
				moduleNode.getChildren().add(policyNode);
			}

			tree.getChildren().add(moduleNode);
		}
	}

	public TreeNode getTree()
	{
		return tree;
	}

	public Boolean getShowExternProperties()
	{
		return showExternProperties;
	}

	public void setShowExternProperties(Boolean showExternProperties)
	{
		this.showExternProperties = showExternProperties;
	}

	public Boolean getShowExpiration()
	{
		return showExpiration;
	}

	public void setShowExpiration(Boolean showExpiration)
	{
		this.showExpiration = showExpiration;
	}

	public Boolean getShowDraft()
	{
		return showDraft;
	}

	public void setShowDraft(Boolean showDraft)
	{
		this.showDraft = showDraft;
	}

	public Boolean getShowComment()
	{
		return showComment;
	}

	public void setShowComment(Boolean showComment)
	{
		this.showComment = showComment;
	}
}
