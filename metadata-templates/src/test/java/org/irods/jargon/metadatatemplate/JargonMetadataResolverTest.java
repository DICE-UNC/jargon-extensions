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
		// Create M public template dirs
		// Create N template files in those dirs
		// Add dirs to public template locations via setPublicTemplateLocations
		// Call listPublicTemplates
		// Assert that list has N elements
	}

	@Test
	public void listPublicTemplatesDuplicates() throws Exception {
		// Create M public template dirs
		// Create N template files in those dirs, where two of them have the
		// same name
		// Add dirs to public template locations via setPublicTemplateLocations
		// Call listPublicTemplates
		// Assert that list has N-1 elements, and that the fqName of the
		// duplicated name is the first one in the list
	}

	@Test
	public void listAllTemplatesNoDuplicates() throws Exception {
		// Create M directories making a directory hierarchy
		// Create N template files in those dirs
		// Call listTemplatesInDirectoryHierarchyAbovePath with the leaf dir in
		// the hierarchy
		// Assert that list has N elements
	}

	@Test
	public void listAllTemplatesDuplicates() throws Exception {
		// Create M directories making a directory hierarchy
		// Create N template files in those dirs, where two of them have the
		// same name
		// Call listTemplatesInDirectoryHierarchyAbovePath with the leaf dir in
		// the hierarchy
		// Assert that list has N-1 elements, and that the fqName of the
		// duplicated name is the lowest one in the hierarchy
	}

	@Test
	public void findTemplateByNameSingleMatch() throws Exception {
		// Create K public directories
		// Create M directories in a hierarchy
		// Create N template files across those directories, with no name
		// conflicts
		// Call setPublicTemplateLocations
		// Call findTemplateByName with the leaf dir in the hierarchy
		// Assert that the returned template is non-null, and the fqName is the
		// appropriate one
	}

	@Test
	public void findTemplateByNameDuplicateMatch() throws Exception {
		// Create K public directories
		// Create M directories in a hierarchy
		// Create N template files across those directories, with a name
		// conflict
		// Call setPublicTemplateLocations
		// Call findTemplateByName with the leaf dir in the hierarchy
		// Assert that the returned template is non-null, and it is the lowest
		// one in the directory hierarchy
	}

	@Test
	public void findTemplateByNameNoMatch() throws Exception {
		// Create K public directories
		// Create M directories in a hierarchy
		// Create N template files across those directories, with a name
		// conflict
		// Call setPublicTemplateLocations
		// Call findTemplateByName with the leaf dir in the hierarchy
		// Assert that the return value is null
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
	}

	@Test
	public void findTemplateByFqNameValid() throws Exception {
		// Create a template file in a directory
		// Call findTemplateByFqName with the fully-qualified name of the
		// template file
		// Assert that the returned template is non-null
	}

	@Test
	public void findTemplateByFqNameInvalid() throws Exception {
		// Call findTemplateByFqName with the fully-qualified name of a file
		// that doesn't exist
		// Assert that the returned template is null
	}

	@Test
	public void getFqNameForUuidValid() throws Exception {
		// Create a template file in a directory
		// Call findTemplateByFqName with the correct UUID of that file
		// Assert that the returned path is correct
	}

	@Test
	public void getFqNameForUuidInvalid() throws Exception {
		// Create a template file in a directory
		// Call findTemplateByFqName with a bogus UUID
		// Assert that the returned value is null
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
	}

	@Test
	public void saveFormBasedTemplateAsJsonGivenParentDir() throws Exception {
		// Create an instantiated FormBasedMetadataTemplate
		// Create a directory without a .irods subdirectory
		// Call saveFormBased... with the instantiated object and the full path
		// Assert that a file of that name and size exists at
		// path/.irods/mdtemplates
	}

	@Test
	public void saveFormBasedTemplateAsJsonCheckAVUs() throws Exception {
		// Create an instantiated FormBasedMetadataTemplate
		// Create a directory, as well as its .irods and mdtemplates
		// subdirectories
		// Call saveFormBased... with the instantiated object and the full path
		// Assert that the AVUs on the file saved at the specified location
		// include the correct mdtemplate and mdelement AVUs
	}

	@Test
	public void renameTemplateByFqNameValid() throws Exception {
		// Create at least two directories
		// Create a template in one of the directories
		// call renameTemplate... to move it to the other one
		// Assert that the newly renamed template is in the other directory
	}

	@Test
	public void renameTemplateByFqNameInvalid() throws Exception {
		// Create at least two directories
		// Create a template in one of the directories
		// call renameTemplate... with a bogus path
		// Assert that the template is unchanged in the original directory
	}

	@Test
	public void updateFormBasedTemplateValid() throws Exception {
		// Create a directory
		// Create a template file in that directory (with correct AVUs and UUID)
		// Import the template file (using findTemplateByFqName)
		// Modify the file
		// Save it back using updateFormBased...
		// Assert that the file has been modified
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
	}

	@Test
	public void deleteTemplateByFqNameValid() throws Exception {
		// Create a directory
		// Create a template file in that directory (with correct AVUs and UUID)
		// Attempt to delete it using deleteTemplate...
		// Assert that the file is no longer present at that location
	}

	@Test
	public void deleteTemplateByFqNameNotATemplateFile() throws Exception {
		// Create a directory
		// Create a NON-METADATA-TEMPLATE file in that directory
		// Attempt to delete it using deleteTemplate...
		// Assert that the file is still present and unmodified
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
