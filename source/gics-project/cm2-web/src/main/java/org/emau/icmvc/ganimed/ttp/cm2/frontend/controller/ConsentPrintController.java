package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

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

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.PrintPrefillEntry;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.util.SessionMapKeys;
import org.icmvc.ttp.web.model.WebFile;

/**
 * Backing Bean for Consent Print View
 *
 * @author Arne Blumentritt
 */
@ManagedBean(name = "consentPrintController")
@ViewScoped
public class ConsentPrintController extends AbstractConsentController
{
	// TODO add download for example csv in frontend

	private final List<PrintPrefillEntry> printPrefillEntries = new ArrayList<>();
	private ConsentTemplateDTO template;

	private PrintPrefillEntry selectedPrefillEntry;
	private PrintPrefillEntry editPrefillEntry;
	private Integer editIndex;

	// File
	private WebFile webFile;

	@PostConstruct
	protected void init()
	{
		webFile = new WebFile("gICS");
		onNewUpload();

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		if (sessionMap.containsKey(SessionMapKeys.PRINT_TEMPLATE))
		{
			try
			{
				template = service.getConsentTemplate((ConsentTemplateKeyDTO) sessionMap.get(SessionMapKeys.PRINT_TEMPLATE));
			}
			catch (UnknownDomainException | UnknownConsentTemplateException | InvalidVersionException e)
			{
				logger.error(e.getLocalizedMessage());
			}
			sessionMap.remove(SessionMapKeys.PRINT_TEMPLATE);
		}
	}

	public void onNewUpload()
	{

		webFile.onNewUpload();
	}

	public void onDoAction()
	{
		int rowIndex = 0;
		onClearSignerIds();

		for (List<String> row : webFile.getElements())
		{
			rowIndex++;
			PrintPrefillEntry entry = new PrintPrefillEntry();

			int typeCount = domainSelector.getSelectedDomain().getSignerIdTypes().size();
			int additionalCount = 4;

			if ((typeCount + additionalCount) == webFile.getColumns().size())
			{
				try
				{
					//n = number of signertypes in domain
					//item 0 - n-1: signerids
					List<SignerIdDTO> sids = new ArrayList<>();
					for (String type : domainSelector.getSelectedDomain().getSignerIdTypes())
					{
						sids.add(new SignerIdDTO(type, row.get(sids.size())));
					}
					entry.setSignerIdDtos(sids);
					// item n : Datum (Unterschrift betroffene Person)
					if (row.get(typeCount) != null && !row.get(typeCount).isEmpty())
					{
						entry.setSignerDate(row.get(typeCount));
					}
					// item n+1: Ort (Unterschrift betroffene Person)
					if (row.get(typeCount + 1) != null && !row.get(typeCount + 1).isEmpty())
					{
						entry.setSignerPlace(row.get(typeCount + 1));
					}
					// item n+2: Datum (Unterschrift aufklärende Person)
					if (row.get(typeCount + 2) != null && !row.get(typeCount + 2).isEmpty())
					{
						entry.setPhysicianDate(row.get(typeCount + 2));
					}
					// item n+3: Ort (Unterschrift aufklärende Person)
					if (row.get(typeCount + 3) != null && !row.get(typeCount + 3).isEmpty())
					{
						entry.setPhysicianPlace(row.get(typeCount + 3));
					}
					printPrefillEntries.add(entry);
				}
				catch (ParseException e)
				{
					logMessage(e.getLocalizedMessage(), Severity.ERROR);
				}
			}
			else
			{
				String msg = "Invalid column count in line {0}, should be {1}.";

				Object[] args = { rowIndex, typeCount + additionalCount };
				logger.error(new String().format(msg, args[0], args[1]));
				logMessage(new MessageFormat(msg).format(args), Severity.ERROR);
			}
		}
	}

	public WebFile getWebFile()
	{
		return webFile;
	}

	public void onNewSignerIds()
	{
		editPrefillEntry = new PrintPrefillEntry();
		List<SignerIdDTO> signerIdDtos = new ArrayList<>();
		for (String type : domainSelector.getSelectedDomain().getSignerIdTypes())
		{
			signerIdDtos.add(new SignerIdDTO(type, "", null, null));
		}
		editPrefillEntry.setSignerIdDtos(signerIdDtos);
	}

	public void onAddSignerIds()
	{
		printPrefillEntries.add(editPrefillEntry);
	}

	public void onClearSignerIds()
	{
		printPrefillEntries.clear();
	}

	public void onEditPrefillEntry(PrintPrefillEntry selectedEntry)
	{
		editIndex = printPrefillEntries.indexOf(selectedEntry);
		selectedPrefillEntry = printPrefillEntries.get(editIndex);

		editPrefillEntry = new PrintPrefillEntry();
		List<SignerIdDTO> sids = new ArrayList<>();

		for (SignerIdDTO ids : selectedEntry.getSignerIdDtos())
		{
			sids.add(new SignerIdDTO(ids.getIdType(), ids.getId(), null, null));
		}
		editPrefillEntry.setSignerIdDtos(sids);
		editPrefillEntry.setPhysicianDate(selectedEntry.getPhysicianDate());
		editPrefillEntry.setPhysicianPlace(selectedEntry.getPhysicianPlace());
		editPrefillEntry.setSignerDate(selectedEntry.getSignerDate());
		editPrefillEntry.setSignerPlace(selectedEntry.getSignerPlace());

	}

	public void onUpdatePrefillEntries()
	{
		logger.debug("onUpdatePrefillEntries");
		printPrefillEntries.set(editIndex, editPrefillEntry);
		editIndex = null;
	}

	public void onDeletePrefillEntry(PrintPrefillEntry selectedEntry)
	{
		logger.debug("onDeletePrefillEntry");
		printPrefillEntries.remove(selectedEntry);
	}

	public String onPrint()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put(SessionMapKeys.PRINT_TEMPLATE, template.getKey());
		sessionMap.put(SessionMapKeys.PRINT_SIGNER_IDS, printPrefillEntries);

		return "/html/internal/consents.xhtml?print=true&faces-redirect=true";
	}

	public ConsentTemplateDTO getTemplate()
	{
		return template;
	}

	public List<PrintPrefillEntry> getPrintPrefillEntries()
	{
		return printPrefillEntries;
	}

	public PrintPrefillEntry getEditPrefillEntry()
	{
		return editPrefillEntry;
	}

	public boolean isEdit()
	{
		return editIndex != null;
	}

	public PrintPrefillEntry getSelectedPrefillEntry()
	{
		return selectedPrefillEntry;
	}

	public void setSelectedPrefillEntry(PrintPrefillEntry selectedSignerIds)
	{
		this.selectedPrefillEntry = selectedSignerIds;
	}
}
