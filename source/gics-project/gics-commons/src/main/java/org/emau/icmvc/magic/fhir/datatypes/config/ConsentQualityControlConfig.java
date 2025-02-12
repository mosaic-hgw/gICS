package org.emau.icmvc.magic.fhir.datatypes.config;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.config.QualityControlConfig;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidExchangeFormatException;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;
import org.emau.icmvc.ttp.util.SneakyThrowUtils;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

@DatatypeDef(name = "ConsentQualityControlConfig")
public class ConsentQualityControlConfig extends Type implements ICompositeType
{
	@Serial
	private static final long serialVersionUID = 2582864026666598871L;

	@Child(name = "types", order = 0, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "list of qc types")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private List<ConsentQCType> types = new ArrayList<>();
	@Child(name = "defaultType", order = 2)
	@Description(shortDefinition = "the default QC type")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private StringDt defaultType = new StringDt();
	@Child(name = "problemTypes", order = 3, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "QC problem types")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private List<ConsentQCProblemType> problemTypes = new ArrayList<>();
	@Child(name = "problemTypeActions", order = 4, max = Child.MAX_UNLIMITED)
	@Description(shortDefinition = "actions for QC problem types")
	@SuppressWarnings("FieldMayBeFinal") // not final for deserialization
	private List<ConsentQCProblemTypeAction> problemTypeActions = new ArrayList<>();

	public ConsentQualityControlConfig()
	{
	}

	public ConsentQualityControlConfig(QualityControlConfig config)
	{
		if (config == null)
		{
			config = new QualityControlConfig();
		}
		setTypes(config.getTypes().stream().map(ConsentQCType::new).collect(Collectors.toSet()));
		setDefaultType(config.getDefaultTypeId());
		setProblemTypes(config.getProblemTypes().stream().map(ConsentQCProblemType::new).collect(Collectors.toSet()));
		setProblemTypeActions(config.getProblemTypeActions().stream().map(ConsentQCProblemTypeAction::new).collect(Collectors.toSet()));
	}

	public QualityControlConfig toQualityControlConfig() throws InvalidExchangeFormatException
	{
		SneakyThrowUtils.notSneaky(InvalidExchangeFormatException.class);
		return new QualityControlConfig(
				types.stream().map(ConsentQCType::toQCType).collect(Collectors.toSet()),
				getDefaultType(),
				problemTypes.stream().map(SneakyThrowUtils.sneakyF(t -> t.toQCProblemType(getProblemTypeActions()))).collect(Collectors.toSet()),
				problemTypeActions.stream().map(ConsentQCProblemTypeAction::toQCProblemTypeAction).collect(Collectors.toSet()));
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(types, defaultType, problemTypes, problemTypeActions);
	}

	@Override
	protected Type typedCopy()
	{
		return deepCopy();
	}

	public ConsentQualityControlConfig deepCopy()
	{
		ConsentQualityControlConfig config = new ConsentQualityControlConfig();
		copyValues(config);
		config.capture(this);
		return config;
	}

	public void capture(ConsentQualityControlConfig config)
	{
		if (config == null)
		{
			config = new ConsentQualityControlConfig();
		}
		setTypes(config.getTypes());
		setDefaultType(config.getDefaultType());
		setProblemTypes(config.getProblemTypes());
		setProblemTypeActions(config.getProblemTypeActions());
	}

	public Set<ConsentQCType> getTypes()
	{
		return new HashSet<>(types);
	}

	public void setTypes(Set<ConsentQCType> types)
	{
		this.types.clear();
		if (types != null)
		{
			this.types.addAll(types);
		}
	}

	public String getDefaultType()
	{
		return defaultType.getValue();
	}

	public void setDefaultType(String defaultType)
	{
		this.defaultType.setValue(defaultType);
	}

	public Set<ConsentQCProblemType> getProblemTypes()
	{
		return new HashSet<>(problemTypes);
	}

	public void setProblemTypes(Set<ConsentQCProblemType> problemTypes)
	{
		this.problemTypes.clear();
		if (problemTypes != null)
		{
			this.problemTypes.addAll(problemTypes);
		}
	}

	public Set<ConsentQCProblemTypeAction> getProblemTypeActions()
	{
		return new HashSet<>(problemTypeActions);
	}

	public void setProblemTypeActions(Set<ConsentQCProblemTypeAction> problemTypeActions)
	{
		this.problemTypeActions.clear();
		if (problemTypeActions != null)
		{
			this.problemTypeActions.addAll(problemTypeActions);
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ConsentQualityControlConfig that))
			return false;

		return new EqualsBuilder()
				.append(getTypes(), that.getTypes())
				.append(getDefaultType(), that.getDefaultType())
				.append(getProblemTypeActions(), that.getProblemTypeActions())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getTypes())
				.append(getDefaultType())
				.append(getProblemTypes())
				.append(getProblemTypeActions())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("validQcTypes", getTypes())
				.append("defaultQcType", getDefaultType())
				.append("problemTypes", getProblemTypes())
				.append("problemTypeActions", getProblemTypeActions())
				.toString();
	}
}
