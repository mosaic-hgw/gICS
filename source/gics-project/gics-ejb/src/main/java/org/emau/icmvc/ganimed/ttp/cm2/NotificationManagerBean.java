package org.emau.icmvc.ganimed.ttp.cm2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.SignerIdDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownSignerIdTypeException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentCache;
import org.emau.icmvc.ganimed.ttp.cm2.internal.DAO;
import org.emau.icmvc.ganimed.ttp.cm2.internal.OrgDatCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.SignerId;
import org.emau.icmvc.ganimed.ttp.cm2.util.CalcUtils;
import org.emau.icmvc.ganimed.ttp.cm2.util.ConsentNotificationSender;

import static org.emau.icmvc.ganimed.ttp.cm2.util.SystemKeys.SYSPROP_CHECK_POLICY_CHANGED_AT_TIME;
import static org.emau.icmvc.ganimed.ttp.cm2.util.SystemKeys.SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES;

/**
 * checks periodically (each day) or via call from extern (via web e.g.), if policy status have changed according to their legal-consentdates calculated by properties (valid, expiration) etc.
 * and sends notifications to the notification-service
 * @author Peter Penndorf
 */
@Singleton
@Startup
public class NotificationManagerBean extends TimerTask
{
	public static final String CLIENT_ID = "gics-service";
	private static final Logger LOGGER = LogManager.getLogger(NotificationManagerBean.class);
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private final Timer timer = new Timer(true);
	@EJB
	private DAO dao;
	@EJB
	private ConsentNotificationSender notificationSender;
	private static final ReentrantReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();


	@PostConstruct
	public void init()
	{
		if (System.getProperty(SYSPROP_CHECK_POLICY_CHANGED_AT_TIME) == null)
		{
			System.setProperty(SYSPROP_CHECK_POLICY_CHANGED_AT_TIME, "02:00");
		}
		String atTimeStr = System.getProperty(SYSPROP_CHECK_POLICY_CHANGED_AT_TIME);

		if (System.getProperty(SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES) == null)
		{
			System.setProperty(SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES, String.valueOf(1440));
		}
		long periodInMs = 1000L * 60 * Integer.parseInt(System.getProperty(SYSPROP_CHECK_POLICY_CHANGED_PERIOD_MINUTES));
			
		if (periodInMs == 86400000)
		{
			LOGGER.info("checking for policy status changes every day at time {}", atTimeStr);
		}
		else
		{
			LOGGER.info("checking for policy status changes every {} minutes with starting at time {}", periodInMs / 1000 / 60, atTimeStr);
		}

		LocalTime atTime = LocalTime.parse(atTimeStr, TIME_FORMATTER);
		LocalDateTime scheduledAtDT = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth(), atTime.getHour(), atTime.getMinute());
		timer.schedule(this, Date.from(scheduledAtDT.atZone(ZoneId.systemDefault()).toInstant()), periodInMs);
	}

	@Override
	public void run()
	{
		try
		{
			processChangedPoliciesForAllDomains();
		}
		catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	@PreDestroy
	public void destroy()
	{
		timer.cancel();
	}

	public void processChangedPoliciesForAllDomains() throws UnknownSignerIdTypeException, UnknownSignerIdException, InvalidVersionException, UnknownDomainException, VersionConverterClassException
	{
		processChangedPolicies(OrgDatCache.listDomainsCFEU(true));
	}

	public void processChangedPolicies(List<DomainDTO> domains)
			throws UnknownDomainException, InvalidVersionException, VersionConverterClassException, UnknownSignerIdTypeException, UnknownSignerIdException
	{
		READ_WRITE_LOCK.writeLock().lock();
		try
		{

			LOGGER.info(">>> processing changed policies");
			Date toDate = new Date();
			for (DomainDTO domain : domains)
			{
				if (Boolean.FALSE.equals(domain.getConfig().getNotificationsConfig().isSendPolicyValidityChanged()))
				{
					LOGGER.info("skipping domain '{}' to check for changed policies", domain.getName());
					continue;
				}
				Date fromDate = null;
				String domainName = domain.getName();
				LOGGER.debug("processing domain {}", domainName);
				if (domain.getLastCheckDate() == null)
				{
					//from yesterday to now
					fromDate = Date.from(LocalDateTime.ofInstant(toDate.toInstant(), ZoneId.systemDefault()).minusDays(1).atZone(ZoneId.systemDefault()).toInstant());
				}
				else
				{
					fromDate = domain.getLastCheckDate();
				}
				List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies = ConsentCache.getChangedPolicies(domainName, fromDate, toDate);
				LOGGER.info("found {} changed policies for domain '{}' between '{}' and '{}'", changedPolicies.size(), domainName, fromDate, toDate);
				if (!changedPolicies.isEmpty())
					sendNotiForDomain(domainName, changedPolicies);
				domain.setLastCheckDate(toDate);
				dao.updateDomainInUse(domain.getDomainName(), domain.getLastCheckDate());
				OrgDatCache.updateDomain(domain);
			}
			LOGGER.info("<<< processing changed policies");
		}
		finally
		{
			READ_WRITE_LOCK.writeLock().unlock();
		}
	}

	private void sendNotiForDomain(String domainName, List<CalcUtils.ChangedSignedPolicyValidity> changedPolicies)
			throws UnknownDomainException, InvalidVersionException, UnknownSignerIdTypeException, UnknownSignerIdException
	{
		Map<SignerId, List<CalcUtils.ChangedSignedPolicyValidity>> policiesMap = CalcUtils.groupPoliciesBySignerId(changedPolicies, dao::getSignerIdsForVP, true);
		//get aliases and convert map
		Map<Pair<SignerIdDTO, List<SignerIdDTO>>, List<CalcUtils.ChangedSignedPolicyValidity>> resultMap = new HashMap<>();
		for (Map.Entry<SignerId, List<CalcUtils.ChangedSignedPolicyValidity>> entry : policiesMap.entrySet())
		{
			List<SignerIdDTO> aliase = dao.listAliases(domainName, entry.getKey().toDTO());
			resultMap.put(Pair.of(entry.getKey().toDTO(), aliase), entry.getValue());
		}
		notificationSender.sendPoliciesChangedNotification(CLIENT_ID, domainName, resultMap);
	}
}
