package org.irods.jargon.metadatatemplate;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.After;
import org.junit.Assert;
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
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@After
	public void tearDown() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testSaveMetadataTemplateAsJson() throws Exception {

		String testDirName = "testSaveMetadataTemplateAsJson";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		MetadataTemplate template = new MetadataTemplate();
		template.setAuthor("me");
		template.setDescription("descr");
		template.setName(testDirName);
		template.setRequired(true);
		template.setSource(SourceEnum.USER);
		template.setVersion("0.0.1");

		MetadataTemplateContext context = new MetadataTemplateContext();
		context.setIrodsAccount(irodsAccount);
		MetadataTemplateConfiguration metadataConfiguration = new MetadataTemplateConfiguration();

		JargonMetadataResolver resolver = new JargonMetadataResolver(context, accessObjectFactory,
				metadataConfiguration);

		UUID uuid = resolver.saveTemplate(template, MetadataTemplateLocationTypeEnum.USER);
		MetadataTemplate metadataTemplate = resolver.findTemplateByUUID(uuid.toString());
		Assert.assertNotNull("no metadata template stored", metadataTemplate);

	}

	@Test
	public void testSaveMetadataTemplateAsJsonInPublic() throws Exception {

		String testDirName = "testSaveMetadataTemplateAsJsonInPublic";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		// put scratch file into irods in the right place for public

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testDirName);
		IRODSFile pubCollection = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		pubCollection.delete();
		pubCollection.mkdirs();

		MetadataTemplate template = new MetadataTemplate();
		template.setAuthor("me");
		template.setDescription("descr");
		template.setName(testDirName);
		template.setRequired(true);
		template.setSource(SourceEnum.USER);
		template.setVersion("0.0.1");

		MetadataTemplateContext context = new MetadataTemplateContext();

		context.setIrodsAccount(irodsAccount);
		MetadataTemplateConfiguration metadataConfiguration = new MetadataTemplateConfiguration();
		metadataConfiguration.setPublicTemplateIdentifier(targetIrodsCollection);

		JargonMetadataResolver resolver = new JargonMetadataResolver(context, accessObjectFactory,
				metadataConfiguration);

		UUID uuid = resolver.saveTemplate(template, MetadataTemplateLocationTypeEnum.PUBLIC);
		MetadataTemplate metadataTemplate = resolver.findTemplateByUUID(uuid.toString());
		Assert.assertNotNull("no metadata template stored", metadataTemplate);

	}

	@Test
	public void testListPublic() throws Exception {

		String testDirName = "testListPublic";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		// put scratch file into irods in the right place for public

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testDirName);
		IRODSFile pubCollection = accessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsCollection);
		pubCollection.delete();
		pubCollection.mkdirs();

		MetadataTemplate template = new MetadataTemplate();
		template.setAuthor("me");
		template.setDescription("descr");
		template.setName(testDirName);
		template.setRequired(true);
		template.setSource(SourceEnum.USER);
		template.setVersion("0.0.1");

		MetadataTemplateContext context = new MetadataTemplateContext();

		context.setIrodsAccount(irodsAccount);
		MetadataTemplateConfiguration metadataConfiguration = new MetadataTemplateConfiguration();
		metadataConfiguration.setPublicTemplateIdentifier(targetIrodsCollection);

		JargonMetadataResolver resolver = new JargonMetadataResolver(context, accessObjectFactory,
				metadataConfiguration);
		UUID uuid = resolver.saveTemplate(template, MetadataTemplateLocationTypeEnum.PUBLIC);

		List<MetadataTemplate> metadataTemplates = resolver.listPublicTemplates();

		Assert.assertFalse("no templates returned", metadataTemplates.isEmpty());

	}

}
