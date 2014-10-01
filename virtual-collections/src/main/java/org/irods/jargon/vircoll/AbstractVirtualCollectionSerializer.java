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
	 * @return {@link VirtualCollection} that is deserialzied from the
	 *         string representation
	 * @throws VirtualCollectionMarshalingException
	 */
	public abstract VirtualCollection deserializeFromStringRepresentation(
			final String stringRepresentation)
			throws VirtualCollectionMarshalingException;

	/**
	 * Turn a virtual collection into its string representation, which typically
	 * is via JSON.
	 * 
	 * @param abstractVirtualCollection
	 *            {@link VirtualCollection}
	 * @return
	 * @throws VirtualCollectionMarshalingException
	 */
	public abstract String serializeToStringRepresentation(
			VirtualCollection abstractVirtualCollection)
			throws VirtualCollectionMarshalingException;

}
