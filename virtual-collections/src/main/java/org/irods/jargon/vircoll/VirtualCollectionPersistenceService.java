package org.irods.jargon.vircoll;

import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a service to maintain and modify virtual collection metadata
 * 
 * @author Mike Conway - DICE
 * 
 */
public interface VirtualCollectionPersistenceService {

	/**
	 * Add the given virtual collection, in a serialized form, to the user
	 * collection, which is under the .irods folder in the user home directory
	 * 
	 * @param configurableVirtualCollection
	 *            {@link ConfigurableVirtualCollection} which is serializable
	 *            for the user
	 * @throws DuplicateDataException
	 *             if the virtual collection already exists
	 * @throws JargonException
	 */
	public abstract void addVirtualCollectionToUserCollection(
			VirtualCollection configurableVirtualCollection)
			throws DuplicateDataException, JargonException;

	/**
	 * Convert the given virtual collection into a JSON format
	 * 
	 * @param configurableVirtualCollection
	 *            {@link ConfigurableVirtualCollection} that may be serialized
	 *            to JSON
	 * @return <code>String</code> with the JSON formmated virtual collection
	 *         data
	 * @throws VirtualCollectionException
	 */
	public abstract String serializeVirtualCollectionToJson(
			VirtualCollection configurableVirtualCollection)
			throws VirtualCollectionException;

	/**
	 * Given an absolute path, retrieve the virtual collection from iRODS as an
	 * object
	 * 
	 * @param virtualCollectionAbsolutePath
	 *            <code>String</code> with the absolute path to the virtual
	 *            collection
	 * @return {@link ConfigurableVirtualCollection} available at that location
	 * @throws FileNotFoundException
	 * @throws VirtualCollectionException
	 */
	public abstract VirtualCollection retrieveVirtualCollectionFromFile(
			final String virtualCollectionAbsolutePath)
			throws FileNotFoundException, VirtualCollectionException;

	public abstract VirtualCollection retrieveVirtualCollectionFromUserCollection(
			final String userName, final String virtualCollectionName)
			throws FileNotFoundException, VirtualCollectionException;

}