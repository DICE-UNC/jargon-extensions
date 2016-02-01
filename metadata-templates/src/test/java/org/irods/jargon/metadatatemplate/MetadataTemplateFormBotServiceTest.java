package org.irods.jargon.metadatatemplate;

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
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.formbot.FormBotExecutionEnum;
import org.irods.jargon.formbot.FormBotExecutionResult;
import org.irods.jargon.formbot.FormBotField;
import org.irods.jargon.formbot.FormBotForm;
import org.irods.jargon.formbot.FormBotValidationEnum;
import org.irods.jargon.formbot.FormBotValidationResult;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class MetadataTemplateFormBotServiceTest {

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
	public void buildFormBotFormBadJson() throws Exception {
		String testDirName = "buildFormBotFormBadJson";

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testDirName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		IRODSFile targetCollectionAsFile1 = accessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						targetIrodsCollection);

		targetCollectionAsFile1.mkdirs();

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath1 = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath1, irodsAccount.getDefaultStorageResource(),
				null, null);

		String dummyJson = "{\n\t\"notAName\":\"garbage\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotForm returnForm = formBotService.buildFormBotForm(dummyJson);

		Assert.assertNull("buildFormBotForm should have returned null",
				returnForm);
	}

	@Test
	public void buildFormBotFormUuidJson() throws Exception {
		String testDirName = "buildFormBotFormUuidJson";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String uuidJson = "{\n\t\"uuid\":\"" + uuid.toString() + "\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotForm returnForm = formBotService.buildFormBotForm(uuidJson);

		Assert.assertNotNull("buildFormBotForm should not have returned null",
				returnForm);
	}

	@Test
	public void buildFormBotFormFqNameJson() throws Exception {
		String testDirName = "buildFormBotFormFqNameJson";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		String faNameJson = "{\n\t\"fqName\":\"" + mdTemplateFqName + "\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotForm returnForm = formBotService.buildFormBotForm(faNameJson);

		Assert.assertNotNull("buildFormBotForm should not have returned null",
				returnForm);
	}

	@Test
	public void buildFormBotFormNameJson() throws Exception {
		String testDirName = "buildFormBotFormNameJson";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String nameJson = "{\n\t\"name\":\"test1\",\n\t\"activeDir\":\""
				+ targetIrodsCollection + "\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotForm returnForm = formBotService.buildFormBotForm(nameJson);

		Assert.assertNotNull("buildFormBotForm should not have returned null",
				returnForm);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateFormBotFieldTemplateNotFound() throws Exception {
		String testDirName = "validateFormBotFieldTemplateNotFound";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\n\t\"value\":\"15\",\n\t\"fieldUniqueName\":\"00000000-0000-0000-0000-000000000000attribute2\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotValidationResult returnResult = formBotService
				.validateFormBotField(fieldJson);

		Assert.assertEquals(
				"validateFormBotField should have returned an error",
				FormBotValidationEnum.ERROR, returnResult.getCode());
	}

	@Test
	public void validateFormBotFieldAttributeNotFound() throws Exception {
		String testDirName = "validateFormBotFieldAttributeNotFound";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\n\t\"value\":\"15\",\n\t\"fieldUniqueName\":\""
				+ uuid.toString() + "garbage\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotValidationResult returnResult = formBotService
				.validateFormBotField(fieldJson);

		Assert.assertEquals(
				"validateFormBotField should have returned an error",
				FormBotValidationEnum.ERROR, returnResult.getCode());
	}

	@Test
	public void validateFormBotFieldValidationFailed() throws Exception {
		String testDirName = "validateFormBotFieldValidationFailed";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\n\t\"value\":\"notaninteger\",\n\t\"fieldUniqueName\":\""
				+ uuid.toString() + "attribute2\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotValidationResult returnResult = formBotService
				.validateFormBotField(fieldJson);

		Assert.assertEquals(
				"validateFormBotField should have returned FAILURE, because value is not convertible to integer",
				FormBotValidationEnum.FAILURE, returnResult.getCode());
	}

	@Test
	public void validateFormBotFieldValidationSuccess() throws Exception {
		String testDirName = "validateFormBotFieldValidationSuccess";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\n\t\"value\":\"15\",\n\t\"fieldUniqueName\":\""
				+ uuid.toString() + "attribute2\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotValidationResult returnResult = formBotService
				.validateFormBotField(fieldJson);

		Assert.assertEquals(
				"validateFormBotField should have returned SUCCESS",
				FormBotValidationEnum.SUCCESS, returnResult.getCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void executeFormBotFieldTemplateNotFound() throws Exception {
		String testDirName = "executeFormBotFieldTemplateNotFound";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection + '/' + TEST_FILE_NOPATH;

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\n\t\"value\":\"15\",\n\t\"fieldUniqueName\":\"00000000-0000-0000-0000-000000000000attribute2\",\n\t\"pathToFile\":\""
				+ testFileNameFQ + "\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotExecutionResult returnResult = formBotService
				.executeFormBotField(fieldJson);

		Assert.assertEquals(
				"executeFormBotField should have returned an error",
				FormBotExecutionEnum.ERROR, returnResult.getCode());
	}

	@Test
	public void executeFormBotFieldFileNotFound() throws Exception {
		String testDirName = "executeFormBotFieldFileNotFound";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\n\t\"value\":\"15\",\n\t\"fieldUniqueName\":\""
				+ uuid.toString()
				+ "attribute2\",\n\t\"pathToFile\":\"path/is/bogus\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotExecutionResult returnResult = formBotService
				.executeFormBotField(fieldJson);

		Assert.assertEquals(
				"executeFormBotField should have returned an error",
				FormBotExecutionEnum.ERROR, returnResult.getCode());
	}

	@Test
	public void executeFormBotFieldAttributeNotFound() throws Exception {
		String testDirName = "executeFormBotFieldAttributeNotFound";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection + '/' + TEST_FILE_NOPATH;

		String fieldJson = "{\n\t\"value\":\"15\",\n\t\"fieldUniqueName\":\""
				+ uuid.toString() + "bogus\",\n\t\"pathToFile\":\""
				+ testFileNameFQ + "\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotExecutionResult returnResult = formBotService
				.executeFormBotField(fieldJson);

		Assert.assertEquals(
				"executeFormBotField should have returned an error",
				FormBotExecutionEnum.ERROR, returnResult.getCode());
	}

	@Test
	public void executeFormBotFieldValidationFailed() throws Exception {
		String testDirName = "executeFormBotFieldValidationFailed";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection + '/' + TEST_FILE_NOPATH;

		String fieldJson = "{\n\t\"value\":\"bogus\",\n\t\"fieldUniqueName\":\""
				+ uuid.toString()
				+ "attribute2\",\n\t\"pathToFile\":\""
				+ testFileNameFQ + "\"}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotExecutionResult returnResult = formBotService
				.executeFormBotField(fieldJson);

		Assert.assertEquals(
				"validateFormBotField should have returned VALIDATION_FAILED, because value is not convertible to integer",
				FormBotExecutionEnum.VALIDATION_FAILED, returnResult.getCode());
	}

	@Test
	public void executeFormBotFieldValidationSuccess() throws Exception {
		String testDirName = "executeFormBotFieldValidationSuccess";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection + '/' + TEST_FILE_NOPATH;

		String fieldJson = "{\n\t\"value\":\"42\",\n\t\"fieldUniqueName\":\""
				+ uuid.toString() + "attribute2\",\n\t\"pathToFile\":\""
				+ testFileNameFQ + "\"}";
		;

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		FormBotExecutionResult returnResult = formBotService
				.executeFormBotField(fieldJson);

		Assert.assertEquals(
				"validateFormBotField should have returned SUCCESS",
				FormBotExecutionEnum.SUCCESS, returnResult.getCode());
	}

	@Test
	public void validateFormBotFormBadJson() throws Exception {
		String testDirName = "validateFormBotFormBadJson";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\"broken\" : \"WhereItLives\", \"fields\" : [ {\"fieldName\" : \"NameOfField\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"NameOfField\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"NameOfField\", \"value\" : \"MyValue\"}]}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		List<FormBotValidationResult> returnResult = formBotService
				.validateFormBotForm(fieldJson);

		Assert.assertEquals(
				"validateFormBotForm should have returned an error",
				FormBotValidationEnum.ERROR, returnResult.get(0).getCode());
	}

	@Test
	public void validateFormBotFormValidationFailed() throws Exception {
		String testDirName = "validateFormBotFormValidationFailed";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\"formUniqueName\" : \""
				+ uuid.toString()
				+ "\", \"fields\" : [ {\"fieldName\" : \"attribute1\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"attribute2\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"optional1\", \"value\" : \"MyValue\"}]}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		List<FormBotValidationResult> returnResult = formBotService
				.validateFormBotForm(fieldJson);

		Assert.assertEquals(
				"validateFormBotForm return list should have 4 elements", 4,
				returnResult.size());
		Assert.assertEquals(
				"validateFormBotForm overall return value should be FAILURE",
				FormBotValidationEnum.FAILURE, returnResult.get(0).getCode());
	}

	@Test
	public void validateFormBotFormValidationSuccess() throws Exception {
		String testDirName = "validateFormBotFormValidationSuccess";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\"formUniqueName\" : \""
				+ uuid.toString()
				+ "\", \"fields\" : [ {\"fieldName\" : \"attribute1\", \"value\" : \"hello\"}, {\"fieldName\" : \"attribute2\", \"value\" : \"42\"}, {\"fieldName\" : \"optional1\", \"value\" : \"MyValue\"}]}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		List<FormBotValidationResult> returnResult = formBotService
				.validateFormBotForm(fieldJson);

		Assert.assertEquals(
				"validateFormBotForm return list should have 4 elements", 4,
				returnResult.size());
		Assert.assertEquals(
				"validateFormBotForm overall return value should be FAILURE",
				FormBotValidationEnum.SUCCESS, returnResult.get(0).getCode());
	}

	@Test
	public void executeFormBotFormBadJson() throws Exception {
		String testDirName = "executeFormBotFormBadJson";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		String fieldJson = "{\"broken\" : \"WhereItLives\", \"pathToFile\" : \"path\\to\\file\", \"fields\" : [ {\"fieldName\" : \"NameOfField\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"NameOfField\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"NameOfField\", \"value\" : \"MyValue\"}]}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		List<FormBotExecutionResult> returnResult = formBotService
				.executeFormBotForm(fieldJson);

		Assert.assertEquals(
				"validateFormBotForm should have returned an error",
				FormBotExecutionEnum.ERROR, returnResult.get(0).getCode());
	}

	@Test
	public void executeFormBotFormValidationFailed() throws Exception {
		String testDirName = "executeFormBotFormValidationFailed";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection + '/' + TEST_FILE_NOPATH;

		String fieldJson = "{\"formUniqueName\" : \""
				+ uuid.toString()
				+ "\", \"pathToFile\" : \""
				+ testFileNameFQ
				+ "\", \"fields\" : [ {\"fieldName\" : \"attribute1\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"attribute2\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"optional1\", \"value\" : \"MyValue\"}]}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		List<FormBotExecutionResult> returnResult = formBotService
				.executeFormBotForm(fieldJson);

		Assert.assertEquals(
				"executeFormBotForm return list should have 4 elements", 4,
				returnResult.size());
		Assert.assertEquals(
				"validateFormBotForm overall return value should be VALIDATION_FAILED",
				FormBotExecutionEnum.VALIDATION_FAILED, returnResult.get(0).getCode());
	}

	@Test
	public void executeFormBotFormSuccess() throws Exception {
		String testDirName = "executeFormBotFormSuccess";

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

		JargonMetadataResolver resolver = new JargonMetadataResolver(
				irodsAccount, accessObjectFactory);

		String mdTemplatePath = resolver
				.findOrCreateMetadataTemplatesCollection(targetIrodsCollection);

		DataTransferOperations dataTransferOperations = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dataTransferOperations.putOperation(TEMPLATE_FILE_NAME1,
				mdTemplatePath, irodsAccount.getDefaultStorageResource(), null,
				null);

		String mdTemplateFqName = mdTemplatePath + '/' + TEMPLATE_NOPATH1;

		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance("test1", uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		accessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				mdTemplateFqName, avuData);

		// Create a file in targetIrodsCollection1
		dataTransferOperations.putOperation(TEST_FILE_NAME,
				targetIrodsCollection,
				irodsAccount.getDefaultStorageResource(), null, null);
		String testFileNameFQ = targetIrodsCollection + '/' + TEST_FILE_NOPATH;

		String fieldJson = "{\"formUniqueName\" : \""
				+ uuid.toString()
				+ "\", \"pathToFile\" : \""
				+ testFileNameFQ
				+ "\", \"fields\" : [ {\"fieldName\" : \"attribute1\", \"value\" : \"MyValue\"}, {\"fieldName\" : \"attribute2\", \"value\" : \"42\"}, {\"fieldName\" : \"optional1\", \"value\" : \"MyValue\"}]}";

		MetadataTemplateFormBotService formBotService = new MetadataTemplateFormBotService(
				accessObjectFactory, irodsAccount);

		List<FormBotExecutionResult> returnResult = formBotService
				.executeFormBotForm(fieldJson);

		Assert.assertEquals(
				"executeFormBotForm return list should have 4 elements", 4,
				returnResult.size());
		Assert.assertEquals(
				"executeFormBotForm overall return value should be SUCCESS",
				FormBotExecutionEnum.SUCCESS, returnResult.get(0).getCode());
	}

}
