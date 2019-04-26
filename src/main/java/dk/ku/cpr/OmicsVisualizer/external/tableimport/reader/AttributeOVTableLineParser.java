package dk.ku.cpr.OmicsVisualizer.external.tableimport.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.SourceColumnSemantic;

/**
 * Take a line of data, analyze it, and map to CyAttributes.
 */
public class AttributeOVTableLineParser extends AbstractLineParser {
	
	private final AttributeMappingParameters mapping;
	private final Map<String, Object> invalid = new HashMap<>();

	public AttributeOVTableLineParser(final AttributeMappingParameters mapping, final CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		this.mapping = mapping;
	}

	/**
	 * Import everything regardless associated nodes/edges exist or not.
	 * @param parts entries in a line.
	 */
	// Modification ML: In order to have several rows with the same ID, we use the rownumber as en id
	public void parseAll(final CyTable table, final String[] parts, int rowid) {
//		// Get key
//		final Object primaryKey ;
		final Object primaryKey = Integer.valueOf(rowid);
		final int partsLen = parts.length;
//		final AttributeDataType typeKey = mapping.getDataTypes()[mapping.getKeyIndex()];
		
//		switch (typeKey) {
//			case TYPE_BOOLEAN:
//				primaryKey = Boolean.valueOf(parts[mapping.getKeyIndex()].trim());
//				break;
//			case TYPE_INTEGER:
//				primaryKey = Integer.valueOf(parts[mapping.getKeyIndex()].trim());
//				break;
//			case TYPE_LONG:
//				primaryKey = Long.valueOf(parts[mapping.getKeyIndex()].trim());
//				break;
//			case TYPE_FLOATING:
//				primaryKey = Double.valueOf(parts[mapping.getKeyIndex()].trim());
//				break;
//			default:
//				primaryKey = parts[mapping.getKeyIndex()].trim();
//		}

		if (partsLen == 1) {
			// Modification ML: to avoid empty lines :
			if ((parts[0] != null) && (parts[0].length() > 0))
				setAttribute(table, mapping.getDataTypes()[0], primaryKey, mapping.getAttributeNames()[0], parts[0]);
		} else {
			final SourceColumnSemantic[] types = mapping.getTypes();
			
			// Modification ML: We can have several
			for (int i = 0; i < partsLen; i++) {
//				if (i != mapping.getKeyIndex() && types[i] != SourceColumnSemantic.NONE) {
				if (types[i] != SourceColumnSemantic.NONE) {
					if (parts[i] == null)
						continue;
					// Modification ML: to avoid empty lines :
					else if (parts[i].length() == 0)
						continue;
					else
						mapAttribute(table, primaryKey, parts[i].trim(), i);
				}
			}
		}
	}

	/**
	 * Based on the attribute types, map the entry to CyAttributes.<br>
	 */
	private void mapAttribute(final CyTable table, final Object key, String entry, final int index) {
		final AttributeDataType type = mapping.getDataTypes()[index];
		
		// ML : Get rid of starting and ending " if any
		if(entry.startsWith("\"") && entry.endsWith("\"")) {
			entry = entry.substring(1, entry.length()-1);
		}

		try {
			if (type.isList()) {
				final String[] delimiters = mapping.getListDelimiters();
				String delimiter = delimiters != null && delimiters.length > index ?
						delimiters[index] : AbstractMappingParameters.DEF_LIST_DELIMITER;
						
				if (delimiter == null || delimiter.isEmpty())
					delimiter = AbstractMappingParameters.DEF_LIST_DELIMITER;
				
				Object value = parse(entry, type, delimiter);
				setListAttribute(table, type, key, mapping.getAttributeNames()[index], value);
			} else {
				setAttribute(table, type, key, mapping.getAttributeNames()[index], entry);
			}
		} catch (Exception e) {
			invalid.put(key.toString(), entry);
		}
	}

	private void setAttribute(final CyTable tbl, final AttributeDataType type, final Object key,
			final String attrName, final String attrValue) {
		if (tbl.getColumn(attrName) == null) {
			tbl.createColumn(attrName, type.getType(), false);
		}

		final Object value = parse(attrValue, type, null);
		final CyRow row = tbl.getRow(key);
		row.set(attrName, value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setListAttribute(final CyTable tbl, final AttributeDataType type, final Object key,
			final String attributeName, Object value) {
		if (tbl.getColumn(attributeName) == null)
			tbl.createListColumn(attributeName, type.getListType(), false);
		
		final CyRow row = tbl.getRow(key);
		
		if (value instanceof List) {
			// In case of list, do not overwrite the attribute. Get the existing list, and add it to the list.
			List<?> curList = row.getList(attributeName, type.getListType());

			if (curList == null)
				curList = new ArrayList<>();
			
			curList.addAll((List)value);
			value = curList;
		}
		
		row.set(attributeName, value);
	}

	protected Map<String, Object> getInvalidMap() {
		return invalid;
	}
}
