package org.emau.icmvc.ganimed.ttp.cm2.config;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.emau.icmvc.ganimed.ttp.cm2.dto.enums.ConsentTemplateType;

/**
 * parameter for get...Paginated functions
 * <p>
 * <b>firstEntry</b><br>
 * default = 0
 * <p>
 * <b>pageSize</b><br>
 * default = 10
 * <p>
 * <b>filter</b><br>
 * default = empty map<br>
 * filter values; for possible keys see {@link ConsentField}, except {@link ConsentField#DATE}
 * <p>
 * <b>startDate</b><br>
 * default = null<br>
 * consents with date >= startDate are returned
 * <p>
 * <b>endDate</b><br>
 * default = null<br>
 * consents with date <= endDate are returned
 * <p>
 * <b>templateType</b><br>
 * default = null<br>
 * <p>
 * <b>qcType</b><br>
 * default = true
 * <p>
 * <b>filterFieldsAreTreatedAsConjuction</b><br>
 * default = true<br>
 * has no effect on template type
 * <p>
 * <b>filterIsCaseSensitive</b><br>
 * this parameter is only applied to filter for the field {@link ConsentField#CT_NAME}<br>
 * default = true
 * <p>
 * <b>sortField</b><br>
 * see {@link ConsentField}<br>
 * default = null
 * <p>
 * <b>sortIsAscending</b><br>
 * default = true
 * <p>
 * <b>useAliases</b><br>
 * should aliases be used when searching for signer ids?<br>
 * default = true
 *
 *
 * @author geidell
 *
 */
public class PaginationConfig implements Serializable
{
	private static final long serialVersionUID = 4733956568853784480L;
	private int firstEntry = 0;
	private int pageSize = 10;
	private final Map<ConsentField, String> filter = new HashMap<>();
	private Date startDate = null;
	private Date endDate = null;
	private ConsentTemplateType templateType = null;
	private boolean filterFieldsAreTreatedAsConjunction = true;
	private boolean filterIsCaseSensitive = true;
	private ConsentField sortField = null;
	private boolean sortIsAscending = true;
	private boolean useAliases = true;

	public PaginationConfig()
	{
		super();
	}

	public PaginationConfig(int firstEntry, int pageSize)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
	}

	public PaginationConfig(int firstEntry, int pageSize, ConsentField sortField, boolean sortIsAscending)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
		this.sortField = sortField;
		this.sortIsAscending = sortIsAscending;
	}

	public PaginationConfig(int firstEntry, int pageSize, Map<ConsentField, String> filter, boolean filterFieldsAreTreatedAsConjuction,
			boolean filterIsCaseSensitive)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
		this.filter.putAll(filter);
		this.filterFieldsAreTreatedAsConjunction = filterFieldsAreTreatedAsConjuction;
		this.filterIsCaseSensitive = filterIsCaseSensitive;
	}

	public PaginationConfig(int firstEntry, int pageSize, Map<ConsentField, String> filter, boolean filterFieldsAreTreatedAsConjuction,
			boolean filterIsCaseSensitive, ConsentField sortField, boolean sortIsAscending, boolean useAliases)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
		this.filter.putAll(filter);
		this.filterFieldsAreTreatedAsConjunction = filterFieldsAreTreatedAsConjuction;
		this.filterIsCaseSensitive = filterIsCaseSensitive;
		this.sortField = sortField;
		this.sortIsAscending = sortIsAscending;
		this.useAliases = useAliases;
	}

	public PaginationConfig(int firstEntry, int pageSize, Map<ConsentField, String> filter, Date startDate, Date endDate, ConsentTemplateType templateType, boolean filterFieldsAreTreatedAsConjuction,
			boolean filterIsCaseSensitive, ConsentField sortField, boolean sortIsAscending, boolean useAliases)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
		this.filter.putAll(filter);
		this.startDate = startDate;
		this.endDate = endDate;
		this.templateType = templateType;
		this.filterFieldsAreTreatedAsConjunction = filterFieldsAreTreatedAsConjuction;
		this.filterIsCaseSensitive = filterIsCaseSensitive;
		this.sortField = sortField;
		this.sortIsAscending = sortIsAscending;
		this.useAliases = useAliases;
	}

	public int getFirstEntry()
	{
		return firstEntry;
	}

	public void setFirstEntry(int firstEntry)
	{
		this.firstEntry = firstEntry;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	public ConsentField getSortField()
	{
		return sortField;
	}

	public void setSortField(ConsentField sortField)
	{
		this.sortField = sortField;
	}

	@SuppressWarnings("unchecked")
	public Map<ConsentField, String> getFilter()
	{
		return (Map<ConsentField, String>) ((HashMap<ConsentField, String>) filter).clone();
	}

	public void setFilter(Map<ConsentField, String> filter)
	{
		if (this.filter != filter)
		{
			this.filter.clear();
			this.filter.putAll(filter);
		}
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public Date getEndDate()
	{
		return endDate;
	}

	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public ConsentTemplateType getTemplateType()
	{
		return templateType;
	}

	public void setTemplateType(ConsentTemplateType templateType)
	{
		this.templateType = templateType;
	}

	public boolean isFilterFieldsAreTreatedAsConjunction()
	{
		return filterFieldsAreTreatedAsConjunction;
	}

	public void setFilterFieldsAreTreatedAsConjunction(boolean filterFieldsAreTreatedAsConjuction)
	{
		this.filterFieldsAreTreatedAsConjunction = filterFieldsAreTreatedAsConjuction;
	}

	public boolean isFilterIsCaseSensitive()
	{
		return filterIsCaseSensitive;
	}

	public void setFilterIsCaseSensitive(boolean filterIsCaseSensitive)
	{
		this.filterIsCaseSensitive = filterIsCaseSensitive;
	}

	public boolean isSortIsAscending()
	{
		return sortIsAscending;
	}

	public void setSortIsAscending(boolean sortIsAscending)
	{
		this.sortIsAscending = sortIsAscending;
	}

	public boolean isUseAliases()
	{
		return useAliases;
	}

	public void setUseAliases(boolean useAliases)
	{
		this.useAliases = useAliases;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (endDate == null ? 0 : endDate.hashCode());
		result = prime * result + (filter == null ? 0 : filter.hashCode());
		result = prime * result + (filterFieldsAreTreatedAsConjunction ? 1231 : 1237);
		result = prime * result + (filterIsCaseSensitive ? 1231 : 1237);
		result = prime * result + firstEntry;
		result = prime * result + pageSize;
		result = prime * result + (sortField == null ? 0 : sortField.hashCode());
		result = prime * result + (sortIsAscending ? 1231 : 1237);
		result = prime * result + (startDate == null ? 0 : startDate.hashCode());
		result = prime * result + (templateType == null ? 0 : templateType.hashCode());
		result = prime * result + (useAliases ? 1231 : 1237);
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
		PaginationConfig other = (PaginationConfig) obj;
		if (endDate == null)
		{
			if (other.endDate != null)
			{
				return false;
			}
		}
		else if (!endDate.equals(other.endDate))
		{
			return false;
		}
		if (filter == null)
		{
			if (other.filter != null)
			{
				return false;
			}
		}
		else if (!filter.equals(other.filter))
		{
			return false;
		}
		if (filterFieldsAreTreatedAsConjunction != other.filterFieldsAreTreatedAsConjunction)
		{
			return false;
		}
		if (filterIsCaseSensitive != other.filterIsCaseSensitive)
		{
			return false;
		}
		if (firstEntry != other.firstEntry)
		{
			return false;
		}
		if (pageSize != other.pageSize)
		{
			return false;
		}
		if (sortField != other.sortField)
		{
			return false;
		}
		if (sortIsAscending != other.sortIsAscending)
		{
			return false;
		}
		if (startDate == null)
		{
			if (other.startDate != null)
			{
				return false;
			}
		}
		else if (!startDate.equals(other.startDate))
		{
			return false;
		}
		if (templateType != other.templateType)
		{
			return false;
		}
		if (useAliases != other.useAliases)
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "PaginationConfig [firstEntry=" + firstEntry + ", pageSize=" + pageSize + ", filter=" + filter + ", startDate=" + startDate + ", endDate=" + endDate + ", templateType=" + templateType
				+ ", filterFieldsAreTreatedAsConjunction=" + filterFieldsAreTreatedAsConjunction + ", filterIsCaseSensitive=" + filterIsCaseSensitive + ", sortField=" + sortField
				+ ", sortIsAscending=" + sortIsAscending + ", useAliases=" + useAliases + "]";
	}
}
