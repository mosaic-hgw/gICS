package org.emau.icmvc.ganimed.ttp.cm2;

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
import java.net.ConnectException;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.emau.icmvc.ganimed.ttp.cm2.util.NotificationMessage;
import org.emau.icmvc.ttp.notification.interfaces.INotificationClient;

/**
 * An utility to send notification messages via the THS notification-service.
 */
@Stateless
public class NotificationSender
{

	private static final Logger logger = LogManager.getLogger(NotificationSender.class);

	/*
	JNDI bindings for session bean named 'notification-client-default' in
	deployment unit 'subdeployment "ths-notification-client-1.0.0.jar" of deployment "ths-notification-client-ear-1.0.0.ear"'
	are as follows:

	java:global/notification-client/notification-client-ejb/notification-client-default!org.emau.icmvc.ttp.notification.interfaces.INotificationClient
	java:app/notification-client-ejb/notification-client-default!org.emau.icmvc.ttp.notification.interfaces.INotificationClient
	java:module/notification-client-default!org.emau.icmvc.ttp.notification.interfaces.INotificationClient
	java:jboss/exported/notification-client/notification-client-ejb/notification-client-default!org.emau.icmvc.ttp.notification.interfaces.INotificationClient
	ejb:notification-client/notification-client-ejb/notification-client-default!org.emau.icmvc.ttp.notification.interfaces.INotificationClient
	java:global/notification-client/notification-client-ejb/notification-client-default
	java:app/notification-client-ejb/notification-client-default
	java:module/notification-client-default
	*/
	public static final String NOTIFICATION_CLIENT_NAME = "java:global/notification-client/notification-client-ejb/notification-client-default";

	private static INotificationClient notificationClient;

	/**
	 * Lookup client and cache it.
	 *
	 * @return the notification client or null if lookup failed
	 */
	private static INotificationClient lookupClient()
	{
		if (notificationClient == null)
		{
			try
			{
				notificationClient = InitialContext.doLookup(NOTIFICATION_CLIENT_NAME);
			}
			catch (NamingException e)
			{
				logger.warn("Cannot send notification: lookup for notification client failed", e);
			}
		}
		return notificationClient;
	}

	/**
	 * Send a notification with the given message.
	 * The notification message must contain a non-blank client ID ({@link NotificationMessage#getClientId()})
	 * as well as a non-blank event type ({@link NotificationMessage#getType()}), otherwise an IllegalArgumentException
	 * will be thrown.
	 *
	 * @param msg the message to send
	 * @return true if the notification message could be sent successfully
	 * @throws IllegalArgumentException when the client ID or the event type of the message is blank
	 */
	public boolean sendNotification(NotificationMessage msg)
	{
		try
		{
			String json = msg.toJson();
			new NotificationMessage(json); // ensure sending faultlessly deserializable messages only
			return sendNotification(new JSONObject(json), msg.getClientId(), msg.getType());
		}
		catch (JSONException | IOException shouldNotHappen)
		{
			// TODO(FMM): maybe better to rethrow as an IllegalStateException?
			//  to let the whole transaction fail instead to warn only when sending notification failed
			//   maybe optionally (via domain config) as an improvement in a later version
			logger.error("Unexpected exception while creating notification message for " + msg.getType() + " from " + msg.getClientId(), shouldNotHappen);
		}
		return false;
	}

	/**
	 * Send a notification with the given message.
	 * Both the given client ID and event type must not be blank, otherwise an IllegalArgumentException will be thrown.
	 *
	 * @param msg the message to send
	 * @return true if the notification message could be sent successfully
	 * @throws IllegalArgumentException when the client ID or the event type of the message is blank
	 */
	public boolean sendNotification(JSONObject msg, String clientId, String type)
	{
		if (StringUtils.isBlank(clientId))
		{
			throw new IllegalArgumentException("Cannot send notification with a blank clientId: '" + clientId + "'");
		}

		if (StringUtils.isBlank(type))
		{
			throw new IllegalArgumentException("Cannot send notification with a blank type: '" + type + "'");
		}

		INotificationClient client = lookupClient();
		// TODO(FMM): maybe better to throw an IllegalStateException when client lookup failed?
		//   to let the whole transaction fail instead to warn only when sending notification failed,
		//   maybe optionally (via domain config) as an improvement in a later version
		if (client != null)
		{
			try
			{
				msg.put("clientId", clientId); // accept to overwrite notification client ID in msg if already exists
				msg.put("type", type); // accept to overwrite event type in msg if already exists

				if (logger.isDebugEnabled())
				{
					logger.debug("Sending notification for " + type + " from " + clientId + "\n" + msg.toString(2));
				}
				notificationClient.sendNotification(type, msg, clientId);
				logger.debug("Sent notification for " + type + " from " + clientId);
				return true;
			}
			catch (ConnectException e)
			{
				// TODO(FMM): maybe better to rethrow as an IllegalStateException?
				//   to let the whole transaction fail instead to warn only when sending notification failed?
				//   maybe optionally (via domain config) as an improvement in a later version
				logger.error("Could not send notification for " + type + " from " + clientId, e);
			}
			catch (JSONException shouldNotHappen)
			{
				// TODO(FMM): maybe better to rethrow as an IllegalStateException
				//   to let the whole transaction fail instead to warn only when sending notification failed?
				//   maybe optionally (via domain config) as an improvement in a later version
				logger.error("Unexpected exception while creating notification message for " + type + " from " + clientId, shouldNotHappen);
			}
		}
		return false;
	}
}
