/**
 * 
 */
package org.irods.jargon.extensions.dotirods;

import org.irods.jargon.core.pub.domain.Collection;

/**
 * Represents data about a .irods collection
 * @author Mike Conway - DICE
 *
 */
public class DotIrodsCollection {
	
	/**
	 * Absolute irods path to this .irods file
	 */
	private String absolutePath = "";
	
	/**
	 * Indicates that this is the user home directory
	 */
	private boolean homeDir = false;
	
	/**
	 * iRODS catalog metadata about the .irods collection
	 */
	private Collection collection;
	
	public String getAbsolutePath() {
		return absolutePath;
	}
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	public boolean isHomeDir() {
		return homeDir;
	}
	public void setHomeDir(boolean homeDir) {
		this.homeDir = homeDir;
	}
	public Collection getCollection() {
		return collection;
	}
	public void setCollection(Collection collection) {
		this.collection = collection;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DotIrodsCollection [");
		if (absolutePath != null) {
			builder.append("absolutePath=");
			builder.append(absolutePath);
			builder.append(", ");
		}
		builder.append("homeDir=");
		builder.append(homeDir);
		builder.append(", ");
		if (collection != null) {
			builder.append("collection=");
			builder.append(collection);
		}
		builder.append("]");
		return builder.toString();
	}
	

}
