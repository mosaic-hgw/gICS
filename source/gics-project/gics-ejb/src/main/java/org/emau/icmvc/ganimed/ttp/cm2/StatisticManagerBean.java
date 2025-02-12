package org.emau.icmvc.ganimed.ttp.cm2;

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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ejb.Remote;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.StatisticException;
import org.emau.icmvc.ganimed.ttp.cm2.util.StatisticKeys;

@WebService(name = "statisticService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(StatisticManager.class)
public class StatisticManagerBean extends AbstractGicsServiceBase implements StatisticManager
{
	private boolean enableAutoUpdate = true;

	@Override
	public StatisticDTO getFirstStats()
	{
		return filterAllowedStatisticDomains(dad.getFirstStats());
	}
	
	@Override
	public StatisticDTO getLatestStats()
	{
		return filterAllowedStatisticDomains(dad.getLatestStats());
	}

	@Override
	public List<StatisticDTO> getAllStats()
	{
		return filterAllowedStatisticDomains(dad.getAllStats());
	}

	@Override
	public List<StatisticDTO> getStatsFromTo(Date from, Date to)
	{
		return filterAllowedStatisticDomains(dad.getStatsFromTo(from, to));
	}

	@Override
	public StatisticDTO updateStats() throws StatisticException
	{
		logger.debug("call to updateStats");
		StatisticDTO result = dad.updateStats();
		if (logger.isDebugEnabled())
		{
			logger.debug("result of updateStats: " + result);
		}
		return filterAllowedStatisticDomains(result);
	}

	@Override
	public void addStat(StatisticDTO statisticDTO)
	{
		if (logger.isDebugEnabled())
		{
			logger.info("call to addStat with " + statisticDTO);
		}
		dad.addStat(statisticDTO);
		if (logger.isDebugEnabled())
		{
			logger.info("stat for " + statisticDTO + " added");
		}
	}

	@Schedule(second = "0", minute = "0", hour = "4")
	public void autoUpdate()
	{
		if (enableAutoUpdate)
		{
			logger.debug("Scheduled execution of updateStats.");
			try
			{
				updateStats();
			}
			catch (StatisticException e)
			{
				logger.error(e.getLocalizedMessage());
			}
		}
		else
		{
			logger.debug("Scheduling execution of updateStats skipped because autoUpdate is disabled.");
		}
	}

	@Override
	public void enableScheduling(boolean status)
	{
		this.enableAutoUpdate = status;
		logger.debug("scheduling mode enabled: " + enableAutoUpdate);
	}

	/**
	 * {@return A list of statistic DTOs with filtered map entries}.
	 * See {@link #filterAllowedStatisticDomains(StatisticDTO)} for more detail on filtering.
	 *
	 * @param statisticDTOs
	 * 		the statistic DTOs to filter
	 */
	protected List<StatisticDTO> filterAllowedStatisticDomains(List<StatisticDTO> statisticDTOs)
	{
		FilterInfo info = new FilterInfo();
		return statisticDTOs.stream().map(s -> filterAllowedStatisticDomains(s, info)).collect(Collectors.toList());
	}

	/**
	 * If the current auth context indicates, that authorization with domain-based roles is activated,
	 * and if the currently permitted roles in this context effectively deny access to at least one of
	 * the domains of this E-Pix instance, then this method will return a new statistics DTO which only
	 * contains the statistic keys referring to allowed domains. Otherwise, the provided statistics DTO
	 * will be returned directly.
	 *
	 * @param statisticDTO
	 * 		the statistic DTO to filter
	 * @return if necessary the filtered statistics or the provided statistics otherwise
	 */
	protected StatisticDTO filterAllowedStatisticDomains(StatisticDTO statisticDTO)
	{
		return filterAllowedStatisticDomains(statisticDTO, new FilterInfo());
	}

	private StatisticDTO filterAllowedStatisticDomains(StatisticDTO statisticDTO, FilterInfo info)
	{
		FilterInfo finalInfo = info != null ? info : new FilterInfo();

		if (finalInfo.isDenyingDomains())
		{
			logger.trace("applying allowed domain filter {} to {}", info, statisticDTO);
			Map<String, Long> stats = statisticDTO.getMappedStatValue();
			Set<String> visitedDomains = new HashSet<>();
			Map<String, Long> allowedStats = stats.entrySet().stream()
					.filter(e -> isAllowedKey(e.getKey(), finalInfo.getAllowedDomains(), visitedDomains))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k2, HashMap::new));
			allowedStats.put(StatisticKeys.CALCULATION_TIME, stats.get(StatisticKeys.CALCULATION_TIME));
			allowedStats.put(StatisticKeys.DOMAINS, (long) visitedDomains.size());
			statisticDTO = new StatisticDTO(statisticDTO.getId(), statisticDTO.getEntrydate(), allowedStats);
			logger.trace("filtered statistics {}", statisticDTO);
		}

		return statisticDTO;
	}

	private boolean isAllowedKey(String statKey, Set<String> allowedDomains, Set<String> visitedDomains)
	{
		if (statKey.contains(StatisticKeys.PER_DOMAIN))
		{
			String domain = StringUtils.substringBetween(statKey, StatisticKeys.PER_DOMAIN, ".");

			if (domain == null)
			{
				domain = StringUtils.substringAfter(statKey, StatisticKeys.PER_DOMAIN);
			}

			if (allowedDomains.contains(domain))
			{
				visitedDomains.add(domain);
				return true;
			}
		}
		return false;
	}

	private class FilterInfo extends AllowedDomainsFilterInfo
	{
		private static final boolean TESTING = false;

		public FilterInfo()
		{
			super(() -> dad.listDomains().stream().map(DomainDTO::getName).toList());
		}

		@Override
		public Set<String> getAllDomains()
		{
			return TESTING ? Set.of("Demo Deutschland", "Demo EU", "MII") : super.getAllDomains();
		}

		@Override
		public Set<String> getAllowedDomains()
		{
			return TESTING ? Set.of("Demo Deutschland", "Demo EU") : super.getAllowedDomains();
		}

		@Override
		public boolean isUsingDomainBasedRoles()
		{
			return TESTING || super.isUsingDomainBasedRoles();
		}

		@Override
		public boolean isDenyingDomains()
		{
			return TESTING || super.isDenyingDomains();
		}
	}
}
