package org.emau.icmvc.magic.fhir.datatypes;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2018 The MOSAIC Project - Institut fuer Community
 * 							Medicine of the University Medicine Greifswald -
 * 							mosaic-projekt@uni-greifswald.de
 * 
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt, m.bialke
 * 
 * 							Selected functionalities of gICS were developed as part of the MAGIC Project (funded by the DFG HO 1937/5-1).
 * 
 * 							please cite our publications
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


import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.instance.model.api.ICompositeType;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.util.ElementUtil;

/**
 * FHIR Datatype to hold FreeText specific information for templates
 * 
 * @author bialkem {@link mosaic-greifswald.de}
 */
@DatatypeDef(name = "ConsentTemplateFreeText")
public class ConsentTemplateFreeText extends Type implements ICompositeType {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6122932550569090062L;

	@Child(name = "Name", order = 0, min = 1, max = 1)	
	@Description(shortDefinition = "name of new freetext for consent template")
	private StringDt name = new StringDt();
		
	@Child(name = "type", order = 1, min = 1, max = 1)	
	@Description(shortDefinition = "type of new freetext for consent template")
	private StringDt type = new StringDt();
	
	@Child(name = "comment", order = 2, min = 0, max = 1)	
	@Description(shortDefinition = "comment to describe purpose of ConsentTemplateFreeText")
	private StringDt comment = new StringDt();
	
	@Child(name = "converterString", order = 3, min = 0, max = 1)	
	@Description(shortDefinition = "converterString of consent template, only required in case of type=date")
	private StringDt converterString=  new StringDt();
	
	@Child(name = "required", order = 4, min = 1, max = 1)
	@Description(shortDefinition = "required state of freetext")
	private BooleanDt required=  new BooleanDt();
	
	public ConsentTemplateFreeText() {}
	
	public ConsentTemplateFreeText(String name, FreeTextType type, Boolean required) {
		
		this.setName(name);		
		this.setRequired(required);
		this.setType(type);		
	}
	
	public ConsentTemplateFreeText(String name, FreeTextType type, String comment, Boolean required) {
	
		this.setName(name);
		this.setComment(comment);
		this.setRequired(required);
		this.setType(type);		
	}
	
	/**
	 * get Name of freetext for consent template
	 * 
	 * 
	 * @return name of freetext for consent template
	 */
	public String getName() {
		if (name == null)
			name = new StringDt();
		return name.getValue();
	}

	/**
	 * set Name of freetext for consent template
	 * 
	 * @param freetextName
	 *            Name of freetext for consent template
	 * @return instance of ConsentTemplateFreeText
	 */
	public ConsentTemplateFreeText setName(String freetextName) {
		if (freetextName == null) {
			throw new NullPointerException("Given name of type String is null.");
		}

		if (freetextName.isEmpty()) {
			throw new NullPointerException("Given  name of type String cannot be empty.");
		}
		name.setValue(freetextName);
		return this;
	}
	
	
	


	/**
	 * get type of freetext for consent template
	 * 
	 * 
	 * @return type of freetext for consent template
	 */
	public FreeTextType getType() {
		if (type == null)
			type = new StringDt();
		return FreeTextType.valueOf(type.getValue());
	}

	/**
	 * set type of freetext for consent template
	 * 
	 * @param freetextType
	 *            type of freetext for consent template, if type=date then converterstring is 
	 *            automatically set to "dd.MM.yyyy"
	 *            
	 * @return instance of ConsentTemplateFreeText
	 */
	public ConsentTemplateFreeText setType(FreeTextType freetextType) {
		if (freetextType == null) {
			throw new NullPointerException("Given type of type enum is null.");
		}
		
		if(freetextType.equals(FreeTextType.Date))
		{
			setConverterString("dd.MM.yyyy");
		}

		type.setValue(freetextType.toString());
		return this;
	}
	
	
	

	/**
	 * get comment to describe purpose of ConsentTemplateFreeText
	 * 
	 * @return comment to describe purpose of ConsentTemplateFreeText
	 */
	public String getComment() {
		if (comment == null)
			comment = new StringDt();
		return comment.getValue();
	}

	/**
	 * set comment to describe purpose of cConsentTemplateFreeText
	 * 
	 * @param freetextComment
	 *            comment to describe purpose of ConsentTemplateFreeText
	 * @return instance of ConsentTemplateFreeText
	 */
	public ConsentTemplateFreeText setComment(String freetextComment) {
		if (freetextComment == null) {
			throw new NullPointerException("Given ConsentTemplateFreeText comment of type String is null.");
		}

		comment.setValue(freetextComment);
		return this;
	}
	
	
	
	
	/**
	 * get required state of freetext
	 * @return required state of freetext
	 */
	public Boolean getRequired() {
		return required.getValue();
	}

	/**
	 * set required state of freetext
	 * @param required required state of freetext
	 * @return instance of ConsentTemplateFreeText
	 */
	public ConsentTemplateFreeText setRequired(Boolean required) {
		
		if (required == null) {
			throw new NullPointerException("Given required value of  of type Boolean is null.");
		}

				
		this.required.setValue(required);
		return this;
	}

	
	
	/**
	 * get converter string for freetext
	 * @return converter string for freetext
	 */
	public String getConverterString() {		
		return converterString.getValueAsString();
	}

	/**
	 * set converter string for freetext of type date, default is "dd.MM.yyyy"
	 * @param converterString
	 * @return instance of ConsentTemplateFreeText
	 */
	public ConsentTemplateFreeText setConverterString(String converterString) {
		
		if (converterString == null) {
			throw new NullPointerException("Given converterString of  of type String is null.");
		}

		if (converterString.isEmpty()) {
			throw new NullPointerException("Given  converterString of type String cannot be empty.");
		}
		
		this.converterString.setValue(converterString);
		return this;
	}


	@Override
	public String toString() {
		return "ConsentTemplateFreeText [_name=" + name + ", _type=" + type + ", _comment=" + comment
				+ ", _required=" + required + ", _converterString=" + converterString + "]";
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((converterString == null) ? 0 : converterString.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((required == null) ? 0 : required.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsentTemplateFreeText other = (ConsentTemplateFreeText) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (converterString == null) {
			if (other.converterString != null)
				return false;
		} else if (!converterString.equals(other.converterString))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (required == null) {
			if (other.required != null)
				return false;
		} else if (!required.equals(other.required))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	protected Type typedCopy() {
		ConsentTemplateFreeText retValue = new ConsentTemplateFreeText();
		super.copyValues(retValue);
		retValue.setRequired(this.getRequired());
		retValue.setType(this.getType());
		retValue.setComment(this.getComment());
		retValue.setName(this.getName());
		retValue.setConverterString(this.getConverterString());
		return retValue;
	}

	@Override
	public boolean isEmpty() {
		return ElementUtil.isEmpty(comment,converterString,name,required,type);
	}

	/**
	 * internal enumeration to differentiate types of free text fields 
	 * 
	 * internal use only
	 * 
	 * @author bialkem
	 *
	 */
	public enum FreeTextType {
		String, Date, Integer, Double, Boolean
		
		
	}
	
	
}



