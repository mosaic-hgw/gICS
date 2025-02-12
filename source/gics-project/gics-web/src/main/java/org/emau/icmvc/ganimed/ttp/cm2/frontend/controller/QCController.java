package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeFieldGroup;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeOccurrence;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCTypeStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCProblemDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCProblemHistoryDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.QCProblemStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.QCProblemStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownConsentTemplateException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownModuleException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.WebConsent;
import org.primefaces.event.MenuActionEvent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSeparator;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;

/**
 * Backing Bean for QC Workflow
 *
 * @author Arne Blumentritt, Martin Bialke
 */
@ViewScoped
@Named("qcController")
public class QCController extends AbstractGICSBean
{
	@Serial private static final long serialVersionUID = 5627590533797893798L;

	private WebConsent consent;
	private QCDTO qc;
	private boolean saveAsNewQc;
	private QCProblemDTO selectedQcProblem;
	private PageMode qcProblemMode;

	private List<SelectItem> problemStatusSelectList;
	private List<SelectItem> qcTypeSelectList;

	public static final String META_SCAN_FILENAME = "meta_scan_filename";
	public static final String META_MODULE_STATE = "meta_module_state";
	public static final String META_MODULE_KEY_NAME = "meta_module_keyName";
	public static final String META_MODULE_KEY_VERSION = "meta_module_keyVersion";

	@PostConstruct
	public void init()
	{
		problemStatusSelectList = new ArrayList<>();
		SelectItemGroup open = new SelectItemGroup(getBundle().getString("model.qc.problem.status.type.OPEN"));
		open.setSelectItems(Arrays.stream(QCProblemStatus.values())
				.filter(s -> QCProblemStatusType.OPEN.equals(s.getType()))
				.map(s -> new SelectItem(s, getBundle().getString("model.qc.problem.status." + s.name())))
				.toArray(SelectItem[]::new));
		SelectItemGroup closed = new SelectItemGroup(getBundle().getString("model.qc.problem.status.type.CLOSED"));
		closed.setSelectItems(Arrays.stream(QCProblemStatus.values())
				.filter(s -> QCProblemStatusType.CLOSED.equals(s.getType()))
				.map(s -> new SelectItem(s, getBundle().getString("model.qc.problem.status." + s.name())))
				.toArray(SelectItem[]::new));
		problemStatusSelectList.add(open);
		problemStatusSelectList.add(closed);

		qcTypeSelectList = new ArrayList<>();
		SelectItemGroup valid = new SelectItemGroup(getBundle().getString("model.consent.qc.status.valid"));
		valid.setSelectItems(getQcConfig().getTypes().stream()
				.filter(t -> QCTypeStatus.VALID.equals(t.getStatus()))
				.map(t -> new SelectItem(t.getId(), t.getLabelOrId(languageBean.getLanguage())))
				.toArray(SelectItem[]::new));
		SelectItemGroup invalid = new SelectItemGroup(getBundle().getString("model.consent.qc.status.invalid"));
		invalid.setSelectItems(getQcConfig().getTypes().stream()
				.filter(t -> QCTypeStatus.INVALID.equals(t.getStatus()))
				.map(t -> new SelectItem(t.getId(), t.getLabelOrId(languageBean.getLanguage())))
				.toArray(SelectItem[]::new));
		qcTypeSelectList.add(valid);
		qcTypeSelectList.add(invalid);
	}

	public void loadAndEdit(WebConsent consent)
	{
		load(consent);
		onEdit();
	}

	public void load(WebConsent consent)
	{
		this.consent = consent;
		this.qc = consent.getQualityControl();
		pageMode = PageMode.READ;
	}

	public void onEdit()
	{
		loadInspector();
		saveAsNewQc = true;
		pageMode = PageMode.EDIT;
	}

	private void loadInspector()
	{
		String user = getUser();
		if (!StringUtils.isEmpty(user))
		{
			qc.setInspector(user);
		}
	}

	public void onCancel()
	{
		pageMode = PageMode.READ;
		qcProblemMode = PageMode.READ;
	}

	public void onPrepareFinish()
	{
		qc.setType(null);
	}

	public void onSave() throws UnknownSignerIdTypeException, InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException,
			UnknownDomainException, UnknownConsentException, InconsistentStatusException
	{
		getServiceWithAutomaticNotification().setQCForConsent(consent.getKey(), qc);
		this.qc = getService().getConsent(this.consent.getKey()).getQualityControl();
		this.consent.setQcHistory(getService().getQCHistoryForConsent(consent.getKey()));
		onCancel();
	}

	public List<QCProblemDTO> getProblemsFor(String group)
	{
		return qc.getProblems().stream().filter(p -> p.getType(getQcConfig()).getField().getGroup().name().equals(group)).toList();
	}

	public void onNewProblem(QCProblemType problemType, String ref, Object meta)
	{
		selectedQcProblem = new QCProblemDTO(problemType.getId(), QCProblemStatus.OPEN_EXTERN);
		selectedQcProblem.setRef(ref);
		switch (problemType.getField())
		{
			case TEMPLATE_NAME -> selectedQcProblem.setFormValue(consent.getTemplate().getLabelOrName());
			case TEMPLATE_VERSION -> selectedQcProblem.setFormValue(consent.getTemplate().getVersionLabelOrVersion());
			case SIGNER_ID_VALUE -> selectedQcProblem.setFormValue(consent.getSignerIdsAsString());
			case SIGNATURE_PHYSICIAN_DATE -> selectedQcProblem.setFormValue(dateToString(consent.getPhysicianSigningDate(), "date"));
			case SIGNATURE_PHYSICIAN_LOCATION -> selectedQcProblem.setFormValue(consent.getPhysicianSigningPlace());
			case SIGNATURE_PARTICIPANT_DATE -> selectedQcProblem.setFormValue(dateToString(consent.getPatientSigningDate(), "date"));
			case SIGNATURE_PARTICIPANT_LOCATION -> selectedQcProblem.setFormValue(consent.getPatientSigningPlace());
			case MODULE_STATUS ->
			{
				if (meta instanceof ModuleStateDTO moduleStateDTO)
				{
					selectedQcProblem.setFormValue(getCommonBundle().getString("model.consent.status.user.CONSENT." + moduleStateDTO.getConsentState().name()));
					try
					{
						ModuleDTO moduleDTO = getService().getModule(moduleStateDTO.getKey());
						selectedQcProblem.setCommentExtern(StringUtils.isNotEmpty(moduleDTO.getShortText()) ?
								moduleDTO.getShortText() : (StringUtils.isNotEmpty(moduleDTO.getTitle()) ? text.sanitizePlain(moduleDTO.getTitle()) : moduleDTO.getLabelOrName()));
					}
					catch (UnknownDomainException | UnknownModuleException | InvalidParameterException | InvalidVersionException e)
					{
						logger.error(e.getLocalizedMessage());
					}
				}
			}
			case SCAN ->
			{
				if (meta instanceof String fileName)
				{
					selectedQcProblem.setCommentExtern(fileName);
				}
			}
			default ->
			{
			}
		}
		qcProblemMode = PageMode.NEW;
	}

	public void onNewProblemByMenu(ActionEvent event)
	{
		MenuItem menuItem = ((MenuActionEvent) event).getMenuItem();
		String problemTypeId = menuItem.getParams().get("problemTypeId").getFirst();
		String ref = menuItem.getParams().get("ref").getFirst();
		Object meta = null;
		if (menuItem.getParams().containsKey("meta"))
		{
			meta = menuItem.getParams().get("meta").getFirst();
		}
		if (menuItem.getParams().containsKey(META_MODULE_STATE) && menuItem.getParams().containsKey(META_MODULE_KEY_NAME) && menuItem.getParams().containsKey(META_MODULE_KEY_VERSION))
		{
			meta = new ModuleStateDTO(
					new ModuleKeyDTO(consent.getDomainName(), menuItem.getParams().get(META_MODULE_KEY_NAME).getFirst(), menuItem.getParams().get(META_MODULE_KEY_VERSION).getFirst()),
					ConsentStatus.valueOf(menuItem.getParams().get(META_MODULE_STATE).getFirst()), null);
		}
		onNewProblem(getQcConfig().getProblemTypeById(problemTypeId), ref, meta);
	}

	public void onEditProblem(QCProblemDTO qcProblem)
	{
		selectedQcProblem = qcProblem;
		qcProblemMode = PageMode.READ.equals(pageMode) ? PageMode.READ : PageMode.EDIT;
	}

	public void onDeleteProblem(QCProblemDTO qcProblem)
	{
		qcProblem.setStatus(QCProblemStatus.DELETED_INTERN);
	}

	public void onResurrectProblem(QCProblemDTO qcProblem)
	{
		qcProblem.setStatus(QCProblemStatus.OPEN_INTERN);
	}

	public void onSaveProblem()
	{
		if (PageMode.NEW.equals(qcProblemMode))
		{
			selectedQcProblem.setCreatedAt(new Date());
			qc.getProblems().add(selectedQcProblem);
		}
	}

	public MenuModel getProblemTypesMenu(String groupName, String ref, Object meta)
	{
		MenuModel menu = new DefaultMenuModel();

		if (StringUtils.isEmpty(groupName))
		{
			for (QCProblemTypeFieldGroup group : QCProblemTypeFieldGroup.values())
			{
				DefaultSubMenu subMenu = DefaultSubMenu.builder()
						.label(getBundle().getString("model.qc.problem.type.field.group." + group.name()))
						.icon("mdi mdi-" + getBundle().getString("model.qc.problem.type.field.group." + group.name() + ".icon"))
						.build();

				for (QCProblemType qcProblemType : getAvailableProblemTypesForGroup(group.name(), ref).stream()
						.sorted(Comparator.comparing(p -> p.getLabelOrId(getBundle().getLocale().getLanguage()).toLowerCase())).toList())
				{
					subMenu.getElements().add(createMenuItem(qcProblemType, ref, meta));
					subMenu.getElements().add(new DefaultSeparator());
				}
				if (subMenu.getElements().isEmpty())
				{
					subMenu.setDisabled(true);
				}
				else
				{
					// remove last separator
					subMenu.getElements().removeLast();
				}

				menu.getElements().add(subMenu);
				menu.getElements().add(new DefaultSeparator());
			}
		}
		else
		{
			for (QCProblemType qcProblemType : getAvailableProblemTypesForGroup(groupName, ref))
			{
				menu.getElements().add(createMenuItem(qcProblemType, ref, meta));
				menu.getElements().add(new DefaultSeparator());
			}
		}
		if (!menu.getElements().isEmpty())
		{
			menu.getElements().removeLast();
		}

		return menu;
	}

	public List<QCProblemType> getAvailableProblemTypesForGroup(String groupName, String ref)
	{
		List<String> usedProblemTypesForRef = qc.getProblems().stream()
				.filter(p -> Objects.equals(p.getRef(), ref))
				.map(QCProblemDTO::getType)
				.filter(type -> groupName.equals(getProblemType(type).getField().getGroup().name()))
				.toList();

		return getProblemTypes().stream().filter(t -> t.getField().getGroup().name().equals(groupName))
				.filter(t -> !usedProblemTypesForRef.contains(t.getId())).toList();
	}

	public boolean isHistory(QCProblemDTO qcProblem)
	{
		return qcProblem instanceof QCProblemHistoryDTO;
	}

	public boolean isHistoryOrDeleted(QCProblemDTO qcProblem)
	{
		return qcProblem instanceof QCProblemHistoryDTO || QCProblemStatusType.DELETED.equals(qcProblem.getStatus().getType());
	}

	/**
	 * Get a list of the current problem and its history for rendering the tabs.
	 * The current problem is needed for editing purposes.
	 * The history must be cast to QCProblemDTO to have a unique map.
	 * The most recent history element must be omitted because its the same one as the current problem.
	 *
	 * @param qcProblem
	 * 		problem to get history for
	 * @return list of current problem and its history
	 */
	public List<QCProblemDTO> getCurrentProblemAndHistory(QCProblemDTO qcProblem)
			throws UnknownSignerIdTypeException, InvalidParameterException, UnknownConsentTemplateException, InvalidVersionException, UnknownDomainException, UnknownConsentException
	{
		return Stream.concat(Stream.of(qcProblem), getService().getQCProblemHistoryForQCProblem(qcProblem, this.consent.getKey()).stream()
						.sorted(Comparator.comparing(QCProblemHistoryDTO::getStartDate).reversed())
						.skip(1)
						.map(QCProblemDTO.class::cast))
				.collect(Collectors.toList());
	}

	private DefaultMenuItem createMenuItem(QCProblemType qcProblemType, String ref, Object meta)
	{
		DefaultMenuItem item = DefaultMenuItem.builder()
				.value(qcProblemType.getLabelOrId(getBundle().getLocale().getLanguage()))
				.id(qcProblemType.getId())
				.command("#{qcController.onNewProblemByMenu}")
				.process("@this")
				.update("@widgetVar(qc_problem_dialog):qc_problem_dialog")
				.oncomplete("PF('qc_problem_dialog').show()")
				.build();
		item.setParam("problemTypeId", qcProblemType.getId());
		item.setParam("ref", ref);
		if (meta instanceof ModuleStateDTO moduleStateDTO)
		{
			item.setParam(META_MODULE_STATE, moduleStateDTO.getConsentState().name());
			item.setParam(META_MODULE_KEY_NAME, moduleStateDTO.getKey().getName());
			item.setParam(META_MODULE_KEY_VERSION, moduleStateDTO.getKey().getVersion());
		}
		else if (meta != null)
		{
			item.setParam("meta", meta);
		}
		return item;
	}

	public QCDTO getQc()
	{
		return qc;
	}

	public void setQc(QCDTO qc)
	{
		this.qc = qc;
	}

	public QCProblemType getProblemType(String type)
	{
		return getQcConfig().getProblemTypeById(type);
	}

	public QCProblemDTO getSelectedQcProblem()
	{
		return selectedQcProblem;
	}

	public void setSelectedQcProblem(QCProblemDTO selectedQcProblem)
	{
		this.selectedQcProblem = selectedQcProblem;
	}

	public Set<QCProblemType> getProblemTypes()
	{
		QCProblemTypeOccurrence nonMatch = this.consent.isDigitalPatientSignature() ? QCProblemTypeOccurrence.PAPER : QCProblemTypeOccurrence.DIGITAL;
		return getQcConfig().getProblemTypes().stream().filter(t -> nonMatch != t.getOccurrence()).collect(Collectors.toSet());
	}

	public PageMode getQcProblemMode()
	{
		return qcProblemMode;
	}

	public boolean isSaveAsNewQc()
	{
		return saveAsNewQc;
	}

	public void setSaveAsNewQc(boolean saveAsNewQc)
	{
		this.saveAsNewQc = saveAsNewQc;
	}

	public List<SelectItem> getProblemStatusSelectList()
	{
		return problemStatusSelectList;
	}

	public List<SelectItem> getQcTypeSelectList()
	{
		return qcTypeSelectList;
	}

	public boolean isEdit()
	{
		return PageMode.EDIT.equals(pageMode);
	}
}
