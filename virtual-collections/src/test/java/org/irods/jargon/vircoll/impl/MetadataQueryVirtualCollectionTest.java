package org.irods.jargon.vircoll.impl;

import java.io.IOException;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.vircoll.ConfigurableVirtualCollection;
import org.irods.jargon.vircoll.exception.VirtualCollectionException;
import org.irods.jargon.vircoll.types.MetadataQueryMaintenanceService;
import org.irods.jargon.vircoll.types.MetadataQueryVirtualCollection;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetadataQueryVirtualCollectionTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	private static final String TEMPLATE_FILE_NAME1 = "src/test/resources/templates/test1.mdtemplate";
	private static final String TEMPLATE_FILE_NAME2 = "src/test/resources/templates/test2.mdtemplate";
	private static final String TEMPLATE_FILE_NAME3 = "src/test/resources/templates/test3.mdtemplate";
	private static final String TEST_FILE_NAME = "src/test/resources/testFile.txt";

	private static final String TEMPLATE_NOPATH1 = "test1.mdtemplate";
	private static final String TEMPLATE_NOPATH2 = "test2.mdtemplate";
	private static final String TEMPLATE_NOPATH3 = "test3.mdtemplate";
	private static final String TEST_FILE_NOPATH = "testFile.txt";

	public static final String IRODS_TEST_SUBDIR_PATH = "MetadataQueryVirtualCollectionTest";
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
	public void testAddVirtualCollectionSuccess() throws Exception {
		String testDirName = "testAddVirtualCollectionSuccess";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");
		
		String queryName = "query1";
		cvc.setUniqueName(queryName);
		
		String savePath = targetIrodsCollection + "/" + queryName;
		
		mdQueryService.addVirtualCollection(cvc, targetIrodsCollection, queryName);
		
		IRODSFile mdQueryFile = getPathAsIrodsFile(accessObjectFactory, irodsAccount, savePath);
		
		Assert.assertNotNull("file not saved", mdQueryFile);
		
		String fileJSON = getContentsOfFileAsString(accessObjectFactory, irodsAccount, mdQueryFile);
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = objectMapper.readValue(fileJSON, JsonNode.class);
		
		String compareString = node.get("queryString").asText();
		boolean testVal = ("QueryStringTest42".equals(compareString));
		
		Assert.assertTrue("object serialized incorrectly", testVal);
	}

	@Test(expected = DuplicateDataException.class)
	public void testAddVirtualCollectionDuplicate() throws Exception {
		String testDirName = "testAddVirtualCollectionDuplicate";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");
		
		String queryName = "query1";
		cvc.setUniqueName(queryName);
		
		mdQueryService.addVirtualCollection(cvc, targetIrodsCollection, queryName);
		
		mdQueryService.addVirtualCollection(cvc, targetIrodsCollection, queryName);
	}

	@Test
	public void testStoreVirtualCollectionNoDuplicate() throws Exception {
		String testDirName = "testStoreVirtualCollectionNoDuplicate";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");
		
		String queryName = "query1";
		cvc.setUniqueName(queryName);
		
		String savePath = targetIrodsCollection + "/" + queryName;
		
		mdQueryService.storeVirtualCollection(cvc, targetIrodsCollection, queryName);
		
		IRODSFile mdQueryFile = getPathAsIrodsFile(accessObjectFactory, irodsAccount, savePath);
		
		Assert.assertNotNull("file not saved", mdQueryFile);
		
		String fileJSON = getContentsOfFileAsString(accessObjectFactory, irodsAccount, mdQueryFile);
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = objectMapper.readValue(fileJSON, JsonNode.class);
		
		String compareString = node.get("queryString").asText();
		boolean testVal = ("QueryStringTest42".equals(compareString));
		
		Assert.assertTrue("object serialized incorrectly", testVal);
	}

	@Test
	public void testStoreVirtualCollectionDuplicate() throws Exception {
		String testDirName = "testStoreVirtualCollectionDuplicate";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("OriginalQueryString");
		
		String queryName = "query1";
		cvc.setUniqueName(queryName);
		
		String savePath = targetIrodsCollection + "/" + queryName;
		
		mdQueryService.storeVirtualCollection(cvc, targetIrodsCollection, queryName);
		
		IRODSFile mdQueryFile = getPathAsIrodsFile(accessObjectFactory, irodsAccount, savePath);
		
		Assert.assertNotNull("file not saved", mdQueryFile);
		
		String fileJSON = getContentsOfFileAsString(accessObjectFactory, irodsAccount, mdQueryFile);
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = objectMapper.readValue(fileJSON, JsonNode.class);
		
		String compareString = node.get("queryString").asText();
		boolean testVal = ("OriginalQueryString".equals(compareString));
		
		Assert.assertTrue("object serialized incorrectly", testVal);
		
		cvc.setQueryString("QueryStringTest42");
		
		mdQueryService.storeVirtualCollection(cvc, targetIrodsCollection, queryName);
		
		mdQueryFile = getPathAsIrodsFile(accessObjectFactory, irodsAccount, savePath);
		
		Assert.assertNotNull("file not saved", mdQueryFile);
		
		fileJSON = getContentsOfFileAsString(accessObjectFactory, irodsAccount, mdQueryFile);
		node = objectMapper.readValue(fileJSON, JsonNode.class);
		
		compareString = node.get("queryString").asText();
		testVal = ("QueryStringTest42".equals(compareString));
		
		Assert.assertTrue("object not overwritten", testVal);
	}

	@Test
	public void testSerializeVirtualCollectionToJson() throws Exception {
		String testDirName = "testSerializeVirtualCollectionToJson";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");
		
		String queryName = "query1";
		cvc.setUniqueName(queryName);
		
		String json = mdQueryService.serializeVirtualCollectionToJson(cvc);
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode node = objectMapper.readValue(json, JsonNode.class);
		
		String compareString = node.get("queryString").asText();
		boolean testVal = ("QueryStringTest42".equals(compareString));
		
		Assert.assertTrue("object not serialized correctly", testVal);
		
		compareString = node.get("uniqueName").asText();
		testVal = ("query1".equals(compareString));
		
		Assert.assertTrue("object not serialized correctly", testVal);
	}

	@Test(expected = FileNotFoundException.class)
	public void testRetrieveVirtualCollectionFileNotFound() throws Exception {
		String testDirName = "testRetrieveVirtualCollectionFileNotFound";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		mdQueryService.retrieveVirtualCollection(targetIrodsCollection, "junkFileName");
	}

	@Test
	public void testRetrieveVirtualCollectionSuccess() throws Exception {
		String testDirName = "testRetrieveVirtualCollectionSuccess";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");
		
		String queryName = "query1";
		cvc.setUniqueName(queryName);
		
		mdQueryService.storeVirtualCollection(cvc, targetIrodsCollection, queryName);
		
		ConfigurableVirtualCollection cvcTest = mdQueryService.retrieveVirtualCollection(targetIrodsCollection, queryName);
		
		Assert.assertTrue("file not deserialized correctly", cvcTest.getQueryString().equals("QueryStringTest42"));		
	}

	@Test(expected = FileNotFoundException.class)
	public void testDeleteVirtualCollectionFileNotFound() throws Exception {
		String testDirName = "testDeleteVirtualCollectionFileNotFound";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		mdQueryService.deleteVirtualCollection(targetIrodsCollection, "junkFileName");
	}

	@Test
	public void testDeleteVirtualCollectionSuccess() throws Exception {
		String testDirName = "testDeleteVirtualCollectionSuccess";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();
		IRODSFile targetCollectionAsFile = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);
		
		targetCollectionAsFile.mkdirs();
		
		MetadataQueryMaintenanceService mdQueryService = new MetadataQueryMaintenanceService(accessObjectFactory, irodsAccount);
		
		ConfigurableVirtualCollection cvc = new ConfigurableVirtualCollection();
		cvc.setQueryString("QueryStringTest42");
		
		String queryName = "query1";
		cvc.setUniqueName(queryName);
		
		mdQueryService.storeVirtualCollection(cvc, targetIrodsCollection, queryName);
		
		String savePath = targetIrodsCollection + "/" + queryName;
		
		IRODSFile mdQueryFile = getPathAsIrodsFile(accessObjectFactory, irodsAccount, savePath);
		
		Assert.assertTrue("file not saved by store", mdQueryFile.exists());
		
		mdQueryService.deleteVirtualCollection(targetIrodsCollection, queryName);
		
		mdQueryFile = getPathAsIrodsFile(accessObjectFactory, irodsAccount, savePath);
		
		Assert.assertFalse("file not deleted", mdQueryFile.exists());
	}

	IRODSFile getPathAsIrodsFile(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount, String irodsAbsolutePath) {
		IRODSFile retFile = null;

		try {
			retFile = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
							irodsAbsolutePath);
		} catch (JargonException je) {
			retFile = null;
		}

		return retFile;
	}
	
	String getContentsOfFileAsString(IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount, IRODSFile irodsFile) {
		IRODSFileInputStream irodsFileInputStream = null;
		byte[] b = null;
		try {
			irodsFileInputStream = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFileInputStream(irodsFile);
			b = new byte[irodsFileInputStream.available()];
			irodsFileInputStream.read(b);
			String decoded = new String(b, "UTF-8");

			return decoded;
		} catch (JargonException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

}
