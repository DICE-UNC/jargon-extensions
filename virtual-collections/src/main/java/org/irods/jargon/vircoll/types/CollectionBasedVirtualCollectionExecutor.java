/**
 *
 */
package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionPagerAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.irods.jargon.vircoll.PathHintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a virtual collection that is an actual iRODS collection (parent
 * folders are virtual collections themselves)
 * 
 * @author Mike Conway - DICE
 * 
 */

public class CollectionBasedVirtualCollectionExecutor extends
		AbstractVirtualCollectionExecutor<CollectionBasedVirtualCollection>
		implements PathHintable {

	static Logger log = LoggerFactory
			.getLogger(CollectionBasedVirtualCollectionExecutor.class);

	/**
	 * Default constructor necessary to support mocks
	 */
	public CollectionBasedVirtualCollectionExecutor() {
		super();
	}

	/**
	 * @param collection
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public CollectionBasedVirtualCollectionExecutor(
			final CollectionBasedVirtualCollection collection,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(collection, irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#queryAll(int)
	 */
	@Override
	public PagingAwareCollectionListing queryAll(final int offset)
			throws JargonException {

		log.info("queryAll()");

		log.info("offset:{}", offset);

		log.info("collection parent:{}", getVirtualCollection().getRootPath());

		CollectionPagerAO collectionPager = getIrodsAccessObjectFactory()
				.getCollectionPagerAO(getIrodsAccount());
		return collectionPager
				.retrieveFirstPageUnderParent(getVirtualCollection()
						.getRootPath());
	}

	public String getCollectionParentAbsolutePath() {
		return getVirtualCollection().getRootPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.PathHintable#queryAll(java.lang.String,
	 * int)
	 */
	@Override
	public PagingAwareCollectionListing queryAll(String path, int offset)
			throws JargonException {
		log.info("queryAll()");

		if (path == null) {
			throw new IllegalArgumentException("null path");
		}

		log.info("offset:{}", offset);
		log.info("path:{}", path);

		log.info("collection parent:{}", getVirtualCollection().getRootPath());
		String myPath;
		if (path.isEmpty()) {
			myPath = getVirtualCollection().getRootPath();
		} else if (path.indexOf(getVirtualCollection().getRootPath()) != 0) {
			log.error("my given path is not under the root path");
			throw new JargonException(
					"given path is not under root path of virtual collection");
		} else {
			myPath = path;
		}
		CollectionPagerAO collectionPager = getIrodsAccessObjectFactory()
				.getCollectionPagerAO(getIrodsAccount());
		return collectionPager.retrieveFirstPageUnderParent(myPath);
	}

}
