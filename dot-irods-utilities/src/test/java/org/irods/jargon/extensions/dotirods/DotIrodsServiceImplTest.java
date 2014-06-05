package org.irods.jargon.extensions.dotirods;

import java.util.Properties;

import junit.framework.Assert;

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
	
	@Test(expected=FileNotFoundException.class)
	public void findUserHomeCollectionNotExists()
			throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
	
		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		dotIrodsService.findUserHomeCollection("obviously a bogus user");
		
	}
	
	
	@Test
	public void findUserHomeCollection()
			throws Exception {
		
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		
		String dotIrodsPath = MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount) + "/.irods";
		IRODSFile dotIrodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(dotIrodsPath);
		dotIrodsFile.delete();
		dotIrodsFile.mkdirs();
		
		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);
		DotIrodsCollection dotIrodsCollection = dotIrodsService.findUserHomeCollection(irodsAccount.getUserName());
		Assert.assertNotNull("null dotIrodsCollection returned",dotIrodsCollection);
		Assert.assertEquals("didnt set path correctly", dotIrodsPath, dotIrodsCollection.getAbsolutePath());
		Assert.assertTrue("did not set as home dir", dotIrodsCollection.isHomeDir());
		Assert.assertNotNull("did not set collection", dotIrodsCollection.getCollection());
		
		
	}
	
}
