package org.irods.jargon.metadatatemplate;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.query.MetaDataAndDomainData;
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
	// "/Users/rskarbez/Documents/metadataTemplates/jargon-extensions/metadata-templates/src/test/resources/templates/test1.mdtemplate",

	private String PRIMARY_RESOURCE_NAME = testingProperties
			.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY);

	private static final String TEMPLATE_FILE_NAME1 = "src/test/resources/templates/test1.mdtemplate";
	private static final String TEMPLATE_FILE_NAME2 = "src/test/resources/templates/test2.mdtemplate";
	private static final String TEMPLATE_FILE_NAME3 = "src/test/resources/templates/test3.mdtemplate";
	/*
	 * private static final String TEMPLATE_FILE_PATH1 =
	 * "/templates/test1.mdtemplate"; private static final String
	 * TEMPLATE_FILE_PATH2 = "/templates/test2.mdtemplate"; private static final
	 * String TEMPLATE_FILE_PATH3 = "/templates/test3.mdtemplate";
	 */
	private static final String TEMPLATE_FILE_PATH1 = "/Users/rskarbez/Documents/metadataTemplates/jargon-extensions/metadata-templates/src/test/resources/templates/test1.mdtemplate";
	private static final String TEMPLATE_FILE_PATH2 = "/Users/rskarbez/Documents/metadataTemplates/jargon-extensions/metadata-templates/src/test/resources/templates/test2.mdtemplate";
	private static final String TEMPLATE_FILE_PATH3 = "/Users/rskarbez/Documents/metadataTemplates/jargon-extensions/metadata-templates/src/test/resources/templates/test3.mdtemplate";
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

		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(
				accessObjectFactory, irodsAccount);
		File[] metadataTemplateFiles = dotIrodsService
				.listFilesOfTypeInDirectoryHierarchyDotIrods(
						targetIrodsCollection,
						new MetadataTemplateFileFilter(), true);

		Assert.assertFalse("no metadata template stored",
				metadataTemplateFiles.length == 0);

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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		metadataTemplates = resolver.listPublicTemplates();

		Assert.assertFalse("wrong list returned from listPublicTemplates",
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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);

		String firstTemplateFqName = targetIrodsCollection1
				+ TEMPLATE_FILE_NAME1;

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		metadataTemplates = resolver.listPublicTemplates();

		Assert.assertFalse("wrong list returned from listPublicTemplates",
				metadataTemplates.size() == 2);
		Assert.assertTrue("first appearance of template name not kept",
				metadataTemplates.get(0).getFqName()
						.equals(firstTemplateFqName));
	}

	@Test
	public void listTemplatesInDirectoryHierarchyAbovePathNoDuplicates()
			throws Exception {
		String testDirName1 = "listPublicTemplatesNoDuplicatesDir";
		String testDirName2 = "listPublicTemplatesNoDuplicatesSubDir";
		String testDirName3 = "listPublicTemplatesNoDuplicatesStartDir";

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
				mdTemplatePath3, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		metadataTemplates = resolver
				.listTemplatesInDirectoryHierarchyAbovePath(targetIrodsCollection3);

		Assert.assertFalse(
				"wrong list returned from listTemplatesInDirectoryHierarchyAbovePath",
				metadataTemplates.size() == 3);
	}

	@Test
	public void listTemplatesInDirectoryHierarchyAbovePathDuplicates()
			throws Exception {
		String testDirName1 = "listPublicTemplatesDuplicatesDir";
		String testDirName2 = "listPublicTemplatesDuplicatesSubDir";
		String testDirName3 = "listPublicTemplatesDuplicatesStartDir";

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

		String firstTemplateFqName = mdTemplatePath3 + TEMPLATE_FILE_NAME1;

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath3, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		metadataTemplates = resolver
				.listTemplatesInDirectoryHierarchyAbovePath(targetIrodsCollection3);

		Assert.assertFalse(
				"wrong list returned from listTemplatesInDirectoryHierarchyAbovePath",
				metadataTemplates.size() == 2);
		Assert.assertTrue("first appearance of template name not kept",
				metadataTemplates.get(0).getFqName()
						.equals(firstTemplateFqName));
	}

	@Test
	public void listAllTemplatesNoDuplicates() throws Exception {
		String testDirName1 = "listAllTemplatesNoDuplicatesDir1";
		String testDirName2 = "listAllTemplatesNoDuplicatesSubDir";
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
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection3, PRIMARY_RESOURCE_NAME, null, null);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection3));
		metadataTemplates = resolver.listAllTemplates(targetIrodsCollection2);

		Assert.assertFalse("wrong list returned from listAllTemplates",
				metadataTemplates.size() == 3);
	}

	@Test
	public void listAllTemplatesDuplicates() throws Exception {
		String testDirName1 = "listAllTemplatesDuplicatesDir1";
		String testDirName2 = "listAllTemplatesDuplicatesSubDir";
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
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection3, PRIMARY_RESOURCE_NAME, null, null);

		String firstTemplateFqName = mdTemplatePath1 + TEMPLATE_FILE_NAME2;

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		resolver.setPublicTemplateLocations(Arrays
				.asList(targetIrodsCollection3));
		metadataTemplates = resolver.listAllTemplates(targetIrodsCollection2);

		Assert.assertFalse("wrong list returned from listAllTemplates",
				metadataTemplates.size() == 2);
		Assert.assertTrue("first appearance of template name not kept",
				metadataTemplates.get(0).getFqName()
						.equals(firstTemplateFqName));
	}

	@Test
	public void findTemplateByNameSingleMatch() throws Exception {
		String testDirName1 = "findTemplateByNameSingleMatchDir1";
		String testDirName2 = "findTemplateByNameSingleMatchSubDir";
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
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection3, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = mdTemplatePath2 + '/' + TEMPLATE_FILE_NAME1;

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
	public void findTemplateByNameDuplicateMatch() throws Exception {
		String testDirName1 = "findTemplateByNameDuplicateMatchDir1";
		String testDirName2 = "findTemplateByNameDuplicateMatchSubDir";
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
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection3, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = mdTemplatePath2 + '/' + TEMPLATE_FILE_NAME1;

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
		String testDirName2 = "findTemplateByNameNoMatchSubDir";
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
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection3, PRIMARY_RESOURCE_NAME, null, null);

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
		String testDirName2 = "findTemplateByNameInDirectoryHierarchyFilePresentSubDir";
		String testDirName3 = "findTemplateByNameInDirectoryHierarchyFilePresentStartDir";

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
				mdTemplatePath3, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = mdTemplatePath3 + '/' + TEMPLATE_FILE_NAME1;

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
		String testDirName2 = "findTemplateByNameInDirectoryHierarchyFileAbsentSubDir";
		String testDirName3 = "findTemplateByNameInDirectoryHierarchyFileAbsentStartDir";

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
				mdTemplatePath3, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				mdTemplatePath2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		resolver.findTemplateByNameInPublicTemplates("test1");

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

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME3,
				targetIrodsCollection2, PRIMARY_RESOURCE_NAME, null, null);

		MetadataTemplate metadataTemplate = new FormBasedMetadataTemplate();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		resolver.setPublicTemplateLocations(Arrays.asList(
				targetIrodsCollection1, targetIrodsCollection2));

		resolver.findTemplateByNameInPublicTemplates("test1");

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
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_FILE_NAME1;

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
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;

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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;

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
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_FILE_NAME1;

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
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_FILE_NAME1;

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
				mdTemplatePath1, PRIMARY_RESOURCE_NAME, null, null);

		String templateFqName = mdTemplatePath1 + '/' + TEMPLATE_FILE_NAME1;

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
				+ TEMPLATE_FILE_NAME1;

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplateFqName, PRIMARY_RESOURCE_NAME, null, null);

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

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;
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
				.queryTemplateAVUForFile(newFqName);

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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);

		String mdTemplateFqName1 = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;
		String mdTemplateFqName2 = targetIrodsCollection2 + '/'
				+ TEMPLATE_FILE_NAME1;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName1);

		resolver.saveFormBasedTemplateAsJSON(template, targetIrodsCollection2);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		MetadataElement me = new MetadataElement();
		me.setElementName("addedElement");
		me.setType(TypeEnum.INT);
		me.setDefaultValue("42");

		template.setDescription("TemplateModified");
		template.getElements().add(me);

		boolean retVal = resolver.updateFormBasedTemplateByFqName(
				mdTemplateFqName2, template);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		Assert.assertTrue("updateFormBasedTemplateByFqName returned false",
				retVal);
		Assert.assertEquals(
				"template description not changed by updateFormBasedTemplateByFqName",
				template.getDescription(), "templateModified");
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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);

		String mdTemplateFqName1 = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;
		String mdTemplateFqName2 = targetIrodsCollection2 + '/'
				+ TEMPLATE_FILE_NAME1;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName1);

		resolver.saveFormBasedTemplateAsJSON(template, targetIrodsCollection2);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		MetadataElement me = new MetadataElement();
		me.setElementName("addedElement");
		me.setType(TypeEnum.INT);
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

		String testDirName1 = "updateFormBasedTemplateUuidMismatchDir1";
		String testDirName2 = "updateFormBasedTemplateUuidMismatchDir2";
		String testDirName3 = "updateFormBasedTemplateUuidMismatchDir3";

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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);
		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME2,
				targetIrodsCollection3, PRIMARY_RESOURCE_NAME, null, null);

		String mdTemplateFqName1 = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;
		String mdTemplateFqName2 = targetIrodsCollection2 + '/'
				+ TEMPLATE_FILE_NAME1;
		String mdTemplateFqName3 = targetIrodsCollection3 + '/'
				+ TEMPLATE_FILE_NAME1;
		String mdTemplateFqName4 = targetIrodsCollection3 + '/'
				+ TEMPLATE_FILE_NAME2;

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		FormBasedMetadataTemplate template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName1);

		resolver.saveFormBasedTemplateAsJSON(template, targetIrodsCollection2);

		template = (FormBasedMetadataTemplate) resolver
				.findTemplateByFqName(mdTemplateFqName2);

		MetadataElement me = new MetadataElement();
		me.setElementName("addedElement");
		me.setType(TypeEnum.INT);
		me.setDefaultValue("42");

		template.setDescription("TemplateModified");
		template.getElements().add(me);

		boolean retVal = resolver.updateFormBasedTemplateByFqName(
				mdTemplateFqName2, template);

		Assert.assertFalse("updateFormBasedTemplateByFqName returned true",
				retVal);

		MetadataTemplate template2 = resolver
				.findTemplateByFqName(mdTemplateFqName3);
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
				targetIrodsCollection1, PRIMARY_RESOURCE_NAME, null, null);

		String mdTemplateFqName = targetIrodsCollection1 + '/'
				+ TEMPLATE_FILE_NAME1;

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
		// Create a directory
		// Create a file in that directory
		// Create two metadata template file in that directory, one "required"
		// (A) and one that the file already uses (B)
		// Create a public directory
		// Add that directory to the public template locations
		// Create a metadata template file (C) in that directory that the test
		// file uses
		// Set the AVUs on the test file to correspond to the created metadata
		// template files B & C, as well as at least one "orphan" AVU
		// Call getAndMerge on the test file
		// Assert that MetadataTemplates A, B, & C (B & C at least partially
		// instantiated from the file) are returned, and that the orphan list
		// contains all the orphan AVUs from the file

	}
}
