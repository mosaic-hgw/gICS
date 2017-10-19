package org.emau.icmvc.ganimed.ttp.cm2.factories;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
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

import org.apache.log4j.Logger;

public class ReflectionClassFactory {

	private static final Logger logger = Logger.getLogger(ReflectionClassFactory.class);
	private static final ReflectionClassFactory instance = new ReflectionClassFactory();
	private static final HashMap<String, Class<?>> classCache = new HashMap<String, Class<?>>();

	private ReflectionClassFactory() {
	}

	public static ReflectionClassFactory getInstance() {
		return instance;
	}

	public <T> Class<? extends T> getSubClass(String className, Class<T> superClass) throws ClassNotFoundException, ClassCastException {
		Class<?> temp = getClass(className);
		if (logger.isDebugEnabled()) {
			logger.debug("verifiing, that the result is a subclass of " + superClass.getName());
		}
		Class<? extends T> result;
		try {
			result = temp.asSubclass(superClass);
		} catch (ClassCastException e) {
			String message = "class '" + className + "' is not a subclass of '" + superClass.getName() + "'";
			logger.error(message, e);
			throw new ClassCastException(message);
		}
		return result;
	}

	public Class<?> getClass(String className) throws ClassNotFoundException {
		if (logger.isDebugEnabled()) {
			logger.debug("loading class '" + className + "'");
		}
		Class<?> result = classCache.get(className);
		if (result == null) {
			result = Class.forName(className);
			classCache.put(className, result);
		}
		return result;
	}
}
