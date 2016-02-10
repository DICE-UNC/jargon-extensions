package org.irods.jargon.vircoll.impl;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.vircoll.ConfigurableVirtualCollection;
import org.irods.jargon.vircoll.UserVirtualCollectionProfile;
import org.irods.jargon.vircoll.VirtualCollectionDiscoveryService;
import org.irods.jargon.vircoll.types.MetadataQueryMaintenanceService;
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

	@Test
	public void testUserVcsNoQueryDir() throws Exception {
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
		Assert.assertEquals("did not get temp queries", 0, actual
				.getUserRecentQueries().size());
	}

	@Test
	public void testUserVcsEmptyQueryDir() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		VirtualCollectionDiscoveryService virtualCollectionDiscoveryService = new VirtualCollectionDiscoveryServiceImpl(
				accessObjectFactory, irodsAccount);

		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(
						"/testZone/home/test1/.irods/virtualCollections/user_vc_temp_recent_vc_queries");

		targetCollectionAsFile.mkdirs();

		UserVirtualCollectionProfile actual = virtualCollectionDiscoveryService
				.userVirtualCollectionProfile(null);
		Assert.assertNotNull("null profile returned", actual);
		Assert.assertEquals("did not set user", irodsAccount.getUserName(),
				actual.getUserName());
		Assert.assertEquals("did not set zone", irodsAccount.getZone(),
				actual.getHomeZone());
		Assert.assertFalse("did not get vcs", actual.getUserHomeCollections()
				.isEmpty());
		Assert.assertEquals("did not get temp queries", 0, actual
				.getUserRecentQueries().size());
	}

	@Test
	public void testUserVcsWithQueries() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		VirtualCollectionDiscoveryService virtualCollectionDiscoveryService = new VirtualCollectionDiscoveryServiceImpl(
				accessObjectFactory, irodsAccount);

		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(
						"/testZone/home/test1/.irods/virtualCollections/user_vc_temp_recent_vc_queries");

		targetCollectionAsFile.mkdirs();

		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(
				accessObjectFactory, irodsAccount);

		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");

		String queryName = "query1";
		cvc.setUniqueName(queryName);

		String savePath = "/testZone/home/test1/.irods/virtualCollections/user_vc_temp_recent_vc_queries";

		mdQueryService.storeVirtualCollection(cvc, savePath, queryName);

		UserVirtualCollectionProfile actual = virtualCollectionDiscoveryService
				.userVirtualCollectionProfile(null);
		Assert.assertNotNull("null profile returned", actual);
		Assert.assertEquals("did not set user", irodsAccount.getUserName(),
				actual.getUserName());
		Assert.assertEquals("did not set zone", irodsAccount.getZone(),
				actual.getHomeZone());
		Assert.assertFalse("did not get vcs", actual.getUserHomeCollections()
				.isEmpty());
		Assert.assertEquals("did not get temp queries", 1, actual
				.getUserRecentQueries().size());
		Assert.assertTrue("did not retrieve mdquery correctly",
				((ConfigurableVirtualCollection) actual.getUserRecentQueries()
						.get(0)).getQueryString().equals("QueryStringTest42"));
	}
}