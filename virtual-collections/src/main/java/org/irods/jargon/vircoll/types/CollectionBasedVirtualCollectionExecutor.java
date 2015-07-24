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
		AbstractVirtualCollectionExecutor<CollectionBasedVirtualCollection> {

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

		log.info("queryAll() with offset");

		log.info("offset:{}", offset);

		log.info("collection parent:{}", getCollection().getRootPath());
		CollectionPagerAO collectionPager = getIrodsAccessObjectFactory()
				.getCollectionPagerAO(irodsAccount);
		return collectionPager.retrieveFirstPageUnderParent(getCollection()
				.getRootPath());

		// FIXME: see https://github.com/DICE-UNC/jargon-extensions/issues/7

	}

	public String getCollectionParentAbsolutePath() {
		return getCollection().getRootPath();
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
		log.info("queryAll() with path and offset");

		if (path == null) {
			throw new IllegalArgumentException("null path");
		}

		log.info("offset:{}", offset);
		log.info("path:{}", path);

		log.info("collection parent:{}", getCollection().getRootPath());
		String myPath;
		if (path.isEmpty()) {
			myPath = getCollection().getRootPath();
		} else if (path.indexOf(getCollection().getRootPath()) != 0) {
			log.error("my given path is not under the root path");
			throw new JargonException(
					"given path is not under root path of virtual collection");
		} else {
			myPath = path;
		}

		log.info("using myPath:{}", myPath);

		CollectionPagerAO collectionPager = getIrodsAccessObjectFactory()
				.getCollectionPagerAO(irodsAccount);
		return collectionPager.retrieveFirstPageUnderParent(myPath);
	}
}
