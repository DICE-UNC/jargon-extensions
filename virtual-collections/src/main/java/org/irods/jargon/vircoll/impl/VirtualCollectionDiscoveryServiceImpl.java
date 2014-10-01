/**
 *
 */
package org.irods.jargon.vircoll.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.vircoll.VirtualCollection;
import org.irods.jargon.vircoll.AbstractVirtualCollectionSerializer;
import org.irods.jargon.vircoll.VirtualCollectionDiscoveryService;
import org.irods.jargon.vircoll.VirtualCollectionFactory;
import org.irods.jargon.vircoll.VirtualCollectionMarshalingException;
import org.irods.jargon.vircoll.types.CollectionBasedVirtualCollection;
import org.irods.jargon.vircoll.types.StarredFoldersVirtualCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for maintaining, discovering and listing virtual collections (as
 * opposed to listing their contents). This can discover them, and return lists
 * of <code>AbstractVirtualCollection</code> subclasses
 * <p/>
 * This class serves as a point of registration, using a Builder pattern to
 * allow virtual collections to register themselves.
 * 
 * @author Mike Conway (DICE)
 * 
 */
public class VirtualCollectionDiscoveryServiceImpl extends
		AbstractJargonService implements VirtualCollectionDiscoveryService {

	/**
	 * Settable factory for virtual collections
	 */
	private VirtualCollectionFactory virtualCollectionFactory;
	private final ObjectMapper mapper = new ObjectMapper();

	private static Logger log = LoggerFactory
			.getLogger(VirtualCollectionDiscoveryServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to access Jargon services
	 * @param irodsAccount
	 *            {@link IRODSAccount} that represents the user login
	 *            credentials
	 */
	public VirtualCollectionDiscoveryServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Given a virtual collection, use its unmarshaling capability to turn the
	 * object into a string representation.
	 * <p/>
	 * Note that the specific deserializer may just do a default conversion from
	 * the JSON, but has the ability to do other decoration or manipulation as
	 * part of the process.
	 * 
	 * @param abstractVirtualCollection
	 * @return
	 * @throws VirtualCollectionMarshalingException
	 */
	public String stringRepresentationFromVirtualCollection(
			final VirtualCollection abstractVirtualCollection)
			throws VirtualCollectionMarshalingException {
		log.info("stringRepresentationFromVirtualCollection()");

		/*
		 * if (abstractVirtualCollection == null) { throw new
		 * IllegalArgumentException("null abstractVirtualCollection"); }
		 * 
		 * AbstractVirtualCollectionSerializer serializerObject;
		 * serializerObject =
		 * instantiateSerializerFromName(abstractVirtualCollection
		 * .getSerializerClass());
		 * 
		 * log.info("...got serializer, parse JSON as that object type"); return
		 * serializerObject
		 * .serializeToStringRepresentation(abstractVirtualCollection);
		 */return null;
	}

	/**
	 * Given a <code>String</code> representing a virtual collection in raw
	 * (JSON) form, return the corresponding object.
	 * <p/>
	 * This is done by creating the specified deserialzier object, which may do
	 * a straight conversion from JSON, or it may do addtional decoration based
	 * on the chose deserializer.
	 * 
	 * @param stringRepresentation
	 *            <code>String</code> with a representation of the virtual
	 *            collection
	 * @return {@link VirtualCollection}
	 * @throws VirtualCollectionMarshalingException
	 */
	public VirtualCollection virtualCollectionFromStringRepresentation(
			final String stringRepresentation)
			throws VirtualCollectionMarshalingException {

		/*
		 * log.info("virtualCollectionFromStringRepresentation()"); if
		 * (stringRepresentation == null || stringRepresentation.isEmpty()) {
		 * throw new IllegalArgumentException(
		 * "null or empty stringRepresentation"); }
		 * 
		 * log.info("..turning underlying JSON representation into a map...");
		 * 
		 * try {
		 * 
		 * @SuppressWarnings("unchecked") Map<String, Object> result = new
		 * ObjectMapper().readValue( stringRepresentation, HashMap.class);
		 * log.info("unmarshalled..."); String serializerClassName = (String)
		 * result.get("serializerClass"); AbstractVirtualCollectionSerializer
		 * serializerObject =
		 * instantiateSerializerFromName(serializerClassName);
		 * 
		 * log.info("...got serializer, parse JSON as that object type"); return
		 * serializerObject
		 * .deserializeFromStringRepresentation(stringRepresentation);
		 * 
		 * } catch (JsonParseException e) { log.info("JsonParseException", e);
		 * throw new VirtualCollectionMarshalingException(e); } catch
		 * (JsonMappingException e) { log.info("JsonMappingException", e); throw
		 * new VirtualCollectionMarshalingException(e); } catch (IOException e)
		 * { log.info("IOException", e); throw new
		 * VirtualCollectionMarshalingException(e); }
		 */

		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.vircoll.impl.VirtualCollectionMaintenanceService#
	 * listDefaultUserCollections()
	 */
	@Override
	public List<VirtualCollection> listDefaultUserCollections() {
		log.info("listDefaultUserCollections()");

		List<VirtualCollection> virtualCollections = new ArrayList<VirtualCollection>();
		// add root
		virtualCollections
				.add(new CollectionBasedVirtualCollection("root", "/"));
		// add user dir
		virtualCollections
				.add(new CollectionBasedVirtualCollection(
						"home",
						MiscIRODSUtils
								.computeHomeDirectoryForIRODSAccount(getIrodsAccount())));
		// add starred folders
		virtualCollections.add(new StarredFoldersVirtualCollection());
		log.info("done...");
		return virtualCollections;

	}

	public VirtualCollectionFactory getVirtualCollectionFactory() {
		return virtualCollectionFactory;
	}

	public void setVirtualCollectionFactory(
			VirtualCollectionFactory virtualCollectionFactory) {
		this.virtualCollectionFactory = virtualCollectionFactory;
	}

}
