package org.emau.icmvc.ganimed.ttp.cm2.config;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeAction;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeError;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeField;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCProblemTypeOccurrence;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCType;
import org.emau.icmvc.ganimed.ttp.cm2.config.qc.QCTypeStatus;
import org.emau.icmvc.ganimed.ttp.cm2.dto.QCDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DomainConfigTest
{
	private static final Logger logger = LogManager.getLogger(DomainConfigTest.class);

	public static final String CHECKED_NO_FAULTS = "checked_no_faults";
	public static final String NOT_CHECKED = "not_checked";
	public static final String CHECKED_MINOR_FAULTS = "checked_minor_faults";
	public static final String CHECKED_MAJOR_FAULTS = "checked_major_faults";
	public static final String INVALIDATED = "invalidated";
	public static final String DOMAIN_PROPERTIES =
			";VALID_QC_TYPES=" + NOT_CHECKED + "," + CHECKED_NO_FAULTS + "," + CHECKED_MINOR_FAULTS + ";"
			+ "INVALID_QC_TYPES=" + CHECKED_MAJOR_FAULTS + "," + INVALIDATED + ";"
			+ "DEFAULT_QC_TYPE=" + NOT_CHECKED + ";"
			+ "SCANS_SIZE_LIMIT=10000000;"
			+ "REVOKE_IS_PERMANENT=true;"
			+ "TAKE_HIGHEST_VERSION_INSTEAD_OF_NEWEST=true;"
			+ "TAKE_MOST_SPECIFIC_PERIOD_OF_VALIDITY_INSTEAD_OF_SHORTEST=true;"
			+ "SCANS_ARE_NOT_MANDATORY_FOR_ACCEPTED_CONSENTS=true;"
			+ "SEND_NOTIFICATIONS_WEB=true;"
			+ "STATISTIC_DOCUMENT_DETAILS=true;"
			+ "STATISTIC_POLICY_DETAILS=true";
	private DomainConfig dc;
	private QualityControlConfig qc;
	private QCProblemTypeAction actionFixNow;
	private QCProblemTypeAction actionFixLater;
	private QCProblemType problemWithName;
	private QCProblemType problemWithScan;

	static String createXML(String inner)
	{
		return "<ns2:domain-config xmlns:ns2=\"http://www.ttp.ganimed.icmvc.emau.org/cm2/config\">" + inner + "</ns2:domain-config>";
	}

	@BeforeEach
	void setup()
	{
		dc = new DomainConfig("domain", DOMAIN_PROPERTIES);

		// assert that the config matches the CSV properties
		assertLegacyConfig(dc,true, true, true,
				true,true, true, true,
				10000000, NOT_CHECKED,
				Set.of(NOT_CHECKED, CHECKED_NO_FAULTS, CHECKED_MINOR_FAULTS, QCDTO.AUTO_GENERATED),
				Set.of(CHECKED_MAJOR_FAULTS, INVALIDATED));

		qc = dc.getQualityControlConfig();

		qc.getTypeById(NOT_CHECKED).setLabels(Map.of("de", "Noch nicht kontrolliert", "en", "Not yet checked"));
		qc.getTypeById(CHECKED_MAJOR_FAULTS).setLabels(Map.of("de", "Schwerwiegende Fehler", "en", "Major faults"));

		actionFixNow = new QCProblemTypeAction("fix-now",
				Map.of("de", "Schnell fixen", "en", "Fix quickly"));
		actionFixLater = new QCProblemTypeAction("fix-later",
				Map.of("de", "Später fixen", "en", "Fix later"));

		problemWithName = new QCProblemType("problem-with-name", QCProblemTypeError.INCONSISTENT,
				QCProblemTypeField.IDAT_LASTNAME, QCProblemTypeOccurrence.BOTH, actionFixNow,
				Map.of("de", "Problem mit dem Namen", "en", "Problem with the name"));
		problemWithScan = new QCProblemType("problem-with-scan", QCProblemTypeError.MISSING_PART,
				QCProblemTypeField.SIGNATURE_PARTICIPANT_DATE, QCProblemTypeOccurrence.DIGITAL, actionFixLater,
				Map.of("de", "Problem mit dem Scan", "en", "Problem with the scan"));

		qc.getProblemTypeActions().addAll(Set.of(actionFixNow, actionFixLater));
		qc.getProblemTypes().addAll(Set.of(problemWithName, problemWithScan));
	}

	@Test
	void testEmptyConfig() throws JAXBException
	{
		DomainConfig config = normalized(DomainConfig.fromXml(createXML("")));
		logger.info(config.toString());

		// assert that the default config is the same no matter if created with empty XML or empty CSC
		DomainConfig otherConfig = normalized(new DomainConfig("unknown", ""));
		assertEquals(config, new DomainConfig("unknown", ""));
		assertNotEquals(config, new DomainConfig());
		assertEquals(config, DomainConfig.createDefaultDomainConfig());

		// assert the default values of an empty config
		assertLegacyConfig(config,false, false, false,
				false,false, false, false,
				ScansConfig.DEFAULT_SIZE_LIMIT, QCDTO.AUTO_GENERATED, Set.of(QCDTO.AUTO_GENERATED), Set.of());

		logger.debug("Without validation:\n{}", config.toXml(false));
		String xml = config.toXml();
		logger.info("With validation:\n{}", xml);

		// assert that a conversion roundtrip does not change the config
		assertEquals(config, DomainConfig.fromXml(xml));
		assertEqualsQc(config.getQualityControlConfig(), DomainConfig.fromXml(xml).getQualityControlConfig());
	}

	@Test
	void testZeroScanSizeLimit() throws JAXBException
	{
		// assert that an explicit 0 is not handled as implicit default
		DomainConfig config = normalized(DomainConfig.fromXml(createXML("<scans size-limit=\"0\"/>")));
		assertLegacyConfig(config,
				false, false, false, false,
				false, false, false, 0,
				QCDTO.AUTO_GENERATED, Set.of(QCDTO.AUTO_GENERATED), Set.of());
	}

	@Test
	void testHandlingDefaultQcType() throws JAXBException
	{
		// assert that default-qc-type is ignored when not found in valid-qc-types
		assertLegacyConfig(normalized(DomainConfig.fromXml(createXML(
				"<quality-control default-type=\"foo\"/>"))),
				false, false, false,
				false,false, false, false,
				ScansConfig.DEFAULT_SIZE_LIMIT, QCDTO.AUTO_GENERATED, Set.of(QCDTO.AUTO_GENERATED), Set.of());
		// assert that default-qc-type is used when found in valid-qc-types
		assertLegacyConfig(DomainConfig.fromXml(createXML(
				"<quality-control default-type=\"foo\"><type id='foo' status='VALID'/></quality-control>")),
				false, false, false,
				false,false, false, false,
				ScansConfig.DEFAULT_SIZE_LIMIT, "foo", Set.of("foo"), Set.of());
	}

	@Test
	void testMutableObjectsInSets()
	{
		assertSame(qc, dc.getQualityControlConfig());
		assertEquals(6, qc.getTypes().size());

		QCType type = qc.getTypeById(NOT_CHECKED);
		type.setStatus(QCTypeStatus.INVALID);
		assertTrue(new HashSet<>(qc.getTypes()).contains(type));
		assertTrue(qc.getTypes().contains(type));
		assertSame(type, qc.getTypeById(NOT_CHECKED));

		for (QCType t : qc.getTypes())
		{
			assertTrue(qc.getTypes().contains(t));
		}

		assertTrue(qc.getTypes().containsAll(qc.getTypes()));
		assertEqualsQc(qc, qc);
	}

	@Test
	void testToFromXmlRoundtrip() throws JAXBException
	{
		logger.info(dc.toString());
		assertEquals(Set.of(actionFixNow, actionFixLater), qc.getProblemTypeActions());
		assertEquals(Set.of(problemWithName, problemWithScan), qc.getProblemTypes());

		String xml = dc.toXml();
		logger.info("With validation:\n{}", xml);

		// assert that the XML contains some well-known strings
		assertTrue(xml.contains("domain-config"), "contains root element");
		assertTrue(xml.contains(qc.getDefaultTypeId()), "contains default QC type");
		assertTrue(xml.contains(dc.getScansConfig().getSizeLimit() + ""), "contains default scn size limit");

		// assert that a conversion roundtrip does not change the config
		DomainConfig dc2 = DomainConfig.fromXml(xml);
		QualityControlConfig qc2 = dc2.getQualityControlConfig();

		assertEqualsQc(qc, qc2);
		assertEquals(dc, dc2);
		assertEquals(dc2, dc);

		assertEquals("Fix quickly", qc2.getProblemTypeActionById("fix-now").getLabel("en"));
		assertEquals("Fix later", qc2.getProblemTypeActionById("fix-later").getLabel("en"));

		assertSame(
				qc2.getProblemTypeById(problemWithName.getId()).getAction(),
				qc2.getProblemTypeActionById(qc2.getProblemTypeById(problemWithName.getId()).getAction().getId()));

		assertSame(
				qc2.getProblemTypeById(problemWithScan.getId()).getAction(),
				qc2.getProblemTypeActionById(qc2.getProblemTypeById(problemWithScan.getId()).getAction().getId()));

		assertSame(qc2.getDefaultType(), qc2.getTypeById(qc2.getDefaultTypeId()));
	}

	@Test
	void testModifyingQcTypes()
	{
		assertEquals(QCTypeStatus.VALID, qc.getTypeById(NOT_CHECKED).getStatus());
		assertSame(qc.getTypeById(NOT_CHECKED), qc.getDefaultType());

		qc.getTypeById(NOT_CHECKED).setStatus(QCTypeStatus.INVALID);

		assertEquals(QCTypeStatus.INVALID, qc.getTypeById(NOT_CHECKED).getStatus());
		assertEquals(QCTypeStatus.INVALID, qc.getDefaultType().getStatus());

		qc.setDefaultType(new QCType(NOT_CHECKED, QCTypeStatus.VALID));

		assertEquals(QCTypeStatus.VALID, qc.getTypeById(NOT_CHECKED).getStatus());
		assertEquals(QCTypeStatus.VALID, qc.getDefaultType().getStatus());
	}

	@Test
	void testModifyingQcProblemTypesAndActions()
	{
		assertEquals(actionFixNow, problemWithName.getAction());
		assertEquals(qc.getProblemTypeActionById(actionFixNow.getId()), qc.getProblemTypeById(problemWithName.getId()).getAction());

		problemWithName.setAction(actionFixLater);

		assertEquals(actionFixLater, problemWithName.getAction());
		assertEquals(qc.getProblemTypeActionById(actionFixLater.getId()), qc.getProblemTypeById(problemWithName.getId()).getAction());

		problemWithName.setError(QCProblemTypeError.ILLEGIBLE);

		assertEquals(QCProblemTypeError.ILLEGIBLE, problemWithName.getError());
		assertEquals(QCProblemTypeError.ILLEGIBLE, qc.getProblemTypeById(problemWithName.getId()).getError());

		qc.getProblemTypeActionById(actionFixLater.getId()).setLabel("a", "1");
		assertEquals("1", qc.getProblemTypeActionById(actionFixLater.getId()).getLabel("a"));

		qc.getProblemTypeById(problemWithName.getId()).setLabel("b", "2");
		assertEquals("2", qc.getProblemTypeById(problemWithName.getId()).getLabel("b"));

		qc.getProblemTypeById(problemWithName.getId()).getAction().setLabel("c", "3");
		assertEquals("3", qc.getProblemTypeActionById(actionFixLater.getId()).getLabel("c"));

		qc.getProblemTypeActionById(actionFixLater.getId()).setLabel("d", "4");
		assertEquals("4", qc.getProblemTypeById(problemWithName.getId()).getAction().getLabel("d"));
	}

	public static void assertLegacyConfig(DomainConfig config,
			boolean permanentRevoke, boolean noMandatoryScans, boolean takeSpecificValidity, boolean takeHighestVersion,
			boolean sendNotificationsWeb, boolean statisticDocumentDetails, boolean statisticPolicyDetails,
			int scansSizeLimit, String defaultQcType, Set<String> validQcTypes, Set<String> invalidQcTypes)
	{
		assertEquals(permanentRevoke, config.getPoliciesConfig().isPermanentRevoke(), "PermanentRevoke");
		assertEquals(noMandatoryScans, !config.getScansConfig().isMandatory(), "NoMandatoryScans");
		assertEquals(takeSpecificValidity, config.getPoliciesConfig().isTakeMostSpecificValidityInsteadOfShortest(), "TakeSpecificValidity");
		assertEquals(takeHighestVersion, config.getPoliciesConfig().isTakeHighestVersionInsteadOfNewest(), "TakeHighestVersion");
		assertEquals(sendNotificationsWeb, config.getNotificationsConfig().isSendFromWeb(), "SendNotificationsWeb");
		assertEquals(statisticDocumentDetails, config.getStatisticConfig().isCalculateDocumentDetails(), "StatisticDocumentDetails");
		assertEquals(statisticPolicyDetails, config.getStatisticConfig().isCalculatePolicyDetails(), "StatisticPolicyDetails");
		assertEquals(scansSizeLimit, config.getScansConfig().getSizeLimit(), "ScanSizeLimit");
		assertEquals(defaultQcType, config.getQualityControlConfig().getDefaultTypeId(), "DefaultQcType");
		assertEquals(validQcTypes, config.getQualityControlConfig().getValidQcTypeValues(), "ValidQcTypes");
		assertEquals(invalidQcTypes, config.getQualityControlConfig().getInvalidQcTypeValues(), "InvalidQcTypes");
	}
	private void assertEqualsQc(QualityControlConfig qc1, QualityControlConfig qc2)
	{
		assertEquals(qc1.getDefaultType(), qc2.getDefaultType());
		assertEquals(qc2.getDefaultType(), qc1.getDefaultType());
		assertEquals(qc1.getTypes(), qc2.getTypes());
		assertEquals(qc2.getTypes(), qc1.getTypes());
		assertEquals(qc1, qc2);
		assertEquals(qc2, qc1);
	}

	@Test
	public void testValidIds()
	{
		assertTrue(new QCType("###_auto_generated_###", true).hasValidId());
		assertFalse(new QCType("### auto generated ###", true).hasValidId());
		assertTrue(new QCProblemType("auto_generated").hasValidId());
		assertFalse(new QCProblemType("###_auto_generated_###").hasValidId());
		assertTrue(new QCProblemTypeAction("auto_generated").hasValidId());
		assertFalse(new QCProblemTypeAction("###_auto_generated_###").hasValidId());
	}

	static DomainConfig normalized(DomainConfig config)
	{
		config.normalize();
		return config;
	}

	/**
	 * The example works without @XmlJavaTypeAdapter(MapExampleAdapter.class) but when trying to
	 * patch the marshalling with the adapter the XML map is empty -why?
	 */
	//@Test
	void testMapExample() throws Exception {
		MapExample example = new MapExample(new HashMap<>(
				Map.of("a", "1", "b", "2", "c", "3")));

		JAXBContext context = JAXBContext.newInstance(MapExample.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(example, System.out);
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "example")
	public static class MapExample
	{

		@XmlJavaTypeAdapter(MapExampleAdapter.class)
		@XmlElement(name = "label")
		Map<String, String> map;

		// Konstruktor mit Parametern
		public MapExample(Map<String, String> map)
		{
			this.map = map;
		}

		MapExample()
		{
		}
	}

	public static class MapExampleAdapter extends XmlAdapter<HashSet<Label>, Map<String, String>>
	{
		@Override
		public Map<String, String> unmarshal(HashSet<Label> labels) throws Exception {
			Map<String, String> map = new HashMap<>();
			labels.forEach(l -> map.put(l.getLang(), l.getValue()));
			return map;
		}

		@Override
		public HashSet<Label> marshal(Map<String, String> map) throws Exception {
			HashSet<Label> labels = new HashSet<>();
			map.forEach((k, v) -> labels.add(new Label(k, v)));
			return labels;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Label", propOrder = {
			"lang",
			"value"
	})
	public static class Label implements Serializable
	{
		@Serial
		private static final long serialVersionUID = 2263992772722741056L;

		@XmlAttribute(name = "lang")
		private String lang;
		@XmlAttribute(name = "value")
		private String value;

		public Label()
		{
		}

		public Label(String lang, String value)
		{
			this.lang = lang;
			this.value = value;
		}

		public String getLang()
		{
			return lang;
		}

		public String getValue()
		{
			return value;
		}
	}
}
