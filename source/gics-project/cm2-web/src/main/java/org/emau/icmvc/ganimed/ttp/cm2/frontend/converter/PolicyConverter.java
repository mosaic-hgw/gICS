package org.emau.icmvc.ganimed.ttp.cm2.frontend.converter;

import java.util.ArrayList;
import java.util.List;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;

@FacesConverter("policyConverter")
public class PolicyConverter implements Converter
{

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value)
	{
		String[] args = value.split(",");
		if (!(args.length < 3))
		{
			PolicyDTO policy = new PolicyDTO(new PolicyKeyDTO(args[0], args[1], args[2])); // Integer.parseInt(args[2])
			return policy;
		}
		else
		{
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object value)
	{
		PolicyKeyDTO key = ((PolicyDTO) value).getKey();

		return key.getDomainName() + "," + key.getName() + "," + key.getVersion();
	}

	/**
	 * convert keydto to keystring
	 * 
	 * @param key
	 *            PolicyKeyDTO to be converted to keystring domain<sep>name<sep>version
	 * @param sep
	 *            specify key separator, default=','
	 * @return keystring
	 */
	public static String getAsString(PolicyKeyDTO key, char sep)
	{

		String result = "";
		if (key != null)
		{
			if (sep != '\0')
			{
				result = key.getDomainName() + sep + key.getName() + sep + key.getVersion();
			}
			else
			{
				result = key.getDomainName() + "," + key.getName() + "," + key.getVersion();
			}
		}

		return result;
	}
	
	/**
	 * convert keydto to list of keystrings
	 * 
	 * @param list
	 *            list of PolicyDTO to be converted to keystrings domain<sep>name<sep>version
	 * @param sep
	 *            specify key separator, default=','
	 * @return list with keystrings
	 */
	public static List<String> getAsStrings(List<PolicyDTO> list, char sep)
	{

		List<String> result = new ArrayList<String>();
		
		if (list != null && list.size()>0)
		{
			if (sep == '\0') {sep=',';}
			for (PolicyDTO p : list)
			{
				result.add(p.getKey().getDomainName()+sep+p.getKey().getName()+sep+p.getKey().getVersion());
			}
		}

		return result;
	}

	/**
	 * split given keystring and use arguments to return respective PolicyKeyDTO
	 * 
	 * @param keystring
	 * @param sep
	 *            seperator used to split string, default=','
	 * @return PolicyDTO or is null (if keystring is invalid)
	 */
	public static PolicyDTO getAsObject(String keystring, char sep)
	{
		if (keystring != null && !keystring.isEmpty())
		{
			String[] args;

			if (sep != '\0')
			{
				args = keystring.split(",");

			}
			else
			{
				args = keystring.split(String.valueOf(sep));
			}

			if (!(args.length < 3))
			{
				PolicyDTO pol = new PolicyDTO(new PolicyKeyDTO(args[0], args[1], args[2]));
				return pol;
			}
			else
			{
				return null;
			}
		}

		return null;
	}

}
