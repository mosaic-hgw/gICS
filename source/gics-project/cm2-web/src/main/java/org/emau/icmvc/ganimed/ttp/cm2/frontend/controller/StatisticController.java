package org.emau.icmvc.ganimed.ttp.cm2.frontend.controller;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.cm2.frontend.controller.common.AbstractGICSBean;
import org.emau.icmvc.ganimed.ttp.gstats.CommonStatisticBean;
import org.emau.icmvc.ganimed.ttp.gstats.StatisticManager;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

@ManagedBean(name = "statisticController")
@ViewScoped
/**
 * Controller Class to use statistic manager bean
 * 
 * @author Martin Bialke
 *
 */
public class StatisticController extends AbstractGICSBean implements Serializable
{
	private static final long serialVersionUID = -4389177830741354541L;

	@EJB(lookup = "java:global/gics/cm2-ejb/StatisticManagerBean!org.emau.icmvc.ganimed.ttp.gstats.StatisticManager")
	private StatisticManager statManager;

	private CommonStatisticBean lastStat;
	private List<CommonStatisticBean> allStat;
	private TreeMap<Long, CommonStatisticBean> sortedMapOfStats;

	private LineChartModel statSignedPolicies;
	private LineChartModel statConsentsWithdrawals;
	private String chartBase64;
	private String chartName;

	@PostConstruct
	public void init()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("initializing");
		}

		update();
		supressDuplicates();
		createLineModels();
		initLinearModels();
	}

	public StreamedContent getCsvDownloadFile()
	{
		ByteArrayInputStream resultStream = new ByteArrayInputStream(convertToCSV().getBytes());
		return new DefaultStreamedContent(resultStream, "text/csv", "gics_full_statistical_data.csv");
	}

	public CommonStatisticBean getLastStat()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("getLastStat()");
		}
		return lastStat;
	}

	public void update()
	{
		// invoke update
		statManager.updateStats();

		// getdata
		lastStat = statManager.getLatestStats();
		allStat = statManager.getAllStats();
	}

	public StreamedContent getChartDownloadFile()
	{
		ByteArrayInputStream resultStream = null;
		if (chartBase64.split(",").length > 1)
		{
			String encoded = chartBase64.split(",")[1];
			byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
			resultStream = new ByteArrayInputStream(decoded);
		}

		// ByteArrayInputStream resultStream = new ByteArrayInputStream(chartBase64.getBytes());
		return new DefaultStreamedContent(resultStream, "image/png", "gics_chart_" + chartName + ".png");
	}

	private void createLineModels()
	{

		statConsentsWithdrawals = new LineChartModel();
		statSignedPolicies = new LineChartModel();

		// signed policies line graph
		statSignedPolicies.setTitle(getBundle().getString("label.statistic.overviewSignedPolicies"));
		statSignedPolicies.setLegendPosition("e");
		Axis spYaxis = statSignedPolicies.getAxis(AxisType.Y);
		spYaxis.setMin(0);

		DateAxis spAxis = new DateAxis(getBundle().getString("label.statistic.legend.dates"));
		statSignedPolicies.getAxes().put(AxisType.X, spAxis);
		statSignedPolicies.setSeriesColors("58BA27");

		// consent withdrawal line graph
		statConsentsWithdrawals.setTitle(getBundle().getString("label.statistic.overviewConsensWithdrawals"));
		statConsentsWithdrawals.setLegendPosition("e");
		Axis consentYaxis = statConsentsWithdrawals.getAxis(AxisType.Y);
		consentYaxis.setMin(0);

		DateAxis consentAxis = new DateAxis(getBundle().getString("label.statistic.legend.dates"));
		statConsentsWithdrawals.getAxes().put(AxisType.X, consentAxis);
		statConsentsWithdrawals.setSeriesColors("FFCC33,F74A4A,333333");
	}

	/**
	 * keep only last entry of the date and first value occuring
	 * 
	 */
	private void supressDuplicates()
	{

		// allStat

		sortedMapOfStats = new TreeMap<>();

		// supress duplicate dates

		String lastDate = null;

		for (int i = allStat.size() - 1; i > 0; i--)
		{

			// keine doppelten in die map, stets nur den letzten eintrag eines datums
			CommonStatisticBean csb = allStat.get(i);
			String currentDate = csb.getEntrydate().substring(0, 10);
			if (!currentDate.equals(lastDate))
			{
				sortedMapOfStats.put(csb.getId(), csb);
			}
			lastDate = currentDate;
		}
	}

	private String convertToCSV()
	{

		Set<String> lastKeyset = allStat.get(allStat.size() - 1).getMappedStatValue().keySet();

		StringBuilder sb = new StringBuilder();
		// write header to string
		sb.append("date;");
		for (String key : lastKeyset)
		{
			sb.append(key);
			sb.append(";");
		}
		sb.append(System.getProperty("line.separator"));

		// anhand der id sortieren
		for (Entry<Long, CommonStatisticBean> entry : sortedMapOfStats.entrySet())
		{

			sb.append(entry.getValue().getEntrydate() + ";");

			for (String key : lastKeyset)
			{
				sb.append(entry.getValue().getMappedStatValue().get(key));
				sb.append(";");
			}

			sb.append(System.getProperty("line.separator"));
		}

		logger.info(sb.toString());

		return sb.toString();
	}

	private void initLinearModels()
	{

		// signed pol line graph
		LineChartSeries spSeries = new LineChartSeries();
		spSeries.setLabel(getBundle().getString("label.statistic.legend.numberOfSignedPolicies"));

		// consent
		LineChartSeries consentSeries = new LineChartSeries();
		consentSeries.setLabel(getBundle().getString("label.statistic.legend.numberOfConsents"));

		// withdrawal
		LineChartSeries withdrawalSeries = new LineChartSeries();
		withdrawalSeries.setLabel(getBundle().getString("label.statistic.legend.numberOfWithdrawals"));

		// refusals
		LineChartSeries refusalSeries = new LineChartSeries();
		refusalSeries.setLabel(getBundle().getString("label.statistic.legend.numberOfRefusals"));

		// prevent duplicate data points based on values

		// store last value for each graph
		int lastSignedPol = -1;
		int lastConsent = -1;
		int lastWithdrawal = -1;
		int lastRefusal = -1;

		for (Entry<Long, CommonStatisticBean> entry : sortedMapOfStats.entrySet())
		{

			String date = entry.getValue().getEntrydate();

			Map<String, String> current_entry = entry.getValue().getMappedStatValue();

			int currentSignedPol = Integer.parseInt(current_entry.get("signed_policies"));
			int currentIC = Integer.parseInt(current_entry.get("informed_consents"));
			int currentWithdrawal = Integer.parseInt(current_entry.get("withdrawals"));
			//TODO Kommentar weg wenn Prozedur fertig
//			int currentRefusal = Integer.parseInt(current_entry.get("refusals"));
			int currentRefusal = 0;

			if (lastSignedPol != currentSignedPol)
			{
				spSeries.set(date, currentSignedPol);
				lastSignedPol = currentSignedPol;
			}

			if (lastConsent != currentIC)
			{
				consentSeries.set(date, currentIC);
				lastConsent = currentIC;
			}

			if (lastWithdrawal != currentWithdrawal)
			{
				withdrawalSeries.set(date, currentWithdrawal);
				lastWithdrawal = currentWithdrawal;
			}

			if (lastRefusal != currentRefusal)
			{
				refusalSeries.set(date, currentRefusal);
				lastRefusal = currentRefusal;
			}
		}

		// add multiline graph
		statConsentsWithdrawals.addSeries(consentSeries);
		statConsentsWithdrawals.addSeries(withdrawalSeries);
		//TODO wieder einbauen
//		statConsentsWithdrawals.addSeries(refusalSeries);

		// add single line graph
		statSignedPolicies.addSeries(spSeries);
	}

	public LineChartModel getStatConsentsWithdrawals()
	{
		return statConsentsWithdrawals;
	}

	public LineChartModel getStatSignedPolicies()
	{
		return statSignedPolicies;
	}

	public String getChartBase64()
	{
		return chartBase64;
	}

	public void setChartBase64(String chartBase64)
	{
		this.chartBase64 = chartBase64;
	}

	public void setChartName(String chartName)
	{
		this.chartName = chartName;
	}
}
