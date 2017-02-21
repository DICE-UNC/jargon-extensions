package org.irods.jargon.metadatatemplate;

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
