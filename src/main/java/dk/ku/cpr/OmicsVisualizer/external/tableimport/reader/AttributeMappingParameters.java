package dk.ku.cpr.OmicsVisualizer.external.tableimport.reader;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.AttributeDataType;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.SourceColumnSemantic;
import dk.ku.cpr.OmicsVisualizer.external.tableimport.util.TypeUtil;

/**
 * Parameter object for text table <---> CyAttributes mapping.
 * This object will be used by all attribute readers.
 */
public class AttributeMappingParameters extends AbstractMappingParameters {

//	private final int keyIndex;
	
	public AttributeMappingParameters(InputStream is, String fileType) {
		super(is, fileType);

//		this.keyIndex = -1;
	}

	public AttributeMappingParameters(
			final String name,
			final List<String> delimiters,
			final String[] listDelimiters,
			final String[] attrNames,
			final AttributeDataType[] dataTypes,
	        final SourceColumnSemantic[] types,
	        final String[] namespaces,
	        Character groupingSeparator,
	    	// ML: Add custom decimal format
	        Character decimalSeparator
	) throws Exception {
		// ML: Add custom decimal format
		this(name, delimiters, listDelimiters, attrNames, dataTypes, types, namespaces, 0, null, decimalSeparator);
	}

	public AttributeMappingParameters(
			final String name,
			final List<String> delimiters,
            final String[] listDelimiters,
            final String[] attrNames,
            final AttributeDataType[] dataTypes,
            final SourceColumnSemantic[] types,
            final String[] namespaces,
            final int startNumber,
            final String commentChar,
        	// ML: Add custom decimal format
	        Character decimalSeparator
    ) throws Exception {
		// ML: Add custom decimal format
		super(name, delimiters, listDelimiters, attrNames, dataTypes, types, namespaces, startNumber, commentChar, decimalSeparator);
		
		if (attrNames == null)
			throw new Exception("attributeNames should not be null.");
		
		/*
		 * If not specified, import everything as String attributes.
		 */
		if (dataTypes == null) {
			this.dataTypes = new AttributeDataType[attrNames.length];
			Arrays.fill(this.dataTypes, AttributeDataType.TYPE_STRING);
		} else {
			this.dataTypes = dataTypes;
		}

		/*
		 * If not specified, import everything.
		 */
		if (types == null) {
			this.types = new SourceColumnSemantic[attrNames.length];
			Arrays.fill(this.types, SourceColumnSemantic.ATTR);
		} else {
			this.types = types;
		}
		
		/*
		 * If namespaces were not specified, use the preferred ones
		 */
		if (namespaces == null)
			this.namespaces = TypeUtil.getPreferredNamespaces(this.types);
		else
			this.namespaces = namespaces;
	}
	
//	public int getKeyIndex() {
//		return keyIndex;
//	}
}
