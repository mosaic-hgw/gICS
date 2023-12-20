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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.AssignedModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextDefDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextValDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.NoValueException;
import org.icmvc.ttp.web.model.WebSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model that contains a consent and required dependencies (domain, template) to construct complete
 * maps for frontend usage.
 *
 * @author Arne Blumentritt
 */
public class WebConsent extends ConsentDTO implements Serializable
{
	private static final long serialVersionUID = 142259150623031876L;
	private transient final Logger logger = LoggerFactory.getLogger(getClass());

	private List<WebFreeText> freeTexts;
	private LinkedHashMap<AssignedModuleDTO, ModuleStateDTO> modules;
	private ConsentTemplateDTO template;
	private DomainDTO domain;

	private WebSignature patientSignature = new WebSignature();
	private WebSignature physicianSignature = new WebSignature();

	private boolean printOptionQrCode;

	private List<QCHistoryDTO> qcHistory = new ArrayList<>();

	private String selectedScan;
	private boolean deleteConfirmation = false;

	public WebConsent(ConsentKeyDTO key)
	{
		super(key);
	}

	public WebConsent(ConsentDTO dto)
	{
		super(dto);
		patientSignature.setBase64(dto.getPatientSignatureBase64());
		physicianSignature.setBase64(dto.getPhysicianSignatureBase64());
	}

	/**
	 * Convert back to super class for usage in EJBs.
	 *
	 * @return DTO
	 */
	public ConsentDTO toDTO()
	{
		ConsentDTO dto = new ConsentDTO(getKey());
		dto.getKey().getSignerIds().removeIf(sid -> StringUtils.isEmpty(sid.getId()));
		dto.setComment(getComment());
		dto.setExternProperties(getExternProperties());
		dto.setFreeTextVals(getFreeTextVals());
		dto.setModuleStates(getModuleStates());
		dto.setPatientSignatureBase64(getPatientSignatureBase64());
		dto.setPatientSignatureIsFromGuardian(getPatientSignatureIsFromGuardian());
		dto.setPatientSigningDate(getPatientSigningDate());
		dto.setPatientSigningPlace(getPatientSigningPlace());
		dto.setPhysicianId(getPhysicianId());
		dto.setPhysicianSignatureBase64(getPhysicianSignatureBase64());
		dto.setPhysicianSigningDate(getPhysicianSigningDate());
		dto.setPhysicianSigningPlace(getPhysicianSigningPlace());
		dto.setScans(getScans());
		dto.setTemplateType(getTemplateType());

		return dto;
	}

	public void updateFromLightDTO(ConsentLightDTO consentLightDTO)
	{
		setQualityControl(consentLightDTO.getQualityControl());
	}

	/**
	 * Get freeTextVals only, without freeTextDefs for usage in DTO.
	 */
	@Override
	public List<FreeTextValDTO> getFreeTextVals()
	{
		return freeTexts.stream()
				.filter(t -> StringUtils.isNotEmpty(t.getValue()))
				.map(t -> t.toDTO())
				.collect(Collectors.toList());
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

	@Override
	public void setModuleStates(Map<ModuleKeyDTO, ModuleStateDTO> moduleStates)
	{
		super.setModuleStates(moduleStates);

		// if (template == null): called from constructor of ConsentLightDTO without template
		// https://git.icm.med.uni-greifswald.de/ths/gics-project/-/merge_requests/133#note_12941
		// https://git.icm.med.uni-greifswald.de/ths/gics-project/commit/67355917#7e14705ef717a70132d3e947389339b54c07d3b4_90_83

		if (template != null)
		{
			List<AssignedModuleDTO> mods = new ArrayList<>(List.copyOf(template.getAssignedModules()));
			sortAssignedModules(mods);
			updateLocalModules(mods, moduleStates);
		}

	}

	/**
	 * update local module list with current consent states
	 *
	 * @param mods
	 * 		list of assigned module for the template
	 * @param moduleStates
	 * 		current module states
	 */
	private void updateLocalModules(List<AssignedModuleDTO> mods, Map<ModuleKeyDTO, ModuleStateDTO> moduleStates)
	{
		// reset modules list
		modules = new LinkedHashMap<>();

		for (AssignedModuleDTO mod : mods)
		{
			if (moduleStates.containsKey(mod.getModule().getKey()))
			{
				modules.put(mod, moduleStates.get(mod.getModule().getKey()));
			}
			else
			{
				List<AssignedModuleDTO> tmpAssModList = new ArrayList<>();
				tmpAssModList.add(mod);
				setDefaultConsentStatesForModules(tmpAssModList);
			}
		}
	}

	/**
	 * Combine Template and Consent information about free text values and definitions.
	 */
	private void initFreeTexts()
	{
		// Sort FreeTextDefs by position in template
		List<FreeTextDefDTO> defs = new ArrayList<>(List.copyOf(template.getFreeTextDefs()));
		Collections.sort(defs, new Comparator<FreeTextDefDTO>() {
			@Override
			public int compare(FreeTextDefDTO d1, FreeTextDefDTO d2)
			{
				return Integer.valueOf(d1.getPos()).compareTo(Integer.valueOf(d2.getPos()));
			}
		});

		// Create Linked Map with sorted defs and values
		freeTexts = new ArrayList<>();
		for (FreeTextDefDTO def : defs)
		{
			try
			{
				freeTexts.add(new WebFreeText(def, super.getFreeTextValForDef(def.getName())));
			}
			catch (NoValueException ignore)
			{
				freeTexts.add(new WebFreeText(def, new FreeTextValDTO(def.getName(), "", "")));
			}
		}
	}

	/**
	 * Combine Template and Consent information about modules and module position, options etc.
	 */
	private void initModules()
	{
		// Sort Modules by position in template
		List<AssignedModuleDTO> mods = new ArrayList<>(List.copyOf(template.getAssignedModules()));
		sortAssignedModules(mods);

		modules = new LinkedHashMap<>();
		setDefaultConsentStatesForModules(mods);
	}

	private void setDefaultConsentStatesForModules(List<AssignedModuleDTO> assignedModules)
	{
		for (AssignedModuleDTO mod : assignedModules)
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
	 * directly sort the given list of assignedmodules
	 *
	 * @param modulesToBeSorted
	 */
	private void sortAssignedModules(List<AssignedModuleDTO> modulesToBeSorted)
	{
		Collections.sort(modulesToBeSorted, new Comparator<AssignedModuleDTO>()
		{
			@Override
			public int compare(AssignedModuleDTO m1, AssignedModuleDTO m2)
			{
				return Integer.valueOf(m1.getOrderNumber()).compareTo(Integer.valueOf(m2.getOrderNumber()));
			}
		});
	}

	/**
	 * Use domain information to prepare signerIds.
	 */
	private void initSignerIds()
	{
		int o = 1;
		for (String type : domain.getSignerIdTypes())
		{
			SignerIdDTO id = new SignerIdDTO();
			id.setIdType(type);
			id.setOrderNumber(o);
			getKey().getSignerIds().add(id);
			o++;
		}
	}

	public List<WebFreeText> getFreeTexts()
	{
		return freeTexts;
	}

	public LinkedHashMap<AssignedModuleDTO, ModuleStateDTO> getModules()
	{
		return modules;
	}

	public List<ConsentStatus> orderForType(List<ConsentStatus> list)
	{
		List<ConsentStatus> result = new ArrayList<>();

		switch (getTemplateType())
		{
			case CONSENT:
				result.addAll(list.stream().filter(cs -> ConsentStatusType.ACCEPTED.equals(cs.getConsentStatusType())).collect(Collectors.toList()));
				result.addAll(list.stream().filter(cs -> ConsentStatusType.DECLINED.equals(cs.getConsentStatusType())).collect(Collectors.toList()));
				result.addAll(list.stream().filter(cs -> ConsentStatusType.UNKNOWN.equals(cs.getConsentStatusType())).collect(Collectors.toList()));
				break;
			case REVOCATION:
			case REFUSAL:
				result.addAll(list.stream().filter(cs -> ConsentStatusType.DECLINED.equals(cs.getConsentStatusType())).collect(Collectors.toList()));
				result.addAll(list.stream().filter(cs -> ConsentStatusType.UNKNOWN.equals(cs.getConsentStatusType())).collect(Collectors.toList()));
				result.addAll(list.stream().filter(cs -> ConsentStatusType.ACCEPTED.equals(cs.getConsentStatusType())).collect(Collectors.toList()));
				break;
			default:
				logger.warn("Cannot order consentStatus for templateType {}", getTemplateType());
		}
		return result;
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
		getKey().setConsentTemplateKey(template.getKey());
		setTemplateType(template.getType());

		initFreeTexts();
		initModules();
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

	public String getSignerIdsAsString()
	{
		// if only one signerId, then only return value
		if (getKey().getSignerIds().size() == 1)
		{
			return getKey().getSignerIds().iterator().next().getId();
		}
		// if more than one signerIds, then return type and value
		else
		{
			return getKey().getOrderedSignerIds().stream()
					.map(s -> s.getIdType() + "=" + s.getId())
					.collect(Collectors.joining(", "));
		}

	}

	public WebSignature getPatientSignature()
	{
		return patientSignature;
	}

	public void setPatientSignature(WebSignature patientSignature)
	{
		this.patientSignature = patientSignature;
	}

	public WebSignature getPhysicianSignature()
	{
		return physicianSignature;
	}

	public void setPhysicianSignature(WebSignature physicianSignature)
	{
		this.physicianSignature = physicianSignature;
	}

	/**
	 * Return true if the full consent will expire in the future but is not expired yet
	 * @return
	 * @throws ParseException
	 */
	public Boolean getExpires() throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		return getConsentDates().getConsentExpirationDate().before(sdf.parse("01.01.2900")) && !getExpired();
	}

	/**
	 * Return true if the full consent is expired
	 * @return
	 */
	public Boolean getExpired()
	{
		return getConsentDates().getConsentExpirationDate().before(new Date());
	}

	/**
	 * Return true if only a part of the consent will expire but nothing is expired yet
	 * @return
	 * @throws ParseException
	 */
	public Boolean getPartlyExpires() throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		Date end = sdf.parse("01.01.2900");

		// Not expires fully
		if (getConsentDates().getConsentExpirationDate().after(end))
		{
			// Check if any module will expire
			for (Date expiration : getConsentDates().getModuleExpirations().values())
			{
				if (expiration.before(end))
				{
					return !getPartlyExpired();
				}
			}
		}

		return false;
	}

	/**
	 * Return true if only a part of the consent is expired
	 * @return
	 */
	public Boolean getPartlyExpired()
	{
		Date now = new Date();

		// Not full expired
		if (getConsentDates().getConsentExpirationDate().after(new Date()))
		{
			// Check if any module is expired
			for (Date expiration : getConsentDates().getModuleExpirations().values())
			{
				if (expiration.before(now))
				{
					return true;
				}
			}
		}

		return false;
	}

	public Boolean getModuleExpired(ModuleStateDTO module)
	{
		return getModuleExpiresBefore(module, new Date());
	}

	public Boolean getModuleExpires(ModuleStateDTO module) throws ParseException
	{
		return getModuleExpiresBefore(module, new SimpleDateFormat("dd.MM.yyyy").parse("01.01.2900"));
	}

	private Boolean getModuleExpiresBefore(ModuleStateDTO module, Date date)
	{
		Date expirationDate = getConsentDates().getModuleExpirations().get(module.getKey());
		return expirationDate != null && expirationDate.before(date);
	}

	public void prepareScanPreview()
	{
		if (!getScans().isEmpty())
		{
			prepareScanPreview(getScans().get(0).getFhirID());
		}
	}

	public void prepareScanPreview(String id)
	{
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		session.setAttribute("preview_pdf", getScanByFhirId(id).getBase64());
		session.setAttribute("preview_name", id);
	}
	
	public List<ConsentScanDTO> getScansSorted()
	{
		return getScans().stream().sorted(Comparator.comparing(ConsentScanDTO::getUploadDate)).collect(Collectors.toList());
	}

	public String getSelectedScan()
	{
		return selectedScan;
	}

	public void setSelectedScan(String selectedScan)
	{
		this.selectedScan = selectedScan;
	}

	public ConsentScanDTO getScanByFhirId(String id)
	{
		return getScans().stream().filter(s -> s.getFhirID().equals(id)).findFirst().orElse(null);
	}

	public List<QCHistoryDTO> getQcHistory()
	{
		return qcHistory.stream().sorted(Comparator.comparing(QCHistoryDTO::getStartDate).reversed()).collect(Collectors.toList());
	}

	public void setQcHistory(List<QCHistoryDTO> qcHistory)
	{
		this.qcHistory = qcHistory;
	}

	public boolean isPrintOptionQrCode()
	{
		return printOptionQrCode;
	}

	public void setPrintOptionQrCode(boolean printOptionQrCode)
	{
		this.printOptionQrCode = printOptionQrCode;
	}

	public boolean isDeleteConfirmation()
	{
		return deleteConfirmation;
	}

	public void setDeleteConfirmation(boolean deleteConfirmation)
	{
		this.deleteConfirmation = deleteConfirmation;
	}
}
