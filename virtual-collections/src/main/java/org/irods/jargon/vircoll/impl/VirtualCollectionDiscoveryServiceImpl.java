/**
 *
 */
package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.UserVirtualCollectionProfile;
import org.irods.jargon.vircoll.VirtualCollectionDiscoveryService;
import org.irods.jargon.vircoll.exception.VirtualCollectionProfileException;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for maintaining, discovering and listing virtual collections (as
 * opposed to listing their contents). This can discover them, and return lists
 * of <code>AbstractVirtualCollection</code> subclasses
 * <p/>
 * This class serves as a point of registration, using a Builder pattern to
 * allow virtual collections to register themselves.
 * 
 * @author Mike Conway (DICE)
 * 
 */
public class VirtualCollectionDiscoveryServiceImpl extends
		AbstractJargonService implements VirtualCollectionDiscoveryService {

	private final ObjectMapper mapper = new ObjectMapper();

	private static Logger log = LoggerFactory
			.getLogger(VirtualCollectionDiscoveryServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to access Jargon services
	 * @param irodsAccount
	 *            {@link IRODSAccount} that represents the user login
	 *            credentials
	 */
	public VirtualCollectionDiscoveryServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionMaintenanceService#
	 * listDefaultUserCollections()
	 */
	@Override
	public List<AbstractVirtualCollection> listDefaultUserCollections() {
		log.info("listDefaultUserCollections()");

		List<AbstractVirtualCollection> virtualCollections = new ArrayList<AbstractVirtualCollection>();
		// add root
		virtualCollections
				.add(new CollectionBasedVirtualCollection("root", "/"));
		// add user dir
		virtualCollections
				.add(new CollectionBasedVirtualCollection(
						"My Home",
						MiscIRODSUtils
								.computeHomeDirectoryForIRODSAccount(getIrodsAccount())));
		// add starred folders
		virtualCollections.add(new StarredFoldersVirtualCollection());

		log.info("done...");
		return virtualCollections;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.VirtualCollectionDiscoveryService#
	 * userVirtualCollectionProfile(java.lang.String)
	 */
	@Override
	public UserVirtualCollectionProfile userVirtualCollectionProfile(
			String userName) throws VirtualCollectionProfileException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.VirtualCollectionDiscoveryService#
	 * listUserRecentQueries(java.lang.String)
	 */
	@Override
	public List<AbstractVirtualCollection> listUserRecentQueries(String userName)
			throws VirtualCollectionProfileException {
		return null;
	}
}
