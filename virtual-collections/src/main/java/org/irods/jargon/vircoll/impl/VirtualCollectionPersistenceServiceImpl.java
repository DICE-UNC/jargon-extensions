/**
 * 
 */
package org.irods.jargon.vircoll.impl;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.irods.jargon.vircoll.VirtualCollectionException;
import org.irods.jargon.vircoll.VirtualCollectionMaintenanceService;
import org.irods.jargon.vircoll.types.ConfigurableVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to maintain virtual collections and persist them properly in iRODS
 * 
 * @author Mike Conway - DICE
 * 
 */
public class VirtualCollectionPersistenceServiceImpl extends
		AbstractJargonService implements VirtualCollectionMaintenanceService {

	private final DotIrodsService dotIrodsService;

	private static Logger log = LoggerFactory
			.getLogger(VirtualCollectionPersistenceServiceImpl.class);

	private final ObjectMapper mapper = new ObjectMapper();

	/*
	 * @Override public VirtualCollection
	 * retrieveVirtualCollectionFromUserCollection( final String userName, final
	 * String virtualCollectionName) throws FileNotFoundException,
	 * VirtualCollectionException {
	 * 
	 * log.info("retrieveVirtualCollectionFromUserCollection()");
	 * 
	 * if (userName == null || userName.isEmpty()) { throw new
	 * IllegalArgumentException("null userName"); }
	 * 
	 * if (virtualCollectionName == null || virtualCollectionName.isEmpty()) {
	 * throw new IllegalArgumentException("null virtualCollectionName"); }
	 * 
	 * try { String vcParentPath =
	 * returnVirtualCollectionPathInUserHome(userName); StringBuilder sb = new
	 * StringBuilder(); sb.append(vcParentPath); sb.append("/");
	 * sb.append(virtualCollectionName); String computedPath = sb.toString();
	 * log.info("computedPath:{}", computedPath); return
	 * retrieveVirtualCollectionFromFile(computedPath);
	 * 
	 * } catch (JargonException e) {
	 * log.error("jargon exception getting virtual collection", e); throw new
	 * VirtualCollectionException(
	 * "exception getting virtual collection for user", e); }
	 * 
	 * }
	 * 
	 * 
	 * @Override public void addVirtualCollectionToUserCollection( final
	 * AbstractVirtualCollection configurableVirtualCollection) throws
	 * DuplicateDataException, JargonException {
	 * 
	 * log.info("addVirtualCollectionToUserCollection()");
	 * 
	 * if (configurableVirtualCollection == null) { throw new
	 * IllegalArgumentException( "null configurableVirtualCollection"); }
	 * 
	 * String liveVirtualCollectionPath =
	 * createIfNecessaryAndReturnVirtualCollectionPathInUserHome
	 * (getIrodsAccount() .getUserName());
	 * 
	 * StringBuilder sb = new StringBuilder();
	 * sb.append(liveVirtualCollectionPath); sb.append("/");
	 * sb.append(configurableVirtualCollection.getUniqueName()); String
	 * absPathToVc = sb.toString();
	 * 
	 * IRODSFile vcFile = getIrodsAccessObjectFactory().getIRODSFileFactory(
	 * getIrodsAccount()).instanceIRODSFile(absPathToVc); if (vcFile.exists()) {
	 * log.error("vc exists"); throw new DuplicateDataException(
	 * "virtual collection already exists"); }
	 * 
	 * String jsonForVc =
	 * serializeVirtualCollectionToJson(configurableVirtualCollection);
	 * 
	 * log.info("saving virtual collection to path:{}", absPathToVc);
	 * 
	 * Stream2StreamAO stream2StreamAO = getIrodsAccessObjectFactory()
	 * .getStream2StreamAO(getIrodsAccount());
	 * stream2StreamAO.streamBytesToIRODSFile(jsonForVc.getBytes(), vcFile);
	 * 
	 * log.info("done!");
	 * 
	 * }
	 * 
	 * 
	 * @Override public VirtualCollection retrieveVirtualCollectionFromFile(
	 * final String virtualCollectionAbsolutePath) throws FileNotFoundException,
	 * VirtualCollectionException {
	 * 
	 * log.info("serializeVirtualCollectionToJson()");
	 * 
	 * if (virtualCollectionAbsolutePath == null ||
	 * virtualCollectionAbsolutePath.isEmpty()) { throw new
	 * IllegalArgumentException( "null or empty virtualCollectionAbsolutePath");
	 * }
	 * 
	 * IRODSFile virtualCollectionFile; try { virtualCollectionFile =
	 * getIrodsAccessObjectFactory()
	 * .getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(
	 * virtualCollectionAbsolutePath); } catch (JargonException e) {
	 * log.error("exception getting virtual collection file", e); throw new
	 * VirtualCollectionException( "exception getting virtual collection file",
	 * e); }
	 * 
	 * if (!virtualCollectionFile.exists()) { log.error("cannot find file");
	 * throw new FileNotFoundException( "cannot find virtual collection file");
	 * }
	 * 
	 * try { InputStream irodsFileInputStream = new BufferedInputStream(
	 * getIrodsAccessObjectFactory().getIRODSFileFactory(
	 * getIrodsAccount()).instanceIRODSFileInputStream( virtualCollectionFile));
	 * 
	 * return mapper.readValue(irodsFileInputStream, VirtualCollection.class); }
	 * catch (JsonProcessingException e) {
	 * log.error("error writing virtual collection as string", e); throw new
	 * VirtualCollectionException( "cannot retrieve virtualCollection", e); }
	 * catch (JargonException e) { log.error(
	 * "Jargon exception reading virtual collection from irods file", e); throw
	 * new VirtualCollectionException(
	 * "cannot deserialize virtual collection from json", e); } catch
	 * (IOException e) { log.error(
	 * "io exception reading virtual collection from irods file", e); throw new
	 * VirtualCollectionException(
	 * "cannot deserialize virtual collection from json", e); }
	 * 
	 * }
	 * 
	 * 
	 * @Override public String serializeVirtualCollectionToJson( final
	 * VirtualCollection configurableVirtualCollection) throws
	 * VirtualCollectionException {
	 * 
	 * log.info("serializeVirtualCollectionToJson()");
	 * 
	 * if (configurableVirtualCollection == null) { throw new
	 * IllegalArgumentException( "null or empty configurableVirtualCollection");
	 * }
	 * 
	 * try { String json = mapper
	 * .writeValueAsString(configurableVirtualCollection); log.info("json:{}",
	 * json); // FIXME: delete later return json; } catch
	 * (JsonProcessingException e) {
	 * log.error("error writing virtual collection as string", e); throw new
	 * VirtualCollectionException( "cannot extract JSON from virtualCollection",
	 * e); }
	 * 
	 * }
	 * 
	 * private String returnVirtualCollectionPathInUserHome(final String
	 * userName) throws FileNotFoundException, JargonException {
	 * 
	 * log.info("returnVirtualCollectionPathInUserHome()");
	 * 
	 * if (userName == null || userName.isEmpty()) { throw new
	 * IllegalArgumentException("null or empty userName"); }
	 * 
	 * log.info("userName:{}", userName);
	 * 
	 * DotIrodsCollection dotIrodsFile = dotIrodsService
	 * .findUserHomeCollection(userName);
	 * 
	 * StringBuilder sb = new StringBuilder();
	 * sb.append(dotIrodsFile.getAbsolutePath()); sb.append("/");
	 * sb.append(VirtualCollectionConstants.VIRTUAL_COLLECTIONS_SUBDIR);
	 * 
	 * String virtualCollectionsDir = sb.toString();
	 * log.info("virtualCollectionsDir:{}", virtualCollectionsDir);
	 * 
	 * IRODSFile virtualCollectionFile = getIrodsAccessObjectFactory()
	 * .getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(
	 * virtualCollectionsDir);
	 * 
	 * if (!virtualCollectionFile.exists()) { throw new FileNotFoundException(
	 * "file not found for virtual collections"); }
	 * 
	 * return virtualCollectionsDir;
	 * 
	 * }
	 * 
	 * private String createIfNecessaryAndReturnVirtualCollectionPathInUserHome(
	 * final String userName) throws JargonException {
	 * 
	 * log.info("createIfNecessaryAndReturnVirtualCollectionPathInUserHome()");
	 * 
	 * if (userName == null || userName.isEmpty()) { throw new
	 * IllegalArgumentException("null or empty userName"); }
	 * 
	 * log.info("userName:{}", userName);
	 * 
	 * DotIrodsCollection dotIrodsFile = dotIrodsService
	 * .findOrCreateUserHomeCollection(userName);
	 * 
	 * StringBuilder sb = new StringBuilder();
	 * sb.append(dotIrodsFile.getAbsolutePath()); sb.append("/");
	 * sb.append(VirtualCollectionConstants.VIRTUAL_COLLECTIONS_SUBDIR);
	 * 
	 * String virtualCollectionsDir = sb.toString();
	 * log.info("virtualCollectionsDir:{}", virtualCollectionsDir);
	 * 
	 * IRODSFile virtualCollectionFile = getIrodsAccessObjectFactory()
	 * .getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(
	 * virtualCollectionsDir); virtualCollectionFile.mkdirs();
	 * 
	 * return virtualCollectionsDir;
	 * 
	 * }
	 */

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public VirtualCollectionPersistenceServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		dotIrodsService = new DotIrodsServiceImpl(irodsAccessObjectFactory,
				irodsAccount);
	}

	@Override
	public void addVirtualCollectionToUserCollection(
			ConfigurableVirtualCollection configurableVirtualCollection)
			throws DuplicateDataException, JargonException {
		// TODO Auto-generated method stub

	}

	@Override
	public String serializeVirtualCollectionToJson(
			ConfigurableVirtualCollection configurableVirtualCollection)
			throws VirtualCollectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConfigurableVirtualCollection retrieveVirtualCollectionFromFile(
			String virtualCollectionAbsolutePath) throws FileNotFoundException,
			VirtualCollectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConfigurableVirtualCollection retrieveVirtualCollectionFromUserCollection(
			String userName, String virtualCollectionName)
			throws FileNotFoundException, VirtualCollectionException {
		// TODO Auto-generated method stub
		return null;
	}

}
