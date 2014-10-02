/**
 * 
 */
package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;
import org.irods.jargon.vircoll.VirtualCollection;
import org.irods.jargon.vircoll.impl.VirtualCollectionTypeEnum;

/**
 * Represents a virtual collection derived from a REST service that accepts
 * SPARQL queries
 * 
 * @author Mike Conway - DICE
 *
 */
public class SparqlViaRestVirtualCollection extends VirtualCollection {

	public static final String DESCRIPTION_KEY_HOME = "virtual.collection.description.home";
	public static final String DESCRIPTION_KEY_ROOT = "virtual.collection.description.root";
	public static final String DESCRIPTION = "Virtual collection derived from a SPARQL query";

	/**
	 * 
	 */
	public SparqlViaRestVirtualCollection() {
		setDescription(DESCRIPTION);
		setI18icon(DEFAULT_ICON_KEY);
		setPagingStyle(PagingStyle.SPLIT_COLLECTIONS_AND_FILES);
		this.setVirtualCollectionTypeEnum(VirtualCollectionTypeEnum.SPARQL);
		this.setCanPersist(true);
	}

}
