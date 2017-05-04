package org.irods.jargon.metadatatemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
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

	/**
	 * Default constructor takes config and context information
	 *
	 * @param metadataTemplateContext
	 * @param irodsAccessObjectFactory
	 * @param metadataTemplateConfiguration
	 */
	public JargonMetadataResolver(final MetadataTemplateContext metadataTemplateContext,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final MetadataTemplateConfiguration metadataTemplateConfiguration) {
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
	 * @throws MetadataTemplateProcessingException
	 */
	@Override
	public List<MetadataTemplate> listPublicTemplates() throws MetadataTemplateProcessingException {
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
			throw new MetadataTemplateProcessingException("cannot find public templates", je);
		} catch (IOException ie) {
			log.error("IOException when processing public templates", ie);
			throw new MetadataTemplateProcessingException("cannot find public templates", ie);

		}
		// }

		return tempList;
	}

	private DotIrodsService instanceDotIrodsService() {
		DotIrodsService dotIrodsService = new DotIrodsServiceImpl(getIrodsAccessObjectFactory(),
				retrieveIrodsAccountFromContext());
		return dotIrodsService;
	}

	private String retrivePublicLocation() {
		return getMetadataTemplateConfiguration().getPublicTemplateIdentifier();
	}

	private IRODSAccount retrieveIrodsAccountFromContext() {
		return getMetadataTemplateContext().getIrodsAccount();
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
	 * @throws MetadataTemplateProcessingException
	 */
	private String findAbsolutePathForUUID(final String uuid) throws MetadataTemplateProcessingException {
		log.info("findAbsolutePathForUUID()");

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = null;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.VALUE,
					QueryConditionOperators.EQUAL, uuid.toString()));

			queryResult = getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext())
					.findMetadataValuesByMetadataQuery(queryElements);
		} catch (JargonQueryException jqe) {
			log.error("AvuQuery for UUID failed!", jqe);
			throw new MetadataTemplateProcessingException("avu query for UUID failed", jqe);
		} catch (JargonException je) {
			log.error("JargonException in getFqNameForUUID", je);
			throw new MetadataTemplateProcessingException("avu query for UUID failed", je);
		}

		if (queryResult.isEmpty()) {
			log.error("No match for specified UUID!");
			return null;
		}

		if (queryResult.size() > 1) {
			log.error("{} matches for specified UUID! This should be impossible!", queryResult.size());
			throw new MetadataTemplateProcessingException("no match found");

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
	public UUID saveTemplate(final MetadataTemplate metadataTemplate,
			final MetadataTemplateLocationTypeEnum metadataTemplateLocationTypeEnum)
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
				sb.append(getMetadataTemplateConfiguration().getPublicTemplateIdentifier());
			} else if (metadataTemplateLocationTypeEnum == MetadataTemplateLocationTypeEnum.USER) {
				log.info("user dir used");
				sb.append(
						dotIrodsService.findOrCreateUserHomeCollection(retrieveIrodsAccountFromContext().getUserName())
								.getAbsolutePath());
				sb.append("/");
				sb.append(DotIrodsConstants.METADATA_TEMPLATES_SUBDIR);
			} else {
				log.error("unknown template location type:{}", metadataTemplateLocationTypeEnum);
				throw new MetadataTemplateProcessingException("Unsupported metadata template location type");
			}

			IRODSFile mdTemplateParentFile = getPathAsIrodsFile(sb.toString());
			log.info("parent for mdtemplate dir is:{}", mdTemplateParentFile);
			mdTemplateParentFile.mkdirs();
			mdTemplateFile = getIrodsAccessObjectFactory().getIRODSFileFactory(retrieveIrodsAccountFromContext())
					.instanceIRODSFile(mdTemplateParentFile.getAbsolutePath(), generateMdTemplateFileName());
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

		String absPath = findAbsolutePathForUUID(metadataTemplate.getUuid().toString());
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

		for (MetadataTemplate mt : listAllTemplatesBoundToPath(templateSearchPath)) {
			log.info("Required template found: {}", mt.getName());
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

	private MetadataMergeResult mergeTemplateListAndAVUs(final Map<String, MetadataTemplate> templateMap,
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
					tempMt = findTemplateByUUID(uuid);
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

	private String getValueFromRefQuery(final String refQuery, final String irodsAbsolutePath) {
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
	private UUID addMdTemplateAVUToFile(final String name, final String path) throws JargonException {
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
	private void addMdElementAVUToFile(final String name, final String path) throws JargonException {
		log.info("addMdElementAVUToFile, name = {}", name);
		UUID uuid = UUID.randomUUID();
		AvuData avuData = AvuData.instance(name, uuid.toString(), JargonMetadataTemplateConstants.MD_ELEMENT_UNIT);
		getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext()).addAVUMetadata(path, avuData);
	}

	private IRODSFile getPathAsIrodsFile(final String irodsAbsolutePath) {
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

	private void saveJSONStringToFile(final String json, final String irodsAbsolutePath)
			throws JargonException, IOException {
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

	private List<MetaDataAndDomainData> queryTemplateAVUForFile(final String irodsAbsolutePathToFile)
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

	private String getPathFromFqName(final String inFileName) {
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
	public MetadataTemplate findTemplateByUUID(final String uuid)
			throws MetadataTemplateProcessingException, MetadataTemplateParsingException {

		log.info("findTemplateByUUID()");
		if (uuid == null || uuid.isEmpty()) {
			throw new IllegalArgumentException("null or empty UUID");
		}

		log.info("uuid:{}", uuid);
		String absPath = findAbsolutePathForUUID(uuid);
		IRODSFile templateFile;
		try {
			templateFile = getIrodsAccessObjectFactory().getIRODSFileFactory(retrieveIrodsAccountFromContext())
					.instanceIRODSFile(absPath);
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

	@Override
	public List<MetadataTemplate> listAvailableTemplates() throws MetadataTemplateProcessingException {
		log.info("listAvailableTemplates()");

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();
		List<MetadataTemplate> templates = new ArrayList<MetadataTemplate>();

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT));

			queryResult = getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext())
					.findMetadataValuesByMetadataQuery(queryElements);

			for (MetaDataAndDomainData metadata : queryResult) {
				log.info("metadata:{}", metadata);
				templates.add(findTemplateByUUID(metadata.getAvuValue()));
			}

			return templates;

		} catch (JargonQueryException | JargonException e) {
			log.error("error listing metadata templates", e);
			throw new MetadataTemplateParsingException("error processing template", e);
		}

	}

	@Override
	public List<MetadataTemplate> listAllTemplatesBoundToPath(final String irodsAbsolutePath)
			throws FileNotFoundException, MetadataTemplateProcessingException {
		log.info("listAllTemplatesBoundToPath()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}
		log.info("for path:{}", irodsAbsolutePath);
		try {
			IRODSFileFactory irodsFileFactory = getIrodsAccessObjectFactory()
					.getIRODSFileFactory(retrieveIrodsAccountFromContext());
			IRODSFile dirFile = irodsFileFactory.instanceIRODSFile(irodsAbsolutePath);

			if (!dirFile.exists()) {
				log.warn("file path does not exist:{}", irodsAbsolutePath);
				throw new FileNotFoundException("path does not exist");
			}

			String queryPath;

			if (dirFile.isFile()) {
				log.info("this is a file, get the parent and start looking at templates");
				queryPath = dirFile.getParent();
			} else {
				queryPath = dirFile.getAbsolutePath();
			}

			/*
			 * start walking up the tree querying each directory to accumulate
			 * templates, closest template wins
			 */

			List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();
			accumlateTemplatesInHeirarchy(metadataTemplates, queryPath);

			/**
			 * The top of the list is the closest, so move into set, this will
			 * filter
			 */

			Set<MetadataTemplate> finalTemplatesList = new LinkedHashSet<MetadataTemplate>();
			for (MetadataTemplate templateCandidate : metadataTemplates) {
				log.debug("candidate:{}", templateCandidate);
				boolean added = finalTemplatesList.add(templateCandidate);
				log.debug("added?:{}", added);
			}

			// TODO: shouldn't the accumulator just make a set? There is an
			// intermediate collection here not necessary - mcc
			return new ArrayList<MetadataTemplate>(finalTemplatesList);

		} catch (JargonException e) {
			log.error("exception listing bound templates", e);
			throw new MetadataTemplateProcessingException(e);

		}

	}

	/**
	 * This is a list, so it will pick up duplicate bindings. It recurses
	 * backwards up the tree, so that elements at the top of the list should
	 * supercede elements at the bottom. This selecting of the overriding
	 * template is external to the function of this method
	 * 
	 * @param metadataTemplates
	 * @param queryPath
	 * @throws MetadataTemplateParsingException
	 */
	private void accumlateTemplatesInHeirarchy(final List<MetadataTemplate> metadataTemplates, final String queryPath)
			throws MetadataTemplateParsingException {

		log.info("accumulateTemplatesInHeirarchy()");
		log.info("queryPath:{}", queryPath);
		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();
		String parentPath;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, JargonMetadataTemplateConstants.MD_BINDING_UNIT));

			queryResult = getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.findMetadataValuesByMetadataQuery(queryElements);

			for (MetaDataAndDomainData metadata : queryResult) {
				log.info("metadata:{}", metadata);
				metadataTemplates.add(findTemplateByUUID(metadata.getAvuValue()));
			}

			IRODSFile currentPathFile = getIrodsAccessObjectFactory()
					.getIRODSFileFactory(retrieveIrodsAccountFromContext()).instanceIRODSFile(queryPath);

			// TODO: add early escape checks

			parentPath = currentPathFile.getParent();
			if (parentPath.equals("/")) {
				log.info("done accumulating");
				return;
			}

			// more to walk, query again
			accumlateTemplatesInHeirarchy(metadataTemplates, parentPath);

		} catch (JargonQueryException | JargonException e) {
			log.error("error listing metadata templates", e);
			throw new MetadataTemplateParsingException("error processing template", e);
		}

	}

	@Override
	public List<MetadataTemplate> findTemplateByName(final String name,
			final MetadataTemplateLocationTypeEnum metadataTemplateLocationTypeEnum)
			throws MetadataTemplateProcessingException, MetadataTemplateParsingException {
		log.info("findTemplateByName()");
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}

		log.info("name:{}", name);
		log.info("metadataTemplateLocationTypeEnum:{}", metadataTemplateLocationTypeEnum);

		List<MetadataTemplate> metadataTemplates = new ArrayList<MetadataTemplate>();

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = new ArrayList<MetaDataAndDomainData>();

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.UNITS,
					QueryConditionOperators.EQUAL, JargonMetadataTemplateConstants.MD_TEMPLATE_UNIT));

			queryResult = getIrodsAccessObjectFactory().getDataObjectAO(retrieveIrodsAccountFromContext())
					.findMetadataValuesByMetadataQuery(queryElements);
		} catch (JargonQueryException | JargonException e) {
			log.error("error querying for templates", e);
			throw new MetadataTemplateProcessingException("error querying for templates", e);
		}

		// filter based on type

		for (MetaDataAndDomainData metadata : queryResult) {

			// if no filter just add
			if (metadataTemplateLocationTypeEnum == null) {
				log.debug("no filter");
				metadataTemplates.add(findTemplateByUUID(metadata.getAvuValue()));
				continue;

			}

			// filter out public or user as appropriate
			if (metadataTemplateLocationTypeEnum == MetadataTemplateLocationTypeEnum.PUBLIC) {
				log.debug("only public");
				if (metadata.getDomainObjectUniqueName()
						.equals(getMetadataTemplateConfiguration().getPublicTemplateIdentifier())) {
					log.debug("data is public:{}", metadata);
					metadataTemplates.add(findTemplateByUUID(metadata.getAvuValue()));
				} else {
					log.debug("filtered, not public");
				}
			} else if (metadataTemplateLocationTypeEnum == MetadataTemplateLocationTypeEnum.USER) {
				log.debug("only user");

				if (!(metadata.getDomainObjectUniqueName()
						.equals(getMetadataTemplateConfiguration().getPublicTemplateIdentifier()))) {
					log.debug("data is user:{}", metadata);
					metadataTemplates.add(findTemplateByUUID(metadata.getAvuValue()));
				} else {
					log.debug("filtered, not user");
				}

			}

		}

		return metadataTemplates;

	}

	// FIXME: required? how to serialize? should this be absorbed into the JSON
	// delievered to client?
	@Override
	public void bindTemplateToPath(final String irodsAbsolutePath, final String templateUuid, final boolean required)
			throws FileNotFoundException, MetadataTemplateProcessingException {

		log.info("bindTemplateToPath()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (templateUuid == null || templateUuid.isEmpty()) {
			throw new IllegalArgumentException("null or empty templateUuid");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("templateUuid:{}", templateUuid);

		IRODSFile targetFile;
		try {
			targetFile = this.getIrodsAccessObjectFactory().getIRODSFileFactory(this.retrieveIrodsAccountFromContext())
					.instanceIRODSFile(irodsAbsolutePath);
			if (!targetFile.exists()) {
				log.error("file for bind does not exist at :{}", irodsAbsolutePath);
				throw new FileNotFoundException("file does not exist, cannot bind");
			}

			if (!targetFile.isDirectory()) {
				log.error("file for bind does not exist at :{}", irodsAbsolutePath);
				throw new FileNotFoundException("file does not exist, cannot bind");
			}

			addMdBindAvuToFile(templateUuid, targetFile.getAbsolutePath());
		} catch (JargonException e) {
			log.error("error binding templates", e);
			throw new MetadataTemplateProcessingException("error binding templates", e);
		}

	}

	private void addMdBindAvuToFile(final String uuid, final String path) throws MetadataTemplateProcessingException {
		log.info("addMdBindAvuToFile()");
		try {
			AvuData avuData = AvuData.instance(JargonMetadataTemplateConstants.MD_BINDING_NAME, uuid,
					JargonMetadataTemplateConstants.MD_BINDING_UNIT);
			getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext()).addAVUMetadata(path,
					avuData);
		} catch (JargonException e) {
			log.error("error binding templates", e);
			throw new MetadataTemplateProcessingException("error binding templates", e);
		}
	}

	@Override
	public void unbindTemplate(final String irodsAbsolutePath, final String templateUuid)
			throws MetadataTemplateProcessingException {
		log.info("unbindTemplate()");

		try {
			AvuData avuData = AvuData.instance(JargonMetadataTemplateConstants.MD_BINDING_NAME, templateUuid,
					JargonMetadataTemplateConstants.MD_BINDING_UNIT);
			getIrodsAccessObjectFactory().getCollectionAO(retrieveIrodsAccountFromContext())
					.deleteAVUMetadata(irodsAbsolutePath, avuData);
		} catch (JargonException e) {
			log.error("error unbinding templates", e);
			throw new MetadataTemplateProcessingException("error unbinding templates", e);
		}
	}

}