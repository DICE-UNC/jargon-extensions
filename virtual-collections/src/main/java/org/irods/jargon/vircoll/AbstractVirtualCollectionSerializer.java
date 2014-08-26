/**
 * 
 */
package org.irods.jargon.vircoll;

/**
 * Abstract superclass for implementations that can serialize/deserialize a
 * virtual collection from JSON to a POJO form
 * 
 * @author Mike Conway - DICE
 * 
 */
public abstract class AbstractVirtualCollectionSerializer {

	/**
	 * 
	 */
	public AbstractVirtualCollectionSerializer() {

	}

	/**
	 * Turn a string representation into a subclass of
	 * <code>AbstractVirtualCollection</code>
	 * 
	 * @param stringRepresentation
	 *            <code>String</code> with the raw string representation
	 * @return {@link AbstractVirtualCollection} that is deserialzied from the
	 *         string representation
	 * @throws VirtualCollectionMarshalingException
	 */
	public abstract AbstractVirtualCollection deserializeFromStringRepresentation(
			final String stringRepresentation)
			throws VirtualCollectionMarshalingException;

	/**
	 * Turn a virtual collection into its string representation, which typically
	 * is via JSON.
	 * 
	 * @param abstractVirtualCollection
	 *            {@link AbstractVirtualCollection}
	 * @return
	 * @throws VirtualCollectionMarshalingException
	 */
	public abstract String serializeToStringRepresentation(
			AbstractVirtualCollection abstractVirtualCollection)
			throws VirtualCollectionMarshalingException;

}
