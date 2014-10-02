package org.irods.jargon.vircoll.impl;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.vircoll.VirtualCollection;
import org.irods.jargon.vircoll.VirtualCollectionPersistenceService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class VirtualCollectionMaintenanceServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "VirtualCollectionMaintenanceServiceImplTest";
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

	@Ignore
	// FIXME: update how vcs are named
	public void retrieveVirtualCollectionFromFile() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String vcName = "retrieveVirtualCollectionFromFile";

		VirtualCollection configurableVirtualCollection = new VirtualCollection();
		configurableVirtualCollection.setDescription("description");
		configurableVirtualCollection.setI18Description("i18ndescription");
		configurableVirtualCollection.setI18icon("i18icon");
		configurableVirtualCollection.setI18Name("i18name");
		configurableVirtualCollection.setPagingStyle(PagingStyle.CONTINUOUS);
		configurableVirtualCollection.setUniqueName(vcName);
		configurableVirtualCollection.getParameters().put("test", "hello");

		VirtualCollectionPersistenceService virtualCollectionMaintenanceService = new VirtualCollectionPersistenceServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		virtualCollectionMaintenanceService
				.addVirtualCollectionToUserCollection(configurableVirtualCollection);

		VirtualCollection actual = virtualCollectionMaintenanceService
				.retrieveVirtualCollectionFromUserCollection(
						irodsAccount.getUserName(), vcName);

		Assert.assertNotNull("null vc returned", actual);

	}

	@Test
	public void serializeVirtualCollectionToJson() throws Exception {
		VirtualCollection configurableVirtualCollection = new VirtualCollection();
		configurableVirtualCollection.setDescription("description");
		configurableVirtualCollection.setI18Description("i18ndescription");
		configurableVirtualCollection.setI18icon("i18icon");
		configurableVirtualCollection.setI18Name("i18name");
		configurableVirtualCollection.setPagingStyle(PagingStyle.CONTINUOUS);
		configurableVirtualCollection.setUniqueName("sparql1");
		configurableVirtualCollection.getParameters().put("test", "hello");

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildDummyIrodsAccount();

		VirtualCollectionPersistenceService virtualCollectionMaintenanceService = new VirtualCollectionPersistenceServiceImpl(
				irodsAccessObjectFactory, irodsAccount);

		String json = virtualCollectionMaintenanceService
				.serializeVirtualCollectionToJson(configurableVirtualCollection);
		Assert.assertNotNull("null json from service", json);
		Assert.assertFalse("empty json", json.isEmpty());

	}

}
