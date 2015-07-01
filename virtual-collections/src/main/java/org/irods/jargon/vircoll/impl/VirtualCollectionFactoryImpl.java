/**
 *
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.usertagging.starring.IRODSStarringService;
import org.irods.jargon.usertagging.starring.IRODSStarringServiceImpl;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.irods.jargon.vircoll.VirtualCollectionExecutorFactory;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollectionExecutor;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollectionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory implementation for virtual collections.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class VirtualCollectionFactoryImpl extends AbstractJargonService
		implements VirtualCollectionExecutorFactory {

	static Logger log = LoggerFactory
			.getLogger(VirtualCollectionFactoryImpl.class);

	/**
	 * Public constructor necessary (argh) for grails mocking, sorry, don't use
	 * this
	 */
	public VirtualCollectionFactoryImpl() {
		super();
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionFactoryImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	public AbstractVirtualCollectionExecutor instanceExecutorBasedOnVirtualCollection(
			AbstractVirtualCollection virtualCollection)
			throws DataNotFoundException, JargonException {
		log.info("instanceExecutor()");

		if (virtualCollection == null) {
			throw new IllegalArgumentException(
					"null or empty virtualCollection");
		}

		log.info("virtualCollection:{}", virtualCollection);

		log.info("finding executor for vc...");

		if (virtualCollection.getType().equals(
				CollectionBasedVirtualCollection.MY_TYPE)) {

			return new CollectionBasedVirtualCollectionExecutor(
					(CollectionBasedVirtualCollection) virtualCollection,
					getIrodsAccessObjectFactory(), getIrodsAccount());
		} else if (virtualCollection.getType().equals(
				StarredFoldersVirtualCollection.MY_TYPE)) {
			// TODO: refactor into executor code
			IRODSStarringService irodsStarringService = new IRODSStarringServiceImpl(
					getIrodsAccessObjectFactory(), getIrodsAccount());
			return new StarredFoldersVirtualCollectionExecutor(
					(StarredFoldersVirtualCollection) virtualCollection,
					getIrodsAccessObjectFactory(), getIrodsAccount(),
					irodsStarringService);
		} else {
			throw new UnsupportedOperationException(
					"cannot support collection type yet");
		}
	}
}
