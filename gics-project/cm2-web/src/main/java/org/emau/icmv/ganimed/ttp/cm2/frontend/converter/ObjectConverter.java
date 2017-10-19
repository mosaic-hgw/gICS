package org.emau.icmv.ganimed.ttp.cm2.frontend.converter;

/*
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Source: http://stackoverflow.com/questions/3621251/how-to-write-a-custom-converter-when-working-with-primefaces-components-that-co
 * 
 * Converter for Seriazable Objects
 * 
 * @author weiherg
 * 
 */
@FacesConverter("seriazableConverter")
public class ObjectConverter implements Converter {

	private Logger logger = LoggerFactory.getLogger(ObjectConverter.class);;

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String objectString) {
		try {
			byte[] data = Base64.decode(objectString);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception e) {
			logger.error("exception while converting stream to object", e);
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.close();

			return Base64.encodeToString(baos.toByteArray(), true);
		} catch (IOException e) {
			logger.error("exception while converting object to string", e);
			return "";
		}
	}

}
