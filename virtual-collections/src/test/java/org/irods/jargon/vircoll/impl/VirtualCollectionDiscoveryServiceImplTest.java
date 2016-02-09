package org.irods.jargon.vircoll.impl;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.vircoll.UserVirtualCollectionProfile;
import org.irods.jargon.vircoll.VirtualCollectionDiscoveryService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class VirtualCollectionDiscoveryServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "MetadataQueryServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testUserVcsNoQueries() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		VirtualCollectionDiscoveryService virtualCollectionDiscoveryService = new VirtualCollectionDiscoveryServiceImpl(
				accessObjectFactory, irodsAccount);

		UserVirtualCollectionProfile actual = virtualCollectionDiscoveryService
				.userVirtualCollectionProfile(irodsAccount.getUserName());
		Assert.assertNotNull("null profile returned", actual);
		Assert.assertEquals("did not set user", irodsAccount.getUserName(),
				actual.getUserName());
		Assert.assertEquals("did not set zone", irodsAccount.getZone(),
				actual.getHomeZone());
		Assert.assertFalse("did not get vcs", actual.getUserHomeCollections()
				.isEmpty());

	}

	@Test
	public void testUserVcsNoQueriesNullAccount() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		VirtualCollectionDiscoveryService virtualCollectionDiscoveryService = new VirtualCollectionDiscoveryServiceImpl(
				accessObjectFactory, irodsAccount);

		UserVirtualCollectionProfile actual = virtualCollectionDiscoveryService
				.userVirtualCollectionProfile(null);
		Assert.assertNotNull("null profile returned", actual);
		Assert.assertEquals("did not set user", irodsAccount.getUserName(),
				actual.getUserName());
		Assert.assertEquals("did not set zone", irodsAccount.getZone(),
				actual.getHomeZone());
		Assert.assertFalse("did not get vcs", actual.getUserHomeCollections()
				.isEmpty());

	}

}
