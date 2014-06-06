/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.vircoll.types.ConfigurableVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to maintain virtual collections and persist them properly in iRODS
 * 
 * @author Mike Conway - DICE
 *
 */
public class VirtualCollectionMaintenanceServiceImpl extends
		AbstractJargonService {

	private final DotIrodsServiceImpl dotIrodsService;

	private static Logger log = LoggerFactory
			.getLogger(VirtualCollectionMaintenanceServiceImpl.class);

	public void addVirtualCollectionToUserCollection(
			final ConfigurableVirtualCollection configurableVirtualCollection)
			throws DuplicateDataException, JargonException {

		log.info("addVirtualCollectionToUserCollection()");

		if (configurableVirtualCollection == null) {
			throw new IllegalArgumentException(
					"null configurableVirtualCollection");
		}

		String liveVirtualCollectionPath = createIfNecessaryAndReturnVirtualCollectionPathInUserHome(this
				.getIrodsAccount().getUserName());

	}

	private String createIfNecessaryAndReturnVirtualCollectionPathInUserHome(
			String userName) {

		DotIrodsCollection dotIrodsFile = dotIrodsService
				.findUserHomeCollection(userName);

	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionMaintenanceServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		this.dotIrodsService = new DotIrodsServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
	}

}
