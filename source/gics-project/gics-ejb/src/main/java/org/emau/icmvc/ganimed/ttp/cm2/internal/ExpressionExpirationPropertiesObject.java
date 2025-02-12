package org.emau.icmvc.ganimed.ttp.cm2.internal;

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
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpressionExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.ExpirationProperties;
import org.emau.icmvc.ganimed.ttp.cm2.util.ExpressionUtils;
import org.mvel2.MVEL;

public class ExpressionExpirationPropertiesObject extends ExpirationPropertiesObject implements Serializable
{
	private static final Logger LOGGER = LogManager.getLogger(ExpressionExpirationPropertiesObject.class);
	@Serial
	private static final long serialVersionUID = 175836570691445372L;
	private final String expirationExpression;

	public ExpressionExpirationPropertiesObject(String propertiesString) throws ParseException, DateTimeParseException
	{
		super(propertiesString);
		Properties properties = getProperties();
		String temp = (String) properties.get(ExpirationProperties.EXPIRATION_EXPRESSION.toString());
		if (StringUtils.isNotEmpty(temp))
		{
			expirationExpression = temp;
		}
		else
		{
			expirationExpression = null;
		}
	}

	public ExpressionExpirationPropertiesObject(ExpressionExpirationPropertiesDTO dto)
	{
		this(dto == null ? null : dto.getFixedExpirationDate(), dto == null ? null : dto.getValidPeriod(), dto == null ? null : dto.getExpirationExpression());
	}

	public ExpressionExpirationPropertiesObject(ExpirationPropertiesObject obj)
	{
		this(obj.getExpirationDate(), obj.getValidDuration(), null);
	}

	public ExpressionExpirationPropertiesObject(Date expirationDate, Period validDuration, String expirationExpression)
	{
		super(expirationDate, validDuration);
		this.expirationExpression = StringUtils.isNotEmpty(expirationExpression) ? expirationExpression : null;
	}

	public Date getExpirationDateForConsentDate(Date date, Map<String, String> metaData) throws RequirementsNotFullfilledException
	{
		LocalDateTime result = super.getExpirationDateForConsentDate(date).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		if (expirationExpression != null)
		{
			HashMap<String, Object> contextMap = new HashMap<>();
			contextMap.put("metaData", metaData == null ? Map.of() : metaData);
			contextMap.put("utils", ExpressionUtils.getInstance());
			Object exprResult;
			try
			{
				exprResult = MVEL.eval(expirationExpression, contextMap);
				if (exprResult instanceof LocalDateTime dt)
				{
					if (dt.isBefore(result))
						result = dt;
				}
				else
				{
					LOGGER.error("result of expirationExpression is not LocalDateTime, instead '{}'", exprResult.getClass().getName());
					throw new RequirementsNotFullfilledException("result of expirationExpression is not of type LocalDateTime");
				}
			}
			catch (Exception e)
			{
				throw new RequirementsNotFullfilledException(e.getMessage(), e);
			}
		}
		return Date.from(result.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Returns a normalized form of this object without the time part of the fixed expiration date
	 * as defined in {@link #toPropertiesString()}
	 *
	 * @return a normalized form of this object
	 */
	@Override
	public ExpressionExpirationPropertiesObject normalized()
	{
		try
		{
			return new ExpressionExpirationPropertiesObject(toPropertiesString());
		}
		catch (ParseException e)
		{
			// this never happens (except the code to format and parse is wrong)
			throw new RuntimeException(e);
		}
	}

	public ExpressionExpirationPropertiesObject createMergedExpirationProperties(ExpressionExpirationPropertiesDTO expiration, boolean finalised)
	{
		ExpirationPropertiesObject mergedExpirationProperties = super.createMergedExpirationProperties(expiration, finalised);
		// expression might change the expirationDate to a past date which can only be checked with metaData which is not available here (at this time)
		// ergo change of expression afterwards is not allowed at all
		if (!finalised)
			return new ExpressionExpirationPropertiesObject(mergedExpirationProperties.getExpirationDate(), mergedExpirationProperties.getValidDuration(), expiration.getExpirationExpression());
		return new ExpressionExpirationPropertiesObject(mergedExpirationProperties.getExpirationDate(), mergedExpirationProperties.getValidDuration(), expirationExpression);
	}

	@Override
	public String toPropertiesString()
	{
		String result = super.toPropertiesString();
		if (expirationExpression != null)
			result += ExpirationProperties.EXPIRATION_EXPRESSION + "=" + expirationExpression + ";";
		return result;
	}

	public String getExpirationExpression()
	{
		return expirationExpression;
	}

	@Override
	public ExpressionExpirationPropertiesDTO toDTO()
	{
		return new ExpressionExpirationPropertiesDTO(expirationDate, validDuration, expirationExpression);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		ExpressionExpirationPropertiesObject that = (ExpressionExpirationPropertiesObject) o;
		return Objects.equals(expirationExpression, that.expirationExpression);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), expirationExpression);
	}

	@Override
	public String toString()
	{
		return super.toString() + "; " + "expirationExpression='" + getExpirationExpression();
	}
}
