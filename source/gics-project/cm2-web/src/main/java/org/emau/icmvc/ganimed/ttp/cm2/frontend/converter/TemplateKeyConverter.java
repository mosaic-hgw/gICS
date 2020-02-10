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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateKeyDTO;

//@FacesConverter("templateKeyConverter")
@ManagedBean(name = "templateKeyConverter")
public class TemplateKeyConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		String[] args = arg2.split(",");
		if (!(args.length < 3)) {
			return new ConsentTemplateKeyDTO(args[0], args[1], args[2]);
		} else {
			return null;
		}

	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {

		if (value == null || value.equals("")) {
			return null;
		} else {
			ConsentTemplateKeyDTO key = (ConsentTemplateKeyDTO) value;

			return key.getDomainName() + "," + key.getName() + "," + key.getVersion();
		}

	}
	
	/**
	 * convert keydto to keystring
	 * 
	 * @param key
	 *            TemplateKeyDTO to be converted to keystring domain<sep>name<sep>version
	 * @param sep
	 *            specify key separator, default=','
	 * @return keystring
	 */
	public static String getAsString(ConsentTemplateKeyDTO key, char sep)
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
	 * @param keys
	 *            list of ConsentTemplateDTO to be converted to keystrings domain<sep>name<sep>version
	 * @param sep
	 *            specify key separator, default=','
	 * @return list with keystrings
	 */
	public static List<String> getAsStrings(List<ConsentTemplateDTO> keys, char sep)
	{

		List<String> result = new ArrayList<String>();
		
		if (keys != null && keys.size()>0)
		{
			if (sep == '\0') {sep=',';}
			for (ConsentTemplateDTO t : keys)
			{
				result.add(t.getKey().getDomainName()+sep+t.getKey().getName()+sep+t.getKey().getVersion());
			}
		}

		return result;
	}

	/**
	 * split given keystring and use arguments to return respective ConsentTemplateDTO
	 * 
	 * @param keystring
	 * @param sep
	 *            seperator used to split string, default=','
	 * @return ConsentTemplateDTO or is null (if keystring is invalid)
	 */
	public static ConsentTemplateDTO getAsObject(String keystring, char sep)
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
				ConsentTemplateDTO templ = new ConsentTemplateDTO(new ConsentTemplateKeyDTO(args[0], args[1], args[2]));
				return templ;
			}
			else
			{
				return null;
			}
		}

		return null;
	}

}
