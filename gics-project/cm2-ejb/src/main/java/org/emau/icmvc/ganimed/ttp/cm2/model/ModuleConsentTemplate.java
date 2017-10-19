package org.emau.icmvc.ganimed.ttp.cm2.model;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

/**
 * objekt fuer die m-n tabelle consent template <-> module
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "module_consent_template")
@Cache(isolation = CacheIsolationType.PROTECTED)
public class ModuleConsentTemplate implements Serializable, Comparable<ModuleConsentTemplate> {

	private static final long serialVersionUID = -8252447528697896589L;
	@EmbeddedId
	private ModuleConsentTemplateKey key;
	private boolean mandatory;
	/**
	 * die liste aus AssignedModuleDTO als bitfeld
	 */
	private long displayCheckboxes;
	@Enumerated
	private ConsentStatus defaultConsentStatus;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "M_DOMAIN", referencedColumnName = "DOMAIN_NAME"), @JoinColumn(name = "M_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "M_VERSION", referencedColumnName = "VERSION") })
	@MapsId("moduleKey")
	private Module module;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "VERSION") })
	@MapsId("consentTemplateKey")
	private ConsentTemplate consentTemplate;
	@Column(name = "ORDER_NUMBER")
	private Integer orderNumber;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumns({ @JoinColumn(name = "PARENT_M_DOMAIN", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "PARENT_M_NAME", referencedColumnName = "NAME"),
			@JoinColumn(name = "PARENT_M_VERSION", referencedColumnName = "VERSION") })
	private Module parent;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;

	public ModuleConsentTemplate() {
	}

	public ModuleConsentTemplate(ConsentTemplate consentTemplate, Module module, AssignedModuleDTO assignedModuleDTO, Module parent) {
		super();
		this.module = module;
		this.consentTemplate = consentTemplate;
		this.mandatory = assignedModuleDTO.getMandatory();
		this.displayCheckboxes = listToLong(assignedModuleDTO.getDisplayCheckboxes());
		this.defaultConsentStatus = assignedModuleDTO.getDefaultConsentStatus();
		this.orderNumber = assignedModuleDTO.getOrderNumber();
		this.parent = parent;
		this.comment = assignedModuleDTO.getComment();
		this.externProperties = assignedModuleDTO.getExternProperties();
	}

	public ModuleConsentTemplateKey getKey() {
		return key;
	}

	public boolean getMandatory() {
		return mandatory;
	}

	public long getDisplayCheckboxes() {
		return displayCheckboxes;
	}

	public List<ConsentStatus> getDisplayCheckboxesList() {
		List<ConsentStatus> result = new ArrayList<ConsentStatus>();
		long temp = displayCheckboxes;
		int i = 0;
		ConsentStatus[] allStatus = ConsentStatus.values();
		// i == allStatus.length muesste eigentlich eine exception werfen
		while (temp > 0 && i < allStatus.length) {
			if ((temp & 1l) == 1l) {
				result.add(allStatus[i]);
			}
			temp >>= 1;
			i++;
		}
		return result;
	}

	public ConsentStatus getDefaultConsentStatus() {
		return defaultConsentStatus;
	}

	public Module getModule() {
		return module;
	}

	public ConsentTemplate getConsentTemplate() {
		return consentTemplate;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public Module getParent() {
		return parent;
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

	private long listToLong(List<ConsentStatus> list) {
		long result = 0;
		for (ConsentStatus status : list) {
			result += 1 << status.ordinal();
		}
		return result;
	}

	public AssignedModuleDTO toAssignedModuleDTO() throws VersionConverterClassException, InvalidVersionException {
		ModuleKeyDTO parentKeyDTO = null;
		if (parent != null) {
			parentKeyDTO = parent.getKey().toDTO(module.getDomain().getModuleVersionConverterInstance());
		}
		return new AssignedModuleDTO(module.toDTO(), mandatory, defaultConsentStatus, getDisplayCheckboxesList(), orderNumber, parentKeyDTO, comment,
				externProperties);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((defaultConsentStatus == null) ? 0 : defaultConsentStatus.hashCode());
		result = prime * result + (int) (displayCheckboxes ^ (displayCheckboxes >>> 32));
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + (mandatory ? 1231 : 1237);
		result = prime * result + ((orderNumber == null) ? 0 : orderNumber.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		ModuleConsentTemplate other = (ModuleConsentTemplate) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (defaultConsentStatus != other.defaultConsentStatus)
			return false;
		if (displayCheckboxes != other.displayCheckboxes)
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
		if (mandatory != other.mandatory)
			return false;
		if (orderNumber == null) {
			if (other.orderNumber != null)
				return false;
		} else if (!orderNumber.equals(other.orderNumber))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	@Override
	public int compareTo(ModuleConsentTemplate o) {
		if (orderNumber < o.getOrderNumber())
			return -1;
		else if (orderNumber.equals(o.getOrderNumber()))
			return 0;
		else
			return 1;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(key);
		sb.append(", default value: '");
		sb.append((defaultConsentStatus == null) ? "null" : defaultConsentStatus.toString());
		sb.append("' is mandatory: ");
		sb.append(mandatory);
		sb.append(", has order number ");
		sb.append(orderNumber);
		sb.append(", as parent: '");
		sb.append(parent);
		sb.append("', should show checkboxes for ");
		List<ConsentStatus> checkBoxList = getDisplayCheckboxesList();
		for (ConsentStatus consentStatus : checkBoxList) {
			sb.append(" ");
			sb.append(consentStatus.toString());
		}
		sb.append("', has as comment '");
		sb.append(comment);
		sb.append("' and externProperties '");
		sb.append(externProperties);
		return sb.toString();
	}
}
