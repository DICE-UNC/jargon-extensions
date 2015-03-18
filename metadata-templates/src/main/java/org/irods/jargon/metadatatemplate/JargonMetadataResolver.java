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
import org.irods.jargon.metadatatemplatesif.MetadataTemplate;
import org.irods.jargon.metadatatemplatesif.MetadataTemplateFileFilter;
import org.irods.jargon.metadatatemplatesif.TemplateParserSingleton;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFileImpl;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
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
	public List<MetadataTemplate> listTemplatesAssociatedWithIrodsHierarchyForPath(
			String absolutePath) {
		log.info("listTemplatesAssociatedWithIrodsHierarchyForPath");
		List<MetadataTemplate> templateList = null;
		File[] templateFiles = {};

		try {
			templateFiles = dotIrodsService
					.listFilesInDirectoryHierarchyDotIrods(absolutePath);
		} catch (FileNotFoundException fnfe) {

		} catch (JargonException je) {

		}
		
		try {
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException je) {
			
		} catch (IOException ioe) {
			
		}

		return templateList;
	}

	public int howManyTemplatesInUserHome(String userName) {
		return listTemplatesInUserHome(userName).size();
	}

	public int return42() {
		return 42;
	}
	
	public List<MetadataTemplate> userHomeTest(String userName) {
		List<MetadataTemplate> templateList = null;
		File[] templateFiles = {};

		try {
			templateFiles = dotIrodsService.listFilesOfTypeInDotIrodsUserHome(
					userName, new MetadataTemplateFileFilter());
		} catch (FileNotFoundException fnfe) {

		} catch (JargonException je) {

		}
		
		try {
			templateList = processFilesToMetadataTemplates(templateFiles);
		} catch (JargonException je) {
			
		} catch (IOException ioe) {
			
		}

		return templateList;		
	}

	public List<MetadataTemplate> listTemplatesInUserHomeUsingDotIrodsService(
			String userName) {
		List<MetadataTemplate> templateList = new ArrayList<MetadataTemplate>();
		File[] templateFiles = {};

		try {
			templateFiles = dotIrodsService.listFilesOfTypeInDotIrodsUserHome(
					userName, new MetadataTemplateFileFilter());
		} catch (FileNotFoundException fnfe) {

		} catch (JargonException je) {

		}

		for (File f : templateFiles) {
			IRODSFileInputStream fis = null;
			byte[] b = null;

			try {
				fis = irodsFileFactory
						.instanceIRODSFileInputStream((IRODSFileImpl) f);
			} catch (JargonException je) {
				log.info("Jargon exception when opening file {} for input",
						f.getName());
				continue;
			}

			try {
				b = new byte[fis.available()];
			} catch (IOException ioe) {
				log.info("IO exception when checking file size for file {}",
						f.getName());
				continue;
			}

			log.info("Size of byte array: {}", b.length);

			try {
				fis.read(b);
			} catch (IOException ioe) {
				log.info("IO exception when reading file {}", f.getName());
				continue;
			}

			String decoded = "";
			try {
				decoded = new String(b, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.info("ERROR: UnsupportedEncodingException");
			}
			log.info("Decoded string rep of byte array:\n{}", decoded);

			templateList.add(parser.createMetadataTemplateFromJSON(decoded)
					.deepCopy());

			log.info(templateList.get(0).getName());

			log.info("IN RESOLVER LOOP, list contains {} elements:",
					templateList.size());

			try {
				fis.close();
			} catch (IOException ioe) {
				log.info("IO exception when closing file input stream {}",
						f.getName());
				continue;
			}
		}

		log.info("AT END OF RESOLVER, list contains {} elements:",
				templateList.size());
		log.info("AT END OF RESOLVER, first template is\n{}",
				templateList.get(0).toString());

		return templateList;
	}

	@Override
	public List<MetadataTemplate> listTemplatesInUserHome(String userName) {
		DotIrodsCollection userHome = null;
		IRODSFile userHomeAsFile = null;
		String userHomePath = "";
		List<MetadataTemplate> templateList = new ArrayList<MetadataTemplate>();

		try {
			userHome = dotIrodsService.findUserHomeCollection(userName);
		} catch (FileNotFoundException fnfe) {
			log.info("User home directory not found");
			return templateList;
		} catch (JargonException je) {
			log.info("Jargon exception when finding user home collection");
			return templateList;
		}

		userHomePath = userHome.getAbsolutePath();

		try {
			userHomeAsFile = irodsFileFactory.instanceIRODSFile(userHomePath);
		} catch (JargonException je) {
			log.info("Jargon exception when retrieving userHome as an IRODS file");
			return templateList;
		}

		File[] templateFiles = userHomeAsFile
				.listFiles(new MetadataTemplateFileFilter());

		for (File f : templateFiles) {
			IRODSFileInputStream fis = null;
			byte[] b = null;

			try {
				fis = irodsFileFactory
						.instanceIRODSFileInputStream((IRODSFileImpl) f);
			} catch (JargonException je) {
				log.info("Jargon exception when opening file {} for input",
						f.getName());
				continue;
			}

			try {
				b = new byte[fis.available()];
			} catch (IOException ioe) {
				log.info("IO exception when checking file size for file {}",
						f.getName());
				continue;
			}

			log.info("Size of byte array: {}", b.length);

			try {
				fis.read(b);
			} catch (IOException ioe) {
				log.info("IO exception when reading file {}", f.getName());
				continue;
			}

			String decoded = "";
			try {
				decoded = new String(b, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.info("ERROR: UnsupportedEncodingException");
			}
			log.info("Decoded string rep of byte array:\n{}", decoded);

			// templateList.add(parser.createMetadataTemplateFromJSON(b));
			// templateList.add(parser.createMetadataTemplateFromJSON(fis));
			templateList.add(parser.createMetadataTemplateFromJSON(decoded)
					.deepCopy());

			log.info(templateList.get(0).getName());

			log.info("IN RESOLVER LOOP, list contains {} elements:",
					templateList.size());

			try {
				fis.close();
			} catch (IOException ioe) {
				log.info("IO exception when closing file input stream {}",
						f.getName());
				continue;
			}
		}

		log.info("AT END OF RESOLVER, list contains {} elements:",
				templateList.size());
		log.info("AT END OF RESOLVER, first template is\n{}",
				templateList.get(0).toString());

		return templateList;
	}

	@Override
	public void save(MetadataTemplate metadataTemplate) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String uniqueName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public MetadataTemplate findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetadataTemplate findByFqName(String fqName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetadataTemplate findByUUID(UUID uuid) {
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

		b = new byte[fis.available()];

		log.info("Size of byte array: {}", b.length);

		fis.read(b);

		String decoded = new String(b, "UTF-8");

		log.info("Decoded string rep of byte array:\n{}", decoded);

		return parser.createMetadataTemplateFromJSON(decoded);
	}

	private List<MetadataTemplate> processFilesToMetadataTemplates(File[] inFileArray)
			throws JargonException, IOException {
		log.info("processFilesToMetadataTemplates()");
		List<MetadataTemplate> returnList = new ArrayList<MetadataTemplate>();
		for (File f : inFileArray)
			returnList.add(processFileToMetadataTemplate(f).deepCopy());

		return returnList;
	}

	// public static String computePublicDirectory(final IRODSAccount
	// irodsAccount) {
}
