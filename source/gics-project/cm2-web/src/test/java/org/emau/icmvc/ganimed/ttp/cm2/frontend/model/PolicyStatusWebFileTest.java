package org.emau.icmvc.ganimed.ttp.cm2.frontend.model;

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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.dto.PolicyKeyDTO;
import org.emau.icmvc.ganimed.ttp.cm2.frontend.testtools.GicsWebTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyStatusWebFileTest extends GicsWebTest
{
	PolicyStatusWebFile webFile;
	UploadedFile uploadFile;
	FileUploadEvent event;
	String tool;
	private List<PolicyDTO> availablePolicies = new ArrayList<>();

	@BeforeEach
	void createWebFile()
	{
		tool = "gICS";
		event = mock(FileUploadEvent.class);
		uploadFile = mock(UploadedFile.class);
		when(event.getFile()).thenReturn(uploadFile);
	}

	@Test
	void constructor()
	{
		// Arrange
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test1", "1.0"));
		PolicyDTO p2 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test2", "1.0"));
		availablePolicies = Arrays.asList(p1, p2);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);

		// Assert
		assertEquals("gICS", webFile.getTool());
		assertEquals(availablePolicies, webFile.getAvailablePolicies());
	}

	@Test
	void onUpload()
	{
		// Arrange
		String file = "1\n"
				+ "2";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.onUpload(event);

		// Assert
		verify(event).getFile();
		assertEquals(uploadFile, webFile.getUploadFile());
		assertEquals(2, webFile.getElements().size());
		// First row entries
		assertEquals(1, webFile.getElements().get(0).size());
		assertEquals("1", webFile.getElements().get(0).get(0));
		// Second row entries
		assertEquals(1, webFile.getElements().get(1).size());
		assertEquals("2", webFile.getElements().get(1).get(0));
	}

	@Test
	void onUploadPolicyWithName()
	{
		// Arrange
		String columnName = "Test Name";
		String file = "PSN;" + columnName + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test Name", "1.0"));
		availablePolicies = Arrays.asList(p1);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(1, webFile.getColumnPolicyMapping().size());
		assertEquals(p1, webFile.getColumnPolicyMapping().get(columnName));
	}

	@Test
	void onUploadMultiplePolicies()
	{
		// Arrange
		String columnName1 = "Test1";
		String columnName2 = "Test2";
		String file = "PSN;" + columnName1 + ";" + columnName2 + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test1", "1.0"));
		PolicyDTO p2 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test2", "1.0"));
		availablePolicies = Arrays.asList(p1, p2);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(2, webFile.getColumnPolicyMapping().size());
		assertEquals(p1, webFile.getColumnPolicyMapping().get(columnName1));
		assertEquals(p2, webFile.getColumnPolicyMapping().get(columnName2));
	}

	@Test
	void onUploadPolicyWithLabel()
	{
		// Arrange
		String columnName = "Test Label";
		String file = "PSN;" + columnName + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test Name", "1.0"));
		p1.setLabel("Test Label");
		availablePolicies = Arrays.asList(p1);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(1, webFile.getColumnPolicyMapping().size());
		assertEquals(p1, webFile.getColumnPolicyMapping().get(columnName));
	}

	@Test
	void onUploadPolicyUnNormalized()
	{
		// Arrange
		String columnName = "Test_Underscore Whitespace";
		String file = "PSN;" + columnName + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test Underscore_Whitespace", "1.0"));
		availablePolicies = Arrays.asList(p1);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(1, webFile.getColumnPolicyMapping().size());
		assertEquals(p1, webFile.getColumnPolicyMapping().get(columnName));
	}

	@Test
	void onUploadPolicyWithoutVersion()
	{
		// Arrange
		String columnName = "Test";
		String file = "PSN;" + columnName + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.1"));
		PolicyDTO pHighestVersion = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.2"));
		PolicyDTO p3 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.0"));
		availablePolicies = Arrays.asList(p1, pHighestVersion, p3);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(1, webFile.getColumnPolicyMapping().size());
		assertEquals(pHighestVersion, webFile.getColumnPolicyMapping().get(columnName));
	}

	@Test
	void onUploadPolicyWithVersion()
	{
		// Arrange
		String columnName = "Test 1.1";
		String file = "PSN;" + columnName + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.0"));
		PolicyDTO p2 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.1"));
		PolicyDTO p3 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.2"));
		availablePolicies = Arrays.asList(p1, p2, p3);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(1, webFile.getColumnPolicyMapping().size());
		assertEquals(p2, webFile.getColumnPolicyMapping().get(columnName));
	}

	@Test
	void onUploadPolicyWithMultipleVersions()
	{
		// Arrange
		String columnName1 = "Test 1.0";
		String columnName2 = "Test 1.1";
		String file = "PSN;" + columnName1 + ";" + columnName2 + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.0"));
		PolicyDTO p2 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.1"));
		PolicyDTO p3 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.2"));
		availablePolicies = Arrays.asList(p1, p2, p3);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(2, webFile.getColumnPolicyMapping().size());
		assertEquals(p1, webFile.getColumnPolicyMapping().get(columnName1));
		assertEquals(p2, webFile.getColumnPolicyMapping().get(columnName2));
	}

	@Test
	void onUploadPolicyWithMultipleSameVersions()
	{
		// Arrange
		String duplicateColumnName = "Test V1.0";
		String file = "PSN;" + duplicateColumnName + ";" + duplicateColumnName + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.0"));
		PolicyDTO p2 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.1"));
		PolicyDTO p3 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.2"));
		availablePolicies = Arrays.asList(p1, p2, p3);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(1, webFile.getColumnPolicyMapping().size());
		assertEquals(p1, webFile.getColumnPolicyMapping().get(duplicateColumnName));
	}

	@Test
	void onUploadPolicyWithVersionAndV()
	{
		// Arrange
		String columnName1 = "Test V1.0";
		String columnName2 = "Test V1.1";
		String file = "PSN;" + columnName1 + ";" + columnName2 + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.0"));
		PolicyDTO p2 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.1"));
		PolicyDTO p3 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.2"));
		availablePolicies = Arrays.asList(p1, p2, p3);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);

		// Assert
		assertEquals(2, webFile.getColumnPolicyMapping().size());
		assertEquals(p1, webFile.getColumnPolicyMapping().get(columnName1));
		assertEquals(p2, webFile.getColumnPolicyMapping().get(columnName2));
	}
	
	@Test
	void removeDetectedPolicyColumns()
	{
		// Arrange
		String columnName = "Test";
		String file = "PSN;" + columnName + "\n "
				+ "1";
		when(uploadFile.getContent()).thenReturn(file.getBytes(StandardCharsets.UTF_8));
		PolicyDTO p1 = new PolicyDTO(new PolicyKeyDTO("Domain", "Test", "1.0"));
		availablePolicies = Arrays.asList(p1);

		// Act
		webFile = new PolicyStatusWebFile(tool, availablePolicies);
		webFile.setContainsHeader(true);
		webFile.onUpload(event);
		
		// Assert
		assertEquals(2, webFile.getColumns().size());
		
		// Act
		webFile.removeDetectedPolicyColumns();

		// Assert
		assertEquals(1, webFile.getColumns().size());
	}
}
