package org.emau.icmvc.ganimed.ttp.cm2.internal;

import java.time.Period;
import java.util.Date;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpressionExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.servicebased.AbstractGicsTest;
import org.emau.icmvc.ganimed.ttp.cm2.util.ExpressionUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionExpirationPropertiesObjectTest extends AbstractGicsTest
{
	ExpressionUtils expressionUtils = ExpressionUtils.getInstance();

	@Test
	void getExpirationDateWithFixedDateBeforeExpression() throws RequirementsNotFullfilledException
	{
		// Arrange
		ExpressionExpirationPropertiesDTO expDto = new ExpressionExpirationPropertiesDTO();
		// Fixed expiration date in 1 day
		expDto.setFixedExpirationDate(expressionUtils.toDate(expressionUtils.toLocalDateTime(NOW).plusDays(1)));
		// Expiration expression in 2 days
		expDto.setExpirationExpression("utils.getDateFromTimestamp(" + NOW.getTime() + ").plusDays(2)");
		ExpressionExpirationPropertiesObject exp = new ExpressionExpirationPropertiesObject(expDto);

		// Act
		Date expirationResult = exp.getExpirationDateForConsentDate(NOW, null);

		// Assert
		Date expirationFromFixedDate = expDto.getFixedExpirationDate();
		assertEquals(expirationFromFixedDate, expirationResult);
	}

	@Test
	void getExpirationDateWithFixedDateAfterExpression() throws RequirementsNotFullfilledException
	{
		// Arrange
		ExpressionExpirationPropertiesDTO expDto = new ExpressionExpirationPropertiesDTO();
		// Fixed expiration date in 2 days
		expDto.setFixedExpirationDate(expressionUtils.toDate(expressionUtils.toLocalDateTime(NOW).plusDays(2)));
		// Expiration expression in 1 day
		expDto.setExpirationExpression("utils.getDateFromTimestamp(" + NOW.getTime() + ").plusDays(1)");
		ExpressionExpirationPropertiesObject exp = new ExpressionExpirationPropertiesObject(expDto);

		// Act
		Date expirationResult = exp.getExpirationDateForConsentDate(NOW, null);

		// Assert
		Date expirationFromExpression = expressionUtils.toDate(expressionUtils.toLocalDateTime(NOW).plusDays(1));
		assertEquals(expirationFromExpression, expirationResult);
	}

	@Test
	void getExpirationDateWithPeriodBeforeExpression() throws RequirementsNotFullfilledException
	{
		// Arrange
		ExpressionExpirationPropertiesDTO expDto = new ExpressionExpirationPropertiesDTO();
		// Expiration period in 1 day
		expDto.setValidPeriod(Period.ofDays(1));
		// Expiration expression in 2 days
		expDto.setExpirationExpression("utils.getDateFromTimestamp(" + NOW.getTime() + ").plusDays(2)");
		ExpressionExpirationPropertiesObject exp = new ExpressionExpirationPropertiesObject(expDto);

		// Act
		Date expirationResult = exp.getExpirationDateForConsentDate(NOW, null);

		// Assert
		Date expirationFromPeriod = expressionUtils.toDate(expressionUtils.toLocalDateTime(NOW).plusDays(1));
		assertEquals(expirationFromPeriod, expirationResult);
	}

	@Test
	void getExpirationDateWithPeriodAfterExpression() throws RequirementsNotFullfilledException
	{
		// Arrange
		ExpressionExpirationPropertiesDTO expDto = new ExpressionExpirationPropertiesDTO();
		// Expiration period in 2 days
		expDto.setValidPeriod(Period.ofDays(2));
		// Expiration expression in 1 day
		expDto.setExpirationExpression("utils.getDateFromTimestamp(" + NOW.getTime() + ").plusDays(1)");
		ExpressionExpirationPropertiesObject exp = new ExpressionExpirationPropertiesObject(expDto);

		// Act
		Date expirationResult = exp.getExpirationDateForConsentDate(NOW, null);

		// Assert
		Date expirationFromExpression = expressionUtils.toDate(expressionUtils.toLocalDateTime(NOW).plusDays(1));
		assertEquals(expirationFromExpression, expirationResult);
	}
}
