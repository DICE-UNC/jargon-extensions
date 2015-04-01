package org.irods.jargon.metadatatemplate;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.irods.jargon.metadatatemplatesif.AbstractMetadataResolver;
import org.irods.jargon.metadatatemplatesif.FormBasedMetadataTemplate;
import org.irods.jargon.metadatatemplatesif.MetadataElement;
import org.irods.jargon.metadatatemplatesif.MetadataTemplate;
import org.irods.jargon.metadatatemplatesif.MetadataTemplateFileFilter;
import org.irods.jargon.metadatatemplatesif.TemplateParserSingleton;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.extensions.dotirods.DotIrodsCollection;
import org.irods.jargon.extensions.dotirods.DotIrodsService;
import org.irods.jargon.extensions.dotirods.DotIrodsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JargonMetadataResolver extends AbstractMetadataResolver {
	static Logger log = LoggerFactory.getLogger(IRODSFileFactoryImpl.class);

	TemplateParserSingleton parser = TemplateParserSingleton.PARSER;

	private IRODSFileSystem irodsFileSystem;
	private IRODSFileFactory irodsFileFactory;
	private IRODSAccount irodsAccount;
	private DataTransferOperations dto;
	private DotIrodsService dotIrodsService;

	public JargonMetadataResolver(IRODSAccount irodsAdminAccount)
			throws JargonException {
		irodsAccount = irodsAdminAccount;
		irodsFileSystem = IRODSFileSystem.instance();

		irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);

		dotIrodsService = new DotIrodsServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);

		// XXX Need to close the session
	}

	@Override
	public List<MetadataTemplate> listPublicTemplates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MetadataTemplate> listTemplatesInIrodsHierarchyAbovePath(
			String absolutePath) throws IOException {
		log.info("listTemplatesInIrodsHierarchyAbovePath");
		List<MetadataTemplate> templateList = null;
		File[] templateFiles = {};

		try {
			templateFiles = dotIrodsService
					.listFilesOfTypeInDirectoryHierarchyDotIrods(absolutePath,
							new MetadataTemplateFileFilter());
		} catch (JargonException je) {
			log.info("JargonException when listing files in directory");
			return templateList;
		}

		try {
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException je) {
			log.info("JargonException when processing metadata template files");
			return templateList;
		}

		return templateList;
	}

	public int howManyTemplatesInUserHome(String userName) throws IOException {
		return listTemplatesInUserHome(userName).size();
	}

	// This is probably the correct way to implement listTemplatesInUserHome (or
	// all others)
	public List<MetadataTemplate> listTemplatesInUserHome(String userName)
			throws IOException {
		List<MetadataTemplate> templateList = null;
		File[] templateFiles = {};

		try {
			templateFiles = dotIrodsService.listFilesOfTypeInDotIrodsUserHome(
					userName, new MetadataTemplateFileFilter());
		} catch (JargonException je) {
			log.info("JargonException when listing files in directory");
			return templateList;
		}

		try {
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException je) {
			log.info("JargonException when processing metadata template files");
			return templateList;
		}

		return templateList;
	}

	/**
	 * Save this metadata template to a JSON file for use later.
	 * 
	 * Here, the second argument <code>location</code>, is assumed to be a path
	 * to an irods collection. If it is a .irods collection, the file will be
	 * stored in that directory. Else, the metadata template will be stored in a
	 * .irods collection found or created under the given collection.
	 * 
	 * @param metadataTemplate
	 * @param location
	 *            a String containing an irods absolute path where a .irods
	 *            collection can be found or created.
	 */
	@Override
	public void saveTemplateAsJSON(MetadataTemplate metadataTemplate,
			String location) {
		log.info("saveTemplateAsJSON()");

		String dotIrodsLocation;

		if (location == null || location.isEmpty()) {
			throw new IllegalArgumentException("location is null or empty");
		}

		if (metadataTemplate == null) {
			throw new IllegalArgumentException("location is null or empty");
		}

		if (location.endsWith(".irods")) {
			log.info("location is already a .irods collection: {}", location);
			dotIrodsLocation = location;
		} else {
			boolean test = true;

			try {
				test = dotIrodsService
						.dotIrodsCollectionPresentInCollection(location);
			} catch (JargonException je) {
				log.info("JargonException when checking for .irods collection");
				log.info("Template NOT SAVED");
				return;
			}

		}

		// TODO Auto-generated method stub

	}

	@Override
	public void deleteTemplateByName(String uniqueName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteTemplateByUUID(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public MetadataTemplate findTemplateByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetadataTemplate findTemplateByFqName(String fqName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetadataTemplate findTemplateByUUID(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	private MetadataTemplate processFileToMetadataTemplate(File inFile)
			throws JargonException, IOException {
		log.info("processFileToMetadataTemplate()");

		IRODSFileInputStream fis = null;
		byte[] b = null;

		fis = irodsFileFactory
				.instanceIRODSFileInputStream((IRODSFileImpl) inFile);

		// If a template does not have a UUID assigned on opening, generate a
		// new one and apply it.
		//
		// By checking here, we already know that the File must exist due to
		// .instanceIRODSFileInputStream()
		//
		// By convention, a metadata template file must have a metadata
		// attribute TemplateUUID, where the value is the UUID as a string.
		IRODSGenQueryBuilder uuidQuery = new IRODSGenQueryBuilder(true,true,null);
		uuidQuery.addConditionAsGenQueryField(RodsGenQueryEnum.COL_META_DATA_ATTR_NAME, 
				QueryConditionOperators.EQUAL, "TemplateUUID");
		
		List<MetaDataAndDomainData> metadataList = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount)
				.findMetadataValuesForDataObject((IRODSFileImpl) inFile);
		
		List<MetaDataAndDomainData> metadataList = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount).find
				
				
		b = new byte[fis.available()];

		log.info("Size of byte array: {}", b.length);

		fis.read(b);

		String decoded = new String(b, "UTF-8");

		log.info("Decoded string rep of byte array:\n{}", decoded);

		return parser.createMetadataTemplateFromJSON(decoded);
	}

	private List<MetadataTemplate> processFilesToMetadataTemplates(
			File[] inFileArray) throws JargonException, IOException {
		log.info("processFilesToMetadataTemplates()");
		List<MetadataTemplate> returnList = new ArrayList<MetadataTemplate>();
		for (File f : inFileArray)
			returnList.add(processFileToMetadataTemplate(f).deepCopy());

		return returnList;
	}

	/**
	 * Populate metadata template from a list of AVUs
	 * 
	 * @param inTemplate
	 *            {@link MetadataMergeResult}
	 * @param avuList
	 *            List of {@link AvuData}
	 * 
	 * @return {@link MetadataMergeResult}, containing a
	 *         {@link MetadataTemplate} that was initialized as a copy of
	 *         <code>inTemplate</code> whose <code>elements</code>'
	 *         <code>currentValue</code>s have been populated using
	 *         <code>avuList</code>, and a List of {@link AvuData} that contains
	 *         all avus that were not matched
	 * 
	 */
	public MetadataMergeResult populateFormBasedMetadataTemplateWithAVUs(
			FormBasedMetadataTemplate inTemplate,
			List<MetaDataAndDomainData> avuList) {
		FormBasedMetadataTemplate template = inTemplate.deepCopy();
		List<MetaDataAndDomainData> orphans = new ArrayList<MetaDataAndDomainData>();
		boolean matched = false;

		for (MetaDataAndDomainData avu : avuList) {
			matched = false;

			for (MetadataElement me : template.getElements()) {
				if (me.getName().equalsIgnoreCase(avu.getAvuAttribute())) {
					me.setCurrentValue(avu.getAvuValue());
					matched = true;
					break;
				}
			}

			if (!matched) {
				orphans.add(avu);
			}
		}

		return new MetadataMergeResult(template, orphans);
	}

	// public static String computePublicDirectory(final IRODSAccount
	// irodsAccount) {
}
