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

	/**
	 * 
	 */
	public DefaultFileTemplateServiceImpl() {
	}

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public DefaultFileTemplateServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
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

		List<FileTemplate> fileTemplates = new ArrayList<FileTemplate>();

		FileTemplate template;

		/*
		 * .txt
		 */
		template = new FileTemplate();
		template.setI18nTemplateName("jargon.file.template.text");
		template.setInfoType("");
		template.setMimeType("text/plain");
		template.setTemplateName("txt");
		template.setTemplateUniqueIdentifier("default.file.template.text");
		fileTemplates.add(template);

		/*
		 * .xml
		 */
		template = new FileTemplate();
		template.setI18nTemplateName("jargon.file.template.xml");
		template.setInfoType("");
		template.setMimeType("application/xml");
		template.setTemplateName("xml");
		template.setTemplateUniqueIdentifier("default.file.template.xml");
		fileTemplates.add(template);

		/*
		 * .r (rule)
		 */
		template = new FileTemplate();
		template.setI18nTemplateName("jargon.file.template.rule");
		template.setInfoType("");
		template.setMimeType("application/irods-rule");
		template.setTemplateName("xml");
		template.setTemplateUniqueIdentifier("default.file.template.rule");
		fileTemplates.add(template);

		return fileTemplates;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.filetemplate.FileTemplateService#createFileBasedOnTemplate
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public TemplateCreatedFile createFileBasedOnTemplate(String parentPath,
			String fileName, String templateUniqueIdentifier)
			throws DuplicateDataException, FileTemplateNotFoundException,
			FileTemplateException {
		return null;
	}

}
