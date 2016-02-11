/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import java.util.UUID;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
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
		sb.append("-");
		sb.append(UUID.randomUUID());
		return sb.toString();
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

	/**
	 * TODO: prime for refactor
	 * 
	 * @param irodsAbsolutePathToDotIrods
	 * @return
	 * @throws VirtualCollectionException
	 */
	private String computeTempQueryPathUnderDotIrods(final String userName)
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

}
