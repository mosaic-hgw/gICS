package org.emau.icmvc.ganimed.ttp.cm2.logging;

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
 * 							in 2014 as part of the research project "MOSAIC" (funded by the DFG HO 1937/2-1).
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * see https://www.lenar.io/logging-method-invocations-in-java-with-aspectj/
 */
@Aspect
public class AspectJLogger
{
	private static final Logger logger = LogManager.getLogger(AspectJLogger.class);

	@Around("execution(* *(..)) && @annotation(org.emau.icmvc.ganimed.ttp.cm2.logging.LogThat)")
	public Object logMethods(ProceedingJoinPoint jp) throws Throwable
	{
		String methodName = jp.getSignature().getName();
		logMethodInvocationAndParameters(jp);

		long startTime = System.currentTimeMillis();
		Object result = jp.proceed(jp.getArgs());
		long endTime = System.currentTimeMillis();

		if (logger.isInfoEnabled())
		{
			logger.info("execution time: " + (endTime - startTime) + "ms");
		}
		if (logger.isDebugEnabled())
		{
			logger.debug(methodName + " returns \n" + result.toString());
		}

		return result;
	}

	private void logMethodInvocationAndParameters(ProceedingJoinPoint jp)
	{
		if (logger.isInfoEnabled())
		{
			logger.info("accessing " + jp.getSignature().getDeclaringTypeName() + "." + jp.getSignature().getName());
		}
		if (logger.isDebugEnabled())
		{
			String[] argNames = ((MethodSignature) jp.getSignature()).getParameterNames();
			Object[] values = jp.getArgs();
			if (argNames.length != 0)
			{
				logger.debug("parameter:");
				for (int i = 0; i < argNames.length; i++)
				{
					logger.debug(argNames[i], values[i]);
				}
			}
		}
	}
}
