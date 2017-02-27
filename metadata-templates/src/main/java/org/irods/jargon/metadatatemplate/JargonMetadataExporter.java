package org.irods.jargon.metadatatemplate;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.FileCatalogObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JargonMetadataExporter extends AbstractMetadataExporter {
	static private Logger log = LoggerFactory
			.getLogger(IRODSFileFactoryImpl.class);

	public JargonMetadataExporter() {
		super();
	}

	public JargonMetadataExporter(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	public void saveTemplateToSystemMetadataOnObject(MetadataTemplate template,
			String pathToObject) throws JargonException, FileNotFoundException {
		log.info("saveTemplateToSystemMetadataOnObject()");

		if (pathToObject == null || pathToObject.isEmpty()) {
			throw new IllegalArgumentException("pathToObject is null or empty");
		}

		if (template == null) {
			throw new IllegalArgumentException("metadata template is null");
		}

		IRODSFile irodsObject = irodsAccessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(pathToObject);

		if (!irodsObject.exists()) {
			throw new FileNotFoundException(
					"pathToObject does not resolve to an iRODS object");
		}
		/*
		 * FileCatalogObjectAO objectAO = null;
		 * 
		 * if (irodsObject.isFile()) { objectAO =
		 * irodsAccessObjectFactory.getDataObjectAO(irodsAccount); } else if
		 * (irodsObject.isDirectory()) { objectAO =
		 * irodsAccessObjectFactory.getCollectionAO(irodsAccount); } else {
		 * throw new IllegalArgumentException( "object at " + pathToObject +
		 * " is neither a data object nor a collection - the JargonMetadataResolver currently only supports these types of objects"
		 * ); }
		 */
		if (template.getType() == TemplateTypeEnum.FORM_BASED) {
			for (MetadataElement me : ((FormBasedMetadataTemplate) template)
					.getElements()) {
				this.saveElementToSystemMetadataOnObject(me, pathToObject);
				/*
				 * if (!me.getCurrentValue().isEmpty()) { for (String s :
				 * me.getCurrentValue()) { AvuData avuData = AvuData .instance(
				 * me.getName(), s,
				 * JargonMetadataTemplateConstants.AVU_UNIT_PREFIX +
				 * template.getUuid() .toString()); if (irodsObject.isFile()) {
				 * ((DataObjectAO) objectAO).addAVUMetadata( pathToObject,
				 * avuData); } else if (irodsObject.isDirectory()) {
				 * ((CollectionAO) objectAO).addAVUMetadata( pathToObject,
				 * avuData); } } }
				 */
			}
		} // TODO else if for different TemplateTypeEnum types
	}

	@Override
	public void saveElementToSystemMetadataOnObject(MetadataElement element,
			String pathToObject) throws JargonException, FileNotFoundException {
		log.info("saveTemplateToSystemMetadataOnObject()");

		if (pathToObject == null || pathToObject.isEmpty()) {
			throw new IllegalArgumentException("pathToObject is null or empty");
		}

		if (element == null) {
			throw new IllegalArgumentException("metadata element is null");
		}

		IRODSFile irodsObject = irodsAccessObjectFactory.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(pathToObject);

		if (!irodsObject.exists()) {
			throw new FileNotFoundException(
					"pathToObject does not resolve to an iRODS object");
		}

		FileCatalogObjectAO objectAO = null;

		if (irodsObject.isFile()) {
			objectAO = irodsAccessObjectFactory.getDataObjectAO(irodsAccount);
		} else if (irodsObject.isDirectory()) {
			objectAO = irodsAccessObjectFactory.getCollectionAO(irodsAccount);
		} else {
			throw new IllegalArgumentException(
					"object at "
							+ pathToObject
							+ " is neither a data object nor a collection - the JargonMetadataResolver currently only supports these types of objects");
		}

		// // Clear out "stale" metadata before adding new metadata
		// if (irodsObject.isFile()) {
		// List<MetaDataAndDomainData> metadataList = ((DataObjectAO) objectAO)
		// .findMetadataValuesForDataObject(pathToObject);
		// if (!metadataList.isEmpty()) {
		// ((DataObjectAO) objectAO)
		// .deleteAllAVUForDataObject(pathToObject);
		// }
		// } else if (irodsObject.isDirectory()) {
		// List<MetaDataAndDomainData> metadataList;
		// try {
		// metadataList = ((CollectionAO) objectAO)
		// .findMetadataValuesForCollection(pathToObject);
		// } catch (JargonQueryException e) {
		// log.error("query exception looking up data object:{}",
		// pathToObject, e);
		// throw new JargonException(e);
		// }
		// if (!metadataList.isEmpty()) {
		// ((CollectionAO) objectAO).deleteAllAVUMetadata(pathToObject);
		// }
		// }
		/*
		 * ((CollectionAO) objectAO).modifyAVUMetadata(absolutePath,
		 * currentAvuData, newAvuData);
		 * 
		 * ((CollectionAO)
		 * objectAO).findMetadataValuesByMetadataQueryForCollection(avuQuery,
		 * collectionAbsolutePath)
		 */

		/*
		 * if (object has avu with matching attribute) overwrite else add
		 */
		/*
		 * Check if the object already has an AVU with this attribute name If
		 * not, add. If so, delete, then add.
		 * 
		 * Need to delete then add to account for list types. (For example,
		 * replacing a list with a shorter list.)
		 */

		List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
		List<MetaDataAndDomainData> queryResult = null;

		try {
			queryElements.add(AVUQueryElement.instanceForValueQuery(
					AVUQueryElement.AVUQueryPart.ATTRIBUTE,
					QueryConditionOperators.EQUAL, element.getName()));

			if (irodsObject.isFile()) {
				queryResult = ((DataObjectAO) objectAO)
						.findMetadataValuesForDataObjectUsingAVUQuery(
								queryElements, pathToObject);
			} else if (irodsObject.isDirectory()) {
				queryResult = ((CollectionAO) objectAO)
						.findMetadataValuesByMetadataQueryForCollection(
								queryElements, pathToObject);
			}
		} catch (JargonQueryException e) {
			log.error(
					"Jargon query exception looking up AVUs on iRODS object:{}",
					pathToObject, e);
			throw new JargonException(e);
		}

		// Attribute already exists at least once on object
		// Need to create list of AVUs for deletion
		if (!queryResult.isEmpty()) {
			List<AvuData> oldAvuData = new ArrayList<AvuData>();
			for (MetaDataAndDomainData mdd : queryResult) {
				AvuData avuData = AvuData.instance(mdd.getAvuAttribute(),
						mdd.getAvuValue(), mdd.getAvuUnit());
				oldAvuData.add(avuData);
			}

			// Delete AVUs
			if (irodsObject.isFile()) {
				((DataObjectAO) objectAO).deleteBulkAVUMetadataFromDataObject(
						pathToObject, oldAvuData);
			} else if (irodsObject.isDirectory()) {
				((CollectionAO) objectAO).deleteBulkAVUMetadataFromCollection(
						pathToObject, oldAvuData);
			}
		}

		// Add new AVUs, if currentValue is not empty (don't create an AVU for
		// empty input)
		if (!element.getCurrentValue().isEmpty()) {
			for (String s : element.getCurrentValue()) {
				AvuData avuData = AvuData.instance(element.getName(), s,
						JargonMetadataTemplateConstants.AVU_UNIT_PREFIX
								+ element.getTemplateUuid().toString());
				if (irodsObject.isFile()) {
					((DataObjectAO) objectAO).addAVUMetadata(pathToObject,
							avuData);
				} else if (irodsObject.isDirectory()) {
					((CollectionAO) objectAO).addAVUMetadata(pathToObject,
							avuData);
				}
			}
		}
	}
}
