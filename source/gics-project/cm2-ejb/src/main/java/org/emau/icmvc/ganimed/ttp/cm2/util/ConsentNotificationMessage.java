package org.emau.icmvc.ganimed.ttp.cm2.util;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2023 Trusted Third Party of the University Medicine Greifswald -
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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.emau.icmvc.ttp.json.JsonMapConverter;
import org.emau.icmvc.ttp.notification.NotificationMessage;

/**
 * A class to encapsulate a {@link ConsentKeyDTO},a map with {@link PolicyKeyDTO} as keys
 * and their consent status as values as well as some metadata as a notification message
 * which can be serialized to and deserialized from a JSON representation.
 */
public class ConsentNotificationMessage extends NotificationMessage
{
	private static final JsonMapConverter<PolicyKeyDTO, Boolean> POLICY_STATUS_CONVERTER =
			new JsonMapConverter<>(PolicyKeyDTO.class, Boolean.class);

	private ConsentKeyDTO consentKey;
	@JsonDeserialize(using = PolicyStatusDeserializer.class)
	@JsonSerialize(using = PolicyStatusSerializer.class)
	private Map<PolicyKeyDTO, Boolean> previousPolicyStates;
	@JsonDeserialize(using = PolicyStatusDeserializer.class)
	@JsonSerialize(using = PolicyStatusSerializer.class)
	private Map<PolicyKeyDTO, Boolean> currentPolicyStates;

	/**
	 * Creates an empty notification message (needed by deserialization).
	 */
	public ConsentNotificationMessage()
	{}

	/**
	 * Creates a notification message directly from its JSON represention.
	 */
	public ConsentNotificationMessage(String json) throws IOException
	{
		super(json);
	}

	/**
	 * Creates a notification message for the given values.
	 *
	 * @param consentKey
	 *            the consent key
	 * @param type
	 *            the message type
	 * @param clientId
	 *            the client ID
	 * @param comment
	 *            a comment
	 */
	public ConsentNotificationMessage(ConsentKeyDTO consentKey, String type, String clientId, String comment)
	{
		this(consentKey, null, type, clientId, comment);
	}

	/**
	 * Creates a notification message for the given values.
	 *
	 * @param consentKey
	 *            the consent key
	 * @param previousPolicyStates
	 *            a map with the previous policy status for referred consent
	 * @param type
	 *            the message type
	 * @param clientId
	 *            the client ID
	 * @param comment
	 *            a comment
	 */
	public ConsentNotificationMessage(ConsentKeyDTO consentKey, Map<PolicyKeyDTO, Boolean> previousPolicyStates, String type, String clientId, String comment)
	{
		this(consentKey, previousPolicyStates, null, type, clientId, comment, null);

	}

	/**
	 * Creates a notification message for the given values.
	 *
	 * @param consentKey
	 *            the consent key
	 * @param previousPolicyStates
	 *            a map with the previous policy status for referred consent
	 * @param currentPolicyStates
	 *            a map with the current policy status for referred consent
	 * @param type
	 *            the message type
	 * @param clientId
	 *            the client ID
	 * @param comment
	 *            a comment
	 */
	public ConsentNotificationMessage(ConsentKeyDTO consentKey, Map<PolicyKeyDTO, Boolean> previousPolicyStates, Map<PolicyKeyDTO, Boolean> currentPolicyStates,
			String type, String clientId, String comment, Map<String, Serializable> context)
	{
		super(type, clientId, comment, context);
		this.consentKey = consentKey;
		this.previousPolicyStates = previousPolicyStates;
		this.currentPolicyStates = currentPolicyStates;
	}

	/**
	 * Captures the content from the given message into this message.
	 *
	 * @param msg
	 *            the message to capture
	 */
	@Override
	public void capture(NotificationMessage msg)
	{
		super.capture(msg);

		if (msg instanceof ConsentNotificationMessage cMsg)
		{
			consentKey = cMsg.consentKey;
			previousPolicyStates = cMsg.previousPolicyStates == null ? null : new HashMap<>(cMsg.previousPolicyStates);
			currentPolicyStates = cMsg.currentPolicyStates == null ? null : new HashMap<>(cMsg.currentPolicyStates);
		}
	}

	public ConsentKeyDTO getConsentKey()
	{
		return consentKey;
	}

	public void setConsentKey(ConsentKeyDTO consentKey)
	{
		this.consentKey = consentKey;
	}

	public Map<PolicyKeyDTO, Boolean> getPreviousPolicyStates()
	{
		return previousPolicyStates;
	}

	public void setPreviousPolicyStates(Map<PolicyKeyDTO, Boolean> previousPolicyStates)
	{
		this.previousPolicyStates = previousPolicyStates;
	}

	public Map<PolicyKeyDTO, Boolean> getCurrentPolicyStates()
	{
		return currentPolicyStates;
	}

	public void setCurrentPolicyStates(Map<PolicyKeyDTO, Boolean> currentPolicyStates)
	{
		this.currentPolicyStates = currentPolicyStates;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		ConsentNotificationMessage that = (ConsentNotificationMessage) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(consentKey, that.consentKey)
				.append(previousPolicyStates, that.previousPolicyStates)
				.append(currentPolicyStates, that.currentPolicyStates)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(consentKey)
				.append(previousPolicyStates)
				.append(currentPolicyStates)
				.toHashCode();
	}

	@Override
	public String toString()
	{
		try
		{
			return toJson();
		}
		catch (JsonProcessingException e)
		{
			return super.toString();
		}
	}

	private static class PolicyStatusSerializer extends JsonMapConverter.Serializer<PolicyKeyDTO, Boolean>
	{
		public PolicyStatusSerializer()
		{
			super(POLICY_STATUS_CONVERTER);
		}
	}

	private static class PolicyStatusDeserializer extends JsonMapConverter.Deserializer<PolicyKeyDTO, Boolean>
	{
		public PolicyStatusDeserializer()
		{
			super(POLICY_STATUS_CONVERTER);
		}
	}
}
