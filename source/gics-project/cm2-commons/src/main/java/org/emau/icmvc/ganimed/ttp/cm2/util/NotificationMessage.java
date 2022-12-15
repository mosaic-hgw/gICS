package org.emau.icmvc.ganimed.ttp.cm2.util;

/*-
 * ###license-information-start###
 * E-PIX - Enterprise Patient Identifier Cross-referencing
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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A class to encapsulate a {@link ConsentKeyDTO},a map with {@link PolicyKeyDTO} as keys
 * and their consent status as values as well as some metadata as a notification message
 * which can be serialized to and deserialized from a JSON representation.
 */
public class NotificationMessage
{
	private static final JsonMapConverter<PolicyKeyDTO, Boolean> POLICY_STATUS_CONVERTER = new JsonMapConverter<>(PolicyKeyDTO.class, Boolean.class);
	private static final ObjectReader jsonReader = JsonConverter.createDefaultObjectMapper().readerFor(NotificationMessage.class);
	private static final ObjectWriter jsonWriter = JsonConverter.createDefaultObjectMapper().writerFor(NotificationMessage.class);

	private ConsentKeyDTO consentKey;
	@JsonDeserialize(using = PolicyStatusDeserializer.class)
	@JsonSerialize(using = PolicyStatusSerializer.class)
	private Map<PolicyKeyDTO, Boolean> previousPolicyStates;
	@JsonDeserialize(using = PolicyStatusDeserializer.class)
	@JsonSerialize(using = PolicyStatusSerializer.class)
	private Map<PolicyKeyDTO, Boolean> currentPolicyStates;
	private String type;
	private String clientId;
	private String comment;
	private Map<String, Serializable> context;

	/**
	 * Creates an empty notification message (needed by deserialization).
	 */
	public NotificationMessage()
	{}

	/**
	 * Creates a notification message directly from its JSON represention.
	 */
	public NotificationMessage(String json) throws IOException
	{
		fromJson(json);
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
	public NotificationMessage(ConsentKeyDTO consentKey, String type, String clientId, String comment)
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
	public NotificationMessage(ConsentKeyDTO consentKey, Map<PolicyKeyDTO, Boolean> previousPolicyStates, String type, String clientId, String comment)
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
	public NotificationMessage(ConsentKeyDTO consentKey, Map<PolicyKeyDTO, Boolean> previousPolicyStates, Map<PolicyKeyDTO, Boolean> currentPolicyStates,
			String type, String clientId, String comment, Map<String, Serializable> context)
	{
		this.consentKey = consentKey;
		this.previousPolicyStates = previousPolicyStates;
		this.currentPolicyStates = currentPolicyStates;
		this.type = type;
		this.clientId = clientId;
		this.comment = comment;
		if (context != null)
		{
			context = new HashMap<>(context); // do not pass a direct reference
		}
		this.context = context;
	}

	/**
	 * Captures the content from the given message into this message.
	 *
	 * @param msg
	 *            the message to capture
	 */
	public void capture(NotificationMessage msg)
	{
		setConsentKey(msg.consentKey);
		setClientId(msg.clientId);
		setType(msg.type);
		setComment(msg.comment);
		previousPolicyStates = msg.previousPolicyStates == null ? null : new HashMap<>(msg.previousPolicyStates);
		currentPolicyStates = msg.currentPolicyStates == null ? null : new HashMap<>(msg.currentPolicyStates);
		context = msg.context == null ? null : new HashMap<>(msg.context);
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

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * Returns a non-null modifiable direct reference to the context map
	 *
	 * @return a non-null modifiable direct reference to the context map
	 */
	public Map<String, Serializable> getContext()
	{
		synchronized (this)
		{
			if (context == null)
			{
				context = new HashMap<>();
			}
			return context;
		}
	}

	public void setContext(Map<String, Serializable> context)
	{
		synchronized (this)
		{
			if (context != null)
			{
				context = new HashMap<>(context); // do not pass a direct reference
			}
			this.context = context;
		}
	}

	/**
	 * Returns the JSON representation for this message.
	 *
	 * @return the JSON representation for the given object
	 * @throws JsonProcessingException
	 *             if the object cannot be converted to JSON
	 */
	public String toJson() throws JsonProcessingException
	{
		return jsonWriter.writeValueAsString(this);
	}

	/**
	 * Reads the given JSON representation into this message.
	 *
	 * @param json
	 *            the JSON representation to convert
	 * @throws IOException
	 *             if the JSON string cannot be converted into an object
	 */
	public void fromJson(String json) throws IOException
	{
		capture(jsonReader.readValue(json, NotificationMessage.class));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		NotificationMessage that = (NotificationMessage) o;

		return new EqualsBuilder().append(consentKey, that.consentKey)
				.append(previousPolicyStates, that.previousPolicyStates).append(currentPolicyStates, that.currentPolicyStates)
				.append(type, that.type).append(clientId, that.clientId).append(comment, that.comment).append(context, that.context).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37).append(consentKey)
				.append(previousPolicyStates).append(currentPolicyStates).append(type).append(clientId).append(comment).append(context).toHashCode();
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
