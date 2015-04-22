package org.irods.jargon.metadatatemplate;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
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
	public void testSaveMetadataTemplateAsJsonNoDotIrodsSpecifiedDir()
			throws Exception {

		String testDirName = "testSaveMetadataTemplateAsJsonNoDotIrodsSpecifiedDir";

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
		FormBasedMetadataTemplate template = new FormBasedMetadataTemplate();
		template.setAuthor("me");
		template.setDescription("descr");
		template.setName(testDirName);
		template.setRequired(true);
		template.setSource(SourceEnum.USER);
		template.setVersion("0.0.1");

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.saveTemplateAsJSON(template, targetIrodsCollection);

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				accessObjectFactory, irodsAccount);
		File[] metadataTemplateFiles = dotIrodsService
				.listFilesOfTypeInDirectoryHierarchyDotIrods(
						targetIrodsCollection,
						new MetadataTemplateFileFilter(), true);

		Assert.assertFalse("no metadata template stored",
				metadataTemplateFiles.length == 0);

	}
}
