package org.emau.icmvc.ganimed.ttp.cm2.model;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
 * __
 * Copyright (C) 2014 - 2024 Trusted Third Party of the University Medicine Greifswald -
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

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.UuidGenerator;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.cm2.config.QualityControlConfig;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDateValuesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentScanDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ExpressionExpirationPropertiesDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.FreeTextValDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.NoValueException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.RequirementsNotFullfilledException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ConsentDateValues;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.ExpressionExpirationPropertiesObject;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;
import org.emau.icmvc.ganimed.ttp.cm2.model.enums.SignatureType;

/**
 * ein consent ist ein ausgefuelltes und unterschriebenes konsentdokument (consent template)
 *
 * @author geidell
 */
@Entity
@Table(name = "consent")
@Cache(isolation = CacheIsolationType.ISOLATED)
@UuidGenerator(name = "FHIR_ID_C")
public class Consent implements Serializable, FhirDTOExporter<ConsentDTO>
{
	@Serial
	private static final long serialVersionUID = -5975669725630538901L;
	private static final Logger logger = LogManager.getLogger(Consent.class);
	@EmbeddedId
	private ConsentKey key;
	// muss "eager" sein - auf das feld wird nicht direkt zugegriffen und daher weiss jpa nicht, wann das nachgeladen werden muss
	@OneToMany(mappedBy = "consent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	// kein batchfetch - mysql kriegt das resultierende sql nicht vernuenftig optimiert wenn mehr als ein consent zu einer person existiert
	private List<Signature> signatures = new ArrayList<>();
	@Column(name = "PATIENTSIGNATURE_IS_FROM_GUARDIAN")
	private Boolean patientSignatureIsFromGuardian;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "VERSION") })
	@MapsId("ctKey")
	private ConsentTemplate consentTemplate;
	private String physicianId;
	private String comment;
	@Transient
	private ExpirationPropertiesObject expirationPropertiesObject = null;
	@Column(name = "EXPIRATION_PROPERTIES")
	private String expirationProperties;
	@Column(name = "EXTERN_PROPERTIES", length = 4095)
	private String externProperties;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "consent")
	private List<ConsentScan> scans = new ArrayList<>();

	@OneToMany(mappedBy = "consent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<SignedPolicy> signedPolicies = new ArrayList<>();
	@OneToMany(mappedBy = "consent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<FreeTextVal> freeTextValues = new ArrayList<>();
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "ID")
	@MapsId("virtualPersonId")
	private VirtualPerson virtualPerson;
	@Column(name = "CREATE_TIMESTAMP", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "UPDATE_TIMESTAMP", nullable = false)
	private Timestamp updateTimestamp;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "VALID_FROM", nullable = true)
	private Date validFrom;
	@OneToOne(fetch = FetchType.EAGER, mappedBy = "consent", cascade = CascadeType.ALL)
	private QC qc;
	@Column(name = "FHIR_ID", length = 41)
	@GeneratedValue(generator = "FHIR_ID_C")
	private String fhirID;
	@Transient
	private ConsentDateValues consentDateValues = null;
	@Transient
	private boolean hasPatientSignature = false;
	@Transient
	private final Object consentDateValuesSyncObject = new Object();
	@Transient
	private Map<String, String> metaData = new HashMap<>();

	public Consent()
	{
		// diese ueberfluessigen zuweisungen stehen hier nur, damit kein autocode die felder als final markiert. jpa setzt diese per reflection
		signatures = new ArrayList<>();
		scans = new ArrayList<>();
		signedPolicies = new ArrayList<>();
		freeTextValues = new ArrayList<>();
	}

	public Consent(ConsentTemplate ct, ConsentDTO dto, Map<ModuleKeyDTO, Module> modules, VirtualPerson signer, DomainConfig domainProperties)
			throws MissingRequiredObjectException, InvalidFreeTextException, InvalidParameterException
	{
		super();
		Map<String, FreeTextDef> freeTextDefs = new HashMap<>();
		for (FreeTextDef freeTextDef : ct.getFreeTextDefs())
		{
			freeTextDefs.put(freeTextDef.getKey().getName(), freeTextDef);
			if (freeTextDef.getRequired())
			{
				try
				{
					dto.getFreeTextValForDef(freeTextDef.getKey().getName());
				}
				catch (NoValueException e)
				{
					throw new MissingRequiredObjectException("there's no value for the mandatory free text field '" + freeTextDef.getKey().getName());
				}
			}
		}
		key = new ConsentKey(ct.getKey(), dto.getKey().getConsentDate(), signer.getId());
		physicianId = dto.getPhysicianId();
		signatures.add(new Signature(this, SignatureType.PATIENT, dto.getPatientSignatureBase64(), dto.getPatientSigningDate(), dto.getPatientSigningPlace()));
		patientSignatureIsFromGuardian = dto.getPatientSignatureIsFromGuardian();
		hasPatientSignature = StringUtils.isNotEmpty(dto.getPatientSignatureBase64()) && !ConsentLightDTO.NO_SIGNATURE.equals(dto.getPatientSignatureBase64())
							  && !ConsentLightDTO.NO_REAL_SIGNATURE.equals(dto.getPatientSignatureBase64());
		signatures.add(new Signature(this, SignatureType.PHYSICIAN, dto.getPhysicianSignatureBase64(), dto.getPhysicianSigningDate(), dto.getPhysicianSigningPlace()));
		for (ConsentScanDTO csDTO : dto.getScans())
		{
			scans.add(new ConsentScan(this, csDTO.getBase64(), csDTO.getFileType(), csDTO.getFileName()));
		}
		this.comment = dto.getComment();
		this.expirationPropertiesObject = new ExpirationPropertiesObject(dto.getExpirationProperties());
		this.expirationProperties = expirationPropertiesObject.toPropertiesString();
		this.externProperties = dto.getExternProperties();
		this.metaData = dto.getMetaData();
		for (Entry<ModuleKeyDTO, ModuleStateDTO> moduleState : dto.getModuleStates().entrySet())
		{
			for (ModulePolicy mp : modules.get(moduleState.getKey()).getModulePolicies())
			{
				SignedPolicy signedPolicy = new SignedPolicy(this, mp.getPolicy(), moduleState.getValue().getConsentState());
				signedPolicies.add(signedPolicy);
			}
		}
		for (FreeTextValDTO freeTextValDTO : dto.getFreeTextVals())
		{
			FreeTextDef freeTextDef = freeTextDefs.get(freeTextValDTO.getFreeTextDefName());
			if (freeTextDef == null)
			{
				throw new InvalidFreeTextException("unknown free text with name '" + freeTextValDTO.getFreeTextDefName() + "'");
			}
			FreeTextVal freeTextVal = new FreeTextVal(this, freeTextValDTO.getFreeTextDefName(), freeTextValDTO.getValue(), freeTextDef);
			freeTextValues.add(freeTextVal);
		}
		virtualPerson = signer;
		consentTemplate = ct;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		createTimestamp = timestamp;
		updateTimestamp = timestamp;
		validFrom = dto.getValidFromDate();
		QCDTO qcDTO = dto.getQualityControl() != null ? dto.getQualityControl() : new QCDTO();
		qc = new QC(this, qcDTO, timestamp);
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void loadPropertiesFromString()
	{
		try
		{
			expirationPropertiesObject = new ExpirationPropertiesObject(expirationProperties);
		}
		catch (ParseException e)
		{
			logger.fatal("exception while parsing expirationProperties '{}'", expirationProperties, e);
		}
	}

	/**
	 * this method is called by jpa
	 */
	@PrePersist
	public void fhirIDToLowerCase()
	{
		fhirID = fhirID.toLowerCase();
	}

	public ConsentKey getKey()
	{
		return key;
	}

	public Signature getPatientSignature()
	{
		Signature result = null;
		for (Signature signature : signatures)
		{
			if (signature.getKey().getType().equals(SignatureType.PATIENT))
			{
				result = signature;
				break;
			}
		}
		return result;
	}

	public Signature getPhysicianSignature()
	{
		Signature result = null;
		for (Signature signature : signatures)
		{
			if (signature.getKey().getType().equals(SignatureType.PHYSICIAN))
			{
				result = signature;
				break;
			}
		}
		return result;
	}

	public ConsentTemplate getConsentTemplate()
	{
		return consentTemplate;
	}

	public String getPhysicianId()
	{
		return physicianId;
	}

	public String getComment()
	{
		return comment;
	}

	public ExpirationPropertiesObject getExpirationPropertiesObject()
	{
		return expirationPropertiesObject;
	}

	public String getExternProperties()
	{
		return externProperties;
	}

	public Map<String, String> getMetaData()
	{
		return metaData;
	}

	public void setMetaData(Map<String, String> metaData)
	{
		this.metaData = metaData;
	}

	public void addScan(ConsentScan scan)
	{
		scans.add(scan);
	}

	public void removeScan(String fhirID)
	{
		if (fhirID != null)
		{
			scans.removeIf(s -> s.getFhirID().equals(fhirID));
		}
	}

	public List<ConsentScan> getScans()
	{
		return scans;
	}

	public List<SignedPolicy> getSignedPolicies()
	{
		return signedPolicies;
	}

	public List<FreeTextVal> getFreeTextValues()
	{
		return freeTextValues;
	}

	public VirtualPerson getVirtualPerson()
	{
		return virtualPerson;
	}

	public Timestamp getCreateTimestamp()
	{
		return createTimestamp;
	}

	public Timestamp getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	public Date getValidFrom()
	{
		return validFrom;
	}

	public QC getQc()
	{
		return qc;
	}

	public String getFhirID()
	{
		return fhirID;
	}

	public ConsentDateValues getConsentDateValues()
	{
		return getConsentDateValues(false);
	}

	public ConsentDateValues getConsentDateValues(boolean recalculate)
	{
		synchronized (consentDateValuesSyncObject)
		{
			if (recalculate || consentDateValues == null)
			{
				consentDateValues = calculateConsentDateValues();
			}
		}
		return consentDateValues;
	}

	public ConsentDateValuesDTO getConsentDatesDTO()
	{
		return getConsentDateValues().toDTO(consentTemplate);
	}

	public void updateInUse(String comment, String externProperties, ConsentScanDTO scan) throws InvalidParameterException
	{
		this.comment = comment;
		this.externProperties = externProperties;

		if (scan != null)
		{
			boolean found = false;
			Iterator<ConsentScan> it = scans.iterator();
			while (it.hasNext() && !found)
			{
				ConsentScan tmp = it.next();
				if (tmp.getFhirID().equals(scan.getFhirID()))
				{
					tmp.update(scan.getBase64(), scan.getFileType(), scan.getFileName());
					found = true;
				}
			}

			if (!found)
			{
				scans.add(new ConsentScan(this, scan.getBase64(), scan.getFileType(), scan.getFileName()));
			}
		}

		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void updateQC(QCDTO qcDTO, QualityControlConfig config) throws InvalidParameterException
	{
		QCDTO oldQC = qc.toDTO();
		QCDTO newQC = new QCDTO(qcDTO);

		if (!oldQC.equals(newQC))
		{
			updateTimestamp = new Timestamp(System.currentTimeMillis()); // consent changed

			oldQC.setProblems(null);
			newQC.setProblems(null);

			if (!oldQC.equals(newQC)) // there are qc changes even when ignoring problems
			{
				qc.update(qcDTO, updateTimestamp, config); // update all including problems
			}
			else
			{
				qc.updateProblems(qcDTO, updateTimestamp); // only update problems
			}
		}
	}

	public ConsentLightDTO toLightDTO() throws InvalidVersionException, InconsistentStatusException, UnknownDomainException
	{
		ConsentLightDTO result = new ConsentLightDTO(
				key.toDTO(VersionConverterCache.getCTVersionConverter(consentTemplate.getDomain().getName()), virtualPerson));
		result.setTemplateType(consentTemplate.getType());
		result.setPatientSigningDate(getPatientSignature() != null ? getPatientSignature().getSignatureDate() : null);
		result.setPatientSigningPlace(getPatientSignature() != null ? getPatientSignature().getSignaturePlace() : null);
		hasPatientSignature = getPatientSignature() != null && StringUtils.isNotEmpty(getPatientSignature().getSignatureScanBase64())
							  && !ConsentLightDTO.NO_SIGNATURE.equals(getPatientSignature().getSignatureScanBase64())
							  && !ConsentLightDTO.NO_REAL_SIGNATURE.equals(getPatientSignature().getSignatureScanBase64());
		result.setHasPatientSignature(hasPatientSignature);
		result.setPatientSignatureIsFromGuardian(patientSignatureIsFromGuardian);
		result.setPhysicianId(physicianId);
		result.setPhysicianSigningDate(getPhysicianSignature() != null ? getPhysicianSignature().getSignatureDate() : null);
		result.setPhysicianSigningPlace(getPhysicianSignature() != null ? getPhysicianSignature().getSignaturePlace() : null);
		result.setComment(comment);
		result.setExternProperties(externProperties);
		result.setExpirationProperties(expirationPropertiesObject.toDTO());
		result.setValidFromProperties(consentTemplate.getValidFromPropertiesObject() != null ? consentTemplate.getValidFromPropertiesObject().toDTO() : null);
		result.setCreationDate(new Date(createTimestamp.getTime()));
		result.setUpdateDate(new Date(updateTimestamp.getTime()));
		result.setValidFromDate(validFrom);
		HashMap<PolicyKeyDTO, ConsentStatus> policyStates = new HashMap<>();
		for (SignedPolicy signedPolicy : signedPolicies)
		{
			policyStates.put(signedPolicy.getKey().getPolicyKey()
					.toDTO(VersionConverterCache.getPolicyVersionConverter(consentTemplate.getDomain().getName())), signedPolicy.getStatus());
		}
		Map<ModuleKeyDTO, ModuleStateDTO> moduleStates = new HashMap<>();
		for (ModuleConsentTemplate mct : consentTemplate.getModuleConsentTemplates())
		{
			ConsentStatus moduleStatus = null;
			List<PolicyKeyDTO> policyKeys = new ArrayList<>();
			for (ModulePolicy mp : mct.getModule().getModulePolicies())
			{
				PolicyKeyDTO policyKey = mp.getPolicy().getKey()
						.toDTO(VersionConverterCache.getPolicyVersionConverter(consentTemplate.getDomain().getName()));
				ConsentStatus tempModuleStatus = policyStates.get(policyKey);
				if (moduleStatus == null)
				{
					moduleStatus = tempModuleStatus;
				}
				else if (!moduleStatus.equals(tempModuleStatus))
				{
					String sb = "inconsistence for "
								+ key
								+ " signed consent for "
								+ mp.getPolicy().getKey()
								+ " status should be "
								+ moduleStatus
								+ " but is "
								+ tempModuleStatus;
					throw new InconsistentStatusException(sb);
				}
				policyKeys.add(policyKey);
			}
			ModuleKeyDTO moduleKey = mct.getModule().getKey()
					.toDTO(VersionConverterCache.getModuleVersionConverter(consentTemplate.getDomain().getName()));
			moduleStates.put(moduleKey, new ModuleStateDTO(moduleKey, moduleStatus, policyKeys));
		}
		result.setModuleStates(moduleStates);
		result.setQualityControl(qc != null ? qc.toDTO() : new QCDTO());
		result.setFhirID(fhirID);
		result.setConsentDates(getConsentDateValues().toDTO(consentTemplate));

		return result;
	}

	@Override
	public ConsentDTO toDTO() throws InvalidVersionException, InconsistentStatusException, UnknownDomainException
	{
		ConsentDTO result = new ConsentDTO(toLightDTO());
		result.setPatientSignatureBase64(getPatientSignature() != null ? getPatientSignature().getSignatureScanBase64() : null);
		result.setPhysicianSignatureBase64(getPhysicianSignature() != null ? getPhysicianSignature().getSignatureScanBase64() : null);

		List<ConsentScanDTO> base64Scans = new ArrayList<>();
		for (ConsentScan cs : scans)
		{
			base64Scans.add(new ConsentScanDTO(cs.getFhirID(), result.getKey(), cs.getContent(), cs.getFileType(), cs.getFileName(), cs.getUploadDate()));
		}

		result.setScans(base64Scans);
		List<FreeTextValDTO> freeTextValueList = new ArrayList<>();
		for (FreeTextVal freeTextVal : freeTextValues)
		{
			freeTextValueList.add(freeTextVal.toDTO());
		}
		result.setFreeTextVals(freeTextValueList);
		return result;
	}

	private ConsentDateValues calculateConsentDateValues()
	{
		ConsentDateValues result = new ConsentDateValues();
		ConsentTemplate template = getConsentTemplate();
		Date calculatedConsentDate = ConsentDateValues.START_DATE;
		//calculate legalConsentDate
		Date validFromPropsDate = template.getValidFromPropertiesObject() != null ?
				template.getValidFromPropertiesObject().getValidFromDateForConsentDate(DateUtils.truncate(this.getCreateTimestamp(), Calendar.DATE)) :
				null;
		if (validFromPropsDate != null && validFromPropsDate.after(calculatedConsentDate))
		{
			calculatedConsentDate = validFromPropsDate;
		}
		if (validFrom != null && validFrom.after(calculatedConsentDate))
		{
			calculatedConsentDate = validFrom;
		}
		for (Signature sig : signatures)
		{
			if (sig.getSignatureDate() != null && sig.getSignatureDate().after(calculatedConsentDate))
			{
				calculatedConsentDate = sig.getSignatureDate();
			}
		}
		if (ConsentDateValues.START_DATE.equals(calculatedConsentDate))
		{
			calculatedConsentDate = key.getConsentDate();
		}
		result.setLegalConsentTimestamp(calculatedConsentDate.getTime());
		result.setGicsConsentTimestamp(calculatedConsentDate.after(new Date(createTimestamp.getTime())) ? calculatedConsentDate.getTime() : createTimestamp.getTime());

		//calculate expirationDate
		Date domainExpiration = template.getDomain().getExpirationPropertiesObject().getExpirationDateForConsentDate(calculatedConsentDate);
		Date consentTemplateExpiration = template.getExpirationPropertiesObject().getExpirationDateForConsentDate(calculatedConsentDate);
		Date consentExpiration = getExpirationPropertiesObject().getExpirationDateForConsentDate(calculatedConsentDate);
		consentExpiration = domainExpiration.before(consentExpiration) ? domainExpiration : consentExpiration;
		consentExpiration = consentTemplateExpiration.before(consentExpiration) ? consentTemplateExpiration : consentExpiration;
		result.setConsentExpirationTimestamp(consentExpiration.getTime());
		for (ModuleConsentTemplate moduleCTP : template.getModuleConsentTemplates())
		{
			Date moduleExpiration = moduleCTP.getExpirationPropertiesObject().getExpirationDateForConsentDate(calculatedConsentDate);
			moduleExpiration = moduleExpiration.before(consentExpiration) ? moduleExpiration : consentExpiration;
			for (ModulePolicy mp : moduleCTP.getModule().getModulePolicies())
			{
				SignedPolicy signedPolicy = getSignedPolicyByKey(mp.getPolicy().getKey());
				//just get the expirationdate which was set in the policy when consent was created first time, no calculation needed
				if (signedPolicy != null)
				{
					ExpressionExpirationPropertiesObject signedPolicyExpirationPropertiesObject = signedPolicy.getExpirationPropertiesObject();
					Date expDateOfSignedPolicy = signedPolicyExpirationPropertiesObject.getExpirationDateForConsentDate(calculatedConsentDate);
					if (expDateOfSignedPolicy.before(moduleExpiration))
					{
						result.getPolicyExpirations().put(mp.getPolicy().getKey(), expDateOfSignedPolicy.getTime());
						continue;
					}
				}
				Date policyExpiration = mp.getExpirationPropertiesObject().getExpirationDateForConsentDate(calculatedConsentDate);
				policyExpiration = policyExpiration.before(moduleExpiration) ? policyExpiration : moduleExpiration;
				result.getPolicyExpirations().put(mp.getPolicy().getKey(), policyExpiration.getTime());
			}
		}
		return result;
	}

	private SignedPolicy getSignedPolicyByKey(PolicyKey key)
	{
		for (SignedPolicy signedPolicy : signedPolicies)
		{
			if (signedPolicy.getPolicy().getKey().equals(key))
				return signedPolicy;
		}
		logger.warn("signed policy {} is not part of this consent", key);
		return null;
	}

	/**
	 * loops through all policies and calculates the expiration-date of each if an expirationDateExpression exists
	 * <br/>
	 * <b>should only be used once when a consent is added to the database</b>
	 *
	 * @throws RequirementsNotFullfilledException
	 * 		when the expirationExpression is failing. Or if policy does not have a default expiration-date (which should not happen)
	 */
	public void calculateAndSetSignedPolicyExpirationDates() throws RequirementsNotFullfilledException
	{
		ConsentDateValues dateValues = getConsentDateValues();
		for (ModuleConsentTemplate moduleCTP : getConsentTemplate().getModuleConsentTemplates())
		{
			for (ModulePolicy mp : moduleCTP.getModule().getModulePolicies())
			{
				Date expirationDateFromExpression = mp.getExpirationPropertiesObject().getExpirationDateForConsentDate(getCreateTimestamp(), getMetaData());
				if (expirationDateFromExpression != null)
				{
					Long oldExpiration = dateValues.getPolicyExpirations().get(mp.getPolicy().getKey());
					//should not happen, but just in case
					if (oldExpiration == null)
						throw new RequirementsNotFullfilledException("could not find calculated expirationDate for policyKey {}" + mp.getPolicy().getKey());
					if (expirationDateFromExpression.getTime() < oldExpiration)
					{
						for (SignedPolicy signedPolicy : getSignedPolicies())
						{
							if (signedPolicy.getPolicy().getKey().equals(mp.getPolicy().getKey()))
							{
								ExpressionExpirationPropertiesObject expPropsObject = signedPolicy.getExpirationPropertiesObject()
										.createMergedExpirationProperties(
												new ExpressionExpirationPropertiesDTO(expirationDateFromExpression, signedPolicy.getExpirationPropertiesObject().getValidDuration(), null),
												false);
								signedPolicy.setExpirationProperties(expPropsObject
										.toPropertiesString());
								break;
							}
						}
					}
				}
			}
		}
		//needs to be done because expression might have changed the expirationDate of policy but was not used in the calculation of dates
		getConsentDateValues(true);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (key == null ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Consent other = (Consent) obj;
		if (!Objects.equals(key, other.key))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with comment '");
		sb.append(comment);
		sb.append("', extern properties: ");
		sb.append(externProperties);
		sb.append("', expiration properties: ");
		sb.append(expirationProperties);
		sb.append("', ");
		sb.append(signedPolicies.size());
		sb.append(" signed policies and ");
		sb.append(freeTextValues.size());
		sb.append(" free text values");
		sb.append(". FHIR-ID: ");
		sb.append(fhirID);
		return sb.toString();
	}
}
