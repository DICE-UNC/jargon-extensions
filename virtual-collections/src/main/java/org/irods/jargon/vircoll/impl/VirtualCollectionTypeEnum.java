/**
 * 
 */
package org.irods.jargon.vircoll.impl;

/**
 * Enumeration of virtual collection types (surely to be refactored)
 * 
 * @author Mike Conway - DICE
 * 
 */
public enum VirtualCollectionTypeEnum {

	COLLECTION_BASED("collectionBased", false), STARRED("starred", false), SPARQL(
			"sparql", true);

	private String textValue;
	private boolean persistent;

	VirtualCollectionTypeEnum(final String textValue, final boolean persistent) {
		this.textValue = textValue;
		this.persistent = persistent;
	}

	/**
	 * Find the collection type based on the text value, note that
	 * <code>null</code> is returned if no value matches
	 * 
	 * @param textValue
	 * @return
	 */
	public static VirtualCollectionTypeEnum findTypeByTextValue(
			final String textValue) {
		for (VirtualCollectionTypeEnum virtualCollectionTypeEnum : VirtualCollectionTypeEnum
				.values()) {
			if (virtualCollectionTypeEnum.getTextValue().equals(textValue)) {
				return virtualCollectionTypeEnum;
			}
		}

		return null;

	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

}
