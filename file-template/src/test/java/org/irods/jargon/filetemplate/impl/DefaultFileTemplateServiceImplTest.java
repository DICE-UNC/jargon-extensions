package org.irods.jargon.filetemplate.impl;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.filetemplate.FileTemplate;
import org.irods.jargon.filetemplate.FileTemplateService;
import org.irods.jargon.filetemplate.TemplateCreatedFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultFileTemplateServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;
	public static final String IRODS_TEST_SUBDIR_PATH = "DefaultFileTemplateServiceImplTest";
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
	public void testListAvailableFileTemplates() throws Exception {
		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		IRODSAccount irodsAccount = TestingPropertiesHelper
				.buildBogusIrodsAccount();
		FileTemplateService templateService = new DefaultFileTemplateServiceImpl(
				irodsAccessObjectFactory, irodsAccount);
		templateService.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		templateService.setIrodsAccount(irodsAccount);
		List<FileTemplate> templates = templateService
				.listAvailableFileTemplates();
		Assert.assertNotNull("null templates", templates);
		Assert.assertFalse("empty templates", templates.isEmpty());

	}

	@Test
	public void testCreateFileBasedOnTemplateUniqueIdentifierText()
			throws Exception {
		String testFileName = "testCreateFileBasedOnTemplateUniqueIdentifierText.txt";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		FileTemplateService fileTemplateService = new DefaultFileTemplateServiceImpl(
				accessObjectFactory, irodsAccount);

		List<FileTemplate> fileTemplates = fileTemplateService
				.listAvailableFileTemplates();

		FileTemplate textTemplate = null;
		for (FileTemplate fileTemplate : fileTemplates) {
			if (fileTemplate.getMimeType().equals("text/plain")) {
				textTemplate = fileTemplate;
				break;
			}
		}

		if (textTemplate == null) {
			throw new JargonException("cannot find text template");
		}

		TemplateCreatedFile templateCreatedFile = fileTemplateService
				.createFileBasedOnTemplateUniqueIdentifier(
						targetIrodsCollection, testFileName,
						textTemplate.getTemplateUniqueIdentifier());
		Assert.assertNotNull("null template created file", templateCreatedFile);
		Assert.assertEquals(testFileName, templateCreatedFile.getFileName());
		Assert.assertEquals(targetIrodsCollection,
				templateCreatedFile.getParentCollectionAbsolutePath());
		Assert.assertNotNull(templateCreatedFile.getFileTemplate());
		IRODSFile createdFile = accessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection,
				testFileName);
		Assert.assertTrue("file not created", createdFile.exists());

	}
}
