/**
 * 
 */
package org.irods.jargon.filetemplate.impl;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.filetemplate.FileTemplate;
import org.irods.jargon.filetemplate.FileTemplateService;
import org.irods.jargon.filetemplate.TemplateCreatedFile;
import org.irods.jargon.filetemplate.exception.FileTemplateException;
import org.irods.jargon.filetemplate.exception.FileTemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link FileTemplateService} that uses classpath
 * resources to store a small set of templates
 * 
 * @author Mike Conway - DICE
 *
 */
public class DefaultFileTemplateServiceImpl extends FileTemplateService {

	public static final Logger log = LoggerFactory
			.getLogger(DefaultFileTemplateServiceImpl.class);
	private final FileTemplateRepository fileTemplateRepository = new FileTemplateRepository();
	private final FileCreatorFactory fileCreatorFactory;

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public DefaultFileTemplateServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
		this.fileCreatorFactory = new FileCreatorFactory(
				irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.filetemplate.FileTemplateService#listAvailableFileTemplates
	 * ()
	 */
	@Override
	public List<FileTemplate> listAvailableFileTemplates()
			throws FileTemplateException {

		log.info("listAvailableFileTemplates()");
		return new ArrayList<FileTemplate>(fileTemplateRepository
				.getFileTemplates().values());

	}

	@Override
	protected TemplateCreatedFile createFileBasedOnTemplate(
			FileTemplate fileTemplate, String parentPath, String fileName)
			throws DuplicateDataException, FileTemplateException {

		log.info("createFileBasedOnTemplate()");
		if (fileTemplate == null) {
			throw new IllegalArgumentException("null fileTemplate");
		}

		if (parentPath == null || parentPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty parentPath");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		log.info("fileTemplate:{}", fileTemplate);
		log.info("parentPath:{}", parentPath);
		log.info("fileName:{}", fileName);

		log.info("getting creator for fileTemplate...");
		FileCreator fileCreator = this.getFileCreatorFactory()
				.instanceCreatorForFileTemplate(fileTemplate);
		log.info("have creator...make file");
		return fileCreator.create(parentPath, fileName);

	}

	@Override
	protected FileTemplate retrieveTemplateByUniqueName(
			String templateUniqueIdentifier)
			throws FileTemplateNotFoundException, FileTemplateException {

		if (templateUniqueIdentifier == null
				|| templateUniqueIdentifier.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty templateUniqueIdentifier");
		}
		FileTemplate fileTemplate = fileTemplateRepository.getFileTemplates()
				.get(templateUniqueIdentifier);

		if (fileTemplate == null) {
			throw new FileTemplateNotFoundException("cannot find file template");
		}

		return fileTemplate;
	}

	/**
	 * @return the fileCreatorFactory
	 */
	public FileCreatorFactory getFileCreatorFactory() {
		return fileCreatorFactory;
	}

}
