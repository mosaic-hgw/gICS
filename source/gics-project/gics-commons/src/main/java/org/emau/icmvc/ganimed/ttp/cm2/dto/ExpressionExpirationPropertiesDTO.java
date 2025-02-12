package org.emau.icmvc.ganimed.ttp.cm2.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.Period;
import java.util.Date;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ExpressionExpirationPropertiesDTO extends ExpirationPropertiesDTO implements Serializable
{
	@Serial
	private static final long serialVersionUID = -211698841563522337L;
	private String expirationExpression;

	public ExpressionExpirationPropertiesDTO()
	{
	}

	public ExpressionExpirationPropertiesDTO(Date fixedExpirationDate, Period validPeriod, String expression)
	{
		super(fixedExpirationDate, validPeriod);
		expirationExpression = expression;
	}

	public ExpressionExpirationPropertiesDTO(ExpressionExpirationPropertiesDTO dto)
	{
		this(dto.getFixedExpirationDate(), dto.getValidPeriod(), dto.getExpirationExpression());
	}

	public String getExpirationExpression()
	{
		return expirationExpression;
	}

	public void setExpirationExpression(String expirationExpression)
	{
		this.expirationExpression = expirationExpression;
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
		ExpressionExpirationPropertiesDTO that = (ExpressionExpirationPropertiesDTO) o;
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
		return "ExpressionExpirationPropertiesDTO [fixedExpirationDate=" + getFixedExpirationDate() + ", validPeriod=" + getValidPeriod() +
			   ", expirationExpression=" + getExpirationExpression() + "]";
	}
}
