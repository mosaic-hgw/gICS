package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

/*-
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model that contains a consent and required dependencies (domain, template) to construct complete
 * maps for frontend usage.
 * 
 * @author Arne Blumentritt
 *
 */
public class WebConsent extends ConsentDTO
{
	private static final long serialVersionUID = 142259150623031876L;
	public Logger logger = LoggerFactory.getLogger(getClass());

	private LinkedHashMap<FreeTextDefDTO, Object> freeTexts;
	private LinkedHashMap<AssignedModuleDTO, ModuleStateDTO> modules;
	private ConsentTemplateDTO template;
	private DomainDTO domain;

	public WebConsent(ConsentKeyDTO key)
	{
		super(key);
	}

	public WebConsent(ConsentDTO dto)
	{
		super(dto, dto.getKey().getConsentDate());
	}

	/**
	 * Convert back to super class for usage in EJBs.
	 * 
	 * @return DTO
	 */
	public ConsentDTO toDTO()
	{
		ConsentDTO dto = new ConsentDTO(this.getKey());
		dto.setComment(this.getComment());
		dto.setExternProperties(this.getExternProperties());
		dto.setFreeTextVals(this.getFreeTextVals());
		dto.setModuleStates(this.getModuleStates());
		dto.setPatientSignatureBase64(this.getPatientSignatureBase64());
		dto.setPatientSignatureIsFromGuardian(this.getPatientSignatureIsFromGuardian());
		dto.setPatientSigningDate(this.getPatientSigningDate());
		dto.setPhysicanId(this.getPhysicanId());
		dto.setPhysicanSignatureBase64(this.getPhysicanSignatureBase64());
		dto.setPhysicanSigningDate(this.getPhysicanSigningDate());
		dto.setScanBase64(this.getScanBase64());
		dto.setScanFileType(this.getScanFileType());
		dto.setTemplateType(this.getTemplateType());

		return dto;
	}

	/**
	 * Get freeTextVals only, without freeTextDefs for usage in DTO.
	 */
	@Override
	public Map<String, String> getFreeTextVals()
	{
		HashMap<String, String> result = new HashMap<>();
		for (Entry<FreeTextDefDTO, Object> e : freeTexts.entrySet())
		{
			switch (e.getKey().getType())
			{
				case Date:
					SimpleDateFormat sdf = new SimpleDateFormat(e.getKey().getConverterString());
					try
					{
						result.put(e.getKey().getName(), e.getValue() == null ? null : sdf.format(e.getValue()));
					}
					catch (IllegalArgumentException e1)
					{
						logger.error("Cannot format date " + e.getValue().toString() + " into string");
					}
					break;
				default:
					result.put(e.getKey().getName(), e.getValue() == null ? null : e.getValue().toString());
					break;
			}
		}
		return result;
	}

	/**
	 * Get module states only, without template information (position, mandatory etc.) for usage in
	 * DTO.
	 */
	@Override
	public Map<ModuleKeyDTO, ModuleStateDTO> getModuleStates()
	{
		HashMap<ModuleKeyDTO, ModuleStateDTO> result = new HashMap<>();
		for (Entry<AssignedModuleDTO, ModuleStateDTO> e : modules.entrySet())
		{
			result.put(e.getKey().getModule().getKey(), e.getValue());
		}
		return result;
	}

	/**
	 * Combine Template and Consent information about free text values and definitions.
	 */
	private void initFreeTexts()
	{
		// Sort FreeTextDefs by position in template
		List<FreeTextDefDTO> defs = template.getFreeTextDefs();
		Collections.sort(defs, new Comparator<FreeTextDefDTO>() {
			@Override
			public int compare(FreeTextDefDTO d1, FreeTextDefDTO d2)
			{
				return Integer.valueOf(d1.getPos()).compareTo(Integer.valueOf(d2.getPos()));
			}
		});

		// Create Linked Map with sorted defs and values
		freeTexts = new LinkedHashMap<>();
		for (FreeTextDefDTO def : defs)
		{
			freeTexts.put(def, super.getFreeTextVals().get(def.getName()));
		}
	}

	/**
	 * Combine Template and Consent information about modules and module position, options etc.
	 */
	private void initModules()
	{
		// Sort Modules by position in template
		List<AssignedModuleDTO> mods = template.getAssignedModules();
		Collections.sort(mods, new Comparator<AssignedModuleDTO>() {
			@Override
			public int compare(AssignedModuleDTO m1, AssignedModuleDTO m2)
			{
				return Integer.valueOf(m1.getOrderNumber()).compareTo(Integer.valueOf(m2.getOrderNumber()));
			}
		});

		modules = new LinkedHashMap<>();
		for (AssignedModuleDTO mod : mods)
		{
			ModuleStateDTO state = super.getModuleStates().get(mod.getModule().getKey());
			if (state == null)
			{
				// set accepted if no default and no checkboxes
				ConsentStatus status = mod.getDisplayCheckboxes().isEmpty() && mod.getDefaultConsentStatus() == null ? ConsentStatus.ACCEPTED : mod.getDefaultConsentStatus();
				state = new ModuleStateDTO(mod.getModule().getKey(), status, new ArrayList<PolicyKeyDTO>());
			}
			modules.put(mod, state);
		}
	}

	/**
	 * Use domain information to prepare signerIds.
	 */
	private void initSignerIds()
	{
		for (String type : domain.getSignerIdTypes())
		{
			SignerIdDTO id = new SignerIdDTO();
			id.setIdType(type);
			getKey().getSignerIds().add(id);
		}
	}

	public LinkedHashMap<FreeTextDefDTO, Object> getFreeTexts()
	{
		return freeTexts;
	}

	public LinkedHashMap<AssignedModuleDTO, ModuleStateDTO> getModules()
	{
		return modules;
	}

	public ConsentTemplateDTO getTemplate()
	{
		return template;
	}

	/**
	 * Set template and init complex freeTexts and modules maps if empty.
	 * 
	 * @param template
	 */
	public void setTemplate(ConsentTemplateDTO template)
	{
		this.template = template;
		this.getKey().setConsentTemplateKey(template.getKey());
		this.setTemplateType(template.getType());

		//TODO warum waren die Bedinungen da drum? Die bereiten Probleme wenn das Template beim Ausfüllen gewechselt wird
//		if (freeTexts == null)
//		{
			initFreeTexts();
//		}

//		if (modules == null)
//		{
			initModules();
//		}
	}

	/**
	 * Set domain and init signerIds if empty
	 * 
	 * @param domain
	 */
	public void setDomain(DomainDTO domain)
	{
		this.domain = domain;

		if (getKey().getSignerIds().isEmpty())
		{
			initSignerIds();
		}
	}
}
