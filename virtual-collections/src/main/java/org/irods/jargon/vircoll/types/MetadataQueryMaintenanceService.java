package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.VirtualCollectionMaintenanceService;
import org.irods.jargon.vircoll.exception.VirtualCollectionRuntimeException;
import org.irods.jargon.vircoll.impl.AbstractVirtualCollectionMaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataQueryMaintenanceService extends
		AbstractVirtualCollectionMaintenanceService implements
		VirtualCollectionMaintenanceService {
	static Logger log = LoggerFactory
			.getLogger(MetadataQueryMaintenanceService.class);

	public MetadataQueryMaintenanceService(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);

	}

	/**
	 * Finds the iRODS absolute path to the user home temp metadata query
	 * collections, will create it if it does not exist. This is idempotent so
	 * if the coll exists it will silently ignore.
	 * 
	 * @param userName
	 *            <code>String</code> user name used to locate the home
	 *            directory
	 * @return
	 */
	public String findOrCreateUserTempMetadataQueryCollection(
			final String userName) {

		log.info("findOrCreateUserTempMetadataQueryCollection()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName: {}", userName);

		try {
			DotIrodsCollection dotIrodsCollection = getDotIrodsService()
					.findOrCreateUserHomeCollection(userName);
			StringBuilder sb = new StringBuilder();
			sb.append(dotIrodsCollection.getAbsolutePath());
			sb.append("/");
			sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
			String collName = sb.toString();
			IRODSFile vcFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(collName);
			vcFile.mkdirs();
			return collName;

		} catch (JargonException e) {
			log.error("unable to compute path for vc", e);
			throw new VirtualCollectionRuntimeException(
					"unable to compute path to user home dir", e);
		}

	}

	String findOrCreateTempMetadataQueryCollection(
			final String irodsAbsolutePathToParent) throws JargonException {
		log.info("findOrCreateMetadataTemplatesCollection()");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToParent");
		}

		log.info("irodsAbsolutePathToParent: {}", irodsAbsolutePathToParent);

		String irodsAbsolutePathToCollection = null;

		if (this.isTempMetadataQueryCollection(irodsAbsolutePathToParent)) {
			// Already a .irods/md_queries collection, so we're good
			log.info("{} is a .irods/metadataTemplates collection",
					irodsAbsolutePathToParent);
			irodsAbsolutePathToCollection = irodsAbsolutePathToParent;
		} else if (this.isDotIrodsCollection(irodsAbsolutePathToParent)) {
			// Parameter is a .irods collection, need to find or create a
			// md_queries collection beneath it.
			log.info("{} is a .irods collection", irodsAbsolutePathToParent);

			if (this.isTempMetadataQueryCollectionPresentUnderDotIrodsCollection(irodsAbsolutePathToParent)) {
				log.info("{} contains a metadataTemplates collection",
						irodsAbsolutePathToParent);
				irodsAbsolutePathToCollection = this
						.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToParent);
			} else {
				log.info(
						"{} does not contain a metadataTemplates collection, attempting to create",
						irodsAbsolutePathToParent);

				if (this.createTempMetadataQueryCollectionUnderDotIrods(irodsAbsolutePathToParent)) {
					log.info("metadataTemplates collection created");
					irodsAbsolutePathToCollection = this
							.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToParent);
				} else {
					log.error("Error, collection not created");
					throw new JargonException(
							"Error in createMetadataTemplatesCollectionUnderDotIrods");
				}
			}
		} else {
			// Parameter is neither a .irods/md_queries nor a .irods
			// collection
			log.info(
					"{} is neither a .irods collection nor a metadata query collection",
					irodsAbsolutePathToParent);

			// Is it a valid irods collection at all?
			// This will throw a JargonException if not.
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(irodsAbsolutePathToParent);

			log.info("{} exists", irodsAbsolutePathToParent);

			// Create .irods subcollection
			getDotIrodsService().createDotIrodsUnderParent(
					irodsAbsolutePathToParent);

			// Make sure it was created
			if (!this
					.isDotIrodsCollectionPresentInCollection(irodsAbsolutePathToParent)) {
				log.error("Error, .irods collection was not created");
				throw new JargonException(
						"DotIrodsService.createDotIrodsUnderParent failed to create .irods");
			}

			String dotIrodsDir = this
					.computeDotIrodsPathUnderParent(irodsAbsolutePathToParent);

			log.info(".irods created: {}", dotIrodsDir);

			// Create md_queries subcollection
			if (this.createTempMetadataQueryCollectionUnderDotIrods(dotIrodsDir)) {
				log.info("metadata query collection created");
				irodsAbsolutePathToCollection = this
						.computeTempMetadataQueryPathUnderDotIrods(dotIrodsDir);
			} else {
				log.error("Error, collection not created");
				throw new JargonException(
						"Error in createMetadataQueryCollectionUnderDotIrods");
			}
		}

		log.info("irodsAbsolutePathToCollection: {}",
				irodsAbsolutePathToCollection);

		return irodsAbsolutePathToCollection;
	}

	boolean isTempMetadataQueryCollectionPresentUnderDotIrodsCollection(
			String irodsAbsolutePathToDotIrods) {
		log.info("isMetadataQueryCollectionPresentUnderParentCollection()");

		if (irodsAbsolutePathToDotIrods == null
				|| irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToDotIrods");
		}

		log.info("irodsAbsolutePathToDotIrods:{}", irodsAbsolutePathToDotIrods);

		try {
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(
					irodsAbsolutePathToDotIrods);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					irodsAbsolutePathToDotIrods, irodsAccount);
			return false;
		}

		log.info("{} exists", irodsAbsolutePathToDotIrods);

		IRODSFile metadataQueryCollectionAsFile = this
				.getPathAsIrodsFile(this
						.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToDotIrods));

		return (metadataQueryCollectionAsFile != null);
	}

	boolean isTempMetadataQueryCollection(String irodsAbsolutePath) {
		log.info("isMetadataQueryCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath),
					irodsAccount);
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		boolean retVal = false;

		if (!irodsAbsolutePath
				.endsWith(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES)) {
			retVal = false;
		} else {
			IRODSFile pathAsFile = this.getPathAsIrodsFile(irodsAbsolutePath);

			if (pathAsFile == null) {
				retVal = false;
			} else {
				retVal = pathAsFile.isDirectory();
			}
		}

		return retVal;
	}

	String computeTempMetadataQueryPathUnderParent(
			final String irodsAbsolutePathToParent) {
		log.info("computeMetadataQueryPathUnderParent");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToParent is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToParent);
		sb.append("/");
		sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
		return sb.toString();
	}

	String computeTempMetadataQueryPathUnderDotIrods(
			final String irodsAbsolutePathToDotIrods) {
		log.info("computeMetadataQueryPathUnderDotIrods");

		if (irodsAbsolutePathToDotIrods == null
				|| irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToDotIrods is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToDotIrods);
		sb.append("/");
		sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
		return sb.toString();
	}

	boolean createTempMetadataQueryCollectionUnderDotIrods(
			final String irodsAbsolutePathToDotIrods) throws JargonException {
		log.info("createMetadataQueryCollectionUnderDotIrods()");

		if (irodsAbsolutePathToDotIrods == null
				|| irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToDotIrods");
		}

		log.info("irodsAbsolutePathToDotIrods: {}", irodsAbsolutePathToDotIrods);

		String metadataQueryPath = this
				.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToDotIrods);
		log.info("metadataQueryPath computed to be:{}", metadataQueryPath);

		IRODSFile metadataQueryFile = this
				.getPathAsIrodsFile(metadataQueryPath);
		if (metadataQueryFile == null) {
			// A JargonException was caught in getPathAsIrodsFile
			throw new JargonException(
					"JargonException thrown by instanceIRODSFile in getPathAsIrodsFile");
		}
		log.info("created");

		return metadataQueryFile.mkdirs();
	}

}