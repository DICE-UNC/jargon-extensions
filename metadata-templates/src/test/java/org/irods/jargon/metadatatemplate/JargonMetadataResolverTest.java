package org.irods.jargon.metadatatemplate;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class JargonMetadataResolverTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "JargonMetadataResolverTest";
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
	public void testListTemplatesInIrodsHierarchyAbovePath() {
		fail("Not yet implemented");
	}

	@Test
	public void testListPublicTemplates() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindTemplateByName() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindTemplateByFqName() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveTemplateAsJSON() {
		fail("Not yet implemented");
	}

	@Test
	public void testRenameTemplateByFqName() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateTemplateByFqName() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteTemplateByFqName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFqNameForUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testJargonMetadataResolver() {
		fail("Not yet implemented");
	}

	@Test
	public void testPopulateFormBasedMetadataTemplateWithAVUs() {
		fail("Not yet implemented");
	}

	@Test
	public void testAbstractMetadataResolver() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPublicTemplateLocations() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPublicTemplateLocations() {
		fail("Not yet implemented");
	}

	@Test
	public void testListAllTemplates() {
		fail("Not yet implemented");
	}

	@Test
	public void testListAllRequiredTemplates() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindTemplateByUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testRenameTemplateByUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateTemplateByUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteTemplateByUUID() {
		fail("Not yet implemented");
	}

}
