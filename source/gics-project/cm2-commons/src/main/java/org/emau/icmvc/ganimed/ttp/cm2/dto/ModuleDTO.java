package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * ein modul ist eine zustimmbare unterteilung eines consents; sie fasst mehrere policies zusammen,
 * denen gewoehnlicherweise gemeinsam zugestimmt wird
 *
 * @author geidell
 *
 */
public class ModuleDTO extends FhirIdDTO implements Serializable, DomainRelated
{
	@Serial
	private static final long serialVersionUID = -5350386498803973205L;
	private ModuleKeyDTO key;
	private String text;
	private String title;
	private String comment;
	private String externProperties;
	private final Set<AssignedPolicyDTO> assignedPolicies = new HashSet<>();
	private Date creationDate;
	private Date updateDate;
	private String label;
	private String shortText;
	private boolean finalised = false;

	public ModuleDTO()
	{
		super(null);
	}

	public ModuleDTO(ModuleKeyDTO key)
	{
		super(null);
		setKey(key);
	}

	public ModuleDTO(ModuleKeyDTO key, String text, Set<AssignedPolicyDTO> assignedPolicies)
	{
		super(null);
		setKey(key);
		this.text = text;
		setAssignedPolicies(assignedPolicies);
	}

	public ModuleDTO(ModuleKeyDTO key, String text, String title, String comment, String externProperties, Set<AssignedPolicyDTO> assignedPolicies, String label, String shortText,
			boolean finalised, Date creationDate, Date updateDate, String fhirID)
	{
		super(fhirID);
		setKey(key);
		this.text = text;
		this.title = title;
		this.comment = comment;
		this.externProperties = externProperties;
		setAssignedPolicies(assignedPolicies);
		this.label = label;
		this.shortText = shortText;
		this.finalised = finalised;
		setCreationDate(creationDate);
		setUpdateDate(updateDate);
	}

	public ModuleDTO(ModuleDTO dto)
	{
		this(dto.getKey(), dto.getText(), dto.getTitle(), dto.getComment(), dto.getExternProperties(), dto.getAssignedPolicies(), dto.getLabel(), dto.getShortText(),
				dto.getFinalised(), dto.getCreationDate(), dto.getUpdateDate(), dto.getFhirID());
	}

	public ModuleKeyDTO getKey()
	{
		return key;
	}

	public void setKey(ModuleKeyDTO key)
	{
		if (key != null)
		{
			this.key = new ModuleKeyDTO(key);
		}
		else
		{
			this.key = null;
		}
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public void setExternProperties(String externProperties)
	{
		this.externProperties = externProperties;
	}

	public Set<AssignedPolicyDTO> getAssignedPolicies()
	{
		return assignedPolicies;
	}

	public void setAssignedPolicies(Set<AssignedPolicyDTO> assignedPolicies)
	{
		if (this.assignedPolicies != assignedPolicies)
		{
			this.assignedPolicies.clear();
			if (assignedPolicies != null)
			{
				for (AssignedPolicyDTO assignedPolicy : assignedPolicies)
				{
					this.assignedPolicies.add(new AssignedPolicyDTO(assignedPolicy));
				}
			}
		}
	}

	public Date getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(Date creationDate)
	{
		if (creationDate != null)
		{
			this.creationDate = new Date(creationDate.getTime());
		}
		else
		{
			this.creationDate = null;
		}
	}

	public Date getUpdateDate()
	{
		return updateDate;
	}

	public void setUpdateDate(Date updateDate)
	{
		if (updateDate != null)
		{
			this.updateDate = new Date(updateDate.getTime());
		}
		else
		{
			this.updateDate = null;
		}
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getLabelOrName()
	{
		return StringUtils.isNotEmpty(label) ? label : key.getName();
	}

	public String getShortText()
	{
		return shortText;
	}

	public void setShortText(String shortText)
	{
		this.shortText = shortText;
	}

	public boolean getFinalised()
	{
		return finalised;
	}

	public void setFinalised(boolean finalised)
	{
		this.finalised = finalised;
	}

	@Override
	public String getDomainName()
	{
		return getKey().getDomainName();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
		result = prime * result + (externProperties == null ? 0 : externProperties.hashCode());
		result = prime * result + (finalised ? 1231 : 1237);
		result = prime * result + (key == null ? 0 : key.hashCode());
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (assignedPolicies == null ? 0 : assignedPolicies.hashCode());
		result = prime * result + (shortText == null ? 0 : shortText.hashCode());
		result = prime * result + (text == null ? 0 : text.hashCode());
		result = prime * result + (title == null ? 0 : title.hashCode());
		result = prime * result + (updateDate == null ? 0 : updateDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ModuleDTO other = (ModuleDTO) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (creationDate == null)
		{
			if (other.creationDate != null)
			{
				return false;
			}
		}
		else if (!creationDate.equals(other.creationDate))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (finalised != other.finalised)
		{
			return false;
		}
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		if (label == null)
		{
			if (other.label != null)
			{
				return false;
			}
		}
		else if (!label.equals(other.label))
		{
			return false;
		}
		if (assignedPolicies == null)
		{
			if (other.assignedPolicies != null)
			{
				return false;
			}
		}
		else if (!assignedPolicies.equals(other.assignedPolicies))
		{
			return false;
		}
		if (shortText == null)
		{
			if (other.shortText != null)
			{
				return false;
			}
		}
		else if (!shortText.equals(other.shortText))
		{
			return false;
		}
		if (text == null)
		{
			if (other.text != null)
			{
				return false;
			}
		}
		else if (!text.equals(other.text))
		{
			return false;
		}
		if (title == null)
		{
			if (other.title != null)
			{
				return false;
			}
		}
		else if (!title.equals(other.title))
		{
			return false;
		}
		if (updateDate == null)
		{
			if (other.updateDate != null)
			{
				return false;
			}
		}
		else if (!updateDate.equals(other.updateDate))
		{
			return false;
		}
		return true;
	}

	public boolean equalsForFhirSerice(Object obj)
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
		ModuleDTO other = (ModuleDTO) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}

		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (finalised != other.finalised)
		{
			return false;
		}
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		if (label == null)
		{
			if (other.label != null)
			{
				return false;
			}
		}
		else if (!label.equals(other.label))
		{
			return false;
		}
		if (shortText == null)
		{
			if (other.shortText != null)
			{
				return false;
			}
		}
		else if (!shortText.equals(other.shortText))
		{
			return false;
		}

		if (assignedPolicies == null || assignedPolicies.isEmpty())
		{
			if (other.assignedPolicies != null && !other.assignedPolicies.isEmpty())
			{
				return false;
			}
		}
		else
		{
			if (other.assignedPolicies == null || other.assignedPolicies.isEmpty())
			{
				return false;
			}
			// only based on policykeys
			if (assignedPolicies.size() != other.assignedPolicies.size())
			{
				return false;
			}

			Map<PolicyKeyDTO, AssignedPolicyDTO> otherAssignedPolicyDTOs = new HashMap<>();
			for (AssignedPolicyDTO assignedPolicyDTO : other.assignedPolicies)
			{
				otherAssignedPolicyDTOs.put(assignedPolicyDTO.getPolicy().getKey(), assignedPolicyDTO);
			}

			for (AssignedPolicyDTO assignedPolicyDTO : assignedPolicies)
			{
				AssignedPolicyDTO otherAssignedPolicyDTO = otherAssignedPolicyDTOs.get(assignedPolicyDTO.getPolicy().getKey());
				if (!assignedPolicyDTO.equalsForFhirSerice(otherAssignedPolicyDTO))
				{
					return false;
				}
			}
		}

		if (text == null)
		{
			if (other.text != null)
			{
				return false;
			}
		}
		else if (!text.equals(other.text))
		{
			return false;
		}
		if (title == null)
		{
			if (other.title != null)
			{
				return false;
			}
		}
		else if (!title.equals(other.title))
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(key.toString());
		sb.append(", label '");
		sb.append(label);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("' and ");
		sb.append(assignedPolicies.size());
		sb.append(" policies. created at ");
		sb.append(creationDate);
		sb.append(" last update at ");
		sb.append(updateDate);
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(super.toString());
		return sb.toString();
	}

	public String toLongString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(key.toString());
		sb.append(", label '");
		sb.append(label);
		sb.append(", shortText '");
		sb.append(shortText);
		sb.append("', comment '");
		sb.append(comment);
		sb.append("', extern properties '");
		sb.append(externProperties);
		sb.append("' and ");
		sb.append(assignedPolicies.size());
		sb.append(" policies. created at ");
		sb.append(creationDate);
		sb.append(" last update at ");
		sb.append(updateDate);
		sb.append(" is finalised ");
		sb.append(finalised);
		sb.append(super.toString());
		return sb.toString();
	}
}
