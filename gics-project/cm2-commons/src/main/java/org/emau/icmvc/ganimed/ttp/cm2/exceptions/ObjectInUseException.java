package org.emau.icmvc.ganimed.ttp.cm2.exceptions;

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


/**
 * should be thrown when the given object can't be deleted
 * 
 * @author geidell
 *
 */
public class ObjectInUseException extends Exception {

	private static final long serialVersionUID = 7883583294443246932L;

	public ObjectInUseException() {
		super();
	}

	public ObjectInUseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectInUseException(String message) {
		super(message);
	}

	public ObjectInUseException(Throwable cause) {
		super(cause);
	}
}
