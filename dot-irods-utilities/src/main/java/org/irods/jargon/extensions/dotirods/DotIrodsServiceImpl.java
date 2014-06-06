/**
 *
 */
package org.irods.jargon.extensions.dotirods;

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
		log.info("findUserHomeCollection()");

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

}
