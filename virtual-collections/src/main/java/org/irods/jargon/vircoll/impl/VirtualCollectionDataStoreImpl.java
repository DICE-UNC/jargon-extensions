/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.vircoll.AbstractVirtualCollection;
import org.irods.jargon.vircoll.exception.VirtualCollectionProfileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle lower level data storage for virtual collections
 * 
 * @author Mike Conway - DICE
 *
 */
public class VirtualCollectionDataStoreImpl extends AbstractJargonService {

	private static Logger log = LoggerFactory
			.getLogger(VirtualCollectionDataStoreImpl.class);
	private DotIrodsService dotIrodsService;

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionDataStoreImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		dotIrodsService = new DotIrodsServiceImpl(
				this.irodsAccessObjectFactory, this.irodsAccount);
	}

	/**
	 * 
	 */
	public VirtualCollectionDataStoreImpl() {
	}

	public List<AbstractVirtualCollection> listTemporaryUserVirtualCollections(
			final String userName) throws VirtualCollectionProfileException {

		log.info("listTemporaryUserVirtualCollections()");
		String myUserName;
		if (userName == null || userName.isEmpty()) {
			myUserName = this.getIrodsAccount().getUserName();
		} else {
			myUserName = userName;
		}

		log.info("myUserName:{}", myUserName);
		log.info("finding a dot irods collection for the user...");
		DotIrodsCollection userHome;
		try {
			userHome = dotIrodsService.findUserHomeCollection(myUserName);

		} catch (JargonException e) {
			log.error("error finding dot irods collection", e);
			throw new VirtualCollectionProfileException(
					"could not list temporary virtual collections", e);
		}

		return null;

	}

	public DotIrodsService getDotIrodsService() {
		return dotIrodsService;
	}

	public void setDotIrodsService(DotIrodsService dotIrodsService) {
		this.dotIrodsService = dotIrodsService;
	}

}
