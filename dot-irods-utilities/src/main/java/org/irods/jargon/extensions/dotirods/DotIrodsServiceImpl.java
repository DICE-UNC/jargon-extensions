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
public class DotIrodsServiceImpl extends AbstractJargonService implements DotIrodsService {

	private final CollectionAO collectionAO;

	public static final Logger log = LoggerFactory
			.getLogger(DotIrodsServiceImpl.class);

	/**
	 * Constructor
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory} for obtaining service objects
	 * @param irodsAccount {@link IRODSAccount} with logged in identity
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

	/* (non-Javadoc)
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#findUserHomeCollection(java.lang.String)
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

	/* (non-Javadoc)
	 * @see org.irods.jargon.extensions.dotirods.DotIrodsService#retrieveDotIrodsAtPath(java.lang.String, boolean)
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

}
