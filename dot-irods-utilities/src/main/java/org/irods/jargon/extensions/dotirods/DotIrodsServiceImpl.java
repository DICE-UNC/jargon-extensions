/**
 *
 */
package org.irods.jargon.extensions.dotirods;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementation that can discover and manipulate .irods collections
 * <p/>
 * Specific implementations of .irods collections will use this service to
 * locate and maintain the parent configurations. Examples include virtual
 * collections and metadata template stores.
 * 
 * @author Mike Conway - DICE
 */
public class DotIrodsServiceImpl extends AbstractJargonService implements
		DotIrodsService {

	private final CollectionAO collectionAO;

	public static final Logger log = LoggerFactory
			.getLogger(DotIrodsServiceImpl.class);

	private String computeHomeDirPathForDotIrodsFile(final String userName) {
		StringBuilder sb = new StringBuilder();
		sb.append(MiscIRODSUtils
				.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
						getIrodsAccount(), userName));
		sb.append("/");
		sb.append(DotIrodsConstants.DOT_IRODS_DIR);
		return sb.toString();
	}

	private String computeDotIrodsPathUnderParent(
			final String irodsAbsolutePathToParent) {
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToParent);
		sb.append("/");
		sb.append(DotIrodsConstants.DOT_IRODS_DIR);
		return sb.toString();
	}

	@Override
	public List<String> listStringifiedFilesInDotIrodsCollection(
			final String irodsAbsolutePath, final String dotIrodsSubdir)
			throws JargonException {
		log.info("listStringifiedFilesInDotIrodsCollection()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or emtpy irodsAbsolutePath");
		}

		if (dotIrodsSubdir == null || dotIrodsSubdir.isEmpty()) {
			throw new IllegalArgumentException("null or empty dotIrodsSubdir");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("dotIrodsSubdir:{}", dotIrodsSubdir);

		IRODSFile irodsFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(irodsAbsolutePath, dotIrodsSubdir);

		List<String> stringifiedFiles = new ArrayList<String>();
		String encoding = this.getIrodsAccessObjectFactory()
				.getJargonProperties().getEncoding();
		InputStream inputStream;
		for (File vcFile : irodsFile.listFiles()) {
			inputStream = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFileInputStream((IRODSFile) vcFile);
			try {
				stringifiedFiles.add(MiscIRODSUtils.convertStreamToString(
						inputStream, encoding));
			} catch (Exception e) {
				log.error("error stringifying file at:{}", vcFile, e);
				throw new JargonException("unable to get file contents", e);
			}

		}

		return stringifiedFiles;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * createDotIrodsForUserHome(java.lang.String)
	 */
	@Override
	public void createDotIrodsForUserHome(final String userName)
			throws JargonException {
		log.info("createDotIrodsForUserHome()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName:{}", userName);

		String homeDirPath = computeHomeDirPathForDotIrodsFile(userName);

		log.info("home dir computed to be:{}", homeDirPath);
		IRODSFile dotIrodsFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(homeDirPath);
		dotIrodsFile.mkdirs();
		log.info("created");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * deleteDotIrodsForUserHome(java.lang.String)
	 */
	@Override
	public void deleteDotIrodsForUserHome(final String userName)
			throws JargonException {
		log.info("deleteDotIrodsForUserHome()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName:{}", userName);

		String homeDirPath = computeHomeDirPathForDotIrodsFile(userName);

		log.info("home dir computed to be:{}", homeDirPath);
		IRODSFile dotIrodsFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(homeDirPath);
		dotIrodsFile.deleteWithForceOption();
		log.info("deleted");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.extensions.dotirods.DotIrodsService#deleteDotIrodsFileAtPath
	 * (java.lang.String)
	 */
	@Override
	public void deleteDotIrodsFileAtPath(final String irodsAbsolutePath)
			throws JargonException {
		log.info("deleteDotIrodsFileAtPath()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		IRODSFile dotIrodsFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(irodsAbsolutePath);
		dotIrodsFile.deleteWithForceOption();
		log.info("deleted");
	}

	/**
	 * Constructor
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} for obtaining service objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} with logged in identity
	 */
	public DotIrodsServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		try {
			collectionAO = getIrodsAccessObjectFactory().getCollectionAO(
					getIrodsAccount());
		} catch (JargonException e) {
			log.error("exception creating collectionAO", e);
			throw new JargonRuntimeException("error creating collectionAO", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.extensions.dotirods.DotIrodsService#findUserHomeCollection
	 * (java.lang.String)
	 */
	@Override
	public DotIrodsCollection findUserHomeCollection(final String userName)
			throws FileNotFoundException, JargonException {
		log.info("findUserHomeCollection()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName:{}", userName);

		String homeDirPath = MiscIRODSUtils
				.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(
						getIrodsAccount(), userName);
		log.info("home dir computed to be:{}", homeDirPath);
		StringBuilder sb = new StringBuilder();
		sb.append(homeDirPath);
		sb.append("/");
		sb.append(DotIrodsConstants.DOT_IRODS_DIR);
		return retrieveDotIrodsAtPath(sb.toString(), true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * findOrCreateUserHomeCollection(java.lang.String)
	 */
	@Override
	public DotIrodsCollection findOrCreateUserHomeCollection(
			final String userName) throws JargonException {

		log.info("findOrCreateUserHomeCollection()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName:{}", userName);

		try {
			return findUserHomeCollection(userName);
		} catch (FileNotFoundException dnf) {
			log.info("didn't find home, create one");
			createDotIrodsForUserHome(userName);
			return findUserHomeCollection(userName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.extensions.dotirods.DotIrodsService#retrieveDotIrodsAtPath
	 * (java.lang.String, boolean)
	 */
	@Override
	public DotIrodsCollection retrieveDotIrodsAtPath(
			final String irodsAbsolutePath, final boolean homeDir)
			throws FileNotFoundException, JargonException {
		log.info("retrieveDotIrodsAtPath()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		Collection collection = collectionAO
				.findByAbsolutePath(irodsAbsolutePath);
		DotIrodsCollection dotIrodsCollection = new DotIrodsCollection();
		dotIrodsCollection.setAbsolutePath(irodsAbsolutePath);
		dotIrodsCollection.setCollection(collection);
		dotIrodsCollection.setHomeDir(homeDir);
		log.info("found dotIrodsCollection:{}", dotIrodsCollection);
		return dotIrodsCollection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * createDotIrodsUnderParent(java.lang.String)
	 */
	@Override
	public void createDotIrodsUnderParent(
			final String irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated)
			throws JargonException {
		log.info("createDotIrodsUnderParent()");

		if (irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated == null
				|| irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated
						.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated");
		}

		log.info("irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated:{}",
				irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated);
		String dotIrodsPath = this
				.computeDotIrodsPathUnderParent(irodsAbsolutePathToParentUnderWhichDotIrodsWillBeCreated);
		log.info("dotIrodsPath computed to be:{}", dotIrodsPath);
		IRODSFile dotIrodsFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(dotIrodsPath);
		dotIrodsFile.mkdirs();
		log.info("created");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * dotIrodsCollectionPresentInCollection (java.lang.String)
	 */
	@Override
	public boolean dotIrodsCollectionPresentInCollection(
			final String irodsAbsolutePathToParent) throws JargonException {
		log.info("dotIrodsCollectionPresentInCollection()");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToParent");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePathToParent);

		@SuppressWarnings("unused")
		Collection collection = collectionAO
				.findByAbsolutePath(irodsAbsolutePathToParent);

		log.info("{} exists", irodsAbsolutePathToParent);

		boolean retVal = true;

		try {
			@SuppressWarnings("unused")
			IRODSFile userHomeAsFile = this
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(
							computeDotIrodsPathUnderParent(irodsAbsolutePathToParent));
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by instanceIRODSFile, {} does not exist",
					computeDotIrodsPathUnderParent(irodsAbsolutePathToParent));
			retVal = false;
		}

		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesInDotIrodsUserHome (java.lang.String)
	 */
	@Override
	public File[] listFilesInDotIrodsUserHome(final String userName)
			throws FileNotFoundException, JargonException {
		log.info("listFilesInDotIrodsUserHome()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		DotIrodsCollection userHomeAsDotIrodsCollection = null;
		IRODSFile userHomeAsFile = null;

		log.info("supplied username: {}", userName);

		userHomeAsDotIrodsCollection = findUserHomeCollection(userName);

		log.info(".irods dir found at {}:",
				userHomeAsDotIrodsCollection.getAbsolutePath());

		userHomeAsFile = this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeAsDotIrodsCollection.getAbsolutePath());

		return userHomeAsFile.listFiles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesOfTypeInDotIrodsUserHome (java.lang.String,
	 * java.io.FilenameFilter)
	 */
	@Override
	public File[] listFilesOfTypeInDotIrodsUserHome(final String userName,
			FilenameFilter filter) throws FileNotFoundException,
			JargonException {
		log.info("listFilesOfTypeInDotIrodsUserHome()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (filter == null) {
			throw new IllegalArgumentException("null filter");
		}

		DotIrodsCollection userHomeAsDotIrodsCollection = null;
		IRODSFile userHomeAsFile = null;

		log.info("supplied username: {}", userName);

		userHomeAsDotIrodsCollection = this.findUserHomeCollection(userName);

		log.info(".irods dir found at {}:",
				userHomeAsDotIrodsCollection.getAbsolutePath());

		userHomeAsFile = this
				.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(
						userHomeAsDotIrodsCollection.getAbsolutePath());

		return userHomeAsFile.listFiles(filter);
	}

	File[] listFilesInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath, final String subDir,
			FilenameFilter filter, boolean resolveConflicts)
			throws FileNotFoundException, JargonException {
		log.info("listFilesInDirectoryHierarchyDotIrods()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		boolean useSubDir = true;
		boolean useFilter = true;

		if (subDir == null || subDir.isEmpty()) {
			useSubDir = false;
		}

		if (filter == null) {
			useFilter = false;
		}

		Collection collection = collectionAO
				.findByAbsolutePath(irodsAbsolutePath);

		log.info("{} exists", irodsAbsolutePath);

		IRODSFile parent = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(collection.getAbsolutePath());

		@SuppressWarnings("unused")
		IRODSFile dotIrodsFile = null;

		IRODSFile dirFile = null;
		List<File> returnFileList = new ArrayList<File>();
		File[] dirFileList;

		while (parent != null) {
			boolean dotIrodsCollectionPresent = false;
			try {
				dotIrodsCollectionPresent = this
						.dotIrodsCollectionPresentInCollection(parent
								.getAbsolutePath());
			} catch (JargonException je) {
				log.info("Exception when trying to access parent (probably lack of permissions), ending recursion");
				break;
			}

			if (dotIrodsCollectionPresent) {
				String dotIrodsAbsPath = computeDotIrodsPathUnderParent(parent
						.getAbsolutePath());

				log.info(".irods collection exists: {}", dotIrodsAbsPath);

				dotIrodsFile = this.getIrodsAccessObjectFactory()
						.getIRODSFileFactory(getIrodsAccount())
						.instanceIRODSFile(dotIrodsAbsPath);

				String absPathToDir = dotIrodsAbsPath;
				absPathToDir += useSubDir ? ('/' + subDir) : "";

				try {
					@SuppressWarnings("unused")
					Collection testCollection = irodsAccessObjectFactory
							.getCollectionAO(irodsAccount).findByAbsolutePath(
									absPathToDir);

				} catch (JargonException je) {
					log.info("Exception when trying to access subdir (does not exist or lack of permissions) - continuing");
					parent = (IRODSFile) parent.getParentFile();
					continue;
				}

				dirFile = this.getIrodsAccessObjectFactory()
						.getIRODSFileFactory(getIrodsAccount())
						.instanceIRODSFile(absPathToDir);

				if (useFilter) {
					dirFileList = dirFile.listFiles(filter);
				} else {
					dirFileList = dirFile.listFiles();
				}

				for (File f : dirFileList) {
					if (resolveConflicts) {
						// Check if there is already a file with the same name
						// in the list
						// Since we start with the leaf and work up, higher
						// priority will be placed in the list first
						boolean nameConflict = false;
						for (File test : returnFileList) {
							if (f.getName().compareTo(test.getName()) == 0) {
								log.info(
										"Name collision with {},\n {} not added to file list",
										test.getPath(), f.getPath());
								nameConflict = true;
								break;
							}
						}

						if (!nameConflict) {
							log.info("{} added to file list", f.getPath());
							returnFileList.add(f);
						}
					} else {
						log.info("{} added to file list", f.getPath());
						returnFileList.add(f);
					}
				}
			} else {
				log.info("No .irods collection found in collection {}",
						parent.getAbsolutePath());
			}

			log.info("End of loop, continuing with parent collection {}",
					parent.getParent());

			parent = (IRODSFile) parent.getParentFile();
		}

		return returnFileList.toArray(new File[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesInDirectoryHierarchyDotIrods (java.lang.String, boolean)
	 */
	@Override
	public File[] listFilesInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath, boolean resolveConflicts)
			throws FileNotFoundException, JargonException {
		log.info("listFilesInDirectoryHierarchyDotIrods()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		return listFilesInDirectoryHierarchyDotIrods(irodsAbsolutePath, null,
				null, resolveConflicts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesInDirectoryHierarchyDotIrods (java.lang.String)
	 */
	@Override
	public File[] listFilesInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath) throws FileNotFoundException,
			JargonException {
		log.info("listFilesInDirectoryHierarchyDotIrods()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		return listFilesInDirectoryHierarchyDotIrods(irodsAbsolutePath, null,
				null, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesOfTypeInDirectoryHierarchyDotIrods (java.lang.String,
	 * java.io.FilenameFilter, boolean)
	 */
	@Override
	public File[] listFilesOfTypeInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath, FilenameFilter filter,
			boolean resolveConflicts) throws FileNotFoundException,
			JargonException {
		log.info("listFilesOfTypeInDirectoryHierarchyDotIrods()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (filter == null) {
			throw new IllegalArgumentException("null filter");
		}

		return listFilesInDirectoryHierarchyDotIrods(irodsAbsolutePath, null,
				filter, resolveConflicts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesOfTypeInDirectoryHierarchyDotIrods (java.lang.String,
	 * java.io.FilenameFilter, boolean)
	 */
	@Override
	public File[] listFilesOfTypeInDirectoryHierarchyDotIrods(
			final String irodsAbsolutePath, FilenameFilter filter)
			throws FileNotFoundException, JargonException {
		log.info("listFilesOfTypeInDirectoryHierarchyDotIrods()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (filter == null) {
			throw new IllegalArgumentException("null filter");
		}

		return listFilesInDirectoryHierarchyDotIrods(irodsAbsolutePath, null,
				null, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesOfTypeInDirectoryHierarchyDotIrodsSubdir (java.lang.String,
	 * java.lang.String, java.io.FilenameFilter, boolean)
	 */
	@Override
	public File[] listFilesOfTypeInDirectoryHierarchyDotIrodsSubDir(
			final String irodsAbsolutePath, final String subDir,
			FilenameFilter filter, boolean resolveConflicts)
			throws FileNotFoundException, JargonException {
		log.info("listFilesOfTypeInDirectoryHierarchyDotIrodsSubDir()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (subDir == null || subDir.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty subDir - if this is intentional, use listFilesOfTypeInDirectoryHierarchyDotIrods() instead");
		}

		if (filter == null) {
			throw new IllegalArgumentException("null filter");
		}

		return listFilesInDirectoryHierarchyDotIrods(irodsAbsolutePath, subDir,
				filter, resolveConflicts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#
	 * listFilesOfTypeInDirectoryHierarchyDotIrodsSubdir (java.lang.String,
	 * java.lang.String, java.io.FilenameFilter)
	 */
	@Override
	public File[] listFilesOfTypeInDirectoryHierarchyDotIrodsSubDir(
			final String irodsAbsolutePath, final String subDir,
			FilenameFilter filter) throws FileNotFoundException,
			JargonException {
		log.info("listFilesOfTypeInDirectoryHierarchyDotIrodsSubDir()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (subDir == null || subDir.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty subDir - if this is intentional, use listFilesOfTypeInDirectoryHierarchyDotIrods() instead");
		}

		if (filter == null) {
			throw new IllegalArgumentException("null filter");
		}

		return listFilesInDirectoryHierarchyDotIrods(irodsAbsolutePath, subDir,
				filter, true);

	}
}