package org.emau.icmvc.ganimed.ttp.cm2.factories;

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


import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ReflectionClassFactory
{
	private static final Logger LOGGER = LogManager.getLogger(ReflectionClassFactory.class);
	private static final HashMap<String, Class<?>> CLASS_CACHE = new HashMap<String, Class<?>>();

	private ReflectionClassFactory()
	{}

	public static <T> Class<? extends T> getSubClass(String className, Class<T> superClass) throws ClassNotFoundException, ClassCastException
	{
		Class<?> temp = getClass(className);
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("verifiing, that the result is a subclass of " + superClass.getName());
		}
		Class<? extends T> result;
		try
		{
			result = temp.asSubclass(superClass);
		}
		catch (ClassCastException e)
		{
			String message = "class '" + className + "' is not a subclass of '" + superClass.getName() + "'";
			LOGGER.error(message, e);
			throw new ClassCastException(message);
		}
		return result;
	}

	public static Class<?> getClass(String className) throws ClassNotFoundException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("loading class '" + className + "'");
		}
		Class<?> result = CLASS_CACHE.get(className);
		if (result == null)
		{
			result = Class.forName(className);
			CLASS_CACHE.put(className, result);
		}
		return result;
	}
}
