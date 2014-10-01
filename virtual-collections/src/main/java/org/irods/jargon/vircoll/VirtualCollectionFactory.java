package org.irods.jargon.vircoll;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a factory service that can find and initialize lists of virtual
 * collections, as well as create new instances of a virtual collection type
 * 
 * @author Mike Conway - DICE
 * 
 */
public interface VirtualCollectionFactory {

	/**
	 * Given some form of virtual collection, return the associated executor
	 * 
	 * @param virtualCollection
	 *            {@link VirtualCollection} subtype
	 * @return associated {@link AbstractVirtualCollectionExecutor}
	 * @throws DataNotFoundException
	 * @throws JargonException
	 */
	@SuppressWarnings("rawtypes")
	AbstractVirtualCollectionExecutor instanceExecutorBasedOnVirtualCollection(
			final VirtualCollection virtualCollection)
			throws DataNotFoundException, JargonException;

}