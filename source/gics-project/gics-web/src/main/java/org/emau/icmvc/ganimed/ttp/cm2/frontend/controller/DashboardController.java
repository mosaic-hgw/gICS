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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.StatisticException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.StatisticPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.util.StatisticKeys;
import org.icmvc.ttp.web.controller.ThemeBean;
import org.icmvc.ttp.web.util.Chart;
import org.icmvc.ttp.web.util.File;
import org.primefaces.model.StreamedContent;
import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.charts.PieChart;

@SessionScoped
@Named("dashboardController")
public class DashboardController extends AbstractGICSBean
{
	@Serial
	private static final long serialVersionUID = 6417350538004746392L;

	@Inject
	@ManagedProperty(value = "#{themeBean}")
	private ThemeBean themeBean;

	private Map<String, String> qcColors;
	private List<ConsentTemplateDTO> domainTemplates;
	private List<PolicyDTO> domainPolicies;
	private List<StatisticDTO> rangeStats;
	private List<String> validQcTypes;
	private List<String> invalidQcTypes;
	private StatisticDTO stats;
	private boolean hasStats;
	private long allDocuments = 0L;
	private Chart.BarScale documentsBarScale = Chart.BarScale.MONTHS_12;
	private Chart.BarScale qcBarScale = Chart.BarScale.MONTHS_12;

	private DashboardDomain domain = DashboardDomain.CURRENT;

	private Date rangeStartDate;
	private Date rangeEndDate;
	private Date statsDate;
	private Date statsMinDate;
	private Date statsMaxDate;

	private boolean includeUnknownPolicyStatus = false;
	private int selectedSignerIdIndex = 0;
	
	private boolean rangeStatsLoaded = false;
	private String domainName;

	@PostConstruct
	public void init()
	{
		statsDate = null;
		statsMinDate = null;
		statsMaxDate = null;
		rangeStartDate = null;
		rangeEndDate = null;
		rangeStatsLoaded = false;
		domainName = domainSelector.getSelectedDomainName();
		loadStats();

		if (isHasStatsInTimespan())
		{
			createQcColors();
		}
	}
	
	public void checkDomainChange()
	{
		if (FacesContext.getCurrentInstance().isPostback())
		{
			return;
		}
		if (!domainSelector.getSelectedDomainName().equals(domainName))
		{
			init();
		}
	}
	
	public void loadRangeStats()
	{
		rangeStats = getStatisticService().getStatsFromTo(rangeStartDate, rangeEndDate);
		rangeStatsLoaded = true;
	}

	public void updateStats()
	{
		try
		{
			getStatisticService().updateStats();
			init();
			logMessage(getCommonBundle().getString("page.dashboard.statistic.updated"), Severity.INFO);
		}
		catch (StatisticException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	public void onDateChange()
	{
		Calendar statsCal = Calendar.getInstance();
		statsCal.setTime(statsDate);
		statsCal.set(Calendar.HOUR_OF_DAY, 23);
		statsCal.set(Calendar.MINUTE, 59);
		statsCal.set(Calendar.SECOND, 59);
		statsDate = statsCal.getTime();

		Calendar rangeEndCal = Calendar.getInstance();
		rangeEndCal.setTime(rangeEndDate);
		rangeEndCal.set(Calendar.HOUR_OF_DAY, 23);
		rangeEndCal.set(Calendar.MINUTE, 59);
		rangeEndCal.set(Calendar.SECOND, 59);
		rangeEndDate = rangeEndCal.getTime();
		rangeStatsLoaded = false;
		loadStats();
		createQcColors();
	}

	/* Stats Overview */
	public Map<String, String> getLatestStatsLabels()
	{
		if (DashboardDomain.ALL.equals(domain))
		{
			return getLatestStatsAllDomainsLabels(false);
		}
		else
		{
			return getLatestStatsActiveDomainLabels(false);
		}
	}

	/* Stats Overview */
	public Map<String, String> getLatestStatsAllDomainsLabels(boolean extended)
	{
		Map<String, String> result = new LinkedHashMap<>();
		for (Map.Entry<ConsentTemplateType, String> templateTypeKey : getAllTemplateTypeStatisticKeys().entrySet())
		{
			result.put(templateTypeKey.getValue(), getBundle().getString("template.type.plural." + detailedTypes() + "short." + templateTypeKey.getKey().name()));
		}
		result.put(StatisticKeys.TEMPLATES, getBundle().getString("template.templates"));
		result.put(StatisticKeys.MODULES, getBundle().getString("module.modules"));
		if (extended)
		{
			result.put(StatisticKeys.MODULES_WITHOUT_VERSIONS, getBundle().getString("module.modulesWithoutVersions"));
			result.put(StatisticKeys.SIGNED_POLICIES, getBundle().getString("page.dashboard.policies.signed"));
		}
		result.put(StatisticKeys.POLICIES, getBundle().getString("policy.policies"));
		if (extended)
		{
			result.put(StatisticKeys.DOCUMENTS_WITH_SCANS, getBundle().getString("page.dashboard.documents.details.withScan"));
			result.put(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE, getBundle().getString("page.dashboard.documents.details.withDigitalSignature"));
		}
		return result;
	}

	public Map<String, String> getLatestStatsActiveDomainLabels(boolean extended)
	{
		Map<String, String> result = new LinkedHashMap<>();
		for (Map.Entry<ConsentTemplateType, String> templateTypeKeys : getDomainTemplateTypeStatisticKeys().entrySet())
		{
			result.put(new StatisticKeys(templateTypeKeys.getValue()).perDomain(getSelectedDomain().getName()).build(),
					getBundle().getString("template.type.plural." + detailedTypes() + "short." + templateTypeKeys.getKey().name()));
		}
		result.put(new StatisticKeys(StatisticKeys.TEMPLATES).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("template.templates"));
		result.put(new StatisticKeys(StatisticKeys.MODULES).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("module.modules"));
		if (extended)
		{
			result.put(new StatisticKeys(StatisticKeys.MODULES_WITHOUT_VERSIONS).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("module.modulesWithoutVersions"));
			result.put(new StatisticKeys(StatisticKeys.SIGNED_POLICIES).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("page.dashboard.policies.signed"));
		}
		result.put(new StatisticKeys(StatisticKeys.POLICIES).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("policy.policies"));
		if (extended)
		{
			result.put(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_SCANS).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("page.dashboard.documents.details.withScan"));
			result.put(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE).perDomain(getSelectedDomain().getName()).build(),
					getBundle().getString("page.dashboard.documents.details.withDigitalSignature"));
		}
		return result;
	}

	private Map<ConsentTemplateType, String> getTemplateTypeStatisticKeys()
	{
		return DashboardDomain.ALL.equals(domain) ? getAllTemplateTypeStatisticKeys() : getDomainTemplateTypeStatisticKeys();
	}

	private Map<ConsentTemplateType, String> getAllTemplateTypeStatisticKeys()
	{
		return StatisticKeys.templateTypeKeys;
	}

	private Map<ConsentTemplateType, String> getDomainTemplateTypeStatisticKeys()
	{
		return getAllTemplateTypeStatisticKeys().entrySet().stream()
				.filter(e -> getDomainTemplateTypes().contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
	}

	/* Pie Charts */

	/**
	 * get a pie chart of all document types
	 * available for all domains and single domain
	 *
	 * @param mobile
	 * 		optimize the chart legend position for mobile
	 * @return pie chart
	 */
	public PieChart getDocumentsChart(boolean mobile)
	{
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<String> colors = getDocumentColors(false);

		for (Map.Entry<ConsentTemplateType, String> templateTypeKeys : getTemplateTypeStatisticKeys().entrySet())
		{
			if (DashboardDomain.ALL.equals(domain))
			{
				values.add(stats.getMappedStatValue().getOrDefault(new StatisticKeys(templateTypeKeys.getValue()).build(), 0L));
			}
			else
			{
				values.add(stats.getMappedStatValue().getOrDefault(new StatisticKeys(templateTypeKeys.getValue()).perDomain(getSelectedDomain().getName()).build(), 0L));
			}
			labels.add(getBundle().getString("template.type.plural." + detailedTypes() + templateTypeKeys.getKey().name()));
		}

		return Chart.initPieChart(values, labels, colors, mobile ? Chart.LegendPosition.TOP : Chart.LegendPosition.LEFT, themeBean.getDarkMode());
	}

	/**
	 * get a pie chart of all qc types
	 * only available for single domain
	 *
	 * @param mobile
	 * 		optimize the chart legend position for mobile
	 * @return pie chart
	 */
	public PieChart getQcChart(boolean mobile)
	{
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<String> colors = new ArrayList<>();

		if (!DashboardDomain.ALL.equals(domain))
		{
			for (String type : Stream.concat(validQcTypes.stream(), invalidQcTypes.stream()).toList())
			{
				values.add(stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(type).build(), 0L));
				labels.add(getQcTypeLabel(type));
				colors.add(qcColors.get(type));
			}
		}

		return Chart.initPieChart(values, labels, colors, mobile ? Chart.LegendPosition.TOP : Chart.LegendPosition.LEFT, themeBean.getDarkMode());
	}

	/* Percentage bars */

	/**
	 * get a percentage values for all signed policies
	 * only available for single domain
	 *
	 * @param signerIdType
	 * 		signerIdType to get the statistic for
	 * @return list with percentages
	 */
	public List<StatisticPolicy> getPolicyStatistic(String signerIdType)
	{
		List<StatisticPolicy> statisticPolicies = new ArrayList<>();

		if (!DashboardDomain.ALL.equals(domain))
		{
			for (PolicyDTO policy : domainPolicies)
			{
				long accepted = stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.SIGNER_IDS)
						.perDomain(getSelectedDomain().getName())
						.perIdType(signerIdType)
						.perPolicy(policy.getKey())
						.perStatus(ConsentStatusType.ACCEPTED)
						.build(), 0L);
				long declined = stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.SIGNER_IDS)
						.perDomain(getSelectedDomain().getName())
						.perIdType(signerIdType)
						.perPolicy(policy.getKey())
						.perStatus(ConsentStatusType.DECLINED)
						.build(), 0L);

				long unknown = stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.SIGNER_IDS)
						.perDomain(getSelectedDomain().getName())
						.perIdType(signerIdType)
						.perPolicy(policy.getKey())
						.perStatus(ConsentStatusType.UNKNOWN)
						.build(), 0L);

				statisticPolicies.add(new StatisticPolicy(policy, accepted, declined, unknown));
			}
		}

		return statisticPolicies.stream()
				.sorted(Comparator.comparing(StatisticPolicy::getAccepted).thenComparing(p -> p.getAll(includeUnknownPolicyStatus)).reversed())
				.collect(Collectors.toList());
	}

	public StreamedContent getPolicyStatisticDownload()
	{
		String signerIdType = getDomainSignerIdTypes().get(selectedSignerIdIndex);
		List<StatisticPolicy> policyCount = getPolicyStatistic(signerIdType);
		Map<String, List<Object>> values = new LinkedHashMap<>();
		values.put(getBundle().getString("policy"), policyCount.stream().map(p -> p.getLabelOrName() + " " + p.getKey().getVersion()).collect(Collectors.toList()));
		values.put(getBundle().getString("page.dashboard.policies.accepted"), policyCount.stream().map(StatisticPolicy::getAccepted).collect(Collectors.toList()));
		values.put(getBundle().getString("page.dashboard.policies.all"), policyCount.stream().map(p -> p.getAll(includeUnknownPolicyStatus)).collect(Collectors.toList()));
		values.put(getCommonBundle().getString("page.dashboard.percentage"), policyCount.stream().map(p -> p.getAcceptedPercentage(includeUnknownPolicyStatus)).collect(Collectors.toList()));

		return File.get3DDataAsCSV(values, null, "consent to policies for signerIdType " + signerIdType, TOOL);
	}

	/**
	 * get a percentage values for all used templates
	 * only available for single domain
	 *
	 * @return map with percentages
	 */
	public Map<ConsentTemplateDTO, Long> getTemplateStatistic()
	{
		Map<ConsentTemplateDTO, Long> result = new LinkedHashMap<>();

		if (!DashboardDomain.ALL.equals(domain))
		{
			for (ConsentTemplateDTO templateDTO : domainTemplates)
			{
				result.put(templateDTO, stats.getMappedStatValue()
						.getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS).perDomain(getSelectedDomain().getName()).perTemplate(templateDTO.getKey()).build(), 0L));
			}
		}

		return result.entrySet().stream()
				.sorted(Map.Entry.<ConsentTemplateDTO, Long>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public StreamedContent getTemplateStatisticDownload()
	{
		Map<ConsentTemplateDTO, Long> templateCount = getTemplateStatistic();
		Map<String, List<Object>> values = new LinkedHashMap<>();
		values.put(getBundle().getString("consent.template"), templateCount.keySet().stream().map(t -> t.getLabelOrName() + " " + t.getVersionLabelOrVersion()).collect(Collectors.toList()));
		values.put(getCommonBundle().getString("page.dashboard.count"), new ArrayList<>(templateCount.values()));
		values.put(getCommonBundle().getString("page.dashboard.percentage"), templateCount.values().stream().map(c -> getPercentage(c, getAllDocuments())).collect(Collectors.toList()));

		return File.get3DDataAsCSV(values, null, "template usage", TOOL);
	}

	/* History Line Charts */

	/**
	 * get a line chart with historic template types
	 * available for all domains and single domain
	 *
	 * @return line chart
	 */
	public LineChart getDocumentsHistoryChart()
	{
		List<Number> allDocumentsValues = new ArrayList<>();
		Map<ConsentTemplateType, List<Number>> templateTypeValues = new LinkedHashMap<>();
		for (ConsentTemplateType templateType : getTemplateTypeStatisticKeys().keySet())
		{
			templateTypeValues.put(templateType, new ArrayList<>());
		}
		List<String> dataLabels = new ArrayList<>();
		List<String> dataSetLabels = getDocumentLabels(true);
		List<String> dataSetColors = getDocumentColors(true);

		List<List<Number>> valuesLists = new ArrayList<>();
		valuesLists.add(allDocumentsValues);
		valuesLists.addAll(templateTypeValues.values());

		for (StatisticDTO statisticDTO : Chart.reduceStatistic(rangeStats, 50))
		{
			long all = 0;
			for (Map.Entry<ConsentTemplateType, String> templateTypeKeys : getTemplateTypeStatisticKeys().entrySet())
			{
				long type;
				if (DashboardDomain.ALL.equals(domain))
				{
					type = statisticDTO.getMappedStatValue().getOrDefault(new StatisticKeys(templateTypeKeys.getValue()).build(), 0L);
				}
				else
				{
					type = statisticDTO.getMappedStatValue().getOrDefault(new StatisticKeys(templateTypeKeys.getValue()).perDomain(getSelectedDomain().getName()).build(), 0L);
				}
				templateTypeValues.get(templateTypeKeys.getKey()).add(type);
				all += type;
			}
			allDocumentsValues.add(all);
			dataLabels.add(dateToString(statisticDTO.getEntrydate(), "date"));
		}

		return Chart.initLineChart(valuesLists, dataSetLabels, dataSetColors, dataLabels, themeBean.getDarkMode());
	}

	/**
	 * get a line chart with historic qc types
	 * only available for single domain
	 *
	 * @return line chart
	 */
	public LineChart getQcHistoryChart()
	{
		Map<String, List<Number>> qcTypes = new LinkedHashMap<>();
		List<String> dataSetLabels = new ArrayList<>();
		List<String> dataSetColors = new ArrayList<>();
		List<String> dataLabels = new ArrayList<>();
		for (String qcType : Stream.concat(validQcTypes.stream(), invalidQcTypes.stream()).toList())
		{
			qcTypes.put(qcType, new ArrayList<>());
			dataSetLabels.add(getQcTypeLabel(qcType));
			dataSetColors.add(qcColors.get(qcType));
		}
		List<List<Number>> valuesLists = new ArrayList<>(qcTypes.values());
		
		if (!DashboardDomain.ALL.equals(domain))
		{
			for (StatisticDTO statisticDTO : Chart.reduceStatistic(rangeStats, 50))
			{
				for (Map.Entry<String, List<Number>> qcType : qcTypes.entrySet())
				{
					qcTypes.get(qcType.getKey())
							.add(statisticDTO.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(qcType.getKey()).build(), 0L));
				}

				dataLabels.add(dateToString(statisticDTO.getEntrydate(), "date"));
			}
		}
		return Chart.initLineChart(valuesLists, dataSetLabels, dataSetColors, dataLabels, themeBean.getDarkMode());
	}

	/**
	 * get a bar chart with monthly historic template types
	 * available for all domains and single domain
	 *
	 * @return bar chart
	 */
	public BarChart getDocumentsMonthChart()
	{
		List<String> dataSetLabels = getDocumentLabels(false);
		List<String> dataSetColors = getDocumentColors(false);
		List<String> dataSetTypes = getDocumentTypes(false);

		return getMonthChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.DOCUMENTS);
	}

	/**
	 * get a bar chart with yearly historic template types
	 * available for all domains and single domain
	 *
	 * @return bar chart
	 */
	public BarChart getDocumentsYearChart()
	{
		List<String> dataSetLabels = getDocumentLabels(false);
		List<String> dataSetColors = getDocumentColors(false);
		List<String> dataSetTypes = getDocumentTypes(false);

		return getYearChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.DOCUMENTS);
	}

	/**
	 * get a bar chart with monthly historic qc types
	 * only available for single domain
	 *
	 * @return bar chart
	 */
	public BarChart getQcMonthChart()
	{
		List<String> dataSetLabels = getQcLabels();
		List<String> dataSetColors = getQcColors();
		List<String> dataSetTypes = getQcTypes();

		return getMonthChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.QC);
	}

	/**
	 * get a bar chart with yearly historic qc types
	 * only available for single domain
	 *
	 * @return bar chart
	 */
	public BarChart getQcYearChart()
	{
		List<String> dataSetLabels = getQcLabels();
		List<String> dataSetColors = getQcColors();
		List<String> dataSetTypes = getQcTypes();

		return getYearChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.QC);
	}

	public BarChart getMonthChart(List<String> dataSetLabels, List<String> dataSetColors, List<String> dataSetTypes, BarStatType barStatType)
	{
		List<String> dataLabels = new ArrayList<>();

		Map<String, List<Number>> allValues = new LinkedHashMap<>();
		Map<String, Long> previousValues = new LinkedHashMap<>();
		Map<String, Long> currentValues = new LinkedHashMap<>();

		for (String type : dataSetTypes)
		{
			allValues.put(type, new ArrayList<>());
			previousValues.put(type, 0L);
			currentValues.put(type, 0L);
		}
		
		// get start date
		LocalDate start = rangeStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		// year*12 + month = startMonths
		int startMonths = start.getYear() * 12 + start.getMonthValue();

		// get end date
		LocalDate end = rangeEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		// year*12 + month = endMonths
		int endMonths = end.getYear() * 12 + end.getMonthValue();

		// for all months from start to end
		for (int months = startMonths; months < endMonths; months++)
		{
			// month and year
			int year = months / 12;
			int month = months % 12 + 1;

			// get stats of the month
			List<StatisticDTO> monthStats = rangeStats.stream().filter(s -> s.getEntrydate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == year
					&& s.getEntrydate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue() == month).toList();

			// get last stat of the month if any stats for the month exist
			StatisticDTO stat = null;
			if (!monthStats.isEmpty())
			{
				stat = monthStats.get(monthStats.size() - 1);
			}

			if (stat != null)
			{
				for (String type : dataSetTypes)
				{
					if (BarStatType.DOCUMENTS.equals(barStatType))
					{
						if (DashboardDomain.ALL.equals(domain))
						{
							currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(type).build(), 0L));
						}
						else
						{
							currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(type).perDomain(getSelectedDomain().getName()).build(), 0L));
						}
					}
					else if (BarStatType.QC.equals(barStatType))
					{
						if (!DashboardDomain.ALL.equals(domain))
						{
							currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(type).build(), 0L));
						}
					}
					allValues.get(type).add(currentValues.get(type) - previousValues.get(type));
					previousValues.put(type, currentValues.get(type));
				}
			}
			else
			{
				for (String type : dataSetTypes)
				{
					allValues.get(type).add(0L);
				}
			}
			dataLabels.add(year + "-" + month);
		}
		return Chart.initBarChart(allValues.values().stream().toList(), dataSetLabels, dataSetColors, dataLabels, false, themeBean.getDarkMode(), true);
	}

	public BarChart getYearChart(List<String> dataSetLabels, List<String> dataSetColors, List<String> dataSetTypes, BarStatType barStatType)
	{
		List<String> dataLabels = new ArrayList<>();

		Map<String, List<Number>> allValues = new LinkedHashMap<>();
		Map<String, Long> previousValues = new LinkedHashMap<>();
		Map<String, Long> currentValues = new LinkedHashMap<>();

		for (String type : dataSetTypes)
		{
			allValues.put(type, new ArrayList<>());
			previousValues.put(type, 0L);
			currentValues.put(type, 0L);
		}
		
		// get start date
		LocalDate start = rangeStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int startYear = start.getYear();

		// get end date
		LocalDate end = rangeEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		// year*12 + month = endMonths
		int endYear = end.getYear();

		// for all 12 previous months + year
		for (int year = startYear; year <= endYear; year++)
		{
			// get stats of the year
			int streamYear = year;

			// get stats of the month
			List<StatisticDTO> yearStats = rangeStats.stream().filter(s -> s.getEntrydate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == streamYear).toList();

			// get last stat of the year if any stats for the year exist
			StatisticDTO stat = null;
			if (!yearStats.isEmpty())
			{
				stat = yearStats.get(yearStats.size() - 1);
			}

			if (stat != null)
			{
				for (String type : dataSetTypes)
				{
					if (BarStatType.DOCUMENTS.equals(barStatType))
					{
						if (DashboardDomain.ALL.equals(domain))
						{
							currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(type).build(), 0L));
						}
						else
						{
							currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(type).perDomain(getSelectedDomain().getName()).build(), 0L));
						}
					}
					else if (BarStatType.QC.equals(barStatType))
					{
						if (!DashboardDomain.ALL.equals(domain))
						{
							currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(type).build(), 0L));
						}
					}
					allValues.get(type).add(currentValues.get(type) - previousValues.get(type));
					previousValues.put(type, currentValues.get(type));
				}
			}
			else
			{
				for (String type : dataSetTypes)
				{
					allValues.get(type).add(0L);
				}
			}
			dataLabels.add(String.valueOf(year));
		}
		return Chart.initBarChart(allValues.values().stream().toList(), dataSetLabels, dataSetColors, dataLabels, false, themeBean.getDarkMode(), true);
	}

	/* Downloads */
	public StreamedContent getLatestStatsDownload()
	{
		if (DashboardDomain.ALL.equals(domain))
		{
			return getLatestStatsAllDomainsDownload();
		}
		else
		{
			return getLatestStatsActiveDomainDownload();
		}
	}

	public StreamedContent getLatestStatsAllDomainsDownload()
	{
		Map<String, Number> valueMap = new LinkedHashMap<>();
		for (String key : getLatestStatsAllDomainsLabels(true).keySet())
		{
			valueMap.put(key, stats.getMappedStatValue().getOrDefault(key, 0L));
		}
		return getMapAsCsv(valueMap, stats.getEntrydate(), "all_domains stats latest");
	}

	public StreamedContent getHistoryStatsDownload()
	{
		if (DashboardDomain.ALL.equals(domain))
		{
			return getHistoryStatsAllDomainsDownload();
		}
		else
		{
			return getHistoryStatsActiveDomainDownload();
		}
	}

	public StreamedContent getHistoryStatsAllDomainsDownload()
	{
		return getHistoryStats(new ArrayList<>(getLatestStatsAllDomainsLabels(true).keySet()), "all_domains stats history");
	}

	public StreamedContent getLatestStatsActiveDomainDownload()
	{
		Map<String, Number> valueMap = new LinkedHashMap<>();
		for (String key : getLatestStatsActiveDomainLabels(true).keySet())
		{
			valueMap.put(key, stats.getMappedStatValue().getOrDefault(key, 0L));
		}
		return getMapAsCsv(valueMap, stats.getEntrydate(), getSelectedDomain().getName() + " stats latest");
	}

	public StreamedContent getHistoryStatsActiveDomainDownload()
	{
		return getHistoryStats(new ArrayList<>(getLatestStatsActiveDomainLabels(true).keySet()), getSelectedDomain().getName() + " stats history");
	}

	/* Private methods */
	public void loadStats()
	{
		allDocuments = 0L;
		// Look if any stats exist
		stats = getStatisticService().getLatestStats();
		hasStats = stats != null && stats.getMappedStatValue().containsKey(StatisticKeys.CALCULATION_TIME);

		if (hasStats)
		{
			// Set min and max date for stats
			statsMinDate = getStatisticService().getFirstStats().getEntrydate();
			statsMaxDate = stats.getEntrydate();

			// Get stats for custom date
			if (statsDate != null && !stats.getEntrydate().equals(statsDate))
			{
				List<StatisticDTO> historyForCustomStatsDate = getStatisticService().getStatsFromTo(new Date(0), statsDate);
				stats = historyForCustomStatsDate.get(historyForCustomStatsDate.size() - 1);
			}
			else
			{
				statsDate = stats.getEntrydate();
			}

			// Set range start date if not set
			rangeStartDate = rangeStartDate != null ? rangeStartDate : statsMinDate;

			// set range end date if not set or if range ends after custom statsDate
			rangeEndDate = rangeEndDate != null && !rangeEndDate.after(statsDate) ? rangeEndDate : statsDate;
		}
		if (getSelectedDomain() != null && stats != null)
		{
			for (Map.Entry<ConsentTemplateType, String> templateTypeKeys : getTemplateTypeStatisticKeys().entrySet())
			{
				if (DashboardDomain.ALL.equals(domain))
				{
					allDocuments += stats.getMappedStatValue().getOrDefault(new StatisticKeys(templateTypeKeys.getValue()).build(), 0L);
				}
				else
				{
					allDocuments += stats.getMappedStatValue().getOrDefault(new StatisticKeys(templateTypeKeys.getValue()).perDomain(getSelectedDomain().getName()).build(), 0L);
				}
			}
			try
			{
				domainPolicies = getService().listPolicies(getSelectedDomain().getName(), true);
				domainTemplates = getService().listConsentTemplates(getSelectedDomain().getName(), true);
			}
			catch (UnknownDomainException | InvalidVersionException | InvalidParameterException e)
			{
				logger.error(e.getLocalizedMessage());
			}

			validQcTypes = getSelectedDomain().getValidQcTypes();
			invalidQcTypes = getSelectedDomain().getInvalidQcTypes();
		}
		else
		{
			domainPolicies = new ArrayList<>();
			domainTemplates = new ArrayList<>();
			validQcTypes = new ArrayList<>();
			invalidQcTypes = new ArrayList<>();
		}
	}

	private void createQcColors()
	{
		// Generate colors
		qcColors = new HashMap<>();
		int i = 0;
		for (String type : validQcTypes)
		{
			int hue = 142 - 40 / validQcTypes.size() * i;
			int sat = 53 + 20 / validQcTypes.size() * i;
			int lum = 40 + 25 / validQcTypes.size() * i;
			qcColors.put(type, "hsl(" + hue + ", " + sat + "%, " + lum + "%)");
			i++;
		}
		i = 0;
		for (String type : invalidQcTypes)
		{
			int hue = 35 / invalidQcTypes.size() * i;
			int sat = 78 + 10 / validQcTypes.size() * i;
			int lum = 53 + 30 / validQcTypes.size() * i;
			qcColors.put(type, "hsl(" + (hue > 360 ? hue - 360 : hue) + ", " + sat + "%, " + lum + "%)");
			i++;
		}
	}

	private List<String> getDocumentLabels(boolean includeAll)
	{
		List<String> result = getTemplateTypeStatisticKeys().keySet().stream().map(t -> getBundle().getString("template.type.plural." + detailedTypes() + t.name()))
				.collect(Collectors.toList());
		if (includeAll)
		{
			result.add(0, getBundle().getString("template.type.plural.ALL"));
		}
		return result;
	}

	private List<String> getDocumentColors(boolean includeAll)
	{
		List<String> result = getTemplateTypeStatisticKeys().keySet().stream().map(t -> TemplateColor.valueOf(t.name()).color)
				.collect(Collectors.toList());

		if (includeAll)
		{
			result.add(0, "#7A7A7A");
		}
		return result;
	}

	private List<String> getDocumentTypes(boolean includeAll)
	{
		List<String> result = new ArrayList<>(getTemplateTypeStatisticKeys().values());
		if (includeAll)
		{
			result.add(0, StatisticKeys.DOCUMENTS);
		}
		return result;
	}

	private List<String> getQcLabels()
	{
		return Stream.concat(validQcTypes.stream(), invalidQcTypes.stream()).map(this::getQcTypeLabel).collect(Collectors.toList());
	}

	private List<String> getQcColors()
	{
		return Stream.concat(validQcTypes.stream(), invalidQcTypes.stream()).map(t -> qcColors.get(t)).collect(Collectors.toList());
	}

	private List<String> getQcTypes()
	{
		return Stream.concat(validQcTypes.stream(), invalidQcTypes.stream()).collect(Collectors.toList());
	}

	private StreamedContent getMapAsCsv(Map<String, Number> map, Date date, String details)
	{
		return File.get2DDataAsCsv(new ArrayList<>(map.values()), new ArrayList<>(map.keySet()), date, details, TOOL);
	}

	private StreamedContent getHistoryStats(List<String> keys, String details)
	{
		// Prepare lists
		List<String> dates = new ArrayList<>();
		Map<String, List<Object>> valueMap = new LinkedHashMap<>();
		for (String key : keys)
		{
			valueMap.put(key, new ArrayList<>());
		}

		// Fill lists
		for (StatisticDTO statisticDTO : rangeStats)
		{
			dates.add(dateToString(statisticDTO.getEntrydate(), "date"));
			for (Map.Entry<String, List<Object>> entry : valueMap.entrySet())
			{
				entry.getValue().add(statisticDTO.getMappedStatValue().getOrDefault(entry.getKey(), 0L));
			}
		}

		return File.get3DDataAsCSV(valueMap, dates, details, TOOL);
	}

	public StatisticDTO getStats()
	{
		return stats;
	}

	public String getLatestStatsDateTimeString()
	{
		if (stats.getEntrydate().toInstant().truncatedTo(ChronoUnit.DAYS).equals(new Date().toInstant().truncatedTo(ChronoUnit.DAYS)))
		{
			return getCommonBundle().getString("ui.date.today") + " " + getLatestStatsTimeString();
		}
		else
		{
			return dateToString(stats.getEntrydate(), "date") + " " + getLatestStatsTimeString();
		}
	}

	public int getPercentage(long value, long total)
	{
		return total == 0 ? 0 : (int) ((double) value / total * 100);
	}

	public int getQcPercentage()
	{
		long validQc = DashboardDomain.ALL.equals(domain) ?
				0L :
				stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC_VALID).perDomain(getSelectedDomain().getName()).build(), 0L);
		return getPercentage(validQc, allDocuments);
	}

	public long getQcChecked()
	{
		long checked = 0L;
		for (String qcType : Stream.concat(getSelectedDomain().getValidQcTypes().stream(), getSelectedDomain().getInvalidQcTypes().stream()).toList())
		{
			if (!qcType.equals(NOT_CHECKED))
			{
				checked += DashboardDomain.ALL.equals(domain) ?
						0L :
						stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(qcType).build(), 0L);
			}
		}
		return checked;
	}

	public int getQcCheckedPercentage()
	{
		return getPercentage(getQcChecked(), allDocuments);
	}

	public long getDocumentsWithScan()
	{
		return DashboardDomain.ALL.equals(domain) ?
				stats.getMappedStatValue().getOrDefault(StatisticKeys.DOCUMENTS_WITH_SCANS, 0L) :
				stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_SCANS).perDomain(getSelectedDomain().getName()).build(), 0L);
	}

	public int getDocumentsWithScanPercentage()
	{
		return getPercentage(getDocumentsWithScan(), allDocuments);
	}

	public long getDocumentsWithDigitalSignature()
	{
		return DashboardDomain.ALL.equals(domain) ?
				stats.getMappedStatValue().getOrDefault(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE, 0L) :
				stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE).perDomain(getSelectedDomain().getName()).build(), 0L);
	}

	public int getDocumentsWithDigitalSignaturePercentage()
	{
		return getPercentage(getDocumentsWithDigitalSignature(), allDocuments);
	}

	public long getDocumentsExpiredFull()
	{
		return DashboardDomain.ALL.equals(domain) ?
				stats.getMappedStatValue().getOrDefault(StatisticKeys.DOCUMENTS_EXPIRED_FULL, 0L) :
				stats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_EXPIRED_FULL).perDomain(getSelectedDomain().getName()).build(), 0L);
	}

	public int getDocumentsExpiredFullPercentage()
	{
		return getPercentage(getDocumentsExpiredFull(), allDocuments);
	}

	public long getAllDocuments()
	{
		return allDocuments;
	}

	public String getLatestStatsTimeString()
	{
		return dateToString(stats.getEntrydate(), "time");
	}

	public long getLatestStatsCalculationTime()
	{
		return stats.getMappedStatValue().getOrDefault(StatisticKeys.CALCULATION_TIME, -1L);
	}

	public boolean isHasStats()
	{
		return hasStats;
	}

	public boolean isHasSummaryStats()
	{
		return hasStats && stats != null && stats.containsSummary();
	}

	public boolean isHasStatsInTimespan()
	{
		return hasStats && stats != null;
	}

	/**
	 * Sets the managed property to color graphs according to selected theme
	 *
	 * @param themeBean
	 * 		web-common theme bean with information about darkmode/lightmode
	 */
	public void setThemeBean(ThemeBean themeBean)
	{
		this.themeBean = themeBean;
	}

	public boolean isIncludeUnknownPolicyStatus()
	{
		return includeUnknownPolicyStatus;
	}

	public void setIncludeUnknownPolicyStatus(boolean includeUnknownPolicyStatus)
	{
		this.includeUnknownPolicyStatus = includeUnknownPolicyStatus;
	}

	public int getSelectedSignerIdIndex()
	{
		return selectedSignerIdIndex;
	}

	public void setSelectedSignerIdIndex(int selectedSignerIdIndex)
	{
		this.selectedSignerIdIndex = selectedSignerIdIndex;
	}

	public List<Chart.BarScale> getAvailableBarScales()
	{
		return Arrays.asList(Chart.getAvailableBarScales());
	}

	public Chart.BarScale getDocumentsBarScale()
	{
		return documentsBarScale;
	}

	public void setDocumentsBarScale(Chart.BarScale documentsBarScale)
	{
		this.documentsBarScale = documentsBarScale;
	}

	public Chart.BarScale getQcBarScale()
	{
		return qcBarScale;
	}

	public void setQcBarScale(Chart.BarScale qcBarScale)
	{
		this.qcBarScale = qcBarScale;
	}

	public enum BarStatType
	{
		DOCUMENTS, QC
	}

	public DashboardDomain getDomain()
	{
		return domain;
	}

	public void setDomain(DashboardDomain domain)
	{
		this.domain = domain;
	}

	public DashboardDomain[] getAvailableDomains()
	{
		return DashboardDomain.values();
	}

	public Date getStatsDate()
	{
		return statsDate;
	}

	public void setStatsDate(Date statsDate)
	{
		this.statsDate = statsDate;
	}

	public Date getRangeStartDate()
	{
		return rangeStartDate;
	}

	public void setRangeStartDate(Date rangeStartDate)
	{
		this.rangeStartDate = rangeStartDate;
	}

	public Date getRangeEndDate()
	{
		return rangeEndDate;
	}

	public void setRangeEndDate(Date rangeEndDate)
	{
		this.rangeEndDate = rangeEndDate;
	}

	public Date getStatsMinDate()
	{
		return statsMinDate;
	}

	public Date getStatsMaxDate()
	{
		return statsMaxDate;
	}

	public boolean isRangeStatsLoaded()
	{
		return rangeStatsLoaded;
	}

	public enum DashboardDomain
	{
		ALL, CURRENT
	}

	public enum TemplateColor
	{
		CONSENT("#30AE69"), CONSENT_OPT_OUT("#30AE99"), REVOCATION("#E84949"), OBJECTION("#E85979"), REFUSAL("#60A4DF");

		private final String color;

		TemplateColor(String color)
		{
			this.color = color;
		}
	}
}
