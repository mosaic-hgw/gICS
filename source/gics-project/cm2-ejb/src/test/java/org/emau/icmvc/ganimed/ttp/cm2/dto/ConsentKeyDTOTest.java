package org.emau.icmvc.ganimed.ttp.cm2.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ConsentKeyDTOTest
{
	@Test
	void getOrderedSignerIds()
	{
		SignerIdDTO id1 = new SignerIdDTO();
		SignerIdDTO id2 = new SignerIdDTO();
		ConsentKeyDTO key = new ConsentKeyDTO();
		id1.setOrderNumber(1);
		id2.setOrderNumber(2);

		// pre-assert
		key.setSignerIds(Stream.of(id1, id2).collect(Collectors.toSet()));
		assertEquals(2, key.getOrderedSignerIds().size());
		assertEquals(Arrays.asList(id1, id2), key.getOrderedSignerIds());

		// act
		for (SignerIdDTO sid : key.getSignerIds())
		{
			if (sid.getOrderNumber() == 1)
			{
				sid.setOrderNumber(3);
			}
		}
		id1.setOrderNumber(3);

		// assert
		assertEquals(Arrays.asList(id2, id1), key.getOrderedSignerIds());
	}
}
