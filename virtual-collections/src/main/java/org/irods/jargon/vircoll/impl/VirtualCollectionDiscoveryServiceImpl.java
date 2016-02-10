/**
 *
 */
package org.irods.jargon.vircoll.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.UserVirtualCollectionProfile;
import org.irods.jargon.vircoll.VirtualCollectionDiscoveryService;
import org.irods.jargon.vircoll.exception.VirtualCollectionException;
import org.irods.jargon.vircoll.exception.VirtualCollectionProfileException;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.MetadataQueryMaintenanceService;
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
		log.info("userVirtualCollectionProfile()");

		String user = userName;

		if (userName == null || userName.isEmpty()) {
			log.info("null or empty username passed - using name of logged-in user");
			user = this.getIrodsAccount().getUserName();
		}

		UserVirtualCollectionProfile userVirtualCollectionProfile = new UserVirtualCollectionProfile();
		userVirtualCollectionProfile.setHomeZone(this.getIrodsAccount()
				.getZone());
		/*
		 * XXX - should delete if below replacement is correct
		 * userVirtualCollectionProfile.setUserName(this.getIrodsAccount()
		 * .getUserName());
		 */
		userVirtualCollectionProfile.setUserName(user);
		userVirtualCollectionProfile
				.setUserHomeCollections(listDefaultUserCollections());
		userVirtualCollectionProfile
				.setUserRecentQueries(listUserRecentQueries(user));
		return userVirtualCollectionProfile;
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
		log.info("listUserRecentQueries()");

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount());
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(
				this.getIrodsAccessObjectFactory(), this.getIrodsAccount());
		DotIrodsCollection userHomeDir = null;
		try {
			userHomeDir = dotIrodsService
					.findOrCreateUserHomeCollection(userName);
		} catch (JargonException e) {
			log.error(
					"JargonException trying to find user home .irods collection",
					e);
			throw new VirtualCollectionProfileException(
					"JargonException trying to find user home .irods collection",
					e);
		}

		String tempQueryDir = computeTempMetadataQueryPathUnderDotIrods(userHomeDir
				.getAbsolutePath());
		IRODSFile tempQueryDirAsIrodsFile = getPathAsIrodsFile(tempQueryDir);

		if (tempQueryDirAsIrodsFile == null | !tempQueryDirAsIrodsFile.exists()) {
			tempQueryDirAsIrodsFile.mkdirs();
		}

		List<AbstractVirtualCollection> returnList = new ArrayList<AbstractVirtualCollection>();

		try {
			for (File f : tempQueryDirAsIrodsFile.listFiles()) {
				returnList.add(mdQueryService.retrieveVirtualCollection(
						tempQueryDir, f.getName()));
			}
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException trying to read mdQuery VC file", e);
			throw new VirtualCollectionProfileException(
					"FileNotFoundException trying to read mdQuery VC file", e);
		} catch (VirtualCollectionException e) {
			log.error(
					"VirtualCollectionException trying to read mdQuery VC file",
					e);
			throw new VirtualCollectionProfileException(
					"VirtualCollectionException trying to read mdQuery VC file",
					e);
		}

		return returnList;
	}

	String computeTempMetadataQueryPathUnderDotIrods(
			final String irodsAbsolutePathToDotIrods) {
		log.info("computeMetadataQueryPathUnderDotIrods");

		if (irodsAbsolutePathToDotIrods == null
				|| irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToParent is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToDotIrods);
		sb.append("/");
		sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
		return sb.toString();
	}

	IRODSFile getPathAsIrodsFile(String irodsAbsolutePath) {
		log.info("getPathAsIrodsFile()");

		IRODSFile retFile = null;

		try {
			retFile = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
							irodsAbsolutePath);
		} catch (JargonException je) {
			log.error(
					"JargonException thrown by instanceIRODSFile, {} does not exist",
					irodsAbsolutePath, je);
			retFile = null;
		}

		return retFile;
	}
}
