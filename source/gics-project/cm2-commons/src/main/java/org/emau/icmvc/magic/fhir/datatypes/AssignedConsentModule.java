package org.emau.icmvc.magic.fhir.datatypes;

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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.Type;

/**
 * FHIR Datatype to hold assigned module specific information for templates
 *
 * @author bialkem
 *
 */
@DatatypeDef(name = "AssignedConsentModule")
public class AssignedConsentModule extends Type implements ICompositeType
{
	/**
	 *
	 */
	private static final long serialVersionUID = 2658001551317399850L;

	@Child(name = "comment", order = 0, min = 1, max = 1)
	@Description(shortDefinition = "comment to describe purpose of AssignedConsentModule")
	private StringDt comment = new StringDt();

	@Child(name = "defaultConsentStatus", order = 1, min = 1, max = 1)
	@Description(shortDefinition = "default patient consent status to  be used for this assigned consent module")
	private StringDt defaultConsentStatus = new StringDt();

	@Child(name = "displayCheckBoxes", order = 2, min = 1, max = 9)
	@Description(shortDefinition = "semicolon separated list of check box values of type PatientConsentStatus to be displayed for this assigned consent module")
	private List<StringDt> displayCheckBoxes = new ArrayList<>();

	@Child(name = "mandatory", order = 3, min = 1, max = 1)
	@Description(shortDefinition = "mandatory state of AssignedConsentModule")
	private BooleanDt mandatory = new BooleanDt();

	@Child(name = "moduleKey", order = 4, min = 1, max = 1)
	@Description(shortDefinition = "unique module key")
	private StringDt moduleKey = new StringDt();

	@Child(name = "orderNumber", order = 5, min = 0, max = 1)
	@Description(shortDefinition = "order number for this assigned consent module")
	private IntegerDt orderNumber = new IntegerDt();

	@Child(name = "externProperties", order = 6, min = 0, max = 1)
	@Description(shortDefinition = "externProperties of assigned modules")
	private StringDt externProperties = new StringDt();

	@Child(name = "expirationProperties", order = 7, min = 0, max = 1)
	@Description(shortDefinition = "expirationProperties of assigned modules")
	private StringDt expirationProperties = new StringDt();

	public AssignedConsentModule()
	{}

	/**
	 * create new instance of AssignedConsentModule using only obligatory parameters
	 *
	 * @param moduleKey
	 *            module reference as semicolon separated string
	 * @param displayCheckBoxes
	 *            list of to be displayed checkbox values for patient consent status
	 * @param defaultConsentStatus
	 *            to be used default patient consent status for this module
	 * @param mandatoryModule
	 *            true if acceptance of this module should be mandatory for the consent template
	 */
	public AssignedConsentModule(String moduleKey, List<PatientConsentStatus> displayCheckBoxes, PatientConsentStatus defaultConsentStatus,
			Boolean mandatoryModule)
	{
		setModuleKey(moduleKey);
		setDisplayCheckBoxes(displayCheckBoxes);
		setDefaultConsentStatus(defaultConsentStatus);
		setMandatory(mandatoryModule);
		setOrderNumber(-1);
		setComment("");
		setExternProperties("");
		setExpirationProperties("");

	}

	/***
	 *
	 * create new instance of AssignedConsentModule using additional parameters
	 *
	 * @param moduleKey
	 *            module reference as semicolon separated string
	 * @param displayCheckBoxes
	 *            list of to be displayed checkbox values for patient consent status
	 * @param defaultConsentStatus
	 *            to be used default patient consent status for this module
	 * @param mandatoryModule
	 *            true if acceptance of this module should be mandatory for the consent template
	 * @param comment
	 *            comment
	 * @param orderNumber
	 *            ordernumber to change order of modules in template (future use)
	 * @param externProperties
	 *            externProperties for assigned module
	 * @param expirationProperties
	 *            expirationProperties for assigned module
	 */
	public AssignedConsentModule(String moduleKey, List<PatientConsentStatus> displayCheckBoxes, PatientConsentStatus defaultConsentStatus,
			Boolean mandatoryModule, String comment, Integer orderNumber, String externProperties, String expirationProperties)
	{
		this(moduleKey, displayCheckBoxes, defaultConsentStatus, mandatoryModule, comment, orderNumber);
		setExternProperties(externProperties);
		setExpirationProperties(expirationProperties);
	}

	/***
	 *
	 * create new instance of AssignedConsentModule using additional parameters
	 *
	 * @param moduleKey
	 *            module reference as semicolon separated string
	 * @param displayCheckBoxes
	 *            list of to be displayed checkbox values for patient consent status
	 * @param defaultConsentStatus
	 *            to be used default patient consent status for this module
	 * @param mandatoryModule
	 *            true if acceptance of this module should be mandatory for the consent template
	 * @param comment
	 *            comment
	 * @param orderNumber
	 *            ordernumber to change order of modules in template (future use)
	 * @param externProperties
	 *            externProperties for assigned module
	 */
	public AssignedConsentModule(String moduleKey, List<PatientConsentStatus> displayCheckBoxes, PatientConsentStatus defaultConsentStatus,
			Boolean mandatoryModule, String comment, Integer orderNumber, String externProperties)
	{
		this(moduleKey, displayCheckBoxes, defaultConsentStatus, mandatoryModule, comment, orderNumber);
		setExternProperties(externProperties);
	}

	/***
	 *
	 * create new instance of AssignedConsentModule using additional parameters
	 *
	 * @param moduleKey
	 *            module reference as semicolon separated string
	 * @param displayCheckBoxes
	 *            list of to be displayed checkbox values for patient consent status
	 * @param defaultConsentStatus
	 *            to be used default patient consent status for this module
	 * @param mandatoryModule
	 *            true if acceptance of this module should be mandatory for the consent template
	 * @param comment
	 *            comment
	 * @param orderNumber
	 *            ordernumber to change order of modules in template (future use)
	 */
	public AssignedConsentModule(String moduleKey, List<PatientConsentStatus> displayCheckBoxes, PatientConsentStatus defaultConsentStatus,
			Boolean mandatoryModule, String comment, Integer orderNumber)
	{
		this(moduleKey, displayCheckBoxes, defaultConsentStatus, mandatoryModule);
		setComment(comment);
		setOrderNumber(orderNumber);
	}

	/**
	 * get mandatory state of AssignedConsentModule
	 *
	 * @return mandatory state of AssignedConsentModule
	 */
	public Boolean getMandatory()
	{
		return mandatory.getValue();
	}

	/**
	 * set mandatory state of AssignedConsentModule
	 *
	 * @param mandatory
	 *            required state of AssignedConsentModule
	 * @return instance of AssignedConsentModule
	 */
	public AssignedConsentModule setMandatory(Boolean mandatory)
	{
		if (mandatory == null)
		{
			throw new NullPointerException("Given mandatory value of type Boolean is null.");
		}

		this.mandatory.setValue(mandatory);
		return this;
	}

	/**
	 * get comment to describe purpose of AssignedConsentModule
	 *
	 * @return comment to describe purpose of AssignedConsentModule
	 */
	public String getComment()
	{
		if (comment == null)
		{
			comment = new StringDt();
		}
		return comment.getValue();
	}

	/**
	 * set comment to describe purpose of AssignedConsentModule
	 *
	 * @param moduleComment
	 *            comment to describe purpose of AssignedConsentModule
	 * @return instance of AssignedConsentModule
	 */
	public AssignedConsentModule setComment(String moduleComment)
	{
		if (moduleComment == null)
		{
			throw new NullPointerException("Given AssignedConsentModule comment of type String is null.");
		}

		comment.setValue(moduleComment);
		return this;
	}

	/**
	 * get module reference as semicolon separated string
	 *
	 * @return referenced module key
	 */
	public String getModuleKey()
	{
		if (moduleKey == null)
		{
			moduleKey = new StringDt();
		}

		return moduleKey.getValue();
	}

	/**
	 * setmodule reference as semicolon separated string
	 *
	 * @param consentmodulekey
	 *            referenced module to be assigned to template, as semicolon separated string
	 * @return instance of AssignedConsentModule
	 */
	public AssignedConsentModule setModuleKey(String consentmodulekey)
	{
		if (consentmodulekey == null || consentmodulekey.isEmpty())
		{
			throw new NullPointerException("Given module key of type String is null or empty.");
		}

		moduleKey.setValue(consentmodulekey);

		return this;
	}

	/**
	 * get defaultConsentStatus of AssignedConsentModule
	 *
	 * @return defaultConsentStatus of AssignedConsentModule
	 */
	public String getDefaultConsentStatus()
	{
		if (defaultConsentStatus == null)
		{
			defaultConsentStatus = new StringDt();
		}
		return defaultConsentStatus.getValue();
	}

	/**
	 * set defaultConsentStatus of AssignedConsentModule
	 *
	 * @param defaultState
	 *            defaultConsentStatus of AssignedConsentModule
	 * @return instance of AssignedConsentModule
	 */
	public AssignedConsentModule setDefaultConsentStatus(PatientConsentStatus defaultState)
	{
		if (defaultState == null)
		{
			throw new NullPointerException("Given defaultConsentStatus  of type enum is null.");
		}

		defaultConsentStatus.setValue(defaultState.toString());
		return this;
	}

	/**
	 * get orderNumber of AssignedConsentModule
	 *
	 * @return orderNumber of AssignedConsentModule
	 */
	public Integer getOrderNumber()
	{
		if (orderNumber == null)
		{
			orderNumber = new IntegerDt();
		}
		return orderNumber.getValue();
	}

	/**
	 * set orderNumber of AssignedConsentModule
	 *
	 * @param moduleOrdNo
	 *            orderNumber of AssignedConsentModule
	 * @return instance of AssignedConsentModule
	 */
	public AssignedConsentModule setOrderNumber(Integer moduleOrdNo)
	{
		if (moduleOrdNo == null)
		{
			throw new NullPointerException("Given orderNumber  of type Integer is null.");
		}

		orderNumber.setValue(moduleOrdNo);
		return this;
	}

	/**
	 * get list of check box values of type PatientConsentStatus of AssignedConsentModule
	 *
	 * @return assigned checkbox values of this AssignedConsentModule
	 */
	public List<PatientConsentStatus> getDisplayCheckBoxes()
	{
		if (displayCheckBoxes == null)
		{
			displayCheckBoxes = new ArrayList<>();
		}

		List<PatientConsentStatus> result = new ArrayList<>();

		for (StringDt checkbox : displayCheckBoxes)
		{
			result.add(PatientConsentStatus.valueOfIncludingObsolete(checkbox.getValue()));
		}

		return result;
	}

	/**
	 * get list of check box values of type PatientConsentStatus of AssignedConsentModule as string
	 *
	 * @return assigned checkbox values of this AssignedConsentModule as string
	 */
	public String getDisplayCheckBoxesAsString()
	{
		return convertToString(getDisplayCheckBoxes());
	}

	/**
	 * convert list of PatientConsentStatus to semicolonseparated string
	 *
	 * @param listStatus
	 *            list of PatientConsentStatus
	 * @return converted string
	 */
	private String convertToString(List<PatientConsentStatus> listStatus)
	{
		StringBuilder sb = new StringBuilder();

		if (listStatus != null && !listStatus.isEmpty())
		{
			for (PatientConsentStatus patientConsentStatus : listStatus)
			{
				sb.append(patientConsentStatus.toString());
				if (listStatus.indexOf(patientConsentStatus) != listStatus.size() - 1)
				{
					// not last element, add separator
					sb.append(";");
				}
			}
		}

		return sb.toString();
	}

	/**
	 * set list of check box values of type PatientConsentStatus of AssignedConsentModule
	 *
	 * @param toBeDisplayedCheckBoxes
	 *            list of displayCheckBoxes for AssignedConsentModule
	 * @return instance of AssignedConsentModule
	 */
	public AssignedConsentModule setDisplayCheckBoxes(List<PatientConsentStatus> toBeDisplayedCheckBoxes)
	{
		if (toBeDisplayedCheckBoxes == null)
		{
			throw new NullPointerException("Given list of defaultConsentStatus  of type enum is null.");
		}

		displayCheckBoxes = new ArrayList<>();

		for (PatientConsentStatus p : toBeDisplayedCheckBoxes)
		{
			displayCheckBoxes.add(new StringDt(p.toString()));
		}

		return this;
	}

	/**
	 * get externProperties of assigned module
	 *
	 * @return externProperties of assigned module
	 */
	public String getExternProperties()
	{
		if (externProperties == null)
		{
			externProperties = new StringDt();
		}
		return externProperties.getValue();
	}

	/**
	 * set externProperties of assigned module with 0-n properties separated by semicolon e.g.
	 * validity_period=p1y
	 *
	 * @param externProperties
	 *            externProperties of assigned module
	 * @return instance of Assigend Module
	 */
	public AssignedConsentModule setExternProperties(String externProperties)
	{
		if (externProperties == null)
		{
			throw new NullPointerException("Given externProperties of type String is null.");
		}
		this.externProperties.setValue(externProperties);
		return this;
	}

	/**
	 * get expirationProperties of assigned module
	 *
	 * @return expirationProperties of assigned module
	 */
	public String getExpirationProperties()
	{
		if (expirationProperties == null)
		{
			expirationProperties = new StringDt();
		}
		return expirationProperties.getValue();
	}

	/**
	 * set expirationProperties of assigned module
	 *
	 * @param expirationProperties
	 *            expirationProperties of assigned module
	 * @return instance of Assigend Module
	 */
	public AssignedConsentModule setExpirationProperties(String expirationProperties)
	{
		if (expirationProperties == null)
		{
			throw new NullPointerException("Given expirationProperties of type String is null.");
		}
		this.expirationProperties.setValue(expirationProperties);
		return this;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((defaultConsentStatus == null) ? 0 : defaultConsentStatus.hashCode());
		result = prime * result + ((displayCheckBoxes == null) ? 0 : displayCheckBoxes.hashCode());
		result = prime * result + ((externProperties == null) ? 0 : externProperties.hashCode());
		result = prime * result + ((expirationProperties == null) ? 0 : expirationProperties.hashCode());
		result = prime * result + ((mandatory == null) ? 0 : mandatory.hashCode());
		result = prime * result + ((moduleKey == null) ? 0 : moduleKey.hashCode());
		result = prime * result + ((orderNumber == null) ? 0 : orderNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AssignedConsentModule other = (AssignedConsentModule) obj;
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (defaultConsentStatus == null)
		{
			if (other.defaultConsentStatus != null)
			{
				return false;
			}
		}
		else if (!defaultConsentStatus.equals(other.defaultConsentStatus))
		{
			return false;
		}
		if (displayCheckBoxes == null)
		{
			if (other.displayCheckBoxes != null)
			{
				return false;
			}
		}
		else if (!displayCheckBoxes.equals(other.displayCheckBoxes))
		{
			return false;
		}
		if (expirationProperties == null)
		{
			if (other.expirationProperties != null)
			{
				return false;
			}
		}
		else if (!expirationProperties.equals(other.expirationProperties))
		{
			return false;
		}
		if (externProperties == null)
		{
			if (other.externProperties != null)
			{
				return false;
			}
		}
		else if (!externProperties.equals(other.externProperties))
		{
			return false;
		}
		if (mandatory == null)
		{
			if (other.mandatory != null)
			{
				return false;
			}
		}
		else if (!mandatory.equals(other.mandatory))
		{
			return false;
		}
		if (moduleKey == null)
		{
			if (other.moduleKey != null)
			{
				return false;
			}
		}
		else if (!moduleKey.equals(other.moduleKey))
		{
			return false;
		}
		if (orderNumber == null)
		{
			if (other.orderNumber != null)
			{
				return false;
			}
		}
		else if (!orderNumber.equals(other.orderNumber))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("key=" + moduleKey + ";");

		sb.append("checkboxlist=" + convertToString(getDisplayCheckBoxes()) + ";");
		sb.append("defaultconsentstatus=" + getDefaultConsentStatus() + ";");
		sb.append("mandatory=" + getMandatory() + ";");
		sb.append("comment=" + getComment() + ";");
		sb.append("ordernumber=" + getOrderNumber());
		sb.append("externalproperties=" + getExternProperties());
		sb.append("expirationProperties=" + getExpirationProperties());

		return sb.toString();
	}

	@Override
	public boolean isEmpty()
	{
		return ElementUtil.isEmpty(comment, defaultConsentStatus, displayCheckBoxes, mandatory, moduleKey, orderNumber, externProperties, expirationProperties);
	}

	@Override
	protected Type typedCopy()
	{
		AssignedConsentModule retValue = new AssignedConsentModule();
		super.copyValues(retValue);

		retValue.setComment(getComment());
		retValue.setDefaultConsentStatus(PatientConsentStatus.valueOfIncludingObsolete(getDefaultConsentStatus()));
		retValue.setDisplayCheckBoxes(getDisplayCheckBoxes());
		retValue.setMandatory(getMandatory());
		retValue.setModuleKey(getModuleKey());
		retValue.setOrderNumber(getOrderNumber());
		retValue.setExternProperties(getExternProperties());
		retValue.setExpirationProperties(getExpirationProperties());
		return retValue;
	}

	/**
	 * internal enumeration to differentiate types of patient consent status
	 *
	 * internal use only
	 *
	 * @author bialkem
	 *
	 */
	public enum PatientConsentStatus
	{
		ACCEPTED, DECLINED, UNKNOWN, NOT_ASKED, NOT_CHOSEN("NOT_CHOOSEN"), WITHDRAWN("REVOKED"), INVALIDATED, REFUSED, EXPIRED;

		private final String obsolete;

		PatientConsentStatus()
		{
			this(null);
		}


		PatientConsentStatus(String obsolete)
		{
			this.obsolete = obsolete;
		}

		public String getObsolete()
		{
			return obsolete;
		}

		/**
		 * Returns the enum constant of this type with the specified name or an obsoleted form of a former name.
		 *
		 * @param name the name or an obsoleted form of a former name
		 * @return enum constant with the specified name or its obsoleted form
		 * @see #valueOf(String)
		 *
		 * @throws IllegalArgumentException – if this enum type has no constant with the specified name including obsoleted forms of a former names.
		 */
		public static PatientConsentStatus valueOfIncludingObsolete(String name)
		{
			try
			{
				return PatientConsentStatus.valueOf(name);
			}
			catch (IllegalArgumentException e)
			{
				PatientConsentStatus status = Arrays.stream(PatientConsentStatus.values()).filter(s -> name.equals(s.getObsolete())).findFirst().orElse(null);

				if (status == null) {
					throw e;
				}
				return status;
			}
		}
	}
}
