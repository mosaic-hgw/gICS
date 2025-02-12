package org.emau.icmvc.ganimed.ttp.cm2.util;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ConsentTemplateDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.ModuleKeyDTO;

/**
 * Converts module names to QR code text (referred to as QR code hereafter) and
 * QR codes back to module names when a reference set of possible module names is given.
 * <p>
 * The implementation ensures constant length (and entropy) of the QR codes, in contrast
 * to previous versions, where the module name directly was used as QR code.
 * For backward compatibility old QR codes (module names) are accepted and can be
 * used to lookup module names.
 * </p>
 * <p>
 * The implementation is a bit more complicated than simply using hashes to allow at least
 * guessing the module name with a QR code reader outside gICS. The exact conversion and
 * lookup rules can be read in {@link #toQRCodeFromModuleName(String)} and
 * {@link #fromQRCodeToModuleName(String, Set)} as well as in
 * <a href="https://git.icm.med.uni-greifswald.de/ths/gics-project/-/issues/415">gics#415: Fehlerhafte Darstellung der Größe von QR-Codes</a>
 * </p>
 * @param qrCodeLength the length for the QR codes
 * @author moserf
 */
public record ModuleQRCodec(int qrCodeLength)
{
	public static final int DEFAULT_QR_CODE_LENGTH = 64;
	public static String SEPARATOR = "_###_";
	public static String TRIMMER = "...";

	public static final ModuleQRCodec DEFAULT = new ModuleQRCodec(DEFAULT_QR_CODE_LENGTH);

	/**
	 * {@return the QR code for a module.}
	 * @param module the module
	 */
	public String toQRCodeFromModule(ModuleDTO module)
	{
		return toQRCodeFromModuleKey(module.getKey());
	}

	/**
	 * {@return the QR code for a module key.}
	 * @param moduleKey the module key
	 */
	private String toQRCodeFromModuleKey(ModuleKeyDTO moduleKey)
	{
		return toQRCodeFromModuleName(moduleKey.getName());
	}

	/**
	 * Converts a QR code into a module key DTO in modern style:
	 * searches the given consent template for a module with a name that matches the given QR code
	 * by the rules found in {@link #fromQRCodeToModuleName(String, Set)},
	 * returning it if found or null otherwise.
	 *
	 * @param qrCode
	 * 		the QR code
	 * @param consentTemplate
	 * 		the consent template for searching modules in
	 * @return the matching module key DTO or null
	 */
	public ModuleKeyDTO fromQRCodeToModuleKey(String qrCode, ConsentTemplateDTO consentTemplate)
	{
		if (consentTemplate != null && consentTemplate.getAssignedModules() != null)
		{
			return fromQRCodeToModuleKey(qrCode,
					// map assigned modules to a set with module key DTOs
					consentTemplate.getAssignedModules().stream()
							.map(am -> am.getModule().getKey()).collect(Collectors.toSet()));
		}
		return null;
	}

	/**
	 * Converts a QR code into a module key DTO in modern style: suche ein module dessen name lang ist
	 * searches the given module key DTOs for a module with a name that matches the given QR code
	 * by the rules found in {@link #fromQRCodeToModuleName(String, Set)},
	 * returning it if found or null otherwise.
	 *
	 * @param qrCode
	 * 		the QR code
	 * @param moduleKeys
	 * 		the module key DTOs for searching
	 * @return the matching module key DTO or null
	 */
	public ModuleKeyDTO fromQRCodeToModuleKey(String qrCode, Set<ModuleKeyDTO> moduleKeys)
	{
		if (moduleKeys != null)
		{
			// create a map with module names as keys and module key DTOs as values
			Map<String, ModuleKeyDTO> moduleKeyMap = moduleKeys.stream().collect(
					Collectors.toMap(ModuleKeyDTO::getName, Function.identity()));
			return moduleKeyMap.get(fromQRCodeToModuleName(qrCode, moduleKeyMap.keySet()));
		}
		return null;
	}

	/**
	 * {@return the matching module or null.}
	 * Regeln für die Suche nach Modul zum QR-Code:
	 * <pre>
	 * 1. **Abwärtskompatibel**: QR-Code mit exakten Modulnamen werden immer erkannt
	 * 2. QR-Code mit exakten Modulnamen nach Padding mit Spaces werden immer erkannt
	 * 3. QR-Code mit exakten Modulnamen vor `_###_` werden immer erkannt
	 * 4. Durch Abschneiden eindeutig gebliebene Präfixe von Modulnamen vor `...` werden immer erkannt
	 * 5. Durch Abschneiden uneindeutig gewordene Präfixe von Modulnamen vor `...` werden **nicht** erkannt
	 * </pre>
	 *
	 * @param qrCode
	 * 		the QR code
	 * @param moduleNames
	 * 		the names of the modules
	 * @return the matching module or null
	 */
	public String fromQRCodeToModuleName(String qrCode, Set<String> moduleNames)
	{
		if (StringUtils.isBlank(qrCode))
		{
			return null;
		}

		// Check if the QR code directly matches a module name (backward compatibility)
		if (moduleNames.contains(qrCode))
		{
			return qrCode;
		}

		// Check if the trimmed QR code directly matches a module name (shortcut)
		String trimmed = qrCode.trim();
		if (moduleNames.contains(trimmed))
		{
			return trimmed;
		}

		// Check if the first part of the split QR code directly matches a module name (shortcut)
		String firstOnSplit = qrCode.split(SEPARATOR)[0];
		if (moduleNames.contains(firstOnSplit))
		{
			return firstOnSplit;
		}

		// Compute the qr codes for all module names and build a map with qr code as key and module name as value
		Map<String, String> qrCodeToModule = new HashMap<>();
		Set<String> ambiguousQRCodes = new HashSet<>();

		for (String m : moduleNames)
		{
			String qr = toQRCodeFromModuleName(m);
			if (qrCodeToModule.containsKey(qr))
			{
				ambiguousQRCodes.add(qr);
			}
			else
			{
				qrCodeToModule.put(qr, m);
			}
		}

		// Check ambiguity of qr codes from truncated module names
		if (ambiguousQRCodes.contains(qrCode))
		{
			return null;
		}

		// lookup module name for given qr code from the map
		return qrCodeToModule.get(qrCode);
	}

	/**
	 * {@return the QR code.}
	 * Konvertierungsregeln (für QR-Code-Länge 64):
	 * <pre>
	 * 1. Modulnamen < 59 Zeichen werden mit Trenner `_###_` und verschiedenen (aber **nicht** zufälligen) Zeichen rechts aufgefüllt
	 * 2. Modulnamen < 64 (>= 59) Zeichen werden mit ` ` (Leerzeichen) rechts aufgefüllt
	 * 3. Modulnamen = 64 Zeichen bleiben unverändert
	 * 3. Modulnamen > 64 Zeichen werden bei 61 abgeschnitten und mit `...` aufgefüllt
	 * </pre>
	 *
	 * @param moduleName
	 * 		the module moduleName
	 */
	public String toQRCodeFromModuleName(String moduleName)
	{
		// Check the length of the module name
		if (moduleName.length() <= qrCodeLength - SEPARATOR.length())
		{
			// Compute the hash of the module name
			String hash = DigestUtils.sha256Hex(moduleName);

			// Pad with separator and hash
			return moduleName + SEPARATOR + hash.substring(0, qrCodeLength - moduleName.length() - SEPARATOR.length());
		}
		else if (moduleName.length() < qrCodeLength)
		{
			// Pad with spaces
			return StringUtils.rightPad(moduleName, qrCodeLength);
		}
		else if (moduleName.length() > qrCodeLength)
		{
			// Cut and suffix with trimmer
			return moduleName.substring(0, qrCodeLength - 3) + TRIMMER;
		}
		// Return as it is
		return moduleName;
	}
}
