package org.irods.jargon.metadatatemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.extensions.dotirods.DotIrodsConstants;
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
	 * <code>setPublicTemplateLocations</code> and/or
	 * <code>appendToPublicTemplateLocations</code>.
	 * </p>
	 * <p>
	 * By convention, the locations in publicTemplateLocations are the actual
	 * directories in which the template files can be found. In other words,
	 * this function will not look into /.irods or /.irods/metadataTemplates
	 * subfolders to find template files.
	 * </p>
	 * <p>
	 * WARNING: If the same template name appears in multiple public template
	 * directories, which one is returned will be determined by the list order
	 * of publicTemplateLocations.
	 * 
	 * @return List of {@link MetadataTemplate}
	 */
	@Override
	public List<MetadataTemplate> listPublicTemplates() {
		List<MetadataTemplate> tempList = new ArrayList<MetadataTemplate>();

		for (String dir : this.getPublicTemplateLocations()) {
			try {
				tempList.addAll(listTemplatesInCollection(dir));
			} catch (JargonException je) {
				log.error("JargonException when processing templates in {}",
						dir, je);
			} catch (IOException ie) {
				log.error("IOException when processing templates in {}", dir,
						ie);
			}
		}

		return tempList;
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
			log.error("JargonException when listing files in directory", je);
			return templateList;
		}

		try {
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException je) {
			log.error(
					"JargonException when processing metadata template files",
					je);
			return templateList;
		}

		return templateList;
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
	 * </p>
	 * <p>
	 * If no match is found, returns <code>null</code>.
	 * </p>
	 * <p>
	 * WARNING: If multiple public templates have the same name, results may be
	 * unexpected. See the documentation for @link{listPublicTemplates} for more
	 * information.
	 * 
	 * @param name
	 *            {@link String} containing the template name to be searched for
	 * @param activeDir
	 *            {@link String} containing the active iRODS path
	 * 
	 * @return Nearest metadata template matching the name, or <code>null</code>
	 *         if no match
	 */
	@Override
	public MetadataTemplate findTemplateByName(String name, String activeDir)
			throws FileNotFoundException, IOException,
			MetadataTemplateProcessingException,
			MetadataTemplateParsingException {
		log.info("findTemplateByName()");

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("name is null or empty");
		}

		if (activeDir == null || activeDir.isEmpty()) {
			throw new IllegalArgumentException("activeDir is null or empty");
		}

		MetadataTemplate returnTemplate = null;
		File[] templateFilesInHierarchy = new File[0];
		boolean matched = false;

		try {
			templateFilesInHierarchy = dotIrodsService
					.listFilesOfTypeInDirectoryHierarchyDotIrods(activeDir,
							new MetadataTemplateFileFilter());
		} catch (JargonException je) {
			log.error("JargonException when listing files in directory", je);
			log.info("Error getting file list, not searching in hierarchy");
		}

		if (templateFilesInHierarchy.length > 0) {
			for (File f : templateFilesInHierarchy) {
				String nameFromFilename;

				if (f.getName().split(".").length > 0) {
					nameFromFilename = f.getName().split(".")[0];
				} else {
					nameFromFilename = f.getName();
				}

				if (nameFromFilename.equalsIgnoreCase(name)) {
					log.info("Name matched: {}", f.getAbsolutePath());

					try {
						returnTemplate = this.processFileToMetadataTemplate(f);
						matched = true;
						break;
					} catch (JargonException je) {
						log.error(
								"JargonException in processFileToMetadataTemplate",
								je);
						log.info(
								"Matched {} with {}, but file could not be processed",
								name, f.getAbsolutePath());
					}
				}
			}
		}

		if (!matched) {
			log.info("No match in directory hierarchy, trying public locations");

			for (String publicDir : this.getPublicTemplateLocations()) {
				IRODSFile collectionIrodsFile = null;

				try {
					collectionIrodsFile = irodsAccessObjectFactory
							.getIRODSFileFactory(irodsAccount)
							.instanceIRODSFile(publicDir);
				} catch (JargonException je) {
					log.error("JargonException when opening {} as IRODSFile",
							publicDir, je);
					log.info("Could not open {}, skipping to next public dir",
							publicDir);
					continue;
				}

				for (File f : collectionIrodsFile
						.listFiles(new MetadataTemplateFileFilter())) {
					String nameFromFilename;

					if (f.getName().split(".").length > 0) {
						nameFromFilename = f.getName().split(".")[0];
					} else {
						nameFromFilename = f.getName();
					}

					if (nameFromFilename.equalsIgnoreCase(name)) {
						log.info("Name matched: {}", f.getAbsolutePath());

						try {
							returnTemplate = this
									.processFileToMetadataTemplate(f);
							matched = true;
							break;
						} catch (JargonException je) {
							log.error(
									"JargonException in processFileToMetadataTemplate",
									je);
							log.info(
									"Matched {} with {}, but file could not be processed",
									name, f.getAbsolutePath());
						}
					}
				}
			}
		}

		if (!matched) {
			log.info("No match found for name {}, returning null", name);
		}

		return returnTemplate;
	}

	/**
	 * Return a MetadataTemplate given the fully-qualified path to the template
	 * file.
	 * 
	 * @param fqName
	 *            {@link String} containing the iRODS path to a metadata
	 *            template file.
	 */
	@Override
	public MetadataTemplate findTemplateByFqName(String fqName)
			throws FileNotFoundException, IOException,
			MetadataTemplateProcessingException,
			MetadataTemplateParsingException {
		log.info("findTemplateByFqName()");

		MetadataTemplate returnTemplate = null;

		if (fqName == null || fqName.isEmpty()) {
			throw new IllegalArgumentException("fqName is null or empty");
		}

		IRODSFile templateFile = this.getPathAsIrodsFile(fqName);

		try {
			returnTemplate = this.processFileToMetadataTemplate(templateFile
					.getAbsoluteFile());
		} catch (JargonException je) {
			log.error("JargonException in processFileToMetadataTemplate", je);
			throw new MetadataTemplateProcessingException(
					"Error in processFileToMetadataTemplate for " + fqName);
		}

		return returnTemplate;
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
		log.info("getFqNameForUUID()");

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
			log.error("AvuQuery for UUID failed!", jqe);
			return null;
		} catch (JargonException je) {
			log.error("JargonException in getFqNameForUUID", je);
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

	/*
	 * /** Returns a List of MetadataTemplates found in the home directory of a
	 * given user.
	 * 
	 * @param userName
	 * 
	 * @return
	 * 
	 * @throws JargonException
	 * 
	 * @throws IOException
	 * 
	 * private List<MetadataTemplate> listTemplatesInUserHome(String userName)
	 * throws JargonException { List<MetadataTemplate> templateList = null;
	 * File[] templateFiles = {};
	 * 
	 * try { templateFiles = dotIrodsService.listFilesOfTypeInDotIrodsUserHome(
	 * userName, new MetadataTemplateFileFilter()); templateList =
	 * processFilesToMetadataTemplates(templateFiles); } catch (JargonException
	 * | IOException je) {
	 * log.error("JargonException when processing metadata template files");
	 * throw new JargonException("unable to listFiles in user home", je); }
	 * 
	 * return templateList; }
	 */
	/**
	 * Save this metadata template to a JSON file for use later.
	 * 
	 * Here, the second argument
	 * <code>irodsAbsolutePathToParentCollection</code>, is assumed to be a path
	 * to a .irods/metadataTemplates collection. If it is, the file will be
	 * stored in that directory. If it is not, this function will attempt to
	 * create a /metadataTemplates collection (if location is a .irods
	 * collection) or a /.irods/metadataTemplates collection underneath this
	 * parent collection, and if successful, the template will be stored in that
	 * newly created collection.
	 * 
	 * @param metadataTemplate
	 * @param irodsAbsolutePathToParentCollection
	 *            a String containing an irods absolute path where a
	 *            .irods/metadataTemplates collection can be found or created.
	 * 
	 * @return a String containing the absolute path to the saved file, or null
	 *         if save failed
	 */
	@Override
	public String saveFormBasedTemplateAsJSON(
			FormBasedMetadataTemplate metadataTemplate,
			String irodsAbsolutePathToParentCollection)
			throws FileNotFoundException, IOException,
			MetadataTemplateProcessingException {
		log.info("saveFormBasedTemplateAsJSON()");

		String metadataTemplatesLocation = null;

		if (irodsAbsolutePathToParentCollection == null
				|| irodsAbsolutePathToParentCollection.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToParentCollection is null or empty");
		}

		if (metadataTemplate == null) {
			throw new IllegalArgumentException(
					"metadataTemplate is null or empty");
		}

		try {
			metadataTemplatesLocation = this
					.findOrCreateMetadataTemplatesCollection(irodsAbsolutePathToParentCollection);
		} catch (JargonException je) {
			log.error(
					"JargonException in findOrCreateMetadataTemplatesCollection",
					je);
			log.info("Save directory could not be found or created, file not saved.");
			return null;
		}

		String jsonString = parser
				.createJSONFromMetadataTemplate(metadataTemplate);

		log.info("json string created");

		String fileName = metadataTemplate.getName()
				+ MetadataTemplateConstants.TEMPLATE_FILE_EXT;
		String absolutePath = metadataTemplatesLocation + '/' + fileName;

		log.info("Saving to: {}", absolutePath);

		try {
			this.saveJSONStringToFile(jsonString, absolutePath);
		} catch (JargonException je) {
			log.error("JargonException when trying to write String to file");
			log.info("MetadataTemplate not saved to file");
			return null;
		}

		try {
			this.addMdTemplateAVUToFile(metadataTemplate.getName(),
					absolutePath);
		} catch (JargonException je) {
			log.info("Could not add template {} AVU to file",
					metadataTemplate.getName());
		}

		for (MetadataElement me : metadataTemplate.getElements()) {
			try {
				this.addMdElementAVUToFile(me.getName(), absolutePath);
			} catch (JargonException je) {
				log.info("Could not add element {} AVU to file", me.getName());
			}
		}

		log.info("saved");

		return absolutePath;
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
	public boolean renameTemplateByFqName(String fqName, String newFqName) {
		log.info("renameTemplateByFqName()");

		if (fqName == null || fqName.isEmpty()) {
			throw new IllegalArgumentException("fqName is null or empty");
		}

		if (newFqName == null || newFqName.isEmpty()) {
			throw new IllegalArgumentException("newFqName is null or empty");
		}

		IRODSFile inFile = this.getPathAsIrodsFile(fqName);

		if (inFile == null) {
			log.error("{} could not be retrieved as IRODSFile; rename failed",
					fqName);
			throw new IllegalArgumentException(
					"fqName could not be retrieved as IRODSFile");
		}

		IRODSFile irodsRenameFile = this.getPathAsIrodsFile(newFqName);

		if (irodsRenameFile == null) {
			log.error("{} could not be created as IRODSFile; rename failed",
					newFqName);
			throw new IllegalArgumentException(
					"newFqName could not be retrieved as IRODSFile");
		}

		// getPathAsIrodsFile returns null if .instanceIRODSFile fails.
		// This is checked for in renameTo, and will result in a
		// JargonRuntimeException.
		// TODO Should the above checks be removed?

		return inFile.renameTo(irodsRenameFile);
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
	 * 
	 * @return a boolean indicating whether the update succeeded (true) or
	 *         failed
	 */
	@Override
	public boolean updateFormBasedTemplateByFqName(String fqName,
			FormBasedMetadataTemplate metadataTemplate) throws IOException {
		log.info("updateTemplateByFqName()");

		if (fqName == null || fqName.isEmpty()) {
			throw new IllegalArgumentException("fqName is null or empty");
		}

		if (!fqName.endsWith(MetadataTemplateConstants.JSON_FILE_EXT)
				|| !fqName
						.endsWith(MetadataTemplateConstants.TEMPLATE_FILE_EXT)) {
			throw new IllegalArgumentException(
					"fqName does not represent a metadata template file, update not attempted");
		}

		if (metadataTemplate == null) {
			throw new IllegalArgumentException(
					"metadataTemplate is null or empty");
		}

		IRODSFile inFile = this.getPathAsIrodsFile(fqName);

		if (inFile == null) {
			log.error("{} could not be retrieved as IRODSFile; update failed",
					fqName);
			throw new IllegalArgumentException(
					"fqName could not be retrieved as IRODSFile");
		}

		List<MetaDataAndDomainData> queryResult = null;

		try {
			queryResult = this.queryTemplateAVUForFile(fqName);
		} catch (JargonQueryException jqe) {
			log.error("AvuQuery for UUID failed!", jqe);
			log.info("Could not retrieve file UUID, not attempting to save file");
			return false;
		} catch (JargonException je) {
			log.error("JargonException in queryTemplateAVUForFile()", je);
			log.info("JargonException when querying UUID, not attempting to save file");
			return false;
		}

		UUID fileUUID = UUID.fromString(queryResult.get(0).getAvuValue());
		if (fileUUID.compareTo(metadataTemplate.getUuid()) != 0) {
			log.info("UUID in file metadata is not the same as UUID in MetadataTemplate object");
			log.info("updateFormBased... should ONLY be used when modifying an existing template");
			log.info("Template not saved");
			return false;
		}

		String jsonString = null;
		try {
			jsonString = parser
					.createJSONFromMetadataTemplate(metadataTemplate);
		} catch (JargonException je) {
			log.error("JargonException in createJSONFromMetadataTemplate()", je);
			log.info("JargonException when writing MetadataTemplate to JSON, unable to save");
			return false;
		}

		log.info("json string created");

		log.info("Saving to: {}", fqName);
		try {
			this.saveJSONStringToFile(jsonString, fqName);
		} catch (JargonException je) {
			log.error("JargonException when trying to write String to file");
			log.info("Template file not updated");
			return false;
		}

		return true;
	}

	/**
	 * Delete a metadata template give its fully-qualified iRODS path.
	 * 
	 * @param fqName
	 *            {@link String}
	 */
	@Override
	public boolean deleteTemplateByFqName(String fqName) {
		log.info("deleteTemplateByFqName()");

		if (fqName == null || fqName.isEmpty()) {
			throw new IllegalArgumentException("fqName is null or empty");
		}

		if (!fqName.endsWith(MetadataTemplateConstants.JSON_FILE_EXT)
				|| !fqName
						.endsWith(MetadataTemplateConstants.TEMPLATE_FILE_EXT)) {
			throw new IllegalArgumentException(
					"fqName does not represent a metadata template file, delete not attempted");
		}

		IRODSFile inFile = null;

		try {
			inFile = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(fqName);
		} catch (JargonException e) {
			log.error("JargonException when trying to create IRODSFile for {}",
					fqName, e);
			log.error("Delete not performed.");

			return false;
		}

		return inFile.delete();
	}

	/**
	 * Populate metadata template from a list of AVUs
	 * 
	 * TODO Are unaffiliated AVUs and orphan AVUs properly two separate lists?
	 * 
	 * @param irodsAbsolutePathToFile
	 * 
	 * @return {@link MetadataMergeResult}, containing a
	 *         {@link MetadataTemplate} that was initialized as a copy of
	 *         <code>inTemplate</code> whose <code>elements</code>'
	 *         <code>currentValue</code>s have been populated using
	 *         <code>avuList</code>, and a List of {@link AvuData} that contains
	 *         all avus that were not matched
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws org.irods.jargon.core.exception.FileNotFoundException 
	 * @throws JargonException 
	 * 
	 */
	public MetadataMergeResult getAndMergeTemplateListForFile(
			String irodsAbsolutePathToFile) throws FileNotFoundException, IOException, JargonException {
		log.info("getAndMergeTemplateListForFile()");

		List<MetaDataAndDomainData> orphans = new ArrayList<MetaDataAndDomainData>();
		Map<String, FormBasedMetadataTemplate> templateMap = new HashMap<String, FormBasedMetadataTemplate>();

		for (MetadataTemplate mt : this
				.listAllRequiredTemplates(irodsAbsolutePathToFile)) {
			log.info("Required template found: {}", mt.getName());
			// TODO Right now, only supports searching by UUID
			// nameUUID would be more general
			// i.e. dublinCore01234567-01234-01234-01234-0123456789ab
			String hashKey = mt.getUuid().toString();
			// TODO Need to address different kinds of templates
			templateMap.put(hashKey, (FormBasedMetadataTemplate) mt);
		}

		List<MetaDataAndDomainData> avuList = irodsAccessObjectFactory
				.getDataObjectAO(irodsAccount).findMetadataValuesForDataObject(
						irodsAbsolutePathToFile);

		boolean matched = false;
		FormBasedMetadataTemplate tempMt = null;
		int unitIndex = 0;
		int uuidStart = 0;

		for (MetaDataAndDomainData avu : avuList) {
			if (avu.getAvuAttribute().contains(
					JargonMetadataTemplateConstants.AVU_UNIT_PREFIX)) {
				log.info("unit contains a template UUID");
				unitIndex = avu.getAvuAttribute().indexOf(
						JargonMetadataTemplateConstants.AVU_UNIT_PREFIX);
				uuidStart = unitIndex
						+ JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
								.length();

				// 36 because UUID string is always 36 characters long
				// The unit string might contain, e.g.,
				// "fromTemplate:01234567-01234-01234-01234-0123456789ab"
				String uuid = avu.getAvuAttribute().substring(uuidStart,
						uuidStart + 36);

				// See if the template is already in the hash map
				if (templateMap.containsKey(uuid)) {
					log.info("avu belongs to a template already in the map");
					tempMt = templateMap.get(uuid);

					for (MetadataElement me : tempMt.getElements()) {
						if (avu.getAvuAttribute()
								.equalsIgnoreCase(me.getName())) {
							me.setCurrentValue(avu.getAvuValue());
							matched = true;
							break;
						}
					}

					if (!matched) {
						log.info(
								"AVU claims to be from template {}, but name not matched",
								uuid);
						log.info(
								"AVU: {}",
								avu.getAvuAttribute() + ", "
										+ avu.getAvuValue() + ", "
										+ avu.getAvuUnit());
					}
				} else {
					log.info("avu belongs to a template not in the map");
					// XXX FormBasedMetadataTemplate typecast
					tempMt = (FormBasedMetadataTemplate) this
							.findTemplateByUUID(uuid);
					if (tempMt == null) {
						log.info("no template found for UUID {}", uuid);
					} else {
						for (MetadataElement me : tempMt.getElements()) {
							if (avu.getAvuAttribute().equalsIgnoreCase(
									me.getName())) {
								me.setCurrentValue(avu.getAvuValue());
								matched = true;
								break;
							}
						}
					}

					if (!matched) {
						log.info(
								"AVU claims to be from template {}, but name not matched",
								uuid);
						log.info(
								"AVU: {}",
								avu.getAvuAttribute() + ", "
										+ avu.getAvuValue() + ", "
										+ avu.getAvuUnit());
					} else {
						log.info("AVU matched with new template {}", uuid);
						log.info("Adding template to map");
						templateMap.put(uuid, tempMt);
					}
				}
			}

			if (!matched) {
				log.info("AVU not matched with any template, adding to orphans list");
				orphans.add(avu);
			}

			matched = false;
			tempMt = null;
			unitIndex = 0;
			uuidStart = 0;
		}

		List<MetadataTemplate> returnList = new ArrayList<MetadataTemplate>();
		returnList.addAll(templateMap.values());

		return new MetadataMergeResult(returnList, orphans);
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
			File[] inFileArray) throws JargonException, IOException,
			MetadataTemplateProcessingException,
			MetadataTemplateParsingException {
		log.info("processFilesToMetadataTemplates()");

		List<MetadataTemplate> returnList = new ArrayList<MetadataTemplate>();
		for (File f : inFileArray)
			returnList.add(processFileToMetadataTemplate(f).deepCopy());

		return returnList;
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
			throws JargonException, IOException,
			MetadataTemplateProcessingException,
			MetadataTemplateParsingException {
		log.info("processFileToMetadataTemplate()");

		FormBasedMetadataTemplate returnTemplate = null;
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
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		log.info("AvuQuery to see if file has UUID already");

		try {
			queryResult = this
					.queryTemplateAVUForFile(inFile.getAbsolutePath());
		} catch (JargonQueryException jqe) {
			log.error("AvuQuery for UUID failed!", jqe);
		}

		if (queryResult.isEmpty()) {
			log.info("MDTemplate AVU not found. Generating new one...");
			addMdTemplateAVUToFile(
					LocalFileUtils.getFileNameUpToExtension(inFile.getName()),
					inFile.getAbsolutePath());

			try {
				queryResult = this.queryTemplateAVUForFile(inFile
						.getAbsolutePath());
			} catch (JargonQueryException jqe) {
				log.error("AvuQuery for UUID failed!", jqe);
			}
		} else {
			log.info("MDTemplate AVU present. continuing...");
		}

		b = new byte[fis.available()];

		log.info("Size of file in bytes: {}", b.length);

		fis.read(b);

		String decoded = new String(b, "UTF-8");

		log.info("Decoded string rep of byte array:\n{}", decoded);

		returnTemplate = parser.createMetadataTemplateFromJSON(decoded);

		// make sure UUID is populated correctly
		// "Canonical" UUID is in AVU, not in json text
		returnTemplate.setUuid(UUID
				.fromString(queryResult.get(0).getAvuValue()));

		return parser.createMetadataTemplateFromJSON(decoded);
	}

	/**
	 * 
	 */
	private List<MetadataTemplate> listTemplatesInCollection(
			String irodsAbsolutePathToCollection) throws IOException,
			JargonException, MetadataTemplateProcessingException,
			MetadataTemplateParsingException {
		log.info("listTemplatesInCollection()");

		if (irodsAbsolutePathToCollection == null
				|| irodsAbsolutePathToCollection.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToCollection");
		}

		log.info("irodsAbsolutePathToCollection: {}",
				irodsAbsolutePathToCollection);

		IRODSFile collectionIrodsFile = null;

		collectionIrodsFile = irodsAccessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(irodsAbsolutePathToCollection);

		return processFilesToMetadataTemplates(collectionIrodsFile
				.listFiles(new MetadataTemplateFileFilter()));
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

	private String findOrCreateMetadataTemplatesCollection(
			final String irodsAbsolutePathToParent) throws JargonException {
		log.info("findOrCreateMetadataTemplatesCollection()");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToParent");
		}

		log.info("irodsAbsolutePathToParent: {}", irodsAbsolutePathToParent);

		String irodsAbsolutePathToCollection = null;

		if (this.isMetadataTemplatesCollection(irodsAbsolutePathToParent)) {
			// Already a .irods/metadataTemplates collection, so we're good
			log.info("{} is a .irods/metadataTemplates collection",
					irodsAbsolutePathToParent);
			irodsAbsolutePathToCollection = irodsAbsolutePathToParent;
		} else if (this.isDotIrodsCollection(irodsAbsolutePathToParent)) {
			// Parameter is a .irods collection, need to find or create a
			// metadataTemplates collection beneath it.
			log.info("{} is a .irods collection", irodsAbsolutePathToParent);

			if (this.isMetadataTemplatesCollectionPresentInCollection(irodsAbsolutePathToParent)) {
				log.info("{} contains a metadataTemplates collection",
						irodsAbsolutePathToParent);
				irodsAbsolutePathToCollection = this
						.computeMetadataTemplatesPathUnderParent(irodsAbsolutePathToParent);
			} else {
				log.info(
						"{} does not contain a metadataTemplates collection, attempting to create",
						irodsAbsolutePathToParent);

				if (this.createMetadataTemplatesCollectionUnderParent(irodsAbsolutePathToParent)) {
					log.info("metadataTemplates collection created");
					irodsAbsolutePathToCollection = this
							.computeMetadataTemplatesPathUnderParent(irodsAbsolutePathToParent);
				} else {
					log.error("Error, collection not created");
					throw new JargonException(
							"Error in createMetadataTemplatesCollectionUnderParent");
				}
			}
		} else {
			// Parameter is neither a .irods/metadataTemplates nor a .irods
			// collection
			log.info(
					"{} is neither a .irods collection nor a metadata templates collection",
					irodsAbsolutePathToParent);

			// Is it a valid irods collection at all?
			// This will throw a JargonException if not.
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(irodsAbsolutePathToParent);

			log.info("{} exists", irodsAbsolutePathToParent);

			// Create .irods subcollection
			dotIrodsService
					.createDotIrodsUnderParent(irodsAbsolutePathToParent);

			// Make sure it was created
			if (!this
					.isDotIrodsCollectionPresentInCollection(irodsAbsolutePathToParent)) {
				log.error("Error, .irods collection was not created");
				throw new JargonException(
						"DotIrodsService.createDotIrodsUnderParent failed to create .irods");
			}

			String dotIrodsDir = this
					.computeDotIrodsPathUnderParent(irodsAbsolutePathToParent);

			log.info(".irods created: {}", dotIrodsDir);

			// Create metadataTemplates subcollection
			if (this.createMetadataTemplatesCollectionUnderParent(dotIrodsDir)) {
				log.info("metadataTemplates collection created");
				irodsAbsolutePathToCollection = this
						.computeMetadataTemplatesPathUnderParent(dotIrodsDir);
			} else {
				log.error("Error, collection not created");
				throw new JargonException(
						"Error in createMetadataTemplatesCollectionUnderParent");
			}
		}

		log.info("irodsAbsolutePathToCollection: {}",
				irodsAbsolutePathToCollection);

		return irodsAbsolutePathToCollection;
	}

	private boolean createMetadataTemplatesCollectionUnderParent(
			final String irodsAbsolutePathToParent) throws JargonException {
		log.info("createDotIrodsUnderParent()");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToParent");
		}

		log.info("irodsAbsolutePathToParent: {}", irodsAbsolutePathToParent);

		String dotIrodsPath = this
				.computeMetadataTemplatesPathUnderParent(irodsAbsolutePathToParent);
		log.info("dotIrodsPath computed to be:{}", dotIrodsPath);

		IRODSFile dotIrodsFile = this.getPathAsIrodsFile(dotIrodsPath);
		if (dotIrodsFile == null) {
			// A JargonException was caught in getPathAsIrodsFile
			throw new JargonException(
					"JargonException thrown by instanceIRODSFile in getPathAsIrodsFile");
		}
		log.info("created");

		return dotIrodsFile.mkdirs();
	}

	private String computeDotIrodsPathUnderParent(
			final String irodsAbsolutePathToParent) {
		log.info("computeDotIrodsPathUnderParent");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToParent is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToParent);
		sb.append("/");
		sb.append(DotIrodsConstants.DOT_IRODS_DIR);
		return sb.toString();
	}

	private String computeMetadataTemplatesPathUnderParent(
			final String irodsAbsolutePathToParent) {
		log.info("computeMetadataTemplatesPathUnderParent");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToParent is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToParent);
		sb.append("/");
		sb.append(DotIrodsConstants.METADATA_TEMPLATES_DIR);
		return sb.toString();
	}

	private boolean isMetadataTemplatesCollectionPresentInCollection(
			String irodsAbsolutePath) {
		log.info("isMetadataTemplatesCollectionPresentInCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath),
					irodsAccount);
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		IRODSFile metadataTemplatesCollectionAsFile = this
				.getPathAsIrodsFile(this
						.computeMetadataTemplatesPathUnderParent(irodsAbsolutePath));

		return (metadataTemplatesCollectionAsFile != null);
	}

	private boolean isDotIrodsCollectionPresentInCollection(
			String irodsAbsolutePath) {
		log.info("isDotIrodsCollectionPresentInCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath),
					irodsAccount);
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		IRODSFile dotIrodsCollectionAsFile = this.getPathAsIrodsFile(this
				.computeDotIrodsPathUnderParent(irodsAbsolutePath));

		return (dotIrodsCollectionAsFile != null);
	}

	private boolean isMetadataTemplatesCollection(String irodsAbsolutePath) {
		log.info("isMetadataTemplatesCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath),
					irodsAccount);
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		boolean retVal = false;

		if (!irodsAbsolutePath
				.endsWith(DotIrodsConstants.METADATA_TEMPLATES_DIR)) {
			retVal = false;
		} else {
			IRODSFile pathAsFile = this.getPathAsIrodsFile(irodsAbsolutePath);

			if (pathAsFile == null) {
				retVal = false;
			} else {
				retVal = pathAsFile.isDirectory();
			}
		}

		return retVal;
	}

	private boolean isDotIrodsCollection(String irodsAbsolutePath) {
		log.info("isDotIrodsCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath),
					irodsAccount);
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		boolean retVal = false;

		if (!irodsAbsolutePath.endsWith(DotIrodsConstants.DOT_IRODS_DIR)) {
			retVal = false;
		} else {
			IRODSFile pathAsFile = this.getPathAsIrodsFile(irodsAbsolutePath);

			if (pathAsFile == null) {
				retVal = false;
			} else {
				retVal = pathAsFile.isDirectory();
			}
		}

		return retVal;
	}

	private IRODSFile getPathAsIrodsFile(String irodsAbsolutePath) {
		log.info("getPathAsIrodsFile()");

		IRODSFile retFile = null;

		try {
			retFile = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
							irodsAbsolutePath);
		} catch (JargonException je) {
			log.error(
					"JargonException thrown by instanceIRODSFile, {} does not exist",
					computeDotIrodsPathUnderParent(irodsAbsolutePath), je);
			retFile = null;
		}

		return retFile;
	}

	private void saveJSONStringToFile(String json, String irodsAbsolutePath)
			throws JargonException, IOException {
		log.info("saveJSONStringToFile()");

		if (json == null || json.isEmpty()) {
			throw new IllegalArgumentException("json is null or empty");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePath is null or empty");
		}

		IRODSFile templateIrodsFile = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsAbsolutePath);
		IRODSFileOutputStream irodsFileOutputStream = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFileOutputStream(templateIrodsFile);

		byte[] jsonByteArray = json.getBytes();

		irodsFileOutputStream.write(jsonByteArray);

		templateIrodsFile.close();
	}

	private List<MetaDataAndDomainData> queryTemplateAVUForFile(
			String irodsAbsolutePathToFile) throws JargonQueryException,
			JargonException {
		log.info("queryTemplateAVUForFile()");

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryElement.AVUQueryPart.UNITS, AVUQueryOperatorEnum.EQUAL,
				JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT));

		queryResult = irodsAccessObjectFactory.getDataObjectAO(irodsAccount)
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements,
						irodsAbsolutePathToFile);

		return queryResult;
	}
}