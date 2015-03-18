package org.irods.jargon.extensions.dotirods;

import java.io.File;
import java.io.FilenameFilter;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for service to discover and manipulate .irods collections
 * 
 * @author Mike Conway - DICE
 */
public interface DotIrodsService {

	/**
	 * Find the .irods collection, if it exists, for the user in their home
	 * directory
	 * <p/>
	 * This assumes a standard /zone/home/user format
	 * 
	 * @param userName
	 *            <code>String</code> user name that will be used to find the
	 *            home directory
	 * @return {@link DotIrodsCollection} associated with the user
	 * @throws FileNotFoundException
	 *             if a .irods collection is not available
	 * @throws JargonException
	 */
	public abstract DotIrodsCollection findUserHomeCollection(String userName)
			throws FileNotFoundException, JargonException;

	/**
	 * Find the .irods collection at the given absolute path (including the
	 * .irods in the path)
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to a .irods
	 *            collection
	 * @param homeDir
	 *            <code>boolean</code> if this is a path to a home directory
	 * @return {@link DotIrodsCollection} at that path
	 * @throws FileNotFoundException
	 *             if a .irods collection is not available
	 * @throws JargonException
	 */
	public abstract DotIrodsCollection retrieveDotIrodsAtPath(
			String irodsAbsolutePath, boolean homeDir)
			throws FileNotFoundException, JargonException;

	/**
	 * Delete the .irods collection for the user home dir
	 * 
	 * @param userName
	 *            <code>String</code> user name that will be used to find the
	 *            home directory
	 * @throws JargonException
	 */
	public abstract void deleteDotIrodsForUserHome(final String userName)
			throws JargonException;

	/**
	 * Delete the .irods collection at the given absolute path
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to a .irods
	 *            collection
	 * @throws JargonException
	 */
	public abstract void deleteDotIrodsFileAtPath(final String irodsAbsolutePath)
			throws JargonException;

	/**
	 * Create a .irods collection in the user home dir
	 * 
	 * @param userName
	 *            <code>String</code> user name that will be used to find the
	 *            home directory
	 * @throws JargonException
	 */
	public abstract void createDotIrodsForUserHome(final String userName)
			throws JargonException;

	/**
	 * Create a .irods directory under the given parent path
	 * 
	 * @param irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated
	 *            <code>String</code> absolute path to the parent collection
	 *            under which the .irods collection will be created
	 * @throws JargonException
	 */
	public abstract void createDotIrodsUnderParent(
			final String irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated)
			throws JargonException;

	/**
	 * Create a .irods collection in the user home dir if it does not exist, and
	 * if it does exist just return it.
	 * 
	 * @param userName
	 *            <code>String</code> user name that will be used to find the
	 *            home directory
	 * @return {@link DotIrodsCollection} that was created or already existed
	 * @throws JargonException
	 */
	public abstract DotIrodsCollection findOrCreateUserHomeCollection(
			final String userName) throws JargonException;

	/**
	 * Returns true if there is a .irods collection in the specified collection,
	 * false if there is not.
	 * 
	 * @param irodsAbsolutePathToParent
	 *            <code>String</code> absolute path to the collection to be
	 *            queried.
	 * @return <code>boolean</code> indicating whether a .irods collection
	 *         exists.
	 * @throws FileNotFoundException
	 *             if the parent collection does not exist
	 * @throws JargonException
	 */
	public abstract boolean dotIrodsCollectionPresentInCollection(
			final String irodsAbsolutePathToParent)
			throws JargonException;

	/**
	 * Lists all files in the user home .irods collection
	 * 
	 * @param userName
	 *            <code>String</code> user name that will be used to find the
	 *            home directory
	 * @return Array of File objects {@link IRODSFileImpl} found in the user
	 *         home .irods collection
	 * @throws FileNotFoundException
	 *             if a .irods collection is not available
	 * @throws JargonException
	 */
	public abstract File[] listFilesInDotIrodsUserHome(final String userName)
			throws FileNotFoundException, JargonException;

	/**
	 * Lists all files (filtered by type) in the user home .irods collection
	 * 
	 * @param userName
	 *            <code>String</code> user name that will be used to find the
	 *            home directory
	 * @param filter
	 *            <code>FilenameFilter</code> a subclass of type FileFilter that
	 *            returns TRUE if a given file should be considered a match
	 * @return Array of File objects {@link IRODSFileImpl} that match
	 *         <code>filter</code> found in the user home .irods collection
	 * @throws FileNotFoundException
	 *             if a .irods collection is not available
	 * @throws JargonException
	 */
	public abstract File[] listFilesOfTypeInDotIrodsUserHome(
			final String userName, FilenameFilter filter)
			throws FileNotFoundException, JargonException;

	/**
	 * Lists all* files in all .irods collections found in the hierarchy of
	 * parent folders above the specified folder.
	 * 
	 * * The <code>resolveConflicts</code> flag determines whether multiple
	 * files with the same name are allowed. If TRUE, when multiple files have
	 * the same name, only the file closest in the directory hierarchy to the
	 * specified folder is included in the list.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to an IRODS
	 *            collection
	 * @param resolveConflicts
	 *            <code>boolean</code> True if only one File should be returned
	 *            for any given filename (conflicts resolved as above), False if
	 *            all files should be returned
	 * @return Array of File objects {@link IRODSFileImpl} found in the
	 *         directory hierarchy's .irods collections
	 * @throws FileNotFoundException
	 *             if the specified directory does not exist
	 * @throws JargonException
	 */
	public abstract File[] listFilesInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath, boolean resolveConflicts)
			throws FileNotFoundException, JargonException;

	/**
	 * Lists all* files in all .irods collections found in the hierarchy of
	 * parent folders above the specified folder.
	 * 
	 * * If multiple files have the same name, only the file closest in the
	 * directory hierarchy to the specified folder is included in the list. This
	 * function is a convenience for the "default" behavior of the function;
	 * that is, the function body should likely be
	 * <code>return listFilesInDirectoryHierarchyDotIrods(irodsAbsolutePath, true);</code>
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to an IRODS
	 *            collection
	 * @return Array of File objects {@link IRODSFileImpl} found in the
	 *         directory hierarchy's .irods collections
	 * @throws FileNotFoundException
	 *             if the specified directory does not exist
	 * @throws JargonException
	 */
	public abstract File[] listFilesInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath) throws FileNotFoundException,
			JargonException;

	/**
	 * Lists all* files (filtered by type) in all .irods collections found in
	 * the hierarchy of parent folders above the specified folder.
	 * 
	 * * The <code>resolveConflicts</code> flag determines whether multiple
	 * files with the same name are allowed. If <code>true</code>, when multiple
	 * files have the same name, only the file closest in the directory
	 * hierarchy to the specified folder is included in the list.
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to an IRODS
	 *            collection
	 * @param filter
	 *            <code>FilenameFilter</code> a subclass of type FileFilter that
	 *            returns TRUE if a given file should be considered a match
	 * @param resolveConflicts
	 *            <code>boolean</code> True if only one File should be returned
	 *            for any given filename (conflicts resolved as above), False if
	 *            all files should be returned
	 * @return Array of File objects {@link IRODSFileImpl} found in the
	 *         directory hierarchy's .irods collections
	 * @throws FileNotFoundException
	 *             if the specified directory does not exist
	 * @throws JargonException
	 */
	public abstract File[] listFilesOfTypeInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath, FilenameFilter filter,
			boolean resolveConflicts) throws FileNotFoundException,
			JargonException;

	/**
	 * Lists all* files (filtered by type) in all .irods collections found in
	 * the hierarchy of parent folders above the specified folder.
	 * 
	 * * If multiple files have the same name, only the file closest in the
	 * directory hierarchy to the specified folder is included in the list. This
	 * function is a convenience for the "default" behavior of the function;
	 * that is, the function body should likely be
	 * <code>return listFilesOfTypeInDirectoryHierarchyDotIrods(irodsAbsolutePath, filter, true);</code>
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the absolute path to an IRODS
	 *            collection
	 * @param filter
	 *            <code>FilenameFilter</code> a subclass of type FileFilter that
	 *            returns TRUE if a given file should be considered a match
	 * @return Array of File objects {@link IRODSFileImpl} found in the
	 *         directory hierarchy's .irods collections
	 * @throws FileNotFoundException
	 *             if the specified directory does not exist
	 * @throws JargonException
	 */
	public abstract File[] listFilesOfTypeInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath, FilenameFilter filter)
			throws FileNotFoundException, JargonException;

}