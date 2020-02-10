package org.emau.icmvc.ganimed.ttp.cm2.model;

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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;

/**
 * extra tabelle, um text-blob-felder auszulagern<br>
 * nur ein string und nicht die felder domain, name, version, type als fk-teile eines zusammengesetzten pks, da die ersten drei aus unterschiedlichen tabellen kommen koennen (module, consenttemplate
 * und consent)
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "text")
@Cache(isolation = CacheIsolationType.ISOLATED)
public class Text implements Serializable {

	private static final long serialVersionUID = -2526546843194537950L;
	private static final String DELIMITER = "_###_";
	@Id
	private String id;
	@Lob
	private String text;

	public Text() {
	}

	public Text(String id, String text) {
		super();
		this.id = id;
		this.text = text;
	}

	public Text(ConsentTemplateKey ctKey, TextType type, String text) {
		super();
		this.id = createId(ctKey.getDomainName(), ctKey.getName(), ctKey.getVersion(), type);
		this.text = text;
	}

	public Text(ConsentKey consentKey, TextType type, String text) {
		super();
		ConsentTemplateKey ctKey = consentKey.getCtKey();
		this.id = createId(ctKey.getDomainName(),
				ctKey.getName() + DELIMITER + consentKey.getVirtualPersonId() + DELIMITER + consentKey.getConsentDate(), ctKey.getVersion(), type);
		this.text = text;
	}

	public Text(ModuleKey moduleKey, TextType type, String text) {
		super();
		this.id = createId(moduleKey.getDomainName(), moduleKey.getName(), moduleKey.getVersion(), type);
		this.text = text;
	}

	private static String createId(String domain, String name, int version, TextType type) {
		StringBuilder sb = new StringBuilder();
		sb.append(domain);
		sb.append(DELIMITER);
		sb.append(name);
		sb.append(DELIMITER);
		sb.append(version);
		sb.append(DELIMITER);
		sb.append(type.toString());
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		Text other = (Text) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "text for " + id;
	}
}
