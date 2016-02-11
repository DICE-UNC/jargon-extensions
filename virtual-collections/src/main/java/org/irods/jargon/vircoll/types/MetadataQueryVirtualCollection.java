package org.irods.jargon.vircoll.types;

import org.irods.jargon.vircoll.ConfigurableVirtualCollection;

public class MetadataQueryVirtualCollection extends
		ConfigurableVirtualCollection {
	public static final String DESCRIPTION_KEY = "virtual.collection.description.mdquery";
	public static final String DESCRIPTION = "Files and folders found by this metadata query";
	public static final String NAME = "Metadata Query";
	public static final String NAME_KEY = "virtual.collection.name.mdquery";
	public static final String ICON_KEY = "virtual.collection.icon.mdquery";
	public static final String MY_TYPE = "MD_QUERY";

	/**
	 * Default (no values) constructor
	 */
	public MetadataQueryVirtualCollection() {
		super();
	}

	/**
	 * Constructor that can take a query string
	 * 
	 * @param queryString
	 *            <code>String</code> with the metadata query string
	 */
	public MetadataQueryVirtualCollection(String queryString) {
		this.setQueryString(queryString);
		this.setType(MY_TYPE);
	}
}
