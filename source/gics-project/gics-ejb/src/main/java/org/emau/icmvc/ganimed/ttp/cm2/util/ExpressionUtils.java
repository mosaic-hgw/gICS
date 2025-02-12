package org.emau.icmvc.ganimed.ttp.cm2.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;

public final class ExpressionUtils
{
	private static final ExpressionUtils instance = new ExpressionUtils();
	private static final Cache<String, Pattern> regexPatterns = CacheBuilder.newBuilder().expireAfterWrite(6, TimeUnit.HOURS).build();


	private ExpressionUtils()
	{
	}

	public static ExpressionUtils getInstance()
	{
		return instance;
	}

	public LocalDateTime getNow()
	{
		return LocalDateTime.now();
	}
	
	public LocalDateTime getDateFromTimestamp(long timestamp)
	{
		return toLocalDateTime(new Date(timestamp));
	}

	public LocalDateTime getDate(String value)
	{
		List<String> datePatterns = Arrays.asList("yyyy-MM-dd", "dd.MM.yyyy");

		for (String pattern : datePatterns)
		{
			try
			{
				return DateTimeFormatter.ofPattern(pattern).parse(value, LocalDate::from).atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime();
			}
			catch (Exception e)
			{
				// try next pattern
			}
		}
		return null;
	}

	public LocalDateTime getLocalDateTimeFromString(String value, String format)
	{
		return DateTimeFormatter.ofPattern(format).parse(value, LocalDateTime::from).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public Date getDateFromString(String value, String format)
	{
		return Date.from(DateTimeFormatter.ofPattern(format).parse(value, LocalDateTime::from).atZone(ZoneId.systemDefault()).toInstant());
	}

	public boolean isOneValueOfMultiple(String v, String... compares)
	{
		if (StringUtils.isEmpty(v))
			return false;
		List<String> values = Arrays.asList(v.split(";"));
		for (String compare : compares)
		{
			if (values.contains(compare))
				return true;
		}
		return false;
	}

	public boolean isAllValuesOfMultiple(String v, String... compares)
	{
		if (StringUtils.isEmpty(v))
			return false;
		List<String> values = Arrays.asList(v.split(";"));
		for (String compare : compares)
		{
			if (!values.contains(compare))
				return false;
		}
		return true;
	}

	public Date toDate(LocalDateTime d)
	{
		return Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
	}

	public LocalDateTime toLocalDateTime(Date d)
	{
		return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
	}

	public LocalDate toLocalDate(Date d)
	{
		return LocalDate.ofInstant(d.toInstant(), ZoneId.systemDefault());
	}

	public boolean isOlderThenMin(LocalDateTime compare, int minutes)
	{
		return Duration.between(compare, LocalDateTime.now()).getSeconds() / 60 > minutes;
	}

	public long age(Date date)
	{
		if (date != null)
		{
			LocalDate localDate = toLocalDate(date);
			return ChronoUnit.YEARS.between(localDate, LocalDate.now());
		}
		return Long.MIN_VALUE;
	}

	public Matcher getMatcher(String value, String pattern)
	{
		Pattern patternObj = regexPatterns.getIfPresent(pattern);
		if (patternObj == null)
		{
			patternObj = Pattern.compile(pattern);
			regexPatterns.put(pattern, patternObj);
		}
		return patternObj.matcher(value);
	}

	public boolean matchesRegex(String value, String pattern)
	{
		return getMatcher(value, pattern).matches();
	}

	public boolean matchesRegexAtLeastOne(String value, String pattern)
	{
		return getMatcher(value, pattern).find();
	}
}
