package org.irods.jargon.metadatatemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.irods.jargon.core.connection.IRODSAccount;
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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JargonMetadataExporterTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static IRODSFileSystem irodsFileSystem;

	private static final String TEMPLATE_FILE_NAME1 = "src/test/resources/templates/test1.mdtemplate";
	private static final String TEMPLATE_FILE_NAME6 = "src/test/resources/templates/test6.mdtemplate";
	private static final String TEST_FILE_NAME = "src/test/resources/testFile.txt";

	private static final String TEMPLATE_NOPATH1 = "test1.mdtemplate";
	private static final String TEMPLATE_NOPATH6 = "test6.mdtemplate";
	private static final String TEST_FILE_NOPATH = "testFile.txt";

	public static final String IRODS_TEST_SUBDIR_PATH = "JargonMetadataExporterTest";
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

		JargonMetadataExporter exporter = new JargonMetadataExporter(
				accessObjectFactory, irodsAccount);

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
				me.getCurrentValue().add("value1");
			} else if (me.getName().equalsIgnoreCase("attribute2")) {
				me.getCurrentValue().add("42");
			} else if (me.getName().equalsIgnoreCase("optional1")) {
				me.getCurrentValue().add("optional_value1");
			}
		}

		exporter.saveTemplateToSystemMetadataOnObject(fbmt, testFileNameFQ);

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

		JargonMetadataExporter exporter = new JargonMetadataExporter(
				accessObjectFactory, irodsAccount);

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

		MetadataTemplate metadataTemplate = resolver
				.findTemplateByFqName(templateFqName1);
		FormBasedMetadataTemplate fbmt = null;

		if (metadataTemplate.getType() == TemplateTypeEnum.FORM_BASED) {
			fbmt = (FormBasedMetadataTemplate) metadataTemplate;
		}

		for (MetadataElement me : fbmt.getElements()) {
			if (me.getName().equalsIgnoreCase("attribute1")) {
				me.getCurrentValue().add("value1");
			} else if (me.getName().equalsIgnoreCase("attribute2")) {
				me.getCurrentValue().add("42");
			} else if (me.getName().equalsIgnoreCase("optional1")) {
				me.getCurrentValue().add("optional_value1");
			}
		}

		exporter.saveTemplateToSystemMetadataOnObject(fbmt,
				targetIrodsCollection1);

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

	@Test
	public void saveTemplateToSystemMetadataWithListInput() throws Exception {
		String testDirName1 = "saveTemplateToSystemMetadataWithListInputDir";

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

		JargonMetadataExporter exporter = new JargonMetadataExporter(
				accessObjectFactory, irodsAccount);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME6,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH6;

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test6", uuid1.toString(),
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
			if (me.getName().equalsIgnoreCase("string_list")) {
				me.getCurrentValue().add("value1");
				me.getCurrentValue().add("value2");
				me.getCurrentValue().add("value3");
				me.getCurrentValue().add("value4");
			}
		}

		exporter.saveTemplateToSystemMetadataOnObject(fbmt, testFileNameFQ);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "string_list"));

		queryResult = accessObjectFactory.getDataObjectAO(irodsAccount)
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
						testFileNameFQ);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute string_list",
				!queryResult.isEmpty());

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create 4 instances of Attribute string_list",
				queryResult.size() == 4);
	}

	@Test
	public void saveTemplateToSystemMetadataDeleteListAVUsOnFile()
			throws Exception {
		String testDirName1 = "saveTemplateToSystemMetadataDeleteListAVUsOnFileDir";

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

		JargonMetadataExporter exporter = new JargonMetadataExporter(
				accessObjectFactory, irodsAccount);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection1);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME6,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String templateFqName1 = mdTemplatePath1 + '/' + TEMPLATE_NOPATH6;

		UUID uuid1 = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test6", uuid1.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				templateFqName1, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection1,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection1 + '/' + TEST_FILE_NOPATH;

		// Create a list AVU on the test file
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, AvuData.instance("string_list", "value1", ""));
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, AvuData.instance("string_list", "value2", ""));
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, AvuData.instance("string_list", "value3", ""));
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				testFileNameFQ, AvuData.instance("string_list", "value4", ""));

		MetadataTemplate metadataTemplate = resolver
				.findTemplateByFqName(templateFqName1);
		FormBasedMetadataTemplate fbmt = null;

		if (metadataTemplate.getType() == TemplateTypeEnum.FORM_BASED) {
			fbmt = (FormBasedMetadataTemplate) metadataTemplate;
		}

		for (MetadataElement me : fbmt.getElements()) {
			if (me.getName().equalsIgnoreCase("string_list")) {
				me.getCurrentValue().add("new_value");
			}
		}

		exporter.saveTemplateToSystemMetadataOnObject(fbmt, testFileNameFQ);

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				QueryConditionOperators.EQUAL, "string_list"));

		queryResult = accessObjectFactory.getDataObjectAO(irodsAccount)
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
						testFileNameFQ);

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject did not create Attribute string_list",
				!queryResult.isEmpty());

		Assert.assertTrue(
				"saveTemplateToSystemMetadataOnObject removed original instances of Attribute string_list",
				queryResult.size() == 1);
		
		for (MetaDataAndDomainData mdd : queryResult) {
			Assert.assertEquals("string_list has wrong value",
					"new_value", mdd.getAvuValue());
		}
	}
}
