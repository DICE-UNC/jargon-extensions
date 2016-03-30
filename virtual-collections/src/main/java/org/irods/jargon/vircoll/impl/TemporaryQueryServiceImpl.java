/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.ConfigurableVirtualCollection;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.TemporaryQueryService;
import org.irods.jargon.vircoll.VirtualCollectionMaintenanceService;
import org.irods.jargon.vircoll.exception.VirtualCollectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to manage temporary queries as virtual collections
 * 
 * @author Mike Conway - DICE
 *
 */
public class TemporaryQueryServiceImpl extends AbstractJargonService implements
		TemporaryQueryService {

	private static Logger log = LoggerFactory
			.getLogger(TemporaryQueryServiceImpl.class);
	private DotIrodsService dotIrodsService;

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public TemporaryQueryServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		dotIrodsService = new DotIrodsServiceImpl(
				this.irodsAccessObjectFactory, this.irodsAccount);

	}

	/**
	 * 
	 */
	public TemporaryQueryServiceImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.TempQueryService#generateTempUniqueName()
	 */
	@Override
	public String generateTempUniqueName() {

		StringBuilder sb = new StringBuilder();
		sb.append(System.currentTimeMillis());
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.TemporaryQueryService#getTemporaryQueryByUniqueName
	 * (java.lang.String,
	 * org.irods.jargon.vircoll.VirtualCollectionMaintenanceService,
	 * java.lang.String)
	 */
	@Override
	public ConfigurableVirtualCollection getTemporaryQueryByUniqueName(
			final String userName,
			final VirtualCollectionMaintenanceService virtualCollectionMaintenanceService,
			final String uniqueName) throws VirtualCollectionException {
		log.info("getTemporaryQueryByUniqueName()");
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (virtualCollectionMaintenanceService == null) {
			throw new IllegalArgumentException(
					"null or empty virtualCollectionMaintenanceService");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty uniqueName");
		}

		log.info("userName:{}", userName);
		log.info("uniqueName:{}", uniqueName);
		ConfigurableVirtualCollection configurableVirtualCollection = null;

		try {
			configurableVirtualCollection = virtualCollectionMaintenanceService
					.retrieveVirtualCollectionGivenUniqueName(

					uniqueName);
		} catch (FileNotFoundException fnf) {
			log.info("collection not found, returning null");
			return null;
		} catch (JargonException e) {
			log.error("error retrieving virtual collection with name:{}",
					uniqueName, e);
			throw new VirtualCollectionException(
					"error storing virtual collection", e);
		}

		log.info("found:{}", configurableVirtualCollection);
		return configurableVirtualCollection;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.vircoll.TempQueryService#nameAndStoreTemporaryQuery(
	 * org.irods.jargon.vircoll.AbstractVirtualCollection, java.lang.String)
	 */
	@Override
	public String nameAndStoreTemporaryQuery(
			final ConfigurableVirtualCollection virtualCollection,
			final String userName,
			final VirtualCollectionMaintenanceService virtualCollectionMaintenanceService)
			throws VirtualCollectionException {

		if (virtualCollection == null) {
			throw new IllegalArgumentException("null virtualCollection");
		}

		String myUser = userName;
		if (myUser == null || myUser.isEmpty()) {
			myUser = this.getIrodsAccount().getUserName();
		}

		if (virtualCollection.getUniqueName() == null
				|| virtualCollection.getUniqueName().isEmpty()) {
			virtualCollection.setUniqueName(generateTempUniqueName());
		}

		log.info("storing temp query:{}", virtualCollection);

		String parentPath = computeTempQueryPathUnderDotIrods(myUser);

		try {
			virtualCollectionMaintenanceService.addVirtualCollection(
					virtualCollection, parentPath,
					virtualCollection.getUniqueName());
			return virtualCollection.getUniqueName();
		} catch (JargonException e) {
			log.error("error storing virtual collection:{}", virtualCollection,
					e);
			throw new VirtualCollectionException(
					"error storing virtual collection", e);
		}

	}

	@Override
	public List<ConfigurableVirtualCollection> getLastNTemporaryQueries(
			int n,
			String userName,
			VirtualCollectionMaintenanceService virtualCollectionMaintenanceService)
			throws VirtualCollectionException {
		log.info("retrieveLastNVirtualCollectionsFromTemp");

		String myUser = userName;
		if (userName == null || userName.isEmpty()) {
			myUser = this.getIrodsAccount().getUserName();
		}

		if (n < 0) {
			throw new IllegalArgumentException(
					"requested a negative number of virtual collections");
		}

		List<ConfigurableVirtualCollection> returnList = new ArrayList<ConfigurableVirtualCollection>();

		String tempQueryDir = computeTempQueryPathUnderDotIrods(myUser);
		IRODSFile tempQueryDirAsIrodsFile = getPathAsIrodsFile(tempQueryDir);

		File[] tempQueries = tempQueryDirAsIrodsFile.listFiles();
		int oldestIndex = (tempQueries.length < n) ? 0
				: (tempQueries.length - n);

		for (int i = (tempQueries.length - 1); i >= oldestIndex; i--) {
			try {
				returnList
						.add(retrieveVirtualCollectionGivenFile(tempQueries[i]
								.getAbsolutePath()));
			} catch (FileNotFoundException e) {
				log.error("error reading temp query file:{}", tempQueries[i], e);
				throw new VirtualCollectionException(
						"error reading temp query file", e);
			}
		}

		return returnList;
	}

	private ConfigurableVirtualCollection retrieveVirtualCollectionGivenFile(
			String absolutePath) throws FileNotFoundException,
			VirtualCollectionException {

		log.info("retrieveVirtualCollectionGivenFile()");

		VirtualCollectionDiscoveryServiceImpl virtualCollectionDiscoveryService = new VirtualCollectionDiscoveryServiceImpl(
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount());
		AbstractVirtualCollection coll = virtualCollectionDiscoveryService
				.findVirtualCollectionBasedOnAbsolutePath(absolutePath);
		if (!(coll instanceof ConfigurableVirtualCollection)) {
			log.error("not a configurableVirtualCollection");
			throw new VirtualCollectionException(
					"Returned collection is not configurable");
		}
		return (ConfigurableVirtualCollection) coll;

	}

	/**
	 * 
	 * @param irodsAbsolutePathToDotIrods
	 * @return
	 * @throws VirtualCollectionException
	 */
	public String computeTempQueryPathUnderDotIrods(final String userName)
			throws VirtualCollectionException {
		log.info("computeTempQueryPathUnderDotIrods");
		try {
			DotIrodsCollection parent = dotIrodsService
					.findOrCreateUserHomeCollection(userName);

			IRODSFile parentFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(parent.getAbsolutePath());
			parentFile.mkdirs();

			IRODSFile tempFile = this
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(
							parent.getAbsolutePath(),
							GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
			log.info("creating temp query location at:{}", tempFile);
			tempFile.mkdirs();
			return tempFile.getAbsolutePath();
		} catch (JargonException e) {
			log.error("error saving temp query", e);
			throw new VirtualCollectionException(
					"cannot store virtual collection query", e);
		}
	}

	IRODSFile getPathAsIrodsFile(String irodsAbsolutePath) {
		log.info("getPathAsIrodsFile()");

		IRODSFile retFile = null;

		try {
			retFile = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
							irodsAbsolutePath);
		} catch (JargonException je) {
			log.error(
					"JargonException thrown by instanceIRODSFile, {} does not exist",
					irodsAbsolutePath, je);
			retFile = null;
		}

		return retFile;
	}

}
