package org.irods.jargon.vircoll.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.vircoll.ConfigurableVirtualCollection;
import org.irods.jargon.vircoll.TemporaryQueryService;
import org.irods.jargon.vircoll.types.MetadataQueryMaintenanceService;
import org.irods.jargon.vircoll.types.MetadataQueryVirtualCollection;
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

		ConfigurableVirtualCollection cvc = new MetadataQueryVirtualCollection();
		cvc.setQueryString("QueryStringTest42");

		TemporaryQueryService temporaryQueryService = new TemporaryQueryServiceImpl(
				accessObjectFactory, irodsAccount);
		String uniqueName = temporaryQueryService.addOrUpdateTemporaryQuery(
				cvc, irodsAccount.getUserName(), mdQueryService);
		Assert.assertNotNull(uniqueName);

	}

	@Test
	public void testRetrieveLastNQueriesFewerThanNQueries() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		TemporaryQueryServiceImpl tempQueryService = new TemporaryQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						tempQueryService
								.computeTempQueryPathUnderDotIrods(irodsAccount
										.getUserName()));

		// FIXME: temporarily remove force
		// targetCollectionAsFile.deleteWithForceOption();
		targetCollectionAsFile.delete();
		targetCollectionAsFile.mkdirs();

		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(
				accessObjectFactory, irodsAccount);

		ConfigurableVirtualCollection cvc = new MetadataQueryVirtualCollection();

		for (int i = 1; i <= 5; i++) {
			cvc = new MetadataQueryVirtualCollection();
			cvc.setQueryString("QueryStringTest" + i);

			tempQueryService.addOrUpdateTemporaryQuery(cvc,
					irodsAccount.getUserName(), mdQueryService);
		}

		// List<ConfigurableVirtualCollection> returnedList =
		// mdQueryService.retrieveLastNVirtualCollectionsFromTemp(10,
		// irodsAccount.getUserName());

		List<ConfigurableVirtualCollection> returnedList = tempQueryService
				.getLastNTemporaryQueries(10, irodsAccount.getUserName(),
						mdQueryService);

		Assert.assertEquals("Wrong number of queries returned", 5,
				returnedList.size());
		Assert.assertTrue("Newest query not returned first", returnedList
				.get(0).getQueryString().equals("QueryStringTest5"));
	}

	@Test
	public void testRetrieveQueryByName() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		String uniqueName = "testRetrieveQueryByName";

		TemporaryQueryServiceImpl tempQueryService = new TemporaryQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(
				accessObjectFactory, irodsAccount);

		ConfigurableVirtualCollection cvc = new MetadataQueryVirtualCollection();

		cvc.setQueryString(uniqueName);
		cvc.setUniqueName(uniqueName);
		tempQueryService.addOrUpdateTemporaryQuery(cvc,
				irodsAccount.getUserName(), mdQueryService);

		// List<ConfigurableVirtualCollection> returnedList =
		// mdQueryService.retrieveLastNVirtualCollectionsFromTemp(10,
		// irodsAccount.getUserName());

		ConfigurableVirtualCollection actual = tempQueryService
				.getTemporaryQueryByUniqueName(irodsAccount.getUserName(),
						mdQueryService, uniqueName);
		Assert.assertNotNull("no query found", actual);
	}

	@Test
	public void testRetrieveLastNQueriesExactlyNQueries() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		TemporaryQueryServiceImpl tempQueryService = new TemporaryQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						tempQueryService
								.computeTempQueryPathUnderDotIrods(irodsAccount
										.getUserName()));
		// FIXME: temporarily remove force
		// targetCollectionAsFile.deleteWithForceOption();
		targetCollectionAsFile.delete();
		targetCollectionAsFile.mkdirs();

		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(
				accessObjectFactory, irodsAccount);

		ConfigurableVirtualCollection cvc = new MetadataQueryVirtualCollection();

		for (int i = 1; i <= 10; i++) {
			cvc = new MetadataQueryVirtualCollection();
			cvc.setQueryString("QueryStringTest" + i);

			tempQueryService.addOrUpdateTemporaryQuery(cvc,
					irodsAccount.getUserName(), mdQueryService);
		}

		List<ConfigurableVirtualCollection> returnedList = tempQueryService
				.getLastNTemporaryQueries(10, irodsAccount.getUserName(),
						mdQueryService);

		Assert.assertEquals("Wrong number of queries returned", 10,
				returnedList.size());
		Assert.assertTrue("Newest query not returned first", returnedList
				.get(0).getQueryString().equals("QueryStringTest10"));
	}

	@Test
	public void testRetrieveLastNQueriesMoreThanNQueries() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		TemporaryQueryServiceImpl tempQueryService = new TemporaryQueryServiceImpl(
				accessObjectFactory, irodsAccount);

		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						tempQueryService
								.computeTempQueryPathUnderDotIrods(irodsAccount
										.getUserName()));

		// FIXME: temporarily remove force
		// targetCollectionAsFile.deleteWithForceOption();
		targetCollectionAsFile.delete();
		targetCollectionAsFile.mkdirs();

		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(
				accessObjectFactory, irodsAccount);

		ConfigurableVirtualCollection cvc = new MetadataQueryVirtualCollection();

		List<String> filenameList = new ArrayList<String>();

		for (int i = 1; i <= 15; i++) {
			cvc = new MetadataQueryVirtualCollection();
			cvc.setQueryString("QueryStringTest" + i);

			String path = tempQueryService.addOrUpdateTemporaryQuery(cvc,
					irodsAccount.getUserName(), mdQueryService);
			filenameList.add(path);
		}

		List<ConfigurableVirtualCollection> returnedList = tempQueryService
				.getLastNTemporaryQueries(10, irodsAccount.getUserName(),
						mdQueryService);

		Assert.assertEquals("Wrong number of queries returned", 10,
				returnedList.size());
		Assert.assertTrue("Newest query not returned first", returnedList
				.get(0).getQueryString().equals("QueryStringTest15"));
	}

}
