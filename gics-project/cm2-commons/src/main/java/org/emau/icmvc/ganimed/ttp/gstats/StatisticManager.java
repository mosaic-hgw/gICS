package org.emau.icmvc.ganimed.ttp.gstats;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author bialkem
 * 
 *         Interface for Statistic service to get statistic data from project specific db regarding the associated persistence unit
 * 
 *         JNDI Only
 * 
 */

@WebService
public interface StatisticManager {

	/**
	 * get last generated statistic
	 * 
	 * @return last generated stat
	 */
	public CommonStatisticBean getLatestStats();

	/**
	 * get all generated statistics
	 * 
	 * @return list of stats
	 */
	public List<CommonStatisticBean> getAllStats();

	/**
	 * call updateStats procedure in gpas database
	 */
	public void updateStats();
	
	/**
	 * enable or disable scheduled call of update stats at 8am daily
	 * @param status default is true,set status=false to disable scheduling mode 
	 */
	public void enableScheduling(@XmlElement(required = true) @WebParam(name = "status")boolean status);
}
