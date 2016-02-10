package org.irods.jargon.vircoll.unittest;

import org.irods.jargon.vircoll.impl.CollectionBasedVirtualCollectionTest;
import org.irods.jargon.vircoll.impl.MetadataQueryVirtualCollectionTest;
import org.irods.jargon.vircoll.impl.StarredFoldersVirtualCollectionImplTest;
import org.irods.jargon.vircoll.impl.VirtualCollectionDiscoveryServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CollectionBasedVirtualCollectionTest.class,
		StarredFoldersVirtualCollectionImplTest.class,
		VirtualCollectionDiscoveryServiceImplTest.class,
		MetadataQueryVirtualCollectionTest.class })
public class AllTests {

}
