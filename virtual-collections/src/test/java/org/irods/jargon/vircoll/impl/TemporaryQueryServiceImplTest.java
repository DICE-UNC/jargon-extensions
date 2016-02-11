package org.irods.jargon.vircoll.impl;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.vircoll.ConfigurableVirtualCollection;
import org.irods.jargon.vircoll.TemporaryQueryService;
import org.irods.jargon.vircoll.types.MetadataQueryMaintenanceService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class TemporaryQueryServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	public static final String IRODS_TEST_SUBDIR_PATH = "TemporaryQueryServiceImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsFileSystem = IRODSFileSystem.instance();
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testNameAndStoreTemporaryQuery() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(
				accessObjectFactory, irodsAccount);

		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");

		TemporaryQueryService temporaryQueryService = new TemporaryQueryServiceImpl(
				accessObjectFactory, irodsAccount);
		String uniqueName = temporaryQueryService.nameAndStoreTemporaryQuery(
				cvc, irodsAccount.getUserName(), mdQueryService);
		Assert.assertNotNull(uniqueName);

	}

}
