package org.irods.jargon.metadatatemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class JargonMetadataResolverTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	private static final String TEMPLATE_FILE_NAME1 = "src/test/resources/templates/test1.mdtemplate";
	private static final String TEMPLATE_FILE_NAME2 = "src/test/resources/templates/test2.mdtemplate";
	private static final String TEMPLATE_FILE_NAME3 = "src/test/resources/templates/test3.mdtemplate";
	private static final String TEMPLATE_FILE_NAME4 = "src/test/resources/templates/test4.mdtemplate";
	private static final String TEMPLATE_FILE_NAME5 = "src/test/resources/templates/test5.mdtemplate";
	private static final String TEST_FILE_NAME = "src/test/resources/testFile.txt";

	private static final String TEMPLATE_NOPATH1 = "test1.mdtemplate";
	private static final String TEMPLATE_NOPATH2 = "test2.mdtemplate";
	private static final String TEMPLATE_NOPATH3 = "test3.mdtemplate";
	private static final String TEMPLATE_NOPATH4 = "test4.mdtemplate";
	private static final String TEMPLATE_NOPATH5 = "test5.mdtemplate";
	private static final String TEST_FILE_NOPATH = "testFile.txt";

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

		resolver.saveFormBasedTemplateAsJSON(template, targetIrodsCollection);
		List<MetadataTemplate> metadataTemplates = resolver
				.listAllTemplates(targetIrodsCollection);
		Assert.assertTrue("no metadata template stored",
				metadataTemplates.size() != 0);

	}

	@Test
	public void listPublicTemplatesNoDuplicates() throws Exception {
		String testDirName1 = "listPublicTemplatesNoDuplicatesDir1";
		String testDirName2 = "listPublicTemplatesNoDuplicatesDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		metadataTemplates = resolver.listPublicTemplates();

		Assert.assertTrue("wrong list returned from listPublicTemplates",
				metadataTemplates.size() == 3);
	}

	@Test
	public void listPublicTemplatesDuplicates() throws Exception {
		String testDirName1 = "listPublicTemplatesDuplicatesDir1";
		String testDirName2 = "listPublicTemplatesDuplicatesDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);

		String firstTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		metadataTemplates = resolver.listPublicTemplates();

		Assert.assertTrue("wrong list returned from listPublicTemplates",
				metadataTemplates.size() == 2);
		Assert.assertTrue("first appearance of template name not kept",
				metadataTemplates.get(0).getFqName()
						.equals(firstTemplateFqName));
	}

	@Test
	public void listTemplatesInDirectoryHierarchyAbovePathNoDuplicates()
			throws Exception {
		String testDirName1 = "listPublicTemplatesNoDuplicatesDir";
		String testDirName2 = "SubDir";
		String testDirName3 = "StartDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = targetIrodsCollection2 + '/'
				+ testDirName3;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection3);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);
		String mdTemplatePath3 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath3, irodsAccount.getDefaultStorageResource(),
				null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		metadataTemplates = resolver
				.listTemplatesInDirectoryHierarchyAbovePath(targetIrodsCollection3);

		Assert.assertTrue(
				"wrong list returned from listTemplatesInDirectoryHierarchyAbovePath",
				metadataTemplates.size() == 3);
	}

	@Test
	public void listTemplatesInDirectoryHierarchyAbovePathDuplicates()
			throws Exception {
		String testDirName1 = "listPublicTemplatesDuplicatesDir";
		String testDirName2 = "SubDir";
		String testDirName3 = "StartDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = targetIrodsCollection2 + '/'
				+ testDirName3;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection3);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);
		String mdTemplatePath3 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		String firstTemplateFqName = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath3, irodsAccount.getDefaultStorageResource(),
				null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		metadataTemplates = resolver
				.listTemplatesInDirectoryHierarchyAbovePath(targetIrodsCollection3);

		Assert.assertTrue(
				"wrong list returned from listTemplatesInDirectoryHierarchyAbovePath",
				metadataTemplates.size() == 2);
		Assert.assertTrue("first appearance of template name not kept",
				metadataTemplates.get(0).getFqName()
						.equals(firstTemplateFqName));
	}

	@Test
	public void listAllTemplatesNoDuplicates() throws Exception {
		String testDirName1 = "listAllTemplatesNoDuplicatesDir1";
		String testDirName2 = "SubDir";
		String testDirName3 = "listAllTemplatesNoDuplicatesDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName3);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection3,
				irodsAccount.getDefaultStorageResource(), null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection3));
		metadataTemplates = resolver.listAllTemplates(targetIrodsCollection2);

		Assert.assertTrue("wrong list returned from listAllTemplates",
				metadataTemplates.size() == 3);
	}

	@Test
	public void listAllTemplatesDuplicates() throws Exception {
		String testDirName1 = "listAllTemplatesDuplicatesDir1";
		String testDirName2 = "SubDir";
		String testDirName3 = "listAllTemplatesDuplicatesDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName3);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection3,
				irodsAccount.getDefaultStorageResource(), null, null);

		String firstTemplateFqName = mdTemplatePath1 + '/' + TEMPLATE_NOPATH2;

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection3));
		metadataTemplates = resolver.listAllTemplates(targetIrodsCollection2);

		Assert.assertTrue("wrong list returned from listAllTemplates",
				metadataTemplates.size() == 2);
		Assert.assertTrue("first appearance of template name not kept",
				metadataTemplates.get(1).getFqName()
						.equals(firstTemplateFqName));
	}

	@Test
	public void findTemplateByNameSingleMatch() throws Exception {
		String testDirName1 = "findTemplateByNameSingleMatchDir1";
		String testDirName2 = "SubDir";
		String testDirName3 = "findTemplateByNameSingleMatchDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName3);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection3,
				irodsAccount.getDefaultStorageResource(), null, null);

		String templateFqName = mdTemplatePath2 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection3));
		metadataTemplate = resolver.findTemplateByName("test1",
				targetIrodsCollection2);

		Assert.assertNotNull("no template returned from findTemplateByName",
				metadataTemplate);
		Assert.assertEquals("wrong template returned from findTemplateByName",
				templateFqName, metadataTemplate.getFqName());
	}

	@Test
	public void findTemplateByNameDuplicateMatch() throws Exception {
		String testDirName1 = "findTemplateByNameDuplicateMatchDir1";
		String testDirName2 = "SubDir";
		String testDirName3 = "findTemplateByNameDuplicateMatchDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName3);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection3,
				irodsAccount.getDefaultStorageResource(), null, null);

		String templateFqName = mdTemplatePath2 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection3));
		metadataTemplate = resolver
				.findTemplateByName("test1", mdTemplatePath2);

		Assert.assertNotNull("no template returned from findTemplateByName",
				metadataTemplate);
		Assert.assertEquals("wrong template returned from findTemplateByName",
				templateFqName, metadataTemplate.getFqName());
	}

	@Test
	public void findTemplateByNameNoMatch() throws Exception {
		String testDirName1 = "findTemplateByNameNoMatchDir1";
		String testDirName2 = "SubDir";
		String testDirName3 = "findTemplateByNameNoMatchDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName3);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection3,
				irodsAccount.getDefaultStorageResource(), null, null);

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection3));
		metadataTemplate = resolver.findTemplateByName("notGoingToMatch",
				mdTemplatePath2);

		Assert.assertNull(
				"findTemplateByName should have returned null for no match",
				metadataTemplate);
	}

	@Test
	public void findTemplateByNameInDirectoryHierarchyFilePresent()
			throws Exception {
		// Create M directories in a hierarchy
		// Create N template files across those directories, one of which with
		// the desired name
		// Call findTemplateByNameInDirectoryHierarchy with the leaf dir in the
		// hierarchy
		// Assert that the returned template is non-null, and that the fqName
		// matches appropriately

		String testDirName1 = "findTemplateByNameInDirectoryHierarchyFilePresentDir";
		String testDirName2 = "SubDir";
		String testDirName3 = "StartDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = targetIrodsCollection2 + '/'
				+ testDirName3;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);
		String mdTemplatePath3 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection3);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath3, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName = mdTemplatePath3 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		metadataTemplate = resolver.findTemplateByNameInDirectoryHierarchy(
				"test1", targetIrodsCollection3);

		Assert.assertNotNull(
				"no template returned from findTemplateByNameInDirectoryHierarchy",
				metadataTemplate);
		Assert.assertEquals(
				"wrong template returned from findTemplateByNameInDirectoryHierarchy",
				templateFqName, metadataTemplate.getFqName());
	}

	@Test
	public void findTemplateByNameInDirectoryHierarchyFileAbsent()
			throws Exception {
		// Create M directories in a hierarchy
		// Create N template files across those directories, none of which with
		// the desired name
		// Call findTemplateByNameInDirectoryHierarchy with the leaf dir in the
		// hierarchy
		// Assert that the returned template is null

		String testDirName1 = "findTemplateByNameInDirectoryHierarchyFileAbsentDir";
		String testDirName2 = "SubDir";
		String testDirName3 = "StartDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = targetIrodsCollection1 + '/'
				+ testDirName2;
		String targetIrodsCollection3 = targetIrodsCollection2 + '/'
				+ testDirName3;

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);
		String mdTemplatePath3 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection3);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath3, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		metadataTemplate = resolver.findTemplateByNameInDirectoryHierarchy(
				"notGoingToBeFound", targetIrodsCollection3);

		Assert.assertNull(
				"findTemplateByNameInDirectoryHierarchy should have returned null for no match",
				metadataTemplate);
	}

	@Test
	public void findTemplateByNameInPublicTemplatesFilePresent()
			throws Exception {
		// Create M public template dirs
		// Create N template files in those dirs, one of which with
		// the desired name
		// Add dirs to public template locations via setPublicTemplateLocations
		// Call findTemplateByNameInPublicTemplates
		// Assert that the returned template is non-null, and that the fqName
		// matches appropriately

		String testDirName1 = "findTemplateByNameInPublicTemplatesFilePresentDir1";
		String testDirName2 = "findTemplateByNameInPublicTemplatesFilePresentDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);

		String templateFqName = targetIrodsCollection1 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		metadataTemplate = resolver
				.findTemplateByNameInPublicTemplates("test1");

		Assert.assertNotNull(
				"no template returned from findTemplateByNameInPublicTemplates",
				metadataTemplate);
		Assert.assertEquals(
				"wrong template returned from findTemplateByNameInPublicTemplates",
				templateFqName, metadataTemplate.getFqName());
	}

	@Test
	public void findTemplateByNameInPublicTemplatesFileAbsent()
			throws Exception {
		// Create M public template dirs
		// Create N template files in those dirs, none of which with
		// the desired name
		// Add dirs to public template locations via setPublicTemplateLocations
		// Call findTemplateByNameInPublicTemplates
		// Assert that the returned template is null

		String testDirName1 = "findTemplateByNameInPublicTemplatesFileAbsentDir1";
		String testDirName2 = "findTemplateByNameInPublicTemplatesFileAbsentDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		metadataTemplate = resolver
				.findTemplateByNameInPublicTemplates("test1");

		Assert.assertNull(
				"findTemplateByNameInPublicTemplates should have returned null for no match",
				metadataTemplate);
	}

	@Test
	public void findTemplateByFqNameValid() throws Exception {
		// Create a template file in a directory
		// Call findTemplateByFqName with the fully-qualified name of the
		// template file
		// Assert that the returned template is non-null

		String testDirName1 = "findTemplateByFqNameValidDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		metadataTemplate = resolver.findTemplateByFqName(templateFqName);

		Assert.assertNotNull("no template returned from findTemplateByName",
				metadataTemplate);
	}

	@Test
	public void findTemplateByFqNameInvalid() throws Exception {
		// Call findTemplateByFqName with the fully-qualified name of a file
		// that doesn't exist
		// Assert that the returned template is null

		String testDirName1 = "findTemplateByFqNameInvalidDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String badTemplateFqName = mdTemplatePath1 + "/notReallyATemplateFile";

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		metadataTemplate = resolver.findTemplateByFqName(badTemplateFqName);

		Assert.assertNull("findTemplateByFqName should have returned null",
				metadataTemplate);
	}

	@Test
	public void getFqNameForUuidValid() throws Exception {
		// Create a template file in a directory
		// Call findTemplateByFqName with the correct UUID of that file
		// Assert that the returned path is correct

		String testDirName1 = "getFqNameForUuidValidDir1";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String retFqName = resolver.getFqNameForUUID(uuid);

		Assert.assertEquals("wrong fqName returned from getFqNameForUUID",
				retFqName, mdTemplateFqName);
	}

	@Test
	public void getFqNameForUuidInvalid() throws Exception {
		String testDirName1 = "getFqNameForUuidInvalidDir1";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String retFqName = resolver.getFqNameForUUID(UUID
				.fromString("01234567-01234-01234-01234-0123456789ab"));

		Assert.assertNull("getFqNameForUUID should have returned null",
				retFqName);
	}

	@Test
	public void saveFormBasedTemplateAsJsonGivenMdTemplateDir()
			throws Exception {
		// Create an instantiated FormBasedMetadataTemplate
		// Create a directory, as well as its .irods and mdtemplates
		// subdirectories
		// Call saveFormBased... with the instantiated object and the full path
		// Assert that a file of that name and size exists at the specified
		// location

		String testDirName1 = "saveFormBasedTemplateAsJsonGivenMdTemplateDirLoadDir";
		String testDirName2 = "saveFormBasedTemplateAsJsonGivenMdTemplateDirSaveDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		metadataTemplate = resolver.findTemplateByFqName(templateFqName);

		String pathToSavedFile = resolver.saveFormBasedTemplateAsJSON(
				(FormBasedMetadataTemplate) metadataTemplate, mdTemplatePath2);

		MetadataTemplate template = resolver
				.findTemplateByFqName(pathToSavedFile);

		Assert.assertNotNull(
				"saveFormBasedTemplateAsJSON did not save template", template);
		Assert.assertEquals(
				"saveFormBasedTemplateAsJSON saved template incorrectly",
				template.getName(), "test1");
	}

	@Test
	public void saveFormBasedTemplateAsJsonGivenParentDir() throws Exception {
		// Create an instantiated FormBasedMetadataTemplate
		// Create a directory without a .irods subdirectory
		// Call saveFormBased... with the instantiated object and the full path
		// Assert that a file of that name and size exists at
		// path/.irods/mdtemplates

		String testDirName1 = "saveFormBasedTemplateAsJsonGivenParentDirLoadDir";
		String testDirName2 = "saveFormBasedTemplateAsJsonGivenParentDirSaveDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		metadataTemplate = resolver.findTemplateByFqName(templateFqName);

		String pathToSavedFile = resolver.saveFormBasedTemplateAsJSON(
				(FormBasedMetadataTemplate) metadataTemplate,
				targetIrodsCollection2);

		MetadataTemplate template = resolver
				.findTemplateByFqName(pathToSavedFile);

		Assert.assertNotNull(
				"saveFormBasedTemplateAsJSON did not save template", template);
		Assert.assertEquals(
				"saveFormBasedTemplateAsJSON saved template incorrectly",
				template.getName(), "test1");
	}

	@Test
	public void saveFormBasedTemplateAsJsonCheckAVUs() throws Exception {
		// Create an instantiated FormBasedMetadataTemplate
		// Create a directory, as well as its .irods and mdtemplates
		// subdirectories
		// Call saveFormBased... with the instantiated object and the full path
		// Assert that the AVUs on the file saved at the specified location
		// include the correct mdtemplate and mdelement AVUs

		String testDirName1 = "saveFormBasedTemplateAsJsonCheckAVUsLoadDir";
		String testDirName2 = "saveFormBasedTemplateAsJsonCheckAVUsSaveDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);
		String mdTemplatePath2 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection2);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		metadataTemplate = resolver.findTemplateByFqName(templateFqName);

		String pathToSavedFile = resolver.saveFormBasedTemplateAsJSON(
				(FormBasedMetadataTemplate) metadataTemplate, mdTemplatePath2);

		List<MetaDataAndDomainData> templateAVUs = resolver
				.queryTemplateAVUForFile(pathToSavedFile);
		List<MetaDataAndDomainData> elementAVUs = resolver
				.queryElementAVUForFile(pathToSavedFile);

		Assert.assertTrue(
				"saveFormBasedMetadataTemplate did not create mdTemplate AVU",
				templateAVUs.size() == 1);
		Assert.assertTrue(
				"saveFormBasedMetadataTemplate did not create mdElements AVUs",
				elementAVUs.size() == 3);
	}

	@Test
	public void renameTemplateByFqNameValid() throws Exception {
		String testDirName1 = "renameTemplateByFqNameValidDir1";
		String testDirName2 = "renameTemplateByFqNameValidDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplateFqName, irodsAccount.getDefaultStorageResource(),
				null, null);

		String newFqName = targetIrodsCollection2
				+ "/newTemplateName.mdTemplate";

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		boolean retVal = resolver.renameTemplateByFqName(mdTemplateFqName,
				newFqName);

		Assert.assertTrue("renameTemplateByFqName returned false", retVal);

		MetadataTemplate template = resolver.findTemplateByFqName(newFqName);

		Assert.assertNotNull("renameTemplateByFqName did not move template",
				template);

		List<MetaDataAndDomainData> templateAVUs = resolver
				.queryTemplateAVUForFile(newFqName);

		Assert.assertEquals(
				"renameTemplateByFqName did not update mdTemplate AVU",
				templateAVUs.get(0).getAvuAttribute(), "newTemplateName");
	}

	@Test
	public void renameTemplateByFqNameInvalid() throws Exception {
		// Create at least two directories
		// Create a template in one of the directories
		// call renameTemplate... with a bogus path
		// Assert that the template is unchanged in the original directory

		String testDirName1 = "renameTemplateByFqNameInvalidDir1";
		String testDirName2 = "renameTemplateByFqNameInvalidDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;
		String newFqName = "this/is/a/bogus/filename.mdtemplate";

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		boolean retVal = resolver.renameTemplateByFqName(mdTemplateFqName,
				newFqName);

		Assert.assertFalse("renameTemplateByFqName returned true", retVal);

		MetadataTemplate template = resolver
				.findTemplateByFqName(mdTemplateFqName);

		Assert.assertNotNull(
				"renameTemplateByFqName moved original template inappropriately",
				template);

		List<MetaDataAndDomainData> templateAVUs = resolver
				.queryTemplateAVUForFile(mdTemplateFqName);

		Assert.assertEquals("renameTemplateByFqName changed mdTemplate AVU",
				templateAVUs.get(0).getAvuAttribute(), "test1");
	}

	@Test
	public void updateFormBasedTemplateValid() throws Exception {
		// Create a directory
		// Create a template file in that directory (with correct AVUs and UUID)
		// Import the template file (using findTemplateByFqName)
		// Modify the file
		// Save it back using updateFormBased...
		// Assert that the file has been modified

		String testDirName1 = "updateFormBasedTemplateValidDir1";
		String testDirName2 = "updateFormBasedTemplateValidDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName1 = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName1);

		String mdTemplateFqName2 = resolver.saveFormBasedTemplateAsJSON(
				template, targetIrodsCollection2);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		MetadataElement me = new MetadataElement();
		me.setElementName("addedElement");
		me.setType(ElementTypeEnum.RAW_INT);
		me.setDefaultValue("42");

		template.setDescription("TemplateModified");
		template.getElements().add(me);

		boolean retVal = resolver.updateFormBasedTemplateByFqName(
				mdTemplateFqName2, template);

		Assert.assertTrue("updateFormBasedTemplateByFqName returned false",
				retVal);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		Assert.assertEquals(
				"template description not changed by updateFormBasedTemplateByFqName",
				template.getDescription(), "TemplateModified");
		Assert.assertTrue(
				"elements list not changed by updateFormBasedTemplateByFqName",
				template.getElements().size() == 4);
	}

	@Test
	public void updateFormBasedTemplateUuidMismatch() throws Exception {
		// Create a directory
		// Create a template file in that directory (with correct AVUs and UUID)
		// Import the template file (using findTemplateByFqName)
		// Modify the file
		// Set a bogus UUID
		// Attempt to save it back using updateFormBased...
		// Assert that the file on disk is unchanged

		String testDirName1 = "updateFormBasedTemplateUuidMismatchDir1";
		String testDirName2 = "updateFormBasedTemplateUuidMismatchDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName1 = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName1);

		String mdTemplateFqName2 = resolver.saveFormBasedTemplateAsJSON(
				template, targetIrodsCollection2);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		MetadataElement me = new MetadataElement();
		me.setElementName("addedElement");
		me.setType(ElementTypeEnum.RAW_INT);
		me.setDefaultValue("42");

		template.setDescription("TemplateModified");
		template.getElements().add(me);
		template.setUuid(UUID
				.fromString("01234567-01234-01234-01234-0123456789ab"));

		boolean retVal = resolver.updateFormBasedTemplateByFqName(
				mdTemplateFqName2, template);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		Assert.assertFalse("updateFormBasedTemplateByFqName returned true",
				retVal);
		Assert.assertEquals(
				"template description inappropriately changed by updateFormBasedTemplateByFqName",
				template.getDescription(), "First test metadata template");
		Assert.assertTrue(
				"elements list inappropriately changed by updateFormBasedTemplateByFqName",
				template.getElements().size() == 3);
	}

	@Test
	public void updateFormBasedTemplateFqNameInvalid() throws Exception {
		// Create two directories
		// Create a template file in that directory (with correct AVUs and UUID)
		// Import the template file (using findTemplateByFqName)
		// Modify the file
		// Attempt to save it back using updateFormBased... with the second
		// directory as the fqPath
		// Assert that nothing was saved to the specified path

		String testDirName1 = "updateFormBasedTemplateFqNameInvalidDir1";
		String testDirName2 = "updateFormBasedTemplateFqNameInvalidDir2";
		String testDirName3 = "updateFormBasedTemplateFqNameInvalidDir3";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);
		String targetIrodsCollection3 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName3);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);
		IRODSFile targetCollectionAsFile3 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection3);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();
		targetCollectionAsFile3.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName1 = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;
		String mdTemplateFqName2 = targetIrodsCollection2 + '/'
				+ TEMPLATE_NOPATH2;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName1);

		String mdTemplateFqName3 = resolver.saveFormBasedTemplateAsJSON(
				template, targetIrodsCollection1);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		String mdTemplateFqName4 = resolver.saveFormBasedTemplateAsJSON(
				template, targetIrodsCollection2);

		String mdTemplateFqName5 = resolver
				.computeMetadataTemplatesPathUnderParent(targetIrodsCollection2)
				+ '/' + TEMPLATE_NOPATH1;

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName3);

		MetadataElement me = new MetadataElement();
		me.setElementName("addedElement");
		me.setType(ElementTypeEnum.RAW_INT);
		me.setDefaultValue("42");

		template.setDescription("TemplateModified");
		template.getElements().add(me);

		boolean retVal = resolver.updateFormBasedTemplateByFqName(
				mdTemplateFqName4, template);

		Assert.assertFalse("updateFormBasedTemplateByFqName returned true",
				retVal);

		MetadataTemplate template2 = resolver
				.findTemplateByFqName(mdTemplateFqName5);
		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName4);

		Assert.assertNull(
				"updateFormBasedTemplateByFqName inappropriately saved a file",
				template2);

		Assert.assertEquals(
				"updateFormBasedTemplateByFqName inappropriately modified an existing file",
				template.getName(), "test2");
	}

	@Test
	public void deleteTemplateByFqNameValid() throws Exception {
		// Create a directory
		// Create a template file in that directory (with correct AVUs and UUID)
		// Attempt to delete it using deleteTemplate...
		// Assert that the file is no longer present at that location

		String testDirName1 = "deleteTemplateByFqNameValidDir1";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_NOPATH1;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		MetadataTemplate template = resolver
				.findTemplateByFqName(mdTemplateFqName);

		Assert.assertNotNull("template could not be loaded", template);

		boolean retVal = resolver.deleteTemplateByFqName(mdTemplateFqName);

		Assert.assertTrue("deleteTemplateByFqName returned false", retVal);

		template = resolver.findTemplateByFqName(mdTemplateFqName);

		Assert.assertNull("template was not deleted by deleteTemplateByFqName",
				template);
	}

	@Test(expected = IllegalArgumentException.class)
	public void deleteTemplateByFqNameNotATemplateFile() throws Exception {

		String testDirName1 = "deleteTemplateByFqNameNotATemplateFileDir1";
		String bogusFilename = "jagnoadngaoig.gif";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		String mdTemplateFqName = targetIrodsCollection1 + '/' + bogusFilename;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.deleteTemplateByFqName(mdTemplateFqName);
	}

	@Test
	public void getAndMergeTemplateListForFile() throws Exception {
		String testDirName1 = "getAndMergeTemplateListForFileDir1";
		String testDirName2 = "getAndMergeTemplateListForFileDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;
		String templateFqName3 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH3;
		String templateFqName2 = targetIrodsCollection2 + '/'
				+ TEMPLATE_NOPATH2;

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection2));

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid1.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName1, avuData);

		UUID uuid2 = UUID.randomUUID();
		avuData = AvuData.instance("test2", uuid2.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName2, avuData);

		UUID uuid3 = UUID.randomUUID();
		avuData = AvuData.instance("test3", uuid3.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName3, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection1 + '/' + TEST_FILE_NOPATH;

		avuData = AvuData.instance(
				"attribute3",
				"test_value",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid2.toString());
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, avuData);
		avuData = AvuData.instance(
				"optional2",
				"42",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid2.toString());
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, avuData);
		avuData = AvuData.instance(
				"attribute5",
				"12",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid3.toString());
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, avuData);
		avuData = AvuData.instance(
				"attribute6",
				"true",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid3.toString());
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, avuData);
		avuData = AvuData.instance("orphan1", "littleOrphanAnnie", "");
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, avuData);
		avuData = AvuData.instance("orphan2", "oliverTwist", "");
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, avuData);

		MetadataMergeResult result = resolver
				.getAndMergeTemplateListForPath(testFileNameFQ);

		Assert.assertEquals("Wrong number of templates found", 3, result
				.getTemplates().size());
		Assert.assertEquals("Wrong number of orphan AVUs", 2, result
				.getUnmatchedAvus().size());

		// Because templates are assigned random UUIDs, they come out in random
		// order. Need to find the right one to test.
		FormBasedMetadataTemplate template = null;
		for (MetadataTemplate mt : result.getTemplates()) {
			if (mt.getName().compareTo("test2") == 0) {
				template = (FormBasedMetadataTemplate) mt;
				break;
			}
		}

		Assert.assertEquals("Templates not instantiated", "test_value",
				template.getElements().get(0).getCurrentValue());
	}

	@Test
	public void getAndMergeTemplateListForCollection() throws Exception {
		String testDirName1 = "getAndMergeTemplateListForCollectionDir1";
		String testDirName2 = "getAndMergeTemplateListForCollectionDir2";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);
		String targetIrodsCollection2 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName2);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);
		IRODSFile targetCollectionAsFile2 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection2);

		targetCollectionAsFile1.mkdirs();
		targetCollectionAsFile2.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2,
				irodsAccount.getDefaultStorageResource(), null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;
		String templateFqName3 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH3;
		String templateFqName2 = targetIrodsCollection2 + '/'
				+ TEMPLATE_NOPATH2;

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection2));

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid1.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName1, avuData);

		UUID uuid2 = UUID.randomUUID();
		avuData = AvuData.instance("test2", uuid2.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName2, avuData);

		UUID uuid3 = UUID.randomUUID();
		avuData = AvuData.instance("test3", uuid3.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName3, avuData);

		String irodsCollectionString = targetIrodsCollection1;

		avuData = AvuData.instance(
				"attribute3",
				"test_value",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid2.toString());
		accessObjectFactory.getCollectionAO(irodsAccount).addAVUMetadata(
				irodsCollectionString, avuData);
		avuData = AvuData.instance(
				"optional2",
				"42",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid2.toString());
		accessObjectFactory.getCollectionAO(irodsAccount).addAVUMetadata(
				irodsCollectionString, avuData);
		avuData = AvuData.instance(
				"attribute5",
				"12",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid3.toString());
		accessObjectFactory.getCollectionAO(irodsAccount).addAVUMetadata(
				irodsCollectionString, avuData);
		avuData = AvuData.instance(
				"attribute6",
				"true",
				JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
						+ uuid3.toString());
		accessObjectFactory.getCollectionAO(irodsAccount).addAVUMetadata(
				irodsCollectionString, avuData);
		avuData = AvuData.instance("orphan1", "littleOrphanAnnie", "");
		accessObjectFactory.getCollectionAO(irodsAccount).addAVUMetadata(
				irodsCollectionString, avuData);
		avuData = AvuData.instance("orphan2", "oliverTwist", "");
		accessObjectFactory.getCollectionAO(irodsAccount).addAVUMetadata(
				irodsCollectionString, avuData);

		MetadataMergeResult result = resolver
				.getAndMergeTemplateListForPath(irodsCollectionString);

		Assert.assertEquals("Wrong number of templates found", 3, result
				.getTemplates().size());
		Assert.assertEquals("Wrong number of orphan AVUs", 2, result
				.getUnmatchedAvus().size());

		// Because templates are assigned random UUIDs, they come out in random
		// order. Need to find the right one to test.
		FormBasedMetadataTemplate template = null;
		for (MetadataTemplate mt : result.getTemplates()) {
			if (mt.getName().compareTo("test2") == 0) {
				template = (FormBasedMetadataTemplate) mt;
				break;
			}
		}

		Assert.assertEquals("Templates not instantiated", "test_value",
				template.getElements().get(0).getCurrentValue());
	}

	@Test
	public void getAndMergeTemplateWithRefIrodsQueryForFile() throws Exception {
		String testDirName1 = "getAndMergeTemplateWithRefIrodsQueryForFileDir1";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME4,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH4;

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test4", uuid1.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName1, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection1 + '/' + TEST_FILE_NOPATH;

		MetadataMergeResult result = resolver
				.getAndMergeTemplateListForPath(testFileNameFQ);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) result
				.getTemplates().get(0);

		for (MetadataElement me : template.getElements()) {
			if (me.getName().equalsIgnoreCase("data_name")) {
				Assert.assertEquals("data.name not in currentValue",
						"data.name", me.getCurrentValue());
				Assert.assertEquals("data.name not populated",
						TEST_FILE_NOPATH, me.getName());
			} else if (me.getName().equalsIgnoreCase("data_owner_name")) {
				Assert.assertEquals("data.owner_name not in currentValue",
						"data.owner_name", me.getCurrentValue());
				Assert.assertEquals(
						"data.owner_name not populated",
						testingPropertiesHelper.getTestProperties()
								.getProperty(
										TestingPropertiesHelper.IRODS_USER_KEY),
						me.getName());
			} else if (me.getName().equalsIgnoreCase("data_owner_zone")) {
				Assert.assertEquals("data.owner_zone not in currentValue",
						"data.owner_zone", me.getCurrentValue());
				Assert.assertEquals(
						"data.owner_zone not populated",
						testingPropertiesHelper.getTestProperties()
								.getProperty(
										TestingPropertiesHelper.IRODS_ZONE_KEY),
						me.getName());
			} else if (me.getName().equalsIgnoreCase("user_type")) {
				Assert.assertEquals("user.type not in currentValue",
						"user.type", me.getCurrentValue());
				Assert.assertEquals("user.type not populated", "rodsuser",
						me.getName());
			}
		}
	}

	@Test
	public void getAndMergeTemplateWithRefIrodsQueryForCollection()
			throws Exception {
		String testDirName1 = "getAndMergeTemplateWithRefIrodsQueryForCollectionDir1";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME5,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH5;

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test5", uuid1.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName1, avuData);

		MetadataMergeResult result = resolver
				.getAndMergeTemplateListForPath(targetIrodsCollection1);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) result
				.getTemplates().get(0);

		for (MetadataElement me : template.getElements()) {
			if (me.getName().equalsIgnoreCase("coll_owner")) {
				Assert.assertEquals("coll.owner not in currentValue",
						"coll.owner", me.getCurrentValue());
				Assert.assertEquals(
						"coll.owner not populated",
						testingPropertiesHelper.getTestProperties()
								.getProperty(
										TestingPropertiesHelper.IRODS_USER_KEY),
						me.getName());
			} else if (me.getName().equalsIgnoreCase("coll_owner_zone")) {
				Assert.assertEquals("coll.owner_zone not in currentValue",
						"coll.owner_zone", me.getCurrentValue());
				Assert.assertEquals(
						"coll.owner_zone not populated",
						testingPropertiesHelper.getTestProperties()
								.getProperty(
										TestingPropertiesHelper.IRODS_ZONE_KEY),
						me.getName());
			}
		}
	}

	@Test
	public void saveTemplateToSystemMetadataForDataObject() throws Exception {
		String testDirName1 = "saveTemplateToSystemMetadataForDataObjectDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid1.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName1, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection1 + '/' + TEST_FILE_NOPATH;

		MetadataTemplate metadataTemplate = resolver
				.findTemplateByFqName(templateFqName1);
		FormBasedMetadataTemplate fbmt = null;

		if (metadataTemplate.getType() == TemplateTypeEnum.FORM_BASED) {
			fbmt = (FormBasedMetadataTemplate) metadataTemplate;
		}

		for (MetadataElement me : fbmt.getElements()) {
			if (me.getName().equalsIgnoreCase("attribute1")) {
				me.setCurrentValue("value1");
			} else if (me.getName().equalsIgnoreCase("attribute2")) {
				me.setCurrentValue("42");
			} else if (me.getName().equalsIgnoreCase("optional1")) {
				me.setCurrentValue("optional_value1");
			}
		}

		resolver.saveTemplateToSystemMetadataOnObject(fbmt, testFileNameFQ);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "attribute1"));

		queryResult = accessObjectFactory.getDataObjectAO(irodsAccount)
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
						testFileNameFQ);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute attribute1",
				!queryResult.isEmpty());

		for (MetaDataAndDomainData mdd : queryResult) {
			Assert.assertEquals("attribute1 has wrong value", "value1",
					mdd.getAvuValue());
		}

		queryElements.clear();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "attribute2"));

		queryResult = accessObjectFactory.getDataObjectAO(irodsAccount)
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
						testFileNameFQ);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute attribute2",
				!queryResult.isEmpty());

		for (MetaDataAndDomainData mdd : queryResult) {
			Assert.assertEquals("attribute2 has wrong value", "42",
					mdd.getAvuValue());
		}

		queryElements.clear();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "optional1"));

		queryResult = accessObjectFactory.getDataObjectAO(irodsAccount)
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
						testFileNameFQ);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute optional1",
				!queryResult.isEmpty());

		for (MetaDataAndDomainData mdd : queryResult) {
			Assert.assertEquals("attribute2 has wrong value",
					"optional_value1", mdd.getAvuValue());
		}
	}

	@Test
	public void saveTemplateToSystemMetadataForCollection() throws Exception {
		String testDirName1 = "saveTemplateToSystemMetadataForCollectionDir";

		String targetIrodsCollection1 = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName1);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection1);

		targetCollectionAsFile1.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH1;

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid1.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName1, avuData);

//		// Create a file in targetIrodsCollection1
//		dataTransferOperations.putOperation(TEST_FILE_NAME,
//				targetIrodsCollection1,
//				irodsAccount.getDefaultStorageResource(), null, null);
//		String testFileNameFQ = targetIrodsCollection1 + '/' + TEST_FILE_NOPATH;

		MetadataTemplate metadataTemplate = resolver
				.findTemplateByFqName(templateFqName1);
		FormBasedMetadataTemplate fbmt = null;

		if (metadataTemplate.getType() == TemplateTypeEnum.FORM_BASED) {
			fbmt = (FormBasedMetadataTemplate) metadataTemplate;
		}

		for (MetadataElement me : fbmt.getElements()) {
			if (me.getName().equalsIgnoreCase("attribute1")) {
				me.setCurrentValue("value1");
			} else if (me.getName().equalsIgnoreCase("attribute2")) {
				me.setCurrentValue("42");
			} else if (me.getName().equalsIgnoreCase("optional1")) {
				me.setCurrentValue("optional_value1");
			}
		}

		resolver.saveTemplateToSystemMetadataOnObject(fbmt, targetIrodsCollection1);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "attribute1"));

		queryResult = accessObjectFactory.getCollectionAO(irodsAccount)
				.findMetadataValuesByMetadataQueryForCollection(queryElements,
						targetIrodsCollection1);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute attribute1",
				!queryResult.isEmpty());

		for (MetaDataAndDomainData mdd : queryResult) {
			Assert.assertEquals("attribute1 has wrong value", "value1",
					mdd.getAvuValue());
		}

		queryElements.clear();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "attribute2"));

		queryResult = accessObjectFactory.getCollectionAO(irodsAccount)
				.findMetadataValuesByMetadataQueryForCollection(queryElements,
						targetIrodsCollection1);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute attribute2",
				!queryResult.isEmpty());

		for (MetaDataAndDomainData mdd : queryResult) {
			Assert.assertEquals("attribute2 has wrong value", "42",
					mdd.getAvuValue());
		}

		queryElements.clear();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "optional1"));

		queryResult = accessObjectFactory.getCollectionAO(irodsAccount)
				.findMetadataValuesByMetadataQueryForCollection(queryElements,
						targetIrodsCollection1);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute optional1",
				!queryResult.isEmpty());

		for (MetaDataAndDomainData mdd : queryResult) {
			Assert.assertEquals("attribute2 has wrong value",
					"optional_value1", mdd.getAvuValue());
		}
	}
}
