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
	 * Get the executor associated with this virtual collection
	 * 
	 * @param virtualCollection
	 *            {@link VirtualCollection} to be executed
	 * @return {@link AbstractVirtualCollectionExecutor} subclass
	 * @throws DataNotFoundException
	 *             if the executor cannot be found
	 * @throws JargonException
	 */
	@SuppressWarnings("rawtypes")
	AbstractVirtualCollectionExecutor instanceExecutor(
			VirtualCollection virtualCollection) throws DataNotFoundException,
			JargonException;

}