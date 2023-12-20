package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.cm2.StatisticManager;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.StatisticException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.model.StatisticPolicy;
import org.emau.icmvc.ganimed.ttp.cm2.util.StatisticKeys;
import org.emau.icmvc.ttp.auth.TTPNames.Tool;
import org.icmvc.ttp.web.controller.ThemeBean;
import org.icmvc.ttp.web.util.Chart;
import org.icmvc.ttp.web.util.File;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

@ViewScoped
@ManagedBean(name = "dashboardController")
public class DashboardController extends AbstractGICSBean
{
	@EJB(lookup = "java:global/gics/cm2-ejb/StatisticManagerBean!org.emau.icmvc.ganimed.ttp.cm2.StatisticManager")
	private StatisticManager statisticServiceTarget;
	private StatisticManager statisticService;

	@ManagedProperty(value = "#{themeBean}")
	private ThemeBean themeBean;

	private Map<String, String> qcColors;
	private List<ConsentTemplateDTO> domainTemplates;
	private List<PolicyDTO> domainPolicies;
	private List<StatisticDTO> historyStats;
	private List<String> validQcTypes;
	private List<String> invalidQcTypes;
	private StatisticDTO latestStats;
	private long allDocuments = 0L;
	private Chart.BarScale documentsBarScale = Chart.BarScale.MONTHS_12;
	private Chart.BarScale qcBarScale = Chart.BarScale.MONTHS_12;

	private boolean includeUnknownPolicyStatus = false;

	private static final String CONSENT_COLOR = "hsl(129, 78%, 70%)";
	private static final String REVOCATION_COLOR = "hsl(0, 78%, 70%)";
	private static final String REFUSAL_COLOR = "hsl(208, 78%, 70%)";

	@PostConstruct
	public void init()
	{
		if (getWebAuthContext().isUsingDomainBasedRolesDisabled(Tool.gics))
		{
			statisticService = statisticServiceTarget;
		}
		else
		{
			statisticService = getWebAuthContext().createUpdateAuthContextProxy(statisticServiceTarget, StatisticManager.class);
		}

		loadStats();
		if (getInit())
		{
			createQcColors();
		}
	}

	public void updateStats()
	{
		try
		{
			statisticService.updateStats();
			init();
			logMessage(getCommonBundle().getString("page.dashboard.statistic.updated"), Severity.INFO);
		}
		catch (StatisticException e)
		{
			logMessage(e.getLocalizedMessage(), Severity.ERROR);
		}
	}

	/* Stats Overview */
	public Map<String, String> getLatestStatsAllDomainsLabels(boolean extended)
	{
		Map<String, String> result = new LinkedHashMap<>();
		result.put(StatisticKeys.CONSENTS, getBundle().getString("consent.title.type_CONSENT"));
		result.put(StatisticKeys.REVOCATIONS, getBundle().getString("consent.title.type_REVOCATION"));
		result.put(StatisticKeys.REFUSALS, getBundle().getString("consent.title.type_REFUSAL"));
		result.put(StatisticKeys.TEMPLATES, getBundle().getString("template.templates"));
		result.put(StatisticKeys.MODULES, getBundle().getString("module.modules"));
		if (extended)
		{
			result.put(StatisticKeys.MODULES_WITHOUT_VERSIONS, getBundle().getString("module.modulesWithoutVersions"));
			// result.put(StatisticKeys.SIGNED_POLICIES, getBundle().getString("page.dashboard.policies.signed"));
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
		result.put(new StatisticKeys(StatisticKeys.CONSENTS).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("consent.title.type_CONSENT"));
		result.put(new StatisticKeys(StatisticKeys.REVOCATIONS).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("consent.title.type_REVOCATION"));
		result.put(new StatisticKeys(StatisticKeys.REFUSALS).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("consent.title.type_REFUSAL"));
		result.put(new StatisticKeys(StatisticKeys.TEMPLATES).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("template.templates"));
		result.put(new StatisticKeys(StatisticKeys.MODULES).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("module.modules"));
		if (extended)
		{
			result.put(new StatisticKeys(StatisticKeys.MODULES_WITHOUT_VERSIONS).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("module.modulesWithoutVersions"));
			// result.put(new StatisticKeys(StatisticKeys.SIGNED_POLICIES).perDomain(getSelectedDomain().getName()).build(), getBundle().getString("page.dashboard.policies.signed"));
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

	/* Pie Charts */
	public PieChartModel getDocumentsChart(boolean mobile)
	{
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<String> colors = new ArrayList<>();

		values.add(latestStats.getMappedStatValue().get(new StatisticKeys(StatisticKeys.CONSENTS).perDomain(getSelectedDomain().getName()).build()));
		values.add(latestStats.getMappedStatValue().get(new StatisticKeys(StatisticKeys.REVOCATIONS).perDomain(getSelectedDomain().getName()).build()));
		values.add(latestStats.getMappedStatValue().get(new StatisticKeys(StatisticKeys.REFUSALS).perDomain(getSelectedDomain().getName()).build()));
		labels.add(getBundle().getString("consent.title.type_CONSENT"));
		labels.add(getBundle().getString("consent.title.type_REVOCATION"));
		labels.add(getBundle().getString("consent.title.type_REFUSAL"));
		colors.add(CONSENT_COLOR);
		colors.add(REVOCATION_COLOR);
		colors.add(REFUSAL_COLOR);

		return Chart.initPieChart(values, labels, colors, mobile ? "top" : "left", themeBean.getDarkMode());
	}

	public PieChartModel getQcChart(boolean mobile)
	{
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<String> colors = new ArrayList<>();

		for (String type : Stream.concat(validQcTypes.stream(), invalidQcTypes.stream()).collect(Collectors.toList()))
		{
			long value = latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(type).build(), 0L);
			values.add(value);
			labels.add(getQcTypeLabel(type));
			colors.add(qcColors.get(type));
		}

		return Chart.initPieChart(values, labels, colors, mobile ? "top" : "left", themeBean.getDarkMode());
	}

	/* Percentage bars */
	public List<StatisticPolicy> getPolicyStatistic(String signerIdType)
	{
		List<StatisticPolicy> statisticPolicies = new ArrayList<>();

		for (PolicyDTO policy : domainPolicies)
		{
			long accepted = latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.SIGNER_IDS)
					.perDomain(getSelectedDomain().getName())
					.perIdType(signerIdType)
					.perPolicy(policy.getKey())
					.perStatus(ConsentStatusType.ACCEPTED)
					.build(), 0L);
			long declined = latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.SIGNER_IDS)
					.perDomain(getSelectedDomain().getName())
					.perIdType(signerIdType)
					.perPolicy(policy.getKey())
					.perStatus(ConsentStatusType.DECLINED)
					.build(), 0L);

			long unknown = latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.SIGNER_IDS)
					.perDomain(getSelectedDomain().getName())
					.perIdType(signerIdType)
					.perPolicy(policy.getKey())
					.perStatus(ConsentStatusType.UNKNOWN)
					.build(), 0L);

			statisticPolicies.add(new StatisticPolicy(policy, accepted, declined, unknown));
		}

		return statisticPolicies.stream()
				.sorted(Comparator.comparing(StatisticPolicy::getAccepted).thenComparing(p -> p.getAll(includeUnknownPolicyStatus)).reversed())
				.collect(Collectors.toList());
	}

	public Map<ConsentTemplateDTO, Long> getTemplateStatistic()
	{
		Map<ConsentTemplateDTO, Long> result = new LinkedHashMap<>();

		for (ConsentTemplateDTO templateDTO : domainTemplates)
		{
			result.put(templateDTO, latestStats.getMappedStatValue()
					.getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS).perDomain(getSelectedDomain().getName()).perTemplate(templateDTO.getKey()).build(), 0L));
		}

		return result.entrySet().stream()
				.sorted(Map.Entry.<ConsentTemplateDTO, Long> comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	/* History Line Charts */
	public LineChartModel getDocumentsHistoryChart()
	{
		List<Object> allDocumentsValues = new ArrayList<>();
		List<Object> consentsValues = new ArrayList<>();
		List<Object> revocationsValues = new ArrayList<>();
		List<Object> refusalsValues = new ArrayList<>();
		List<String> dataLabels = new ArrayList<>();
		List<String> dataSetLabels = getDocumentLabels(true);
		List<String> dataSetColors = getDocumentColors(true);

		List<List<Object>> valuesLists = new ArrayList<>();
		valuesLists.add(allDocumentsValues);
		valuesLists.add(consentsValues);
		valuesLists.add(revocationsValues);
		valuesLists.add(refusalsValues);

		LineChartModel documentsHistoryChart = Chart.initLineChartModel(valuesLists, dataSetLabels, dataSetColors, dataLabels, themeBean.getDarkMode());

		for (StatisticDTO statisticDTO : Chart.reduceStatistic(historyStats, 50))
		{
			long consents = statisticDTO.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.CONSENTS).perDomain(getSelectedDomain().getName()).build(), 0L);
			long revocations = statisticDTO.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.REVOCATIONS).perDomain(getSelectedDomain().getName()).build(), 0L);
			long refusals = statisticDTO.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.REFUSALS).perDomain(getSelectedDomain().getName()).build(), 0L);

			allDocumentsValues.add(consents + revocations + refusals);
			consentsValues.add(consents);
			revocationsValues.add(revocations);
			refusalsValues.add(refusals);

			dataLabels.add(dateToString(statisticDTO.getEntrydate(), "date"));
		}

		return documentsHistoryChart;
	}

	public LineChartModel getQcHistoryChart()
	{
		Map<String, List<Object>> qcTypes = new LinkedHashMap<>();
		List<String> dataSetLabels = new ArrayList<>();
		List<String> dataSetColors = new ArrayList<>();
		List<String> dataLabels = new ArrayList<>();
		for (String qcType : Stream.concat(validQcTypes.stream(), invalidQcTypes.stream()).collect(Collectors.toList()))
		{
			qcTypes.put(qcType, new ArrayList<>());
			dataSetLabels.add(getQcTypeLabel(qcType));
			dataSetColors.add(qcColors.get(qcType));
		}
		List<List<Object>> valuesLists = new ArrayList<>(qcTypes.values());

		LineChartModel qcTypeHistoryChart = Chart.initLineChartModel(valuesLists, dataSetLabels, dataSetColors, dataLabels, themeBean.getDarkMode());

		for (StatisticDTO statisticDTO : Chart.reduceStatistic(historyStats, 50))
		{
			for (Map.Entry<String, List<Object>> qcType : qcTypes.entrySet())
			{
				qcTypes.get(qcType.getKey())
						.add(statisticDTO.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(qcType.getKey()).build(), 0L));
			}

			dataLabels.add(dateToString(statisticDTO.getEntrydate(), "date"));
		}

		return qcTypeHistoryChart;
	}

	/* Documents monthly increase bar chart */
	public BarChartModel getDocumentsMonthChart()
	{
		List<String> dataSetLabels = getDocumentLabels(false);
		List<String> dataSetColors = getDocumentColors(false);
		List<String> dataSetTypes = getDocumentTypes(false);

		return getMonthChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.DOCUMENTS);
	}

	/* Documents yearly increase bar chart */
	public BarChartModel getDocumentsYearChart()
	{
		List<String> dataSetLabels = getDocumentLabels(false);
		List<String> dataSetColors = getDocumentColors(false);
		List<String> dataSetTypes = getDocumentTypes(false);

		return getYearChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.DOCUMENTS);
	}

	/* QC monthly change bar chart */
	public BarChartModel getQcMonthChart()
	{
		List<String> dataSetLabels = getQcLabels();
		List<String> dataSetColors = getQcColors();
		List<String> dataSetTypes = getQcTypes();

		return getMonthChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.QC);
	}

	/* QC yearly change bar chart */
	public BarChartModel getQcYearChart()
	{
		List<String> dataSetLabels = getQcLabels();
		List<String> dataSetColors = getQcColors();
		List<String> dataSetTypes = getQcTypes();

		return getYearChart(dataSetLabels, dataSetColors, dataSetTypes, BarStatType.QC);
	}

	public BarChartModel getMonthChart(List<String> dataSetLabels, List<String> dataSetColors, List<String> dataSetTypes, BarStatType barStatType)
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

		BarChartModel barChart = Chart.initVerticalBarChart(allValues.values().stream().toList(), dataSetLabels, dataSetColors, dataLabels, false, themeBean.getDarkMode());

		// get current month + year
		LocalDate today = LocalDate.now();

		// year*12 + month = currentMonth
		int todayYearMonth = today.getYear() * 12 + today.getMonthValue();

		// for all 12 previous months + year
		for (int yearMonth = todayYearMonth - 12; yearMonth < todayYearMonth; yearMonth++)
		{
			// month and year
			int year = yearMonth / 12;
			int month = yearMonth % 12 + 1;

			// get stats of the month
			List<StatisticDTO> monthStats = historyStats.stream().filter(s -> s.getEntrydate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == year
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
						currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(type).perDomain(getSelectedDomain().getName()).build(), 0L));
					}
					else if (BarStatType.QC.equals(barStatType))
					{
						currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(type).build(), 0L));
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
		return barChart;
	}

	public BarChartModel getYearChart(List<String> dataSetLabels, List<String> dataSetColors, List<String> dataSetTypes, BarStatType barStatType)
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

		BarChartModel barChart = Chart.initVerticalBarChart(allValues.values().stream().toList(), dataSetLabels, dataSetColors, dataLabels, false, themeBean.getDarkMode());

		// get current month + year
		LocalDate today = LocalDate.now();
		int todayYear = today.getYear();

		// first year
		int firstYear = historyStats.get(0).getEntrydate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();

		// for all 12 previous months + year
		for (int year = firstYear; year <= todayYear; year++)
		{
			// get stats of the year
			int streamYear = year;

			// get stats of the month
			List<StatisticDTO> yearStats = historyStats.stream().filter(s -> s.getEntrydate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear() == streamYear).toList();

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
						currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(type).perDomain(getSelectedDomain().getName()).build(), 0L));
					}
					else if (BarStatType.QC.equals(barStatType))
					{
						currentValues.put(type, stat.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(type).build(), 0L));
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
		return barChart;
	}

	/* Downloads */
	public StreamedContent getLatestStatsAllDomains()
	{
		Map<String, Number> valueMap = new LinkedHashMap<>();
		for (String key : getLatestStatsAllDomainsLabels(true).keySet())
		{
			valueMap.put(key, latestStats.getMappedStatValue().getOrDefault(key, 0L));
		}
		return getMapAsCsv(valueMap, latestStats.getEntrydate(), "all_domains stats latest");
	}

	public StreamedContent getHistoryStatsAllDomains()
	{
		return getHistoryStats(new ArrayList<>(getLatestStatsAllDomainsLabels(true).keySet()), "all_domains stats history");
	}

	public StreamedContent getLatestStatsActiveDomain()
	{
		Map<String, Number> valueMap = new LinkedHashMap<>();
		for (String key : getLatestStatsActiveDomainLabels(true).keySet())
		{
			valueMap.put(key, latestStats.getMappedStatValue().getOrDefault(key, 0L));
		}
		return getMapAsCsv(valueMap, latestStats.getEntrydate(), getSelectedDomain().getName() + " stats latest");
	}

	public StreamedContent getHistoryStatsActiveDomain()
	{
		return getHistoryStats(new ArrayList<>(getLatestStatsActiveDomainLabels(true).keySet()), getSelectedDomain().getName() + " stats history");
	}

	/* Private methods */
	private void loadStats()
	{
		historyStats = statisticService.getAllStats();
		latestStats = statisticService.getLatestStats();
		if (getSelectedDomain() != null)
		{
			allDocuments = latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.CONSENTS).perDomain(getSelectedDomain().getName()).build(), 0L)
					+ latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.REVOCATIONS).perDomain(getSelectedDomain().getName()).build(), 0L)
					+ latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.REFUSALS).perDomain(getSelectedDomain().getName()).build(), 0L);
			try
			{
				domainPolicies = service.listPolicies(getSelectedDomain().getName(), true);
				domainTemplates = service.listConsentTemplates(getSelectedDomain().getName(), true);
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
			allDocuments = 0L;
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
			int hue = 120 + 80 / validQcTypes.size() * i;
			int sat = 83 - 10 / validQcTypes.size() * i;
			int lum = 72 - 15 / validQcTypes.size() * i;
			qcColors.put(type, "hsl(" + hue + ", " + sat + "%, " + lum + "%)");
			i++;
		}
		i = 0;
		for (String type : invalidQcTypes)
		{
			int hue = 345 + 35 / invalidQcTypes.size() * i;
			int sat = 78 - 10 / validQcTypes.size() * i;
			int lum = 70 - 20 / validQcTypes.size() * i;
			qcColors.put(type, "hsl(" + (hue > 360 ? hue - 360 : hue) + ", " + sat + "%, " + lum + "%)");
			i++;
		}
		logger.warn(qcColors.toString());
	}

	private List<String> getDocumentLabels(boolean includeAll)
	{
		List<String> result = new ArrayList<>(Arrays.asList(
				getBundle().getString("consent.title.type_CONSENT"),
				getBundle().getString("consent.title.type_REVOCATION"),
				getBundle().getString("consent.title.type_REFUSAL")));
		if (includeAll)
		{
			result.add(0, getBundle().getString("consent.title.type_ALL"));
		}
		return result;
	}

	private List<String> getDocumentColors(boolean includeAll)
	{
		List<String> result = new ArrayList<>(Arrays.asList(
				CONSENT_COLOR,
				REVOCATION_COLOR,
				REFUSAL_COLOR));
		if (includeAll)
		{
			result.add(0, "#7A7A7A");
		}
		return result;
	}

	private List<String> getDocumentTypes(boolean includeAll)
	{
		List<String> result = new ArrayList<>(Arrays.asList(
				StatisticKeys.CONSENTS,
				StatisticKeys.REVOCATIONS,
				StatisticKeys.REFUSALS));
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
		for (StatisticDTO statisticDTO : historyStats)
		{
			dates.add(dateToString(statisticDTO.getEntrydate(), "date"));
			for (Map.Entry<String, List<Object>> entry : valueMap.entrySet())
			{
				entry.getValue().add(statisticDTO.getMappedStatValue().getOrDefault(entry.getKey(), 0L));
			}
		}

		return File.get3DDataAsCSV(valueMap, dates, details, TOOL);
	}

	public StatisticDTO getLatestStats()
	{
		return latestStats;
	}

	public String getLatestStatsDate()
	{
		if (latestStats.getEntrydate().toInstant().truncatedTo(ChronoUnit.DAYS).equals(new Date().toInstant().truncatedTo(ChronoUnit.DAYS)))
		{
			return getCommonBundle().getString("ui.date.today");
		}
		else
		{
			return dateToString(latestStats.getEntrydate(), "date");
		}
	}

	public int getPercentage(long value, long total)
	{
		return total == 0 ? 0 : (int) ((double) value / total * 100);
	}

	public int getQcPercentage()
	{
		long validQc = latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC_VALID).perDomain(getSelectedDomain().getName()).build(), 0L);
		return getPercentage(validQc, allDocuments);
	}

	public long getQcChecked()
	{
		long checked = 0L;
		for (String qcType : Stream.concat(getSelectedDomain().getValidQcTypes().stream(), getSelectedDomain().getInvalidQcTypes().stream()).collect(Collectors.toList()))
		{
			if (!qcType.equals(NOT_CHECKED))
			{
				checked += latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.QC).perDomain(getSelectedDomain().getName()).perQcType(qcType).build(), 0L);
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
		return latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_SCANS).perDomain(getSelectedDomain().getName()).build(), 0L);
	}

	public int getDocumentsWithScanPercentage()
	{
		return getPercentage(getDocumentsWithScan(), allDocuments);
	}

	public long getDocumentsWithDigitalSignature()
	{
		return latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_WITH_DIGITAL_PATIENTSIGNATURE).perDomain(getSelectedDomain().getName()).build(), 0L);
	}

	public int getDocumentsWithDigitalSignaturePercentage()
	{
		return getPercentage(getDocumentsWithDigitalSignature(), allDocuments);
	}

	public long getDocumentsExpiredFull()
	{
		return latestStats.getMappedStatValue().getOrDefault(new StatisticKeys(StatisticKeys.DOCUMENTS_EXPIRED_FULL).perDomain(getSelectedDomain().getName()).build(), 0L);
	}

	public int getDocumentsExpiredFullPercentage()
	{
		return getPercentage(getDocumentsExpiredFull(), allDocuments);
	}

	public long getAllDocuments()
	{
		return allDocuments;
	}

	public String getLatestStatsTime()
	{
		return dateToString(latestStats.getEntrydate(), "time");
	}

	public long getLatestStatsCalculationTime()
	{
		return latestStats.getMappedStatValue().getOrDefault(StatisticKeys.CALCULATION_TIME, -1L);
	}

	/**
	 * LatestStats should have calculation time and stats for current domain
	 *
	 * @return true if init was successful
	 */
	public boolean getInit()
	{
		return latestStats != null && latestStats.getMappedStatValue().containsKey(StatisticKeys.CALCULATION_TIME)
				&& latestStats.getMappedStatValue().containsKey(new StatisticKeys(StatisticKeys.CONSENTS).perDomain(getSelectedDomainName()).build());
	}

	/**
	 * Sets the managed property to color graphs according to selected theme
	 *
	 * @param themeBean
	 *            web-common theme bean with information about darkmode/lightmode
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

	public Chart.BarScale[] getAvailableBarScales()
	{
		return Chart.BarScale.values();
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

	private enum BarStatType
	{
		DOCUMENTS, QC
	}
}
