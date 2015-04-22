package org.irods.jargon.metadatatemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.metadatatemplate.AbstractMetadataResolver;
import org.irods.jargon.metadatatemplate.FormBasedMetadataTemplate;
import org.irods.jargon.metadatatemplate.MetadataElement;
import org.irods.jargon.metadatatemplate.MetadataTemplate;
import org.irods.jargon.metadatatemplate.MetadataTemplateFileFilter;
import org.irods.jargon.metadatatemplate.TemplateParserSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JargonMetadataResolver extends AbstractMetadataResolver {
	static Logger log = LoggerFactory.getLogger(IRODSFileFactoryImpl.class);

	TemplateParserSingleton parser = TemplateParserSingleton.PARSER;

	private final IRODSAccount irodsAccount;
	private final DotIrodsService dotIrodsService;
	private final IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * Constructor for a JargonMetadataResolver. JargonMetadataResolver must be
	 * initialized with an irodsAccount on the relevant server.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @throws JargonException
	 */
	public JargonMetadataResolver(IRODSAccount irodsAccount,
			IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws JargonException {

		this.irodsAccount = irodsAccount;
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		dotIrodsService = new DotIrodsServiceImpl(irodsAccessObjectFactory,
				irodsAccount);
	}

	/**
	 * Return a list of MetadataTemplates for all "publicly available" metadata
	 * template files.</p>
	 * <p>
	 * Looks for public templates in the locations stored in the
	 * <code>publicTemplateLocations</code> variable. This variable must be
	 * initialized by the client by calling
	 * <code>setPublicTemplateLocations</code>.
	 * 
	 * @return List of {@link MetadataTemplate}
	 */
	@Override
	public List<MetadataTemplate> listPublicTemplates() {
		List<MetadataTemplate> tempList = new ArrayList<MetadataTemplate>();
		for (String dir : this.getPublicTemplateLocations()) {
			// TODO Auto-generated method stub
		}
		return null;
	}

	/**
	 * Return a list of MetadataTemplates found in the iRODS hierarchy of which
	 * the given path is the lowermost leaf.</p>
	 * <p>
	 * This list will only contain one MetadataTemplate for each template name.
	 * In the event that the hierarchy has multiple templates with the same
	 * name, the template nearest to the given directory will be returned. (That
	 * is, if the directory's parent and grandparent both contain a metadata
	 * template "specialMetadata", the template that is returned will be the one
	 * specified in the parent directory, and the template specified in the
	 * grandparent directory will not appear in the list.)
	 * 
	 * @param absolutePath
	 *            {@link String} containing a fully-qualified iRODS path
	 * @return List of {@link MetadataTemplate}
	 */
	@Override
	public List<MetadataTemplate> listTemplatesInIrodsHierarchyAbovePath(
			String absolutePath) throws IOException {
		log.info("listTemplatesInIrodsHierarchyAbovePath");
		List<MetadataTemplate> templateList = null;
		File[] templateFiles = {};

		try {
			templateFiles = dotIrodsService
					.listFilesOfTypeInDirectoryHierarchyDotIrods(absolutePath,
							new MetadataTemplateFileFilter());
		} catch (JargonException je) {
			log.info("JargonException when listing files in directory");
			return templateList;
		}

		try {
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException je) {
			log.info("JargonException when processing metadata template files");
			return templateList;
		}

		return templateList;
	}

	/**
	 * Returns a List of MetadataTemplates found in the home directory of a
	 * given user.
	 * 
	 * @param userName
	 * @return
	 * @throws JargonException
	 * @throws IOException
	 */
	private List<MetadataTemplate> listTemplatesInUserHome(String userName)
			throws JargonException {
		List<MetadataTemplate> templateList = null;
		File[] templateFiles = {};

		try {
			templateFiles = dotIrodsService.listFilesOfTypeInDotIrodsUserHome(
					userName, new MetadataTemplateFileFilter());
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException | IOException je) {
			log.error("JargonException when processing metadata template files");
			throw new JargonException("unable to listFiles in user home", je);
		}

		return templateList;
	}

	/**
	 * Save this metadata template to a JSON file for use later.
	 * 
	 * Here, the second argument <code>location</code>, is assumed to be a path
	 * to an irods collection. If it is a .irods collection, the file will be
	 * stored in that directory. Else, the metadata template will be stored in a
	 * .irods collection found or created under the given collection.
	 * 
	 * @param metadataTemplate
	 * @param location
	 *            a String containing an irods absolute path where a .irods
	 *            collection can be found or created.
	 */
	@Override
	public void saveTemplateAsJSON(MetadataTemplate metadataTemplate,
			String location) {
		log.info("saveTemplateAsJSON()");

		String dotIrodsLocation;

		if (location == null || location.isEmpty()) {
			throw new IllegalArgumentException("location is null or empty");
		}

		if (metadataTemplate == null) {
			throw new IllegalArgumentException("location is null or empty");
		}

		if (location.endsWith(".irods")) {
			log.info("location is already a .irods collection: {}", location);
			dotIrodsLocation = location;
		} else {
			boolean test = true;

			try {
				test = dotIrodsService
						.dotIrodsCollectionPresentInCollection(location);
			} catch (JargonException je) {
				log.error("JargonException when checking for .irods collection");
				log.error("Template NOT SAVED");
				je.printStackTrace();
				return;
			}

		}

		// TODO Auto-generated method stub

	}

	/**
	 * Return a MetadataTemplate given its name.</p>
	 * <p>
	 * If there are multiple templates for a given name, only one will be
	 * returned. The resolution scheme is first in activeDir, then nearest in
	 * the iRODS hierarchy above activeDir, then finally in the public template
	 * directories. In practice, this will first call
	 * <code>listTemplatesInIrodsHierarchyAbovePath</code>, and if no match is
	 * found in that list, <code>listPublicTemplates</code>.
	 * 
	 * @param name
	 *            {@link String} containing the template name to be searched for
	 * @param activeDir
	 *            {@link String} containing the active iRODS path
	 */
	@Override
	public MetadataTemplate findTemplateByName(String name, String activeDir)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return a MetadataTemplate given the fully-qualified path tot the template
	 * file.
	 * 
	 * @param fqName
	 *            {@link String} containing the iRODS path to a metadata
	 *            template file.
	 */
	@Override
	public MetadataTemplate findTemplateByFqName(String fqName)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Populate metadata template from a list of AVUs
	 * 
	 * XXX Are unaffiliated AVUs and orphan AVUs properly two separate lists?
	 * 
	 * @param inTemplate
	 *            {@link MetadataMergeResult}
	 * @param avuList
	 *            List of {@link AvuData}
	 * 
	 * @return {@link MetadataMergeResult}, containing a
	 *         {@link MetadataTemplate} that was initialized as a copy of
	 *         <code>inTemplate</code> whose <code>elements</code>'
	 *         <code>currentValue</code>s have been populated using
	 *         <code>avuList</code>, and a List of {@link AvuData} that contains
	 *         all avus that were not matched
	 * 
	 */
	public MetadataMergeResult populateFormBasedMetadataTemplateWithAVUs(
			FormBasedMetadataTemplate inTemplate,
			List<MetaDataAndDomainData> avuList) {
		FormBasedMetadataTemplate template = inTemplate.deepCopy();
		List<MetaDataAndDomainData> orphans = new ArrayList<MetaDataAndDomainData>();
		boolean matched = false;

		// Have bag of AVUs
		// Iterate over bag
		// Map of templates

		// Create map
		// List all required templates and put in map
		// Iterate over AVUs
		// Match with existing template in map
		// OR Add template to match
		// OR Add to "unmatched" template

		for (MetaDataAndDomainData avu : avuList) {
			matched = false;

			for (MetadataElement me : template.getElements()) {
				if (me.getName().equalsIgnoreCase(avu.getAvuAttribute())) {
					me.setCurrentValue(avu.getAvuValue());
					matched = true;
					break;
				}
			}

			if (!matched) {
				orphans.add(avu);
			}
		}

		return new MetadataMergeResult(template, orphans);
	}

	/**
	 * Rename a metadata template file.
	 * 
	 * @param fqName
	 *            {@link String}
	 * @param newFqName
	 *            {@link String}
	 */
	@Override
	public void renameTemplateByFqName(String fqName, String newFqName) {
		IRODSFile inFile = null;

		try {
			inFile = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(fqName);
		} catch (JargonException e) {
			log.error("JargonException when trying to create IRODSFile");
			log.error("File not renamed");
			e.printStackTrace();
			// TODO throw new IOException();
		}

		IRODSFile irodsRenameFile = null;

		try {
			irodsRenameFile = irodsAccessObjectFactory.getIRODSFileFactory(
					irodsAccount).instanceIRODSFile(newFqName);
		} catch (JargonException e) {
			// TODO Auto-generated catch block
			log.error("JargonException when trying to create IRODSFile");
			log.error("File not renamed");
			e.printStackTrace();
		}

		inFile.renameTo(irodsRenameFile);
		// TODO Check boolean
	}

	/**
	 * Save over a metadata template file given its fully-qualified iRODS
	 * path.</p>
	 * <p>
	 * This function contains a check to see if the UUID of the given file
	 * matches the UUID of the new template to be saved. If not, the update
	 * fails.
	 * 
	 * @param fqName
	 *            {@link String}
	 * @param mdTemplate
	 *            {@link MetadataTemplate}
	 */
	@Override
	public void updateTemplateByFqName(String fqName,
			MetadataTemplate mdTemplate) {
		// TODO Auto-generated method stub

	}

	/**
	 * Delete a metadata template give its fully-qualified iRODS path.
	 * 
	 * @param fqName
	 *            {@link String}
	 */
	@Override
	public void deleteTemplateByFqName(String fqName) {
		// TODO Auto-generated method stub
		IRODSFile inFile = null;

		try {
			inFile = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(fqName);
		} catch (JargonException e) {
			log.error("JargonException when trying to create IRODSFile");
			log.error("File not renamed");
			e.printStackTrace();
			// TODO throw new IOException();
		}

		inFile.delete();
		// TODO Check boolean
	}

	/**
	 * Return the fully-qualified path to a metadata template file given a UUID.
	 * 
	 * @param uuid
	 *            {@link UUID}
	 * @return String
	 */
	@Override
	public String getFqNameForUUID(UUID uuid) {
		log.info("getFqNameForUUID");

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = null;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryElement.AVUQueryPart.VALUE,
					AVUQueryOperatorEnum.EQUAL, uuid.toString()));

			queryResult = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount)
					.findMetadataValuesByMetadataQuery(queryElements);
		} catch (JargonQueryException jqe) {
			log.error("AvuQuery for UUID failed!");
			jqe.printStackTrace();
			return null;
		} catch (JargonException je) {
			log.error("JargonException in getFqNameForUUID");
			je.printStackTrace();
			return null;
		}

		if (queryResult.isEmpty()) {
			log.error("No match for specified UUID!");
			return null;
		}

		if (queryResult.size() > 1) {
			log.error(
					"{} matches for specified UUID! This should be impossible!",
					queryResult.size());
			log.info("Returning the fully-qualified name for only the first matched file.");
		}

		return queryResult.get(0).getDomainObjectUniqueName();
	}

	/**
	 * Parse a File object to a MetadataTemplate object by passing the contents
	 * of the file to
	 * <code>TemplateParserSingleton.createMetadataTemplateFromJSON()</p>
	 * <p>
	 * If the file does not already have a UUID associated with it, this
	 * function will generate one and add an AVU containing it to the file as a
	 * side effect.</p
	 * 
	 * @param inFile
	 *            {@link File}
	 * @return a <code>MetadataTemplate</code>
	 * @throws JargonException
	 * @throws IOException
	 */
	private MetadataTemplate processFileToMetadataTemplate(File inFile)
			throws JargonException, IOException {
		log.info("processFileToMetadataTemplate()");

		IRODSFileInputStream fis = null;
		byte[] b = null;

		fis = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFileInputStream((IRODSFileImpl) inFile);

		// If a template does not have a UUID assigned on opening, generate a
		// new one and apply it.
		//
		// By checking here, we already know that the File must exist due to
		// .instanceIRODSFileInputStream()
		//
		// By convention, a metadata template file must have an associated AVU
		// s.t.
		// Attribute = Template Name, Value = UUID, and unit = iRODS:MDTemplate
		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = null;

		log.info("AvuQuery to see if file has UUID already");

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryElement.AVUQueryPart.UNITS,
					AVUQueryOperatorEnum.EQUAL,
					JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT));

			queryResult = irodsAccessObjectFactory
					.getDataObjectAO(irodsAccount)
					.findMetadataValuesForDataObjectUsingAVUQuery(
							queryElements, inFile.getAbsolutePath());
		} catch (JargonQueryException e) {
			log.error("AvuQuery for UUID failed!");
			e.printStackTrace();
			throw new JargonException(e);
		}

		if (queryResult.isEmpty()) {
			log.info("MDTemplate AVU not found. Generating new one...");
			addMdTemplateAVUToFile(
					LocalFileUtils.getFileNameUpToExtension(inFile.getName()),
					inFile.getAbsolutePath());
		} else {
			log.info("MDTemplate AVU present. continuing...");
		}

		b = new byte[fis.available()];

		log.info("Size of file in bytes: {}", b.length);

		fis.read(b);

		String decoded = new String(b, "UTF-8");

		log.info("Decoded string rep of byte array:\n{}", decoded);

		return parser.createMetadataTemplateFromJSON(decoded);
	}

	/**
	 * Parse an array of File objects to a list of MetadataTemplate objects.
	 * Calls <code>processFileToMetadataTemplate</code> iteratively for each
	 * <code>File</code>.
	 * 
	 * @param inFileArray
	 *            an array of File objects
	 * @return returnList, an ArrayList of metadataTemplates, in the same order
	 *         they appeared in inFileArray
	 * @throws JargonException
	 * @throws IOException
	 */
	private List<MetadataTemplate> processFilesToMetadataTemplates(
			File[] inFileArray) throws JargonException, IOException {
		log.info("processFilesToMetadataTemplates()");
		List<MetadataTemplate> returnList = new ArrayList<MetadataTemplate>();
		for (File f : inFileArray)
			returnList.add(processFileToMetadataTemplate(f).deepCopy());

		return returnList;
	}

	/**
	 * Adds an AVU to a metadata template file denoting the template itself. The
	 * AVU is of the format:
	 * <ul>
	 * <li>Attribute: Name of template</li>
	 * <li>Value: UUID associated with template</li>
	 * <li>Unit: iRODS:mdTemplate</li>
	 * </ul>
	 * 
	 * @param name
	 * @param path
	 * @throws JargonException
	 */
	private void addMdTemplateAVUToFile(String name, String path)
			throws JargonException {
		log.info("addMdTemplateAVUToFile, name = {}", name);
		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance(name, uuid.toString(),
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		irodsAccessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				path, avuData);
	}

	/**
	 * Adds an AVU to a metadata template file denoting an element that appears
	 * in that template. The AVU is of the format:
	 * <ul>
	 * <li>Attribute: Name of element</li>
	 * <li>Value: UUID associated with element</li>
	 * <li>Unit: iRODS:mdElement</li>
	 * </ul>
	 * 
	 * @param name
	 * @param path
	 * @throws JargonException
	 */
	private void addMdElementAVUToFile(String name, String path)
			throws JargonException {
		log.info("addMdElementAVUToFile, name = {}", name);
		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance(name, uuid.toString(),
				JargonMetadataTemplateConstants.MD_ELEMENT_UNIT);
		irodsAccessObjectFactory.getDataObjectAO(irodsAccount).addAVUMetadata(
				path, avuData);
	}

	// public static String computePublicDirectory(final IRODSAccount
	// irodsAccount) {
}
