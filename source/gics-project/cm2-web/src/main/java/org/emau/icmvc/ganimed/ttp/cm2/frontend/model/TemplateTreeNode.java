package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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


import java.text.SimpleDateFormat;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.icmvc.ttp.web.controller.AbstractBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateTreeNode extends AbstractBean
{
	private Logger logger = LoggerFactory.getLogger(getClass());

	private Object key;
	private String label;
	private String versionLabel;
	private Type type;
	private Boolean finalised;
	private String externProperties;
	private ExpirationPropertiesDTO expiration;
	private String comment;
	private Boolean mandatory;
	private SimpleDateFormat sdf;

	private TemplateTreeNode(Object key, String label, String versionLabel, Boolean finalised, String externProperties, String comment)
	{
		this.key = key;
		this.label = label;
		this.versionLabel = versionLabel;
		this.initType();
		this.finalised = finalised;
		this.externProperties = externProperties;
		this.expiration = null;
		this.comment = comment;
		this.mandatory = true;
	}

	// Policy
	public TemplateTreeNode(Object key, String label, Boolean finalised, String externProperties, ExpirationPropertiesDTO expiration, String comment)
	{
		this(key, label, null, finalised, externProperties, comment);
		this.expiration = expiration;
		this.sdf = getSimpleDateFormat("date");
	}

	// Template
	public TemplateTreeNode(Object key, String label, String versionLabel, Boolean finalised, String externProperties, ExpirationPropertiesDTO expiration, String comment)
	{
		this(key, label, versionLabel, finalised, externProperties, comment);
		this.expiration = expiration;
		this.sdf = getSimpleDateFormat("date");
	}

	// Module
	public TemplateTreeNode(Object key, String label, Boolean finalised, String externProperties, ExpirationPropertiesDTO expiration, Boolean mandatory, String comment)
	{
		this(key, label, null, finalised, externProperties, comment);
		this.expiration = expiration;
		this.sdf = getSimpleDateFormat("date");
		this.mandatory = mandatory;
	}

	private void initType()
	{
		if (key instanceof ConsentTemplateKeyDTO)
		{
			type = Type.TEMPLATE;
		}
		else if (key instanceof ModuleKeyDTO)
		{
			type = Type.MODULE;
		}
		else if (key instanceof PolicyKeyDTO)
		{
			type = Type.POLICY;
		}
		else
		{
			logger.error("Cannot parse object for template tree.");
		}
	}

	public Object getKey()
	{
		return key;
	}

	public String getLabel()
	{
		return label;
	}

	public String getVersionLabel()
	{
		return versionLabel;
	}

	public Type getType()
	{
		return type;
	}

	public Boolean getFinalised()
	{
		return finalised;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public String getExpiration()
	{
		StringBuilder result = new StringBuilder();
		if (expiration != null)
		{
			if (expiration.getFixedExpirationDate() != null)
			{
				result.append(sdf.format(expiration.getFixedExpirationDate()));
			}
			if (expiration.getValidPeriod() != null)
			{
				if (result.length() > 0)
				{
					result.append(", ");
				}
				result.append(expiration.getValidPeriod().toString());
			}
		}
		return result.toString();
	}

	public String getComment()
	{
		return comment;
	}

	public Boolean getMandatory()
	{
		return mandatory;
	}

	public enum Type
	{
		TEMPLATE, MODULE, POLICY
	}
}
