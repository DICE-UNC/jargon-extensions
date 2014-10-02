/**
 *
 */
package org.irods.jargon.vircoll;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;
import org.irods.jargon.vircoll.impl.VirtualCollectionTypeEnum;

/**
 * Model of a virtual collection, which is an arbitrary source that can be
 * serialized into an iRODS file, and which produces an 'ils' like listing.
 * <p/>
 * The function of a virtual collection is to break away from reliance on a
 * hierarchical file tree as the sole arrangement of collections.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class VirtualCollection {

	/**
	 * The style of paging for this collection
	 * 
	 * @author Mike Conway - DICE
	 * 
	 */
	public static final String DEFAULT_ICON_KEY = "virtual.collection.default.icon";

	/**
	 * Enumarated type of this virtual collection
	 */
	public VirtualCollectionTypeEnum virtualCollectionTypeEnum;

	/**
	 * Map of optional parameters used in the operation of the virtual
	 * collection
	 */
	private Map<String, String> parameters = new HashMap<String, String>();

	/**
	 * Indicates the paging behavior of this collection
	 */
	private PagingStyle pagingStyle = PagingStyle.CONTINUOUS;

	/**
	 * Unique name for this virtual collection, used when presenting the
	 * collection in a listing. If this VC is serializable to JSON, the unique
	 * name will be the absolute path to the VC
	 */
	private String uniqueName = "Collection";

	/**
	 * Plain language descripton
	 */
	private String description = "Collection";

	/**
	 * i18n selector that can be used to name this collection
	 */
	private String i18Name = "virtual.collection.default.name";

	/**
	 * i18n selector that can be used to describe this collection
	 */
	private String i18Description = "virtual.collection.default.description";

	/**
	 * i18n selector that can be mapped to an icon depiction of this collection
	 */
	private String i18icon = DEFAULT_ICON_KEY;

	/**
	 * Generic body representing the query itself
	 */
	private String queryBody = "";

	/**
	 * Can this virtual collection be persisted in iRODS?
	 */
	private boolean canPersist = false;

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(final String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getI18Name() {
		return i18Name;
	}

	public void setI18Name(final String i18Name) {
		this.i18Name = i18Name;
	}

	public String getI18Description() {
		return i18Description;
	}

	public void setI18Description(final String i18Description) {
		this.i18Description = i18Description;
	}

	public String getI18icon() {
		return i18icon;
	}

	public void setI18icon(final String i18icon) {
		this.i18icon = i18icon;
	}

	public PagingStyle getPagingStyle() {
		return pagingStyle;
	}

	public void setPagingStyle(PagingStyle pagingStyle) {
		this.pagingStyle = pagingStyle;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public VirtualCollectionTypeEnum getVirtualCollectionTypeEnum() {
		return virtualCollectionTypeEnum;
	}

	public void setVirtualCollectionTypeEnum(
			VirtualCollectionTypeEnum virtualCollectionTypeEnum) {
		this.virtualCollectionTypeEnum = virtualCollectionTypeEnum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 5;
		StringBuilder builder = new StringBuilder();
		builder.append("VirtualCollection [");
		if (virtualCollectionTypeEnum != null) {
			builder.append("virtualCollectionTypeEnum=");
			builder.append(virtualCollectionTypeEnum);
			builder.append(", ");
		}
		if (parameters != null) {
			builder.append("parameters=");
			builder.append(toString(parameters.entrySet(), maxLen));
			builder.append(", ");
		}
		if (pagingStyle != null) {
			builder.append("pagingStyle=");
			builder.append(pagingStyle);
			builder.append(", ");
		}
		if (uniqueName != null) {
			builder.append("uniqueName=");
			builder.append(uniqueName);
			builder.append(", ");
		}
		if (description != null) {
			builder.append("description=");
			builder.append(description);
			builder.append(", ");
		}
		if (i18Name != null) {
			builder.append("i18Name=");
			builder.append(i18Name);
			builder.append(", ");
		}
		if (i18Description != null) {
			builder.append("i18Description=");
			builder.append(i18Description);
			builder.append(", ");
		}
		if (i18icon != null) {
			builder.append("i18icon=");
			builder.append(i18icon);
			builder.append(", ");
		}
		if (queryBody != null) {
			builder.append("queryBody=");
			builder.append(queryBody);
			builder.append(", ");
		}
		builder.append("canPersist=");
		builder.append(canPersist);
		builder.append("]");
		return builder.toString();
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
				&& i < maxLen; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the queryBody
	 */
	public String getQueryBody() {
		return queryBody;
	}

	/**
	 * @param queryBody
	 *            the queryBody to set
	 */
	public void setQueryBody(String queryBody) {
		this.queryBody = queryBody;
	}

	/**
	 * @return the canPersist
	 */
	public boolean isCanPersist() {
		return canPersist;
	}

	/**
	 * @param canPersist
	 *            the canPersist to set
	 */
	public void setCanPersist(boolean canPersist) {
		this.canPersist = canPersist;
	}

}
