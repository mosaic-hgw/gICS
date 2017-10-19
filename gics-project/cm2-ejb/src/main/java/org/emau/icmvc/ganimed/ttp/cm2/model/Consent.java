package org.emau.icmvc.ganimed.ttp.cm2.model;

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

import java.io.Serializable;
import java.util.ArrayList;
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
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InconsistentStatusException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidFreeTextException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.InvalidVersionException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.MissingRequiredObjectException;
import org.emau.icmvc.ganimed.ttp.cm2.exceptions.VersionConverterClassException;

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

	private static final long serialVersionUID = 1413013617439366206L;
	@EmbeddedId
	private ConsentKey key;
	// muss "eager" sein - auf das feld wird nicht direkt zugegriffen und daher weiss jpa nicht, wann das nachgeladen werden muss
	@OneToMany(mappedBy = "consent", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@BatchFetch(BatchFetchType.IN)
	private List<Signature> signatures = new ArrayList<Signature>();
	@Column(name = "PATIENTSIGNATURE_IS_FROM_GUARDIAN")
	private Boolean patientSignatureIsFromGuardian;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumns({ @JoinColumn(name = "CT_DOMAIN_NAME", referencedColumnName = "DOMAIN_NAME"),
			@JoinColumn(name = "CT_NAME", referencedColumnName = "NAME"), @JoinColumn(name = "CT_VERSION", referencedColumnName = "VERSION") })
	@MapsId("ctKey")
	private ConsentTemplate consentTemplate;
	private String physicanId;
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
		for (Entry<ModuleKeyDTO, ConsentStatus> moduleEntry : dto.getModuleStates().entrySet()) {
			for (Policy policy : modules.get(moduleEntry.getKey()).getPolicies()) {
				SignedPolicy signedPolicy = new SignedPolicy(this, policy, moduleEntry.getValue());
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

	public ConsentDTO toDTO() throws VersionConverterClassException, InvalidVersionException, InconsistentStatusException {
		ConsentDTO result = new ConsentDTO(key.toDTO(consentTemplate.getDomain().getCTVersionConverterInstance(), virtualPerson));
		result.setPatientSignatureBase64(getPatientSignature().getSignatureScanBase64());
		result.setPatientSigningDate(getPatientSignature().getSignatureDate());
		result.setPatientSignatureIsFromGuardian(patientSignatureIsFromGuardian);
		result.setPhysicanId(physicanId);
		result.setPhysicanSignatureBase64(getPhysicanSignature().getSignatureScanBase64());
		result.setPhysicanSigningDate(getPhysicanSignature().getSignatureDate());
		result.setScanBase64(scanBase64.getText());
		result.setScanFileType(scanFileType);
		HashMap<PolicyKeyDTO, ConsentStatus> policyStates = new HashMap<PolicyKeyDTO, ConsentStatus>();
		for (SignedPolicy signedPolicy : signedPolicies) {
			policyStates.put(signedPolicy.getKey().getPolicyKey().toDTO(consentTemplate.getDomain().getPolicyVersionConverterInstance()),
					signedPolicy.getStatus());
		}
		HashMap<ModuleKeyDTO, ConsentStatus> moduleStates = new HashMap<ModuleKeyDTO, ConsentStatus>();
		for (ModuleConsentTemplate mct : consentTemplate.getModuleConsentTemplates()) {
			ConsentStatus moduleStatus = null;
			for (Policy policy : mct.getModule().getPolicies()) {
				ConsentStatus tempModuleStatus = policyStates.get(policy.getKey().toDTO(
						consentTemplate.getDomain().getPolicyVersionConverterInstance()));
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
			}
			moduleStates.put(mct.getModule().getKey().toDTO(consentTemplate.getDomain().getModuleVersionConverterInstance()), moduleStatus);
		}
		result.setModuleStates(moduleStates);
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
		result = prime * result + ((freeTextValues == null) ? 0 : freeTextValues.hashCode());
		result = prime * result + ((signatures == null) ? 0 : signatures.hashCode());
		result = prime * result + ((patientSignatureIsFromGuardian == null) ? 0 : patientSignatureIsFromGuardian.hashCode());
		result = prime * result + ((physicanId == null) ? 0 : physicanId.hashCode());
		result = prime * result + ((scanBase64 == null) ? 0 : scanBase64.hashCode());
		result = prime * result + ((scanFileType == null) ? 0 : scanFileType.hashCode());
		result = prime * result + ((signedPolicies == null) ? 0 : signedPolicies.hashCode());
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
		if (freeTextValues == null) {
			if (other.freeTextValues != null)
				return false;
		} else if (!freeTextValues.equals(other.freeTextValues))
			return false;
		if (signatures == null) {
			if (other.signatures != null)
				return false;
		} else if (!signatures.equals(other.signatures))
			return false;
		if (patientSignatureIsFromGuardian == null) {
			if (other.patientSignatureIsFromGuardian != null)
				return false;
		} else if (!patientSignatureIsFromGuardian.equals(other.patientSignatureIsFromGuardian))
			return false;
		if (physicanId == null) {
			if (other.physicanId != null)
				return false;
		} else if (!physicanId.equals(other.physicanId))
			return false;
		if (scanBase64 == null) {
			if (other.scanBase64 != null)
				return false;
		} else if (!scanBase64.equals(other.scanBase64))
			return false;
		if (scanFileType == null) {
			if (other.scanFileType != null)
				return false;
		} else if (!scanFileType.equals(other.scanFileType))
			return false;
		if (signedPolicies == null) {
			if (other.signedPolicies != null)
				return false;
		} else if (!signedPolicies.equals(other.signedPolicies))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(" with ");
		sb.append(signedPolicies.size());
		sb.append(" signed policies and ");
		sb.append(freeTextValues.size());
		sb.append(" free text values");
		return sb.toString();
	}
}
