package org.emau.icmvc.ganimed.ttp.cm2.config;

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
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.LabeledId;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeAction;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCTypeStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ttp.util.FlexibleToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QualityControlConfig", propOrder = {
		"types",
		"defaultTypeId",
		"problemTypes",
		"problemTypeActions"
})
public class QualityControlConfig implements Serializable
{
	@Serial
	private static final long serialVersionUID = 8490691369318078074L;
	private static final Logger logger = LogManager.getLogger(QualityControlConfig.class);

	@XmlElement(name = "type")
	private final Set<QCType> types = new LabeledIdSet<>();
	@XmlAttribute(name = "default-type")
	private String defaultTypeId = QCDTO.AUTO_GENERATED;
	@XmlElement(name = "problem-type")
	private final Set<QCProblemType> problemTypes = new LabeledIdSet<>();
	@XmlElement(name = "problem-type-action")
	private final Set<QCProblemTypeAction> problemTypeActions = new LabeledIdSet<>();

	/**
	 * Empty constructor for deserialization.
	 */
	public QualityControlConfig()
	{
	}

	/**
	 * Copy constructor.
	 */
	public QualityControlConfig(QualityControlConfig config)
	{
		capture(config);
	}

	/**
	 * All fields constructor.
	 */
	public QualityControlConfig(Set<QCType> types, String defaultQcType, Set<QCProblemType> problemTypes, Set<QCProblemTypeAction> actions)
	{
		setTypes(types);
		setDefaultTypeId(defaultQcType);
		setProblemTypes(problemTypes);
		setProblemTypeActions(actions);
	}

	public void capture(QualityControlConfig config)
	{
		if (config == null)
		{
			config = new QualityControlConfig();
		}
		setTypes(config.getTypes());
		setDefaultTypeId(config.getDefaultTypeId());
		setProblemTypes(config.getProblemTypes());
		setProblemTypeActions(config.getProblemTypeActions());
	}

	public QCType getTypeById(String id)
	{
		return types.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
	}

	public Set<QCType> getTypes()
	{
		return types;
	}

	public void setTypes(Set<QCType> types)
	{
		this.types.clear();
		if (types != null)
		{
			this.types.addAll(types.stream().map(QCType::new).toList());
		}
	}

	public boolean hasType(String id, QCTypeStatus status)
	{
		return types.stream()
				.filter(t -> t.getId().equals(id))
				.filter(t -> t.getStatus() == status)
				.findFirst().orElse(null) != null;
	}

	public Set<String> getTypeIds(QCTypeStatus status)
	{
		return types.stream().filter(t -> t.getStatus() == status).map(QCType::getId).collect(Collectors.toSet());
	}

	public void setTypeIds(QCTypeStatus status, Set<String> ids)
	{
		types.removeIf(t -> t.getStatus() == status);
		if (ids != null)
		{
			types.addAll(ids.stream().map(t -> new QCType(t, status)).toList()); // LabeledIdSet removes all types with the same id first
		}
	}
	
	public Set<String> getValidQcTypeValues()
	{
		return getTypeIds(QCTypeStatus.VALID);
	}

	public void setValidQcTypeValues(Set<String> values)
	{
		setTypeIds(QCTypeStatus.VALID, values);
	}

	public Set<String> getInvalidQcTypeValues()
	{
		return getTypeIds(QCTypeStatus.INVALID);
	}

	public void setInvalidQcTypeValues(Set<String> values)
	{
		setTypeIds(QCTypeStatus.INVALID, values);
	}

	public QCType getDefaultType()
	{
		return getTypeById(defaultTypeId);
	}

	public void setDefaultType(QCType type)
	{
		types.add(new QCType(type)); // LabeledIdSet removes all types with the same id first
		defaultTypeId = type.getId();
	}

	public String getDefaultTypeId()
	{
		return defaultTypeId;
	}

	public void setDefaultTypeId(String defaultQcType)
	{
		this.defaultTypeId = defaultQcType;
	}

	// ##### problem types #####

	public QCProblemType getProblemTypeById(String id)
	{
		return problemTypes.stream().filter(pt -> pt.getId().equals(id)).findFirst().orElse(null);
	}

	public Set<QCProblemType> getProblemTypes()
	{
		return problemTypes;
	}

	public void setProblemTypes(Set<QCProblemType> problemTypes)
	{
		this.problemTypes.clear();
		if (problemTypes != null)
		{
			this.problemTypes.addAll(problemTypes.stream().map(QCProblemType::new).toList());
		}
	}

	// ##### problem type actions #####

	public QCProblemTypeAction getProblemTypeActionById(String id)
	{
		return problemTypeActions.stream().filter(pta -> pta.getId().equals(id)).findFirst().orElse(null);
	}

	public Set<QCProblemTypeAction> getProblemTypeActions()
	{
		return problemTypeActions;
	}

	public void setProblemTypeActions(Set<QCProblemTypeAction> problemTypeActions)
	{
		this.problemTypeActions.clear();
		if (problemTypeActions!= null)
		{
			this.problemTypeActions.addAll(problemTypeActions.stream().map(QCProblemTypeAction::new).toList());
		}
	}

	/**
	 * Ensures, that the default qc type is set and either listed as valid or invalid.
	 * @param domainName the name of the domain used for warning messages only
	 */
	public void normalize(String domainName)
	{
		Set<String> validQcTypes = getTypeIds(QCTypeStatus.VALID);
		Set<String> invalidQcTypes = getTypeIds(QCTypeStatus.INVALID);

		if (!validQcTypes.contains(defaultTypeId) && !invalidQcTypes.contains(defaultTypeId))
		{
			logger.warn("default qc type {} doesn't exist in valid or invalid qc types for domain {}, setting it to {}",
					defaultTypeId, domainName, QCDTO.AUTO_GENERATED);
			defaultTypeId = QCDTO.AUTO_GENERATED;
		}
		// Es kann (z.B. in Alt-Systemen) Consente geben, deren qc.type null oder
		// autogenerated ist (siehe auch update_database_gics_2.11.x-2.12.0.sql:170).
		// Darum muss QCDTO.AUTO_GENERATED immer ein valider qc-type sein,
		// und nicht nur, wenn QCDTO.AUTO_GENERATED in der domain-config verwendet wird
		if (!validQcTypes.contains(QCDTO.AUTO_GENERATED))
		{
			if (logger.isInfoEnabled())
			{
				logger.info("{} doesn't exist in valid qc types for domain {}, adding it", QCDTO.AUTO_GENERATED, domainName);
			}
			validQcTypes.add(QCDTO.AUTO_GENERATED);
			setTypeIds(QCTypeStatus.VALID, validQcTypes);
		}
	}

	/**
	 * @see #validate(String)
	 */
	public void validate() throws InvalidParameterException
	{
		validate(null);
	}

	/**
	 * Assert, that the qc config is consistent, which means, that
	 *
	 * <ul>
	 *     <li>the default type is configered in the set of qc types ({@link #getTypes()}),</li>
	 *     <li>all actions refered to from qc problem types are configured in the set of qc problem type actions ({@link #getProblemTypeActions()}), and</li>
	 *     <li>for each set ({@link #getTypes()}, {@link #getProblemTypes()}, and {@link #getProblemTypeActions()}) the labeled IDs are distinct.</li>
	 * </ul>
	 *
	 * @param paramName an optional name of the parameter
	 * @throws InvalidParameterException if the qc config is not consistent
	 */
	public void validate(String paramName) throws InvalidParameterException
	{
		String msgPrefix = StringUtils.isBlank(paramName) ? "" : paramName + ": ";
		paramName = StringUtils.isBlank(paramName) ? "unknown" : paramName;

		// check if default qc type is valid and configured

		if (StringUtils.isNotBlank(getDefaultTypeId())) // formally no default qc type is possible, will be fixed on save by adding '###_auto_generated_###'
		{
			// but if set it must be valid and configured
			QCType defaultType = getDefaultType();

			if (defaultType == null)
			{
				throw new InvalidParameterException(paramName, msgPrefix + "default qc type '"
						+ getDefaultTypeId() + "' is not found in configured qc types " + getTypes());
			}
			else if (!defaultType.hasValidId())
			{
				throw new InvalidParameterException(paramName, msgPrefix + "default qc type '"
						+ getDefaultTypeId() + "' is invalid");
			}
		}

		// check for invalid or duplicate ids

		validateIDs(paramName, msgPrefix, "types", getTypes());
		validateIDs(paramName, msgPrefix, "problem types", getProblemTypes());
		validateIDs(paramName, msgPrefix, "problem type actions", getProblemTypeActions());

		// check for orphaned actions

		List<String>  orphanedActions = getProblemTypes().stream().map(QCProblemType::getAction).filter(Objects::nonNull)
				.map(LabeledId::getId).distinct().filter(aid -> getProblemTypeActionById(aid) == null).toList();

		if (!orphanedActions.isEmpty())
		{
			throw new InvalidParameterException(paramName, msgPrefix + "the qc problem type actions " + orphanedActions
					+ " are not found in configured qc problem type actions " + getProblemTypeActions());
		}
	}

	private void validateIDs(String paramName, String msgPrefix, String idType, Collection<? extends LabeledId> lids) throws InvalidParameterException
	{
		// check invalid ids
		Set<String> invalidIds = lids.stream()
				.filter(Predicate.not(LabeledId::hasValidId))
				.map(LabeledId::getId).collect(Collectors.toSet());
		if (!invalidIds.isEmpty())
		{
			throw new InvalidParameterException(paramName, msgPrefix + "the set of qc " + idType
					+ " contains invalid ids: " + invalidIds);
		}

		// check duplicate ids
		Set<String> tmp = new HashSet<>();
		Set<String> duplicateIds = lids.stream()
				.map(LabeledId::getId)
				.filter(id -> !tmp.add(id)).collect(Collectors.toSet());
		if (!duplicateIds.isEmpty())
		{
			throw new InvalidParameterException(paramName, msgPrefix + "the set of qc " + idType
					+ " contains duplicate ids: " + duplicateIds);
		}
	}

	/**
	 * Updates the labels of existing types, problem types, and problem type actions,
	 * and takes over new types, ... as well as the default qc type from the given config.
	 * @param config the new config to update from
	 * @return true if this qc config has changed on update
	 */
	public boolean updateUnlockedParts(QualityControlConfig config)
	{
		QualityControlConfig qc = new QualityControlConfig(this);

		// update labels
		getTypes().forEach(t -> t.updateLabels(config.getTypeById(t.getId())));
		getProblemTypes().forEach(t -> t.updateLabels(config.getProblemTypeById(t.getId())));
		getProblemTypeActions().forEach(t -> t.updateLabels(config.getProblemTypeActionById(t.getId())));

		// add new types, problem types, and problem type actions
		getTypes().addAll(config.getTypes().stream()
				.filter(t -> getTypeById(t.getId()) == null).toList());
		getProblemTypes().addAll(config.getProblemTypes().stream()
				.filter(pt -> getProblemTypeById(pt.getId()) == null).toList());
		getProblemTypeActions().addAll(config.getProblemTypeActions().stream()
				.filter(pta -> getProblemTypeActionById(pta.getId()) == null).toList());

		// update the default qc type
		setDefaultTypeId(config.getDefaultTypeId());

		return !equals(qc);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof QualityControlConfig that))
			return false;

		return new EqualsBuilder()
				.append(getTypes(), that.getTypes())
				.append(getDefaultTypeId(), that.getDefaultTypeId())
				.append(getProblemTypes(), that.getProblemTypes())
				.append(getProblemTypeActions(), that.getProblemTypeActions())
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(getTypes())
				.append(getDefaultTypeId())
				.append(getProblemTypes())
				.append(getProblemTypeActions())
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, FlexibleToStringStyle.THS_TO_STRING_STYLE)
				.append("types", types)
				.append("defaultType", defaultTypeId)
				.append("problemTypes", problemTypes)
				.append("problemTypeActions", problemTypeActions)
				.toString();
	}

	/**
	 * A set based on an ArrayList and so not being a hash-based collection.
	 * Avoids problems with mutable objects n hash-based collections.
	 * Allows changing properties of types, problemTypes, or problemTypeActions
	 * which are already contained in the sets of QcConfig
	 */
	 static class LabeledIdSet<T extends LabeledId> extends AbstractSet<T> implements Serializable
	{
		@Serial
		private static final long serialVersionUID = 580360416040986070L;

		private final List<T> list = new ArrayList<>();

		@Override
		public Iterator<T> iterator()
		{
			return list.iterator();
		}

		@Override
		public int size()
		{
			return list.size();
		}

		@Override
		public boolean add(T t)
		{
			boolean removed = list.removeIf(lid -> lid.getId().equals(t.getId()));
			list.add(t);
			return !removed;
		}

		@Override
		public boolean remove(Object o)
		{
			if (o instanceof LabeledId other)
			{
				return list.removeIf(lid -> lid.getId().equals(other.getId()));
			}
			return list.remove(o);
		}
	}
}
