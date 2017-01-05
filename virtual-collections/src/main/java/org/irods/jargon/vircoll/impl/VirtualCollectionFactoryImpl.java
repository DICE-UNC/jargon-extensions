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
import org.irods.jargon.vircoll.types.MetadataQueryVirtualCollection;
import org.irods.jargon.vircoll.types.MetadataQueryVirtualCollectionExecutor;
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

	/**
	 * TODO: this needs refactoring to a pluggable discovery mechanism with the
	 * ability to dynamically load and discover the collections/executors. Right
	 * now this is all done in code
	 */
	@SuppressWarnings("unchecked")
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
		} else if (virtualCollection.getType().equals(
				MetadataQueryVirtualCollection.MY_TYPE)) {
			return new MetadataQueryVirtualCollectionExecutor(
					(MetadataQueryVirtualCollection) virtualCollection,
					getIrodsAccessObjectFactory(), getIrodsAccount());
		} else {
			throw new UnsupportedOperationException(
					"cannot support collection type yet");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AbstractVirtualCollectionExecutor instanceCollectionBasedVirtualCollectionExecutorAtRoot()
			throws JargonException {
		log.info("instanceCollectionBasedVirtualCollectionExecutorAtRoot()");
		AbstractVirtualCollection virColl = new CollectionBasedVirtualCollection(
				"root", "/");
		return instanceExecutorBasedOnVirtualCollection(virColl);
	}
}
