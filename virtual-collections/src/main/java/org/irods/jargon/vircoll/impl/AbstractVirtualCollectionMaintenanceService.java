package org.irods.jargon.vircoll.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.extensions.dotirods.DotIrodsConstants;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.vircoll.CollectionTypes;
import org.irods.jargon.vircoll.ConfigurableVirtualCollection;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.VirtualCollectionMaintenanceService;
import org.irods.jargon.vircoll.VirtualCollectionMarshalingException;
import org.irods.jargon.vircoll.exception.VirtualCollectionException;
import org.irods.jargon.vircoll.exception.VirtualCollectionRuntimeException;
import org.irods.jargon.vircoll.types.MetadataQueryVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractVirtualCollectionMaintenanceService extends
		AbstractJargonService implements VirtualCollectionMaintenanceService {

	private DotIrodsService dotIrodsService;
	private ObjectMapper objectMapper = new ObjectMapper();

	static Logger log = LoggerFactory
			.getLogger(AbstractVirtualCollectionMaintenanceService.class);

	public AbstractVirtualCollectionMaintenanceService(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		dotIrodsService = new DotIrodsServiceImpl(irodsAccessObjectFactory,
				irodsAccount);
	}

	public AbstractVirtualCollectionMaintenanceService() {
		super();
	}

	@Override
	public void updateVirtualCollection(
			ConfigurableVirtualCollection configurableVirtualCollection)
			throws FileNotFoundException, VirtualCollectionException {
		log.info("updateVirtualCollection()");

		if (configurableVirtualCollection.getUniqueName() == null
				|| configurableVirtualCollection.getUniqueName().isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty uniqueName in virtual collection");
		}

		String path = absolutePathForUniqueName(configurableVirtualCollection
				.getUniqueName());

		try {
			saveJsonStringToFile(
					serializeVirtualCollectionToJson(configurableVirtualCollection),
					path);
		} catch (IOException | JargonException e) {
			log.error("Exception saving JSON to file", e);
			throw new VirtualCollectionException(
					"Exception saving JSON to file", e);
		}
	}

	@Override
	public void addVirtualCollection(
			ConfigurableVirtualCollection configurableVirtualCollection,
			CollectionTypes collection, String uniqueName)
			throws DuplicateDataException, JargonException {
		log.info("addVirtualCollection()");

		if (collection == null) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty uniqueName");
		}

		String path = fileNameForCollectionTypeAndUniqueName(collection,
				uniqueName);

		IRODSFile vcFile = getPathAsIrodsFile(path);

		if (vcFile != null && vcFile.exists()) {
			log.error("File already exists: " + path);
			throw new DuplicateDataException("File already exists: " + path);
		}

		try {
			saveJsonStringToFile(
					serializeVirtualCollectionToJson(configurableVirtualCollection),
					path);
			addVcTypeAVUToFile(path, configurableVirtualCollection.getType());
			addUniqueNameAVUToFile(path, uniqueName);
		} catch (IOException e) {
			log.error("IOException saving JSON to file", e);
			throw new FileNotFoundException("IOException saving JSON to file",
					e);
		}
	}

	private String fileNameForCollectionTypeAndUniqueName(
			CollectionTypes collection, String uniqueName)
			throws VirtualCollectionException {

		StringBuilder sb = new StringBuilder();

		switch (collection) {
		case USER_HOME:
			try {
				sb.append(this.dotIrodsService.findUserHomeCollection(
						this.irodsAccount.getUserName()).getAbsolutePath());
			} catch (JargonException e) {
				log.error("error resolving collection name", e);
				throw new VirtualCollectionException(
						"cannot resulve collection name", e);
			}
			sb.append("/");
			sb.append(GeneralParameterConstants.USER_VC_SUBDIR);
			break;

		case TEMPORARY_QUERY:
			try {
				sb.append(this.dotIrodsService.findUserHomeCollection(
						this.irodsAccount.getUserName()).getAbsolutePath());
			} catch (JargonException e) {
				log.error("error resolving collection name", e);
				throw new VirtualCollectionException(
						"cannot resulve collection name", e);
			}
			sb.append("/");
			sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
			break;

		default:
			log.error("cannot currently process type:{}", collection);
			throw new VirtualCollectionRuntimeException(
					"Unable to process collection type");
		}

		sb.append("/");
		sb.append(uniqueName);
		String name = sb.toString();
		log.info("resolved name:{}", name);
		return name;

	}

	@Override
	public String serializeVirtualCollectionToJson(
			ConfigurableVirtualCollection configurableVirtualCollection)
			throws VirtualCollectionException {
		log.info("serializeVirtualCollectionToJson");
		if (configurableVirtualCollection == null) {
			throw new IllegalArgumentException(
					"null configurableVirtualCollection");
		}

		try {
			return objectMapper
					.writeValueAsString(configurableVirtualCollection);
		} catch (JsonProcessingException e) {
			log.error("error creating JSON from metadata query", e);
			throw new VirtualCollectionMarshalingException(
					"error creating JSON", e);
		}
	}

	/**
	 * Given a unique name, return the virtual collection information
	 * 
	 * @param uniqueName
	 *            <code>String</code> with the unique name of this virtual
	 *            collection within the collection type
	 * @return {@link ConfigurableVirtualCollection}
	 * @throws FileNotFoundException
	 * @throws VirtualCollectionException
	 */
	@Override
	public ConfigurableVirtualCollection retrieveVirtualCollectionGivenUniqueName(
			final String uniqueName) throws FileNotFoundException,
			VirtualCollectionException {
		log.info("retrieveVirtualCollection()");

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		String vcAbsolutePath = absolutePathForUniqueName(uniqueName);

		IRODSFile vcFile = getPathAsIrodsFile(vcAbsolutePath);

		if (vcFile == null || !vcFile.exists()) {
			log.error("Cannot find file: " + vcAbsolutePath);
			throw new FileNotFoundException("Cannot find file: "
					+ vcAbsolutePath);
		}

		IRODSFileInputStream irodsFileInputStream = null;
		byte[] b = null;
		try {
			irodsFileInputStream = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFileInputStream(vcFile);
			b = new byte[irodsFileInputStream.available()];
			irodsFileInputStream.read(b);
			String decoded = new String(b, "UTF-8");

			return objectMapper.readValue(decoded,
					MetadataQueryVirtualCollection.class);

		} catch (JargonException e) {
			log.error(
					"JargonException reading virtual collection from irods file",
					e);
			throw new VirtualCollectionException(
					"Cannot deserialize virtual collection from JSON", e);
		} catch (IOException e) {
			log.error("IOException reading virtual collection from irods file",
					e);
			throw new VirtualCollectionException(
					"Cannot deserialize virtual collection from JSON", e);
		} finally {
			try {
				irodsFileInputStream.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	private String absolutePathForUniqueName(final String uniqueName)
			throws FileNotFoundException, VirtualCollectionException {
		List<DataObject> vcsAsDataObject = null;
		String vcAbsolutePath = "";
		try {
			DataObjectAO dataObjectAO = this.getIrodsAccessObjectFactory()
					.getDataObjectAO(getIrodsAccount());
			List<AVUQueryElement> query = new ArrayList<AVUQueryElement>();
			query.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.UNITS,
					AVUQueryOperatorEnum.EQUAL,
					GeneralParameterConstants.UNIQUE_NAME_AVU_UNIT));
			query.add(AVUQueryElement.instanceForValueQuery(AVUQueryPart.VALUE,
					AVUQueryOperatorEnum.EQUAL, uniqueName));

			vcsAsDataObject = dataObjectAO.findDomainByMetadataQuery(query);
			if (vcsAsDataObject.isEmpty()) {
				log.error("no vc found with unique name:{}", uniqueName);
				throw new FileNotFoundException("unable to find vc");
			} else if (vcsAsDataObject.size() > 1) {
				log.warn(
						"multiple vcs found with unique name: {}, returning first",
						uniqueName);
				vcAbsolutePath = vcsAsDataObject.get(0).getAbsolutePath();
			} else {
				log.info("found vc for unique name");
				vcAbsolutePath = vcsAsDataObject.get(0).getAbsolutePath();
			}

		} catch (FileNotFoundException fnf) {
			throw fnf;
		} catch (JargonException | JargonQueryException e1) {
			log.error("error querying for vc with unique name:{}", uniqueName);
			throw new VirtualCollectionException(e1);
		}
		return vcAbsolutePath;
	}

	@Override
	public void deleteVirtualCollection(final String uniqueName)
			throws FileNotFoundException, VirtualCollectionException {
		log.info("deleteVirtualCollection()");

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		IRODSFile vcFile;
		try {
			vcFile = getPathAsIrodsFile(this
					.absolutePathForUniqueName(uniqueName));
			vcFile.delete();

		} catch (FileNotFoundException e) {
			// ignore
		}

	}

	protected boolean isDotIrodsCollectionPresentInCollection(
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

	protected boolean isDotIrodsCollection(String irodsAbsolutePath) {
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

	protected String computeDotIrodsPathUnderParent(
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

	protected IRODSFile getPathAsIrodsFile(String irodsAbsolutePath) {
		log.info("getPathAsIrodsFile()");

		IRODSFile retFile = null;

		try {
			retFile = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
							irodsAbsolutePath);
		} catch (JargonException je) {
			log.error(
					"JargonException thrown by instanceIRODSFile, {} does not exist",
					irodsAbsolutePath, je);
			retFile = null;
		}

		return retFile;
	}

	/**
	 * Save the JSON-ized virtual collection data as an iRODS file. Will
	 * overwrite existing data
	 * 
	 * @param json
	 *            <code>String</code> with the JSON
	 * @param irodsAbsolutePath
	 *            <code>String</code> with the iRODS absolute path
	 * @throws JargonException
	 * @throws IOException
	 */
	void saveJsonStringToFile(String json, String irodsAbsolutePath)
			throws JargonException, IOException {
		log.info("saveJsonStringToFile()");

		if (json == null || json.isEmpty()) {
			throw new IllegalArgumentException("json is null or empty");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePath is null or empty");
		}

		IRODSFile queryIrodsFile = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount).instanceIRODSFile(
						irodsAbsolutePath);
		IRODSFileOutputStream irodsFileOutputStream = irodsAccessObjectFactory
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFileOutputStream(queryIrodsFile,
						OpenFlags.WRITE_TRUNCATE);

		byte[] jsonByteArray = json.getBytes();

		irodsFileOutputStream.write(jsonByteArray);

		try {
			irodsFileOutputStream.close();
		} catch (Exception e) {
			// ignore
		}

		queryIrodsFile.close();
	}

	void addVcTypeAVUToFile(String path, String type) throws JargonException {
		log.info("addVcTypeAVUToFile()");
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		if (type == null || type.isEmpty()) {
			throw new IllegalArgumentException("null or empty type");
		}

		AvuData avuData = AvuData.instance(
				GeneralParameterConstants.VCTYPE_AVU_ATTRIBUTE, type,
				GeneralParameterConstants.VCTYPE_AVU_UNIT);
		getIrodsAccessObjectFactory().getDataObjectAO(irodsAccount)
				.deleteAVUMetadata(path, avuData);
		getIrodsAccessObjectFactory().getDataObjectAO(irodsAccount)
				.addAVUMetadata(path, avuData);
	}

	void addUniqueNameAVUToFile(String path, String name)
			throws JargonException {
		log.info("addUniqueNameAVUToFile()");
		AvuData avuData = AvuData.instance(
				GeneralParameterConstants.UNIQUE_NAME_AVU_ATTRIBUTE, name,
				GeneralParameterConstants.UNIQUE_NAME_AVU_UNIT);
		getIrodsAccessObjectFactory().getDataObjectAO(irodsAccount)
				.deleteAVUMetadata(path, avuData);
		getIrodsAccessObjectFactory().getDataObjectAO(irodsAccount)
				.addAVUMetadata(path, avuData);

	}

	/**
	 * @return the dotIrodsService
	 */
	public DotIrodsService getDotIrodsService() {
		return dotIrodsService;
	}

	/**
	 * @param dotIrodsService
	 *            the dotIrodsService to set
	 */
	public void setDotIrodsService(DotIrodsService dotIrodsService) {
		this.dotIrodsService = dotIrodsService;
	}

	/**
	 * @return the objectMapper
	 */
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * @param objectMapper
	 *            the objectMapper to set
	 */
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	private CollectionTypes collectionTypeForParentCollectionName(
			final String parentCollectionName) {

		log.info("collectionTypeForParentCollectionName()");

		if (parentCollectionName
				.equals(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES)) {
			return CollectionTypes.TEMPORARY_QUERY;
		} else if (parentCollectionName
				.equals(GeneralParameterConstants.USER_VC_SUBDIR)) {
			return CollectionTypes.USER_HOME;
		} else if (parentCollectionName
				.equals(GeneralParameterConstants.USER_DISABLED_VC_SUBDIR)) {
			return CollectionTypes.USER_HIDDEN;
		} else {
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.VirtualCollectionMaintenanceService#
	 * reclassifyVirtualCollection(org.irods.jargon.vircoll.CollectionTypes,
	 * java.lang.Stringhttps://www.youtube.com/watch?v=ZFKYH_Cm7So)
	 */
	@Override
	public void reclassifyVirtualCollection(CollectionTypes collection,
			String uniqueName) throws FileNotFoundException,
			VirtualCollectionException {

		log.info("reclassifyVirtualCollection()");
		if (collection == null) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty uniqueName");
		}

		log.info("will move collection {}", uniqueName);
		log.info("to class: {}", collection);

		String vcAbsolutePath = absolutePathForUniqueName(uniqueName);

		IRODSFile vcFile = getPathAsIrodsFile(vcAbsolutePath);
		String parentPath = vcFile.getParent();
		log.info("path of parent is:{}", parentPath);

		// get the last bit of the path

		String parentCollectionName = MiscIRODSUtils
				.getLastPathComponentForGivenAbsolutePath(parentPath);

		CollectionTypes typeForCurrent = this
				.collectionTypeForParentCollectionName(parentCollectionName);
		if (typeForCurrent == null) {
			log.error(
					"cannot resolve the vrtual collection type for the given path:{}",
					parentCollectionName);
			throw new FileNotFoundException("cannot find parent type");
		}

		String newPath = fileNameForCollectionTypeAndUniqueName(collection,
				uniqueName);

		IRODSFile newPathAsFile = this.getPathAsIrodsFile(newPath);
		IRODSFile newPathParentFile = this.getPathAsIrodsFile(newPathAsFile
				.getParent());
		// make sure dest file is not htere
		IRODSFile destFile = this.getPathAsIrodsFile(newPath);
		destFile.delete();
		newPathParentFile.mkdirs();
		try {
			DataTransferOperations dto = this.getIrodsAccessObjectFactory()
					.getDataTransferOperations(getIrodsAccount());
			log.info("moving:{}", vcAbsolutePath);
			log.info("to:{}", newPath);
			dto.move(vcAbsolutePath, newPath);
		} catch (JargonException e) {
			log.error("exception moving file", e);
			throw new VirtualCollectionException("error moving file", e);
		}

	}
}
