package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractConsentController;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 * Backing Bean for Consent Print View
 * 
 * @author Arne Blumentritt
 * 
 */
@ManagedBean(name = "consentPrintController")
@ViewScoped
public class ConsentPrintController extends AbstractConsentController
{
	// TODO add download f�r example csv in frontend

	private List<HashMap<String, String>> signerIdMatrix = new ArrayList<>();
	private ConsentTemplateDTO template;

	private HashMap<String, String> selectedSignerIds;
	private List<SignerIdDTO> editSignerIds;
	private Integer editIndex;

	@PostConstruct
	protected void init()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		if (sessionMap.containsKey("printTemplate"))
		{
			try
			{
				template = cmManager.getConsentTemplate((ConsentTemplateKeyDTO) sessionMap.get("printTemplate"));
			}
			catch (UnknownDomainException | UnknownConsentTemplateException | VersionConverterClassException | InvalidVersionException e)
			{
				logger.error(e.getLocalizedMessage());
			}
			sessionMap.remove("printTemplate");
		}
	}

	/**
	 * Upload a list of persons
	 *
	 * @param event
	 */
	public void onUploadSignerIds(FileUploadEvent event)
	{
		init();

		UploadedFile file = event.getFile();
		BufferedReader rd = new BufferedReader(new StringReader(new String(file.getContents())));

		try
		{
			String sep = null;
			String tmp;

			Pattern pattern = null;
			Matcher matcher;

			while ((tmp = rd.readLine()) != null)
			{
				if (sep == null)
				{
					// Get seperator
					if (tmp.contains("sep"))
					{
						sep = tmp.substring(4);
						tmp = rd.readLine();
					}
					else
					{
						sep = ",";
					}

					// Compile pattern
					pattern = Pattern.compile("((?:\"[^\"]*?\")*|[^\"][^" + sep + "]*?)([" + sep + "]|$)");
				}

				if (tmp != null && !tmp.isEmpty())
				{
					HashMap<String, String> ids = new HashMap<>();
					matcher = pattern.matcher(tmp);
					int i = 0;

					while (matcher.find())
					{
						if (i + 1 > domainSelector.getSelectedDomain().getSignerIdTypes().size())
						{
							break;
						}
						ids.put(domainSelector.getSelectedDomain().getSignerIdTypes().get(i), matcher.group(1));
						i++;
					}
					signerIdMatrix.add(ids);
				}
			}

		}
		catch (Exception e)
		{
			logger.error(e.getLocalizedMessage());
		}
		finally
		{
			if (rd != null)
			{
				try
				{
					rd.close();
				}
				catch (IOException e)
				{
					if (logger.isErrorEnabled())
					{
						logger.error(e.getLocalizedMessage());
					}
				}
			}
		}
	}

	public void onNewSignerIds()
	{
		editSignerIds = new ArrayList<>();
		for (String type : domainSelector.getSelectedDomain().getSignerIdTypes())
		{
			editSignerIds.add(new SignerIdDTO(type, ""));
		}
	}

	public void onAddSignerIds()
	{
		HashMap<String, String> ids = new HashMap<>();
		for (SignerIdDTO id : editSignerIds)
		{
			ids.put(id.getIdType(), id.getId());
		}
		//TODO check ob schon vorhanden
		signerIdMatrix.add(ids);
	}
	
	public void onClearSignerIds()
	{
		signerIdMatrix.clear();
	}

	public void onEditSignerIds(HashMap<String, String> signerIds)
	{
		editIndex = signerIdMatrix.indexOf(signerIds);
		selectedSignerIds = signerIdMatrix.get(editIndex);
		editSignerIds = new ArrayList<>();
		
		for (Entry<String, String> ids : selectedSignerIds.entrySet())
		{
			editSignerIds.add(new SignerIdDTO(ids.getKey(), ids.getValue()));
		}
	}

	public void onUpdateSignerIds()
	{
		HashMap<String, String> ids = signerIdMatrix.get(editIndex);
		ids.clear();
		//TODO check ob schon vorhanden
		for (SignerIdDTO id : editSignerIds)
		{
			ids.put(id.getIdType(), id.getId());
		}
		editIndex = null;
	}
	
	public void onDeleteSignerIds(HashMap<String, String> selectedSignerIds)
	{
		signerIdMatrix.remove(selectedSignerIds);
	}

	public String onPrint()
	{
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, Object> sessionMap = externalContext.getSessionMap();
		sessionMap.put("printTemplate", template.getKey());
		sessionMap.put("printSignerIds", signerIdMatrix);

		return "/html/internal/consents.xhtml?print=true&faces-redirect=true";
	}

	public ConsentTemplateDTO getTemplate()
	{
		return template;
	}

	public List<HashMap<String, String>> getSignerIdMatrix()
	{
		return signerIdMatrix;
	}

	public List<SignerIdDTO> getEditSignerIds()
	{
		return editSignerIds;
	}

	public boolean isEdit()
	{
		return editIndex != null;
	}

	public HashMap<String, String> getSelectedSignerIds()
	{
		return selectedSignerIds;
	}

	public void setSelectedSignerIds(HashMap<String, String> selectedSignerIds)
	{
		this.selectedSignerIds = selectedSignerIds;
	}
}
