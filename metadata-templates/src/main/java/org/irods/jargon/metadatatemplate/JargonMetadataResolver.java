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
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.extensions.dotirods.DotIrodsConstants;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JargonMetadataResolver extends AbstractMetadataResolver<MetadataTemplateContext> {

	static private Logger log = LoggerFactory.getLogger(IRODSFileFactoryImpl.class);

	static private TemplateParserSingleton parser = TemplateParserSingleton.PARSER;

	// FIXME: account for version in avus and in add/update
	// FIXME: semantics to promote template to public?
	// FIXME: menu of other mdtemplate locations? How to set up a group repo?
	// How to config?

	/**
	 * Default constructor takes config and context information
	 * 
	 * @param metadataTemplateContext
	 * @param irodsAccessObjectFactory
	 * @param metadataTemplateConfiguration
	 */
	public JargonMetadataResolver(MetadataTemplateContext metadataTemplateContext,
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			MetadataTemplateConfiguration metadataTemplateConfiguration) {
		super(metadataTemplateContext, irodsAccessObjectFactory, metadataTemplateConfiguration);
	}

	/**
	 * Return a list of MetadataTemplates for all "publicly available" metadata
	 * template files.
	 * </p>
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
	 * directories, only one is returned; determined by the list order of
	 * publicTemplateLocations.
	 *
	 * @return List of {@link MetadataTemplate}
	 */
	@Override
	public List<MetadataTemplate> listPublicTemplates() {
		List<MetadataTemplate> tempList = new ArrayList<MetadataTemplate>();

		// for (String dir : getPublicTemplateLocations()) {
		try {
			for (MetadataTemplate newMT : listTemplatesInCollection(retrivePublicLocation())) {
				boolean isDuplicate = false;

				for (MetadataTemplate existingMT : tempList) {
					if (newMT.getName().compareTo(existingMT.getName()) == 0) {
						// Another template of the same name has already
						// been found
						isDuplicate = true;
						break;
					}
				}

				if (!isDuplicate) {
					tempList.add(newMT);
				}
			}
		} catch (JargonException je) {
			log.error("JargonException when obtaining public templates", je);
		} catch (IOException ie) {
			log.error("IOException when processing public templates", ie);
		}
		// }

		return tempList;
	}

	/**
	 * Return a list of MetadataTemplates found in the iRODS hierarchy of which
	 * the given path is the lowermost leaf.
	 * </p>
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
	public List<MetadataTemplate> listTemplatesInDirectoryHierarchyAbovePath(final String absolutePath)
			throws IOException {
		log.info("listTemplatesInDirectoryHierarchyAbovePath");
		List<MetadataTemplate> templateList = null;
		File[] templateFiles = {};
		DotIrodsService dotIrodsService = instanceDotIrodsService();

		try {
			templateFiles = dotIrodsService.listFilesOfTypeInDirectoryHierarchyDotIrodsSubDir(absolutePath,
					DotIrodsConstants.METADATA_TEMPLATES_SUBDIR, new MetadataTemplateFileFilter());
		} catch (JargonException je) {
			log.error("JargonException when listing files in directory", je);
			return templateList;
		}

		try {
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException je) {
			log.error("JargonException when processing metadata template files", je);
			return templateList;
		}

		return templateList;
	}

	/**
	 * Return a MetadataTemplate given its name.
	 * </p>
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
	@Override // FIXME: switch to AVU query
	public MetadataTemplate findTemplateByName(final String name, final String activeDir) throws FileNotFoundException,
			IOException, MetadataTemplateProcessingException, MetadataTemplateParsingException {
		log.info("findTemplateByName()");

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("name is null or empty");
		}

		if (activeDir == null || activeDir.isEmpty()) {
			throw new IllegalArgumentException("activeDir is null or empty");
		}

		MetadataTemplate returnTemplate = null;

		// First look in directory hierarchy
		returnTemplate = findTemplateByNameInDirectoryHierarchy(name, activeDir);

		// If null, no template was found in directory hierarchy
		// Look in public locations
		if (returnTemplate == null) {
			log.info("No match in directory hierarchy, trying public locations");

			returnTemplate = findTemplateByNameInPublicTemplates(name);
		}

		if (returnTemplate == null) {
			log.info("No match found for name {}, returning null", name);
		}

		return returnTemplate;
	}

	@Override // FIXME: switch to AVU query
	public MetadataTemplate findTemplateByNameInDirectoryHierarchy(final String name, final String activeDir)
			throws FileNotFoundException, IOException, MetadataTemplateProcessingException,
			MetadataTemplateParsingException {
		log.info("findTemplateByNameInDirectoryHierarchy()");

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("name is null or empty");
		}

		if (activeDir == null || activeDir.isEmpty()) {
			throw new IllegalArgumentException("activeDir is null or empty");
		}

		MetadataTemplate returnTemplate = null;
		File[] templateFilesInHierarchy;

		DotIrodsService dotIrodsService = instanceDotIrodsService();

		try {
			templateFilesInHierarchy = dotIrodsService.listFilesOfTypeInDirectoryHierarchyDotIrodsSubDir(activeDir,
					DotIrodsConstants.METADATA_TEMPLATES_SUBDIR, new MetadataTemplateFileFilter());
		} catch (JargonException je) {
			log.error("JargonException when listing files in directory", je);
			throw new MetadataTemplateProcessingException("error listing .irods files", je);
		}

		if (templateFilesInHierarchy.length > 0) {
			for (File f : templateFilesInHierarchy) {
				String nameFromFilename = LocalFileUtils.getFileNameUpToExtension(f.getName());

				if (nameFromFilename.equalsIgnoreCase(name)) {
					log.info("Name matched: {}", f.getAbsolutePath());

					try {
						returnTemplate = processFileToMetadataTemplate((IRODSFile) f);
					} catch (JargonException je) {
						log.error("JargonException in processFileToMetadataTemplate", je);
						log.info("Matched {} with {}, but file could not be processed", name, f.getAbsolutePath());
						returnTemplate = null;
					}
					break;
				}
			}
		}

		return returnTemplate;
	}

	private DotIrodsService instanceDotIrodsService() {
		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(this.getIrodsAccessObjectFactory(),
				retrieveIrodsAccountFromContext());
		return dotIrodsService;
	}

	/**
	 * FIXME: change to AVU based query - mcc
	 */

	@Override
	public MetadataTemplate findTemplateByNameInPublicTemplates(final String name) throws FileNotFoundException,
			IOException, MetadataTemplateProcessingException, MetadataTemplateParsingException {
		log.info("findTemplateByNameInPublicTemplates()");

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("name is null or empty");
		}

		MetadataTemplate returnTemplate = null;
		IRODSFile collectionIrodsFile = null;
		// for (String publicDir : getPublicTemplateLocations()) {
		try {
			collectionIrodsFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(retrieveIrodsAccountFromContext()).instanceIRODSFile(retrivePublicLocation());
		} catch (JargonException je) {
			log.error("JargonException when opening {} as IRODSFile", retrivePublicLocation(), je);
			log.info("Could not open {}, skipping to next public dir", retrivePublicLocation());
		}

		for (File f : collectionIrodsFile.listFiles(new MetadataTemplateFileFilter())) {
			String nameFromFilename = LocalFileUtils.getFileNameUpToExtension(f.getName());

			if (nameFromFilename.equalsIgnoreCase(name)) {
				log.info("Name matched: {}", f.getAbsolutePath());

				try {
					returnTemplate = processFileToMetadataTemplate((IRODSFile) f);
				} catch (JargonException je) {
					log.error("JargonException in processFileToMetadataTemplate", je);
					log.info("Matched {} with {}, but file could not be processed", name, f.getAbsolutePath());
					returnTemplate = null;
				}
				break;
			}
		}

		// if (matched) {
		// break;
		// }
		// }

		return returnTemplate;
	}

	private String retrivePublicLocation() {
		return this.getMetadataTemplateConfiguration().getPublicTemplateIdentifier();
	}

	private IRODSAccount retrieveIrodsAccountFromContext() {
		return this.getMetadataTemplateContext().getIrodsAccount();
	}

	/**
	 * Return the fully-qualified path to a metadata template file given a UUID.
	 *
	 * @param uuid
	 *            {@link UUID}
	 * @return String
	 *
	 *         XXX Should this make sure a template is in an "appropriate"
	 *         location?
	 */
	private String findAbsolutePathForUUID(final String uuid) {
		log.info("findAbsolutePathForUUID()");

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = null;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.VALUE,
					QueryConditionOperators.EQUAL, uuid.toString()));

			queryResult = this.getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext())
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
			log.error("{} matches for specified UUID! This should be impossible!", queryResult.size());
			log.info("Returning the fully-qualified name for only the first matched file.");
		}

		return queryResult.get(0).getDomainObjectUniqueName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.metadatatemplate.AbstractMetadataResolver#saveTemplate(
	 * org.irods.jargon.metadatatemplate.MetadataTemplate,
	 * org.irods.jargon.metadatatemplate.MetadataTemplateLocationTypeEnum)
	 */
	@Override // FIXME: what about duplicate add? - mcc
	public UUID saveTemplate(MetadataTemplate metadataTemplate,
			MetadataTemplateLocationTypeEnum metadataTemplateLocationTypeEnum)
			throws MetadataTemplateProcessingException {

		log.info("saveFormBasedTemplateAsJSON()");

		if (metadataTemplateLocationTypeEnum == null) {
			throw new IllegalArgumentException("metadataTemplateLocationTypeEnum is null or empty");
		}

		if (metadataTemplate == null) {
			throw new IllegalArgumentException("metadataTemplate is null or empty");
		}

		IRODSFile mdTemplateFile;
		try {
			DotIrodsService dotIrodsService = instanceDotIrodsService();

			StringBuilder sb = new StringBuilder();

			if (metadataTemplateLocationTypeEnum == MetadataTemplateLocationTypeEnum.PUBLIC) {
				log.info("public dir used");
				sb.append(this.getMetadataTemplateConfiguration().getPublicTemplateIdentifier());
			} else if (metadataTemplateLocationTypeEnum == MetadataTemplateLocationTypeEnum.USER) {
				log.info("user dir used");
				sb.append(dotIrodsService
						.findOrCreateUserHomeCollection(this.retrieveIrodsAccountFromContext().getUserName())
						.getAbsolutePath());
				sb.append("/");
				sb.append(DotIrodsConstants.METADATA_TEMPLATES_SUBDIR);
			} else {
				log.error("unknown template location type:{}", metadataTemplateLocationTypeEnum);
				throw new MetadataTemplateProcessingException("Unsupported metadata template location type");
			}

			IRODSFile mdTemplateParentFile = this.getPathAsIrodsFile(sb.toString());
			log.info("parent for mdtemplate dir is:{}", mdTemplateParentFile);
			mdTemplateParentFile.mkdirs();
			mdTemplateFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(this.retrieveIrodsAccountFromContext())
					.instanceIRODSFile(mdTemplateParentFile.getAbsolutePath(), this.generateMdTemplateFileName());
		} catch (JargonException e) {
			log.error("exception creating mdtemplate file", e);
			throw new MetadataTemplateProcessingException("unable to create mdtemplate file", e);
		}

		String jsonString = parser.createJSONFromMetadataTemplate(metadataTemplate);

		log.info("json string created");

		log.info("Saving to: {}", mdTemplateFile);
		UUID uuid;
		try {
			saveJSONStringToFile(jsonString, mdTemplateFile.getAbsolutePath());
			uuid = addMdTemplateAVUToFile(metadataTemplate.getName(), mdTemplateFile.getAbsolutePath());
			for (MetadataElement me : metadataTemplate.getElements()) {
				addMdElementAVUToFile(me.getName(), mdTemplateFile.getAbsolutePath());
			}
		} catch (JargonException | IOException je) {
			log.error("JargonException when trying to write String to file");
			throw new MetadataTemplateProcessingException("error writing mdtemplate file", je);
		}

		log.info("saved");

		return uuid;
	}

	private String generateMdTemplateFileName() {
		StringBuilder sb = new StringBuilder();
		sb.append("mdtemplate");
		sb.append(System.currentTimeMillis());
		sb.append(MetadataTemplateConstants.TEMPLATE_FILE_EXT);
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.metadatatemplate.AbstractMetadataResolver#updateTemplate
	 * (java.lang.String, org.irods.jargon.metadatatemplate.MetadataTemplate)
	 */
	@Override
	public void updateTemplate(final MetadataTemplate metadataTemplate) throws MetadataTemplateProcessingException {
		log.info("updateTemplateByFqName()");

		if (metadataTemplate == null) {
			throw new IllegalArgumentException("metadataTemplate is null or empty");
		}

		String absPath = this.findAbsolutePathForUUID(metadataTemplate.getUuid().toString());
		log.info("resolved absPath:{}", absPath);

		IRODSFile inFile = getPathAsIrodsFile(absPath);

		if (inFile.exists() && inFile.isFile()) {
			log.info("found file");
		} else {
			log.error("{} could not be retrieved as IRODSFile; update failed", inFile);
			throw new MetadataTemplateNotFoundException("unable to find metadata template");
		}

		String jsonString = null;
		try {
			jsonString = parser.createJSONFromMetadataTemplate(metadataTemplate);
		} catch (JargonException je) {
			log.error("JargonException in createJSONFromMetadataTemplate()", je);
			throw new MetadataTemplateParsingException(je);
		}

		log.info("json string created");

		try {
			saveJSONStringToFile(jsonString, absPath);
		} catch (JargonException | IOException je) {
			log.error("JargonException when trying to write String to file");
			throw new MetadataTemplateProcessingException(je);
		}

		log.info("done!");

	}

	/**
	 * Delete a metadata template give its fully-qualified iRODS path.
	 *
	 * @param fqName
	 *            {@link String}
	 */
	@Override
	public boolean deleteTemplate(final String fqName) {
		log.info("deleteTemplateByFqName()");

		if (fqName == null || fqName.isEmpty()) {
			throw new IllegalArgumentException("fqName is null or empty");
		}

		if (!fqName.endsWith(MetadataTemplateConstants.TEMPLATE_FILE_EXT)
				&& !fqName.endsWith(MetadataTemplateConstants.JSON_FILE_EXT)) {
			throw new IllegalArgumentException(
					"fqName does not represent a metadata template file, delete not attempted");
		}

		IRODSFile inFile = null;

		try {
			inFile = getIrodsAccessObjectFactory().getIRODSFileFactory(retrieveIrodsAccountFromContext())
					.instanceIRODSFile(fqName);
		} catch (JargonException e) {
			log.error("JargonException when trying to create IRODSFile for {}", fqName, e);
			log.error("Delete not performed.");

			return false;
		}

		return inFile.delete();
	}

	/**
	 * Save the values in the MetadataTemplate onto the object in the system
	 * metadata table.
	 *
	 *
	 * @param metadataTemplate
	 * @param pathToObject
	 *
	 * @throws FileNotFoundException
	 * @throws JargonException
	 *
	 */
	/*
	 * public void saveTemplateToSystemMetadataOnObject( MetadataTemplate
	 * metadataTemplate, String pathToObject) throws FileNotFoundException,
	 * JargonException { log.info("saveTemplateToSystemMetadataOnObject()");
	 *
	 * if (pathToObject == null || pathToObject.isEmpty()) { throw new
	 * IllegalArgumentException("pathToObject is null or empty"); }
	 *
	 * if (metadataTemplate == null) { throw new
	 * IllegalArgumentException("metadataTemplate is null"); }
	 *
	 * IRODSFile irodsObject = irodsAccessObjectFactory.getIRODSFileFactory(
	 * irodsAccount).instanceIRODSFile(pathToObject);
	 *
	 * if (!irodsObject.exists()) { throw new FileNotFoundException(
	 * "pathToObject does not resolve to an iRODS object"); }
	 *
	 * FileCatalogObjectAO objectAO = null;
	 *
	 * if (irodsObject.isFile()) { objectAO =
	 * irodsAccessObjectFactory.getDataObjectAO(irodsAccount); } else if
	 * (irodsObject.isDirectory()) { objectAO =
	 * irodsAccessObjectFactory.getCollectionAO(irodsAccount); } else { throw
	 * new IllegalArgumentException( "object at " + pathToObject +
	 * " is neither a data object nor a collection - the JargonMetadataResolver currently only supports these types of objects"
	 * ); }
	 *
	 * if (metadataTemplate.getType() == TemplateTypeEnum.FORM_BASED) { for
	 * (MetadataElement me : ((FormBasedMetadataTemplate) metadataTemplate)
	 * .getElements()) { if (!me.getCurrentValue().isEmpty()) { for (String s :
	 * me.getCurrentValue()) { AvuData avuData = AvuData .instance(
	 * me.getName(), s, JargonMetadataTemplateConstants.AVU_UNIT_PREFIX +
	 * metadataTemplate.getUuid() .toString()); if (irodsObject.isFile()) {
	 * ((DataObjectAO) objectAO).addAVUMetadata( pathToObject, avuData); } else
	 * if (irodsObject.isDirectory()) { ((CollectionAO)
	 * objectAO).addAVUMetadata( pathToObject, avuData); } } } } } // TODO else
	 * if for different TemplateTypeEnum types }
	 */
	/**
	 * Populate metadata templates from a list of AVUs
	 *
	 * TODO Are unaffiliated AVUs and orphan AVUs properly two separate lists?
	 *
	 * @param irodsAbsolutePathToCollection
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
	 * @throws JargonQueryException
	 *
	 */
	public MetadataMergeResult getAndMergeTemplateListForPath(final String irodsAbsolutePath)
			throws org.irods.jargon.core.exception.FileNotFoundException, JargonException, JargonQueryException,
			FileNotFoundException, IOException {
		log.info("getAndMergeTemplateListForPath()");

		ObjStat pathObjStat = getIrodsAccessObjectFactory().getIRODSFileSystemAO(retrieveIrodsAccountFromContext())
				.getObjStat(irodsAbsolutePath);

		Map<String, MetadataTemplate> templateMap = new HashMap<String, MetadataTemplate>();

		String templateSearchPath = pathObjStat.isSomeTypeOfCollection() ? irodsAbsolutePath
				: getPathFromFqName(irodsAbsolutePath);

		for (MetadataTemplate mt : listAllRequiredTemplates(templateSearchPath)) {
			log.info("Required template found: {}", mt.getName());
			// TODO Right now, only supports searching by UUID
			// nameUUID would be more general
			// i.e. dublinCore01234567-0123-0123-0123-0123456789ab
			String hashKey = mt.getUuid().toString();
			templateMap.put(hashKey, mt);
		}

		List<MetaDataAndDomainData> avuList;
		if (pathObjStat.isSomeTypeOfCollection()) {
			avuList = getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.findMetadataValuesForCollection(irodsAbsolutePath);
		} else {
			avuList = getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext())
					.findMetadataValuesForDataObject(irodsAbsolutePath);
		}

		return mergeTemplateListAndAVUs(templateMap, avuList, irodsAbsolutePath);
	}

	MetadataMergeResult mergeTemplateListAndAVUs(final Map<String, MetadataTemplate> templateMap,
			final List<MetaDataAndDomainData> avuList, final String irodsAbsolutePath)
			throws FileNotFoundException, IOException, JargonException {
		log.info("mergeTemplateListAndAVUs()");

		List<MetaDataAndDomainData> orphans = new ArrayList<MetaDataAndDomainData>();

		boolean matched = false;
		MetadataTemplate tempMt = null;
		int unitIndex = 0;
		int uuidStart = 0;

		for (MetaDataAndDomainData avu : avuList) {
			if (avu.getAvuUnit().contains(JargonMetadataTemplateConstants.AVU_UNIT_PREFIX)) {
				log.info("unit contains a template UUID");
				unitIndex = avu.getAvuUnit().indexOf(JargonMetadataTemplateConstants.AVU_UNIT_PREFIX);
				uuidStart = unitIndex + JargonMetadataTemplateConstants.AVU_UNIT_PREFIX.length();

				// The unit string might contain, e.g.,
				// "fromTemplate:01234567-01234-01234-01234-0123456789ab"
				String uuid = avu.getAvuUnit().substring(uuidStart);

				// See if the template is already in the hash map
				if (templateMap.containsKey(uuid)) {
					log.info("avu belongs to a template already in the map");
					tempMt = templateMap.get(uuid);

					MetadataTemplate tempFbmt = tempMt;
					for (MetadataElement me : tempFbmt.getElements()) {
						if (avu.getAvuAttribute().equalsIgnoreCase(me.getName())) {
							// Not a REF_IRODS_QUERY type, set current
							// value to raw value
							me.getCurrentValue().add(avu.getAvuValue());
							matched = true;
							break;
						}
					}

					if (!matched) {
						log.info("AVU claims to be from template {}, but name not matched", uuid);
						log.info("AVU: {}", avu.getAvuAttribute() + ", " + avu.getAvuValue() + ", " + avu.getAvuUnit());
					}
				} else {
					log.info("avu belongs to a template not in the map");
					tempMt = this.findTemplateByUUID(uuid);
					if (tempMt == null) {
						log.info("no template found for UUID {}", uuid);
					} else {
						MetadataTemplate tempFbmt = tempMt;
						for (MetadataElement me : tempFbmt.getElements()) {
							if (avu.getAvuAttribute().equalsIgnoreCase(me.getName())) {
								// Not a REF_IRODS_QUERY type, set current
								// value to raw value
								me.getCurrentValue().add(avu.getAvuValue());

								matched = true;
								break;
							}
						}
					} // XXX else if (tempMt is a different kind of template)

					if (!matched) {
						log.info("AVU claims to be from template {}, but name not matched", uuid);
						log.info("AVU: {}", avu.getAvuAttribute() + ", " + avu.getAvuValue() + ", " + avu.getAvuUnit());
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

		for (MetadataTemplate mt : returnList) {
			for (MetadataElement me : mt.getElements()) {
				if (me.getType() == ElementTypeEnum.REF_IRODS_QUERY) {
					me.getCurrentValue().add(getValueFromRefQuery(me.getDefaultValue().get(0), irodsAbsolutePath));
				}

			}
		}

		return new MetadataMergeResult(returnList, orphans);
	}

	String getValueFromRefQuery(final String refQuery, final String irodsAbsolutePath) {
		String returnString = "";
		/*
		 * try { // Check if need to reinitialize dataProfileAccessor if
		 * (dataProfileAccessor == null ||
		 * !dataProfileAccessor.getIrodsAbsolutePath()
		 * .equalsIgnoreCase(irodsAbsolutePath) ||
		 * !dataProfileAccessor.getIrodsUserName()
		 * .equalsIgnoreCase(irodsAccount.getUserName())) {
		 * log.info("XXXXX Initializing dataProfileAccessor");
		 * dataProfileAccessor = new DataProfileAccessorServiceImpl(
		 * irodsAccount, irodsAccessObjectFactory, irodsAbsolutePath); }
		 * returnString = dataProfileAccessor.retrieveValueFromKey(refQuery); }
		 * catch (ObjectNotFoundException e) { returnString =
		 * "OBJECT_OR_COLLECTION_NOT_FOUND"; } catch
		 * (WrongDataProfileTypeException e) { returnString =
		 * "WRONG_DATA_PROFILE_TYPE"; } catch (AttributeNotFoundException e) {
		 * returnString = "DATA_OR_COLLECTION_ATTRIBUTE_NOT_FOUND"; } catch
		 * (JargonException e) { returnString =
		 * "ERROR_CREATING_DATA_PROFILE_ACCESSOR"; }
		 */

		return returnString;
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
	private List<MetadataTemplate> processFilesToMetadataTemplates(final File[] inFileArray)
			throws MetadataTemplateProcessingException, MetadataTemplateParsingException {
		log.info("processFilesToMetadataTemplates()");

		List<MetadataTemplate> returnList = new ArrayList<MetadataTemplate>();

		List<String> templateNames = new ArrayList<String>();
		String fileNameWithoutExtension = null;

		for (File f : inFileArray) {
			// Handle "list order" for TemplatesInCollection and Public
			// Templates
			fileNameWithoutExtension = LocalFileUtils.getFileNameUpToExtension(f.getName());
			if (templateNames.contains(fileNameWithoutExtension)) {
				break;
			}

			returnList.add(processFileToMetadataTemplate((IRODSFile) f));
			templateNames.add(returnList.get(returnList.size() - 1).getName());
		}

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
	 * &#64;param inFile
	 *            {@link File}
	 * @return a <code>MetadataTemplate</code>
	 *
	 * @throws JargonException
	 * @throws IOException
	 */
	private MetadataTemplate processFileToMetadataTemplate(final IRODSFile inFile)
			throws MetadataTemplateProcessingException, MetadataTemplateParsingException {
		log.info("processFileToMetadataTemplate()");

		MetadataTemplate returnTemplate = null;
		IRODSFileInputStream fis = null;
		byte[] b = null;

		try {
			fis = getIrodsAccessObjectFactory().getIRODSFileFactory(retrieveIrodsAccountFromContext())
					.instanceIRODSFileInputStream(inFile);
		} catch (JargonException e) {
			log.error("error getting stream for template file", e);
			throw new MetadataTemplateParsingException("error getting input stream to parse template");
		}

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
			queryResult = queryTemplateAVUForFile(inFile.getAbsolutePath());
		} catch (JargonQueryException | JargonException jqe) {
			log.error("AvuQuery for UUID failed!", jqe);
			throw new MetadataTemplateParsingException("cannot find UUID AVU", jqe);
		}

		if (queryResult.isEmpty()) {
			log.error("AvuQuery for UUID failed for file:{}", inFile);
			throw new MetadataTemplateParsingException("cannot find UUID AVU");
		}

		UUID uuid = UUID.fromString(queryResult.get(0).getAvuValue());

		String decoded;
		try {
			b = new byte[fis.available()];
			fis.read(b);
			decoded = new String(b, "UTF-8");
		} catch (IOException e) {
			log.error("Error reading json from file:{}", inFile);
			throw new MetadataTemplateParsingException("cannot read template json", e);
		}

		log.info("Decoded string rep of byte array:\n{}", decoded);

		returnTemplate = parser.createMetadataTemplateFromJSON(decoded);

		// Decorate with data stored in iRODS db, not in file text

		// fqName
		returnTemplate.setFqName(inFile.getAbsolutePath());

		// UUID
		returnTemplate.setUuid(uuid);

		MetadataTemplate fbmt = returnTemplate;
		for (MetadataElement me : fbmt.getElements()) {
			me.setTemplateUuid(uuid);
		}

		// Date created, dateModified
		try {
			getIrodsAccessObjectFactory().getIRODSFileSystemAO(retrieveIrodsAccountFromContext());
			ObjStat objStat = getIrodsAccessObjectFactory().getIRODSFileSystemAO(retrieveIrodsAccountFromContext())
					.getObjStat(inFile.getAbsolutePath());
			returnTemplate.getCreated().setTime(objStat.getCreatedAt().getTime());
			returnTemplate.getModified().setTime(objStat.getModifiedAt().getTime());
		} catch (JargonException e) {
			log.error("Error adding date information to file:{}", inFile);
			throw new MetadataTemplateParsingException("Unable to read system catalog info for file", e);
		}

		return returnTemplate;
	}

	/**
	 *
	 */
	List<MetadataTemplate> listTemplatesInCollection(final String irodsAbsolutePathToCollection)
			throws IOException, JargonException, MetadataTemplateProcessingException, MetadataTemplateParsingException {
		log.info("listTemplatesInCollection()");

		if (irodsAbsolutePathToCollection == null || irodsAbsolutePathToCollection.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePathToCollection");
		}

		log.info("irodsAbsolutePathToCollection: {}", irodsAbsolutePathToCollection);

		IRODSFile collectionIrodsFile = null;

		collectionIrodsFile = getIrodsAccessObjectFactory().getIRODSFileFactory(retrieveIrodsAccountFromContext())
				.instanceIRODSFile(irodsAbsolutePathToCollection);

		return processFilesToMetadataTemplates(collectionIrodsFile.listFiles(new MetadataTemplateFileFilter()));
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
	UUID addMdTemplateAVUToFile(final String name, final String path) throws JargonException {
		log.info("addMdTemplateAVUToFile, name = {}", name);
		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance(name, uuid.toString(), JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT);
		getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext()).addAVUMetadata(path, avuData);
		return uuid;
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
	void addMdElementAVUToFile(final String name, final String path) throws JargonException {
		log.info("addMdElementAVUToFile, name = {}", name);
		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance(name, uuid.toString(), JargonMetadataTemplateConstants.MD_ELEMENT_UNIT);
		getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext()).addAVUMetadata(path, avuData);
	}

	boolean createMetadataTemplatesCollectionUnderParent(final String irodsAbsolutePathToParent)
			throws JargonException {
		log.info("createMetadataTemplatesCollectionUnderParent()");

		if (irodsAbsolutePathToParent == null || irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePathToParent");
		}

		log.info("irodsAbsolutePathToParent: {}", irodsAbsolutePathToParent);

		String metadataTemplatesPath = computeMetadataTemplatesPathUnderParent(irodsAbsolutePathToParent);
		log.info("metadataTemplatesPath computed to be:{}", metadataTemplatesPath);

		IRODSFile dotIrodsFile = getPathAsIrodsFile(metadataTemplatesPath);
		if (dotIrodsFile == null) {
			// A JargonException was caught in getPathAsIrodsFile
			throw new JargonException("JargonException thrown by instanceIRODSFile in getPathAsIrodsFile");
		}
		log.info("created");

		return dotIrodsFile.mkdirs();
	}

	boolean createMetadataTemplatesCollectionUnderDotIrods(final String irodsAbsolutePathToDotIrods)
			throws JargonException {
		log.info("createMetadataTemplatesCollectionUnderDotIrods()");

		if (irodsAbsolutePathToDotIrods == null || irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePathToDotIrods");
		}

		log.info("irodsAbsolutePathToDotIrods: {}", irodsAbsolutePathToDotIrods);

		String metadataTemplatesPath = computeMetadataTemplatesPathUnderDotIrods(irodsAbsolutePathToDotIrods);
		log.info("metadataTemplatesPath computed to be:{}", metadataTemplatesPath);

		IRODSFile metadataTemplatesFile = getPathAsIrodsFile(metadataTemplatesPath);
		if (metadataTemplatesFile == null) {
			// A JargonException was caught in getPathAsIrodsFile
			throw new JargonException("JargonException thrown by instanceIRODSFile in getPathAsIrodsFile");
		}
		log.info("created");

		return metadataTemplatesFile.mkdirs();
	}

	String computeDotIrodsPathUnderParent(final String irodsAbsolutePathToParent) {
		log.info("computeDotIrodsPathUnderParent");

		if (irodsAbsolutePathToParent == null || irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException("irodsAbsolutePathToParent is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToParent);
		sb.append("/");
		sb.append(DotIrodsConstants.DOT_IRODS_DIR);
		return sb.toString();
	}

	String computeMetadataTemplatesPathUnderParent(final String irodsAbsolutePathToParent) {
		log.info("computeMetadataTemplatesPathUnderParent");

		if (irodsAbsolutePathToParent == null || irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException("irodsAbsolutePathToParent is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToParent);
		sb.append("/");
		sb.append(DotIrodsConstants.METADATA_TEMPLATES_DIR);
		return sb.toString();
	}

	String computeMetadataTemplatesPathUnderDotIrods(final String irodsAbsolutePathToDotIrods) {
		log.info("computeMetadataTemplatesPathUnderDotIrods");

		if (irodsAbsolutePathToDotIrods == null || irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException("irodsAbsolutePathToDotIrods is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToDotIrods);
		sb.append("/");
		sb.append(DotIrodsConstants.METADATA_TEMPLATES_SUBDIR);
		return sb.toString();
	}

	boolean isMetadataTemplatesCollectionPresentUnderParentCollection(final String irodsAbsolutePath) {
		log.info("isMetadataTemplatesCollectionPresentUnderParentCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					irodsAbsolutePath, retrieveIrodsAccountFromContext());
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		IRODSFile metadataTemplatesCollectionAsFile = getPathAsIrodsFile(
				computeMetadataTemplatesPathUnderParent(irodsAbsolutePath));

		return (metadataTemplatesCollectionAsFile != null);
	}

	boolean isMetadataTemplatesCollectionPresentUnderDotIrodsCollection(final String irodsAbsolutePathToDotIrods) {
		log.info("isMetadataTemplatesCollectionPresentUnderParentCollection()");

		if (irodsAbsolutePathToDotIrods == null || irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePathToDotIrods");
		}

		log.info("irodsAbsolutePathToDotIrods:{}", irodsAbsolutePathToDotIrods);

		try {
			getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.findByAbsolutePath(irodsAbsolutePathToDotIrods);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					irodsAbsolutePathToDotIrods, retrieveIrodsAccountFromContext());
			return false;
		}

		log.info("{} exists", irodsAbsolutePathToDotIrods);

		IRODSFile metadataTemplatesCollectionAsFile = getPathAsIrodsFile(
				computeMetadataTemplatesPathUnderDotIrods(irodsAbsolutePathToDotIrods));

		return (metadataTemplatesCollectionAsFile != null);
	}

	boolean isDotIrodsCollectionPresentInCollection(final String irodsAbsolutePath) {
		log.info("isDotIrodsCollectionPresentInCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath), retrieveIrodsAccountFromContext());
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		IRODSFile dotIrodsCollectionAsFile = getPathAsIrodsFile(computeDotIrodsPathUnderParent(irodsAbsolutePath));

		return (dotIrodsCollectionAsFile != null);
	}

	boolean isMetadataTemplatesCollection(final String irodsAbsolutePath) {
		log.info("isMetadataTemplatesCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath), retrieveIrodsAccountFromContext());
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		boolean retVal = false;

		if (!irodsAbsolutePath.endsWith(DotIrodsConstants.METADATA_TEMPLATES_DIR)) {
			retVal = false;
		} else {
			IRODSFile pathAsFile = getPathAsIrodsFile(irodsAbsolutePath);

			if (pathAsFile == null) {
				retVal = false;
			} else {
				retVal = pathAsFile.isDirectory();
			}
		}

		return retVal;
	}

	boolean isDotIrodsCollection(final String irodsAbsolutePath) {
		log.info("isDotIrodsCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		try {
			getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.findByAbsolutePath(irodsAbsolutePath);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					computeDotIrodsPathUnderParent(irodsAbsolutePath), retrieveIrodsAccountFromContext());
			return false;
		}

		log.info("{} exists", irodsAbsolutePath);

		boolean retVal = false;

		if (!irodsAbsolutePath.endsWith(DotIrodsConstants.DOT_IRODS_DIR)) {
			retVal = false;
		} else {
			IRODSFile pathAsFile = getPathAsIrodsFile(irodsAbsolutePath);

			if (pathAsFile == null) {
				retVal = false;
			} else {
				retVal = pathAsFile.isDirectory();
			}
		}

		return retVal;
	}

	IRODSFile getPathAsIrodsFile(final String irodsAbsolutePath) {
		log.info("getPathAsIrodsFile()");

		IRODSFile retFile = null;

		try {
			retFile = getIrodsAccessObjectFactory().getIRODSFileFactory(retrieveIrodsAccountFromContext())
					.instanceIRODSFile(irodsAbsolutePath);
		} catch (JargonException je) {
			log.error("JargonException thrown by instanceIRODSFile, {} does not exist", irodsAbsolutePath, je);
			retFile = null;
		}

		return retFile;
	}

	void saveJSONStringToFile(final String json, final String irodsAbsolutePath) throws JargonException, IOException {
		log.info("saveJSONStringToFile()");

		if (json == null || json.isEmpty()) {
			throw new IllegalArgumentException("json is null or empty");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("irodsAbsolutePath is null or empty");
		}

		IRODSFile templateIrodsFile = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(retrieveIrodsAccountFromContext()).instanceIRODSFile(irodsAbsolutePath);
		IRODSFileOutputStream irodsFileOutputStream = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(retrieveIrodsAccountFromContext())
				.instanceIRODSFileOutputStream(templateIrodsFile);

		byte[] jsonByteArray = json.getBytes();

		irodsFileOutputStream.write(jsonByteArray);

		templateIrodsFile.close();
	}

	List<MetaDataAndDomainData> queryTemplateAVUForFile(final String irodsAbsolutePathToFile)
			throws JargonQueryException, JargonException {
		log.info("queryTemplateAVUForFile()");

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.UNITS,
				QueryConditionOperators.EQUAL, JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT));

		queryResult = getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext())
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements, irodsAbsolutePathToFile);

		return queryResult;
	}

	List<MetaDataAndDomainData> queryElementAVUForFile(final String irodsAbsolutePathToFile)
			throws JargonQueryException, JargonException {
		log.info("queryElementAVUForFile()");

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.UNITS,
				QueryConditionOperators.EQUAL, JargonMetadataTemplateConstants.MD_ELEMENT_UNIT));

		queryResult = getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext())
				.findMetadataValuesForDataObjectUsingAVUQuery(queryElements, irodsAbsolutePathToFile);

		return queryResult;
	}

	String getLocalFileNameWithoutExtension(final String inFileName) {
		String localFileName;
		int lastSlash = inFileName.lastIndexOf('/');
		if (lastSlash == -1) {
			localFileName = inFileName;
		} else {
			localFileName = inFileName.substring(lastSlash + 1);
		}
		return LocalFileUtils.getFileNameUpToExtension(localFileName);
	}

	String getPathFromFqName(final String inFileName) {
		String path;
		int lastSlash = inFileName.lastIndexOf('/');
		if ((lastSlash == -1) || (lastSlash == (inFileName.length() - 1))) {
			path = inFileName;
		} else {
			path = inFileName.substring(0, lastSlash);
		}
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.metadatatemplate.AbstractMetadataResolver#
	 * findTemplateByUUID(java.lang.String)
	 */
	@Override
	public MetadataTemplate findTemplateByUUID(String uuid)
			throws MetadataTemplateProcessingException, MetadataTemplateParsingException {

		log.info("findTemplateByUUID()");
		if (uuid == null || uuid.isEmpty()) {
			throw new IllegalArgumentException("null or empty UUID");
		}

		log.info("uuid:{}", uuid);
		String absPath = findAbsolutePathForUUID(uuid);
		IRODSFile templateFile;
		try {
			templateFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(this.retrieveIrodsAccountFromContext()).instanceIRODSFile(absPath);
		} catch (JargonException e) {
			log.error("error obtaining template file", e);
			throw new MetadataTemplateProcessingException(e);
		}

		if (!templateFile.exists()) {
			log.error("no file exists for metadata template with uuid:{}", uuid);
			throw new MetadataTemplateNotFoundException("unable to find metadata template");
		}

		try {
			return processFileToMetadataTemplate(templateFile);
		} catch (JargonException e) {
			log.error("error processing metadata template", e);
			throw new MetadataTemplateParsingException("error processing template", e);
		}

	}

}