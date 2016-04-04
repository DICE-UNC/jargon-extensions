/**
 * 
 */
package org.irods.jargon.vircoll;


/**
 * Collection type and path as a POJO
 * 
 * @author Mike Conway - DICE
 *
 */
public class CollectionTypeAndPath {

	private CollectionTypes collectionType = CollectionTypes.USER_HOME;
	private String path = "";

	/**
	 * @return the collectionType
	 */
	public CollectionTypes getCollectionType() {
		return collectionType;
	}

	/**
	 * @param collectionType
	 *            the collectionType to set
	 */
	public void setCollectionType(CollectionTypes collectionType) {
		this.collectionType = collectionType;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CollectionTypeAndPath [");
		if (collectionType != null) {
			builder.append("collectionType=").append(collectionType)
					.append(", ");
		}
		if (path != null) {
			builder.append("path=").append(path);
		}
		builder.append("]");
		return builder.toString();
	}

}
