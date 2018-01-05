package org.irods.jargon.extensions.dotirods;

import java.util.Properties;

import org.junit.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class DotIrodsServiceImplTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "DotIrodsServiceImplTest";
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

	@Test(expected = FileNotFoundException.class)
	public void findUserHomeCollectionNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		dotIrodsService.findUserHomeCollection("obviously a bogus user");
	}

	@Test
	public void findUserHomeCollection() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String dotIrodsPath = MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount)
				+ "/.irods";
		IRODSFile dotIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(dotIrodsPath);
		dotIrodsFile.delete();
		dotIrodsFile.mkdirs();

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		DotIrodsCollection dotIrodsCollection = dotIrodsService
				.findUserHomeCollection(irodsAccount.getUserName());
		Assert.assertNotNull("null dotIrodsCollection returned",
				dotIrodsCollection);
		Assert.assertEquals("didnt set path correctly", dotIrodsPath,
				dotIrodsCollection.getAbsolutePath());
		Assert.assertTrue("did not set as home dir",
				dotIrodsCollection.isHomeDir());
		Assert.assertNotNull("did not set collection",
				dotIrodsCollection.getCollection());
	}

	@Test
	public void deleteDotIrodsForUserHome() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String dotIrodsPath = MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount)
				+ "/.irods";
		IRODSFile dotIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(dotIrodsPath);
		dotIrodsFile.delete();
		dotIrodsFile.mkdirs();

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		dotIrodsService.deleteDotIrodsForUserHome(irodsAccount.getUserName());
		dotIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dotIrodsPath);
		Assert.assertFalse("file not deleted", dotIrodsFile.exists());

	}

	@Test
	public void deleteDotIrodsFileAtPath() throws Exception {

		String testSubdir = "deleteDotIrodsFileAtPath";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testSubdir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String dotIrodsPath = targetIrodsCollection + "/.irods";
		IRODSFile dotIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(dotIrodsPath);
		dotIrodsFile.delete();
		dotIrodsFile.mkdirs();

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		dotIrodsService.deleteDotIrodsFileAtPath(dotIrodsPath);
		dotIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dotIrodsPath);
		Assert.assertFalse("file not deleted", dotIrodsFile.exists());

	}

	@Test
	public void createDotIrodsForUserHome() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String dotIrodsPath = MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount)
				+ "/.irods";
		IRODSFile dotIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(dotIrodsPath);
		dotIrodsFile.delete();
		dotIrodsFile.mkdirs();

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		dotIrodsService.createDotIrodsForUserHome(irodsAccount.getUserName());
		dotIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dotIrodsPath);
		Assert.assertTrue("file not created", dotIrodsFile.exists());
	}

	@Test
	public void findOrCreateDotIrodsForUserHome() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String dotIrodsPath = MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount)
				+ "/.irods";
		IRODSFile dotIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(dotIrodsPath);
		dotIrodsFile.delete();
		dotIrodsFile.mkdirs();

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		dotIrodsService.findOrCreateUserHomeCollection(irodsAccount
				.getUserName());
		dotIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(dotIrodsPath);
		Assert.assertTrue("file not created", dotIrodsFile.exists());
	}

	@Test
	public void createDotIrodsAtPath() throws Exception {

		String testSubdir = "createDotIrodsAtPath";
		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testSubdir);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile dotIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		dotIrodsFile.delete();
		dotIrodsFile.mkdirs();

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		dotIrodsService.createDotIrodsUnderParent(targetIrodsCollection);
		dotIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(
						targetIrodsCollection + "/"
								+ DotIrodsConstants.DOT_IRODS_DIR);
		Assert.assertTrue("file not created", dotIrodsFile.exists());

	}

}
