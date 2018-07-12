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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentLightDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleStateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InternalException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidPropertiesException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;
import org.emau.icmvc.ganimed.ttp.cm2.internal.VersionConverterCache;

/**
 * ein consent ist ein ausgefuelltes und unterschriebenes konsentdokument (consent template)
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "consent")
@Cache(isolation = CacheIsolationType.ISOLATED)
public class Consent implements Serializable {

	private static final long serialVersionUID = 5603707985566847520L;
	@EmbeddedId
	private ConsentKey key;
	// muss "eager" sein - auf das feld wird nicht direkt zugegriffen und daher weiss jpa nicht, wann das nachgeladen werden muss
	@OneToMany(mappedBy = "consent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	// kein batchfetch - mysql kriegt das resultierende sql nicht vernuenftig optimiert wenn mehr als ein consent zu einer person existiert
	private List<Signature> signatures = new ArrayList<Signature>();
	@Column(name = "PATIENTSIGNATURE_IS_FROM_GUARDIAN")
	private Boolean patientSignatureIsFromGuardian;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "VERSION") })
	@MapsId("ctKey")
	private ConsentTemplate consentTemplate;
	private String physicanId;
	private String comment;
	@Column(name = "EXTERN_PROPERTIES")
	private String externProperties;
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "SCAN_BASE64", referencedColumnName = "ID")
	private Text scanBase64;
	private String scanFileType;
	@OneToMany(mappedBy = "consent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<SignedPolicy> signedPolicies = new ArrayList<SignedPolicy>();
	@OneToMany(mappedBy = "consent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<FreeTextVal> freeTextValues = new ArrayList<FreeTextVal>();
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VIRTUAL_PERSON_ID", referencedColumnName = "ID")
	@MapsId("virtualPersonId")
	private VirtualPerson virtualPerson;

	public Consent() {
	}

	public Consent(ConsentTemplate ct, ConsentDTO dto, Map<ModuleKeyDTO, Module> modules, VirtualPerson signer)
			throws VersionConverterClassException, InvalidVersionException, MissingRequiredObjectException, InvalidFreeTextException {
		super();
		Map<String, FreeTextDef> freeTextDefs = new HashMap<String, FreeTextDef>();
		for (FreeTextDef freeTextDef : ct.getFreeTextDefs()) {
			freeTextDefs.put(freeTextDef.getKey().getName(), freeTextDef);
			if (freeTextDef.getRequired()) {
				String value = dto.getFreeTextVals().get(freeTextDef.getKey().getName());
				if (value == null || value.isEmpty()) {
					throw new MissingRequiredObjectException("there's no value for the mandatory free text field '" + freeTextDef.getKey().getName());
				}
			}
		}
		this.key = new ConsentKey(ct, dto.getKey().getConsentDate(), signer.getId());
		this.physicanId = dto.getPhysicanId();
		signatures.add(new Signature(this, SignatureType.PATIENT, dto.getPatientSignatureBase64(), dto.getPatientSigningDate()));
		this.patientSignatureIsFromGuardian = dto.getPatientSignatureIsFromGuardian();
		signatures.add(new Signature(this, SignatureType.PHYSICAN, dto.getPhysicanSignatureBase64(), dto.getPhysicanSigningDate()));
		this.scanBase64 = new Text(key, TextType.CONSENTSCAN, dto.getScanBase64());
		this.scanFileType = dto.getScanFileType();
		this.comment = dto.getComment();
		this.externProperties = dto.getExternProperties();
		for (Entry<ModuleKeyDTO, ModuleStateDTO> moduleState : dto.getModuleStates().entrySet()) {
			for (Policy policy : modules.get(moduleState.getKey()).getPolicies()) {
				SignedPolicy signedPolicy = new SignedPolicy(this, policy, moduleState.getValue().getConsentState());
				signedPolicies.add(signedPolicy);
			}
		}
		for (Entry<String, String> entry : dto.getFreeTextVals().entrySet()) {
			FreeTextDef freeTextDef = freeTextDefs.get(entry.getKey());
			if (freeTextDef == null) {
				throw new InvalidFreeTextException("unknown free text with name '" + entry.getKey() + "'");
			}
			FreeTextVal freeTextVal = new FreeTextVal(this, entry.getKey(), entry.getValue(), freeTextDef);
			freeTextValues.add(freeTextVal);
		}
		this.virtualPerson = signer;
		this.consentTemplate = ct;
	}

	public ConsentKey getKey() {
		return key;
	}

	public Signature getPatientSignature() {
		Signature result = null;
		for (Signature signature : signatures) {
			if (signature.getKey().getType().equals(SignatureType.PATIENT)) {
				result = signature;
				break;
			}
		}
		return result;
	}

	public Signature getPhysicanSignature() {
		Signature result = null;
		for (Signature signature : signatures) {
			if (signature.getKey().getType().equals(SignatureType.PHYSICAN)) {
				result = signature;
				break;
			}
		}
		return result;
	}

	public ConsentTemplate getConsentTemplate() {
		return consentTemplate;
	}

	public String getPhysicanId() {
		return physicanId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getExternProperties() {
		return externProperties;
	}

	public void setExternProperties(String externProperties) {
		this.externProperties = externProperties;
	}

	public void setScanBase64(String scanBase64, String fileType) {
		this.scanBase64.setText(scanBase64);
		this.scanFileType = fileType;
	}

	public Text getScanBase64() {
		return scanBase64;
	}

	public String getScanFileType() {
		return scanFileType;
	}

	public List<SignedPolicy> getSignedPolicies() {
		return signedPolicies;
	}

	public VirtualPerson getVirtualPerson() {
		return virtualPerson;
	}

	public Date getExpirationDate() throws InternalException {
		try {
			return consentTemplate.getPropertiesObject().getExpirationDateForConsentWithDate(key.getConsentDate());
		} catch (InvalidPropertiesException e) {
			throw new InternalException("unexpected exception retrieving consent expiration date: " + e.getMessage());
		}
	}

	public ConsentLightDTO toLightDTO(VersionConverterCache vcc)
			throws InvalidVersionException, VersionConverterClassException, InconsistentStatusException {
		try {
			ConsentLightDTO result = new ConsentLightDTO(
					key.toDTO(vcc.getCTVersionConverter(consentTemplate.getDomain().getName()), virtualPerson));
			result.setTemplateType(consentTemplate.getType());
			result.setPatientSigningDate(getPatientSignature() != null ? getPatientSignature().getSignatureDate() : null);
			result.setPatientSignatureIsFromGuardian(patientSignatureIsFromGuardian);
			result.setPhysicanId(physicanId);
			result.setPhysicanSigningDate(getPhysicanSignature() != null ? getPhysicanSignature().getSignatureDate() : null);
			result.setComment(comment);
			result.setExternProperties(externProperties);
			result.setScanFileType(scanFileType);
			HashMap<PolicyKeyDTO, ConsentStatus> policyStates = new HashMap<PolicyKeyDTO, ConsentStatus>();
			for (SignedPolicy signedPolicy : signedPolicies) {
				policyStates.put(signedPolicy.getKey().getPolicyKey().toDTO(vcc.getPolicyVersionConverter(consentTemplate.getDomain().getName())),
						signedPolicy.getStatus());
			}
			Map<ModuleKeyDTO, ModuleStateDTO> moduleStates = new HashMap<ModuleKeyDTO, ModuleStateDTO>();
			for (ModuleConsentTemplate mct : consentTemplate.getModuleConsentTemplates()) {
				ConsentStatus moduleStatus = null;
				List<PolicyKeyDTO> policyKeys = new ArrayList<PolicyKeyDTO>();
				for (Policy policy : mct.getModule().getPolicies()) {
					PolicyKeyDTO policyKey = policy.getKey().toDTO(vcc.getPolicyVersionConverter(consentTemplate.getDomain().getName()));
					ConsentStatus tempModuleStatus = policyStates.get(policyKey);
					if (moduleStatus == null) {
						moduleStatus = tempModuleStatus;
					} else if (!moduleStatus.equals(tempModuleStatus)) {
						StringBuilder sb = new StringBuilder();
						sb.append("inconsistence for ");
						sb.append(key);
						sb.append(" signed consent for ");
						sb.append(policy.getKey());
						sb.append(" status should be ");
						sb.append(moduleStatus);
						sb.append(" but is ");
						sb.append(tempModuleStatus);
						throw new InconsistentStatusException(sb.toString());
					}
					policyKeys.add(policyKey);
				}
				ModuleKeyDTO moduleKey = mct.getModule().getKey().toDTO(vcc.getModuleVersionConverter(consentTemplate.getDomain().getName()));
				moduleStates.put(moduleKey, new ModuleStateDTO(moduleKey, moduleStatus, policyKeys));
			}
			result.setModuleStates(moduleStates);
			return result;
		} catch (UnknownDomainException impossible) {
			throw new VersionConverterClassException("impossible UnknownDomainException", impossible);
		}
	}

	public ConsentDTO toDTO(VersionConverterCache vcc) throws VersionConverterClassException, InvalidVersionException, InconsistentStatusException {
		ConsentDTO result = new ConsentDTO(toLightDTO(vcc));
		result.setPatientSignatureBase64(getPatientSignature().getSignatureScanBase64());
		result.setPhysicanSignatureBase64(getPhysicanSignature().getSignatureScanBase64());
		result.setScanBase64(scanBase64.getText());
		Map<String, String> freeTextValuesMap = new HashMap<String, String>();
		for (FreeTextVal freeTextVal : freeTextValues) {
			freeTextValuesMap.put(freeTextVal.getKey().getFreeTextDevName(), freeTextVal.getValue());
		}
		result.setFreeTextVals(freeTextValuesMap);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		Consent other = (Consent) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with comment '");
		sb.append(comment);
		sb.append("', extern properties: '");
		sb.append(externProperties);
		sb.append("', ");
		sb.append(signedPolicies.size());
		sb.append(" signed policies and ");
		sb.append(freeTextValues.size());
		sb.append(" free text values");
		return sb.toString();
	}
}
