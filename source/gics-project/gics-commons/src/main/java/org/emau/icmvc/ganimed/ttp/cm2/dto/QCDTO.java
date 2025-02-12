package org.emau.icmvc.ganimed.ttp.cm2.dto;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.QualityControlConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.QCProblemStatusType;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;

/**
 * qualitaetskontrolleintrag fuer einen consent
 *
 * @author geidell
 */
public class QCDTO extends FhirIdDTO implements Serializable
{
	@Serial
	private static final long serialVersionUID = -6997907750129296193L;
	public static final String AUTO_GENERATED = "###_auto_generated_###";
	public static final String EMPTY_STRING = "";

	private boolean qcPassed = true;
	private String type;
	private final List<QCProblemDTO> problems = new ArrayList<>();
	private Date date;
	private String inspector;
	private String comment;
	private String externProperties;

	public QCDTO()
	{
		super(null);
	}

	/**
	 * Copy constructor.
	 *
	 * @param dto
	 * 		the qc to copy
	 */
	public QCDTO(QCDTO dto)
	{
		this(dto.isQcPassed(), dto.getType(), dto.getDate(), dto.getInspector(), dto.getComment(), dto.getExternProperties(), dto.getProblems(), dto.getFhirID());
	}

	public QCDTO(boolean qcPassed, String type, Date date, String inspector, String comment, String externProperties, List<QCProblemDTO> problems, String fhirID)
	{
		super(fhirID);
		this.qcPassed = qcPassed;
		this.type = type;
		this.date = date;
		this.inspector = inspector;
		this.comment = comment;
		this.externProperties = externProperties;
		setProblems(problems);
	}

	/**
	 * is read-only because it will be set according to the type and domain-config
	 */
	public boolean isQcPassed()
	{
		return qcPassed;
	}

	public String getType()
	{
		return type;
	}

	public QCType getType(QualityControlConfig config)
	{
		return config.getTypeById(getType());
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public List<QCProblemDTO> getProblemsWithStatusType(String problemStatusType)
	{
		return problems.stream().filter(p -> QCProblemStatusType.valueOf(problemStatusType).equals(p.getStatus().getType())).collect(Collectors.toList());
	}

	public List<QCProblemDTO> getProblems()
	{
		return problems;
	}

	public void setProblems(List<QCProblemDTO> problems)
	{
		try
		{
			validate(null, "qc.problems", problems);
		}
		catch (InvalidParameterException e)
		{
			throw new RuntimeException(e);
		}
		this.problems.clear();
		if (problems != null)
		{
			this.problems.addAll(problems.stream().map(QCProblemDTO::new).toList());
		}
	}

	public QCProblemDTO getProblemByTypeRef(String type, String ref)
	{
		return getProblemByTypeRef(new QCProblemDTO.TypeRef(type, ref));
	}

	public QCProblemDTO getProblemByTypeRef(QCProblemDTO.TypeRef typeRef)
	{
		return getProblems().stream().filter(p -> p.getTypeRef().equals(typeRef)).findFirst().orElse(null);
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		if (date != null)
		{
			this.date = new Date(date.getTime());
		}
		else
		{
			this.date = null;
		}
	}

	public String getInspector()
	{
		if (inspector != null)
		{
			return inspector;
		}
		else
		{
			return EMPTY_STRING;
		}
	}

	public void setInspector(String inspector)
	{
		this.inspector = inspector;
	}

	public String getComment()
	{
		if (comment != null)
		{
			return comment;
		}
		else
		{
			return EMPTY_STRING;
		}
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public void setExternProperties(String externProperties)
	{
		this.externProperties = externProperties;
	}

	/**
	 * @see #validate(QualityControlConfig, String)
	 */
	public void validate(QualityControlConfig config) throws InvalidParameterException
	{
		validate(config, null);
	}

	/**
	 * Assert the consistency of the list of qc problems, which means
	 *
	 * <ul>
	 *     <li>that there are no duplicate type-ref pairs in list of problems ({@link #getProblems()}) and</li>
	 *     <li>that all referred qc problem types ({@link QCProblemDTO#getType()}) from the list of problems are configured.</li>
	 * </ul>
	 *
	 * @param paramName
	 * 		an optional name of the parameter
	 * @throws InvalidParameterException
	 * 		if the qc config is not consistent
	 */
	public void validate(QualityControlConfig config, String paramName) throws InvalidParameterException
	{
		validate(config, paramName, getProblems());
	}

	private static void validate(QualityControlConfig config, String paramName, Collection<QCProblemDTO> problems) throws InvalidParameterException
	{
		if (problems == null || problems.isEmpty())
		{
			return;
		}

		String msgPrefix = StringUtils.isBlank(paramName) ? "" : paramName + ": ";
		paramName = StringUtils.isBlank(paramName) ? "unknown" : paramName;

		// assert that there are no duplicate type-ref pairs in list of problems
		Set<QCProblemDTO.TypeRef> tmp = new HashSet<>();
		Set<QCProblemDTO.TypeRef> duplicateTypeRefs = problems.stream().map(QCProblemDTO::getTypeRef)
				.filter(typeref -> !tmp.add(typeref)).collect(Collectors.toSet());
		if (!duplicateTypeRefs.isEmpty())
		{
			throw new InvalidParameterException(paramName, msgPrefix + "the list of qc problems contains duplicate type-ref-pairs "
					+ duplicateTypeRefs);
		}

		if (config != null)
		{
			// assert that all referred qc problem types are configured
			List<String> unconfiguredProblemTypes = problems.stream().map(QCProblemDTO::getType).distinct()
					.filter(ptid -> config.getProblemTypeById(ptid) == null).toList();
			if (!unconfiguredProblemTypes.isEmpty())
			{
				throw new InvalidParameterException(paramName, msgPrefix + "the list of qc problems is using unconfigured types: "
						+ unconfiguredProblemTypes);
			}
		}
	}


	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QCDTO qcdto))
			return false;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(getType(), qcdto.getType())
				.append(isQcPassed(), qcdto.isQcPassed())
				.append(getProblems(), qcdto.getProblems())
				.append(getDate(), qcdto.getDate())
				.append(getInspector(), qcdto.getInspector())
				.append(getComment(), qcdto.getComment())
				.append(getExternProperties(), qcdto.getExternProperties())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(getType())
				.append(isQcPassed())
				.append(getProblems())
				.append(getDate())
				.append(getInspector())
				.append(getComment())
				.append(getExternProperties()).toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this)
				.append("type", type)
				.append("qcPassed", qcPassed)
				.append("problems", problems)
				.append("date", date)
				.append("inspector", inspector)
				.append("comment", comment)
				.append("externProperties", externProperties)
				.appendSuper(super.toString())
				.toString();
	}
}
