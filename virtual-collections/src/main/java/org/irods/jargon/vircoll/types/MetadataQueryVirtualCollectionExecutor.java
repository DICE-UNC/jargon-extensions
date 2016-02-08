package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.mdquery.exception.MetadataQueryException;
import org.irods.jargon.mdquery.service.MetadataQueryService;
import org.irods.jargon.mdquery.service.MetadataQueryServiceImpl;
import org.irods.jargon.vircoll.exception.VirtualCollectionException;
import org.irods.jargon.vircoll.impl.VirtualCollectionExecutorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataQueryVirtualCollectionExecutor extends
		VirtualCollectionExecutorImpl<MetadataQueryVirtualCollection> {

	static Logger log = LoggerFactory
			.getLogger(MetadataQueryVirtualCollectionExecutor.class);

	/**
	 * Create an instance of an executor for Metadata Queries
	 * 
	 * @param metadataQueryVirtualCollection
	 *            {@link MetadataQueryVirtualCollection} that describes the
	 *            collection
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} used to connect to iRODS
	 * @param irodsAccount
	 *            {@link IRODSAccount} with host and login information
	 */
	public MetadataQueryVirtualCollectionExecutor(
			final MetadataQueryVirtualCollection metadataQueryVirtualCollection,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccount, irodsAccessObjectFactory,
				metadataQueryVirtualCollection);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#queryAll(int)
	 */
	@Override
	public PagingAwareCollectionListing queryAll(final int offset)
			throws VirtualCollectionException {
		log.info("queryAll()");
		MetadataQueryService metadataQueryService = new MetadataQueryServiceImpl(
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount());
		try {
			return metadataQueryService.executeQuery(this.getCollection()
					.getQueryString());
		} catch (MetadataQueryException e) {
			log.error("error executing query:{}", this.getCollection(), e);
			throw new VirtualCollectionException("unable to execute query", e);
		}

	}

}
