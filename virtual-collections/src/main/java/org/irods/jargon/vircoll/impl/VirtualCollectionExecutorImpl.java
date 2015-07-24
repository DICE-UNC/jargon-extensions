/**
 *
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.AbstractVirtualCollectionExecutor;
import org.irods.jargon.vircoll.VirtualCollectionExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract model of a service that can execute operations on a given virtual
 * collection. This means CRUD operations on the virtual collection, as well as
 * execution of the embedded query.
 * 
 * <p/>
 * This subclass implements a fallback to queryAll with a path hint, so that non
 * path-hintable virtual collections can behave as normal iRODS file and folder
 * queries
 * 
 * @author Mike Conway - DICE
 * 
 */
public abstract class VirtualCollectionExecutorImpl<T extends AbstractVirtualCollection>
		extends AbstractVirtualCollectionExecutor<T> {

	static Logger log = LoggerFactory
			.getLogger(VirtualCollectionExecutorImpl.class);

	private final VirtualCollectionExecutorFactory virtualCollectionExecutorFactory;

	/**
	 * Default constructor includes iRODS access information
	 * 
	 * @param irodsAccount
	 * @param irodsAccessObjectFactory
	 */
	public VirtualCollectionExecutorImpl(IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory, T collection) {
		super(collection, irodsAccessObjectFactory, irodsAccount);

		this.virtualCollectionExecutorFactory = new VirtualCollectionFactoryImpl(
				irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Generate a result list based on executing the virtual collection query
	 * 
	 * @param offset
	 *            <code>int</code> with the offset into the result set (paging
	 *            may not be supported in all subclasses)
	 * @return {@link PagingAwareCollectionListing} with the result of the query
	 * @throws JargonException
	 */
	@Override
	public abstract PagingAwareCollectionListing queryAll(int offset)
			throws JargonException;

	/**
	 * Given a path, process the query. Note that a virtual collection may be
	 * able to take a path and query and query the external service with that
	 * provided path as a 'hint'. That behavior is indicated by setting the
	 * 'pathHintable' property of the virtual collection.
	 * <p/>
	 * The system is designed such that, for external indexes that cannot search
	 * 'beneath' a given path, it will instead defer to a straight iRODS file
	 * listing under the parent.
	 * <p/>
	 * If this default behavior is not desired, it must be overridden in the
	 * child class.
	 * 
	 * @param path
	 *            <code>String</code> with the iRODS absolute path of the parent
	 *            for the query.
	 * @param offset
	 *            <code>int</code> with an offset into the result set
	 * @return {@link PagingAwareCollectionListing} with the query results
	 * @throws JargonException
	 */
	@Override
	public PagingAwareCollectionListing queryAll(String path, int offset)
			throws JargonException {

		log.info("queryAll()");
		if (path == null) {
			throw new IllegalArgumentException("null path");
		}

		log.info("path:{}", path);
		log.info("offset:{}", offset);

		if (getCollection().isPathHintable()) {
			log.error("pathHintable collection has not overridden the queryAll method that takes a path hint");
			throw new UnsupportedOperationException(
					"a pathHintable virtual collection has not overridden the queryAll method that takes a path hint");
		}

		log.info("dropping down to normal iRODS ls query as the virtual collection is not path hintable");
		AbstractVirtualCollectionExecutor<?> exec = this.virtualCollectionExecutorFactory
				.instanceCollectionBasedVirtualCollectionExecutorAtRoot();
		return exec.queryAll(path, offset);

	}

}
