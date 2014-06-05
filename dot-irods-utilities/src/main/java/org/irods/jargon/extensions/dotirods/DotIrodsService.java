package org.irods.jargon.extensions.dotirods;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for service to discover and manipulate .irods collections
 * @author Mike Conway - DICE
 */
public interface DotIrodsService {

	/**
	 * Find the .irods collection, if it exists, for the user in their home directory
	 * <p/>
	 * This assumes a standard /zone/home/user format
	 * 
	 * @param userName <code>String</code> user name that will be used to find the home directory
	 * @return {@link DotIrodsCollection} associated with the user
	 * @throws FileNotFoundException if a .irods collection is not available
	 * @throws JargonException
	 */
	public abstract DotIrodsCollection findUserHomeCollection(String userName)
			throws FileNotFoundException, JargonException;

	/**
	 * Find the .irods collection at the given absolute path (including the .irods in the path)
	 * @param irodsAbsolutePath <code>String</code> with the absolute path to a .irods collection
	 * @param homeDir <code>boolean</code> if this is a path to a home directory
	 * @return {@link DotIrodsCollection} at that path
	 * @throws FileNotFoundException if a .irods collection is not available
	 * @throws JargonException
	 */
	public abstract DotIrodsCollection retrieveDotIrodsAtPath(
			String irodsAbsolutePath, boolean homeDir)
			throws FileNotFoundException, JargonException;

	/**
	 * Delete the .irods collection for the user home dir
	 * @param userName <code>String</code> user name that will be used to find the home directory
	 * @throws JargonException
	 */
	public abstract void deleteDotIrodsForUserHome(final String userName)
			throws JargonException;

	/**
	 * Delete the .irods collection at the given absolute path
	 * @param irodsAbsolutePath <code>String</code> with the absolute path to a .irods collection
	 * @throws JargonException
	 */
	public abstract void deleteDotIrodsFileAtPath(final String irodsAbsolutePath)
			throws JargonException;

	/**
	 * Create a .irods collection in the user home dir
	 * @param userName <code>String</code> user name that will be used to find the home directory
	 * @throws JargonException
	 */
	public abstract void createDotIrodsForUserHome(final String userName)
			throws JargonException;

	/**
	 * Create a .irods directory under the given parent path
	 * @param irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated <code>String</code> absolute path to the parent collection 
	 * under which the .irods collection will be created
	 * @throws JargonException
	 */
	public abstract void createDotIrodsUnderParent(final String irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated)
			throws JargonException;


}