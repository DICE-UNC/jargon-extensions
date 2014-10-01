/**
 *
 */
package org.irods.jargon.vircoll;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract model of a service that can execute operations on a given virtual
 * collection. This means CRUD operations on the virtual collection, as well as
 * execution of the embedded query
 * 
 * @author Mike Conway - DICE
 * 
 */
public abstract class AbstractVirtualCollectionExecutor<T extends VirtualCollection>
		extends AbstractJargonService {

	static Logger log = LoggerFactory
			.getLogger(AbstractVirtualCollectionExecutor.class);

	private final T virtualCollection;

	/**
	 * Public constructor so I can mock this, even though this is stupid. Don't
	 * use this constructor
	 */
	public AbstractVirtualCollectionExecutor() {
		super();
		virtualCollection = null;
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
	public abstract PagingAwareCollectionListing queryAll(int offset)
			throws JargonException;

	/**
	 * Get the virtual collection associated with this executor
	 * 
	 * @return {@link VirtualCollection}
	 */
	public T getVirtualCollection() {
		return virtualCollection;
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	protected AbstractVirtualCollectionExecutor(final T virtualCollection,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);

		if (virtualCollection == null) {
			throw new IllegalArgumentException("null collection");
		}

		this.virtualCollection = virtualCollection;
	}

}
