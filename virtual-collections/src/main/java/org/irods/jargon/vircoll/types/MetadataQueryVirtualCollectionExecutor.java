package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.PagingAwareCollectionListing;
import org.irods.jargon.vircoll.impl.VirtualCollectionExecutorImpl;
import org.irods.jargon.mdquery.service.MetadataQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataQueryVirtualCollectionExecutor extends
		VirtualCollectionExecutorImpl<MetadataQueryVirtualCollection> {
	private final MetadataQueryService metadataQueryService;

	static Logger log = LoggerFactory
			.getLogger(MetadataQueryVirtualCollectionExecutor.class);

	/**
	 * Create an instance of an executor for starred folders
	 * 
	 * @param metadataQueryVirtualCollection
	 *            {@link MetadataQueryVirtualCollection} that describes the
	 *            collection
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} used to connect to iRODS
	 * @param irodsAccount
	 *            {@link IRODSAccount} with host and login information
	 * @param metadataQueryService
	 * 			  {@link MetadataQuerySerice}
	 */
	public MetadataQueryVirtualCollectionExecutor(
			final MetadataQueryVirtualCollection metadataQueryVirtualCollection,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final MetadataQueryService metadataQueryService) {
		super(irodsAccount, irodsAccessObjectFactory,
				metadataQueryVirtualCollection);

		if (metadataQueryService == null) {
			throw new IllegalArgumentException("null metadataQueryService");
		}

		this.metadataQueryService = metadataQueryService;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.AbstractVirtualCollection#queryAll(int)
	 */
	@Override
	public PagingAwareCollectionListing queryAll(final int offset)
			throws JargonException {

		PagingAwareCollectionListing listing = buildInitialPagingAwareCollectionListing();
		log.info("queryAll()");
		log.info("adding colls");
		this.addAndCharacterizeCollectionListingForSplitListing(listing,
				queryCollections(0));
		log.info("adding data objects");
		this.addAndCharacterizeDataObjectListingForSplitListing(listing,
				queryDataObjects(0));
		return listing;

	}
}
