package org.emau.icmvc.ganimed.ttp.cm2.dto;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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
import java.util.ArrayList;
import java.util.List;

/**
 * ein modul ist eine zustimmbare unterteilung eines consents; sie fasst mehrere policies zusammen, denen gewoehnlicherweise gemeinsam zugestimmt wird
 * 
 * @author geidell
 * 
 */
public class ModuleDTO implements Serializable {

	private static final long serialVersionUID = 7810170655576763368L;
	private ModuleKeyDTO key;
	private String text;
	private String title;
	private String comment;
	private String externProperties;
	private List<PolicyDTO> policies = new ArrayList<PolicyDTO>();

	public ModuleDTO() {
	}

	public ModuleDTO(ModuleKeyDTO key) {
		super();
		this.key = key;
	}

	public ModuleDTO(ModuleKeyDTO key, String text, List<PolicyDTO> policies) {
		super();
		this.key = key;
		this.text = text;
		if (policies != null) {
			this.policies = policies;
		}
	}

	public ModuleDTO(ModuleKeyDTO key, String text, String title, String comment, String externProperties, List<PolicyDTO> policies) {
		super();
		this.key = key;
		this.text = text;
		this.title = title;
		this.comment = comment;
		this.externProperties = externProperties;
		if (policies != null) {
			this.policies = policies;
		}
	}

	public ModuleKeyDTO getKey() {
		return key;
	}

	public void setKey(ModuleKeyDTO key) {
		if (key != null) {
			this.key = key;
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getExternProperties() {
		return externProperties;
	}

	public void setExternProperties(String externProperties) {
		this.externProperties = externProperties;
	}

	public List<PolicyDTO> getPolicies() {
		return policies;
	}

	public void setPolicies(List<PolicyDTO> policies) {
		if (policies != null) {
			this.policies = policies;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((policies == null) ? 0 : policies.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuleDTO other = (ModuleDTO) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (externProperties == null) {
			if (other.externProperties != null)
				return false;
		} else if (!externProperties.equals(other.externProperties))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (policies == null) {
			if (other.policies != null)
				return false;
		} else if (!policies.equals(other.policies))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return key.toString() + " with " + ((policies == null) ? 0 : policies.size()) + " policies";
	}
}
