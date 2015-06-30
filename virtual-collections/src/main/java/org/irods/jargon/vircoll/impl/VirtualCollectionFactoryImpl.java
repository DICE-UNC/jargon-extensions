/**
 *
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.irods.jargon.vircoll.VirtualCollectionExecutorFactory;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollectionExecutor;
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
	public StarredFoldersVirtualCollectionExecutor instanceStarredFolderVirtualCollection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionBasedVirtualCollectionExecutor instanceCollectionBasedVirtualCollectionExecutor(
			String uniqueName, String parentPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractVirtualCollectionExecutor instanceExecutorBasedOnVirtualCollection(
			AbstractVirtualCollection virtualCollection)
			throws DataNotFoundException, JargonException {
		// TODO Auto-generated method stub
		return null;
	}
}
