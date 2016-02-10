package org.irods.jargon.vircoll.types;

import java.io.IOException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.extensions.dotirods.DotIrodsConstants;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.vircoll.ConfigurableVirtualCollection;
import org.irods.jargon.vircoll.GeneralParameterConstants;
import org.irods.jargon.vircoll.VirtualCollectionMaintenanceService;
import org.irods.jargon.vircoll.VirtualCollectionMarshalingException;
import org.irods.jargon.vircoll.exception.VirtualCollectionException;
import org.irods.jargon.vircoll.exception.VirtualCollectionRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetadataQueryMaintenanceService extends AbstractJargonService
		implements VirtualCollectionMaintenanceService {
	private final DotIrodsService dotIrodsService;

	private static Logger log = LoggerFactory
			.getLogger(MetadataQueryMaintenanceService.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	public MetadataQueryMaintenanceService(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		dotIrodsService = new DotIrodsServiceImpl(irodsAccessObjectFactory,
				irodsAccount);
	}

	@Override
	public void addVirtualCollection(
			ConfigurableVirtualCollection configurableVirtualCollection,
			String collection, String uniqueName)
			throws DuplicateDataException, JargonException {
		log.info("addVirtualCollection()");

		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		IRODSFile vcFile = getPathAsIrodsFile(collection + "/" + uniqueName);

		if (vcFile != null && vcFile.exists()) {
			log.error("File already exists: " + collection + "/" + uniqueName);
			throw new DuplicateDataException("File already exists: "
					+ collection + "/" + uniqueName);
		}

		try {
			saveJsonStringToFile(
					serializeVirtualCollectionToJson(configurableVirtualCollection),
					collection + "/" + uniqueName);
		} catch (IOException e) {
			log.error("IOException saving JSON to file", e);
			throw new FileNotFoundException("IOException saving JSON to file",
					e);
		}
	}

	@Override
	public void storeVirtualCollection(
			ConfigurableVirtualCollection configurableVirtualCollection,
			String collection, String uniqueName) throws JargonException {
		log.info("storeVirtualCollection()");

		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		try {
			saveJsonStringToFile(
					serializeVirtualCollectionToJson(configurableVirtualCollection),
					collection + "/" + uniqueName);
		} catch (IOException e) {
			log.error("IOException saving JSON to file", e);
			throw new FileNotFoundException("IOException saving JSON to file",
					e);
		}
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

	@Override
	public ConfigurableVirtualCollection retrieveVirtualCollection(
			final String collection, final String uniqueName)
			throws FileNotFoundException, VirtualCollectionException {
		log.info("retrieveVirtualCollection()");

		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		IRODSFile vcFile = getPathAsIrodsFile(collection + "/" + uniqueName);

		if (vcFile == null || !vcFile.exists()) {
			log.error("Cannot find file: " + collection + "/" + uniqueName);
			throw new FileNotFoundException("Cannot find file: " + collection
					+ "/" + uniqueName);
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
		}
	}

	@Override
	public void deleteVirtualCollection(final String collection,
			final String uniqueName) throws FileNotFoundException,
			VirtualCollectionException {
		log.info("deleteVirtualCollection()");

		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection");
		}

		IRODSFile vcFile = getPathAsIrodsFile(collection + "/" + uniqueName);

		if (vcFile == null || !vcFile.exists()) {
			log.error("Cannot find file: " + collection + "/" + uniqueName);
			throw new FileNotFoundException("Cannot find file: " + collection
					+ "/" + uniqueName);
		}

		vcFile.delete();

	}

	/**
	 * Finds the iRODS absolute path to the user home temp metadata query
	 * collections, will create it if it does not exist. This is idempotent so
	 * if the coll exists it will silently ignore.
	 * 
	 * @param userName
	 *            <code>String</code> user name used to locate the home
	 *            directory
	 * @return
	 */
	public String findOrCreateUserTempMetadataQueryCollection(
			final String userName) {

		log.info("findOrCreateUserTempMetadataQueryCollection()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("userName: {}", userName);

		try {
			DotIrodsCollection dotIrodsCollection = dotIrodsService
					.findOrCreateUserHomeCollection(userName);
			StringBuilder sb = new StringBuilder();
			sb.append(dotIrodsCollection.getAbsolutePath());
			sb.append("/");
			sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
			String collName = sb.toString();
			IRODSFile vcFile = this.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFile(collName);
			vcFile.mkdirs();
			return collName;

		} catch (JargonException e) {
			log.error("unable to compute path for vc", e);
			throw new VirtualCollectionRuntimeException(
					"unable to compute path to user home dir", e);
		}

	}

	String findOrCreateTempMetadataQueryCollection(
			final String irodsAbsolutePathToParent) throws JargonException {
		log.info("findOrCreateMetadataTemplatesCollection()");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToParent");
		}

		log.info("irodsAbsolutePathToParent: {}", irodsAbsolutePathToParent);

		String irodsAbsolutePathToCollection = null;

		if (this.isTempMetadataQueryCollection(irodsAbsolutePathToParent)) {
			// Already a .irods/md_queries collection, so we're good
			log.info("{} is a .irods/metadataTemplates collection",
					irodsAbsolutePathToParent);
			irodsAbsolutePathToCollection = irodsAbsolutePathToParent;
		} else if (this.isDotIrodsCollection(irodsAbsolutePathToParent)) {
			// Parameter is a .irods collection, need to find or create a
			// md_queries collection beneath it.
			log.info("{} is a .irods collection", irodsAbsolutePathToParent);

			if (this.isTempMetadataQueryCollectionPresentUnderDotIrodsCollection(irodsAbsolutePathToParent)) {
				log.info("{} contains a metadataTemplates collection",
						irodsAbsolutePathToParent);
				irodsAbsolutePathToCollection = this
						.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToParent);
			} else {
				log.info(
						"{} does not contain a metadataTemplates collection, attempting to create",
						irodsAbsolutePathToParent);

				if (this.createTempMetadataQueryCollectionUnderDotIrods(irodsAbsolutePathToParent)) {
					log.info("metadataTemplates collection created");
					irodsAbsolutePathToCollection = this
							.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToParent);
				} else {
					log.error("Error, collection not created");
					throw new JargonException(
							"Error in createMetadataTemplatesCollectionUnderDotIrods");
				}
			}
		} else {
			// Parameter is neither a .irods/md_queries nor a .irods
			// collection
			log.info(
					"{} is neither a .irods collection nor a metadata query collection",
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

			// Create md_queries subcollection
			if (this.createTempMetadataQueryCollectionUnderDotIrods(dotIrodsDir)) {
				log.info("metadata query collection created");
				irodsAbsolutePathToCollection = this
						.computeTempMetadataQueryPathUnderDotIrods(dotIrodsDir);
			} else {
				log.error("Error, collection not created");
				throw new JargonException(
						"Error in createMetadataQueryCollectionUnderDotIrods");
			}
		}

		log.info("irodsAbsolutePathToCollection: {}",
				irodsAbsolutePathToCollection);

		return irodsAbsolutePathToCollection;
	}

	boolean isDotIrodsCollectionPresentInCollection(String irodsAbsolutePath) {
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

	boolean isTempMetadataQueryCollectionPresentUnderDotIrodsCollection(
			String irodsAbsolutePathToDotIrods) {
		log.info("isMetadataQueryCollectionPresentUnderParentCollection()");

		if (irodsAbsolutePathToDotIrods == null
				|| irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToDotIrods");
		}

		log.info("irodsAbsolutePathToDotIrods:{}", irodsAbsolutePathToDotIrods);

		try {
			@SuppressWarnings("unused")
			Collection collection = irodsAccessObjectFactory.getCollectionAO(
					irodsAccount).findByAbsolutePath(
					irodsAbsolutePathToDotIrods);
		} catch (JargonException je) {
			log.info(
					"JargonException thrown by findByAbsolutePath, {} does not exist or {} does not have sufficient permissions",
					irodsAbsolutePathToDotIrods, irodsAccount);
			return false;
		}

		log.info("{} exists", irodsAbsolutePathToDotIrods);

		IRODSFile metadataQueryCollectionAsFile = this
				.getPathAsIrodsFile(this
						.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToDotIrods));

		return (metadataQueryCollectionAsFile != null);
	}

	boolean isTempMetadataQueryCollection(String irodsAbsolutePath) {
		log.info("isMetadataQueryCollection()");

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
				.endsWith(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES)) {
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

	boolean isDotIrodsCollection(String irodsAbsolutePath) {
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

	String computeDotIrodsPathUnderParent(final String irodsAbsolutePathToParent) {
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

	String computeTempMetadataQueryPathUnderParent(
			final String irodsAbsolutePathToParent) {
		log.info("computeMetadataQueryPathUnderParent");

		if (irodsAbsolutePathToParent == null
				|| irodsAbsolutePathToParent.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToParent is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToParent);
		sb.append("/");
		sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
		return sb.toString();
	}

	String computeTempMetadataQueryPathUnderDotIrods(
			final String irodsAbsolutePathToDotIrods) {
		log.info("computeMetadataQueryPathUnderDotIrods");

		if (irodsAbsolutePathToDotIrods == null
				|| irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException(
					"irodsAbsolutePathToDotIrods is null or empty");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(irodsAbsolutePathToDotIrods);
		sb.append("/");
		sb.append(GeneralParameterConstants.USER_VC_TEMP_RECENT_VC_QUERIES);
		return sb.toString();
	}

	boolean createTempMetadataQueryCollectionUnderDotIrods(
			final String irodsAbsolutePathToDotIrods) throws JargonException {
		log.info("createMetadataQueryCollectionUnderDotIrods()");

		if (irodsAbsolutePathToDotIrods == null
				|| irodsAbsolutePathToDotIrods.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToDotIrods");
		}

		log.info("irodsAbsolutePathToDotIrods: {}", irodsAbsolutePathToDotIrods);

		String metadataQueryPath = this
				.computeTempMetadataQueryPathUnderDotIrods(irodsAbsolutePathToDotIrods);
		log.info("metadataQueryPath computed to be:{}", metadataQueryPath);

		IRODSFile metadataQueryFile = this
				.getPathAsIrodsFile(metadataQueryPath);
		if (metadataQueryFile == null) {
			// A JargonException was caught in getPathAsIrodsFile
			throw new JargonException(
					"JargonException thrown by instanceIRODSFile in getPathAsIrodsFile");
		}
		log.info("created");

		return metadataQueryFile.mkdirs();
	}

	IRODSFile getPathAsIrodsFile(String irodsAbsolutePath) {
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
				.instanceIRODSFileOutputStream(queryIrodsFile);

		byte[] jsonByteArray = json.getBytes();

		irodsFileOutputStream.write(jsonByteArray);

		queryIrodsFile.close();
	}

}