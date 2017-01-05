/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;

/**
 * This is a transitional class, certain to be refactored as we resolve the
 * dynamic loading of maintenance and execution services for various virtual
 * collection types. For generic handling of virtual collections, this class
 * exposes general functions to delete or recategorize a virtual collection.
 * 
 * @author Mike Conway - DICE
 *
 */
public class GenericVirtualCollectionMaintenanceService extends
		AbstractVirtualCollectionMaintenanceService {

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public GenericVirtualCollectionMaintenanceService(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public GenericVirtualCollectionMaintenanceService() {
	}

}
