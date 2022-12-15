package org.emau.icmvc.ganimed.ttp.cm2;

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

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.emau.icmvc.ganimed.ttp.cm2.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.StatisticException;

@WebService(name = "statisticService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(StatisticManager.class)
public class StatisticManagerBean implements StatisticManager
{
	private static final Logger logger = LogManager.getLogger(StatisticManagerBean.class);
	@EJB
	protected DAO dao;
	private boolean enableAutoUpdate = true;

	@Override
	public StatisticDTO getLatestStats()
	{
		logger.debug("call to getLatestStats");
		StatisticDTO result = dao.getLatestStats();
		if (logger.isDebugEnabled())
		{
			logger.debug("result of getLatestStats: " + result);
		}
		return result;
	}

	@Override
	public List<StatisticDTO> getAllStats()
	{
		logger.debug("call to getAllStats");
		List<StatisticDTO> result = dao.getAllStats();
		if (logger.isDebugEnabled())
		{
			logger.debug("number of results: " + result.size());
		}
		return result;
	}

	@Override
	public StatisticDTO updateStats() throws StatisticException
	{
		logger.debug("call to updateStats");
		StatisticDTO result = dao.updateStats();
		if (logger.isDebugEnabled())
		{
			logger.debug("result of updateStats: " + result);
		}
		return result;
	}

	@Override
	public void addStat(StatisticDTO statisticDTO)
	{
		if (logger.isDebugEnabled())
		{
			logger.info("call to addStat with " + statisticDTO);
		}
		dao.addStat(statisticDTO);
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
		logger.debug("Scheduling Mode enabled: " + enableAutoUpdate);
	}
}
