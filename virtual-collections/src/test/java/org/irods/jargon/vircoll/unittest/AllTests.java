package org.irods.jargon.vircoll.unittest;

import org.irods.jargon.vircoll.impl.CollectionBasedVirtualCollectionTest;
import org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollectionImplTest;
import org.irods.jargon.vircoll.impl.VirtualCollectionExecutorFactoryImplTest;
import org.irods.jargon.vircoll.impl.VirtualCollectionMaintenanceServiceImplTest;
import org.irods.jargon.vircoll.types.SparqlViaRestVirtualCollectionExecutorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CollectionBasedVirtualCollectionTest.class,
		StarredFoldersVirtualCollectionImplTest.class,
		VirtualCollectionExecutorFactoryImplTest.class,
		VirtualCollectionMaintenanceServiceImplTest.class,
		SparqlViaRestVirtualCollectionExecutorTest.class })
public class AllTests {

}
